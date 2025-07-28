package com.example.voipsim.Activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voipsim.utils.CallScheduler;
import com.example.voipsim.utils.NotificationHelper;
import com.example.voipsim.R;
import com.example.voipsim.data.AppDatabase;
import com.example.voipsim.data.CallLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFS = 1001;
    private RecyclerView rvLogs;
    private LogsAdapter adapter;
    private List<CallLog> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createChannels(this);

        rvLogs = findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogsAdapter(logs);
        rvLogs.setAdapter(adapter);

        Button btnSchedule = findViewById(R.id.btnSchedule);
        btnSchedule.setOnClickListener(v -> scheduleDialog());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFS);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void scheduleDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Delay in seconds");

        EditText name = new EditText(this);
        name.setHint("Caller name/number");

        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.addView(name);
        ll.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Schedule simulated incoming call")
                .setView(ll)
                .setPositiveButton("Schedule", (dialog, which) -> {
                    int seconds = 5;
                    try {
                        seconds = Integer.parseInt(input.getText().toString());
                    } catch (Exception ignored) { }
                    String caller = name.getText().toString().trim();
                    if (caller.isEmpty()) caller = "Test User";
                    CallScheduler.scheduleIncomingCall(this, seconds * 1000L, caller);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLogs();
    }

    private void refreshLogs() {
        new Thread(() -> {
            List<CallLog> newLogs = AppDatabase.get(getApplicationContext()).callDao().getAll();
            runOnUiThread(() -> {
                logs.clear();
                logs.addAll(newLogs);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFS) {
        }
    }

    static class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.VH> {

        private final List<CallLog> data;
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        LogsAdapter(List<CallLog> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            CallLog log = data.get(position);
            h.caller.setText("Caller: " + log.caller);
            h.type.setText("Type: " + log.type);
            h.time.setText("Start: " + sdf.format(new Date(log.startTime)));
            long sec = log.duration / 1000;
            h.duration.setText("Duration: " + sec + "s");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView caller, type, time, duration;
            VH(@NonNull android.view.View itemView) {
                super(itemView);
                caller = itemView.findViewById(R.id.tvItemCaller);
                type = itemView.findViewById(R.id.tvItemType);
                time = itemView.findViewById(R.id.tvItemTime);
                duration = itemView.findViewById(R.id.tvItemDuration);
            }
        }
    }
}
