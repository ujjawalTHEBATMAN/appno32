package com.example.examtimetablemanagement.featurs.teacherFeatures.scheduler;

import com.example.examtimetablemanagement.models.TimeTable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TimetableScheduler {
    private static final int MIN_HOUR = 8;  // 8 AM
    private static final int MAX_HOUR = 18; // 6 PM
    
    // Store conflicts for each time slot
    private Map<String, List<TimeTable>> timeSlotConflicts;
    
    public TimetableScheduler() {
        timeSlotConflicts = new HashMap<>();
    }
    
    // Check for conflicts in a given time slot
    public boolean hasConflict(TimeTable newEntry, List<TimeTable> existingEntries) {
        for (TimeTable existing : existingEntries) {
            if (existing.getDayOfWeek().equals(newEntry.getDayOfWeek()) &&
                isTimeOverlap(existing, newEntry)) {
                return true;
            }
        }
        return false;
    }
    
    // Find available time slots for a given day
    public List<String> findAvailableTimeSlots(String dayOfWeek, List<TimeTable> existingEntries) {
        List<String> availableSlots = new ArrayList<>();
        
        for (int hour = MIN_HOUR; hour < MAX_HOUR; hour++) {
            String timeSlot = String.format("%02d:00", hour);
            boolean isAvailable = true;
            
            for (TimeTable entry : existingEntries) {
                if (entry.getDayOfWeek().equals(dayOfWeek) &&
                    entry.getStartTime().startsWith(timeSlot)) {
                    isAvailable = false;
                    break;
                }
            }
            
            if (isAvailable) {
                availableSlots.add(timeSlot);
            }
        }
        
        return availableSlots;
    }
    
    // Suggest alternative time slots when conflicts occur
    public List<String> suggestAlternativeSlots(TimeTable conflictingEntry, List<TimeTable> existingEntries) {
        return findAvailableTimeSlots(conflictingEntry.getDayOfWeek(), existingEntries);
    }
    
    // Helper method to check if two time slots overlap
    private boolean isTimeOverlap(TimeTable entry1, TimeTable entry2) {
        int start1 = timeToMinutes(entry1.getStartTime());
        int end1 = timeToMinutes(entry1.getEndTime());
        int start2 = timeToMinutes(entry2.getStartTime());
        int end2 = timeToMinutes(entry2.getEndTime());
        
        return (start1 < end2 && end1 > start2);
    }
    
    // Convert time string (HH:mm) to minutes since midnight
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
    
    // Get all conflicts for debugging and UI highlighting
    public Map<String, List<TimeTable>> getConflicts() {
        return timeSlotConflicts;
    }
    
    // Clear conflicts
    public void clearConflicts() {
        timeSlotConflicts.clear();
    }
}