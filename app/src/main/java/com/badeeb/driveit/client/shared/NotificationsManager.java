package com.badeeb.driveit.client.shared;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.badeeb.driveit.client.BuildConfig;

import com.badeeb.driveit.client.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by meldeeb on 10/2/17.
 */

public class NotificationsManager {

    // Logging Purpose
    public static final String TAG = NotificationsManager.class.getSimpleName();

    private static NotificationsManager mNotificationManager;
    private final static AtomicInteger notificationId = new AtomicInteger(0);

    private static String channelName;

    public static NotificationsManager getInstance() {
        if (mNotificationManager == null) {
            mNotificationManager = new NotificationsManager();
            channelName = BuildConfig.APPLICATION_ID;
        }

        return mNotificationManager;
    }


    public void createNotification(Context context, String notificationTitle, String notificationMsg,
                                   Intent intent, Resources resources) {

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // defining the destination intent that will be passed to the notification manager
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0 /* Request code for the sender */,
                intent /*this is the target intent*/,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel creation
            String name = resources.getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelName, name, importance);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.notification_icon)
//                .setLargeIcon(icon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMsg)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setStyle(new Notification.BigTextStyle().bigText(notificationMsg))    // Used to display full/Multiline text of any notification
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setColor(resources.getColor(R.color.colorAccent))
                    .setVibrate(new long[]{500, 500})
                    .setContentIntent(pendingIntent) // passing the destination intent
                    .setChannelId(channelName)
                    ;
            notificationManager.notify(notificationId.incrementAndGet() /* incremented id*/, notificationBuilder.build());
        }
        else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.notification_icon)
//                .setLargeIcon(icon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMsg)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMsg))    // Used to display full/Multiline text of any notification
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setColor(resources.getColor(R.color.colorAccent))
                    .setVibrate(new long[]{500, 500})
                    .setContentIntent(pendingIntent) // passing the destination intent
                    ;

            notificationManager.notify(notificationId.incrementAndGet() /* incremented id*/, notificationBuilder.build());
        }
    }
}
