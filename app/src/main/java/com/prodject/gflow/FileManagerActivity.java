package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FileManagerActivity extends Activity {
    private File current;
    private File moveCandidate;
    private LinearLayout contentHost;
    private TextView progressView;
    private String lastMoveSummary = "Операции перемещения пока не запускались";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current = Environment.getExternalStorageDirectory();
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        renderContent();
    }

    private View buildShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(Ui.dashboardBg(this));
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildStoragePanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildActionPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildFileListPanel(), lpMatchWrap(0, 0, 0, 16));
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "Storage / USB / Files"));
        titleBlock.addView(Ui.text(this, "Файловый менеджер", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Items", String.valueOf(visibleFiles().size())));
        bar.addView(buildTopStat("USB", String.valueOf(usbRoots().size())));
        bar.addView(buildTopStat("Move", moveCandidate == null ? "idle" : moveCandidate.getName()));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Path / Storage Status / Move Queue"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Путь", current.getAbsolutePath()));
        left.addView(metricLine("Storage", storageInfo(current)));
        left.addView(metricLine("USB lookup", usbRoots().isEmpty() ? "не найдено" : usbRoots().get(0).getAbsolutePath()));
        left.addView(metricLine("Move candidate", moveCandidate == null ? "не выбран" : moveCandidate.getAbsolutePath()));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout stateCard = Ui.glassCard(this);
        stateCard.setGravity(Gravity.CENTER);
        stateCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18));
        TextView label = Ui.text(this, moveCandidate == null ? "FILES" : "MOVE", 30, true);
        label.setGravity(Gravity.CENTER);
        stateCard.addView(label);
        LinearLayout.LayoutParams stateLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        stateLp.leftMargin = Ui.dp(this, 12);
        row.addView(stateCard, stateLp);
        hero.addView(row);

        progressView = Ui.text(this, lastMoveSummary, 15, true);
        progressView.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(progressView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Вверх", this::goUp);
        addActionChip(quick, "USB", this::openUsb);
        addActionChip(quick, "Папка", this::createFolder);
        addActionChip(quick, "Вставить", this::pasteMove);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Current", shortPath(current), Ui.CYAN);
        addStatusCard(grid, "Storage", shortStorageInfo(current), Ui.SUCCESS);
        addStatusCard(grid, "USB candidates", usbPreview(), Ui.WARNING);
        addStatusCard(grid, "Move queue", moveCandidate == null ? "empty" : moveCandidate.getName(), Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildStoragePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Storage Card"));
        panel.addView(Ui.text(this, "Показывает путь, storage status и доступные USB candidates.", 14, false));
        panel.addView(Ui.muted(this, "Текущий путь: " + current.getAbsolutePath()), lpMatchWrap(0, 10, 0, 0));
        panel.addView(Ui.muted(this, storageInfo(current)), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "Internal: " + Environment.getExternalStorageDirectory().getAbsolutePath()), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "USB candidates: " + usbPreview()), lpMatchWrap(0, 4, 0, 0));
        return panel;
    }

    private LinearLayout buildActionPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Actions"));
        panel.addView(Ui.text(this, "Создание папки, переход вверх, USB lookup и move fallback с progress/log.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Вверх", this::goUp);
        addActionChip(row, "USB", this::openUsb);
        addActionChip(row, "Папка", this::createFolder);
        addActionChip(row, "Переместить сюда", this::pasteMove);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildFileListPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Files"));
        panel.addView(Ui.text(this, "Открыть, поделиться, удалить, выбрать для перемещения, открыть как media/text.", 14, false));

        List<File> files = visibleFiles();
        if (files.isEmpty()) {
            panel.addView(emptyState("Файлы не найдены"));
            return panel;
        }
        for (File file : files) panel.addView(buildFileCard(file), lpMatchWrap(0, 0, 0, 14));
        return panel;
    }

    private LinearLayout buildFileCard(File file) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, fileLabel(file), 18, true));
        card.addView(Ui.muted(this, fileMeta(file)));

        LinearLayout row = Ui.row(this);
        addMiniAction(row, "Open", () -> open(file));
        addMiniAction(row, "Share", () -> share(file));
        addMiniAction(row, "Move", () -> {
            moveCandidate = file;
            lastMoveSummary = "Выбран для перемещения: " + file.getAbsolutePath();
            renderContent();
        });
        addMiniAction(row, "Delete", () -> confirmDelete(file));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));

        LinearLayout row2 = Ui.row(this);
        addMiniAction(row2, "Text", () -> openAsText(file));
        addMiniAction(row2, "Media", () -> openAsMedia(file));
        card.addView(row2, lpMatchWrap(0, 8, 0, 0));
        return card;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Up", this::goUp, false);
        addDockButton(dock, "USB", this::openUsb, false);
        addDockButton(dock, "Move", this::pasteMove, moveCandidate != null);
        addDockButton(dock, "Folder", this::createFolder, false);
        addDockButton(dock, "Back", this::finish, false);
        return dock;
    }

    private List<File> visibleFiles() {
        File[] files = current.listFiles();
        ArrayList<File> result = new ArrayList<>();
        if (files == null) return result;
        result.addAll(Arrays.asList(files));
        result.sort((a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        return result;
    }

    private void goUp() {
        if (current.getParentFile() != null) {
            current = current.getParentFile();
            renderContent();
        }
    }

    private void open(File file) {
        if (file.isDirectory()) {
            current = file;
            renderContent();
            return;
        }
        if (isTextFile(file)) openAsText(file);
        else openAsMedia(file);
    }

    private void openAsText(File file) {
        startActivity(new Intent(this, TextViewerActivity.class).putExtra("path", file.getAbsolutePath()));
    }

    private void openAsMedia(File file) {
        startActivity(new Intent(this, MediaViewerActivity.class).putExtra("path", file.getAbsolutePath()));
    }

    private void confirmDelete(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage(file.getAbsolutePath())
                .setPositiveButton("Удалить", (d, w) -> {
                    boolean ok = delete(file);
                    lastMoveSummary = ok ? "Удалено: " + file.getName() : "Не удалось удалить: " + file.getName();
                    renderContent();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void createFolder() {
        EditText input = edit("Новая папка", "");
        new AlertDialog.Builder(this)
                .setTitle("Создать папку")
                .setView(input)
                .setPositiveButton("Создать", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Ui.toast(this, "Имя папки пустое");
                        return;
                    }
                    File target = new File(current, name);
                    boolean ok = target.mkdirs();
                    lastMoveSummary = ok ? "Папка создана: " + target.getAbsolutePath() : "Не удалось создать папку";
                    renderContent();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void openUsb() {
        ArrayList<File> roots = usbRoots();
        if (!roots.isEmpty()) {
            current = roots.get(0);
            lastMoveSummary = "USB открыт: " + current.getAbsolutePath();
            renderContent();
        } else {
            Ui.toast(this, "USB-флешка не найдена");
        }
    }

    private void pasteMove() {
        if (moveCandidate == null) {
            Ui.toast(this, "Файл не выбран");
            return;
        }
        File target = new File(current, moveCandidate.getName());
        if (target.equals(moveCandidate)) {
            lastMoveSummary = "Источник и цель совпадают";
            renderContent();
            return;
        }
        progress("Перемещение: " + moveCandidate.getName());
        new Thread(() -> {
            boolean renameOk = moveCandidate.renameTo(target);
            if (renameOk) {
                runOnUiThread(() -> {
                    lastMoveSummary = "Перемещено renameTo: " + target.getAbsolutePath();
                    moveCandidate = null;
                    renderContent();
                });
                return;
            }
            try {
                progress("renameTo не сработал, запускаю copy/delete fallback…");
                copyRec(moveCandidate, target);
                progress("Копирование завершено, удаляю исходник…");
                boolean deleteOk = delete(moveCandidate);
                runOnUiThread(() -> {
                    if (deleteOk) {
                        lastMoveSummary = "Fallback copy/delete завершен: " + target.getAbsolutePath();
                        moveCandidate = null;
                    } else {
                        lastMoveSummary = "Copy выполнен, но delete исходника не удался: " + moveCandidate.getAbsolutePath();
                    }
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    lastMoveSummary = "Ошибка fallback move: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void progress(String message) {
        runOnUiThread(() -> {
            lastMoveSummary = message;
            if (progressView != null) progressView.setText(message);
        });
    }

    private void share(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mime(file));
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Поделиться"));
        } catch (Exception e) {
            Ui.toast(this, "Share error: " + e.getMessage());
        }
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) for (File child : files) delete(child);
        }
        return file.delete();
    }

    private ArrayList<File> usbRoots() {
        ArrayList<File> roots = new ArrayList<>();
        try {
            Object storageManager = getSystemService(Context.STORAGE_SERVICE);
            Object volumes = storageManager.getClass().getMethod("getStorageVolumes").invoke(storageManager);
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

    private boolean isUsbLike(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();
        return file.isDirectory()
                && !name.equals("emulated")
                && !name.equals("self")
                && !path.contains("/storage/emulated");
    }

    private void copyRec(File source, File target) throws IOException {
        if (source.isDirectory()) {
            target.mkdirs();
            File[] files = source.listFiles();
            if (files != null) {
                for (File child : files) {
                    copyRec(child, new File(target, child.getName()));
                }
            }
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

    private boolean isTextFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".json") || name.endsWith(".xml");
    }

    private String fileLabel(File file) {
        return (file.isDirectory() ? "[DIR] " : "") + file.getName();
    }

    private String fileMeta(File file) {
        if (file.isDirectory()) return "Папка · " + safeCount(file) + " items";
        return readableBytes(file.length()) + " · " + mime(file);
    }

    private int safeCount(File file) {
        File[] files = file.listFiles();
        return files == null ? 0 : files.length;
    }

    private String mime(File file) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String mime = ext == null ? null : MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase(Locale.ROOT));
        return mime == null ? "*/*" : mime;
    }

    private String storageInfo(File file) {
        try {
            StatFs stat = new StatFs(file.getAbsolutePath());
            long free = stat.getAvailableBytes();
            long total = stat.getTotalBytes();
            return "Свободно " + readableBytes(free) + " / занято " + readableBytes(total - free) + " / всего " + readableBytes(total);
        } catch (Exception e) {
            return "Storage info unavailable: " + e.getMessage();
        }
    }

    private String shortStorageInfo(File file) {
        try {
            StatFs stat = new StatFs(file.getAbsolutePath());
            return readableBytes(stat.getAvailableBytes()) + " free";
        } catch (Exception e) {
            return "n/a";
        }
    }

    private String usbPreview() {
        ArrayList<File> roots = usbRoots();
        if (roots.isEmpty()) return "none";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(2, roots.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(" · ");
            sb.append(roots.get(i).getName());
        }
        if (roots.size() > limit) sb.append(" +").append(roots.size() - limit);
        return sb.toString();
    }

    private String shortPath(File file) {
        String value = file.getAbsolutePath();
        return value.length() > 28 ? "…" + value.substring(value.length() - 28) : value;
    }

    private String readableBytes(long bytes) {
        if (bytes > 1024L * 1024L * 1024L) return String.format(Locale.US, "%.1f GB", bytes / 1024d / 1024d / 1024d);
        if (bytes > 1024L * 1024L) return String.format(Locale.US, "%.1f MB", bytes / 1024d / 1024d);
        if (bytes > 1024L) return String.format(Locale.US, "%.1f KB", bytes / 1024d);
        return bytes + " B";
    }

    private EditText edit(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 18), Ui.glassLine(this)));
        field.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        field.setLayoutParams(lpMatchWrap(0, 12, 0, 0));
        return field;
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextSize(13);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : Color.argb(54, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 18, true));
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private TextView emptyState(String text) {
        TextView view = Ui.text(this, text, 16, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 24));
        return view;
    }

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }
}
