package com.prodject.gcontrol;

import android.content.*;
import android.os.*;
import java.io.*;
import java.util.*;

final class DvrArchive {
    static final String PREFS = "dvr";
    static final String KEY_CAMERAS = "cameras";
    static final String KEY_SEGMENT_SECONDS = "segment_seconds";
    static final String KEY_LIMIT_GB = "limit_gb";
    static final String KEY_STORAGE = "storage";
    static final String STORAGE_EXTERNAL = "external";
    static final String STORAGE_INTERNAL = "internal";
    static final String STORAGE_USB = "usb";
    static final String DEFAULT_CAMERAS = "front,rear,left,right";

    static File dir(Context c) {
        File base = baseDir(c);
        File d = new File(base == null ? c.getFilesDir() : base, "GControlDvr");
        d.mkdirs();
        return d;
    }

    static File newSegment(Context c, String cameraId) {
        File cameraDir = cameraDir(c, cameraId);
        return new File(cameraDir, "cam" + cameraId + "-" + System.currentTimeMillis() + ".mp4");
    }

    static File cameraDir(Context c, String cameraId) {
        File d = new File(dir(c), "cam" + sanitize(cameraId));
        d.mkdirs();
        return d;
    }

    static String[] selectedCameras(Context c) {
        String raw = prefs(c).getString(KEY_CAMERAS, DEFAULT_CAMERAS);
        ArrayList<String> out = new ArrayList<>();
        for (String item : raw.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) out.add(sanitize(trimmed));
        }
        return out.isEmpty() ? DEFAULT_CAMERAS.split(",") : out.toArray(new String[0]);
    }

    static int segmentMillis(Context c) {
        int seconds = prefs(c).getInt(KEY_SEGMENT_SECONDS, 60);
        return Math.max(10, Math.min(600, seconds)) * 1000;
    }

    static long limitBytes(Context c) {
        int gb = prefs(c).getInt(KEY_LIMIT_GB, 5);
        return Math.max(1, Math.min(128, gb)) * 1024L * 1024L * 1024L;
    }

    static void saveSettings(Context c, String cameras, int seconds, int limitGb, String storage) {
        prefs(c).edit()
                .putString(KEY_CAMERAS, cameras)
                .putInt(KEY_SEGMENT_SECONDS, Math.max(10, Math.min(600, seconds)))
                .putInt(KEY_LIMIT_GB, Math.max(1, Math.min(128, limitGb)))
                .putString(KEY_STORAGE, storage)
                .apply();
    }

    static long size(File d) {
        File[] files = d.listFiles();
        long total = 0;
        if (files != null) for (File f : files) total += f.isDirectory() ? size(f) : f.length();
        return total;
    }

    static int prune(Context c, long limitBytes) {
        ArrayList<File> all = new ArrayList<>();
        collectSegments(dir(c), all);
        File[] files = all.toArray(new File[0]);
        if (files == null) return 0;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        int removed = 0;
        for (File f : files) {
            if (size(dir(c)) <= limitBytes) break;
            if (f.delete()) removed++;
        }
        return removed;
    }

    static String summary(Context c) {
        File d = dir(c);
        ArrayList<File> files = new ArrayList<>();
        collectSegments(d, files);
        return "Папка: " + d.getAbsolutePath()
                + "\nХранилище: " + prefs(c).getString(KEY_STORAGE, STORAGE_EXTERNAL)
                + "\nКамеры: " + Arrays.toString(selectedCameras(c))
                + "\nСегмент: " + (segmentMillis(c) / 1000) + " сек"
                + "\nЛимит: " + String.format(Locale.US, "%.2f GB", limitBytes(c) / 1024d / 1024d / 1024d)
                + "\nФайлов: " + files.size()
                + "\nРазмер: " + String.format(Locale.US, "%.2f GB", size(d) / 1024d / 1024d / 1024d)
                + "\nПо камерам:\n" + cameraSummary(c);
    }

    private static File baseDir(Context c) {
        String storage = prefs(c).getString(KEY_STORAGE, STORAGE_EXTERNAL);
        if (STORAGE_INTERNAL.equals(storage)) return c.getFilesDir();
        if (STORAGE_USB.equals(storage)) {
            File usb = usbDir();
            if (usb != null) return usb;
        }
        File base = c.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return base == null ? c.getFilesDir() : base;
    }

    private static File usbDir() {
        File storage = new File("/storage");
        File[] roots = storage.listFiles(file -> file.isDirectory()
                && file.canWrite()
                && !"emulated".equals(file.getName())
                && !"self".equals(file.getName()));
        if (roots == null || roots.length == 0) return null;
        Arrays.sort(roots, Comparator.comparing(File::getAbsolutePath));
        return roots[0];
    }

    private static void collectSegments(File dir, ArrayList<File> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) collectSegments(file, out);
            else if (file.getName().endsWith(".mp4")) out.add(file);
        }
    }

    private static String cameraSummary(Context c) {
        StringBuilder sb = new StringBuilder();
        for (String camera : selectedCameras(c)) {
            File d = cameraDir(c, camera);
            File[] files = d.listFiles((x, name) -> name.endsWith(".mp4"));
            sb.append("cam").append(camera)
                    .append(": ")
                    .append(files == null ? 0 : files.length)
                    .append(" files, ")
                    .append(String.format(Locale.US, "%.2f MB", size(d) / 1024d / 1024d))
                    .append("\n");
        }
        return sb.toString();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
