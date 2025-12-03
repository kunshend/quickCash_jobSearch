package com.example.quickcash.utilities;

import com.example.quickcash.entities.Job;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * JobCRUD class, responsible for accessing and updating jobs in Firebase
 */
public class JobCRUD {

    private final FirebaseDatabase database;
    private final DatabaseReference databaseReference;

    /**
     * Constructor for FirebaseCRUD objects
     */
    public JobCRUD() {
        this.database = FirebaseDatabase.getInstance("https://quickcash-ae34a-default-rtdb.firebaseio.com/");
        this.databaseReference = database.getReference("jobs");
    }

    /**
     * Add a new Job to the database, using a Job object passed in. Note that this method can also be
     *  used to update an existing job by passing in a new job with the same name
     * @param job The job object to be added to the database
     */
    public void addNewJob(Job job) {
        // Generate a unique key for the new job
        String jobId = databaseReference.push().getKey();
        job.setId(jobId);

        // Create a map to ensure all fields are saved explicitly
        Map<String, Object> jobValues = new HashMap<>();
        jobValues.put("id", job.getId());
        jobValues.put("name", job.getName());
        jobValues.put("description", job.getDescription());
        jobValues.put("category", job.getCategory());
        jobValues.put("latitude", job.getLatitude());
        jobValues.put("longitude", job.getLongitude());
        jobValues.put("email", job.getEmail()); // This ensures the email is saved
        jobValues.put("status", job.getStatus());

        // Save the job to Firebase with all fields
        databaseReference.child(jobId).setValue(jobValues);
    }

    /**
     * Fetch a job pointed to by a given name. Note that it is necessary to use a valueEventListener,
     *  and that the required job is not simply returned. You can still access the Job object from within
     *  the event listener
     * @param jobID String - the name of the job to fetch
     * @param valueEventListener A ValueEventListener containing the UI-level tasks to execute in relation
     *                           to the requested data
     */
    public void readJob(String jobID, ValueEventListener valueEventListener) {
        databaseReference.child(jobID).addListenerForSingleValueEvent(valueEventListener);
    }
    //us4 read jobs
    public void readAllJobs(ValueEventListener valueEventListener) {
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }


    /**
     * Remove a given job from the database using the name of the job
     * @param jobID The name of the job to remove
     */
    public void deleteJob(String jobID) {
        databaseReference.child(jobID).removeValue();
    }

    /**
     * Remove a given job from the database using a Job object
     * @param job The job object to remove
     */
    public void deleteJob(Job job) {
        if (job.getId() != null) {
            databaseReference.child(job.getId()).removeValue();
        } else {
            // Fallback to using name if ID is not available (for backward compatibility)
            databaseReference.child(job.getName()).removeValue();
        }
    }
}
