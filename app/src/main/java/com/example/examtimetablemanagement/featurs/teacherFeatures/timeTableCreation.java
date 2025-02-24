package com.example.examtimetablemanagement.featurs.teacherFeatures;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.TimeTableAdapter;
import com.example.examtimetablemanagement.models.TimeTable;
import com.example.examtimetablemanagement.session.SessionManagement;
import com.google.android.material.textfield.TextInputEditText;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class timeTableCreation extends AppCompatActivity {
    private RecyclerView timeTableGrid;
    private Spinner daySpinner, timeSlotSpinner;
    private Button saveButton, addSlotButton;
    private TextInputEditText subjectInput, roomInput;
    private Map<String, Map<String, String>> timeTableData;
    private List<String> days;
    private List<String> timeSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_table_creation);
        
        initializeViews();
        
        setupCloudinary();
        setupSpinners();
        setupListeners();
        loadTimeTable();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        timeTableGrid = findViewById(R.id.timeTableGrid);
        daySpinner = findViewById(R.id.daySpinner);
        timeSlotSpinner = findViewById(R.id.timeSlotSpinner);
        saveButton = findViewById(R.id.saveButton);
        addSlotButton = findViewById(R.id.addSlotButton);
        subjectInput = findViewById(R.id.subjectInput);
        roomInput = findViewById(R.id.roomInput);
        
        timeTableData = new HashMap<>();
    }
    

    private static boolean isCloudinaryInitialized = false;
    
    private void setupCloudinary() {
        if (!isCloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dxbmuhra0");
            config.put("api_key", "768683796493156");
            config.put("api_secret", "TAk8h67Wbm5stfo5485KkHuaZwg");
            MediaManager.init(this, config);
            isCloudinaryInitialized = true;
        }
    }

    private void setupSpinners() {
        days = new ArrayList<>();
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");

        timeSlots = new ArrayList<>();
        timeSlots.add("9:00 AM - 10:00 AM");
        timeSlots.add("10:00 AM - 11:00 AM");
        timeSlots.add("11:00 AM - 12:00 PM");
        timeSlots.add("12:00 PM - 1:00 PM");
        timeSlots.add("2:00 PM - 3:00 PM");
        timeSlots.add("3:00 PM - 4:00 PM");

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSlots);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(timeAdapter);
    }

    private void setupListeners() {
        addSlotButton.setOnClickListener(v -> addTimeTableSlot());
        saveButton.setOnClickListener(v -> saveTimeTable());
    }

    private void addTimeTableSlot() {
        String day = daySpinner.getSelectedItem().toString();
        String timeSlot = timeSlotSpinner.getSelectedItem().toString();
        String subject = subjectInput.getText().toString().trim();
        String room = roomInput.getText().toString().trim();

        if (subject.isEmpty() || room.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!timeTableData.containsKey(day)) {
            timeTableData.put(day, new HashMap<>());
        }

        Map<String, String> daySchedule = timeTableData.get(day);
        daySchedule.put(timeSlot, subject + " - " + room);

        updateGridView();
        clearInputs();
    }

    private void clearInputs() {
        subjectInput.setText("");
        roomInput.setText("");
    }

    private void updateGridView() {
        List<TimeTable> timeTableEntries = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, String>> dayEntry : timeTableData.entrySet()) {
            String day = dayEntry.getKey();
            Map<String, String> slots = dayEntry.getValue();
            
            for (Map.Entry<String, String> slotEntry : slots.entrySet()) {
                String timeSlot = slotEntry.getKey();
                String[] subjectRoom = slotEntry.getValue().split(" - ");
                
                TimeTable entry = new TimeTable();
                entry.setDayOfWeek(day);
                entry.setSubject(subjectRoom[0]);
                entry.setClassroom(subjectRoom[1]);
                String[] times = timeSlot.split(" - ");
                entry.setStartTime(times[0]);
                entry.setEndTime(times[1]);
                
                timeTableEntries.add(entry);
            }
        }
        
        TimeTableAdapter adapter = new TimeTableAdapter(timeTableCreation.this, timeTableEntries);
        timeTableGrid.setLayoutManager(new GridLayoutManager(timeTableCreation.this, 1));
        timeTableGrid.setAdapter(adapter);
        
        adapter.setOnItemClickListener(timeTable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(timeTableCreation.this);
            builder.setTitle("Time Table Entry")
                   .setMessage(String.format("%s\n%s\n%s - %s\nRoom: %s",
                            timeTable.getDayOfWeek(),
                            timeTable.getSubject(),
                            timeTable.getStartTime(),
                            timeTable.getEndTime(),
                            timeTable.getClassroom()))
                   .setPositiveButton("OK", null)
                   .show();
        });
    }

    private void saveTimeTable() {
        Map<String, Object> timeTable = new HashMap<>();
        timeTable.put("schedule", timeTableData);

        String jsonData = new com.google.gson.Gson().toJson(timeTable);
        String fileName = "timetable.json";

        MediaManager.get().upload(jsonData.getBytes())
                .option("public_id", fileName)
                .option("resource_type", "raw")
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        runOnUiThread(() -> {
                            Toast.makeText(timeTableCreation.this, "Time table saved successfully", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            Toast.makeText(timeTableCreation.this, "Error saving time table: " + error.getDescription(), 
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            Toast.makeText(timeTableCreation.this, "Upload rescheduled: " + error.getDescription(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .dispatch();
    }

    private void loadTimeTable() {
        String fileName = "timetable.json";
        String url = MediaManager.get().url().resourceType("raw").generate(fileName);

        new Thread(() -> {
            try {
                java.net.URL cloudinaryUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) cloudinaryUrl.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Map<String, Object> timeTable = new com.google.gson.Gson().fromJson(
                            response.toString(), Map.class);
                    
                    if (timeTable != null && timeTable.containsKey("schedule")) {
                        timeTableData = (Map<String, Map<String, String>>) timeTable.get("schedule");
                        runOnUiThread(() -> updateGridView());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(timeTableCreation.this, "Error loading time table: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}