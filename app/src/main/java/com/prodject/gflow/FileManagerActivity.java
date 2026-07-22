package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*;
import java.util.*;

public class FileManagerActivity extends Activity {
    private File current;
    private File moveCandidate;
    private LinearLayout list;
    private TextView path;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        current = Environment.getExternalStorageDirectory();
        render();
    }

    private void render() {
        LinearLayout root = Ui.root(this, "Файловый менеджер", this::finish);
        path = Ui.text(this, current.getAbsolutePath() + "\n" + storageInfo(current) + (moveCandidate == null ? "" : "\nК перемещению: " + moveCandidate.getName()), 14, false);
        root.addView(path);
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        Button up = Ui.button(this, "Вверх");
        Button mkdir = Ui.button(this, "Папка");
        Button usb = Ui.button(this, "USB");
        Button paste = Ui.button(this, "Переместить сюда");
        up.setOnClickListener(v -> { if (current.getParentFile() != null) { current = current.getParentFile(); render(); }});
        mkdir.setOnClickListener(v -> createFolder());
        usb.setOnClickListener(v -> openUsb());
        paste.setOnClickListener(v -> pasteMove());
        bar.addView(up); bar.addView(mkdir); bar.addView(usb); bar.addView(paste);
        root.addView(bar);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        load();
    }

    private void load() {
        list.removeAllViews();
        File[] files = current.listFiles();
        if (files == null) return;
        Arrays.sort(files, (a, b) -> Boolean.compare(b.isDirectory(), a.isDirectory()) != 0 ? Boolean.compare(b.isDirectory(), a.isDirectory()) : a.getName().compareToIgnoreCase(b.getName()));
        for (File f : files) {
            Button row = Ui.button(this, (f.isDirectory() ? "[DIR] " : "") + f.getName());
            row.setOnClickListener(v -> open(f));
            row.setOnLongClickListener(v -> { actions(f); return true; });
            list.addView(row);
        }
    }

    private void open(File f) {
        if (f.isDirectory()) { current = f; render(); return; }
        String name = f.getName().toLowerCase(Locale.ROOT);
        Class<?> cls = name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".json") || name.endsWith(".xml") ? TextViewerActivity.class : MediaViewerActivity.class;
        startActivity(new Intent(this, cls).putExtra("path", f.getAbsolutePath()));
    }

    private void actions(File f) {
        String[] items = {"Открыть", "Поделиться", "Удалить", "Выбрать для перемещения"};
        new AlertDialog.Builder(this).setTitle(f.getName()).setItems(items, (d, which) -> {
            if (which == 0) open(f);
            if (which == 1) share(f);
            if (which == 2 && delete(f)) render();
            if (which == 3) { moveCandidate = f; render(); }
        }).show();
    }

    private void pasteMove() {
        if (moveCandidate == null) { Ui.toast(this, "Файл не выбран"); return; }
        File target = new File(current, moveCandidate.getName());
        boolean ok = moveCandidate.renameTo(target) || copyThenDelete(moveCandidate, target);
        Ui.toast(this, ok ? "Перемещено" : "Не удалось переместить");
        moveCandidate = null;
        render();
    }

    private void share(File f) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("*/*");
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", f);
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i, "Поделиться"));
    }

    private boolean delete(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) for (File child : files) delete(child);
        }
        return f.delete();
    }

    private void createFolder() {
        final EditText input = new EditText(this);
        input.setHint("Новая папка");
        new AlertDialog.Builder(this).setTitle("Создать папку").setView(input).setPositiveButton("Создать", (d, w) -> {
            new File(current, input.getText().toString()).mkdirs();
            render();
        }).setNegativeButton("Отмена", null).show();
    }

    private void openUsb() {
        ArrayList<File> roots = usbRoots();
        if (!roots.isEmpty()) { current = roots.get(0); render(); } else Ui.toast(this, "USB-флешка не найдена");
    }

    private ArrayList<File> usbRoots() {
        ArrayList<File> roots = new ArrayList<>();
        try {
            Object sm = getSystemService(Context.STORAGE_SERVICE);
            Object volumes = sm.getClass().getMethod("getStorageVolumes").invoke(sm);
            if (volumes instanceof List) {
                for (Object volume : (List<?>) volumes) {
                    File dir = volumeDirectory(volume);
                    if (dir != null && dir.canRead() && isUsbLike(dir)) roots.add(dir);
                }
            }
        } catch (Exception ignored) {
        }
        if (!roots.isEmpty()) return roots;
        File storage = new File("/storage");
        File[] fallback = storage.listFiles(f -> f.canRead() && isUsbLike(f));
        if (fallback != null) roots.addAll(Arrays.asList(fallback));
        return roots;
    }

    private File volumeDirectory(Object volume) {
        try {
            Object dir = volume.getClass().getMethod("getDirectory").invoke(volume);
            if (dir instanceof File) return (File) dir;
        } catch (Exception ignored) {
        }
        try {
            Object path = volume.getClass().getMethod("getPath").invoke(volume);
            if (path instanceof String) return new File((String) path);
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isUsbLike(File f) {
        String name = f.getName();
        String path = f.getAbsolutePath();
        return f.isDirectory()
                && !name.equals("emulated")
                && !name.equals("self")
                && !path.contains("/storage/emulated");
    }

    private boolean copyThenDelete(File source, File target) {
        try {
            copyRec(source, target);
            return delete(source);
        } catch (Exception e) {
            android.util.Log.e("GFlowFiles", "move fallback failed", e);
            return false;
        }
    }

    private void copyRec(File source, File target) throws IOException {
        if (source.isDirectory()) {
            target.mkdirs();
            File[] files = source.listFiles();
            if (files != null) for (File child : files) copyRec(child, new File(target, child.getName()));
            return;
        }
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
    }

    private String storageInfo(File f) {
        StatFs s = new StatFs(f.getAbsolutePath());
        long free = s.getAvailableBytes();
        long total = s.getTotalBytes();
        return "Свободно " + gb(free) + " / занято " + gb(total - free) + " / всего " + gb(total);
    }

    private String gb(long b) { return String.format(Locale.US, "%.1f GB", b / 1024d / 1024d / 1024d); }
}
