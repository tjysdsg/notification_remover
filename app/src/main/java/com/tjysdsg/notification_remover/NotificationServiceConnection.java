package com.tjysdsg.notification_remover;

import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.ComponentName;
import android.service.notification.NotificationListenerService;
import android.util.Log;


public class NotificationServiceConnection implements ServiceConnection {
    private static final String TAG = "NotificationServiceConnection";

    private boolean isConnected = false;
    private final INotificationListenerOwner owner;

    public NotificationServiceConnection(INotificationListenerOwner a) {
        this.owner = a;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        isConnected = true;

        var binder = (NotificationListener.NotificationListenerBinder) service;
        NotificationListener listener = binder.getService();

        owner.onNotificationListenerServiceStarted(listener);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Log.e(TAG, "onServiceDisconnected");
        isConnected = true;
        owner.onNotificationListenerServiceStopped();
    }

    @Override
    public void onNullBinding(ComponentName name) {
        throw new RuntimeException("Null binding received");
    }

    public boolean isConnected() {
        return isConnected;
    }
}
