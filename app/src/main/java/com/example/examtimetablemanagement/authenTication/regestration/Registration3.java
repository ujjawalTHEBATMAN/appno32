package com.example.examtimetablemanagement.authenTication.regestration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examtimetablemanagement.DashboardActivity.DashboardActivity;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class Registration3 extends AppCompatActivity {

    private Spinner spinnerDepartment, spinnerSemester;
    private Button btnRegister;
    private String regName, regEmail, regPassword, regRole, selectedCollege,images;
    private String selectedDepartment, selectedSemester;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userInformationRef;
    private loginActivity.SessionManagement sessionManagement;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration3);

        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        btnRegister = findViewById(R.id.btnRegister);

        Intent intent = getIntent();
        regName = intent.getStringExtra("name");
        regEmail = intent.getStringExtra("email");
        regPassword = intent.getStringExtra("password");
        regRole = intent.getStringExtra("role");
        selectedCollege = intent.getStringExtra("selectedCollege");
        images=intent.getStringExtra("imagess");

        //set up
        String[] departments = {"BCA", "BCom", "BSc"};
        ArrayAdapter<String> adapterDepartment = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapterDepartment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapterDepartment);

        String[] semesters = {"1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> adapterSemester = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapterSemester);

        //  SessionManagement class and firebase
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        userInformationRef = FirebaseDatabase.getInstance().getReference("userInformation");
        sessionManagement = new loginActivity.SessionManagement(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDepartment = spinnerDepartment.getSelectedItem().toString();
                selectedSemester = spinnerSemester.getSelectedItem().toString();
                // set data on map
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", regName);
                userData.put("email", regEmail);
                userData.put("userRole", regRole);
                userData.put("college", selectedCollege);
                userData.put("department", selectedDepartment);
                userData.put("semester", selectedSemester);
                userData.put("createdAt", ServerValue.TIMESTAMP);
                userData.put("image",images);
                userData.put("lastLogin", ServerValue.TIMESTAMP);
                String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : userInformationRef.push().getKey();

                userInformationRef.child(userId).setValue(userData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Create the login session with stored user details
                                    sessionManagement.createLoginSession(userId, regEmail, regName, regRole, images);
                                    // Navigate to DashboardActivity
                                    Intent dashboardIntent = new Intent(Registration3.this, DashboardActivity.class);
                                    startActivity(dashboardIntent);
                                    finish();
                                } else {
                                    Toast.makeText(Registration3.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
