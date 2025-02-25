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
    private OnItemLongClickListener longClickListener;
    private OnItemDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(TimeTable timeTable);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(TimeTable timeTable, int position);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(TimeTable timeTable, int position);
    }

    public TimeTableAdapter(Context context, List<TimeTable> timeTableList) {
        this.context = context;
        this.timeTableList = timeTableList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
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
        holder.classroomChip.setText(timeTable.getClassroom());
        holder.timeChip.setText(String.format("%s - %s", timeTable.getStartTime(), timeTable.getEndTime()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(timeTable);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(timeTable, position);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return timeTableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView;
        TextView teacherTextView;
        com.google.android.material.chip.Chip classroomChip;
        com.google.android.material.chip.Chip timeChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            teacherTextView = itemView.findViewById(R.id.teacherTextView);
            classroomChip = itemView.findViewById(R.id.classroomChip);
            timeChip = itemView.findViewById(R.id.timeChip);
        }
    }

    public void updateData(List<TimeTable> newTimeTableList) {
        timeTableList.clear();
        timeTableList.addAll(newTimeTableList);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < timeTableList.size()) {
            TimeTable removedItem = timeTableList.get(position);
            timeTableList.remove(position);
            notifyItemRemoved(position);
            if (deleteListener != null) {
                deleteListener.onItemDelete(removedItem, position);
            }
        }
    }
}