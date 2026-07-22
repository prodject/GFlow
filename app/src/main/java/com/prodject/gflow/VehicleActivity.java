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
import android.widget.TextView;

public class VehicleActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildVehicleShell());
        Ui.animateIn(getWindow().getDecorView());
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
        root.addView(buildBodyControls(), lpMatchWrap(0, 0, 0, 16));
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
        titleBlock.addView(Ui.label(this, "Body / Access"));
        TextView title = Ui.text(this, "Автомобиль", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Двери", "Закрыты"));
        bar.addView(buildTopStat("Окна", "Норма"));
        bar.addView(buildTopStat("Режим", "Comfort"));
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
        hero.addView(Ui.label(this, "Vehicle Visual"));

        LinearLayout top = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Статус кузова", "Комфортный доступ активен"));
        left.addView(metricLine("Замки", "Все двери заперты"));
        left.addView(metricLine("Люк", "Закрыт"));
        left.addView(metricLine("Свет", "Auto"));
        top.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        VehicleBodyView visual = new VehicleBodyView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 320), Ui.dp(this, 250));
        visualLp.leftMargin = Ui.dp(this, 12);
        top.addView(visual, visualLp);
        hero.addView(top);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Замки", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "Окна", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN));
        addActionChip(quick, "Люк", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "Багажник", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK));
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
        addTile(grid, "Зеркала", Color.rgb(94, 201, 196), () -> sendVehicle(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout driveModes = Ui.row(this);
        addActionChip(driveModes, "Eco", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO));
        addActionChip(driveModes, "Comfort", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT));
        addActionChip(driveModes, "Sport", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SPORT));
        addActionChip(driveModes, "Snow", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW));
        panel.addView(driveModes, lpMatchWrap(0, 14, 0, 0));

        LinearLayout lights = Ui.row(this);
        addActionChip(lights, "Ближний", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(lights, "Аварийка", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(lights, "DRL", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(lights, "Grille", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(lights, lpMatchWrap(0, 12, 0, 0));
        return panel;
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

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Кузов", "Двери, окна, люк и шторка доступны", Ui.SUCCESS);
        addStatusCard(grid, "Свет", "DRL · Atmosphere · Grille", Ui.CYAN);
        addStatusCard(grid, "Режим движения", "Eco / Comfort / Sport / Snow", Ui.WARNING);
        addStatusCard(grid, "Зеркала и руль", "Fold / heat / adjust", Color.rgb(129, 149, 255));
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
        addDockButton(dock, "Замки", () -> sendVehicle(EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON), true);
        addDockButton(dock, "Окна", () -> sendVehicle(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN), false);
        addDockButton(dock, "Люк", () -> sendVehicle(EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Свет", () -> sendVehicle(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Drive", () -> sendVehicle(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT), false);
        return dock;
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
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#080A0F"), Color.parseColor("#0D1420"), Color.parseColor("#101B2A")});
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
