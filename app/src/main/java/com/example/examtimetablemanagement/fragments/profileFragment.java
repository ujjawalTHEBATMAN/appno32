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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileFragment extends Fragment {

    private ShimmerFrameLayout shimmerLayout;
    private CircleImageView profileImage;
    private MaterialTextView nameText, emailText, collegeText, departmentText,
            semesterText, userRoleText, createdAtText, lastLoginText;
    private ExtendedFloatingActionButton editProfileFab;
    private DatabaseReference userRef;
    private loginActivity.SessionManagement sessionManagement;

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

        sessionManagement = new loginActivity.SessionManagement(getContext());
    }

    private void setupProfileImage() {
        shimmerLayout.startShimmer();

        profileImage.setOnClickListener(v -> {showFullScreenImage();
        });
    }

    private void loadProfileData() {
        String sessionUsername = sessionManagement.getUsername();
        if (sessionUsername == null) {
            showMessage("no more session");
            return;
        }

        DatabaseReference currentUserRef = userRef.child(sessionUsername);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TransitionManager.beginDelayedTransition((ViewGroup) getView());

                    updateUIWithUserData(snapshot);
                    loadProfileImage(snapshot.child("image").getValue(String.class));

                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                } else {
                    showMessage("user informtion not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("database fucked : " + error.getMessage());
            }
        });
    }

    private void updateUIWithUserData(DataSnapshot snapshot) {
        String retrievedEmail = snapshot.child("email").getValue(String.class);
        String retrievedName = snapshot.child("name").getValue(String.class);
        String retrievedUserRole = snapshot.child("userRole").getValue(String.class);
        String retrievedCollege = snapshot.child("college").getValue(String.class);
        String retrievedDepartment = snapshot.child("department").getValue(String.class);
        String retrievedSemester = snapshot.child("semester").getValue(String.class);

        Long retrievedCreatedAt = snapshot.child("createdAt").getValue(Long.class);
        Long retrievedLastLogin = snapshot.child("lastLogin").getValue(Long.class);

        nameText.setText(retrievedName != null ? retrievedName : "N/A");
        emailText.setText(retrievedEmail != null ? retrievedEmail : "N/A");
        userRoleText.setText(retrievedUserRole != null ? retrievedUserRole : "N/A");
        collegeText.setText("College: " + (retrievedCollege != null ? retrievedCollege : "N/A"));
        departmentText.setText("Department: " + (retrievedDepartment != null ? retrievedDepartment : "N/A"));
        semesterText.setText("Semester: " + (retrievedSemester != null ? retrievedSemester : "N/A"));
        createdAtText.setText("Created At: " + (retrievedCreatedAt != null ? retrievedCreatedAt : "N/A"));
        lastLoginText.setText("Last Login: " + (retrievedLastLogin != null ? retrievedLastLogin : "N/A"));
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(requireContext())
                .load(imageUrl != null ? imageUrl : "default/collageimage.png")
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.collageimage)
                .into(profileImage);
    }

    private View editProfileCardView;
    private boolean isEditProfileCardVisible = false;

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

            editProfileCardView.setOnClickListener(v -> hideEditProfileCard());




            editProfileCardView.findViewById(R.id.editProfileCardContent)
                    .setOnClickListener(v -> {

                    });
        }



        // anime
        editProfileCardView.setVisibility(View.VISIBLE);
        editProfileCardView.setAlpha(0f);
        editProfileCardView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        isEditProfileCardVisible = true;
    }

    private void setupEditProfileCard() {
        TextInputEditText nameInput = editProfileCardView.findViewById(R.id.nameInput);
        TextInputEditText collegeInput = editProfileCardView.findViewById(R.id.collegeInput);
        TextInputEditText departmentInput = editProfileCardView.findViewById(R.id.departmentInput);
        TextInputEditText semesterInput = editProfileCardView.findViewById(R.id.semesterInput);

        // setting current values
        nameInput.setText(nameText.getText());
        nameInput.setEnabled(false);
        collegeInput.setText(collegeText.getText().toString().replace("College: ", ""));
        departmentInput.setText(departmentText.getText().toString().replace("Department: ", ""));
        semesterInput.setText(semesterText.getText().toString().replace("Semester: ", ""));

        MaterialButton cancelButton = editProfileCardView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = editProfileCardView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> hideEditProfileCard());

        saveButton.setOnClickListener(v -> {
            String college = collegeInput.getText().toString().trim();
            String department = departmentInput.getText().toString().trim();
            String semester = semesterInput.getText().toString().trim();

            if (validateInputs(college, department, semester)) {
                updateUserProfile(college, department, semester);
            }
        });
    }

    private boolean validateInputs(String college, String department, String semester) {
        if (college.isEmpty() || department.isEmpty() || semester.isEmpty()) {
            showMessage("fill all");
            return false;
        }

        try {
            int semesterNum = Integer.parseInt(semester);
            if (semesterNum < 1 || semesterNum > 6) {
                showMessage("semester should be 1 and 6");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("not right semester");
            return false;
        }

        return true;
    }

    private void updateUserProfile(String college, String department, String semester) {
        String sessionUsername = sessionManagement.getUsername();
        if (sessionUsername == null) {
            showMessage("session exired login again ");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(sessionUsername);
        Map<String, Object> updates = new HashMap<>();
        updates.put("college", college);
        updates.put("department", department);
        updates.put("semester", semester);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showMessage("profileUpdated");
                hideEditProfileCard();
                loadProfileData();
            } else {
                showMessage("failed updadation: " + task.getException().getMessage());
            }
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
                        editProfileFab.setEnabled(true); // Re-enable FAB when card is hidden
                    })
                    .start();
        }
    }

    private void showFullScreenImage() {
        showMessage("fullscreen view coming soon");
    }
    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}