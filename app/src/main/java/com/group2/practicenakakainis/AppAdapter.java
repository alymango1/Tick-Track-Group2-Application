package com.group2.practicenakakainis;

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

import java.util.List;

public class AppAdapter extends ArrayAdapter<App> {

    private Context context;
    private List<App> appList;
    private LayoutInflater inflater;
    private OnSwitchClickListener onSwitchClickListener;
    private SparseBooleanArray itemStateArray = new SparseBooleanArray();


    public AppAdapter(@NonNull Context context, @NonNull List<App> objects) {
        super(context, 0, objects);
        this.context = context;
        this.appList = objects;
        inflater = LayoutInflater.from(context);
    }

    public void setOnSwitchClickListener(OnSwitchClickListener listener) {
        this.onSwitchClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItems = convertView;
        if (listItems == null) {
            listItems = inflater.inflate(R.layout.app_view, parent, false);
        }

        App app = appList.get(position);
        TextView name = listItems.findViewById(R.id.appName);
        ImageView icon = listItems.findViewById(R.id.appIcon);
        Switch switcher = listItems.findViewById(R.id.switcher);

        name.setText(app.name);
        icon.setImageDrawable(app.icon);

        switcher.setOnCheckedChangeListener(null);

        // Restore the state of the switch from the SparseBooleanArray
        if (itemStateArray.get(position, false)) {
            switcher.setChecked(true);
        } else {
            switcher.setChecked(false);
        }

        switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the state of the switch to the SparseBooleanArray
            itemStateArray.put(position, isChecked);

            if (onSwitchClickListener != null) {
                onSwitchClickListener.onSwitchClick(position, isChecked);
            }
        });

        return listItems;
    }

    public interface OnSwitchClickListener {
        void onSwitchClick(int position, boolean isChecked);
    }
}
