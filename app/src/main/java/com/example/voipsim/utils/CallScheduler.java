package com.example.voipsim.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class CallScheduler {

    public static void scheduleIncomingCall(Context context, long delayMillis, String caller) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CallBroadcastReceiver.class);
        intent.setAction(CallBroadcastReceiver.ACTION_SIMULATE_INCOMING);
        intent.putExtra(CallBroadcastReceiver.EXTRA_CALLER, caller);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) (SystemClock.elapsedRealtime() % Integer.MAX_VALUE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = System.currentTimeMillis() + delayMillis;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}
