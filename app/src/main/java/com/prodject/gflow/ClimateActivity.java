package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class ClimateActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String CLIMATE_PRESETS = "climate_presets";
    private static final String CLIMATE_PRESET_ORDER = "order";
    static final String EXTRA_MODE = "climate_mode";
    static final String MODE_ADVANCED = "advanced";
    static final String MODE_PRESETS = "presets";
    static final String MODE_SMART = "smart";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout contentHost;
    private TextView topModeValue;
    private TextView topZoneValue;
    private TextView topCabinValue;
    private TextView driverTempValue;
    private TextView passengerTempValue;
    private TextView indoorTempValue;
    private TextView outdoorTempValue;
    private TextView fanValue;
    private TextView summaryValue;
    private LinearLayout heroPassengerCard;
    private TextView heroDriverLabel;
    private SeekBar heroTempSeekBar;
    private CheckBox heroSyncToggle;
    private String editingPresetName = "";
    private Mode mode = Mode.HOME;
    private boolean updatingHeroControls;
    private Boolean lastUnifiedClimate;
    private final Runnable stateTicker = new Runnable() {
        @Override public void run() {
            refreshState();
            handler.postDelayed(this, 20_000L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String requestedMode = getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_ADVANCED.equals(requestedMode)) mode = Mode.ADVANCED;
        else if (MODE_PRESETS.equals(requestedMode)) mode = Mode.PRESETS;
        else if (MODE_SMART.equals(requestedMode)) mode = Mode.SMART;
        setContentView(buildClimateShell());
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

    private View buildClimateShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildClimateTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 84)));
        root.addView(buildClimateHero(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        root.addView(buildClimateBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void renderContent() {
        contentHost.removeAllViews();
        if (mode == Mode.HOME) {
            contentHost.addView(buildClimateComfortPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildStockHvacFluentPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildClimateMainPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildClimateReadbackGrid(), lpMatchWrap(0, 0, 0, 0));
        } else if (mode == Mode.ADVANCED) {
            contentHost.addView(buildAdvancedPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildClimateReadbackGrid(), lpMatchWrap(0, 0, 0, 0));
        } else if (mode == Mode.PRESETS) {
            contentHost.addView(buildPresetsPanel(), lpMatchWrap(0, 0, 0, 16));
        } else if (mode == Mode.PRESET_EDITOR) {
            contentHost.addView(buildPresetEditorPanel(), lpMatchWrap(0, 0, 0, 16));
        } else if (mode == Mode.SMART) {
            contentHost.addView(buildSmartClimatePanel(), lpMatchWrap(0, 0, 0, 16));
        }
    }

    private LinearLayout buildClimateTopBar() {
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
        TextView title = Ui.text(this, "Климат", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        topModeValue = buildTopStat(bar, "Режим", "...");
        topZoneValue = buildTopStat(bar, "Зона", "...");
        topCabinValue = buildTopStat(bar, "Датчики", "...");
        return bar;
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

    private LinearLayout buildClimateHero() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Обзор климата"));

        LinearLayout tempRow = Ui.row(this);
        tempRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout driverCard = Ui.glassCard(this);
        driverCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        heroDriverLabel = Ui.label(this, "Водитель");
        driverCard.addView(heroDriverLabel);
        driverTempValue = Ui.text(this, "--", 38, true);
        driverTempValue.setPadding(0, Ui.dp(this, 4), 0, 0);
        driverCard.addView(driverTempValue);
        TextView driverHint = Ui.muted(this, "Уставка климата");
        driverHint.setPadding(0, Ui.dp(this, 6), 0, 0);
        driverCard.addView(driverHint);
        tempRow.addView(driverCard, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f));

        heroPassengerCard = Ui.glassCard(this);
        heroPassengerCard.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        heroPassengerCard.addView(Ui.label(this, "Пассажир"));
        passengerTempValue = Ui.text(this, "--", 24, true);
        passengerTempValue.setPadding(0, Ui.dp(this, 8), 0, 0);
        heroPassengerCard.addView(passengerTempValue);
        TextView passengerHint = Ui.muted(this, "Уставка климата");
        passengerHint.setPadding(0, Ui.dp(this, 6), 0, 0);
        heroPassengerCard.addView(passengerHint);
        LinearLayout.LayoutParams passengerLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.85f);
        passengerLp.leftMargin = Ui.dp(this, 12);
        tempRow.addView(heroPassengerCard, passengerLp);

        hero.addView(tempRow, lpMatchWrap(0, 8, 0, 0));

        LinearLayout adjustRow = Ui.row(this);
        adjustRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout sliderCard = Ui.glassCard(this);
        sliderCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 12), Ui.dp(this, 18), Ui.dp(this, 12));
        sliderCard.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(104, 24, 32, 46) : Color.argb(236, 244, 249, 255),
                Ui.dp(this, 22),
                Ui.dark(this) ? Color.argb(116, 77, 163, 255) : Color.argb(92, 77, 163, 255)));
        sliderCard.addView(Ui.label(this, "Температура"));
        heroTempSeekBar = new SeekBar(this);
        heroTempSeekBar.setMax(32);
        heroTempSeekBar.setProgress(12);
        heroTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || updatingHeroControls) return;
                float target = heroTempValueFromProgress(progress);
                EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(ClimateActivity.this)
                        .setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, target);
                Ui.toast(ClimateActivity.this, result.success ? "Температура обновлена" : "Температура не обновлена");
                refreshState();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sliderCard.addView(heroTempSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        adjustRow.addView(sliderCard, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        heroSyncToggle = new CheckBox(this);
        heroSyncToggle.setText("Sync");
        heroSyncToggle.setChecked(false);
        heroSyncToggle.setTextColor(Color.rgb(72, 181, 165));
        heroSyncToggle.setButtonDrawable(null);
        heroSyncToggle.setGravity(Gravity.CENTER);
        heroSyncToggle.setPadding(Ui.dp(this, 20), 0, Ui.dp(this, 20), 0);
        styleHeroSyncToggle(false);
        heroSyncToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updatingHeroControls) return;
            styleHeroSyncToggle(isChecked);
            command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE,
                    isChecked ? EcarxVehicleAdapter.CLIMATE_ZONE_DUAL : EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE);
        });
        LinearLayout.LayoutParams syncLp = new LinearLayout.LayoutParams(Ui.dp(this, 110), Ui.dp(this, 56));
        syncLp.leftMargin = Ui.dp(this, 12);
        adjustRow.addView(heroSyncToggle, syncLp);
        hero.addView(adjustRow, lpMatchWrap(0, 12, 0, 0));

        LinearLayout quick = Ui.row(this);
        quick.setWeightSum(4f);
        addClimateActionChip(quick, "Auto", () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON));
        addClimateActionChip(quick, "A/C", () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON));
        addClimateActionChip(quick, "Обдув", this::showBlowingSheet);
        addClimateActionChip(quick, "Defrost", this::showDefrostSheet);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        indoorTempValue = Ui.text(this, "В салоне: --", 16, true);
        indoorTempValue.setPadding(0, Ui.dp(this, 6), 0, 0);
        outdoorTempValue = Ui.text(this, "Снаружи: --", 16, true);
        outdoorTempValue.setPadding(0, Ui.dp(this, 4), 0, 0);
        fanValue = Ui.text(this, "Вентилятор: --", 16, true);
        fanValue.setPadding(0, Ui.dp(this, 6), 0, 0);
        summaryValue = Ui.text(this, "Состояние климата: обновление...", 14, false);
        summaryValue.setTextColor(Ui.secondaryText(this));
        status.addView(indoorTempValue);
        status.addView(outdoorTempValue);
        status.addView(fanValue);
        status.addView(summaryValue);
        hero.addView(status, lpMatchWrap(0, 10, 0, 0));
        return hero;
    }

    private LinearLayout buildClimateComfortPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Панель комфорта"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        addClimateToggle(grid, "HVAC", Ui.CYAN, () -> command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Auto", Ui.SUCCESS, () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C", Ui.CYAN, () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C Max", Ui.WARNING, () -> command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Eco", Color.rgb(69, 186, 134), () -> command(EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON));
        if (!climateZonesUnified()) {
            addClimateToggle(grid, "Sync", Color.rgb(103, 147, 255), () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL));
            addClimateToggle(grid, "Split", Color.rgb(134, 103, 255), () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE));
        }
        addClimateToggle(grid, "°F", Color.rgb(255, 122, 89), () -> command(EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_F));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildClimateMainPanel() {
        LinearLayout panel = Ui.glassCard(this);
        boolean unified = climateZonesUnified();
        panel.addView(Ui.label(this, unified ? "Температура" : "Водитель / Пассажир"));

        LinearLayout tempRow = Ui.row(this);
        tempRow.setGravity(Gravity.CENTER_VERTICAL);
        tempRow.addView(buildTempCard(unified ? "Общая температура" : "Водитель", EcarxVehicleAdapter.ZONE_DRIVER_LEFT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, unified ? 1.1f : 0.9f));

        LinearLayout center = Ui.glassCard(this);
        center.addView(Ui.label(this, "Потоки воздуха"));
        View flow = new AirFlowView(this);
        center.addView(flow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 220)));
        TextView fanLabel = Ui.text(this, "Вентилятор: 3", 18, true);
        center.addView(fanLabel);
        SeekBar fan = new SeekBar(this);
        fan.setMax(2);
        fan.setProgress(1);
        fan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int level = progress + 4;
                fanLabel.setText("Вентилятор: " + level);
                if (fromUser) {
                    command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, fanSpeedValue(level));
                    animatePulse(flow);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        center.addView(fan);
        LinearLayout quickModes = Ui.row(this);
        addClimateActionChip(quickModes, "Стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW));
        addClimateActionChip(quickModes, "Лицо+стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW));
        addClimateActionChip(quickModes, "Все зоны", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_ALL));
        center.addView(quickModes, lpMatchWrap(0, 8, 0, 0));
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f);
        centerLp.leftMargin = Ui.dp(this, 12);
        centerLp.rightMargin = Ui.dp(this, 12);
        tempRow.addView(center, centerLp);

        if (!unified) {
            tempRow.addView(buildTempCard("Пассажир", EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));
        }
        panel.addView(tempRow);

        LinearLayout seats = Ui.row(this);
        seats.setWeightSum(4f);
        addClimateActionChip(seats, "Подогрев сид.", () -> cycleSeatClimate(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT));
        if (developerModeEnabled()) {
            addClimateActionChip(seats, "Вентиляция", () -> cycleSeatClimate(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT));
        }
        addClimateActionChip(seats, "Руль", this::cycleWheelHeat);
        addClimateActionChip(seats, "Обдув стекла", this::showDefrostSheet);
        panel.addView(seats, lpMatchWrap(0, 16, 0, 0));

        LinearLayout presets = Ui.row(this);
        presets.setWeightSum(3f);
        addClimateActionChip(presets, "Тихий", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, fanSpeedValue(4))));
        addClimateActionChip(presets, "Комфорт", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, fanSpeedValue(5))));
        addClimateActionChip(presets, "Прогрев", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, fanSpeedValue(6)),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_ALL)));
        panel.addView(presets, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildStockHvacFluentPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Точные настройки"));
        panel.addView(Ui.text(this, "Режимы климата", 22, true));
        panel.addView(Ui.muted(this, "Подробная настройка вентилятора, температуры и потоков воздуха."));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout fanCard = Ui.deepCard(this);
        fanCard.addView(Ui.label(this, "Вентилятор"));
        TextView fanLabel = Ui.text(this, "Уровень 5", 26, true);
        fanCard.addView(fanLabel);
        FluentFanDial dial = new FluentFanDial(this);
        dial.setLevel(5);
        dial.setOnLevelChanged(level -> {
            fanLabel.setText("Уровень " + level);
            command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, fanSpeedValue(level));
        });
        fanCard.addView(dial, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 190)));
        LinearLayout.LayoutParams fanLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.05f);
        row.addView(fanCard, fanLp);

        LinearLayout controls = Ui.glassCard(this);
        controls.addView(Ui.label(this, "Режимы"));
        addStockSegment(controls, "Auto fan silent", () -> command(EcarxVehicleAdapter.HVAC_AUTO_FAN_SETTING, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.AUTO_FAN_SETTING_SILENT));
        addStockSegment(controls, "Auto fan normal", () -> command(EcarxVehicleAdapter.HVAC_AUTO_FAN_SETTING, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.AUTO_FAN_SETTING_NORMAL));
        addStockSegment(controls, "Обдув стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW));
        addStockSegment(controls, "Лицо + стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW));
        addStockSegment(controls, "Все зоны", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_ALL));
        LinearLayout.LayoutParams controlsLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        controlsLp.leftMargin = Ui.dp(this, 12);
        row.addView(controls, controlsLp);

        panel.addView(row, lpMatchWrap(0, 14, 0, 12));

        LinearLayout bottom = Ui.row(this);
        bottom.setWeightSum(5f);
        addStockPill(bottom, "17.0°", () -> setStockDriverTemp(17.0f));
        addStockPill(bottom, "18.5°", () -> setStockDriverTemp(18.5f));
        addStockPill(bottom, "20.0°", () -> setStockDriverTemp(20.0f));
        addStockPill(bottom, "Внутр.", () -> command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER));
        addStockPill(bottom, "Наруж.", () -> command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE));
        panel.addView(bottom);
        return panel;
    }

    private LinearLayout buildPresetsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Пресеты климата"));
        panel.addView(Ui.text(this, "Сохраненные пользовательские HVAC-пресеты и быстрые сценарии теперь живут внутри нового климатического экрана.", 14, false));
        Button add = Ui.button(this, "Создать пресет");
        add.setTextColor(Ui.primaryText(this));
        add.setOnClickListener(v -> openPresetEditor("", defaultPresetText()));
        panel.addView(add, lpMatchWrap(0, 12, 0, 8));
        for (String name : climatePresetNames()) {
            EcarxVehicleAdapter.Command[] commands = decodeCommands(getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(name, ""));
            if (commands.length == 0) continue;
            panel.addView(buildSavedPresetCard(name, commands), lpMatchWrap(0, 8, 0, 0));
        }
        if (climatePresetNames().isEmpty()) {
            TextView empty = Ui.text(this, "Пока нет сохраненных пресетов", 15, false);
            empty.setTextColor(Ui.secondaryText(this));
            panel.addView(empty, lpMatchWrap(0, 8, 0, 0));
        }
        return panel;
    }

    private LinearLayout buildSavedPresetCard(String label, EcarxVehicleAdapter.Command[] commands) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, "Сохраненный пресет"));
        card.addView(Ui.text(this, label, 20, true));
        TextView body = Ui.text(this, compact(encodeCommands(commands).replace('\n', ' ')), 13, false);
        body.setTextColor(Ui.secondaryText(this));
        card.addView(body);
        LinearLayout row = Ui.row(this);
        Button apply = Ui.button(this, "Применить");
        apply.setTextColor(Ui.primaryText(this));
        apply.setOnClickListener(v -> applyClimatePreset(commands));
        Button edit = Ui.button(this, "Редактировать");
        edit.setTextColor(Ui.primaryText(this));
        edit.setOnClickListener(v -> openPresetEditor(label, encodeCommands(commands)));
        Button delete = Ui.button(this, "Удалить");
        delete.setTextColor(Ui.primaryText(this));
        delete.setOnClickListener(v -> {
            deleteClimatePreset(label);
            renderContent();
        });
        row.addView(apply, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(edit, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(delete, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(row, lpMatchWrap(0, 10, 0, 0));
        return card;
    }

    private LinearLayout buildPresetEditorPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Редактор пресета"));
        panel.addView(Ui.text(this, editingPresetName.isEmpty() ? "Новый пресет" : editingPresetName, 20, true));
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(editingPresetName);
        EditText commands = new EditText(this);
        commands.setHint("functionId,zone,value по одной команде на строку");
        commands.setMinLines(10);
        commands.setText(getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString("__draft__", defaultPresetText()));
        Button save = Ui.button(this, "Сохранить пресет");
        save.setTextColor(Ui.primaryText(this));
        save.setOnClickListener(v -> {
            String presetName = name.getText().toString().trim();
            EcarxVehicleAdapter.Command[] parsed = decodeCommands(commands.getText().toString());
            if (presetName.isEmpty() || parsed.length == 0) {
                Ui.toast(this, "Нужно имя и хотя бы одна команда");
                return;
            }
            if (!editingPresetName.isEmpty() && !editingPresetName.equals(presetName)) deleteClimatePreset(editingPresetName);
            saveClimatePreset(presetName, encodeCommands(parsed));
            editingPresetName = "";
            getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().remove("__draft__").apply();
            openMode(Mode.PRESETS);
        });
        Button cancel = Ui.button(this, "Назад к пресетам");
        cancel.setTextColor(Ui.primaryText(this));
        cancel.setOnClickListener(v -> {
            editingPresetName = "";
            getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().remove("__draft__").apply();
            openMode(Mode.PRESETS);
        });
        panel.addView(Ui.text(this, "Формат: functionId,zone,value. Можно использовать decimal или 0xHEX.", 14, false), lpMatchWrap(0, 8, 0, 8));
        panel.addView(name);
        panel.addView(commands, lpMatchWrap(0, 8, 0, 8));
        panel.addView(save);
        panel.addView(cancel, lpMatchWrap(0, 8, 0, 0));
        commands.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().putString("__draft__", commands.getText().toString()).apply();
            }
        });
        return panel;
    }

    private LinearLayout buildSmartClimatePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Умный климат"));
        SharedPreferences prefs = SmartClimateController.prefs(this);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("Контроллер включен");
        enabled.setChecked(prefs.getBoolean(SmartClimateController.KEY_ENABLED, false));
        EditText modeField = new EditText(this);
        modeField.setHint("off / fast_cool / fast_heat / stabilize / maintain / dry / summer");
        modeField.setText(prefs.getString(SmartClimateController.KEY_MODE, SmartClimateController.MODE_OFF));
        EditText cabin = new EditText(this);
        cabin.setHint("Fallback: температура салона");
        cabin.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_CABIN_TEMP, 26.0f)));
        EditText outside = new EditText(this);
        outside.setHint("Fallback: внешняя температура");
        outside.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_OUTSIDE_TEMP, 26.0f)));
        EditText driverTarget = new EditText(this);
        driverTarget.setHint("Цель водительской зоны");
        driverTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f)));
        EditText passengerTarget = new EditText(this);
        passengerTarget.setHint("Цель пассажирской зоны");
        passengerTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f)));
        EditText engineMinutes = new EditText(this);
        engineMinutes.setHint("Минуты работы двигателя");
        engineMinutes.setText(String.valueOf(prefs.getInt(SmartClimateController.KEY_ENGINE_MINUTES, 0)));
        CheckBox fogging = new CheckBox(this);
        fogging.setText("Запотевание стекол");
        fogging.setChecked(prefs.getBoolean(SmartClimateController.KEY_FOGGING, false));
        CheckBox call = new CheckBox(this);
        call.setText("Активный звонок");
        call.setChecked(prefs.getBoolean(SmartClimateController.KEY_CALL_ACTIVE, false));
        CheckBox dryAfterTrip = new CheckBox(this);
        dryAfterTrip.setText("Просушка после поездки");
        dryAfterTrip.setChecked(prefs.getBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, true));
        panel.addView(enabled);
        panel.addView(modeField, lpMatchWrap(0, 8, 0, 0));
        panel.addView(cabin, lpMatchWrap(0, 8, 0, 0));
        panel.addView(outside, lpMatchWrap(0, 8, 0, 0));
        panel.addView(driverTarget, lpMatchWrap(0, 8, 0, 0));
        panel.addView(passengerTarget, lpMatchWrap(0, 8, 0, 0));
        panel.addView(engineMinutes, lpMatchWrap(0, 8, 0, 0));
        panel.addView(fogging);
        panel.addView(call);
        panel.addView(dryAfterTrip);
        Button save = Ui.button(this, "Сохранить Smart climate");
        save.setTextColor(Ui.primaryText(this));
        save.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean(SmartClimateController.KEY_ENABLED, enabled.isChecked())
                    .putString(SmartClimateController.KEY_MODE, modeField.getText().toString().trim())
                    .putFloat(SmartClimateController.KEY_CABIN_TEMP, AutomationEngine.parseFloat(cabin.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_OUTSIDE_TEMP, AutomationEngine.parseFloat(outside.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_DRIVER_TARGET, AutomationEngine.parseFloat(driverTarget.getText().toString(), 22.0f))
                    .putFloat(SmartClimateController.KEY_PASSENGER_TARGET, AutomationEngine.parseFloat(passengerTarget.getText().toString(), 22.0f))
                    .putInt(SmartClimateController.KEY_ENGINE_MINUTES, AutomationEngine.parseInt(engineMinutes.getText().toString(), 0))
                    .putBoolean(SmartClimateController.KEY_FOGGING, fogging.isChecked())
                    .putBoolean(SmartClimateController.KEY_CALL_ACTIVE, call.isChecked())
                    .putBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, dryAfterTrip.isChecked())
                    .apply();
            Ui.toast(this, "Smart climate сохранен");
            refreshState();
        });
        Button run = Ui.button(this, "Контроллер: шаг сейчас");
        run.setTextColor(Ui.primaryText(this));
        run.setOnClickListener(v -> {
            save.performClick();
            new AlertDialog.Builder(this)
                    .setTitle("Smart climate")
                    .setMessage(AutomationEngine.runSmartClimate(this))
                    .setPositiveButton("OK", null)
                    .show();
        });
        panel.addView(save, lpMatchWrap(0, 8, 0, 0));
        panel.addView(run, lpMatchWrap(0, 8, 0, 0));
        return panel;
    }

    private LinearLayout buildAdvancedPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Расширенный HVAC"));
        panel.addView(Ui.text(this, "Полный расширенный flow теперь живет здесь: воздух, очистка, зоны, hardkeys, pre/post climate и быстрые HVAC-макросы.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addAdvancedCard(grid, "Air Quality", "AQS, CO2, ions, fragrance", new QuickItem[]{
                new QuickItem("AQS On", () -> command(EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("CO2 On", () -> command(EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Ionizer", () -> command(EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Fragrance", () -> command(EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Defrost & Dry", "Front/rear/max/auto dry", new QuickItem[]{
                new QuickItem("Front", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rear", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Front Max", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Dry", () -> command(EcarxVehicleAdapter.HVAC_AUTOMATIC_VENTILATION_DRY, EcarxVehicleAdapter.COMMON_ON))
        });
        addAdvancedCard(grid, "Zones & Sync", "Single/dual/triple/four", new QuickItem[]{
                new QuickItem("Single", () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE)),
                new QuickItem("Dual", () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL)),
                new QuickItem("Triple", () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_TRIPLE)),
                new QuickItem("Four", () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_FOUR))
        });
        addAdvancedCard(grid, "Drive Climate", "Rapid cooling/warming and pre/post climate", new QuickItem[]{
                new QuickItem("Rapid Cool", () -> command(EcarxVehicleAdapter.HVAC_RAPID_COOLING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rapid Warm", () -> command(EcarxVehicleAdapter.HVAC_RAPID_WARMING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Pre-climate", () -> command(EcarxVehicleAdapter.HVAC_PRE_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Post-climate", () -> command(EcarxVehicleAdapter.HVAC_POST_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON))
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 12));

        LinearLayout hardkeys = Ui.row(this);
        addClimateActionChip(hardkeys, "Fan +", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_UP));
        addClimateActionChip(hardkeys, "Fan -", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_DOWN));
        addClimateActionChip(hardkeys, "Temp Sync", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_TEMP_SYNC));
        addClimateActionChip(hardkeys, "A/C Key", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_AC));
        panel.addView(hardkeys, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private View buildClimateReadbackGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addReadbackCard(grid, "Основное состояние", readbackByIds(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED));
        addReadbackCard(grid, "Температура и датчики", readback(
                floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT),
                sensorReadback("В салоне", EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_INDOOR),
                sensorReadback("Снаружи", EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_AMBIENT),
                singleReadback(EcarxVehicleAdapter.HVAC_TEMP_UNIT),
                singleReadback(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE)));
        addReadbackCard(grid, "Сиденья и руль", developerModeEnabled()
                ? readback(
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                singleReadback(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT))
                : readback(
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                singleReadback(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT)));
        addReadbackCard(grid, "Качество воздуха", readback(
                singleReadback(EcarxVehicleAdapter.HVAC_AQS_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_CO2_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_IONS_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE)));
        addReadbackCard(grid, "Обдув и осушение", readback(
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_FRONT),
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_REAR),
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX),
                singleReadback(EcarxVehicleAdapter.HVAC_AUTOMATIC_VENTILATION_DRY)));
        addReadbackCard(grid, "Дополнительные режимы", readback(
                singleReadback(EcarxVehicleAdapter.HVAC_RAPID_COOLING),
                singleReadback(EcarxVehicleAdapter.HVAC_RAPID_WARMING),
                singleReadback(EcarxVehicleAdapter.HVAC_PRE_CLIMATISATION),
                singleReadback(EcarxVehicleAdapter.HVAC_POST_CLIMATISATION)));
        return grid;
    }

    private LinearLayout buildTempCard(String label, int zone) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, label));
        TextView temp = Ui.text(this, zone == EcarxVehicleAdapter.ZONE_DRIVER_LEFT ? "22.0°C" : "22.0°C", 34, true);
        card.addView(temp);
        SeekBar seek = new SeekBar(this);
        seek.setMax(32);
        seek.setProgress(12);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float t = 16f + progress / 2f;
                temp.setText(String.format(Locale.US, "%.1f°C", t));
                if (fromUser) {
                    new EcarxVehicleAdapter(ClimateActivity.this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, t);
                    refreshState();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        card.addView(seek);
        card.setOnLongClickListener(v -> {
            showTemperatureSheet(label, zone);
            return true;
        });
        return card;
    }

    private void addClimateToggle(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14));
        tile.setBackground(Ui.cardBg(this, Color.argb(84, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 20), Color.argb(90, 255, 255, 255)));
        tile.setOnClickListener(v -> action.run());
        tile.setOnLongClickListener(v -> {
            showQuickHvacSheet();
            return true;
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private void addClimateActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
        b.setOnClickListener(v -> action.run());
        b.setOnLongClickListener(v -> {
            showQuickHvacSheet();
            return true;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addStockSegment(LinearLayout root, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(72, 77, 163, 255) : Color.argb(232, 238, 246, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.argb(72, 77, 163, 255) : Color.argb(96, 77, 163, 255)));
        b.setOnClickListener(v -> action.run());
        root.addView(b, lpMatchWrap(0, 8, 0, 0));
    }

    private void addStockPill(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(136, 77, 163, 255), Ui.dp(this, 22), Color.argb(128, 121, 190, 255)));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1f);
        lp.leftMargin = Ui.dp(this, 5);
        lp.rightMargin = Ui.dp(this, 5);
        row.addView(b, lp);
    }

    private void setStockDriverTemp(float value) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this)
                .setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, value);
        refreshState();
        Ui.toast(this, result.success ? "Температура " + value : result.message);
    }

    private void addAdvancedCard(GridLayout grid, String title, String body, QuickItem[] items) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, body, 15, false));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
            button.setBackground(Ui.cardBg(this,
                    Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                    Ui.dp(this, 16),
                    Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
            button.setOnClickListener(v -> item.action.run());
            actions.addView(button, lpMatchWrap(0, 8, 0, 0));
        }
        card.addView(actions);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private void addReadbackCard(GridLayout grid, String title, String body) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        TextView value = Ui.text(this, body, 13, false);
        value.setTextColor(Ui.secondaryText(this));
        card.addView(value);
        card.setOnClickListener(v -> showReadbackSheet());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private LinearLayout buildClimateBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Auto", () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON), mode == Mode.HOME, new QuickItem[]{
                new QuickItem("Auto", () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Eco", () -> command(EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("A/C", () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "A/C", () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON), false, new QuickItem[]{
                new QuickItem("A/C On", () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("A/C Max", () -> command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Off", () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF))
        });
        addDockButton(dock, "Обдув", this::showDefrostSheet, false, new QuickItem[]{
                new QuickItem("Перед", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Зад", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Перед макс", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Умный", () -> command(EcarxVehicleAdapter.HVAC_AI_POWER, EcarxVehicleAdapter.COMMON_ON), false, new QuickItem[]{
                new QuickItem("Умный климат", () -> openMode(Mode.SMART)),
                new QuickItem("Rapid Cool", () -> command(EcarxVehicleAdapter.HVAC_RAPID_COOLING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rapid Warm", () -> command(EcarxVehicleAdapter.HVAC_RAPID_WARMING, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Пресеты", () -> openMode(Mode.PRESETS), mode == Mode.PRESETS || mode == Mode.PRESET_EDITOR, new QuickItem[]{
                new QuickItem("Пресеты", () -> openMode(Mode.PRESETS)),
                new QuickItem("Создать", () -> openPresetEditor("", defaultPresetText())),
                new QuickItem("Комфорт", () -> openMode(Mode.HOME))
        });
        addDockButton(dock, "Расширенно", () -> openMode(Mode.ADVANCED), mode == Mode.ADVANCED, new QuickItem[]{
                new QuickItem("Расширенно", () -> openMode(Mode.ADVANCED)),
                new QuickItem("Состояние", this::showReadbackSheet),
                new QuickItem("Комфорт", () -> openMode(Mode.HOME))
        });
        return dock;
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

    private void openPresetEditor(String name, String commandsText) {
        editingPresetName = name;
        getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().putString("__draft__", commandsText).apply();
        openMode(Mode.PRESET_EDITOR);
    }

    private void showQuickHvacSheet() {
        showActionSheet("Быстрый климат", new QuickItem[]{
                new QuickItem("Климат выкл", () -> command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF)),
                new QuickItem("Auto", () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("AC Max", () -> command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Auto fan normal", () -> command(EcarxVehicleAdapter.HVAC_AUTO_FAN_SETTING, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.AUTO_FAN_SETTING_NORMAL)),
                new QuickItem("Рецирк. внутр", () -> command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER)),
                new QuickItem("Рецирк. наруж", () -> command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE))
        });
    }

    private void showDefrostSheet() {
        showActionSheet("Обдув стекла", new QuickItem[]{
                new QuickItem("Перед", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Зад", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Перед макс", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Auto Defrost", () -> command(EcarxVehicleAdapter.HVAC_AUTO_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON))
        });
    }

    private void showTemperatureSheet(String label, int zone) {
        showActionSheet(label, new QuickItem[]{
                new QuickItem("18.0°C", () -> new EcarxVehicleAdapter(this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, 18.0f)),
                new QuickItem("20.0°C", () -> new EcarxVehicleAdapter(this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, 20.0f)),
                new QuickItem("22.0°C", () -> new EcarxVehicleAdapter(this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, 22.0f)),
                new QuickItem("24.0°C", () -> new EcarxVehicleAdapter(this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, 24.0f))
        });
    }

    private void showReadbackSheet() {
        showActionSheet("Состояние климата", new QuickItem[]{
                new QuickItem(singleReadback(EcarxVehicleAdapter.HVAC_POWER), this::refreshState),
                new QuickItem(singleReadback(EcarxVehicleAdapter.HVAC_AUTO), this::refreshState),
                new QuickItem(singleReadback(EcarxVehicleAdapter.HVAC_AC), this::refreshState),
                new QuickItem(floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT), this::refreshState),
                new QuickItem(floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT), this::refreshState),
                new QuickItem(singleReadback(EcarxVehicleAdapter.HVAC_FAN_SPEED), this::refreshState)
        });
    }

    private void showActionSheet(String title, QuickItem[] items) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        sheet.addView(Ui.label(this, "Действия климата"));
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
                refreshState();
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

    private void showBlowingSheet() {
        showActionSheet("Обдув", new QuickItem[]{
                new QuickItem("Стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW)),
                new QuickItem("Лицо + стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW)),
                new QuickItem("Все зоны", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_ALL)),
                new QuickItem("Auto fan silent", () -> command(EcarxVehicleAdapter.HVAC_AUTO_FAN_SETTING, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.AUTO_FAN_SETTING_SILENT))
        });
    }

    private void command(int functionId, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.support(functionId);
        if (support != null && !support.isSupported()) {
            Ui.toast(this, "Функция недоступна на этом автомобиле");
            return;
        }
        EcarxVehicleAdapter.Result result = adapter.set(functionId, value);
        refreshState();
        Ui.toast(this, result.success ? "Климат обновлен" : result.message);
    }

    private void command(int functionId, int zone, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.support(functionId, zone);
        if (support != null && !support.isSupported()) {
            Ui.toast(this, "Функция недоступна в этой зоне");
            return;
        }
        EcarxVehicleAdapter.Result result = adapter.set(functionId, zone, value);
        refreshState();
        Ui.toast(this, result.success ? "Климат обновлен" : result.message);
    }

    private void applyClimatePreset(EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        boolean ok = true;
        for (EcarxVehicleAdapter.Result result : results) ok &= result.success;
        refreshState();
        Ui.toast(this, ok ? "Пресет применен" : "Пресет выполнен частично");
    }

    private void refreshState() {
        boolean unified = climateZonesUnified();
        if (lastUnifiedClimate == null || lastUnifiedClimate != unified) {
            lastUnifiedClimate = unified;
            if (contentHost != null) {
                renderContent();
            }
        }
        if (topModeValue != null) topModeValue.setText(simpleState(EcarxVehicleAdapter.HVAC_AUTO, "Auto"));
        if (topZoneValue != null) topZoneValue.setText(simpleState(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, "Zone"));
        if (topCabinValue != null) topCabinValue.setText(cabinSummary());
        if (driverTempValue != null) driverTempValue.setText(formatHeroTemp(floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT)));
        if (passengerTempValue != null) passengerTempValue.setText(formatHeroTemp(floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT)));
        if (indoorTempValue != null) indoorTempValue.setText("В салоне: " + sensorTemperature(EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_INDOOR));
        if (outdoorTempValue != null) outdoorTempValue.setText("Снаружи: " + sensorTemperature(EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_AMBIENT));
        if (fanValue != null) fanValue.setText("Вентилятор: " + simpleState(EcarxVehicleAdapter.HVAC_FAN_SPEED, "Fan"));
        if (summaryValue != null) summaryValue.setText(buildSummary());
        updateHeroControls();
    }

    private String cabinSummary() {
        return sensorTemperature(EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_INDOOR) + " · "
                + sensorTemperature(EcarxVehicleAdapter.SENSOR_TYPE_TEMPERATURE_AMBIENT);
    }

    private boolean climateZonesUnified() {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).get(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE);
        if (result == null) return false;
        if (result.value == EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE) return true;
        String message = result.message == null ? "" : result.message.toLowerCase(Locale.ROOT);
        return message.contains("single") || message.contains(EcarxVehicleAdapter.hex(EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE).toLowerCase(Locale.ROOT));
    }

    private String buildSummary() {
        return "Питание " + simpleState(EcarxVehicleAdapter.HVAC_POWER, "выкл")
                + " · A/C " + simpleState(EcarxVehicleAdapter.HVAC_AC, "выкл")
                + " · Обдув стекла " + simpleState(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, "выкл");
    }

    private String simpleState(int functionId, String fallback) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).get(functionId);
        if (result == null || result.message == null || result.message.trim().isEmpty()) return fallback;
        if (!result.isSupported()) return fallback;
        return compact(result.message);
    }

    private String floatState(int functionId, int zone) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).getFloat(functionId, zone);
        if (result == null || result.message == null || result.message.trim().isEmpty()) return "--";
        return compact(result.message);
    }

    private String sensorTemperature(int sensorId) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.supportSensor(sensorId);
        if (support == null || !support.isSupported()) return "--";
        EcarxVehicleAdapter.Result result = adapter.getSensorFloat(sensorId);
        if (result == null || result.message == null || result.message.trim().isEmpty()) return "--";
        return compact(result.message);
    }

    private String sensorReadback(String label, int sensorId) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        return label + ": " + compact(adapter.supportSensor(sensorId).message) + "\n" + compact(adapter.getSensorFloat(sensorId).message);
    }

    private String zonedReadback(int functionId, int zone) {
        return compact(new EcarxVehicleAdapter(this).support(functionId, zone).message) + "\n" + compact(new EcarxVehicleAdapter(this).get(functionId, zone).message);
    }

    private String singleReadback(int functionId) {
        return compact(new EcarxVehicleAdapter(this).support(functionId).message) + "\n" + compact(new EcarxVehicleAdapter(this).get(functionId).message);
    }

    private String floatReadback(int functionId, int zone) {
        return compact(new EcarxVehicleAdapter(this).support(functionId, zone).message) + "\n" + compact(new EcarxVehicleAdapter(this).getFloat(functionId, zone).message);
    }

    private String readback(String... lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
        }
        return sb.toString();
    }

    private String readbackByIds(int... ids) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(compact(adapter.get(id).message));
        }
        return sb.toString();
    }

    private String compact(String message) {
        if (message == null) return "--";
        String line = message.replace('\n', ' ').trim();
        line = line.replace("getCustomizeFunctionValue", "").replace("getFunctionValue", "").trim();
        int eq = line.indexOf('=');
        if (eq >= 0 && eq + 1 < line.length()) line = line.substring(eq + 1).trim();
        return line.length() > 72 ? line.substring(0, 72) : line;
    }

    private int fanSpeedValue(int level) {
        int normalized = Math.max(0, Math.min(9, level));
        if (normalized == 0) return EcarxVehicleAdapter.COMMON_OFF;
        return EcarxVehicleAdapter.HVAC_FAN_SPEED + normalized;
    }

    private void cycleSeatClimate(int functionId, int zone) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.support(functionId, zone);
        if (support != null && !support.isSupported()) {
            Ui.toast(this, "Функция недоступна в этой зоне");
            return;
        }
        EcarxVehicleAdapter.Result current = adapter.get(functionId, zone);
        int next = nextSeatClimateLevel(current == null ? 0 : current.value);
        EcarxVehicleAdapter.Result result = adapter.set(functionId, zone, next);
        refreshState();
        Ui.toast(this, result.success ? "Климат обновлен" : result.message);
    }

    private int nextSeatClimateLevel(int current) {
        if (current == EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_1
                || current == EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_LEVEL_1) {
            return current + 1;
        }
        if (current == EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_2
                || current == EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_LEVEL_2) {
            return current + 1;
        }
        if (current == EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_3
                || current == EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_LEVEL_3
                || current == EcarxVehicleAdapter.HVAC_SEAT_HEATING_AUTO
                || current == EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_AUTO) {
            return EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF;
        }
        if (current <= 0 || current == 0xff) return EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_1;
        return current;
    }

    private void cycleWheelHeat() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result support = adapter.support(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT);
        if (support != null && !support.isSupported()) {
            Ui.toast(this, "Подогрев руля недоступен на этом автомобиле");
            return;
        }
        EcarxVehicleAdapter.Result current = adapter.get(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT);
        int next = nextWheelHeatLevel(current == null ? 0 : current.value);
        EcarxVehicleAdapter.Result result = adapter.set(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, next);
        refreshState();
        Ui.toast(this, result.success ? "Климат обновлен" : result.message);
    }

    private int nextWheelHeatLevel(int current) {
        if (current == EcarxVehicleAdapter.WHEEL_HEAT_LOW) return EcarxVehicleAdapter.WHEEL_HEAT_MID;
        if (current == EcarxVehicleAdapter.WHEEL_HEAT_MID) return EcarxVehicleAdapter.WHEEL_HEAT_HIGH;
        if (current == EcarxVehicleAdapter.WHEEL_HEAT_HIGH || current == EcarxVehicleAdapter.WHEEL_HEAT_AUTO) {
            return EcarxVehicleAdapter.COMMON_OFF;
        }
        return EcarxVehicleAdapter.WHEEL_HEAT_LOW;
    }

    private void updateHeroControls() {
        if (heroTempSeekBar == null && heroSyncToggle == null) return;
        updatingHeroControls = true;
        try {
            boolean unified = climateZonesUnified();
            if (heroDriverLabel != null) heroDriverLabel.setText(unified ? "Общая температура" : "Водитель");
            if (heroPassengerCard != null) heroPassengerCard.setVisibility(unified ? View.GONE : View.VISIBLE);
            if (heroTempSeekBar != null) {
                heroTempSeekBar.setProgress(heroTempProgressFromValue(parseClimateTemp(floatState(
                        EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT), 22.0f)));
            }
            if (heroSyncToggle != null) {
                String zone = simpleState(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, "");
                boolean sync = zone.contains("10010502") || zone.toLowerCase(Locale.ROOT).contains("dual");
                heroSyncToggle.setVisibility(unified ? View.GONE : View.VISIBLE);
                heroSyncToggle.setChecked(sync);
                styleHeroSyncToggle(sync);
            }
        } finally {
            updatingHeroControls = false;
        }
    }

    private int heroTempProgressFromValue(float temp) {
        float clamped = Math.max(16.0f, Math.min(32.0f, temp));
        return Math.round((clamped - 16.0f) / 0.5f);
    }

    private float heroTempValueFromProgress(int progress) {
        return 16.0f + (Math.max(0, Math.min(32, progress)) * 0.5f);
    }

    private float parseClimateTemp(String value, float fallback) {
        if (value == null) return fallback;
        String normalized = value.replace("°C", "").replace("°F", "").trim();
        if ("HI".equalsIgnoreCase(normalized)) return 32.0f;
        if ("LO".equalsIgnoreCase(normalized)) return 16.0f;
        try {
            return Float.parseFloat(normalized);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String formatHeroTemp(String value) {
        String compact = compact(value);
        if ("--".equals(compact)) return compact;
        float parsed = parseClimateTemp(compact, Float.NaN);
        if (!Float.isNaN(parsed)) {
            if (parsed >= 31.5f) return "HI";
            if (parsed <= 16.0f) return "LO";
        }
        return compact.endsWith("°C") || compact.endsWith("°F") || "HI".equalsIgnoreCase(compact) || "LO".equalsIgnoreCase(compact)
                ? compact : compact + "°";
    }

    private void styleHeroSyncToggle(boolean active) {
        if (heroSyncToggle == null) return;
        heroSyncToggle.setTextColor(active ? Color.WHITE : Color.rgb(72, 181, 165));
        heroSyncToggle.setBackground(Ui.cardBg(this,
                active ? Color.argb(196, 72, 181, 165) : Color.argb(38, 72, 181, 165),
                Ui.dp(this, 16),
                active ? Color.argb(140, 72, 181, 165) : Color.argb(72, 72, 181, 165)));
    }

    private String modeLabel() {
        if (mode == Mode.ADVANCED) return "HVAC / Расширенно";
        if (mode == Mode.PRESETS) return "HVAC / Пресеты";
        if (mode == Mode.PRESET_EDITOR) return "HVAC / Редактор";
        if (mode == Mode.SMART) return "HVAC / Умный";
        return "HVAC / Комфорт";
    }

    private String defaultPresetText() {
        return EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_POWER) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_AUTO) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_FAN_SPEED) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.FAN_SPEED_3);
    }

    private void saveClimatePreset(String name, String encoded) {
        SharedPreferences prefs = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE);
        ArrayList<String> names = climatePresetNames();
        if (!names.contains(name)) names.add(name);
        prefs.edit().putString(name, encoded).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private void deleteClimatePreset(String name) {
        ArrayList<String> names = climatePresetNames();
        names.remove(name);
        getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().remove(name).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private ArrayList<String> climatePresetNames() {
        String order = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(CLIMATE_PRESET_ORDER, "");
        ArrayList<String> names = new ArrayList<>();
        for (String item : order.split("\n")) {
            String name = item.trim();
            if (!name.isEmpty()) names.add(name);
        }
        return names;
    }

    private String encodeCommands(EcarxVehicleAdapter.Command[] commands) {
        StringBuilder sb = new StringBuilder();
        for (EcarxVehicleAdapter.Command command : commands) {
            sb.append(EcarxVehicleAdapter.hex(command.functionId))
                    .append(",")
                    .append(command.zone)
                    .append(",")
                    .append(EcarxVehicleAdapter.hex(command.value))
                    .append("\n");
        }
        return sb.toString();
    }

    private EcarxVehicleAdapter.Command[] decodeCommands(String raw) {
        ArrayList<EcarxVehicleAdapter.Command> commands = new ArrayList<>();
        for (String line : raw.split("\n")) {
            String clean = line.trim();
            if (clean.isEmpty() || clean.startsWith("#")) continue;
            String[] parts = clean.split(",");
            if (parts.length < 2) continue;
            try {
                int functionId = parseNumber(parts[0]);
                int zone = parts.length > 2 ? parseNumber(parts[1]) : 0;
                int value = parseNumber(parts.length > 2 ? parts[2] : parts[1]);
                commands.add(new EcarxVehicleAdapter.Command(functionId, zone, value));
            } catch (Exception ignored) {
            }
        }
        return commands.toArray(new EcarxVehicleAdapter.Command[0]);
    }

    private int parseNumber(String raw) {
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("0x")) return (int) Long.parseLong(value.substring(2), 16);
        return Integer.parseInt(value);
    }

    private String join(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) sb.append(name).append("\n");
        return sb.toString();
    }

    private void animatePulse(View view) {
        view.animate().scaleX(1.04f).scaleY(1.04f).setDuration(110).setInterpolator(new DecelerateInterpolator()).withEndAction(
                () -> view.animate().scaleX(1f).scaleY(1f).setDuration(180).setInterpolator(new DecelerateInterpolator()).start()
        ).start();
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private enum Mode {
        HOME,
        ADVANCED,
        PRESETS,
        PRESET_EDITOR,
        SMART
    }

    private static final class QuickItem {
        final String label;
        final Runnable action;

        QuickItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private boolean developerModeEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getBoolean(KEY_DEVELOPER_MODE, false);
    }

    private static final class FluentFanDial extends View {
        interface OnLevelChanged {
            void onLevelChanged(int level);
        }

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int level = 5;
        private OnLevelChanged listener;

        FluentFanDial(Context context) {
            super(context);
            setClickable(true);
        }

        void setLevel(int level) {
            this.level = clampLevel(level);
            invalidate();
        }

        void setOnLevelChanged(OnLevelChanged listener) {
            this.listener = listener;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float radius = Math.min(w, h) * 0.36f;

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, w, h,
                    new int[]{Color.argb(180, 77, 163, 255), Color.argb(150, 53, 208, 127)},
                    null,
                    Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setShader(null);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 10));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.argb(64, 255, 255, 255));
            RectF arc = new RectF(cx - radius - Ui.dp(getContext(), 18), cy - radius - Ui.dp(getContext(), 18),
                    cx + radius + Ui.dp(getContext(), 18), cy + radius + Ui.dp(getContext(), 18));
            canvas.drawArc(arc, 135, 270, false, paint);

            paint.setColor(Color.WHITE);
            canvas.drawArc(arc, 135, progressSweep(), false, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.WHITE);
            paint.setTextSize(Ui.dp(getContext(), 42));
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(level), cx, cy + Ui.dp(getContext(), 14), paint);
            paint.setFakeBoldText(false);
            paint.setTextSize(Ui.dp(getContext(), 12));
            paint.setColor(Color.argb(210, 255, 255, 255));
            canvas.drawText("FAN", cx, cy + Ui.dp(getContext(), 42), paint);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getActionMasked() != MotionEvent.ACTION_DOWN
                    && event.getActionMasked() != MotionEvent.ACTION_MOVE
                    && event.getActionMasked() != MotionEvent.ACTION_UP) {
                return true;
            }
            float dx = event.getX() - getWidth() / 2f;
            float dy = event.getY() - getHeight() / 2f;
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            double normalized = angle + 225.0;
            while (normalized < 0) normalized += 360.0;
            while (normalized > 360.0) normalized -= 360.0;
            int next;
            if (normalized < 90) next = 4;
            else if (normalized < 190) next = 5;
            else next = 6;
            next = clampLevel(next);
            if (next != level) {
                level = next;
                invalidate();
                if (listener != null) listener.onLevelChanged(level);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP && listener != null) {
                listener.onLevelChanged(level);
            }
            return true;
        }

        private float progressSweep() {
            if (level <= 4) return 90f;
            if (level == 5) return 180f;
            return 270f;
        }

        private static int clampLevel(int value) {
            return Math.max(4, Math.min(6, value));
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
}
