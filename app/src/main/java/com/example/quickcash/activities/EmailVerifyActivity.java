package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;
import com.example.quickcash.utilities.Validator;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * EmailVerifyActivity handles the email verification process for password reset functionality.
 * This activity allows users to enter their email address to verify account ownership before
 * proceeding with password reset.
 *
 * The activity is responsible for:
 * 1. Collecting email input from the user
 * 2. Validating the email format
 * 3. Verifying the email exists in the database
 * 4. Redirecting to the password reset screen if verification is successful
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class EmailVerifyActivity extends AppCompatActivity implements View.OnClickListener {

    /** Firebase Database URL */
    private static final String DB_URL = "https://quickcash-ae34a-default-rtdb.firebaseio.com";

    /** Static Firebase database instance */
    private static FirebaseDatabase databaseInstance;

    /** Reference to users node in Firebase */
    private DatabaseReference userRef;

    /** Email input field */
    private EditText enterEmail;

    /** Submit button */
    private Button submitButton;

    /** Error message text view */
    private TextView errorMessage;

    /** Toolbar for navigation */
    private Toolbar toolbar;

    /** Input validator */
    private Validator validator;

    /**
     * Initializes the activity, sets up UI components and Firebase connections.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);

        this.initializeComponents();
        this.getDatabase();
        this.setupToolbar();

        userRef = databaseInstance.getReference("users");
    }

    /**
     * Initializes UI components and sets up click listeners.
     */
    private void initializeComponents() {
        // UI Elements
        toolbar = findViewById(R.id.toolbar);
        enterEmail = findViewById(R.id.editTextTextEmailAddress);
        submitButton = findViewById(R.id.submitButton);
        errorMessage = findViewById(R.id.notExistEmail);
        //Validator
        validator = new Validator();
        //Click Listener
        submitButton.setOnClickListener(this);
    }

    /**
     * Gets or initializes the Firebase database instance.
     *
     * @return FirebaseDatabase instance
     */
    protected FirebaseDatabase getDatabase() {
        // Initialize Firebase
        if (databaseInstance == null) {
            databaseInstance = FirebaseDatabase.getInstance(DB_URL);
        }
        return databaseInstance;
    }

    /**
     * Sets up the toolbar with a back button and title.
     * Configures navigation back to settings activity.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
            actionBar.setTitle("Password Reset");
        }
        //This part is return to setting page if click the back arrow.
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
    }

    /**
     * Gets the trimmed email text from the input field.
     *
     * @return Email text
     */
    public String getEmail(){
        return enterEmail.getText().toString().trim();
    }

    /**
     * Handles click events for the submit button.
     * Validates the email and checks if it exists in the database.
     *
     * @param view The view that was clicked
     */
    @Override
    public void onClick(View view) {
        String email = getEmail();

        if (validator.checkInputEmpty(email)) {
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }
        if (!validator.checkValidEmail(email)) {
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        Query emailQuery = userRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String email = getEmail();
                    Intent intent = new Intent(EmailVerifyActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("VALIDATED_EMAIL", email);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(EmailVerifyActivity.this, "Email not registered!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmailVerifyActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}