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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SteeringActivity extends Activity {
    private SharedPreferences automationPrefs;
    private SharedPreferences steeringPrefs;
    private LinearLayout contentHost;
    private Mode mode = Mode.HOME;
    private String selectedName = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        automationPrefs = AutomationEngine.prefs(this);
        steeringPrefs = getSharedPreferences("steering", MODE_PRIVATE);
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        automationPrefs = AutomationEngine.prefs(this);
        steeringPrefs = getSharedPreferences("steering", MODE_PRIVATE);
        renderContent();
    }

    private View buildShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(Ui.dashboardBg(this));
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));
        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateIn(dock, 220, 18f);
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        if (mode == Mode.EDITOR) contentHost.addView(buildEditorPanel(selectedName), lpMatchWrap(0, 0, 0, 16));
        else {
            contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildBindingsPanel(), lpMatchWrap(0, 0, 0, 16));
            contentHost.addView(buildExamplesPanel(), lpMatchWrap(0, 0, 0, 16));
        }
        Ui.staggerIn(collectChildren(contentHost), 30, 55);
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.bindPress(back, () -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME, selectedName);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, mode == Mode.EDITOR ? "Binding Editor / Review" : "Wheel Gestures / Shortcuts"));
        titleBlock.addView(Ui.text(this, "Кнопки руля", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Event", lastEventShort()));
        bar.addView(buildTopStat("Bindings", String.valueOf(AutomationEngine.names(automationPrefs, AutomationEngine.KEY_BUTTON_ORDER).size())));
        bar.addView(buildTopStat("Mode", mode == Mode.EDITOR ? "Edit" : "Ready"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Wheel Command Flow"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Последнее событие", steeringPrefs.getString("last_event", "нет")));
        left.addView(metricLine("Время", lastEventTime()));
        left.addView(metricLine("Назначений", String.valueOf(AutomationEngine.names(automationPrefs, AutomationEngine.KEY_BUTTON_ORDER).size())));
        left.addView(metricLine("Жесты", "press · double · triple · hold"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        SteeringVisualView visual = new SteeringVisualView(this);
        LinearLayout.LayoutParams visualLp = new LinearLayout.LayoutParams(Ui.dp(this, 340), Ui.dp(this, 240));
        visualLp.leftMargin = Ui.dp(this, 12);
        row.addView(visual, visualLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "New Hold", () -> openEditor("", "77", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addActionChip(quick, "New Double", () -> openEditor("", "77", "double", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addActionChip(quick, "Examples", () -> {
            installExamples();
            renderContent();
        });
        addActionChip(quick, "Last Event", this::showLastEventSheet);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Last Event", lastEventShort(), Ui.CYAN);
        addStatusCard(grid, "Bindings", previewBindingNames(), Ui.SUCCESS);
        addStatusCard(grid, "Conditions", "always · stationary · moving · app · profile · cabinTemp", Ui.WARNING);
        addStatusCard(grid, "Targets", "preset · scenario · action · voice · launch · command", Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildBindingsPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Assignments"));
        panel.addView(Ui.text(this, "Главный слой: готовые steering bindings, быстрый выбор жестов и явное тестирование.", 14, false));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Hold", () -> openEditor("", "77", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addActionChip(actions, "Double", () -> openEditor("", "77", "double", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        addActionChip(actions, "Triple", () -> openEditor("", "77", "triple", "", "always", "replace", "command", "0x21110100/0=0x1"));
        addActionChip(actions, "Press", () -> openEditor("", "77", "press", "", "always", "replace", "preset", AutomationStore.firstPreset(this)));
        panel.addView(actions, lpMatchWrap(0, 12, 0, 12));

        List<String> names = AutomationEngine.names(automationPrefs, AutomationEngine.KEY_BUTTON_ORDER);
        if (names.isEmpty()) {
            panel.addView(emptyState("Назначения пока не созданы"));
            return panel;
        }
        for (String name : names) panel.addView(buildBindingCard(name), lpMatchWrap(0, 0, 0, 14));
        return panel;
    }

    private LinearLayout buildBindingCard(String name) {
        String raw = automationPrefs.getString("button2:" + name, automationPrefs.getString("button:" + name, ""));
        AutomationEngine.ButtonBinding binding = AutomationEngine.ButtonBinding.parse(raw);

        LinearLayout card = Ui.glassCard(this);
        boolean selected = name.equals(selectedName);
        card.setBackground(Ui.cardBg(this,
                selected ? Color.argb(118, 77, 163, 255) : Ui.glassSurface(this),
                Ui.dp(this, 24),
                selected ? Color.argb(120, 77, 163, 255) : Ui.glassLine(this)));

        LinearLayout top = Ui.row(this);
        top.addView(Ui.text(this, name, 20, true), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(Ui.pill(this, binding.gesture, gestureColor(binding.gesture)));
        card.addView(top);
        card.addView(Ui.muted(this, "keyCode " + binding.keyCode + " · modifier " + modifierLabel(binding.modifier) + " · behavior " + binding.behavior));
        card.addView(Ui.muted(this, "condition " + binding.condition + " · target " + binding.targetType + "=" + binding.target));

        LinearLayout row = Ui.row(this);
        addMiniAction(row, "Select", () -> {
            selectedName = name;
            renderContent();
        });
        addMiniAction(row, "Test", () -> showRunResult(name, binding));
        addMiniAction(row, "Edit", () -> openEditor(name, String.valueOf(binding.keyCode), binding.gesture, binding.modifier, binding.condition, binding.behavior, binding.targetType, binding.target));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildExamplesPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 16, 24, 42) : Color.argb(246, 240, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Examples"));
        panel.addView(Ui.text(this, "Примеры из `Design.txt`: hold 360, double cooling, voice hold, mute media, eco comfort toggle, stationary trunk.", 14, false));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        addExampleTile(grid, "Hold 360", Ui.CYAN, () -> openEditor("M hold 360", "77", "hold", "", "always", "replace", "command", "0x21110100/0=0x1"));
        addExampleTile(grid, "Double Cooling", Ui.SUCCESS, () -> openEditor("M double cooling", "77", "double", "", "always", "replace", "preset", "Летнее охлаждение"));
        addExampleTile(grid, "Voice Hold", Ui.WARNING, () -> openEditor("Voice hold Monji", "231", "hold", "", "always", "replace", "launch", "com.prodject.gflow"));
        addExampleTile(grid, "Mute Media", Color.rgb(129, 149, 255), () -> openEditor("Volume down double mute", "25", "double", "", "always", "together", "voice", "mute media"));
        addExampleTile(grid, "Eco Comfort", Color.rgb(88, 190, 172), () -> openEditor("Next hold eco comfort", "87", "hold", "", "always", "replace", "scenario", "Eco Comfort toggle"));
        addExampleTile(grid, "Stationary Trunk", Color.rgb(255, 158, 91), () -> openEditor("M stationary trunk", "77", "press", "", "stationary", "stationary-only", "command", "0x21110100/0=0x64"));
        panel.addView(grid, lpMatchWrap(0, 12, 0, 0));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Install Examples", () -> {
            installExamples();
            renderContent();
        });
        addActionChip(row, "Refresh", this::renderContent);
        addActionChip(row, "Home", () -> openMode(Mode.HOME, selectedName));
        panel.addView(row, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildEditorPanel(String seed) {
        BindingDraft draft = draft(seed);

        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(238, 12, 18, 32) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Binding Editor"));
        panel.addView(Ui.text(this, draft.oldName.isEmpty() ? "Новое назначение" : "Назначение: " + draft.oldName, 22, true));
        panel.addView(Ui.muted(this, "Поддерживаются gesture, modifier, conditions, behavior и target types из `Design.txt`."));

        EditText name = edit("Название", draft.name);
        EditText key = edit("keyCode", draft.keyCode);
        key.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText gesture = edit("press / double / triple / hold", draft.gesture);
        EditText modifier = edit("empty / held key / combo", draft.modifier);
        EditText condition = edit("always / stationary / moving / app=maps / profile=Driver / cabinTemp>28", draft.condition);
        EditText behavior = edit("replace / together / hold-only / stationary-only", draft.behavior);
        EditText targetType = edit("preset / scenario / action / voice / launch / command", draft.targetType);
        EditText target = edit("Цель", draft.target);

        panel.addView(name);
        panel.addView(key);
        panel.addView(buildChoicePanel("Gesture Types", gesture, new String[]{"press", "double", "triple", "hold"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(modifier);
        panel.addView(buildChoicePanel("Conditions", condition, new String[]{"always", "stationary", "moving", "app=maps", "profile=Driver", "cabinTemp>28"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoicePanel("Behavior", behavior, new String[]{"replace", "together", "hold-only", "stationary-only"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(buildChoicePanel("Target Types", targetType, new String[]{"preset", "scenario", "action", "voice", "launch", "command"}), lpMatchWrap(0, 12, 0, 0));
        panel.addView(target);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Save", () -> {
            saveBinding(draft.oldName, name.getText().toString(), key.getText().toString(), gesture.getText().toString(),
                    modifier.getText().toString(), condition.getText().toString(), behavior.getText().toString(),
                    targetType.getText().toString(), target.getText().toString());
            selectedName = name.getText().toString().trim();
            openMode(Mode.HOME, selectedName);
        });
        addActionChip(row, "Test", () -> {
            AutomationEngine.ButtonBinding binding = AutomationEngine.ButtonBinding.parse(composeRaw(name.getText().toString(), key.getText().toString(), gesture.getText().toString(),
                    modifier.getText().toString(), condition.getText().toString(), behavior.getText().toString(),
                    targetType.getText().toString(), target.getText().toString()));
            showResultSheet("Тест назначения", AutomationEngine.runSteering(this, parseInt(key.getText().toString(), 0),
                    gesture.getText().toString().trim().toLowerCase(Locale.ROOT),
                    modifier.getText().toString().trim(), currentForeground()).message + "\npreview " + binding.targetType + "=" + binding.target);
        });
        addActionChip(row, "Delete", () -> {
            if (draft.oldName.isEmpty()) {
                Ui.toast(this, "Нечего удалять");
                return;
            }
            AutomationStore.deleteNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", draft.oldName);
            getSharedPreferences(AutomationEngine.PREFS, MODE_PRIVATE).edit().remove("button:" + draft.oldName).apply();
            if (draft.oldName.equals(selectedName)) selectedName = "";
            openMode(Mode.HOME, selectedName);
        });
        addActionChip(row, "Home", () -> openMode(Mode.HOME, selectedName));
        panel.addView(row, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildChoicePanel(String title, EditText field, String[] items) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        LinearLayout row = Ui.row(this);
        for (String item : items) {
            Button button = Ui.button(this, item);
            button.setTextSize(13);
            Ui.bindPress(button, () -> field.setText(item));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1f);
            lp.leftMargin = Ui.dp(this, 4);
            lp.rightMargin = Ui.dp(this, 4);
            row.addView(button, lp);
        }
        card.addView(row, lpMatchWrap(0, 8, 0, 0));
        return card;
    }

    private void saveBinding(String oldName, String name, String key, String gesture, String modifier, String condition,
                             String behavior, String targetType, String target) {
        String clean = name.trim();
        if (clean.isEmpty()) {
            Ui.toast(this, "Укажите название");
            return;
        }
        String normalizedGesture = gesture.trim().toLowerCase(Locale.ROOT);
        String raw = composeRaw(clean, key, normalizedGesture, modifier, condition, behavior, targetType, target);
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button:", oldName, clean,
                clean + "|" + key.trim() + "|" + normalizedGesture + "|" + target.trim());
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", oldName, clean, raw);
        Ui.toast(this, "Назначение сохранено");
    }

    private String composeRaw(String name, String key, String gesture, String modifier, String condition,
                              String behavior, String targetType, String target) {
        return name.trim()
                + "|" + key.trim()
                + "|" + gesture.trim().toLowerCase(Locale.ROOT)
                + "|" + modifier.trim()
                + "|" + condition.trim()
                + "|" + behavior.trim()
                + "|" + targetType.trim()
                + "|" + target.trim();
    }

    private void installExamples() {
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M hold 360", "M hold 360|77|hold||always|replace|command|0x21110100/0=0x1");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M double cooling", "M double cooling|77|double||always|replace|preset|Летнее охлаждение");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Voice hold Monji", "Voice hold Monji|231|hold||always|replace|launch|com.prodject.gflow");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Volume down double mute", "Volume down double mute|25|double||always|together|voice|mute media");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Next hold eco comfort", "Next hold eco comfort|87|hold||always|replace|scenario|Eco Comfort toggle");
        AutomationStore.saveNamed(this, AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M stationary trunk", "M stationary trunk|77|press||stationary|stationary-only|command|0x21110100/0=0x64");
        Ui.toast(this, "Примеры кнопок руля добавлены");
    }

    private void showRunResult(String name, AutomationEngine.ButtonBinding binding) {
        selectedName = name;
        String result = AutomationEngine.runSteering(this, binding.keyCode, binding.gesture, binding.modifier, currentForeground()).message;
        showResultSheet("Тест назначения", result);
        renderContent();
    }

    private void showLastEventSheet() {
        showResultSheet("Последнее событие", steeringPrefs.getString("last_event", "нет") + "\n" + lastEventTime());
    }

    private void showResultSheet(String title, String body) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, title, 22, true));
        sheet.addView(Ui.muted(this, body == null || body.trim().isEmpty() ? "Нет данных" : body));
        scroll.addView(sheet);
        builder.setView(scroll);
        builder.setPositiveButton("Закрыть", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 22), Color.TRANSPARENT));
        });
        dialog.show();
    }

    private void openEditor(String oldName, String key, String gesture, String modifier, String condition, String behavior, String targetType, String target) {
        selectedName = oldName.isEmpty() ? composeRaw("draft", key, gesture, modifier, condition, behavior, targetType, target) : oldName;
        mode = Mode.EDITOR;
        editorSeed = new BindingDraft(oldName, oldName, key, gesture, modifier, condition, behavior, targetType, target);
        renderContent();
    }

    private BindingDraft editorSeed;

    private BindingDraft draft(String seed) {
        if (editorSeed != null) return editorSeed;
        if (seed == null || seed.trim().isEmpty()) {
            return new BindingDraft("", "", "77", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this));
        }
        String raw = automationPrefs.getString("button2:" + seed, automationPrefs.getString("button:" + seed, ""));
        if (raw == null || raw.trim().isEmpty() || raw.startsWith("draft|")) {
            return new BindingDraft("", "", "77", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this));
        }
        AutomationEngine.ButtonBinding binding = AutomationEngine.ButtonBinding.parse(raw);
        return new BindingDraft(seed, seed, String.valueOf(binding.keyCode), binding.gesture, binding.modifier, binding.condition, binding.behavior, binding.targetType, binding.target);
    }

    private void openMode(Mode next, String name) {
        mode = next;
        selectedName = name == null ? "" : name;
        if (next == Mode.HOME) editorSeed = null;
        renderContent();
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

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextSize(13);
        Ui.bindPress(b, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(118, 255, 255, 255) : Color.argb(232, 255, 255, 255),
                Ui.dp(this, 26),
                Ui.glassLine(this)));
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 18, true));
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

    private void addExampleTile(GridLayout grid, String label, int color, Runnable action) {
        TextView tile = new TextView(this);
        tile.setText(label);
        tile.setTextColor(Color.WHITE);
        tile.setTextSize(14);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16));
        tile.setBackground(Ui.cardBg(this, Color.argb(88, Color.red(color), Color.green(color), Color.blue(color)), Ui.dp(this, 22), Color.argb(80, 255, 255, 255)));
        Ui.bindPress(tile, action);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 12), Ui.dp(this, 12));
        grid.addView(tile, lp);
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Hold", () -> openEditor("", "77", "hold", "", "always", "replace", "preset", AutomationStore.firstPreset(this)), false);
        addDockButton(dock, "Double", () -> openEditor("", "77", "double", "", "always", "replace", "preset", AutomationStore.firstPreset(this)), false);
        addDockButton(dock, "Examples", () -> {
            installExamples();
            renderContent();
        }, false);
        addDockButton(dock, "Voice", () -> startActivity(new android.content.Intent(this, VoiceActivity.class)), false);
        addDockButton(dock, "Home", () -> openMode(Mode.HOME, selectedName), mode == Mode.HOME);
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
        Ui.bindPress(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private EditText edit(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 18), Ui.glassLine(this)));
        field.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        field.setLayoutParams(lpMatchWrap(0, 12, 0, 0));
        return field;
    }

    private TextView emptyState(String text) {
        TextView view = Ui.text(this, text, 16, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 24));
        return view;
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private String previewBindingNames() {
        List<String> names = AutomationEngine.names(automationPrefs, AutomationEngine.KEY_BUTTON_ORDER);
        if (names.isEmpty()) return "нет";
        if (names.size() == 1) return names.get(0);
        return names.get(0) + " +" + (names.size() - 1);
    }

    private String lastEventShort() {
        String event = steeringPrefs.getString("last_event", "нет");
        return event.length() > 28 ? event.substring(0, 28) + "…" : event;
    }

    private String lastEventTime() {
        long at = steeringPrefs.getLong("last_event_at", 0L);
        if (at == 0L) return "неизвестно";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(at));
    }

    private String modifierLabel(String modifier) {
        return modifier == null || modifier.trim().isEmpty() ? "none" : modifier.trim();
    }

    private int gestureColor(String gesture) {
        if ("hold".equals(gesture)) return Ui.CYAN;
        if ("double".equals(gesture)) return Ui.SUCCESS;
        if ("triple".equals(gesture)) return Ui.WARNING;
        return Color.rgb(129, 149, 255);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String currentForeground() {
        return getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE)
                .getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "");
    }

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
    }

    private enum Mode {
        HOME,
        EDITOR
    }

    private static final class BindingDraft {
        final String oldName;
        final String name;
        final String keyCode;
        final String gesture;
        final String modifier;
        final String condition;
        final String behavior;
        final String targetType;
        final String target;

        BindingDraft(String oldName, String name, String keyCode, String gesture, String modifier,
                     String condition, String behavior, String targetType, String target) {
            this.oldName = oldName;
            this.name = name;
            this.keyCode = keyCode;
            this.gesture = gesture;
            this.modifier = modifier;
            this.condition = condition;
            this.behavior = behavior;
            this.targetType = targetType;
            this.target = target;
        }
    }

    private static final class SteeringVisualView extends View {
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

        SteeringVisualView(Context context) {
            super(context);
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(Ui.dp(context, 12));
            ring.setColor(Color.argb(180, 255, 255, 255));
            glow.setStyle(Paint.Style.FILL);
            glow.setColor(Color.argb(54, 77, 163, 255));
            accent.setStyle(Paint.Style.FILL);
            accent.setColor(Ui.CYAN);
            text.setColor(Color.WHITE);
            text.setTextSize(Ui.dp(context, 12));
            text.setTextAlign(Paint.Align.CENTER);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float radius = Math.min(w, h) * 0.32f;
            canvas.drawCircle(cx, cy, radius * 1.28f, glow);
            canvas.drawCircle(cx, cy, radius, ring);
            RectF left = new RectF(cx - radius - Ui.dp(getContext(), 28), cy - Ui.dp(getContext(), 18), cx - radius + Ui.dp(getContext(), 8), cy + Ui.dp(getContext(), 18));
            RectF right = new RectF(cx + radius - Ui.dp(getContext(), 8), cy - Ui.dp(getContext(), 18), cx + radius + Ui.dp(getContext(), 28), cy + Ui.dp(getContext(), 18));
            RectF bottom = new RectF(cx - Ui.dp(getContext(), 22), cy + radius - Ui.dp(getContext(), 8), cx + Ui.dp(getContext(), 22), cy + radius + Ui.dp(getContext(), 46));
            canvas.drawRoundRect(left, Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), accent);
            canvas.drawRoundRect(right, Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), accent);
            canvas.drawRoundRect(bottom, Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), accent);
            canvas.drawText("MODE", left.centerX(), left.centerY() + Ui.dp(getContext(), 4), text);
            canvas.drawText("VOICE", right.centerX(), right.centerY() + Ui.dp(getContext(), 4), text);
            canvas.drawText("MENU", bottom.centerX(), bottom.centerY() + Ui.dp(getContext(), 4), text);
            canvas.drawText("press / double / triple / hold", cx, cy, text);
        }
    }
}
