package com.example.voipsim.Activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voipsim.utils.CallBroadcastReceiver;
import com.example.voipsim.utils.CallService;
import com.example.voipsim.utils.NotificationHelper;
import com.example.voipsim.R;
import com.example.voipsim.data.AppDatabase;
import com.example.voipsim.data.CallLog;

public class IncomingCallActivity extends AppCompatActivity {

    private TextView tvNumber;
    private Button btnAnswer, btnReject;
    private String caller;
    private CountDownTimer autoMissTimer;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private static final int AUTO_MISS_MILLIS = 10_000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationHelper.createChannels(this);

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.activity_incoming_call);

        TextView tvCaller = findViewById(R.id.tvCaller);
        tvNumber = findViewById(R.id.tvNumber);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnReject = findViewById(R.id.btnReject);

        caller = getIntent().getStringExtra(CallBroadcastReceiver.EXTRA_CALLER);
        if (caller == null) caller = "Anonymous";
        tvNumber.setText(caller);

        startRinging();

        btnAnswer.setOnClickListener(v -> answerCall());
        btnReject.setOnClickListener(v -> rejectCall());

        autoMissTimer = new CountDownTimer(AUTO_MISS_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { }
            @Override
            public void onFinish() {
                markMissedAndFinish();
            }
        }.start();

        showFullScreenNotification();
    }

    private void showFullScreenNotification() {
        Intent fsIntent = new Intent(this, IncomingCallActivity.class);
        fsIntent.putExtra(CallBroadcastReceiver.EXTRA_CALLER, caller);
        fsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(this, 1, fsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = NotificationHelper.buildIncomingCallNotification(this, caller, pi);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(777, n);
    }

    private void startRinging() {
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        if (ringtone != null) ringtone.play();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopRinging() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        if (vibrator != null) vibrator.cancel();
    }

    private void answerCall() {
        stopRinging();
        if (autoMissTimer != null) autoMissTimer.cancel();

        long start = System.currentTimeMillis();

        CallService.start(this, caller, start);

        finish();
    }

    private void rejectCall() {
        stopRinging();
        if (autoMissTimer != null) autoMissTimer.cancel();

        long now = System.currentTimeMillis();
        saveCallLog(caller, now, now, "Missed", 0);
        NotificationHelper.showMissedCallNotification(this, caller, now);

        finish();
    }

    private void markMissedAndFinish() {
        stopRinging();
        long now = System.currentTimeMillis();
        saveCallLog(caller, now, now, "Missed", 0);
        NotificationHelper.showMissedCallNotification(this, caller, now);

        finish();
    }

    private void saveCallLog(String caller, long start, long end, String type, long duration) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.get(getApplicationContext());
            CallLog log = new CallLog();
            log.caller = caller;
            log.startTime = start;
            log.endTime = end;
            log.type = type;
            log.duration = duration;
            db.callDao().insert(log);
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRinging();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(777);
    }
}
