package com.prodject.gflow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
        handler.post(stateTicker);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(stateTicker);
    }

    private View buildVehicleShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
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
            case SEATS: return "Seats / Memory";
            case MIRRORS: return "Mirrors / Roof";
            case LIGHTS: return "Lights / Exterior";
            case DRIVE: return "Drive / Profiles";
            case HOME:
            default: return "Body / Access";
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
        hero.addView(Ui.label(this, "Vehicle Visual"));

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
        panel.addView(Ui.label(this, "Body Controls"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Запереть", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Открыть", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF));
        addTile(grid, "Окна вниз", Color.rgb(98, 162, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN));
        addTile(grid, "Окна вверх", Color.rgb(91, 209, 167), () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE));
        addTile(grid, "Люк открыть", Color.rgb(159, 122, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Люк закрыть", Color.rgb(115, 136, 165), () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Шторка открыть", Color.rgb(255, 138, 80), () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Шторка закрыть", Color.rgb(255, 179, 64), () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Багажник", Color.rgb(94, 201, 196), () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Замки", () -> showActionSheet("Замки", new QuickItem[]{
                new QuickItem("Lock On", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Lock Off", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF)),
                new QuickItem("Child lock", () -> sendVehicle(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_ON))
        }));
        addActionChip(actions, "Окна", () -> showActionSheet("Окна", new QuickItem[]{
                new QuickItem("All Open", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("All Close", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE)),
                new QuickItem("Half", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_HALF))
        }));
        addActionChip(actions, "Люк", () -> openMode(Mode.MIRRORS));
        addActionChip(actions, "Drive", () -> openMode(Mode.DRIVE));
        panel.addView(actions, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildSeatsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Seats / Memory"));
        panel.addView(Ui.text(this, "Регулировка длины, высоты, спинки, memory positions и переход в полноценные профили.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Driver seat", "Длина, высота, спинка", new QuickItem[]{
                new QuickItem("Вперед", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_FORWARD)),
                new QuickItem("Назад", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKWARD)),
                new QuickItem("Выше", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_UP)),
                new QuickItem("Ниже", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN))
        });
        addAdvancedCard(grid, "Backrest / Memory", "Спинка, save/set", new QuickItem[]{
                new QuickItem("Спинка +", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD)),
                new QuickItem("Спинка -", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD)),
                new QuickItem("Save P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_1)),
                new QuickItem("Recall P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout memory = Ui.row(this);
        addActionChip(memory, "Save P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_2));
        addActionChip(memory, "Recall P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2));
        addActionChip(memory, "Comfort", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(memory, "Профили", () -> startActivity(new Intent(this, MainActivity.class)));
        panel.addView(memory, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildMirrorsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Mirrors / Roof"));
        panel.addView(Ui.text(this, "Зеркала, defrost, roof, sun curtain и штатный mirror dialog.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Mirrors", "Fold, adjust, defrost", new QuickItem[]{
                new QuickItem("Fold", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Left adjust", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Right adjust", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Defrost", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Roof / Curtain", "Sunroof and sun curtain", new QuickItem[]{
                new QuickItem("Sunroof Open", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Sunroof Close", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Curtain Open", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Curtain Close", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Mirror Dialog", this::showMirrorDialogSheet);
        addActionChip(actions, "Display A", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_A));
        addActionChip(actions, "Display B", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_B));
        addActionChip(actions, "Back", () -> openMode(Mode.HOME));
        panel.addView(actions, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildLightsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Lights / Exterior"));
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
        panel.addView(Ui.label(this, "Drive / Profiles"));
        panel.addView(Ui.text(this, "Drive modes, steering feel, custom keys и переход в отдельные пользовательские профили.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Drive Modes", "Eco, Comfort, Dynamic, Snow", new QuickItem[]{
                new QuickItem("Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC)),
                new QuickItem("Snow", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW))
        });
        addAdvancedCard(grid, "Steering / Custom", "Feel and shortcut keys", new QuickItem[]{
                new QuickItem("Soft", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC)),
                new QuickItem("Custom 360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360)),
                new QuickItem("Custom Drive", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DRIVING_MODE))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Профили", () -> Ui.toast(this, "Профили вынесены в отдельный раздел"));
        addActionChip(actions, "Seats", () -> openMode(Mode.SEATS));
        addActionChip(actions, "Lights", () -> openMode(Mode.LIGHTS));
        addActionChip(actions, "Home", () -> openMode(Mode.HOME));
        panel.addView(actions, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Кузов", readback(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_WINDOW), Ui.SUCCESS);
        addStatusCard(grid, "Свет", readback(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.BCM_LIGHT_GRILLE), Ui.CYAN);
        addStatusCard(grid, "Drive / Seats", readback(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.SEAT_POSITION_SET), Ui.WARNING);
        addStatusCard(grid, "Roof / Mirror", readback(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.BCM_MIRROR_FOLD), Color.rgb(129, 149, 255));
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
        addDockButton(dock, "Body", () -> openMode(Mode.HOME), mode == Mode.HOME, new QuickItem[]{
                new QuickItem("Locks", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Windows", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("Trunk", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK))
        });
        addDockButton(dock, "Seats", () -> openMode(Mode.SEATS), mode == Mode.SEATS, new QuickItem[]{
                new QuickItem("Seat P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1)),
                new QuickItem("Seat P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Mirrors", () -> openMode(Mode.MIRRORS), mode == Mode.MIRRORS, new QuickItem[]{
                new QuickItem("Fold", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Defrost", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Sunroof", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Lights", () -> openMode(Mode.LIGHTS), mode == Mode.LIGHTS, new QuickItem[]{
                new QuickItem("Dipped", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Hazard", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON)),
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
                    new QuickItem("Open section", () -> openMode(Mode.HOME)),
                    new QuickItem("Lights", () -> openMode(Mode.LIGHTS)),
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
            button.setTextColor(Color.WHITE);
            button.setBackground(Ui.cardBg(this, Color.argb(56, 255, 255, 255), Ui.dp(this, 16), Color.TRANSPARENT));
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
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active, QuickItem[] items) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : Color.argb(54, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
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
        EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
        Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
        refreshState();
    }

    private void sendVehicle(int functionId, int zone, int value) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).set(functionId, zone, value);
        Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
        refreshState();
    }

    private void showMirrorDialogSheet() {
        showActionSheet("Mirror Dialog", new QuickItem[]{
                new QuickItem("Open OEM dialog", () -> {
                    EcarxControlBoardAdapter.Result result = new EcarxControlBoardAdapter(this).showMirrorDialog();
                    Ui.toast(this, result.success ? "Диалог открыт" : "Диалог не открыт");
                }),
                new QuickItem("Left adjust", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Right adjust", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE))
        });
    }

    private void showReadbackSheet(String title, String value) {
        showActionSheet(title, new QuickItem[]{
                new QuickItem(value, this::refreshState),
                new QuickItem("Обновить", this::refreshState),
                new QuickItem("Body", () -> openMode(Mode.HOME))
        });
    }

    private void showActionSheet(String title, QuickItem[] items) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        sheet.addView(Ui.label(this, "Vehicle Actions"));
        sheet.addView(Ui.text(this, title, 24, true));
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Color.WHITE);
            button.setBackground(Ui.cardBg(this, Color.argb(56, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
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
        if (topDoorsValue != null) topDoorsValue.setText(compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR).message));
        if (topWindowsValue != null) topWindowsValue.setText(compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_WINDOW).message));
        if (topDriveValue != null) topDriveValue.setText(compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.DRIVE_MODE_SELECT).message));
        if (heroStatusValue != null) heroStatusValue.setText("Статус кузова: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_STATUS).message));
        if (heroLocksValue != null) heroLocksValue.setText("Замки: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message));
        if (heroRoofValue != null) heroRoofValue.setText("Люк: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_SUNROOF_OPEN).message));
        if (heroLightsValue != null) heroLightsValue.setText("Свет: " + compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM).message));
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
        return line.length() > 84 ? line.substring(0, 84) : line;
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#080A0F"), Color.parseColor("#0D1420"), Color.parseColor("#101B2A")});
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
