package com.prodject.gcontrol;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.os.*;
import android.view.Surface;
import java.io.*;
import java.util.*;

public class DvrService extends BaseForegroundService {
    static final String ACTION_START = "com.prodject.gcontrol.DVR_START";
    static final String ACTION_STOP = "com.prodject.gcontrol.DVR_STOP";
    private Handler handler;
    private HandlerThread recorderThread;
    private Handler recorderHandler;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private MediaRecorder mediaRecorder;
    private boolean cameraRecording;
    private boolean running;
    private final Runnable segmentTick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            if (!startCameraSegment()) writeMarkerSegments();
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
            stopCameraSegment();
            stopForeground(true);
            stopSelf();
        } else {
            running = true;
            handler.removeCallbacks(segmentTick);
            handler.post(segmentTick);
        }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        stopCameraSegment();
        if (recorderThread != null) recorderThread.quitSafely();
        super.onDestroy();
    }

    private boolean startCameraSegment() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return false;
        String cameraId = firstCamera2Id();
        if (cameraId == null) return false;
        stopCameraSegment();
        File out = DvrArchive.newSegment(this, cameraId);
        try {
            ensureRecorderThread();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(out.getAbsolutePath());
            mediaRecorder.setVideoEncodingBitRate(4_000_000);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(1280, 720);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            CameraManager manager = getSystemService(CameraManager.class);
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    createRecordingSession();
                }
                @Override public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }
                @Override public void onError(CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    stopCameraSegment();
                }
            }, recorderHandler);
            cameraRecording = true;
            return true;
        } catch (Exception e) {
            android.util.Log.e("GControlDvr", "camera segment failed", e);
            stopCameraSegment();
            return false;
        }
    }

    private void createRecordingSession() {
        try {
            Surface surface = mediaRecorder.getSurface();
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        builder.addTarget(surface);
                        session.setRepeatingRequest(builder.build(), null, recorderHandler);
                        mediaRecorder.start();
                    } catch (Exception e) {
                        android.util.Log.e("GControlDvr", "record start failed", e);
                        stopCameraSegment();
                    }
                }
                @Override public void onConfigureFailed(CameraCaptureSession session) {
                    stopCameraSegment();
                }
            }, recorderHandler);
        } catch (Exception e) {
            android.util.Log.e("GControlDvr", "session failed", e);
            stopCameraSegment();
        }
    }

    private void stopCameraSegment() {
        try {
            if (captureSession != null) captureSession.close();
        } catch (Exception ignored) {
        }
        captureSession = null;
        try {
            if (cameraDevice != null) cameraDevice.close();
        } catch (Exception ignored) {
        }
        cameraDevice = null;
        try {
            if (mediaRecorder != null && cameraRecording) mediaRecorder.stop();
        } catch (Exception ignored) {
        }
        try {
            if (mediaRecorder != null) mediaRecorder.release();
        } catch (Exception ignored) {
        }
        mediaRecorder = null;
        cameraRecording = false;
    }

    private void ensureRecorderThread() {
        if (recorderThread != null) return;
        recorderThread = new HandlerThread("GControlDvrRecorder");
        recorderThread.start();
        recorderHandler = new Handler(recorderThread.getLooper());
    }

    private String firstCamera2Id() {
        try {
            Set<String> selected = new LinkedHashSet<>(Arrays.asList(DvrArchive.selectedCameras(this)));
            CameraManager manager = getSystemService(CameraManager.class);
            String[] ids = manager.getCameraIdList();
            for (String id : ids) if (selected.contains(id)) return id;
            return ids.length > 0 ? ids[0] : null;
        } catch (Exception e) {
            return null;
        }
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
