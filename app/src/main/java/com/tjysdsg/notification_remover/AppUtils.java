package com.tjysdsg.notification_remover;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppUtils {
    public static String getAppName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        return (String) packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        );
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("AppUtils.isServiceRunning", "Running service - " + service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
