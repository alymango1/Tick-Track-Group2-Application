package com.group2.practicenakakainis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AppListView extends AppCompatActivity {

    private List<App> appList = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppAdapter adapter;

    private static final String SHARED_PREFERENCES_NAME = "APP_PREFERENCES";
    private static final String BLOCKED_APPS_KEY = "BLOCKED_APPS";
    private static final String SWITCH_STATES_KEY = "SWITCH_STATES";

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

        recyclerView = findViewById(R.id.listApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        queryIntents();

        adapter = new AppAdapter(appList);
        recyclerView.setAdapter(adapter);

        adapter.setOnSwitchClickListener((position, isChecked) -> {
            App selectedApp = appList.get(position);
            showToast(isChecked ? "Application " + selectedApp.name + " is BLOCKED"
                    : "Application " + selectedApp.name + " is UNBLOCKED");
            if (isChecked) {
                blockApp(selectedApp);
                MainActivity.areAppsSelected = true;
            } else {
                unblockApp(selectedApp);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSwitchStates();  // Load switch states from shared preferences
        adapter.notifyDataSetChanged();
        checkAppsBlocked();
    }


    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }

    private void blockApp(App app) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(app.activityInfo);
        saveBlockedApps(blockedApps);

        // Update the switch state
        updateSwitchState(app.activityInfo, true);
        saveSwitchState(this, app.activityInfo, true); // Save switch state in shared preferences
    }

    private void unblockApp(App app) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.remove(app.activityInfo);
        saveBlockedApps(blockedApps);

        // Update the switch state
        updateSwitchState(app.activityInfo, false);
        saveSwitchState(this, app.activityInfo, false); // Save switch state in shared preferences
    }

    private void updateSwitchState(String packageName, boolean isChecked) {
        for (App app : appList) {
            if (packageName.equals(app.activityInfo)) {
                app.isBlocked = isChecked;
                break;
            }
        }
        adapter.notifyDataSetChanged(); // Refresh the RecyclerView to reflect the changes
    }

    private void checkAppsBlocked() {
        MainActivity.areAppsSelected = !getBlockedApps().isEmpty();
    }

    private Set<String> getBlockedApps() {
        return getSharedPreferences().getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
    }

    private void saveBlockedApps(Set<String> blockedApps) {
        getSharedPreferences().edit().putStringSet(BLOCKED_APPS_KEY, blockedApps).apply();
    }

    public static boolean getSwitchState(Context context, String key) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }
    private void loadSwitchStates() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        Map<String, ?> switchStates = sharedPreferences.getAll();
        for (App app : appList) {
            Boolean switchState = (Boolean) switchStates.get(app.activityInfo);
            if (switchState != null) {
                app.isBlocked = switchState;
            }
        }
    }

    public static void saveSwitchState(Context context, String key, boolean state) {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(key, state).apply();
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private void queryIntents() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getPackageManager();

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo application : apps) {
            String packageName = application.activityInfo.packageName;

            if (!"com.group2.practicenakakainis".equals(packageName)) {
                App appItem = new App();
                appItem.name = application.loadLabel(pm).toString();
                appItem.activityInfo = application.activityInfo.packageName;
                appItem.icon = application.loadIcon(pm);

                appList.add(appItem);
            }
        }
    }

    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void goBackButton(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}