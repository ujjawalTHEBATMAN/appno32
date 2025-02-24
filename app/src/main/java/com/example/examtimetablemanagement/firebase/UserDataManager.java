package com.example.examtimetablemanagement.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDataManager {
    private static final String USERS_REF = "users";
    private final FirebaseDatabase database;
    private final DatabaseReference usersRef;

    public UserDataManager() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(USERS_REF);
    }

    public Task<String> getTeacherCollege(String username) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String college = snapshot.child("college").getValue(String.class);
                    taskCompletionSource.setResult(college);
                } else {
                    taskCompletionSource.setException(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<Void> updateTeacherCollege(String username, String college) {
        return usersRef.child(username).child("college").setValue(college);
    }

    public DatabaseReference getUsersReference() {
        return usersRef;
    }
}