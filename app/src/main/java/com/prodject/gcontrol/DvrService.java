package com.prodject.gcontrol;

import android.content.*;
import android.os.*;
import java.io.*;

public class DvrService extends BaseForegroundService {
    static final String ACTION_START = "com.prodject.gcontrol.DVR_START";
    static final String ACTION_STOP = "com.prodject.gcontrol.DVR_STOP";
    private Handler handler;
    private boolean running;
    private final Runnable segmentTick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            writeMarkerSegments();
            DvrArchive.prune(DvrService.this, DvrArchive.limitBytes(DvrService.this));
            handler.postDelayed(this, DvrArchive.segmentMillis(DvrService.this));
        }
    };

    @Override protected String title() { return "GControl DVR"; }
    @Override protected int notificationId() { return 101; }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            running = false;
            stopForeground(true);
            stopSelf();
        } else {
            running = true;
            handler.removeCallbacks(segmentTick);
            handler.post(segmentTick);
        }
        return START_STICKY;
    }

    private void writeMarkerSegments() {
        long now = System.currentTimeMillis();
        for (String cameraId : DvrArchive.selectedCameras(this)) {
            File f = DvrArchive.newSegment(this, cameraId);
            try (FileOutputStream out = new FileOutputStream(f)) {
                out.write(("GControl DVR segment placeholder cam" + cameraId + " " + now).getBytes("UTF-8"));
            } catch (Exception e) {
                android.util.Log.e("GControlDvr", "segment error cam" + cameraId, e);
            }
        }
    }
}
