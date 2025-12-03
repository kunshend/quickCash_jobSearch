package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;

import com.example.quickcash.utilities.Validator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ResetPasswordActivity handles the password reset process for authenticated users.
 * This activity allows users to change their password after verifying their current password.
 *
 * The activity is responsible for:
 * 1. Collecting the current password for re-authentication
 * 2. Collecting and validating the new password
 * 3. Updating the password in Firebase Authentication
 * 4. Redirecting to login after successful password change
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class ResetPasswordActivity extends AppCompatActivity{
    /** Input field for current password */
    private EditText getCurrentPassword;

    /** Input field for new password */
    private EditText getNewPassword;

    /** Error message for invalid password */
    private TextView invalidPWDerror;

    /** Button to submit password reset */
    private Button submitButton;

    /** Toolbar for navigation */
    private Toolbar toolbar;

    /** Firebase Authentication instance */
    private FirebaseAuth firebaseAuth;

    /** Current Firebase user */
    private FirebaseUser currentUser;

    /**
     * Initializes the activity, sets up UI components and Firebase instances.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initializeComponents();
        setupToolbar();
        //Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    /**
     * Initializes UI components and sets up click listeners.
     */
    private void initializeComponents(){
        //UI Components
        toolbar = findViewById(R.id.toolbar);
        getCurrentPassword = findViewById(R.id.currentPWD);
        getNewPassword = findViewById(R.id.newPWD);
        submitButton = findViewById(R.id.submitButton);
        invalidPWDerror = findViewById(R.id.INVALID_PWD);
        //Utilities
        submitButton.setOnClickListener(v -> handlePasswordReset());
    }

    /**
     * Sets up the toolbar with a back button and title.
     * Configures navigation back to the email verification screen.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
            actionBar.setTitle("Reset Password");
        }
        //This part is return to setting page if click the back arrow.
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, EmailVerifyActivity.class));
            finish();
        });
    }

    /**
     * Handles the password reset process.
     * Validates input, checks if new password is different, and initiates re-authentication.
     */
    private void handlePasswordReset() {
        String currentPassword = getCurrentPassword.getText().toString().trim();
        String newPassword = getNewPassword.getText().toString().trim();

        // Input Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            invalidPWDerror.setVisibility(View.VISIBLE);
            //Hide the error message after 1 second
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidPWDerror.setVisibility(View.INVISIBLE);
                }
            }, 1000);
            return;
        }

        if (currentPassword.equals(newPassword)) {
            Toast.makeText(this, "New password cannot be the same as the old one!", Toast.LENGTH_SHORT).show();
            return;
        }

        Validator v1 = new Validator();
        if (!v1.checkValidPassword(newPassword)) {
            Toast.makeText(this, "Please choose a valid new password", Toast.LENGTH_LONG).show();
            return;
        }

        // Re-authenticate user with their current password
        reauthenticateUser(currentPassword, newPassword);
    }

    /**
     * Re-authenticates the user with their current password before allowing password change.
     * This is a Firebase security requirement for sensitive operations.
     *
     * @param currentPassword The user's current password for verification
     * @param newPassword The new password to set if re-authentication succeeds
     */
    private void reauthenticateUser(String currentPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If re-authentication is successful, update the password
                        updatePassword(newPassword);
                    } else {
                        // If the original password is incorrect
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Updates the user's password in Firebase Authentication.
     * Redirects to login screen after successful update.
     *
     * @param newPassword The new password to set for the user
     */
    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,"Password Reset Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}