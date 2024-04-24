package com.group2.practicenakakainis;

import static com.group2.practicenakakainis.MainActivity.SHARED_PREFERENCES_NAME;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;



public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<App> appList;
    private OnSwitchClickListener onSwitchClickListener;

    public AppAdapter(@NonNull List<App> objects) {
        this.appList = objects;
    }

    public void setOnSwitchClickListener(OnSwitchClickListener listener) {
        this.onSwitchClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_view, parent, false);
        return new ViewHolder(view, onSwitchClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        App app = appList.get(position);

        holder.name.setText(app.name);
        holder.icon.setImageDrawable(app.icon);

        holder.switcher.setOnCheckedChangeListener(null); // Prevent triggering listener during recycling
        holder.switcher.setChecked(app.isBlocked);

        holder.switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.isBlocked = isChecked; // Update the state in the dataset
            if (onSwitchClickListener != null) {
                onSwitchClickListener.onSwitchClick(position, isChecked);
            }
        });
    }


    @Override
    public int getItemCount() {
        return appList.size();
    }

    public interface OnSwitchClickListener {
        void onSwitchClick(int position, boolean isChecked);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        Switch switcher;
        OnSwitchClickListener onSwitchClickListener;

        public ViewHolder(@NonNull View itemView, OnSwitchClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            icon = itemView.findViewById(R.id.appIcon);
            switcher = itemView.findViewById(R.id.switcher);
            this.onSwitchClickListener = listener;
        }

        public void bind(App app) {
            name.setText(app.name);
            icon.setImageDrawable(app.icon);

            // Retrieve the state of the switch
            boolean isChecked = AppListView.getSwitchState(itemView.getContext(), app.activityInfo);
            switcher.setChecked(isChecked);

            switcher.setOnCheckedChangeListener((buttonView, isCheckedSwitch) -> {
                // Save the state of the switch
                AppListView.saveSwitchState(itemView.getContext(), app.activityInfo, isCheckedSwitch);

                if (onSwitchClickListener != null) {
                    onSwitchClickListener.onSwitchClick(getAdapterPosition(), isCheckedSwitch);
                }
            });
        }
    }
}
