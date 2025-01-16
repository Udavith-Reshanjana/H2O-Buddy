package com.example.h2obuddy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        lvHistory = findViewById(R.id.lvHistory);
        databaseHelper = new DatabaseHelper(this);

        // Retrieve user ID from intent
        userId = getIntent().getExtras().getInt("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadWeeklyHistory();
    }

    private void loadWeeklyHistory() {
        // Get user email by ID
        String userEmail = databaseHelper.getUserEmailById(userId);
        if (userEmail == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch weekly history from the database
        List<String> history = databaseHelper.getWeeklyHistory(userEmail);

        if (history.isEmpty()) {
            Toast.makeText(this, "No history available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an adapter to display the history
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, highlightSuccessfulDays(history, userEmail));
        lvHistory.setAdapter(adapter);
    }

    private List<String> highlightSuccessfulDays(List<String> history, String userEmail) {
        int dailyGoal = databaseHelper.getDailyGoal(userEmail);

        for (int i = 0; i < history.size(); i++) {
            String entry = history.get(i);
            String[] parts = entry.split(": ");
            if (parts.length == 2) {
                try {
                    int intake = Integer.parseInt(parts[1].replace(" ml", ""));
                    if (intake >= dailyGoal) {
                        history.set(i, entry + " (✅ Goal Achieved)");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return history;
    }
}
