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
import com.example.examtimetablemanagement.fragments.adminFragment;
import com.example.examtimetablemanagement.fragments.generatorFragment;
import com.example.examtimetablemanagement.fragments.homeFragment;
import com.example.examtimetablemanagement.fragments.notificationFragment;
import com.example.examtimetablemanagement.fragments.profileFragment;
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
        userRole = sessionManagement.getUserRole();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setElevation(8f);
        ViewCompat.setElevation(bottomNav, 8f);

        // Inflate menu based on user role
        switch (userRole.toLowerCase()) {
            case "student":
                bottomNav.inflateMenu(R.menu.bottom_nav_student);
                break;
            case "teacher":
                bottomNav.inflateMenu(R.menu.bottom_nav_teacher);
                break;
            case "admin":
                bottomNav.inflateMenu(R.menu.bottom_nav_admin);
                break;
            default:
                bottomNav.inflateMenu(R.menu.bottom_nav_student);
                break;
        }


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Replace switch with if-else to handle non-constant resource IDs
            if (itemId == R.id.nav_home) {
                selectedFragment = new homeFragment();
            } else if (itemId == R.id.nav_notification) {
                selectedFragment = new notificationFragment();
            } else if (itemId == R.id.nav_generator) {
                selectedFragment = new generatorFragment();
            } else if (itemId == R.id.nav_admin) {
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

        // Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new homeFragment())
                    .commit();
        }
    }
}
