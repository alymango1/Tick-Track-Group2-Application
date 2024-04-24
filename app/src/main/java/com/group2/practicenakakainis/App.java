package com.group2.practicenakakainis;

import android.graphics.drawable.Drawable;

public class App {
    String name;
    String activityInfo;
    Drawable icon;
    public boolean isBlocked;

    public App() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(String activityInfo) {
        this.activityInfo = activityInfo;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
