package com.example.h2obuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvDailyGoal;
    private TextView tvProgress;
    private ProgressBar progressBar;

    private int dailyGoal = 2000; // Default daily goal in ml
    private int currentIntake = 0; // Tracks the current water intake
    private int userId = -1; // Holds the user ID passed from MainActivity

    private DatabaseHelper databaseHelper; // Database helper instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        tvDailyGoal = findViewById(R.id.tvDailyGoal);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Get user ID from intent extras
        userId = getIntent().getExtras().getInt("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load user data from the database
        loadUserData();
    }

    private void loadUserData() {
        // Fetch user details from the database
        String userEmail = databaseHelper.getUserEmailById(userId);
        if (userEmail == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dailyGoal = databaseHelper.getDailyGoal(userEmail);
        currentIntake = databaseHelper.getDailyWaterIntake(userEmail, getCurrentDate());
        updateUI();
    }

    private void addWaterIntake(int amount) {
        // Update water intake
        currentIntake += amount;

        // Ensure intake doesn't exceed the daily goal
        if (currentIntake > dailyGoal) {
            currentIntake = dailyGoal;
        }

        // Save updated intake to the database
        String userEmail = databaseHelper.getUserEmailById(userId);
        boolean success = databaseHelper.insertWaterLog(userEmail, getCurrentDate(), amount);

        if (success) {
            updateUI(); // Refresh UI with the updated values
        } else {
            Toast.makeText(this, "Failed to update water intake", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvDailyGoal.setText("Daily Goal: " + dailyGoal + " ml");
        tvProgress.setText("Progress: " + currentIntake + " / " + dailyGoal + " ml");
        progressBar.setMax(dailyGoal);
        progressBar.setProgress(currentIntake);
    }

    public void addWaterIntake250(View view) {
        addWaterIntake(250);
    }

    public void addWaterIntake500(View view) {
        addWaterIntake(500);
    }

    public void gotoSettings(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    public void gotoHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private String getCurrentDate() {
        // Returns the current date in YYYY-MM-DD format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
