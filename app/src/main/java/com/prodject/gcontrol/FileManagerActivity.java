package com.prodject.gcontrol;

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
        LinearLayout root = Ui.root(this, "Файловый менеджер");
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
        boolean ok = moveCandidate.renameTo(target);
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
        File storage = new File("/storage");
        File[] roots = storage.listFiles(f -> f.canRead() && !f.getName().equals("emulated") && !f.getName().equals("self"));
        if (roots != null && roots.length > 0) { current = roots[0]; render(); } else Ui.toast(this, "USB-флешка не найдена");
    }

    private String storageInfo(File f) {
        StatFs s = new StatFs(f.getAbsolutePath());
        long free = s.getAvailableBytes();
        long total = s.getTotalBytes();
        return "Свободно " + gb(free) + " / занято " + gb(total - free) + " / всего " + gb(total);
    }

    private String gb(long b) { return String.format(Locale.US, "%.1f GB", b / 1024d / 1024d / 1024d); }
}
