package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
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

public class ParkingActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private LinearLayout advancedHost;

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

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        root.addView(buildParkingModes(), lpMatchWrap(0, 0, 0, 16));
        root.addView(buildAdvancedParkingPanel(), lpMatchWrap(0, 0, 0, 16));
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
        titleBlock.addView(Ui.label(this, "Parking / APA"));
        TextView title = Ui.text(this, "Парковка / APA", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("AVM", "Standby"));
        bar.addView(buildTopStat("PDC", "Active"));
        bar.addView(buildTopStat("RCTA", "Ready"));
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
        hero.addView(Ui.label(this, "Parking Visual"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Auto Park", "Готов к запуску"));
        left.addView(metricLine("360 Camera", "Rear + Top"));
        left.addView(metricLine("PDC", "Front / Rear"));
        left.addView(metricLine("RCTA", "Мониторинг сзади"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ParkingVisualView visual = new ParkingVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Auto Park", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK));
        addActionChip(quick, "360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addActionChip(quick, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        addActionChip(quick, "APA/RPA", this::scrollAdvancedIntoView);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout buildParkingModes() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Parking Controls"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Open Auto Park", Ui.CYAN, () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK));
        addTile(grid, "Open 360", Color.rgb(72, 153, 255), () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addTile(grid, "PDC On", Ui.SUCCESS, () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "PDC Off", Ui.ERROR, () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_OFF));
        addTile(grid, "RCTA On", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "RCTA Off", Color.rgb(128, 140, 156), () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout modes = Ui.row(this);
        addActionChip(modes, "Parallel", () -> sendSignalParkMode(CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN));
        addActionChip(modes, "Perp", () -> sendSignalParkMode(CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN));
        addActionChip(modes, "Out", () -> sendSignalParkMode(CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT));
        addActionChip(modes, "Cancel", () -> sendSignalParkMode(CarSignalManagerAdapter.PARK_MODE_CANCEL));
        panel.addView(modes, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildAdvancedParkingPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Advanced Parking"));
        panel.addView(Ui.muted(this, experimentalFeaturesEnabled()
                ? "Полный перенос experimental parking: APA/RPA, PAS/AVM, SAP/RCTA и remote parking теперь доступны в новом экране."
                : "Включите Experimental features в настройках, чтобы открыть raw APA/RPA, PAS/AVM и remote parking diagnostics."));

        advancedHost = new LinearLayout(this);
        advancedHost.setOrientation(LinearLayout.VERTICAL);
        panel.addView(advancedHost, lpMatchWrap(0, 12, 0, 0));
        renderAdvancedParking();
        return panel;
    }

    private void renderAdvancedParking() {
        if (advancedHost == null) return;
        advancedHost.removeAllViews();

        LinearLayout apa = Ui.glassCard(this);
        apa.addView(Ui.text(this, "APA / RPA", 18, true));
        apa.addView(Ui.muted(this, "Штатный вход в автопарковку, raw park modes, remote parking и HAL readback."));
        addCommand(apa, "Открыть штатный Auto Park UI", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(apa, "Открыть 360 panorama", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addDiagnostic(apa, "BCM parking entry", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME);
        if (experimentalFeaturesEnabled()) {
            addSignalDiagnostic(apa, "APA/RPA status",
                    "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                    "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                    "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                    "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
            addSignalCommand(apa, "APA on button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
            addSignalCommand(apa, "APA undo", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_UNDO);
            addSignalCommand(apa, "APA cancel", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
            addSignalCommand(apa, "APA manual", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
            addSignalCommand(apa, "APA confirm enter", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
            addSignalCommand(apa, "PAS button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_PAS);
            addSignalCommand(apa, "RPA button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA);
            addSignalCommand(apa, "RPA button alt", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA_ALT);
            addSignalCommand(apa, "Parking mode horizontal in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
            addSignalCommand(apa, "Parking mode perpendicular in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
            addSignalCommand(apa, "Parking mode left out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
            addSignalCommand(apa, "Parking mode right out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
            addSignalCommand(apa, "Parking mode cancel", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
            addSignalCommand(apa, "Remote parking enable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_ON);
            addSignalCommand(apa, "Remote parking disable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_OFF);
            addSignalCommand(apa, "Remote parking self-search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
            addHalPropertyDiagnostic(apa, "Mobile RPA HAL properties",
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
        avm.addView(Ui.muted(this, "AVM/PAC camera state, radar work modes, overlays, SAP и RCTA внутри нового parking flow."));
        addDiagnostic(avm, "PAC / AVM camera state",
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STATUS,
                EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        addDiagnostic(avm, "PAS radar state",
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_REAR_CENTER);
        addDiagnostic(avm, "SAP / RCTA readback",
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
        addCommandGroup(avm, "Auto reverse camera", EcarxVehicleAdapter.PAS_PAC_AUTO_REVERSE_CAMERA,
                new String[]{"Reverse off", "Reverse rear", "Reverse top"},
                new int[]{EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_OFF, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_REAR, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_TOP});
        addCommandGroup(avm, "Radar work mode", EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                new String[]{"Radar off", "Radar standby", "Radar front+rear", "Radar front", "Radar rear"},
                new int[]{EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_OFF, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_STANDBY, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_REAR_ACTIVE});
        addCommandGroup(avm, "PAC 3D view", EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                new String[]{"3D surround", "Rear left 3D", "Rear right 3D"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_LEFT_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_RIGHT_3D});
        addCommandGroup(avm, "PAC 3D position", EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                new String[]{"3D off", "Front center", "Front left", "Front right", "Left", "Right", "Rear center", "Rear left", "Rear right"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_3D_POS_OFF, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_RIGHT});
        addCommand(avm, "Guide path on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Guide path off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Distance overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Distance overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Towbar overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Towbar overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Transparent model on", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Transparent model off", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Top view on", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Top view off", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "Touring view on", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "Touring view off", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "PAS graphics on", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "PAS graphics off", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "PAS mute on", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "PAS mute off", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(avm, "SAP parking", EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                new String[]{"SAP park in", "SAP park out"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_IN, EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_OUT});
        addCommandGroup(avm, "SAP park-in type", EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                new String[]{"SAP perpendicular", "SAP parallel"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PERP, EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PARA});
        addCommand(avm, "RCTA on", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "RCTA off", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(avm, "RCTA graphics on", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(avm, "RCTA graphics off", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(avm, "RCTA warning volume", EcarxVehicleAdapter.PAS_RCTA_WARNING_VOLUME,
                new String[]{"RCTA off", "RCTA low", "RCTA mid", "RCTA high"},
                new int[]{EcarxVehicleAdapter.PAS_RCTA_VOLUME_OFF, EcarxVehicleAdapter.PAS_RCTA_VOLUME_LOW, EcarxVehicleAdapter.PAS_RCTA_VOLUME_MID, EcarxVehicleAdapter.PAS_RCTA_VOLUME_HIGH});
        advancedHost.addView(avm, lpMatchWrap(0, 0, 0, 0));
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "AVM / PAC", "Top view · Rear view · 3D ready", Ui.CYAN);
        addStatusCard(grid, "Park Modes", "Parallel / Perpendicular / Exit", Ui.SUCCESS);
        addStatusCard(grid, "PDC / Radar", "Front/rear sensors active", Ui.WARNING);
        addStatusCard(grid, "RCTA / SAP", "Rear cross traffic / smart assist", Color.rgb(129, 149, 255));
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
        addDockButton(dock, "Auto Park", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK), true);
        addDockButton(dock, "360", () -> sendVehicle(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360), false);
        addDockButton(dock, "PDC", () -> sendVehicle(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "RCTA", () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON), false);
        addDockButton(dock, "EXP", this::scrollAdvancedIntoView, false);
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

    private void sendSignalParkMode(int mode) {
        CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this)
                .set("setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, mode);
        Ui.toast(this, result.success ? "Parking mode отправлен" : "Parking mode ошибка");
    }

    private void scrollAdvancedIntoView() {
        if (advancedHost != null) advancedHost.requestFocus();
        Ui.toast(this, experimentalFeaturesEnabled() ? "Открыт advanced parking flow" : "Для полного набора включите Experimental features");
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
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
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Signal отправлен" : "Signal ошибка");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        Button b = Ui.button(this, "Signal diag: " + label);
        b.setOnClickListener(v -> {
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
        Button b = Ui.button(this, "HAL diag: " + label);
        b.setOnClickListener(v -> {
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
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(32, 255, 255, 255));
            canvas.drawOval(new RectF(w * 0.16f, h * 0.74f, w * 0.84f, h * 0.94f), paint);
            paint.setColor(Color.argb(220, 235, 242, 248));
            canvas.drawRoundRect(new RectF(w * 0.30f, h * 0.14f, w * 0.70f, h * 0.84f), Ui.dp(getContext(), 28), Ui.dp(getContext(), 28), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 5));
            paint.setColor(Color.argb(130, 72, 153, 255));
            canvas.drawArc(new RectF(w * 0.06f, h * 0.20f, w * 0.94f, h * 0.92f), 180, 180, false, paint);
            paint.setColor(Color.argb(130, 255, 179, 64));
            canvas.drawArc(new RectF(w * 0.16f, h * 0.08f, w * 0.84f, h * 0.56f), 200, 140, false, paint);
            paint.setColor(Color.argb(130, 53, 208, 127));
            canvas.drawLine(w * 0.14f, h * 0.50f, w * 0.24f, h * 0.50f, paint);
            canvas.drawLine(w * 0.76f, h * 0.50f, w * 0.86f, h * 0.50f, paint);
        }
    }
}
