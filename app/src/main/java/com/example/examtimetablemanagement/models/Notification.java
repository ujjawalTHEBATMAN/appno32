package com.example.examtimetablemanagement.models;

import com.google.firebase.database.ServerValue;
import java.util.Map;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String imageUrl;
    private String teacherName;
    private String collegeName;
    private Long timestamp;
    private String status; // PENDING, SENT, FAILED
    private String errorMessage;
    private int retryCount;
    private boolean isRead;
    private long deliveredTimestamp;
    private String senderRole;
    private String senderCollege;
    private String targetRole;
    private String targetCollege;

    public Notification() {
        // Required empty constructor for Firebase
        this.status = "PENDING";
        this.retryCount = 0;
        this.isRead = false;
    }

    public Notification(String title, String message, String imageUrl, String teacherName, String collegeName) {
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.teacherName = teacherName;
        this.collegeName = collegeName;
        this.timestamp = System.currentTimeMillis();
        this.status = "PENDING";
        this.retryCount = 0;
        this.isRead = false;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getSenderCollege() {
        return senderCollege;
    }

    public void setSenderCollege(String senderCollege) {
        this.senderCollege = senderCollege;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetCollege() {
        return targetCollege;
    }

    public void setTargetCollege(String targetCollege) {
        this.targetCollege = targetCollege;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getDeliveredTimestamp() {
        return deliveredTimestamp;
    }

    public void setDeliveredTimestamp(long deliveredTimestamp) {
        this.deliveredTimestamp = deliveredTimestamp;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < 3; // Maximum 3 retry attempts
    }
}