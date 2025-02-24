package com.example.examtimetablemanagement.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.example.examtimetablemanagement.models.Notification;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class generatorFragment extends Fragment {
    private TextInputEditText titleInput;
    private TextInputEditText messageInput;
    private MaterialButton attachImageButton;
    private MaterialButton sendButton;
    private ImageView attachedImage;
    private CircularProgressIndicator sendingIndicator;
    
    private Uri selectedImageUri;
    private DatabaseReference notificationsRef;
    private FirebaseAuth auth;
    private String uploadedImageUrl;
    
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    attachedImage.setImageURI(selectedImageUri);
                    attachedImage.setVisibility(View.VISIBLE);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generator, container, false);
        
        initializeViews(view);
        setupFirebase();
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        titleInput = view.findViewById(R.id.titleInput);
        messageInput = view.findViewById(R.id.messageInput);
        attachImageButton = view.findViewById(R.id.attachImageButton);
        sendButton = view.findViewById(R.id.sendButton);
        attachedImage = view.findViewById(R.id.attachedImage);
        sendingIndicator = view.findViewById(R.id.sendingIndicator);
    }

    private void setupFirebase() {
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        auth = FirebaseAuth.getInstance();
        
        // Initialize Cloudinary
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dxbmuhra0");
            config.put("api_key", "768683796493156");
            config.put("api_secret", "TAk8h67Wbm5stfo5485KkHuaZwg");
            MediaManager.init(requireContext(), config);
        }
    }

    private void setupClickListeners() {
        attachImageButton.setOnClickListener(v -> openImagePicker());
        sendButton.setOnClickListener(v -> validateAndSendNotification());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSendNotification() {
        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();
    
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }
    
        if (message.isEmpty()) {
            messageInput.setError("Message is required");
            return;
        }
    
        if (!isNetworkAvailable()) {
            handleError("No internet connection");
            return;
        }
    
        setLoadingState(true);
    
        if (selectedImageUri != null) {
            uploadImageAndSendNotification(title, message);
        } else {
            sendNotification(title, message, null);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void uploadImageAndSendNotification(String title, String message) {
        MediaManager.get()
                .upload(selectedImageUri)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        sendingIndicator.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        sendingIndicator.setProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        uploadedImageUrl = (String) resultData.get("secure_url");
                        sendNotification(title, message, uploadedImageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        handleError("Upload failed: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle rescheduling if needed
                    }
                })
                .dispatch();
    }

    private void sendNotification(String title, String message, String imageUrl) {
        loginActivity.SessionManagement sessionManagement = new loginActivity.SessionManagement(requireContext());
        String teacherName = sessionManagement.getName() != null ? sessionManagement.getName() : "Unknown Teacher";
        String collegeName = sessionManagement.getCollege() != null ? sessionManagement.getCollege() : "Unknown College";
        String userRole = sessionManagement.getUserRole();

        // Create notification with visibility rules
        Notification notification = new Notification(title, message, imageUrl, teacherName, collegeName);
        String notificationId = notificationsRef.push().getKey();

        if (notificationId != null) {
            notification.setId(notificationId);
            
            // Add user role specific data
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("title", notification.getTitle());
            notificationData.put("message", notification.getMessage());
            notificationData.put("imageUrl", notification.getImageUrl());
            notificationData.put("teacherName", notification.getTeacherName());
            notificationData.put("collegeName", notification.getCollegeName());
            notificationData.put("timestamp", notification.getTimestamp());
            notificationData.put("status", "SENT");
            notificationData.put("read", false);
            notificationData.put("deliveredTimestamp", System.currentTimeMillis());
            notificationData.put("senderRole", userRole);
            notificationData.put("senderCollege", collegeName);

            notificationsRef.child(notificationId)
                    .setValue(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        setLoadingState(false);
                        showSuccess();
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        handleError("Failed to send notification: " + e.getMessage());
                    });
        } else {
            handleError("Failed to generate notification ID");
        }
    }

    private void setLoadingState(boolean isLoading) {
        sendingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!isLoading);
        attachImageButton.setEnabled(!isLoading);
        titleInput.setEnabled(!isLoading);
        messageInput.setEnabled(!isLoading);
    }

    private void handleError(String message) {
        setLoadingState(false);
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> validateAndSendNotification())
                    .show();
        }
    }

    private void showSuccess() {
        if (getView() != null) {
            Snackbar.make(getView(), "Notification sent successfully", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        titleInput.setText("");
        messageInput.setText("");
        attachedImage.setVisibility(View.GONE);
        selectedImageUri = null;
    }
}