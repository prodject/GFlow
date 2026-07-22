package com.prodject.gflow;

import android.content.*;
import android.os.*;

public class LowSpeedCameraService extends BaseForegroundService {
    static final String ACTION_START = "com.prodject.gflow.LOW_SPEED_CAMERA_START";
    static final String ACTION_STOP = "com.prodject.gflow.LOW_SPEED_CAMERA_STOP";
    static final String KEY_ENABLED = "low_speed_camera_enabled";
    static final String KEY_THRESHOLD = "low_speed_camera_threshold";
    static final String KEY_RESET_THRESHOLD = "low_speed_camera_reset_threshold";
    static final String KEY_LAST_RESULT = "low_speed_camera_last_result";
    private static final long TICK_MS = 3000L;
    private static final long MIN_OPEN_INTERVAL_MS = 30000L;

    private Handler handler;
    private boolean running;
    private boolean armed = true;
    private long lastOpenedAt;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            checkSpeed();
            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override protected String title() { return "GFlow low speed cameras"; }
    @Override protected int notificationId() { return 106; }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            running = false;
            handler.removeCallbacks(tick);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        running = true;
        handler.removeCallbacks(tick);
        handler.post(tick);
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        if (handler != null) handler.removeCallbacks(tick);
        super.onDestroy();
    }

    private void checkSpeed() {
        SharedPreferences p = AutomationEngine.prefs(this);
        float threshold = p.getFloat(KEY_THRESHOLD, 30.0f);
        float reset = Math.max(threshold + 1.0f, p.getFloat(KEY_RESET_THRESHOLD, 35.0f));
        VehicleSignalStateAdapter signals = new VehicleSignalStateAdapter(this);
        float speed = signals.speedKmh(Float.NaN);
        if (Float.isNaN(speed)) {
            save("speed unavailable\n" + signals.status());
            return;
        }
        if (speed >= reset) {
            armed = true;
            save("armed, speed=" + speed + " km/h, reset=" + reset + "\n" + signals.status());
            return;
        }
        if (!armed || speed >= threshold) {
            save("watching, speed=" + speed + " km/h, threshold=" + threshold + ", armed=" + armed + "\n" + signals.status());
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastOpenedAt < MIN_OPEN_INTERVAL_MS) {
            save("cooldown, speed=" + speed + " km/h\n" + signals.status());
            return;
        }
        lastOpenedAt = now;
        armed = false;
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this)
                .set(EcarxVehicleAdapter.BCM_CUSTOM_KEY, 0, EcarxVehicleAdapter.CUSTOM_KEY_360);
        save("open 360 by speed=" + speed + " km/h -> " + result.message + "\n" + signals.status());
    }

    private void save(String value) {
        AutomationEngine.prefs(this).edit().putString(KEY_LAST_RESULT, value).apply();
    }
}
