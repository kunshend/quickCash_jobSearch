package com.example.quickcash.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;
import com.example.quickcash.entities.ApplicationData;
import com.example.quickcash.entities.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * MyJobsDetailsActivity
 *
 * Displays detailed information for an accepted job (job in progress).
 *
 * Shows job name, address, application status, job status, and the user's application message.
 * If the application was accepted, a "Complete" button allows the user to mark the job as completed.
 * Once clicked, the button is disabled.
 *
 * @author Ethan Pancura
 */
public class MyJobsDetailsActivity extends AppCompatActivity {

    private TextView jobTitleTextView, jobStatusTextView, appStatusTextView, appMessageTextView;
    private Button completeButton;
    private String jobId;
    private String userEmail;

    private DatabaseReference jobsRef, applicationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_jobs_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Job Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        jobTitleTextView = findViewById(R.id.jobTitleTextView);
        jobStatusTextView = findViewById(R.id.jobStatusTextView);
        appStatusTextView = findViewById(R.id.appStatusTextView);
        appMessageTextView = findViewById(R.id.appMessageTextView);
        completeButton = findViewById(R.id.completeButton);

        jobId = getIntent().getStringExtra("jobId");
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        applicationsRef = FirebaseDatabase.getInstance().getReference("applications");

        loadJobAndApplicationDetails();
    }

    /**
     * Loads job details and the user's application details.
     * Displays all relevant info and controls the "Complete" button visibility.
     */
    private void loadJobAndApplicationDetails() {
        //load job info
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Job job = snapshot.getValue(Job.class);
                if (job != null) {
                    jobTitleTextView.setText(job.getName());
                    jobStatusTextView.setText("Job Status: " + job.getStatus());

                    if ("Completed".equalsIgnoreCase(job.getStatus())) {
                        disableButton("Already Completed");
                    }

                    getAddressFromLatLng(job.getLatitude(), job.getLongitude());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyJobsDetailsActivity.this, "Failed to load job", Toast.LENGTH_SHORT).show();
            }
        });

        //load application info
        applicationsRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ApplicationData app = data.getValue(ApplicationData.class);
                            if (app != null && jobId.equals(app.getJobId())) {
                                appStatusTextView.setText("Application Status: " + app.getStatus());
                                appMessageTextView.setText("Message: " + app.getMessage());

                                if ("accepted".equalsIgnoreCase(app.getStatus())) {
                                    enableCompleteButton();
                                } else {
                                    completeButton.setVisibility(View.GONE);
                                }
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyJobsDetailsActivity.this, "Failed to load application", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Android's Geocoder fetches a street address from latitude and longitude,
     * that way we can display a human-readable address and not garbage lat/lng
     *
     * @param lat The job's latitude
     * @param lng The job's longitude
     */
    private void getAddressFromLatLng(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                //full street address
                String addressText = address.getAddressLine(0);
                TextView addressTextView = findViewById(R.id.jobAddressTextView);
                addressTextView.setText("Address: " + addressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
        }
    }


    private void enableCompleteButton() {
        completeButton.setVisibility(View.VISIBLE);
        completeButton.setOnClickListener(v -> {
            jobsRef.child(jobId).child("status").setValue("Completed")
                    .addOnSuccessListener(aVoid -> {
                        disableButton("Completed");
                        Toast.makeText(MyJobsDetailsActivity.this, "Job marked as completed!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyJobsDetailsActivity.this, "Failed to update job status.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void disableButton(String text) {
        completeButton.setText(text);
        completeButton.setEnabled(false);
        completeButton.setAlpha(0.5f);
        completeButton.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

