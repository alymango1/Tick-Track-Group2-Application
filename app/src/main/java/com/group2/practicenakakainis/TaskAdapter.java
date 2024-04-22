package com.group2.practicenakakainis;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<ToDoModel> {

    private final Context context;
    private final List<ToDoModel> tasks;

    public TaskAdapter(Context context, List<ToDoModel> tasks) {
        super(context, R.layout.task_layout, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        MediaPlayer clickSound = MediaPlayer.create(getContext(), R.raw.button_sound);
        MediaPlayer popSound = MediaPlayer.create(getContext(), R.raw.pop);


        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.task_layout, parent, false);

            holder = new ViewHolder();
            holder.button = convertView.findViewById(R.id.button_item);
            holder.moreSettingsButton = convertView.findViewById(R.id.more_settings);
            holder.pauseButton = convertView.findViewById(R.id.pause_button);
            holder.continueButton = convertView.findViewById(R.id.continue_button);
            holder.stopButton = convertView.findViewById(R.id.stop_button);
            holder.layout = convertView.findViewById(R.id.layout_color);
            holder.pinIcon = convertView.findViewById(R.id.pin_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ToDoModel task = tasks.get(position);
        holder.button.setText(task.getTask());

        holder.layout.setBackgroundColor(task.getColor());

        if (task.isPinned()) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.GONE);
        }


        // Set the click listener on the Button
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.task_dialog_content);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                clickSound.start();

                // Find the "Start" button in the dialog
                Button startButton = dialog.findViewById(R.id.start_task);  // Replace with the actual ID of your "Start" button
                Button cancelButton = dialog.findViewById(R.id.cancel_task);

                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MainActivity.isTimeSelected) {
                            if (MainActivity.isSessionActive) {
                                // Show a toast message
                                Toast toast = Toast.makeText(context, "A session is already active.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM,0,100);
                                toast.show();
                                dialog.dismiss();

                            } else if (!MainActivity.areAppsSelected){
                                Toast toast = Toast.makeText(context, "You haven't blocked anything yet.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM,0,100);
                                toast.show();

                            }else {
                                // Start the timer
                                MainActivity.shouldBlockApps = true;
                                MainActivity.isSessionActive = true;

                                // Call startTask method
                                ((MainActivity) context).startTask(task.getId());

                                if (MainActivity.isSessionActive) {
                                    holder.pauseButton.setVisibility(View.VISIBLE);
                                    holder.moreSettingsButton.setVisibility(View.GONE);
                                    holder.stopButton.setVisibility(View.VISIBLE);
                                }

                                // Set the alarm
                                setAlarm(context, task.getTask(), MainActivity.selectedTimeInMillis);

                                // Close the dialog
                                dialog.dismiss();

                                // Show a toast message indicating the timer has started
                                Toast toast = Toast.makeText(context, "Applications are now blocked.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM,0,100);
                                toast.show();
                            }
                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });

        holder.moreSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = position;  // Store the clicked position

                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.more_settings_menu, popup.getMenu());
                final TaskAdapter adapter = TaskAdapter.this;

                // Set the title of the pin_task MenuItem based on the pinned status of the task
                MenuItem pinMenuItem = popup.getMenu().findItem(R.id.pin_task);
                if (task.isPinned()) {
                    pinMenuItem.setTitle("Unpin");
                } else {
                    pinMenuItem.setTitle("Pin");
                }

                PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        ToDoModel task = tasks.get(clickedPosition);  // Use the clicked position
                        popSound.start();
                        if (id == R.id.delete_task) {
                            ((MainActivity) context).deleteTask(task.getId());
                            clickSound.start();
                            return true;
                        } else if (id == R.id.change_color){
                            ((MainActivity) context).changeColor(task);
                            clickSound.start();
                            return true;
                        } else if (id == R.id.change_name){
                            ((MainActivity) context).changeName(task);
                            clickSound.start();
                            return true;
                        } else if (id == R.id.pin_task) {
                            if (task.isPinned()) {
                                ((MainActivity) context).unpinTask(tasks, task, adapter);
                                item.setTitle("Pin");  // Update the MenuItem title to "Pin"
                                task.setPinned(false);  // Update the pinned status of the task
                            } else {
                                ((MainActivity) context).pinTask(tasks, task, adapter);
                                item.setTitle("Unpin");  // Update the MenuItem title to "Unpin"
                                task.setPinned(true);  // Update the pinned status of the task
                            }

                            // Notify the adapter of the data change
                            adapter.notifyDataSetChanged();
                            return true;
                        }

                        return false;
                    }
                };

                popup.setOnMenuItemClickListener(listener);
                popup.show();
            }
        });



        return convertView;
    }

    private void setAlarm(Context context, String taskName, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("taskName", taskName);  // Pass the task name
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        }

        // Set the exact alarm time here
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }


    static class ViewHolder {
        Button button;
        ImageButton moreSettingsButton;

        ImageButton pauseButton;

        ImageButton stopButton;
        ImageButton continueButton;

        RelativeLayout layout;
        ImageView pinIcon;
    }
}

