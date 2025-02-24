package com.example.examtimetablemanagement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.*;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;
import java.util.Map;

public class profileFragment extends Fragment {

    // View declarations
    private ShimmerFrameLayout shimmerLayout;
    private CircleImageView profileImage;
    private MaterialButton logoutButton;
    private loginActivity.SessionManagement sessionManagement;
    private MaterialTextView nameText, emailText, collegeText, departmentText,
            semesterText, userRoleText, createdAtText, lastLoginText;
    private ExtendedFloatingActionButton editProfileFab;
    private DatabaseReference userRef;
    private View editProfileCardView;
    private boolean isEditProfileCardVisible = false;

    public profileFragment() {
        // Required empty public constructor
    }

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
        editProfileFab = view.findViewById(R.id.editProfileFab);
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
        if (getView() == null) return;

        if (editProfileCardView == null) {
            editProfileCardView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.edit_profile_card, (ViewGroup) getView(), false);
            ((ViewGroup) getView()).addView(editProfileCardView);
            setupEditProfileCard();
        }

        editProfileCardView.setVisibility(View.VISIBLE);
        editProfileCardView.setAlpha(0f);
        editProfileCardView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        isEditProfileCardVisible = true;
    }

    private void setupEditProfileCard() {
        TextInputEditText collegeInput = editProfileCardView.findViewById(R.id.collegeInput);
        TextInputEditText departmentInput = editProfileCardView.findViewById(R.id.departmentInput);
        TextInputEditText semesterInput = editProfileCardView.findViewById(R.id.semesterInput);

        collegeInput.setText(cleanLabel(collegeText.getText().toString()));
        departmentInput.setText(cleanLabel(departmentText.getText().toString()));
        semesterInput.setText(cleanLabel(semesterText.getText().toString()));

        MaterialButton cancelButton = editProfileCardView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = editProfileCardView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> hideEditProfileCard());
        saveButton.setOnClickListener(v -> validateAndUpdate(
                collegeInput.getText().toString().trim(),
                departmentInput.getText().toString().trim(),
                semesterInput.getText().toString().trim()
        ));
    }

    private String cleanLabel(String text) {
        return text.replaceAll("^\\w+:\\s*", "");
    }

    private void validateAndUpdate(String college, String department, String semester) {
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

    private void updateUserProfile(String college, String department, String semester) {
        String sessionUsername = sessionManagement.getUsername();
        if (sessionUsername == null) {
            showMessage("Session expired - please login again");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sessionUsername);

        Map<String, Object> updates = new HashMap<>();
        updates.put("college", college);
        updates.put("department", department);
        updates.put("semester", semester);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
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
        if (editProfileCardView != null) {
            editProfileCardView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        editProfileCardView.setVisibility(View.GONE);
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