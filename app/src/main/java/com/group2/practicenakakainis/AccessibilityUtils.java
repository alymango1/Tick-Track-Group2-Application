package com.group2.practicenakakainis;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class AccessibilityUtils {

    private static final String PREF_NAME = "AccessibilityPrefs";
    private static final String KEY_SETTINGS_CHECKED = "settingsChecked";

    public static boolean isAccessibilityServiceRunning(Context context, Class<?> serviceClass) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : enabledServices) {
            ComponentName enabledService = new ComponentName(service.getResolveInfo().serviceInfo.packageName, service.getResolveInfo().serviceInfo.name);
            ComponentName targetService = new ComponentName(context, serviceClass);

            if (enabledService.equals(targetService)) {
                return true;
            }
        }

        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Reset the accessibility settings checked flag.
     *
     * @param context The context of the application.
     */
    public static void resetAccessibilitySettingsChecked(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SETTINGS_CHECKED, false).apply();
    }

    public static void checkAndOpenAccessibilitySettings(Context context, Class<?> serviceClass) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean settingsChecked = prefs.getBoolean(KEY_SETTINGS_CHECKED, false);

        if (!settingsChecked && !isAccessibilityServiceRunning(context, serviceClass)) {
            openAccessibilitySettings(context);
            prefs.edit().putBoolean(KEY_SETTINGS_CHECKED, true).apply();
        }
    }


    public static void setAccessibilitySettingsChecked(Context context, boolean isChecked) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SETTINGS_CHECKED, isChecked).apply();
    }
}
