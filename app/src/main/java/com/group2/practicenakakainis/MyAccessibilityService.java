package com.group2.practicenakakainis;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashSet;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final String SHARED_PREFERENCES_NAME = "APP_PREFERENCES";
    private static final String BLOCKED_APPS_KEY = "BLOCKED_APPS";


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();

        if (MainActivity.shouldBlockApps && isAppBlocked(packageName)) {
            Log.e(TAG, "Blocked app " + packageName + " was opened. Redirecting to home screen.");

            // Get the application name from the package name
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo applicationInfo;
            String appName = "";
            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                appName = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "");
            } catch (final PackageManager.NameNotFoundException e) {
                appName = packageName;
            }

            // Show toast when a restricted app is opened
            Toast toast = Toast.makeText(this, "Restricted app " + appName + " was opened.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,100);
            toast.show();

            performGlobalAction(GLOBAL_ACTION_HOME);
        }

        if (!MainActivity.shouldBlockApps && isAppBlocked(packageName)){
            Toast toast = Toast.makeText(this, "You haven't started a task yet", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,100);
            toast.show();
        }
    }


    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: oops");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.e(TAG, "Tick Track Service is Connected Succesfully!");
    }

    private boolean isAppBlocked(String packageName) {
        Set<String> blockedApps = getBlockedApps();
        return blockedApps.contains(packageName);
    }

    private Set<String> getBlockedApps() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
    }
}