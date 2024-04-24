package com.group2.practicenakakainis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.DarkModeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);




        // Change the color of the notification bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.beige));

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.settingsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate data
        List<String> values = new ArrayList<>();
        values.add("Dark Mode");


        // Set up the adapter
        SettingsAdapter adapter = new SettingsAdapter(values, this);

        recyclerView.setAdapter(adapter);

            }
    @Override
    public void onDarkModeChanged(boolean isDarkMode) {
        if (isDarkMode) {
            // Enable dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Disable dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate(); // Recreate the activity to apply the new theme
    }
}
