package com.group2.practicenakakainis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.group2.practicenakakainis.MainActivity;


public class AppListView extends AppCompatActivity {
    List<App> appList = new ArrayList<>();
    ListView listView;
    AppAdapter adapter;

    private ProgressDialog progress;
    private static final String SHARED_PREFERENCES_NAME = "APP_PREFERENCES";
    private static final String BLOCKED_APPS_KEY = "BLOCKED_APPS";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app_list_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.listApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        queryIntents();

        adapter = new AppAdapter(this, appList);
        recyclerView.setAdapter(adapter);

        adapter.setOnSwitchClickListener((position, isChecked) -> {
            App selectedApp = appList.get(position);
            if (isChecked) {
                Toast toast = Toast.makeText(this, "Application " + selectedApp.name + " is BLOCKED", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM,0,100);
                toast.show();
                blockApp(selectedApp);
                MainActivity.areAppsSelected = true;
            } else {
                Toast toast = Toast.makeText(this, "Application: " + selectedApp.name +  "is UNBLOCKED", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM,0,100);
                toast.show();
                unblockApp(selectedApp);
            }
        });
    }


    public void blockApp(App app) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(app.activityInfo);
        saveBlockedApps(blockedApps);
    }

    public void unblockApp(App app) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.remove(app.activityInfo);
        saveBlockedApps(blockedApps);
    }

    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public Set<String> getBlockedApps() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
    }

    public void saveBlockedApps(Set<String> blockedApps) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(BLOCKED_APPS_KEY, blockedApps);
        editor.apply();
    }

    public void queryIntents() {
        Intent anotherIntent = new Intent(Intent.ACTION_MAIN);
        anotherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getApplicationContext().getPackageManager();

        List<ResolveInfo> apps = pm.queryIntentActivities(anotherIntent, 0);

        for (ResolveInfo application : apps) {
            String packageName = application.activityInfo.packageName;

            // Skip if the app is this app
            if ("com.group2.practicenakakainis".equals(packageName)) {
                continue;
            }

            App appItem = new App();
            appItem.name = application.loadLabel(pm).toString();
            appItem.activityInfo = application.activityInfo.packageName;
            appItem.icon = application.loadIcon(pm);

            appList.add(appItem);
        }

    }
    public void goBackButton(View view) {
        super.onBackPressed();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}

