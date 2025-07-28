package com.example.voipsim.utils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.voipsim.Activity.OngoingCallActivity;
import com.example.voipsim.data.AppDatabase;
import com.example.voipsim.data.CallLog;

public class CallService extends Service {

    private static final int ID_ONGOING = 1001;
    private final IBinder binder = new LocalBinder();

    private String caller;
    private long startTime;

    public class LocalBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }

    public static void start(Context context, String caller, long startTime) {
        Intent i = new Intent(context, CallService.class);
        i.putExtra("caller", caller);
        i.putExtra("start", startTime);
        context.startForegroundService(i);
        Intent ui = new Intent(context, OngoingCallActivity.class);
        ui.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(ui);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationHelper.createChannels(this);
        if (intent != null) {
            caller = intent.getStringExtra("caller");
            startTime = intent.getLongExtra("start", System.currentTimeMillis());
        }
        Notification n = NotificationHelper.buildOngoingNotification(this, caller, startTime);
        startForeground(ID_ONGOING, n);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public String getCaller() {
        return caller;
    }

    public long getStartTime() {
        return startTime;
    }

    public void endCall() {
        long end = System.currentTimeMillis();
        long duration = end - startTime;
        new Thread(() -> {
            AppDatabase db = AppDatabase.get(getApplicationContext());
            com.example.voipsim.data.CallLog log = new CallLog();
            log.caller = caller;
            log.startTime = startTime;
            log.endTime = end;
            log.duration = duration;
            log.type = "Answered";
            db.callDao().insert(log);
        }).start();
        stopForeground(true);
        stopSelf();
    }
}
