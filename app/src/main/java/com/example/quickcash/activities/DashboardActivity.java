package com.example.quickcash.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.quickcash.R;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.DashboardState;
import com.example.quickcash.utilities.EmployeeDashboardState;
import com.example.quickcash.utilities.EmployerDashboardState;
import com.example.quickcash.utilities.FeaturesAdapter;
import com.example.quickcash.utilities.JobAdapter;
import com.example.quickcash.utilities.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;


import android.app.Dialog;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DashboardActivity serves as the main hub for the QuickCash application.
 * It allows users to switch between Employer and Employee roles, provides access to
 * role-specific features, and displays nearby job opportunities for employees.
 *
 * Key features include:
 * 1. Role-based UI and functionality for employers and employees
 * 2. Location-aware job discovery
 * 3. Feature navigation via recyclerview
 * 4. User data management and authentication
 *
 * @author QuickCash Team
 * @author Ross (location features)
 * @version 1.0
 */
public class DashboardActivity extends AppCompatActivity {
    /** Firebase authentication instance */
    private FirebaseAuth mAuth;

    /** Database reference for Firebase */
    private DatabaseReference mDatabase;

    /** UI elements for displaying user information */
    private TextView welcomeText, currentRoleText;

    /** RecyclerView for displaying available features */
    private RecyclerView featuresRecyclerView;

    /** Store current user's role */
    private String currentUserRole;

    /** Store username */
    private String username;

    /** Location services client for accessing device location */
    private FusedLocationProviderClient fusedLocationClient;

    /** Request code for location permission */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    /** User's current latitude */
    private double userLatitude = 0.0;

    /** User's current longitude */
    private double userLongitude = 0.0;

    /** Default radius for nearby jobs in kilometers */
    private static final double DEFAULT_NEARBY_RADIUS_KM = 25.0;

    /** RecyclerView for displaying nearby jobs */
    private RecyclerView nearbyJobsRecyclerView;

    /** Adapter for job listings */
    private JobAdapter jobAdapter;

    /** List of all jobs from database */
    private List<Job> allJobs = new ArrayList<>();

    /** TextView for displaying current location */
    private TextView currentLocationText;

    /** Dashboard state, used to determine UI layout based on user role */
    private DashboardState currentState;

    /**
     * Initializes the activity, sets up UI components, location services,
     * and loads user data from Firebase.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            // Initialize everything needed for the activity
            initializeViews();
            setupFirebase();
            // Added by Ross: Setup location services for nearby jobs feature
            setupLocation();

            // Initialize jobAdapter with empty list (will be populated later)
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userEmail = currentUser != null ? currentUser.getEmail().toLowerCase() : "";
            loadUserData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Initializes all UI elements and sets up component configurations.
     */
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        currentRoleText = findViewById(R.id.currentRoleText);
        featuresRecyclerView = findViewById(R.id.featuresRecyclerView);
        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Added: Current location text view
        currentLocationText = findViewById(R.id.currentLocationText);

        // Added by Ross: Setup nearby jobs RecyclerView
        nearbyJobsRecyclerView = findViewById(R.id.nearbyJobsRecyclerView);
        nearbyJobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Added by Ross: Initialize jobAdapter with empty list (will be populated later)
        jobAdapter = new JobAdapter(new ArrayList<>(), this, userLatitude, userLongitude);
        nearbyJobsRecyclerView.setAdapter(jobAdapter);


