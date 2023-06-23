package com.tjysdsg.notification_remover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.content.ComponentName;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

abstract public class NotificationListenerActivity extends AppCompatActivity implements INotificationListenerOwner {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    Intent notificationListenerServiceIntent;
    NotificationServiceConnection notificationServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationServiceConnection = new NotificationServiceConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ask user for permission to manage notifications
        if (hasNotificationServicePermission()) {
            startNotificationListenerService();
            askForNotificationServicePermission(false);
        } else {
            askForNotificationServicePermission(true);
        }
    }

    protected abstract void askForNotificationServicePermission(boolean needPermission);

    /**
     * Start the notification listener service. Do nothing if it's already running
     */
    public void startNotificationListenerService() {
        assert hasNotificationServicePermission();

        if (notificationServiceConnection.isConnected()) return;

        notificationListenerServiceIntent = new Intent(
                this,
                NotificationListener.class
        );
        boolean res = bindService(
                notificationListenerServiceIntent, notificationServiceConnection,
                Context.BIND_AUTO_CREATE
        );

        if (!res) {
            // TODO: show error dialog
            throw new RuntimeException("Failed to bindService");
        }
    }

    /**
     * Stop the notification service if it's running.
     */
    public void stopNotificationListenerService() {
        if (notificationServiceConnection.isConnected()) {
            unbindService(notificationServiceConnection);

            boolean stopped = stopService(notificationListenerServiceIntent);
            Log.e(getClass().getName(), "stopService returned " + stopped);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopNotificationListenerService();
    }

    /**
     * Check whether the notification listener service is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    protected boolean hasNotificationServicePermission() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void jumpToNotificationServicePermissionSettingPage() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

}
