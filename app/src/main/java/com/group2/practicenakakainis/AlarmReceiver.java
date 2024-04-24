    package com.group2.practicenakakainis;

    import android.annotation.SuppressLint;
    import android.app.AlarmManager;
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.icu.util.Calendar;
    import android.media.MediaPlayer;
    import android.util.Log;
    import android.view.Gravity;
    import android.view.View;
    import android.widget.Toast;

    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.core.app.NotificationManagerCompat;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;

    import java.util.Date;


    public class AlarmReceiver extends BroadcastReceiver {

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {

            MainActivity.shouldBlockApps = false;

            MainActivity.playSound(context);

            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("showDialog", true);
            String taskName = intent.getStringExtra("taskName");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

            Intent stopIntent = new Intent(context, MainActivity.class);
            stopIntent.setAction("ACTION_STOP");
            PendingIntent stopPendingIntent = PendingIntent.getActivity(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ticktrack")
                    .setSmallIcon(R.drawable.baseline_track_changes_24)
                    .setContentTitle("Tick Track")
                    .setContentText("The timer for the task ran out of time!")
                    .setAutoCancel(false)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.baseline_stop_24, "Stop", stopPendingIntent);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(124, builder.build());

            Toast toast = Toast.makeText(context, "Timer has finished", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            MainActivity.isSessionActive = false;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendInt = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendInt);

            TaskAdapter.handler.removeCallbacks(TaskAdapter.runnable);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(123);
        }
    }
