package com.tjysdsg.notification_remover;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NotificationListener extends NotificationListenerService implements INotificationDataSource {
    private static final String TAG = "Notification Listener";

    private static final long OneHundredYearMS = 100L * 365 * 24 * 3600 * 1000;

    private final IBinder binder = new NotificationListenerBinder();

    private final List<Consumer<NotificationListener>> callbacks = new ArrayList<>();

    private final Map<String, NotificationItem> notificationItems = new LinkedHashMap<>();

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

    public void hideOngoingNotification(StatusBarNotification sbn) {
        this.snoozeOngoingNotification(sbn, OneHundredYearMS);
    }

    public void unHideOngoingNotification(StatusBarNotification sbn) {
        this.snoozeOngoingNotification(sbn, 1);
    }

    public void snoozeOngoingNotification(StatusBarNotification sbn, long milliseconds) {
        // Will not work: notificationManager.cancel(notification.getId());
        // But hey, 100 years, mfk!
        snoozeNotification(sbn.getKey(), milliseconds);
    }

    private Consumer<Boolean> getSetActiveCallback(StatusBarNotification sbn) {
        return (active) -> {
            if (active) {
                unHideOngoingNotification(sbn);
            } else {
                hideOngoingNotification(sbn);
            }
        };
    }

    public void retrieveCurrentStatusBarNotifications() {
        notificationItems.clear();
        var activeNotifications = this.getActiveNotifications();
        if (activeNotifications != null) {
            for (var sbn : activeNotifications) {
                Log.e(TAG, "Active: " + sbn.getPackageName() + ", id: " + sbn.getKey());
                notificationItems.put(
                        sbn.getKey(),
                        new NotificationItem(sbn, true, getSetActiveCallback(sbn))
                );
            }
        }

        var snoozedNotifications = this.getSnoozedNotifications();
        if (snoozedNotifications != null) {
            for (var sbn : snoozedNotifications) {
                Log.e(TAG, "Snoozed: " + sbn.getPackageName() + ", id: " + sbn.getKey());
                notificationItems.put(
                        sbn.getKey(),
                        new NotificationItem(sbn, false, getSetActiveCallback(sbn))
                );
            }
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
    public List<NotificationItem> getAllNotifications() {
        return new ArrayList<>(notificationItems.values());
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
