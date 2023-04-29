package com.tjysdsg.notification_remover;

import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "Notification Listener";

    private static NotificationListener Singleton;
    private static final long OneHundredYearMS = 100L * 365 * 24 * 3600 * 1000;
    // private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationListener get() {
        if (Singleton == null) {
            Log.e(TAG, "Not yet connected, try again");
        }
        return Singleton;
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        Singleton = this;
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected");
        Singleton = null;
    }

    public void ClearOngoingNotifications() {
        this.ClearOngoingNotifications(OneHundredYearMS);
    }

    public void ClearOngoingNotifications(long milliseconds) {
        var activeNotifications = this.getActiveNotifications();
        List<StatusBarNotification> notifications = new ArrayList<>();
        if (activeNotifications != null) {
            notifications.addAll(Arrays.asList(activeNotifications));
        }
        var snoozedNotifications = this.getSnoozedNotifications();
        if (snoozedNotifications != null) {
            notifications.addAll(Arrays.asList(snoozedNotifications));
        }

        for (StatusBarNotification notification : notifications) {
            if (matchNotificationCode(notification)) {
                // Will not work: notificationManager.cancel(notification.getId());

                // But hey, 100 years, mfk!
                snoozeNotification(notification.getKey(), milliseconds);
            }
        }
    }

    private boolean matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        Log.d(TAG, "Notification from app: " + packageName);
        return packageName.equals(getString(R.string.voicemail_package_name));
    }
}
