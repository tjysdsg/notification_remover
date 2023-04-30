package com.tjysdsg.notification_remover;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class NotificationListener extends NotificationListenerService implements INotificationDataSource {
    private static final String TAG = "Notification Listener";

    private static final long OneHundredYearMS = 100L * 365 * 24 * 3600 * 1000;

    private final IBinder binder = new NotificationListenerBinder();

    private final List<Consumer<NotificationListener>> callbacks = new ArrayList<>();

    private final List<StatusBarNotification> notifications = new ArrayList<>();

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        TriggerAllCallbacks();
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected");
        TriggerAllCallbacks();
    }

    public void registerListenerCallback(Consumer<NotificationListener> callback) {
        callbacks.add(callback);
    }

    @Override
    public void hideOngoingNotification(StatusBarNotification sbn) {
        this.snoozeOngoingNotification(sbn, OneHundredYearMS);
    }

    @Override
    public void unHideOngoingNotification(StatusBarNotification sbn) {
        this.snoozeOngoingNotification(sbn, 1);
    }

    public void snoozeOngoingNotification(StatusBarNotification sbn, long milliseconds) {
        // Will not work: notificationManager.cancel(notification.getId());
        // But hey, 100 years, mfk!
        snoozeNotification(sbn.getKey(), milliseconds);
    }

    public void retrieveCurrentStatusBarNotifications() {
        notifications.clear();
        var activeNotifications = this.getActiveNotifications();
        if (activeNotifications != null) {
            notifications.addAll(Arrays.asList(activeNotifications));
        }
        var snoozedNotifications = this.getSnoozedNotifications();
        if (snoozedNotifications != null) {
            notifications.addAll(Arrays.asList(snoozedNotifications));
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        retrieveCurrentStatusBarNotifications();
        TriggerAllCallbacks();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        retrieveCurrentStatusBarNotifications();
        TriggerAllCallbacks();
    }

    @Override
    public List<StatusBarNotification> getAllNotifications() {
        return notifications;
    }

    private void TriggerAllCallbacks() {
        for (var c : callbacks) {
            c.accept(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();

        if (SERVICE_INTERFACE.equals(action)) {
            Log.d(TAG, "Bound by system");
            return super.onBind(intent);
        } else {
            Log.d(TAG, "Bound by application");
            return binder;
        }
    }

    public class NotificationListenerBinder extends Binder {
        public NotificationListener getService() {
            return NotificationListener.this;
        }
    }
}
