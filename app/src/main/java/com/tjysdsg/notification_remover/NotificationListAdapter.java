package com.tjysdsg.notification_remover;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {
    private final INotificationDataSource dataSource;
    private int notificationIconColor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        CheckBox checkBox;
        ImageView icon;
        TextView appName;
        TextView title;
        TextView desc;
        int iconColor;

        public ViewHolder(View view, int iconColor) {
            super(view);
            this.view = view;
            this.checkBox = view.findViewById(R.id.clear_notification_checkbox);
            this.icon = view.findViewById(R.id.notification_icon);
            this.appName = view.findViewById(R.id.app_name);
            this.title = view.findViewById(R.id.notification_title);
            this.desc = view.findViewById(R.id.notification_detail);
            this.iconColor = iconColor;
        }

        public void update(NotificationItem notification) {
            checkBox.setOnCheckedChangeListener(null);

            // set checkBox initial status based on whether notification is snoozed
            checkBox.setChecked(!notification.isActive());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    notification.setActive(!isChecked);
                }
            });

            // set icon
            var sbn = notification.getSbn();
            Bundle extras = sbn.getNotification().extras;
            Icon notificationIcon = sbn.getNotification().getSmallIcon();
            notificationIcon.setTint(iconColor);
            icon.setImageIcon(notificationIcon);

            // app name
            String appName_ = null;
            try {
                appName_ = AppUtils.getAppName(appName.getContext(), sbn.getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("NotificationListAdapter", "Cannot get the app name of package: " + sbn.getPackageName());
            }
            appName.setText(appName_);

            // title and description
            title.setText(extras.getCharSequence(Notification.EXTRA_TITLE));
            desc.setText(extras.getCharSequence(Notification.EXTRA_TEXT));
        }
    }

    public NotificationListAdapter(INotificationDataSource dataSource, int notificationIconColor) {
        this.dataSource = dataSource;
        this.notificationIconColor = notificationIconColor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.notification_item_view, viewGroup, false);

        return new ViewHolder(view, notificationIconColor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.update(dataSource.getAllNotifications().get(position));
    }

    @Override
    public int getItemCount() {
        return dataSource.getAllNotifications().size();
    }
}
