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

    private Context context;
    private List<App> appList;
    private OnSwitchClickListener onSwitchClickListener;
    private SparseBooleanArray itemStateArray = new SparseBooleanArray();

    public AppAdapter(@NonNull Context context, @NonNull List<App> objects) {
        this.context = context;
        this.appList = objects;
    }

    public void setOnSwitchClickListener(OnSwitchClickListener listener) {
        this.onSwitchClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        App app = appList.get(position);

        holder.name.setText(app.name);
        holder.icon.setImageDrawable(app.icon);

        // Retrieve the state of the switch from SharedPreferences
        boolean isChecked = loadSwitchState(app.activityInfo);
        holder.switcher.setChecked(isChecked);

        holder.switcher.setOnCheckedChangeListener((buttonView, isCheckedSwitch) -> {
            // Save the state of the switch to SharedPreferences and SparseBooleanArray
            saveSwitchState(app.activityInfo, isCheckedSwitch);
            itemStateArray.put(position, isCheckedSwitch);

            if (onSwitchClickListener != null) {
                onSwitchClickListener.onSwitchClick(position, isCheckedSwitch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private void saveSwitchState(String key, boolean state) {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, state)
                .apply();
    }

    private boolean loadSwitchState(String key) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }

    public interface OnSwitchClickListener {
        void onSwitchClick(int position, boolean isChecked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        Switch switcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            icon = itemView.findViewById(R.id.appIcon);
            switcher = itemView.findViewById(R.id.switcher);
        }
    }
}
