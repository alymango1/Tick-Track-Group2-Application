package com.group2.practicenakakainis;

import static androidx.core.app.ActivityCompat.recreate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsViewHolder> {

    private final List<String> values;
    private final DarkModeListener darkModeListener;

    public SettingsAdapter(List<String> values, DarkModeListener darkModeListener) {
        this.values = values;
        this.darkModeListener = darkModeListener;
    }

    @Override
    public SettingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_layout, parent, false);
        return new SettingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SettingsViewHolder holder, int position) {
        holder.textView.setText(values.get(position));
        holder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleDarkMode(isChecked);
            }


            private void toggleDarkMode(boolean isDarkMode) {
                if (isDarkMode) {
                    // Enable dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    // Disable dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                // Notify the listener to recreate the activity
                if (darkModeListener != null) {
                    darkModeListener.onDarkModeChanged(isDarkMode);
                }
            }
        });
    }
    private void toggleDarkMode(boolean isDarkMode) {
        if (darkModeListener != null) {
            darkModeListener.onDarkModeChanged(isDarkMode);
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
    public interface DarkModeListener {
        void onDarkModeChanged(boolean isDarkMode);
    }

}