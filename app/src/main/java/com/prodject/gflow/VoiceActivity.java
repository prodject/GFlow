package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String PREF_LOG = "voice_log";
    private static final int LOG_LIMIT = 16;

    private final VoskVoiceRecognizer recognizer = new VoskVoiceRecognizer();
    private SharedPreferences prefs;
    private LinearLayout contentHost;
    private TextView recognitionView;
    private EditText commandInput;
    private String latestRecognition = "";
    private boolean listening;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getPreferences(MODE_PRIVATE);
        startForegroundService(new Intent(this, VoiceForegroundService.class));
        setContentView(buildShell());
        seedFromIntent();
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        prefs = getPreferences(MODE_PRIVATE);
        renderContent();
    }

    @Override protected void onDestroy() {
        recognizer.stop();
        listening = false;
        super.onDestroy();
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

        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void seedFromIntent() {
        String command = getIntent().getStringExtra("command");
        if (command != null && !command.trim().isEmpty()) latestRecognition = command.trim();
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildAssistantPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildCommandPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildAliasPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildLogPanel(), lpMatchWrap(0, 0, 0, 16));
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
        titleBlock.addView(Ui.label(this, "Голосовой ассистент"));
        titleBlock.addView(Ui.text(this, "Голосовой ассистент", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Сервис", serviceStatus()));
        bar.addView(buildTopStat("Vosk", voskStatus()));
        bar.addView(buildTopStat("Алиасы", String.valueOf(aliases().size())));
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
        hero.addView(Ui.label(this, "Foreground Voice / Распознавание"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Foreground voice service", serviceStatus()));
        left.addView(metricLine("Vosk", voskStatus()));
        left.addView(metricLine("Источник", sourceSummary()));
        left.addView(metricLine("Fallback", "broadcast command + log + alias prompt"));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout micCard = Ui.glassCard(this);
        micCard.setGravity(Gravity.CENTER);
        micCard.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        TextView mic = Ui.text(this, listening ? "MIC ON" : "MIC", 30, true);
        mic.setGravity(Gravity.CENTER);
        micCard.addView(mic);
        LinearLayout.LayoutParams micLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        micLp.leftMargin = Ui.dp(this, 12);
        row.addView(micCard, micLp);
        hero.addView(row);

        recognitionView = Ui.text(this, recognitionSummary(), 16, true);
        recognitionView.setTextColor(Ui.primaryText(this));
        recognitionView.setGravity(Gravity.CENTER_HORIZONTAL);
        recognitionView.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 4));
        hero.addView(recognitionView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Слушать Vosk", this::startListening);
        addActionChip(quick, "Стоп", this::stopListening);
        addActionChip(quick, "Выполнить", this::runInputCommand);
        addActionChip(quick, "Ручной ввод", this::focusInput);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Сервис", serviceStatus(), Ui.CYAN);
        addStatusCard(grid, "Vosk", voskStatus(), Ui.SUCCESS);
        addStatusCard(grid, "Команды", "climate · body · HUD · drive · DVR · nav · apps", Ui.WARNING);
        addStatusCard(grid, "Алиасы / Лог", aliases().size() + " alias · " + logCount() + " log", Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildAssistantPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Voice Flow"));
        panel.addView(Ui.text(this, "Расширенный voice-сценарий: отдельный listening entry, assistant flow, запуск приложений и прием share-location.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Listening", () -> showResultSheet("Listening", VoiceFlowRouter.launchVoiceUi(this, "manual", latestRecognition, "listening")));
        addActionChip(row, "Assistant", () -> showResultSheet("Assistant", VoiceFlowRouter.openAssistant(this)));
        addActionChip(row, "Навигация", () -> showResultSheet("Навигация", VoiceFlowRouter.openNavigation(this, latestRecognition)));
        addActionChip(row, "Запуск app", this::launchAppFromInput);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));

        panel.addView(Ui.muted(this, "Поддерживаются фразы вроде `открой навигацию`, `маршрут до дома`, `запусти yandex`, `открой браузер`."));
        return panel;
    }

    private LinearLayout buildCommandPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Команды"));
        panel.addView(Ui.text(this, "Локальный Vosk, ручной ввод команд и выполнение встроенного parser flow.", 14, false));

        commandInput = edit("Введите или продиктуйте команду", latestRecognition);
        panel.addView(commandInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Слушать", this::startListening);
        addActionChip(row, "Стоп", this::stopListening);
        addActionChip(row, "Выполнить", this::runInputCommand);
        addActionChip(row, "Очистить", () -> {
            latestRecognition = "";
            if (commandInput != null) commandInput.setText("");
            if (recognitionView != null) recognitionView.setText(recognitionSummary());
        });
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));

        LinearLayout commandList = new LinearLayout(this);
        commandList.setOrientation(LinearLayout.VERTICAL);
        commandList.addView(Ui.muted(this, experimentalFeaturesEnabled()
                ? "Поддержка: климат on/off, A/C, A/C max, температура, обдув, рециркуляция, вентилятор, окна, двери, замки, child lock, люк, шторка, багажник, 360, DVR, дворники, омыватель, свет, подогрев руля, сиденья, вентиляция сидений, HUD, базовые и experimental drive modes, custom propulsion/suspension/steering/climate."
                : "Поддержка: климат on/off, A/C, A/C max, температура, обдув, рециркуляция, вентилятор, окна, двери, замки, child lock, люк, шторка, багажник, 360, DVR, дворники, омыватель, свет, подогрев руля, сиденья, вентиляция сидений, HUD, Eco/Comfort/Dynamic/Snow/Offroad."));
        panel.addView(commandList, lpMatchWrap(0, 14, 0, 0));
        return panel;
    }

    private LinearLayout buildAliasPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Команды алиасов"));
        panel.addView(Ui.text(this, "Alias может запускать preset, scenario, action или broadcast command. Долгое нажатие — редактирование.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Добавить", () -> editAlias("", ""));
        addActionChip(row, "Проверить", this::testAliasCommand);
        addActionChip(row, "Примеры", this::installAliasExamples);
        addActionChip(row, "Обновить", this::renderContent);
        panel.addView(row, lpMatchWrap(0, 12, 0, 12));

        for (String item : aliases()) {
            String[] parts = item.split("\\|", 2);
            String phrase = parts[0];
            String action = parts.length > 1 ? parts[1] : parts[0];
            panel.addView(buildAliasCard(phrase, action), lpMatchWrap(0, 0, 0, 14));
        }
        return panel;
    }

    private LinearLayout buildAliasCard(String phrase, String action) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, phrase, 18, true));
        card.addView(Ui.muted(this, "→ " + action));

        LinearLayout row = Ui.row(this);
        addMiniAction(row, "Исп.", () -> {
            latestRecognition = phrase;
            if (commandInput != null) commandInput.setText(phrase);
            if (recognitionView != null) recognitionView.setText(recognitionSummary());
        });
        addMiniAction(row, "Тест", () -> showResultSheet("Alias test", runVoiceCommand(phrase.toLowerCase(Locale.ROOT))));
        addMiniAction(row, "Изм.", () -> editAlias(phrase, action));
        card.addView(row, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildLogPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Лог"));
        panel.addView(Ui.text(this, "Неизвестные команды идут в broadcast fallback, логируются и могут быть сохранены как alias.", 14, false));

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Очистить лог", () -> {
            prefs.edit().putString(PREF_LOG, "").apply();
            renderContent();
        });
        addActionChip(row, "Создать alias", this::createAliasFromRecognition);
        panel.addView(row, lpMatchWrap(0, 12, 0, 12));

        String[] items = voiceLog().split("\n");
        boolean hasLog = false;
        for (String item : items) {
            if (item.trim().isEmpty()) continue;
            hasLog = true;
            panel.addView(Ui.muted(this, item), lpMatchWrap(0, 4, 0, 4));
        }
        if (!hasLog) panel.addView(emptyState("Лог пока пуст"));
        return panel;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Слушать", this::startListening, listening);
        addDockButton(dock, "Выполнить", this::runInputCommand, false);
        addDockButton(dock, "Alias", () -> editAlias("", ""), false);
        addDockButton(dock, "Log", this::showLogSheet, false);
        addDockButton(dock, "Назад", this::finish, false);
        return dock;
    }

    private void startListening() {
        listening = true;
        if (recognitionView != null) recognitionView.setText("Слушаю…");
        recognizer.start(this, text -> runOnUiThread(() -> {
            latestRecognition = normalizeRecognition(text);
            if (recognitionView != null) recognitionView.setText(recognitionSummary());
            if (commandInput != null && latestRecognition.length() > 0) commandInput.setText(latestRecognition);
        }));
    }

    private void stopListening() {
        recognizer.stop();
        listening = false;
        if (recognitionView != null) recognitionView.setText(recognitionSummary());
    }

    private void runInputCommand() {
        String raw = commandInput == null ? latestRecognition : commandInput.getText().toString();
        latestRecognition = raw == null ? "" : raw.trim();
        String result = runVoiceCommand(latestRecognition.toLowerCase(Locale.ROOT));
        showResultSheet("Выполнение команды", result);
        if (recognitionView != null) recognitionView.setText(recognitionSummary());
    }

    private void focusInput() {
        if (commandInput != null) {
            commandInput.requestFocus();
            commandInput.setSelection(commandInput.getText().length());
        }
    }

    private void testAliasCommand() {
        String raw = commandInput == null ? latestRecognition : commandInput.getText().toString();
        if (raw == null || raw.trim().isEmpty()) {
            Ui.toast(this, "Сначала введите alias");
            return;
        }
        showResultSheet("Alias test", runVoiceCommand(raw.toLowerCase(Locale.ROOT)));
    }

    private void installAliasExamples() {
        LinkedHashSet<String> items = new LinkedHashSet<>(aliases());
        items.add("замёрз|зима");
        items.add("проветри|окно водитель открыть");
        items.add("паркуюсь|камера 360");
        prefs.edit().putStringSet("aliases", items).apply();
        renderContent();
    }

    private void createAliasFromRecognition() {
        String phrase = latestRecognition == null ? "" : latestRecognition.trim();
        if (phrase.isEmpty()) {
            Ui.toast(this, "Нет последней команды");
            return;
        }
        editAlias(phrase, "");
    }

    private void launchAppFromInput() {
        String raw = commandInput == null ? latestRecognition : commandInput.getText().toString();
        if (raw == null || raw.trim().isEmpty()) {
            Ui.toast(this, "Сначала введите app или навигацию");
            return;
        }
        showResultSheet("Запуск app", VoiceFlowRouter.launchByToken(this, raw));
    }

    private void editAlias(String oldPhrase, String oldAction) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 18);
        box.setPadding(p, p, p, 0);

        EditText phrase = edit("Фраза", oldPhrase);
        EditText action = edit("preset / scenario / action / broadcast command", oldAction);
        box.addView(phrase);
        box.addView(action);

        new AlertDialog.Builder(this)
                .setView(box)
                .setPositiveButton("Сохранить", (d, w) -> {
                    LinkedHashSet<String> items = new LinkedHashSet<>(aliases());
                    if (!oldPhrase.isEmpty()) items.remove(oldPhrase + "|" + oldAction);
                    items.add(phrase.getText().toString().trim() + "|" + action.getText().toString().trim());
                    prefs.edit().putStringSet("aliases", items).apply();
                    renderContent();
                })
                .setNegativeButton("Удалить", (d, w) -> {
                    LinkedHashSet<String> items = new LinkedHashSet<>(aliases());
                    items.remove(oldPhrase + "|" + oldAction);
                    prefs.edit().putStringSet("aliases", items).apply();
                    renderContent();
                })
                .show();
    }

    private String runVoiceCommand(String cmd) {
        if (cmd.trim().isEmpty()) return "Пустая команда";
        String initial = cmd.trim().toLowerCase(Locale.ROOT);
        String alias = aliasFor(initial);
        boolean aliasHit = alias != null && !alias.equals(initial);
        String resolved = aliasHit ? alias.toLowerCase(Locale.ROOT) : initial;
        AutomationEngine.runTrigger(this, "voice", resolved);

        EcarxVehicleAdapter.Result[] preset = parsePreset(resolved);
        if (preset != null) {
            String result = describePreset(resolved, preset);
            appendVoiceLog("preset", initial, result);
            return result;
        }

        EcarxVehicleAdapter.Result result = parseVehicleCommand(resolved);
        if (result != null) {
            appendVoiceLog(aliasHit ? "alias->vehicle" : "vehicle", initial, result.message);
            return result.message;
        }

        CarCommandBus.send(this, "voice", resolved);
        String fallback = "Broadcast fallback: " + resolved;
        appendVoiceLog(aliasHit ? "alias->broadcast" : "broadcast", initial, fallback);
        return fallback + "\nКоманда сохранена в лог. При необходимости создайте alias.";
    }

    private Set<String> aliases() {
        LinkedHashSet<String> defaults = new LinkedHashSet<>();
        defaults.add("включи климат|климат включить");
        defaults.add("охлади салон|охлаждение");
        defaults.add("зимний режим|зима");
        defaults.add("открой камеры|камера 360");
        defaults.add("включи подогрев руля|подогрев руля");
        return prefs.getStringSet("aliases", defaults);
    }

    private String aliasFor(String cmd) {
        String normalized = cmd.trim().toLowerCase(Locale.ROOT);
        for (String item : aliases()) {
            String[] parts = item.split("\\|", 2);
            if (parts.length == 2 && normalized.equals(parts[0].trim().toLowerCase(Locale.ROOT))) return parts[1];
        }
        return null;
    }

    private void appendVoiceLog(String kind, String command, String message) {
        ArrayList<String> lines = new ArrayList<>();
        for (String item : voiceLog().split("\n")) {
            if (!item.trim().isEmpty()) lines.add(item);
        }
        lines.add(0, kind + " | " + command + " | " + oneLine(message));
        while (lines.size() > LOG_LIMIT) lines.remove(lines.size() - 1);
        prefs.edit().putString(PREF_LOG, join(lines)).apply();
    }

    private String voiceLog() {
        return prefs.getString(PREF_LOG, "");
    }

    private int logCount() {
        int count = 0;
        for (String item : voiceLog().split("\n")) if (!item.trim().isEmpty()) count++;
        return count;
    }

    private void showLogSheet() {
        showResultSheet("Voice log", voiceLog().trim().isEmpty() ? "Лог пуст" : voiceLog());
    }

    private String serviceStatus() {
        return "Foreground active";
    }

    private String voskStatus() {
        return listening ? "Listening" : "Ready";
    }

    private String sourceSummary() {
        String source = getIntent().getStringExtra("source");
        String event = getIntent().getStringExtra("event");
        if (source == null && event == null) return "manual";
        return (source == null ? "unknown" : source) + " · " + (event == null ? "" : event);
    }

    private String recognitionSummary() {
        if (latestRecognition == null || latestRecognition.trim().isEmpty()) return listening ? "Слушаю…" : "Команда не распознана";
        return latestRecognition;
    }

    private String normalizeRecognition(String text) {
        if (text == null) return "";
        String clean = text.trim();
        if (clean.startsWith("{") && clean.endsWith("}")) return clean;
        return clean.replace('\n', ' ').trim();
    }

    private String oneLine(String value) {
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    private EcarxVehicleAdapter.Result simpleResult(String message) {
        return new EcarxVehicleAdapter.Result(true, message);
    }

    private String stripCommandPrefix(String cmd, String... prefixes) {
        String value = cmd == null ? "" : cmd.trim();
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String prefix : prefixes) {
            String p = prefix.toLowerCase(Locale.ROOT);
            if (normalized.startsWith(p)) return value.substring(Math.min(value.length(), p.length())).trim();
        }
        return value;
    }

    private String extractAfterKeyword(String cmd, String... markers) {
        String normalized = cmd == null ? "" : cmd.toLowerCase(Locale.ROOT);
        for (String marker : markers) {
            int index = normalized.indexOf(marker);
            if (index >= 0) return cmd.substring(index + marker.length()).trim();
        }
        return "";
    }

    private String join(Iterable<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(value);
        }
        return sb.toString();
    }

    private void showResultSheet(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, title, 22, true));
        sheet.addView(Ui.muted(this, body == null || body.trim().isEmpty() ? "Нет данных" : body));
        scroll.addView(sheet);
        builder.setView(scroll);
        builder.setPositiveButton("Закрыть", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 22), Color.TRANSPARENT));
        });
        dialog.show();
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

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextSize(13);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
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

    private EditText edit(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private EcarxVehicleAdapter.Result parseVehicleCommand(String cmd) {
        EcarxVehicleAdapter.Result temperature = parseTemperatureCommand(cmd);
        if (temperature != null) return temperature;

        if ((has(cmd, "ассист") || has(cmd, "assistant")) && (has(cmd, "откр") || has(cmd, "open") || has(cmd, "запу"))) {
            return simpleResult(VoiceFlowRouter.openAssistant(this));
        }
        if ((has(cmd, "нави") || has(cmd, "maps")) && (has(cmd, "откр") || has(cmd, "open") || has(cmd, "запу"))) {
            return simpleResult(VoiceFlowRouter.openNavigation(this, extractAfterKeyword(cmd, "в ", "to ", "до ")));
        }
        if (has(cmd, "маршрут") || has(cmd, "navigate to") || has(cmd, "route to")) {
            return simpleResult(VoiceFlowRouter.openNavigation(this, stripCommandPrefix(cmd,
                    "маршрут", "navigate to", "route to", "до", "в")));
        }
        if (has(cmd, "запусти") || has(cmd, "открой приложение") || has(cmd, "open app") || has(cmd, "launch")) {
            return simpleResult(VoiceFlowRouter.launchByToken(this, stripCommandPrefix(cmd,
                    "запусти", "открой приложение", "open app", "launch", "открой")));
        }

        if (has(cmd, "климат") && off(cmd)) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        }
        if (has(cmd, "климат")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        }
        if ((has(cmd, "кондиционер") || has(cmd, "a/c") || has(cmd, " ac ")) && off(cmd)) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        }
        if ((has(cmd, "макс") || has(cmd, "max")) && (has(cmd, "конди") || has(cmd, "a/c") || has(cmd, " ac "))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "кондиционер") || has(cmd, "a/c") || has(cmd, " ac ")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "лобов") || has(cmd, "обдув")) {
            if (has(cmd, "лицо") && has(cmd, "ног")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG);
            if (has(cmd, "лицо") && (has(cmd, "стек") || has(cmd, "лоб"))) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW);
            if (has(cmd, "ног") && (has(cmd, "стек") || has(cmd, "лоб"))) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW);
            if (has(cmd, "лицо")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE);
            if (has(cmd, "ног")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG);
            if (has(cmd, "все")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL);
            if (has(cmd, "auto") || has(cmd, "авто")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_AUTO);
            if (has(cmd, "макс") || has(cmd, "max")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON);
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "задн") && has(cmd, "стек")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "рециркуляц") && (has(cmd, "внутр") || has(cmd, "inner"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER);
        }
        if (has(cmd, "рециркуляц")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE);
        }
        if (has(cmd, "вентилятор") || has(cmd, "fan")) {
            if (has(cmd, "auto") || has(cmd, "авто")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO);
            if (has(cmd, "9")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_9);
            if (has(cmd, "8")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_8);
            if (has(cmd, "7")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_7);
            if (has(cmd, "6")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_6);
            if (has(cmd, "5")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
            if (has(cmd, "4")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_4);
            if (has(cmd, "3")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
            if (has(cmd, "2")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_2);
            if (has(cmd, "1")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        }
        if (has(cmd, "зона") && (has(cmd, "dual") || has(cmd, "дв"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL);
        }
        if (has(cmd, "зона") && (has(cmd, "single") || has(cmd, "одн"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE);
        }
        if (has(cmd, "окн") && (has(cmd, "откр") || has(cmd, "open"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WINDOW, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_ALL), EcarxVehicleAdapter.WINDOW_OPEN);
        }
        if (has(cmd, "окн") && (has(cmd, "закр") || has(cmd, "close"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WINDOW, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_ALL), EcarxVehicleAdapter.WINDOW_CLOSE);
        }
        if (has(cmd, "двер") && (has(cmd, "откр") || has(cmd, "open"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_DOOR, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_ALL), EcarxVehicleAdapter.DOOR_OPEN);
        }
        if (has(cmd, "двер") && (has(cmd, "закр") || has(cmd, "close"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_DOOR, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_ALL), EcarxVehicleAdapter.DOOR_CLOSE);
        }
        if (has(cmd, "зам") && has(cmd, "двер")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_DOOR_LOCK, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "дет") && (has(cmd, "зам") || has(cmd, "lock"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "люк") && has(cmd, "откр")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "люк") && has(cmd, "закр")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "штор") && has(cmd, "откр")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "штор") && has(cmd, "закр")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "багаж")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        }
        if (has(cmd, "360") || has(cmd, "камера")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        }
        if (has(cmd, "dvr") || has(cmd, "регистратор")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR);
        }
        if (has(cmd, "дворник") || has(cmd, "wiper")) {
            if (off(cmd)) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_OFF);
            if (has(cmd, "авто") || has(cmd, "auto")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_AUTO);
            if (has(cmd, "быстр") || has(cmd, "high")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_HIGH);
            if (has(cmd, "прерыв") || has(cmd, "intermittent")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_INTERMITTENT);
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_LOW);
        }
        if (has(cmd, "омыв")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "аварий")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_LIGHT_HAZARD, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "ближн") && has(cmd, "свет")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "дальн") && has(cmd, "свет")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if ((has(cmd, "птф") || has(cmd, "туман")) && has(cmd, "зад")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "птф") || has(cmd, "туман")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "салон") && has(cmd, "свет")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        if (has(cmd, "руль") && has(cmd, "подогрев")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.WHEEL_HEAT_MID);
        }
        if (has(cmd, "сиден") && has(cmd, "подогрев")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_SEAT_HEATING, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_DRIVER_LEFT), off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.SEAT_LEVEL_2);
        }
        if (has(cmd, "сиден") && has(cmd, "вент")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_DRIVER_LEFT), off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.SEAT_LEVEL_2);
        }
        if (has(cmd, "hud") || has(cmd, "проектор")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HUD_ACTIVE, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON);
        }
        EcarxVehicleAdapter.Result advancedDrive = parseAdvancedDriveCommand(cmd);
        if (advancedDrive != null) return advancedDrive;
        if (has(cmd, "eco") || has(cmd, "эко")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO);
        }
        if (has(cmd, "comfort") || has(cmd, "комфорт")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT);
        }
        if (has(cmd, "dynamic") || has(cmd, "sport") || has(cmd, "динами")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC);
        }
        if (has(cmd, "snow") || has(cmd, "снег")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW);
        }
        if (has(cmd, "offroad") || has(cmd, "оффро") || has(cmd, "бездорож")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD);
        }
        return null;
    }

    private EcarxVehicleAdapter.Result parseAdvancedDriveCommand(String cmd) {
        if (!experimentalFeaturesEnabled()) return null;
        if (has(cmd, "pure") || has(cmd, "пьюр")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PURE);
        if (has(cmd, "hybrid") || has(cmd, "гибрид")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HYBRID);
        if (has(cmd, "power") || has(cmd, "пауэр")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_POWER);
        if (has(cmd, "mud") || has(cmd, "гряз")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_MUD);
        if (has(cmd, "rock") || has(cmd, "камн")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ROCK);
        if (has(cmd, "sand") || has(cmd, "пес")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAND);
        if (has(cmd, "save") || has(cmd, "battery save") || has(cmd, "сейв")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SAVE);
        if (has(cmd, "adaptive") || has(cmd, "адаптив")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE);
        if (has(cmd, "custom mode") || has(cmd, "drive custom") || has(cmd, "кастом режим")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM);
        if (has(cmd, "awd") || has(cmd, "полный привод")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_AWD);
        if (has(cmd, "eawd")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_EAWD);
        if (has(cmd, "hdc")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_HDC);
        if (has(cmd, "phev")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_PHEV);
        if (has(cmd, "eco plus")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS);
        if (has(cmd, "sport plus")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS);
        if (has(cmd, "custom propulsion") || has(cmd, "propulsion")) return parseCustomPropulsion(cmd);
        if (has(cmd, "custom suspension") || has(cmd, "suspension")) return parseCustomSuspension(cmd);
        if (has(cmd, "steering feel")) return parseCustomSteering(cmd);
        if (has(cmd, "drive climate")) return parseCustomClimate(cmd);
        return null;
    }

    private EcarxVehicleAdapter.Result parseCustomPropulsion(String cmd) {
        if (has(cmd, "off")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.COMMON_OFF);
        if (has(cmd, "eco")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO);
        if (has(cmd, "comfort") || has(cmd, "комфорт")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT);
        if (has(cmd, "sport") || has(cmd, "dynamic") || has(cmd, "спорт")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_SPORT);
        if (has(cmd, "offroad") || has(cmd, "бездорож")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD);
        if (has(cmd, "snow") || has(cmd, "снег")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_SNOW);
        if (has(cmd, "sand") || has(cmd, "пес")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND);
        if (has(cmd, "hybrid") || has(cmd, "гибрид")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID);
        if (has(cmd, "pure") || has(cmd, "пьюр")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE);
        if (has(cmd, "power") || has(cmd, "пауэр")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_POWER);
        if (has(cmd, "awd")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD);
        return null;
    }

    private EcarxVehicleAdapter.Result parseCustomSuspension(String cmd) {
        if (has(cmd, "off")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.COMMON_OFF);
        if (has(cmd, "standard") || has(cmd, "normal") || has(cmd, "стандарт")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_STANDARD);
        if (has(cmd, "comfort") || has(cmd, "комфорт")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT);
        if (has(cmd, "sport") || has(cmd, "спорт")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT);
        if (has(cmd, "offroad") || has(cmd, "бездорож")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD);
        if (has(cmd, "snow") || has(cmd, "снег")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SNOW);
        if (has(cmd, "auto") || has(cmd, "авто")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.CUSTOM_SUSPENSION_AUTOMATIC);
        return null;
    }

    private EcarxVehicleAdapter.Result parseCustomSteering(String cmd) {
        if (has(cmd, "off")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.COMMON_OFF);
        if (has(cmd, "light") || has(cmd, "легк")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT);
        if (has(cmd, "balanced") || has(cmd, "normal") || has(cmd, "сбаланс")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.CUSTOM_STEERING_BALANCED);
        if (has(cmd, "heavy") || has(cmd, "тяж")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY);
        return null;
    }

    private EcarxVehicleAdapter.Result parseCustomClimate(String cmd) {
        if (has(cmd, "off")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.COMMON_OFF);
        if (has(cmd, "eco") || has(cmd, "эко")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO);
        if (has(cmd, "normal") || has(cmd, "standard") || has(cmd, "обыч")) return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL);
        return null;
    }

    private EcarxVehicleAdapter.Result parseTemperatureCommand(String cmd) {
        if (!isTemperatureCommand(cmd)) return null;
        Float value = temperatureValue(cmd);
        if (value == null) return null;
        if (value < 16.0f || value > 32.0f) {
            return EcarxVehicleAdapter.Result.status(EcarxVehicleAdapter.HVAC_TEMP, zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_DRIVER_LEFT),
                    String.format(Locale.US, "temperature %.1f outside expected HVAC range 16.0..32.0", value));
        }
        int zone = zoneFromCommand(cmd, EcarxVehicleAdapter.ZONE_DRIVER_LEFT);
        return new EcarxVehicleAdapter(this).setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone, value);
    }

    private boolean isTemperatureCommand(String cmd) {
        return has(cmd, "температур") || has(cmd, "градус") || has(cmd, "temp") || has(cmd, "temperature");
    }

    private Float temperatureValue(String cmd) {
        Matcher matcher = Pattern.compile("(\\d{2})(?:[\\.,](\\d))?").matcher(cmd);
        if (!matcher.find()) return null;
        String raw = matcher.group(1) + (matcher.group(2) == null ? "" : "." + matcher.group(2));
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private EcarxVehicleAdapter.Result[] parsePreset(String cmd) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
        if ((has(cmd, "пресет") || has(cmd, "климат")) && (has(cmd, "комфорт") || has(cmd, "comfort"))) {
            return adapter.setAll(
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE));
        }
        if (has(cmd, "зима") || has(cmd, "winter")) {
            return adapter.setAll(
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        }
        if (has(cmd, "охлаж") || has(cmd, "cool")) {
            return adapter.setAll(
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2));
        }
        return null;
    }

    private String describePreset(String cmd, EcarxVehicleAdapter.Result[] results) {
        StringBuilder sb = new StringBuilder("Пресет: ").append(cmd).append("\n");
        for (EcarxVehicleAdapter.Result item : results) sb.append(item.message).append("\n");
        return sb.toString();
    }

    private boolean off(String cmd) {
        return has(cmd, "выкл") || has(cmd, "off") || has(cmd, "отключ");
    }

    private int zoneFromCommand(String cmd, int fallback) {
        if (has(cmd, "все") || has(cmd, "all")) return EcarxVehicleAdapter.ZONE_ALL;
        if (has(cmd, "задн") && (has(cmd, "лев") || has(cmd, "left"))) return EcarxVehicleAdapter.ZONE_ROW_2_LEFT;
        if (has(cmd, "задн") && (has(cmd, "прав") || has(cmd, "right"))) return EcarxVehicleAdapter.ZONE_ROW_2_RIGHT;
        if (has(cmd, "пассаж") || has(cmd, "прав") || has(cmd, "right")) return EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT;
        if (has(cmd, "водител") || has(cmd, "лев") || has(cmd, "left")) return EcarxVehicleAdapter.ZONE_DRIVER_LEFT;
        return fallback;
    }

    private boolean has(String cmd, String value) {
        return cmd.contains(value);
    }

    private boolean experimentalFeaturesEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }
}
