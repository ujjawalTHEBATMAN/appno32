package com.example.examtimetablemanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.User;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoFragment extends Fragment {

    private ShimmerFrameLayout shimmerLayout;
    private CircleImageView profileImage;
    private MaterialTextView nameText, emailText, collegeText, departmentText,
            semesterText, userRoleText, createdAtText, lastLoginText;
    private String userId;
    private DatabaseReference userRef;

    public static UserInfoFragment newInstance(String userId) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupProfileImage();
        loadUserData();

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

        // Hide the edit profile FAB as this is view-only

    }

    private void setupProfileImage() {
        shimmerLayout.startShimmer();
    }

    private void loadUserData() {
        if (userId == null) {
            showMessage("User ID not provided");
            return;
        }

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        updateUI(user);
                    }
                }
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Error loading user data: " + error.getMessage());
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
        });
    }

    private void updateUI(User user) {
        if (getContext() == null) return;

        nameText.setText(user.getName());
        emailText.setText(user.getEmail());
        collegeText.setText(user.getCollege());
        departmentText.setText(user.getDepartment());
        semesterText.setText(user.getSemester());
        userRoleText.setText(user.getUserRole());
        createdAtText.setText(" "+user.getCreatedAt());
        lastLoginText.setText(" "+user.getLastLogin());

        Glide.with(getContext())
                .load(user.getImage())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(profileImage);
    }

    private void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}