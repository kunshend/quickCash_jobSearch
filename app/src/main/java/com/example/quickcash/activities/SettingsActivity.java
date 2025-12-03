// This activity handles app settings which includes changing roles and user logout functionality
package com.example.quickcash.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quickcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * SettingsActivity provides user account management functions including role switching,
 * password reset, and logout functionality.
 *
 * This activity allows users to:
 * 1. View their current role (Employee or Employer)
 * 2. Change their role after email verification
 * 3. Reset their password
 * 4. Log out of the application
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class SettingsActivity extends AppCompatActivity {
    /** Firebase authentication instance */
    private FirebaseAuth mAuth;

    /** Firebase database reference */
    private DatabaseReference mDatabase;

    /** UI element for displaying current role */
    private TextView currentRoleText;

    /** Store current user's role */
    private String currentUserRole;

    /** Store username */
    private String username;

    /**
     * Initializes the activity, sets up UI components and Firebase connections.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Add back button to action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        currentRoleText = findViewById(R.id.currentRoleText);
        Button changeRoleButton = findViewById(R.id.btnChangeRole);
        Button logoutButton = findViewById(R.id.btnLogout);
        Button resetPasswordButton = findViewById(R.id.btnResetPassword);
        initializeToolbar();

        // Setup activity
        loadUserData();
        setupRoleButton(changeRoleButton);
        setupLogoutButton(logoutButton);
        setupResetPasswordButton(resetPasswordButton);
    }

    /**
     * Loads user data from Firebase database.
     * Updates the UI with user role information.
     */
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail().toLowerCase();
            currentRoleText.setText("Loading...");

            mDatabase.child("users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                        String userEmail = userSnapshot.child("email").getValue(String.class);
                        if (userEmail != null && email.equals(userEmail.toLowerCase())) {
                            username = userSnapshot.getKey();
                            String role = userSnapshot.child("role").getValue(String.class);
                            updateRoleDisplay(role);
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * Updates the role display text in the UI.
     *
     * @param role The user's current role
     */
    private void updateRoleDisplay(String role) {
        currentUserRole = role;
        currentRoleText.setText("Current Role: " + role);
    }

    /**
     * Sets up the role change button with a click listener.
     *
     * @param changeRoleButton The button for changing roles
     */
    private void setupRoleButton(Button changeRoleButton) {
        changeRoleButton.setOnClickListener(v -> showRoleChangeDialog());
    }

    /**
     * Shows a dialog for selecting a new role.
     * Presents options for Employer or Employee roles.
     */
    private void showRoleChangeDialog() {
        String[] roles = {"Employer", "Employee"};
        new AlertDialog.Builder(this)
                .setTitle("Select Role")
                .setItems(roles, (dialog, which) -> {
                    String newRole = roles[which];
                    if (!newRole.equals(currentUserRole)) {
                        showEmailConfirmationDialog(newRole);
                    }
                })
                .show();
    }

    /**
     * Shows a dialog for email confirmation when changing roles.
     * Requires the user to verify their identity by re-entering their email.
     *
     * @param newRole The new role selected by the user
     */
    private void showEmailConfirmationDialog(String newRole) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_email_confirmation);

        EditText emailInput = dialog.findViewById(R.id.emailConfirmationEmail);
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        TextView messageText = dialog.findViewById(R.id.messageText);

        messageText.setText("You are switching to a " + newRole.toLowerCase() + ", please re-enter your email");

        confirmButton.setOnClickListener(v -> {
            String enteredEmail = emailInput.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                mDatabase.child("users").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailFound = false;
                        for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                            String userEmail = userSnapshot.child("email").getValue(String.class);
                            if (enteredEmail.equals(userEmail)) {
                                emailFound = true;
                                username = userSnapshot.getKey();
                                updateUserRole(newRole);
                                dialog.dismiss();
                                break;
                            }
                        }
                        if (!emailFound) {
                            Toast.makeText(SettingsActivity.this, "Incorrect email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Incorrect email. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Updates the user's role in the Firebase database.
     * Redirects to the dashboard with updated role upon success.
     *
     * @param newRole The new role to set for the user
     */
    private void updateUserRole(String newRole) {
        if (username != null) {
            mDatabase.child("users").child(username).child("role").setValue(newRole).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Role successfully changed to " + newRole, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to update role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Sets up the logout button with a click listener.
     * Shows a confirmation dialog before logging out.
     *
     * @param logoutButton The button for logging out
     */
    private void setupLogoutButton(Button logoutButton) {
        logoutButton.setOnClickListener(view -> showLogoutConfirmDialog());
    }

    /**
     * Shows a confirmation dialog before logging out.
     * Prevents accidental logout by requiring confirmation.
     */
    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(SettingsActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Initializes the toolbar with a back button and title.
     * Sets up navigation back to the dashboard.
     */
    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }

    /**
     * Sets up the reset password button with a click listener.
     * Navigates to the email verification screen to begin password reset.
     *
     * @param resetPasswordButton The button for resetting password
     */
    private void setupResetPasswordButton(Button resetPasswordButton) {
        resetPasswordButton.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, EmailVerifyActivity.class);
            startActivity(intent);
            finish();
        });
    }
}