package com.example.quickcash.activities;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.quickcash.R;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.AccessTokenListener;
import com.example.quickcash.utilities.JobCRUD;
import com.example.quickcash.utilities.Validator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * PostJobActivityClass: Accessible from the employer dashboard.
 * This activity allows employers to create and post new job listings.
 * It includes form validation, location selection via Google Places API,
 * and notification sending capabilities.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class PostJobActivity extends AppCompatActivity {
    /**
     * Path to the Firebase service account credentials file
     */
    private static final String CREDENTIALS_FILE_PATH = "key.json";

    /**
     * Firebase Cloud Messaging API endpoint for sending notifications
     */
    private static final String PUSH_NOTIFICATION_ENDPOINT = "https://fcm.googleapis.com/v1/projects/quickcash-ae34a/messages:send";

    /**
     * UI element for entering job name
     */
    TextView etName;

    /**
     * UI element for entering job description
     */
    TextView etDescription;

    /**
     * Fragment for selecting job location using Google Places Autocomplete
     */
    AutocompleteSupportFragment etAddress;

    /**
     * Dropdown menu for selecting job category
     */
    Spinner categorySpinner;

    /**
     * Button to submit the job posting
     */
    Button btnSubmit;

    /**
     * Utility for performing CRUD operations on Job objects in Firebase
     */
    JobCRUD jobCRUD;

    /**
     * Queue for managing network requests
     */
    private RequestQueue requestQueue;

    /**
     * Geographic coordinates for the job location
     */
    LatLng jobLocation;

    /**
     * onCreate method -> initializes all fields and UI elements
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_job);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA5NI3J5fhdeN_NDgXoAqLnJWVVcKRzSWg");
        }
        initUIElements();
        jobCRUD = new JobCRUD();
        setNotificationListeners();
        //adding multiple network request to a queue, FIFO based, running it separate threads, cannot run network request on the main thread in android
        //volley creates a separate thread for the network request
        requestQueue = Volley.newRequestQueue(this);
        //jobs is the topic name,subscribing to the jobs notification tray
        FirebaseMessaging.getInstance().subscribeToTopic("jobs");
    }

    /**
     * Initializes all UI elements including toolbar, text fields, spinner, and button
     */
    private void initUIElements() {
        initToolbar();
        etName = findViewById(R.id.etJobName);
        etDescription = findViewById(R.id.etDescription);
        categorySpinner = findViewById(R.id.categorySpinner);
        setupPlacesAutocomplete();
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    /**
     * Validates all form fields to ensure required data is present
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean verifyFields() {
        Validator v1 = new Validator();
        if (v1.checkInputEmpty(etName.getText().toString().trim())) {
            Toast.makeText(this, "Job name should not be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (v1.checkInputEmpty(etDescription.getText().toString().trim())) {
            Toast.makeText(this, "Job description should not be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (categorySpinner.getSelectedItem().toString().equals("Select a Category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        } else if(jobLocation==null){
            Toast.makeText(this, "Please select a job location", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Sets up the toolbar with navigation functionality
     */
    private void initToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Post Job");
        }
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }

    /**
     * Submits the job to Firebase database
     * Associates the job with the current user's email
     */
    private void submitJob() {
        Job submittedJob = createJobFromFields();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        submittedJob.setEmail(email);
        jobCRUD.addNewJob(submittedJob);
    }

    /**
     * Creates a Job object from the user-entered form fields
     *
     * @return a new Job object with data from the form fields
     */
    private Job createJobFromFields() {

        String jobName = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        return new Job(jobName, description, category, jobLocation.latitude, jobLocation.longitude);
    }

    /**
     * Sets up the Places Autocomplete functionality for selecting job location
     * Configures the fragment with appropriate place fields and type filter
     */
    private void setupPlacesAutocomplete() {
        etAddress = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.etAddress);

        if (etAddress != null) {
            etAddress.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

            //set place type filter to cities
            etAddress.setTypeFilter(TypeFilter.CITIES);
            etAddress.setHint("Job location...");

            //PlaceSelectionListener to handle response
            etAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                /**
                 * Callback when a place is selected from the autocomplete dropdown
                 * Stores the latitude and longitude of the selected place
                 *
                 * @param place The selected place
                 */
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    jobLocation = place.getLatLng();

                }

                /**
                 * Callback when an error occurs during place selection
                 * Displays an error message to the user
                 *
                 * @param status The error status
                 */
                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(PostJobActivity.this,
                            "Location selection failed: " + status.getStatusMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Sets up the click listener for the submit button
     * Handles form validation, job submission, and notification sending
     */
    private void setNotificationListeners() {
        btnSubmit.setOnClickListener(view -> {


            if (verifyFields()) {
                // Attempt to get the access token
                getAccessToken(this, new AccessTokenListener() {
                    /**
                     * Callback when access token is successfully received
                     * Proceeds to send notification about the new job
                     *
                     * @param token The Firebase access token
                     */
                    @Override
                    public void onAccessTokenReceived(String token) {
                        // When the token is received, send the notification
                        sendNotification(token);
                    }

                    /**
                     * Callback when access token retrieval fails
                     * Displays error message to the user
                     *
                     * @param exception The exception that occurred
                     */
                    @Override
                    public void onAccessTokenError(Exception exception) {
                        // Handle the error appropriately
                        Toast.makeText(PostJobActivity.this, "Error getting access token: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                });

                submitJob();
                Intent intent = new Intent(PostJobActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }

        });
    }

    /**
     * Retrieves a Firebase access token for sending notifications
     * Executes asynchronously to avoid blocking the main thread
     *
     * @param context The current context
     * @param listener Callback for token retrieval success or failure
     */
    private void getAccessToken(Context context, AccessTokenListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                InputStream serviceAccountStream = context.getAssets().open(CREDENTIALS_FILE_PATH);
                GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccountStream).createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));

                googleCredentials.refreshIfExpired(); // This will refresh the token if it's expired
                String token = googleCredentials.getAccessToken().getTokenValue();
                listener.onAccessTokenReceived(token);
                Log.d("token", "token" + token);
            } catch (IOException e) {
                listener.onAccessTokenError(e);
            }
        });
        executorService.shutdown();
    }

    /**
     * Sends a push notification to all users subscribed to the "jobs" topic
     * Notification informs users about a new job posting
     *
     * @param authToken Firebase access token for authorization
     */
    private void sendNotification(String authToken) {
        try {
            // Build the notification payload
            JSONObject notificationJSONBody = new JSONObject();
            notificationJSONBody.put("title", "New Job Posted!");
            notificationJSONBody.put("body", "There is a new job available near you!");

            JSONObject dataJSONBody = new JSONObject();
            dataJSONBody.put("job_id", etName.getText().toString().trim());

            JSONObject messageJSONBody = new JSONObject();
            messageJSONBody.put("topic", "jobs");
            messageJSONBody.put("notification", notificationJSONBody);
            messageJSONBody.put("data", dataJSONBody);

            JSONObject pushNotificationJSONBody = new JSONObject();
            pushNotificationJSONBody.put("message", messageJSONBody);

            // Log the complete JSON payload for debugging
            Log.d("NotificationBody", "JSON Body: " + pushNotificationJSONBody);

            // Create the request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, PUSH_NOTIFICATION_ENDPOINT, pushNotificationJSONBody, response -> {
                Log.d("NotificationResponse", "Response: " + response.toString());
                Toast.makeText(this, "Notification Sent Successfully", Toast.LENGTH_SHORT).show();
            }, error -> {
                Log.e("NotificationError", "Error Response: " + error.toString());
                if (error.networkResponse != null) {
                    Log.e("NotificationError", "Status Code: " + error.networkResponse.statusCode);

                    Log.e("NotificationError", "Error Data: " + new String(error.networkResponse.data));
                }
                Toast.makeText(this, "Failed to Send Notification", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }) {
                /**
                 * Provides headers for the HTTP request
                 * Includes content type and authorization token
                 *
                 * @return Map of header key-value pairs
                 * @throws AuthFailureError if authorization fails
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    headers.put("Authorization", "Bearer " + authToken);
                    Log.d("NotificationHeaders", "Headers: " + headers);
                    return headers;
                }
            };

            // Add the request to the queue
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("NotificationJSONException", "Error creating notification JSON: " + e.getMessage());
            Toast.makeText(this, "Error creating notification payload", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}