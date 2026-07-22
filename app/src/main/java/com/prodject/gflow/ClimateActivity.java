package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public class ClimateActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildClimateShell());
        Ui.animateIn(getWindow().getDecorView());
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
        root.addView(buildClimateComfortPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildClimateMainPanel(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildClimateBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private LinearLayout buildClimateTopBar() {
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
        titleBlock.addView(Ui.label(this, "HVAC / Comfort"));
        TextView title = Ui.text(this, "Климат", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Режим", "Auto"));
        bar.addView(buildTopStat("Синхр.", "Dual"));
        bar.addView(buildTopStat("Салон", "22°C"));
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

    private LinearLayout buildClimateComfortPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Comfort Panel"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        addClimateToggle(grid, "HVAC", Ui.CYAN, () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Auto", Ui.SUCCESS, () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C", Ui.CYAN, () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "A/C Max", Ui.WARNING, () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Eco", Color.rgb(69, 186, 134), () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON));
        addClimateToggle(grid, "Sync", Color.rgb(103, 147, 255), () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL));
        addClimateToggle(grid, "Split", Color.rgb(134, 103, 255), () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE));
        addClimateToggle(grid, "°C / °F", Color.rgb(255, 122, 89), () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_C));
        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridLp.topMargin = Ui.dp(this, 12);
        panel.addView(grid, gridLp);
        return panel;
    }

    private void addClimateToggle(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14));
        tile.setBackground(Ui.cardBg(this, Color.argb(84, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 20), Color.argb(90, 255, 255, 255)));
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

    private LinearLayout buildClimateMainPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22));
        panel.addView(Ui.label(this, "Driver / Passenger"));

        LinearLayout tempRow = Ui.row(this);
        tempRow.setGravity(Gravity.CENTER_VERTICAL);
        addClimateTempCard(tempRow, "Водитель", 22.0f, EcarxVehicleAdapter.ZONE_DRIVER_LEFT);

        LinearLayout center = Ui.glassCard(this);
        center.addView(Ui.label(this, "Airflow Visual"));
        center.addView(new AirFlowView(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 220)));
        TextView fanLabel = Ui.text(this, "Вентилятор: 3", 18, true);
        fanLabel.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 4));
        center.addView(fanLabel);
        SeekBar fan = new SeekBar(this);
        fan.setMax(8);
        fan.setProgress(2);
        fan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fanLabel.setText("Вентилятор: " + (progress + 1));
                if (fromUser) new EcarxVehicleAdapter(ClimateActivity.this).set(EcarxVehicleAdapter.HVAC_FAN_SPEED, progress + 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        center.addView(fan);
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f);
        centerLp.leftMargin = Ui.dp(this, 12);
        centerLp.rightMargin = Ui.dp(this, 12);
        tempRow.addView(center, centerLp);

        addClimateTempCard(tempRow, "Пассажир", 22.0f, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        panel.addView(tempRow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout seats = Ui.row(this);
        seats.setWeightSum(4f);
        addClimateActionChip(seats, "Seat Heat", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addClimateActionChip(seats, "Seat Vent", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addClimateActionChip(seats, "Wheel Heat", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        addClimateActionChip(seats, "Defrost", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON));
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

    private void addClimateTempCard(LinearLayout row, String label, float value, int zone) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, label));
        TextView temp = Ui.text(this, String.format(Locale.US, "%.1f°C", value), 34, true);
        temp.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
        card.addView(temp);
        SeekBar seek = new SeekBar(this);
        seek.setMax(32);
        seek.setProgress(Math.round((value - 16f) * 2f));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float t = 16f + progress / 2f;
                temp.setText(String.format(Locale.US, "%.1f°C", t));
                if (fromUser) new EcarxVehicleAdapter(ClimateActivity.this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, t);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        card.addView(seek);
        row.addView(card, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));
    }

    private void addClimateActionChip(LinearLayout row, String label, Runnable action) {
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

    private LinearLayout buildClimateBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Auto", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON), true);
        addDockButton(dock, "A/C", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Defrost", () -> new EcarxVehicleAdapter(this).set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Smart", this::finish, false);
        addDockButton(dock, "Расширенно", this::finish, false);
        return dock;
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

    private void applyClimatePreset(EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        boolean ok = true;
        for (EcarxVehicleAdapter.Result result : results) ok &= result.success;
        Ui.toast(this, ok ? "Пресет применен" : "Пресет выполнен частично");
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private android.graphics.drawable.GradientDrawable dashboardBg() {
        return new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#080A0F"), Color.parseColor("#0D1420"), Color.parseColor("#101B2A")});
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
