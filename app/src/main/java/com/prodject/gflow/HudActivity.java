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

public class HudActivity extends Activity {
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
        root.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
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
        titleBlock.addView(Ui.label(this, "HUD / Cluster / OneOS"));
        TextView title = Ui.text(this, "HUD / Cluster / OneOS", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
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
        hero.addView(Ui.label(this, "Projection Visual"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("HUD", "Навигация / медиа / безопасность"));
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
        addActionChip(quick, "HUD On", () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "Навигация", () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "DIM Night", this::requestDimNight);
        addActionChip(quick, "Service", this::openLegacyHud);
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

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "HUD On", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "HUD Off", Ui.ERROR, () -> sendVehicle(EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF));
        addTile(grid, "Calibration", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Media", Color.rgb(119, 83, 132), () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Navigation", Color.rgb(72, 153, 255), () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "Safety", Ui.SUCCESS, () -> sendVehicle(EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout dim = Ui.row(this);
        addActionChip(dim, "DIM Night", this::requestDimNight);
        addActionChip(dim, "Presentation", () -> setDimPresentation(true));
        addActionChip(dim, "Music Tab", () -> dimTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addActionChip(dim, "Control", () -> dimTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        panel.addView(dim, lpMatchWrap(0, 14, 0, 0));

        LinearLayout services = Ui.row(this);
        addActionChip(services, "HUD SVC", () -> startForegroundService(new Intent(this, HudPresentationService.class)));
        addActionChip(services, "Observer", () -> startForegroundService(new Intent(this, HudObserverService.class)));
        addActionChip(services, "Cluster", () -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
        addActionChip(services, "Legacy", this::openLegacyHud);
        panel.addView(services, lpMatchWrap(0, 12, 0, 0));
        return panel;
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
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 18, true);
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
        tile.setOnClickListener(v -> {
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
        b.setOnClickListener(v -> {
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
        button.setOnClickListener(v -> action.run());
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

    private void openLegacyHud() {
        Ui.toast(this, "Raw HUD/DIM/AudioExt команды пока оставлены в legacy fallback");
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
