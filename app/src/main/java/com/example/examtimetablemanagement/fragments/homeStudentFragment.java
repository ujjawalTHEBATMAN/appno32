package com.example.examtimetablemanagement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.featurs.studentFeatures.TimeTableDisplayStudentActivity;


public class homeStudentFragment extends Fragment {
    private Button viewTimeTableButton;

    public homeStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);

        viewTimeTableButton = view.findViewById(R.id.viewTimeTableButton);
        viewTimeTableButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimeTableDisplayStudentActivity.class);
            startActivity(intent);
        });

        return view;
    }
}