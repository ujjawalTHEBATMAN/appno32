package com.example.examtimetablemanagement.featurs.teacherFeatures;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyTimeTableActivity extends AppCompatActivity {
    private RecyclerView weeklyTimeTableRecyclerView;
    private WeeklyTimeTableAdapter adapter;
    private List<DaySchedule> weeklySchedule;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_time_table);

        weeklyTimeTableRecyclerView = findViewById(R.id.weeklyTimeTableRecyclerView);
        weeklySchedule = new ArrayList<>();
        
        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("timetables");

        setupRecyclerView();
        loadTimeTableFromFirebase();
    }

    private void setupRecyclerView() {
        adapter = new WeeklyTimeTableAdapter(weeklySchedule);
        weeklyTimeTableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weeklyTimeTableRecyclerView.setAdapter(adapter);
    }

    private void loadTimeTableFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                weeklySchedule.clear();
                Map<String, List<TimeTable>> dayWiseSchedule = new HashMap<>();

                // Get current day of week
                Calendar calendar = Calendar.getInstance();
                int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Initialize days list starting from current day
                List<String> orderedDays = getOrderedDays(currentDayOfWeek);

                // Populate dayWiseSchedule
                for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                    String day = daySnapshot.getKey();
                    List<TimeTable> daySchedule = new ArrayList<>();

                    for (DataSnapshot slotSnapshot : daySnapshot.getChildren()) {
                        TimeTable timeTable = slotSnapshot.getValue(TimeTable.class);
                        if (timeTable != null) {
                            daySchedule.add(timeTable);
                        }
                    }

                    if (!daySchedule.isEmpty()) {
                        Collections.sort(daySchedule, (t1, t2) -> 
                            t1.getStartTime().compareTo(t2.getStartTime()));
                        dayWiseSchedule.put(day, daySchedule);
                    }
                }

                // Create DaySchedule objects in order
                for (String day : orderedDays) {
                    List<TimeTable> schedule = dayWiseSchedule.getOrDefault(day, new ArrayList<>());
                    weeklySchedule.add(new DaySchedule(day, schedule));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(WeeklyTimeTableActivity.this, 
                    "Error loading timetable: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getOrderedDays(int currentDayOfWeek) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        List<String> orderedDays = new ArrayList<>();
        
        // Add days starting from current day
        for (int i = currentDayOfWeek - 1; i < days.length; i++) {
            orderedDays.add(days[i]);
        }
        // Add remaining days
        for (int i = 0; i < currentDayOfWeek - 1; i++) {
            orderedDays.add(days[i]);
        }
        
        return orderedDays;
    }

    // Data class for day-wise schedule
    private static class DaySchedule {
        private String day;
        private List<TimeTable> schedule;

        public DaySchedule(String day, List<TimeTable> schedule) {
            this.day = day;
            this.schedule = schedule;
        }

        public String getDay() { return day; }
        public List<TimeTable> getSchedule() { return schedule; }
    }

    // Adapter for weekly time table
    private class WeeklyTimeTableAdapter extends RecyclerView.Adapter<WeeklyTimeTableAdapter.ViewHolder> {
        private List<DaySchedule> weeklySchedule;

        public WeeklyTimeTableAdapter(List<DaySchedule> weeklySchedule) {
            this.weeklySchedule = weeklySchedule;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_day_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DaySchedule daySchedule = weeklySchedule.get(position);
            holder.dayTextView.setText(daySchedule.getDay());

            if (daySchedule.getSchedule().isEmpty()) {
                holder.holidayView.setVisibility(View.VISIBLE);
                holder.scheduleContainer.setVisibility(View.GONE);
            } else {
                holder.holidayView.setVisibility(View.GONE);
                holder.scheduleContainer.setVisibility(View.VISIBLE);
                
                // Clear previous views
                holder.scheduleContainer.removeAllViews();
                
                // Add time slots
                for (TimeTable slot : daySchedule.getSchedule()) {
                    View slotView = getLayoutInflater().inflate(
                        R.layout.item_time_slot, holder.scheduleContainer, false);
                    
                    TextView timeTextView = slotView.findViewById(R.id.timeTextView);
                    TextView subjectTextView = slotView.findViewById(R.id.subjectTextView);
                    TextView roomTextView = slotView.findViewById(R.id.roomTextView);
                    
                    timeTextView.setText(String.format("%s - %s", 
                        slot.getStartTime(), slot.getEndTime()));
                    subjectTextView.setText(slot.getSubject());
                    roomTextView.setText(String.format("Room: %s", slot.getClassroom()));
                    
                    holder.scheduleContainer.addView(slotView);
                }
            }

            // Highlight current day
            Calendar calendar = Calendar.getInstance();
            String currentDay = getOrderedDays(calendar.get(Calendar.DAY_OF_WEEK)).get(0);
            holder.dayCard.setStrokeColor(getResources().getColor(
                currentDay.equals(daySchedule.getDay()) ? 
                android.R.color.holo_blue_dark : 
                android.R.color.darker_gray));
        }

        @Override
        public int getItemCount() {
            return weeklySchedule.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView dayTextView;
            View holidayView;
            android.widget.LinearLayout scheduleContainer;
            MaterialCardView dayCard;

            ViewHolder(View itemView) {
                super(itemView);
                dayTextView = itemView.findViewById(R.id.dayTextView);
                holidayView = itemView.findViewById(R.id.holidayView);
                scheduleContainer = itemView.findViewById(R.id.scheduleContainer);
                dayCard = itemView.findViewById(R.id.dayCard);
            }
        }
    }
}