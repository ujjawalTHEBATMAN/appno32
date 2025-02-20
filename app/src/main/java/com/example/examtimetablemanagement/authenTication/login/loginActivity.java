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
import com.example.examtimetablemanagement.DashboardActivity.DashboardActivity;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.regestration.RegistrationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    private EditText editTextUserName;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth firebaseAuthInstance;
    private SessionManagement sessionManagement;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth and SessionManagement
        firebaseAuthInstance = FirebaseAuth.getInstance();
        sessionManagement = new SessionManagement(this);

        // If a session already exists, redirect to DashboardActivity immediately.
        if (sessionManagement.isLoggedIn()) {
            startActivity(new Intent(loginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        editTextUserName = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.tvRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = ((EditText)findViewById(R.id.etUser)).getText().toString().trim();
                String userPasswordInput = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(userInput) || TextUtils.isEmpty(userPasswordInput)) {
                    Toast.makeText(loginActivity.this, "Please enter your email/name and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                // email validation
                if (userInput.contains("@")) {
                    firebaseAuthInstance.signInWithEmailAndPassword(userInput, userPasswordInput)
                            .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> signInTask) {
                                    if (signInTask.isSuccessful()) {
                                        sessionManagement.createLoginSession(
                                                firebaseAuthInstance.getCurrentUser().getUid(),
                                                userInput,
                                                "UserName",
                                                "userRole",
                                                "default/collageimage.png"
                                        );
                                        startActivity(new Intent(loginActivity.this, DashboardActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(loginActivity.this, "Username/email or password is incorrect", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Input
                    DatabaseReference userInformationReference = FirebaseDatabase.getInstance().getReference("userInformation");
                    Query usernameQuery = userInformationReference.orderByChild("name").equalTo(userInput);
                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String retrievedEmail = null;
                                String retrievedName = null;
                                String retrievedUserRole = null;
                                String retrievedProfileImage = null;
                                String userId = null;
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    retrievedEmail = userSnapshot.child("email").getValue(String.class);
                                    retrievedName = userSnapshot.child("name").getValue(String.class);
                                    retrievedUserRole = userSnapshot.child("userRole").getValue(String.class);
                                    retrievedProfileImage = userSnapshot.child("profileImage").getValue(String.class);
                                    userId = userSnapshot.getKey();
                                    break;
                                }
                                if (retrievedEmail != null) {
                                    String finalUserId = userId;
                                    String finalRetrievedEmail = retrievedEmail;
                                    String finalRetrievedName = retrievedName;
                                    String finalRetrievedUserRole = retrievedUserRole;
                                    String finalRetrievedProfileImage = retrievedProfileImage;
                                    firebaseAuthInstance.signInWithEmailAndPassword(retrievedEmail, userPasswordInput)
                                            .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> signInTask) {
                                                    if (signInTask.isSuccessful()) {
                                                        sessionManagement.createLoginSession(
                                                                finalUserId,
                                                                finalRetrievedEmail,
                                                                finalRetrievedName,
                                                                finalRetrievedUserRole,
                                                                finalRetrievedProfileImage != null ? finalRetrievedProfileImage : "default/collageimage.png"
                                                        );
                                                        startActivity(new Intent(loginActivity.this, DashboardActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(loginActivity.this, "Username or password is incorrect", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(loginActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(loginActivity.this, "User information not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(loginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
        private static final String KEY_USER_ID = "userId";
        private static final String KEY_EMAIL = "email";
        private static final String KEY_NAME = "name";
        private static final String KEY_USER_ROLE = "userRole";
        private static final String KEY_PROFILE_IMAGE = "profileImage";
        private static final String KEY_LAST_LOGIN = "lastLogin";

        private SharedPreferences pref;
        private SharedPreferences.Editor editor;
        private Context context;

        public SessionManagement(Context context) {
            this.context = context;
            pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            editor = pref.edit();
        }
        public void createLoginSession(String userId, String email, String name, String userRole, String profileImage) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USER_ID, userId);
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_NAME, name);
            editor.putString(KEY_USER_ROLE, userRole);
            editor.putString(KEY_PROFILE_IMAGE, profileImage);
            editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis());
            editor.apply();
        }

        public boolean isLoggedIn() {
            return pref.getBoolean(KEY_IS_LOGGED_IN, false);
        }
        public String getUserId() {
            return pref.getString(KEY_USER_ID, null);
        }
        public String getEmail() {
            return pref.getString(KEY_EMAIL, null);
        }
        public String getName() {
            return pref.getString(KEY_NAME, null);
        }
        public String getUserRole() {
            return pref.getString(KEY_USER_ROLE, null);
        }
        public String getProfileImage() {
            return pref.getString(KEY_PROFILE_IMAGE, null);
        }
        public long getLastLoginTime() {
            return pref.getLong(KEY_LAST_LOGIN, 0);
        }
        public void logout() {
            editor.clear();
            editor.apply();
        }
    }
}
