package com.example.examtimetablemanagement.firebase;

import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirebaseManagerTest {

    @Mock
    private FirebaseDatabase mockDatabase;

    @Mock
    private DatabaseReference mockDatabaseReference;

    @Mock
    private Task<Void> mockTask;

    private FirebaseManager firebaseManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDatabase.getReference(anyString())).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.child(anyString())).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.push()).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.getKey()).thenReturn("test-key");
        when(mockDatabaseReference.setValue(any())).thenReturn(mockTask);
        when(mockDatabaseReference.removeValue()).thenReturn(mockTask);
        
        firebaseManager = new FirebaseManager();
    }

    @Test
    public void testCreateTimetable() {
        // Arrange
        TimeTable timeTable = new TimeTable();
        timeTable.setCollege("Test College");
        timeTable.setDepartment("Test Department");

        // Act
        Task<Void> result = firebaseManager.createTimetable(timeTable);

        // Assert
        verify(mockDatabaseReference).setValue(timeTable);
        assert timeTable.getId() != null;
        assert timeTable.getCreatedAt() > 0;
        assert timeTable.getUpdatedAt() > 0;
    }

    @Test
    public void testUpdateTimetable() {
        // Arrange
        TimeTable timeTable = new TimeTable();
        timeTable.setId("test-id");
        timeTable.setCollege("Test College");
        timeTable.setDepartment("Test Department");
        long beforeUpdate = System.currentTimeMillis();

        // Act
        Task<Void> result = firebaseManager.updateTimetable(timeTable);

        // Assert
        verify(mockDatabaseReference).setValue(timeTable);
        assert timeTable.getUpdatedAt() >= beforeUpdate;
    }

    @Test
    public void testDeleteTimetable() {
        // Arrange
        TimeTable timeTable = new TimeTable();
        timeTable.setId("test-id");
        timeTable.setCollege("Test College");
        timeTable.setDepartment("Test Department");

        // Act
        Task<Void> result = firebaseManager.deleteTimetable(timeTable);

        // Assert
        verify(mockDatabaseReference).removeValue();
    }

    @Test
    public void testGetTimetablesByCollegeAndDepartment() {
        // Act
        Query result = firebaseManager.getTimetablesByCollegeAndDepartment("Test College", "Test Department");

        // Assert
        verify(mockDatabaseReference).child("Test College");
        verify(mockDatabaseReference).child("Test Department");
    }

    @Test
    public void testGetTimetablesByTeacher() {
        // Act
        Query result = firebaseManager.getTimetablesByTeacher("Test College", "Test Department", "Test Teacher");

        // Assert
        verify(mockDatabaseReference).orderByChild("teacher");
        verify(mockDatabaseReference).equalTo("Test Teacher");
    }
}