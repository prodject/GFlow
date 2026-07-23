package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MediaViewerActivity extends Activity {
    private File currentFile;
    private ArrayList<File> gallery = new ArrayList<>();
    private int currentIndex = -1;
    private LinearLayout contentHost;
    private TextView heroStatusView;
    private boolean fullscreen;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveInput();
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        resolveInput();
        renderContent();
    }

    private void resolveInput() {
        String path = getIntent().getStringExtra("path");
        currentFile = path == null ? null : new File(path);
        gallery.clear();
        currentIndex = -1;
        if (currentFile == null) return;
        File parent = currentFile.getParentFile();
        if (parent == null) return;
        File[] files = parent.listFiles(this::isMediaFile);
        if (files == null) return;
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        gallery.addAll(Arrays.asList(files));
        for (int i = 0; i < gallery.size(); i++) {
            if (gallery.get(i).getAbsolutePath().equals(currentFile.getAbsolutePath())) {
                currentIndex = i;
                break;
            }
        }
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
        contentHost.addView(buildViewerPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildInfoPanel(), lpMatchWrap(0, 0, 0, 16));
        Ui.staggerIn(collectChildren(contentHost), 40, 70);
        updateHeroStatus();
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> {
            Ui.press(v);
            finish();
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, mediaTypeLabel()));
        titleBlock.addView(Ui.text(this, "Media Viewer", 28, true));
        TextView subtitle = Ui.muted(this, "Media first. Gallery navigation and file actions stay secondary.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Type", mediaType()));
        bar.addView(buildTopStat("Index", galleryIndexLabel()));
        bar.addView(buildTopStat("Size", currentFile == null ? "n/a" : readableBytes(currentFile.length())));
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
        hero.addView(Ui.label(this, "Media Overview"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(buildHeroMetric("File", currentFile == null ? "No file" : currentFile.getName()));
        left.addView(buildHeroMetric("Type", mediaType()));
        left.addView(buildHeroMetric("Gallery", galleryIndexLabel()));
        left.addView(buildHeroMetric("Mode", fullscreen ? "fullscreen" : "panel"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.VERTICAL);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(Ui.cardBg(this, Color.argb(58, 255, 255, 255), Ui.dp(this, 32), Color.argb(34, 255, 255, 255)));
        TextView mode = Ui.text(this, isImage() ? "IMG" : isVideo() ? "VID" : "MEDIA", 30, true);
        mode.setGravity(Gravity.CENTER);
        badge.addView(mode);
        TextView hint = Ui.muted(this, isImage() ? "Swipe and zoom" : isVideo() ? "Play and browse" : "Media");
        hint.setGravity(Gravity.CENTER);
        badge.addView(hint);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        badgeLp.leftMargin = Ui.dp(this, 12);
        row.addView(badge, badgeLp);
        hero.addView(row);

        heroStatusView = Ui.text(this, viewerStatus(), 16, true);
        heroStatusView.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(heroStatusView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Prev", this::showPrev);
        addActionChip(quick, "Next", this::showNext);
        addActionChip(quick, "Share", this::shareCurrent);
        addActionChip(quick, "Delete", this::deleteCurrent);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private View buildHeroMetric(String key, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(58, 255, 255, 255), Ui.dp(this, 24), Color.argb(40, 255, 255, 255)));
        card.addView(Ui.label(this, key));
        TextView text = Ui.text(this, value, 16, true);
        text.setPadding(0, Ui.dp(this, 2), 0, 0);
        card.addView(text);
        return card;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Viewer", viewerStatus(), Ui.CYAN);
        addStatusCard(grid, "Info", currentFile == null ? "n/a" : currentFile.getName(), Ui.SUCCESS);
        addStatusCard(grid, "Gallery", galleryIndexLabel(), Ui.WARNING);
        addStatusCard(grid, "Mode", fullscreen ? "fullscreen" : "panel", Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildViewerPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Viewing Surface"));
        panel.addView(Ui.text(this, "Контент доминирует над chrome: изображения для swipe/zoom, видео для direct playback.", 14, false));
        if (currentFile == null || !currentFile.exists()) {
            panel.addView(emptyState("Откройте фото или видео из файлового менеджера"));
            return panel;
        }

        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 26), Ui.glassLine(this)));
        frame.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        if (isImage()) {
            ZoomImageView image = new ZoomImageView(this);
            image.setImageURI(Uri.fromFile(currentFile));
            image.setOnSwipeListener(new ZoomImageView.SwipeListener() {
                @Override public void onSwipeLeft() { showNext(); }
                @Override public void onSwipeRight() { showPrev(); }
            });
            frame.addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, fullscreen ? 780 : 520)));
        } else if (isVideo()) {
            VideoView video = new VideoView(this);
            video.setVideoURI(Uri.fromFile(currentFile));
            MediaController controller = new MediaController(this);
            controller.setAnchorView(video);
            video.setMediaController(controller);
            video.setOnPreparedListener(mp -> video.start());
            frame.addView(video, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, fullscreen ? 780 : 520)));
        } else {
            panel.addView(emptyState("Поддерживаются изображения и видео"));
            return panel;
        }
        panel.addView(frame, lpMatchWrap(0, 12, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Fullscreen", () -> {
            fullscreen = !fullscreen;
            renderContent();
        });
        addActionChip(row, "Info", this::showInfoSheet);
        addActionChip(row, "File Manager", this::finish);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildInfoPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Info"));
        if (currentFile == null || !currentFile.exists()) {
            panel.addView(Ui.text(this, "Файл не выбран.", 14, false));
            return panel;
        }
        panel.addView(Ui.muted(this, "Имя: " + currentFile.getName()), lpMatchWrap(0, 8, 0, 0));
        panel.addView(Ui.muted(this, "Путь: " + currentFile.getAbsolutePath()), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "Размер: " + readableBytes(currentFile.length())), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "Изменен: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(currentFile.lastModified()))), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "Mime: " + mediaType()), lpMatchWrap(0, 4, 0, 0));
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Prev", this::showPrev, false);
        addDockButton(dock, "Next", this::showNext, false);
        addDockButton(dock, "Share", this::shareCurrent, false);
        addDockButton(dock, "Delete", this::deleteCurrent, false);
        addDockButton(dock, "Back", this::finish, false);
        Ui.animateIn(dock, 150, 10f);
        return dock;
    }

    private void showPrev() {
        if (gallery.isEmpty() || currentIndex <= 0) {
            Ui.toast(this, "Предыдущий файл недоступен");
            return;
        }
        openIndex(currentIndex - 1);
    }

    private void showNext() {
        if (gallery.isEmpty() || currentIndex < 0 || currentIndex >= gallery.size() - 1) {
            Ui.toast(this, "Следующий файл недоступен");
            return;
        }
        openIndex(currentIndex + 1);
    }

    private void openIndex(int index) {
        if (index < 0 || index >= gallery.size()) return;
        currentFile = gallery.get(index);
        currentIndex = index;
        fullscreen = false;
        renderContent();
    }

    private void shareCurrent() {
        if (currentFile == null || !currentFile.exists()) {
            Ui.toast(this, "Файл не найден");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(isImage() ? "image/*" : isVideo() ? "video/*" : "*/*");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", currentFile);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Поделиться"));
        } catch (Exception e) {
            Ui.toast(this, "Share error: " + e.getMessage());
        }
    }

    private void deleteCurrent() {
        if (currentFile == null || !currentFile.exists()) {
            Ui.toast(this, "Файл не найден");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Удалить медиа")
                .setMessage(currentFile.getAbsolutePath())
                .setPositiveButton("Удалить", (d, w) -> {
                    boolean ok = currentFile.delete();
                    if (!ok) {
                        Ui.toast(this, "Не удалось удалить");
                        return;
                    }
                    if (!gallery.isEmpty() && currentIndex >= 0 && currentIndex < gallery.size()) gallery.remove(currentIndex);
                    if (gallery.isEmpty()) {
                        currentFile = null;
                        currentIndex = -1;
                    } else {
                        if (currentIndex >= gallery.size()) currentIndex = gallery.size() - 1;
                        currentFile = gallery.get(currentIndex);
                    }
                    renderContent();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showInfoSheet() {
        String body = currentFile == null ? "Нет данных"
                : "Имя: " + currentFile.getName()
                + "\nПуть: " + currentFile.getAbsolutePath()
                + "\nРазмер: " + readableBytes(currentFile.length())
                + "\nТип: " + mediaType()
                + "\nГалерея: " + galleryIndexLabel();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, "Media Info", 22, true));
        sheet.addView(Ui.muted(this, body));
        builder.setView(sheet);
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    private void updateHeroStatus() {
        if (heroStatusView != null) heroStatusView.setText(viewerStatus());
    }

    private boolean isMediaFile(File file) {
        if (file == null || !file.isFile()) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.matches(".+\\.(png|jpg|jpeg|webp|gif|bmp|mp4|mkv|webm|3gp|avi|mov)$");
    }

    private boolean isImage() {
        return currentFile != null && currentFile.getName().toLowerCase(Locale.ROOT).matches(".+\\.(png|jpg|jpeg|webp|gif|bmp)$");
    }

    private boolean isVideo() {
        return currentFile != null && currentFile.getName().toLowerCase(Locale.ROOT).matches(".+\\.(mp4|mkv|webm|3gp|avi|mov)$");
    }

    private String mediaType() {
        if (isImage()) return "image";
        if (isVideo()) return "video";
        return currentFile == null ? "n/a" : "media";
    }

    private String mediaTypeLabel() {
        if (isImage()) return "Image Viewer";
        if (isVideo()) return "Video Viewer";
        return "Media Viewer";
    }

    private String galleryIndexLabel() {
        if (gallery.isEmpty() || currentIndex < 0) return "n/a";
        return (currentIndex + 1) + " / " + gallery.size();
    }

    private String viewerStatus() {
        if (currentFile == null) return "Откройте фото, видео или аудио из файлового менеджера.";
        return (isImage() ? "Image viewer" : isVideo() ? "Video viewer" : "Media viewer")
                + " · " + galleryIndexLabel()
                + " · " + (fullscreen ? "fullscreen" : "panel");
    }

    private String readableBytes(long bytes) {
        if (bytes > 1024L * 1024L * 1024L) return String.format(Locale.US, "%.1f GB", bytes / 1024d / 1024d / 1024d);
        if (bytes > 1024L * 1024L) return String.format(Locale.US, "%.1f MB", bytes / 1024d / 1024d);
        if (bytes > 1024L) return String.format(Locale.US, "%.1f KB", bytes / 1024d);
        return bytes + " B";
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
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
        button.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(24, 255, 255, 255), Ui.dp(this, 24), Color.argb(20, 255, 255, 255)));
        card.addView(Ui.label(this, title));
        TextView body = Ui.text(this, value, 13, false);
        body.setTextColor(Ui.secondaryText(this));
        card.addView(body);
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 4));
        accentLp.topMargin = Ui.dp(this, 10);
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

    private View[] collectChildren(LinearLayout layout) {
        View[] views = new View[layout.getChildCount()];
        for (int i = 0; i < layout.getChildCount(); i++) views[i] = layout.getChildAt(i);
        return views;
    }

    private static final class ZoomImageView extends ImageView implements View.OnTouchListener {
        interface SwipeListener {
            void onSwipeLeft();
            void onSwipeRight();
        }

        private final Matrix matrix = new Matrix();
        private final PointF last = new PointF();
        private final ScaleGestureDetector scaleDetector;
        private float scaleFactor = 1f;
        private boolean dragging;
        private float downX;
        private SwipeListener swipeListener;

        ZoomImageView(Activity context) {
            super(context);
            setScaleType(ScaleType.MATRIX);
            setImageMatrix(matrix);
            setAdjustViewBounds(true);
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector detector) {
                    scaleFactor *= detector.getScaleFactor();
                    scaleFactor = Math.max(1f, Math.min(scaleFactor, 4f));
                    matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                    setImageMatrix(matrix);
                    return true;
                }
            });
            setOnTouchListener(this);
        }

        void setOnSwipeListener(SwipeListener listener) {
            swipeListener = listener;
        }

        @Override public boolean onTouch(View v, MotionEvent event) {
            scaleDetector.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(event.getX(), event.getY());
                    downX = event.getX();
                    dragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!scaleDetector.isInProgress()) {
                        float dx = event.getX() - last.x;
                        float dy = event.getY() - last.y;
                        if (Math.abs(dx) > 4f || Math.abs(dy) > 4f) dragging = true;
                        matrix.postTranslate(dx, dy);
                        setImageMatrix(matrix);
                        last.set(event.getX(), event.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - downX;
                    if (!dragging && scaleFactor <= 1.1f) {
                        performClick();
                    } else if (Math.abs(deltaX) > 120f && scaleFactor <= 1.1f && swipeListener != null) {
                        if (deltaX < 0f) swipeListener.onSwipeLeft();
                        else swipeListener.onSwipeRight();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override public boolean performClick() {
            return super.performClick();
        }
    }
}
