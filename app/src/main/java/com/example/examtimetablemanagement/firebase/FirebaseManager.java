package com.example.examtimetablemanagement.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseManager {
    private static final String TIMETABLES_REF = "timetables";
    private static final String COLLEGES_REF = "colleges";
    private static final String TEACHERS_REF = "teachers";
    private static final int CACHE_SIZE = 100; // Number of entries to cache
    
    private final FirebaseDatabase database;
    private final DatabaseReference timetablesRef;
    private final Map<String, TimeTable> cache;
    private final Map<String, Long> cacheTimestamps;
    
    public FirebaseManager() {
        database = FirebaseDatabase.getInstance();
        timetablesRef = database.getReference(TIMETABLES_REF);
        cache = new HashMap<>();
        cacheTimestamps = new HashMap<>();
        
        // Enable disk persistence
        database.setPersistenceEnabled(true);
        // Keep timetables synced
        timetablesRef.keepSynced(true);
    }
    
    private boolean validateTimeTable(TimeTable timeTable) {
        return timeTable != null &&
               timeTable.getCollege() != null && !timeTable.getCollege().isEmpty() &&
               timeTable.getDepartment() != null && !timeTable.getDepartment().isEmpty() &&
               timeTable.getSemester() != null && !timeTable.getSemester().isEmpty() &&
               timeTable.getSubject() != null && !timeTable.getSubject().isEmpty() &&
               timeTable.getTeacher() != null && !timeTable.getTeacher().isEmpty() &&
               timeTable.getStartTime() != null && !timeTable.getStartTime().isEmpty() &&
               timeTable.getEndTime() != null && !timeTable.getEndTime().isEmpty();
    }

    private void addToCache(TimeTable timeTable) {
        String cacheKey = getCacheKey(timeTable);
        cache.put(cacheKey, timeTable);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        
        // Remove oldest entry if cache is full
        if (cache.size() > CACHE_SIZE) {
            String oldestKey = cacheTimestamps.entrySet().stream()
                .min((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
            if (oldestKey != null) {
                cache.remove(oldestKey);
                cacheTimestamps.remove(oldestKey);
            }
        }
    }

    private String getCacheKey(TimeTable timeTable) {
        return String.format("%s_%s_%s", 
            timeTable.getCollege(), 
            timeTable.getDepartment(), 
            timeTable.getId());
    }

    // Create a new timetable entry with validation
    public Task<Void> createTimetable(TimeTable timeTable) {
        if (!validateTimeTable(timeTable)) {
            return Tasks.forException(new IllegalArgumentException("Invalid timetable data"));
        }

        String timetableId = timetablesRef.push().getKey();
        if (timetableId == null) {
            return Tasks.forException(new IllegalStateException("Failed to generate timetable ID"));
        }

        timeTable.setId(timetableId);
        timeTable.setCreatedAt(System.currentTimeMillis());
        timeTable.setUpdatedAt(System.currentTimeMillis());
        
        return timetablesRef
            .child(timeTable.getCollege())
            .child(timeTable.getDepartment())
            .child(timetableId)
            .setValue(timeTable)
            .addOnSuccessListener(aVoid -> addToCache(timeTable));
    }
    
    // Update an existing timetable entry with validation
    public Task<Void> updateTimetable(TimeTable timeTable) {
        if (!validateTimeTable(timeTable) || timeTable.getId() == null) {
            return Tasks.forException(new IllegalArgumentException("Invalid timetable data"));
        }

        timeTable.setUpdatedAt(System.currentTimeMillis());
        
        return timetablesRef
            .child(timeTable.getCollege())
            .child(timeTable.getDepartment())
            .child(timeTable.getId())
            .setValue(timeTable)
            .addOnSuccessListener(aVoid -> addToCache(timeTable));
    }
    
    // Delete a timetable entry with validation
    public Task<Void> deleteTimetable(TimeTable timeTable) {
        if (timeTable == null || timeTable.getId() == null || 
            timeTable.getCollege() == null || timeTable.getDepartment() == null) {
            return Tasks.forException(new IllegalArgumentException("Invalid timetable data"));
        }

        String cacheKey = getCacheKey(timeTable);
        return timetablesRef
            .child(timeTable.getCollege())
            .child(timeTable.getDepartment())
            .child(timeTable.getId())
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                cache.remove(cacheKey);
                cacheTimestamps.remove(cacheKey);
            });
    }
    
    // Get timetables for a specific college and department with caching
    public Query getTimetablesByCollegeAndDepartment(String college, String department) {
        if (college == null || department == null || college.isEmpty() || department.isEmpty()) {
            throw new IllegalArgumentException("College and department must not be null or empty");
        }

        DatabaseReference ref = timetablesRef.child(college).child(department);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot timeTableSnapshot : snapshot.getChildren()) {
                    TimeTable timeTable = timeTableSnapshot.getValue(TimeTable.class);
                    if (timeTable != null) {
                        addToCache(timeTable);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        return ref;
    }
    
    // Get timetables for a specific teacher
    public Query getTimetablesByTeacher(String college, String department, String teacher) {
        return timetablesRef
            .child(college)
            .child(department)
            .orderByChild("teacher")
            .equalTo(teacher);
    }
    
    // Get timetables for a specific day
    public Query getTimetablesByDay(String college, String department, String dayOfWeek) {
        return timetablesRef
            .child(college)
            .child(department)
            .orderByChild("dayOfWeek")
            .equalTo(dayOfWeek);
    }
    
    // Get reference to colleges
    public DatabaseReference getCollegesRef() {
        return database.getReference(COLLEGES_REF);
    }
    
    // Get reference to teachers
    public DatabaseReference getTeachersRef() {
        return database.getReference(TEACHERS_REF);
    }
}