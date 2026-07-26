package com.prodject.gflow;

import android.content.Intent;

public class DvrService extends BaseForegroundService {
    static final String ACTION_START = "com.prodject.gflow.DVR_START";
    static final String ACTION_STOP = "com.prodject.gflow.DVR_STOP";

    @Override protected String title() { return "GFlow DVR"; }
    @Override protected int notificationId() { return 101; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        Intent proxy = new Intent(this, CameraRecordingService.class);
        if (ACTION_STOP.equals(action)) proxy.setAction(CameraRecordingService.ACTION_STOP_RECORDING);
        else proxy.setAction(CameraRecordingService.ACTION_START_RECORDING);
        startForegroundService(proxy);
        stopForeground(true);
        stopSelf();
        return START_NOT_STICKY;
    }
}
