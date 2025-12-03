package com.example.quickcash.utilities;

import android.util.Log;

import com.example.quickcash.entities.Application;
import com.example.quickcash.entities.Job;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * JobCRUD class, responsible for accessing and updating jobs in Firebase
 */
public class ApplicationCRUD {

    private final FirebaseDatabase database;
    private final DatabaseReference databaseReference;

    /**
     * Constructor for FirebaseCRUD objects
     */
    public ApplicationCRUD() {
        this.database = FirebaseDatabase.getInstance("https://quickcash-ae34a-default-rtdb.firebaseio.com/");
        this.databaseReference = database.getReference("applications");
    }

    /**
     * Add a new Job to the database, using a Job object passed in. Note that this method can also be
     *  used to update an existing job by passing in a new job with the same name
     * @param application The job object to be added to the database
     */
    public void addNewApplication(Application application) {
        databaseReference.child(application.getId()).setValue(application)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Data successfully written to Firebase.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error writing data to Firebase: " + e.getMessage());
                });
    }

    /**
     * Fetch a job pointed to by a given name. Note that it is necessary to use a valueEventListener,
     *  and that the required job is not simply returned. You can still access the Job object from within
     *  the event listener
     * @param application String - the name of the job to fetch
     * @param valueEventListener A ValueEventListener containing the UI-level tasks to execute in relation
     *                           to the requested data
     */
    public void readApplication(String application, ValueEventListener valueEventListener) {
        databaseReference.child(application).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Remove a given job from the database using the name of the job
     * @param application The name of the job to remove
     */
    public void deleteApplication(String application) {
        databaseReference.child(application).removeValue();
    }

}