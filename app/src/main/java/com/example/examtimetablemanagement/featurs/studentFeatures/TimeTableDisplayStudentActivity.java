package com.example.examtimetablemanagement.featurs.studentFeatures;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.TimeTableAdapter;
import com.example.examtimetablemanagement.featurs.teacherFeatures.TimeTableDetailsBottomSheet;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeTableDisplayStudentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TimeTableAdapter adapter;
    private List<TimeTable> timeTableList;
    private ChipGroup dayFilterChipGroup;
    private DatabaseReference timeTableRef;
    private String selectedDay = "Monday";
    private View emptyStateLayout;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set status/navigation bar colors and transitions for a smooth material feel.
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        getWindow().setEnterTransition(new MaterialFadeThrough());
        getWindow().setExitTransition(new MaterialFadeThrough());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_display); // use the same layout as teacher's view

        initializeViews();
        setupToolbar();
        setupFirebase();
        setupDayFilter();
        setupRecyclerView();
        loadTimeTable();
    }

    private void initializeViews() {
        // Make sure your layout file includes these IDs:
        // recyclerView: RecyclerView for timetable entries.
        // dayFilterChipGroup: ChipGroup for filtering by day.
        // emptyStateLayout: Layout to show when no data is available.
        // toolbar: AppBar MaterialToolbar.
        recyclerView = findViewById(R.id.timeTableRecyclerView);
        dayFilterChipGroup = findViewById(R.id.dayFilterChipGroup);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        toolbar = findViewById(R.id.toolbar);

        // Setup a layout transition for smooth changes between empty and data views.
        ViewGroup rootView = findViewById(android.R.id.content);
        LayoutTransition layoutTransition = new LayoutTransition();
        rootView.setLayoutTransition(layoutTransition);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Timetable");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupFirebase() {
        // Using the Realtime Database as in the teacher code for consistency.
        timeTableRef = FirebaseDatabase.getInstance().getReference().child("timetables");
    }

    private void setupDayFilter() {
        // Create chips for each day of the week.
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : days) {
            Chip chip = new Chip(this);
            chip.setText(day);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Check the default selected day.
            if (day.equals(selectedDay)) {
                chip.setChecked(true);
            }
            // When a chip is checked, update the timetable.
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
        // The third parameter (false) disables any edit/update functionality in the adapter.
        adapter = new TimeTableAdapter(this, timeTableList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Only set a click listener to show details.
        adapter.setOnItemClickListener(this::showTimeTableDetails);
    }

    private void loadTimeTable() {
        // Query timetable entries filtered by the selected day.
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

                // Animate between empty and data views.
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
                Toast.makeText(TimeTableDisplayStudentActivity.this,
                        "Error loading timetable: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimeTableDetails(TimeTable timeTable) {
        // Launch a bottom sheet dialog that shows detailed information for the selected timetable entry.
        TimeTableDetailsBottomSheet bottomSheet = new TimeTableDetailsBottomSheet(timeTable);
        bottomSheet.show(getSupportFragmentManager(), "TimeTableDetails");
    }
}
