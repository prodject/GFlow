package com.prodject.gcontrol;

import android.content.*;
import android.os.*;
import java.io.*;
import java.util.*;

final class DvrArchive {
    static File dir(Context c) {
        File base = c.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File d = new File(base == null ? c.getFilesDir() : base, "GControlDvr");
        d.mkdirs();
        return d;
    }

    static File newSegment(Context c, String cameraId) {
        return new File(dir(c), "cam" + cameraId + "-" + System.currentTimeMillis() + ".mp4");
    }

    static long size(File d) {
        File[] files = d.listFiles();
        long total = 0;
        if (files != null) for (File f : files) total += f.isDirectory() ? size(f) : f.length();
        return total;
    }

    static int prune(Context c, long limitBytes) {
        File[] files = dir(c).listFiles((d, name) -> name.endsWith(".mp4"));
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
        File[] files = d.listFiles((x, name) -> name.endsWith(".mp4"));
        return "Папка: " + d.getAbsolutePath() + "\nФайлов: " + (files == null ? 0 : files.length) + "\nРазмер: " + String.format(Locale.US, "%.2f GB", size(d) / 1024d / 1024d / 1024d);
    }
}
