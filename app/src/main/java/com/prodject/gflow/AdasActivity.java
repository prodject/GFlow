package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class AdasActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String MODE_OVERVIEW = "overview";
    private static final String MODE_EXPERIMENTAL = "experimental";
    private static final String MODE_DIAGNOSTICS = "diagnostics";
    private static final String STATE_MODE = "adas_mode";
    private static final String STATE_LAST_LABEL = "adas_last_label";
    private static final String STATE_LAST_RAW = "adas_last_raw";
    private static final String STATE_ACC_GAP = "adas_acc_gap";
    private String currentMode = MODE_OVERVIEW;
    private String lastCommandLabel = "Команд пока не было";
    private String lastCommandRaw = "";
    private int currentAccGap = 3;
    private TextView topSafetyValue;
    private TextView topAccValue;
    private TextView topPdcValue;
    private TextView heroAebValue;
    private TextView heroFcwValue;
    private TextView heroLkaValue;
    private TextView heroPdcValue;
    private TextView heroAccValue;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentMode = savedInstanceState.getString(STATE_MODE, MODE_OVERVIEW);
            lastCommandLabel = savedInstanceState.getString(STATE_LAST_LABEL, lastCommandLabel);
            lastCommandRaw = savedInstanceState.getString(STATE_LAST_RAW, lastCommandRaw);
            currentAccGap = savedInstanceState.getInt(STATE_ACC_GAP, 3);
        }
        setContentView(buildAdasShell());
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MODE, currentMode);
        outState.putString(STATE_LAST_LABEL, lastCommandLabel);
        outState.putString(STATE_LAST_RAW, lastCommandRaw);
        outState.putInt(STATE_ACC_GAP, currentAccGap);
    }

    private View buildAdasShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildSafetyControls(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildAccPanel(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildPdcPanel(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildModeSwitcher(), lpMatchWrap(0, 0, 0, 16));
        if (experimentalFeaturesEnabled()) {
            root.addView(buildExperimentalPanel(), lpMatchWrap(0, 0, 0, 16));
        }
        if (developerModeEnabled()) {
            root.addView(buildDiagnosticsPanel(), lpMatchWrap(0, 0, 0, 16));
        }
        root.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 16));
        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.staggerIn(collectChildren(root), 40, 55);
        Ui.animateIn(dock, 220, 18f);
        return scroll;
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.press(back, this::finish);
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "Confidence / Cruise / Lane"));
        TextView title = Ui.text(this, "ADAS / Вождение", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        topSafetyValue = buildTopStat(bar, "Безопасность", "Базовый набор");
        topAccValue = buildTopStat(bar, "ACC", "Gap " + currentAccGap);
        topPdcValue = buildTopStat(bar, "PDC", "Готов");
        refreshHeaderState();
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
        card.setLayoutParams(lp);
        parent.addView(card);
        return valueView;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Drive Confidence"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        heroAebValue = metricLine(left, "Safety stack", "AEB · FCW · RCW");
        heroFcwValue = metricLine(left, "Lane stack", "LKA · LDW · ELKA");
        heroLkaValue = metricLine(left, "Cruise stack", "ACC gap " + currentAccGap);
        heroPdcValue = metricLine(left, "Parking bridge", "PDC ready");
        heroAccValue = metricLine(left, "Mode", MODE_OVERVIEW.equals(currentMode) ? "Everyday assist" : currentMode);
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        AdasVisualView visual = new AdasVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "AEB", () -> sendVehicle("AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "FCW", () -> sendVehicle("FCW включить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "LKA", () -> sendVehicle("LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "PDC", () -> sendVehicle("PDC включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(LinearLayout parent, String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        parent.addView(line);
        return line;
    }

    private LinearLayout buildSafetyControls() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Safety Stack"));
        panel.addView(Ui.muted(this, "Основной слой помощи водителю: collision, lane and blind-spot controls без developer noise."));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "AEB", Color.rgb(113, 91, 177), () -> sendVehicle("AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "FCW", Color.rgb(95, 133, 255), () -> sendVehicle("FCW включить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "LKA", Color.rgb(72, 184, 164), () -> sendVehicle("LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "LDW", Color.rgb(255, 179, 64), () -> sendVehicle("LDW включить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "RCW", Color.rgb(255, 122, 89), () -> sendVehicle("RCW включить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "ELKA", Color.rgb(129, 149, 255), () -> sendVehicle("ELKA включить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "LCA", Color.rgb(104, 196, 255), () -> sendVehicle("Lane change assist", EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "ALCA", Color.rgb(118, 142, 255), () -> sendVehicle("Auto lane change assist", EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "BSD", Color.rgb(95, 195, 156), () -> sendVehicle("Blind spot detection", EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "TSR", Color.rgb(255, 171, 78), () -> sendVehicle("Traffic sign recognition", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Оповещение", Color.rgb(255, 128, 112), () -> sendVehicle("Traffic sign alert", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_ALERT, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Лимит", Color.rgb(177, 118, 255), () -> sendVehicle("Speed limit warning", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAccPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Cruise Control"));

        LinearLayout switches = Ui.row(this);
        addActionChip(switches, "ACC", () -> sendVehicle("ACC режим", EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ACC));
        addActionChip(switches, "ICC", () -> sendVehicle("ICC режим", EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ICC));
        addActionChip(switches, "TSR", () -> sendVehicle("ACC with TSR", EcarxVehicleAdapter.ADAS_ACC_WITH_TSR, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(switches, "Лимит", () -> sendVehicle("Speed limitation mode", EcarxVehicleAdapter.ADAS_SPEED_LIMITATION_MODE, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(switches, lpMatchWrap(0, 12, 0, 0));

        TextView gapLabel = Ui.text(this, "Дистанция ACC: " + currentAccGap, 18, true);
        gapLabel.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 4));
        panel.addView(gapLabel);
        SeekBar gap = new SeekBar(this);
        gap.setMax(4);
        gap.setProgress(Math.max(0, currentAccGap - 1));
        gap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gapLabel.setText("Дистанция ACC: " + (progress + 1));
                if (fromUser) {
                    currentAccGap = progress + 1;
                    sendVehicle("ACC gap " + (progress + 1), EcarxVehicleAdapter.ADAS_ACC_TIME_GAP, accGapValue(progress));
                    if (heroAccValue != null) heroAccValue.setText("ACC: gap " + (progress + 1));
                    if (topAccValue != null) topAccValue.setText("Gap " + (progress + 1));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        panel.addView(gap);

        TextView spacing = Ui.muted(this, "Расстояние между машинами отражает текущий gap.");
        spacing.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        panel.addView(spacing);
        panel.addView(new AccGapVisualView(this, gap.getProgress() + 1), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 96)));

        LinearLayout speedModes = Ui.row(this);
        addActionChip(speedModes, "60", () -> sendVehicle("Max cruising speed 60", EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 60));
        addActionChip(speedModes, "80", () -> sendVehicle("Max cruising speed 80", EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 80));
        addActionChip(speedModes, "100", () -> sendVehicle("Max cruising speed 100", EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 100));
        addActionChip(speedModes, "120", () -> sendVehicle("Max cruising speed 120", EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 120));
        panel.addView(speedModes, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildPdcPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(232, 18, 26, 44) : Color.argb(248, 239, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Parking Bridge"));
        panel.addView(Ui.muted(this, "Здесь остается только быстрый PDC bridge. Полный 360 / APA сценарий живет в Parking."));

        LinearLayout controls = Ui.row(this);
        addActionChip(controls, "PDC вкл", () -> sendVehicle("PDC включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(controls, "PDC выкл", () -> sendVehicle("PDC выключить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(controls, "Громкость сред.", () -> sendVehicle("PDC volume mid", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_MID));
        addActionChip(controls, "Парковка", this::openParkingActivity);
        panel.addView(controls, lpMatchWrap(0, 12, 0, 0));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Низкая", Ui.SUCCESS, () -> sendVehicle("PDC volume low", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_LOW));
        addTile(grid, "Средняя", Ui.WARNING, () -> sendVehicle("PDC volume mid", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_MID));
        addTile(grid, "Высокая", Ui.ERROR, () -> sendVehicle("PDC volume high", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_HIGH));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        panel.addView(new PdcVisualView(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 120)));
        return panel;
    }

    private LinearLayout buildModeSwitcher() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Drive Layers"));
        LinearLayout row = Ui.row(this);
        addModeChip(row, "Обзор", MODE_OVERVIEW);
        if (experimentalFeaturesEnabled()) addModeChip(row, "Эксперимент", MODE_EXPERIMENTAL);
        if (developerModeEnabled()) addModeChip(row, "Диагностика", MODE_DIAGNOSTICS);
        panel.addView(row, lpMatchWrap(0, 10, 0, 0));
        return panel;
    }

    private LinearLayout buildExperimentalPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 14, 21, 38) : Color.argb(246, 241, 245, 252),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.setVisibility(MODE_EXPERIMENTAL.equals(currentMode) ? View.VISIBLE : View.GONE);
        panel.addView(Ui.label(this, "Экспериментальный ADAS"));
        panel.addView(Ui.muted(this, "Панель доступна только при включенном experimental gate. Команды отправляются через новый UI, без возврата в legacy ADAS."));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "AI Assist", Color.rgb(94, 149, 255), () -> sendVehicle("AI driver assist", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Fusion Navi", Color.rgb(129, 149, 255), () -> sendVehicle("Fusion navigation", EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Pilot", Color.rgb(72, 184, 164), () -> sendVehicle("Drive Pilot", EcarxVehicleAdapter.ADAS_DRIVE_PILOT, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "ACC/LCC", Color.rgb(255, 179, 64), () -> sendVehicle("Drive Pilot ACC/LCC switch", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "APB", Color.rgb(255, 122, 89), () -> sendVehicle("APB switch", EcarxVehicleAdapter.ADAS_APB_SWITCH, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "TLB", Color.rgb(177, 118, 255), () -> sendVehicle("TLB switch", EcarxVehicleAdapter.ADAS_TLB_SWITCH, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout strategy = Ui.row(this);
        addActionChip(strategy, "LCA Gentle", () -> sendVehicle("AI lane strategy gentle", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_GENTLE));
        addActionChip(strategy, "Standard", () -> sendVehicle("AI lane strategy standard", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_STANDARD));
        addActionChip(strategy, "Radical", () -> sendVehicle("AI lane strategy radical", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_RADICAL));
        addActionChip(strategy, "Подтверждение", () -> sendVehicle("Lane-change confirm", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(strategy, lpMatchWrap(0, 12, 0, 0));

        LinearLayout warning = Ui.row(this);
        addActionChip(warning, "Голос", () -> sendVehicle("AI lane warning voice", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VOICE));
        addActionChip(warning, "Вибрация", () -> sendVehicle("AI lane warning vibrate", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VIBRATE));
        addActionChip(warning, "Оба", () -> sendVehicle("AI lane warning both", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_BOTH));
        addActionChip(warning, "Пресет", this::installHighwayAssistPreset);
        panel.addView(warning, lpMatchWrap(0, 12, 0, 0));

        panel.addView(buildReadableExperimentalAssistPanel(), lpMatchWrap(0, 16, 0, 0));
        return panel;
    }

    private LinearLayout buildReadableExperimentalAssistPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Pilot / Traffic Assist"));
        panel.addView(Ui.muted(this, "Здесь вынесены только те функции, для которых понятен сценарий взаимодействия: toggle, понятный selector или явный readback-only режим."));

        GridLayout toggles = new GridLayout(this);
        toggles.setColumnCount(2);
        addTile(toggles, "AI Assist Default", Color.rgb(93, 156, 255), () -> sendVehicle("AI Assist default on", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_ON));
        addTile(toggles, "Выход из обгона", Color.rgb(108, 194, 255), () -> sendVehicle("Overtaking lane exit assist", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_ON));
        addTile(toggles, "Светофор", Color.rgb(255, 179, 64), () -> sendVehicle("Traffic light attention", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.COMMON_ON));
        addTile(toggles, "Звук светофора", Color.rgb(255, 142, 98), () -> sendVehicle("Traffic light attention sound", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_ON));
        addTile(toggles, "Paddle assist", Color.rgb(156, 128, 255), () -> sendVehicle("Paddle lane change assist", EcarxVehicleAdapter.ADAS_PADDLE_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON));
        addTile(toggles, "Предупр. скорости", Color.rgb(103, 198, 157), () -> sendVehicle("Speed limit warning", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(toggles, lpMatchWrap(0, 12, 0, 0));

        LinearLayout togglesOff = Ui.row(this);
        addActionChip(togglesOff, "AI Default Off", () -> sendVehicle("AI Assist default off", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(togglesOff, "Выход выкл", () -> sendVehicle("Overtaking lane exit off", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(togglesOff, "Звук выкл", () -> sendVehicle("Traffic light sound off", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_OFF));
        addActionChip(togglesOff, "Paddle выкл", () -> sendVehicle("Paddle lane assist off", EcarxVehicleAdapter.ADAS_PADDLE_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(togglesOff, lpMatchWrap(0, 12, 0, 0));

        LinearLayout tlbModes = Ui.row(this);
        addActionChip(tlbModes, "TLB Low", () -> sendVehicle("Traffic light brake mode low", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.TLB_MODE_LOW));
        addActionChip(tlbModes, "TLB Mid", () -> sendVehicle("Traffic light brake mode middle", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.TLB_MODE_MIDDLE));
        addActionChip(tlbModes, "TLB High", () -> sendVehicle("Traffic light brake mode high", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.TLB_MODE_HIGH));
        addActionChip(tlbModes, "TLB Off", () -> sendVehicle("Traffic light brake mode off", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(tlbModes, lpMatchWrap(0, 12, 0, 0));

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        summary.addView(Ui.muted(this, "APB mode и speed warning mode оставлены как readback/raw controls: в текущем коде есть ID, но нет подтвержденных enum-значений, поэтому пользовательский selector для них пока не выдумывается."), lpMatchWrap(0, 0, 0, 8));
        summary.addView(diagnosticCard("Чтение режимов Pilot",
                EcarxVehicleAdapter.ADAS_APB_MODE,
                EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARNING_MODE,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND,
                EcarxVehicleAdapter.ADAS_PADDLE_LANE_CHANGE_ASSIST));
        panel.addView(summary);
        return panel;
    }

    private LinearLayout buildDiagnosticsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(238, 12, 18, 32) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.setVisibility(MODE_DIAGNOSTICS.equals(currentMode) ? View.VISIBLE : View.GONE);
        panel.addView(Ui.label(this, "Диагностика разработчика"));
        panel.addView(Ui.muted(this, "Support/readback, последние команды и сырой набор ID вынесены в новый экран ADAS вместо legacy ветки."));

        panel.addView(diagnosticCard("ADAS support/readback",
                EcarxVehicleAdapter.ADAS_AEB,
                EcarxVehicleAdapter.ADAS_FCW,
                EcarxVehicleAdapter.ADAS_LKA,
                EcarxVehicleAdapter.ADAS_LDW,
                EcarxVehicleAdapter.ADAS_RCW,
                EcarxVehicleAdapter.ADAS_ELKA,
                EcarxVehicleAdapter.ADAS_PDC,
                EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH));
        panel.addView(diagnosticCard("ACC / pilot readback",
                EcarxVehicleAdapter.ADAS_ACC_TIME_GAP,
                EcarxVehicleAdapter.ADAS_ACC_WITH_TSR,
                EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_NZP_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ALARM_INFO,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH));
        if (experimentalFeaturesEnabled()) {
            panel.addView(diagnosticCard("Experimental fault/readback",
                    EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE,
                    EcarxVehicleAdapter.ADAS_LANE_KEEPING_ASSISTANCE_FAILURE,
                    EcarxVehicleAdapter.ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE,
                    EcarxVehicleAdapter.ADAS_EMERGENCY_STEERING_FAILURE,
                    EcarxVehicleAdapter.ADAS_FORWARD_PRECOLLISION_FAULT,
                    EcarxVehicleAdapter.ADAS_FRONT_SIDE_ASSIST_FAILURE,
                    EcarxVehicleAdapter.ADAS_ADAPTIVE_CRUISE_FAILURE,
                    EcarxVehicleAdapter.ADAS_REAR_COLLISION_WARNING_FAILURE));
        }

        TextView last = Ui.text(this, "Последняя команда: " + lastCommandLabel, 15, true);
        last.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 4));
        panel.addView(last);
        TextView raw = Ui.muted(this, lastCommandRaw.isEmpty() ? "Raw ID: еще не отправлялось" : lastCommandRaw);
        panel.addView(raw);

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Экспорт", () -> exportDiagnostics(last.getText() + "\n" + raw.getText()));
        addActionChip(actions, "Парковка", this::openParkingActivity);
        panel.addView(actions, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Ассистенты", experimentalFeaturesEnabled() ? "AEB · FCW · LKA · AI" : "AEB · FCW · LKA · LDW", Ui.SUCCESS);
        addStatusCard(grid, "Круиз", "ACC / ICC / TSR", Ui.CYAN);
        addStatusCard(grid, "Парковка", "PDC здесь · AVM и APA в Parking", Ui.WARNING);
        addStatusCard(grid, "Полоса", developerModeEnabled() ? "Lane keep · readback · diagnostics" : "Lane keep / lane warning", Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(122, 255, 255, 255) : Color.argb(232, 255, 255, 255),
                Ui.dp(this, 26),
                Ui.glassLine(this)));
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 17, true);
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
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "AEB", () -> sendVehicle("AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON), true);
        addDockButton(dock, "LKA", () -> sendVehicle("LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "ACC", () -> sendVehicle("ACC режим", EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ACC), false);
        addDockButton(dock, "ICC", () -> sendVehicle("ICC режим", EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ICC), false);
        addDockButton(dock, "PDC", () -> sendVehicle("PDC включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "APA", this::openParkingActivity, false);
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
        Ui.press(tile, () -> {
            action.run();
            Ui.toast(this, label);
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        Ui.press(b, () -> {
            action.run();
            Ui.toast(this, label);
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
        Ui.press(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void sendVehicle(String label, int functionId, int value) {
        EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
        rememberCommand(label, functionId, value, result.success);
        Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
    }

    private void rememberCommand(String label, int functionId, int value, boolean success) {
        lastCommandLabel = label + (success ? " · ok" : " · error");
        lastCommandRaw = "0x" + Integer.toHexString(functionId) + " = 0x" + Integer.toHexString(value);
        refreshHeaderState();
    }

    private void refreshHeaderState() {
        if (topSafetyValue != null) topSafetyValue.setText(experimentalFeaturesEnabled() ? "База + EXP" : "Базовый набор");
        if (topAccValue != null) topAccValue.setText("Gap " + currentAccGap);
        if (topPdcValue != null) topPdcValue.setText("PDC + Parking");
        if (heroAebValue != null) heroAebValue.setText("Safety stack: AEB · FCW · RCW");
        if (heroFcwValue != null) heroFcwValue.setText("Lane stack: LKA · LDW · ELKA");
        if (heroLkaValue != null) heroLkaValue.setText("Cruise stack: ACC gap " + currentAccGap);
        if (heroPdcValue != null) heroPdcValue.setText("Parking bridge: PDC ready");
        if (heroAccValue != null) heroAccValue.setText("Mode: " + (MODE_OVERVIEW.equals(currentMode) ? "Everyday assist" : currentMode));
    }

    private void addModeChip(LinearLayout row, String label, String mode) {
        Button b = Ui.button(this, label);
        boolean active = mode.equals(currentMode);
        b.setTextColor(active ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(214, 255, 255, 255)),
                Ui.dp(this, 18),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
        Ui.press(b, () -> {
            currentMode = mode;
            recreate();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private LinearLayout diagnosticCard(String title, int... functionIds) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, title, 16, true));
        StringBuilder support = new StringBuilder();
        StringBuilder raw = new StringBuilder();
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        for (int functionId : functionIds) {
            EcarxVehicleAdapter.Result result = adapter.support(functionId);
            if (support.length() > 0) support.append('\n');
            support.append(result.message);
            if (raw.length() > 0) raw.append(", ");
            raw.append(EcarxVehicleAdapter.hex(functionId));
        }
        card.addView(Ui.muted(this, support.toString()));
        TextView rawIds = Ui.muted(this, "Raw IDs: " + raw);
        rawIds.setPadding(0, Ui.dp(this, 10), 0, 0);
        card.addView(rawIds);
        return card;
    }

    private void exportDiagnostics(CharSequence body) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "GFlow ADAS diagnostics");
        share.putExtra(Intent.EXTRA_TEXT, body.toString());
        startActivity(Intent.createChooser(share, "Экспорт diagnostics"));
    }

    private void openParkingActivity() {
        startActivity(new Intent(this, ParkingActivity.class));
    }

    private void installHighwayAssistPreset() {
        AutomationStore.savePreset(this, "", "Highway assist ready", highwayAssistReadyPreset());
        Ui.toast(this, "Пресет Highway assist ready сохранен");
    }

    private String highwayAssistReadyPreset() {
        return "# Experimental: prepares ADAS assist settings only; driver must confirm activation through stock controls.\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.ACC_ICC_ICC) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_TIME_GAP) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.ACC_TIME_GAP_2) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_WITH_TSR) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_LKA) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n";
    }

    private int accGapValue(int progress) {
        switch (progress) {
            case 0: return EcarxVehicleAdapter.ACC_TIME_GAP_0;
            case 1: return EcarxVehicleAdapter.ACC_TIME_GAP_1;
            case 2: return EcarxVehicleAdapter.ACC_TIME_GAP_2;
            case 3: return EcarxVehicleAdapter.ACC_TIME_GAP_3;
            default: return EcarxVehicleAdapter.ACC_TIME_GAP_3;
        }
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }

    private boolean experimentalFeaturesEnabled() {
        return prefs().getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private boolean developerModeEnabled() {
        return prefs().getBoolean(KEY_DEVELOPER_MODE, false);
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private static final class AccGapVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int gap;

        AccGapVisualView(Context context, int gap) {
            super(context);
            this.gap = gap;
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(40, 255, 255, 255));
            canvas.drawRoundRect(new RectF(0, h * 0.34f, w, h * 0.66f), Ui.dp(getContext(), 20), Ui.dp(getContext(), 20), paint);

            float leadX = w * 0.74f;
            float selfX = w * (0.18f + gap * 0.05f);
            paint.setColor(Color.argb(220, 235, 242, 248));
            canvas.drawRoundRect(new RectF(selfX - 34, h * 0.46f, selfX + 34, h * 0.70f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);
            paint.setColor(Color.argb(180, 129, 149, 255));
            canvas.drawRoundRect(new RectF(leadX - 34, h * 0.24f, leadX + 34, h * 0.48f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 3));
            paint.setColor(Color.argb(150, 77, 163, 255));
            canvas.drawLine(selfX + 40, h * 0.56f, leadX - 40, h * 0.36f, paint);
        }
    }

    private static final class PdcVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        PdcVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(220, 235, 242, 248));
            canvas.drawRoundRect(new RectF(w * 0.34f, h * 0.20f, w * 0.66f, h * 0.82f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 4));
            paint.setColor(Color.argb(140, 53, 208, 127));
            canvas.drawArc(new RectF(w * 0.18f, h * 0.12f, w * 0.82f, h * 0.58f), 200, 140, false, paint);
            paint.setColor(Color.argb(150, 255, 179, 64));
            canvas.drawArc(new RectF(w * 0.24f, h * 0.28f, w * 0.76f, h * 0.78f), 18, 144, false, paint);
            paint.setColor(Color.argb(160, 255, 77, 77));
            canvas.drawArc(new RectF(w * 0.28f, h * 0.42f, w * 0.72f, h * 0.92f), 20, 140, false, paint);
        }
    }

    private static final class AdasVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        AdasVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(36, 255, 255, 255));
            canvas.drawOval(new RectF(w * 0.18f, h * 0.74f, w * 0.82f, h * 0.94f), paint);

            Path body = new Path();
            body.moveTo(w * 0.50f, h * 0.10f);
            body.cubicTo(w * 0.68f, h * 0.18f, w * 0.78f, h * 0.42f, w * 0.76f, h * 0.78f);
            body.lineTo(w * 0.62f, h * 0.88f);
            body.lineTo(w * 0.38f, h * 0.88f);
            body.lineTo(w * 0.24f, h * 0.78f);
            body.cubicTo(w * 0.22f, h * 0.42f, w * 0.32f, h * 0.18f, w * 0.50f, h * 0.10f);
            paint.setColor(Color.argb(225, 235, 242, 248));
            canvas.drawPath(body, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 4));
            paint.setColor(Color.argb(140, 77, 163, 255));
            canvas.drawArc(new RectF(w * 0.14f, h * 0.18f, w * 0.86f, h * 0.62f), 200, 140, false, paint);
            canvas.drawArc(new RectF(w * 0.24f, h * 0.04f, w * 0.76f, h * 0.38f), 210, 120, false, paint);

            paint.setColor(Color.argb(150, 53, 208, 127));
            canvas.drawLine(w * 0.12f, h * 0.60f, w * 0.24f, h * 0.60f, paint);
            canvas.drawLine(w * 0.76f, h * 0.60f, w * 0.88f, h * 0.60f, paint);
            canvas.drawLine(w * 0.50f, h * 0.86f, w * 0.50f, h * 0.98f, paint);
        }
    }
}
