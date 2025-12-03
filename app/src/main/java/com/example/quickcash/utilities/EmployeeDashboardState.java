package com.example.quickcash.utilities;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import com.example.quickcash.R;
import com.example.quickcash.activities.DashboardActivity;
import com.example.quickcash.activities.MyApplicationsActivity;

public class EmployeeDashboardState implements DashboardState {

    @Override
    public void setupUI(DashboardActivity context, String username) {
        context.findViewById(R.id.nearbyJobsSection).setVisibility(View.VISIBLE);



        TextView welcomeText = context.findViewById(R.id.welcomeText);
        TextView roleText = context.findViewById(R.id.currentRoleText);
        welcomeText.setText("Welcome, " + username);
        roleText.setText("Current Role: Employee");
    }

    @Override
    public void loadJobs(DashboardActivity context) {
        context.loadNearbyJobs(true); //true means employee
    }
}

