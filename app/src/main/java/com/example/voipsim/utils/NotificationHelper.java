package com.example.voipsim.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.voipsim.Activity.MainActivity;

public class NotificationHelper {

    public static final String CHANNEL_INCOMING = "incoming_call_channel";
    public static final String CHANNEL_ONGOING = "ongoing_call_channel";
    public static final String CHANNEL_MISSED = "missed_call_channel";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();

            NotificationChannel incoming = new NotificationChannel(CHANNEL_INCOMING, "Incoming calls", NotificationManager.IMPORTANCE_HIGH);
            incoming.setDescription("Shows incoming simulated calls");
            incoming.setSound(ringtone, attrs);

            NotificationChannel ongoing = new NotificationChannel(CHANNEL_ONGOING, "Ongoing calls", NotificationManager.IMPORTANCE_LOW);
            ongoing.setDescription("Keeps the simulated call alive");

            NotificationChannel missed = new NotificationChannel(CHANNEL_MISSED, "Missed calls", NotificationManager.IMPORTANCE_DEFAULT);

            nm.createNotificationChannel(incoming);
            nm.createNotificationChannel(ongoing);
            nm.createNotificationChannel(missed);
        }
    }

    public static Notification buildIncomingCallNotification(Context context, String caller, PendingIntent fullScreenIntent) {
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_INCOMING)
                .setSmallIcon(android.R.drawable.sym_call_incoming)
                .setContentTitle("Incoming call")
                .setContentText(caller)
                .setCategory(Notification.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(ringtone)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreenIntent, true);

        return b.build();
    }

    public static Notification buildOngoingNotification(Context context, String caller, long startTime) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ONGOING)
                .setSmallIcon(android.R.drawable.sym_call_outgoing)
                .setContentTitle("On call with " + caller)
                .setWhen(startTime)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return b.build();
    }

    public static void showMissedCallNotification(Context context, String caller, long when) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_MISSED)
                .setSmallIcon(android.R.drawable.sym_call_missed)
                .setContentTitle("Missed call")
                .setContentText(caller)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setWhen(when)
                .setCategory(Notification.CATEGORY_MISSED_CALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        nm.notify((int) (when % Integer.MAX_VALUE), b.build());
    }
}
