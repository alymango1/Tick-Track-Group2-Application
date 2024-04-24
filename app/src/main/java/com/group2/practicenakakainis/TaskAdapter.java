package com.group2.practicenakakainis;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends ArrayAdapter<ToDoModel> implements OnAlarmTriggeredListener   {

    private final Context context;
    private final List<ToDoModel> tasks;

    public static Handler handler;
    public static Runnable runnable;

    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent;

    private MediaPlayer clickSound;
    private MediaPlayer popSound;

    public TaskAdapter(Context context, List<ToDoModel> tasks) {
        super(context, R.layout.task_layout, tasks);
        this.context = context;
        this.tasks = tasks;
        clickSound = MediaPlayer.create(context, R.raw.button_sound);
        popSound = MediaPlayer.create(context, R.raw.pop);
    }
    public void updateUIForPausedState(ViewHolder holder) {
        holder.pauseButton.setVisibility(View.GONE);
        holder.continueButton.setVisibility(View.VISIBLE);
    }

    public void updateUIForRunningState(ViewHolder holder) {
        holder.pauseButton.setVisibility(View.VISIBLE);
        holder.continueButton.setVisibility(View.GONE);
        holder.stopButton.setVisibility(View.VISIBLE);
        holder.moreSettingsButton.setVisibility(View.GONE);
    }

    public void updateUIForStoppedState(ViewHolder holder) {
        holder.pauseButton.setVisibility(View.GONE);
        holder.continueButton.setVisibility(View.GONE);
        holder.stopButton.setVisibility(View.GONE);
        holder.moreSettingsButton.setVisibility(View.VISIBLE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

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
                Button startButton = dialog.findViewById(R.id.start_task);
                Button cancelButton = dialog.findViewById(R.id.cancel_task);

                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MainActivity.isTimeSelected) {
                            if (MainActivity.pauseStartTime != 0) {
                                long pauseDuration = System.currentTimeMillis() - MainActivity.pauseStartTime;
                                MainActivity.selectedTimeInMillis += pauseDuration;
                                MainActivity.pauseStartTime = 0;
                            }

                            if (MainActivity.isSessionActive) {
                                // Show a toast message
                                Toast toast = Toast.makeText(context, "A session is already active.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 100);
                                toast.show();
                                dialog.dismiss();
                            } else if (!MainActivity.areAppsSelected) {
                                Toast toast = Toast.makeText(context, "You haven't blocked anything yet.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 100);
                                toast.show();
                            } else {
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

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                                final long[] timeLeft = {MainActivity.selectedTimeInMillis - System.currentTimeMillis()};

                                handler = new Handler();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    CharSequence name = "Tick Track";
                                    String description = "Alarm Countdown";
                                    int importance = NotificationManager.IMPORTANCE_LOW;
                                    NotificationChannel channel = new NotificationChannel("ticktrack", name, importance);
                                    channel.setDescription(description);
                                    channel.setSound(null, null);
                                    NotificationManager notificationManage = context.getSystemService(NotificationManager.class);
                                    notificationManage.createNotificationChannel(channel);
                                }

                                runnable = new Runnable() {
                                    @SuppressLint("MissingPermission")
                                    @Override
                                    public void run() {
                                        timeLeft[0] -= 1000;


                                        if (timeLeft[0] >= 1500) {
                                            long hours = (timeLeft[0] / 1000) / 3600;
                                            long minutes = ((timeLeft[0] / 1000) % 3600) / 60;
                                            long seconds = (timeLeft[0] / 1000) % 60;

                                            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                                            // Create an intent to open the app when the notification is tapped
                                            Intent intent = new Intent(context, MainActivity.class);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ticktrack")
                                                    .setSmallIcon(R.drawable.baseline_track_changes_24)
                                                    .setContentTitle("Tick Track")
                                                    .setContentText("Time until Alarm " + timeLeftFormatted)
                                                    .setContentIntent(pendingIntent)  // Set the pending intent
                                                    .setAutoCancel(true)  // Dismiss the notification when tapped
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)  // Set the priority to high to show as a heads-up notification
                                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // Set the visibility to public to show the full content on the lock screen
                                                    .setSound(null);

                                            // Show the heads-up notification
                                            notificationManager.notify(123, builder.build());

                                            handler.postDelayed(this, 1000);
                                        } else {
                                            // Timer has reached 00:00:00
                                            handler.removeCallbacks(this);

                                            // Update UI directly within the TaskAdapter
                                            TaskAdapter.handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    holder.pauseButton.setVisibility(View.GONE);
                                                    holder.stopButton.setVisibility(View.GONE);
                                                    holder.continueButton.setVisibility(View.GONE);
                                                    holder.moreSettingsButton.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }
                                    }
                                };


                                handler.post(runnable);
                            }

                                // Close the dialog
                                dialog.dismiss();

                                // Show a toast message indicating the timer has started
                                Toast toast = Toast.makeText(context, "Applications are now blocked.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 100);
                                toast.show();
                            }
                        }
                });

                holder.pauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.shouldBlockApps = false;

                        MainActivity.pauseStartTime = System.currentTimeMillis();

                        // Stop the handler callbacks
                        TaskAdapter.handler.removeCallbacks(TaskAdapter.runnable);

                        // Cancel the existing alarm
                        TaskAdapter.alarmManager.cancel(TaskAdapter.pendingIntent);

                        Toast.makeText(context, "The timer has been paused.", Toast.LENGTH_SHORT).show();
                        updateUIForPausedState(holder);  // Update UI for paused state
                    }
                });

                holder.continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.shouldBlockApps = true;
                        // Calculate the pause duration
                        long pauseDuration = System.currentTimeMillis() - MainActivity.pauseStartTime;

                        // Adjust the alarm time
                        MainActivity.selectedTimeInMillis += pauseDuration;

                        // Reset the alarm
                        setAlarm(context, task.getTask(), MainActivity.selectedTimeInMillis);

                        // Start the handler callbacks
                        TaskAdapter.handler.post(TaskAdapter.runnable);

                        // Reset pauseStartTime
                        MainActivity.pauseStartTime = 0;

                        Toast.makeText(context, "The timer has been resumed.", Toast.LENGTH_SHORT).show();
                        updateUIForRunningState(holder);  // Update UI for running state
                    }
                });

                holder.stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.shouldBlockApps = false;
                        alarmManager.cancel(pendingIntent);
                        handler.removeCallbacks(runnable);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        notificationManager.cancel(123);

                        Toast.makeText(context, "The timer has been stopped.", Toast.LENGTH_SHORT).show();
                        updateUIForStoppedState(holder);  // Update UI for stopped state
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

                // Set the title of the pin_task MenuItem based on the pinned status of the task
                MenuItem pinMenuItem = popup.getMenu().findItem(R.id.pin_task);
                if (task.isPinned()) {
                    pinMenuItem.setTitle("Unpin");
                } else {
                    pinMenuItem.setTitle("Pin");
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        ToDoModel clickedTask = tasks.get(clickedPosition);  // Use the clicked position
                        popSound.start();

                        if (id == R.id.delete_task) {
                            ((MainActivity) context).deleteTask(clickedTask.getId());
                            clickSound.start();
                            return true;
                        } else if (id == R.id.change_color) {
                            ((MainActivity) context).changeColor(clickedTask);
                            clickSound.start();
                            return true;
                        } else if (id == R.id.change_name) {
                            ((MainActivity) context).changeName(clickedTask);
                            clickSound.start();
                            return true;
                        } else if (id == R.id.pin_task) {
                            if (clickedTask.isPinned()) {
                                ((MainActivity) context).unpinTask(tasks, clickedTask, TaskAdapter.this);
                                item.setTitle("Pin");  // Update the MenuItem title to "Pin"
                                clickedTask.setPinned(false);  // Update the pinned status of the task
                            } else {
                                ((MainActivity) context).pinTask(tasks, clickedTask, TaskAdapter.this);
                                item.setTitle("Unpin");  // Update the MenuItem title to "Unpin"
                                clickedTask.setPinned(true);  // Update the pinned status of the task
                            }

                            // Notify the adapter of the data change
                            notifyDataSetChanged();
                            return true;
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });

        return convertView;
    }

    private void setAlarm(Context context, String taskName, long timeInMillis) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("taskName", taskName);  // Pass the task name

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    @Override
    public void onAlarmTriggered(HomeFragment homeFragment) {

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

    public interface OnAlarmTriggeredListener {
        void onAlarmTriggered(HomeFragment homeFragment);
        TaskAdapter getTaskAdapter();
    }
    @Override
    public TaskAdapter getTaskAdapter() {
        return this;
    }
}