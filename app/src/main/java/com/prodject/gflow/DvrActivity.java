package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DvrActivity extends Activity {
    private TextView status;
    private EditText camerasInput;
    private EditText segmentInput;
    private EditText limitInput;
    private EditText storagePathInput;
    private Spinner storageInput;
    private Spinner qualityInput;
    private LinearLayout root;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(Ui.dashboardBg(this));
        scroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), margin(0, 16, 0, 16));
        root.addView(buildArchivePanel(), margin(0, 0, 0, 16));
        root.addView(buildCapturePanel(), margin(0, 0, 0, 16));
        root.addView(buildBridgePanel(), margin(0, 0, 0, 16));
        root.addView(buildStatusPanel(), margin(0, 0, 0, 16));
        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));

        setContentView(scroll);
        Ui.staggerIn(collectChildren(root), 40, 55);
        Ui.animateIn(dock, 220, 18f);
        Ui.animateIn(getWindow().getDecorView());
        refresh();
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.press(back, this::finish);
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "Legacy Capture / Archive"));
        titleBlock.addView(Ui.text(this, "DVR Legacy", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Sources", Arrays.toString(DvrArchive.selectedCameras(this))));
        bar.addView(buildTopStat("Quality", qualityLabel()));
        bar.addView(buildTopStat("Archive", "GFlowDvr"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Capture Legacy Surface"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Mode", "Camera2 + EVS bridge"));
        left.addView(metricLine("Archive", DvrArchive.dir(this).getAbsolutePath()));
        left.addView(metricLine("Quality", qualityLabel()));
        left.addView(metricLine("Sources", Arrays.toString(DvrArchive.selectedCameras(this))));
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout visualCard = Ui.glassCard(this);
        visualCard.setGravity(Gravity.CENTER);
        visualCard.addView(new CameraPreviewWidget(this), new LinearLayout.LayoutParams(Ui.dp(this, 220), Ui.dp(this, 150)));
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 240), Ui.dp(this, 180));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visualCard, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Save", this::saveSettings);
        addActionChip(quick, "Start", this::startRecording);
        addActionChip(quick, "Stop", this::stopRecording);
        addActionChip(quick, "Refresh", this::refresh);
        hero.addView(quick, margin(0, 14, 0, 0));
        return hero;
    }

    private LinearLayout buildArchivePanel() {
        SharedPreferences prefs = getSharedPreferences(DvrArchive.PREFS, MODE_PRIVATE);
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 16, 24, 42) : Color.argb(246, 240, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Archive Settings"));
        panel.addView(Ui.muted(this, "Legacy DVR screen kept for compatibility, but archive setup is visually demoted behind the primary capture flow."));

        camerasInput = new EditText(this);
        camerasInput.setHint("front,rear,camera2:0,evs:360,evs:rear,evs:dvr");
        camerasInput.setText(prefs.getString(DvrArchive.KEY_CAMERAS, DvrArchive.DEFAULT_CAMERAS));
        segmentInput = new EditText(this);
        segmentInput.setHint("Segment, sec");
        segmentInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        segmentInput.setText(String.valueOf(prefs.getInt(DvrArchive.KEY_SEGMENT_SECONDS, 60)));
        limitInput = new EditText(this);
        limitInput.setHint("Limit, GB");
        limitInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        limitInput.setText(String.valueOf(prefs.getInt(DvrArchive.KEY_LIMIT_GB, 5)));
        storagePathInput = new EditText(this);
        storagePathInput.setHint("Storage path");
        storagePathInput.setText(prefs.getString(DvrArchive.KEY_STORAGE_PATH, ""));
        styleInput(camerasInput);
        styleInput(segmentInput);
        styleInput(limitInput);
        styleInput(storagePathInput);

        storageInput = new Spinner(this);
        String[] storage = {DvrArchive.STORAGE_EXTERNAL, DvrArchive.STORAGE_INTERNAL, DvrArchive.STORAGE_USB};
        storageInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, storage));
        String currentStorage = prefs.getString(DvrArchive.KEY_STORAGE, DvrArchive.STORAGE_EXTERNAL);
        for (int i = 0; i < storage.length; i++) if (storage[i].equals(currentStorage)) storageInput.setSelection(i);

        qualityInput = new Spinner(this);
        String[] quality = {DvrArchive.QUALITY_720P, DvrArchive.QUALITY_1080P, DvrArchive.QUALITY_480P};
        qualityInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, quality));
        String currentQuality = prefs.getString(DvrArchive.KEY_QUALITY, DvrArchive.QUALITY_720P);
        for (int i = 0; i < quality.length; i++) if (quality[i].equals(currentQuality)) qualityInput.setSelection(i);

        panel.addView(camerasInput, margin(0, 12, 0, 8));
        LinearLayout numbers = Ui.row(this);
        numbers.addView(segmentInput, buttonLp());
        numbers.addView(limitInput, buttonLp());
        panel.addView(numbers);
        panel.addView(storageInput, margin(0, 8, 0, 8));
        panel.addView(storagePathInput, margin(0, 0, 0, 8));
        panel.addView(qualityInput, margin(0, 0, 0, 12));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Save", this::saveSettings);
        addActionChip(actions, "Refresh", this::refresh);
        panel.addView(actions);
        return panel;
    }

    private LinearLayout buildCapturePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Capture Controls"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addCommandTile(grid, "Start Recording", Ui.SUCCESS, this::startRecording);
        addCommandTile(grid, "Stop Recording", Ui.ERROR, this::stopRecording);
        addCommandTile(grid, "Refresh", Ui.CYAN, this::refresh);
        addCommandTile(grid, "Camera Center", Color.rgb(129, 149, 255), () -> startActivity(new Intent(this, CameraActivity.class)));
        panel.addView(grid, margin(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildBridgePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(238, 12, 18, 32) : Color.argb(244, 236, 241, 247),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "EVS / DVR Bridge"));
        panel.addView(Ui.muted(this, "Legacy raw bridge commands stay available here, but no longer compete with the capture flow above."));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addBridgeTile(grid, "EVS rear open", Ui.WARNING, () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_REAR, true));
        addBridgeTile(grid, "EVS rear close", Color.rgb(120, 136, 156), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_REAR, false));
        addBridgeTile(grid, "EVS 360 open", Color.rgb(129, 149, 255), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_AVM, true));
        addBridgeTile(grid, "EVS 360 close", Color.rgb(120, 136, 156), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_AVM, false));
        addBridgeTile(grid, "EVS DVR open", Color.rgb(95, 133, 255), () -> runEvs(EcarxDvrAdapter.EVS_CAMERA_DVR, true));
        addBridgeTile(grid, "DVR online", Ui.SUCCESS, () -> runDvr("DVR online", a -> a.dvrCameraOnline()));
        addBridgeTile(grid, "DVR capture", Ui.CYAN, () -> runDvr("DVR capture", a -> a.dvrCapture()));
        addBridgeTile(grid, "DVR mode", Color.rgb(88, 190, 172), () -> runDvr("DVR mode", a -> a.dvrCurrentMode()));
        addBridgeTile(grid, "DVR SD", Color.rgb(255, 158, 91), () -> runDvr("DVR SD", a -> a.dvrSdcardStatus()));
        panel.addView(grid, margin(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildStatusPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(118, 255, 255, 255) : Color.argb(232, 255, 255, 255),
                Ui.dp(this, 26),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Status"));
        status = Ui.text(this, "", 14, false);
        status.setTextColor(Ui.primaryText(this));
        status.setTextIsSelectable(true);
        status.setPadding(0, Ui.dp(this, 10), 0, 0);
        panel.addView(status);
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Save", this::saveSettings, false);
        addDockButton(dock, "Start", this::startRecording, false);
        addDockButton(dock, "Stop", this::stopRecording, false);
        addDockButton(dock, "Center", () -> startActivity(new Intent(this, CameraActivity.class)), false);
        addDockButton(dock, "Back", this::finish, false);
        return dock;
    }

    private void saveSettings() {
        DvrArchive.saveSettings(this,
                camerasInput.getText().toString(),
                parseInt(segmentInput.getText().toString(), 60),
                parseInt(limitInput.getText().toString(), 5),
                String.valueOf(storageInput.getSelectedItem()),
                String.valueOf(qualityInput.getSelectedItem()),
                storagePathInput.getText().toString());
        Ui.toast(this, "Настройки DVR сохранены");
    }

    private void startRecording() {
        saveSettings();
        startForegroundService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_START));
        refresh();
    }

    private void stopRecording() {
        startService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_STOP));
        refresh();
    }

    private void runEvs(int cameraId, boolean open) {
        EcarxDvrAdapter adapter = new EcarxDvrAdapter(this);
        EcarxDvrAdapter.Result result = open ? adapter.openEvs(cameraId) : adapter.closeEvs(cameraId);
        Ui.toast(this, result.success ? "EVS команда отправлена" : "EVS ошибка");
        prependStatus(result.message);
    }

    private interface DvrCall {
        EcarxDvrAdapter.Result run(EcarxDvrAdapter adapter);
    }

    private void runDvr(String label, DvrCall call) {
        EcarxDvrAdapter.Result result = call.run(new EcarxDvrAdapter(this));
        Ui.toast(this, result.success ? label + " выполнен" : label + " ошибка");
        prependStatus(result.message);
    }

    private void refresh() {
        StringBuilder sb = new StringBuilder();
        sb.append("Camera2 sources:\n");
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
        sb.append("\nEVS sources: rear, 360, dvr\n");
        sb.append("\nUSB roots:\n").append(usbRootsSummary());
        sb.append("\n").append(DvrArchive.summary(this));
        status.setText(sb.toString());
    }

    private void prependStatus(String text) {
        if (status == null) return;
        status.setText(text + "\n\n" + status.getText());
    }

    private String usbRootsSummary() {
        StringBuilder sb = new StringBuilder();
        File storage = new File("/storage");
        File[] roots = storage.listFiles(file -> file.isDirectory()
                && file.canWrite()
                && !"emulated".equals(file.getName())
                && !"self".equals(file.getName()));
        if (roots == null || roots.length == 0) return "не найдены\n";
        Arrays.sort(roots, Comparator.comparing(File::getAbsolutePath));
        for (File root : roots) sb.append(root.getAbsolutePath()).append("\n");
        return sb.toString();
    }

    private String facingName(Integer facing) {
        if (facing == null) return "unknown";
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "front";
        if (facing == CameraCharacteristics.LENS_FACING_BACK) return "rear";
        if (Build.VERSION.SDK_INT >= 23 && facing == CameraCharacteristics.LENS_FACING_EXTERNAL) return "external";
        return "other";
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String qualityLabel() {
        DvrArchive.Quality quality = DvrArchive.quality(this);
        return quality.width + "x" + quality.height;
    }

    private void styleInput(EditText e) {
        e.setTextColor(Ui.textColor(this));
        e.setHintTextColor(Ui.mutedColor(this));
        e.setSingleLine(true);
        e.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        e.setBackground(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 14), Ui.lineColor(this)));
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        Ui.press(b, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addCommandTile(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16));
        tile.setBackground(Ui.cardBg(this, Color.argb(88, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 22), Color.argb(80, 255, 255, 255)));
        Ui.press(tile, action);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private void addBridgeTile(GridLayout grid, String label, int color, Runnable action) {
        addCommandTile(grid, label, color, action);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : Color.argb(54, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
        Ui.press(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1f);
        lp.setMargins(Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5));
        return lp;
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }

    private static final class CameraPreviewWidget extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        CameraPreviewWidget(Context c) {
            super(c);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Ui.dark(getContext()) ? Color.rgb(24, 29, 34) : Color.rgb(222, 230, 236));
            canvas.drawRoundRect(new RectF(w * .03f, h * .08f, w * .97f, h * .92f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), p);
            int[] colors = {Color.rgb(54, 132, 210), Color.rgb(45, 150, 118), Color.rgb(190, 92, 75)};
            for (int i = 0; i < 3; i++) {
                float left = w * (.08f + i * .29f);
                RectF r = new RectF(left, h * .22f, left + w * .24f, h * .78f);
                p.setColor(colors[i]);
                canvas.drawRoundRect(r, Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), p);
                p.setColor(Color.argb(110, 255, 255, 255));
                canvas.drawCircle(r.centerX(), r.centerY(), Ui.dp(getContext(), 22), p);
            }
        }
    }
}
