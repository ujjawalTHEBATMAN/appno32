package com.example.examtimetablemanagement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;
import java.util.Map;

public class profileFragment extends Fragment {

    private ShimmerFrameLayout shimmerLayout;
    private CircleImageView profileImage;
    private LinearLayout logoutButton;
    private loginActivity.SessionManagement sessionManagement;
    private MaterialTextView nameText, emailText, collegeText, departmentText,
            semesterText, userRoleText, createdAtText, lastLoginText;
    private FloatingActionButton editProfileFab;
    private DatabaseReference userRef;

    private View editProfileCardContainer;
    private boolean isEditProfileCardVisible = false;
    private boolean isEditProfileCardSetup = false;

    public profileFragment() {}

    public static profileFragment newInstance() {
        return new profileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeViews(view);
        setupProfileImage();
        loadProfileData();
        setupFabButton();
        setupLogoutButton();
        return view;
    }

    private void initializeViews(View view) {
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        profileImage = view.findViewById(R.id.profileImage);
        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        collegeText = view.findViewById(R.id.collegeText);
        departmentText = view.findViewById(R.id.departmentText);
        semesterText = view.findViewById(R.id.semesterText);
        userRoleText = view.findViewById(R.id.roleText);
        createdAtText = view.findViewById(R.id.createdAtText);
        lastLoginText = view.findViewById(R.id.lastLoginText);
        editProfileFab = view.findViewById(R.id.editProfileFAB);
        logoutButton = view.findViewById(R.id.logoutButton);
        sessionManagement = new loginActivity.SessionManagement(requireContext());
    }

    private void setupProfileImage() {
        shimmerLayout.startShimmer();
        profileImage.setOnClickListener(v -> showFullScreenImage());
    }

    private void loadProfileData() {
        String sessionUsername = sessionManagement.getUsername();
        if (sessionUsername == null) {
            showMessage("Session expired");
            return;
        }
        userRef.child(sessionUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TransitionManager.beginDelayedTransition((ViewGroup) getView());
                    updateUIWithUserData(snapshot);
                    loadProfileImage(snapshot.child("image").getValue(String.class));
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                } else {
                    showMessage("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Database error: " + error.getMessage());
            }
        });
    }

    private void updateUIWithUserData(DataSnapshot snapshot) {
        nameText.setText(getValueOrNA(snapshot, "name"));
        emailText.setText(getValueOrNA(snapshot, "email"));
        userRoleText.setText(getValueOrNA(snapshot, "userRole"));
        collegeText.setText(formatValue("College", snapshot, "college"));
        departmentText.setText(formatValue("Department", snapshot, "department"));
        semesterText.setText(formatValue("Semester", snapshot, "semester"));
        createdAtText.setText(formatValue("Created At", snapshot, "createdAt"));
        lastLoginText.setText(formatValue("Last Login", snapshot, "lastLogin"));
    }
    private String getValueOrNA(DataSnapshot snapshot, String key) {
        return snapshot.child(key).exists() ?
                snapshot.child(key).getValue().toString() : "N/A";
    }






    private String formatValue(String prefix, DataSnapshot snapshot, String key) {
        return String.format("%s: %s", prefix, getValueOrNA(snapshot, key));
    }
    private void loadProfileImage(String imageUrl) {
        Glide.with(requireContext())
                .load(imageUrl != null ? imageUrl : R.drawable.collageimage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.collageimage)
                .into(profileImage);
    }

    private void setupFabButton() {
        editProfileFab.setOnClickListener(v -> {
            if (!isEditProfileCardVisible) {
                editProfileFab.setEnabled(false);
                showEditProfileCard();
            }
        });
    }

    private void showEditProfileCard() {
        if (editProfileCardContainer == null) return;


        ViewGroup container = (ViewGroup) editProfileCardContainer;
        if (container.getChildCount() == 0) {
            LayoutInflater.from(requireContext()).inflate(R.layout.edit_profile_card, container, true);
        }

        editProfileCardContainer.setVisibility(View.VISIBLE);
        editProfileCardContainer.setAlpha(0f);
        editProfileCardContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction(() -> {
                    isEditProfileCardVisible = true;
                    editProfileFab.setEnabled(true);
                    if (!isEditProfileCardSetup) {
                        setupEditProfileCard();
                        isEditProfileCardSetup = true;
                    }
                })
                .start();
    }

    private void setupEditProfileCard() {
        ViewGroup container = (ViewGroup) editProfileCardContainer;
        View cardView = container.getChildAt(0);
        TextInputEditText nameInput = cardView.findViewById(R.id.nameInput);
        TextInputEditText collegeInput = cardView.findViewById(R.id.collegeInput);
        TextInputEditText departmentInput = cardView.findViewById(R.id.departmentInput);
        TextInputEditText semesterInput = cardView.findViewById(R.id.semesterInput);







        nameInput.setText(cleanLabel(nameText.getText().toString()));
        collegeInput.setText(cleanLabel(collegeText.getText().toString()));
        departmentInput.setText(cleanLabel(departmentText.getText().toString()));
        semesterInput.setText(cleanLabel(semesterText.getText().toString()));
        MaterialButton cancelButton = cardView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = cardView.findViewById(R.id.saveButton);
        cancelButton.setOnClickListener(v -> hideEditProfileCard());
        saveButton.setOnClickListener(v -> validateAndUpdate(
                nameInput.getText().toString().trim(),
                collegeInput.getText().toString().trim(),
                departmentInput.getText().toString().trim(),
                semesterInput.getText().toString().trim()
        ));
    }
    private String cleanLabel(String text) {
        int colonIndex = text.indexOf(":");
        return colonIndex != -1 ? text.substring(colonIndex + 1).trim() : text.trim();
    }

    private void validateAndUpdate(String name, String college, String department, String semester) {
        if (validateInputs(college, department, semester)) {
            updateUserProfile(college, department, semester);
        }
    }

    private boolean validateInputs(String college, String department, String semester) {
        if (college.isEmpty() || department.isEmpty() || semester.isEmpty()) {
            showMessage("Please fill all fields");
            return false;
        }
        try {
            int semesterNum = Integer.parseInt(semester);
            if (semesterNum < 1 || semesterNum > 6) {
                showMessage("Semester must be between 1 and 6");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Invalid semester format");
            return false;
        }
        return true;
    }

    // Reuses the already initialized userRef to update the profile information.
    private void updateUserProfile(String college, String department, String semester) {
        String sessionUsername = sessionManagement.getUsername();
        if (sessionUsername == null) {
            showMessage("Session expired - please login again");
            return;
        }

        // Reuse the userRef field and get the current user node
        DatabaseReference currentUserRef = userRef.child(sessionUsername);

        Map<String, Object> updates = new HashMap<>();
        updates.put("college", college);
        updates.put("department", department);
        updates.put("semester", semester);

        currentUserRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showMessage("Profile updated");
                hideEditProfileCard();
                loadProfileData();
            } else {
                showMessage("Update failed: " + task.getException().getMessage());
            }
        });
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            sessionManagement.logout();
            Intent intent = new Intent(requireActivity(), loginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finishAffinity();
        });
    }

    private void hideEditProfileCard() {
        if (editProfileCardContainer != null) {
            editProfileCardContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        editProfileCardContainer.setVisibility(View.GONE);
                        isEditProfileCardVisible = false;
                        editProfileFab.setEnabled(true);
                    })
                    .start();
        }
    }

    private void showFullScreenImage() {
        showMessage("Fullscreen view coming soon");
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
