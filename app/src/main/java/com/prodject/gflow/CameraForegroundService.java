package com.prodject.gflow;

import android.content.Context;
import android.content.Intent;

public class CameraForegroundService extends BaseForegroundService {
    static final String ACTION_START = "com.prodject.gflow.CAMERA_FOREGROUND_START";
    static final String ACTION_STOP = "com.prodject.gflow.CAMERA_FOREGROUND_STOP";

    static void start(Context context) {
        context.startForegroundService(new Intent(context, CameraForegroundService.class).setAction(ACTION_START));
    }

    static void stop(Context context) {
        context.startService(new Intent(context, CameraForegroundService.class).setAction(ACTION_STOP));
    }

    @Override protected String title() { return "GFlow Camera Core"; }
    @Override protected int notificationId() { return 108; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }
}
