package com.example.examtimetablemanagement.firebase;

import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TimeTablePerformanceTest {

    @Mock
    private FirebaseManager firebaseManager;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTimetableQueryPerformance() throws InterruptedException {
        // Arrange
        String testCollege = "Test College";
        String testDepartment = "Test Department";
        String testDay = "Monday";
        when(firebaseManager.getTimetablesByDay(testCollege, testDepartment, testDay))
            .thenReturn(mockQuery);

        // Act
        long startTime = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(1);
        final List<TimeTable> results = new ArrayList<>();

        mockQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeTable timeTable = snapshot.getValue(TimeTable.class);
                    results.add(timeTable);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        // Wait for async operation with timeout
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        // Assert
        assertTrue("Query should complete within 5 seconds", completed);
        assertTrue("Query should complete within 1 second", queryTime < 1000);
    }

    @Test
    public void testBulkTimetableCreation() {
        // Arrange
        int batchSize = 100;
        List<TimeTable> timeTables = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            TimeTable timeTable = new TimeTable();
            timeTable.setCollege("Test College");
            timeTable.setDepartment("Test Department");
            timeTable.setDayOfWeek("Monday");
            timeTables.add(timeTable);
        }

        // Act
        long startTime = System.currentTimeMillis();
        for (TimeTable timeTable : timeTables) {
            Task<Void> task = firebaseManager.createTimetable(timeTable);
            // In a real scenario, we would wait for task completion
        }
        long endTime = System.currentTimeMillis();
        long batchTime = endTime - startTime;

        // Assert
        assertTrue("Bulk creation should be reasonably fast", 
                 batchTime / batchSize < 50); // Average 50ms per entry
    }

    @Test
    public void testConcurrentTimetableUpdates() throws InterruptedException {
        // Arrange
        int numThreads = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(numThreads);
        final List<Long> updateTimes = new ArrayList<>();

        // Act
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    long threadStartTime = System.currentTimeMillis();
                    
                    TimeTable timeTable = new TimeTable();
                    timeTable.setId("test-id-" + Thread.currentThread().getId());
                    Task<Void> task = firebaseManager.updateTimetable(timeTable);
                    
                    long threadEndTime = System.currentTimeMillis();
                    synchronized (updateTimes) {
                        updateTimes.add(threadEndTime - threadStartTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        
        // Assert
        assertTrue("All concurrent updates should complete", completed);
        
        // Calculate average update time
        double avgUpdateTime = updateTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
            
        assertTrue("Average update time should be reasonable", 
                 avgUpdateTime < 200); // Less than 200ms per update
    }
}