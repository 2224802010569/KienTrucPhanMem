package com.example.fintrack.AlertService.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.AlertService.entity.Reminder;
import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.NotificationService.data.entity.AppNotification;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    List<Reminder> list;
    ReminderListener listener;

    public ReminderAdapter(List<Reminder> list) {
        this.list = list;
    }

    public interface ReminderListener {

        void onEdit(Reminder r);

        void onDelete(Reminder r);
    }

    public void setListener(ReminderListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Reminder r = list.get(position);

        holder.txtTitle.setText(r.getTitle());

        holder.txtInfo.setText(
                "Due day: "
                        + r.getDay()
                        + " • "
                        + String.format("%,.0f", r.getAmount())
                        + " VND"
        );

        holder.swReminder.setOnCheckedChangeListener(null);
        holder.swReminder.setChecked(r.isEnabled());

        holder.swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {

            r.enabled = isChecked;

            if (isChecked) {

                NotificationRepository repo =
                        new NotificationRepository(holder.itemView.getContext());

                repo.pushAndSave(
                        "Bill Reminder",
                        "📅 " + r.title + " - "
                                + String.format("%,.0f", r.amount)
                                + " VND",
                        AppNotification.TYPE_ALERT
                );
            }
        });

        // LONG CLICK -> EDIT / DELETE
        holder.itemView.setOnLongClickListener(v -> {

            PopupMenu menu = new PopupMenu(v.getContext(), v);

            menu.getMenu().add("Edit");
            menu.getMenu().add("Delete");

            menu.setOnMenuItemClickListener(item -> {

                if (item.getTitle().equals("Edit")) {

                    if (listener != null) {
                        listener.onEdit(r);
                    }

                } else if (item.getTitle().equals("Delete")) {

                    if (listener != null) {
                        listener.onDelete(r);
                    }
                }

                return true;
            });

            menu.show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtInfo;
        Switch swReminder;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            swReminder = itemView.findViewById(R.id.swReminder);
        }
    }
}