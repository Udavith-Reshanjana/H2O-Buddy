package com.example.h2obuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private TextView tvDailyGoal;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private Button btnAdd250;
    private Button btnAdd500;

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
        btnAdd250 = findViewById(R.id.btnAdd250);
        btnAdd500 = findViewById(R.id.btnAdd500);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper();

        // Get user ID from intent extras
        userId = getIntent().getExtras().getInt("USER_ID", -1);

        // Load user data from database
        loadUserData();

        // Set button click listeners
        btnAdd250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWaterIntake(250);
            }
        });

        btnAdd500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWaterIntake(500);
            }
        });
    }

    private void loadUserData() {
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch user data from the database
        User user = databaseHelper.getUserById(userId);
        if (user != null) {
            dailyGoal = user.dailyGoal;
            currentIntake = user.currentIntake;
            updateUI();
        } else {
            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void addWaterIntake(int amount) {
        currentIntake += amount;

        // Ensure intake doesn't exceed daily goal
        if (currentIntake > dailyGoal) {
            currentIntake = dailyGoal;
        }

        // Save updated intake to the database
        boolean success = databaseHelper.updateUserIntake(userId, currentIntake);

        if (success) {
            updateUI(); // Update UI with the new values
        } else {
            Toast.makeText(this, "Failed to update water intake", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvProgress.setText("Progress: " + currentIntake + " / " + dailyGoal + " ml");
        progressBar.setMax(100); // Ensure progressBar max value is set to 100
        progressBar.setProgress((currentIntake * 100) / dailyGoal); // Update progress
    }

    private class DatabaseHelper {

        private Map<Integer, User> users = new HashMap<>();

        public DatabaseHelper() {
            users.put(1, new User(1, "test@example.com", "password123", 2000, 0));
            users.put(2, new User(2, "user2@example.com", "pass456", 2500, 500));
        }

        public User getUserById(int userId) {
            return users.get(userId);
        }

        public boolean updateUserIntake(int userId, int intake) {
            User user = users.get(userId);
            if (user != null) {
                user.currentIntake = intake;
                return true;
            }
            return false;
        }

        public int validateUser(String email, String password) {
            for (Map.Entry<Integer, User> entry : users.entrySet()) {
                User user = entry.getValue();
                if (user.email.equals(email) && user.password.equals(password)) {
                    return entry.getKey();
                }
            }
            return -1;
        }
    }

    private static class User {
        int id;
        String email;
        String password;
        int dailyGoal;
        int currentIntake;

        public User(int id, String email, String password, int dailyGoal, int currentIntake) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.dailyGoal = dailyGoal;
            this.currentIntake = currentIntake;
        }
    }
}
