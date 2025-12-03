package com.example.quickcash.activities;

import com.example.quickcash.R;
import com.example.quickcash.utilities.ApplicationAdapter;
import com.example.quickcash.entities.ApplicationData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Activity for employers to view and manage job applications for their posted jobs.
 * This activity displays all applications submitted to jobs created by the current user,
 * and allows them to accept or reject applications, as well as process payments for
 * completed jobs.
 */
public class ViewApplications extends AppCompatActivity implements ApplicationAdapter.OnApplicationClickListener {

    private static final String TAG = "ViewApplications";
    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<ApplicationData> applicationList = new ArrayList<>();
    private String employerEmail;
    private TextView titleText;
    // Declare the noApplicationsText TextView.
    private TextView noApplicationsText;

    /**
     * Initializes the activity, sets up the UI components and fetches data from Firebase.
     * Gets the employer email from the intent and uses it to find jobs posted by this employer,
     * then loads all applications for those jobs.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this contains the data it most recently
     *                          supplied in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_application_list);

        titleText = findViewById(R.id.applicationList);
        titleText.setText("Applications for My Jobs");
        // Initialize the noApplicationsText TextView.
        noApplicationsText = findViewById(R.id.noApplicationsText);

        // Get employer email from Intent.
        employerEmail = getIntent().getStringExtra("userEmail");
        if (employerEmail == null || employerEmail.isEmpty()) {
            Log.e(TAG, "Employer email is null or empty!");
            return;
        }

        Log.d(TAG, "Viewing applications for employer: " + employerEmail);

        setupToolbar();
        setupRecyclerView();

        // Get jobs posted by this employer, then get applications for those jobs.
        findJobsPostedByEmployer();
    }

    /**
     * Sets up the toolbar with a return button that navigates back to the dashboard.
     */
    private void setupToolbar() {
        ImageView returnButton = findViewById(R.id.returenButton);
        if (returnButton != null) {
            returnButton.setOnClickListener(v -> {
                Intent intent = new Intent(ViewApplications.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Initializes the RecyclerView with a LinearLayoutManager and sets up the adapter
     * for displaying application data.
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycledViewApplicationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(applicationList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Queries the Firebase database to find all jobs posted by the current employer.
     * For each job found, it stores the job ID and name for later use in fetching applications.
     * If no jobs are found, displays a message indicating there are no applications.
     */
    private void findJobsPostedByEmployer() {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> employerJobNames = new HashSet<>();
                Set<String> employerJobIds = new HashSet<>();
                Log.d(TAG, "Loaded " + snapshot.getChildrenCount() + " jobs from Firebase");

                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    String postedByEmail = jobSnapshot.child("email").getValue(String.class);
                    String jobName = jobSnapshot.child("name").getValue(String.class);
                    String jobId = jobSnapshot.getKey();
                    Log.d(TAG, "Checking job: " + jobName + " (ID: " + jobId + "), posted by: " + postedByEmail);
                    if (postedByEmail != null && jobName != null && postedByEmail.equals(employerEmail)) {
                        employerJobNames.add(jobName);
                        employerJobIds.add(jobId);
                        Log.d(TAG, "Found job posted by employer: " + jobName + " (ID: " + jobId + ")");
                    }
                }

                if (employerJobNames.isEmpty()) {
                    Log.d(TAG, "No jobs found posted by employer: " + employerEmail);
                    displayNoApplicationsMessage();
                } else {
                    Log.d(TAG, "Found " + employerJobNames.size() + " jobs posted by employer");
                    fetchApplicationsForJobs(employerJobIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching jobs: " + error.getMessage());
                displayNoApplicationsMessage();
            }
        });
    }

    /**
     * Fetches all applications from Firebase that are associated with the job IDs
     * posted by the current employer. Creates ApplicationData objects for each application
     * and adds them to the application list for display.
     *
     * @param jobIds Set of job IDs that were posted by the current employer
     */
    private void fetchApplicationsForJobs(Set<String> jobIds) {
        DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("applications");
        applicationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationList.clear();
                Log.d(TAG, "Applications data loaded, checking " + snapshot.getChildrenCount() + " applications");

                Map<String, String> jobIdToNameMap = new HashMap<>();
                Map<String, String> jobIdToStatusMap = new HashMap<>();
                DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
                jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot jobsSnapshot) {
                        for (DataSnapshot jobSnapshot : jobsSnapshot.getChildren()) {
                            String jobId = jobSnapshot.getKey();
                            String jobName = jobSnapshot.child("name").getValue(String.class);
                            String jobStatus = jobSnapshot.child("status").getValue(String.class);

                            if (jobId != null && jobIds.contains(jobId)) {
                                if (jobName != null) {
                                    jobIdToNameMap.put(jobId, jobName);
                                }
                                if (jobStatus != null) {
                                    jobIdToStatusMap.put(jobId, jobStatus);
                                }
                            }
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            try {
                                String applicationId = dataSnapshot.getKey();
                                String email = dataSnapshot.child("email").getValue(String.class);
                                String message = dataSnapshot.child("message").getValue(String.class);
                                String status = dataSnapshot.child("status").getValue(String.class);
                                String jobId = dataSnapshot.child("jobId").getValue(String.class);

                                if (status == null) {
                                    status = "open";
                                }

                                if (jobId != null && jobIds.contains(jobId)) {
                                    String jobName = jobIdToNameMap.containsKey(jobId) ? jobIdToNameMap.get(jobId) : "(Unknown Job)";
                                    ApplicationData application = new ApplicationData(applicationId, email, jobName, message);
                                    application.setStatus(status);
                                    application.setJobId(jobId);
                                    String jobStatus = jobIdToStatusMap.get(jobId);
                                    if (jobStatus != null) {
                                        application.setJobStatus(jobStatus);
                                    }
                                    applicationList.add(application);
                                    Log.d(TAG, "Added application to list: Job=" + jobName + " from " + email);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing application: " + e.getMessage(), e);
                            }
                        }

                        if (applicationList.isEmpty()) {
                            Log.d(TAG, "No applications found for employer's jobs");
                            displayNoApplicationsMessage();
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            noApplicationsText.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching job names: " + error.getMessage());
                        displayNoApplicationsMessage();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching applications: " + error.getMessage());
                displayNoApplicationsMessage();
            }
        });
    }

    /**
     * Displays a message indicating there are no applications to show.
     * Clears the application list, hides the RecyclerView, and shows a text message.
     */
    private void displayNoApplicationsMessage() {
        applicationList.clear();
        adapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.GONE);
        noApplicationsText.setText("No Applications");
        noApplicationsText.setVisibility(View.VISIBLE);
    }

    /**
     * Handles clicks on application items in the RecyclerView. Shows different dialogs
     * based on the application status and job status:
     * - For rejected applications: Shows a message that the application is already processed
     * - For accepted applications with incomplete jobs: Shows a message to wait for job completion
     * - For accepted applications with completed jobs: Shows a dialog to proceed to payment
     * - For open applications: Shows a dialog with application details and options to accept/reject
     *
     * @param application The ApplicationData object that was clicked
     */
    @Override
    public void onApplicationClick(ApplicationData application) {
        String appStatus = application.getStatus() != null ? application.getStatus().toLowerCase() : "open";
        String jobStatus = application.getJobStatus() != null ? application.getJobStatus().toLowerCase() : "";

        if (appStatus.equals("rejected")) {
            new AlertDialog.Builder(this)
                    .setTitle("Application Processed")
                    .setMessage("This application has been rejected and cannot be changed.")
                    .setPositiveButton("OK", null)
                    .show();
            return;

        } else if (appStatus.equals("accepted") && !jobStatus.equals("completed")) {
            new AlertDialog.Builder(this)
                    .setTitle("Job Not Yet Completed")
                    .setMessage("This application was accepted, but the job has not been marked as completed by the employee. Please wait before proceeding to payment.")
                    .setPositiveButton("OK", null)
                    .show();
            return;

        } else if (appStatus.equals("accepted") && jobStatus.equals("completed")) {
            new AlertDialog.Builder(this)
                    .setTitle("Job Completed")
                    .setMessage("The employee has marked this job as completed. You may proceed with payment.")
                    .setPositiveButton("Pay", (dialog, which) -> {
                        Intent intent = new Intent(this, CompleteAndPayActivity.class);
                        intent.putExtra("jobId", application.getJobId());
                        intent.putExtra("jobName", application.getJobName());
                        intent.putExtra("employeeEmail", application.getEmail());
                        intent.putExtra("applicationId", application.getId());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;

        } else if (appStatus.equals("completed")) {
            // Fallback, just in case application status is directly marked "completed"
            new AlertDialog.Builder(this)
                    .setTitle("Job Completed")
                    .setMessage("This application is marked as completed. You may proceed with payment.")
                    .setPositiveButton("Pay", (dialog, which) -> {
                        Intent intent = new Intent(this, CompleteAndPayActivity.class);
                        intent.putExtra("jobId", application.getJobId());
                        intent.putExtra("jobName", application.getJobName());
                        intent.putExtra("employeeEmail", application.getEmail());
                        intent.putExtra("applicationId", application.getId());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        // OPEN: Normal accept/reject workflow
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_applicant_details, null);

        TextView txtEmail = view.findViewById(R.id.txtEmail);
        TextView txtMessage = view.findViewById(R.id.txtMessage);
        TextView txtStatus = view.findViewById(R.id.txtStatus);

        txtEmail.setText("Email: " + application.getEmail());
        txtMessage.setText("Message: " + application.getMessage());
        txtStatus.setText("Status: " + application.getStatus());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Application Details");
        builder.setView(view);

        builder.setPositiveButton("Accept", (dialog, which) -> {
            FirebaseDatabase.getInstance().getReference("applications")
                    .child(application.getId())
                    .child("status")
                    .setValue("accepted")
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Application accepted", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Reject", (dialog, which) -> {
            FirebaseDatabase.getInstance().getReference("applications")
                    .child(application.getId())
                    .child("status")
                    .setValue("rejected")
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Application rejected", Toast.LENGTH_SHORT).show());
        });

        builder.show();
    }

}