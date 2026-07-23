package com.prodject.gflow;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AdbShellActivity extends Activity {
    private EditText commandInput;
    private EditText zoomPackagesInput;
    private EditText zoomScaleInput;
    private TextView outputView;
    private LinearLayout contentHost;
    private String lastOutput = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildShell());
        restoreDefaults();
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        restoreDefaults();
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

        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateIn(dock, 220, 18f);
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildPermissionsPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildShellPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildAdbDpiPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildAutozoomPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildOutputPanel(), lpMatchWrap(0, 0, 0, 16));
        Ui.staggerIn(collectChildren(contentHost), 30, 55);
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.bindPress(back, this::finish);
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "System Access / Shell / Autozoom"));
        titleBlock.addView(Ui.text(this, "ADB / Система", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Write", String.valueOf(Settings.System.canWrite(this))));
        bar.addView(buildTopStat("Файлы", String.valueOf(Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())));
        bar.addView(buildTopStat("Autozoom", String.valueOf(watchdogPrefs().getBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, false))));
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
        hero.addView(Ui.label(this, "System Controls"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Accessibility", String.valueOf(accessibilitySummary())));
        left.addView(metricLine("WRITE_SETTINGS", String.valueOf(Settings.System.canWrite(this))));
        left.addView(metricLine("All files", String.valueOf(Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())));
        left.addView(metricLine("Последнее приложение", watchdogPrefs().getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "")));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout badge = Ui.glassCard(this);
        badge.setGravity(Gravity.CENTER);
        TextView label = Ui.text(this, "SYS", 30, true);
        label.setGravity(Gravity.CENTER);
        badge.addView(label);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        badgeLp.leftMargin = Ui.dp(this, 12);
        row.addView(badge, badgeLp);
        hero.addView(row);

        outputView = Ui.text(this, outputSummary(), 16, true);
        outputView.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(outputView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Shell", this::runCurrentCommand);
        addActionChip(quick, "ADB", this::toggleAdb);
        addActionChip(quick, "DPI 440", () -> execute("wm density 440"));
        addActionChip(quick, "Grants", this::showGrants);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Accessibility", accessibilitySummary(), Ui.CYAN);
        addStatusCard(grid, "WRITE_SETTINGS", String.valueOf(Settings.System.canWrite(this)), Ui.SUCCESS);
        addStatusCard(grid, "All files", String.valueOf(Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()), Ui.WARNING);
        addStatusCard(grid, "Autozoom", autozoomSummary(), Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildPermissionsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Карта разрешений"));
        panel.addView(Ui.text(this, "Accessibility, WRITE_SETTINGS, All files access, app card и grants check.", 14, false));
        panel.addView(Ui.muted(this, "Accessibility: " + accessibilitySummary()), lpMatchWrap(0, 8, 0, 0));
        panel.addView(Ui.muted(this, "WRITE_SETTINGS: " + Settings.System.canWrite(this)), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, "All files: " + (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())), lpMatchWrap(0, 4, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Accessibility", () -> openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        addActionChip(row, "WRITE_SETTINGS", () -> openSettings(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())));
        addActionChip(row, "All files", () -> openSettings(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        addActionChip(row, "App card", () -> openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));

        LinearLayout row2 = Ui.row(this);
        addActionChip(row2, "Проверить grants", this::showGrants);
        panel.addView(row2, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildShellPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Shell"));
        panel.addView(Ui.text(this, "Ввод shell-команды, выполнить, показать результат, скопировать и очистить.", 14, false));

        commandInput = edit("Команда shell", currentCommand());
        panel.addView(commandInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Выполнить", this::runCurrentCommand);
        addActionChip(row, "Копировать", this::copyOutput);
        addActionChip(row, "Очистить", () -> {
            lastOutput = "";
            if (outputView != null) outputView.setText(outputSummary());
            renderContent();
        });
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAdbDpiPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 16, 24, 42) : Color.argb(246, 240, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "ADB / DPI"));
        panel.addView(Ui.text(this, "ADB toggle attempt, DPI shortcut и вывод результата.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "ADB toggle", this::toggleAdb);
        addActionChip(row, "DPI 440", () -> execute("wm density 440"));
        addActionChip(row, "ADB state", () -> execute("settings get global adb_enabled"));
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAutozoomPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 14, 21, 38) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Autozoom"));
        panel.addView(Ui.text(this, "Packages, scale, save autozoom и autozoom on/off.", 14, false));

        zoomPackagesInput = edit("Packages", watchdogPrefs().getString(AppWatchdogAccessibilityService.KEY_PACKAGES, "maps,navi,browser"));
        zoomScaleInput = edit("Scale", String.valueOf(watchdogPrefs().getFloat(AppWatchdogAccessibilityService.KEY_SCALE, 1.15f)));
        zoomScaleInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        panel.addView(zoomPackagesInput);
        panel.addView(zoomScaleInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сохранить Autozoom", this::saveAutozoomNow);
        addActionChip(row, "Autozoom вкл/выкл", this::toggleAutozoom);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildOutputPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(238, 12, 18, 32) : Color.argb(244, 236, 241, 247),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Output"));
        TextView out = Ui.text(this, lastOutput.isEmpty() ? "Результат shell и системных действий появится здесь." : lastOutput, 14, false);
        out.setTextColor(Ui.primaryText(this));
        out.setTextIsSelectable(true);
        out.setPadding(0, Ui.dp(this, 10), 0, 0);
        panel.addView(out);
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Shell", this::runCurrentCommand, false);
        addDockButton(dock, "Права", this::showGrants, false);
        addDockButton(dock, "ADB", this::toggleAdb, false);
        addDockButton(dock, "Autozoom", this::toggleAutozoom, false);
        addDockButton(dock, "Назад", this::finish, false);
        return dock;
    }

    private void restoreDefaults() {
        if (commandInput != null && commandInput.getText().toString().trim().isEmpty()) {
            commandInput.setText(currentCommand());
        }
    }

    private SharedPreferences watchdogPrefs() {
        return getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE);
    }

    private String currentCommand() {
        return commandInput == null ? "settings get global adb_enabled" : commandInput.getText().toString();
    }

    private void runCurrentCommand() {
        execute(currentCommand());
    }

    private void toggleAdb() {
        try {
            int current = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, current == 1 ? 0 : 1);
            lastOutput = "ADB: " + (current == 1 ? "выключен" : "включен");
            renderContent();
        } catch (Exception e) {
            lastOutput = "Нужен WRITE_SECURE_SETTINGS/adb-grants: " + e.getMessage();
            renderContent();
        }
    }

    private void execute(String cmd) {
        String clean = cmd == null ? "" : cmd.trim();
        if (clean.isEmpty()) {
            lastOutput = "Команда пустая";
            renderContent();
            return;
        }
        lastOutput = "Выполняю: " + clean;
        renderContent();
        new Thread(() -> {
            try {
                Process process = new ProcessBuilder("sh", "-c", clean).redirectErrorStream(true).start();
                String text = read(process.getInputStream());
                int code = process.waitFor();
                runOnUiThread(() -> {
                    lastOutput = "$ " + clean + "\nexit " + code + "\n" + text;
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    lastOutput = "Ошибка: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void showGrants() {
        String pkg = getPackageName();
        lastOutput = "ADB grants для " + pkg + ":\n"
                + "adb shell pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS\n"
                + "adb shell appops set " + pkg + " GET_USAGE_STATS allow\n"
                + "adb shell appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow\n"
                + "adb shell appops set " + pkg + " android:system_alert_window allow\n"
                + "adb shell settings get global adb_enabled\n\n"
                + "Проверка внутри приложения:\n"
                + "WRITE_SETTINGS=" + Settings.System.canWrite(this) + "\n"
                + "MANAGE_EXTERNAL_STORAGE=" + (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()) + "\n"
                + "Accessibility last package=" + watchdogPrefs().getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "") + "\n"
                + "SDK=" + Build.VERSION.SDK_INT + "\n"
                + "Autozoom enabled=" + watchdogPrefs().getBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, false);
        renderContent();
    }

    private void openSettings(String action) {
        openSettings(action, null);
    }

    private void openSettings(String action, Uri data) {
        try {
            Intent intent = new Intent(action);
            if (data != null) intent.setData(data);
            startActivity(intent);
        } catch (Exception e) {
            lastOutput = "Не удалось открыть настройки: " + e.getMessage();
            renderContent();
        }
    }

    private void saveAutozoomNow() {
        saveAutozoom(zoomPackagesInput == null ? "" : zoomPackagesInput.getText().toString(),
                zoomScaleInput == null ? "" : zoomScaleInput.getText().toString());
    }

    private void saveAutozoom(String packages, String scale) {
        float value = parseFloat(scale, 1.15f);
        watchdogPrefs().edit()
                .putString(AppWatchdogAccessibilityService.KEY_PACKAGES, packages)
                .putFloat(AppWatchdogAccessibilityService.KEY_SCALE, Math.max(0.85f, Math.min(1.6f, value)))
                .apply();
        lastOutput = "Autozoom packages сохранены: " + packages + "\nscale=" + Math.max(0.85f, Math.min(1.6f, value));
        renderContent();
    }

    private void toggleAutozoom() {
        SharedPreferences prefs = watchdogPrefs();
        boolean next = !prefs.getBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, false);
        prefs.edit().putBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, next).apply();
        lastOutput = "Autozoom: " + (next ? "включен" : "выключен") + "\nПоследний пакет: " + prefs.getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "");
        renderContent();
    }

    private void copyOutput() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("adb_output", lastOutput));
            Ui.toast(this, "Вывод скопирован");
        }
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }

    private String accessibilitySummary() {
        return watchdogPrefs().getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "").isEmpty() ? "inactive" : "active";
    }

    private String autozoomSummary() {
        SharedPreferences prefs = watchdogPrefs();
        return prefs.getBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, false)
                ? prefs.getString(AppWatchdogAccessibilityService.KEY_PACKAGES, "")
                : "off";
    }

    private String outputSummary() {
        return lastOutput.isEmpty() ? "Shell result / grants / system actions" : lastOutput;
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
        Ui.bindPress(b, action);
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
        Ui.bindPress(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(118, 255, 255, 255) : Color.argb(232, 255, 255, 255),
                Ui.dp(this, 26),
                Ui.glassLine(this)));
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

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }
}
