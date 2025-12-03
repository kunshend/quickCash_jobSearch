package com.example.quickcash.activities;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;
import com.example.quickcash.entities.Application;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.ApplicationCRUD;
import com.example.quickcash.utilities.Validator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * ApplicationActivity - Handles the job application process for users.
 * This activity allows users to submit an application message for a specific job.
 *
 * The activity is responsible for:
 * 1. Collecting user input (application message)
 * 2. Validating the input
 * 3. Creating and submitting an Application object to Firebase
 * 4. Redirecting the user back to the dashboard upon successful submission
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class ApplicationActivity extends AppCompatActivity {

    /** UI field for application message input */
    EditText messageField;

    /** Button to submit the application */
    Button submitButton;

    /** Utility class for application CRUD operations */
    ApplicationCRUD applicationCRUD;

    /** Firebase database instance */
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    /** Reference to applications node in Firebase */
    DatabaseReference databaseReference = database.getReference("applications");

    /**
     * Initializes the activity, sets up UI components and listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        // initialize views
        messageField = findViewById(R.id.message_field);
        submitButton = findViewById(R.id.submit_button);

        applicationCRUD = new ApplicationCRUD();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitApplication();
            }
        });
    }

    /**
     * Validates the input fields to ensure they meet requirements.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean verifyFields() {
        // validation
        Validator v1 = new Validator();
        if (v1.checkInputEmpty(messageField.getText().toString().trim())) {
            Toast.makeText(this, "Application message should not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Handles the process of submitting an application.
     * Validates fields first, then creates and submits the application object.
     * Upon successful submission, navigates back to the dashboard activity.
     */
    private void submitApplication() {
        // make sure fields are verified before proceeding
        if (verifyFields()) {
            Application submittedApplication = createApplicationFromFields();
            applicationCRUD.addNewApplication(submittedApplication);
            Toast.makeText(this, "Application Submitted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ApplicationActivity.this, DashboardActivity.class));
            finish();
        }
    }

    /**
     * Creates an Application object from user input fields and other data from the intent.
     *
     * @return An Application object containing all necessary information
     *
     *
     * Version 2.0
     * @author Ethan Pancura
     * While working on the applications list I noticed a bug in this method,
     * the comments had labeled it saying that an application was
     * receiving a job name when it was actually receving a jobID. The ID
     * was unused, which led to errors in use cases for applications.
     */
    private Application createApplicationFromFields() {
        //get data
        String jobId = getIntent().getStringExtra("jobId");
        String jobName = getIntent().getStringExtra("jobName");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String id = databaseReference.push().getKey();
        String message = messageField.getText().toString().trim();

        //build application
        Application application = new Application();
        application.setId(id);
        application.setJobId(jobId);
        application.setJobName(jobName);
        application.setEmail(email);
        application.setMessage(message);
        application.setStatus("open");

        return application;
    }
}