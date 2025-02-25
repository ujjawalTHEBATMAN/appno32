package com.example.examtimetablemanagement.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class fragmentHolder extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private loginActivity.SessionManagement sessionManagement;
    private String userRole;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManagement = new loginActivity.SessionManagement(this);
        userRole = sessionManagement.getUserRole().toLowerCase();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setElevation(8f);
        ViewCompat.setElevation(bottomNav, 8f);

        // Inflate menu based on user role
        Fragment defaultFragment;
        switch (userRole) {
            case "student":
                bottomNav.inflateMenu(R.menu.bottom_nav_student);
                defaultFragment = new homeStudentFragment();
                break;
            case "teacher":
                bottomNav.inflateMenu(R.menu.bottom_nav_teacher);
                defaultFragment = new homeFragment();
                break;
            case "admin":
                bottomNav.inflateMenu(R.menu.bottom_nav_admin);
                    defaultFragment = new homeAdminFragment();
                break;
            default:
                bottomNav.inflateMenu(R.menu.bottom_nav_student);
                defaultFragment = new homeStudentFragment();
                break;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = userRole.equals("student") ? new homeStudentFragment()
                        : userRole.equals("teacher") ? new homeFragment()
                        : new homeAdminFragment();
            } else if (itemId == R.id.nav_notification) {
                selectedFragment = new notificationFragment();
            } else if (itemId == R.id.nav_generator && userRole.equals("teacher")) {
                selectedFragment = new generatorFragment();
            } else if (itemId == R.id.nav_admin && userRole.equals("admin")) {
                selectedFragment = new adminFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new profileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, defaultFragment)
                    .commit();
        }
    }
}
