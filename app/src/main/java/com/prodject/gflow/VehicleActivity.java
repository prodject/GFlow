package com.prodject.gflow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
    private TextView topLocksValue;
    private TextView topDriveValue;
    private TextView heroBodyValue;
    private TextView heroLocksValue;
    private TextView heroModeValue;
    private LinearLayout experimentalDriveHost;
    private TextView experimentalDriveHint;
    private boolean experimentalDriveVisible;
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

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        root.addView(shell, lpMatchWrap(0, 0, 0, 0));

        shell.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 74)));
        shell.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        shell.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        shell.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateScaleIn(shell, 0);
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
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, modeLabel()));
        TextView title = Ui.text(this, "Vehicle Control", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        TextView subtitle = Ui.muted(this, "Body-first layout with slower disclosure for lights, drive and expert tools.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        topDoorsValue = buildTopStat(bar, "Кузов", "...");
        topLocksValue = buildTopStat(bar, "Замки", "...");
        topDriveValue = buildTopStat(bar, "Режим", "...");
        return bar;
    }

    private String modeLabel() {
        switch (mode) {
            case SEATS: return "Seats / Memory";
            case MIRRORS: return "Mirrors / Roof";
            case LIGHTS: return "Lights / Exterior";
            case DRIVE: return "Drive / Dynamics";
            case HOME:
            default: return "Body / Access";
        }
    }

    private TextView buildTopStat(LinearLayout parent, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(78, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
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
        hero.addView(Ui.label(this, "Vehicle Overview"));

        LinearLayout top = Ui.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        heroBodyValue = buildHeroMetric(left, "Body", "...");
        heroLocksValue = buildHeroMetric(left, "Locks", "...");
        heroModeValue = buildHeroMetric(left, "Current mode", "...");
        top.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.94f));

        VehicleBodyView visual = new VehicleBodyView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 280));
        visualLp.leftMargin = Ui.dp(this, 12);
        top.addView(visual, visualLp);
        hero.addView(top);

        hero.addView(buildModeStrip(), lpMatchWrap(0, 16, 0, 0));
        return hero;
    }

    private TextView buildHeroMetric(LinearLayout parent, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(58, 255, 255, 255), Ui.dp(this, 24), Color.argb(40, 255, 255, 255)));
        card.addView(Ui.label(this, label));
        TextView body = Ui.text(this, value, 17, true);
        body.setPadding(0, Ui.dp(this, 2), 0, 0);
        card.addView(body);
        parent.addView(card, lpMatchWrap(0, 0, 0, 10));
        return body;
    }

    private LinearLayout buildModeStrip() {
        LinearLayout strip = Ui.row(this);
        strip.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
        strip.setBackground(Ui.cardBg(this, Color.argb(44, 255, 255, 255), Ui.dp(this, 26), Color.TRANSPARENT));
        addModeChip(strip, "Body", Mode.HOME);
        addModeChip(strip, "Seats", Mode.SEATS);
        addModeChip(strip, "Mirrors", Mode.MIRRORS);
        addModeChip(strip, "Lights", Mode.LIGHTS);
        addModeChip(strip, "Drive", Mode.DRIVE);
        return strip;
    }

    private void addModeChip(LinearLayout row, String label, Mode target) {
        Button chip = Ui.button(this, label);
        boolean active = mode == target;
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(14);
        chip.setBackground(Ui.cardBg(this,
                active ? Color.argb(144, 77, 163, 255) : Color.argb(26, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(86, 131, 199, 255) : Color.TRANSPARENT));
        chip.setOnClickListener(v -> {
            Ui.press(v);
            openMode(target);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1f);
        lp.leftMargin = Ui.dp(this, 4);
        lp.rightMargin = Ui.dp(this, 4);
        row.addView(chip, lp);
    }

    private LinearLayout buildBodyControls() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Body"));
        panel.addView(Ui.text(this, "Кузов теперь начинается с трёх понятных блоков: доступ, окна и багажник. Крыша и свет вынесены в соседние режимы.", 14, false));

        GridLayout primary = new GridLayout(this);
        primary.setColumnCount(3);
        addTile(primary, "Запереть", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON));
        addTile(primary, "Открыть", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF));
        addTile(primary, "Багажник", Color.rgb(94, 201, 196), () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK));
        panel.addView(primary, lpMatchWrap(0, 14, 0, 0));

        GridLayout secondary = new GridLayout(this);
        secondary.setColumnCount(2);
        addAdvancedCard(secondary, "Locks", "Быстрые действия для замков и child lock.", new QuickItem[]{
                new QuickItem("Lock On", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Lock Off", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF)),
                new QuickItem("Child lock", () -> sendVehicle(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(secondary, "Windows", "Все стёкла через один аккуратный блок вместо россыпи плиток.", new QuickItem[]{
                new QuickItem("All Open", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("All Close", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE)),
                new QuickItem("Half", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_HALF))
        });
        panel.addView(secondary, lpMatchWrap(0, 14, 0, 0));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Окна вниз", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN));
        addActionChip(actions, "Окна вверх", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE));
        addActionChip(actions, "Roof", () -> openMode(Mode.MIRRORS));
        addActionChip(actions, "Drive", () -> openMode(Mode.DRIVE));
        panel.addView(actions, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildSeatsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Seats / Memory"));
        panel.addView(Ui.text(this, "Только два смысловых блока: регулировка и память. Детали не вываливаются сразу в длинный список.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Сиденье водителя", "Длина, высота и быстрое позиционирование.", new QuickItem[]{
                new QuickItem("Вперед", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_FORWARD)),
                new QuickItem("Назад", () -> sendVehicle(EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKWARD)),
                new QuickItem("Выше", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_UP)),
                new QuickItem("Ниже", () -> sendVehicle(EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN))
        });
        addAdvancedCard(grid, "Спинка / Memory", "Спинка и сохранённые позиции в одном месте.", new QuickItem[]{
                new QuickItem("Спинка +", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD)),
                new QuickItem("Спинка -", () -> sendVehicle(EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD)),
                new QuickItem("Save P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_1)),
                new QuickItem("Recall P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1))
        });
        panel.addView(grid, lpMatchWrap(0, 14, 0, 14));

        LinearLayout memory = Ui.row(this);
        addActionChip(memory, "Save P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_2));
        addActionChip(memory, "Recall P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2));
        addActionChip(memory, "Комфорт", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(memory, "Профили", () -> startActivity(new Intent(this, ProfileActivity.class)));
        panel.addView(memory, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildMirrorsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Mirrors / Roof"));
        panel.addView(Ui.text(this, "Крыша теперь логически живёт рядом с зеркалами, а не в кузове. Так основной экран остаётся чище.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Mirrors", "Складывание, регулировка и обогрев.", new QuickItem[]{
                new QuickItem("Сложить", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Левое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Правое", () -> sendVehicle(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE)),
                new QuickItem("Обогрев", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Roof", "Люк и солнцезащитная шторка собраны в отдельный cluster.", new QuickItem[]{
                new QuickItem("Люк открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Люк закрыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Шторка открыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Шторка закрыть", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(grid, lpMatchWrap(0, 14, 0, 14));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Диалог зеркал", this::showMirrorDialogSheet);
        addActionChip(actions, "Позиция A", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_A));
        addActionChip(actions, "Позиция B", () -> sendVehicle(EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_B));
        addActionChip(actions, "Body", () -> openMode(Mode.HOME));
        panel.addView(actions, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildLightsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Lights / Exterior"));
        panel.addView(Ui.text(this, "Основной свет и безопасность отделены от декоративного и редкого экстрерьера.", 14, false));

        GridLayout primary = new GridLayout(this);
        primary.setColumnCount(2);
        addAdvancedCard(primary, "Primary Lights", "Ключевой свет и аварийный контур.", new QuickItem[]{
                new QuickItem("Ближний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Дальний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("DRL", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Аварийка", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(primary, "Signals / Wipers", "Поворотники, ПТФ и стеклоочистители.", new QuickItem[]{
                new QuickItem("Left turn", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Right turn", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Wiper Auto", () -> sendVehicle(EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_AUTO)),
                new QuickItem("Washer", () -> sendVehicle(EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(primary, lpMatchWrap(0, 14, 0, 14));

        LinearLayout secondary = buildSecondaryPanel(
                "Accent Exterior",
                "Редкие световые сценарии убраны во вторичный tier, чтобы экран не выглядел как сервисное меню.",
                new QuickItem[]{
                        new QuickItem("Grille", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_ON)),
                        new QuickItem("Welcome", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON)),
                        new QuickItem("Rear logo", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_ON)),
                        new QuickItem("Atmosphere", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_ON)),
                        new QuickItem("Front fog", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_ON)),
                        new QuickItem("Plate", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_ON))
                }
        );
        panel.addView(secondary, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildDrivePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Drive / Dynamics"));
        panel.addView(Ui.text(this, experimentalFeaturesEnabled()
                ? "Основные режимы вынесены в быстрый слой, а experimental drive закрыт до явного раскрытия."
                : "Быстрый слой оставляет на экране только ежедневные режимы. Experimental drive по-прежнему выключен в настройках.", 14, false));

        GridLayout primary = new GridLayout(this);
        primary.setColumnCount(2);
        addAdvancedCard(primary, "Drive Modes", "Четыре повседневных режима без перегруза.", new QuickItem[]{
                new QuickItem("Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC)),
                new QuickItem("Snow", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW))
        });
        addAdvancedCard(primary, "Steering / Custom", "Часто используемые настройки руля и hotkeys.", new QuickItem[]{
                new QuickItem("Soft", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC)),
                new QuickItem("Custom 360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360)),
                new QuickItem("Custom Drive", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DRIVING_MODE))
        });
        panel.addView(primary, lpMatchWrap(0, 14, 0, 14));

        panel.addView(buildSecondaryPanel(
                "Profiles / Instrument Theme",
                "Профили и приборная тема остаются доступными, но не конкурируют с основным drive UI.",
                new QuickItem[]{
                        new QuickItem("Профили", () -> startActivity(new Intent(this, ProfileActivity.class))),
                        new QuickItem("DIM Red", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_RED)),
                        new QuickItem("DIM Blue", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_BLUE)),
                        new QuickItem("Driver Info Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_ECO))
                }), lpMatchWrap(0, 0, 0, 14));

        if (experimentalFeaturesEnabled()) {
            panel.addView(buildExperimentalDriveSummary(), lpMatchWrap(0, 0, 0, 0));
        } else {
            panel.addView(Ui.muted(this, "Включите Experimental features в настройках, чтобы открыть PURE/HYBRID/POWER, AWD/SAVE/ADAPTIVE и custom propulsion/suspension/climate."), lpMatchWrap(0, 0, 0, 0));
        }
        return panel;
    }

    private LinearLayout buildExperimentalDriveSummary() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(36, 10, 14, 20), Ui.dp(this, 28), Color.argb(48, 255, 179, 64)));
        panel.addView(Ui.label(this, "Experimental Drive"));
        panel.addView(Ui.text(this, "Expert-only modes and custom propulsion remain collapsed until explicitly opened.", 16, true));
        experimentalDriveHint = Ui.muted(this, "Скрыто по умолчанию. Это слой для редких и потенциально рискованных drive-сценариев.");
        panel.addView(experimentalDriveHint, lpMatchWrap(0, 6, 0, 0));

        Button toggle = Ui.button(this, "Открыть experimental drive");
        toggle.setOnClickListener(v -> {
            Ui.press(v);
            toggleExperimentalDrive();
        });
        panel.addView(toggle, lpMatchWrap(0, 12, 0, 0));

        experimentalDriveHost = new LinearLayout(this);
        experimentalDriveHost.setOrientation(LinearLayout.VERTICAL);
        experimentalDriveHost.setVisibility(View.GONE);
        panel.addView(experimentalDriveHost, lpMatchWrap(0, 14, 0, 0));
        renderExperimentalDriveHost();
        return panel;
    }

    private void renderExperimentalDriveHost() {
        if (experimentalDriveHost == null) return;
        experimentalDriveHost.removeAllViews();

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Extended Modes I", "Offroad, HDC, Mud, Rock.", new QuickItem[]{
                new QuickItem("Offroad", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD)),
                new QuickItem("HDC", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HDC)),
                new QuickItem("Mud", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_MUD)),
                new QuickItem("Rock", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ROCK))
        });
        addAdvancedCard(grid, "Extended Modes II", "Sand, AWD, eAWD, Save.", new QuickItem[]{
                new QuickItem("Sand", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAND)),
                new QuickItem("AWD", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_AWD)),
                new QuickItem("eAWD", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_EAWD)),
                new QuickItem("Save", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAVE))
        });
        addAdvancedCard(grid, "Hybrid Modes", "Pure, Hybrid, PHEV, Power.", new QuickItem[]{
                new QuickItem("Pure", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PURE)),
                new QuickItem("Hybrid", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HYBRID)),
                new QuickItem("PHEV", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PHEV)),
                new QuickItem("Power", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_POWER))
        });
        addAdvancedCard(grid, "Adaptive / Custom", "Adaptive, custom, eco+, sport+.", new QuickItem[]{
                new QuickItem("Adaptive", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE)),
                new QuickItem("Custom", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM)),
                new QuickItem("Eco Plus", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS)),
                new QuickItem("Sport Plus", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS))
        });
        experimentalDriveHost.addView(grid, lpMatchWrap(0, 0, 0, 14));

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
        }), lpMatchWrap(0, 0, 0, 10));
        profileRows.addView(buildDriveThemeAndStartPanel(), lpMatchWrap(0, 0, 0, 0));
        experimentalDriveHost.addView(profileRows);
    }

    private LinearLayout buildDriveThemeAndStartPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Cluster / Start"));
        panel.addView(Ui.muted(this, "Редкие настройки темы приборки и стартового поведения."));

        LinearLayout syncRow = Ui.row(this);
        addActionChip(syncRow, "Theme Sync On", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SYNC, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(syncRow, "Theme Sync Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SYNC, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(syncRow, "Info Standard", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_STANDARD));
        addActionChip(syncRow, "Info Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CUSTOM_DRIVER_INFO, EcarxVehicleAdapter.CUSTOM_DRIVER_INFO_ECO));
        panel.addView(syncRow, lpMatchWrap(0, 12, 0, 0));

        LinearLayout dimThemes = Ui.row(this);
        addActionChip(dimThemes, "DIM Red", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_RED));
        addActionChip(dimThemes, "DIM Gold", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_GOLD));
        addActionChip(dimThemes, "DIM Blue", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.DIM_THEME_BLUE));
        addActionChip(dimThemes, "DIM Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(dimThemes, lpMatchWrap(0, 12, 0, 0));

        LinearLayout launchRow = Ui.row(this);
        addActionChip(launchRow, "Creep On", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CREEP_SET, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(launchRow, "Creep Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_CREEP_SET, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(launchRow, "Launch On", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(launchRow, "Launch Off", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(launchRow, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildSecondaryPanel(String title, String body, QuickItem[] items) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(34, 255, 255, 255), Ui.dp(this, 26), Color.argb(30, 255, 255, 255)));
        panel.addView(Ui.label(this, title));
        TextView text = Ui.text(this, body, 14, false);
        text.setTextColor(Ui.secondaryText(this));
        panel.addView(text);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Color.WHITE);
            button.setBackground(Ui.cardBg(this, Color.argb(44, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
            button.setOnClickListener(v -> {
                Ui.press(v);
                item.action.run();
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
            grid.addView(button, lp);
        }
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Body", readback(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_WINDOW), Ui.SUCCESS);
        addStatusCard(grid, "Locks", readback(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK), Ui.CYAN);
        addStatusCard(grid, "Drive", readback(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_STEERING_MODE), Ui.WARNING);
        addStatusCard(grid, "Roof / Mirrors", readback(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.BCM_MIRROR_FOLD), Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(26, 255, 255, 255), Ui.dp(this, 24), Color.argb(20, 255, 255, 255)));
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 13, false);
        v.setTextColor(Ui.secondaryText(this));
        card.addView(v);
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 4));
        accentLp.topMargin = Ui.dp(this, 10);
        card.addView(accent, accentLp);
        card.setOnClickListener(view -> {
            Ui.press(view);
            showReadbackSheet(title, value);
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 14), Ui.dp(this, 14));
        grid.addView(card, lp);
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Body", () -> openMode(Mode.HOME), mode == Mode.HOME, new QuickItem[]{
                new QuickItem("Замки", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Окна", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN)),
                new QuickItem("Багажник", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK))
        });
        addDockButton(dock, "Seats", () -> openMode(Mode.SEATS), mode == Mode.SEATS, new QuickItem[]{
                new QuickItem("Сиденье P1", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1)),
                new QuickItem("Сиденье P2", () -> sendVehicle(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2)),
                new QuickItem("Комфорт", () -> sendVehicle(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Mirrors", () -> openMode(Mode.MIRRORS), mode == Mode.MIRRORS, new QuickItem[]{
                new QuickItem("Сложить", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Обогрев", () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Люк", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Lights", () -> openMode(Mode.LIGHTS), mode == Mode.LIGHTS, new QuickItem[]{
                new QuickItem("Ближний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Аварийка", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Welcome", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Drive", () -> openMode(Mode.DRIVE), mode == Mode.DRIVE, new QuickItem[]{
                new QuickItem("Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO)),
                new QuickItem("Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT)),
                new QuickItem("Dynamic", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC))
        });
        Ui.animateIn(dock, 150, 10f);
        return dock;
    }

    private void addTile(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(15);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 14), Ui.dp(this, 20), Ui.dp(this, 14), Ui.dp(this, 20));
        tile.setBackground(Ui.cardBg(this, Color.argb(92, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 24), Color.argb(80, 255, 255, 255)));
        tile.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
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
            button.setOnClickListener(v -> {
                Ui.press(v);
                item.action.run();
            });
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
        b.setBackground(Ui.cardBg(this, Color.argb(62, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
        });
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
        button.setOnLongClickListener(v -> {
            showActionSheet(label, items);
            return true;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void toggleExperimentalDrive() {
        if (experimentalDriveHost == null) return;
        experimentalDriveVisible = !experimentalDriveVisible;
        experimentalDriveHost.setVisibility(experimentalDriveVisible ? View.VISIBLE : View.GONE);
        if (experimentalDriveHint != null) {
            experimentalDriveHint.setText(experimentalDriveVisible
                    ? "Расширенный drive раскрыт. Этот слой остаётся вторичным и не должен конкурировать с повседневными режимами."
                    : "Скрыто по умолчанию. Это слой для редких и потенциально рискованных drive-сценариев.");
        }
        if (experimentalDriveVisible) Ui.staggerIn(collectChildren(experimentalDriveHost), 0, 60);
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
                Ui.press(v);
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
        Ui.animateScaleIn(sheet, 0);
    }

    private void refreshState() {
        String door = compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_STATUS).message);
        String lock = compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.BCM_DOOR_LOCK).message);
        String drive = compact(new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.DRIVE_MODE_SELECT).message);
        if (topDoorsValue != null) topDoorsValue.setText(door);
        if (topLocksValue != null) topLocksValue.setText(lock);
        if (topDriveValue != null) topDriveValue.setText(drive);
        if (heroBodyValue != null) heroBodyValue.setText(door);
        if (heroLocksValue != null) heroLocksValue.setText(lock);
        if (heroModeValue != null) heroModeValue.setText(drive);
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

    private View[] collectChildren(LinearLayout layout) {
        View[] views = new View[layout.getChildCount()];
        for (int i = 0; i < layout.getChildCount(); i++) views[i] = layout.getChildAt(i);
        return views;
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

            paint.setShader(new LinearGradient(0f, 0f, 0f, h,
                    Color.argb(0, 0, 0, 0),
                    Color.argb(120, 4, 8, 16),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(w * 0.06f, h * 0.04f, w * 0.94f, h * 0.96f), Ui.dp(getContext(), 30), Ui.dp(getContext(), 30), paint);

            paint.setShader(new RadialGradient(cx, h * 0.72f, w * 0.40f,
                    Color.argb(130, 59, 120, 255),
                    Color.argb(0, 59, 120, 255),
                    Shader.TileMode.CLAMP));
            canvas.drawOval(new RectF(w * 0.18f, h * 0.72f, w * 0.82f, h * 0.98f), paint);

            path.reset();
            path.moveTo(cx, h * 0.08f);
            path.cubicTo(w * 0.72f, h * 0.13f, w * 0.82f, h * 0.40f, w * 0.79f, h * 0.78f);
            path.lineTo(w * 0.62f, h * 0.90f);
            path.lineTo(w * 0.38f, h * 0.90f);
            path.lineTo(w * 0.21f, h * 0.78f);
            path.cubicTo(w * 0.18f, h * 0.40f, w * 0.28f, h * 0.13f, cx, h * 0.08f);
            paint.setShader(new LinearGradient(w * 0.2f, h * 0.08f, w * 0.8f, h * 0.92f,
                    Color.argb(244, 244, 248, 252),
                    Color.argb(226, 193, 204, 218),
                    Shader.TileMode.CLAMP));
            canvas.drawPath(path, paint);

            paint.setShader(null);
            paint.setColor(Color.argb(92, 18, 28, 42));
            canvas.drawRoundRect(new RectF(w * 0.36f, h * 0.19f, w * 0.64f, h * 0.39f), Ui.dp(getContext(), 20), Ui.dp(getContext(), 20), paint);

            paint.setColor(Color.argb(120, 77, 163, 255));
            canvas.drawRoundRect(new RectF(w * 0.18f, h * 0.42f, w * 0.28f, h * 0.73f), Ui.dp(getContext(), 12), Ui.dp(getContext(), 12), paint);
            canvas.drawRoundRect(new RectF(w * 0.72f, h * 0.42f, w * 0.82f, h * 0.73f), Ui.dp(getContext(), 12), Ui.dp(getContext(), 12), paint);

            paint.setColor(Color.argb(110, 255, 179, 64));
            canvas.drawRoundRect(new RectF(w * 0.41f, h * 0.07f, w * 0.59f, h * 0.13f), Ui.dp(getContext(), 10), Ui.dp(getContext(), 10), paint);
            paint.setColor(Color.argb(118, 77, 208, 127));
            canvas.drawRoundRect(new RectF(w * 0.32f, h * 0.79f, w * 0.68f, h * 0.88f), Ui.dp(getContext(), 14), Ui.dp(getContext(), 14), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 2));
            paint.setColor(Color.argb(60, 255, 255, 255));
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
