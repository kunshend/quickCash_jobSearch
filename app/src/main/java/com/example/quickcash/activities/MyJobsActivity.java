package com.example.quickcash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickcash.R;
import com.example.quickcash.entities.ApplicationData;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.JobAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;


/**
 * MyJobsActivity
 *
 * Displays a list of jobs that the current employee has been accepted for.
 *
 * These jobs are fetched by first identifying accepted applications by the current user,
 * then matching those with job entries in the database. Clicking on a job navigates to
 * MyJobsDetailsActivity, where the user can mark it as completed
 *
 * @author Ethan Pancura
 */
public class MyJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private DatabaseReference applicationsRef;
    private DatabaseReference jobsRef;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_jobs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Jobs");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        recyclerView = findViewById(R.id.recyclerViewMyJobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        jobAdapter = new JobAdapter(jobList, this, 0, 0, job -> {
            Intent intent = new Intent(MyJobsActivity.this, MyJobsDetailsActivity.class);
            intent.putExtra("jobId", job.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(jobAdapter);

        //initialize database references and load jobs
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        applicationsRef = FirebaseDatabase.getInstance().getReference("applications");
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");

        loadAcceptedJobs();
    }

    /**
     * Queries the database to find all accepted applications for the current user.
     * For each accepted application, it fetches associated job details.
     */
    private void loadAcceptedJobs() {
        applicationsRef.orderByChild("email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ApplicationData application = data.getValue(ApplicationData.class);
                            if (application != null && "accepted".equalsIgnoreCase(application.getStatus())) {
                                String jobId = application.getJobId();
                                fetchJobDetails(jobId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyJobsActivity", "Error loading applications: " + error.getMessage());
                    }
                });
    }

    private void fetchJobDetails(String jobId) {
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Job job = snapshot.getValue(Job.class);
                if (job != null) {
                    job.setId(snapshot.getKey());
                    jobList.add(job);
                    jobAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyJobsActivity", "Error loading job: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

