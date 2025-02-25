package com.example.examtimetablemanagement.models;

public class TimeTable {
    private String id;
    private String college;
    private String department;
    private String semester;
    private String subject;
    private String teacher;
    private String classroom;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private long createdAt;
    private long updatedAt;

    public TimeTable() {
        // Required empty constructor for Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Generate a unique composite key for preventing duplicates
    public String generateCompositeKey() {
        return String.format("%s_%s_%s_%s_%s_%s",
                college,
                department,
                semester,
                dayOfWeek,
                startTime,
                endTime);
    }

    // Validate time slot format (HH:mm)
    public boolean isValidTimeFormat() {
        return startTime != null && endTime != null &&
                startTime.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") &&
                endTime.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    // Check if time slots overlap
    public boolean overlaps(TimeTable other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }

        String[] thisStart = this.startTime.split(":");
        String[] thisEnd = this.endTime.split(":");
        String[] otherStart = other.startTime.split(":");
        String[] otherEnd = other.endTime.split(":");

        int thisStartMinutes = Integer.parseInt(thisStart[0]) * 60 + Integer.parseInt(thisStart[1]);
        int thisEndMinutes = Integer.parseInt(thisEnd[0]) * 60 + Integer.parseInt(thisEnd[1]);
        int otherStartMinutes = Integer.parseInt(otherStart[0]) * 60 + Integer.parseInt(otherStart[1]);
        int otherEndMinutes = Integer.parseInt(otherEnd[0]) * 60 + Integer.parseInt(otherEnd[1]);

        return !(thisEndMinutes <= otherStartMinutes || thisStartMinutes >= otherEndMinutes);
    }
}