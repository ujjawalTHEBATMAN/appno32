package com.example.examtimetablemanagement.session;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionManagementTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private SessionManagement sessionManagement;

    @Before
    public void setup() {
        when(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);

        sessionManagement = new SessionManagement(mockContext);
    }

    @Test
    public void testCreateLoginSession() {
        // Test data
        String username = "testUser";
        String college = "Test College";
        String department = "Computer Science";

        // Create login session
        sessionManagement.createLoginSession(username, college, department);

        // Verify session creation
        when(mockSharedPreferences.getString("username", null)).thenReturn(username);
        when(mockSharedPreferences.getString("college", null)).thenReturn(college);
        when(mockSharedPreferences.getString("department", null)).thenReturn(department);
        when(mockSharedPreferences.getBoolean("isLoggedIn", false)).thenReturn(true);

        // Assert session data
        assertEquals(username, sessionManagement.getUsername());
        assertEquals(college, sessionManagement.getCollege());
        assertEquals(department, sessionManagement.getDepartment());
        assertTrue(sessionManagement.isLoggedIn());
    }

    @Test
    public void testClearSession() {
        // Set up mock data
        when(mockSharedPreferences.getString("username", null)).thenReturn(null);
        when(mockSharedPreferences.getString("college", null)).thenReturn(null);
        when(mockSharedPreferences.getString("department", null)).thenReturn(null);
        when(mockSharedPreferences.getBoolean("isLoggedIn", false)).thenReturn(false);

        // Clear session
        sessionManagement.clearSession();

        // Verify session cleared
        assertNull(sessionManagement.getUsername());
        assertNull(sessionManagement.getCollege());
        assertNull(sessionManagement.getDepartment());
        assertFalse(sessionManagement.isLoggedIn());
    }
}