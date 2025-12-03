package com.example.quickcash.utilities;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.quickcash.R;
import com.example.quickcash.activities.DashboardActivity;

public class EmployerDashboardState implements DashboardState {

    @Override
    public void setupUI(DashboardActivity context, String username) {
        context.findViewById(R.id.nearbyJobsSection).setVisibility(View.GONE);



        TextView welcomeText = context.findViewById(R.id.welcomeText);
        TextView roleText = context.findViewById(R.id.currentRoleText);
        welcomeText.setText("Welcome, " + username);
        roleText.setText("Current Role: Employer");
    }

    @Override
    public void loadJobs(DashboardActivity context) {
        context.loadNearbyJobs(false); //false means employer
    }
}

