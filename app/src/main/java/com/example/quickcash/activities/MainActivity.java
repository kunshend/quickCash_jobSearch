package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.quickcash.R;
import com.google.firebase.FirebaseApp;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity serves as the entry point to the QuickCash application.
 * This activity checks for an existing user session and either redirects to the
 * dashboard for logged-in users or provides registration access for new users.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    /** Firebase Authentication instance */
    private FirebaseAuth mAuth;

    /**
     * Initializes the activity, sets up UI components and Firebase.
     * Checks if a user is already logged in.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Initialize Firebase
        FirebaseApp.initializeApp(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the Register button and set up its click listener
        Button registerButton = findViewById(R.id.btRegister);
        registerButton.setOnClickListener(view -> {
            //Open registration page
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish(); //Close main page
        });

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user login detected, please log in", Toast.LENGTH_SHORT).show();
            // TODO: Intent intent = new Intent(MainActivity.this, layin.class);
            // TODO: startActivity(intent);
            // TODO: finish();
        } else {
            System.out.println("username id layin：" + currentUser.getEmail());
        }

        // TODO: Need to change when login is finished, redirect to login activity
        // Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        // startActivity(intent);
        // finish(); // close MainActivity, stop return
    }
}