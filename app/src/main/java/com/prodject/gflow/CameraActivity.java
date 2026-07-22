package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class CameraActivity extends Activity {
    private TextView archiveSummary;
    private TextView cameraSummary;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildCameraShell());
        refreshStatus();
        Ui.animateIn(getWindow().getDecorView());
    }

    private View buildCameraShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildControlPanel(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
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
        titleBlock.addView(Ui.label(this, "DVR / Camera Hub"));
        TextView title = Ui.text(this, "DVR / Камеры", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Запись", "Standby"));
        bar.addView(buildTopStat("Источник", "Camera2 / EVS"));
        bar.addView(buildTopStat("Архив", "GFlowDvr"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        TextView valueView = Ui.text(this, value, 14, true);
        valueView.setPadding(0, 0, 0, 0);
        card.addView(valueView);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Camera Visual"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Режим", "Передняя + задняя запись"));
        left.addView(metricLine("Камеры", Arrays.toString(DvrArchive.selectedCameras(this))));
        left.addView(metricLine("Качество", qualityLabel()));
        left.addView(metricLine("Путь", DvrArchive.dir(this).getAbsolutePath()));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        CameraVisualView visual = new CameraVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Старт", this::startRecording);
        addActionChip(quick, "Стоп", this::stopRecording);
        addActionChip(quick, "360", () -> openLegacyDvr("avm"));
        addActionChip(quick, "Архив", () -> openLegacyDvr("archive"));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildControlPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Recording Controls"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Старт записи", Ui.SUCCESS, this::startRecording);
        addTile(grid, "Стоп записи", Ui.ERROR, this::stopRecording);
        addTile(grid, "Обновить", Ui.CYAN, this::refreshStatus);
        addTile(grid, "EVS Rear", Color.rgb(255, 179, 64), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_REAR, true));
        addTile(grid, "EVS 360", Color.rgb(129, 149, 255), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_AVM, true));
        addTile(grid, "EVS DVR", Color.rgb(95, 133, 255), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_DVR, true));
        addTile(grid, "Закрыть Rear", Color.rgb(120, 136, 156), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_REAR, false));
        addTile(grid, "Закрыть 360", Color.rgb(120, 136, 156), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_AVM, false));
        addTile(grid, "Настройки", Ui.WARNING, () -> openLegacyDvr("settings"));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);

        LinearLayout archive = Ui.glassCard(this);
        archive.addView(Ui.label(this, "Архив"));
        archiveSummary = Ui.text(this, "", 15, false);
        archiveSummary.setPadding(0, Ui.dp(this, 8), 0, 0);
        archive.addView(archiveSummary);
        addCard(grid, archive);

        LinearLayout cameras = Ui.glassCard(this);
        cameras.addView(Ui.label(this, "Camera2 / EVS"));
        cameraSummary = Ui.text(this, "", 15, false);
        cameraSummary.setPadding(0, Ui.dp(this, 8), 0, 0);
        cameras.addView(cameraSummary);
        addCard(grid, cameras);

        return grid;
    }

    private void addCard(GridLayout grid, LinearLayout card) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Rec", this::startRecording, true);
        addDockButton(dock, "Stop", this::stopRecording, false);
        addDockButton(dock, "360", () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_AVM, true), false);
        addDockButton(dock, "Rear", () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_REAR, true), false);
        addDockButton(dock, "Legacy", () -> openLegacyDvr("legacy"), false);
        return dock;
    }

    private void addTile(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16));
        tile.setBackground(Ui.cardBg(this, Color.argb(88, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 22), Color.argb(80, 255, 255, 255)));
        tile.setOnClickListener(v -> {
            action.run();
            Ui.toast(this, label);
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> {
            action.run();
            Ui.toast(this, label);
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
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void startRecording() {
        startForegroundService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_START));
        refreshStatus();
    }

    private void stopRecording() {
        startService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_STOP));
        refreshStatus();
    }

    private void runEvs(int cameraId, boolean open) {
        EcarxDvrAdapter adapter = new EcarxDvrAdapter(this);
        EcarxDvrAdapter.Result result = open ? adapter.openEvs(cameraId) : adapter.closeEvs(cameraId);
        Ui.toast(this, result.success ? "EVS команда отправлена" : "EVS ошибка");
        refreshStatus();
    }

    private void openLegacyDvr(String mode) {
        Intent intent = new Intent(this, DvrActivity.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }

    private void refreshStatus() {
        if (archiveSummary != null) archiveSummary.setText(DvrArchive.summary(this));
        if (cameraSummary != null) cameraSummary.setText(cameraSummaryText());
    }

    private String cameraSummaryText() {
        StringBuilder sb = new StringBuilder();
        try {
            CameraManager cm = getSystemService(CameraManager.class);
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics cc = cm.getCameraCharacteristics(id);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                sb.append("camera2:").append(id).append(" · ").append(facingName(facing)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Camera2 error: ").append(e.getMessage()).append("\n");
        }
        sb.append("EVS: rear, 360, dvr\n");
        sb.append("USB:\n").append(usbRootsSummary());
        return sb.toString().trim();
    }

    private String qualityLabel() {
        DvrArchive.Quality quality = DvrArchive.quality(this);
        return quality.width + "x" + quality.height;
    }

    private String usbRootsSummary() {
        StringBuilder sb = new StringBuilder();
        File storage = new File("/storage");
        File[] roots = storage.listFiles(file -> file.isDirectory()
                && file.canWrite()
                && !"emulated".equals(file.getName())
                && !"self".equals(file.getName()));
        if (roots == null || roots.length == 0) return "не найдены";
        Arrays.sort(roots, Comparator.comparing(File::getAbsolutePath));
        for (File root : roots) sb.append(root.getAbsolutePath()).append("\n");
        return sb.toString().trim();
    }

    private String facingName(Integer facing) {
        if (facing == null) return "unknown";
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "front";
        if (facing == CameraCharacteristics.LENS_FACING_BACK) return "rear";
        if (android.os.Build.VERSION.SDK_INT >= 23 && facing == CameraCharacteristics.LENS_FACING_EXTERNAL) return "external";
        return "other";
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private static final class CameraVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        CameraVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(36, 255, 255, 255));
            canvas.drawRoundRect(new RectF(w * 0.08f, h * 0.10f, w * 0.92f, h * 0.90f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), paint);
            int[] colors = {
                    Color.argb(180, 77, 163, 255),
                    Color.argb(180, 53, 208, 127),
                    Color.argb(180, 255, 179, 64),
                    Color.argb(180, 255, 77, 77)
            };
            for (int i = 0; i < 4; i++) {
                float left = w * (0.12f + i * 0.19f);
                paint.setColor(colors[i]);
                canvas.drawRoundRect(new RectF(left, h * 0.28f, left + w * 0.14f, h * 0.72f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);
            }
            paint.setColor(Color.WHITE);
            canvas.drawCircle(w * 0.50f, h * 0.50f, Ui.dp(getContext(), 18), paint);
        }
    }
}
