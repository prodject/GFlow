package com.prodject.gflow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class VehicleActivity extends Activity {
    static final String EXTRA_MODE = "vehicle_mode";
    static final String MODE_SEATS = "seats";
    static final String MODE_MIRRORS = "mirrors";
    static final String MODE_LIGHTS = "lights";
    static final String MODE_DRIVE = "drive";
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout contentHost;
    private TextView topDoorsValue;
    private TextView topWindowsValue;
    private TextView topDriveValue;
    private TextView heroStatusValue;
    private TextView heroLocksValue;
    private TextView heroRoofValue;
    private TextView heroLightsValue;
    private Mode mode = Mode.HOME;
    private EcarxVehicleAdapter liveAdapter;
    private final Runnable stateTicker = new Runnable() {
        @Override public void run() {
            refreshState();
            handler.postDelayed(this, 20_000L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String launchMode = getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_SEATS.equals(launchMode)) mode = Mode.SEATS;
        else if (MODE_MIRRORS.equals(launchMode)) mode = Mode.MIRRORS;
        else if (MODE_LIGHTS.equals(launchMode)) mode = Mode.LIGHTS;
        else if (MODE_DRIVE.equals(launchMode)) mode = Mode.DRIVE;
        setContentView(buildVehicleShell());
        renderContent();
        refreshState();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(stateTicker);
        refreshState();
        startFunctionWatcher();
        handler.post(stateTicker);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(stateTicker);
        stopFunctionWatcher();
    }

    private View buildVehicleShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
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
        contentHost.removeAllViews();
        switch (mode) {
            case SEATS:
                contentHost.addView(buildSeatsPanel(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
                break;
            case MIRRORS:
                contentHost.addView(buildMirrorsPanel(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
                break;
            case LIGHTS:
                contentHost.addView(buildLightsPanel(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
                break;
            case DRIVE:
                contentHost.addView(buildDrivePanel(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
                break;
            case HOME:
            default:
                contentHost.addView(buildBodyControls(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 0));
                break;
        }
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, modeLabel()));
        TextView title = Ui.text(this, "Автомобиль", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        topDoorsValue = buildTopStat(bar, "Двери", "...");
        topWindowsValue = buildTopStat(bar, "Окна", "...");
        topDriveValue = buildTopStat(bar, "Режим", "...");
        return bar;
    }

    private String modeLabel() {
        switch (mode) {
            case SEATS: return "Сиденья / Память";
            case MIRRORS: return "Зеркала / Крыша";
            case LIGHTS: return "Свет / Экстерьер";
            case DRIVE: return "Drive / Профили";
            case HOME:
            default: return "Кузов / Доступ";
        }
    }

    private TextView buildTopStat(LinearLayout parent, String label, String value) {
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
        parent.addView(card, lp);
        return valueView;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Схема автомобиля"));

        LinearLayout top = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        heroStatusValue = metricLine("Статус кузова", "...");
        heroLocksValue = metricLine("Замки", "...");
        heroRoofValue = metricLine("Люк", "...");
        heroLightsValue = metricLine("Свет", "...");
        left.addView(heroStatusValue);
        left.addView(heroLocksValue);
        left.addView(heroRoofValue);
        left.addView(heroLightsValue);
        top.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        VehicleBodyView visual = new VehicleBodyView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 320), Ui.dp(this, 250));
        visualLp.leftMargin = Ui.dp(this, 12);
        top.addView(visual, visualLp);
        hero.addView(top);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Кузов", () -> openMode(Mode.HOME));
        addActionChip(quick, "Сиденья", () -> openMode(Mode.SEATS));
        addActionChip(quick, "Зеркала", () -> openMode(Mode.MIRRORS));
        addActionChip(quick, "Свет", () -> openMode(Mode.LIGHTS));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildBodyControls() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Управление кузовом"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Вод. дверь", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_LEFT, EcarxVehicleAdapter.DOOR_OPEN));
        addTile(grid, "Пасс. дверь", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_RIGHT, EcarxVehicleAdapter.DOOR_OPEN));
        addTile(grid, "Задние двери", Color.rgb(108, 132, 255), this::showDoorSheet);
        addTile(grid, "Окна вниз", Color.rgb(98, 162, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN));
        addTile(grid, "Окна вверх", Color.rgb(91, 209, 167), () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE));
        addTile(grid, "Капот", Color.rgb(159, 122, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_HOOD, EcarxVehicleAdapter.DOOR_OPEN));
        addTile(grid, "Багажник статус", Color.rgb(255, 138, 80), () -> showReadbackSheet("Багажник", compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_REAR).message)));
        addTile(grid, "Child lock", Color.rgb(255, 179, 64), () -> setRearChildLock(EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Багажник", Color.rgb(94, 201, 196), this::openTrunkOemEntry);
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Замки", () -> showActionSheet("Замки", new QuickItem[]{
                new QuickItem("Статус замков", () -> showReadbackSheet("Замки", compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message))),
                new QuickItem("Child lock", () -> setRearChildLock(EcarxVehicleAdapter.COMMON_ON))
        }));
        addActionChip(actions, "Двери", this::showDoorSheet);
        addActionChip(actions, "Окна", () -> showActionSheet("Окна", new QuickItem[]{
                new QuickItem("All Open", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("All Close", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE)),
                new QuickItem("Half", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_HALF))
        }));
        addActionChip(actions, "Кузов статусы", this::showBodyStatusSheet);
        addActionChip(actions, "Drive", () -> openMode(Mode.DRIVE));
        panel.addView(actions, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildSeatsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Сиденья / Память"));
        panel.addView(Ui.text(this, "Регулировка длины, высоты, спинки, memory positions и переход в полноценные профили.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Сиденье водителя", "Длина, высота, спинка", new QuickItem[]{
                new QuickItem("Вперед", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_FORWARD)),
                new QuickItem("Назад", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKWARD)),
                new QuickItem("Выше", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_UP)),
                new QuickItem("Ниже", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN))
        });
        addAdvancedCard(grid, "Спинка / Память", "Спинка, сохранение и вызов", new QuickItem[]{
                new QuickItem("Спинка +", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD)),
                new QuickItem("Спинка -", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD)),
                new QuickItem("Сохранить P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_STOCK_MEMORY_SAVE_2)),
                new QuickItem("Вызвать P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_STOCK_MEMORY_SET_1))
        });
        addAdvancedCard(grid, "Сиденье пассажира", "Stock zone 0x4", new QuickItem[]{
                new QuickItem("Вперед", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.SEAT_FORWARD)),
                new QuickItem("Назад", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.SEAT_BACKWARD)),
                new QuickItem("Спинка +", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD)),
                new QuickItem("Спинка -", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout memory = Ui.row(this);
        addActionChip(memory, "Сохранить stock", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_STOCK_MEMORY_SAVE_2));
        addActionChip(memory, "Вызвать stock", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_STOCK_MEMORY_SET_1));
        addActionChip(memory, "Комфорт", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(memory, "Профили", () -> startActivity(new Intent(this, ProfileActivity.class)));
        panel.addView(memory, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildMirrorsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Зеркала / Крыша"));
        panel.addView(Ui.text(this, "Зеркала, обогрев, люк, шторка и штатный диалог зеркал.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Зеркала", "Складывание, регулировка, обогрев", new QuickItem[]{
                new QuickItem("Сложить", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Левое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Правое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Обогрев", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Люк / Шторка", "Люк и солнцезащитная шторка", new QuickItem[]{
                new QuickItem("Люк открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Люк закрыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Шторка открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Шторка закрыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Диалог зеркал", this::showMirrorDialogSheet);
        addActionChip(actions, "Люк tilt", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_TILT, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(actions, "Roof init", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_INIT, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(actions, "Позиция A", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_A));
        addActionChip(actions, "Позиция B", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_B));
        addActionChip(actions, "Назад", () -> openMode(Mode.HOME));
        panel.addView(actions, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildLightsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Свет / Экстерьер"));
        panel.addView(Ui.text(this, "Наружный свет, поворотники, grille/welcome и atmosphere lamp перенесены в новый экран.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Core Lights", "Dipped, main, DRL, hazard", new QuickItem[]{
                new QuickItem("Ближний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Дальний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("DRL", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Аварийка", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Accent Lights", "Grille, welcome, rear logo", new QuickItem[]{
                new QuickItem("Grille", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Welcome", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Approach", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_LAMP_APPROACH_LIGHT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Courtesy", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_LAMP_COURTESY_LIGHT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rear logo", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Atmosphere", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Signals", "Left/right/fog/plate", new QuickItem[]{
                new QuickItem("Left turn", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Right turn", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Front fog", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Plate", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Wiper / Washer", "Auto, low, high, washer", new QuickItem[]{
                new QuickItem("Wiper Auto", () -> sendVehicle(EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_AUTO)),
                new QuickItem("Wiper Low", () -> sendVehicle(EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_LOW)),
                new QuickItem("Wiper High", () -> sendVehicle(EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_HIGH)),
                new QuickItem("Washer", () -> sendVehicle(EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildDrivePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Drive / Профили"));
        panel.addView(Ui.text(this, experimentalFeaturesEnabled()
                ? "Drive modes, steering feel, custom keys и расширенный experimental drive flow вынесены в новый экран."
                : "Drive modes, steering feel, custom keys и переход в отдельные пользовательские профили.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Режимы движения", "Eco, Comfort, Dynamic, Snow", new QuickItem[]{
                new QuickItem("Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC)),
                new QuickItem("Snow", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW))
        });
        addAdvancedCard(grid, "Руль / Кастом", "Усилие и пользовательские кнопки", new QuickItem[]{
                new QuickItem("Soft", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC)),
                new QuickItem("Open 360", this::openAvmCamera),
                new QuickItem("Custom Drive", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DRIVING_MODE))
        });
        addAdvancedCard(grid, "Штатные переключатели", "Пары function/zone/value из stock settings log", new QuickItem[]{
                new QuickItem("Start/Stop OFF", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_ENGINE_STOP_START, EcarxVehicleAdapter.COMMON_OFF)),
                new QuickItem("Auto Hold ON", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_AUTO_HOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("EPB Auto ON", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_PBC_AUTO_APPLY, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("WPC Auto", () -> sendVehicle(EcarxVehicleAdapter.WPC_WORK_MODE, EcarxVehicleAdapter.WPC_WORK_MODE_AUTO))
        });
        addAdvancedCard(grid, "Штатные safety", "ESC Sport, HDC, easy ingress", new QuickItem[]{
                new QuickItem("ESC Sport", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_ESC_SPORT_MODE, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("HDC ON", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_HDC_SWITCH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Easy Ingress", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_EASY_INGRESS_EGRESS, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("WPC OFF", () -> sendVehicle(EcarxVehicleAdapter.WPC_WORK_MODE, EcarxVehicleAdapter.COMMON_OFF))
        });
        addAdvancedCard(grid, "Stock Settings", "Замки, поиск, звук", new QuickItem[]{
                new QuickItem("Auto close", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_AUTO_CLOSE_WINDOW, 0x20080401)),
                new QuickItem("Lock sound", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_AUDIBLE_LOCKING_FEEDBACK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Locator light", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_CAR_LOCATOR_REMINDER_MODE, 0x20160402)),
                new QuickItem("P unlock", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_PGEAR_UNLOCK, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Stock Advanced", "Approach unlock, sound, steering", new QuickItem[]{
                new QuickItem("Approach unlock", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_APPROACH_UNLOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Warn volume mid", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_SOUND_WARNING_VOLUME, 0x201d0102)),
                new QuickItem("Button sound 1", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_SOFT_BUTTON_SOUND_TYPE, 0x2e020101)),
                new QuickItem("Steer medium", () -> sendVehicle(EcarxVehicleAdapter.VEHICLE_STEERING_ASSISTANCE_LEVEL, EcarxVehicleAdapter.STEERING_ASSISTANCE_MEDIUM))
        });
        addAdvancedCard(grid, "OEM Custom Keys", "Подтвержденные точки входа из GInputBridge", new QuickItem[]{
                new QuickItem("Trunk", this::openTrunkOemEntry),
                new QuickItem("DVR", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR)),
                new QuickItem("Navigation", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_NAVIGATION)),
                new QuickItem("Full Map", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DIM_FULL_SCREEN_MAP))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        if (experimentalFeaturesEnabled()) {
            panel.addView(buildExperimentalDrivePanel(), lpMatchWrap(0, 0, 0, 12));
        } else {
            panel.addView(Ui.muted(this, "Включите Experimental features в настройках, чтобы открыть PURE/HYBRID/POWER, AWD/SAVE/ADAPTIVE, custom propulsion/suspension/climate и risky drive toggles."), lpMatchWrap(0, 0, 0, 12));
        }

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Профили", () -> startActivity(new Intent(this, ProfileActivity.class)));
        addActionChip(actions, "Seats", () -> openMode(Mode.SEATS));
        addActionChip(actions, "Lights", () -> openMode(Mode.LIGHTS));
        addActionChip(actions, "Home", () -> openMode(Mode.HOME));
        panel.addView(actions, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildExperimentalDrivePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Экспериментальный drive"));
        panel.addView(Ui.muted(this, "Полный набор drive-mode и custom-profile команд перенесен из legacy-ветки и доступен только при включенном experimental gate."));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Extended Modes I", "Offroad, HDC, Mud, Rock", new QuickItem[]{
                new QuickItem("Offroad", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD)),
                new QuickItem("HDC", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HDC)),
                new QuickItem("Mud", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_MUD)),
                new QuickItem("Rock", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ROCK))
        });
        addAdvancedCard(grid, "Extended Modes II", "Sand, AWD, eAWD, Save", new QuickItem[]{
                new QuickItem("Sand", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAND)),
                new QuickItem("AWD", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_AWD)),
                new QuickItem("eAWD", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_EAWD)),
                new QuickItem("Save", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAVE))
        });
        addAdvancedCard(grid, "Hybrid Modes", "Pure, Hybrid, PHEV, Power", new QuickItem[]{
                new QuickItem("Pure", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PURE)),
                new QuickItem("Hybrid", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HYBRID)),
                new QuickItem("PHEV", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PHEV)),
                new QuickItem("Power", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_POWER))
        });
        addAdvancedCard(grid, "Adaptive / Custom", "Adaptive, custom, eco+, sport+", new QuickItem[]{
                new QuickItem("Adaptive", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE)),
                new QuickItem("Custom", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM)),
                new QuickItem("Eco Plus", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS)),
                new QuickItem("Sport Plus", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout profileRows = new LinearLayout(this);
        profileRows.setOrientation(LinearLayout.VERTICAL);
        profileRows.addView(buildDriveActionRow(new QuickItem[]{
                new QuickItem("Prop Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO)),
                new QuickItem("Prop Hybrid", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID)),
                new QuickItem("Prop Pure", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE)),
                new QuickItem("Prop AWD", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD))
        }), lpMatchWrap(0, 0, 0, 10));
        profileRows.addView(buildDriveActionRow(new QuickItem[]{
                new QuickItem("Susp Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT)),
                new QuickItem("Susp Sport", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT)),
                new QuickItem("Steer Light", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT)),
                new QuickItem("Steer Heavy", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY))
        }), lpMatchWrap(0, 0, 0, 10));
        profileRows.addView(buildDriveActionRow(new QuickItem[]{
                new QuickItem("Climate Normal", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL)),
                new QuickItem("Climate Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO)),
                new QuickItem("Energy Sport", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_ENERGY_MODE, EcarxVehicleAdapter.ENERGY_MODE_SPORT))
        }), lpMatchWrap(0, 0, 0, 10));
        profileRows.addView(buildDriveActionRow(new QuickItem[]{
                new QuickItem("Perf Save", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("PTS Ready", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP, EcarxVehicleAdapter.POWER_TRAIN_STOP_NOT_BLOCKED))
        }), lpMatchWrap(0, 0, 0, 0));
        panel.addView(profileRows);
        panel.addView(buildDriveThemeAndStartPanel(), lpMatchWrap(0, 16, 0, 0));
        return panel;
    }

    private LinearLayout buildDriveThemeAndStartPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Тема приборки / Старт"));
        panel.addView(Ui.muted(this, "Экспериментальные элементы для темы приборки, синхронизации с drive mode, стиля driver info и стартового поведения силовой установки."));

        LinearLayout syncRow = Ui.row(this);
        addActionChip(syncRow, "Синхр. темы вкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SYNC, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(syncRow, "Синхр. темы выкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SYNC, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(syncRow, "Driver Info Standard", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_STANDARD));
        addActionChip(syncRow, "Driver Info Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_ECO));
        panel.addView(syncRow, lpMatchWrap(0, 12, 0, 0));

        LinearLayout dimThemes = Ui.row(this);
        addActionChip(dimThemes, "DIM Red", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_RED));
        addActionChip(dimThemes, "DIM Gold", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_GOLD));
        addActionChip(dimThemes, "DIM Blue", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_BLUE));
        addActionChip(dimThemes, "DIM Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(dimThemes, lpMatchWrap(0, 12, 0, 0));

        LinearLayout infoRow = Ui.row(this);
        addActionChip(infoRow, "Driver Info Sport", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_SPORT));
        addActionChip(infoRow, "Driver Info Offroad", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_OFFROAD));
        addActionChip(infoRow, "Driver Info Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(infoRow, "Тема info Clear", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_INFOR_THEME, EcarxVehicleAdapter.CUSTOM_INFOR_THEME_CLEAR));
        panel.addView(infoRow, lpMatchWrap(0, 12, 0, 0));

        LinearLayout creepRow = Ui.row(this);
        addActionChip(creepRow, "Creep вкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CREEP_SET, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(creepRow, "Creep выкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CREEP_SET, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(creepRow, "Launch вкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(creepRow, "Launch выкл", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(creepRow, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Кузов", bodyReadback(), Ui.SUCCESS);
        addStatusCard(grid, "Багажник", trunkReadback(), Color.rgb(94, 201, 196));
        addStatusCard(grid, "Свет", readback(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.BCM_LIGHT_GRILLE), Ui.CYAN);
        addStatusCard(grid, "Drive / Сиденья", readback(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.SEAT_POSITION_SET), Ui.WARNING);
        addStatusCard(grid, "Люк / Зеркала", readback(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.BCM_MIRROR_FOLD), Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 14, false);
        v.setTextColor(Ui.primaryText(this));
        card.addView(v);
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        card.setOnClickListener(view -> showReadbackSheet(title, value));
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
        addDockButton(dock, "Кузов", () -> openMode(Mode.HOME), mode == Mode.HOME, new QuickItem[]{
                new QuickItem("Замки", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Окна", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("Багажник", this::openTrunkOemEntry)
        });
        addDockButton(dock, "Сиденья", () -> openMode(Mode.SEATS), mode == Mode.SEATS, new QuickItem[]{
                new QuickItem("Сиденье P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_MEMORY_1)),
                new QuickItem("Сиденье P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_MEMORY_2)),
                new QuickItem("Комфорт", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Зеркала", () -> openMode(Mode.MIRRORS), mode == Mode.MIRRORS, new QuickItem[]{
                new QuickItem("Сложить", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Обогрев", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Люк", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Свет", () -> openMode(Mode.LIGHTS), mode == Mode.LIGHTS, new QuickItem[]{
                new QuickItem("Ближний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Аварийка", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Welcome", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Drive", () -> openMode(Mode.DRIVE), mode == Mode.DRIVE, new QuickItem[]{
                new QuickItem("Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC))
        });
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
        tile.setOnClickListener(v -> action.run());
        tile.setOnLongClickListener(v -> {
            showActionSheet(label, new QuickItem[]{
                    new QuickItem("Открыть раздел", () -> openMode(Mode.HOME)),
                    new QuickItem("Свет", () -> openMode(Mode.LIGHTS)),
                    new QuickItem("Drive", () -> openMode(Mode.DRIVE))
            });
            return true;
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private void addAdvancedCard(GridLayout grid, String title, String body, QuickItem[] items) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, body, 15, false));
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
            button.setBackground(Ui.cardBg(this,
                    Ui.dark(this) ? Color.argb(56, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                    Ui.dp(this, 16),
                    Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
            button.setOnClickListener(v -> item.action.run());
            card.addView(button, lpMatchWrap(0, 8, 0, 0));
        }
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
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

    private LinearLayout buildDriveActionRow(QuickItem[] items) {
        LinearLayout row = Ui.row(this);
        for (QuickItem item : items) addActionChip(row, item.label, item.action);
        return row;
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active, QuickItem[] items) {
        Button button = Ui.button(this, label);
        button.setTextColor(active || Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(238, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214))));
        button.setOnClickListener(v -> action.run());
        button.setOnLongClickListener(v -> {
            showActionSheet(label, items);
            return true;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void openMode(Mode next) {
        mode = next;
        renderContent();
        refreshState();
    }

    private void sendVehicle(int functionId, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        if (!adapter.isWritable(functionId)) {
            Ui.toast(this, "Функция переведена в diagnostics/readback-only");
            refreshState();
            return;
        }
        int zone = adapter.spec(functionId).defaultZone;
        if (CarFunctionSelector.shouldSelect(this, functionId, zone, value)) {
            CarFunctionSelector.show(this, labelFor(functionId), functionId, zone, this::sendVehicleDirect);
            return;
        }
        sendVehicleDirect(functionId, zone, value);
    }

    private void sendVehicle(int functionId, int zone, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        if (!adapter.isWritable(functionId)) {
            Ui.toast(this, "Функция переведена в diagnostics/readback-only");
            refreshState();
            return;
        }
        if (CarFunctionSelector.shouldSelect(this, functionId, zone, value)) {
            CarFunctionSelector.show(this, labelFor(functionId), functionId, zone, this::sendVehicleDirect);
            return;
        }
        sendVehicleDirect(functionId, zone, value);
    }

    private void sendVehicleDirect(int functionId, int zone, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.support(functionId, zone);
        if (!support.isSupported()) {
            Ui.toast(this, "Функция недоступна для этой зоны");
            refreshState();
            return;
        }
        EcarxVehicleAdapter.Result result = adapter.set(functionId, zone, value);
        Ui.toast(this, result.success ? "Команда отправлена" : result.message);
        refreshState();
    }

    private String labelFor(int functionId) {
        CarFunctionCatalog.Entry entry = new EcarxVehicleAdapter(this).catalogEntry(functionId);
        return entry == null ? EcarxVehicleAdapter.hex(functionId) : entry.description.isEmpty() ? entry.key : entry.description;
    }

    private void openAvmCamera() {
        EcarxDvrAdapter.Result result = new EcarxDvrAdapter(this).openEvs(EcarxDvrAdapter.EVS_CAMERA_AVM);
        Ui.toast(this, result.success ? "360 открыт через EVS" : "360 не открыт: " + result.message);
    }

    private void openTrunkOemEntry() {
        EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        Ui.toast(this, result.success ? "OEM вход багажника отправлен" : "OEM вход багажника не выполнен");
        refreshState();
    }

    private void showDoorSheet() {
        showActionSheet("Двери", new QuickItem[]{
                new QuickItem("Водитель открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_LEFT, EcarxVehicleAdapter.DOOR_OPEN)),
                new QuickItem("Пассажир открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_RIGHT, EcarxVehicleAdapter.DOOR_OPEN)),
                new QuickItem("Задняя левая открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_2_LEFT, EcarxVehicleAdapter.DOOR_OPEN)),
                new QuickItem("Задняя правая открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_2_RIGHT, EcarxVehicleAdapter.DOOR_OPEN)),
                new QuickItem("Задняя дверь статус", () -> showReadbackSheet("BCM_DOOR_STATUS rear", compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_REAR).message)))
        });
    }

    private void showBodyStatusSheet() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        String value = "Driver: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_ROW_1_LEFT).message)
                + "\nPassenger: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_ROW_1_RIGHT).message)
                + "\nRear left: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_ROW_2_LEFT).message)
                + "\nRear right: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_ROW_2_RIGHT).message)
                + "\nHood: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_HOOD).message)
                + "\nRear: " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_REAR).message);
        showReadbackSheet("Кузов статусы", value);
    }

    private void setRearChildLock(int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result left = adapter.set(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.BCM_DOOR_ROW_2_LEFT, value);
        EcarxVehicleAdapter.Result right = adapter.set(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.BCM_DOOR_ROW_2_RIGHT, value);
        Ui.toast(this, left.success && right.success ? "Команда отправлена" : "Команда не выполнена");
        refreshState();
    }

    private void showMirrorDialogSheet() {
        showActionSheet("Диалог зеркал", new QuickItem[]{
                new QuickItem("Открыть OEM-диалог", () -> {
                    EcarxControlBoardAdapter.Result result = new EcarxControlBoardAdapter(this).showMirrorDialog();
                    Ui.toast(this, result.success ? "Диалог открыт" : "Диалог не открыт");
                }),
                new QuickItem("Левое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Правое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE))
        });
    }

    private void showReadbackSheet(String title, String value) {
        showActionSheet(title, new QuickItem[]{
                new QuickItem(value, this::refreshState),
                new QuickItem("Обновить", this::refreshState),
                new QuickItem("Кузов", () -> openMode(Mode.HOME))
        });
    }

    private void showActionSheet(String title, QuickItem[] items) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        sheet.addView(Ui.label(this, "Действия автомобиля"));
        sheet.addView(Ui.text(this, title, 24, true));
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
            button.setBackground(Ui.cardBg(this,
                    Ui.dark(this) ? Color.argb(56, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                    Ui.dp(this, 18),
                    Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
            button.setOnClickListener(v -> {
                dialog.dismiss();
                item.action.run();
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

    private void refreshState() {
        if (topDoorsValue != null) topDoorsValue.setText(bodyDoorSummary());
        if (topWindowsValue != null) topWindowsValue.setText(bodyWindowSummary());
        if (topDriveValue != null) topDriveValue.setText(compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.DRIVE_MODE_SELECT).message));
        if (heroStatusValue != null) heroStatusValue.setText("Статус кузова: " + bodyStatusSummary());
        if (heroLocksValue != null) heroLocksValue.setText("Замки: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message));
        if (heroRoofValue != null) heroRoofValue.setText("Люк: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_SUNROOF_OPEN).message));
        if (heroLightsValue != null) heroLightsValue.setText("Свет: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM).message));
    }

    private void startFunctionWatcher() {
        stopFunctionWatcher();
        liveAdapter = new EcarxVehicleAdapter(this);
        liveAdapter.watchFunctions(new EcarxVehicleAdapter.FunctionWatcher() {
            @Override public void onChanged(int functionId) {}

            @Override public void onIntValue(int functionId, int zone, int value) {
                runOnUiThread(() -> updateLiveValue(functionId, value));
            }

            @Override public void onFloatValue(int functionId, int zone, float value) {}

            @Override public void onSupportChanged(int functionId, int zone, String status) {
                runOnUiThread(() -> updateLiveStatus(functionId, status));
            }

            @Override public void onSupportedValuesChanged(int functionId, int[] values) {}
        }, EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_LOCK,
                EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_SUNROOF_OPEN,
                EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.DRIVE_MODE_SELECT);
    }

    private void stopFunctionWatcher() {
        if (liveAdapter == null) return;
        liveAdapter.unwatchFunctions();
        liveAdapter = null;
    }

    private void updateLiveValue(int functionId, int value) {
        String text = EcarxVehicleAdapter.hex(value);
        if (functionId == EcarxVehicleAdapter.BCM_DOOR_STATUS && heroStatusValue != null) heroStatusValue.setText("Двери: " + text);
        else if (functionId == EcarxVehicleAdapter.BCM_DOOR_LOCK && heroLocksValue != null) heroLocksValue.setText("Замки: " + text);
        else if (functionId == EcarxVehicleAdapter.BCM_WINDOW && topWindowsValue != null) topWindowsValue.setText(text);
        else if (functionId == EcarxVehicleAdapter.BCM_SUNROOF_OPEN && heroRoofValue != null) heroRoofValue.setText("Люк: " + text);
        else if (functionId == EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM && heroLightsValue != null) heroLightsValue.setText("Свет: " + text);
        else if (functionId == EcarxVehicleAdapter.DRIVE_MODE_SELECT && topDriveValue != null) topDriveValue.setText(text);
    }

    private void updateLiveStatus(int functionId, String status) {
        if (functionId == EcarxVehicleAdapter.BCM_DOOR_STATUS && heroStatusValue != null) heroStatusValue.setText("Двери: " + status);
        else if (functionId == EcarxVehicleAdapter.DRIVE_MODE_SELECT && topDriveValue != null) topDriveValue.setText(status);
    }

    private String readback(int... ids) {
        StringBuilder sb = new StringBuilder();
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        for (int id : ids) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(compact(adapter.get(id).message));
        }
        return sb.toString();
    }

    private String compact(String message) {
        if (message == null || message.trim().isEmpty()) return "--";
        String line = message.replace('\n', ' ').trim();
        line = line.replace("getFunctionValue", "").replace("getCustomizeFunctionValue", "").trim();
        int eq = line.indexOf('=');
        if (eq >= 0 && eq + 1 < line.length()) line = line.substring(eq + 1).trim();
        return line.length() > 84 ? line.substring(0, 84) : line;
    }

    private String bodyReadback() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        StringBuilder sb = new StringBuilder();
        sb.append("DOOR_STATUS ").append(rawStatus(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS))).append("\n");
        sb.append("DOOR_POS rear ").append(floatBodyReadback(EcarxVehicleAdapter.BCM_DOOR_POS, EcarxVehicleAdapter.BCM_DOOR_REAR)).append("\n");
        sb.append("DOOR_LOCK ").append(compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message)).append("\n");
        sb.append("WINDOW ").append(compact(adapter.get(EcarxVehicleAdapter.BCM_WINDOW).message)).append("\n");
        sb.append("WINDOW_POS FL ").append(floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_LEFT))
                .append(" · FR ").append(floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_RIGHT)).append("\n");
        sb.append("WINDOW_CUR FL ").append(floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_LEFT))
                .append(" · FR ").append(floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_RIGHT));
        return sb.toString();
    }

    private String bodyDoorSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        return rawStatus(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS))
                + " · rear " + floatBodyReadback(EcarxVehicleAdapter.BCM_DOOR_POS, EcarxVehicleAdapter.BCM_DOOR_REAR);
    }

    private String bodyWindowSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        String moving = compact(adapter.get(EcarxVehicleAdapter.BCM_WINDOW_MOVING_STATE).message);
        String frontLeft = floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_LEFT);
        String frontRight = floatBodyReadback(EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_RIGHT);
        return "FL " + frontLeft + " · FR " + frontRight + " · mov " + moving;
    }

    private String bodyStatusSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        return rawStatus(adapter.get(EcarxVehicleAdapter.BCM_DOOR_STATUS))
                + " · lock " + compact(adapter.get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message);
    }

    private String trunkReadback() {
        EcarxSafetyAdapter adapter = new EcarxSafetyAdapter(this);
        String state = compactTrunkState(adapter.get(EcarxSafetyAdapter.SETTING_FUNC_TRUNK_STATE, EcarxVehicleAdapter.BCM_DOOR_REAR).message);
        String percentage = compact(adapter.getFloat(EcarxSafetyAdapter.SETTING_FUNC_TRUNK_OPENING_PERCENTAGE, EcarxVehicleAdapter.BCM_DOOR_REAR).message);
        return state + " · " + percentage;
    }

    private String rawStatus(EcarxVehicleAdapter.Result result) {
        if (result == null) return "--";
        return binary32(result.value);
    }

    private String compactTrunkState(String message) {
        if (message == null || message.trim().isEmpty()) return "--";
        int raw = parseHexValue(message);
        switch (raw) {
            case EcarxSafetyAdapter.TRUNK_STATE_FULL_CLOSE: return "full_close";
            case EcarxSafetyAdapter.TRUNK_STATE_MOVE_UP: return "move_up";
            case EcarxSafetyAdapter.TRUNK_STATE_MOVE_UP_BREAK: return "move_up_break";
            case EcarxSafetyAdapter.TRUNK_STATE_STOP_DURING_OPEN: return "stop_open";
            case EcarxSafetyAdapter.TRUNK_STATE_FULL_OPEN: return "full_open";
            case EcarxSafetyAdapter.TRUNK_STATE_MOVE_DOWN: return "move_down";
            case EcarxSafetyAdapter.TRUNK_STATE_MOVE_DOWN_BREAK: return "move_down_break";
            case EcarxSafetyAdapter.TRUNK_STATE_STOP_DURING_CLOSE: return "stop_close";
            case EcarxSafetyAdapter.TRUNK_STATE_HALF_CLOSE: return "half_close";
            case EcarxSafetyAdapter.TRUNK_STATE_STOP_MIN_POSITION: return "stop_min";
            case EcarxSafetyAdapter.TRUNK_STATE_UNKNOW: return "unknown";
            default: return compact(message);
        }
    }

    private int parseHexValue(String message) {
        int marker = message.lastIndexOf("0x");
        if (marker < 0) return Integer.MIN_VALUE;
        int end = marker + 2;
        while (end < message.length() && Character.digit(message.charAt(end), 16) >= 0) end++;
        try {
            return (int) Long.parseLong(message.substring(marker + 2, end), 16);
        } catch (Exception ignored) {
            return Integer.MIN_VALUE;
        }
    }

    private String floatBodyReadback(int functionId, int zone) {
        EcarxVehicleAdapter.Result support = new EcarxVehicleAdapter(this).support(functionId, zone);
        if (support != null && !support.isSupported()) return "--";
        return compact(new EcarxVehicleAdapter(this).getFloat(functionId, zone).message);
    }

    private String binary32(int value) {
        return String.format(Locale.US, "0x%08x bits=%32s", value, Integer.toBinaryString(value)).replace(' ', '0');
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private boolean experimentalFeaturesEnabled() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private enum Mode {
        HOME,
        SEATS,
        MIRRORS,
        LIGHTS,
        DRIVE
    }

    private static final class QuickItem {
        final String label;
        final Runnable action;

        QuickItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private static final class VehicleBodyView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        VehicleBodyView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(40, 255, 255, 255));
            canvas.drawOval(new RectF(w * 0.18f, h * 0.76f, w * 0.82f, h * 0.95f), paint);

            path.reset();
            path.moveTo(cx, h * 0.10f);
            path.cubicTo(w * 0.70f, h * 0.16f, w * 0.80f, h * 0.42f, w * 0.78f, h * 0.78f);
            path.lineTo(w * 0.62f, h * 0.88f);
            path.lineTo(w * 0.38f, h * 0.88f);
            path.lineTo(w * 0.22f, h * 0.78f);
            path.cubicTo(w * 0.20f, h * 0.42f, w * 0.30f, h * 0.16f, cx, h * 0.10f);
            paint.setColor(Color.argb(232, 235, 242, 248));
            canvas.drawPath(path, paint);

            paint.setColor(Color.rgb(50, 67, 86));
            canvas.drawRoundRect(new RectF(w * 0.37f, h * 0.21f, w * 0.63f, h * 0.38f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);

            paint.setColor(Color.argb(110, 77, 163, 255));
            canvas.drawRoundRect(new RectF(w * 0.18f, h * 0.44f, w * 0.28f, h * 0.72f), Ui.dp(getContext(), 12), Ui.dp(getContext(), 12), paint);
            canvas.drawRoundRect(new RectF(w * 0.72f, h * 0.44f, w * 0.82f, h * 0.72f), Ui.dp(getContext(), 12), Ui.dp(getContext(), 12), paint);
            paint.setColor(Color.argb(110, 255, 179, 64));
            canvas.drawRoundRect(new RectF(w * 0.40f, h * 0.08f, w * 0.60f, h * 0.13f), Ui.dp(getContext(), 10), Ui.dp(getContext(), 10), paint);
            paint.setColor(Color.argb(110, 77, 208, 127));
            canvas.drawRoundRect(new RectF(w * 0.32f, h * 0.78f, w * 0.68f, h * 0.87f), Ui.dp(getContext(), 12), Ui.dp(getContext(), 12), paint);
        }
    }
}
