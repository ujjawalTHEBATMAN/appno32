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
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    private OnNotificationLongClickListener longClickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnNotificationLongClickListener {
        void onNotificationLongClick(View view, Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
        if (listener instanceof OnNotificationLongClickListener) {
            this.longClickListener = (OnNotificationLongClickListener) listener;
        }
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
        holder.senderText.setText(String.format("%s • %s", notification.getTeacherName(), notification.getCollegeName()));
        holder.messageText.setText(notification.getMessage());

        // Format and display the timestamp
        Long timestamp = notification.getTimestamp();
        if (timestamp != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String formattedDate = sdf.format(new Date(timestamp));
                holder.timestampText.setText(formattedDate);
                holder.timestampText.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.timestampText.setVisibility(View.GONE);
            }
        } else {
            holder.timestampText.setVisibility(View.GONE);
        }

        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            holder.notificationImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(notification.getImageUrl())
                    .centerCrop()
                    .into(holder.notificationImage);
        } else {
            holder.notificationImage.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        // Add long-press listener
        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNotificationLongClick(v, notification);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification);
        notifyItemInserted(0);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView titleText;
        TextView senderText;
        TextView messageText;
        TextView timestampText;
        ImageView notificationImage;

        NotificationViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleText = itemView.findViewById(R.id.notificationItemTitle);
            senderText = itemView.findViewById(R.id.notificationItemSender);
            messageText = itemView.findViewById(R.id.notificationItemMessage);
            timestampText = itemView.findViewById(R.id.notificationItemTimestamp);
            notificationImage = itemView.findViewById(R.id.notificationItemImage);
        }
    }
}