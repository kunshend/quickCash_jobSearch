package com.example.quickcash.utilities;

import com.example.quickcash.activities.DashboardActivity;

public interface DashboardState {
    void setupUI(DashboardActivity context, String username);
    void loadJobs(DashboardActivity context);
}
