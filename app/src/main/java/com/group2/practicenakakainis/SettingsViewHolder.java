package com.group2.practicenakakainis;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class SettingsViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public Switch switchView;

    public SettingsViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.darkmode);
        switchView = itemView.findViewById(R.id.darkmode_switch);
    }
}
