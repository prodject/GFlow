package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

public class DesktopActivity extends Activity {
    private static final String KEY_PINNED_ORDER = "pinned_order";
    private static final String KEY_THEME = "theme";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_WEATHER_AT = "weather_at";

    private final ArrayList<String> pinned = new ArrayList<>();
    private LinearLayout contentHost;
    private TextView weatherView;
    private Mode mode = Mode.HOME;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPinned();
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

        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateIn(dock, 220, 18f);
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        if (mode == Mode.DOCK) {
            contentHost.addView(buildPinnedPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildAppLibraryPanel(), lpMatchWrap(0, 0, 0, 16));
        } else if (mode == Mode.ONE_OS) {
            contentHost.addView(buildOneOsDockPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildPinnedPanel(), lpMatchWrap(0, 0, 0, 16));
        } else {
            contentHost.addView(buildPinnedPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildWeatherPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildOneOsDockPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildAppLibraryPanel(), lpMatchWrap(0, 0, 0, 16));
        }
        Ui.staggerIn(collectChildren(contentHost), 30, 55);
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.press(back, () -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, modeLabel()));
        titleBlock.addView(Ui.text(this, "Рабочий стол", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Pins", String.valueOf(pinned.size())));
        bar.addView(buildTopStat("Apps", String.valueOf(apps().size())));
        bar.addView(buildTopStat("Weather", weatherShort()));
        return bar;
    }

    private String modeLabel() {
        switch (mode) {
            case DOCK: return "Pinned Apps / Library";
            case ONE_OS: return "Desktop Bridge / OneOS";
            case HOME:
            default: return "Home Surface / Shortcuts";
        }
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
        hero.addView(Ui.label(this, "Home Surface"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Время", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date())));
        left.addView(metricLine("Дата", new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault()).format(new Date())));
        left.addView(metricLine("Погода", weatherShort()));
        left.addView(metricLine("Профиль", activeProfile()));
        left.addView(metricLine("Dock apps", pinnedPreview()));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout clockCard = Ui.glassCard(this);
        clockCard.setGravity(Gravity.CENTER);
        clockCard.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        TextView clock = Ui.text(this, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), 34, true);
        clock.setGravity(Gravity.CENTER);
        clockCard.addView(clock);
        LinearLayout.LayoutParams clockLp = new LinearLayout.LayoutParams(Ui.dp(this, 200), Ui.dp(this, 180));
        clockLp.leftMargin = Ui.dp(this, 12);
        row.addView(clockCard, clockLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Dock apps", () -> openMode(Mode.DOCK));
        addActionChip(quick, "OneOS Dock", () -> openMode(Mode.ONE_OS));
        addActionChip(quick, "Тема", this::chooseTheme);
        addActionChip(quick, "Погода", this::refreshWeather);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Погода", weatherShort(), Ui.CYAN);
        addStatusCard(grid, "OneOS Dock", new EcarxDockAdapter(this).availability(), Ui.SUCCESS);
        addStatusCard(grid, "Dock Pins", pinnedPreview(), Ui.WARNING);
        addStatusCard(grid, "Theme", themeLabel(), Color.rgb(129, 149, 255));
        addNavCard(grid, "Dock apps", "Пины, порядок и быстрый запуск", Ui.CYAN, () -> openMode(Mode.DOCK));
        addNavCard(grid, "OneOS Dock", "Bridge-команды и handover", Ui.SUCCESS, () -> openMode(Mode.ONE_OS));
        addNavCard(grid, "Профили", "Desktop pins сохраняются в profiles", Ui.WARNING, () -> startActivity(new Intent(this, ProfileActivity.class)));
        addNavCard(grid, "Браузер / Погода", "Переход к weather/browser экрану", Color.rgb(129, 149, 255), () -> startActivity(new Intent(this, WeatherActivity.class)));
        return grid;
    }

    private LinearLayout buildWeatherPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 16, 24, 42) : Color.argb(246, 240, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Weather Widget"));
        panel.addView(Ui.text(this, "Короткая сводка для desktop-shell и ручное обновление кэша погоды.", 14, false));

        weatherView = Ui.text(this, weatherDetailed(), 16, true);
        weatherView.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 4));
        panel.addView(weatherView);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Обновить", this::refreshWeather);
        addActionChip(row, "Браузер / Погода", () -> startActivity(new Intent(this, WeatherActivity.class)));
        panel.addView(row, lpMatchWrap(0, 10, 0, 0));
        return panel;
    }

    private LinearLayout buildPinnedPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Dock Apps"));
        panel.addView(Ui.text(this, "Пины рабочего стола, порядок дока и быстрый запуск приложений.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Все приложения", () -> openMode(Mode.DOCK));
        addActionChip(row, "Очистить док", () -> {
            pinned.clear();
            savePinned();
            renderContent();
        });
        addActionChip(row, "Сохранить", () -> {
            savePinned();
            Ui.toast(this, "Dock order saved");
        });
        panel.addView(row, lpMatchWrap(0, 12, 0, 12));

        if (pinned.isEmpty()) {
            panel.addView(emptyState("Пины пока не добавлены"));
            return panel;
        }
        for (String pkg : pinned) {
            panel.addView(buildPinnedCard(pkg), lpMatchWrap(0, 0, 0, 14));
        }
        return panel;
    }

    private LinearLayout buildPinnedCard(String pkg) {
        String label = appLabel(pkg);
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, label, 18, true));
        card.addView(Ui.muted(this, pkg));

        LinearLayout row = Ui.row(this);
        addMiniAction(row, "Open", () -> launchPackage(pkg, label));
        addMiniAction(row, "Up", () -> movePinned(pkg, -1));
        addMiniAction(row, "Down", () -> movePinned(pkg, 1));
        addMiniAction(row, "Remove", () -> {
            pinned.remove(pkg);
            savePinned();
            renderContent();
        });
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildAppLibraryPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 14, 21, 38) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "App Library"));
        panel.addView(Ui.text(this, "Лончер-приложения устройства: запуск, pin в dock и переход к удалению.", 14, false));

        List<ResolveInfo> list = apps();
        int limit = mode == Mode.DOCK ? list.size() : Math.min(10, list.size());
        if (limit == 0) {
            panel.addView(emptyState("Приложения не найдены"));
            return panel;
        }
        for (int index = 0; index < limit; index++) {
            panel.addView(buildAppLibraryCard(list.get(index)), lpMatchWrap(0, 0, 0, 14));
        }
        if (mode != Mode.DOCK && list.size() > limit) {
            LinearLayout row = Ui.row(this);
            addActionChip(row, "Показать все", () -> openMode(Mode.DOCK));
            panel.addView(row, lpMatchWrap(0, 4, 0, 0));
        }
        return panel;
    }

    private LinearLayout buildAppLibraryCard(ResolveInfo info) {
        String pkg = info.activityInfo.packageName;
        String label = info.loadLabel(getPackageManager()).toString();
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, label, 18, true));
        card.addView(Ui.muted(this, pkg));

        LinearLayout row = Ui.row(this);
        addMiniAction(row, "Open", () -> launchPackage(pkg, label));
        addMiniAction(row, pinned.contains(pkg) ? "Pinned" : "Pin", () -> {
            if (!pinned.contains(pkg)) pinned.add(pkg);
            savePinned();
            renderContent();
        });
        addMiniAction(row, "Delete", () -> startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg))));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildOneOsDockPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(238, 12, 18, 32) : Color.argb(244, 236, 241, 247),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "OneOS Dock"));
        panel.addView(Ui.text(this, "Bridge для hand-over, visibility и notifyItem команд системного dock.", 14, false));
        panel.addView(Ui.muted(this, new EcarxDockAdapter(this).availability()));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addDockCommand(grid, "Hand over ON", a -> a.handOver(true));
        addDockCommand(grid, "Hand over OFF", a -> a.handOver(false));
        addDockCommand(grid, "Status", EcarxDockAdapter::deviceStatus);
        addDockCommand(grid, "Show dock", a -> a.switchDeviceDock(true));
        addDockCommand(grid, "Hide dock", a -> a.switchDeviceDock(false));
        addDockCommand(grid, "HVAC open", a -> a.notifyItem(EcarxDockAdapter.TYPE_HVAC, EcarxDockAdapter.STATE_OPEN));
        addDockCommand(grid, "Media open", a -> a.notifyItem(EcarxDockAdapter.TYPE_MEDIA, EcarxDockAdapter.STATE_OPEN));
        addDockCommand(grid, "Defrost open", a -> a.notifyItem(EcarxDockAdapter.TYPE_DEFROSTING, EcarxDockAdapter.STATE_OPEN));
        addDockCommand(grid, "Volume open", a -> a.notifyItem(EcarxDockAdapter.TYPE_VOLUME, EcarxDockAdapter.STATE_OPEN));
        addDockCommand(grid, "Custom HVAC icon", a -> a.customHvacIcon(0));
        addDockCommand(grid, "Custom app marker", a -> a.customAppIcon(0, getPackageName().getBytes()));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Home", () -> openMode(Mode.HOME), mode == Mode.HOME);
        addDockButton(dock, "Dock", () -> openMode(Mode.DOCK), mode == Mode.DOCK);
        addDockButton(dock, "OneOS", () -> openMode(Mode.ONE_OS), mode == Mode.ONE_OS);
        addDockButton(dock, "Theme", this::chooseTheme, false);
        addDockButton(dock, "Back", this::finish, false);
        return dock;
    }

    private void refreshWeather() {
        if (weatherView != null) weatherView.setText("Загружаю погоду...");
        loadWeather();
    }

    private void loadWeather() {
        new Thread(() -> {
            try {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=55.7558&longitude=37.6173&current=temperature_2m,wind_speed_10m,weather_code";
                JSONObject current = new JSONObject(read(new URL(url))).getJSONObject("current");
                String text = "Погода: " + current.optDouble("temperature_2m") + " C, ветер " + current.optDouble("wind_speed_10m") + " км/ч, код " + current.optInt("weather_code");
                getPreferences(0).edit().putString(KEY_WEATHER, text).putLong(KEY_WEATHER_AT, System.currentTimeMillis()).apply();
                runOnUiThread(() -> {
                    if (weatherView != null) weatherView.setText(weatherDetailed());
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (weatherView != null) weatherView.setText("Ошибка погоды: " + e.getMessage());
                });
            }
        }).start();
    }

    private List<ResolveInfo> apps() {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        Collections.sort(list, Comparator.comparing(a -> a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        return list;
    }

    private void loadPinned() {
        String order = getPreferences(0).getString(KEY_PINNED_ORDER, "");
        pinned.clear();
        if (!order.isEmpty()) {
            for (String pkg : order.split("\n")) if (!pkg.trim().isEmpty()) pinned.add(pkg.trim());
            return;
        }
        pinned.addAll(getPreferences(0).getStringSet("pinned", new LinkedHashSet<>()));
        savePinned();
    }

    private void savePinned() {
        StringBuilder sb = new StringBuilder();
        for (String pkg : pinned) sb.append(pkg).append("\n");
        getPreferences(0).edit().putString(KEY_PINNED_ORDER, sb.toString()).apply();
    }

    private void movePinned(String pkg, int delta) {
        int from = pinned.indexOf(pkg);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= pinned.size()) return;
        Collections.swap(pinned, from, to);
        savePinned();
        renderContent();
    }

    private void chooseTheme() {
        String[] items = {"Tesla Bright", "Graphite", "Sky Glass"};
        new AlertDialog.Builder(this).setTitle("Тема рабочего стола").setItems(items, (d, which) -> {
            getPreferences(0).edit().putInt(KEY_THEME, which).apply();
            renderContent();
        }).show();
    }

    private void openMode(Mode next) {
        mode = next;
        renderContent();
    }

    private String activeProfile() {
        SharedPreferences prefs = UserProfileEngine.prefs(this);
        return prefs.getString(UserProfileEngine.KEY_LAST_USED, "manual");
    }

    private String weatherShort() {
        String cached = getPreferences(0).getString(KEY_WEATHER, "нет данных");
        if (cached.startsWith("Погода: ")) cached = cached.substring("Погода: ".length());
        return cached.length() > 28 ? cached.substring(0, 28) + "…" : cached;
    }

    private String weatherDetailed() {
        String cached = getPreferences(0).getString(KEY_WEATHER, "Погода не обновлялась");
        long at = getPreferences(0).getLong(KEY_WEATHER_AT, 0);
        if (at == 0) return cached;
        return cached + "\nОбновлено: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(at));
    }

    private String themeLabel() {
        int theme = getPreferences(0).getInt(KEY_THEME, 0);
        if (theme == 1) return "Graphite";
        if (theme == 2) return "Sky Glass";
        return "Tesla Bright";
    }

    private String pinnedPreview() {
        if (pinned.isEmpty()) return "empty";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, pinned.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(" · ");
            sb.append(appLabel(pinned.get(i)));
        }
        if (pinned.size() > limit) sb.append(" +").append(pinned.size() - limit);
        return sb.toString();
    }

    private String appLabel(String pkg) {
        PackageManager pm = getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {
            return pkg;
        }
    }

    private void launchPackage(String pkg, String label) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) startActivity(launch);
        else Ui.toast(this, "Не удалось открыть " + label);
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

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextSize(13);
        Ui.bindPress(b, action);
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

    private void addNavCard(GridLayout grid, String title, String body, int color, Runnable action) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, title, 18, true));
        card.addView(Ui.muted(this, body));
        Button open = Ui.button(this, "Открыть");
        Ui.bindPress(open, action);
        card.addView(open, lpMatchWrap(0, 12, 0, 0));
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

    private void addDockCommand(GridLayout grid, String label, DockAction action) {
        Button button = Ui.button(this, label);
        Ui.press(button, () -> {
            EcarxDockAdapter.Result result;
            try {
                result = action.run(new EcarxDockAdapter(this));
            } catch (Exception e) {
                result = EcarxDockAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            showResultSheet(label, result.message);
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(button, lp);
    }

    private void showResultSheet(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, title, 22, true));
        sheet.addView(Ui.muted(this, body == null || body.trim().isEmpty() ? "Нет данных" : body));
        scroll.addView(sheet);
        builder.setView(scroll);
        builder.setPositiveButton("Закрыть", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 22), Color.TRANSPARENT));
        });
        dialog.show();
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

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }

    private String read(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            connection.disconnect();
        }
    }

    interface DockAction {
        EcarxDockAdapter.Result run(EcarxDockAdapter adapter);
    }

    enum Mode {
        HOME,
        DOCK,
        ONE_OS
    }
}
