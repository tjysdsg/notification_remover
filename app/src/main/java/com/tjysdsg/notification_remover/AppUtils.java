package com.tjysdsg.notification_remover;

import android.content.Context;
import android.content.pm.PackageManager;

public class AppUtils {
    public static String getAppName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        return (String) packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        );
    }

}
