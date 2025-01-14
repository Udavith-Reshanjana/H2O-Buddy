package com.example.h2obuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    private EditText etDailyGoal, etReminderInterval;
    private Button btnSaveSettings;
    private DatabaseHelper databaseHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize views
        etDailyGoal = findViewById(R.id.etDailyGoal);
        etReminderInterval = findViewById(R.id.etReminderInterval);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Get user email (passed from previous activity)
        userEmail = getIntent().getStringExtra("userEmail");

        // Load existing settings
        loadSettings();

        // Save settings button click listener
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void loadSettings() {
        int dailyGoal = databaseHelper.getDailyGoal(userEmail);
        // Assume we store reminder interval in user profile; if not, use a default value.
        int reminderInterval = databaseHelper.getReminderInterval(userEmail);

        etDailyGoal.setText(String.valueOf(dailyGoal));
        etReminderInterval.setText(String.valueOf(reminderInterval));
    }

    private void saveSettings() {
        String dailyGoalStr = etDailyGoal.getText().toString().trim();
        String reminderIntervalStr = etReminderInterval.getText().toString().trim();

        // Validate input
        if (dailyGoalStr.isEmpty() || reminderIntervalStr.isEmpty()) {
            Toast.makeText(SettingActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int dailyGoal;
        int reminderInterval;
        try {
            dailyGoal = Integer.parseInt(dailyGoalStr);
            reminderInterval = Integer.parseInt(reminderIntervalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(SettingActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update settings in the database
        boolean isUpdated = databaseHelper.updateUserSettings(userEmail, dailyGoal, reminderInterval);

        if (isUpdated) {
            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }
}
