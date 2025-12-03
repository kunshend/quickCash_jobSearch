package com.example.quickcash.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.quickcash.R;
import com.example.quickcash.entities.Job;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * MapActivity displays nearby job opportunities on a Google Map.
 * This activity obtains the user's current location and displays job markers within a specified radius.
 * Each marker represents a job posting, and users can interact with markers to view more details.
 *
 * The activity handles location permissions, Google Maps initialization, and Firebase database operations
 * to fetch and display jobs based on geographic proximity to the user.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    /** Tag for logging purposes */
    private static final String TAG = "MapActivity";

    /** Request code for location permission */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /** Default zoom level for the map */
    private static final float DEFAULT_ZOOM = 15f;

    /** Maximum distance (in kilometers) to display jobs from user's location */
    private static final double MAX_DISTANCE_KM = 10.0; // 10km radius

    /** Google Map instance */
    private GoogleMap googleMap;

    /** Client for accessing device location */
    private FusedLocationProviderClient fusedLocationClient;

    /** Flag indicating if location permission is granted */
    private boolean locationPermissionGranted = false;

    /** Last known location of the device */
    private Location lastKnownLocation;

    /** Current position of the user */
    private LatLng currentPosition;

    /** List of job markers displayed on the map */
    private List<Marker> jobMarkers = new ArrayList<>();

    /** Reference to jobs database in Firebase */
    private DatabaseReference jobsDatabase;

    /**
     * Initializes the activity, sets up the map, and checks for required permissions.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Check if Google Play Services is available
        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            // Google Play Services is not available
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Google Play Services not available: " + errorCode);
            return;
        }

        Log.d(TAG, "onCreate: Starting MapActivity");

        // Set up back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nearby Jobs");
        }

        // Initialize the location services client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firebase reference
        jobsDatabase = FirebaseDatabase.getInstance().getReference().child("jobs");
        Log.d(TAG, "Firebase reference initialized: " + jobsDatabase.toString());

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            Log.d(TAG, "Getting map async");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
        }

        // Get location permissions
        getLocationPermission();
    }

    /**
     * Called when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * @param map The GoogleMap object representing the initialized map
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "onMapReady: Map is ready");

        // Try setting different map types to see what works
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Enable additional UI controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Set initial camera position to Halifax (without adding a test marker)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(44.6488, -63.5752), DEFAULT_ZOOM));
        Log.d(TAG, "Moved camera to Halifax");

        // Update UI based on permission status
        updateLocationUI();

        // Get current location and update map
        getDeviceLocation();
    }

    /**
     * Checks and requests location permission if not already granted.
     *
     * @return true if permission is already granted, false if permission needs to be requested
     */
    public boolean getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Checking permissions");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d(TAG, "Location permission already granted");
            return true;
        } else {
            Log.d(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    /**
     * Handles the result of the permission request.
     * If permission is granted, updates the UI and gets the device location.
     *
     * @param requestCode The request code passed to requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted in callback");
                locationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation();
            } else {
                Log.d(TAG, "Permission denied in callback");
                Toast.makeText(this, "Location permission denied. Some features may not work properly.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Updates the map UI based on whether location permissions are granted.
     * Enables or disables location-related features on the map.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            Log.e(TAG, "updateLocationUI: Google Map is null");
            return;
        }
        try {
            if (locationPermissionGranted) {
                Log.d(TAG, "Enabling location features on map");
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                Log.d(TAG, "Disabling location features on map");
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the map UI with a specific location, centers the map on that location,
     * and loads nearby jobs.
     *
     * @param location The location to update the UI with
     */
    public void updateLocationUI(Location location) {
        if (googleMap == null || location == null) {
            Log.e(TAG, "updateLocationUI: Google Map or location is null");
            return;
        }

        Log.d(TAG, "Updating map with location: " + location.getLatitude() + ", " + location.getLongitude());
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        currentPosition = position;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));

        // Load and display nearby jobs
        loadNearbyJobs(location, this::displayJobMarkers);
    }

    /**
     * Gets the device's current location and updates the map accordingly.
     * If location cannot be determined, uses a default location.
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Log.d(TAG, "Getting device location");
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                Log.d(TAG, "Got location: " + location.getLatitude() + ", " + location.getLongitude());
                                lastKnownLocation = location;
                                updateLocationUI(location);
                            } else {
                                Log.d(TAG, "Current location is null, using default location");
                                // Use a default location instead
                                Location defaultLocation = new Location("");
                                defaultLocation.setLatitude(44.6488);  // Halifax
                                defaultLocation.setLongitude(-63.5752);
                                updateLocationUI(defaultLocation);
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            Log.e(TAG, "Exception: " + e.getMessage());
                            Toast.makeText(MapActivity.this, "Error getting location: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads nearby jobs from Firebase database based on user's location.
     *
     * @param location The user's current location
     * @param callback Consumer function to handle the list of jobs after loading
     */
    public void loadNearbyJobs(Location location, Consumer<List<Job>> callback) {
        if (location == null) {
            Log.e(TAG, "loadNearbyJobs: Location is null");
            callback.accept(new ArrayList<>());
            return;
        }

        Log.d(TAG, "Loading jobs near: " + location.getLatitude() + ", " + location.getLongitude());
        final List<Job> nearbyJobs = new ArrayList<>();

        jobsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Firebase data received: " + snapshot.getChildrenCount() + " items");

                if (snapshot.getChildrenCount() == 0) {
                    Log.w(TAG, "No jobs found in Firebase database!");
                    Toast.makeText(MapActivity.this, "No jobs found in database", Toast.LENGTH_SHORT).show();
                    callback.accept(new ArrayList<>());
                    return;
                }

                // Loop through each job entry
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    try {
                        // Log the raw data for debugging
                        Log.d(TAG, "Job data: " + jobSnapshot.getKey() + " -> " + jobSnapshot.getValue());

                        // Get job details from Firebase structure
                        String name = jobSnapshot.child("name").getValue(String.class);
                        String description = jobSnapshot.child("description").getValue(String.class);

                        // Try to get latitude and longitude first
                        Double latitude = jobSnapshot.child("Latitude").getValue(Double.class);
                        Double longitude = jobSnapshot.child("Longitude").getValue(Double.class);

                        // If latitude or longitude is null, check lowercase field names
                        if (latitude == null) {
                            latitude = jobSnapshot.child("latitude").getValue(Double.class);
                        }
                        if (longitude == null) {
                            longitude = jobSnapshot.child("longitude").getValue(Double.class);
                        }

                        // If still null, try to get location ID and convert to coordinates
                        if (latitude == null || longitude == null) {
                            Integer locationId = jobSnapshot.child("location").getValue(Integer.class);
                            if (locationId != null) {
                                // Convert location ID to coordinates
                                double[] coordinates = getCoordinatesForLocationId(locationId);
                                latitude = coordinates[0];
                                longitude = coordinates[1];
                                Log.d(TAG, "Converted location ID " + locationId + " to coordinates: " +
                                        latitude + "," + longitude);
                            }
                        }

                        if (name != null && latitude != null && longitude != null) {
                            Log.d(TAG, "Creating job object for: " + name + " at " + latitude + "," + longitude);

                            Job job = new Job();
                            job.setId(jobSnapshot.getKey());
                            job.setName(name);
                            job.setDescription(description != null ? description : "No description available");
                            job.setLatitude(latitude);
                            job.setLongitude(longitude);
                            job.setStatus("open");

                            nearbyJobs.add(job);
                            Log.d(TAG, "Added job to list: " + name);
                        } else {
                            Log.w(TAG, "Skipping job with missing data: " + jobSnapshot.getKey() +
                                    " Name: " + name + ", Lat: " + latitude + ", Lng: " + longitude);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing job " + jobSnapshot.getKey() + ": " + e.getMessage(), e);
                    }
                }

                Log.d(TAG, "Total jobs found: " + nearbyJobs.size());
                callback.accept(nearbyJobs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Toast.makeText(MapActivity.this,
                        "Failed to load jobs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                callback.accept(new ArrayList<>());
            }
        });
    }

    /**
     * Converts a location ID to geographic coordinates.
     *
     * @param locationId The location identifier
     * @return An array with latitude and longitude values
     */
    private double[] getCoordinatesForLocationId(int locationId) {
        // You would define mappings from location IDs to actual coordinates
        // This is just an example
        switch (locationId) {
            case 1:
                return new double[]{44.6488, -63.5752}; // Halifax
            case 2:
                return new double[]{45.5017, -73.5673}; // Montreal
            case 3:
                return new double[]{43.6532, -79.3832}; // Toronto
            default:
                // Return a default location or nearby location
                return new double[]{44.6488, -63.5752}; // Default to Halifax
        }
    }

    /**
     * Determines if a job is within the specified radius of the user's location.
     *
     * @param userLocation The user's current location
     * @param job The job to check
     * @return true if the job is nearby and open, false otherwise
     */
    private boolean isJobNearby(Location userLocation, Job job) {
        if ("open".equals(job.getStatus())) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    job.getLatitude(), job.getLongitude(),
                    results);

            // Convert meters to kilometers
            float distanceInKm = results[0] / 1000;

            return distanceInKm <= MAX_DISTANCE_KM;
        }
        return false;
    }

    /**
     * Displays markers on the map for each job in the provided list.
     *
     * @param jobs List of jobs to display as markers
     */
    public void displayJobMarkers(List<Job> jobs) {
        // Clear previous markers
        for (Marker marker : jobMarkers) {
            marker.remove();
        }
        jobMarkers.clear();

        Log.d(TAG, "Displaying " + jobs.size() + " job markers");

        if (jobs.isEmpty()) {
            Log.w(TAG, "No jobs to display on map!");
            Toast.makeText(this, "No nearby jobs found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add new markers
        for (Job job : jobs) {
            LatLng position = new LatLng(job.getLatitude(), job.getLongitude());
            Log.d(TAG, "Creating marker for " + job.getName() + " at " + position.latitude + "," + position.longitude);

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(job.getName())
                    .snippet(job.getDescription()));

            if (marker != null) {
                jobMarkers.add(marker);
                Log.d(TAG, "Successfully added marker for: " + job.getName());
            } else {
                Log.e(TAG, "Failed to add marker for: " + job.getName());
            }
        }

        Log.d(TAG, "Total markers on map: " + jobMarkers.size());
    }

    /**
     * Gets the current user position.
     *
     * @return LatLng object representing the current user position
     */
    public LatLng getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Gets the list of job markers on the map.
     *
     * @return List of Marker objects
     */
    public List<Marker> getJobMarkers() {
        return jobMarkers;
    }

    /**
     * Handles the up navigation action.
     *
     * @return true if the navigation was handled, false otherwise
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // This replaces the deprecated onBackPressed
        return true;
    }
}