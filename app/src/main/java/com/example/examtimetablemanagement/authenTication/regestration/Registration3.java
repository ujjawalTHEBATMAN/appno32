package com.example.examtimetablemanagement.authenTication.regestration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examtimetablemanagement.fragments.fragmentHolder;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class Registration3 extends AppCompatActivity {

    private Spinner spinnerDepartment, spinnerSemester;
    private Button btnRegister;
    private String regName, regEmail, regPassword, regRole, selectedCollege, images;
    private String username;
    private String selectedDepartment, selectedSemester;
    private DatabaseReference usersRef;
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
        selectedCollege = intent.getStringExtra("selectedCollege");
        regName = intent.getStringExtra("name");   // Also used as username
        regEmail = intent.getStringExtra("email");
        regPassword = intent.getStringExtra("password");
        regRole = intent.getStringExtra("role");
        images = intent.getStringExtra("imagess");
        username = regName; // Using the name as the username key

        String[] departments = {"BCA", "BCom", "BSc"};
        ArrayAdapter<String> adapterDepartment = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapterDepartment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapterDepartment);





        String[] semesters = {"1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> adapterSemester = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapterSemester);







        usersRef = FirebaseDatabase.getInstance().getReference("users");
        sessionManagement = new loginActivity.SessionManagement(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDepartment = spinnerDepartment.getSelectedItem().toString();
                selectedSemester = spinnerSemester.getSelectedItem().toString();

                if (username == null || username.isEmpty()) {
                    Toast.makeText(Registration3.this, "Username is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put("name", regName);
                userData.put("username", username);
                userData.put("email", regEmail);
                userData.put("password", regPassword);
                userData.put("image", images);
                userData.put("college", selectedCollege);
                userData.put("department", selectedDepartment);
                userData.put("semester", selectedSemester);
                userData.put("userRole", regRole);
                userData.put("createdAt", ServerValue.TIMESTAMP);
                userData.put("lastLogin", ServerValue.TIMESTAMP);
                usersRef.child(username).setValue(userData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                sessionManagement.createLoginSession(
                                        username,
                                        regEmail,
                                        regName,
                                        regRole,
                                        images,
                                        selectedCollege,
                                        selectedDepartment,
                                        selectedSemester,
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis()
                                );
                                Intent dashboardIntent = new Intent(Registration3.this, fragmentHolder.class);
                                startActivity(dashboardIntent);
                                finish();
                            } else {
                                Toast.makeText(Registration3.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
