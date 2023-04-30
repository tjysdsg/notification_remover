package com.tjysdsg.notification_remover;

import android.service.notification.StatusBarNotification;

import java.util.function.Consumer;

public class NotificationItem {
    private StatusBarNotification sbn = null;
    private boolean isActive = false;
    private Consumer<Boolean> setActiveCallback = null;

    public NotificationItem(StatusBarNotification sbn, boolean isActive, Consumer<Boolean> setActiveCallback) {
        this.sbn = sbn;
        this.isActive = isActive;
        this.setActiveCallback = setActiveCallback;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (setActiveCallback != null)
            setActiveCallback.accept(active);
    }
}
