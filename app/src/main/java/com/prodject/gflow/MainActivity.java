package com.prodject.gflow;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.core.content.FileProvider;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String KEY_LICENSE_ACCEPTED = "license_accepted";
    private static final String KEY_HOME_WEATHER = "home_weather";
    private static final String KEY_HOME_WEATHER_DESC = "home_weather_desc";
    private static final String KEY_HOME_WEATHER_WIND = "home_weather_wind";
    private static final String KEY_HOME_WEATHER_AT = "home_weather_at";
    private static final String[] RUNTIME_PERMS = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };
    private final Handler dashboardHandler = new Handler(Looper.getMainLooper());
    private TextView topProfileValue;
    private TextView topWeatherValue;
    private TextView topCabinValue;
    private TextView topStatusValue;
    private TextView topTimeValue;
    private TextView heroWeatherTemp;
    private TextView heroWeatherDesc;
    private TextView heroWeatherWind;
    private TextView heroCarSummary;
    private LinearLayout dashboardDrawer;
    private View dashboardDrawerScrim;
    private boolean dashboardDrawerOpen;
    private final Runnable dashboardTicker = new Runnable() {
        @Override public void run() {
            refreshDashboardLiveState();
            dashboardHandler.postDelayed(this, 30_000L);
        }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 23) requestPermissions(RUNTIME_PERMS, 10);
        if (licenseAccepted()) showDashboard();
        else showOnboarding();
    }

    @Override protected void onResume() {
        super.onResume();
        dashboardHandler.removeCallbacks(dashboardTicker);
        if (topTimeValue != null || heroWeatherTemp != null) {
            refreshDashboardLiveState();
            dashboardHandler.post(dashboardTicker);
            maybeRefreshHomeWeather();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        dashboardHandler.removeCallbacks(dashboardTicker);
    }

    private void showOnboarding() {
        LinearLayout root = Ui.root(this, "");
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.gflow_wordmark);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(logo, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 130)));
        TextView title = Ui.text(this, "GFlow", 32, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView intro = Ui.muted(this, "Автомобильный центр управления для Android 11+: файлы, камеры, голос, климат, ADAS, HUD, рабочий стол, ADB и системные функции.");
        intro.setGravity(Gravity.CENTER);
        root.addView(intro, lpMatchWrap(8, 4, 8, 18));
        Button start = Ui.button(this, "Принять и открыть приложение");
        Button legal = Ui.button(this, "Лицензия и юридические документы");
        root.addView(start, lpMatchWrap(0, 8, 0, 8));
        root.addView(legal, lpMatchWrap(0, 0, 0, 0));
        legal.setOnClickListener(v -> showLegal());
        start.setOnClickListener(v -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit().putBoolean(KEY_LICENSE_ACCEPTED, true).apply();
            showDashboard();
        });
        setContentView(root);
    }

    private void showLegal() {
        LinearLayout root = Ui.root(this, "Документы", this::showOnboarding);
        root.addView(Ui.text(this, getString(com.prodject.gflow.R.string.legal_text), 16, false));
        setContentView(root);
    }

    private void showDashboard() {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(dashboardBg());

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));

        shell.addView(buildDashboardTopBar(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        bodyLp.topMargin = Ui.dp(this, 16);
        shell.addView(body, bodyLp);

        body.addView(buildCollapsedNavRail(), new LinearLayout.LayoutParams(Ui.dp(this, 96), ViewGroup.LayoutParams.MATCH_PARENT));
        View spacer = new View(this);
        body.addView(spacer, new LinearLayout.LayoutParams(Ui.dp(this, 16), ViewGroup.LayoutParams.MATCH_PARENT));
        body.addView(buildDashboardContent(), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        LinearLayout.LayoutParams dockLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 120));
        dockLp.topMargin = Ui.dp(this, 16);
        shell.addView(buildDashboardDock(), dockLp);

        frame.addView(shell, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dashboardDrawerScrim = new View(this);
        dashboardDrawerScrim.setBackgroundColor(Color.argb(150, 3, 7, 12));
        dashboardDrawerScrim.setAlpha(0f);
        dashboardDrawerScrim.setVisibility(View.GONE);
        dashboardDrawerScrim.setOnClickListener(v -> setDashboardDrawerOpen(false));
        frame.addView(dashboardDrawerScrim, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dashboardDrawer = buildExpandedDashboardDrawer();
        FrameLayout.LayoutParams drawerLp = new FrameLayout.LayoutParams(Ui.dp(this, 328), ViewGroup.LayoutParams.MATCH_PARENT);
        drawerLp.gravity = Gravity.START;
        frame.addView(dashboardDrawer, drawerLp);
        dashboardDrawer.setTranslationX(-Ui.dp(this, 344));

        setContentView(frame);
        dashboardDrawerOpen = false;
        refreshDashboardLiveState();
        dashboardHandler.removeCallbacks(dashboardTicker);
        dashboardHandler.post(dashboardTicker);
        maybeRefreshHomeWeather();
        Ui.animateIn(shell);
    }

    private LinearLayout buildDashboardTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button menu = Ui.button(this, "Меню");
        menu.setOnClickListener(v -> setDashboardDrawerOpen(!dashboardDrawerOpen));
        bar.addView(menu, new LinearLayout.LayoutParams(Ui.dp(this, 110), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        TextView title = Ui.text(this, "Главная", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        topProfileValue = buildTopStat(bar, "Профиль", activeProfileName());
        topWeatherValue = buildTopStat(bar, "Погода", weatherSummary());
        topCabinValue = buildTopStat(bar, "Салон", cabinSummary());
        topStatusValue = buildTopStat(bar, "Статус", adaptStatus());
        topTimeValue = buildTopStat(bar, "Время", new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        return bar;
    }

    private TextView buildTopStat(LinearLayout parent, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        TextView labelView = Ui.label(this, label);
        TextView valueView = Ui.text(this, value, 14, true);
        valueView.setPadding(0, 0, 0, 0);
        card.addView(labelView);
        card.addView(valueView);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        parent.addView(card);
        return valueView;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private LinearLayout buildCollapsedNavRail() {
        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        rail.setPadding(Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14));
        addDashboardMenuButton(rail, "☰", true, () -> setDashboardDrawerOpen(!dashboardDrawerOpen));
        return rail;
    }

    private void addDashboardMenuButton(LinearLayout menu, String symbol, boolean active, Runnable action) {
        TextView button = new TextView(this);
        button.setText(symbol);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(22);
        button.setTextColor(Ui.primaryText(this));
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(74, 255, 255, 255) : Color.argb(224, 255, 255, 255),
                Ui.dp(this, 22),
                Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(72, 185, 198, 214)));
        button.setOnClickListener(v -> transition(action));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 56));
        menu.addView(button, lp);
    }

    private View buildDashboardContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout hero = Ui.glassCard(this);
        hero.setPadding(Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 24));
        hero.addView(buildWeatherHeroHeader(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        VehicleVisualView visual = new VehicleVisualView(this, false);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 520));
        visualLp.topMargin = Ui.dp(this, 8);
        hero.addView(visual, visualLp);
        hero.addView(buildHeroQuickZones(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(hero, lpMatchWrap(0, 0, 0, 16));

        root.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
        return scroll;
    }

    private LinearLayout buildWeatherHeroHeader() {
        LinearLayout top = Ui.row(this);
        top.setGravity(Gravity.TOP);

        LinearLayout weather = Ui.glassCard(this);
        weather.setOnClickListener(v -> transition(this::showWeb));
        weather.addView(Ui.label(this, "Погода"));
        LinearLayout weatherRow = Ui.row(this);
        weatherRow.setGravity(Gravity.TOP);
        weatherRow.addView(new DashboardWeatherView(this), new LinearLayout.LayoutParams(Ui.dp(this, 116), Ui.dp(this, 88)));
        LinearLayout weatherText = new LinearLayout(this);
        weatherText.setOrientation(LinearLayout.VERTICAL);
        heroWeatherTemp = Ui.text(this, weatherTemperature(), 40, true);
        heroWeatherTemp.setPadding(0, 0, 0, 0);
        weatherText.addView(heroWeatherTemp);
        heroWeatherDesc = Ui.muted(this, weatherDescription());
        heroWeatherWind = Ui.muted(this, weatherWindSummary());
        weatherText.addView(heroWeatherDesc);
        weatherText.addView(heroWeatherWind);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLp.leftMargin = Ui.dp(this, 12);
        weatherRow.addView(weatherText, textLp);
        weather.addView(weatherRow);
        top.addView(weather, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout summary = Ui.glassCard(this);
        summary.setOnClickListener(v -> showDashboardQuickSheet("Состояние автомобиля", new QuickAction[]{
                new QuickAction("Открыть климат", this::showClimateMenu),
                new QuickAction("Открыть автомобиль", this::showVehicleMenu),
                new QuickAction("Открыть ADAS", this::showAdasMenu),
                new QuickAction("Открыть парковку", this::openParkingScreen)
        }));
        summary.addView(Ui.label(this, "Автомобиль"));
        heroCarSummary = buildMetricLine("Климат", dashboardCarSummary());
        summary.addView(heroCarSummary);
        summary.addView(buildMetricLine("Режим", developerModeEnabled() ? "Developer" : "Comfort"));
        summary.addView(buildMetricLine("DVR", dvrSummary()));
        summary.addView(buildMetricLine("ADAS", adasSummary()));
        LinearLayout.LayoutParams summaryLp = new LinearLayout.LayoutParams(Ui.dp(this, 260), ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryLp.leftMargin = Ui.dp(this, 16);
        top.addView(summary, summaryLp);
        return top;
    }

    private TextView buildMetricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildHeroQuickZones() {
        LinearLayout row = Ui.row(this);
        row.setWeightSum(4f);
        addHeroButton(row, "Климат", this::showClimateMenu);
        addHeroButton(row, "Кузов", this::showVehicleMenu);
        addHeroButton(row, "ADAS", this::showAdasMenu);
        addHeroButton(row, "360 / APA", this::openParkingScreen);
        return row;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addDashboardWidget(grid, "Климат", climateDashboardDetails(), Ui.CYAN, this::showComfortClimate);
        addDashboardWidget(grid, "Готовность авто", vehicleDashboardDetails(), Ui.SUCCESS, this::showVehicleMenu);
        addDashboardWidget(grid, "DVR", dvrDashboardDetails(), Ui.WARNING, () -> startActivity(new Intent(this, CameraActivity.class)));
        addDashboardWidget(grid, "ADAS", adasDashboardDetails(), Color.rgb(123, 104, 238), this::showAdasMenu);
        addDashboardWidget(grid, "360 / Parking", parkingDashboardDetails(), Color.rgb(72, 153, 255), this::openParkingScreen);
        addDashboardWidget(grid, "Профиль", profileDashboardDetails(), Color.rgb(101, 208, 168), this::showUserProfiles);
        return grid;
    }

    private void addDashboardWidget(GridLayout grid, String title, String value, int color, Runnable action) {
        LinearLayout card = Ui.glassCard(this);
        card.setClickable(true);
        card.setOnClickListener(v -> transition(action));
        card.setOnLongClickListener(v -> {
            showWidgetActionSheet(title, action);
            return true;
        });
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 20, true);
        v.setTextColor(Ui.primaryText(this));
        v.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(v);
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private LinearLayout buildDashboardDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Климат", this::showClimateMenu, true, new QuickAction[]{
                new QuickAction("Открыть климат", this::showClimateMenu),
                new QuickAction("Умный климат", this::showClimateSmart),
                new QuickAction("Пресеты климата", this::showClimatePresets)
        });
        addDockButton(dock, "360", this::openParkingScreen, false, new QuickAction[]{
                new QuickAction("Парковка", this::openParkingScreen),
                new QuickAction("Камеры", () -> startActivity(new Intent(this, CameraActivity.class))),
                new QuickAction("ADAS", this::showAdasMenu)
        });
        addDockButton(dock, "DVR Rec", () -> startActivity(new Intent(this, CameraActivity.class)), false, new QuickAction[]{
                new QuickAction("Открыть DVR", () -> startActivity(new Intent(this, CameraActivity.class))),
                new QuickAction("Парковка", this::openParkingScreen),
                new QuickAction("Автоматизация", this::showAutomation)
        });
        addDockButton(dock, "Drive Mode", this::showVehicleMenu, false, new QuickAction[]{
                new QuickAction("Открыть кузов", this::showVehicleMenu),
                new QuickAction("HUD", this::showHudMenu),
                new QuickAction("Профиль", this::showUserProfiles)
        });
        addDockButton(dock, "Голос", () -> startActivity(new Intent(this, VoiceActivity.class)), false, new QuickAction[]{
                new QuickAction("Открыть голос", () -> startActivity(new Intent(this, VoiceActivity.class))),
                new QuickAction("Автоматизация", this::showAutomation),
                new QuickAction("Погода", this::showWeb)
        });
        addDockButton(dock, "Профиль", this::showUserProfiles, false, new QuickAction[]{
                new QuickAction("Открыть профили", this::showUserProfiles),
                new QuickAction("Автоматизация", this::showAutomation),
                new QuickAction("Настройки", this::showSettings)
        });
        return dock;
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active, QuickAction[] sheetActions) {
        Button button = Ui.button(this, label);
        button.setTextColor(active ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(206, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(76, 185, 198, 214))));
        button.setOnClickListener(v -> transition(action));
        button.setOnLongClickListener(v -> {
            showDashboardQuickSheet(label, sheetActions);
            return true;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private String activeProfileName() {
        String profile = AutomationEngine.prefs(this).getString(AutomationEngine.KEY_ACTIVE_PROFILE, "");
        return profile == null || profile.trim().isEmpty() ? "Не задан" : profile;
    }

    private String adaptStatus() {
        return vehicleAdapterReady() ? "Интеграция доступна" : "Нет подключения к авто";
    }

    private boolean vehicleAdapterReady() {
        String availability = new EcarxVehicleAdapter(this).availability();
        return availability != null && availability.startsWith("ECarX AdaptAPI доступен:");
    }

    private String weatherSummary() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String temp = prefs.getString(KEY_HOME_WEATHER, "...");
        String desc = prefs.getString(KEY_HOME_WEATHER_DESC, "загрузка");
        return temp + " · " + desc;
    }

    private String weatherTemperature() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getString(KEY_HOME_WEATHER, "--");
    }

    private String weatherDescription() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getString(KEY_HOME_WEATHER_DESC, "Нажмите карточку погоды");
    }

    private String weatherWindSummary() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String wind = prefs.getString(KEY_HOME_WEATHER_WIND, "ветер --");
        long at = prefs.getLong(KEY_HOME_WEATHER_AT, 0L);
        String stamp = at == 0L ? "Москва" : new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(at));
        return wind + " · Москва · " + stamp;
    }

    private String cabinSummary() {
        SharedPreferences prefs = SmartClimateController.prefs(this);
        boolean hasDriver = prefs.contains(SmartClimateController.KEY_DRIVER_TARGET);
        boolean hasPassenger = prefs.contains(SmartClimateController.KEY_PASSENGER_TARGET);
        if (!hasDriver && !hasPassenger) return "Нет данных";
        String driver = hasDriver ? String.format(Locale.US, "%.1f°C", prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f)) : "--";
        String passenger = hasPassenger ? String.format(Locale.US, "%.1f°C", prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f)) : "--";
        return driver + "/" + passenger;
    }

    private String dashboardCarSummary() {
        boolean smart = SmartClimateController.prefs(this).getBoolean(SmartClimateController.KEY_ENABLED, false);
        return (smart ? "Smart" : "Manual") + " · " + cabinSummary();
    }

    private String dvrSummary() {
        return dvrArchiveExists() ? "Архив готов" : "Ожидание";
    }

    private String adasSummary() {
        return developerModeEnabled() ? "AEB · LKA · ACC · DEV" : "AEB · LKA · PDC";
    }

    private String climateDashboardDetails() {
        SharedPreferences prefs = SmartClimateController.prefs(this);
        boolean smart = prefs.getBoolean(SmartClimateController.KEY_ENABLED, false);
        String mode = smart ? "Smart climate" : "Ручной режим";
        boolean hasDriver = prefs.contains(SmartClimateController.KEY_DRIVER_TARGET);
        boolean hasPassenger = prefs.contains(SmartClimateController.KEY_PASSENGER_TARGET);
        String driver = hasDriver ? String.format(Locale.US, "%.1f°C", prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f)) : "не задано";
        String passenger = hasPassenger ? String.format(Locale.US, "%.1f°C", prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f)) : "не задано";
        return "Водитель " + driver + " · Пассажир " + passenger + "\n" + mode + " · " + adaptStatus();
    }

    private String vehicleDashboardDetails() {
        return adaptStatus() + "\nПрофиль: " + activeProfileName();
    }

    private String dvrDashboardDetails() {
        return dvrStatusLine() + "\n" + dvrStorageLine();
    }

    private String adasDashboardDetails() {
        return adasSummary() + "\n" + (developerModeEnabled() ? "Расширенная диагностика активна" : "Базовый набор ассистентов");
    }

    private String parkingDashboardDetails() {
        return "Парковка и обзор\n" + adaptStatus();
    }

    private String profileDashboardDetails() {
        return activeProfileName() + "\nБыстрый доступ к сиденью и настройкам";
    }

    private boolean dvrArchiveExists() {
        File root = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return root != null && new File(root, "MonjiDVR").exists();
    }

    private String dvrStatusLine() {
        return dvrArchiveExists() ? "Архив готов" : "Архив не создан";
    }

    private String dvrStorageLine() {
        File[] dirs = getExternalFilesDirs(Environment.DIRECTORY_MOVIES);
        boolean externalMounted = dirs != null && dirs.length > 1 && dirs[1] != null;
        return externalMounted ? "Внешний носитель доступен" : "Только внутренняя память";
    }

    private void refreshDashboardLiveState() {
        if (topProfileValue != null) topProfileValue.setText(activeProfileName());
        if (topWeatherValue != null) topWeatherValue.setText(weatherSummary());
        if (topCabinValue != null) topCabinValue.setText(cabinSummary());
        if (topStatusValue != null) topStatusValue.setText(adaptStatus());
        if (topTimeValue != null) topTimeValue.setText(new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        if (heroWeatherTemp != null) heroWeatherTemp.setText(weatherTemperature());
        if (heroWeatherDesc != null) heroWeatherDesc.setText(weatherDescription());
        if (heroWeatherWind != null) heroWeatherWind.setText(weatherWindSummary());
        if (heroCarSummary != null) heroCarSummary.setText("Климат: " + dashboardCarSummary());
    }

    private void maybeRefreshHomeWeather() {
        long at = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getLong(KEY_HOME_WEATHER_AT, 0L);
        if (at == 0L || System.currentTimeMillis() - at > 20 * 60_000L) loadHomeWeather();
    }

    private void loadHomeWeather() {
        new Thread(() -> {
            try {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=55.7558&longitude=37.6173&current=temperature_2m,wind_speed_10m,weather_code";
                JSONObject current = new JSONObject(readUrl(url)).getJSONObject("current");
                String temp = String.format(Locale.US, "%.1f°C", current.optDouble("temperature_2m"));
                String wind = String.format(Locale.US, "Ветер %.0f км/ч", current.optDouble("wind_speed_10m"));
                String desc = weatherName(current.optInt("weather_code"));
                getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                        .putString(KEY_HOME_WEATHER, temp)
                        .putString(KEY_HOME_WEATHER_DESC, desc)
                        .putString(KEY_HOME_WEATHER_WIND, wind)
                        .putLong(KEY_HOME_WEATHER_AT, System.currentTimeMillis())
                        .apply();
                runOnUiThread(this::refreshDashboardLiveState);
            } catch (Exception ignored) {
            }
        }).start();
    }

    private String weatherName(int code) {
        if (code == 0) return "Ясно";
        if (code <= 3) return "Облачно";
        if (code >= 45 && code <= 48) return "Туман";
        if (code >= 51 && code <= 67) return "Дождь";
        if (code >= 71 && code <= 77) return "Снег";
        if (code >= 80 && code <= 82) return "Ливень";
        if (code >= 95) return "Гроза";
        return "Код " + code;
    }

    private LinearLayout buildExpandedDashboardDrawer() {
        LinearLayout drawer = Ui.glassCard(this);
        drawer.setPadding(Ui.dp(this, 20), Ui.dp(this, 24), Ui.dp(this, 20), Ui.dp(this, 24));
        drawer.addView(Ui.label(this, "Navigation Drawer"));
        drawer.addView(Ui.text(this, "GFlow Home", 28, true));
        drawer.addView(Ui.muted(this, "Быстрый доступ к крупным разделам и системным действиям."));

        addDrawerAction(drawer, "Главная", this::showDashboard);
        addDrawerAction(drawer, "Климат", this::showClimateMenu);
        addDrawerAction(drawer, "Автомобиль", this::showVehicleMenu);
        addDrawerAction(drawer, "ADAS", this::showAdasMenu);
        addDrawerAction(drawer, "Камеры / DVR", () -> startActivity(new Intent(this, CameraActivity.class)));
        addDrawerAction(drawer, "Парковка / APA", this::openParkingScreen);
        addDrawerAction(drawer, "HUD / Cluster", this::showHudMenu);
        addDrawerAction(drawer, "Автоматизация", this::showAutomation);
        addDrawerAction(drawer, "Профили", this::showUserProfiles);
        addDrawerAction(drawer, "Голос", () -> startActivity(new Intent(this, VoiceActivity.class)));
        addDrawerAction(drawer, "Погода / Браузер", this::showWeb);
        addDrawerAction(drawer, "Настройки", this::showSettings);
        return drawer;
    }

    private void addDrawerAction(LinearLayout drawer, String label, Runnable action) {
        Button button = Ui.button(this, label);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        button.setTextColor(Ui.primaryText(this));
        button.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(56, 255, 255, 255) : Color.argb(214, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(72, 185, 198, 214)));
        button.setOnClickListener(v -> {
            setDashboardDrawerOpen(false);
            transition(action);
        });
        drawer.addView(button, lpMatchWrap(0, 6, 0, 6));
    }

    private void setDashboardDrawerOpen(boolean open) {
        if (dashboardDrawer == null || dashboardDrawerScrim == null) return;
        dashboardDrawerOpen = open;
        if (open) {
            dashboardDrawerScrim.setVisibility(View.VISIBLE);
            dashboardDrawerScrim.animate().alpha(1f).setDuration(180).start();
            dashboardDrawer.animate().translationX(0f).setDuration(220).setInterpolator(new DecelerateInterpolator()).start();
        } else {
            dashboardDrawerScrim.animate().alpha(0f).setDuration(160).withEndAction(() -> dashboardDrawerScrim.setVisibility(View.GONE)).start();
            dashboardDrawer.animate().translationX(-Ui.dp(this, 344)).setDuration(200).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void showHeroActionSheet(String label, Runnable primary) {
        showDashboardQuickSheet(label, new QuickAction[]{
                new QuickAction("Открыть раздел", primary),
                new QuickAction("Открыть настройки", this::showSettings),
                new QuickAction("На главную", this::showDashboard)
        });
    }

    private void showWidgetActionSheet(String title, Runnable primary) {
        showDashboardQuickSheet(title, new QuickAction[]{
                new QuickAction("Открыть раздел", primary),
                new QuickAction("Погода / Браузер", this::showWeb),
                new QuickAction("Настройки", this::showSettings)
        });
    }

    private void showDashboardQuickSheet(String title, QuickAction[] actions) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        sheet.addView(Ui.label(this, "Quick Actions"));
        sheet.addView(Ui.text(this, title, 24, true));
        for (QuickAction action : actions) {
            Button button = Ui.button(this, action.label);
            button.setTextColor(Ui.primaryText(this));
            button.setBackground(Ui.cardBg(this,
                    Ui.dark(this) ? Color.argb(56, 255, 255, 255) : Color.argb(214, 255, 255, 255),
                    Ui.dp(this, 18),
                    Ui.dark(this) ? Color.TRANSPARENT : Color.argb(72, 185, 198, 214)));
            button.setOnClickListener(v -> {
                dialog.dismiss();
                transition(action.action);
            });
            sheet.addView(button, lpMatchWrap(0, 8, 0, 0));
        }
        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private static final class QuickAction {
        final String label;
        final Runnable action;

        QuickAction(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private void addSideChip(LinearLayout col, String title, String value, int color, Runnable action) {
        LinearLayout chip = Ui.card(this);
        chip.setClickable(true);
        chip.setOnClickListener(v -> transition(action));
        chip.addView(Ui.muted(this, title));
        TextView v = Ui.text(this, value, 16, true);
        v.setTextColor(color);
        chip.addView(v);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, Ui.dp(this, 5), 0, Ui.dp(this, 7));
        col.addView(chip, lp);
    }

    private void addMainMetric(LinearLayout row, String title, String value, int color, Runnable action) {
        LinearLayout card = Ui.card(this);
        card.setClickable(true);
        card.setOnClickListener(v -> transition(action));
        TextView t = Ui.muted(this, title);
        TextView v = Ui.text(this, value, 20, true);
        v.setTextColor(color);
        card.addView(t);
        card.addView(v);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(Ui.dp(this, 5), Ui.dp(this, 4), Ui.dp(this, 5), 0);
        row.addView(card, lp);
    }

    private void addHeroButton(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Ui.primaryText(this));
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(214, 255, 255, 255),
                Ui.dp(this, 16),
                Ui.dark(this) ? Color.argb(80, 255, 255, 255) : Color.argb(72, 185, 198, 214)));
        b.setOnClickListener(v -> transition(action));
        b.setOnLongClickListener(v -> {
            showHeroActionSheet(label, action);
            return true;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void addStatusStrip(LinearLayout root) {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        addStatusCard(strip, "Адаптер", vehicleAdapterReady() ? "доступен" : "нет подключения", Ui.BLUE);
        addStatusCard(strip, "DVR", "настройки", Color.rgb(168, 65, 58));
        addStatusCard(strip, "Автоклимат", SmartClimateController.prefs(this).getBoolean(SmartClimateController.KEY_ENABLED, false) ? "включен" : "выключен", Ui.GREEN);
        addStatusCard(strip, "Режим", developerModeEnabled() ? "developer" : "user", developerModeEnabled() ? Ui.BLUE : Ui.GREEN);
        root.addView(strip, lpMatchWrap(0, 0, 0, 14));
    }

    private void addStatusCard(LinearLayout strip, String title, String value, int color) {
        LinearLayout card = Ui.card(this);
        TextView t = Ui.muted(this, title);
        TextView v = Ui.text(this, value, 18, true);
        v.setTextColor(color);
        card.addView(t);
        card.addView(v);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
        strip.addView(card, lp);
    }

    private void addNavGrid(LinearLayout root, NavItem[] items) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(getResources().getConfiguration().screenWidthDp >= 700 ? 2 : 1);
        for (NavItem item : items) addNavCard(grid, item);
        root.addView(grid, lpMatchWrap(0, 2, 0, 16));
    }

    private void addNavCard(GridLayout grid, NavItem item) {
        LinearLayout card = Ui.card(this);
        card.setClickable(true);
        card.setOnClickListener(v -> transition(item.action));
        card.setOnLongClickListener(v -> {
            Ui.dialog(this, item.title, item.help());
            return true;
        });

        LinearLayout row = Ui.row(this);
        TextView icon = Ui.pill(this, item.badge, item.color);
        TextView title = Ui.text(this, item.title, 18, true);
        Button help = Ui.help(this, item.title, item.help());
        row.addView(icon);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        titleLp.setMargins(Ui.dp(this, 12), 0, Ui.dp(this, 8), 0);
        row.addView(title, titleLp);
        row.addView(help);
        card.addView(row);
        card.addView(Ui.muted(this, item.subtitle));

        int columns = getResources().getConfiguration().screenWidthDp >= 700 ? 2 : 1;
        int cardDp = columns == 2 ? Math.max(280, (getResources().getConfiguration().screenWidthDp - 76) / 2) : -1;
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = columns == 2 ? Ui.dp(this, cardDp) : ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.setMargins(Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5));
        grid.addView(card, lp);
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private void transition(Runnable action) {
        View content = getWindow().getDecorView();
        content.animate().alpha(0.92f).scaleX(0.985f).scaleY(0.985f).setDuration(90).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
            content.setAlpha(1f);
            content.setScaleX(1f);
            content.setScaleY(1f);
            action.run();
        }).start();
    }

    private static final class NavItem {
        final String title;
        final String subtitle;
        final String badge;
        final int color;
        final Runnable action;

        NavItem(String title, String subtitle, String badge, int color, Runnable action) {
            this.title = title;
            this.subtitle = subtitle;
            this.badge = badge;
            this.color = color;
            this.action = action;
        }

        String help() {
            return subtitle + "\n\nНажмите карточку для перехода. Удерживайте карточку или нажмите '?' для подсказки.";
        }
    }

    private final class VehicleVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final boolean ambienceMode;
        private final Bitmap home;
        private final Bitmap model;
        private final Bitmap underlay;
        private final Bitmap lightScene;
        private final Bitmap lightup;
        private int accent = Color.rgb(38, 131, 215);

        VehicleVisualView(Context context, boolean ambienceMode) {
            super(context);
            this.ambienceMode = ambienceMode;
            home = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_home);
            model = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_settings_model);
            underlay = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_settings_underlay);
            lightScene = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_light_mode_fx11);
            lightup = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_lightup_fx11);
            setClickable(true);
        }

        void setAccent(int color) {
            accent = color;
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            if (ambienceMode) {
                float x = event.getX() / Math.max(1f, getWidth());
                if (x < 0.20f) setAccent(Color.rgb(232, 83, 70));
                else if (x < 0.40f) setAccent(Color.rgb(230, 143, 39));
                else if (x < 0.60f) setAccent(Color.rgb(235, 197, 54));
                else if (x < 0.80f) setAccent(Color.rgb(52, 137, 224));
                else setAccent(Color.rgb(136, 80, 214));
                return true;
            }
            float y = event.getY() / Math.max(1f, getHeight());
            float x = event.getX() / Math.max(1f, getWidth());
            if (y < 0.40f) transition(MainActivity.this::showClimateMenu);
            else if (y < 0.68f) transition(MainActivity.this::showVehicleMenu);
            else if (x < 0.50f) transition(MainActivity.this::showAdasMenu);
            else transition(MainActivity.this::openParkingScreen);
            return true;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float base = h * 0.72f;

            if (ambienceMode && lightScene != null) {
                drawBitmapFit(canvas, lightScene, new RectF(w * 0.02f, h * 0.04f, w * 0.98f, h * 0.88f), 255);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setColor(Color.argb(190, Color.red(accent), Color.green(accent), Color.blue(accent)));
                paint.setStrokeWidth(dp(5));
                Path glow = new Path();
                glow.moveTo(w * 0.24f, h * 0.54f);
                glow.cubicTo(w * 0.36f, h * 0.43f, w * 0.64f, h * 0.43f, w * 0.76f, h * 0.54f);
                canvas.drawPath(glow, paint);
                paint.setStrokeWidth(dp(7));
                canvas.drawLine(w * 0.30f, h * 0.64f, w * 0.70f, h * 0.64f, paint);
                paint.setColor(Color.argb(70, Color.red(accent), Color.green(accent), Color.blue(accent)));
                paint.setStrokeWidth(dp(18));
                canvas.drawLine(w * 0.32f, h * 0.66f, w * 0.68f, h * 0.66f, paint);
                if (lightup != null) drawBitmapFit(canvas, lightup, new RectF(w * 0.03f, h * 0.02f, w * 0.97f, h * 0.88f), 218);
                drawTouchHint(canvas, "Цвет подсветки", w * 0.50f, h * 0.92f, accent);
                return;
            }

            if (!ambienceMode && (home != null || model != null)) {
                RectF bounds = new RectF(w * 0.04f, h * 0.02f, w * 0.96f, h * 0.98f);
                drawBitmapFit(canvas, home != null ? home : model, bounds, 255);
                return;
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(38, 255, 255, 255));
            canvas.drawOval(new RectF(w * 0.13f, h * 0.63f, w * 0.87f, h * 0.90f), paint);

            path.reset();
            path.moveTo(cx, h * 0.12f);
            path.cubicTo(w * 0.70f, h * 0.17f, w * 0.82f, h * 0.44f, w * 0.80f, base);
            path.lineTo(w * 0.64f, h * 0.86f);
            path.lineTo(w * 0.36f, h * 0.86f);
            path.lineTo(w * 0.20f, base);
            path.cubicTo(w * 0.18f, h * 0.44f, w * 0.30f, h * 0.17f, cx, h * 0.12f);
            paint.setColor(Color.argb(230, 247, 250, 252));
            canvas.drawPath(path, paint);

            paint.setColor(Color.rgb(36, 45, 54));
            canvas.drawRoundRect(new RectF(w * 0.37f, h * 0.23f, w * 0.63f, h * 0.40f), dp(18), dp(18), paint);
            paint.setColor(Color.rgb(206, 218, 228));
            canvas.drawRoundRect(new RectF(w * 0.27f, h * 0.49f, w * 0.73f, h * 0.66f), dp(22), dp(22), paint);

            paint.setColor(Color.rgb(34, 116, 205));
            canvas.drawRoundRect(new RectF(w * 0.31f, h * 0.72f, w * 0.43f, h * 0.78f), dp(10), dp(10), paint);
            canvas.drawRoundRect(new RectF(w * 0.57f, h * 0.72f, w * 0.69f, h * 0.78f), dp(10), dp(10), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.argb(120, 32, 42, 50));
            canvas.drawLine(cx, h * 0.16f, cx, h * 0.84f, paint);
            canvas.drawRoundRect(new RectF(w * 0.23f, h * 0.45f, w * 0.77f, h * 0.85f), dp(36), dp(36), paint);
        }

        private void drawBitmapFit(Canvas canvas, Bitmap bitmap, RectF target, int alpha) {
            float scale = Math.min(target.width() / bitmap.getWidth(), target.height() / bitmap.getHeight());
            float bw = bitmap.getWidth() * scale;
            float bh = bitmap.getHeight() * scale;
            RectF dst = new RectF(
                    target.centerX() - bw / 2f,
                    target.centerY() - bh / 2f,
                    target.centerX() + bw / 2f,
                    target.centerY() + bh / 2f);
            paint.setAlpha(alpha);
            canvas.drawBitmap(bitmap, null, dst, paint);
            paint.setAlpha(255);
        }

        private void drawTouchHint(Canvas canvas, String label, float x, float y, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(208, Color.red(color), Color.green(color), Color.blue(color)));
            RectF r = new RectF(x - dp(38), y - dp(13), x + dp(38), y + dp(13));
            canvas.drawRoundRect(r, dp(13), dp(13), paint);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(11));
            paint.setFakeBoldText(true);
            canvas.drawText(label, x, y + dp(4), paint);
            paint.setFakeBoldText(false);
        }

        private float dp(int value) {
            return Ui.dp(getContext(), value);
        }
    }

    private static final class AirFlowView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        AirFlowView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, w, h, Color.rgb(33, 128, 204), Color.rgb(72, 184, 164), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(w * .04f, h * .10f, w * .96f, h * .90f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            for (int i = 0; i < 4; i++) {
                p.setStrokeWidth(Ui.dp(getContext(), 4 + i));
                p.setColor(Color.argb(190 - i * 28, 255, 255, 255));
                float y = h * (.30f + i * .13f);
                Path path = new Path();
                path.moveTo(w * .16f, y);
                path.cubicTo(w * .34f, y - h * .18f, w * .58f, y + h * .18f, w * .84f, y - h * .04f);
                canvas.drawPath(path, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(220, 255, 255, 255));
            canvas.drawCircle(w * .20f, h * .72f, Ui.dp(getContext(), 11), p);
            canvas.drawCircle(w * .50f, h * .72f, Ui.dp(getContext(), 11), p);
            canvas.drawCircle(w * .80f, h * .72f, Ui.dp(getContext(), 11), p);
        }
    }

    private static final class DashboardWeatherView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        DashboardWeatherView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(35, 72, 151, 235));
            canvas.drawCircle(w * .51f, h * .43f, w * .39f, p);
            p.setShader(new LinearGradient(0, 0, 0, h, Color.rgb(252, 254, 255), Color.rgb(180, 201, 224), Shader.TileMode.CLAMP));
            canvas.drawCircle(w * .38f, h * .45f, w * .22f, p);
            canvas.drawCircle(w * .56f, h * .35f, w * .29f, p);
            canvas.drawCircle(w * .73f, h * .48f, w * .20f, p);
            canvas.drawRoundRect(new RectF(w * .20f, h * .44f, w * .88f, h * .66f), Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), p);
            p.setShader(null);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(Ui.dp(getContext(), 3));
            p.setColor(Color.rgb(59, 139, 238));
            for (int i = 0; i < 4; i++) {
                float x = w * (.31f + i * .15f);
                canvas.drawLine(x, h * .72f, x - w * .035f, h * .86f, p);
            }
        }
    }

    private void panel(String title, String body) {
        LinearLayout root = Ui.root(this, title);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, body, 16, false));
        root.addView(card, lpMatchWrap(0, 8, 0, 12));
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        root.addView(back);
        setContentView(root);
        Ui.animateIn(root);
    }

    private LinearLayout menuRoot(String title, String body) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, title, () -> transition(this::showDashboard));
        addScreenMap(root, "Раздел", body);
        scroll.addView(root);
        setContentView(scroll);
        Ui.animateIn(root);
        return root;
    }

    private void showClimateMenu() {
        startActivity(new Intent(this, ClimateActivity.class));
    }

    private void showVehicleMenu() {
        startActivity(new Intent(this, VehicleActivity.class));
    }

    private void showAdasMenu() {
        startActivity(new Intent(this, AdasActivity.class));
    }

    private void openParkingScreen() {
        startActivity(new Intent(this, ParkingActivity.class));
    }

    private void showHudMenu() {
        startActivity(new Intent(this, HudActivity.class));
    }

    private void showSettings() {
        LinearLayout root = Ui.root(this, "Настройки");
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Экспериментальные функции скрыты по умолчанию. Включайте их только для проверки на конкретной прошивке автомобиля.", 14, false));
        card.addView(Ui.help(this, "Experimental features", "Открывает неподтвержденные PAS/AVM, AVAS, Digital Key, сценарии, подсветку и DayMode-разделы. Эти функции могут требовать привилегий, поддержки прошивки или предварительной диагностики."));
        CheckBox experimental = new CheckBox(this);
        experimental.setText("Experimental features");
        experimental.setTextSize(16);
        experimental.setChecked(experimentalFeaturesEnabled());
        experimental.setOnCheckedChangeListener((button, checked) -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putBoolean(KEY_EXPERIMENTAL_FEATURES, checked)
                    .apply();
            Ui.toast(this, checked ? "Experimental features включены" : "Experimental features выключены");
        });
        CheckBox developer = new CheckBox(this);
        developer.setText("Developer diagnostics");
        developer.setTextSize(16);
        developer.setChecked(developerModeEnabled());
        developer.setOnCheckedChangeListener((button, checked) -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putBoolean(KEY_DEVELOPER_MODE, checked)
                    .apply();
            Ui.toast(this, checked ? "Developer diagnostics включены" : "Developer diagnostics выключены");
        });
        card.addView(experimental);
        card.addView(developer);
        root.addView(card, lpMatchWrap(0, 8, 0, 12));
        addUpdateCard(root);
        addDiagnosticsCard(root);
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        root.addView(back);
        setContentView(root);
        Ui.animateIn(root);
    }

    private boolean experimentalFeaturesEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private boolean developerModeEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getBoolean(KEY_DEVELOPER_MODE, false);
    }

    private boolean licenseAccepted() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getBoolean(KEY_LICENSE_ACCEPTED, false);
    }

    private void addUpdateCard(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Обновления приложения", 18, true));
        TextView state = Ui.muted(this, "Источник: github.com/prodject/GFlow/releases");
        card.addView(state);
        LinearLayout row = Ui.row(this);
        Button check = Ui.button(this, "Проверить");
        Button download = Ui.button(this, "Скачать APK");
        Button install = Ui.button(this, "Установить");
        final String[] apkUrl = {getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getString("latest_apk_url", "")};
        check.setOnClickListener(v -> checkRelease(state, apkUrl));
        download.setOnClickListener(v -> downloadReleaseApk(state, apkUrl[0]));
        install.setOnClickListener(v -> installDownloadedApk(state));
        row.addView(check, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        row.addView(download, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        row.addView(install, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        card.addView(row);
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void checkRelease(TextView state, String[] apkUrl) {
        state.setText("Проверяю releases...");
        new Thread(() -> {
            try {
                JSONArray arr = new JSONArray(readUrl("https://api.github.com/repos/prodject/GFlow/releases"));
                JSONObject release = arr.getJSONObject(0);
                String tag = release.optString("tag_name");
                JSONArray assets = release.optJSONArray("assets");
                String url = "";
                if (assets != null) for (int i = 0; i < assets.length(); i++) {
                    JSONObject a = assets.getJSONObject(i);
                    if (a.optString("name").endsWith(".apk")) url = a.optString("browser_download_url");
                }
                String finalUrl = url;
                getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit().putString("latest_apk_url", finalUrl).apply();
                apkUrl[0] = finalUrl;
                runOnUiThread(() -> state.setText("Последний релиз: " + tag + "\nAPK: " + (finalUrl.isEmpty() ? "не найден" : finalUrl)));
            } catch (Exception e) {
                runOnUiThread(() -> state.setText("Ошибка проверки: " + e.getMessage()));
            }
        }).start();
    }

    private void downloadReleaseApk(TextView state, String url) {
        if (url == null || url.trim().isEmpty()) {
            state.setText("Сначала нажмите Проверить.");
            return;
        }
        state.setText("Скачиваю APK...");
        new Thread(() -> {
            File out = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
            try (InputStream in = new URL(url).openStream(); OutputStream file = new FileOutputStream(out)) {
                byte[] buf = new byte[1024 * 64];
                for (int n; (n = in.read(buf)) > 0;) file.write(buf, 0, n);
                runOnUiThread(() -> state.setText("APK загружен: " + out.getAbsolutePath()));
            } catch (Exception e) {
                runOnUiThread(() -> state.setText("Ошибка загрузки: " + e.getMessage()));
            }
        }).start();
    }

    private void installDownloadedApk(TextView state) {
        File apk = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
        if (!apk.exists()) {
            state.setText("APK еще не загружен.");
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", apk);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void addDiagnosticsCard(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Автодиагностика", 18, true));
        TextView state = Ui.muted(this, "Проверяет AdaptAPI availability, support/readback по основным HVAC/BCM/ADAS/HUD/seat функциям и формирует лог.");
        Button run = Ui.button(this, "Автодиагностика доступных функций");
        run.setOnClickListener(v -> runAutoDiagnostics(state));
        card.addView(state);
        card.addView(run, lpMatchWrap(0, 8, 0, 0));
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void runAutoDiagnostics(TextView state) {
        state.setText("Диагностика выполняется...");
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
                File f = new File(getCacheDir(), "gflow-diagnostics.txt");
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(log.toString().getBytes("UTF-8"));
                }
                runOnUiThread(() -> {
                    state.setText("Лог готов: " + f.getAbsolutePath());
                    shareFile(f);
                });
            } catch (Exception e) {
                runOnUiThread(() -> state.setText("Ошибка лога: " + e.getMessage()));
            }
        }).start();
    }

    private void shareFile(File f) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", f);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i, "Сохранить или отправить лог"));
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setRequestProperty("Accept", "application/vnd.github+json");
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0;) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }

    private LinearLayout commandRoot(String title) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setBackgroundColor(Ui.bg(this));
        LinearLayout left = settingsRail();
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, title, () -> transition(this::showDashboard));

        LinearLayout status = Ui.card(this);
        status.addView(Ui.muted(this, "Статус интеграции"));
        status.addView(Ui.text(this, new EcarxVehicleAdapter(this).availability(), 14, false));
        status.addView(Ui.muted(this, developerModeEnabled()
                ? "Developer diagnostics включены: raw IDs и диагностические блоки видны."
                : "User mode: raw IDs и диагностические блоки скрыты. Включите Developer diagnostics в настройках для проверки прошивки."));
        root.addView(status, lpMatchWrap(0, 0, 0, 12));
        EditText filter = new EditText(this);
        filter.setSingleLine(true);
        filter.setHint("Фильтр по странице: климат, окно, ADAS, diag...");
        filter.setTextSize(15);
        filter.setBackground(Ui.cardBg(this, Ui.PANEL, Ui.dp(this, 14), Ui.LINE));
        filter.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommandViews(root, s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        root.addView(filter, lpMatchWrap(0, 0, 0, 12));
        scroll.addView(root);
        shell.addView(left, new LinearLayout.LayoutParams(Ui.dp(this, 210), ViewGroup.LayoutParams.MATCH_PARENT));
        shell.addView(scroll, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        setContentView(shell);
        Ui.animateIn(root);
        return root;
    }

    private LinearLayout settingsRail() {
        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setPadding(Ui.dp(this, 12), Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18));
        rail.setBackgroundColor(Ui.dark(this) ? Color.rgb(18, 21, 24) : Color.rgb(232, 237, 241));
        rail.addView(Ui.text(this, "GFlow", 22, true));
        addRailButton(rail, "Климат", this::showClimateMenu);
        addRailButton(rail, "Автомобиль", this::showVehicleMenu);
        addRailButton(rail, "ADAS", this::showAdasMenu);
        addRailButton(rail, "HUD", this::showHudMenu);
        addRailButton(rail, "Автоматизация", this::showAutomation);
        addRailButton(rail, "Профили", this::showUserProfiles);
        addRailButton(rail, "DVR", () -> startActivity(new Intent(this, CameraActivity.class)));
        addRailButton(rail, "Погода", this::showWeb);
        addRailButton(rail, "Настройки", this::showSettings);
        return rail;
    }

    private void addRailButton(LinearLayout rail, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setOnClickListener(v -> transition(action));
        rail.addView(b, lpMatchWrap(0, 4, 0, 4));
    }

    private void addClimateControlPanel(LinearLayout root) {
        LinearLayout panel = Ui.card(this);
        panel.addView(Ui.text(this, "Комфорт", 22, true));
        panel.addView(new AirFlowView(this), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 150)));
        LinearLayout temps = Ui.row(this);
        addTempDial(temps, "Водитель", 22.0f, EcarxVehicleAdapter.ZONE_DRIVER_LEFT);
        addTempDial(temps, "Пассажир", 22.0f, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        panel.addView(temps);
        SeekBar fan = new SeekBar(this);
        fan.setMax(8);
        fan.setProgress(2);
        TextView fanLabel = Ui.muted(this, "Вентилятор: 3");
        fan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fanLabel.setText("Вентилятор: " + (progress + 1));
                if (fromUser) new EcarxVehicleAdapter(MainActivity.this).set(EcarxVehicleAdapter.HVAC_FAN_SPEED, progress + 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        panel.addView(fanLabel);
        panel.addView(fan);
        LinearLayout actions = Ui.row(this);
        addMiniAction(actions, "Auto", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON));
        addMiniAction(actions, "A/C", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON));
        addMiniAction(actions, "Стекло", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(actions);
        root.addView(panel, lpMatchWrap(0, 0, 0, 12));
    }

    private void addSmartClimatePanel(LinearLayout root, SharedPreferences prefs) {
        LinearLayout panel = Ui.card(this);
        panel.addView(Ui.text(this, "Алгоритм климата", 22, true));
        String mode = prefs.getString(SmartClimateController.KEY_MODE, SmartClimateController.MODE_OFF);
        panel.addView(Ui.muted(this, "Текущий режим: " + mode));
        SeekBar target = new SeekBar(this);
        target.setMax(120);
        float saved = prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f);
        target.setProgress(Math.max(0, Math.min(120, Math.round((saved - 16f) * 10f))));
        TextView targetLabel = Ui.text(this, String.format(Locale.US, "%.1f C", saved), 30, true);
        target.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 16f + progress / 10f;
                targetLabel.setText(String.format(Locale.US, "%.1f C", value));
                if (fromUser) prefs.edit()
                        .putFloat(SmartClimateController.KEY_DRIVER_TARGET, value)
                        .putFloat(SmartClimateController.KEY_PASSENGER_TARGET, value)
                        .apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        panel.addView(targetLabel);
        panel.addView(target);
        LinearLayout modes = Ui.row(this);
        addModeAction(modes, prefs, "Охладить", SmartClimateController.MODE_FAST_COOL);
        addModeAction(modes, prefs, "Согреть", SmartClimateController.MODE_FAST_HEAT);
        addModeAction(modes, prefs, "Держать", SmartClimateController.MODE_MAINTAIN);
        panel.addView(modes);
        root.addView(panel, lpMatchWrap(0, 0, 0, 12));
    }

    private void addTempDial(LinearLayout row, String label, float value, int zone) {
        LinearLayout card = Ui.card(this);
        TextView title = Ui.muted(this, label);
        TextView temp = Ui.text(this, String.format(Locale.US, "%.1f C", value), 28, true);
        SeekBar seek = new SeekBar(this);
        seek.setMax(120);
        seek.setProgress(Math.round((value - 16f) * 10f));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float t = 16f + progress / 10f;
                temp.setText(String.format(Locale.US, "%.1f C", t));
                if (fromUser) new EcarxVehicleAdapter(MainActivity.this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, t);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        card.addView(title);
        card.addView(temp);
        card.addView(seek);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        row.addView(card, lp);
    }

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setGravity(Gravity.CENTER);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void addModeAction(LinearLayout row, SharedPreferences prefs, String label, String mode) {
        addMiniAction(row, label, () -> {
            prefs.edit().putString(SmartClimateController.KEY_MODE, mode).putBoolean(SmartClimateController.KEY_ENABLED, true).apply();
            Ui.toast(this, label);
        });
    }

    private void addAdasOverview(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Ассистенты водителя", 22, true));
        card.addView(Ui.muted(this, "Основные переключатели вынесены наверх. Подробные firmware-команды и диагностика находятся ниже."));
        LinearLayout safety = Ui.row(this);
        addToggleAction(safety, "AEB", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(safety, "FCW", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(safety, "LKA", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(safety, "PDC", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        card.addView(safety);
        SeekBar gap = new SeekBar(this);
        gap.setMax(4);
        gap.setProgress(2);
        TextView gapText = Ui.muted(this, "ACC дистанция: средняя");
        gap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gapText.setText("ACC дистанция: " + (progress + 1));
                if (fromUser) CarCommandBus.sendVehicle(MainActivity.this, EcarxVehicleAdapter.ADAS_ACC_TIME_GAP, progress + 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        card.addView(gapText);
        card.addView(gap);
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addHudOverview(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Проектор и OneOS", 22, true));
        card.addView(Ui.muted(this, new EcarxHudDimAdapter(this).availability()));
        LinearLayout row = Ui.row(this);
        addToggleAction(row, "HUD on", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(row, "Навигация", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(row, "Медиа", () -> CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON));
        addToggleAction(row, "DIM night", () -> new EcarxHudDimAdapter(this).requestDayNightMode());
        card.addView(row);
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addProfilesOverview(LinearLayout root, SharedPreferences prefs) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Водитель и пассажир", 22, true));
        card.addView(Ui.muted(this, "Активный профиль: " + AutomationEngine.prefs(this).getString(AutomationEngine.KEY_ACTIVE_PROFILE, "не выбран")));
        card.addView(Ui.muted(this, "Последний профиль: " + prefs.getString(UserProfileEngine.KEY_LAST_USED, "нет")));
        LinearLayout row = Ui.row(this);
        addToggleAction(row, "Водитель", () -> showUserProfileEditor("", "driver", "manual=", UserProfileEngine.defaultDriverBody()));
        addToggleAction(row, "Пассажир", () -> showUserProfileEditor("", "passenger", "manual=", UserProfileEngine.defaultPassengerBody()));
        addToggleAction(row, "Последний", () -> root.addView(Ui.text(this, UserProfileEngine.apply(this, prefs.getString(UserProfileEngine.KEY_LAST_USED, "")), 13, false), 2));
        card.addView(row);
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addSteeringOverview(LinearLayout root, SharedPreferences steering, SharedPreferences prefs) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Жесты на руле", 22, true));
        card.addView(Ui.muted(this, "Последнее событие: " + steering.getString("last_event", "нет")));
        card.addView(Ui.muted(this, "Назначений: " + AutomationEngine.names(prefs, AutomationEngine.KEY_BUTTON_ORDER).size()));
        LinearLayout row = Ui.row(this);
        addToggleAction(row, "Hold", () -> showSteeringButtonEditor("", "0", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addToggleAction(row, "Double", () -> showSteeringButtonEditor("", "0", "double", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addToggleAction(row, "Примеры", () -> { installSteeringButtonExamples(); showSteeringButtons(); });
        card.addView(row);
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addToggleAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setGravity(Gravity.CENTER);
        b.setOnClickListener(v -> {
            action.run();
            Ui.toast(this, label);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void filterCommandViews(View view, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Object tag = view.getTag();
        if (tag instanceof String && ((String) tag).startsWith("filter:")) {
            String haystack = ((String) tag).substring("filter:".length()).toLowerCase(Locale.ROOT);
            view.setVisibility(q.isEmpty() || haystack.contains(q) ? View.VISIBLE : View.GONE);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) filterCommandViews(group.getChildAt(i), q);
        }
    }

    private void addGroupTitle(LinearLayout root, String title, int functionId) {
        LinearLayout row = Ui.row(this);
        TextView label = Ui.text(this, title, 14, true);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(Ui.pill(this, safetyFor(title), safetyColor(safetyFor(title))));
        if (developerModeEnabled()) row.addView(Ui.muted(this, "  " + EcarxVehicleAdapter.hex(functionId)));
        root.addView(row);
    }

    private String commandText(String label, String safety, String raw) {
        StringBuilder sb = new StringBuilder(label);
        if (developerModeEnabled() && raw != null && !raw.trim().isEmpty()) sb.append(" · ").append(raw);
        return sb.toString();
    }

    private Button addCommandRow(LinearLayout root, String label, String safety, String raw) {
        LinearLayout row = Ui.row(this);
        row.setTag("filter:" + label + " " + safety + " " + (raw == null ? "" : raw));
        if (developerModeEnabled()) {
            TextView badge = Ui.pill(this, safety, safetyColor(safety));
            row.addView(badge);
        }
        Button b = Ui.button(this, commandText(label, safety, raw));
        applySafety(b, label);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(developerModeEnabled() ? Ui.dp(this, 8) : 0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        row.addView(b, lp);
        root.addView(row);
        return b;
    }

    private void applySafety(Button button, String label) {
        String safety = safetyFor(label);
        button.setTextColor(safetyColor(safety));
        button.setOnLongClickListener(v -> {
            Ui.dialog(this, safetyTitle(safety), safetyHelp(safety));
            return true;
        });
    }

    private String safetyTitle(String safety) {
        if ("SAFE".equals(safety)) return "SAFE";
        if ("DIAG".equals(safety)) return "DIAG";
        if ("EXP".equals(safety)) return "EXPERIMENTAL";
        if ("PRIV".equals(safety)) return "PRIVILEGED";
        return "FIRMWARE";
    }

    private String safetyHelp(String safety) {
        if ("SAFE".equals(safety)) return "Обычное действие приложения без прямой записи в критичные автомобильные функции.";
        if ("DIAG".equals(safety)) return "Диагностика support/readback/HAL. В обычном пользовательском режиме такие пункты скрыты.";
        if ("EXP".equals(safety)) return "Экспериментальная функция. Перед использованием проверьте поддержку прошивки и ожидаемые значения.";
        if ("PRIV".equals(safety)) return "Требует системных разрешений, ADB grants, root или поддержки привилегированной прошивки.";
        return "Firmware-dependent команда автомобиля. UI отправляет команду, но исполнение зависит от ECARX/Geely/OneOS API и разрешений.";
    }

    private String safetyFor(String label) {
        String normalized = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (normalized.contains("диагност") || normalized.contains("diagnostic") || normalized.contains("readback") || normalized.contains("hal ") || normalized.contains("raw")) return "DIAG";
        if (normalized.contains("experimental") || normalized.contains("avas") || normalized.contains("digital key") || normalized.contains("launch control") || normalized.contains("power train") || normalized.contains("risky")) return "EXP";
        if (normalized.contains("adb") || normalized.contains("grant") || normalized.contains("secure") || normalized.contains("shell")) return "PRIV";
        if (normalized.contains("door") || normalized.contains("двер") || normalized.contains("window") || normalized.contains("окн") || normalized.contains("seat") || normalized.contains("сиден") || normalized.contains("adas") || normalized.contains("drive") || normalized.contains("hud") || normalized.contains("dim") || normalized.contains("oneos") || normalized.contains("audioext") || normalized.contains("cluster") || normalized.contains("mirror") || normalized.contains("зерк") || normalized.contains("light") || normalized.contains("свет") || normalized.contains("wiper") || normalized.contains("люк") || normalized.contains("штор") || normalized.contains("climate") || normalized.contains("климат")) return "FIRMWARE";
        return "SAFE";
    }

    private int safetyColor(String safety) {
        if ("SAFE".equals(safety)) return Ui.GREEN;
        if ("DIAG".equals(safety)) return Ui.BLUE;
        if ("EXP".equals(safety)) return Ui.AMBER;
        if ("PRIV".equals(safety)) return Color.rgb(120, 80, 160);
        return Color.rgb(86, 104, 120);
    }

    private void addScreenMap(LinearLayout root, String title, String body, String... chips) {
        LinearLayout card = Ui.card(this);
        LinearLayout row = Ui.row(this);
        TextView h = Ui.text(this, title, 16, true);
        row.addView(h, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(Ui.help(this, title, body));
        card.addView(row);
        card.addView(Ui.muted(this, body));
        if (chips.length > 0) {
            LinearLayout chipRow = Ui.row(this);
            chipRow.setPadding(0, Ui.dp(this, 8), 0, 0);
            for (String chip : chips) {
                TextView pill = Ui.pill(this, chip, Color.rgb(86, 104, 120));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, Ui.dp(this, 6), 0);
                chipRow.addView(pill, lp);
            }
            card.addView(chipRow);
        }
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addCommand(LinearLayout root, String label, int functionId, int zone, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addZoneCommands(LinearLayout root, String label, int functionId, int value, int... zones) {
        for (int zone : zones) addCommand(root, label + " · " + zoneLabel(zone), functionId, zone, value);
    }

    private void addZoneDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика зон: " + label, "DIAG", EcarxVehicleAdapter.hex(functionId));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(zoneLabel(zone)).append(": ")
                        .append(adapter.support(functionId, zone).message).append("\n")
                        .append(adapter.get(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addCommandGroup(LinearLayout root, String title, int functionId, String[] labels, int[] values) {
        addGroupTitle(root, title, functionId);
        for (int i = 0; i < labels.length; i++) addCommand(root, labels[i], functionId, values[i]);
    }

    private void addSignalCommand(LinearLayout root, String label, String methodName, int signalId, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), CarSignalManagerAdapter.hex(signalId) + "=" + CarSignalManagerAdapter.hex(value));
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика raw: " + label, "DIAG", "");
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int i = 0; i + 1 < methodSignalPairs.length; i += 2) {
                String method = (String) methodSignalPairs[i];
                int signalId = (Integer) methodSignalPairs[i + 1];
                sb.append(adapter.get(method, signalId).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addHalPropertyDiagnostic(LinearLayout root, String label, int... propertyIds) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "HAL свойства: " + label, "DIAG", "");
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int propertyId : propertyIds) {
                sb.append(adapter.rawHalProperty(propertyId, "VehiclePropertyVEH2").message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private String zoneLabel(int zone) {
        if (zone == EcarxVehicleAdapter.ZONE_ALL) return "все зоны";
        if (zone == EcarxVehicleAdapter.ZONE_DRIVER_LEFT) return "водитель/1L";
        if (zone == EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT) return "пассажир/1R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_LEFT) return "2L";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_RIGHT) return "2R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_1_ALL) return "1 ряд";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_ALL) return "2 ряд";
        return "zone=" + zone;
    }

    private void addFloatCommand(LinearLayout root, String label, int functionId, int zone, float value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + value);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).setFloat(functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addFloatDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Float диагностика: " + label, "DIAG", EcarxVehicleAdapter.hex(functionId));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(adapter.support(functionId, zone).message).append("\n");
                sb.append(adapter.getFloat(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
            StringBuilder sb = new StringBuilder(label).append("\n");
            boolean ok = true;
            for (EcarxVehicleAdapter.Result r : results) {
                ok &= r.success;
                sb.append(r.message).append("\n");
            }
            Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void runPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        StringBuilder sb = new StringBuilder(label).append("\n");
        boolean ok = true;
        for (EcarxVehicleAdapter.Result r : results) {
            ok &= r.success;
            sb.append(r.message).append("\n");
        }
        Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
        root.addView(Ui.text(this, sb.toString(), 13, false), 2);
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика: " + label, "DIAG", functionIds.length == 0 ? "" : functionIds.length + " ids");
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int functionId : functionIds) {
                EcarxVehicleAdapter.Result support = adapter.support(functionId);
                EcarxVehicleAdapter.Result value = adapter.get(functionId);
                sb.append(support.message).append("\n").append(value.message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void showAutomation() {
        startActivity(new Intent(this, AutomationActivity.class));
    }


    private void showSteeringButtons() {
        LinearLayout root = commandRoot("Кнопки руля");
        SharedPreferences steering = getSharedPreferences("steering", MODE_PRIVATE);
        SharedPreferences prefs = AutomationEngine.prefs(this);
        addSteeringOverview(root, steering, prefs);
        Button add = Ui.button(this, "Назначить кнопку");
        add.setOnClickListener(v -> showSteeringButtonEditor("", "0", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        root.addView(add);
        Button examples = Ui.button(this, "Добавить примеры назначений");
        examples.setOnClickListener(v -> {
            installSteeringButtonExamples();
            showSteeringButtons();
        });
        root.addView(examples);
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_BUTTON_ORDER)) {
            String raw = prefs.getString("button2:" + name, prefs.getString("button:" + name, ""));
            Button b = Ui.button(this, "Button: " + raw);
            b.setOnLongClickListener(v -> {
                String[] p = raw.split("\\|", -1);
                if (p.length >= 8) showSteeringButtonEditor(name, p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                else showSteeringButtonEditor(name, p.length > 1 ? p[1] : "0", p.length > 2 ? p[2] : "hold", "", "always", "replace", "preset", p.length > 3 ? p[3] : "");
                return true;
            });
            root.addView(b);
        }
    }

    private void showSteeringButtonEditor(String oldName, String oldKey, String oldGesture, String oldModifier, String oldCondition, String oldBehavior, String oldTargetType, String oldTarget) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новое назначение" : "Назначение: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText key = new EditText(this);
        key.setHint("keyCode из последнего события");
        key.setText(oldKey);
        EditText gesture = new EditText(this);
        gesture.setHint("press / double / triple / hold");
        gesture.setText(oldGesture);
        EditText modifier = new EditText(this);
        modifier.setHint("Другая удерживаемая кнопка, пусто если не нужно");
        modifier.setText(oldModifier);
        EditText condition = new EditText(this);
        condition.setHint("always / stationary / moving / app=maps / profile=Глеб");
        condition.setText(oldCondition);
        EditText behavior = new EditText(this);
        behavior.setHint("replace / together / hold-only / stationary-only");
        behavior.setText(oldBehavior);
        EditText targetType = new EditText(this);
        targetType.setHint("preset / scenario / action / voice / launch / command");
        targetType.setText(oldTargetType);
        EditText target = new EditText(this);
        target.setHint("Цель: имя preset/scenario или action/команда");
        target.setText(oldTarget);
        Button save = Ui.button(this, "Сохранить назначение");
        save.setOnClickListener(v -> {
            AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim() + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT) + "|" + target.getText().toString().trim());
            AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim()
                            + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT)
                            + "|" + modifier.getText().toString().trim()
                            + "|" + condition.getText().toString().trim()
                            + "|" + behavior.getText().toString().trim()
                            + "|" + targetType.getText().toString().trim()
                            + "|" + target.getText().toString().trim());
            showSteeringButtons();
        });
        root.addView(name);
        root.addView(key);
        root.addView(gesture);
        root.addView(modifier);
        root.addView(condition);
        root.addView(behavior);
        root.addView(targetType);
        root.addView(target);
        root.addView(save);
    }

    private void installSteeringButtonExamples() {
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M hold 360", "M hold 360|77|hold||always|replace|command|0x21110100/0=0x1");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M double cooling", "M double cooling|77|double||always|replace|preset|Летнее охлаждение");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Voice hold Monji", "Voice hold Monji|231|hold||always|replace|launch|com.prodject.gflow");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Volume down double mute", "Volume down double mute|25|double||always|together|voice|mute media");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Next hold eco comfort", "Next hold eco comfort|87|hold||always|replace|scenario|Eco Comfort toggle");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M stationary trunk", "M stationary trunk|77|press||stationary|stationary-only|command|0x21110100/0=0x64");
        Ui.toast(this, "Примеры кнопок руля добавлены");
    }

    private void showUserProfiles() {
        LinearLayout root = commandRoot("Профили пользователей");
        SharedPreferences prefs = UserProfileEngine.prefs(this);
        addProfilesOverview(root, prefs);
        Button addDriver = Ui.button(this, "Создать профиль водителя");
        addDriver.setOnClickListener(v -> showUserProfileEditor("", "driver", "manual=", UserProfileEngine.defaultDriverBody()));
        Button addPassenger = Ui.button(this, "Создать профиль пассажира");
        addPassenger.setOnClickListener(v -> showUserProfileEditor("", "passenger", "manual=", UserProfileEngine.defaultPassengerBody()));
        Button last = Ui.button(this, "Применить последний профиль");
        last.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, prefs.getString(UserProfileEngine.KEY_LAST_USED, "")), 13, false), 2));
        root.addView(addDriver);
        root.addView(addPassenger);
        root.addView(last);
        addProfileSection(root, "Водители", "driver");
        addProfileSection(root, "Пассажиры", "passenger");
    }

    private void addProfileSection(LinearLayout root, String title, String type) {
        root.addView(Ui.text(this, title, 18, true));
        for (String name : UserProfileEngine.names(this, type)) {
            String raw = UserProfileEngine.raw(this, name);
            Button b = Ui.button(this, name + " · " + type);
            b.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name), 13, false), 2));
            b.setOnLongClickListener(v -> {
                String identity = "";
                String body = "";
                for (String line : raw.split("\\n")) {
                    if (line.startsWith("identity:")) identity = line.substring("identity:".length());
                    else if (!line.startsWith("name:") && !line.startsWith("type:")) body += line + "\n";
                }
                showUserProfileEditor(name, type, identity, body);
                return true;
            });
            root.addView(b);
        }
    }

    private void showUserProfileEditor(String oldName, String oldType, String oldIdentity, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый профиль" : "Профиль: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Имя профиля");
        name.setText(oldName);
        EditText type = new EditText(this);
        type.setHint("driver / passenger");
        type.setText(oldType);
        EditText identity = new EditText(this);
        identity.setHint("manual=Глеб; phone=Pixel; bluetooth=AA:BB; face=gleb; digitalKey=id");
        identity.setText(oldIdentity);
        EditText body = new EditText(this);
        body.setMinLines(16);
        body.setGravity(Gravity.TOP);
        body.setHint(UserProfileEngine.defaultDriverBody());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить профиль");
        save.setOnClickListener(v -> {
            String result = UserProfileEngine.save(this, oldName, name.getText().toString(), type.getText().toString().trim(), identity.getText().toString(), body.getText().toString());
            Ui.toast(this, "Профиль сохранен");
            root.addView(Ui.text(this, result, 13, false), 2);
        });
        Button apply = Ui.button(this, "Применить");
        apply.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name.getText().toString().trim()), 13, false), 2));
        Button delete = Ui.button(this, "Удалить");
        delete.setOnClickListener(v -> {
            UserProfileEngine.delete(this, oldName, oldType);
            showUserProfiles();
        });
        root.addView(Ui.text(this, "Доступные строки: seatMemory, seatLength, seatHeight, seatBackrest, mirror, climateTemp, fan, seatHeat, seatVent, drive, steering, hud, brightness, ambience, volume, mediaSource, desktopPins, buttonPreset, preset, scenario, adas.", 13, false));
        root.addView(name);
        root.addView(type);
        root.addView(identity);
        root.addView(body);
        root.addView(save);
        root.addView(apply);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showComfortClimate() {
        startActivity(new Intent(this, ClimateActivity.class));
    }

    private void showClimatePresets() {
        Intent intent = new Intent(this, ClimateActivity.class);
        intent.putExtra(ClimateActivity.EXTRA_MODE, ClimateActivity.MODE_PRESETS);
        startActivity(intent);
    }

    private void showClimateSmart() {
        Intent intent = new Intent(this, ClimateActivity.class);
        intent.putExtra(ClimateActivity.EXTRA_MODE, ClimateActivity.MODE_SMART);
        startActivity(intent);
    }

    private void showClimateAdvanced() {
        Intent intent = new Intent(this, ClimateActivity.class);
        intent.putExtra(ClimateActivity.EXTRA_MODE, ClimateActivity.MODE_ADVANCED);
        startActivity(intent);
    }

    private void showAvasDigitalKey() {
        LinearLayout root = commandRoot("Experimental: AVAS / Digital Key");
        addScreenMap(root, "Карта вкладки", "AVAS содержит экспериментальные настройки внешнего предупреждающего звука. Digital Key оставлен readback-only.",
                "AVAS", "Volume", "Sound", "Readback");
        root.addView(Ui.text(this, "AVAS - внешний звук предупреждения пешеходов у EV/PHEV. Отключение или смена громкости/типа звука может быть юридически и безопасностно спорной, поэтому раздел спрятан за Experimental features.", 14, false));
        Ui.section(root, "AVAS controls", "Switch, volume и sound type. Используйте только после проверки требований безопасности и законодательства.");
        addDiagnostic(root, "AVAS",
                EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH,
                EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_NAME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_PATH);
        addCommand(root, "AVAS switch on", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AVAS switch off", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "AVAS volume", EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                new String[]{"AVAS volume off", "AVAS volume low", "AVAS volume mid", "AVAS volume high"},
                new int[]{EcarxVehicleAdapter.AVAS_VOLUME_OFF, EcarxVehicleAdapter.AVAS_VOLUME_LOW, EcarxVehicleAdapter.AVAS_VOLUME_MID, EcarxVehicleAdapter.AVAS_VOLUME_HIGH});
        addCommandGroup(root, "AVAS sound type", EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                new String[]{"AVAS sound none", "AVAS sound 1", "AVAS sound 2", "AVAS sound 3", "AVAS sound 4", "AVAS sound 5", "AVAS sound 6", "AVAS sound 7", "AVAS sound 8"},
                new int[]{EcarxVehicleAdapter.AVAS_SOUND_NONE, EcarxVehicleAdapter.AVAS_SOUND_1, EcarxVehicleAdapter.AVAS_SOUND_2, EcarxVehicleAdapter.AVAS_SOUND_3, EcarxVehicleAdapter.AVAS_SOUND_4, EcarxVehicleAdapter.AVAS_SOUND_5, EcarxVehicleAdapter.AVAS_SOUND_6, EcarxVehicleAdapter.AVAS_SOUND_7, EcarxVehicleAdapter.AVAS_SOUND_8});
        Ui.section(root, "Digital Key readback", "Ниже только чтение статусов. Команды pair/unpair/delete/termination/suspension намеренно не добавлены.");
        root.addView(Ui.text(this, "Digital key ниже только читает статусы. Команды pair/unpair/delete/termination/suspension намеренно не добавлены.", 14, false));
        addDiagnostic(root, "Digital key statuses",
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_REQ_STS,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_UNPAIR,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TERMINATION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_SUSPENSION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_PAIRING_FAILED,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_WAIT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_RESULT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_RES_TIMEOUT);
    }

    private void showSceneModes() {
        LinearLayout root = commandRoot("Experimental: Сценарии");
        addScreenMap(root, "Карта вкладки", "Scene modes отправляют ON/OFF в готовые режимы автомобиля: Theater, Wash, Pet, Nap, Camping и другие.",
                "Cabin", "Comfort", "Media", "Rear");
        root.addView(Ui.text(this, "Сценарные режимы из ISceneMode.smali. Кнопки отправляют ON/OFF в соответствующий scene function.", 14, false));
        Ui.section(root, "Scene toggles", "Каждый режим добавлен парой ON/OFF. Проверяйте поддержку конкретной прошивки.");
        addDiagnostic(root, "Scene modes",
                EcarxVehicleAdapter.SCENE_THEATER,
                EcarxVehicleAdapter.SCENE_WASH,
                EcarxVehicleAdapter.SCENE_PET,
                EcarxVehicleAdapter.SCENE_SMOKING,
                EcarxVehicleAdapter.SCENE_PARENT_CHILD,
                EcarxVehicleAdapter.SCENE_ROMANTIC,
                EcarxVehicleAdapter.SCENE_NAP,
                EcarxVehicleAdapter.SCENE_QUEEN,
                EcarxVehicleAdapter.SCENE_SLEEP,
                EcarxVehicleAdapter.SCENE_CAMP,
                EcarxVehicleAdapter.SCENE_MEETING,
                EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO,
                EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
        addSceneToggle(root, "Theater", EcarxVehicleAdapter.SCENE_THEATER);
        addSceneToggle(root, "Wash", EcarxVehicleAdapter.SCENE_WASH);
        addSceneToggle(root, "Pet", EcarxVehicleAdapter.SCENE_PET);
        addSceneToggle(root, "Smoking", EcarxVehicleAdapter.SCENE_SMOKING);
        addSceneToggle(root, "Parent-child", EcarxVehicleAdapter.SCENE_PARENT_CHILD);
        addSceneToggle(root, "Romantic", EcarxVehicleAdapter.SCENE_ROMANTIC);
        addSceneToggle(root, "Nap", EcarxVehicleAdapter.SCENE_NAP);
        addSceneToggle(root, "Queen", EcarxVehicleAdapter.SCENE_QUEEN);
        addSceneToggle(root, "Sleep", EcarxVehicleAdapter.SCENE_SLEEP);
        addSceneToggle(root, "Camping", EcarxVehicleAdapter.SCENE_CAMP);
        addSceneToggle(root, "Meeting", EcarxVehicleAdapter.SCENE_MEETING);
        addSceneToggle(root, "Rear-row video", EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO);
        addSceneToggle(root, "PSD passenger theater", EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
    }

    private void addSceneToggle(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " on", functionId, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, label + " off", functionId, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showAmbienceLight() {
        LinearLayout root = commandRoot("Experimental: Подсветка");
        addScreenMap(root, "Карта вкладки", "Ambience light разделен на цвета, эффекты, control mode, welcome/music/voice и зоны.",
                "Color", "Effect", "Mode", "Zones");
        addAmbiencePreview(root);
        root.addView(Ui.text(this, "Ambience light из IAmbienceLight.smali: темы, цвета, weather/music/welcome/voice и зоны.", 14, false));
        Ui.section(root, "Ambience diagnostics", "Readback и поддержка подсветки видны в Developer diagnostics.");
        addDiagnostic(root, "Ambience light",
                EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WEATHER,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MAIN_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_TOP_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_BOT_ZONES);
        Ui.section(root, "Цвета и эффекты", "Выбор theme color, effect и режима управления подсветкой.");
        addCommandGroup(root, "Theme color", EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                new String[]{"Color red", "Color orange", "Color yellow", "Color green", "Color indigo", "Color blue", "Color violet", "Color white", "Color ice blue", "Color off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_RED, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ORANGE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_YELLOW, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_GREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_INDIGO, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_BLUE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_VIOLET, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WHITE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ICE_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Theme mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                new String[]{"Effect solid", "Effect gradients", "Effect breathe", "Theme radical", "Theme simple", "Theme liberating", "Theme agile", "Effect off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_SOLID, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_GRADIENTS, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_BREATHE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_RADICAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_SIMPLE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_LIBERATING, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_AGILE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Control mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                new String[]{"Control more", "Control music", "Control screen", "Control color", "Control time"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MORE, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MUSIC, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_SCREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_COLOR, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_TIME});
        addCommand(root, "Music show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Music show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Welcome show mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                new String[]{"Welcome passionate", "Welcome normal", "Welcome subdued", "Welcome off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_PASSIONATE, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_NORMAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SUBDUED, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Voice light on", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Voice light off", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Zones", EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                new String[]{"Zone all", "Zone front", "Zone headrest", "Zone rear"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_ALL, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_FRONT, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_HEADREST, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_REAR});
    }

    private void addAmbiencePreview(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        LinearLayout top = Ui.row(this);
        TextView title = Ui.text(this, "Визуал подсветки", 18, true);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(Ui.help(this, "Визуал подсветки", "Preview использует ассеты OneOS-ControlBoard: фон салона/двери и подсвеченный слой автомобиля. Нажмите по нижней части preview, чтобы сменить цвет локально; реальные команды ниже отправляют значения в AdaptAPI."));
        card.addView(top);
        card.addView(Ui.muted(this, "Касание по preview меняет демонстрационный цвет. Команды ниже выполняют реальную запись theme/effect/zone."));
        VehicleVisualView visual = new VehicleVisualView(this, true);
        card.addView(visual, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 260)));

        LinearLayout swatches = Ui.row(this);
        addSwatch(swatches, visual, Color.rgb(232, 83, 70));
        addSwatch(swatches, visual, Color.rgb(230, 143, 39));
        addSwatch(swatches, visual, Color.rgb(235, 197, 54));
        addSwatch(swatches, visual, Color.rgb(52, 137, 224));
        addSwatch(swatches, visual, Color.rgb(136, 80, 214));
        card.addView(swatches);
        root.addView(card, lpMatchWrap(0, 8, 0, 14));
    }

    private void addSwatch(LinearLayout row, VehicleVisualView visual, int color) {
        Button b = new Button(this);
        b.setText("");
        b.setMinHeight(Ui.dp(this, 44));
        b.setMinWidth(Ui.dp(this, 44));
        b.setBackground(Ui.cardBg(this, color, Ui.dp(this, 22), Color.argb(120, 255, 255, 255)));
        b.setOnClickListener(v -> visual.setAccent(color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void showDayMode() {
        LinearLayout root = commandRoot("Experimental: Яркость / DayMode");
        addScreenMap(root, "Карта вкладки", "DayMode управляет day/night/auto режимом и яркостью backlight, DIM, floodlight, screen и mirror.",
                "Day", "Night", "Brightness", "DIM");
        root.addView(Ui.text(this, "DayMode и яркость из IDayMode.smali. Для яркости значения 25/50/75 экспериментальные; сначала проверь min/max/step.", 14, false));
        Ui.section(root, "Brightness diagnostics", "Min/max/step и readback видны в Developer diagnostics.");
        addDiagnostic(root, "DayMode / brightness",
                EcarxVehicleAdapter.DAYMODE_SETTING,
                EcarxVehicleAdapter.DAYMODE_SYNC,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_DAY,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_NIGHT,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MIN,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MAX,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_STEP,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN,
                EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_DAY_TIME,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_NIGHT_TIME,
                EcarxVehicleAdapter.DAYMODE_SUN_TIME,
                EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_DAYMODE,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_SCREEN);
        Ui.section(root, "Day/Night mode", "Переключение day/night/auto, sync и linkage.");
        addCommandGroup(root, "DayMode", EcarxVehicleAdapter.DAYMODE_SETTING,
                new String[]{"DayMode day", "DayMode night", "DayMode auto", "DayMode off"},
                new int[]{EcarxVehicleAdapter.DAYMODE_VALUE_DAY, EcarxVehicleAdapter.DAYMODE_VALUE_NIGHT, EcarxVehicleAdapter.DAYMODE_VALUE_AUTO, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "DayMode sync on", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "DayMode sync off", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_OFF);
        addBrightnessCommands(root, "Backlight", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "DIM", EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS);
        addBrightnessCommands(root, "Floodlight", EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "Screen", EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN);
        addBrightnessCommands(root, "Electric rear-view mirror", EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR);
        addCommand(root, "Backlight linkage on", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Backlight linkage off", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Time-control theme on", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Time-control theme off", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void addBrightnessCommands(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " 25", functionId, 25);
        addCommand(root, label + " 50", functionId, 50);
        addCommand(root, label + " 75", functionId, 75);
    }

    private void addExperimentalDriveFeatures(LinearLayout root) {
        root.addView(Ui.text(this, "Experimental drive features: функции из IDriveMode.smali. Перед использованием смотри диагностику support/readback.", 14, false));
        addDiagnostic(root, "Experimental drive modes",
                EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                EcarxVehicleAdapter.DRIVE_CREEP_SET,
                EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL,
                EcarxVehicleAdapter.DRIVE_NOISE_CONTROL,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_VALUE,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MIN,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MAX,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_STEP,
                EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING,
                EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP);
        addCommandGroup(root, "Experimental: расширенные режимы движения", EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                new String[]{"Drive HDC", "Drive Mud", "Drive Rock", "Drive Sand", "Drive AWD", "Drive eAWD", "Drive Save", "Drive Pure", "Drive Hybrid", "Drive PHEV", "Drive Power", "Drive Normal", "Drive Eco HEV/PHEV", "Drive Eco Plus", "Drive Sport Plus", "Drive Adaptive", "Drive Custom", "Drive Start type 18", "Drive Start type 72", "Drive Start type 79", "Drive Start type 97"},
                new int[]{EcarxVehicleAdapter.DRIVE_MODE_HDC, EcarxVehicleAdapter.DRIVE_MODE_MUD, EcarxVehicleAdapter.DRIVE_MODE_ROCK, EcarxVehicleAdapter.DRIVE_MODE_SAND, EcarxVehicleAdapter.DRIVE_MODE_AWD, EcarxVehicleAdapter.DRIVE_MODE_EAWD, EcarxVehicleAdapter.DRIVE_MODE_SAVE, EcarxVehicleAdapter.DRIVE_MODE_PURE, EcarxVehicleAdapter.DRIVE_MODE_HYBRID, EcarxVehicleAdapter.DRIVE_MODE_PHEV, EcarxVehicleAdapter.DRIVE_MODE_POWER, EcarxVehicleAdapter.DRIVE_MODE_NORMAL, EcarxVehicleAdapter.DRIVE_MODE_ECO_HEV_PHEV, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE18, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE72, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE79, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE97});
        addCommandGroup(root, "Experimental: custom propulsion", EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                new String[]{"Propulsion Eco", "Propulsion Comfort", "Propulsion Sport", "Propulsion Offroad", "Propulsion Snow", "Propulsion Sand", "Propulsion Hybrid", "Propulsion Pure", "Propulsion Power", "Propulsion AWD", "Propulsion off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO, EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_SPORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_PROPULSION_SNOW, EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND, EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID, EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE, EcarxVehicleAdapter.CUSTOM_PROPULSION_POWER, EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: custom suspension", EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                new String[]{"Suspension Standard", "Suspension Comfort", "Suspension Sport", "Suspension Offroad", "Suspension Snow", "Suspension Automatic", "Suspension off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_SUSPENSION_STANDARD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SNOW, EcarxVehicleAdapter.CUSTOM_SUSPENSION_AUTOMATIC, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: steering feel", EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                new String[]{"Steering feel Light", "Steering feel Balanced", "Steering feel Heavy", "Steering feel off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT, EcarxVehicleAdapter.CUSTOM_STEERING_BALANCED, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: climate mode", EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                new String[]{"Drive climate Normal", "Drive climate Eco", "Drive climate off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: DIM theme", EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                new String[]{"DIM theme Red", "DIM theme Gold", "DIM theme Blue", "DIM theme off"},
                new int[]{EcarxVehicleAdapter.DIM_THEME_RED, EcarxVehicleAdapter.DIM_THEME_GOLD, EcarxVehicleAdapter.DIM_THEME_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: energy drive mode", EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                new String[]{"Energy Range", "Energy Tour", "Energy Sport", "Energy off"},
                new int[]{EcarxVehicleAdapter.ENERGY_MODE_RANGE, EcarxVehicleAdapter.ENERGY_MODE_TOUR, EcarxVehicleAdapter.ENERGY_MODE_SPORT, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: ESC level", EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                new String[]{"ESC level 1", "ESC level 2", "ESC level 3", "ESC level 4", "ESC level 5", "ESC off"},
                new int[]{EcarxVehicleAdapter.ESC_LEVEL_1, EcarxVehicleAdapter.ESC_LEVEL_2, EcarxVehicleAdapter.ESC_LEVEL_3, EcarxVehicleAdapter.ESC_LEVEL_4, EcarxVehicleAdapter.ESC_LEVEL_5, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: Startrack / champion", EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                new String[]{"Startrack type 18", "Startrack type 72", "Startrack type 79", "Startrack type 97", "Startrack off"},
                new int[]{EcarxVehicleAdapter.STARTRACK_TYPE18, EcarxVehicleAdapter.STARTRACK_TYPE72, EcarxVehicleAdapter.STARTRACK_TYPE79, EcarxVehicleAdapter.STARTRACK_TYPE97, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: risky toggles", EcarxVehicleAdapter.DRIVE_CREEP_SET,
                new String[]{"Creep on", "Creep off"},
                new int[]{EcarxVehicleAdapter.COMMON_ON, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Launch control on", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Launch control off", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Noise control on", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Noise control off", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Performance saving on", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Performance saving off", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Experimental: power-train-stop", EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP,
                new String[]{"Power train stop not blocked", "Power train stop EV blocked", "Power train stop HEV blocked", "Power train stop EV+ blocked"},
                new int[]{EcarxVehicleAdapter.POWER_TRAIN_STOP_NOT_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_HEV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_PLUS_BLOCKED});
    }

    private void showHud() {
        LinearLayout root = commandRoot("HUD / Cluster / OneOS");
        addHudOverview(root);
        addScreenMap(root, "Карта вкладки", "HUD-команды управляют отображением на проекторе, DIM и OneOS media bridge. Service-кнопки запускают фоновые мосты и нужны в основном для проверки интеграции.",
                "HUD", "DIM", "Media", "Service");
        root.addView(Ui.text(this, new EcarxHudDimAdapter(this).availability(), 14, false));
        Ui.section(root, "HUD display", "Включение HUD, calibration и отображаемые блоки: safety, media, navigation, phone и drive environment.");
        addDiagnostic(root, "HUD", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.HUD_DISPLAY_SAFETY);
        addCommand(root, "HUD включить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD выключить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "HUD calibration", EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD angle reset", EcarxVehicleAdapter.HUD_ANGLE_RESET, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD snow mode", EcarxVehicleAdapter.HUD_SNOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD safety on", EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD media on", EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD navi on", EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD phone on", EcarxVehicleAdapter.HUD_DISPLAY_BTPHONE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD drive env on", EcarxVehicleAdapter.HUD_DISPLAY_DRIVE_ENVIRONMENT, EcarxVehicleAdapter.COMMON_ON);
        Ui.section(root, "OneOS / DIM bridge", "Reflection-вызовы к HUDInteraction, DimInteraction и media bridge. Если прошивка не поддерживает классы, команда вернет диагностическое сообщение.");
        addHudDimAction(root, "HUDInteraction: статус", a -> a.hudStatus());
        addHudDimAction(root, "HUDInteraction: height/sync", a -> a.hudSync());
        addHudDimAction(root, "DIMInteraction: статус", a -> a.dimStatus());
        addHudDimAction(root, "DIM: запрос day/night", a -> a.requestDayNightMode());
        addHudDimAction(root, "DIM: presentation on", a -> a.setPresentation(true));
        addHudDimAction(root, "DIM: presentation off", a -> a.setPresentation(false));
        addHudDimAction(root, "DIM Menu: IHU ready/theme", a -> a.dimMenuReadyAndTheme());
        addHudDimAction(root, "DIM Menu: вкладка навигации", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_NAVIGATION));
        addHudDimAction(root, "DIM Menu: вкладка музыки", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addHudDimAction(root, "DIM Menu: control center", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        addHudDimAction(root, "DIM Navi: simplify", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_SIMPLIFY));
        addHudDimAction(root, "DIM Navi: AR", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_AR));
        addHudDimAction(root, "DIM volume 10", a -> a.setDimVolume(false, 10));
        addHudDimAction(root, "DIM climate unit Celsius", a -> a.climateCelsiusUnit());
        addHudDimAction(root, "DIM climate temp 22.0C", a -> a.climateTemp(22.0f));
        addHudDimAction(root, "DIM avg fuel sample", a -> a.updateAvgFuelRanking(0, "{\"source\":\"GFlow\",\"avg\":0}"));
        addHudDimAction(root, "DIM media mute", a -> a.publishMediaMuteState(1));
        addHudDimAction(root, "DIM media unmute", a -> a.publishMediaMuteState(0));
        addAudioExtAction(root, "AudioExt: bind services", a -> a.bindAudioExt());
        addAudioExtAction(root, "AudioExt: visualizer status", a -> a.visualizerStatus());
        addAudioExtAction(root, "AudioExt: media playing", a -> a.notifyMediaStatus(1, getPackageName()));
        addAudioExtAction(root, "AudioExt: media paused", a -> a.notifyMediaStatus(0, getPackageName()));
        addAudioExtAction(root, "AudioExt: VR active", a -> a.notifyVrStatus(1, 0));
        addAudioExtAction(root, "AudioExt: VR inactive", a -> a.notifyVrStatus(0, 0));
        addAudioExtAction(root, "AudioExt: PDC volume on", a -> a.notifyPdcVolumeSwitch(1));
        addAudioExtAction(root, "AudioExt: voice light 0.8", a -> a.voiceLight(0.8f));
        addAudioExtAction(root, "AudioExt: anti-shake on", a -> a.antiShake(true, 0.5f));
        addAudioExtAction(root, "AudioExt: loudness on", a -> a.loudness(true));
        addAudioExtAction(root, "AudioExt: section max on", a -> a.useSectionMax(true));
        addAudioExtAction(root, "AudioExt: voice base -35dB", a -> a.voiceDb(-35));
        addAudioExtAction(root, "AudioExt: spectrum preset", a -> a.spectrumPreset(0, 1, 1.0f, 1.0f));
        Button hud = addCommandRow(root, "Запустить HUD service", "FIRMWARE", "");
        hud.setOnClickListener(v -> startForegroundService(new Intent(this, HudPresentationService.class)));
        Button observer = addCommandRow(root, "Запустить HUD observer", "FIRMWARE", "");
        observer.setOnClickListener(v -> startForegroundService(new Intent(this, HudObserverService.class)));
        Button cluster = addCommandRow(root, "Запустить Cluster bridge", "FIRMWARE", "");
        cluster.setOnClickListener(v -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
    }

    private void addHudDimAction(LinearLayout root, String label, HudDimAction action) {
        Button b = addCommandRow(root, label, safetyFor(label), "");
        b.setOnClickListener(v -> {
            EcarxHudDimAdapter.Result result;
            try {
                result = action.run(new EcarxHudDimAdapter(this));
            } catch (Exception e) {
                result = EcarxHudDimAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "OneOS команда отправлена" : "OneOS команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    interface HudDimAction {
        EcarxHudDimAdapter.Result run(EcarxHudDimAdapter adapter);
    }

    private void addAudioExtAction(LinearLayout root, String label, AudioExtAction action) {
        Button b = addCommandRow(root, label, safetyFor(label), "");
        b.setOnClickListener(v -> {
            AudioExtServiceAdapter.Result result;
            try {
                result = action.run(new AudioExtServiceAdapter(this));
            } catch (Exception e) {
                result = AudioExtServiceAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "AudioExt команда отправлена" : "AudioExt команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    interface AudioExtAction {
        AudioExtServiceAdapter.Result run(AudioExtServiceAdapter adapter);
    }
    private void showLauncher() { startActivity(new Intent(this, DesktopActivity.class)); }
    private void showSystem() { panel("ADB / Система", "ADB toggle, локальный shell, adb-grants, DPI/масштаб, автозум, автозапуск, watchdog и accessibility tracking."); }
    private void showWeb() { startActivity(new Intent(this, WeatherActivity.class)); }
    private void openSplitLauncher() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(apps, Comparator.comparing(a -> a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        if (apps.isEmpty()) {
            Ui.toast(this, "Нет приложений для split-запуска");
            return;
        }
        String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) labels[i] = apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Запустить рядом").setItems(labels, (d, which) -> {
            ResolveInfo info = apps.get(which);
            Intent launch = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(info.activityInfo.packageName, info.activityInfo.name);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(launch);
        }).show();
    }
}
