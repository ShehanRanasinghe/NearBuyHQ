package com.example.nearbuyhq.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationsList;

    public NotificationsAdapter(List<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    /** Replace the full list and refresh the RecyclerView (called after Firestore load). */
    public void updateList(List<Notification> newList) {
        this.notificationsList = newList;
        notifyDataSetChanged();
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
        Notification notification = notificationsList.get(position);
        holder.notificationTitle.setText(notification.getTitle());
        holder.notificationMessage.setText(notification.getMessage());
        holder.notificationTime.setText(notification.getTime());
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle, notificationMessage, notificationTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationTime = itemView.findViewById(R.id.notificationTime);
        }
    }
}

