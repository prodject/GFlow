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
import java.util.ArrayList;
import java.util.List;

public class ParkingActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final int PARK_MODE_PARALLEL = CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN;
    private static final int PARK_MODE_PERP = CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN;
    private static final int PARK_MODE_EXIT_LEFT = CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT;
    private static final int PARK_MODE_CANCEL = CarSignalManagerAdapter.PARK_MODE_CANCEL;

    private LinearLayout advancedHost;
    private TextView advancedToggleHint;
    private boolean advancedVisible;
    private int selectedParkMode = PARK_MODE_PARALLEL;
    private final List<Button> modeButtons = new ArrayList<>();
    private final List<TextView> liveStatusValues = new ArrayList<>();

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

        shell.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 84)));
        shell.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        shell.addView(buildParkingModes(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildAssistShortcuts(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildAdvancedParkingPanel(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildRadarAndVisualPanel(), lpMatchWrap(0, 0, 0, 16));
        shell.addView(buildApaControlPanel(), lpMatchWrap(0, 0, 0, 16));
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
        TextView subtitle = Ui.muted(this, "Быстрый запуск парковки наверху, raw PAS и APA ниже без потери функций.");
        subtitle.setTextSize(13);
        titleBlock.addView(subtitle);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("AVM", "Готово"));
        bar.addView(buildTopStat("PDC", "Активно"));
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
        left.addView(buildHeroMetric("Auto Park", "Готов к поиску места"));
        left.addView(buildHeroMetric("360 Camera", "Верхний и задний обзор"));
        left.addView(buildHeroMetric("PDC", "Передние и задние датчики"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.94f));

        ParkingVisualView visual = new ParkingVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 350), Ui.dp(this, 260));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Auto Park", this::openAutoParkUi);
        addActionChip(quick, "360", this::openAvmCamera);
        addActionChip(quick, "PDC status", this::showPdcStatus);
        addActionChip(quick, "Advanced", this::toggleAdvancedParking);
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
        LinearLayout.LayoutParams lp = lpMatchWrap(0, 0, 0, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildParkingModes() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Primary Parking"));
        panel.addView(Ui.muted(this, "Сверху оставлены только самые частые действия, а подробные PAS / APA блоки идут ниже."));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTile(grid, "Открыть Auto Park", Ui.CYAN, this::openAutoParkUi);
        addTile(grid, "Открыть 360", Color.rgb(72, 153, 255), this::openAvmCamera);
        addTile(grid, "PDC status", Ui.SUCCESS, this::showPdcStatus);
        addTile(grid, "RCTA Вкл", Ui.WARNING, () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON));
        addTile(grid, "RCTA Выкл", Color.rgb(128, 140, 156), () -> sendVehicle(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        panel.addView(buildModeSelector(), lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildModeSelector() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        panel.setBackground(Ui.cardBg(this, Color.argb(36, 255, 255, 255), Ui.dp(this, 26), Color.argb(28, 255, 255, 255)));
        panel.addView(Ui.label(this, "Parking Mode"));
        panel.addView(Ui.muted(this, "Визуально выделенный выбор сценария вместо четырех одинаковых action-chip."));

        LinearLayout strip = Ui.row(this);
        strip.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
        strip.setBackground(Ui.cardBg(this, Color.argb(28, 255, 255, 255), Ui.dp(this, 24), Color.TRANSPARENT));
        addModeButton(strip, "Параллельная", PARK_MODE_PARALLEL);
        addModeButton(strip, "Перпен.", PARK_MODE_PERP);
        addModeButton(strip, "Выезд", PARK_MODE_EXIT_LEFT);
        addModeButton(strip, "Отмена", PARK_MODE_CANCEL);
        panel.addView(strip, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildAssistShortcuts() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Assist Tools"));
        panel.addView(Ui.muted(this, "Быстрые действия для типового сценария, а полный набор команд остается в секциях ниже."));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addShortcutButton(grid, "PDC / PAC старт", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_ON));
        addShortcutButton(grid, "Направляющие", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON));
        addShortcutButton(grid, "Top View", this::openAvmCamera);
        addShortcutButton(grid, "APA Confirm", () -> {
            if (!experimentalFeaturesEnabled()) {
                Ui.toast(this, "Для APA Confirm включите Experimental features");
                return;
            }
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this)
                    .set("setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
            Ui.toast(this, result.success ? "Сигнал отправлен" : "Ошибка сигнала");
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildRadarAndVisualPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Радары / Визуальная помощь"));
        panel.addView(Ui.muted(this, "log_1.32 подтвердил AVM через HAL/EVS и PDC-кандидат PAS_PAC_ACTIVATION. Radar work mode оставлен только как readback."));
        addActionButton(panel, "Открыть AVM 360", this::openAvmCamera);
        addActionButton(panel, "Активировать PDC/PAC", () -> sendVehicle(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_ON));
        addDiagnostic(panel, "Статус радаров и оверлеев",
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH,
                EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO,
                EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR,
                EcarxVehicleAdapter.PAS_SHOW_GRAPHICS,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        return panel;
    }

    private LinearLayout buildApaControlPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Управление APA"));
        panel.addView(Ui.muted(this, experimentalFeaturesEnabled()
                ? "Штатный вход в Auto Park, выбор сценария парковки, подтверждение, отмена и self-search доступны прямо в основном экране."
                : "Штатный запуск Auto Park и выбор сценария доступны сразу. Дополнительные кнопки подтверждения, отмены и self-search открываются в Experimental."));

        addActionButton(panel, "Открыть штатный Auto Park", this::openAutoParkUi);
        addSignalDiagnostic(panel, "Статус APA",
                "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
        addSignalCommand(panel, "APA: Параллельная парковка", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
        addSignalCommand(panel, "APA: Перпендикулярная парковка", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
        addSignalCommand(panel, "APA: Выезд влево", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
        addSignalCommand(panel, "APA: Выезд вправо", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
        addSignalCommand(panel, "APA: Отмена сценария", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
        if (experimentalFeaturesEnabled()) {
            addSignalCommand(panel, "APA: Включить", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
            addSignalCommand(panel, "APA: Подтвердить / Enter", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
            addSignalCommand(panel, "APA: Отмена", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
            addSignalCommand(panel, "APA: Ручной режим", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
            addSignalCommand(panel, "RPA: Self-search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
        }
        return panel;
    }

    private LinearLayout buildAdvancedParkingPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Advanced Parking"));
        panel.addView(Ui.muted(this, experimentalFeaturesEnabled()
                ? "Полный raw-набор APA/RPA, PAS/AVM и remote parking доступен ниже по кнопке."
                : "Включите Experimental features в настройках, чтобы открыть raw APA/RPA, PAS/AVM и remote parking diagnostics."));

        Button toggle = Ui.button(this, "Открыть advanced parking");
        toggle.setOnClickListener(v -> {
            Ui.press(v);
            toggleAdvancedParking();
        });
        panel.addView(toggle, lpMatchWrap(0, 12, 0, 0));

        advancedToggleHint = Ui.muted(this, "Блок свернут по умолчанию, чтобы основной сценарий парковки оставался читаемым.");
        panel.addView(advancedToggleHint, lpMatchWrap(0, 8, 0, 0));

        advancedHost = new LinearLayout(this);
        advancedHost.setOrientation(LinearLayout.VERTICAL);
        advancedHost.setVisibility(View.GONE);
        panel.addView(advancedHost, lpMatchWrap(0, 12, 0, 0));
        renderAdvancedParking();
        return panel;
    }

    private void renderAdvancedParking() {
        if (advancedHost == null) return;
        advancedHost.removeAllViews();

        LinearLayout apa = Ui.glassCard(this);
        apa.addView(Ui.text(this, "APA / RPA", 18, true));
        apa.addView(Ui.muted(this, "Raw parking controls, remote parking и HAL readback. Основные APA-сценарии вынесены выше в отдельный блок."));
        addActionButton(apa, "Открыть штатный Auto Park UI", this::openAutoParkUi);
        addActionButton(apa, "Открыть 360-панораму", this::openAvmCamera);
        addDiagnostic(apa, "Вход в parking через BCM", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME);
        addDiagnostic(apa, "GInputBridge PAS inventory",
                EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_BTN_PUSH,
                EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_PARK_MOD,
                EcarxVehicleAdapter.PAS_AUT_PRKG_SLOT_NR_REQ,
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STEER_LINK,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_VOLUME);
        if (experimentalFeaturesEnabled()) {
            addSignalDiagnostic(apa, "Статус APA/RPA",
                    "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                    "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                    "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                    "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
            addSignalCommand(apa, "APA: Кнопка включения", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
            addSignalCommand(apa, "APA: Undo", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_UNDO);
            addSignalCommand(apa, "APA: Отмена", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
            addSignalCommand(apa, "APA: Ручной режим", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
            addSignalCommand(apa, "APA: Подтвердить / Enter", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
            addSignalCommand(apa, "PAS: Кнопка", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_PAS);
            addSignalCommand(apa, "RPA: Кнопка", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA);
            addSignalCommand(apa, "RPA: Альтернативная кнопка", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA_ALT);
            addSignalCommand(apa, "Parking mode: Параллельная", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
            addSignalCommand(apa, "Parking mode: Перпендикулярная", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
            addSignalCommand(apa, "Parking mode: Выезд влево", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
            addSignalCommand(apa, "Parking mode: Выезд вправо", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
            addSignalCommand(apa, "Parking mode: Отмена", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
            addSignalCommand(apa, "Remote parking: Вкл", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_ON);
            addSignalCommand(apa, "Remote parking: Выкл", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_OFF);
            addSignalCommand(apa, "Remote parking self-search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
            addHalPropertyDiagnostic(apa, "HAL-свойства mobile RPA",
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
        avm.addView(Ui.muted(this, "Raw PAS / AVM diagnostics. Direct PAS write выключен до появления подтвержденных успешных setFunctionValue для этой прошивки."));
        addDiagnostic(avm, "Состояние камер PAC / AVM",
                EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_BTN_PUSH,
                EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_PARK_MOD,
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STATUS,
                EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        addDiagnostic(avm, "Состояние радаров PAS",
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_REAR_CENTER);
        addDiagnostic(avm, "Чтение SAP / RCTA",
                EcarxVehicleAdapter.PAS_SAP_ACTIVATION,
                EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                EcarxVehicleAdapter.PAS_RCTA_ACTIVATION,
                EcarxVehicleAdapter.PAS_RCTA_LEFT_WARNING,
                EcarxVehicleAdapter.PAS_RCTA_RIGHT_WARNING);
        addActionButton(avm, "Открыть AVM 360 через EVS", this::openAvmCamera);
        advancedHost.addView(avm, lpMatchWrap(0, 0, 0, 0));
    }

    private GridLayout buildStatusGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        liveStatusValues.clear();
        addStatusCard(grid, "AVM / PAC", avmStatusSummary(), Ui.CYAN);
        addStatusCard(grid, "APA / RPA", apaStatusSummary(), Ui.SUCCESS);
        addStatusCard(grid, "PDC / Radar", pdcStatusSummary(), Ui.WARNING);
        addStatusCard(grid, "RCTA / SAP", rctaStatusSummary(), Color.rgb(129, 149, 255));
        return grid;
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        TextView v = Ui.text(this, value, 18, true);
        v.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(v);
        liveStatusValues.add(v);
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
        addDockButton(dock, "Auto Park", this::openAutoParkUi, true);
        addDockButton(dock, "360", this::openAvmCamera, false);
        addDockButton(dock, "PDC", this::showPdcStatus, false);
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
        b.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
        b.setOnClickListener(v -> {
            Ui.press(v);
            action.run();
            Ui.toast(this, label);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addModeButton(LinearLayout row, String label, int modeValue) {
        Button button = Ui.button(this, label);
        modeButtons.add(button);
        styleModeButton(button, modeValue == selectedParkMode);
        button.setOnClickListener(v -> {
            Ui.press(v);
            selectedParkMode = modeValue;
            syncModeButtons();
            sendSignalParkMode(modeValue);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1f);
        lp.leftMargin = Ui.dp(this, 4);
        lp.rightMargin = Ui.dp(this, 4);
        row.addView(button, lp);
    }

    private void addShortcutButton(GridLayout grid, String label, Runnable action) {
        Button button = Ui.button(this, label);
        button.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(52, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
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

    private void styleModeButton(Button button, boolean active) {
        button.setTextColor(active || Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(34, 255, 255, 255) : Color.argb(238, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(96, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214))));
    }

    private void syncModeButtons() {
        if (modeButtons.size() < 4) return;
        styleModeButton(modeButtons.get(0), selectedParkMode == PARK_MODE_PARALLEL);
        styleModeButton(modeButtons.get(1), selectedParkMode == PARK_MODE_PERP);
        styleModeButton(modeButtons.get(2), selectedParkMode == PARK_MODE_EXIT_LEFT);
        styleModeButton(modeButtons.get(3), selectedParkMode == PARK_MODE_CANCEL);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(active || Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(238, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214))));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void sendVehicle(int functionId, int value) {
        EcarxVehicleAdapter.Result result = executeVehicleCommand(functionId, value);
        Ui.toast(this, result.success ? "Команда отправлена" : result.message);
        refreshStatusCards();
    }

    private void openAutoParkUi() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result result;
        if (!adapter.isWritable(EcarxVehicleAdapter.BCM_CUSTOM_KEY)) {
            result = EcarxVehicleAdapter.Result.external(
                    "BCM custom key direct path отключен: auto park UI не подтвержден на этой прошивке",
                    false,
                    true);
        } else {
            result = CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        }
        Ui.toast(this, result.success ? "Команда отправлена" : result.message);
        refreshStatusCards();
    }

    private void openAvmCamera() {
        AvmHalAdapter.Result hal = new AvmHalAdapter(this).open360();
        if (hal.success) {
            Ui.toast(this, "AVM 360 открыт через HAL");
            refreshStatusCards();
            return;
        }
        EcarxDvrAdapter.Result result = new EcarxDvrAdapter(this).openEvs(EcarxDvrAdapter.EVS_CAMERA_AVM);
        Ui.toast(this, result.success ? "AVM 360 открыт через EVS fallback" : hal.message + "\n" + result.message);
        refreshStatusCards();
    }

    private void showPdcStatus() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result pdcSupport = adapter.support(EcarxVehicleAdapter.ADAS_PDC);
        EcarxVehicleAdapter.Result pdcValue = adapter.get(EcarxVehicleAdapter.ADAS_PDC);
        EcarxVehicleAdapter.Result pacSupport = adapter.support(EcarxVehicleAdapter.PAS_PAC_ACTIVATION);
        EcarxVehicleAdapter.Result pacValue = adapter.get(EcarxVehicleAdapter.PAS_PAC_ACTIVATION);
        Ui.dialog(this, "PDC / PAC", "write candidate: PAS_PAC_ACTIVATION = 1\nADAS_PDC support: " + pdcSupport.message + "\nADAS_PDC readback: " + pdcValue.message + "\nPAC support: " + pacSupport.message + "\nPAC readback: " + pacValue.message);
    }

    private void sendSignalParkMode(int mode) {
        CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this)
                .set("setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, mode);
        Ui.toast(this, result.success ? "Режим парковки отправлен" : "Ошибка режима парковки");
        refreshStatusCards();
    }

    private void scrollAdvancedIntoView() {
        ensureAdvancedVisible();
        if (advancedHost != null) advancedHost.requestFocus();
        Ui.toast(this, experimentalFeaturesEnabled() ? "Открыт расширенный parking-блок" : "Для полного набора включите Experimental features");
    }

    private void toggleAdvancedParking() {
        advancedVisible = !advancedVisible;
        updateAdvancedVisibility();
        if (advancedVisible && advancedHost != null) advancedHost.requestFocus();
    }

    private void ensureAdvancedVisible() {
        if (!advancedVisible) {
            advancedVisible = true;
            updateAdvancedVisibility();
        }
    }

    private void updateAdvancedVisibility() {
        if (advancedHost != null) advancedHost.setVisibility(advancedVisible ? View.VISIBLE : View.GONE);
        if (advancedToggleHint != null) {
            advancedToggleHint.setText(advancedVisible
                    ? "Advanced parking развернут. Полный raw-набор ниже."
                    : "Блок свернут по умолчанию, чтобы основной сценарий парковки оставался читаемым.");
        }
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = executeVehicleCommand(functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : result.message);
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
            refreshStatusCards();
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addActionButton(LinearLayout root, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            action.run();
            refreshStatusCards();
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
                EcarxVehicleAdapter.Result result = executeVehicleCommand(command.functionId, command.value);
                ok &= result.success;
                sb.append(result.message).append("\n");
            }
            Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
            refreshStatusCards();
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        Button b = Ui.button(this, "Диагностика: " + label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int functionId : functionIds) {
                sb.append(formatDiagnosticLine(adapter, functionId)).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), Math.min(3, root.getChildCount()));
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addSignalCommand(LinearLayout root, String label, String methodName, int signalId, int value) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Сигнал отправлен" : "Ошибка сигнала");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(3, root.getChildCount()));
            refreshStatusCards();
        });
        root.addView(b, lpMatchWrap(0, 6, 0, 0));
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        Button b = Ui.button(this, "Диагностика сигналов: " + label);
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
        Button b = Ui.button(this, "HAL-диагностика: " + label);
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

    private EcarxVehicleAdapter.Result executeVehicleCommand(int functionId, int value) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        if (!adapter.isWritable(functionId)) {
            return EcarxVehicleAdapter.Result.external(
                    "Функция переведена в diagnostics/readback-only: " + compact(EcarxVehicleAdapter.hex(functionId)),
                    false,
                    true);
        }
        EcarxVehicleAdapter.Result support = adapter.support(functionId);
        if (!support.isSupported()) {
            return EcarxVehicleAdapter.Result.external(
                    "Функция недоступна: " + compact(support.message),
                    false,
                    false);
        }
        return CarCommandBus.sendVehicle(this, functionId, value);
    }

    private String formatDiagnosticLine(EcarxVehicleAdapter adapter, int functionId) {
        EcarxVehicleAdapter.Result support = adapter.support(functionId);
        EcarxVehicleAdapter.Result value = adapter.get(functionId);
        return EcarxVehicleAdapter.hex(functionId) + " :: " + compact(support.message) + " :: " + compact(value.message);
    }

    private void refreshStatusCards() {
        if (liveStatusValues.size() < 4) return;
        liveStatusValues.get(0).setText(avmStatusSummary());
        liveStatusValues.get(1).setText(apaStatusSummary());
        liveStatusValues.get(2).setText(pdcStatusSummary());
        liveStatusValues.get(3).setText(rctaStatusSummary());
    }

    private String avmStatusSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        EcarxVehicleAdapter.Result pac = adapter.get(EcarxVehicleAdapter.PAS_PAC_STATUS);
        EcarxVehicleAdapter.Result view = adapter.get(EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION);
        return compact(pac.message) + " · " + compact(view.message) + " · entry EVS";
    }

    private String apaStatusSummary() {
        EcarxVehicleAdapter vehicleAdapter = new EcarxVehicleAdapter(this);
        CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
        String disp = compact(adapter.get("getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP).message);
        String sts = compact(adapter.get("getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS).message);
        String propBtn = compact(vehicleAdapter.get(EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_BTN_PUSH).message);
        String propMode = compact(vehicleAdapter.get(EcarxVehicleAdapter.PAS_DRVR_ASSC_SYS_PARK_MOD).message);
        return disp + " · " + sts + " · " + propBtn + " · " + propMode;
    }

    private String pdcStatusSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        String pdc = compact(adapter.get(EcarxVehicleAdapter.ADAS_PDC).message);
        String pac = compact(adapter.get(EcarxVehicleAdapter.PAS_PAC_ACTIVATION).message);
        return pdc + " · " + pac;
    }

    private String rctaStatusSummary() {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        String activation = compact(adapter.get(EcarxVehicleAdapter.PAS_RCTA_ACTIVATION).message);
        String left = compact(adapter.get(EcarxVehicleAdapter.PAS_RCTA_LEFT_WARNING).message);
        return activation + " · " + left;
    }

    private String compact(String message) {
        if (message == null) return "";
        return message
                .replace("getFunctionValue ", "")
                .replace("getCustomizeFunctionValue ", "")
                .replace("ecarx.car.hardware.signal.", "")
                .trim();
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
