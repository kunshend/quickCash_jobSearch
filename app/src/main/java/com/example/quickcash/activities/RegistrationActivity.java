package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.quickcash.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quickcash.entities.User;
import com.example.quickcash.utilities.FirebaseCRUD;
import com.example.quickcash.utilities.Validator;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

/**
 * RegistrationActivity handles the user registration process for the QuickCash application.
 * This activity allows new users to create an account by providing their personal information,
 * email, password, and selecting their role (Employee or Employer).
 *
 * The activity is responsible for:
 * 1. Collecting user input for registration
 * 2. Validating the input data
 * 3. Creating a new user in Firebase Authentication
 * 4. Storing user profile information in Firebase Realtime Database
 * 5. Redirecting to the Login screen upon successful registration
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class RegistrationActivity extends AppCompatActivity {

    /** UI elements for user input */
    private EditText etEmail, etPassword, etUsername, etFirstName, etLastName;

    /** Role selection spinner */
    private Spinner spinnerRole;

    /** Register button */
    private Button btnRegister;

    /** Firebase Authentication instance */
    private FirebaseAuth mAuth;

    /** Firebase Database reference */
    private DatabaseReference mDatabase;

    /**
     * Initializes the activity, sets up UI components and Firebase instances.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeToolbar();

        initializeUIComponents();

        //Initialize database and firebase auth.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //onclick registration
        btnRegister.setOnClickListener(v -> {

            User user = createUserFromFields();
            String password = etPassword.getText().toString().trim();

            if(checkValidInputs(user, password)) {
                registerUser(user, password);
            }
        });
    }

    /**
     * Validates the user input data against the application requirements.
     *
     * @param user The User object created from input fields
     * @param password The password entered by the user
     * @return true if all inputs are valid, false otherwise
     */
    private boolean checkValidInputs(User user, String password){
        Validator validator = new Validator();
        if(!validator.checkValidEmail(user.getEmail())){
            Toast.makeText(this, "Please input a valid email.", Toast.LENGTH_LONG).show();
        } else if(!validator.checkValidPassword(password)){
            Toast.makeText(this, "Please choose a valid new password", Toast.LENGTH_LONG).show();
        } else if(validator.checkInputEmpty(user.getFirstName())){
            Toast.makeText(this, "Please input a first name", Toast.LENGTH_LONG).show();
        } else if(validator.checkInputEmpty(user.getLastName())){
            Toast.makeText(this, "Please input a last name", Toast.LENGTH_LONG).show();
        } else if(validator.checkInputEmpty(user.getUsername())){
            Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_LONG).show();
        } else{
            return true;
        }
        return false;
    }

    /**
     * Registers a new user with Firebase Authentication and Database.
     * Checks if the username already exists before proceeding with registration.
     *
     * @param user The User object to register
     * @param password The password for the new account
     */
    private void registerUser(User user, String password) {
        FirebaseCRUD firebaseCRUD = new FirebaseCRUD();

        if(!checkValidInputs(user, password)){
            return;
        }

        firebaseCRUD.readUser(user.getUsername(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(RegistrationActivity.this, "Username is already taken", Toast.LENGTH_SHORT).show();
                } else {
                    //Proceed with user registration using FirebaseCRUD
                    firebaseCRUD.registerUser(user, password,
                            authTask -> { //Authentication Listener
                                if (!authTask.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this, "Registration failed: " + Objects.requireNonNull(authTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            },
                            dbTask -> { //Database Listener
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Failed to save user data. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegistrationActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes the UI components and finds view references.
     */
    private void initializeUIComponents() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
    }

    /**
     * Initializes the toolbar with a back button and title.
     * Sets up navigation to the login screen.
     */
    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
            actionBar.setTitle("Register");
        }

        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Creates a User object from the input field values.
     *
     * @return User object containing registration information
     */
    private User createUserFromFields(){
        //Get field values
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        return new User(username, email, role, firstName, lastName);
    }

    /**
     * Sets the email field text.
     * Used for testing purposes.
     *
     * @param email Email text to set
     */
    public void setEmail(String email) {
        etEmail.setText(email);
    }

    /**
     * Sets the password field text.
     * Used for testing purposes.
     *
     * @param password Password text to set
     */
    public void setPassword(String password) {
        etPassword.setText(password);
    }

    /**
     * Sets the first name field text.
     * Used for testing purposes.
     *
     * @param firstName First name text to set
     */
    public void setFirstName(String firstName){etFirstName.setText(firstName);}

    /**
     * Sets the last name field text.
     * Used for testing purposes.
     *
     * @param lastName Last name text to set
     */
    public void setLastName(String lastName){etLastName.setText(lastName);}

    /**
     * Sets the username field text.
     * Used for testing purposes.
     *
     * @param username Username text to set
     */
    public void setUsername(String username) {
        etUsername.setText(username);
    }

    /**
     * Sets the role selection spinner position.
     * Used for testing purposes.
     *
     * @param position Position index to select
     */
    public void setRole(int position) {
        spinnerRole.setSelection(position);
    }

    /**
     * Programmatically clicks the register button.
     * Used for testing purposes.
     */
    public void clickRegisterButton() {
        btnRegister.performClick();
    }
}
