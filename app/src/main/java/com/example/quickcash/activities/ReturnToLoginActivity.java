package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quickcash.R;
//import com.example.quickcash.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ReturnToLoginActivity provides a simple interface for users to return to the login screen.
 * This activity is typically shown after certain operations like account actions or errors
 * that require the user to re-authenticate.
 *
 * The activity logs the user out of Firebase Authentication and redirects to the login screen.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class ReturnToLoginActivity extends AppCompatActivity {

    /** Button to navigate back to login screen */
    private Button reLoginButton;

    /**
     * Initializes the activity and sets up the re-login button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_to_login);

        reLoginButton = findViewById(R.id.reLoginButton);

        reLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    /**
     * Signs the user out of Firebase Authentication and navigates to the login screen.
     * Sets flags to clear the activity stack so the user cannot navigate back.
     */
    private void navigateToLogin() {
        // Sign out the current user
        FirebaseAuth.getInstance().signOut();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}