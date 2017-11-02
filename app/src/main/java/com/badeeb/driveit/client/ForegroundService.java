package com.badeeb.driveit.client;

/**
 * Created by meldeeb on 9/30/17.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.activity.MainActivity;
import com.badeeb.driveit.client.controllers.DriveItApplication;

public class ForegroundService extends Service {
    private static final String TAG = ForegroundService.class.getSimpleName();

    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static final int FOREGROUND_SERVICE_ID = 1000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (intent == null || intent.getBooleanExtra(STOP_FOREGROUND_SERVICE, true)) {
            Log.d(TAG, "stop the service..");
            clearForegroundService();
        } else {
            startForeground(FOREGROUND_SERVICE_ID, generateNotification("Finding nearby drivers"));
        }
        return START_NOT_STICKY;
    }

    private void clearForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on destroy");
        clearForegroundService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification generateNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String name = getString(R.string.app_name);
            String channelName = BuildConfig.APPLICATION_ID;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelName, name, importance);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder notificationBuilder = new Notification.Builder(DriveItApplication.getInstance());
            notificationBuilder.setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentIntent(intent)
                    .setContentText(message)
                    .setColor(getResources().getColor(R.color.colorAccent))
                    .setChannelId(channelName)
            ;
            return notificationBuilder.build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(DriveItApplication.getInstance());
            notificationBuilder.setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentIntent(intent)
                    .setContentText(message)
                    .setColor(getResources().getColor(R.color.colorAccent));

            return notificationBuilder.build();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "on task removed");
        clearForegroundService();
        super.onTaskRemoved(rootIntent);
    }
}