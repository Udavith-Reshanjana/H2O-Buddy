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
    private static final int DATABASE_VERSION = 1;

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
                + COLUMN_BED_TIME_MINUTE + " INTEGER DEFAULT 0)";
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER_LOGS);
        onCreate(db);
    }

    // ====================== User Management ====================== //

    public boolean insertUser(String name, String email, String password, int dailyGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_DAILY_GOAL, dailyGoal);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1; // Default: user not found

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        }
        return userId;
    }

    public boolean updateUserSettings(String email, int dailyGoal, int reminderInterval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAILY_GOAL, dailyGoal);
        values.put(COLUMN_REMINDER_INTERVAL, reminderInterval);

        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public String getUserEmailById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = null;

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL},
                COLUMN_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            }
        }
        return email;
    }

    // ====================== Water Logs Management ====================== //

    public boolean insertWaterLog(String email, String date, int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_AMOUNT, amount);

        long result = db.insert(TABLE_WATER_LOGS, null, values);
        return result != -1;
    }

    public int getDailyWaterIntake(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;

        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_WATER_LOGS +
                " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_DATE + "=?", new String[]{email, date})) {
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        }
        return total;
    }

    public List<String> getWeeklyHistory(String email) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_DATE + ", SUM(" + COLUMN_AMOUNT + ") AS total " +
                        "FROM " + TABLE_WATER_LOGS +
                        " WHERE " + COLUMN_EMAIL + " = ? " +
                        "GROUP BY " + COLUMN_DATE +
                        " ORDER BY " + COLUMN_DATE + " DESC LIMIT 7",
                new String[]{email})) {

            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                history.add(date + ": " + total + " ml");
            }
        }
        return history;
    }

    public Cursor getWeeklyWaterIntake(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COLUMN_DATE + ", SUM(" + COLUMN_AMOUNT + ") AS total " +
                        "FROM " + TABLE_WATER_LOGS +
                        " WHERE " + COLUMN_EMAIL + "=? " +
                        "GROUP BY " + COLUMN_DATE +
                        " ORDER BY " + COLUMN_DATE + " DESC LIMIT 7",
                new String[]{email});
    }

    public int getDailyGoal(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int dailyGoal = 2000; // Default goal

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_DAILY_GOAL},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                dailyGoal = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_GOAL));
            }
        }
        return dailyGoal;
    }

    // ====================== Notification Settings ====================== //

    public boolean saveNotificationTimes(String email, int wakeUpHour, int wakeUpMinute, int bedTimeHour, int bedTimeMinute) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WAKE_UP_HOUR, wakeUpHour);
        values.put(COLUMN_WAKE_UP_MINUTE, wakeUpMinute);
        values.put(COLUMN_BED_TIME_HOUR, bedTimeHour);
        values.put(COLUMN_BED_TIME_MINUTE, bedTimeMinute);

        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public int getReminderInterval(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int interval = 60; // Default reminder interval

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_REMINDER_INTERVAL},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                interval = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_INTERVAL));
            }
        }
        return interval;
    }

    // Add this method to the DatabaseHelper class
    public List<String> getDailyHistory(String email) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to fetch the last 7 days' water intake
            String query = "SELECT " + COLUMN_DATE + ", SUM(" + COLUMN_AMOUNT + ") AS total " +
                    "FROM " + TABLE_WATER_LOGS + " " +
                    "WHERE " + COLUMN_EMAIL + " = ? " +
                    "GROUP BY " + COLUMN_DATE + " " +
                    "ORDER BY " + COLUMN_DATE + " DESC " +
                    "LIMIT 7";

            cursor = db.rawQuery(query, new String[]{email});

            // Process query results
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

}
