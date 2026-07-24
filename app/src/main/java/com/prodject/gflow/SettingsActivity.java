package com.prodject.gflow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
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
    private static final String KEY_AUTO_UPDATE = "auto_update_enabled";
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
        latestApkUrl[0] = prefs().getString(UpdateManager.PREF_LATEST_APK_URL, "");
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        latestApkUrl[0] = prefs().getString(UpdateManager.PREF_LATEST_APK_URL, "");
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

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 84)));
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
        contentHost.addView(buildGeneralPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildSystemPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildUpdatesPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildDiagnosticsPanel(), lpMatchWrap(0, 0, 0, 16));
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
        titleBlock.addView(Ui.label(this, "General / Updates / Diagnostics"));
        titleBlock.addView(Ui.text(this, "Настройки", 28, true));
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
        hero.addView(Ui.label(this, "Settings / Updates / Auto Diagnostics"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Experimental", String.valueOf(experimentalFeaturesEnabled())));
        left.addView(metricLine("Developer diagnostics", String.valueOf(developerModeEnabled())));
        left.addView(metricLine("Theme", prefs().getString(KEY_THEME_MODE, "auto")));
        left.addView(metricLine("Start screen", prefs().getString(KEY_START_SCREEN, "Главная")));
        left.addView(metricLine("Auto update", String.valueOf(prefs().getBoolean(KEY_AUTO_UPDATE, false))));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout badge = Ui.glassCard(this);
        badge.setGravity(Gravity.CENTER);
        TextView label = Ui.text(this, "CFG", 30, true);
        label.setGravity(Gravity.CENTER);
        badge.addView(label);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        badgeLp.leftMargin = Ui.dp(this, 12);
        row.addView(badge, badgeLp);
        hero.addView(row);

        TextView state = Ui.text(this, systemState + "\n" + releaseState + "\n" + diagnosticsState, 15, true);
        state.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(state);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Experimental", () -> toggleBoolean(KEY_EXPERIMENTAL_FEATURES));
        addActionChip(quick, "Developer", () -> toggleBoolean(KEY_DEVELOPER_MODE));
        addActionChip(quick, "Backup", this::startBackupFlow);
        addActionChip(quick, "Restore", this::startRestoreFlow);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
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
        panel.addView(Ui.text(this, "Theme, accent, start screen и safe defaults. Experimental features и developer diagnostics переключаются быстрыми кнопками в верхнем блоке.", 14, false));

        panel.addView(buildChoiceRow("Theme", KEY_THEME_MODE, new String[]{"dark", "light", "auto"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoiceRow("Accent", KEY_ACCENT, new String[]{"blue", "amber", "green"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoiceRow("Start screen", KEY_START_SCREEN, new String[]{"Главная", "Климат", "Автомобиль", "Рабочий стол"}), lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildSystemPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Система"));
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
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, title + ": " + prefs().getString(key, items[items.length - 1]), 16, true));
        LinearLayout row = Ui.row(this);
        for (String item : items) {
            Button button = Ui.button(this, item);
            button.setTextSize(13);
            button.setOnClickListener(v -> {
                prefs().edit().putString(key, item).apply();
                if (KEY_THEME_MODE.equals(key)) recreate();
                else renderContent();
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
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Updates"));
        panel.addView(Ui.text(this, "Проверить GitHub releases, показать текущий релиз, скачать APK и установить скачанный APK. При включенном автообновлении проверка выполняется на каждом запуске приложения.", 14, false));
        panel.addView(Ui.text(this, "Текущий релиз: " + UpdateManager.currentVersionLabel(this), 15, true));
        panel.addView(Ui.muted(this, "Последний найденный релиз: " + UpdateManager.cachedReleaseLabel(this)), lpMatchWrap(0, 4, 0, 0));
        panel.addView(Ui.muted(this, releaseState), lpMatchWrap(0, 8, 0, 0));

        CheckBox autoUpdate = new CheckBox(this);
        autoUpdate.setText("Автоматическое обновление приложения");
        autoUpdate.setChecked(prefs().getBoolean(KEY_AUTO_UPDATE, false));
        styleCheckBox(autoUpdate);
        autoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> prefs().edit().putBoolean(KEY_AUTO_UPDATE, isChecked).apply());
        panel.addView(autoUpdate, lpMatchWrap(0, 10, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Проверить", this::checkRelease);
        addActionChip(row, "Скачать APK", () -> downloadReleaseApk(latestApkUrl[0]));
        addActionChip(row, "Установить", this::installDownloadedApk);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildDiagnosticsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Автодиагностика"));
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
        addDockButton(dock, "General", () -> renderContent(), false);
        addDockButton(dock, "System", this::startBackupFlow, false);
        addDockButton(dock, "Updates", this::checkRelease, false);
        addDockButton(dock, "Diagnostics", this::runAutoDiagnostics, false);
        addDockButton(dock, "Back", this::finish, false);
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

    private void checkRelease() {
        releaseState = "Проверяю releases...";
        renderContent();
        UpdateManager.fetchLatestRelease(this, (info, error) -> runOnUiThread(() -> {
            if (error != null) {
                releaseState = "Ошибка проверки: " + error.getMessage();
            } else {
                latestApkUrl[0] = info.apkUrl;
                releaseState = "Текущий релиз: " + UpdateManager.currentVersionLabel(this)
                        + "\nПоследний релиз: " + info.tag
                        + "\nСтатус: " + (UpdateManager.isNewerRelease(this, info.tag) ? "доступно обновление" : "актуальная версия")
                        + "\nAPK: " + (info.apkUrl.isEmpty() ? "не найден" : info.apkUrl);
            }
            renderContent();
        }));
    }

    private void downloadReleaseApk(String url) {
        if (url == null || url.trim().isEmpty()) {
            releaseState = "Сначала нажмите Проверить.";
            renderContent();
            return;
        }
        releaseState = "Скачиваю APK...";
        renderContent();
        UpdateManager.downloadApk(this, url, (file, error) -> runOnUiThread(() -> {
            if (error != null) {
                releaseState = "Ошибка загрузки: " + error.getMessage();
            } else {
                releaseState = "APK загружен: " + file.getAbsolutePath();
            }
            renderContent();
        }));
    }

    private void installDownloadedApk() {
        UpdateManager.installDownloadedApk(this, (started, message) -> {
            releaseState = started ? "Установщик запущен: " + message : message;
            renderContent();
        });
    }

    private void runAutoDiagnostics() {
        diagnosticsState = "Диагностика выполняется...";
        renderContent();
        new Thread(() -> {
            try {
                EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
                StringBuilder log = new StringBuilder();
                log.append("GFlow auto diagnostics\n").append(new Date()).append("\n\n");
                log.append(safeDiagnosticsBlock("AdaptAPI availability", adapter::availability)).append("\n\n");
                LinkedHashMap<String, int[]> groups = buildDiagnosticsGroups();
                int total = 0;
                for (Map.Entry<String, int[]> entry : groups.entrySet()) {
                    log.append("== ").append(entry.getKey()).append(" ==\n");
                    for (int id : entry.getValue()) {
                        total++;
                        final int functionId = id;
                        log.append(safeDiagnosticsBlock("support " + EcarxVehicleAdapter.hex(functionId),
                                () -> adapter.support(functionId).message)).append("\n");
                        log.append(safeDiagnosticsBlock("get " + EcarxVehicleAdapter.hex(functionId),
                                () -> adapter.get(functionId).message)).append("\n\n");
                    }
                }
                appendAdvancedDiagnostics(log);
                final int totalCount = total;
                File file = new File(getCacheDir(), "gflow-diagnostics.txt");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(log.toString().getBytes("UTF-8"));
                }
                runOnUiThread(() -> {
                    diagnosticsState = "Лог готов: " + file.getAbsolutePath() + " · " + totalCount + " function IDs";
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    diagnosticsState = "Ошибка диагностики: " + e.getClass().getSimpleName() + ": " + e.getMessage();
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
        Intent chooser = Intent.createChooser(intent, "Сохранить или отправить лог");
        if (chooser.resolveActivity(getPackageManager()) == null) {
            Ui.toast(this, "Нет приложения для отправки логов");
            return;
        }
        startActivity(chooser);
    }

    private void appendAdvancedDiagnostics(StringBuilder log) {
        appendSection(log, "Parking Signals", safeDiagnosticsBlock("Parking Signals", this::collectParkingSignalsDiagnostics));
        appendSection(log, "Parking HAL", safeDiagnosticsBlock("Parking HAL", this::collectParkingHalDiagnostics));
        appendSection(log, "HUD / DIM", safeDiagnosticsBlock("HUD / DIM", this::collectHudDimDiagnostics));
        appendSection(log, "AudioExt", safeDiagnosticsBlock("AudioExt", this::collectAudioExtDiagnostics));
        appendSection(log, "DVR / EVS", safeDiagnosticsBlock("DVR / EVS", this::collectDvrDiagnostics));
        appendSection(log, "Camera2 Inventory", safeDiagnosticsBlock("Camera2 Inventory", this::collectCameraInventoryDiagnostics));
        appendSection(log, "OneOS Dock", safeDiagnosticsBlock("OneOS Dock", this::collectDockDiagnostics));
        appendSection(log, "ControlBoard", safeDiagnosticsBlock("ControlBoard", this::collectControlBoardDiagnostics));
    }

    private void appendSection(StringBuilder log, String title, String body) {
        log.append("== ").append(title).append(" ==\n");
        log.append(body == null || body.trim().isEmpty() ? "No data\n\n" : body.trim() + "\n\n");
    }

    private String safeDiagnosticsBlock(String label, DiagnosticsSupplier supplier) {
        try {
            String value = supplier.get();
            return value == null || value.trim().isEmpty() ? label + ": no data" : value;
        } catch (Throwable t) {
            return label + ": error " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
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
        b.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(active || Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(238, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214))));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void styleCheckBox(CheckBox checkBox) {
        checkBox.setTextColor(Ui.primaryText(this));
        checkBox.setButtonTintList(ColorStateList.valueOf(Ui.primaryText(this)));
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

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private interface DiagnosticsSupplier {
        String get() throws Exception;
    }
}
