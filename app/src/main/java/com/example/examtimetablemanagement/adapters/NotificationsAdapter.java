package com.example.examtimetablemanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
    private List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.titleText.setText(notification.getTitle());
        holder.messageText.setText(notification.getMessage());

        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            holder.notificationImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(notification.getImageUrl())
                    .centerCrop()
                    .into(holder.notificationImage);
        } else {
            holder.notificationImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Add to the top of the list
        notifyItemInserted(0);
    }

    public void updateNotification(Notification updatedNotification) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(updatedNotification.getId())) {
                notifications.set(i, updatedNotification);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeNotification(String notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(notificationId)) {
                notifications.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void clearNotifications() {
        notifications.clear();
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView messageText;
        ImageView notificationImage;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.notificationItemTitle);
            messageText = itemView.findViewById(R.id.notificationItemMessage);
            notificationImage = itemView.findViewById(R.id.notificationItemImage);
        }
    }
}