package com.example.examtimetablemanagement.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.NotificationAdapter;
import com.example.examtimetablemanagement.models.Notification;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class notificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener, NotificationAdapter.OnNotificationLongClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private CircularProgressIndicator loadingIndicator;
    private MaterialTextView emptyStateText;
    private DatabaseReference notificationsRef;
    private loginActivity.SessionManagement sessionManagement;
    private String userCollege;
    private String userRole;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        
        setupSessionManagement();
        setupRecyclerView();
        setupFirebase();
        loadNotifications();
        
        return view;
    }

    private void setupSessionManagement() {
        sessionManagement = new loginActivity.SessionManagement(requireContext());
        String username = sessionManagement.getUsername();
        
        // Fetch latest user details from Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userCollege = snapshot.child("college").getValue(String.class);
                    userRole = snapshot.child("userRole").getValue(String.class);
                    loadNotifications(); // Reload notifications with updated user data
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Fallback to session data if Firebase query fails
                userCollege = sessionManagement.getCollege();
                userRole = sessionManagement.getUserRole();
                loadNotifications();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFirebase() {
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
    }

    private void loadNotifications() {
        if (userRole == null || userCollege == null) {
            return; // Wait for user data to be fetched
        }
        
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        Query query;
        if ("Admin".equals(userRole)) {
            // Admin can see all notifications
            query = notificationsRef.orderByChild("timestamp").limitToLast(50);
        } else {
            // Regular users can only see notifications from their college
            query = notificationsRef.orderByChild("collegeName").equalTo(userCollege).limitToLast(50);
        }
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notification.setId(snapshot.getKey());
                        notifications.add(0, notification);
                    }
                }

                loadingIndicator.setVisibility(View.GONE);
                if (notifications.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setNotifications(notifications);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingIndicator.setVisibility(View.GONE);
                // TODO: Show error message to user
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // TODO: Handle notification click (e.g., show details dialog)
    }

    @Override
    public void onNotificationLongClick(View view, Notification notification) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        Menu menu = popupMenu.getMenu();

        if ("Admin".equals(userRole)) {
            menu.add(Menu.NONE, 1, Menu.NONE, "Delete");
        } else if ("Teacher".equals(userRole) && notification.getTeacherName().equals(sessionManagement.getUsername())) {
            menu.add(Menu.NONE, 1, Menu.NONE, "Delete");
            menu.add(Menu.NONE, 2, Menu.NONE, "Update");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: // Delete
                    deleteNotification(notification);
                    return true;
                case 2: // Update
                    updateNotification(notification);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void deleteNotification(Notification notification) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete", (dialog, which) -> {
                notificationsRef.child(notification.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Notification deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to delete notification: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateNotification(Notification notification) {
        // Create dialog with EditText fields
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_notification, null);
        EditText titleEdit = dialogView.findViewById(R.id.editTitle);
        EditText messageEdit = dialogView.findViewById(R.id.editMessage);

        titleEdit.setText(notification.getTitle());
        messageEdit.setText(notification.getMessage());

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Notification")
            .setView(dialogView)
            .setPositiveButton("Update", (dialog, which) -> {
                String newTitle = titleEdit.getText().toString().trim();
                String newMessage = messageEdit.getText().toString().trim();

                if (newTitle.isEmpty() || newMessage.isEmpty()) {
                    Toast.makeText(requireContext(), "Title and message cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update in Firebase
                notificationsRef.child(notification.getId())
                    .updateChildren(Map.of(
                        "title", newTitle,
                        "message", newMessage,
                        "timestamp", ServerValue.TIMESTAMP
                    ))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Notification updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update notification: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}