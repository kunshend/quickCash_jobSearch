package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quickcash.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * ReviewApplicationActivity allows employers to review job applications and make decisions.
 * This activity displays the details of a specific application and provides options
 * to accept or reject the application.
 *
 * The activity is responsible for:
 * 1. Loading and displaying application details
 * 2. Allowing the employer to accept or reject an application
 * 3. Updating the application status in Firebase
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class ReviewApplicationActivity extends AppCompatActivity {

    /** Tag for logging purposes */
    private static final String TAG = "ReviewApplication";

    /** TextView for displaying the job name */
    private TextView jobTitle;

    /** TextView for displaying the applicant's email */
    private TextView applicantEmail;

    /** TextView for displaying the application message */
    private TextView applicationMessage;

    /** Button for accepting the application */
    private Button acceptButton;

    /** Button for rejecting the application */
    private Button rejectButton;

    /** ImageView for returning to the applications list */
    private ImageView backButton;

    /** The ID of the application being reviewed */
    private String applicationId;

    /** The name of the job being applied for */
    private String jobName;

    /** Firebase database reference */
    private DatabaseReference databaseReference;

    /**
     * Initializes the activity, sets up UI components and loads application data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_application);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        jobTitle = findViewById(R.id.jobTitle);
        applicantEmail = findViewById(R.id.applicantEmail);
        applicationMessage = findViewById(R.id.applicationMessage);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);
        backButton = findViewById(R.id.backButton);

        // Get application ID from intent
        applicationId = getIntent().getStringExtra("id");
        // Try to get job name from intent (from our updated ViewApplications)
        jobName = getIntent().getStringExtra("jobName");

        if (applicationId == null) {
            Toast.makeText(this, "Error: Application ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up button click listeners
        setupButtons();

        // Load application details
        loadApplicationDetails();
    }

    /**
     * Sets up button click listeners for accept, reject, and back buttons.
     */
    private void setupButtons() {
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateApplicationStatus("accepted");
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateApplicationStatus("rejected");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });
    }

    /**
     * Loads application details from Firebase based on the application ID.
     * Updates UI elements with the application information.
     */
    private void loadApplicationDetails() {
        DatabaseReference applicationRef = databaseReference.child("applications").child(applicationId);
        applicationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ReviewApplicationActivity.this, "Application not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Get application data
                String email = dataSnapshot.child("email").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                String jobId = dataSnapshot.child("jobId").getValue(String.class);

                // Update UI with application details
                if (email != null) {
                    applicantEmail.setText("From: " + email);
                }

                if (message != null) {
                    applicationMessage.setText(message);
                }

                // If we already have the job name from the intent, use it
                if (jobName != null && !jobName.isEmpty()) {
                    jobTitle.setText("Job: " + jobName);
                } else if (jobId != null) {
                    // Otherwise, look up the job name using the job ID
                    loadJobName(jobId);
                } else {
                    jobTitle.setText("Unknown Job");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading application: " + databaseError.getMessage());
                Toast.makeText(ReviewApplicationActivity.this, "Error loading application details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Loads the job name from Firebase based on the job ID.
     *
     * @param jobId The ID of the job to look up
     */
    private void loadJobName(String jobId) {
        DatabaseReference jobRef = databaseReference.child("jobs").child(jobId);
        jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null) {
                        jobName = name;
                        jobTitle.setText("Job: " + name);
                    } else {
                        jobTitle.setText("Job ID: " + jobId);
                    }
                } else {
                    jobTitle.setText("Job ID: " + jobId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading job: " + databaseError.getMessage());
                jobTitle.setText("Job ID: " + jobId);
            }
        });
    }

    /**
     * Updates the application status in Firebase.
     * Changes the status to either "accepted" or "rejected" and returns to the applications list.
     *
     * @param status The new status for the application ("accepted" or "rejected")
     */
    private void updateApplicationStatus(String status) {
        DatabaseReference applicationRef = databaseReference.child("applications").child(applicationId).child("status");
        applicationRef.setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ReviewApplicationActivity.this,
                            "Application " + status, Toast.LENGTH_SHORT).show();

                    // Get the employer email from the Intent
                    String employerEmail = getIntent().getStringExtra("userEmail");

                    // Navigate back to ViewApplications
                    Intent intent = new Intent(ReviewApplicationActivity.this, ViewApplications.class);
                    // Clear the back stack to prevent multiple instances
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // Pass the employer email to ensure applications load correctly
                    intent.putExtra("userEmail", employerEmail);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReviewApplicationActivity.this,
                            "Failed to update application: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}