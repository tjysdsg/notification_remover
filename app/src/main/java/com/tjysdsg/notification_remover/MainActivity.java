package com.tjysdsg.notification_remover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends NotificationListenerActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView notificationList;
    NotificationListAdapter notificationListAdapter;
    NotificationListener notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // pull down to refresh
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        notificationList = findViewById(R.id.notification_list);
    }

    // Swipe down to refresh
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRefresh() {
        if (notificationListener == null) return;
        notificationListener.retrieveCurrentStatusBarNotifications();
        notificationListAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void askForNotificationServicePermission() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                (dialog, id) -> jumpToNotificationServicePermissionSettingPage()
        );
        alertDialogBuilder.setNegativeButton(R.string.no,
                (dialog, id) -> {
                    // If you choose to not enable the notification listener
                    // the app. will not work as expected
                }
        );

        var dialog = (alertDialogBuilder.create());
        dialog.show();
    }

    @Override
    public void onNotificationListenerServiceStarted(NotificationListener listener) {
        this.notificationListener = listener;

        listener.retrieveCurrentStatusBarNotifications();

        notificationListAdapter = new NotificationListAdapter(notificationListener);
        notificationList.setAdapter(notificationListAdapter);
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        notificationListener.registerListenerCallback(
                (l) -> {
                    notificationListAdapter.notifyDataSetChanged();
                }
        );
    }

    @Override
    public void onNotificationListenerServiceStopped() {
        notificationListener = null;
    }
}