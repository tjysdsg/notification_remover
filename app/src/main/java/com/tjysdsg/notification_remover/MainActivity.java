package com.tjysdsg.notification_remover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends NotificationListenerActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView notificationList;
    NotificationListAdapter notificationListAdapter;
    NotificationListener notificationListener;
    BottomSheet notificationServicePermissionBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        notificationServicePermissionBottomSheet = new BottomSheet();
        notificationServicePermissionBottomSheet.show(getSupportFragmentManager(), BottomSheet.TAG);
        notificationServicePermissionBottomSheet.hide(true);

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
    protected void askForNotificationServicePermission(boolean needPermission) {
        if (needPermission) {
            notificationServicePermissionBottomSheet.hide(false);
            notificationServicePermissionBottomSheet.setOnClickListener(
                    (View v) -> jumpToNotificationServicePermissionSettingPage()
            );
        } else {
            notificationServicePermissionBottomSheet.hide(true);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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