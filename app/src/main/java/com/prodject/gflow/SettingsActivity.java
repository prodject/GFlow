package com.prodject.gflow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class SettingsActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String KEY_ACCENT = "accent_color";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_START_SCREEN = "start_screen";
    private static final int REQ_CREATE_BACKUP = 4101;
    private static final int REQ_RESTORE_BACKUP = 4102;

    private LinearLayout contentHost;
    private String releaseState = "Источник: github.com/prodject/GFlow/releases";
    private String diagnosticsState = "Проверяет availability и полный support/readback по функциям, уже выведенным в проекте, и формирует лог.";
    private String systemState = "Backup / restore / reset приложения.";
    private final String[] latestApkUrl = {""};
    private byte[] pendingBackupBytes;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        latestApkUrl[0] = prefs().getString("latest_apk_url", "");
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        latestApkUrl[0] = prefs().getString("latest_apk_url", "");
        renderContent();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_CREATE_BACKUP) writeBackupToUri(uri);
        else if (requestCode == REQ_RESTORE_BACKUP) restoreBackupFromUri(uri);
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
        contentHost.addView(buildGeneralPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildSystemPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildUpdatesPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildDiagnosticsPanel(), lpMatchWrap(0, 0, 0, 16));
        Ui.staggerIn(collectChildren(contentHost), 40, 70);
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
        titleBlock.addView(Ui.label(this, "General / Updates / Diagnostics"));
        titleBlock.addView(Ui.text(this, "Settings", 28, true));
        TextView subtitle = Ui.muted(this, "Daily configuration first. Risky system actions and diagnostics stay deeper.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Experimental", String.valueOf(experimentalFeaturesEnabled())));
        bar.addView(buildTopStat("Developer", String.valueOf(developerModeEnabled())));
        bar.addView(buildTopStat("Theme", prefs().getString(KEY_THEME_MODE, "auto")));
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
        hero.addView(Ui.label(this, "Settings Overview"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(buildHeroMetric("Experimental", String.valueOf(experimentalFeaturesEnabled())));
        left.addView(buildHeroMetric("Developer", String.valueOf(developerModeEnabled())));
        left.addView(buildHeroMetric("Theme", prefs().getString(KEY_THEME_MODE, "auto")));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.VERTICAL);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(Ui.cardBg(this, Color.argb(58, 255, 255, 255), Ui.dp(this, 32), Color.argb(34, 255, 255, 255)));
        TextView label = Ui.text(this, "SET", 28, true);
        label.setGravity(Gravity.CENTER);
        badge.addView(label);
        TextView badgeHint = Ui.muted(this, "Safe defaults");
        badgeHint.setGravity(Gravity.CENTER);
        badge.addView(badgeHint);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        badgeLp.leftMargin = Ui.dp(this, 12);
        row.addView(badge, badgeLp);
        hero.addView(row);

        TextView state = Ui.text(this, "Start: " + prefs().getString(KEY_START_SCREEN, "Главная") + "\n" + systemState + "\n" + releaseState, 15, true);
        state.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(state);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Experimental", () -> toggleBoolean(KEY_EXPERIMENTAL_FEATURES));
        addActionChip(quick, "Developer", () -> toggleBoolean(KEY_DEVELOPER_MODE));
        addActionChip(quick, "Theme", () -> cycleChoice(KEY_THEME_MODE, new String[]{"dark", "light", "auto"}));
        addActionChip(quick, "Backup", this::startBackupFlow);
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
        addStatusCard(grid, "Experimental", String.valueOf(experimentalFeaturesEnabled()), Ui.CYAN);
        addStatusCard(grid, "Developer", String.valueOf(developerModeEnabled()), Ui.SUCCESS);
        addStatusCard(grid, "Accent", prefs().getString(KEY_ACCENT, "blue"), Ui.WARNING);
        addStatusCard(grid, "Start screen", prefs().getString(KEY_START_SCREEN, "Главная"), Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildGeneralPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "General Settings"));
        panel.addView(Ui.text(this, "Главный everyday-layer: внешность, стартовый сценарий и feature gates без ощущения developer-console.", 14, false));

        CheckBox experimental = new CheckBox(this);
        experimental.setText("Experimental features");
        experimental.setChecked(experimentalFeaturesEnabled());
        experimental.setOnCheckedChangeListener((buttonView, isChecked) -> prefs().edit().putBoolean(KEY_EXPERIMENTAL_FEATURES, isChecked).apply());
        panel.addView(experimental);

        CheckBox developer = new CheckBox(this);
        developer.setText("Developer diagnostics");
        developer.setChecked(developerModeEnabled());
        developer.setOnCheckedChangeListener((buttonView, isChecked) -> prefs().edit().putBoolean(KEY_DEVELOPER_MODE, isChecked).apply());
        panel.addView(developer);

        panel.addView(buildChoiceRow("Theme", KEY_THEME_MODE, new String[]{"dark", "light", "auto"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoiceRow("Accent", KEY_ACCENT, new String[]{"blue", "amber", "green"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoiceRow("Start screen", KEY_START_SCREEN, new String[]{"Главная", "Климат", "Автомобиль", "Рабочий стол"}), lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildSystemPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(36, 10, 14, 20), Ui.dp(this, 28), Color.argb(48, 255, 179, 64)));
        panel.addView(Ui.label(this, "System / Recovery"));
        panel.addView(Ui.text(this, "Резервная копия всех настроек/профилей/сценариев в JSON, восстановление из файла и полная очистка приложения с последующим uninstall-flow.", 14, false));
        panel.addView(Ui.muted(this, systemState), lpMatchWrap(0, 8, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сделать backup", this::startBackupFlow);
        addActionChip(row, "Восстановить", this::startRestoreFlow);
        addActionChip(row, "Полная очистка", this::confirmFullReset);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildChoiceRow(String title, String key, String[] items) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(28, 255, 255, 255), Ui.dp(this, 24), Color.argb(22, 255, 255, 255)));
        card.addView(Ui.text(this, title + ": " + prefs().getString(key, items[items.length - 1]), 16, true));
        LinearLayout row = Ui.row(this);
        for (String item : items) {
            Button button = Ui.button(this, item);
            button.setTextSize(13);
            button.setOnClickListener(v -> {
                Ui.press(v);
                prefs().edit().putString(key, item).apply();
                renderContent();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
            lp.leftMargin = Ui.dp(this, 6);
            lp.rightMargin = Ui.dp(this, 6);
            row.addView(button, lp);
        }
        card.addView(row, lpMatchWrap(0, 10, 0, 0));
        return card;
    }

    private LinearLayout buildUpdatesPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        panel.setBackground(Ui.cardBg(this, Color.argb(24, 255, 255, 255), Ui.dp(this, 24), Color.argb(20, 255, 255, 255)));
        panel.addView(Ui.label(this, "Updates"));
        panel.addView(Ui.text(this, "Проверить GitHub releases, найти APK asset, скачать APK и установить скачанный APK.", 14, false));
        panel.addView(Ui.muted(this, releaseState), lpMatchWrap(0, 8, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Проверить", this::checkRelease);
        addActionChip(row, "Скачать APK", () -> downloadReleaseApk(latestApkUrl[0]));
        addActionChip(row, "Установить", this::installDownloadedApk);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildDiagnosticsPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        panel.setBackground(Ui.cardBg(this, Color.argb(24, 255, 255, 255), Ui.dp(this, 24), Color.argb(20, 255, 255, 255)));
        panel.addView(Ui.label(this, "Auto Diagnostics"));
        panel.addView(Ui.text(this, "Проверка availability и support/readback по всем основным функциям, уже добавленным в новый UI: HVAC, кузов, drive, ADAS, parking, HUD, ambience, daymode, AVAS, digital key и seat.", 14, false));
        panel.addView(Ui.muted(this, diagnosticsState), lpMatchWrap(0, 8, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Запустить", this::runAutoDiagnostics);
        addActionChip(row, "Share log", this::shareDiagnosticsIfExists);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "General", this::renderContent, true);
        addDockButton(dock, "System", this::startBackupFlow, false);
        addDockButton(dock, "Updates", this::checkRelease, false);
        addDockButton(dock, "Diagnostics", this::runAutoDiagnostics, false);
        addDockButton(dock, "Back", this::finish, false);
        Ui.animateIn(dock, 150, 10f);
        return dock;
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
    }

    private boolean experimentalFeaturesEnabled() {
        return prefs().getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private boolean developerModeEnabled() {
        return prefs().getBoolean(KEY_DEVELOPER_MODE, false);
    }

    private void toggleBoolean(String key) {
        boolean next = !prefs().getBoolean(key, false);
        prefs().edit().putBoolean(key, next).apply();
        renderContent();
    }

    private void cycleChoice(String key, String[] values) {
        String current = prefs().getString(key, values[0]);
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                index = i;
                break;
            }
        }
        String next = values[(index + 1) % values.length];
        prefs().edit().putString(key, next).apply();
        renderContent();
    }

    private void checkRelease() {
        releaseState = "Проверяю releases...";
        renderContent();
        new Thread(() -> {
            try {
                JSONArray arr = new JSONArray(readUrl("https://api.github.com/repos/prodject/GFlow/releases"));
                JSONObject release = arr.getJSONObject(0);
                String tag = release.optString("tag_name");
                JSONArray assets = release.optJSONArray("assets");
                String url = "";
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        if (asset.optString("name").endsWith(".apk")) url = asset.optString("browser_download_url");
                    }
                }
                String finalUrl = url;
                prefs().edit().putString("latest_apk_url", finalUrl).apply();
                latestApkUrl[0] = finalUrl;
                runOnUiThread(() -> {
                    releaseState = "Последний релиз: " + tag + "\nAPK: " + (finalUrl.isEmpty() ? "не найден" : finalUrl);
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    releaseState = "Ошибка проверки: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void downloadReleaseApk(String url) {
        if (url == null || url.trim().isEmpty()) {
            releaseState = "Сначала нажмите Проверить.";
            renderContent();
            return;
        }
        releaseState = "Скачиваю APK...";
        renderContent();
        new Thread(() -> {
            File out = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
            try (InputStream in = new URL(url).openStream(); OutputStream file = new FileOutputStream(out)) {
                byte[] buf = new byte[1024 * 64];
                for (int n; (n = in.read(buf)) > 0; ) file.write(buf, 0, n);
                runOnUiThread(() -> {
                    releaseState = "APK загружен: " + out.getAbsolutePath();
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    releaseState = "Ошибка загрузки: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void installDownloadedApk() {
        File apk = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
        if (!apk.exists()) {
            releaseState = "APK еще не загружен.";
            renderContent();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void runAutoDiagnostics() {
        diagnosticsState = "Диагностика выполняется...";
        renderContent();
        new Thread(() -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder log = new StringBuilder();
            log.append("GFlow auto diagnostics\n").append(new Date()).append("\n\n");
            log.append(adapter.availability()).append("\n\n");
            LinkedHashMap<String, int[]> groups = buildDiagnosticsGroups();
            int total = 0;
            for (Map.Entry<String, int[]> entry : groups.entrySet()) {
                log.append("== ").append(entry.getKey()).append(" ==\n");
                for (int id : entry.getValue()) {
                    total++;
                    log.append(adapter.support(id).message).append("\n");
                    log.append(adapter.get(id).message).append("\n\n");
                }
            }
            appendAdvancedDiagnostics(log);
            final int totalCount = total;
            try {
                File file = new File(getCacheDir(), "gflow-diagnostics.txt");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(log.toString().getBytes("UTF-8"));
                }
                runOnUiThread(() -> {
                    diagnosticsState = "Лог готов: " + file.getAbsolutePath() + " · " + totalCount + " function IDs";
                    renderContent();
                    shareFile(file);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    diagnosticsState = "Ошибка лога: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void shareDiagnosticsIfExists() {
        File file = new File(getCacheDir(), "gflow-diagnostics.txt");
        if (!file.exists()) {
            Ui.toast(this, "Сначала запустите автодиагностику");
            return;
        }
        shareFile(file);
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Сохранить или отправить лог"));
    }

    private void appendAdvancedDiagnostics(StringBuilder log) {
        appendSection(log, "Parking Signals", collectParkingSignalsDiagnostics());
        appendSection(log, "Parking HAL", collectParkingHalDiagnostics());
        appendSection(log, "HUD / DIM", collectHudDimDiagnostics());
        appendSection(log, "AudioExt", collectAudioExtDiagnostics());
        appendSection(log, "DVR / EVS", collectDvrDiagnostics());
        appendSection(log, "Camera2 Inventory", collectCameraInventoryDiagnostics());
        appendSection(log, "OneOS Dock", collectDockDiagnostics());
        appendSection(log, "ControlBoard", collectControlBoardDiagnostics());
    }

    private void appendSection(StringBuilder log, String title, String body) {
        log.append("== ").append(title).append(" ==\n");
        log.append(body == null || body.trim().isEmpty() ? "No data\n\n" : body.trim() + "\n\n");
    }

    private LinkedHashMap<String, int[]> buildDiagnosticsGroups() {
        LinkedHashMap<String, int[]> groups = new LinkedHashMap<>();
        groups.put("HVAC Core", resolveFunctionIds(
                "HVAC_POWER", "HVAC_AUTO", "HVAC_AC", "HVAC_FAN_SPEED", "HVAC_TEMP",
                "HVAC_TEMP_UNIT", "HVAC_CLIMATE_ZONE", "HVAC_DEFROST_FRONT", "HVAC_DEFROST_FRONT_MAX",
                "HVAC_DEFROST_REAR", "HVAC_SEAT_HEATING", "HVAC_SEAT_VENTILATION",
                "HVAC_STEERING_WHEEL_HEAT", "HVAC_IONS_SWITCH", "HVAC_AQS_SWITCH",
                "HVAC_PRE_CLIMATISATION", "HVAC_POST_CLIMATISATION", "HVAC_CO2_SWITCH",
                "HVAC_RAPID_COOLING", "HVAC_RAPID_WARMING", "HVAC_AUTOMATIC_VENTILATION_DRY",
                "HVAC_AIR_FRAGRANCE"
        ));
        groups.put("Vehicle Body", resolveFunctionIds(
                "BCM_WINDOW", "BCM_DOOR", "BCM_DOOR_LOCK", "BCM_DOOR_STATUS",
                "BCM_SUNROOF_OPEN", "BCM_MIRROR_FOLD", "BCM_LIGHT_DIPPED_BEAM", "BCM_LIGHT_GRILLE"
        ));
        groups.put("Drive / Cluster", resolveFunctionIds(
                "DRIVE_MODE_SELECT", "DRIVE_CUSTOM_PROPULSION", "DRIVE_CUSTOM_SUSPENSION",
                "DRIVE_CUSTOM_STEERING_FEEL", "DRIVE_CUSTOM_CLIMATE", "DRIVE_DIM_THEME_SET",
                "DRIVE_ENERGY_MODE", "DRIVE_CREEP_SET", "DRIVE_LAUNCH_CONTROL",
                "DRIVE_NOISE_CONTROL", "DRIVE_ESC_LEVEL", "DRIVE_STARTRACK_MODE",
                "DRIVE_PERFORMANCE_SAVING", "DRIVE_POWER_TRAIN_STOP"
        ));
        groups.put("ADAS", resolveFunctionIds(
                "ADAS_AEB", "ADAS_FCW", "ADAS_LKA", "ADAS_LDW", "ADAS_RCW", "ADAS_ELKA",
                "ADAS_ACC_ICC_SWITCH", "ADAS_ACC_TIME_GAP", "ADAS_ACC_WITH_TSR", "ADAS_PDC",
                "ADAS_PDC_WARNING_VOLUME", "ADAS_DRIVE_PILOT", "ADAS_DRIVE_PILOT_STATUS",
                "ADAS_DRIVE_PILOT_ALARM_INFO", "ADAS_DRIVE_PILOT_ACC_LCC_SWITCH",
                "ADAS_DRIVE_NZP_STATUS", "ADAS_MAX_CRUISING_SPEED", "ADAS_APB_MODE",
                "ADAS_TRAFFIC_LIGHT_ATTENTION", "ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND",
                "ADAS_PADDLE_LANE_CHANGE_ASSIST", "ADAS_SPEED_LIMIT_WARNING_MODE",
                "ADAS_ADAPTIVE_CRUISE_FAILURE", "ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE",
                "ADAS_EMERGENCY_STEERING_FAILURE", "ADAS_FORWARD_PRECOLLISION_FAULT",
                "ADAS_FRONT_SIDE_ASSIST_FAILURE", "ADAS_LANE_KEEPING_ASSISTANCE_FAILURE",
                "ADAS_REAR_COLLISION_WARNING_FAILURE", "ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE"
        ));
        groups.put("ADAS Experimental", resolveFunctionIds(
                "ADAS_AI_DRIVER_ASSIST", "ADAS_AI_ASSIST_DEFAULT_ON", "ADAS_AI_ASSIST_FUSION_NAVI",
                "ADAS_AI_ASSIST_OUT_OVERTAKING_LANE", "ADAS_AI_LANE_CHANGE_STRATEGY",
                "ADAS_AI_LANE_CHANGE_CONFIRM", "ADAS_AI_LANE_CHANGE_WARNING",
                "ADAS_APB_SWITCH", "ADAS_TLB_SWITCH", "ADAS_TLB_MODE",
                "ADAS_TTS_ACC_ACTIVATE", "ADAS_TTS_ACC_ACTIVATE_SOUND", "ADAS_TTS_ACC_EXIT",
                "ADAS_TTS_ICC_ACTIVATE", "ADAS_TTS_ICC_ACTIVATE_REMINDER",
                "ADAS_TTS_ICC_ACTIVATE_SOUND", "ADAS_TTS_ICC_DRIVING_STATUS",
                "ADAS_TTS_ICC_EXIT", "ADAS_TTS_ICC_NOA_DRIVING_STATUS",
                "ADAS_DRIVER_FATIGUE_FAILURE", "ADAS_TRAFFIC_LIGHTS_IDENTIFY_FAULTS"
        ));
        groups.put("Parking / APA / AVM", resolveFunctionIds(
                "PAS_ACTIVATED", "PAS_STATUS", "PAS_SHOW_GRAPHICS", "PAS_RADAR_FRONT_CENTER",
                "PAS_RADAR_REAR_CENTER", "PAS_RADAR_WORK_MODE", "PAS_RADAR_WORK_STATUS",
                "PAS_PAC_ACTIVATION", "PAS_PAC_STATUS", "PAS_PAC_AUTO_REVERSE_CAMERA",
                "PAS_PAC_VIEW_SELECTION", "PAS_PAC_3DVIEW_POSITION", "PAS_PAC_OVERLAY_STEERPATH",
                "PAS_PAC_OVERLAY_TOWBAR", "PAS_PAC_OVERLAY_DSTINFO", "PAS_PAC_CAR_MODE_TRANSPARENT",
                "PAS_PAC_TOP_VIEW_ZOOM_IN", "PAS_PAC_TOURING_VIEW", "PAS_SAP_ACTIVATION",
                "PAS_SAP_PARK_TYPE", "PAS_SAP_PARK_IN_TYPE", "PAS_RCTA_ACTIVATION",
                "PAS_RCTA_LEFT_WARNING", "PAS_RCTA_RIGHT_WARNING", "PAS_RCTA_WARNING_VOLUME",
                "PAS_AVM_OR_APA_ACTIVATION"
        ));
        groups.put("HUD / OneOS", resolveFunctionIds(
                "HUD_ACTIVE", "HUD_DISPLAY_SAFETY", "HUD_DISPLAY_MEDIA", "HUD_DISPLAY_NAVI",
                "HUD_DISPLAY_BTPHONE", "HUD_DISPLAY_DRIVE_ENVIRONMENT"
        ));
        groups.put("Ambience / DayMode", resolveFunctionIds(
                "AMBIENCE_LIGHT_THEME_COLOR", "AMBIENCE_LIGHT_EFFECT", "AMBIENCE_LIGHT_CONTROL_MODE",
                "AMBIENCE_LIGHT_MUSIC", "AMBIENCE_LIGHT_MUSIC_SHOW_MODE", "AMBIENCE_LIGHT_WELCOME_SHOW",
                "AMBIENCE_LIGHT_WELCOME_SHOW_MODE", "AMBIENCE_LIGHT_VOICE", "AMBIENCE_LIGHT_ZONE_EXPERIENCE",
                "AMBIENCE_LIGHT_MAIN_ZONES", "AMBIENCE_LIGHT_TOP_ZONES", "AMBIENCE_LIGHT_BOT_ZONES",
                "AMBIENCE_LIGHT_COLOR_WEATHER", "DAYMODE_SETTING", "DAYMODE_SYNC",
                "DAYMODE_BRIGHTNESS_DAY", "DAYMODE_BRIGHTNESS_NIGHT", "DAYMODE_BRIGHTNESS_MAX",
                "DAYMODE_BRIGHTNESS_MIN", "DAYMODE_BRIGHTNESS_STEP", "DAYMODE_BACKLIGHT_LINKAGE",
                "DAYMODE_BACKLIGHT_BRIGHTNESS", "DAYMODE_DIM_BRIGHTNESS", "DAYMODE_FLOODLIGHT_BRIGHTNESS",
                "DAYMODE_ELECTRIC_REAR_VIEW_MIRROR", "DAYMODE_BRIGHTNESS_SCREEN", "DAYMODE_CUSTOM_DAY_TIME",
                "DAYMODE_CUSTOM_NIGHT_TIME", "DAYMODE_SUN_TIME", "DAYMODE_TIME_CONTROL_THEME_SWITCH",
                "DAYMODE_PSD_BRIGHTNESS_DAYMODE", "DAYMODE_PSD_BRIGHTNESS_SCREEN"
        ));
        groups.put("AVAS / Digital Key / Seat", resolveFunctionIds(
                "VEHICLE_AVAS_SWITCH", "VEHICLE_AVAS_VOLUME", "VEHICLE_AVAS_SOUND_TYPE",
                "VEHICLE_AVAS_SOUND_TYPE_NAME", "VEHICLE_AVAS_SOUND_TYPE_PATH",
                "VEHICLE_DIGITAL_KEY", "VEHICLE_DIGITAL_KEY_REQ_STS", "VEHICLE_DIGITAL_KEY_UNPAIR",
                "VEHICLE_DIGITAL_KEY_TERMINATION", "VEHICLE_DIGITAL_KEY_SUSPENSION",
                "VEHICLE_DIGITAL_KEY_PAIRING_FAILED", "VEHICLE_DIGITAL_KEY_TRACKING_WAIT",
                "VEHICLE_DIGITAL_KEY_TRACKING_RESULT", "VEHICLE_DIGITAL_KEY_RES_TIMEOUT",
                "SEAT_LENGTH", "SEAT_HEIGHT", "SEAT_BACKREST", "SEAT_POSITION_SAVE",
                "SEAT_POSITION_SET", "SEAT_RESTORE", "SEAT_ONE_KEY_COMFORT"
        ));
        return groups;
    }

    private int[] resolveFunctionIds(String... fieldNames) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        List<String> missing = new ArrayList<>();
        for (String fieldName : fieldNames) {
            try {
                Field field = EcarxVehicleAdapter.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                values.add(field.getInt(null));
            } catch (Exception e) {
                missing.add(fieldName);
            }
        }
        if (!missing.isEmpty()) {
            android.util.Log.w("GFlow", "Diagnostics fields missing: " + missing);
        }
        int[] result = new int[values.size()];
        int index = 0;
        for (Integer value : values) result[index++] = value;
        return result;
    }

    private String collectParkingSignalsDiagnostics() {
        StringBuilder sb = new StringBuilder();
        CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
        Object[] pairs = {
                "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS
        };
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            sb.append(adapter.get(String.valueOf(pairs[i]), (Integer) pairs[i + 1]).message).append("\n");
        }
        return sb.toString();
    }

    private String collectParkingHalDiagnostics() {
        StringBuilder sb = new StringBuilder();
        CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
        int[] properties = {
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_REQ_RESP,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_ON_OFF1,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_UINT8,
                CarSignalManagerAdapter.VEH_PUSH_APA_INFO_REQ
        };
        for (int property : properties) {
            sb.append(adapter.rawHalProperty(property, "VehiclePropertyVEH2").message).append("\n");
        }
        return sb.toString();
    }

    private String collectHudDimDiagnostics() {
        StringBuilder sb = new StringBuilder();
        EcarxHudDimAdapter adapter = new EcarxHudDimAdapter(this);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.hudStatus().message).append("\n");
        sb.append(adapter.hudSync().message).append("\n");
        sb.append(adapter.dimStatus().message).append("\n");
        sb.append(adapter.dimMenuReadyAndTheme().message).append("\n");
        return sb.toString();
    }

    private String collectAudioExtDiagnostics() {
        StringBuilder sb = new StringBuilder();
        AudioExtServiceAdapter adapter = new AudioExtServiceAdapter(this);
        sb.append(adapter.bindAudioExt().message).append("\n");
        sb.append(adapter.visualizerStatus().message).append("\n");
        return sb.toString();
    }

    private String collectDvrDiagnostics() {
        StringBuilder sb = new StringBuilder();
        EcarxDvrAdapter adapter = new EcarxDvrAdapter(this);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_REAR).message).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_AVM).message).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_DVR).message).append("\n");
        sb.append(adapter.dvrCameraOnline().message).append("\n");
        sb.append(adapter.dvrCapture().message).append("\n");
        sb.append(adapter.dvrCurrentMode().message).append("\n");
        sb.append(adapter.dvrSdcardStatus().message).append("\n");
        return sb.toString();
    }

    private String collectCameraInventoryDiagnostics() {
        StringBuilder sb = new StringBuilder();
        try {
            CameraManager manager = getSystemService(CameraManager.class);
            if (manager == null) return "CameraManager unavailable";
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(id);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                sb.append("camera2:").append(id).append(" · ").append(facingName(facing)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Camera2 error: ").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        }
        sb.append("EVS: rear, 360, dvr");
        return sb.toString();
    }

    private String collectDockDiagnostics() {
        StringBuilder sb = new StringBuilder();
        EcarxDockAdapter adapter = new EcarxDockAdapter(this);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.deviceStatus().message).append("\n");
        return sb.toString();
    }

    private String collectControlBoardDiagnostics() {
        return new EcarxControlBoardAdapter(this).availability();
    }

    private String facingName(Integer facing) {
        if (facing == null) return "unknown";
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "front";
        if (facing == CameraCharacteristics.LENS_FACING_BACK) return "rear";
        if (android.os.Build.VERSION.SDK_INT >= 23 && facing == CameraCharacteristics.LENS_FACING_EXTERNAL) return "external";
        return "other";
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            connection.disconnect();
        }
    }

    private void startBackupFlow() {
        try {
            pendingBackupBytes = buildBackupJson().toString(2).getBytes("UTF-8");
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "gflow-backup-" + System.currentTimeMillis() + ".json");
            startActivityForResult(intent, REQ_CREATE_BACKUP);
        } catch (Exception e) {
            systemState = "Ошибка backup: " + e.getMessage();
            renderContent();
        }
    }

    private void startRestoreFlow() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_RESTORE_BACKUP);
    }

    private void writeBackupToUri(Uri uri) {
        if (pendingBackupBytes == null || uri == null) return;
        try (OutputStream out = getContentResolver().openOutputStream(uri, "w")) {
            if (out == null) throw new IOException("Не удалось открыть файл");
            out.write(pendingBackupBytes);
            out.flush();
            systemState = "Backup сохранен: " + uri;
            pendingBackupBytes = null;
            renderContent();
        } catch (Exception e) {
            systemState = "Ошибка записи backup: " + e.getMessage();
            renderContent();
        }
    }

    private void restoreBackupFromUri(Uri uri) {
        new Thread(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                if (in == null) throw new IOException("Не удалось открыть backup");
                byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
                JSONObject root = new JSONObject(out.toString("UTF-8"));
                applyBackupJson(root);
                runOnUiThread(() -> {
                    systemState = "Backup восстановлен: " + uri;
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    systemState = "Ошибка восстановления: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private JSONObject buildBackupJson() throws Exception {
        JSONObject root = new JSONObject();
        root.put("format", "gflow-backup");
        root.put("version", 1);
        root.put("package", getPackageName());
        root.put("createdAt", System.currentTimeMillis());
        JSONObject prefsRoot = new JSONObject();
        File dir = new File(getApplicationInfo().dataDir, "shared_prefs");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().length() - 4);
                SharedPreferences sp = getSharedPreferences(name, MODE_PRIVATE);
                JSONObject prefsJson = new JSONObject();
                for (Map.Entry<String, ?> entry : sp.getAll().entrySet()) {
                    prefsJson.put(entry.getKey(), encodeValue(entry.getValue()));
                }
                prefsRoot.put(name, prefsJson);
            }
        }
        root.put("sharedPrefs", prefsRoot);
        return root;
    }

    private JSONObject encodeValue(Object value) throws Exception {
        JSONObject obj = new JSONObject();
        if (value instanceof Boolean) {
            obj.put("type", "boolean");
            obj.put("value", value);
        } else if (value instanceof Integer) {
            obj.put("type", "int");
            obj.put("value", value);
        } else if (value instanceof Long) {
            obj.put("type", "long");
            obj.put("value", value);
        } else if (value instanceof Float) {
            obj.put("type", "float");
            obj.put("value", value);
        } else if (value instanceof Set) {
            obj.put("type", "string_set");
            JSONArray arr = new JSONArray();
            for (Object item : (Set<?>) value) arr.put(String.valueOf(item));
            obj.put("value", arr);
        } else {
            obj.put("type", "string");
            obj.put("value", value == null ? "" : String.valueOf(value));
        }
        return obj;
    }

    private void applyBackupJson(JSONObject root) throws Exception {
        JSONObject prefsRoot = root.getJSONObject("sharedPrefs");
        clearAllSharedPrefs();
        JSONArray names = prefsRoot.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String prefsName = names.getString(i);
            JSONObject prefsJson = prefsRoot.getJSONObject(prefsName);
            SharedPreferences.Editor editor = getSharedPreferences(prefsName, MODE_PRIVATE).edit().clear();
            JSONArray keys = prefsJson.names();
            if (keys != null) {
                for (int j = 0; j < keys.length(); j++) {
                    String key = keys.getString(j);
                    JSONObject value = prefsJson.getJSONObject(key);
                    applyValue(editor, key, value);
                }
            }
            editor.apply();
        }
    }

    private void applyValue(SharedPreferences.Editor editor, String key, JSONObject value) throws Exception {
        String type = value.optString("type", "string");
        if ("boolean".equals(type)) editor.putBoolean(key, value.getBoolean("value"));
        else if ("int".equals(type)) editor.putInt(key, value.getInt("value"));
        else if ("long".equals(type)) editor.putLong(key, value.getLong("value"));
        else if ("float".equals(type)) editor.putFloat(key, (float) value.getDouble("value"));
        else if ("string_set".equals(type)) {
            JSONArray arr = value.getJSONArray("value");
            java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
            for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
            editor.putStringSet(key, set);
        } else editor.putString(key, value.optString("value", ""));
    }

    private void clearAllSharedPrefs() {
        File dir = new File(getApplicationInfo().dataDir, "shared_prefs");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - 4);
            getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply();
        }
    }

    private void confirmFullReset() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Полная очистка приложения")
                .setMessage("Будут удалены все настройки, профили, сценарии, логи, кэш и внутренние файлы. После этого откроется uninstall-flow приложения.")
                .setPositiveButton("Очистить", (d, w) -> performFullReset())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void performFullReset() {
        new Thread(() -> {
            try {
                clearAllSharedPrefs();
                deleteRecursive(getFilesDir());
                deleteRecursive(getCacheDir());
                deleteRecursive(getExternalFilesDir(null));
                deleteRecursive(getExternalCacheDir());
                File diagnostics = new File(getCacheDir(), "gflow-diagnostics.txt");
                if (diagnostics.exists()) diagnostics.delete();
                runOnUiThread(() -> {
                    systemState = "Очистка завершена, запускаю uninstall...";
                    renderContent();
                    launchUninstallFlow();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    systemState = "Ошибка полной очистки: " + e.getMessage();
                    renderContent();
                });
            }
        }).start();
    }

    private void launchUninstallFlow() {
        Intent uninstall = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
        uninstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(uninstall);
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        file.delete();
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
}
