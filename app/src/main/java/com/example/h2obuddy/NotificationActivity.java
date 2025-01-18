package com.example.h2obuddy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

public class NotificationActivity extends AppCompatActivity {

    private TimePicker tpWakeUpTime;
    private TimePicker tpBedTime;
    private DatabaseHelper databaseHelper;
    private int userId = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize TimePickers
        tpWakeUpTime = findViewById(R.id.tpWakeUpTime);
        tpBedTime = findViewById(R.id.tpBedTime);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Retrieve user ID from Intent
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load existing notification settings if required (optional)
        loadNotificationSettings();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadNotificationSettings() {
        // Get the user's email using their user ID
        String userEmail = databaseHelper.getUserEmailById(userId);
        if (userEmail == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load notification settings
        DatabaseHelper.NotificationSettings settings = databaseHelper.getNotificationSettings(userEmail);

        if (settings != null) {
            tpWakeUpTime.setHour(settings.wakeUpHour);
            tpWakeUpTime.setMinute(settings.wakeUpMinute);
            tpBedTime.setHour(settings.bedTimeHour);
            tpBedTime.setMinute(settings.bedTimeMinute);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveNotificationSettings(View view) {
        // Get the wake-up and bedtimes from the TimePickers
        int wakeUpHour = tpWakeUpTime.getHour();
        int wakeUpMinute = tpWakeUpTime.getMinute();
        int bedTimeHour = tpBedTime.getHour();
        int bedTimeMinute = tpBedTime.getMinute();

        // Get the user's email using their user ID
        String userEmail = databaseHelper.getUserEmailById(userId);
        if (userEmail == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the notification settings to the database
        boolean success = databaseHelper.saveNotificationTimes(userEmail, wakeUpHour, wakeUpMinute, bedTimeHour, bedTimeMinute);

        if (success) {
            // Display a success message and navigate to the home screen
            Toast.makeText(this, "Notification settings saved successfully", Toast.LENGTH_SHORT).show();

            // Navigate to the home screen and pass the user ID
            Intent intent = new Intent(this, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("USER_ID", userId);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            // Display an error message
            Toast.makeText(this, "Failed to save notification settings", Toast.LENGTH_SHORT).show();
        }
    }
}
