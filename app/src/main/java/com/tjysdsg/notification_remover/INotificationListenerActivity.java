package com.tjysdsg.notification_remover;

public interface INotificationListenerActivity {

    void onNotificationListenerServiceStarted(NotificationListener listener);

    void onNotificationListenerServiceStopped();
}
