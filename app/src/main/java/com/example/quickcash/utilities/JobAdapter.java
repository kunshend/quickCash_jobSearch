package com.example.quickcash.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickcash.R;
import com.example.quickcash.activities.DashboardActivity;
import com.example.quickcash.activities.ViewApplications;
import com.example.quickcash.entities.Application;
import com.example.quickcash.entities.Job;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying job listings in a RecyclerView.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private Context context;
    private double userLatitude;
    private double userLongitude;
    private String currentUserEmail;
    private DatabaseReference applicationsRef;
    private JobClickListener clickListener;

    /**
     * Interface for job item click events.
     */
    public interface JobClickListener {
        void onJobClick(Job job);
    }

    /**
     * Constructor for JobAdapter.
     */
    public JobAdapter(List<Job> jobList, Context context, double userLatitude, double userLongitude) {
        this.jobList = jobList;
        this.context = context;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail().toLowerCase() : "";
        this.applicationsRef = FirebaseDatabase.getInstance().getReference("applications");
    }

    /**
     * Constructor with user email.
     */
    public JobAdapter(List<Job> jobList, Context context, double userLatitude, double userLongitude, String currentUserEmail) {
        this(jobList, context, userLatitude, userLongitude);
        if (currentUserEmail != null) {
            this.currentUserEmail = currentUserEmail.toLowerCase();
        }
    }

    /**
     * Constructor with click listener.
     */
    public JobAdapter(List<Job> jobList, Context context, double userLatitude, double userLongitude, JobClickListener clickListener) {
        this(jobList, context, userLatitude, userLongitude);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        // Set job title
        holder.jobTitleTextView.setText(job.getName());

        // Set job description
        holder.jobDescriptionTextView.setText(job.getDescription());

        // Set job category
        holder.jobCategoryTextView.setText("Category: " + job.getCategory());

        // Calculate and display distance if coordinates are available
        if (userLatitude != 0 && userLongitude != 0) {
            double distance = calculateDistance(
                    userLatitude, userLongitude,
                    job.getLatitude(), job.getLongitude());

            String distanceText = String.format(Locale.getDefault(), "%.1f km", distance);
            holder.jobDistanceTextView.setText(distanceText);
        } else {
            holder.jobDistanceTextView.setText("");
        }

        // Check if this job was posted by the current user
        boolean isOwnJob = job.getEmail() != null &&
                currentUserEmail != null &&
                job.getEmail().equalsIgnoreCase(currentUserEmail);

        // Get current user's role
        String currentUserRole = getCurrentUserRole(context);
        Log.d("JobAdapter", "Job: " + job.getName() + ", Current role: " + currentUserRole);
        Log.d("JobAdapter", "Job email: " + job.getEmail() + ", Current email: " + currentUserEmail);
        Log.d("JobAdapter", "Is own job? " + isOwnJob);

        // Set up the item click listener if provided
        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onJobClick(job));
            return; // Skip the rest of the method since we're using a custom click listener
        }

        // Handle card visuals and click behavior based on role and ownership
        final MaterialCardView cardViewFinal = (holder.itemView instanceof MaterialCardView)
                ? (MaterialCardView) holder.itemView : null;

        if ("Employee".equalsIgnoreCase(currentUserRole)) {
            // If we're an employee, can apply to other people's jobs
            if (isOwnJob) {
                // This is our own job posting - disable clicking, maybe style differently
                if (cardViewFinal != null) {
                    cardViewFinal.setStrokeColor(context.getResources().getColor(android.R.color.darker_gray));
                }
                holder.itemView.setClickable(false);
                Log.d("JobAdapter", "Own job - not clickable");
            } else {
                // Someone else's job - enable clicking depending on application status
                if (cardViewFinal != null) {
                    cardViewFinal.setStrokeColor(context.getResources().getColor(android.R.color.holo_blue_light));
                }
                holder.itemView.setClickable(false); // Temporarily disable until check is done

                DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("applications");

                applicationsRef.orderByChild("jobId").equalTo(job.getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean alreadyApplied = false;
                                final String[] applicationId = {null};
                                final String[] appStatus = {null};

                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    String email = snap.child("email").getValue(String.class);
                                    if (email != null && email.equalsIgnoreCase(currentUserEmail)) {
                                        alreadyApplied = true;
                                        appStatus[0] = snap.child("status").getValue(String.class);
                                        applicationId[0] = snap.getKey();
                                        break;
                                    }
                                }

                                if (alreadyApplied) {
                                    if ("accepted".equalsIgnoreCase(appStatus[0])) {
                                        // Job was accepted - allow employee to mark as completed
                                        holder.itemView.setClickable(true);
                                        holder.itemView.setAlpha(1f);
                                        holder.itemView.setOnClickListener(v -> {
                                            Toast.makeText(context, "This job has already been accepted. You can manage it from 'My Applications'", Toast.LENGTH_LONG).show();
                                        });

                                        Log.d("JobAdapter", "Accepted - allow completion");
                                    } else {
                                        // Applied but not accepted - disable click
                                        if (cardViewFinal != null) {
                                            cardViewFinal.setStrokeColor(context.getResources().getColor(android.R.color.darker_gray));
                                        }
                                        holder.itemView.setClickable(false);
                                        holder.itemView.setAlpha(0.6f);
                                        Log.d("JobAdapter", "Already applied - not clickable");
                                    }
                                } else {
                                    // Not yet applied - allow applying
                                    holder.itemView.setClickable(true);
                                    holder.itemView.setAlpha(1f);
                                    holder.itemView.setOnClickListener(v -> {
                                        Log.d("JobAdapter", "Job clicked: " + job.getName());
                                        showApplicationDialog(job);
                                    });
                                    Log.d("JobAdapter", "Not applied - can apply");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Error checking applications", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if ("Employer".equalsIgnoreCase(currentUserRole)) {
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> {
                Log.d("JobAdapter", "Employer clicked job: " + job.getName());
                Intent intent = new Intent(context, ViewApplications.class);
                intent.putExtra("userEmail", currentUserEmail);
                intent.putExtra("jobId", job.getId());
                context.startActivity(intent);
            });
        }
    }


    /**
     * Shows a dialog for job application.
     */
    private void showApplicationDialog(Job job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Apply for Job: " + job.getName());

        // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter your application message here...");
        input.setMinLines(3);
        input.setMaxLines(6);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String message = input.getText().toString().trim();
                if (!message.isEmpty()) {
                    submitApplication(job, message);
                } else {
                    Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Submits a job application to Firebase.
     */
    private void submitApplication(Job job, String message) {
        // Generate a new application ID
        String applicationId = applicationsRef.push().getKey();

        // Create an application object
        Application application = new Application(
                job.getId(),
                applicationId,
                message,
                currentUserEmail,
                "open"  // Default status for new applications
        );

        // Set the job name if available
        application.setJobName(job.getName());

        // Save to Firebase
        applicationsRef.child(applicationId).setValue(application)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to submit application: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    /**
     * ViewHolder class for job items.
     */
    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitleTextView;
        TextView jobDescriptionTextView;
        TextView jobCategoryTextView;
        TextView jobDistanceTextView;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.jobTitleTextView);
            jobDescriptionTextView = itemView.findViewById(R.id.jobDescriptionTextView);
            jobCategoryTextView = itemView.findViewById(R.id.jobCategoryTextView);
            jobDistanceTextView = itemView.findViewById(R.id.jobDistanceTextView);
        }
    }

    /**
     * Helper method to get current user role from context.
     */
    private String getCurrentUserRole(Context context) {
        if (context instanceof DashboardActivity) {
            return ((DashboardActivity) context).getCurrentUserRole();
        }
        return "";
    }

    /**
     * Calculates the distance between two geographic points using the Haversine formula.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }
}