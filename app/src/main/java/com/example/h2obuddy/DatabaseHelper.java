package com.example.h2obuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "WaterReminder.db";
    private static final int DATABASE_VERSION = 2; // Updated version

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_WATER_LOGS = "water_logs";

    // Common Columns
    private static final String COLUMN_ID = "id";

    // Users Table Columns
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DAILY_GOAL = "daily_goal";
    private static final String COLUMN_REMINDER_INTERVAL = "reminder_interval";
    private static final String COLUMN_WAKE_UP_HOUR = "wake_up_hour";
    private static final String COLUMN_WAKE_UP_MINUTE = "wake_up_minute";
    private static final String COLUMN_BED_TIME_HOUR = "bed_time_hour";
    private static final String COLUMN_BED_TIME_MINUTE = "bed_time_minute";
    private static final String COLUMN_NOTIFICATION_START_HOUR = "notification_start_hour";
    private static final String COLUMN_NOTIFICATION_START_MINUTE = "notification_start_minute";
    private static final String COLUMN_NOTIFICATION_END_HOUR = "notification_end_hour";
    private static final String COLUMN_NOTIFICATION_END_MINUTE = "notification_end_minute";

    // Water Logs Table Columns
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_AMOUNT = "amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DAILY_GOAL + " INTEGER DEFAULT 2000, "
                + COLUMN_REMINDER_INTERVAL + " INTEGER DEFAULT 60, "
                + COLUMN_WAKE_UP_HOUR + " INTEGER DEFAULT 6, "
                + COLUMN_WAKE_UP_MINUTE + " INTEGER DEFAULT 0, "
                + COLUMN_BED_TIME_HOUR + " INTEGER DEFAULT 22, "
                + COLUMN_BED_TIME_MINUTE + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFICATION_START_HOUR + " INTEGER DEFAULT 8, "
                + COLUMN_NOTIFICATION_START_MINUTE + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFICATION_END_HOUR + " INTEGER DEFAULT 20, "
                + COLUMN_NOTIFICATION_END_MINUTE + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Water Logs Table
        String CREATE_WATER_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WATER_LOGS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_AMOUNT + " INTEGER, "
                + COLUMN_EMAIL + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + "))";
        db.execSQL(CREATE_WATER_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add notification start and end columns
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NOTIFICATION_START_HOUR + " INTEGER DEFAULT 8");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NOTIFICATION_START_MINUTE + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NOTIFICATION_END_HOUR + " INTEGER DEFAULT 20");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NOTIFICATION_END_MINUTE + " INTEGER DEFAULT 0");
        }
    }

    // ====================== Notification Settings ====================== //

    public boolean saveNotificationSettings(String email, int wakeUpHour, int wakeUpMinute, int bedTimeHour,
                                            int bedTimeMinute, int startHour, int startMinute, int endHour, int endMinute) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WAKE_UP_HOUR, wakeUpHour);
        values.put(COLUMN_WAKE_UP_MINUTE, wakeUpMinute);
        values.put(COLUMN_BED_TIME_HOUR, bedTimeHour);
        values.put(COLUMN_BED_TIME_MINUTE, bedTimeMinute);
        values.put(COLUMN_NOTIFICATION_START_HOUR, startHour);
        values.put(COLUMN_NOTIFICATION_START_MINUTE, startMinute);
        values.put(COLUMN_NOTIFICATION_END_HOUR, endHour);
        values.put(COLUMN_NOTIFICATION_END_MINUTE, endMinute);

        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public NotificationSettings getNotificationSettings(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        NotificationSettings settings = null;

        try {
            cursor = db.query(TABLE_USERS, new String[]{
                            COLUMN_WAKE_UP_HOUR, COLUMN_WAKE_UP_MINUTE, COLUMN_BED_TIME_HOUR, COLUMN_BED_TIME_MINUTE,
                            COLUMN_NOTIFICATION_START_HOUR, COLUMN_NOTIFICATION_START_MINUTE,
                            COLUMN_NOTIFICATION_END_HOUR, COLUMN_NOTIFICATION_END_MINUTE},
                    COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                settings = new NotificationSettings(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WAKE_UP_HOUR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WAKE_UP_MINUTE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BED_TIME_HOUR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BED_TIME_MINUTE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_START_HOUR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_START_MINUTE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_END_HOUR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_END_MINUTE))
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return settings;
    }

    // Add a helper class to store notification settings
    public static class NotificationSettings {
        public int wakeUpHour, wakeUpMinute, bedTimeHour, bedTimeMinute, startHour, startMinute, endHour, endMinute;

        public NotificationSettings(int wakeUpHour, int wakeUpMinute, int bedTimeHour, int bedTimeMinute,
                                    int startHour, int startMinute, int endHour, int endMinute) {
            this.wakeUpHour = wakeUpHour;
            this.wakeUpMinute = wakeUpMinute;
            this.bedTimeHour = bedTimeHour;
            this.bedTimeMinute = bedTimeMinute;
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
        }
    }

    public int validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1; // Default: user not found

        Cursor cursor = null;
        try {
            // Query to check if the email and password match an existing user
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID}, // Select only the ID column
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", // WHERE condition
                    new String[]{email, password}, // Arguments for placeholders
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the user ID
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return userId;
    }

    public boolean insertUser(String name, String email, String password, int dailyGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Add user details to ContentValues
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password); // Consider hashing the password for security
        values.put(COLUMN_DAILY_GOAL, dailyGoal);

        long result = -1;
        try {
            // Insert the new user into the database
            result = db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return true if insertion was successful, false otherwise
        return result != -1;
    }

    public String getUserEmailById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = null; // Default to null if the user is not found
        Cursor cursor = null;

        try {
            // Query to fetch the email based on the user ID
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_EMAIL}, // Columns to select
                    COLUMN_ID + "=?", // WHERE clause
                    new String[]{String.valueOf(userId)}, // Arguments for WHERE clause
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the email from the cursor
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return email;
    }

    public boolean saveNotificationTimes(String email, int wakeUpHour, int wakeUpMinute, int bedTimeHour, int bedTimeMinute) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Add notification time details to ContentValues
        values.put(COLUMN_WAKE_UP_HOUR, wakeUpHour);
        values.put(COLUMN_WAKE_UP_MINUTE, wakeUpMinute);
        values.put(COLUMN_BED_TIME_HOUR, bedTimeHour);
        values.put(COLUMN_BED_TIME_MINUTE, bedTimeMinute);

        // Update the database with the new settings for the specified email
        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});

        // Return true if at least one row was updated, false otherwise
        return rows > 0;
    }

    public boolean insertWaterLog(String email, String date, int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Add values to insert into the water_logs table
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_AMOUNT, amount);

        long result = -1;
        try {
            // Insert the log into the database
            result = db.insert(TABLE_WATER_LOGS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return true if insertion was successful, false otherwise
        return result != -1;
    }

    public int getDailyWaterIntake(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalIntake = 0; // Default to 0 if no logs are found
        Cursor cursor = null;

        try {
            // Query to calculate the sum of water intake for the given email and date
            String query = "SELECT SUM(" + COLUMN_AMOUNT + ") AS total " +
                    "FROM " + TABLE_WATER_LOGS +
                    " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_DATE + " = ?";
            cursor = db.rawQuery(query, new String[]{email, date});

            if (cursor != null && cursor.moveToFirst()) {
                totalIntake = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return totalIntake;
    }




    // test run support

    public List<String> getDailyHistory(String email) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to fetch the last 7 days' water intake history
            String query = "SELECT " + COLUMN_DATE + ", SUM(" + COLUMN_AMOUNT + ") AS total " +
                    "FROM " + TABLE_WATER_LOGS +
                    " WHERE " + COLUMN_EMAIL + " = ? " +
                    "GROUP BY " + COLUMN_DATE +
                    " ORDER BY " + COLUMN_DATE + " DESC " +
                    "LIMIT 7";

            cursor = db.rawQuery(query, new String[]{email});

            // Process the query results
            while (cursor != null && cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                history.add(date + ": " + total + " ml");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return history;
    }

    public int getDailyGoal(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int dailyGoal = 2000; // Default daily goal
        Cursor cursor = null;

        try {
            // Query to fetch the daily goal for the user
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_DAILY_GOAL},
                    COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                dailyGoal = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_GOAL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return dailyGoal;
    }

    public int getReminderInterval(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int interval = 60; // Default reminder interval
        Cursor cursor = null;

        try {
            // Query to fetch the reminder interval
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_REMINDER_INTERVAL},
                    COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                interval = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_INTERVAL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return interval;
    }

    public boolean updateUserSettings(String email, int dailyGoal, int reminderInterval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Add updated values to ContentValues
        values.put(COLUMN_DAILY_GOAL, dailyGoal);
        values.put(COLUMN_REMINDER_INTERVAL, reminderInterval);

        // Perform the update
        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});

        // Return true if at least one row was updated
        return rows > 0;
    }

}
