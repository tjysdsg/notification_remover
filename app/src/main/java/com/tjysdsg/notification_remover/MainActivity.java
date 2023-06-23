package com.tjysdsg.notification_remover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.content.ComponentName;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        INotificationListenerActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView notificationList;
    NotificationListAdapter notificationListAdapter;
    NotificationListener notificationListener;
    Intent notificationListenerServiceIntent;
    NotificationServiceConnection notificationServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // pull down to refresh
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        notificationList = findViewById(R.id.notification_list);

        notificationServiceConnection = new NotificationServiceConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ask user for permission to manage notifications
        if (isNotificationServiceEnabled()) {
            startNotificationListenerService();
        } else {
            AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
    }

    /**
     * Start the notification listener service. Do nothing if it's already running
     */
    public void startNotificationListenerService() {
        assert isNotificationServiceEnabled();

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
            Log.e("MainActivity", "stopService returned " + stopped);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopNotificationListenerService();
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

    /**
     * Check whether the notification listener service is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled() {
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

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     *
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                (dialog, id) -> jumpToNotificationServicePermissionSettingPage()
        );
        alertDialogBuilder.setNegativeButton(R.string.no,
                (dialog, id) -> {
                    // If you choose to not enable the notification listener
                    // the app. will not work as expected
                }
        );
        return (alertDialogBuilder.create());
    }

    private void jumpToNotificationServicePermissionSettingPage() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

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