package com.example.fintrack.NotificationService.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.NotificationService.data.entity.AppNotification;
import com.example.fintrack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<AppNotification> notifications;

    public NotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification note = notifications.get(position);
        holder.tvTitle.setText(note.title);
        holder.tvMessage.setText(note.message);
        
        // Định dạng thời gian: 2m ago, 1h ago, Yesterday...
        holder.tvTime.setText(formatTime(note.timestamp));

        // Đổi Icon và màu sắc dựa trên type
        if (AppNotification.TYPE_TRANSACTION.equals(note.type)) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_send);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_notification_icon_green);
        } else if (AppNotification.TYPE_ALERT.equals(note.type)) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_notification_icon_orange);
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_notification_icon_blue);
        }

        // Độ mờ cho thông báo đã đọc
        holder.itemView.setAlpha(note.isRead ? 0.6f : 1.0f);
    }

    private String formatTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

    public void updateData(List<AppNotification> newData) {
        this.notifications = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivIcon;
        View iconContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}