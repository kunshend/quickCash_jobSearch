package com.example.quickcash.activities;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;
import com.example.quickcash.entities.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * JobDetailsActivity - Displays detailed job information fetched from Firebase,
 * including title, category, description, employer details, location, and job status.
 * Also integrates Google Maps for location visualization.
 */
public class JobDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap jobMap;
    private LatLng jobLocation; // Stores the job location for Google Maps

    // UI Components
    private TextView jobTitle, jobCategory, jobDescription;
    private TextView employerEmail, jobStatus, jobLatitude, jobLongitude, jobFirebaseId;
    private Button applyButton;
    private String jobId; // Job ID passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        // Initialize UI components
        jobTitle = findViewById(R.id.jobTitle);
        jobCategory = findViewById(R.id.jobCategory);
        jobDescription = findViewById(R.id.jobDescription);
        applyButton = findViewById(R.id.applyButton);

        // Additional fields
        employerEmail = findViewById(R.id.employerEmail);
        jobStatus = findViewById(R.id.jobStatus);
        jobLatitude = findViewById(R.id.jobLatitude);
        jobLongitude = findViewById(R.id.jobLongitude);
        jobFirebaseId = findViewById(R.id.jobFirebaseId);

        initToolbar(); // Initialize toolbar with back navigation

        // Retrieve the job ID passed from the previous activity
        jobId = getIntent().getStringExtra("jobId");

        //first, check to see if we've already applied to this job
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        checkIfAlreadyApplied(jobId, userEmail);


        if (jobId != null) {
            loadJobDetails(jobId); // Fetch job details from Firebase

            // Initialize Google Map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
        else {
            // Display error and close activity if jobId is invalid
            Toast.makeText(this, "Job details unavailable", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Handle "Apply Now" button click
        applyButton.setOnClickListener(v -> {
            Intent intent = new Intent(JobDetailsActivity.this, ApplicationActivity.class);
            intent.putExtra("jobId", jobId);
            intent.putExtra("jobName", jobTitle.getText().toString());
            startActivity(intent);
        });
    }

    /**
     * Fetches job details from Firebase using the provided job ID.
     */
    private void loadJobDetails(String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            Toast.makeText(this, "Job details unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId);

        jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(JobDetailsActivity.this, "Job not found in database", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Job job = snapshot.getValue(Job.class);
                if (job != null) {
                    // Populate UI with job details
                    jobTitle.setText(job.getName());
                    jobCategory.setText("Category: " + job.getCategory());
                    jobDescription.setText("Description: " + job.getDescription());

                    // Display additional job details from Firebase
                    employerEmail.setText("Email: " + (job.getEmail() != null ? job.getEmail() : "null"));
                    jobStatus.setText("Status: " + (job.getStatus() != null ? job.getStatus() : "null"));
                    jobLatitude.setText("Latitude: " + job.getLatitude());
                    jobLongitude.setText("Longitude: " + job.getLongitude());
                    jobFirebaseId.setText("Firebase ID: " + jobId);

                    // Initialize Google Maps marker
                    jobLocation = new LatLng(job.getLatitude(), job.getLongitude());
                    if (jobMap != null) {
                        jobMap.addMarker(new MarkerOptions().position(jobLocation).title(job.getName()));
                        jobMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jobLocation, 12));
                    }

                    // Hide "Apply Now" button if the job is closed
                    if ("closed".equalsIgnoreCase(job.getStatus())) {
                        applyButton.setVisibility(Button.GONE);
                    }

                } else {
                    Toast.makeText(JobDetailsActivity.this, "Failed to load job data", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JobDetailsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes the toolbar with a back button.
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.jobDetailsToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("View Job");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Callback when the Google Map is ready to use.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        jobMap = googleMap;
        if (jobLocation != null) {
            jobMap.addMarker(new MarkerOptions().position(jobLocation).title(jobTitle.getText().toString()));
            jobMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jobLocation, 12));
        }
    }

    /**
     * @author Ethan Pancura
     * This method queries the database for applications where
     * the asociated email matches the current user email, and the
     * associated ID matches the current job ID. If anything is found,
     * then the user has already applied to this job and the button
     * will be greyed out, not allowing duplicate applications.
     *
     * Built for US-1 of Iteration 3
     */
    private void checkIfAlreadyApplied(String jobId, String userEmail) {
        DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("applications");

        applicationsRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyApplied = false;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            String appliedJobId = data.child("jobId").getValue(String.class);
                            if (appliedJobId != null && appliedJobId.equals(jobId)) {
                                alreadyApplied = true;
                                break;
                            }
                        }

                        if (alreadyApplied) {
                            applyButton.setEnabled(false);
                            //if applied already, "grey out" the button
                            applyButton.setAlpha(0.5f);
                            applyButton.setText("Already Applied");
                        } else {
                            applyButton.setEnabled(true);
                            applyButton.setAlpha(1.0f);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error checking existing applications: " + error.getMessage());

                        //in case firebase access fails for some reason, just allow the application
                        applyButton.setEnabled(true);
                        applyButton.setAlpha(1.0f);
                    }
                });
    }

}
