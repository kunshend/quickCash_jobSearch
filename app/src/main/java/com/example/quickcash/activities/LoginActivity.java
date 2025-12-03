package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quickcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * LoginActivity handles user authentication for the QuickCash application.
 * This activity provides functionality for users to log in with their email and password,
 * or navigate to the registration screen to create a new account.
 *
 * The activity is responsible for:
 * 1. Collecting user credentials (email and password)
 * 2. Validating the input
 * 3. Authenticating the user with Firebase Authentication
 * 4. Directing authenticated users to the dashboard
 * 5. Providing access to the registration screen for new users
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    /** UI elements for user input */
    private EditText etPassword, etEmail;

    /** UI buttons for login and registration */
    private Button btnLogin, btnRegister;

    /** Firebase Authentication instance */
    private FirebaseAuth mAuth;

    /** Firebase Database reference */
    private DatabaseReference mDatabase;

    /**
     * Initializes the activity, sets up UI components and Firebase instances.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Otherwise, it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        //Initialize database and firebase auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //onclick Login
        btnLogin.setOnClickListener(view -> loginUser());

        //onclick registration
        btnRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));
    }

    /**
     * Handles the user login process.
     * Validates user input and attempts to authenticate with Firebase.
     */
    private void loginUser() {
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // check email field is non empty
        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        // check password field is non empty
        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        // check if user has entered a valid email address
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signInWithEmail(email, password);
        } else {
            Toast.makeText(LoginActivity.this, "Enter a valid email", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Attempts to sign in the user with the provided email and password using Firebase Authentication.
     * On success, navigates to the DashboardActivity.
     * On failure, displays an error message.
     *
     * @param email The user's email address
     * @param password The user's password
     */
    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // successful login
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        // login unsuccessful
                        Toast.makeText(LoginActivity.this, "Login failed. Check your email/password", Toast.LENGTH_LONG).show();
                    }
                });
    }
}