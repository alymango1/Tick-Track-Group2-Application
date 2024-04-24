package com.group2.practicenakakainis;

import static java.security.AccessController.getContext;

import android.content.IntentFilter;
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
import android.view.MenuItem;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.group2.practicenakakainis.Utils.DataBaseHelper;
import com.group2.practicenakakainis.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static long pauseStartTime;
    ActivityMainBinding binding;

    public static final String SHARED_PREFERENCES_NAME = "APP_PREFERENCES";
    private static final String BLOCKED_APPS_KEY = "BLOCKED_APPS";

    public static long selectedTimeInMillis;

    public static boolean shouldBlockApps = false;
    public static boolean isTimeSelected = false;

    public static boolean isSessionActive = false;

    public static boolean areAppsSelected = false;

    private Map<String, Boolean> switchStates = new HashMap<>();

    public static boolean isPickerShown = false;

    private int activeTaskId = -1;

    private int pickedColor;

    private static final String ACCESSIBILITY_SETTINGS_CHECKED = "ACCESSIBILITY_SETTINGS_CHECKED";
    private SharedPreferences sharedPreferences;

    private DataBaseHelper myDB;
    private AlarmReceiver alarmReceiver;

    private MediaPlayer homeblockSounds;
    private MediaPlayer addSound;
    private MediaPlayer errorSound;
    private MediaPlayer showSound;
    private MediaPlayer pinSound;
    private MediaPlayer unpinSound;

    private MediaPlayer drawerSound;

    private NavigationView navigationView;


    private final ActivityResultLauncher<Intent> accessibilitySettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (!AccessibilityUtils.isAccessibilityServiceRunning(MainActivity.this, MyAccessibilityService.class)) {
                        Toast toast = Toast.makeText(MainActivity.this, "Accessibility Service is not enabled", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        toast.show();
                    }
                }
            }
    );

    public static void playSound(Context context) {
        MediaPlayer pogi = MediaPlayer.create(context, R.raw.pogi);
        pogi.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        homeblockSounds = MediaPlayer.create(this, R.raw.homeblock_sound);
        addSound = MediaPlayer.create(this, R.raw.button_sound);
        errorSound = MediaPlayer.create(this, R.raw.bloop);
        showSound = MediaPlayer.create(this, R.raw.button_sound1);
        pinSound = MediaPlayer.create(this, R.raw.ping_sounds);
        unpinSound = MediaPlayer.create(this, R.raw.homeblock_sound);
        drawerSound = MediaPlayer.create(this, R.raw.pop2);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (getIntent().getAction() != null && getIntent().getAction().equals("ACTION_STOP")) {
            stopAlarm();
        }


        alarmReceiver = new AlarmReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(alarmReceiver, new IntentFilter("UPDATE_UI"));

        boolean showDialog = getIntent().getBooleanExtra("showDialog", false);
        if (showDialog) {
            showDialog();
        }

        ImageButton drawerButton = findViewById(R.id.drawer_button);
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerSound.start();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.about_us) {
                    Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
                    startActivity(intent);
                    addSound.start();
                    return true;
                } else if (id == R.id.settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    addSound.start();
                    return true;
                }

                return false;
            }
        });



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.beige));
        }

        replaceFragments(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
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

        myDB = new DataBaseHelper(MainActivity.this);
    }

    private void stopAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(124);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (currentFragment instanceof HomeFragment) {
        } else if (currentFragment instanceof BlockFragment){
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
            serviceChannel.setVibrationPattern(new long[]{0, 1000, 3000});

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

        EditText folderNameEditText = dialogView.findViewById(R.id.folder_nameText);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button discardButton = dialogView.findViewById(R.id.cancelBtn);
        ImageButton palette = dialogView.findViewById(R.id.color_palette);

        final AlertDialog dialog = builder.setView(dialogView).create();

        addSound.start();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = folderNameEditText.getText().toString();

                if (!folderName.isEmpty()) {
                    ToDoModel newTask = new ToDoModel();
                    newTask.setTask(folderName);
                    newTask.setStatus(0);
                    newTask.setColor(pickedColor);

                    myDB.insertTask(newTask);

                    HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    if (homeFragment != null) {
                        homeFragment.loadTasks();
                    }

                    folderNameEditText.setText("");
                    dialog.dismiss();
                    pickedColor = ContextCompat.getColor(MainActivity.this, R.color.background);
                    addSound.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 100);
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

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void showTimePicker(View v) {
        if (MainActivity.isPickerShown) {
            return;
        }

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

                MainActivity.selectedTimeInMillis = selectedTime.getTimeInMillis();

                long remainingTime = (MainActivity.selectedTimeInMillis - now.getTimeInMillis()) / 60000;
                Toast toast = Toast.makeText(MainActivity.this, "Alarm is set for " + remainingTime + " minute(s)", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 100);
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
        activeTaskId = taskId;
    }

    private boolean areAnyAppsSelected() {
        for (boolean isSelected : switchStates.values()) {
            if (isSelected) {
                return true;
            }
        }
        return false;
    }

    public void changeName(ToDoModel task) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_name);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        final EditText changeNameEditText = dialog.findViewById(R.id.change_name_editText);
        Button cancelButton = dialog.findViewById(R.id.cancelBtn);
        Button saveButton = dialog.findViewById(R.id.save_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = changeNameEditText.getText().toString();

                task.setTask(newName);

                DataBaseHelper myDB = new DataBaseHelper(MainActivity.this);
                myDB.updateTask(task.getId(), newName);

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
        DataBaseHelper dbHelper = new DataBaseHelper(this);

        pinSound.start();

        dbHelper.pinTask(task.getId());

        tasks.remove(task);

        task.setPinned(true);

        tasks.add(0, task);

        adapter.notifyDataSetChanged();
    }

    public void unpinTask(List<ToDoModel> tasks, ToDoModel task, TaskAdapter adapter) {
        DataBaseHelper dbHelper = new DataBaseHelper(this);

        unpinSound.start();

        dbHelper.unpinTask(task.getId());

        tasks.remove(task);

        task.setPinned(false);

        tasks.add(task);

        adapter.notifyDataSetChanged();
    }


    private void updateTaskColor(ToDoModel task, int colorResource) {
        task.setColor(getResources().getColor(colorResource));

        myDB.updateTaskColor(task.getId(), task.getColor());

        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (homeFragment != null) {
            homeFragment.loadTasks();
        }
    }

    public void deleteTask(int taskId) {
        if (!isSessionActive) {
            myDB.deleteTask(taskId);

            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (homeFragment != null) {
                homeFragment.loadTasks();
            }
        } else {
            Toast toast = Toast.makeText(MainActivity.this, "A session is active. ", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }

    public void showDialog() {
        Dialog dialog = new Dialog(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.finished_task_dialog, null);

        dialog.setContentView(dialogView);

        Intent intent = getIntent();
        String taskName = intent.getStringExtra("taskName");

        TextView taskNameTextView = dialogView.findViewById(R.id.taskName);
        taskNameTextView.setText("The task " + taskName + " is finished");

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (homeblockSounds != null) {
            homeblockSounds.release();
        }
        if (addSound != null) {
            addSound.release();
        }
        if (errorSound != null) {
            errorSound.release();
        }
        if (showSound != null) {
            showSound.release();
        }
        if (pinSound != null) {
            pinSound.release();
        }
        if (unpinSound != null) {
            unpinSound.release();
        }
    }
}