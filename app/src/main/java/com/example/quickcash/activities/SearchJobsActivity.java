package com.example.quickcash.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickcash.R;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.JobAdapter;
import com.example.quickcash.utilities.LocationUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchJobsActivity extends AppCompatActivity {

    private EditText searchBar;
    private AutocompleteSupportFragment autocompleteFragment;
    private Button searchLocationButton;
    private Spinner categoryFilter;
    private RecyclerView jobRecyclerView;
    private JobAdapter jobAdapter;
    private double userLatitude;
    private double userLongitude;

    // Search location coordinates
    private double searchLatitude;
    private double searchLongitude;
    private boolean useSearchLocation = false;

    private List<Job> allJobs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_jobs);

        //initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA5NI3J5fhdeN_NDgXoAqLnJWVVcKRzSWg");
        }

        //get location
        userLatitude = getIntent().getDoubleExtra("userLatitude", 0.0);
        userLongitude = getIntent().getDoubleExtra("userLongitude", 0.0);

        if (userLatitude == 0.0 && userLongitude == 0.0) {
            //if location cant be found, use Halifax
            userLatitude = 44.6488;
            userLongitude = -63.5752;
        }

        //initialize search location with user location
        searchLatitude = userLatitude;
        searchLongitude = userLongitude;

        initializeToolbar();
        initializeUI();
        setupPlacesAutocomplete();
        loadJobList();
    }

    /**
     * Sets up the Places Autocomplete functionality
     */
    private void setupPlacesAutocomplete() {
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

            //set place type filter to cities
            autocompleteFragment.setTypeFilter(TypeFilter.CITIES);
            autocompleteFragment.setHint("Search location...");

            //PlaceSelectionListener to handle response
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        searchLatitude = latLng.latitude;
                        searchLongitude = latLng.longitude;
                        useSearchLocation = true;

                        String placeName = place.getName();
                        Toast.makeText(SearchJobsActivity.this,
                                "Searching near: " + placeName, Toast.LENGTH_SHORT).show();

                        //Re-filter jobs based on the selected location
                        filterJobs(searchBar.getText().toString().trim());
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(SearchJobsActivity.this,
                            "Place selection failed: " + status.getStatusMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Initializes the UI components for the search jobs activity.
     */
    private void initializeUI() {
        searchBar = findViewById(R.id.searchBar);
        searchLocationButton = findViewById(R.id.searchLocationButton);
        categoryFilter = findViewById(R.id.categoryFilter);
        jobRecyclerView = findViewById(R.id.jobRecyclerView);

        //hardcoded job categories (can be modified later)
        String[] categories = {"All", "Technology", "Hard Labour", "Marketing", "Retail", "Education"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilter.setAdapter(adapter);

        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter(new ArrayList<>(), this, userLatitude, userLongitude, job -> {
            if ("Employee".equalsIgnoreCase(getIntent().getStringExtra("currentUserRole"))) {
                Intent intent = new Intent(SearchJobsActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", job.getId());
                startActivity(intent);
            }
        });
        jobRecyclerView.setAdapter(jobAdapter);

        //button to hard-set location to user location
        searchLocationButton.setText("Use My Location");
        searchLocationButton.setOnClickListener(v -> {
            useSearchLocation = false;
            searchLatitude = userLatitude;
            searchLongitude = userLongitude;
            Toast.makeText(SearchJobsActivity.this, "Using your current location", Toast.LENGTH_SHORT).show();

            //clear the autocomplete
            if (autocompleteFragment != null) {
                autocompleteFragment.setText("");
            }

            filterJobs(searchBar.getText().toString().trim());
        });

        setupListeners();
    }

    /**
     * This method loads the jobs from Firebase and populates the job list.
     * Added filtering for jobs posted by the current user when in Employee role.
     */
    private void loadJobList() {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");

        // Get current user email for filtering
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserEmail = currentUser != null ? currentUser.getEmail().toLowerCase() : "";

        // Get current user role from intent
        String currentUserRole = getIntent().getStringExtra("currentUserRole");

        jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allJobs.clear();

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String jobId = jobSnapshot.getKey();  // Get the job ID
                    String jobName = jobSnapshot.child("name").getValue(String.class);
                    String jobDescription = jobSnapshot.child("description").getValue(String.class);
                    String jobCategory = jobSnapshot.child("category").getValue(String.class);
                    Double lat = jobSnapshot.child("latitude").getValue(Double.class);
                    Double lng = jobSnapshot.child("longitude").getValue(Double.class);
                    String jobEmail = jobSnapshot.child("email").getValue(String.class);

                    // Skip jobs posted by current user when in Employee role
                    if ("Employee".equalsIgnoreCase(currentUserRole) &&
                            jobEmail != null &&
                            jobEmail.equalsIgnoreCase(currentUserEmail)) {
                        Log.d("SearchJobs", "Skipping job posted by current user: " + jobName);
                        continue;
                    }

                    if (jobName != null && lat != null && lng != null) {
                        LatLng jobLocation = new LatLng(lat, lng);
                        Job job = new Job(jobName, jobDescription, jobLocation, jobCategory);
                        job.setId(jobId);  // Set the job ID
                        job.setEmail(jobEmail);  // Set the job email
                        allJobs.add(job);
                    } else {
                        Log.e("Firebase", "Skipping job due to missing location: " + jobName);
                    }
                }

                updateJobList(allJobs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to load jobs", databaseError.toException());
            }
        });
    }

    /**
     * This method updates the list of displayed jobs
     * @param jobsToShow Accepts the list of filtered jobs as an argument.
     */
    private void updateJobList(List<Job> jobsToShow) {
        sortJobsByDistance(jobsToShow);

        double referenceLatitude = useSearchLocation ? searchLatitude : userLatitude;
        double referenceLongitude = useSearchLocation ? searchLongitude : userLongitude;

        jobAdapter = new JobAdapter(jobsToShow, this, referenceLatitude, referenceLongitude, job -> {
            if ("Employee".equalsIgnoreCase(getIntent().getStringExtra("currentUserRole"))) {
                Intent intent = new Intent(SearchJobsActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", job.getId());
                startActivity(intent);
            }
        });

        jobRecyclerView.setAdapter(jobAdapter);
    }

    /**
     * Sorts the jobs list by distance from user in ascending order
     * @param jobs The list of jobs to sort
     */
    private void sortJobsByDistance(List<Job> jobs) {
        //use the search location or user location based on flag
        final double referenceLatitude = useSearchLocation ? searchLatitude : userLatitude;
        final double referenceLongitude = useSearchLocation ? searchLongitude : userLongitude;

        jobs.sort((job1, job2) -> {
            double distance1 = calculateDistance(job1, referenceLatitude, referenceLongitude);
            double distance2 = calculateDistance(job2, referenceLatitude, referenceLongitude);
            return Double.compare(distance1, distance2);
        });
    }

    /**
     * Calculates the distance between a reference point and a job
     * @param job The job to calculate distance for
     * @param refLatitude Reference latitude
     * @param refLongitude Reference longitude
     * @return The distance in kilometers
     */
    private double calculateDistance(Job job, double refLatitude, double refLongitude) {
        double jobLat, jobLng;

        //get job coordinates (either from LatLng object or separate lat/lng)
        if (job.getLocation() != null) {
            jobLat = job.getLocation().latitude;
            jobLng = job.getLocation().longitude;
        } else {
            jobLat = job.getLatitude();
            jobLng = job.getLongitude();
        }

        //call LocationUtils class to calculate distance
        return LocationUtils.calculateDistance(refLatitude, refLongitude, jobLat, jobLng);
    }

    /**
     * Filters jobs on the list based on user input in the search bar
     * and selected category from the filter
     * @param input The search text entered by the user
     */
    private void filterJobs(String input) {
        List<Job> filteredJobs = new ArrayList<>();

        String selectedCategory = categoryFilter.getSelectedItem().toString();

        for (Job job : allJobs) {
            boolean matchesSearch = input.isEmpty() || job.getName().toLowerCase().contains(input.toLowerCase());
            boolean matchesCategory = selectedCategory.equals("All") || job.getCategory().equals(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredJobs.add(job);
            }
        }
        updateJobList(filteredJobs);
    }

    /**
     * Sets up listeners for search bar and category selection.
     */
    private void setupListeners() {
        //listener to react to search bar input changes
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterJobs(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        categoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterJobs(searchBar.getText().toString().trim()); // Re-filter whenever a category is selected
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Initializes the tool bar at the top of the page and gives an option to
     * return to the previous activity
     */
    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
            actionBar.setTitle("Search Jobs");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}