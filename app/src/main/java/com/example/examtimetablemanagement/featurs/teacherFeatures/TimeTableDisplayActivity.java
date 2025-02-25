package com.example.examtimetablemanagement.featurs.teacherFeatures;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.TimeTableAdapter;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeTableDisplayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TimeTableAdapter adapter;
    private List<TimeTable> timeTableList;
    private ChipGroup dayFilterChipGroup;
    private DatabaseReference timeTableRef;
    private String selectedDay = "Monday";
    private View emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        
        getWindow().setEnterTransition(new MaterialFadeThrough());
        getWindow().setExitTransition(new MaterialFadeThrough());
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_display);

        initializeViews();
        setupToolbar();
        setupFirebase();
        setupDayFilter();
        setupRecyclerView();
        loadTimeTable();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.timeTableRecyclerView);
        dayFilterChipGroup = findViewById(R.id.dayFilterChipGroup);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        ViewGroup rootView = findViewById(android.R.id.content);
        LayoutTransition layoutTransition = new LayoutTransition();
        rootView.setLayoutTransition(layoutTransition);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupFirebase() {
        timeTableRef = FirebaseDatabase.getInstance().getReference().child("timetables");
    }

    private void setupDayFilter() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (String day : days) {
            Chip chip = new Chip(this);
            chip.setText(day);
            chip.setCheckable(true);
            chip.setClickable(true);
            
            if (day.equals(selectedDay)) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDay = day;
                    loadTimeTable();
                }
            });
            
            dayFilterChipGroup.addView(chip);
        }
    }

    private void setupRecyclerView() {
        timeTableList = new ArrayList<>();
        adapter = new TimeTableAdapter(this, timeTableList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showTimeTableDetails);
        adapter.setOnItemLongClickListener((timeTable, position) -> {
            showEditDialog(timeTable);
            return true;
        });

        adapter.setOnItemDeleteListener((timeTable, position) -> {
            deleteTimeTable(timeTable);
        });

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TimeTable timeTable = timeTableList.get(position);
                adapter.removeItem(position);
                deleteTimeTable(timeTable);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void showEditDialog(TimeTable timeTable) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_timetable, null);
        TextInputEditText subjectInput = dialogView.findViewById(R.id.subjectEditText);
        TextInputEditText teacherInput = dialogView.findViewById(R.id.teacherEditText);
        TextInputEditText classroomInput = dialogView.findViewById(R.id.classroomEditText);
        TextInputEditText startTimeInput = dialogView.findViewById(R.id.startTimeEditText);
        TextInputEditText endTimeInput = dialogView.findViewById(R.id.endTimeEditText);


        subjectInput.setText(timeTable.getSubject());
        teacherInput.setText(timeTable.getTeacher());
        classroomInput.setText(timeTable.getClassroom());
        startTimeInput.setText(timeTable.getStartTime());
        endTimeInput.setText(timeTable.getEndTime());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Time Table")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    timeTable.setSubject(subjectInput.getText().toString());
                    timeTable.setTeacher(teacherInput.getText().toString());
                    timeTable.setClassroom(classroomInput.getText().toString());
                    timeTable.setStartTime(startTimeInput.getText().toString());
                    timeTable.setEndTime(endTimeInput.getText().toString());
                    updateTimeTable(timeTable);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTimeTable(TimeTable timeTable) {
        timeTableRef.child(timeTable.getId()).setValue(timeTable)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Time table updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update time table: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteTimeTable(TimeTable timeTable) {
        timeTableRef.child(timeTable.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Time table deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete time table: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTimeTable() {
        Query query = timeTableRef.orderByChild("dayOfWeek").equalTo(selectedDay);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timeTableList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TimeTable timeTable = dataSnapshot.getValue(TimeTable.class);
                    if (timeTable != null) {
                        timeTable.setId(dataSnapshot.getKey());
                        timeTableList.add(timeTable);
                    }
                }
                adapter.notifyDataSetChanged();

                if (timeTableList.isEmpty()) {
                    recyclerView.animate().alpha(0f).withEndAction(() -> {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        emptyStateLayout.setAlpha(0f);
                        emptyStateLayout.animate().alpha(1f).start();
                    }).start();
                } else {
                    emptyStateLayout.animate().alpha(0f).withEndAction(() -> {
                        emptyStateLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAlpha(0f);
                        recyclerView.animate().alpha(1f).start();
                    }).start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TimeTableDisplayActivity.this,
                        "Error loading timetable: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimeTableDetails(TimeTable timeTable) {
        TimeTableDetailsBottomSheet bottomSheet = new TimeTableDetailsBottomSheet(timeTable);
        bottomSheet.show(getSupportFragmentManager(), "TimeTableDetails");
    }
}