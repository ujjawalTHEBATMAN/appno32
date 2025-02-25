package com.example.examtimetablemanagement.featurs.teacherFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.TimeTable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TimeTableDetailsBottomSheet extends BottomSheetDialogFragment {
    private final TimeTable timeTable;

    public TimeTableDetailsBottomSheet(TimeTable timeTable) {
        this.timeTable = timeTable;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_timetable_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView dayTextView = view.findViewById(R.id.dayTextView);
        TextView subjectTextView = view.findViewById(R.id.subjectTextView);
        TextView timeTextView = view.findViewById(R.id.timeTextView);
        TextView roomTextView = view.findViewById(R.id.roomTextView);
        TextView teacherTextView = view.findViewById(R.id.teacherTextView);
        TextView collegeTextView = view.findViewById(R.id.collegeTextView);
        TextView departmentTextView = view.findViewById(R.id.departmentTextView);
        TextView semesterTextView = view.findViewById(R.id.semesterTextView);

        dayTextView.setText(timeTable.getDayOfWeek());
        subjectTextView.setText(timeTable.getSubject());
        timeTextView.setText(String.format("%s - %s", timeTable.getStartTime(), timeTable.getEndTime()));
        roomTextView.setText(String.format("Room: %s", timeTable.getClassroom()));
        teacherTextView.setText(String.format("Teacher: %s", timeTable.getTeacher()));
        collegeTextView.setText(String.format("College: %s", timeTable.getCollege()));
        departmentTextView.setText(String.format("Department: %s", timeTable.getDepartment()));
        semesterTextView.setText(String.format("Semester: %s", timeTable.getSemester()));
    }
}