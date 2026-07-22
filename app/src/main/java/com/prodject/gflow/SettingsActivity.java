package com.prodject.gflow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class SettingsActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String KEY_ACCENT = "accent_color";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_START_SCREEN = "start_screen";

    private LinearLayout contentHost;
    private String releaseState = "Источник: github.com/prodject/GFlow/releases";
    private String diagnosticsState = "Проверяет AdaptAPI availability, support/readback по HVAC/BCM/ADAS/HUD/seat и формирует лог.";
    private final String[] latestApkUrl = {""};

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
        contentHost.addView(buildGeneralPanel(), lpMatchWrap(0, 0, 0, 16));
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

        TextView state = Ui.text(this, releaseState + "\n" + diagnosticsState, 15, true);
        state.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(state);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Experimental", () -> toggleBoolean(KEY_EXPERIMENTAL_FEATURES));
        addActionChip(quick, "Developer", () -> toggleBoolean(KEY_DEVELOPER_MODE));
        addActionChip(quick, "Check release", this::checkRelease);
        addActionChip(quick, "Diagnostics", this::runAutoDiagnostics);
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
        panel.addView(Ui.text(this, "Experimental features, developer diagnostics, theme, accent, start screen и safe defaults.", 14, false));

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

    private LinearLayout buildChoiceRow(String title, String key, String[] items) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, title + ": " + prefs().getString(key, items[items.length - 1]), 16, true));
        LinearLayout row = Ui.row(this);
        for (String item : items) {
            Button button = Ui.button(this, item);
            button.setTextSize(13);
            button.setOnClickListener(v -> {
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
        LinearLayout panel = Ui.glassCard(this);
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
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Автодиагностика"));
        panel.addView(Ui.text(this, "Проверка AdaptAPI availability, support/readback по HVAC/BCM/ADAS/HUD/Seat, генерация и отправка лога.", 14, false));
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
            int[] ids = {
                    EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.HVAC_TEMP,
                    EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM,
                    EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.ADAS_PDC,
                    EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.SEAT_POSITION_SET
            };
            StringBuilder log = new StringBuilder();
            log.append("GFlow auto diagnostics\n").append(new Date()).append("\n\n");
            log.append(adapter.availability()).append("\n\n");
            for (int id : ids) {
                log.append(adapter.support(id).message).append("\n");
                log.append(adapter.get(id).message).append("\n\n");
            }
            try {
                File file = new File(getCacheDir(), "gflow-diagnostics.txt");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(log.toString().getBytes("UTF-8"));
                }
                runOnUiThread(() -> {
                    diagnosticsState = "Лог готов: " + file.getAbsolutePath();
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

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }
}
