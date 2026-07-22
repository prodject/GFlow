package com.prodject.gflow;

import android.content.*;
import android.media.*;
import org.vosk.*;
import java.io.*;

final class VoskVoiceRecognizer {
    interface Callback { void onText(String text); }
    private volatile boolean running;
    private Thread thread;

    void start(Context context, Callback callback) {
        if (running) return;
        running = true;
        thread = new Thread(() -> run(context.getApplicationContext(), callback), "gflow-vosk");
        thread.start();
    }

    void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    private void run(Context context, Callback callback) {
        int rate = 16000;
        int min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        byte[] buffer = new byte[Math.max(min, 4096)];
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length);
        try (Model model = new Model(copyModel(context).getAbsolutePath()); Recognizer recognizer = new Recognizer(model, rate)) {
            record.startRecording();
            while (running) {
                int n = record.read(buffer, 0, buffer.length);
                if (n <= 0) continue;
                String json = recognizer.acceptWaveForm(buffer, n) ? recognizer.getResult() : recognizer.getPartialResult();
                callback.onText(json);
            }
            callback.onText(recognizer.getFinalResult());
        } catch (Exception e) {
            callback.onText("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            try { record.stop(); } catch (Exception ignored) {}
            record.release();
        }
    }

    private File copyModel(Context context) throws IOException {
        File dest = new File(context.getFilesDir(), "vosk-model-ru");
        File marker = new File(dest, "uuid");
        if (marker.exists()) return dest;
        copyAssetDir(context, "vosk-model-ru", dest);
        return dest;
    }

    private void copyAssetDir(Context context, String assetPath, File dest) throws IOException {
        String[] children = context.getAssets().list(assetPath);
        if (children == null || children.length == 0) {
            File parent = dest.getParentFile();
            if (parent != null) parent.mkdirs();
            try (InputStream in = context.getAssets().open(assetPath); OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) > 0;) out.write(buf, 0, n);
            }
            return;
        }
        dest.mkdirs();
        for (String child : children) {
            copyAssetDir(context, assetPath + "/" + child, new File(dest, child));
        }
    }
}
