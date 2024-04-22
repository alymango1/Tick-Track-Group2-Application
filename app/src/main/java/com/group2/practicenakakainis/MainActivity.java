package com.group2.practicenakakainis;

import static java.security.AccessController.getContext;
import android.content.pm.PackageManager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.group2.practicenakakainis.Utils.DataBaseHelper;
import com.group2.practicenakakainis.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    List<String> folderNames = new ArrayList<>();
    TaskAdapter adapter;

    public static final String SHARED_PREFERENCES_NAME = "APP_PREFERENCES";
    private static final String BLOCKED_APPS_KEY = "BLOCKED_APPS";

    public static long selectedTimeInMillis;

    public static boolean shouldBlockApps = false;
    public static boolean isTimeSelected = false;

    public static boolean isSessionActive = false;

    public static boolean isPickerShown = false;

    public static boolean areAppsSelected = false;

    private int activeTaskId = -1;

    private int pickedColor;

    private static final String ACCESSIBILITY_SETTINGS_CHECKED = "ACCESSIBILITY_SETTINGS_CHECKED";
    private SharedPreferences sharedPreferences;

    private DataBaseHelper myDB;


    private final ActivityResultLauncher<Intent> accessibilitySettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (!AccessibilityUtils.isAccessibilityServiceRunning(MainActivity.this, MyAccessibilityService.class)) {
                        Toast toast = Toast.makeText(MainActivity.this, "Accessibility Service is not enabled", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM,0,100);
                        toast.show();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MediaPlayer homeblockSounds = MediaPlayer.create(this, R.raw.homeblock_sound);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (getIntent().getAction() != null && getIntent().getAction().equals("ACTION_STOP")) {
            stopAlarm();
        }

        boolean showDialog = getIntent().getBooleanExtra("showDialog", false);
        if (showDialog) {
            // Show the dialog
            showDialog();
        }

        ImageButton drawerButton = findViewById(R.id.drawer_button);
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.beige));
        }


        replaceFragments(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item ->

    {
        int itemId = item.getItemId();

        if (itemId == R.id.homexd) {
            replaceFragments(new HomeFragment());
            FloatingActionButton fab = findViewById(R.id.add_task_button);
            fab.setVisibility(View.VISIBLE);
            TextView tv = findViewById(R.id.textView4);
            tv.setText("Tasks");
            homeblockSounds.start();
        } else if (itemId == R.id.fabxd) {
            // Handle the selection of the second menu item
        } else if (itemId == R.id.blockxd) {
            replaceFragments(new BlockFragment());
            FloatingActionButton fab = findViewById(R.id.add_task_button);
            fab.setVisibility(View.GONE);
            TextView tv = findViewById(R.id.textView4);
            tv.setText("Block");
            homeblockSounds.start();
        }

        return true;
    });

    createNotificationChannel();

    myDB =new

    DataBaseHelper(MainActivity .this);

}

    private void stopAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(123);
    }



    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (currentFragment instanceof HomeFragment) {
        } else {
            // If the current fragment is not HomeFragment, go back to the previous fragment
            super.onBackPressed();
        }
        AccessibilityUtils.resetAccessibilitySettingsChecked(this);
    }


    private void createNotificationChannel() {

        String description = "Alarm Channel for Tick Track";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "ticktrack",
                    "Tick Track Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription(description);

            serviceChannel.enableVibration(true);
            serviceChannel.setVibrationPattern(new long[]{0,1000,3000});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void replaceFragments(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public void addTask(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);

        MediaPlayer addSound = MediaPlayer.create(this, R.raw.button_sound);
        MediaPlayer errorSound = MediaPlayer.create(this, R.raw.bloop);

        EditText folderNameEditText = dialogView.findViewById(R.id.folder_nameText);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button discardButton = dialogView.findViewById(R.id.cancelBtn);
        ImageButton palette = dialogView.findViewById(R.id.color_palette);

        final AlertDialog dialog = builder.setView(dialogView).create();  // Declare dialog as final

        addSound.start();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = folderNameEditText.getText().toString();

                if (!folderName.isEmpty()) {
                    // Create a new ToDoModel object
                    ToDoModel newTask = new ToDoModel();
                    newTask.setTask(folderName);
                    newTask.setStatus(0);  // Assuming default status is 0
                    newTask.setColor(pickedColor);

                    // Insert the new task into the database
                    myDB.insertTask(newTask);



                    // Reload tasks from the database and update the ListView
                    HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    if (homeFragment != null) {
                        homeFragment.loadTasks();

                    }

                    folderNameEditText.setText("");  // Clear the EditText
                    dialog.dismiss();
                    pickedColor = ContextCompat.getColor(MainActivity.this, R.color.background);
                    addSound.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM,0,100);
                    toast.show();
                    errorSound.start();
                }
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorSound.start();
                dialog.dismiss();

            }
        });

        palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.content_color_picker_dialog);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                addSound.start();

                final RelativeLayout layout = findViewById(R.id.layout_color);

                Button colorRed = dialog.findViewById(R.id.color_red);
                Button colorBlue = dialog.findViewById(R.id.color_blue);
                Button colorYellow = dialog.findViewById(R.id.color_yellow);
                Button colorBrown = dialog.findViewById(R.id.color_brown);
                Button colorGreen = dialog.findViewById(R.id.color_green);
                Button colorOrange = dialog.findViewById(R.id.color_orange);

                colorRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.red);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                colorBlue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.blue);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                colorYellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.yellow);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                colorBrown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.brown);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                colorGreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.green);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                colorOrange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickedColor = ContextCompat.getColor(MainActivity.this, R.color.orange);
                        addSound.start();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        // Set the background drawable for the dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Prevent the dialog from being dismissed on outside touch or back press
        dialog.setCancelable(false);

        dialog.show();
    }

    public void showTimePicker(View v) {
        if (MainActivity.isPickerShown) {
            return;
        }
        MediaPlayer showSound = MediaPlayer.create(this, R.raw.button_sound1);
        MediaPlayer errorSound = MediaPlayer.create(this, R.raw.bloop);
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Alarm Time")
                .build();
        showSound.start();
        isPickerShown = true;

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @SuppressLint("ScheduleExactAlarm")
            @Override
            public void onClick(View v) {
                MainActivity.isTimeSelected = true;

                isPickerShown = false;

                showSound.start();

                Calendar now = Calendar.getInstance();

                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();

                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);


                if (selectedTime.before(now)) {
                    selectedTime.add(Calendar.DATE, 1);
                }

                // Set selectedTimeInMillis to the actual timestamp, not the duration
                MainActivity.selectedTimeInMillis = selectedTime.getTimeInMillis();

                // Calculate the remaining time in minutes
                long remainingTime = (MainActivity.selectedTimeInMillis - now.getTimeInMillis()) / 60000; // Convert milliseconds to minutes

                // Show a toast with the remaining time
                Toast toast = Toast.makeText(MainActivity.this, "Alarm is set for " + remainingTime + " minute(s)", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM,0,100);
                toast.show();
                Log.d("Alarm Time", "Alarm set for: " + new Date(MainActivity.selectedTimeInMillis));
            }
        });
        materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPickerShown = false;
                errorSound.start();
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
        materialTimePicker.setCancelable(false);
    }

    public void startTask(int taskId) {
        // Set the active task ID
        activeTaskId = taskId;
    }

    public void changeName(ToDoModel task) {
        // Create and show the dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_name);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Get the EditText and buttons from the dialog
        final EditText changeNameEditText = dialog.findViewById(R.id.change_name_editText);
        Button cancelButton = dialog.findViewById(R.id.cancelBtn);
        Button saveButton = dialog.findViewById(R.id.save_button);

        // Set click listeners for the buttons
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the new name from the EditText
                String newName = changeNameEditText.getText().toString();

                // Update the task's name
                task.setTask(newName);

                // Update the task in the database
                DataBaseHelper myDB = new DataBaseHelper(MainActivity.this);
                myDB.updateTask(task.getId(), newName);

                // Reload tasks from the database and update the ListView
                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                if (homeFragment != null) {
                    homeFragment.loadTasks();
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void changeColor(final ToDoModel task) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.content_color_picker_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        Button colorRed = dialog.findViewById(R.id.color_red);
        Button colorBlue = dialog.findViewById(R.id.color_blue);
        Button colorYellow = dialog.findViewById(R.id.color_yellow);
        Button colorBrown = dialog.findViewById(R.id.color_brown);
        Button colorGreen = dialog.findViewById(R.id.color_green);
        Button colorOrange = dialog.findViewById(R.id.color_orange);

        colorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.red);
                dialog.dismiss();
            }
        });

        colorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.blue);
                dialog.dismiss();
            }
        });

        colorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.yellow);
                dialog.dismiss();
            }
        });

        colorBrown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.brown);
                dialog.dismiss();
            }
        });

        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.green);
                dialog.dismiss();
            }
        });

        colorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskColor(task, R.color.orange);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void pinTask(List<ToDoModel> tasks, ToDoModel task, TaskAdapter adapter) {
        // Get the instance of DataBaseHelper
        DataBaseHelper dbHelper = new DataBaseHelper(this);

        MediaPlayer pinSound = MediaPlayer.create(this, R.raw.ping_sounds);
        pinSound.start();

        // Pin the task in the database
        dbHelper.pinTask(task.getId());

        // Remove the task from its current position in the list
        tasks.remove(task);

        // Set the task as pinned
        task.setPinned(true);

        // Add the task back at the top of the list
        tasks.add(0, task);

        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
    }

    public void unpinTask(List<ToDoModel> tasks, ToDoModel task, TaskAdapter adapter) {
        // Get the instance of DataBaseHelper
        DataBaseHelper dbHelper = new DataBaseHelper(this);

        MediaPlayer unpinSound = MediaPlayer.create(this, R.raw.homeblock_sound);
        unpinSound.start();


        // Unpin the task in the database
        dbHelper.unpinTask(task.getId());

        // Remove the task from its current position in the list
        tasks.remove(task);

        // Set the task as unpinned
        task.setPinned(false);

        // Add the task back at the end of the list
        tasks.add(task);

        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
    }



    private void updateTaskColor(ToDoModel task, int colorResource) {
        // Update the color of the task
        task.setColor(getResources().getColor(colorResource));

        myDB.updateTaskColor(task.getId(), task.getColor());

        // Reload tasks from the database and update the ListView
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (homeFragment != null) {
            homeFragment.loadTasks();
        }
    }

    public void deleteTask(int taskId) {
        if (!isSessionActive){
            // Delete the task from the database
            myDB.deleteTask(taskId);

            // Reload tasks from the database and update the ListView
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (homeFragment != null) {
                homeFragment.loadTasks();
            }
        } else {
            Toast toast = Toast.makeText(MainActivity.this, "A session is active. ", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,100);
            toast.show();
        }
    }

    public void showDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.finished_task_dialog, null);

        // Set the custom layout as the dialog content
        dialog.setContentView(dialogView);

        // Get the task name from the Intent
        Intent intent = getIntent();
        String taskName = intent.getStringExtra("taskName");

        // Set the task name in the TextView
        TextView taskNameTextView = dialogView.findViewById(R.id.taskName);
        taskNameTextView.setText("The task " + taskName + " is finished");

        // Show the dialog
        dialog.show();
    }
}