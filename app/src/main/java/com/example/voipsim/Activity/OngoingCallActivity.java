package com.example.voipsim.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voipsim.utils.CallService;
import com.example.voipsim.R;

public class OngoingCallActivity extends AppCompatActivity {

    private TextView tvOngoingCaller, tvTimer;
    private Button btnEnd;
    private Handler handler = new Handler();
    private CallService service;
    private boolean bound = false;

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (service != null) {
                long duration = System.currentTimeMillis() - service.getStartTime();
                tvTimer.setText(format(duration));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            CallService.LocalBinder b = (CallService.LocalBinder) binder;
            service = b.getService();
            bound = true;
            tvOngoingCaller.setText(service.getCaller());
            handler.post(updateTimer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            handler.removeCallbacks(updateTimer);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_call);

        tvOngoingCaller = findViewById(R.id.tvOngoingCaller);
        tvTimer = findViewById(R.id.tvTimer);
        btnEnd = findViewById(R.id.btnEnd);

        btnEnd.setOnClickListener(v -> {
            if (service != null) {
                service.endCall();
            }
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(this, CallService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        handler.removeCallbacks(updateTimer);
    }

    private String format(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
