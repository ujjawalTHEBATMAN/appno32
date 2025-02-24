package com.example.examtimetablemanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.adapters.TimeTableAdapter;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeTableFragment extends Fragment {
    private RecyclerView recyclerView;
    private TimeTableAdapter adapter;
    private List<TimeTable> timeTableList;
    private Spinner daySpinner;
    private DatabaseReference timeTableRef;
    private FirebaseUser currentUser;
    private String selectedDay = "Monday";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        
        initializeViews(view);
        setupFirebase();
        setupSpinner();
        setupRecyclerView();
        loadTimeTable();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.timeTableRecyclerView);
        daySpinner = view.findViewById(R.id.daySpinner);
        
        FloatingActionButton addButton = view.findViewById(R.id.addTimeTableButton);
        addButton.setOnClickListener(v -> showAddTimeTableDialog());
    }

    private void setupFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        timeTableRef = FirebaseDatabase.getInstance().getReference().child("timetables");
    }

    private void setupSpinner() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, days);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(spinnerAdapter);

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = days[position];
                loadTimeTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        timeTableList = new ArrayList<>();
        adapter = new TimeTableAdapter(requireContext(), timeTableList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(timeTable -> showEditTimeTableDialog(timeTable));
    }

    private void loadTimeTable() {
        if (currentUser == null) return;

        Query query = timeTableRef
                .orderByChild("dayOfWeek")
                .equalTo(selectedDay);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timeTableList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TimeTable timeTable = dataSnapshot.getValue(TimeTable.class);
                    if (timeTable != null) {
                        timeTableList.add(timeTable);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void showAddTimeTableDialog() {
        // TODO: Implement add time table dialog
    }

    private void showEditTimeTableDialog(TimeTable timeTable) {
        // TODO: Implement edit time table dialog
    }
}