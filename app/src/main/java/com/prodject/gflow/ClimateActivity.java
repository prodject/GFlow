package com.prodject.gflow;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public class ClimateActivity extends Activity {
    static final String EXTRA_MODE = "climate_mode";
    static final String MODE_ADVANCED = "advanced";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout contentHost;
    private TextView topModeValue;
    private TextView topZoneValue;
    private TextView topCabinValue;
    private TextView driverTempValue;
    private TextView passengerTempValue;
    private TextView fanValue;
    private TextView summaryValue;
    private Mode mode = Mode.HOME;
    private final Runnable stateTicker = new Runnable() {
        @Override public void run() {
            refreshState();
            handler.postDelayed(this, 20_000L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MODE_ADVANCED.equals(getIntent().getStringExtra(EXTRA_MODE))) mode = Mode.ADVANCED;
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

        root.addView(buildClimateTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
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
            contentHost.addView(buildClimateMainPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildClimateReadbackGrid(), lpMatchWrap(0, 0, 0, 0));
        } else {
            contentHost.addView(buildAdvancedPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildClimateReadbackGrid(), lpMatchWrap(0, 0, 0, 0));
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
        titleBlock.addView(Ui.label(this, mode == Mode.HOME ? "HVAC / Comfort" : "HVAC / Advanced"));
        TextView title = Ui.text(this, "Климат", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        topModeValue = buildTopStat(bar, "Режим", "...");
        topZoneValue = buildTopStat(bar, "Зона", "...");
        topCabinValue = buildTopStat(bar, "Салон", "...");
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
        hero.addView(Ui.label(this, "Climate Overview"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        driverTempValue = Ui.text(this, "Водитель: --", 18, true);
        passengerTempValue = Ui.text(this, "Пассажир: --", 18, true);
        fanValue = Ui.text(this, "Вентилятор: --", 18, true);
        summaryValue = Ui.text(this, "Состояние HVAC: ожидание readback", 14, false);
        summaryValue.setTextColor(Ui.secondaryText(this));
        left.addView(driverTempValue);
        left.addView(passengerTempValue);
        left.addView(fanValue);
        left.addView(summaryValue);
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        AirFlowView flow = new AirFlowView(this);
        LinearLayout.LayoutParams flowLp = new LinearLayout.LayoutParams(Ui.dp(this, 320), Ui.dp(this, 220));
        flowLp.leftMargin = Ui.dp(this, 12);
        row.addView(flow, flowLp);
        hero.addView(row);

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Comfort", () -> openMode(Mode.HOME));
        addActionChip(actions, "Advanced", () -> openMode(Mode.ADVANCED));
        addActionChip(actions, "Readback", this::showReadbackSheet);
        addActionChip(actions, "Quick HVAC", this::showQuickHvacSheet);
        hero.addView(actions, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private LinearLayout buildClimateComfortPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Comfort Panel"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        addClimateToggle(grid, "HVAC", Ui.CYAN, () -> command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Auto", Ui.SUCCESS, () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C", Ui.CYAN, () -> command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C Max", Ui.WARNING, () -> command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Eco", Color.rgb(69, 186, 134), () -> command(EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Sync", Color.rgb(103, 147, 255), () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL));
        addClimateToggle(grid, "Split", Color.rgb(134, 103, 255), () -> command(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE));
        addClimateToggle(grid, "°F", Color.rgb(255, 122, 89), () -> command(EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_F));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildClimateMainPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Driver / Passenger"));

        LinearLayout tempRow = Ui.row(this);
        tempRow.setGravity(Gravity.CENTER_VERTICAL);
        tempRow.addView(buildTempCard("Водитель", EcarxVehicleAdapter.ZONE_DRIVER_LEFT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));

        LinearLayout center = Ui.glassCard(this);
        center.addView(Ui.label(this, "Airflow Visual"));
        View flow = new AirFlowView(this);
        center.addView(flow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 220)));
        TextView fanLabel = Ui.text(this, "Вентилятор: 3", 18, true);
        center.addView(fanLabel);
        SeekBar fan = new SeekBar(this);
        fan.setMax(8);
        fan.setProgress(2);
        fan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fanLabel.setText("Вентилятор: " + (progress + 1));
                if (fromUser) {
                    command(EcarxVehicleAdapter.HVAC_FAN_SPEED, progress + 1);
                    animatePulse(flow);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        center.addView(fan);
        LinearLayout quickModes = Ui.row(this);
        addActionChip(quickModes, "Лицо", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE));
        addActionChip(quickModes, "Ноги", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG));
        addActionChip(quickModes, "Стекло", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW));
        center.addView(quickModes, lpMatchWrap(0, 8, 0, 0));
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f);
        centerLp.leftMargin = Ui.dp(this, 12);
        centerLp.rightMargin = Ui.dp(this, 12);
        tempRow.addView(center, centerLp);

        tempRow.addView(buildTempCard("Пассажир", EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));
        panel.addView(tempRow);

        LinearLayout seats = Ui.row(this);
        seats.setWeightSum(4f);
        addClimateActionChip(seats, "Seat Heat", () -> command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addClimateActionChip(seats, "Seat Vent", () -> command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addClimateActionChip(seats, "Wheel Heat", () -> command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        addClimateActionChip(seats, "Defrost", this::showDefrostSheet);
        panel.addView(seats, lpMatchWrap(0, 16, 0, 0));

        LinearLayout presets = Ui.row(this);
        presets.setWeightSum(3f);
        addClimateActionChip(presets, "Тихий", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1)));
        addClimateActionChip(presets, "Комфорт", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3)));
        addClimateActionChip(presets, "Прогрев", () -> applyClimatePreset(
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW)));
        panel.addView(presets, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAdvancedPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Advanced HVAC"));
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
        addActionChip(hardkeys, "Fan +", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_UP));
        addActionChip(hardkeys, "Fan -", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_DOWN));
        addActionChip(hardkeys, "Temp Sync", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_TEMP_SYNC));
        addActionChip(hardkeys, "A/C Key", () -> command(EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_AC));
        panel.addView(hardkeys, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private View buildClimateReadbackGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addReadbackCard(grid, "HVAC Core", readback(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED));
        addReadbackCard(grid, "Temperature", readback(
                floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                floatReadback(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT),
                singleReadback(EcarxVehicleAdapter.HVAC_TEMP_UNIT),
                singleReadback(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE)));
        addReadbackCard(grid, "Seats / Wheel", readback(
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                zonedReadback(EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                singleReadback(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT)));
        addReadbackCard(grid, "Air Quality", readback(
                singleReadback(EcarxVehicleAdapter.HVAC_AQS_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_CO2_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_IONS_SWITCH),
                singleReadback(EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE)));
        addReadbackCard(grid, "Defrost / Dry", readback(
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_FRONT),
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_REAR),
                singleReadback(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX),
                singleReadback(EcarxVehicleAdapter.HVAC_AUTOMATIC_VENTILATION_DRY)));
        addReadbackCard(grid, "Advanced Modes", readback(
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
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
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

    private void addAdvancedCard(GridLayout grid, String title, String body, QuickItem[] items) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, body, 15, false));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Color.WHITE);
            button.setBackground(Ui.cardBg(this, Color.argb(54, 255, 255, 255), Ui.dp(this, 16), Color.TRANSPARENT));
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
        addDockButton(dock, "Defrost", this::showDefrostSheet, false, new QuickItem[]{
                new QuickItem("Front", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rear", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Front Max", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Smart", () -> command(EcarxVehicleAdapter.HVAC_AI_POWER, EcarxVehicleAdapter.COMMON_ON), false, new QuickItem[]{
                new QuickItem("Smart climate", () -> command(EcarxVehicleAdapter.HVAC_AI_POWER, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rapid Cool", () -> command(EcarxVehicleAdapter.HVAC_RAPID_COOLING, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rapid Warm", () -> command(EcarxVehicleAdapter.HVAC_RAPID_WARMING, EcarxVehicleAdapter.COMMON_ON))
        });
        addDockButton(dock, "Расширенно", () -> openMode(Mode.ADVANCED), mode == Mode.ADVANCED, new QuickItem[]{
                new QuickItem("Advanced", () -> openMode(Mode.ADVANCED)),
                new QuickItem("Readback", this::showReadbackSheet),
                new QuickItem("Comfort", () -> openMode(Mode.HOME))
        });
        return dock;
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

    private void showQuickHvacSheet() {
        showActionSheet("Quick HVAC", new QuickItem[]{
                new QuickItem("HVAC On", () -> command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Auto", () -> command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Recirculation Auto", () -> command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_AUTO)),
                new QuickItem("All vents", () -> command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL))
        });
    }

    private void showDefrostSheet() {
        showActionSheet("Defrost", new QuickItem[]{
                new QuickItem("Front", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Rear", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Front Max", () -> command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON)),
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
        showActionSheet("HVAC Readback", new QuickItem[]{
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
        sheet.addView(Ui.label(this, "Climate Actions"));
        sheet.addView(Ui.text(this, title, 24, true));
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setTextColor(Color.WHITE);
            button.setBackground(Ui.cardBg(this, Color.argb(56, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
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

    private void command(int functionId, int value) {
        new EcarxVehicleAdapter(this).set(functionId, value);
        refreshState();
        Ui.toast(this, "HVAC updated");
    }

    private void command(int functionId, int zone, int value) {
        new EcarxVehicleAdapter(this).set(functionId, zone, value);
        refreshState();
        Ui.toast(this, "HVAC updated");
    }

    private void applyClimatePreset(EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        boolean ok = true;
        for (EcarxVehicleAdapter.Result result : results) ok &= result.success;
        refreshState();
        Ui.toast(this, ok ? "Пресет применен" : "Пресет выполнен частично");
    }

    private void refreshState() {
        if (topModeValue != null) topModeValue.setText(simpleState(EcarxVehicleAdapter.HVAC_AUTO, "Auto"));
        if (topZoneValue != null) topZoneValue.setText(simpleState(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, "Zone"));
        if (topCabinValue != null) topCabinValue.setText(cabinSummary());
        if (driverTempValue != null) driverTempValue.setText("Водитель: " + floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT));
        if (passengerTempValue != null) passengerTempValue.setText("Пассажир: " + floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT));
        if (fanValue != null) fanValue.setText("Вентилятор: " + simpleState(EcarxVehicleAdapter.HVAC_FAN_SPEED, "Fan"));
        if (summaryValue != null) summaryValue.setText(buildSummary());
    }

    private String cabinSummary() {
        return floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT) + " / "
                + floatState(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
    }

    private String buildSummary() {
        return "Power " + simpleState(EcarxVehicleAdapter.HVAC_POWER, "off")
                + " · A/C " + simpleState(EcarxVehicleAdapter.HVAC_AC, "off")
                + " · Defrost " + simpleState(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, "off");
    }

    private String simpleState(int functionId, String fallback) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).get(functionId);
        if (result == null || result.message == null || result.message.trim().isEmpty()) return fallback;
        return compact(result.message);
    }

    private String floatState(int functionId, int zone) {
        EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).get(functionId, zone);
        if (result == null || result.message == null || result.message.trim().isEmpty()) return "--";
        return compact(result.message);
    }

    private String zonedReadback(int functionId, int zone) {
        return compact(new EcarxVehicleAdapter(this).support(functionId).message) + "\n" + compact(new EcarxVehicleAdapter(this).get(functionId, zone).message);
    }

    private String singleReadback(int functionId) {
        return compact(new EcarxVehicleAdapter(this).support(functionId).message) + "\n" + compact(new EcarxVehicleAdapter(this).get(functionId).message);
    }

    private String floatReadback(int functionId, int zone) {
        return compact(new EcarxVehicleAdapter(this).support(functionId).message) + "\n" + compact(new EcarxVehicleAdapter(this).get(functionId, zone).message);
    }

    private String readback(String... lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
        }
        return sb.toString();
    }

    private String compact(String message) {
        if (message == null) return "--";
        String line = message.replace('\n', ' ').trim();
        return line.length() > 72 ? line.substring(0, 72) : line;
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
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#080A0F"), Color.parseColor("#0D1420"), Color.parseColor("#101B2A")});
    }

    private enum Mode {
        HOME,
        ADVANCED
    }

    private static final class QuickItem {
        final String label;
        final Runnable action;

        QuickItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
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
