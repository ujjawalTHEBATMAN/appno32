package com.example.examtimetablemanagement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.UserAdapter;
import com.example.examtimetablemanagement.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class adminFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private ChipGroup roleFilterChipGroup;
    private CircularProgressIndicator progressIndicator;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupRoleFilter();
        fetchUsers();
        return view;
    }

    private void initializeViews(View view) {
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        roleFilterChipGroup = view.findViewById(R.id.roleFilterChipGroup);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(requireContext(), this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void setupRoleFilter() {
        roleFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                String selectedRole = chip.getText().toString();
                userAdapter.filterByRole(selectedRole);
            }
        });
    }

    private void fetchUsers() {
        progressIndicator.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                userAdapter.setUsers(users);
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressIndicator.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onUserClick(User user) {
    }
}