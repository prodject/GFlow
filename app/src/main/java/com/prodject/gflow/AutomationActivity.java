package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;

public class AutomationActivity extends Activity {
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = AutomationEngine.prefs(this);
        setContentView(buildAutomationShell());
        Ui.animateIn(getWindow().getDecorView());
    }

    private View buildAutomationShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildTemplatePanel(), lpMatchWrap(0, 0, 0, 16));
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
        titleBlock.addView(Ui.label(this, "Automation Engine"));
        TextView title = Ui.text(this, "Автоматизация", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Presets", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_PRESET_ORDER).size())));
        bar.addView(buildTopStat("Scenarios", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_SCENARIO_ORDER).size())));
        bar.addView(buildTopStat("Triggers", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_TRIGGER_ORDER).size())));
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
        hero.addView(Ui.label(this, "Automation Overview"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Smart presets", "HVAC, авто, HUD, media"));
        left.addView(metricLine("Scenarios", "Триггеры, условия, шаги"));
        left.addView(metricLine("Rules", "Low-speed camera / parking"));
        left.addView(metricLine("Profiles", "Контекст и действия"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        AutomationVisualView visual = new AutomationVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Журнал", () -> openLegacy("log"));
        addActionChip(quick, "Новый preset", () -> openLegacy("preset"));
        addActionChip(quick, "Новый сценарий", () -> openLegacy("scenario"));
        addActionChip(quick, "Новый trigger", () -> openLegacy("trigger"));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildTemplatePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Templates & Actions"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Winter/Summer", Ui.CYAN, () -> openLegacy("install_climate"));
        addTile(grid, "Welcome/Leave", Ui.SUCCESS, () -> openLegacy("install_welcome"));
        addTile(grid, "Parking Guard", Ui.WARNING, () -> openLegacy("install_parking"));
        addTile(grid, "Rain", Color.rgb(73, 130, 83), () -> openLegacy("install_rain"));
        addTile(grid, "Night Mode", Color.rgb(88, 105, 130), () -> openLegacy("install_night"));
        addTile(grid, "Navigation", Color.rgb(58, 106, 156), () -> openLegacy("install_navigation"));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout lists = Ui.row(this);
        addActionChip(lists, "Presets", () -> openLegacy("presets"));
        addActionChip(lists, "Scenarios", () -> openLegacy("scenarios"));
        addActionChip(lists, "Triggers", () -> openLegacy("triggers"));
        addActionChip(lists, "Low-speed cam", () -> openLegacy("low_speed"));
        panel.addView(lists, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Smart presets", previewNames(AutomationEngine.KEY_PRESET_ORDER), Ui.CYAN);
        addStatusCard(grid, "Scenarios", previewNames(AutomationEngine.KEY_SCENARIO_ORDER), Ui.SUCCESS);
        addStatusCard(grid, "Triggers", previewNames(AutomationEngine.KEY_TRIGGER_ORDER), Ui.WARNING);
        addStatusCard(grid, "Execution log", shortLogPreview(), Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 16, true);
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
        addDockButton(dock, "Preset", () -> openLegacy("preset"), true);
        addDockButton(dock, "Scenario", () -> openLegacy("scenario"), false);
        addDockButton(dock, "Trigger", () -> openLegacy("trigger"), false);
        addDockButton(dock, "Log", () -> openLegacy("log"), false);
        addDockButton(dock, "Run", () -> openLegacy("run"), false);
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

    private String previewNames(String key) {
        List<String> names = AutomationEngine.names(prefs, key);
        if (names.isEmpty()) return "Пусто";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, names.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(" · ");
            sb.append(names.get(i));
        }
        if (names.size() > limit) sb.append(" +").append(names.size() - limit);
        return sb.toString();
    }

    private String shortLogPreview() {
        String log = AutomationEngine.scenarioLog(this);
        if (log == null || log.trim().isEmpty()) return "Журнал пуст";
        String[] lines = log.split("\n");
        return lines.length == 0 ? "Журнал пуст" : lines[0];
    }

    private void openLegacy(String target) {
        Ui.toast(this, "Редакторы и глубокая automation-логика пока оставлены в legacy fallback");
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

    private static final class AutomationVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        AutomationVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(28, 255, 255, 255));
            canvas.drawRoundRect(new RectF(w * 0.08f, h * 0.12f, w * 0.92f, h * 0.90f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), paint);

            paint.setColor(Color.argb(180, 77, 163, 255));
            canvas.drawCircle(w * 0.26f, h * 0.34f, Ui.dp(getContext(), 18), paint);
            paint.setColor(Color.argb(180, 53, 208, 127));
            canvas.drawCircle(w * 0.74f, h * 0.34f, Ui.dp(getContext(), 18), paint);
            paint.setColor(Color.argb(180, 255, 179, 64));
            canvas.drawCircle(w * 0.26f, h * 0.72f, Ui.dp(getContext(), 18), paint);
            paint.setColor(Color.argb(180, 72, 153, 255));
            canvas.drawCircle(w * 0.74f, h * 0.72f, Ui.dp(getContext(), 18), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 4));
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawLine(w * 0.26f, h * 0.34f, w * 0.74f, h * 0.34f, paint);
            canvas.drawLine(w * 0.26f, h * 0.34f, w * 0.26f, h * 0.72f, paint);
            canvas.drawLine(w * 0.74f, h * 0.34f, w * 0.74f, h * 0.72f, paint);
            canvas.drawLine(w * 0.26f, h * 0.72f, w * 0.74f, h * 0.72f, paint);
        }
    }
}
