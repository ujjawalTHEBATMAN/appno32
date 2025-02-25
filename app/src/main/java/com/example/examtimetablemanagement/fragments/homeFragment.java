package com.example.examtimetablemanagement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.featurs.teacherFeatures.TimeTableDisplayActivity;
import com.example.examtimetablemanagement.featurs.teacherFeatures.examTimeTableCreation;
import com.example.examtimetablemanagement.featurs.teacherFeatures.timeTableCreation;
import com.google.android.material.button.MaterialButton;

public class homeFragment extends Fragment {

    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MaterialButton examTimeTableButton = view.findViewById(R.id.examTimeTableButton);
        MaterialButton timeTableButton = view.findViewById(R.id.timeTableButton);
        MaterialButton viewScheduleButton = view.findViewById(R.id.viewScheduleButton);

        examTimeTableButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), examTimeTableCreation.class);
            startActivity(intent);
        });

        timeTableButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), timeTableCreation.class);
            startActivity(intent);
        });

        viewScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimeTableDisplayActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
