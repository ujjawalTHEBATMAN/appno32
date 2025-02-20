package com.example.examtimetablemanagement.authenTication.regestration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.examtimetablemanagement.DashboardActivity.DashboardActivity;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.regestration.Registration2CardUtils.College;
import com.example.examtimetablemanagement.authenTication.regestration.Registration2CardUtils.CollegeAdapter;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Registration2 extends AppCompatActivity {

    private RecyclerView recyclerViewColleges;
    private CollegeAdapter collegeAdapter;
    private List<College> collegeList;
    private FloatingActionButton fabAddCollege;
    private Button btnNext;
    private String selectedCollegeName = null;
    private String role;

    // regestration old info are save on this variable
    private String regName, regEmail, regPassword,regImagesss;

    private DatabaseReference collegesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);

        recyclerViewColleges = findViewById(R.id.recyclerViewColleges);
        fabAddCollege = findViewById(R.id.fabAddCollege);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false);

        // retrieve registration info from previous activity
        role = getIntent().getStringExtra("role");
        regName = getIntent().getStringExtra("name");
        regEmail = getIntent().getStringExtra("email");
        regPassword = getIntent().getStringExtra("password");
        regImagesss = getIntent().getStringExtra("imageString");

        // Only show to admin
        if (role != null && role.toLowerCase(Locale.US).equals("admin")) {
            fabAddCollege.setVisibility(FloatingActionButton.VISIBLE);
        } else {
            fabAddCollege.setVisibility(FloatingActionButton.GONE);
        }

        collegeList = new ArrayList<>();
        collegeAdapter = new CollegeAdapter(
                this,
                collegeList,
                college -> {
                    selectedCollegeName = college.getName();
                    btnNext.setEnabled(true);
                },
                college -> {
                    if (role != null && role.toLowerCase(Locale.US).equals("admin")) {
                        showDeleteCollegeDialog(college);
                    }
                }
        );
        recyclerViewColleges.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewColleges.setAdapter(collegeAdapter);

        collegesReference = FirebaseDatabase.getInstance().getReference("colleges");
        collegesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<College> newList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String name = ds.child("name").getValue(String.class);
                    String image = ds.child("image").getValue(String.class);
                    newList.add(new College(id, name, image));
                }
                collegeAdapter.updateData(newList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Registration2.this, "Error loading colleges", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddCollege.setOnClickListener(v -> showAddCollegeDialog());

        btnNext.setOnClickListener(v -> {
            if (selectedCollegeName == null) {
                Toast.makeText(Registration2.this, "Select a college", Toast.LENGTH_SHORT).show();
                return;
            }
            if (role != null && role.toLowerCase(Locale.US).equals("admin")) {
                saveAdminDataAndNavigate();
            } else {
                //  non admin intent directly to dashboard
                Intent intent = new Intent(Registration2.this, Registration3.class);
                intent.putExtra("selectedCollege", selectedCollegeName);

                intent.putExtra("name", regName);
                intent.putExtra("email", regEmail);
                intent.putExtra("password", regPassword);
                intent.putExtra("role", role);
                intent.putExtra("imagess",regImagesss);
                startActivity(intent);
            }
        });
    }

    private void showDeleteCollegeDialog(College college) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete College");
        builder.setMessage("Are you sure you want to delete \"" + college.getName() + "\"?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove college from Firebase Database
                collegesReference.child(college.getId()).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Registration2.this, "College deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Registration2.this, "Failed to delete college", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showAddCollegeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add College");
        final EditText input = new EditText(this);
        input.setHint("Enter College Name");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String collegeName = input.getText().toString().trim();
                if (collegeName.isEmpty()) {
                    Toast.makeText(Registration2.this, "College name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                addCollegeToDatabase(collegeName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addCollegeToDatabase(String collegeName) {
        // Generate a new unique key.
        String collegeId = collegesReference.push().getKey();
        if (collegeId != null) {
            College newCollege = new College(collegeId, collegeName, "default/collageimage.png");
            collegesReference.child(collegeId).setValue(newCollege);
        }
    }

    private void saveAdminDataAndNavigate() {
        // Prepare user data to save into Firebase Realtime Database
        DatabaseReference userInformationRef = FirebaseDatabase.getInstance().getReference("userInformation");
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", regName);
        userData.put("email", regEmail);
        userData.put("userRole", role);
        userData.put("college", selectedCollegeName);
        userData.put("createdAt", ServerValue.TIMESTAMP);
        userData.put("lastLogin", ServerValue.TIMESTAMP);
        userData.put("imagess",regImagesss);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser() != null
                ? firebaseAuth.getCurrentUser().getUid()
                : userInformationRef.push().getKey();

        if (userId == null) {
            Toast.makeText(this, "Error generating user id", Toast.LENGTH_SHORT).show();
            return;
        }

        userInformationRef.child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create a session and navigate to Dashboard
                        loginActivity.SessionManagement sessionManagement = new loginActivity.SessionManagement(Registration2.this);
                        sessionManagement.createLoginSession(userId, regEmail, regName, role, regImagesss);
                        Intent dashboardIntent = new Intent(Registration2.this, DashboardActivity.class);
                        startActivity(dashboardIntent);
                        finish();
                    } else {
                        Toast.makeText(Registration2.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
