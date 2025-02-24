package com.example.examtimetablemanagement.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.TimeTable;

import java.util.List;

public class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.ViewHolder> {
    private final Context context;
    private final List<TimeTable> timeTableList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TimeTable timeTable);
    }

    public TimeTableAdapter(Context context, List<TimeTable> timeTableList) {
        this.context = context;
        this.timeTableList = timeTableList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeTable timeTable = timeTableList.get(position);
        holder.subjectTextView.setText(timeTable.getSubject());
        holder.teacherTextView.setText(timeTable.getTeacher());
        holder.classroomTextView.setText(timeTable.getClassroom());
        holder.timeTextView.setText(String.format("%s - %s", timeTable.getStartTime(), timeTable.getEndTime()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(timeTable);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeTableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView;
        TextView teacherTextView;
        TextView classroomTextView;
        TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            teacherTextView = itemView.findViewById(R.id.teacherTextView);
            classroomTextView = itemView.findViewById(R.id.classroomTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }

    public void updateData(List<TimeTable> newTimeTableList) {
        timeTableList.clear();
        timeTableList.addAll(newTimeTableList);
        notifyDataSetChanged();
    }
}