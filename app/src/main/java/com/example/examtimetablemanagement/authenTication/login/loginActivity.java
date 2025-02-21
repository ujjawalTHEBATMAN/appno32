package com.example.examtimetablemanagement.authenTication.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.fragments.fragmentHolder;
import com.example.examtimetablemanagement.authenTication.regestration.RegistrationActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    private EditText editTextUserInput;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private SessionManagement sessionManagement;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManagement
        sessionManagement = new SessionManagement(this);

        // If a session already exists, redirect to DashboardActivity immediately.
        if (sessionManagement.isLoggedIn()) {
            startActivity(new Intent(loginActivity.this, fragmentHolder.class));
            finish();
            return;
        }
        editTextUserInput = findViewById(R.id.etUser);
        editTextPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.tvRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String usernameInput = editTextUserInput.getText().toString().trim();
                final String passwordInput = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(usernameInput) || TextUtils.isEmpty(passwordInput)) {
                    Toast.makeText(loginActivity.this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(usernameInput).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String storedPassword = snapshot.child("password").getValue(String.class);
                            if (passwordInput.equals(storedPassword)) {
                                // Retrieve all user data
                                String name = snapshot.child("name").getValue(String.class);
                                String email = snapshot.child("email").getValue(String.class);
                                String userRole = snapshot.child("userRole").getValue(String.class);
                                String profileImage = snapshot.child("image").getValue(String.class);
                                String college = snapshot.child("college").getValue(String.class);
                                String department = snapshot.child("department").getValue(String.class);
                                String semester = snapshot.child("semester").getValue(String.class);
                                Long createdAt = snapshot.child("createdAt").getValue(Long.class);
                                Long lastLogin = snapshot.child("lastLogin").getValue(Long.class);

                                // Create session with the retrieved data (username is used as the unique key)
                                sessionManagement.createLoginSession(
                                        usernameInput,
                                        email,
                                        name,
                                        userRole,
                                        profileImage,
                                        college,
                                        department,
                                        semester,
                                        createdAt != null ? createdAt : 0,
                                        lastLogin != null ? lastLogin : 0
                                );

                                startActivity(new Intent(loginActivity.this, fragmentHolder.class));
                                finish();
                            } else {
                                Toast.makeText(loginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(loginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(loginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, RegistrationActivity.class));
            }
        });
    }
    public static class SessionManagement {
        private static final String PREF_NAME = "UserSession";
        private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
        private static final String KEY_USERNAME = "username";
        private static final String KEY_EMAIL = "email";
        private static final String KEY_NAME = "name";
        private static final String KEY_USER_ROLE = "userRole";
        private static final String KEY_PROFILE_IMAGE = "profileImage";
        private static final String KEY_COLLEGE = "college";
        private static final String KEY_DEPARTMENT = "department";
        private static final String KEY_SEMESTER = "semester";
        private static final String KEY_CREATED_AT = "createdAt";
        private static final String KEY_LAST_LOGIN = "lastLogin";

        private SharedPreferences pref;
        private SharedPreferences.Editor editor;
        private Context context;

        public SessionManagement(Context context) {
            this.context = context;
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
        }

        public void createLoginSession(String username, String email, String name, String userRole,
                                       String profileImage, String college, String department,
                                       String semester, long createdAt, long lastLogin) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_NAME, name);
            editor.putString(KEY_USER_ROLE, userRole);
            editor.putString(KEY_PROFILE_IMAGE, profileImage);
            editor.putString(KEY_COLLEGE, college);
            editor.putString(KEY_DEPARTMENT, department);
            editor.putString(KEY_SEMESTER, semester);
            editor.putLong(KEY_CREATED_AT, createdAt);
            editor.putLong(KEY_LAST_LOGIN, lastLogin);
            editor.apply();
        }




        public boolean isLoggedIn() {
            return pref.getBoolean(KEY_IS_LOGGED_IN, false);
        }






        public String getUsername() { return pref.getString(KEY_USERNAME, null); }
        public String getEmail() { return pref.getString(KEY_EMAIL, null); }
        public String getName() { return pref.getString(KEY_NAME, null); }
        public String getUserRole() { return pref.getString(KEY_USER_ROLE, null); }
        public String getProfileImage() { return pref.getString(KEY_PROFILE_IMAGE, null); }
        public String getCollege() { return pref.getString(KEY_COLLEGE, null); }
        public String getDepartment() { return pref.getString(KEY_DEPARTMENT, null); }
        public String getSemester() { return pref.getString(KEY_SEMESTER, null); }
        public long getCreatedAt() { return pref.getLong(KEY_CREATED_AT, 0); }
        public long getLastLogin() { return pref.getLong(KEY_LAST_LOGIN, 0); }

        public void logout() {
            editor.clear();
            editor.apply();
        }
    }
}
