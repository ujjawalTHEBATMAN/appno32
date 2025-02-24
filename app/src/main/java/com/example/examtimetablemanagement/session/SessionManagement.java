package com.example.examtimetablemanagement.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    private static final String PREF_NAME = "ExamTimetableSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COLLEGE = "college";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManagement(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String username, String college, String department) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_COLLEGE, college);
        editor.putString(KEY_DEPARTMENT, department);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getCollege() {
        return sharedPreferences.getString(KEY_COLLEGE, null);
    }

    public String getDepartment() {
        return sharedPreferences.getString(KEY_DEPARTMENT, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void updateCollegeAndDepartment(String college, String department) {
        editor.putString(KEY_COLLEGE, college);
        editor.putString(KEY_DEPARTMENT, department);
        editor.apply();

    }
}