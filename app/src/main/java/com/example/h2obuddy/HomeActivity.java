package com.example.h2obuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvDailyGoal, tvProgress;
    private ProgressBar progressBar;
    private Button btnAdd250, btnAdd500;
    private DatabaseHelper databaseHelper;

    private String userEmail;
    private int dailyGoal;
    private int currentIntake;

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
        databaseHelper = new DatabaseHelper(this);

        // Get user email (passed from LoginActivity)
        userEmail = getIntent().getStringExtra("userEmail");

        // Retrieve user's daily goal from database
        dailyGoal = getUserDailyGoal(userEmail);
        currentIntake = getCurrentDayIntake(userEmail);

        // Update UI with current data
        updateUI();

        // Add 250 ml button click listener
        btnAdd250.setOnClickListener(v -> {
            addWaterIntake(250);
        });

        // Add 500 ml button click listener
        btnAdd500.setOnClickListener(v -> {
            addWaterIntake(500);
        });
    }

    private int getUserDailyGoal(String email) {
        return databaseHelper.getDailyGoal(email);
    }

    private int getCurrentDayIntake(String email) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return databaseHelper.getDailyWaterIntake(email, today);
    }

    private void addWaterIntake(int amount) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isInserted = databaseHelper.insertWaterLog(userEmail, today, amount);

        if (isInserted) {
            currentIntake += amount;
            updateUI();
            Toast.makeText(this, amount + " ml added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to log water intake", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvDailyGoal.setText("Daily Goal: " + dailyGoal + " ml");
        tvProgress.setText("Progress: " + currentIntake + " / " + dailyGoal + " ml");
        progressBar.setMax(dailyGoal);
        progressBar.setProgress(currentIntake);
    }
}
