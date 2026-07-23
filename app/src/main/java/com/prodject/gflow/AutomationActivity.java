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
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class AutomationActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout contentHost;
    private Mode mode = Mode.HOME;
    private String selectedName = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = AutomationEngine.prefs(this);
        setContentView(buildAutomationShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        prefs = AutomationEngine.prefs(this);
        renderContent();
    }

    private View buildAutomationShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        switch (mode) {
            case PRESETS:
                contentHost.addView(buildPresetList(), lpMatchWrap(0, 0, 0, 16));
                break;
            case PRESET_EDITOR:
                contentHost.addView(buildPresetEditor(selectedName), lpMatchWrap(0, 0, 0, 16));
                break;
            case SCENARIOS:
                contentHost.addView(buildScenarioList(), lpMatchWrap(0, 0, 0, 16));
                break;
            case SCENARIO_EDITOR:
                contentHost.addView(buildScenarioEditor(selectedName), lpMatchWrap(0, 0, 0, 16));
                break;
            case TRIGGERS:
                contentHost.addView(buildTriggerList(), lpMatchWrap(0, 0, 0, 16));
                break;
            case TRIGGER_EDITOR:
                contentHost.addView(buildTriggerEditor(selectedName), lpMatchWrap(0, 0, 0, 16));
                break;
            case CAMERA_RULE:
                contentHost.addView(buildCameraRulePanel(), lpMatchWrap(0, 0, 0, 16));
                break;
            case LOG:
                contentHost.addView(buildLogPanel(), lpMatchWrap(0, 0, 0, 16));
                break;
            case HOME:
            default:
                contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildTemplatePanel(), lpMatchWrap(0, 0, 0, 16));
                contentHost.addView(buildIdeasPanel(), lpMatchWrap(0, 0, 0, 16));
                break;
        }
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, modeLabel()));
        TextView title = Ui.text(this, "Автоматизация", 28, true);
        title.setPadding(0, 0, 0, 0);
        titleBlock.addView(title);
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Presets", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_PRESET_ORDER).size())));
        bar.addView(buildTopStat("Scenarios", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_SCENARIO_ORDER).size())));
        bar.addView(buildTopStat("Triggers", String.valueOf(AutomationEngine.names(prefs, AutomationEngine.KEY_TRIGGER_ORDER).size())));
        return bar;
    }

    private String modeLabel() {
        switch (mode) {
            case PRESETS: return "Preset Library";
            case PRESET_EDITOR: return "Preset Editor";
            case SCENARIOS: return "Scenario Library";
            case SCENARIO_EDITOR: return "Scenario Editor";
            case TRIGGERS: return "Trigger Library";
            case TRIGGER_EDITOR: return "Trigger Editor";
            case CAMERA_RULE: return "Low-speed Rule";
            case LOG: return "Execution Log";
            case HOME:
            default: return "Automation Engine";
        }
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
        left.addView(metricLine("Smart presets", "HVAC, кузов, HUD, media"));
        left.addView(metricLine("Scenarios", "Триггеры, условия, шаги, политики"));
        left.addView(metricLine("Rules", "Low-speed camera и контекстные режимы"));
        left.addView(metricLine("Runtime", shortLogPreview()));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        AutomationVisualView visual = new AutomationVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Новый preset", () -> openEditor(Mode.PRESET_EDITOR, ""));
        addActionChip(quick, "Новый сценарий", () -> openEditor(Mode.SCENARIO_EDITOR, ""));
        addActionChip(quick, "Новый trigger", () -> openEditor(Mode.TRIGGER_EDITOR, ""));
        addActionChip(quick, "Журнал", () -> openMode(Mode.LOG));
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private View buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Smart presets", previewNames(AutomationEngine.KEY_PRESET_ORDER), Ui.CYAN);
        addStatusCard(grid, "Scenarios", previewNames(AutomationEngine.KEY_SCENARIO_ORDER), Ui.SUCCESS);
        addStatusCard(grid, "Triggers", previewNames(AutomationEngine.KEY_TRIGGER_ORDER), Ui.WARNING);
        addStatusCard(grid, "Low-speed camera", cameraRuleSummary(), Color.rgb(129, 149, 255));
        addNavCard(grid, "Preset Library", "Редактор smart presets", Ui.CYAN, () -> openMode(Mode.PRESETS));
        addNavCard(grid, "Scenario Library", "Полные сценарии и политики", Ui.SUCCESS, () -> openMode(Mode.SCENARIOS));
        addNavCard(grid, "Trigger Library", "Привязка signal/app/manual", Ui.WARNING, () -> openMode(Mode.TRIGGERS));
        addNavCard(grid, "Execution Log", "История запусков и отладка", Color.rgb(129, 149, 255), () -> openMode(Mode.LOG));
        return grid;
    }

    private LinearLayout buildTemplatePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Templates & Actions"));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addTemplateTile(grid, "Winter/Summer", Ui.CYAN, () -> {
            AutomationStore.installClimateScenarios(this);
            Ui.toast(this, "Climate scenarios installed");
            renderContent();
        });
        addTemplateTile(grid, "Welcome/Leave", Ui.SUCCESS, () -> {
            AutomationStore.installWelcomeLeave(this);
            Ui.toast(this, "Welcome / Leave installed");
            renderContent();
        });
        addTemplateTile(grid, "Parking Guard", Ui.WARNING, () -> {
            AutomationStore.installParkingGuard(this);
            Ui.toast(this, "Parking Guard installed");
            renderContent();
        });
        addTemplateTile(grid, "Rain", Color.rgb(73, 130, 83), () -> {
            AutomationStore.installRain(this);
            Ui.toast(this, "Rain scenario installed");
            renderContent();
        });
        addTemplateTile(grid, "Night Mode", Color.rgb(88, 105, 130), () -> {
            AutomationStore.installNightMode(this);
            Ui.toast(this, "Night mode installed");
            renderContent();
        });
        addTemplateTile(grid, "Navigation", Color.rgb(58, 106, 156), () -> {
            AutomationStore.installNavigationContext(this);
            Ui.toast(this, "Navigation context installed");
            renderContent();
        });
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout lists = Ui.row(this);
        addActionChip(lists, "Presets", () -> openMode(Mode.PRESETS));
        addActionChip(lists, "Scenarios", () -> openMode(Mode.SCENARIOS));
        addActionChip(lists, "Triggers", () -> openMode(Mode.TRIGGERS));
        addActionChip(lists, "Low-speed cam", () -> openMode(Mode.CAMERA_RULE));
        panel.addView(lists, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildIdeasPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Automation Notes"));
        panel.addView(Ui.text(this, automationIdeas(), 14, false));
        return panel;
    }

    private LinearLayout buildPresetList() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Smart Presets"));
        panel.addView(Ui.text(this, "Команды пресета: raw AdaptAPI, float-команды и action-шаги.", 14, false));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Новый preset", () -> openEditor(Mode.PRESET_EDITOR, ""));
        addActionChip(actions, "Обновить", this::renderContent);
        addActionChip(actions, "Домой", () -> openMode(Mode.HOME));
        addActionChip(actions, "Журнал", () -> openMode(Mode.LOG));
        panel.addView(actions, lpMatchWrap(0, 12, 0, 12));

        List<String> names = AutomationEngine.names(prefs, AutomationEngine.KEY_PRESET_ORDER);
        if (names.isEmpty()) {
            panel.addView(emptyState("Пресеты пока не созданы"));
            return panel;
        }
        for (String name : names) {
            String body = prefs.getString("preset:" + name, "");
            panel.addView(buildPresetCard(name, body), lpMatchWrap(0, 0, 0, 14));
        }
        return panel;
    }

    private LinearLayout buildScenarioList() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Scenario Library"));
        panel.addView(Ui.text(this, "Сценарии поддерживают triggers, conditions, policy и step-цепочки.", 14, false));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Новый сценарий", () -> openEditor(Mode.SCENARIO_EDITOR, ""));
        addActionChip(actions, "Обновить", this::renderContent);
        addActionChip(actions, "Домой", () -> openMode(Mode.HOME));
        addActionChip(actions, "Журнал", () -> openMode(Mode.LOG));
        panel.addView(actions, lpMatchWrap(0, 12, 0, 12));

        List<String> names = AutomationEngine.names(prefs, AutomationEngine.KEY_SCENARIO_ORDER);
        if (names.isEmpty()) {
            panel.addView(emptyState("Сценарии пока не созданы"));
            return panel;
        }
        for (String name : names) {
            String body = prefs.getString("scenario:" + name, "");
            panel.addView(buildScenarioCard(name, body), lpMatchWrap(0, 0, 0, 14));
        }
        return panel;
    }

    private LinearLayout buildTriggerList() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Trigger Library"));
        panel.addView(Ui.text(this, "Триггеры связывают события `manual/boot/app/voice/button` с готовыми smart presets.", 14, false));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Новый trigger", () -> openEditor(Mode.TRIGGER_EDITOR, ""));
        addActionChip(actions, "Обновить", this::renderContent);
        addActionChip(actions, "Домой", () -> openMode(Mode.HOME));
        addActionChip(actions, "Low-speed", () -> openMode(Mode.CAMERA_RULE));
        panel.addView(actions, lpMatchWrap(0, 12, 0, 12));

        List<String> names = AutomationEngine.names(prefs, AutomationEngine.KEY_TRIGGER_ORDER);
        if (names.isEmpty()) {
            panel.addView(emptyState("Триггеры пока не созданы"));
            return panel;
        }
        for (String name : names) {
            String raw = prefs.getString("trigger:" + name, "");
            panel.addView(buildTriggerCard(name, raw), lpMatchWrap(0, 0, 0, 14));
        }
        return panel;
    }

    private LinearLayout buildPresetEditor(String oldName) {
        String body = oldName.isEmpty() ? "" : prefs.getString("preset:" + oldName, "");
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, oldName.isEmpty() ? "Новый Smart Preset" : "Preset Editor"));
        panel.addView(Ui.text(this, "Поддерживаются строки `action:`, raw-команды и `float:` значения.", 14, false));

        EditText name = textField("Название", oldName, false);
        EditText editor = textField("Тело пресета", body, true);
        editor.setHint(AutomationStore.defaultPresetBody());

        panel.addView(name, lpMatchWrap(0, 12, 0, 12));
        panel.addView(editor, lpMatchWrap(0, 0, 0, 12));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сохранить", () -> {
            AutomationStore.savePreset(this, oldName, name.getText().toString(), editor.getText().toString());
            openMode(Mode.PRESETS);
        });
        addActionChip(row, "Запустить", () -> showResult(panel, AutomationEngine.runPreset(this, name.getText().toString().trim())));
        addActionChip(row, "Назад", () -> openMode(Mode.PRESETS));
        addActionChip(row, "Удалить", () -> {
            if (!oldName.isEmpty()) {
                AutomationStore.deleteNamed(this, AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName);
                openMode(Mode.PRESETS);
            }
        });
        panel.addView(row, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildScenarioEditor(String oldName) {
        String body = oldName.isEmpty() ? "" : prefs.getString("scenario:" + oldName, "");
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, oldName.isEmpty() ? "Новый Сценарий" : "Scenario Editor"));
        panel.addView(Ui.text(this, "Формат: `trigger:`, `condition:`, `policy:`, `step:`. Имя можно хранить как `name:` внутри тела.", 14, false));

        EditText name = textField("Название", oldName, false);
        EditText editor = textField("Описание сценария", body, true);
        editor.setHint(AutomationStore.defaultScenarioBody());

        panel.addView(name, lpMatchWrap(0, 12, 0, 12));
        panel.addView(editor, lpMatchWrap(0, 0, 0, 12));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сохранить", () -> {
            String clean = name.getText().toString().trim();
            String text = editor.getText().toString();
            if (!text.contains("name:")) text = "name:" + clean + "\n" + text;
            AutomationStore.saveNamed(this, AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName, clean, text);
            openMode(Mode.SCENARIOS);
        });
        addActionChip(row, "Запустить", () -> showResult(panel, AutomationEngine.runScenario(this, name.getText().toString().trim(), "manual", "ui")));
        addActionChip(row, "Назад", () -> openMode(Mode.SCENARIOS));
        addActionChip(row, "Удалить", () -> {
            if (!oldName.isEmpty()) {
                AutomationStore.deleteNamed(this, AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName);
                openMode(Mode.SCENARIOS);
            }
        });
        panel.addView(row, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildTriggerEditor(String oldName) {
        String raw = oldName.isEmpty() ? "" : prefs.getString("trigger:" + oldName, "");
        String[] parts = raw.split("\\|", -1);
        String typeValue = parts.length > 1 ? parts[1] : "manual";
        String matchValue = parts.length > 2 ? parts[2] : "";
        String presetValue = parts.length > 3 ? parts[3] : AutomationStore.firstPreset(this);

        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, oldName.isEmpty() ? "Новый Trigger" : "Trigger Editor"));
        panel.addView(Ui.text(this, "Trigger хранит имя, тип события, выражение совпадения и целевой smart preset.", 14, false));

        EditText name = textField("Название", oldName, false);
        EditText type = textField("manual / boot / app / voice / button", typeValue, false);
        EditText match = textField("Подстрока/условие события", matchValue, false);
        EditText preset = textField("Имя smart preset", presetValue, false);

        panel.addView(name, lpMatchWrap(0, 12, 0, 12));
        panel.addView(type, lpMatchWrap(0, 0, 0, 12));
        panel.addView(match, lpMatchWrap(0, 0, 0, 12));
        panel.addView(preset, lpMatchWrap(0, 0, 0, 12));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сохранить", () -> {
            String clean = name.getText().toString().trim();
            String value = clean + "|" + type.getText().toString().trim() + "|" + match.getText().toString().trim() + "|" + preset.getText().toString().trim();
            AutomationStore.saveNamed(this, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", oldName, clean, value);
            openMode(Mode.TRIGGERS);
        });
        addActionChip(row, "Проверить", () -> {
            AutomationEngine.runTrigger(this, type.getText().toString().trim(), match.getText().toString().trim());
            showResult(panel, "Trigger dispatched: " + type.getText().toString().trim() + " / " + match.getText().toString().trim());
        });
        addActionChip(row, "Назад", () -> openMode(Mode.TRIGGERS));
        addActionChip(row, "Удалить", () -> {
            if (!oldName.isEmpty()) {
                AutomationStore.deleteNamed(this, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", oldName);
                openMode(Mode.TRIGGERS);
            }
        });
        panel.addView(row, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildCameraRulePanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Low-speed Camera Rule"));
        panel.addView(Ui.text(this, "Правило открывает штатные 360/3D камеры при падении скорости ниже порога. Повторный запуск разрешается после возврата выше reset threshold.", 14, false));

        CheckBox enabled = new CheckBox(this);
        enabled.setText("Включать камеры при низкой скорости");
        enabled.setTextColor(Color.WHITE);
        enabled.setChecked(prefs.getBoolean(LowSpeedCameraService.KEY_ENABLED, false));

        EditText threshold = textField("Порог скорости, км/ч", String.valueOf(prefs.getFloat(LowSpeedCameraService.KEY_THRESHOLD, 30.0f)), false);
        threshold.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText reset = textField("Порог сброса, км/ч", String.valueOf(prefs.getFloat(LowSpeedCameraService.KEY_RESET_THRESHOLD, 35.0f)), false);
        reset.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        panel.addView(enabled, lpMatchWrap(0, 12, 0, 12));
        panel.addView(threshold, lpMatchWrap(0, 0, 0, 12));
        panel.addView(reset, lpMatchWrap(0, 0, 0, 12));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Сохранить", () -> {
            float thresholdValue = AutomationEngine.parseFloat(threshold.getText().toString(), 30.0f);
            float resetValue = Math.max(AutomationEngine.parseFloat(reset.getText().toString(), thresholdValue + 5.0f), thresholdValue + 1.0f);
            AutomationEngine.setLowSpeedCameraEnabled(this, enabled.isChecked(), thresholdValue);
            prefs.edit().putFloat(LowSpeedCameraService.KEY_RESET_THRESHOLD, resetValue).apply();
            Ui.toast(this, "Low-speed rule saved");
            renderContent();
        });
        addActionChip(row, "Статус", () -> showResult(panel, prefs.getString(LowSpeedCameraService.KEY_LAST_RESULT, "Правило еще не выполнялось")));
        addActionChip(row, "Triggers", () -> openMode(Mode.TRIGGERS));
        addActionChip(row, "Домой", () -> openMode(Mode.HOME));
        panel.addView(row, lpMatchWrap(0, 0, 0, 0));
        return panel;
    }

    private LinearLayout buildLogPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Execution Log"));
        panel.addView(Ui.text(this, shortLogPreview(), 14, false));

        TextView logView = Ui.text(this, fullLogPreview(), 13, false);
        logView.setTextColor(Ui.secondaryText(this));
        logView.setPadding(0, Ui.dp(this, 12), 0, 0);
        panel.addView(logView);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Очистить", () -> {
            prefs.edit().putString(AutomationEngine.KEY_LOG, "").apply();
            Ui.toast(this, "Журнал очищен");
            renderContent();
        });
        addActionChip(row, "Обновить", this::renderContent);
        addActionChip(row, "Сценарии", () -> openMode(Mode.SCENARIOS));
        addActionChip(row, "Домой", () -> openMode(Mode.HOME));
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildPresetCard(String name, String body) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, name));
        card.addView(Ui.text(this, summarizePreset(body), 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Run", () -> showResult(card, AutomationEngine.runPreset(this, name)));
        addActionChip(row, "Edit", () -> openEditor(Mode.PRESET_EDITOR, name));
        addActionChip(row, "Delete", () -> {
            AutomationStore.deleteNamed(this, AutomationEngine.KEY_PRESET_ORDER, "preset:", name);
            renderContent();
        });
        addActionChip(row, "Copy", () -> openEditor(Mode.PRESET_EDITOR, ""));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildScenarioCard(String name, String raw) {
        AutomationEngine.Scenario scenario = AutomationEngine.decodeScenario(raw);
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, name));
        card.addView(Ui.text(this,
                "Triggers: " + scenario.triggers.size() + " · Conditions: " + scenario.conditions.size() + " · Steps: " + scenario.steps.size(),
                14, false));
        card.addView(Ui.muted(this, firstNonEmptyLine(raw, "step:", "trigger:", "condition:")));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Run", () -> showResult(card, AutomationEngine.runScenario(this, name, "manual", "ui")));
        addActionChip(row, "Edit", () -> openEditor(Mode.SCENARIO_EDITOR, name));
        addActionChip(row, "Delete", () -> {
            AutomationStore.deleteNamed(this, AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", name);
            renderContent();
        });
        addActionChip(row, "Log", () -> openMode(Mode.LOG));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildTriggerCard(String name, String raw) {
        String[] parts = raw.split("\\|", -1);
        String type = parts.length > 1 ? parts[1] : "manual";
        String match = parts.length > 2 ? parts[2] : "";
        String preset = parts.length > 3 ? parts[3] : "";
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, name));
        card.addView(Ui.text(this, "Type: " + type + " · Match: " + match + " · Preset: " + preset, 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Test", () -> {
            AutomationEngine.runTrigger(this, type, match);
            showResult(card, "Trigger tested: " + type + " / " + match);
        });
        addActionChip(row, "Edit", () -> openEditor(Mode.TRIGGER_EDITOR, name));
        addActionChip(row, "Delete", () -> {
            AutomationStore.deleteNamed(this, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", name);
            renderContent();
        });
        addActionChip(row, "Preset", () -> openMode(Mode.PRESETS));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
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

    private void addNavCard(GridLayout grid, String title, String value, int color, Runnable action) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 15, false));
        card.setOnClickListener(v -> action.run());
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 72), Ui.dp(this, 6));
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
        addDockButton(dock, "Home", () -> openMode(Mode.HOME), mode == Mode.HOME);
        addDockButton(dock, "Preset", () -> openMode(Mode.PRESETS), mode == Mode.PRESETS || mode == Mode.PRESET_EDITOR);
        addDockButton(dock, "Scenario", () -> openMode(Mode.SCENARIOS), mode == Mode.SCENARIOS || mode == Mode.SCENARIO_EDITOR);
        addDockButton(dock, "Trigger", () -> openMode(Mode.TRIGGERS), mode == Mode.TRIGGERS || mode == Mode.TRIGGER_EDITOR);
        addDockButton(dock, "Log", () -> openMode(Mode.LOG), mode == Mode.LOG);
        return dock;
    }

    private void addTemplateTile(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16));
        tile.setBackground(Ui.cardBg(this, Color.argb(88, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 22), Color.argb(80, 255, 255, 255)));
        tile.setOnClickListener(v -> action.run());
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
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
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

    private EditText textField(String hint, String value, boolean multiline) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(50, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(88, 185, 198, 214)));
        field.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        if (multiline) {
            field.setMinLines(10);
            field.setGravity(Gravity.TOP | Gravity.START);
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        return field;
    }

    private TextView emptyState(String text) {
        TextView view = Ui.text(this, text, 15, false);
        view.setTextColor(Ui.secondaryText(this));
        view.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 4));
        return view;
    }

    private void showResult(LinearLayout panel, String result) {
        TextView view = Ui.text(this, result, 13, false);
        view.setTextColor(Ui.secondaryText(this));
        view.setPadding(0, Ui.dp(this, 12), 0, 0);
        panel.addView(view);
    }

    private void openMode(Mode next) {
        mode = next;
        selectedName = "";
        renderContent();
    }

    private void openEditor(Mode next, String name) {
        mode = next;
        selectedName = name == null ? "" : name;
        renderContent();
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

    private String summarizePreset(String body) {
        AutomationEngine.CommandPlan plan = AutomationEngine.decodePlan(body);
        return "Actions: " + plan.actions.length + " · Commands: " + plan.commands.length + " · Floats: " + plan.floatCommands.length;
    }

    private String firstNonEmptyLine(String raw, String... prefixes) {
        for (String line : raw.split("\n")) {
            String trimmed = line.trim();
            for (String prefix : prefixes) {
                if (trimmed.startsWith(prefix)) return trimmed;
            }
        }
        return raw.trim().isEmpty() ? "Без шагов" : raw.trim().split("\n")[0];
    }

    private String shortLogPreview() {
        String log = AutomationEngine.scenarioLog(this);
        if (log == null || log.trim().isEmpty()) return "Журнал пуст";
        String[] lines = log.split("\n");
        return lines.length == 0 ? "Журнал пуст" : lines[0];
    }

    private String fullLogPreview() {
        String log = AutomationEngine.scenarioLog(this);
        return log == null || log.trim().isEmpty() ? "Журнал пуст" : log;
    }

    private String cameraRuleSummary() {
        boolean enabled = prefs.getBoolean(LowSpeedCameraService.KEY_ENABLED, false);
        float threshold = prefs.getFloat(LowSpeedCameraService.KEY_THRESHOLD, 30.0f);
        return (enabled ? "ON" : "OFF") + " · " + String.format(Locale.US, "%.1f", threshold) + " km/h";
    }

    private String automationIdeas() {
        return "Что еще логично автоматизировать:\n"
                + "- Welcome / уход: при открытии двери водителя включить профиль, климат, подсветку и любимый режим движения.\n"
                + "- Parking guard: при парковке включать DVR/360 и закрывать окна/люк.\n"
                + "- Rain scenario: по ручному триггеру или датчику дождя закрыть окна/люк и включить дворники auto.\n"
                + "- Night mode: вечером менять яркость, HUD, тему DIM и салонную подсветку.\n"
                + "- App context: при запуске навигации включать split, HUD navigation и autozoom.\n"
                + "- Service mode: перед сервисом отключать экспериментальные функции и возвращать стандартный профиль.";
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private enum Mode {
        HOME,
        PRESETS,
        PRESET_EDITOR,
        SCENARIOS,
        SCENARIO_EDITOR,
        TRIGGERS,
        TRIGGER_EDITOR,
        CAMERA_RULE,
        LOG
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
