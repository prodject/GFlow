package com.prodject.gflow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class CameraRecordingService extends BaseForegroundService {
    static final String ACTION_START_RECORDING = "com.prodject.gflow.CAMERA_RECORDING_START";
    static final String ACTION_STOP_RECORDING = "com.prodject.gflow.CAMERA_RECORDING_STOP";
    static final String ACTION_OPEN_EVS = "com.prodject.gflow.CAMERA_RECORDING_OPEN_EVS";
    static final String ACTION_CLOSE_EVS = "com.prodject.gflow.CAMERA_RECORDING_CLOSE_EVS";
    static final String EXTRA_CAMERA_ID = "camera_id";

    private Handler handler;
    private HandlerThread recorderThread;
    private Handler recorderHandler;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private MediaRecorder mediaRecorder;
    private java.lang.Process screenRecordProcess;
    private boolean cameraRecording;
    private boolean running;
    private String activeSource;
    private int openedEvsCameraId = -1;
    private RecordingStateController stateController;
    private final Runnable segmentTick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            if (!startNextSegment()) {
                stateController.update(RecordingStateController.State.ERROR, activeSource);
                writeMarkerSegments();
            }
            DvrArchive.prune(CameraRecordingService.this, DvrArchive.limitBytes(CameraRecordingService.this));
            handler.postDelayed(this, DvrArchive.segmentMillis(CameraRecordingService.this));
        }
    };

    @Override protected String title() { return "GFlow Camera Recording"; }
    @Override protected int notificationId() { return 109; }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        stateController = new RecordingStateController(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent == null ? ACTION_START_RECORDING : intent.getAction();
        if (ACTION_STOP_RECORDING.equals(action)) {
            running = false;
            stateController.update(RecordingStateController.State.STOPPING, activeSource);
            handler.removeCallbacks(segmentTick);
            stopActiveSegment();
            stateController.update(RecordingStateController.State.IDLE, "");
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_OPEN_EVS.equals(action)) {
            CameraForegroundService.start(this);
            int cameraId = intent == null ? EcarxDvrAdapter.EVS_CAMERA_AVM : intent.getIntExtra(EXTRA_CAMERA_ID, EcarxDvrAdapter.EVS_CAMERA_AVM);
            EcarxDvrAdapter.Result result = new EcarxDvrAdapter(this).openEvs(cameraId);
            if (result.success) openedEvsCameraId = cameraId;
            stateController.update(result.success ? RecordingStateController.State.IDLE : RecordingStateController.State.ERROR, "evs:" + cameraId);
            return START_STICKY;
        }
        if (ACTION_CLOSE_EVS.equals(action)) {
            int cameraId = intent == null ? openedEvsCameraId : intent.getIntExtra(EXTRA_CAMERA_ID, openedEvsCameraId);
            if (cameraId != -1) new EcarxDvrAdapter(this).closeEvs(cameraId);
            if (cameraId == openedEvsCameraId) openedEvsCameraId = -1;
            stateController.update(RecordingStateController.State.IDLE, "");
            return START_STICKY;
        }
        CameraForegroundService.start(this);
        running = true;
        stateController.update(RecordingStateController.State.INITIALIZING, activeSource);
        handler.removeCallbacks(segmentTick);
        handler.post(segmentTick);
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        stopActiveSegment();
        if (recorderThread != null) recorderThread.quitSafely();
        stateController.update(RecordingStateController.State.IDLE, "");
        super.onDestroy();
    }

    private boolean startNextSegment() {
        String[] sources = DvrArchive.selectedCameras(this);
        if (sources.length == 0) return false;
        String preferred = nextSource(sources);
        if (trySource(preferred)) return true;
        for (String source : sources) if (!source.equals(preferred) && trySource(source)) return true;
        return false;
    }

    private boolean trySource(String source) {
        stateController.update(RecordingStateController.State.INITIALIZING, source);
        boolean ok = isEvsSource(source) ? startEvsScreenRecordSegment(source) : startCameraSegment(source);
        if (ok) stateController.update(RecordingStateController.State.RECORDING, source);
        return ok;
    }

    private String nextSource(String[] sources) {
        if (activeSource == null) return sources[0];
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].equals(activeSource)) return sources[(i + 1) % sources.length];
        }
        return sources[0];
    }

    private boolean startCameraSegment(String source) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return false;
        String cameraId = camera2IdFor(source);
        if (cameraId == null) return false;
        stopActiveSegment();
        activeSource = source;
        File out = DvrArchive.newSegment(this, "camera2_" + cameraId);
        DvrArchive.Quality quality = DvrArchive.quality(this);
        try {
            ensureRecorderThread();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(out.getAbsolutePath());
            mediaRecorder.setVideoEncodingBitRate(quality.bitrate);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(quality.width, quality.height);
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
                    stopActiveSegment();
                    stateController.update(RecordingStateController.State.ERROR, activeSource);
                }
            }, recorderHandler);
            cameraRecording = true;
            return true;
        } catch (Exception e) {
            android.util.Log.e("GFlowCameraRecording", "camera segment failed " + source, e);
            stopActiveSegment();
            return false;
        }
    }

    private boolean startEvsScreenRecordSegment(String source) {
        stopActiveSegment();
        activeSource = source;
        int evsCameraId = evsCameraId(source);
        File out = DvrArchive.newSegment(this, source);
        DvrArchive.Quality quality = DvrArchive.quality(this);
        try {
            EcarxDvrAdapter.Result open = new EcarxDvrAdapter(this).openEvs(evsCameraId);
            if (!open.success) android.util.Log.w("GFlowCameraRecording", open.message);
            openedEvsCameraId = evsCameraId;
            ArrayList<String> cmd = new ArrayList<>();
            cmd.add("screenrecord");
            cmd.add("--size");
            cmd.add(quality.width + "x" + quality.height);
            cmd.add("--bit-rate");
            cmd.add(String.valueOf(quality.bitrate));
            cmd.add("--time-limit");
            cmd.add(String.valueOf(Math.max(10, Math.min(180, (DvrArchive.segmentMillis(this) / 1000) - 1))));
            cmd.add(out.getAbsolutePath());
            screenRecordProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            return true;
        } catch (Exception e) {
            android.util.Log.e("GFlowCameraRecording", "EVS screenrecord failed " + source, e);
            stopActiveSegment();
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
                        android.hardware.camera2.CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        builder.addTarget(surface);
                        session.setRepeatingRequest(builder.build(), null, recorderHandler);
                        mediaRecorder.start();
                    } catch (Exception e) {
                        android.util.Log.e("GFlowCameraRecording", "record start failed", e);
                        stopActiveSegment();
                        stateController.update(RecordingStateController.State.ERROR, activeSource);
                    }
                }

                @Override public void onConfigureFailed(CameraCaptureSession session) {
                    stopActiveSegment();
                    stateController.update(RecordingStateController.State.ERROR, activeSource);
                }
            }, recorderHandler);
        } catch (Exception e) {
            android.util.Log.e("GFlowCameraRecording", "session failed", e);
            stopActiveSegment();
            stateController.update(RecordingStateController.State.ERROR, activeSource);
        }
    }

    private void stopActiveSegment() {
        stopCameraSegment();
        stopScreenRecordSegment();
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

    private void stopScreenRecordSegment() {
        try {
            if (screenRecordProcess != null) {
                screenRecordProcess.destroy();
                screenRecordProcess.waitFor();
            }
        } catch (Exception ignored) {
        }
        screenRecordProcess = null;
        try {
            if (openedEvsCameraId != -1) new EcarxDvrAdapter(this).closeEvs(openedEvsCameraId);
        } catch (Exception ignored) {
        }
        openedEvsCameraId = -1;
    }

    private void ensureRecorderThread() {
        if (recorderThread != null) return;
        recorderThread = new HandlerThread("GFlowCameraRecording");
        recorderThread.start();
        recorderHandler = new Handler(recorderThread.getLooper());
    }

    private String camera2IdFor(String source) {
        try {
            CameraManager manager = getSystemService(CameraManager.class);
            String[] ids = manager.getCameraIdList();
            String normalized = source == null ? "" : source.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("camera2:")) normalized = normalized.substring("camera2:".length());
            for (String id : ids) if (id.equals(normalized)) return id;
            Integer wantedFacing = wantedFacing(normalized);
            if (wantedFacing != null) {
                for (String id : ids) {
                    CameraCharacteristics cc = manager.getCameraCharacteristics(id);
                    Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                    if (wantedFacing.equals(facing)) return id;
                }
            }
            return ids.length > 0 && ("front".equals(normalized) || "rear".equals(normalized)) ? ids[0] : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer wantedFacing(String source) {
        if ("front".equals(source) || "adas".equals(source)) return CameraCharacteristics.LENS_FACING_FRONT;
        if ("rear".equals(source) || "back".equals(source)) return CameraCharacteristics.LENS_FACING_BACK;
        if ("external".equals(source) || "usb".equals(source) || "left".equals(source) || "right".equals(source)) {
            return Build.VERSION.SDK_INT >= 23 ? CameraCharacteristics.LENS_FACING_EXTERNAL : null;
        }
        return null;
    }

    private boolean isEvsSource(String source) {
        return source != null && source.toLowerCase(Locale.ROOT).startsWith("evs:");
    }

    private int evsCameraId(String source) {
        String normalized = source == null ? "" : source.toLowerCase(Locale.ROOT);
        if (normalized.contains("rear")) return EcarxDvrAdapter.EVS_CAMERA_REAR;
        if (normalized.contains("dvr")) return EcarxDvrAdapter.EVS_CAMERA_DVR;
        return EcarxDvrAdapter.EVS_CAMERA_AVM;
    }

    private void writeMarkerSegments() {
        long now = System.currentTimeMillis();
        for (String cameraId : DvrArchive.selectedCameras(this)) {
            File f = DvrArchive.newSegment(this, cameraId);
            try (FileOutputStream out = new FileOutputStream(f)) {
                out.write(("GFlow DVR segment placeholder cam" + cameraId + " " + now).getBytes("UTF-8"));
            } catch (Exception e) {
                android.util.Log.e("GFlowCameraRecording", "segment error cam" + cameraId, e);
            }
        }
    }
}
