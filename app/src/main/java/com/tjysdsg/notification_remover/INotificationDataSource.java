package com.tjysdsg.notification_remover;

import android.service.notification.StatusBarNotification;

import java.util.List;

public interface INotificationDataSource {
    List<StatusBarNotification> getAllNotifications();

    // TODO: better design
    void hideOngoingNotification(StatusBarNotification sbn);

    void unHideOngoingNotification(StatusBarNotification sbn);
}
