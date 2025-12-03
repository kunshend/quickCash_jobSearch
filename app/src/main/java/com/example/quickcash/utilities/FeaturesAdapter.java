package com.example.quickcash.utilities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickcash.activities.MapActivity;
import com.example.quickcash.activities.MyApplicationsActivity;
import com.example.quickcash.activities.PostJobActivity;
import com.example.quickcash.activities.SearchJobsActivity;
import com.example.quickcash.activities.ViewApplications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class FeaturesAdapter extends RecyclerView.Adapter<FeaturesAdapter.ViewHolder> {
    private List<String> features;
    private Context context;
    private double userLatitude;
    private double userLongitude;

    public FeaturesAdapter(List<String> features, double userLatitude, double userLongitude) {
        this.features = features;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    public FeaturesAdapter(List<String> features) {
        this.features = features;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creates a view for each feature item
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Sets the text for each feature
        String feature = features.get(position);
        holder.textView.setText(feature);

        // Add click listener for each feature
        holder.itemView.setOnClickListener(v -> {
            if ("Search Jobs".equals(feature)) {
                //pass user location into SearchJobsActivity
                Intent intent = getIntent();

                context.startActivity(intent);
            } else if ("Nearby Jobs".equals(feature)) {
                context.startActivity(new Intent(context, MapActivity.class));
            } else if (feature.equals("My Applications")) {
                context.startActivity(new Intent(context, MyApplicationsActivity.class));
            } else if ("My Jobs".equals(feature)) {
                context.startActivity(new Intent(context, com.example.quickcash.activities.MyJobsActivity.class));
            }else if("Post Jobs".equals(feature)){
                context.startActivity(new Intent(context, PostJobActivity.class));
            } else if("View Applications".equals(feature)){
                // Get current user email
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userEmail = user.getEmail();

                    // Create intent and pass the email
                    Intent intent = new Intent(context, com.example.quickcash.activities.ViewApplications.class);
                    intent.putExtra("userEmail", userEmail);
                    context.startActivity(intent);
                } else {
                    // Handle case where user is not logged in
                    Intent intent = new Intent(context, com.example.quickcash.activities.ViewApplications.class);
                    context.startActivity(intent);
                }
            }
            // Add handling for other features as needed
        });
    }

    /**
     * @author Ethan Pancura
     * Android Studio automatically did this extraction refactor once
     * I fixed a bug launching the intent twice. It just pulls logic from the
     * onBindViewHolder which didn't need to be there.
     * @return Intent for search jobs activity
     */
    @NonNull
    private Intent getIntent() {
        Intent intent = new Intent(context, SearchJobsActivity.class);
        intent.putExtra("userLatitude", userLatitude);
        intent.putExtra("userLongitude", userLongitude);

        if (context instanceof com.example.quickcash.activities.DashboardActivity) {
            com.example.quickcash.activities.DashboardActivity dashboardActivity =
                    (com.example.quickcash.activities.DashboardActivity) context;
            String currentUserRole = dashboardActivity.getCurrentUserRole();
            intent.putExtra("currentUserRole", currentUserRole);
        }
        return intent;
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }
}