package com.example.quickcash.activities;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickcash.R;
import com.example.quickcash.entities.ApplicationData;
import com.example.quickcash.utilities.ApplicationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * MyApplicationsActivity displays all job applications submitted by the current user.
 * It retrieves application data from Firebase, displays them in a RecyclerView,
 * and sorts them by status priority.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class MyApplicationsActivity extends AppCompatActivity {

    /**
     * RecyclerView to display application items
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for binding application data to the RecyclerView
     */
    private ApplicationAdapter adapter;

    /**
     * List of applications to display
     */
    private List<ApplicationData> applicationList = new ArrayList<>();

    /**
     * Reference to the Firebase database applications node
     */
    private DatabaseReference databaseReference;

    /**
     * Email of the currently logged in user
     */
    private String currentUserEmail;

    /**
     * Initializes the activity, sets up the toolbar, RecyclerView, and adapter.
     * Fetches the current user's applications from Firebase.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Applications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        recyclerView = findViewById(R.id.recyclerViewMyApplications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        databaseReference = FirebaseDatabase.getInstance().getReference("applications");

        adapter = new ApplicationAdapter(applicationList, application -> {
            //** for when we click an application **

            //not sure what we want to have happen yet.
            //maybe we'll have the option to 'complete' a
            //job, maybe this won't be needed
        });

        recyclerView.setAdapter(adapter);

        fetchApplications();
    }

    /**
     * Handles the back button press in the toolbar.
     * Finishes the activity and returns to the previous screen.
     *
     * @return true to indicate the event was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); //close this activity and returns dashboard
        return true;
    }

    /**
     * Fetches the current user's applications from Firebase.
     * Filters applications by the current user's email.
     * Updates the application list and refreshes the RecyclerView.
     *
     * @author Ethan Pancura
     */
    private void fetchApplications() {
        databaseReference.orderByChild("email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    /**
                     * Called when application data is successfully retrieved from Firebase.
                     * Updates the application list and adapter with the fetched data.
                     *
                     * @param snapshot The data snapshot containing application data
                     */
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        applicationList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ApplicationData app = data.getValue(ApplicationData.class);
                            applicationList.add(app);
                        }
                        sortApplications();
                        adapter.notifyDataSetChanged();
                    }

                    /**
                     * Called when the database operation is cancelled or fails.
                     * Logs the error message.
                     *
                     * @param error The database error details
                     */
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyApplicationsActivity", "DB Error: " + error.getMessage());
                    }
                });
    }

    /**
     * Sorts the application list by status priority.
     * Priority order: accepted, open, rejected, other.
     */
    private void sortApplications() {
        Collections.sort(applicationList, new Comparator<ApplicationData>() {
            /**
             * Compares two applications based on their status priority.
             *
             * @param a1 First application to compare
             * @param a2 Second application to compare
             * @return A negative, zero, or positive integer as the first application
             *         has lower, equal, or higher priority than the second
             */
            @Override
            public int compare(ApplicationData a1, ApplicationData a2) {
                return getStatusPriority(a1.getStatus()) - getStatusPriority(a2.getStatus());
            }

            /**
             * Determines the priority value for a given application status.
             * Lower values indicate higher priority.
             *
             * @param status The application status to evaluate
             * @return An integer representing the priority (0 = highest)
             */
            private int getStatusPriority(String status) {
                switch (status) {
                    case "accepted": return 0;
                    case "open": return 1;
                    case "rejected": return 2;
                    default: return 3;
                }
            }
        });
    }
}