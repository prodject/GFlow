package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HudActivity extends Activity {
    private LinearLayout advancedHost;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildHudShell());
        Ui.animateIn(getWindow().getDecorView());
    }

    private View buildHudShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildControlPanel(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildAdvancedPanel(), lpMatchWrap(0, 0, 0, 16));
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
        Ui.bindPress(back, this::finish);
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "HUD / Cluster / OneOS"));
        TextView title = Ui.text(this, "HUD / Cluster / OneOS", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        TextView subtitle = Ui.muted(this, "Projection first. DIM, bridge and services stay secondary.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("HUD", "Ready"));
        bar.addView(buildTopStat("DIM", "Connected"));
        bar.addView(buildTopStat("Media", "Bridge"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
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
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Projection Overview"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("HUD", "Navigation / media / safety"));
        left.addView(metricLine("DIM", "Day / night / control center"));
        left.addView(metricLine("Cluster", "Bridge standby"));
        left.addView(metricLine("AudioExt", "Media / VR / PDC"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        HudVisualView visual = new HudVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "HUD вкл", () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "Навигация", () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "DIM Night", this::requestDimNight);
        addActionChip(quick, "Расширенно", this::openAdvancedHud);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildControlPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Projection Controls"));
        panel.addView(Ui.text(this, "Everyday HUD and DIM controls stay in the primary layer.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "HUD вкл", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "HUD выкл", Ui.ERROR, () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF));
        addTile(grid, "Калибровка", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Media", Color.rgb(119, 83, 132), () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Навигация", Color.rgb(72, 153, 255), () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Safety", Ui.SUCCESS, () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout dim = Ui.row(this);
        addActionChip(dim, "DIM Night", this::requestDimNight);
        addActionChip(dim, "Презентация", () -> setDimPresentation(true));
        addActionChip(dim, "Вкладка Music", () -> dimTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addActionChip(dim, "Control center", () -> dimTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        panel.addView(dim, lpMatchWrap(0, 14, 0, 0));

        LinearLayout services = Ui.row(this);
        addActionChip(services, "HUD SVC", () -> startForegroundService(new Intent(this, HudPresentationService.class)));
        addActionChip(services, "Observer", () -> startForegroundService(new Intent(this, HudObserverService.class)));
        addActionChip(services, "Cluster", () -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
        addActionChip(services, "Расширенно", this::openAdvancedHud);
        panel.addView(services, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAdvancedPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Ui.tertiarySurface(this), Ui.dp(this, 28), Color.argb(48, 255, 179, 64)));
        panel.addView(Ui.label(this, "Advanced Projection"));
        panel.addView(Ui.muted(this, "Полный набор OneOS / DIM / AudioExt / Cluster перенесен в новый HUD-экран без legacy fallback."));
        advancedHost = new LinearLayout(this);
        advancedHost.setOrientation(LinearLayout.VERTICAL);
        panel.addView(advancedHost, lpMatchWrap(0, 12, 0, 0));
        renderAdvancedPanel();
        return panel;
    }

    private void renderAdvancedPanel() {
        if (advancedHost == null) return;
        advancedHost.removeAllViews();

        LinearLayout hud = Ui.glassCard(this);
        hud.addView(Ui.text(this, "HUD / DIM Bridge", 18, true));
        hud.addView(Ui.muted(this, safeHudDimAvailability()));
        hud.addView(Ui.muted(this, "Вид навигации на приборке определяет, как маршрут отображается на DIM: выключено, упрощенно, полно, AR или 3D-полосы."));
        addCommand(hud, "HUD включить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD выключить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(hud, "HUD калибровка", EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD сброс угла", EcarxVehicleAdapter.HUD_ANGLE_RESET, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD snow mode", EcarxVehicleAdapter.HUD_SNOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD safety on", EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD media on", EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD navi on", EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD phone on", EcarxVehicleAdapter.HUD_DISPLAY_BTPHONE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(hud, "HUD drive env on", EcarxVehicleAdapter.HUD_DISPLAY_DRIVE_ENVIRONMENT, EcarxVehicleAdapter.COMMON_ON);
        addDiagnostic(hud, "HUD", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.HUD_DISPLAY_SAFETY);
        addHudDimAction(hud, "HUDInteraction: статус", a -> a.hudStatus());
        addHudDimAction(hud, "HUDInteraction: height/sync", a -> a.hudSync());
        addHudDimAction(hud, "DIMInteraction: статус", a -> a.dimStatus());
        addHudDimAction(hud, "DIM: запрос day/night", EcarxHudDimAdapter::requestDayNightMode);
        addHudDimAction(hud, "DIM: presentation on", a -> a.setPresentation(true));
        addHudDimAction(hud, "DIM: presentation off", a -> a.setPresentation(false));
        addHudDimAction(hud, "DIM Menu: IHU ready/theme", EcarxHudDimAdapter::dimMenuReadyAndTheme);
        addHudDimAction(hud, "DIM Menu: вкладка навигации", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_NAVIGATION));
        addHudDimAction(hud, "DIM Menu: вкладка музыки", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addHudDimAction(hud, "DIM Menu: control center", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        addHudDimAction(hud, "Навигация на приборке: выкл", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_OFF));
        addHudDimAction(hud, "Навигация на приборке: упрощенно", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_SIMPLIFY));
        addHudDimAction(hud, "Навигация на приборке: полно", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_FULL));
        addHudDimAction(hud, "Навигация на приборке: AR", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_AR));
        addHudDimAction(hud, "Навигация на приборке: 3D-полосы", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_3D_LANE));
        addHudDimAction(hud, "DIM громкость 10", a -> a.setDimVolume(false, 10));
        addHudDimAction(hud, "DIM климат: Celsius", EcarxHudDimAdapter::climateCelsiusUnit);
        addHudDimAction(hud, "DIM климат: 22.0C", a -> a.climateTemp(22.0f));
        addHudDimAction(hud, "DIM средний расход: пример", a -> a.updateAvgFuelRanking(0, "{\"source\":\"GFlow\",\"avg\":0}"));
        addHudDimAction(hud, "DIM медиа: mute", a -> a.publishMediaMuteState(1));
        addHudDimAction(hud, "DIM медиа: unmute", a -> a.publishMediaMuteState(0));
        advancedHost.addView(hud, lpMatchWrap(0, 0, 0, 16));

        LinearLayout audio = Ui.glassCard(this);
        audio.addView(Ui.text(this, "AudioExt / Сервисы", 18, true));
        audio.addView(Ui.muted(this, "Media bridge, VR, громкость PDC, anti-shake, loudness и сервисные действия внутри нового UI."));
        addAudioExtAction(audio, "AudioExt: bind services", AudioExtServiceAdapter::bindAudioExt);
        addAudioExtAction(audio, "AudioExt: visualizer status", AudioExtServiceAdapter::visualizerStatus);
        addAudioExtAction(audio, "AudioExt: media playing", a -> a.notifyMediaStatus(1, getPackageName()));
        addAudioExtAction(audio, "AudioExt: media paused", a -> a.notifyMediaStatus(0, getPackageName()));
        addAudioExtAction(audio, "AudioExt: VR active", a -> a.notifyVrStatus(1, 0));
        addAudioExtAction(audio, "AudioExt: VR inactive", a -> a.notifyVrStatus(0, 0));
        addAudioExtAction(audio, "AudioExt: PDC volume on", a -> a.notifyPdcVolumeSwitch(1));
        addAudioExtAction(audio, "AudioExt: voice light 0.8", a -> a.voiceLight(0.8f));
        addAudioExtAction(audio, "AudioExt: anti-shake on", a -> a.antiShake(true, 0.5f));
        addAudioExtAction(audio, "AudioExt: loudness on", a -> a.loudness(true));
        addAudioExtAction(audio, "AudioExt: section max on", a -> a.useSectionMax(true));
        addAudioExtAction(audio, "AudioExt: voice base -35dB", a -> a.voiceDb(-35));
        addAudioExtAction(audio, "AudioExt: spectrum preset", a -> a.spectrumPreset(0, 1, 1.0f, 1.0f));
        addServiceAction(audio, "Запустить HUD service", () -> startForegroundService(new Intent(this, HudPresentationService.class)));
        addServiceAction(audio, "Запустить HUD observer", () -> startForegroundService(new Intent(this, HudObserverService.class)));
        addServiceAction(audio, "Запустить Cluster bridge", () -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
        advancedHost.addView(audio, lpMatchWrap(0, 0, 0, 0));
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "HUD", "Media · Navi · Safety · Phone", Ui.CYAN);
        addStatusCard(grid, "DIM", "Tabs · Day/night · Volume", Ui.SUCCESS);
        addStatusCard(grid, "AudioExt", "Visualizer · VR · PDC volume", Ui.WARNING);
        addStatusCard(grid, "Services", "Presentation · Observer · Cluster", Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.secondaryCard(this);
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 13, false);
        v.setTextColor(Ui.secondaryText(this));
        v.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(v);
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 4));
        accentLp.topMargin = Ui.dp(this, 10);
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
        addDockButton(dock, "HUD", () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON), true);
        addDockButton(dock, "Media", () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Navi", () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "DIM", this::requestDimNight, false);
        addDockButton(dock, "Cluster", () -> startForegroundService(new Intent(this, ClusterBridgeService.class)), false);
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
        Ui.bindPress(tile, () -> {
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
        Ui.bindPress(b, () -> {
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
        Ui.bindPress(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void sendVehicle(int functionId, int value) {
        EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
        Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
    }

    private void requestDimNight() {
        EcarxHudDimAdapter.Result result = new EcarxHudDimAdapter(this).requestDayNightMode();
        Ui.toast(this, result.success ? "DIM команда отправлена" : "DIM команда не выполнена");
    }

    private void setDimPresentation(boolean enabled) {
        EcarxHudDimAdapter.Result result = new EcarxHudDimAdapter(this).setPresentation(enabled);
        Ui.toast(this, result.success ? "Presentation обновлен" : "Presentation ошибка");
    }

    private void dimTab(int tab) {
        EcarxHudDimAdapter.Result result = new EcarxHudDimAdapter(this).dimMenuTab(tab);
        Ui.toast(this, result.success ? "DIM tab отправлен" : "DIM tab ошибка");
    }

    private void openAdvancedHud() {
        if (advancedHost != null) advancedHost.requestFocus();
        Ui.toast(this, "Открыт advanced HUD flow");
    }

    private String safeHudDimAvailability() {
        try {
            return new EcarxHudDimAdapter(this).availability();
        } catch (Exception e) {
            return "OneOS HUD/DIM\nНедоступно: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = Ui.button(this, label);
        Ui.bindPress(b, () -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        Button b = Ui.button(this, "Диагностика: " + label);
        Ui.bindPress(b, () -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int functionId : functionIds) {
                sb.append(adapter.support(functionId).message).append("\n");
                sb.append(adapter.get(functionId).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addHudDimAction(LinearLayout root, String label, HudDimAction action) {
        Button b = Ui.button(this, label);
        Ui.bindPress(b, () -> {
            EcarxHudDimAdapter.Result result;
            try {
                result = action.run(new EcarxHudDimAdapter(this));
            } catch (Exception e) {
                result = EcarxHudDimAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "OneOS команда отправлена" : "OneOS команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    interface HudDimAction {
        EcarxHudDimAdapter.Result run(EcarxHudDimAdapter adapter);
    }

    private void addAudioExtAction(LinearLayout root, String label, AudioExtAction action) {
        Button b = Ui.button(this, label);
        Ui.bindPress(b, () -> {
            AudioExtServiceAdapter.Result result;
            try {
                result = action.run(new AudioExtServiceAdapter(this));
            } catch (Exception e) {
                result = AudioExtServiceAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "AudioExt команда отправлена" : "AudioExt команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    interface AudioExtAction {
        AudioExtServiceAdapter.Result run(AudioExtServiceAdapter adapter);
    }

    private void addServiceAction(LinearLayout root, String label, Runnable action) {
        Button b = Ui.button(this, label);
        Ui.bindPress(b, () -> {
            action.run();
            Ui.toast(this, label);
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }

    private static final class HudVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        HudVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(26, 255, 255, 255));
            canvas.drawRoundRect(new RectF(w * 0.06f, h * 0.12f, w * 0.94f, h * 0.90f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), paint);

            paint.setColor(Color.argb(180, 72, 153, 255));
            canvas.drawRoundRect(new RectF(w * 0.14f, h * 0.26f, w * 0.86f, h * 0.40f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);
            paint.setColor(Color.argb(180, 53, 208, 127));
            canvas.drawRoundRect(new RectF(w * 0.20f, h * 0.48f, w * 0.46f, h * 0.72f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);
            paint.setColor(Color.argb(180, 119, 83, 132));
            canvas.drawRoundRect(new RectF(w * 0.54f, h * 0.48f, w * 0.80f, h * 0.72f), Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), paint);
        }
    }
}
