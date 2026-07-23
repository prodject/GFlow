package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ParkingActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final int PARK_MODE_PARALLEL = CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN;
    private static final int PARK_MODE_PERP = CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN;
    private static final int PARK_MODE_EXIT_LEFT = CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT;
    private static final int PARK_MODE_CANCEL = CarSignalManagerAdapter.PARK_MODE_CANCEL;

    private LinearLayout advancedHost;
    private TextView advancedToggleLabel;
    private boolean advancedVisible;
    private int selectedParkMode = PARK_MODE_PARALLEL;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildParkingShell());
        Ui.animateIn(getWindow().getDecorView());
    }

    private View buildParkingShell() {
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
        shell.addView(buildPrimaryParkingPanel(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildAssistPanel(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildAdvancedParkingPanel(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildStatusGrid(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateScaleIn(shell, 0);
        return scroll;
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> {
            Ui.press(v);
            finish();
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "Parking / APA"));
        TextView title = Ui.text(this, "Parking Control", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        TextView subtitle = Ui.muted(this, "Fast parking first. Radar, overlays and raw diagnostics move into secondary layers.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("AVM", "Ready"));
        bar.addView(buildTopStat("PDC", "Active"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
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
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Parking Overview"));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(buildHeroMetric("Auto Park", "Ready to search"));
        left.addView(buildHeroMetric("360 Camera", "Top + rear focus"));
        left.addView(buildHeroMetric("PDC", "Front / rear active"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.94f));

        ParkingVisualView visual = new ParkingVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 350), Ui.dp(this, 260));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Auto Park", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK));
        addActionChip(quick, "360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addActionChip(quick, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "Expert", this::toggleAdvancedParking);
        hero.addView(quick, lpMatchWrap(0, 16, 0, 0));
        return hero;
    }

    private View buildHeroMetric(String key, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.cardBg(this, Color.argb(58, 255, 255, 255), Ui.dp(this, 24), Color.argb(40, 255, 255, 255)));
        card.addView(Ui.label(this, key));
        TextView text = Ui.text(this, value, 17, true);
        text.setPadding(0, Ui.dp(this, 2), 0, 0);
        card.addView(text);
        return card;
    }

    private LinearLayout buildPrimaryParkingPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Primary Parking"));
        panel.addView(Ui.text(this, "На первом слое остаются только запуск парковки, обзор и выбор сценария. RCTA и raw PAS/AVM уходят ниже.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Auto Park", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK));
        addTile(grid, "360 View", Color.rgb(72, 153, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addTile(grid, "PDC On", Ui.SUCCESS, () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        panel.addView(grid, lpMatchWrap(0, 14, 0, 0));

        panel.addView(buildModeSelector(), lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildModeSelector() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(36, 255, 255, 255), Ui.dp(this, 26), Color.argb(28, 255, 255, 255)));
        panel.addView(Ui.label(this, "Parking Mode"));
        TextView body = Ui.muted(this, "Сегментированный выбор режима вместо четырёх одинаковых action-chip.");
        panel.addView(body, lpMatchWrap(0, 4, 0, 0));

        LinearLayout strip = Ui.row(this);
        strip.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
        strip.setBackground(Ui.cardBg(this, Color.argb(28, 255, 255, 255), Ui.dp(this, 24), Color.TRANSPARENT));
        addModeButton(strip, "Parallel", PARK_MODE_PARALLEL);
        addModeButton(strip, "Perp", PARK_MODE_PERP);
        addModeButton(strip, "Exit", PARK_MODE_EXIT_LEFT);
        addModeButton(strip, "Cancel", PARK_MODE_CANCEL);
        panel.addView(strip, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private void addModeButton(LinearLayout row, String label, int modeValue) {
        boolean active = selectedParkMode == modeValue;
        Button chip = Ui.button(this, label);
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(14);
        chip.setBackground(Ui.cardBg(this,
                active ? Color.argb(140, 77, 163, 255) : Color.argb(24, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(84, 131, 199, 255) : Color.TRANSPARENT));
        chip.setOnClickListener(v -> {
            Ui.press(v);
            selectedParkMode = modeValue;
            sendSignalParkMode(modeValue);
            recreate();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1f);
        lp.leftMargin = Ui.dp(this, 4);
        lp.rightMargin = Ui.dp(this, 4);
        row.addView(chip, lp);
    }

    private LinearLayout buildAssistPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(34, 255, 255, 255), Ui.dp(this, 26), Color.argb(26, 255, 255, 255)));
        panel.addView(Ui.label(this, "Assist Tools"));
        panel.addView(Ui.text(this, "Радары, overlay и APA-подтверждение остаются быстрыми, но не конкурируют с hero и mode-selection.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addSecondaryButton(grid, "Radars", () -> showQuickSheet("Radar Mode", new QuickItem[]{
                new QuickItem("Front + rear", () -> sendVehicle(EcarxVehicleAdapter.PAS_RADAR_WORK_MODE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE)),
                new QuickItem("Front only", () -> sendVehicle(EcarxVehicleAdapter.PAS_RADAR_WORK_MODE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_ACTIVE)),
                new QuickItem("Rear only", () -> sendVehicle(EcarxVehicleAdapter.PAS_RADAR_WORK_MODE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_REAR_ACTIVE))
        }));
        addSecondaryButton(grid, "Overlays", () -> showQuickSheet("Visual Assist", new QuickItem[]{
                new QuickItem("Trajectory", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Distance", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Top View", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_ON))
        }));
        addSecondaryButton(grid, "APA Confirm", () -> showQuickSheet("APA", new QuickItem[]{
                new QuickItem("Open Auto Park", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK)),
                new QuickItem("Confirm", () -> signalIfExperimental("setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER)),
                new QuickItem("Cancel", () -> signalIfExperimental("setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL))
        }));
        addSecondaryButton(grid, "RCTA", () -> showQuickSheet("RCTA", new QuickItem[]{
                new QuickItem("RCTA On", () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Graphics On", () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("Volume Mid", () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_WARNING_VOLUME, EcarxVehicleAdapter.PAS_RCTA_VOLUME_MID))
        }));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private void addSecondaryButton(GridLayout grid, String label, Runnable action) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setBackground(Ui.cardBg(this, Color.argb(44, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        button.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(button, lp);
    }

    private LinearLayout buildAdvancedParkingPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(36, 10, 14, 20), Ui.dp(this, 28), Color.argb(48, 255, 179, 64)));
        panel.addView(Ui.label(this, "Advanced Parking"));
        panel.addView(Ui.muted(this, experimentalFeaturesEnabled()
                ? "Raw APA/RPA, PAS/AVM and remote parking diagnostics stay hidden until explicitly opened."
                : "Experimental features are required for the deeper APA/RPA signal layer."));

        Button toggle = Ui.button(this, "Open advanced parking");
        toggle.setOnClickListener(v -> {
            Ui.press(v);
            toggleAdvancedParking();
        });
        panel.addView(toggle, lpMatchWrap(0, 12, 0, 0));

        advancedToggleLabel = Ui.muted(this, "Collapsed by default to keep the main parking flow readable.");
        panel.addView(advancedToggleLabel, lpMatchWrap(0, 8, 0, 0));

        advancedHost = new LinearLayout(this);
        advancedHost.setOrientation(LinearLayout.VERTICAL);
        advancedHost.setVisibility(View.GONE);
        panel.addView(advancedHost, lpMatchWrap(0, 14, 0, 0));
        renderAdvancedParking();
        return panel;
    }

    private void renderAdvancedParking() {
        if (advancedHost == null) return;
        advancedHost.removeAllViews();

        LinearLayout apa = Ui.glassCard(this);
        apa.addView(Ui.text(this, "APA / RPA", 18, true));
        apa.addView(Ui.muted(this, "Expert layer for raw parking controls, remote parking and HAL readback."));
        addCommand(apa, "Open OEM Auto Park UI", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(apa, "Open 360 Panorama", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addDiagnostic(apa, "Parking entry", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME);
        if (experimentalFeaturesEnabled()) {
            addSignalDiagnostic(apa, "APA / RPA status",
                    "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                    "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                    "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                    "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
            addSignalCommand(apa, "APA Button On", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
            addSignalCommand(apa, "APA Undo", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_UNDO);
            addSignalCommand(apa, "APA Cancel", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
            addSignalCommand(apa, "APA Manual", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
            addSignalCommand(apa, "APA Confirm", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
            addSignalCommand(apa, "PAS Button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_PAS);
            addSignalCommand(apa, "RPA Button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA);
            addSignalCommand(apa, "RPA Alt", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA_ALT);
            addSignalCommand(apa, "Parallel", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
            addSignalCommand(apa, "Perpendicular", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
            addSignalCommand(apa, "Exit Left", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
            addSignalCommand(apa, "Exit Right", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
            addSignalCommand(apa, "Cancel Mode", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
            addSignalCommand(apa, "Remote On", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_ON);
            addSignalCommand(apa, "Remote Off", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_OFF);
            addSignalCommand(apa, "Remote Self Search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
            addHalPropertyDiagnostic(apa, "Mobile RPA HAL",
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_REQ_RESP,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_ON_OFF1,
                    CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_UINT8,
                    CarSignalManagerAdapter.VEH_PUSH_APA_INFO_REQ);
        }
        advancedHost.addView(apa, lpMatchWrap(0, 0, 0, 16));

        LinearLayout avm = Ui.glassCard(this);
        avm.addView(Ui.text(this, "PAS / AVM", 18, true));
        avm.addView(Ui.muted(this, "Raw PAS / AVM diagnostics remain hidden behind the expert layer."));
        addDiagnostic(avm, "PAC / AVM state",
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STATUS,
                EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        addDiagnostic(avm, "Radar state",
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_REAR_CENTER);
        addDiagnostic(avm, "SAP / RCTA",
                EcarxVehicleAdapter.PAS_SAP_ACTIVATION,
                EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                EcarxVehicleAdapter.PAS_RCTA_ACTIVATION,
                EcarxVehicleAdapter.PAS_RCTA_LEFT_WARNING,
                EcarxVehicleAdapter.PAS_RCTA_RIGHT_WARNING);
        addPreset(avm, "Start AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addPreset(avm, "Stop AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF));
        addCommandGroup(avm, "Reverse camera", EcarxVehicleAdapter.PAS_PAC_AUTO_REVERSE_CAMERA,
                new String[]{"Off", "Rear", "Top"},
                new int[]{EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_OFF, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_REAR, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_TOP});
        addCommandGroup(avm, "Radar mode", EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                new String[]{"Off", "Standby", "Front + rear", "Front", "Rear"},
                new int[]{EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_OFF, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_STANDBY, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_REAR_ACTIVE});
        addCommandGroup(avm, "PAC 3D View", EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                new String[]{"3D", "Rear left 3D", "Rear right 3D"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_LEFT_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_RIGHT_3D});
        addCommandGroup(avm, "PAC 3D Position", EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                new String[]{"Off", "Front center", "Front left", "Front right", "Left", "Right", "Rear center", "Rear left", "Rear right"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_3D_POS_OFF, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_RIGHT});
        addCommand(avm, "Trajectory On", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Trajectory Off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Distance Overlay On", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Distance Overlay Off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Towbar Overlay On", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Towbar Overlay Off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Transparent Car On", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Transparent Car Off", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Top View On", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Top View Off", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Touring On", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Touring Off", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "PAS Graphics On", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "PAS Graphics Off", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "PAS Mute On", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "PAS Mute Off", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(avm, "SAP Mode", EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                new String[]{"Park In", "Park Out"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_IN, EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_OUT});
        addCommandGroup(avm, "SAP Park In Type", EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                new String[]{"Perpendicular", "Parallel"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PERP, EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PARA});
        addCommand(avm, "RCTA On", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "RCTA Off", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "RCTA Graphics On", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "RCTA Graphics Off", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(avm, "RCTA Volume", EcarxVehicleAdapter.PAS_RCTA_WARNING_VOLUME,
                new String[]{"Off", "Low", "Mid", "High"},
                new int[]{EcarxVehicleAdapter.PAS_RCTA_VOLUME_OFF, EcarxVehicleAdapter.PAS_RCTA_VOLUME_LOW, EcarxVehicleAdapter.PAS_RCTA_VOLUME_MID, EcarxVehicleAdapter.PAS_RCTA_VOLUME_HIGH});
        advancedHost.addView(avm, lpMatchWrap(0, 0, 0, 0));
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "AVM / PAC", "Top view and 3D ready", Ui.CYAN);
        addStatusCard(grid, "Parking Modes", "Parallel, perpendicular, exit", Ui.SUCCESS);
        addStatusCard(grid, "PDC / Radar", "Front and rear sensors active", Ui.WARNING);
        addStatusCard(grid, "RCTA / SAP", "Secondary safety tools", Color.rgb(129, 149, 255));
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
        addDockButton(dock, "Auto Park", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK), true);
        addDockButton(dock, "360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360), false);
        addDockButton(dock, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "Assist", () -> showQuickSheet("Assist", new QuickItem[]{
                new QuickItem("Radars", () -> sendVehicle(EcarxVehicleAdapter.PAS_RADAR_WORK_MODE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE)),
                new QuickItem("Overlays", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON)),
                new QuickItem("RCTA", () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON))
        }), false);
        addDockButton(dock, "Expert", this::toggleAdvancedParking, false);
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

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void sendVehicle(int functionId, int value) {
        EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
        Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
    }

    private void sendSignalParkMode(int mode) {
        CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this)
                .set("setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, mode);
        Ui.toast(this, result.success ? "Режим парковки отправлен" : "Ошибка режима парковки");
    }

    private void toggleAdvancedParking() {
        if (advancedHost == null) return;
        advancedVisible = !advancedVisible;
        advancedHost.setVisibility(advancedVisible ? View.VISIBLE : View.GONE);
        if (advancedToggleLabel != null) {
            advancedToggleLabel.setText(advancedVisible
                    ? "Expert layer opened: raw APA/RPA, PAS/AVM, SAP/RCTA and remote parking diagnostics."
                    : "Collapsed by default to keep the main parking flow readable.");
        }
        if (advancedVisible) Ui.animateIn(advancedHost, 0, 12f);
    }

    private void showQuickSheet(String title, QuickItem[] items) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        sheet.addView(Ui.label(this, "Parking Quick Actions"));
        sheet.addView(Ui.text(this, title, 22, true));
        for (QuickItem item : items) {
            Button button = Ui.button(this, item.label);
            button.setOnClickListener(v -> {
                dialog.dismiss();
                Ui.press(v);
                item.action.run();
            });
            sheet.addView(button, lpMatchWrap(0, 8, 0, 0));
        }
        dialog.setContentView(sheet);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
        Ui.animateScaleIn(sheet, 0);
        dialog.show();
    }

    private void signalIfExperimental(String methodName, int signalId, int value) {
        if (!experimentalFeaturesEnabled()) {
            Ui.toast(this, "Включите Experimental features");
            return;
        }
        CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
        Ui.toast(this, result.success ? "Сигнал отправлен" : "Ошибка сигнала");
    }

    private static final class QuickItem {
        final String label;
        final Runnable action;

        QuickItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            Ui.press(v);
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addCommandGroup(LinearLayout root, String title, int functionId, String[] labels, int[] values) {
        for (int i = 0; i < labels.length && i < values.length; i++) addCommand(root, title + ": " + labels[i], functionId, values[i]);
    }

    private void addPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            Ui.press(v);
            boolean ok = true;
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (EcarxVehicleAdapter.Command command : commands) {
                EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, command.functionId, command.value);
                ok &= result.success;
                sb.append(result.message).append("\n");
            }
            Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        Button b = Ui.button(this, "Диагностика: " + label);
        b.setOnClickListener(v -> {
            Ui.press(v);
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

    private void addSignalCommand(LinearLayout root, String label, String methodName, int signalId, int value) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            Ui.press(v);
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Сигнал отправлен" : "Ошибка сигнала");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        Button b = Ui.button(this, "Диагностика сигналов: " + label);
        b.setOnClickListener(v -> {
            Ui.press(v);
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int i = 0; i + 1 < methodSignalPairs.length; i += 2) {
                sb.append(adapter.get(String.valueOf(methodSignalPairs[i]), (Integer) methodSignalPairs[i + 1]).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addHalPropertyDiagnostic(LinearLayout root, String label, int... propertyIds) {
        Button b = Ui.button(this, "HAL-диагностика: " + label);
        b.setOnClickListener(v -> {
            Ui.press(v);
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int propertyId : propertyIds) sb.append(adapter.rawHalProperty(propertyId, "VehiclePropertyVEH2").message).append("\n");
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private boolean experimentalFeaturesEnabled() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private static final class ParkingVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ParkingVisualView(Context context) {
            super(context);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();

            paint.setShader(new LinearGradient(0f, 0f, 0f, h,
                    Color.argb(0, 0, 0, 0),
                    Color.argb(110, 4, 8, 16),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(w * 0.05f, h * 0.06f, w * 0.95f, h * 0.96f), Ui.dp(getContext(), 30), Ui.dp(getContext(), 30), paint);

            paint.setShader(new RadialGradient(w * 0.52f, h * 0.78f, w * 0.36f,
                    Color.argb(125, 77, 163, 255),
                    Color.argb(0, 77, 163, 255),
                    Shader.TileMode.CLAMP));
            canvas.drawOval(new RectF(w * 0.18f, h * 0.72f, w * 0.86f, h * 0.98f), paint);

            paint.setShader(null);
            paint.setColor(Color.argb(224, 235, 242, 248));
            canvas.drawRoundRect(new RectF(w * 0.32f, h * 0.14f, w * 0.68f, h * 0.84f), Ui.dp(getContext(), 30), Ui.dp(getContext(), 30), paint);

            paint.setColor(Color.argb(118, 20, 34, 52));
            canvas.drawRoundRect(new RectF(w * 0.40f, h * 0.22f, w * 0.60f, h * 0.38f), Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 5));
            paint.setColor(Color.argb(148, 72, 153, 255));
            canvas.drawArc(new RectF(w * 0.08f, h * 0.22f, w * 0.92f, h * 0.92f), 178, 184, false, paint);
            paint.setColor(Color.argb(136, 255, 179, 64));
            canvas.drawArc(new RectF(w * 0.14f, h * 0.10f, w * 0.86f, h * 0.58f), 196, 148, false, paint);
            paint.setColor(Color.argb(132, 53, 208, 127));
            canvas.drawLine(w * 0.12f, h * 0.52f, w * 0.26f, h * 0.52f, paint);
            canvas.drawLine(w * 0.74f, h * 0.52f, w * 0.88f, h * 0.52f, paint);
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawRoundRect(new RectF(w * 0.22f, h * 0.16f, w * 0.28f, h * 0.86f), Ui.dp(getContext(), 8), Ui.dp(getContext(), 8), paint);
            canvas.drawRoundRect(new RectF(w * 0.72f, h * 0.16f, w * 0.78f, h * 0.86f), Ui.dp(getContext(), 8), Ui.dp(getContext(), 8), paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
