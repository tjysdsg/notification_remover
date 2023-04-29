package com.tjysdsg.notification_remover;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "Notification Listener";

    private static NotificationListener Singleton;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void ClearNotifications() {
        StatusBarNotification[] activeNotifications = this.getActiveNotifications();

        if (activeNotifications != null && activeNotifications.length > 0) {
            for (StatusBarNotification notification : activeNotifications) {
                if (matchNotificationCode(notification)) {
                    Log.e(TAG, "" + notification.isClearable());
                    Log.e(TAG, "" + notification.isOngoing());
                    // Will not work: cancelNotification(notification.getKey());
                    notificationManager.cancel(notification.getId());
                    // 100 years, mfk!
                    snoozeNotification(notification.getKey(), 100L * 365 * 24 * 3600 * 1000);
                }
            }
        }
    }

    private boolean matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        Log.d(TAG, "Notification from app: " + packageName);
        return packageName.equals(getString(R.string.voicemail_package_name));
    }
}