        // Setup settings button with click listener
        Button settingsButton = findViewById(R.id.btnSettings);
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
        });
    }

    /**
     * Initializes Firebase components including authentication and database.
     */
    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Sets up location services for nearby jobs feature.
     *
     * @author Ross
     */
    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
    }

    /**
     * Checks if location permission is granted, requests it if needed.
     *
     * @author Ross
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    /**
     * Handles the result of the location permission request.
     * If granted, gets the user's location; otherwise, uses a default location.
     *
     * @param requestCode The request code passed in requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     *
     * @author Ross
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied.Using default location.",
                        Toast.LENGTH_LONG).show();
                // Use a default location (Halifax, NS)
                userLatitude = 44.6356;
                userLongitude = -63.5957;

                // Update location display with default location
                updateLocationDisplay("Halifax, NS (Default)");

                // Don't call loadNearbyJobs() here directly
                // It will be called by updateUI when appropriate
            }
        }
    }

    /**
     * Gets the user's current location if permission is granted.
     * If location is unavailable, uses a default location (Halifax, NS).
     *
     * @author Ross
     */
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                userLatitude = location.getLatitude();
                                userLongitude = location.getLongitude();

                                // Get human-readable address from coordinates
                                getAddressFromLocation(location);
                            } else {
                                // If location is null, use default (Halifax, NS)
                                userLatitude = 44.6356;
                                userLongitude = -63.5957;

                                // Update location display with default location
                                updateLocationDisplay("Halifax, NS (Default)");
                            }
                            // Don't call loadNearbyJobs() here anymore
                        }
                    });
        }
    }

    /**
     * Converts location coordinates to a human-readable address.
     *
     * @param location The location to convert
     */
    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Format the address
                String locality = address.getLocality(); // city
                String adminArea = address.getAdminArea(); // state/province
                String countryCode = address.getCountryCode();

                StringBuilder addressText = new StringBuilder();

                if (locality != null) {
                    addressText.append(locality);
                }

                if (adminArea != null) {
                    if (addressText.length() > 0) {
                        addressText.append(", ");
                    }
                    addressText.append(adminArea);
                }

                if (countryCode != null && addressText.length() > 0) {
                    addressText.append(", ").append(countryCode);
                }

                if (addressText.length() == 0) {
                    // If we couldn't get a readable address, use coordinates
                    addressText.append(String.format(Locale.getDefault(),
                            "%.4f, %.4f", location.getLatitude(), location.getLongitude()));
                }

                updateLocationDisplay(addressText.toString());
            } else {
                // If no address found, display coordinates
                updateLocationDisplay(String.format(Locale.getDefault(),
                        "%.4f, %.4f", location.getLatitude(), location.getLongitude()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            // If geocoding fails, display coordinates
            updateLocationDisplay(String.format(Locale.getDefault(),
                    "%.4f, %.4f", location.getLatitude(), location.getLongitude()));
        }
    }

    /**
     * Updates the location display with the provided address text.
     * Makes the location display visible for Employee role users.
     *
     * @param addressText The formatted address to display
     */
    private void updateLocationDisplay(String addressText) {
        if (currentLocationText != null) {
            currentLocationText.setText("Current Location: " + addressText);

//            // Make sure the location is visible for Employee role
//            if ("Employee".equalsIgnoreCase(currentUserRole)) {
//                currentLocationText.setVisibility(View.VISIBLE);
//            }
        }
    }

    /**
     * Loads user data from Firebase database including username and role.
     * Updates the UI based on the retrieved data.
     *
     * Updated by Ethan Iteration3
     */
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail().toLowerCase();
            mDatabase.child("users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                        String userEmail = userSnapshot.child("email").getValue(String.class);
                        if (userEmail != null && email.equalsIgnoreCase(userEmail)) {
                            username = userSnapshot.getKey();
                            String role = userSnapshot.child("role").getValue(String.class);
                            updateUI(username, role);
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * Updates the UI with user information and features based on role.
     * Shows or hides nearby jobs section depending on user role.
     *
     * @param username The username to display in welcome message
     * @param role The user's role (Employee or Employer)
     *
     * Updated by Ethan Iteration 3 (Refactor for state design pattern)
     */
    private void updateUI(String username, String role) {
        currentUserRole = role;

        //depending on role, set the current state to initialize appropriate UI
        if ("Employee".equalsIgnoreCase(role)) {
            currentState = new EmployeeDashboardState();
        } else {
            currentState = new EmployerDashboardState();
        }

        updateFeaturesList(role);
        currentLocationText.setVisibility(View.VISIBLE);
        currentState.setupUI(this, username);
        currentState.loadJobs(this);
    }
    /**
     * Shows dialog for role selection.
     * Allows user to switch between Employer and Employee roles.
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
     * Loads nearby jobs from Firebase based on user location.
     * Filters jobs by distance and updates the nearby jobs display.
     * Excludes jobs posted by the current user when in Employee role.
     *
     * Updated by Ethan iteration 3 (state design pattern refactor)
     */

    public void loadNearbyJobs(boolean isEmployee) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String currentUserEmail = currentUser != null ? currentUser.getEmail().toLowerCase() : "";

        mDatabase.child("jobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allJobs.clear();
                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String jobEmail = jobSnapshot.child("email").getValue(String.class);
                    if (isEmployee && jobEmail != null && jobEmail.equalsIgnoreCase(currentUserEmail)) {
                        continue;
                    }

                    String name = jobSnapshot.child("name").getValue(String.class);
                    String description = jobSnapshot.child("description").getValue(String.class);
                    String category = jobSnapshot.child("category").getValue(String.class);
                    Double latitude = jobSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = jobSnapshot.child("longitude").getValue(Double.class);
                    String jobId = jobSnapshot.getKey();
                    String jobStatus = jobSnapshot.child("status").getValue(String.class);

                    if (name != null && description != null && category != null && latitude != null && longitude != null) {
                        Job job = new Job(name, description, category, latitude, longitude);
                        job.setEmail(jobEmail);
                        job.setId(jobId);
                        if (jobStatus != null) job.setStatus(jobStatus);
                        allJobs.add(job);
                    }
                }
                updateNearbyJobs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the list of nearby jobs based on distance from user.
     * Filters jobs using the LocationUtils helper class.
     *
     * @author Ross
     */
    private void updateNearbyJobs() {
        if (userLatitude != 0.0 && userLongitude != 0.0) {
            List<Job> nearbyJobs = LocationUtils.filterJobsByDistance(
                    allJobs, userLatitude, userLongitude, DEFAULT_NEARBY_RADIUS_KM);

            TextView nearbyJobsCount = findViewById(R.id.nearbyJobsCount);
            nearbyJobsCount.setText(nearbyJobs.size() + " jobs found nearby");

            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userEmail = currentUser != null ? currentUser.getEmail().toLowerCase() : "";

            // Unified click behavior depending on role
            jobAdapter = new JobAdapter(nearbyJobs, this, userLatitude, userLongitude, job -> {
                Log.d("DashboardActivity", "Job clicked: " + job.getName());

                if ("Employer".equalsIgnoreCase(currentUserRole)) {
                    Intent intent = new Intent(DashboardActivity.this, ViewApplications.class);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("jobId", job.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(DashboardActivity.this, JobDetailsActivity.class);
                    intent.putExtra("jobId", job.getId());
                    startActivity(intent);
                }
            });
            nearbyJobsRecyclerView.setAdapter(jobAdapter);

            Log.d("JobFilter", "Found " + nearbyJobs.size() + " nearby jobs after filtering");
        }
    }


    /**
     * Shows dialog for email confirmation when changing roles.
     * Requires user to re-enter their email for security.
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
                            Toast.makeText(DashboardActivity.this, "Incorrect email. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Incorrect email. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Updates user's role in the database and refreshes the UI.
     *
     * @param newRole The new role to assign to the user
     */
    private void updateUserRole(String newRole) {
        if (username != null) {
            mDatabase.child("users").child(username).child("role").setValue(newRole)
                    .addOnSuccessListener(aVoid -> {
                        currentUserRole = newRole;
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUI(username, newRole);  // Fixed: Using username instead of user.getEmail()
                        }
                        Toast.makeText(this, "Role updated successfully changed to " + newRole
                                , Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update role" + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Error: Username not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets list of features based on user role.
     *
     * @param role The user's role (Employee or Employer)
     * @return List of available features for the specified role
     */
    private List<String> getRoleFeatures(String role) {
        List<String> features = new ArrayList<>();
        if ("Employer".equalsIgnoreCase(role)) {
            features.add("Post Jobs");
            features.add("View Applications");

        } else if ("Employee".equalsIgnoreCase(role)) {
            features.add("Search Jobs");
            features.add("My Applications");
            features.add("My Jobs");
        }
        return features;
    }

    /**
     * Updates the features list in RecyclerView based on the user's role.
     *
     * @param role The user's role (Employee or Employer)
     */
    private void updateFeaturesList(String role) {
        List<String> features = getRoleFeatures(role);
        FeaturesAdapter adapter = new FeaturesAdapter(features, userLatitude, userLongitude);
        featuresRecyclerView.setAdapter(adapter);
    }

    /**
     * Getter method for the current user role.
     * Used by adapters that need to access the role.
     *
     * @return The current user's role (Employee or Employer)
     */
    public String getCurrentUserRole() {
        return currentUserRole;
    }
}