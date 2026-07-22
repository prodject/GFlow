package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
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

public class AdasActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildAdasShell());
        Ui.animateIn(getWindow().getDecorView());
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
        titleBlock.addView(Ui.label(this, "Driver Assistance"));
        TextView title = Ui.text(this, "ADAS / Вождение", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Безопасность", "Активна"));
        bar.addView(buildTopStat("ACC", "Standby"));
        bar.addView(buildTopStat("PDC", "Готов"));
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
        hero.addView(Ui.label(this, "Drive Visual"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("AEB", "Вкл"));
        left.addView(metricLine("FCW", "Вкл"));
        left.addView(metricLine("LKA", "Вкл"));
        left.addView(metricLine("PDC", "Готов"));
        left.addView(metricLine("ACC", "Gap 3"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        AdasVisualView visual = new AdasVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "AEB", () -> sendVehicle(EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "LKA", () -> sendVehicle(EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "ACC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ACC));
        addActionChip(quick, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildSafetyControls() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Safety Systems"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "AEB", Color.rgb(113, 91, 177), () -> sendVehicle(EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "FCW", Color.rgb(95, 133, 255), () -> sendVehicle(EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "LKA", Color.rgb(72, 184, 164), () -> sendVehicle(EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "LDW", Color.rgb(255, 179, 64), () -> sendVehicle(EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "RCW", Color.rgb(255, 122, 89), () -> sendVehicle(EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "ELKA", Color.rgb(129, 149, 255), () -> sendVehicle(EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAccPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "ACC / ICC"));

        LinearLayout switches = Ui.row(this);
        addActionChip(switches, "ACC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ACC));
        addActionChip(switches, "ICC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ICC));
        addActionChip(switches, "TSR", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_WITH_TSR, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(switches, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(switches, lpMatchWrap(0, 12, 0, 0));

        TextView gapLabel = Ui.text(this, "Дистанция ACC: 3", 18, true);
        gapLabel.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 4));
        panel.addView(gapLabel);
        SeekBar gap = new SeekBar(this);
        gap.setMax(3);
        gap.setProgress(2);
        gap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gapLabel.setText("Дистанция ACC: " + (progress + 1));
                if (fromUser) sendVehicle(EcarxVehicleAdapter.ADAS_ACC_TIME_GAP, progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        panel.addView(gap);

        LinearLayout speedModes = Ui.row(this);
        addActionChip(speedModes, "60", () -> sendVehicle(EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 60));
        addActionChip(speedModes, "80", () -> sendVehicle(EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 80));
        addActionChip(speedModes, "100", () -> sendVehicle(EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 100));
        addActionChip(speedModes, "120", () -> sendVehicle(EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED, 120));
        panel.addView(speedModes, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Ассистенты", "AEB · FCW · LKA · LDW", Ui.SUCCESS);
        addStatusCard(grid, "Круиз", "ACC / ICC / TSR", Ui.CYAN);
        addStatusCard(grid, "Парковка", "PDC active · AVM via parking", Ui.WARNING);
        addStatusCard(grid, "Полоса", "Lane keep / lane warning", Color.rgb(129, 149, 255));
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
        addDockButton(dock, "AEB", () -> sendVehicle(EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON), true);
        addDockButton(dock, "LKA", () -> sendVehicle(EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "ACC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ACC), false);
        addDockButton(dock, "ICC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH, EcarxVehicleAdapter.ACC_ICC_ICC), false);
        addDockButton(dock, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON), false);
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

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
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
