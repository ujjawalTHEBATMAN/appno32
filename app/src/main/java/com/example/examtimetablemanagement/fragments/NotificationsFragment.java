package com.example.examtimetablemanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.NotificationsAdapter;
import com.example.examtimetablemanagement.models.Notification;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator loadingIndicator;
    private TextView emptyStateText;
    
    private NotificationsAdapter adapter;
    private DatabaseReference notificationsRef;
    private ChildEventListener notificationsListener;
    private String userCollege = "YourCollege"; // TODO: Get from user's profile
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFirebaseListener();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        emptyStateText = view.findViewById(R.id.emptyStateText);
    }
    
    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            adapter.clearNotifications();
            setupFirebaseListener();
        });
    }
    
    private void setupFirebaseListener() {
        if (notificationsListener != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
        
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        Query query = notificationsRef.orderByChild("college").equalTo(userCollege);
        
        showLoading(true);
        
        notificationsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Notification notification = snapshot.getValue(Notification.class);
                if (notification != null) {
                    notification.setId(snapshot.getKey());
                    adapter.addNotification(notification);
                    showEmptyState(false);
                }
                showLoading(false);
            }
            
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Notification notification = snapshot.getValue(Notification.class);
                if (notification != null) {
                    notification.setId(snapshot.getKey());
                    adapter.updateNotification(notification);
                }
            }
            
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String notificationId = snapshot.getKey();
                if (notificationId != null) {
                    adapter.removeNotification(notificationId);
                    showEmptyState(adapter.getItemCount() == 0);
                }
            }
            
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load notifications: " + error.getMessage());
                showLoading(false);
            }
        };
        
        query.addChildEventListener(notificationsListener);
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }
    
    private void showEmptyState(boolean show) {
        emptyStateText.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationsListener != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
    }
}