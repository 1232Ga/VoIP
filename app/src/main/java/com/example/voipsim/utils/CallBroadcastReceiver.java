package com.example.voipsim.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.voipsim.Activity.IncomingCallActivity;

public class CallBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_SIMULATE_INCOMING = "com.example.voipsim.SIMULATE_INCOMING";
    public static final String EXTRA_CALLER = "caller";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_SIMULATE_INCOMING.equals(intent.getAction())) {
            String caller = intent.getStringExtra(EXTRA_CALLER);
            if (caller == null) caller = "Anonymous";
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "voipsim:incoming");
            wl.acquire(5000);

            Intent i = new Intent(context, IncomingCallActivity.class);
            i.putExtra(EXTRA_CALLER, caller);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }
}
