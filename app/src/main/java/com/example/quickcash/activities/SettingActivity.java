package com.example.quickcash.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quickcash.R;

/**
 * SettingActivity provides a stub implementation for application settings.
 *
 * Note: This appears to be an alternative/duplicate version of SettingsActivity.
 * The fully implemented version is in SettingsActivity.java, while this is a stub
 * that may have been created during early development or as an alternate implementation.
 *
 * @author QuickCash Team
 * @version 0.1 (Stub implementation)
 * @see SettingsActivity - For the fully implemented settings functionality
 */
public class SettingActivity extends AppCompatActivity {

    /**
     * Initializes the activity and sets up the UI.
     * Currently implements basic EdgeToEdge display with no functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}