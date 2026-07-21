package com.prodject.gcontrol;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String CLIMATE_PRESETS = "climate_presets";
    private static final String CLIMATE_PRESET_ORDER = "order";
    private static final String[] RUNTIME_PERMS = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 23) requestPermissions(RUNTIME_PERMS, 10);
        showOnboarding();
    }

    private void showOnboarding() {
        LinearLayout root = Ui.root(this, "GControl");
        root.addView(Ui.text(this, "Автомобильный центр управления для Android 11+: файлы, камеры, голос, климат, ADAS, HUD, рабочий стол, ADB и системные функции.", 16, false));
        Button start = Ui.button(this, "Принять и открыть приложение");
        Button legal = Ui.button(this, "Лицензия и юридические документы");
        root.addView(legal);
        root.addView(start);
        legal.setOnClickListener(v -> showLegal());
        start.setOnClickListener(v -> showDashboard());
        setContentView(root);
    }

    private void showLegal() {
        LinearLayout root = Ui.root(this, "Документы");
        root.addView(Ui.text(this, getString(com.prodject.gcontrol.R.string.legal_text), 16, false));
        Button ok = Ui.button(this, "Назад");
        ok.setOnClickListener(v -> showOnboarding());
        root.addView(ok);
        setContentView(root);
    }

    private void showDashboard() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, "GControl");
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(getResources().getConfiguration().screenWidthDp >= 700 ? 3 : 2);
        root.addView(grid);
        add(grid, "Файлы", () -> startActivity(new Intent(this, FileManagerActivity.class)));
        add(grid, "Медиа", () -> startActivity(new Intent(this, MediaViewerActivity.class)));
        add(grid, "Текст", () -> startActivity(new Intent(this, TextViewerActivity.class)));
        add(grid, "Split", this::openSplitLauncher);
        add(grid, "DVR / Камеры", () -> startActivity(new Intent(this, DvrActivity.class)));
        add(grid, "Голос", () -> startActivity(new Intent(this, VoiceActivity.class)));
        add(grid, "Автомобиль", this::showCar);
        add(grid, "Климат", this::showClimate);
        add(grid, "ADAS", this::showAdas);
        add(grid, "Парковка / APA", this::showParkingApa);
        add(grid, "Автоматизация", this::showAutomation);
        add(grid, "Кнопки руля", this::showSteeringButtons);
        add(grid, "Профили", this::showUserProfiles);
        add(grid, "Умный климат", this::showSmartClimate);
        if (experimentalFeaturesEnabled()) {
            add(grid, "PAS / AVM", this::showPasAvm);
            add(grid, "AVAS / Digital Key", this::showAvasDigitalKey);
            add(grid, "Сценарии", this::showSceneModes);
            add(grid, "Подсветка", this::showAmbienceLight);
            add(grid, "Яркость / DayMode", this::showDayMode);
        }
        add(grid, "HUD / OneOS", this::showHud);
        add(grid, "Браузер / Погода", this::showWeb);
        add(grid, "Рабочий стол", this::showLauncher);
        add(grid, "Настройки", this::showSettings);
        add(grid, "ADB / Система", () -> startActivity(new Intent(this, AdbShellActivity.class)));
        scroll.addView(root);
        setContentView(scroll);
    }

    private void add(GridLayout grid, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setMinHeight(Ui.dp(this, 72));
        b.setOnClickListener(v -> action.run());
        grid.addView(b, new ViewGroup.LayoutParams(getResources().getConfiguration().screenWidthDp >= 700 ? Ui.dp(this, 220) : Ui.dp(this, 165), ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void panel(String title, String body) {
        LinearLayout root = Ui.root(this, title);
        root.addView(Ui.text(this, body, 16, false));
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> showDashboard());
        root.addView(back);
        setContentView(root);
    }

    private void showSettings() {
        LinearLayout root = Ui.root(this, "Настройки");
        root.addView(Ui.text(this, "Экспериментальные функции скрыты по умолчанию. Включай их только для проверки на конкретной прошивке автомобиля.", 14, false));
        CheckBox experimental = new CheckBox(this);
        experimental.setText("Experimental features");
        experimental.setTextSize(16);
        experimental.setChecked(experimentalFeaturesEnabled());
        experimental.setOnCheckedChangeListener((button, checked) -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putBoolean(KEY_EXPERIMENTAL_FEATURES, checked)
                    .apply();
            Ui.toast(this, checked ? "Experimental features включены" : "Experimental features выключены");
        });
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> showDashboard());
        root.addView(experimental);
        root.addView(back);
        setContentView(root);
    }

    private boolean experimentalFeaturesEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private LinearLayout commandRoot(String title) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, title);
        root.addView(Ui.text(this, new EcarxVehicleAdapter(this).availability(), 14, false));
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> showDashboard());
        root.addView(back);
        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = Ui.button(this, label + " · " + EcarxVehicleAdapter.hex(functionId) + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    private void addCommand(LinearLayout root, String label, int functionId, int zone, int value) {
        Button b = Ui.button(this, label + " · " + EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    private void addZoneCommands(LinearLayout root, String label, int functionId, int value, int... zones) {
        for (int zone : zones) addCommand(root, label + " · " + zoneLabel(zone), functionId, zone, value);
    }

    private void addZoneDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        Button b = Ui.button(this, "Диагностика зон: " + label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(zoneLabel(zone)).append(": ")
                        .append(adapter.support(functionId, zone).message).append("\n")
                        .append(adapter.get(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void addCommandGroup(LinearLayout root, String title, int functionId, String[] labels, int[] values) {
        root.addView(Ui.text(this, title, 14, true));
        for (int i = 0; i < labels.length; i++) addCommand(root, labels[i], functionId, values[i]);
    }

    private void addSignalCommand(LinearLayout root, String label, String methodName, int signalId, int value) {
        Button b = Ui.button(this, label + " · " + CarSignalManagerAdapter.hex(signalId) + "=" + CarSignalManagerAdapter.hex(value));
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        Button b = Ui.button(this, "Диагностика raw: " + label);
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int i = 0; i + 1 < methodSignalPairs.length; i += 2) {
                String method = (String) methodSignalPairs[i];
                int signalId = (Integer) methodSignalPairs[i + 1];
                sb.append(adapter.get(method, signalId).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void addHalPropertyDiagnostic(LinearLayout root, String label, int... propertyIds) {
        Button b = Ui.button(this, "HAL свойства: " + label);
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int propertyId : propertyIds) {
                sb.append(adapter.rawHalProperty(propertyId, "VehiclePropertyVEH2").message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private String zoneLabel(int zone) {
        if (zone == EcarxVehicleAdapter.ZONE_ALL) return "все зоны";
        if (zone == EcarxVehicleAdapter.ZONE_DRIVER_LEFT) return "водитель/1L";
        if (zone == EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT) return "пассажир/1R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_LEFT) return "2L";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_RIGHT) return "2R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_1_ALL) return "1 ряд";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_ALL) return "2 ряд";
        return "zone=" + zone;
    }

    private void addFloatCommand(LinearLayout root, String label, int functionId, int zone, float value) {
        Button b = Ui.button(this, label + " · " + EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + value);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).setFloat(functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    private void addFloatDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        Button b = Ui.button(this, "Float диагностика: " + label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(adapter.support(functionId, zone).message).append("\n");
                sb.append(adapter.getFloat(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void addPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
            StringBuilder sb = new StringBuilder(label).append("\n");
            boolean ok = true;
            for (EcarxVehicleAdapter.Result r : results) {
                ok &= r.success;
                sb.append(r.message).append("\n");
            }
            Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void addSavedClimatePreset(LinearLayout root, String label, EcarxVehicleAdapter.Command[] commands) {
        Button b = Ui.button(this, "Сохраненный: " + label);
        b.setOnClickListener(v -> runPreset(root, label, commands));
        b.setOnLongClickListener(v -> {
            String[] actions = {"Редактировать", "Удалить"};
            new AlertDialog.Builder(this).setTitle(label).setItems(actions, (d, which) -> {
                if (which == 0) showClimatePresetEditor(label, encodeCommands(commands));
                else {
                    deleteClimatePreset(label);
                    showClimate();
                }
            }).show();
            return true;
        });
        root.addView(b);
    }

    private void runPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        StringBuilder sb = new StringBuilder(label).append("\n");
        boolean ok = true;
        for (EcarxVehicleAdapter.Result r : results) {
            ok &= r.success;
            sb.append(r.message).append("\n");
        }
        Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
        root.addView(Ui.text(this, sb.toString(), 13, false), 2);
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        Button b = Ui.button(this, "Диагностика: " + label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int functionId : functionIds) {
                EcarxVehicleAdapter.Result support = adapter.support(functionId);
                EcarxVehicleAdapter.Result value = adapter.get(functionId);
                sb.append(support.message).append("\n").append(value.message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void showDvr() {
        startForegroundService(new Intent(this, DvrService.class));
        panel("Monji DVR", "Запись со штатных камер: передняя ADAS, левая, задняя, правая. Настройки: выбор камер, длина сегмента, лимит диска, внутренняя память или USB. Сервис DVR запущен.");
    }

    private void showAutomation() {
        LinearLayout root = commandRoot("Автоматизация");
        SharedPreferences prefs = AutomationEngine.prefs(this);
        root.addView(Ui.text(this, "Smart preset - это список команд AdaptAPI. Формат: function/zone=value, для температуры float:function/zone=value. Триггеры: boot, app, manual.", 14, false));
        Button newPreset = Ui.button(this, "Создать smart preset");
        newPreset.setOnClickListener(v -> showSmartPresetEditor("", defaultSmartPresetText()));
        root.addView(newPreset);
        Button newScenario = Ui.button(this, "Создать сценарий v2");
        newScenario.setOnClickListener(v -> showScenarioEditor("", defaultScenarioText()));
        root.addView(newScenario);
        Button climateScenarios = Ui.button(this, "Добавить Зимний запуск / Летнее охлаждение v2");
        climateScenarios.setOnClickListener(v -> {
            installClimateScenarioV2();
            showAutomation();
        });
        root.addView(climateScenarios);
        Button welcomeLeave = Ui.button(this, "Добавить шаблоны Welcome / Leave");
        welcomeLeave.setOnClickListener(v -> {
            installWelcomeLeaveScenarios();
            showAutomation();
        });
        root.addView(welcomeLeave);
        Button parkingGuard = Ui.button(this, "Добавить шаблон Parking Guard");
        parkingGuard.setOnClickListener(v -> {
            installParkingGuardScenario();
            showAutomation();
        });
        root.addView(parkingGuard);
        Button rainScenario = Ui.button(this, "Добавить шаблон Rain Scenario");
        rainScenario.setOnClickListener(v -> {
            installRainScenario();
            showAutomation();
        });
        root.addView(rainScenario);
        Button nightMode = Ui.button(this, "Добавить шаблон Night Mode");
        nightMode.setOnClickListener(v -> {
            installNightModeScenario();
            showAutomation();
        });
        root.addView(nightMode);
        Button appContext = Ui.button(this, "Добавить шаблон App Context: Navigation");
        appContext.setOnClickListener(v -> {
            installNavigationContextScenario();
            showAutomation();
        });
        root.addView(appContext);
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_PRESET_ORDER)) {
            Button b = Ui.button(this, "Preset: " + name);
            b.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runPreset(this, name), 13, false), 2));
            b.setOnLongClickListener(v -> {
                showSmartPresetEditor(name, prefs.getString("preset:" + name, ""));
                return true;
            });
            root.addView(b);
        }
        root.addView(Ui.text(this, "Сценарии v2", 16, true));
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_SCENARIO_ORDER)) {
            Button b = Ui.button(this, "Scenario: " + name);
            b.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runScenario(this, name, "manual", "ui"), 13, false), 2));
            b.setOnLongClickListener(v -> {
                showScenarioEditor(name, prefs.getString("scenario:" + name, ""));
                return true;
            });
            root.addView(b);
        }
        root.addView(Ui.text(this, "Триггеры запуска", 16, true));
        Button newTrigger = Ui.button(this, "Добавить триггер");
        newTrigger.setOnClickListener(v -> showTriggerEditor("", "manual", "", firstAutomationPreset()));
        root.addView(newTrigger);
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_TRIGGER_ORDER)) {
            String raw = prefs.getString("trigger:" + name, "");
            Button b = Ui.button(this, "Trigger: " + raw);
            b.setOnLongClickListener(v -> {
                String[] p = raw.split("\\|", -1);
                showTriggerEditor(name, p.length > 1 ? p[1] : "manual", p.length > 2 ? p[2] : "", p.length > 3 ? p[3] : "");
                return true;
            });
            root.addView(b);
        }
        root.addView(Ui.text(this, automationIdeas(), 14, false));
        Button log = Ui.button(this, "Журнал выполнения");
        log.setOnClickListener(v -> panel("Журнал автоматизации", AutomationEngine.scenarioLog(this)));
        root.addView(log);
    }

    private void showSmartPresetEditor(String oldName, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый smart preset" : "Preset: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText body = new EditText(this);
        body.setMinLines(8);
        body.setGravity(Gravity.TOP);
        body.setHint(defaultSmartPresetText());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить");
        Button delete = Ui.button(this, "Удалить");
        save.setOnClickListener(v -> {
            saveAutomationPreset(oldName, name.getText().toString(), body.getText().toString());
            showAutomation();
        });
        delete.setOnClickListener(v -> {
            deleteAutomationItem(AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName);
            showAutomation();
        });
        root.addView(name);
        root.addView(body);
        root.addView(save);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showScenarioEditor(String oldName, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый сценарий v2" : "Сценарий v2: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText body = new EditText(this);
        body.setMinLines(14);
        body.setGravity(Gravity.TOP);
        body.setHint(defaultScenarioText());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить сценарий");
        save.setOnClickListener(v -> {
            String clean = name.getText().toString().trim();
            String text = body.getText().toString();
            if (!text.contains("name:")) text = "name:" + clean + "\n" + text;
            saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName, clean, text);
            showAutomation();
        });
        Button run = Ui.button(this, "Запустить сейчас");
        run.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runScenario(this, name.getText().toString().trim(), "manual", "ui"), 13, false), 2));
        Button delete = Ui.button(this, "Удалить");
        delete.setOnClickListener(v -> {
            deleteAutomationItem(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName);
            showAutomation();
        });
        root.addView(Ui.text(this, "Формат: trigger:manual=winter, trigger:app=maps, trigger:voice=зима, trigger:button=231:hold; condition:key=value; policy:startDelay=30s; policy:minInterval=30m; step:delay 5m; step:wait cabinTemp<=25 timeout=10m; step:command 0x.../zone=value; step:action smart_climate=true.", 13, false));
        root.addView(name);
        root.addView(body);
        root.addView(save);
        root.addView(run);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showTriggerEditor(String oldName, String oldType, String oldMatch, String oldPreset) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый триггер" : "Триггер: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText type = new EditText(this);
        type.setHint("manual / boot / app");
        type.setText(oldType);
        EditText match = new EditText(this);
        match.setHint("Условие: action/package substring; для boot можно пусто");
        match.setText(oldMatch);
        EditText preset = new EditText(this);
        preset.setHint("Название smart preset");
        preset.setText(oldPreset);
        Button save = Ui.button(this, "Сохранить триггер");
        save.setOnClickListener(v -> {
            saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + type.getText().toString().trim() + "|" + match.getText().toString().trim() + "|" + preset.getText().toString().trim());
            showAutomation();
        });
        Button runManual = Ui.button(this, "Проверить как manual");
        runManual.setOnClickListener(v -> AutomationEngine.runTrigger(this, "manual", match.getText().toString()));
        root.addView(name);
        root.addView(type);
        root.addView(match);
        root.addView(preset);
        root.addView(save);
        root.addView(runManual);
    }

    private void showSteeringButtons() {
        LinearLayout root = commandRoot("Кнопки руля");
        SharedPreferences steering = getSharedPreferences("steering", MODE_PRIVATE);
        SharedPreferences prefs = AutomationEngine.prefs(this);
        root.addView(Ui.text(this, "Последнее событие: " + steering.getString("last_event", "нет") + "\nЖесты: press, double, triple, hold. Поведение: replace, together, hold-only, stationary-only. Условия: always, stationary, moving, app=package, profile=Имя, cabinTemp<25.", 14, false));
        Button add = Ui.button(this, "Назначить кнопку");
        add.setOnClickListener(v -> showSteeringButtonEditor("", "0", "hold", "", "always", "replace", "preset", firstAutomationPreset()));
        root.addView(add);
        Button examples = Ui.button(this, "Добавить примеры назначений");
        examples.setOnClickListener(v -> {
            installSteeringButtonExamples();
            showSteeringButtons();
        });
        root.addView(examples);
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_BUTTON_ORDER)) {
            String raw = prefs.getString("button2:" + name, prefs.getString("button:" + name, ""));
            Button b = Ui.button(this, "Button: " + raw);
            b.setOnLongClickListener(v -> {
                String[] p = raw.split("\\|", -1);
                if (p.length >= 8) showSteeringButtonEditor(name, p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                else showSteeringButtonEditor(name, p.length > 1 ? p[1] : "0", p.length > 2 ? p[2] : "hold", "", "always", "replace", "preset", p.length > 3 ? p[3] : "");
                return true;
            });
            root.addView(b);
        }
    }

    private void showSteeringButtonEditor(String oldName, String oldKey, String oldGesture, String oldModifier, String oldCondition, String oldBehavior, String oldTargetType, String oldTarget) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новое назначение" : "Назначение: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText key = new EditText(this);
        key.setHint("keyCode из последнего события");
        key.setText(oldKey);
        EditText gesture = new EditText(this);
        gesture.setHint("press / double / triple / hold");
        gesture.setText(oldGesture);
        EditText modifier = new EditText(this);
        modifier.setHint("Другая удерживаемая кнопка, пусто если не нужно");
        modifier.setText(oldModifier);
        EditText condition = new EditText(this);
        condition.setHint("always / stationary / moving / app=maps / profile=Глеб");
        condition.setText(oldCondition);
        EditText behavior = new EditText(this);
        behavior.setHint("replace / together / hold-only / stationary-only");
        behavior.setText(oldBehavior);
        EditText targetType = new EditText(this);
        targetType.setHint("preset / scenario / action / voice / launch / command");
        targetType.setText(oldTargetType);
        EditText target = new EditText(this);
        target.setHint("Цель: имя preset/scenario или action/команда");
        target.setText(oldTarget);
        Button save = Ui.button(this, "Сохранить назначение");
        save.setOnClickListener(v -> {
            saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim() + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT) + "|" + target.getText().toString().trim());
            saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim()
                            + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT)
                            + "|" + modifier.getText().toString().trim()
                            + "|" + condition.getText().toString().trim()
                            + "|" + behavior.getText().toString().trim()
                            + "|" + targetType.getText().toString().trim()
                            + "|" + target.getText().toString().trim());
            showSteeringButtons();
        });
        root.addView(name);
        root.addView(key);
        root.addView(gesture);
        root.addView(modifier);
        root.addView(condition);
        root.addView(behavior);
        root.addView(targetType);
        root.addView(target);
        root.addView(save);
    }

    private void installSteeringButtonExamples() {
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M hold 360", "M hold 360|77|hold||always|replace|command|0x21110100/0=0x1");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M double cooling", "M double cooling|77|double||always|replace|preset|Летнее охлаждение");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Voice hold Monji", "Voice hold Monji|231|hold||always|replace|launch|com.prodject.gcontrol");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Volume down double mute", "Volume down double mute|25|double||always|together|voice|mute media");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Next hold eco comfort", "Next hold eco comfort|87|hold||always|replace|scenario|Eco Comfort toggle");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M stationary trunk", "M stationary trunk|77|press||stationary|stationary-only|command|0x21110100/0=0x64");
        Ui.toast(this, "Примеры кнопок руля добавлены");
    }

    private void showUserProfiles() {
        LinearLayout root = commandRoot("Профили пользователей");
        SharedPreferences prefs = UserProfileEngine.prefs(this);
        root.addView(Ui.text(this, "Активный профиль: " + AutomationEngine.prefs(this).getString(AutomationEngine.KEY_ACTIVE_PROFILE, "не выбран")
                + "\nПоследний: " + prefs.getString(UserProfileEngine.KEY_LAST_USED, "нет")
                + "\nИдентификация: Face ID / телефон / Bluetooth / цифровой ключ сохраняются как правила профиля; автоматическое применение включается после появления соответствующего системного события.", 14, false));
        Button addDriver = Ui.button(this, "Создать профиль водителя");
        addDriver.setOnClickListener(v -> showUserProfileEditor("", "driver", "manual=", UserProfileEngine.defaultDriverBody()));
        Button addPassenger = Ui.button(this, "Создать профиль пассажира");
        addPassenger.setOnClickListener(v -> showUserProfileEditor("", "passenger", "manual=", UserProfileEngine.defaultPassengerBody()));
        Button last = Ui.button(this, "Применить последний профиль");
        last.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, prefs.getString(UserProfileEngine.KEY_LAST_USED, "")), 13, false), 2));
        root.addView(addDriver);
        root.addView(addPassenger);
        root.addView(last);
        addProfileSection(root, "Водители", "driver");
        addProfileSection(root, "Пассажиры", "passenger");
    }

    private void addProfileSection(LinearLayout root, String title, String type) {
        root.addView(Ui.text(this, title, 18, true));
        for (String name : UserProfileEngine.names(this, type)) {
            String raw = UserProfileEngine.raw(this, name);
            Button b = Ui.button(this, name + " · " + type);
            b.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name), 13, false), 2));
            b.setOnLongClickListener(v -> {
                String identity = "";
                String body = "";
                for (String line : raw.split("\\n")) {
                    if (line.startsWith("identity:")) identity = line.substring("identity:".length());
                    else if (!line.startsWith("name:") && !line.startsWith("type:")) body += line + "\n";
                }
                showUserProfileEditor(name, type, identity, body);
                return true;
            });
            root.addView(b);
        }
    }

    private void showUserProfileEditor(String oldName, String oldType, String oldIdentity, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый профиль" : "Профиль: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Имя профиля");
        name.setText(oldName);
        EditText type = new EditText(this);
        type.setHint("driver / passenger");
        type.setText(oldType);
        EditText identity = new EditText(this);
        identity.setHint("manual=Глеб; phone=Pixel; bluetooth=AA:BB; face=gleb; digitalKey=id");
        identity.setText(oldIdentity);
        EditText body = new EditText(this);
        body.setMinLines(16);
        body.setGravity(Gravity.TOP);
        body.setHint(UserProfileEngine.defaultDriverBody());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить профиль");
        save.setOnClickListener(v -> {
            String result = UserProfileEngine.save(this, oldName, name.getText().toString(), type.getText().toString().trim(), identity.getText().toString(), body.getText().toString());
            Ui.toast(this, "Профиль сохранен");
            root.addView(Ui.text(this, result, 13, false), 2);
        });
        Button apply = Ui.button(this, "Применить");
        apply.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name.getText().toString().trim()), 13, false), 2));
        Button delete = Ui.button(this, "Удалить");
        delete.setOnClickListener(v -> {
            UserProfileEngine.delete(this, oldName, oldType);
            showUserProfiles();
        });
        root.addView(Ui.text(this, "Доступные строки: seatMemory, seatLength, seatHeight, seatBackrest, mirror, climateTemp, fan, seatHeat, seatVent, drive, steering, hud, brightness, ambience, volume, mediaSource, desktopPins, buttonPreset, preset, scenario, adas.", 13, false));
        root.addView(name);
        root.addView(type);
        root.addView(identity);
        root.addView(body);
        root.addView(save);
        root.addView(apply);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showSmartClimate() {
        LinearLayout root = commandRoot("Умный кондиционер");
        SharedPreferences prefs = SmartClimateController.prefs(this);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("Контроллер включен");
        enabled.setTextSize(16);
        enabled.setChecked(prefs.getBoolean(SmartClimateController.KEY_ENABLED, false));
        EditText mode = new EditText(this);
        mode.setHint("off / fast_cool / fast_heat / stabilize / maintain / dry / summer");
        mode.setText(prefs.getString(SmartClimateController.KEY_MODE, SmartClimateController.MODE_OFF));
        EditText cabin = new EditText(this);
        cabin.setHint("Fallback: температура салона");
        cabin.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_CABIN_TEMP, 26.0f)));
        EditText outside = new EditText(this);
        outside.setHint("Fallback: внешняя температура");
        outside.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_OUTSIDE_TEMP, 26.0f)));
        EditText driverTarget = new EditText(this);
        driverTarget.setHint("Цель водительской зоны");
        driverTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f)));
        EditText passengerTarget = new EditText(this);
        passengerTarget.setHint("Цель пассажирской зоны");
        passengerTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f)));
        EditText engineMinutes = new EditText(this);
        engineMinutes.setHint("Минуты работы двигателя");
        engineMinutes.setText(String.valueOf(prefs.getInt(SmartClimateController.KEY_ENGINE_MINUTES, 0)));
        CheckBox fogging = new CheckBox(this);
        fogging.setText("Запотевание стекол");
        fogging.setChecked(prefs.getBoolean(SmartClimateController.KEY_FOGGING, false));
        CheckBox call = new CheckBox(this);
        call.setText("Активный звонок");
        call.setChecked(prefs.getBoolean(SmartClimateController.KEY_CALL_ACTIVE, false));
        CheckBox dryAfterTrip = new CheckBox(this);
        dryAfterTrip.setText("Просушка после поездки");
        dryAfterTrip.setChecked(prefs.getBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, true));
        Button save = Ui.button(this, "Сохранить настройки");
        save.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean(SmartClimateController.KEY_ENABLED, enabled.isChecked())
                    .putString(SmartClimateController.KEY_MODE, mode.getText().toString().trim())
                    .putFloat(SmartClimateController.KEY_CABIN_TEMP, AutomationEngine.parseFloat(cabin.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_OUTSIDE_TEMP, AutomationEngine.parseFloat(outside.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_DRIVER_TARGET, AutomationEngine.parseFloat(driverTarget.getText().toString(), 22.0f))
                    .putFloat(SmartClimateController.KEY_PASSENGER_TARGET, AutomationEngine.parseFloat(passengerTarget.getText().toString(), 22.0f))
                    .putInt(SmartClimateController.KEY_ENGINE_MINUTES, AutomationEngine.parseInt(engineMinutes.getText().toString(), 0))
                    .putBoolean(SmartClimateController.KEY_FOGGING, fogging.isChecked())
                    .putBoolean(SmartClimateController.KEY_CALL_ACTIVE, call.isChecked())
                    .putBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, dryAfterTrip.isChecked())
                    .apply();
            Ui.toast(this, "Smart climate сохранен");
        });
        Button run = Ui.button(this, "Контроллер: шаг сейчас");
        run.setOnClickListener(v -> {
            save.performClick();
            root.addView(Ui.text(this, AutomationEngine.runSmartClimate(this), 13, false), 2);
        });
        Button resetCooldown = Ui.button(this, "Сбросить минутный cooldown");
        resetCooldown.setOnClickListener(v -> {
            prefs.edit().putLong(SmartClimateController.KEY_LAST_APPLY_AT, 0L).apply();
            Ui.toast(this, "Cooldown сброшен");
        });
        Button dry = Ui.button(this, "Просушка сейчас");
        dry.setOnClickListener(v -> root.addView(Ui.text(this, SmartClimateController.dryAfterTrip(this), 13, false), 2));
        Button log = Ui.button(this, "Журнал климата");
        log.setOnClickListener(v -> panel("Журнал умного климата", SmartClimateController.log(this)));
        Button signals = Ui.button(this, "Статус сигналов авто");
        signals.setOnClickListener(v -> panel("Сигналы авто", VehicleSignalStateAdapter.lastStatus(this)));
        root.addView(Ui.text(this, "Режимы: off, fast_cool, fast_heat, stabilize, maintain, dry, summer. Контроллер читает реальные sensor-сигналы через AdaptAPI, а поля салон/улица используются как fallback.", 14, false));
        root.addView(enabled);
        root.addView(mode);
        root.addView(cabin);
        root.addView(outside);
        root.addView(driverTarget);
        root.addView(passengerTarget);
        root.addView(engineMinutes);
        root.addView(fogging);
        root.addView(call);
        root.addView(dryAfterTrip);
        root.addView(save);
        root.addView(run);
        root.addView(resetCooldown);
        root.addView(dry);
        root.addView(log);
        root.addView(signals);
    }

    private void saveAutomationPreset(String oldName, String newName, String body) {
        saveNamed(AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName, newName, body);
    }

    private void saveNamed(String orderKey, String prefix, String oldName, String newName, String value) {
        String clean = newName.trim();
        if (clean.isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(this);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        if (!oldName.isEmpty() && !oldName.equals(clean)) names.remove(oldName);
        if (!names.contains(clean)) names.add(clean);
        SharedPreferences.Editor editor = prefs.edit().putString(prefix + clean, value).putString(orderKey, AutomationEngine.join(names));
        if (!oldName.isEmpty() && !oldName.equals(clean)) editor.remove(prefix + oldName);
        editor.apply();
    }

    private void deleteAutomationItem(String orderKey, String prefix, String name) {
        if (name == null || name.trim().isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(this);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        names.remove(name);
        prefs.edit().remove(prefix + name).putString(orderKey, AutomationEngine.join(names)).apply();
    }

    private String firstAutomationPreset() {
        List<String> names = AutomationEngine.names(AutomationEngine.prefs(this), AutomationEngine.KEY_PRESET_ORDER);
        return names.isEmpty() ? "" : names.get(0);
    }

    private String defaultSmartPresetText() {
        return "0x10010100/0=0x1\n0x10010300/0=0x1\n0x10020100/0=0x10020103\nfloat:0x10060100/1=22.0\nfloat:0x10060100/4=22.0";
    }

    private String defaultScenarioText() {
        return "name:Morning comfort\n"
                + "trigger:manual=morning\n"
                + "condition:time=06:00..10:00\n"
                + "condition:profile=Глеб\n"
                + "policy:startDelay=10s\n"
                + "policy:minInterval=30m\n"
                + "policy:oncePerTrip=true\n"
                + "policy:cancelOnConditionChange=true\n"
                + "step:action smart_climate=true\n"
                + "step:delay 5m\n"
                + "step:command 0x10020100/0=0x10020102\n"
                + "step:notify Сценарий завершен";
    }

    private void installClimateScenarioV2() {
        saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Зимний запуск",
                "name:Зимний запуск\n"
                        + "trigger:manual=winter\n"
                        + "trigger:boot=BOOT_COMPLETED\n"
                        + "condition:outsideTemp<5\n"
                        + "policy:minInterval=30m\n"
                        + "policy:oncePerTrip=true\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Зимний запуск\n"
                        + "step:action smart_climate=true\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command float:0x10060100/1=22.0\n"
                        + "step:command float:0x10060100/4=22.0\n"
                        + "step:command 0x10070100/0=0x10070106\n"
                        + "step:command 0x10040100/0=0x1\n"
                        + "step:command 0x10090100/0=0x10090203\n"
                        + "step:command 0x10050200/1=0x10050303\n"
                        + "step:delay 5m\n"
                        + "step:command 0x10020100/0=0x10020102\n"
                        + "step:wait cabinTemp>=18 timeout=10m\n"
                        + "step:command 0x10050200/1=0x10050301");
        saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Летнее охлаждение",
                "name:Летнее охлаждение\n"
                        + "trigger:manual=summer\n"
                        + "condition:cabinTemp>25\n"
                        + "policy:minInterval=30m\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Летнее охлаждение\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command 0x10010400/0=0x1\n"
                        + "step:command float:0x10060100/1=18.0\n"
                        + "step:command float:0x10060100/4=18.0\n"
                        + "step:command 0x10030100/0=0x10030101\n"
                        + "step:command 0x10020100/0=0x10020108\n"
                        + "step:command 0x10050100/1=0x10050302\n"
                        + "step:wait cabinTemp<=25 timeout=10m\n"
                        + "step:command 0x10020100/0=0x10020103\n"
                        + "step:wait cabinTemp<=22 timeout=15m\n"
                        + "step:command 0x10010200/0=0x1");
        Ui.toast(this, "Сценарии v2 добавлены");
    }

    private void installWelcomeLeaveScenarios() {
        saveAutomationPreset("", "Welcome drive",
                "action:profile=Driver\n"
                        + "0x10010100/0=0x1\n"
                        + "0x10010200/0=0x1\n"
                        + "float:0x10060100/1=22.0\n"
                        + "float:0x10060100/4=22.0\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010206\n"
                        + "0x2a080100/0=0x2a080103\n"
                        + "0x22010100/0=0x22010102");
        saveAutomationPreset("", "Leave car",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x10010100/0=0x0\n"
                        + "0x2a010100/0=0x0\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Welcome manual", "Welcome manual|manual|welcome|Welcome drive");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Leave manual", "Leave manual|manual|leave|Leave car");
        Ui.toast(this, "Welcome / Leave добавлены");
    }

    private void installParkingGuardScenario() {
        saveAutomationPreset("", "Parking guard",
                "action:start_dvr=true\n"
                        + "0x21110100/0=0x1\n"
                        + "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Parking manual", "Parking manual|manual|parking|Parking guard");
        Ui.toast(this, "Parking Guard добавлен");
    }

    private void installRainScenario() {
        saveAutomationPreset("", "Rain safe",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21010100/0=0x21010101\n"
                        + "0x10040100/0=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Rain manual", "Rain manual|manual|rain|Rain safe");
        Ui.toast(this, "Rain Scenario добавлен");
    }

    private void installNightModeScenario() {
        saveAutomationPreset("", "Night mode",
                "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x20150100/0=0x20150102\n"
                        + "0x29020100/0=0x1\n"
                        + "0x29020500/0=0x1\n"
                        + "0x22040200/0=0x22040203\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010205\n"
                        + "0x2a080100/0=0x2a080103");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Night manual", "Night manual|manual|night|Night mode");
        Ui.toast(this, "Night Mode добавлен");
    }

    private void installNavigationContextScenario() {
        saveAutomationPreset("", "Navigation context",
                "action:autozoom=maps,navi,navitel,yandex,2gis:1.18\n"
                        + "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x21110100/0=0x2\n"
                        + "0x21110100/0=0x3");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app", "Navigation app|app|maps|Navigation context");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app yandex", "Navigation app yandex|app|yandex|Navigation context");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app 2gis", "Navigation app 2gis|app|2gis|Navigation context");
        Ui.toast(this, "Navigation App Context добавлен");
    }

    private String automationIdeas() {
        return "Что еще логично автоматизировать:\n"
                + "- Welcome / уход: при открытии двери водителя включить профиль, климат, подсветку и любимый режим движения.\n"
                + "- Parking guard: при парковке включать DVR/360 и закрывать окна/люк.\n"
                + "- Rain scenario: по ручному триггеру или датчику дождя закрыть окна/люк и включить дворники auto.\n"
                + "- Night mode: вечером менять яркость, HUD, тему DIM и салонную подсветку.\n"
                + "- App context: при запуске навигации включать split, HUD navigation и автоzoom; при музыке менять DIM/media bridge.\n"
                + "- Service mode: перед визитом в сервис отключать экспериментальные функции и возвращать стандартный профиль.";
    }
    private void showCar() {
        LinearLayout root = commandRoot("Управление автомобилем");
        root.addView(Ui.text(this, "BCM-функции из IBcm.smali. Зоны берутся из GlyCarAreaId: все=0x80000000, 1L=1, 1R=4, 2L=16, 2R=64.", 14, false));
        addDiagnostic(root, "BCM / Drive / Seat", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.SEAT_POSITION_SET);
        addDiagnostic(root, "BCM двери/окна расширенно", EcarxVehicleAdapter.BCM_DOOR_POS, EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_OBSTACLE_DETECTED, EcarxVehicleAdapter.BCM_DOOR_ANTI_PINCH, EcarxVehicleAdapter.BCM_WINDOW_MOVING_STATE, EcarxVehicleAdapter.BCM_WINDOW_POS, EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS);
        addDiagnostic(root, "BCM кузов/датчики", EcarxVehicleAdapter.BCM_CHARGING_CAP, EcarxVehicleAdapter.BCM_FUEL_CAP, EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.BCM_STEERING_WHEEL_ADJUST, EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_MIN, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_MAX, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_STEP);
        addZoneDiagnostic(root, "Окна", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneDiagnostic(root, "Двери", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Окна открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN);
        addCommand(root, "Окна закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE);
        addZoneCommands(root, "Окно открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Окно закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Окна пауза", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_PAUSE);
        addCommand(root, "Окна half", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_HALF);
        addCommand(root, "Окна open pause", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN_PAUSE);
        addCommand(root, "Окна close pause", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE_PAUSE);
        addCommand(root, "Блокировка окон вкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Блокировка окон выкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Двери открыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_OPEN);
        addCommand(root, "Двери закрыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_CLOSE);
        addZoneCommands(root, "Дверь открыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_OPEN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Дверь закрыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_CLOSE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Двери пауза", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_PAUSE);
        addCommand(root, "Door control открыть", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_OPEN);
        addCommand(root, "Door control закрыть", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_CLOSE);
        addCommand(root, "Door control пауза", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_PAUSE);
        addCommand(root, "Door lock вкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Door lock выкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Child safety lock вкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Child safety lock выкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Child safety scene вкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK_SCENE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Child safety scene выкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK_SCENE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Автозакрытие дверей по скорости вкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Автозакрытие дверей по скорости выкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Все двери one key", EcarxVehicleAdapter.BCM_ALL_DOORS_ONE_KEY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк открыть", EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк закрыть", EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка открыть", EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка закрыть", EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала сложить/переключить", EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON);
        Button mirrorDialog = Ui.button(this, "Открыть штатную регулировку зеркал · ControlBoard.showMirrorDialog");
        mirrorDialog.setOnClickListener(v -> {
            EcarxControlBoardAdapter.Result result = new EcarxControlBoardAdapter(this).showMirrorDialog();
            Ui.toast(this, result.success ? "Диалог открыт" : "Диалог не открыт");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(mirrorDialog);
        root.addView(Ui.text(this, new EcarxControlBoardAdapter(this).availability(), 13, false));
        addCommand(root, "Зеркало left adjust mode", EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE);
        addCommand(root, "Зеркало right adjust mode", EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE);
        addCommand(root, "Зеркала defrost вкл", EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала defrost выкл", EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Power on/off", EcarxVehicleAdapter.BCM_POWER_ONOFF, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Power confirm", EcarxVehicleAdapter.BCM_POWER_ONOFF_CONFIRM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Display вкл", EcarxVehicleAdapter.BCM_DISPLAY_ONOFF, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Display выкл", EcarxVehicleAdapter.BCM_DISPLAY_ONOFF, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Display position idle", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_IDLE);
        addCommand(root, "Display position A", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_A);
        addCommand(root, "Display position B", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_B);
        addCommand(root, "Свет салона вкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Свет салона выкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Весь свет салона вкл", EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Весь свет салона выкл", EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Wiper off", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_OFF);
        addCommand(root, "Wiper auto", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_AUTO);
        addCommand(root, "Wiper low", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_LOW);
        addCommand(root, "Wiper high", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_HIGH);
        addCommand(root, "Wiper intermittent", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_INTERMITTENT);
        addCommand(root, "Washer", EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Ближний свет вкл", EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Ближний свет выкл", EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Дальний свет вкл", EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Дальний свет выкл", EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Driving lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_DRIVING_LAMPS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Driving lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_DRIVING_LAMPS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Cornering lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_CORNERING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Cornering lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_CORNERING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Spot lights вкл", EcarxVehicleAdapter.BCM_LIGHT_SPOT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Spot lights выкл", EcarxVehicleAdapter.BCM_LIGHT_SPOT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Передние габариты вкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_POSITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Передние габариты выкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_POSITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "DRL вкл", EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "DRL выкл", EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Dim dip вкл", EcarxVehicleAdapter.BCM_LIGHT_DIM_DIP, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Dim dip выкл", EcarxVehicleAdapter.BCM_LIGHT_DIM_DIP, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Side marker вкл", EcarxVehicleAdapter.BCM_LIGHT_SIDE_MARKER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Side marker выкл", EcarxVehicleAdapter.BCM_LIGHT_SIDE_MARKER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Задние габариты вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_POSITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Задние габариты выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_POSITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Stop lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_STOP, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Stop lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_STOP, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Reverse lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_REVERSING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Reverse lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_REVERSING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Аварийка вкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Аварийка выкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Atmosphere lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Atmosphere lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Левый поворотник вкл", EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Левый поворотник выкл", EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Правый поворотник вкл", EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Правый поворотник выкл", EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Передние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Передние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Задние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Задние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Rear logo light вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Rear logo light выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Grille lamp вкл", EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Grille lamp выкл", EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "All weather light вкл", EcarxVehicleAdapter.BCM_LIGHT_ALL_WEATHER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "All weather light выкл", EcarxVehicleAdapter.BCM_LIGHT_ALL_WEATHER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Number plate light вкл", EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Number plate light выкл", EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome light вкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome light выкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Grille color 1", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_1);
        addCommand(root, "Grille color 2", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_2);
        addCommand(root, "Grille color 3", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_3);
        addCommand(root, "FPL follow DRL mode 1", EcarxVehicleAdapter.BCM_FPL_FOLLOW_DRL, EcarxVehicleAdapter.FPL_FOLLOW_DRL_MODE1);
        addCommand(root, "FPL follow DRL mode 2", EcarxVehicleAdapter.BCM_FPL_FOLLOW_DRL, EcarxVehicleAdapter.FPL_FOLLOW_DRL_MODE2);
        addCommand(root, "ICC normal", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_NORMAL);
        addCommand(root, "ICC warning", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_WARNING);
        addCommand(root, "ICC error", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_ERROR);
        addCommand(root, "Custom key: багажник", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        addCommand(root, "Custom key: 360 камера", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "Custom key: DVR", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR);
        addCommand(root, "Custom key: navigation", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_NAVIGATION);
        addCommand(root, "Custom key: DIM map", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DIM_FULL_SCREEN_MAP);
        addCommand(root, "Custom key: sound", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_SOUND_SWITCH);
        addCommand(root, "Custom key: favorite", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_COLLECT_FAV);
        addCommand(root, "Custom key: loud speaker", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_LOUD_SPEAKER);
        addCommand(root, "Custom key: auto park", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(root, "Custom key: driving mode", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DRIVING_MODE);
        root.addView(Ui.text(this, "Режимы движения из IDriveMode.smali.", 14, false));
        addCommand(root, "Drive Eco", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO);
        addCommand(root, "Drive Comfort", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT);
        addCommand(root, "Drive Dynamic", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC);
        addCommand(root, "Drive Snow", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW);
        addCommand(root, "Drive Offroad", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD);
        if (experimentalFeaturesEnabled()) addExperimentalDriveFeatures(root);
        addCommand(root, "Eco button", EcarxVehicleAdapter.DRIVE_ECO_BUTTON, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Руль мягкий", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT);
        addCommand(root, "Руль динамичный", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC);
        root.addView(Ui.text(this, "Профили и регулировки сидений из ISeat.smali.", 14, false));
        addZoneDiagnostic(root, "Сиденье положение", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Сиденье вперед", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_FORWARD);
        addCommand(root, "Сиденье назад", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_BACKWARD);
        addZoneCommands(root, "Сиденье вперед", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_FORWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Сиденье назад", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_BACKWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Сиденье выше", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_UP);
        addCommand(root, "Сиденье ниже", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN);
        addZoneCommands(root, "Сиденье выше", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_UP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Сиденье ниже", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Спинка вперед", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD);
        addCommand(root, "Спинка назад", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD);
        addZoneCommands(root, "Спинка вперед", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Спинка назад", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Сохранить профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Сохранить профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Вызвать профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Вызвать профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Сиденье comfort", EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Сиденье restore", EcarxVehicleAdapter.SEAT_RESTORE, EcarxVehicleAdapter.COMMON_ON);
    }

    private void showClimate() {
        LinearLayout root = commandRoot("Климат");
        root.addView(Ui.text(this, "HVAC-функции из IHvac.smali и OneOS-Dock: обычные int-команды плюс float-температура driver zone=1 / passenger zone=4.", 14, false));
        addDiagnostic(root, "HVAC", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.HVAC_TEMP_MIN, EcarxVehicleAdapter.HVAC_TEMP_MAX, EcarxVehicleAdapter.HVAC_TEMP_STEP);
        addDiagnostic(root, "HVAC расширенный", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.HVAC_FILTER_ELEMENT_LIFE, EcarxVehicleAdapter.HVAC_MODULE_CONNECT_STATUS);
        addFloatDiagnostic(root, "Температура driver/passenger", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneDiagnostic(root, "Подогрев сидений", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneDiagnostic(root, "Вентиляция сидений", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        Button comfortPanel = Ui.button(this, "Комфортный климат");
        comfortPanel.setOnClickListener(v -> showComfortClimate());
        root.addView(comfortPanel);
        Button editor = Ui.button(this, "Создать / редактировать пресет");
        editor.setOnClickListener(v -> showClimatePresetEditor("", defaultPresetText()));
        root.addView(editor);
        for (String name : climatePresetNames()) {
            EcarxVehicleAdapter.Command[] commands = decodeCommands(getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(name, ""));
            if (commands.length > 0) addSavedClimatePreset(root, name, commands);
        }
        addPreset(root, "Пресет Комфорт",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE));
        addPreset(root, "Пресет Охлаждение",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addPreset(root, "Пресет Зима",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        addCommand(root, "Климат включить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат выключить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C включить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C выключить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C Max", EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto climate", EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Eco climate", EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Вентилятор 1", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        addCommand(root, "Вентилятор 2", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_2);
        addCommand(root, "Вентилятор 3", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
        addCommand(root, "Вентилятор 4", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_4);
        addCommand(root, "Вентилятор 5", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
        addCommand(root, "Вентилятор 6", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_6);
        addCommand(root, "Вентилятор 7", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_7);
        addCommand(root, "Вентилятор 8", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_8);
        addCommand(root, "Вентилятор 9", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_9);
        addCommand(root, "Вентилятор auto", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO);
        addCommand(root, "Рециркуляция внутренняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER);
        addCommand(root, "Рециркуляция внешняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE);
        addCommand(root, "Рециркуляция auto", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_AUTO);
        addCommand(root, "Обдув лицо", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE);
        addCommand(root, "Обдув ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG);
        addCommand(root, "Обдув лицо+ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG);
        addCommand(root, "Обдув стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW);
        addCommand(root, "Обдув лицо+стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW);
        addCommand(root, "Обдув ноги+стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW);
        addCommand(root, "Обдув все", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL);
        addCommand(root, "Обдув auto", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_AUTO);
        addCommand(root, "Обдув лобового", EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Max обдув лобового", EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Обогрев заднего стекла", EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат зона single", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE);
        addCommand(root, "Климат зона dual", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL);
        addCommand(root, "Климат зона triple", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_TRIPLE);
        addCommand(root, "Климат зона four", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_FOUR);
        addCommand(root, "Температура dual sync", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Температура dual split", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Температура Celsius", EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_C);
        addCommand(root, "Температура Fahrenheit", EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_F);
        addFloatCommand(root, "Driver temp 18.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 18.0f);
        addFloatCommand(root, "Driver temp 20.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 20.0f);
        addFloatCommand(root, "Driver temp 22.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 22.0f);
        addFloatCommand(root, "Driver temp 24.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 24.0f);
        addFloatCommand(root, "Passenger temp 18.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 18.0f);
        addFloatCommand(root, "Passenger temp 20.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 20.0f);
        addFloatCommand(root, "Passenger temp 22.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 22.0f);
        addFloatCommand(root, "Passenger temp 24.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 24.0f);
        addCommand(root, "Открыть левую температуру", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_LEFT_TEMP);
        addCommand(root, "Открыть правую температуру", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_RIGHT_TEMP);
        addCommand(root, "Открыть вкладку сидений", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_SEAT);
        addCommand(root, "Hardkey левая температура", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_LEFT_TEMP);
        addCommand(root, "Hardkey правая температура", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_RIGHT_TEMP);
        addCommand(root, "Hardkey temp sync", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_TEMP_SYNC);
        addCommand(root, "Hardkey fan up", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_UP);
        addCommand(root, "Hardkey fan down", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_DOWN);
        addCommand(root, "Быстрое охлаждение", EcarxVehicleAdapter.HVAC_RAPID_COOLING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Быстрый прогрев", EcarxVehicleAdapter.HVAC_RAPID_WARMING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат second row auto", EcarxVehicleAdapter.HVAC_AUTO_SECOND_ROW_CLIMATE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат lock on", EcarxVehicleAdapter.HVAC_CLIMATE_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат lock off", EcarxVehicleAdapter.HVAC_CLIMATE_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "AQS on", EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "CO2 control on", EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Ionizer on", EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto dehumidification on", EcarxVehicleAdapter.HVAC_AUTO_DEHUMIDIFICATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Overheat protection on", EcarxVehicleAdapter.HVAC_OVERHEAT_PROTECTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Air fragrance on", EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "G-Clean on", EcarxVehicleAdapter.HVAC_G_CLEAN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto ventilation dry", EcarxVehicleAdapter.HVAC_AUTOMATIC_VENTILATION_DRY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Pre-climatisation on", EcarxVehicleAdapter.HVAC_PRE_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Post-climatisation on", EcarxVehicleAdapter.HVAC_POST_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Direction focus", EcarxVehicleAdapter.HVAC_DIRECTION_MODE, EcarxVehicleAdapter.DIRECTION_MODE_FOCUS);
        addCommand(root, "Direction avoid", EcarxVehicleAdapter.HVAC_DIRECTION_MODE, EcarxVehicleAdapter.DIRECTION_MODE_AVOID);
        addCommand(root, "Sweeping all", EcarxVehicleAdapter.HVAC_SWEEPING_MODE, EcarxVehicleAdapter.SWEEPING_MODE_ALL);
        addCommand(root, "Sweeping custom", EcarxVehicleAdapter.HVAC_SWEEPING_MODE, EcarxVehicleAdapter.SWEEPING_MODE_CUSTOM);
        addCommand(root, "Подогрев сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Подогрев сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Подогрев сиденья off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Вентиляция сиденья off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Массаж сиденья off", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Подогрев руля low", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_LOW);
        addCommand(root, "Подогрев руля off", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showComfortClimate() {
        LinearLayout root = commandRoot("Комфортный климат");
        root.addView(Ui.text(this, "Быстрый HVAC-пульт: питание, auto/A/C, обдув, вентилятор, сиденье и руль.", 14, false));
        Button fullStatus = Ui.button(this, "Статус климата");
        fullStatus.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            int[] ids = {
                    EcarxVehicleAdapter.HVAC_POWER,
                    EcarxVehicleAdapter.HVAC_AUTO,
                    EcarxVehicleAdapter.HVAC_AC,
                    EcarxVehicleAdapter.HVAC_FAN_SPEED,
                    EcarxVehicleAdapter.HVAC_CIRCULATION,
                    EcarxVehicleAdapter.HVAC_BLOWING_MODE,
                    EcarxVehicleAdapter.HVAC_TEMP,
                    EcarxVehicleAdapter.HVAC_TEMP_MIN,
                    EcarxVehicleAdapter.HVAC_TEMP_MAX,
                    EcarxVehicleAdapter.HVAC_TEMP_STEP
            };
            StringBuilder sb = new StringBuilder("HVAC status\n");
            for (int id : ids) sb.append(adapter.get(id).message).append("\n");
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(fullStatus);
        addPreset(root, "Комфорт auto 3",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG));
        addPreset(root, "Тихий режим",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE));
        addPreset(root, "Быстрый прогрев",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        addCommand(root, "Power on", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Power off", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Auto on", EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C on", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C off", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Fan 1", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        addCommand(root, "Fan 3", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
        addCommand(root, "Fan 5", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
        addCommand(root, "Fan auto", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO);
        addCommand(root, "Обдув лицо", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE);
        addCommand(root, "Обдув ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG);
        addCommand(root, "Обдув стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW);
        addCommand(root, "Обдув все", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL);
        addCommand(root, "Seat heat off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Seat heat 2", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2);
        addZoneCommands(root, "Seat heat 2", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Seat heat off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Seat vent off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Seat vent 2", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2);
        addZoneCommands(root, "Seat vent 2", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Seat vent off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Wheel heat off", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Wheel heat mid", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID);
    }

    private void showClimatePresetEditor(String oldName, String commandsText) {
        LinearLayout form = Ui.root(this, "Пресет климата");
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText commands = new EditText(this);
        commands.setHint("functionId,zone,value по одной команде на строку");
        commands.setMinLines(8);
        commands.setText(commandsText);
        Button save = Ui.button(this, "Сохранить");
        Button cancel = Ui.button(this, "Назад");
        save.setOnClickListener(v -> {
            String presetName = name.getText().toString().trim();
            EcarxVehicleAdapter.Command[] parsed = decodeCommands(commands.getText().toString());
            if (presetName.isEmpty() || parsed.length == 0) {
                Ui.toast(this, "Нужно имя и хотя бы одна команда");
                return;
            }
            if (!oldName.isEmpty() && !oldName.equals(presetName)) deleteClimatePreset(oldName);
            saveClimatePreset(presetName, encodeCommands(parsed));
            showClimate();
        });
        cancel.setOnClickListener(v -> showClimate());
        form.addView(Ui.text(this, "Формат: functionId,zone,value. Можно писать decimal или 0xHEX.", 14, false));
        form.addView(name);
        form.addView(commands);
        form.addView(save);
        form.addView(cancel);
        setContentView(form);
    }

    private String defaultPresetText() {
        return EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_POWER) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_AUTO) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_FAN_SPEED) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.FAN_SPEED_3);
    }

    private void saveClimatePreset(String name, String encoded) {
        SharedPreferences prefs = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE);
        ArrayList<String> names = climatePresetNames();
        if (!names.contains(name)) names.add(name);
        prefs.edit().putString(name, encoded).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private void deleteClimatePreset(String name) {
        ArrayList<String> names = climatePresetNames();
        names.remove(name);
        getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().remove(name).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private ArrayList<String> climatePresetNames() {
        String order = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(CLIMATE_PRESET_ORDER, "");
        ArrayList<String> names = new ArrayList<>();
        for (String item : order.split("\n")) {
            String name = item.trim();
            if (!name.isEmpty()) names.add(name);
        }
        return names;
    }

    private String encodeCommands(EcarxVehicleAdapter.Command[] commands) {
        StringBuilder sb = new StringBuilder();
        for (EcarxVehicleAdapter.Command command : commands) {
            sb.append(EcarxVehicleAdapter.hex(command.functionId))
                    .append(",")
                    .append(command.zone)
                    .append(",")
                    .append(EcarxVehicleAdapter.hex(command.value))
                    .append("\n");
        }
        return sb.toString();
    }

    private EcarxVehicleAdapter.Command[] decodeCommands(String raw) {
        ArrayList<EcarxVehicleAdapter.Command> commands = new ArrayList<>();
        for (String line : raw.split("\n")) {
            String clean = line.trim();
            if (clean.isEmpty() || clean.startsWith("#")) continue;
            String[] parts = clean.split(",");
            if (parts.length < 2) continue;
            try {
                int functionId = parseNumber(parts[0]);
                int zone = parts.length > 2 ? parseNumber(parts[1]) : 0;
                int value = parseNumber(parts.length > 2 ? parts[2] : parts[1]);
                commands.add(new EcarxVehicleAdapter.Command(functionId, zone, value));
            } catch (Exception ignored) {
            }
        }
        return commands.toArray(new EcarxVehicleAdapter.Command[0]);
    }

    private int parseNumber(String raw) {
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("0x")) return (int) Long.parseLong(value.substring(2), 16);
        return Integer.parseInt(value);
    }

    private String join(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) sb.append(name).append("\n");
        return sb.toString();
    }

    private void showParkingApa() {
        LinearLayout root = commandRoot("Парковка / APA");
        root.addView(Ui.text(this, "Штатный вход в автопарковку найден через BCM custom key 0x65. Raw APA/RPA сигналы доступны только через Experimental features.", 14, false));
        addCommand(root, "Открыть штатный Auto Park UI", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(root, "Открыть 360 panorama", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addDiagnostic(root, "BCM parking entry", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME);
        if (!experimentalFeaturesEnabled()) {
            root.addView(Ui.text(this, "Включи Settings -> Experimental features, чтобы увидеть raw APA/RPA диагностику и кнопки.", 14, false));
            return;
        }
        root.addView(Ui.text(this, "Experimental APA/RPA: CarSignalManager raw methods. Значения взяты из annotation smali; выполнение зависит от доступа к системному car service.", 14, false));
        addSignalDiagnostic(root, "APA/RPA status",
                "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
        addSignalCommand(root, "APA on button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
        addSignalCommand(root, "APA undo", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_UNDO);
        addSignalCommand(root, "APA cancel", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
        addSignalCommand(root, "APA manual", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
        addSignalCommand(root, "APA confirm enter auto parking", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
        addSignalCommand(root, "PAS button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_PAS);
        addSignalCommand(root, "RPA button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA);
        addSignalCommand(root, "RPA button alt", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA_ALT);
        addSignalCommand(root, "Parking mode default", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_DEFAULT);
        addSignalCommand(root, "Parking mode horizontal in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
        addSignalCommand(root, "Parking mode perpendicular in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
        addSignalCommand(root, "Parking mode perpendicular in forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular in backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN_BACKWARD);
        addSignalCommand(root, "Parking mode horizontal left out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
        addSignalCommand(root, "Parking mode horizontal right out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
        addSignalCommand(root, "Parking mode perpendicular left out forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_LEFT_OUT_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular right out forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_RIGHT_OUT_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular left out backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_LEFT_OUT_BACKWARD);
        addSignalCommand(root, "Parking mode perpendicular right out backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_RIGHT_OUT_BACKWARD);
        addSignalCommand(root, "Parking mode cancel", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
        addSignalCommand(root, "Remote parking enable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_ON);
        addSignalCommand(root, "Remote parking disable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_OFF);
        addSignalCommand(root, "Remote parking self-search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
        addSignalCommand(root, "Remote parking self-search no press", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_NO_PRESS);
        addHalPropertyDiagnostic(root, "Mobile RPA HAL properties",
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_REQ_RESP,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_ON_OFF1,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_UINT8,
                CarSignalManagerAdapter.VEH_PUSH_APA_INFO_REQ);
    }

    private void showPasAvm() {
        LinearLayout root = commandRoot("Experimental: PAS / AVM");
        root.addView(Ui.text(this, "PAS/PAC/AVM функции из IPAS.smali. PAS.smali содержит startAVM/stopAVM/getAVMState, но здесь команды идут через functionId AdaptAPI и BCM custom key 360.", 14, false));
        addDiagnostic(root, "PAC / AVM camera state",
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STATUS,
                EcarxVehicleAdapter.PAS_PAC_SYS_AVA_STATUS,
                EcarxVehicleAdapter.PAS_PAC_CAMERA_TYPE,
                EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_OBSTACLE_DETECTION,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        addDiagnostic(root, "PAS radar state",
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_MIN_DISTANCE,
                EcarxVehicleAdapter.PAS_RADAR_MAX_DISTANCE,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_REAR_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_INNER_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_INNER_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_OUT_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_OUT_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_INNER_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_INNER_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_OUT_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_OUT_RIGHT);
        addDiagnostic(root, "SAP / RCTA / parking readback",
                EcarxVehicleAdapter.PAS_SAP_ACTIVATION,
                EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PROGRESS,
                EcarxVehicleAdapter.PAS_RCTA_ACTIVATION,
                EcarxVehicleAdapter.PAS_RCTA_LEFT_WARNING,
                EcarxVehicleAdapter.PAS_RCTA_RIGHT_WARNING,
                EcarxVehicleAdapter.PAS_PRKG_AUX_INFO_DISP,
                EcarxVehicleAdapter.PAS_PRKG_INTRPT_RELD_BTN);
        addPreset(root, "Start AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addPreset(root, "Stop AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF));
        addCommand(root, "Open 360 panorama key", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "PAC app init completed", EcarxVehicleAdapter.PAS_PAC_APP_INIT_COMPLETED, EcarxVehicleAdapter.COMMON_ON);
        addCommandGroup(root, "Auto reverse camera", EcarxVehicleAdapter.PAS_PAC_AUTO_REVERSE_CAMERA,
                new String[]{"Reverse camera off", "Reverse camera rear", "Reverse camera top"},
                new int[]{EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_OFF, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_REAR, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_TOP});
        addCommandGroup(root, "Radar work mode", EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                new String[]{"Radar off", "Radar standby", "Radar front+rear", "Radar front", "Radar rear"},
                new int[]{EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_OFF, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_STANDBY, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_REAR_ACTIVE});
        addCommandGroup(root, "PAC 3D surround view", EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                new String[]{"3D surround", "Rear left 3D", "Rear right 3D"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_LEFT_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_RIGHT_3D});
        addCommandGroup(root, "PAC 3D position", EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                new String[]{"3D off", "3D front center", "3D front left", "3D front right", "3D left", "3D right", "3D rear center", "3D rear left", "3D rear right"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_3D_POS_OFF, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_RIGHT});
        addCommand(root, "Guide / steer path on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Guide / steer path off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Distance overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Distance overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Towbar overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Towbar overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Transparent model on", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Transparent model off", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Look-down / top view on", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Look-down / top view off", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS top view on", EcarxVehicleAdapter.PAS_TOP_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS top view off", EcarxVehicleAdapter.PAS_TOP_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Turn-round / touring view on", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Turn-round / touring view off", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "3D view lock on", EcarxVehicleAdapter.PAS_PAC_3DVIEW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "3D view lock off", EcarxVehicleAdapter.PAS_PAC_3DVIEW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAC steer link on", EcarxVehicleAdapter.PAS_PAC_STEER_LINK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAC steer link off", EcarxVehicleAdapter.PAS_PAC_STEER_LINK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAC auto front activation on", EcarxVehicleAdapter.PAS_PAC_AUTO_FRONT_ACTIV, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAC auto front activation off", EcarxVehicleAdapter.PAS_PAC_AUTO_FRONT_ACTIV, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS graphics on", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS graphics off", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS mute on", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS mute off", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "SAP parking", EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                new String[]{"SAP park in", "SAP park out"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_IN, EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_OUT});
        addCommandGroup(root, "SAP park-in type", EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                new String[]{"SAP perpendicular", "SAP parallel"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PERP, EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PARA});
        addCommand(root, "RCTA on", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCTA off", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "RCTA graphics on", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCTA graphics off", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "RCTA warning volume", EcarxVehicleAdapter.PAS_RCTA_WARNING_VOLUME,
                new String[]{"RCTA volume off", "RCTA volume low", "RCTA volume mid", "RCTA volume high"},
                new int[]{EcarxVehicleAdapter.PAS_RCTA_VOLUME_OFF, EcarxVehicleAdapter.PAS_RCTA_VOLUME_LOW, EcarxVehicleAdapter.PAS_RCTA_VOLUME_MID, EcarxVehicleAdapter.PAS_RCTA_VOLUME_HIGH});
    }

    private void showAvasDigitalKey() {
        LinearLayout root = commandRoot("Experimental: AVAS / Digital Key");
        root.addView(Ui.text(this, "AVAS - внешний звук предупреждения пешеходов у EV/PHEV. Отключение или смена громкости/типа звука может быть юридически и безопасностно спорной, поэтому раздел спрятан за Experimental features.", 14, false));
        addDiagnostic(root, "AVAS",
                EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH,
                EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_NAME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_PATH);
        addCommand(root, "AVAS switch on", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AVAS switch off", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "AVAS volume", EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                new String[]{"AVAS volume off", "AVAS volume low", "AVAS volume mid", "AVAS volume high"},
                new int[]{EcarxVehicleAdapter.AVAS_VOLUME_OFF, EcarxVehicleAdapter.AVAS_VOLUME_LOW, EcarxVehicleAdapter.AVAS_VOLUME_MID, EcarxVehicleAdapter.AVAS_VOLUME_HIGH});
        addCommandGroup(root, "AVAS sound type", EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                new String[]{"AVAS sound none", "AVAS sound 1", "AVAS sound 2", "AVAS sound 3", "AVAS sound 4", "AVAS sound 5", "AVAS sound 6", "AVAS sound 7", "AVAS sound 8"},
                new int[]{EcarxVehicleAdapter.AVAS_SOUND_NONE, EcarxVehicleAdapter.AVAS_SOUND_1, EcarxVehicleAdapter.AVAS_SOUND_2, EcarxVehicleAdapter.AVAS_SOUND_3, EcarxVehicleAdapter.AVAS_SOUND_4, EcarxVehicleAdapter.AVAS_SOUND_5, EcarxVehicleAdapter.AVAS_SOUND_6, EcarxVehicleAdapter.AVAS_SOUND_7, EcarxVehicleAdapter.AVAS_SOUND_8});
        root.addView(Ui.text(this, "Digital key ниже только читает статусы. Команды pair/unpair/delete/termination/suspension намеренно не добавлены.", 14, false));
        addDiagnostic(root, "Digital key statuses",
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_REQ_STS,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_UNPAIR,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TERMINATION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_SUSPENSION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_PAIRING_FAILED,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_WAIT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_RESULT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_RES_TIMEOUT);
    }

    private void showSceneModes() {
        LinearLayout root = commandRoot("Experimental: Сценарии");
        root.addView(Ui.text(this, "Сценарные режимы из ISceneMode.smali. Кнопки отправляют ON/OFF в соответствующий scene function.", 14, false));
        addDiagnostic(root, "Scene modes",
                EcarxVehicleAdapter.SCENE_THEATER,
                EcarxVehicleAdapter.SCENE_WASH,
                EcarxVehicleAdapter.SCENE_PET,
                EcarxVehicleAdapter.SCENE_SMOKING,
                EcarxVehicleAdapter.SCENE_PARENT_CHILD,
                EcarxVehicleAdapter.SCENE_ROMANTIC,
                EcarxVehicleAdapter.SCENE_NAP,
                EcarxVehicleAdapter.SCENE_QUEEN,
                EcarxVehicleAdapter.SCENE_SLEEP,
                EcarxVehicleAdapter.SCENE_CAMP,
                EcarxVehicleAdapter.SCENE_MEETING,
                EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO,
                EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
        addSceneToggle(root, "Theater", EcarxVehicleAdapter.SCENE_THEATER);
        addSceneToggle(root, "Wash", EcarxVehicleAdapter.SCENE_WASH);
        addSceneToggle(root, "Pet", EcarxVehicleAdapter.SCENE_PET);
        addSceneToggle(root, "Smoking", EcarxVehicleAdapter.SCENE_SMOKING);
        addSceneToggle(root, "Parent-child", EcarxVehicleAdapter.SCENE_PARENT_CHILD);
        addSceneToggle(root, "Romantic", EcarxVehicleAdapter.SCENE_ROMANTIC);
        addSceneToggle(root, "Nap", EcarxVehicleAdapter.SCENE_NAP);
        addSceneToggle(root, "Queen", EcarxVehicleAdapter.SCENE_QUEEN);
        addSceneToggle(root, "Sleep", EcarxVehicleAdapter.SCENE_SLEEP);
        addSceneToggle(root, "Camping", EcarxVehicleAdapter.SCENE_CAMP);
        addSceneToggle(root, "Meeting", EcarxVehicleAdapter.SCENE_MEETING);
        addSceneToggle(root, "Rear-row video", EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO);
        addSceneToggle(root, "PSD passenger theater", EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
    }

    private void addSceneToggle(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " on", functionId, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, label + " off", functionId, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showAmbienceLight() {
        LinearLayout root = commandRoot("Experimental: Подсветка");
        root.addView(Ui.text(this, "Ambience light из IAmbienceLight.smali: темы, цвета, weather/music/welcome/voice и зоны.", 14, false));
        addDiagnostic(root, "Ambience light",
                EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WEATHER,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MAIN_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_TOP_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_BOT_ZONES);
        addCommandGroup(root, "Theme color", EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                new String[]{"Color red", "Color orange", "Color yellow", "Color green", "Color indigo", "Color blue", "Color violet", "Color white", "Color ice blue", "Color off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_RED, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ORANGE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_YELLOW, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_GREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_INDIGO, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_BLUE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_VIOLET, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WHITE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ICE_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Theme mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                new String[]{"Effect solid", "Effect gradients", "Effect breathe", "Theme radical", "Theme simple", "Theme liberating", "Theme agile", "Effect off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_SOLID, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_GRADIENTS, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_BREATHE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_RADICAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_SIMPLE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_LIBERATING, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_AGILE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Control mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                new String[]{"Control more", "Control music", "Control screen", "Control color", "Control time"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MORE, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MUSIC, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_SCREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_COLOR, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_TIME});
        addCommand(root, "Music show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Music show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Welcome show mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                new String[]{"Welcome passionate", "Welcome normal", "Welcome subdued", "Welcome off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_PASSIONATE, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_NORMAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SUBDUED, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Voice light on", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Voice light off", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Zones", EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                new String[]{"Zone all", "Zone front", "Zone headrest", "Zone rear"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_ALL, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_FRONT, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_HEADREST, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_REAR});
    }

    private void showDayMode() {
        LinearLayout root = commandRoot("Experimental: Яркость / DayMode");
        root.addView(Ui.text(this, "DayMode и яркость из IDayMode.smali. Для яркости значения 25/50/75 экспериментальные; сначала проверь min/max/step.", 14, false));
        addDiagnostic(root, "DayMode / brightness",
                EcarxVehicleAdapter.DAYMODE_SETTING,
                EcarxVehicleAdapter.DAYMODE_SYNC,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_DAY,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_NIGHT,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MIN,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MAX,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_STEP,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN,
                EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_DAY_TIME,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_NIGHT_TIME,
                EcarxVehicleAdapter.DAYMODE_SUN_TIME,
                EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_DAYMODE,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_SCREEN);
        addCommandGroup(root, "DayMode", EcarxVehicleAdapter.DAYMODE_SETTING,
                new String[]{"DayMode day", "DayMode night", "DayMode auto", "DayMode off"},
                new int[]{EcarxVehicleAdapter.DAYMODE_VALUE_DAY, EcarxVehicleAdapter.DAYMODE_VALUE_NIGHT, EcarxVehicleAdapter.DAYMODE_VALUE_AUTO, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "DayMode sync on", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "DayMode sync off", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_OFF);
        addBrightnessCommands(root, "Backlight", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "DIM", EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS);
        addBrightnessCommands(root, "Floodlight", EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "Screen", EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN);
        addBrightnessCommands(root, "Electric rear-view mirror", EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR);
        addCommand(root, "Backlight linkage on", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Backlight linkage off", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Time-control theme on", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Time-control theme off", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void addBrightnessCommands(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " 25", functionId, 25);
        addCommand(root, label + " 50", functionId, 50);
        addCommand(root, label + " 75", functionId, 75);
    }

    private void showAdas() {
        LinearLayout root = commandRoot("ADAS / Вождение");
        root.addView(Ui.text(this, "ADAS-функции из IADAS.smali.", 14, false));
        addDiagnostic(root, "ADAS", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.ADAS_PDC);
        addCommand(root, "AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AEB выключить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "FCW включить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "FCW выключить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LKA выключить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "LDW включить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LDW выключить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "RCW включить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCW выключить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "ELKA включить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ELKA выключить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Lane change assist вкл", EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Lane change assist выкл", EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Auto lane change assist вкл", EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto lane change assist выкл", EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Blind spot detection вкл", EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Blind spot detection выкл", EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic sign recognition вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic sign recognition выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic sign alert вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_ALERT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic sign alert выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_ALERT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Speed limit warning", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Speed limit warning off", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Парктроник включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Парктроник выключить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PDC volume low", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_LOW);
        addCommand(root, "PDC volume mid", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_MID);
        addCommand(root, "PDC volume high", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_HIGH);
        if (experimentalFeaturesEnabled()) addExperimentalAdasFeatures(root);
    }

    private void addExperimentalAdasFeatures(LinearLayout root) {
        root.addView(Ui.text(this, "Experimental ADAS: расширенные IADAS-функции. Сначала запускай диагностику support/readback на конкретной машине.", 14, false));
        addDiagnostic(root, "Experimental ADAS controls",
                EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_NZP_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ALARM_INFO,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH,
                EcarxVehicleAdapter.ADAS_APB_SWITCH,
                EcarxVehicleAdapter.ADAS_APB_MODE,
                EcarxVehicleAdapter.ADAS_TLB_SWITCH,
                EcarxVehicleAdapter.ADAS_TLB_MODE,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND);
        addDiagnostic(root, "Experimental ADAS fault/readback",
                EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE,
                EcarxVehicleAdapter.ADAS_LANE_KEEPING_ASSISTANCE_FAILURE,
                EcarxVehicleAdapter.ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE,
                EcarxVehicleAdapter.ADAS_EMERGENCY_STEERING_FAILURE,
                EcarxVehicleAdapter.ADAS_FORWARD_PRECOLLISION_FAULT,
                EcarxVehicleAdapter.ADAS_FRONT_SIDE_ASSIST_FAILURE,
                EcarxVehicleAdapter.ADAS_ADAPTIVE_CRUISE_FAILURE,
                EcarxVehicleAdapter.ADAS_REAR_COLLISION_WARNING_FAILURE,
                EcarxVehicleAdapter.ADAS_DRIVER_FATIGUE_FAILURE,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHTS_IDENTIFY_FAULTS,
                EcarxVehicleAdapter.ADAS_PADDLE_LANE_CHANGE_ASSIST);
        addCommand(root, "AI driver assist вкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AI driver assist выкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "AI assist default-on вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AI assist default-on выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Fusion navigation вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Fusion navigation выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Out overtaking lane вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Out overtaking lane выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Experimental: lane-change strategy", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY,
                new String[]{"AI lane strategy off", "AI lane strategy gentle", "AI lane strategy standard", "AI lane strategy radical"},
                new int[]{EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_OFF, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_GENTLE, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_STANDARD, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_RADICAL});
        addCommandGroup(root, "Experimental: lane-change warning", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING,
                new String[]{"AI lane warning off", "AI lane warning voice", "AI lane warning vibrate", "AI lane warning both"},
                new int[]{EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_OFF, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VOICE, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VIBRATE, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_BOTH});
        addCommand(root, "Lane-change confirm вкл", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Lane-change confirm выкл", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Drive Pilot вкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Drive Pilot выкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "ACC/LCC switch вкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ACC/LCC switch выкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Drive Pilot alarm cancel", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ALARM_INFO_CANCEL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB switch вкл", EcarxVehicleAdapter.ADAS_APB_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB switch выкл", EcarxVehicleAdapter.ADAS_APB_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "APB mode вкл", EcarxVehicleAdapter.ADAS_APB_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB mode выкл", EcarxVehicleAdapter.ADAS_APB_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "TLB switch вкл", EcarxVehicleAdapter.ADAS_TLB_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "TLB switch выкл", EcarxVehicleAdapter.ADAS_TLB_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "TLB mode вкл", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "TLB mode выкл", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic light attention вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic light attention выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic light sound вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic light sound выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void addExperimentalDriveFeatures(LinearLayout root) {
        root.addView(Ui.text(this, "Experimental drive features: функции из IDriveMode.smali. Перед использованием смотри диагностику support/readback.", 14, false));
        addDiagnostic(root, "Experimental drive modes",
                EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                EcarxVehicleAdapter.DRIVE_CREEP_SET,
                EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL,
                EcarxVehicleAdapter.DRIVE_NOISE_CONTROL,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_VALUE,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MIN,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MAX,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_STEP,
                EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING,
                EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP);
        addCommandGroup(root, "Experimental: расширенные режимы движения", EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                new String[]{"Drive HDC", "Drive Mud", "Drive Rock", "Drive Sand", "Drive AWD", "Drive eAWD", "Drive Save", "Drive Pure", "Drive Hybrid", "Drive PHEV", "Drive Power", "Drive Normal", "Drive Eco HEV/PHEV", "Drive Eco Plus", "Drive Sport Plus", "Drive Adaptive", "Drive Custom", "Drive Start type 18", "Drive Start type 72", "Drive Start type 79", "Drive Start type 97"},
                new int[]{EcarxVehicleAdapter.DRIVE_MODE_HDC, EcarxVehicleAdapter.DRIVE_MODE_MUD, EcarxVehicleAdapter.DRIVE_MODE_ROCK, EcarxVehicleAdapter.DRIVE_MODE_SAND, EcarxVehicleAdapter.DRIVE_MODE_AWD, EcarxVehicleAdapter.DRIVE_MODE_EAWD, EcarxVehicleAdapter.DRIVE_MODE_SAVE, EcarxVehicleAdapter.DRIVE_MODE_PURE, EcarxVehicleAdapter.DRIVE_MODE_HYBRID, EcarxVehicleAdapter.DRIVE_MODE_PHEV, EcarxVehicleAdapter.DRIVE_MODE_POWER, EcarxVehicleAdapter.DRIVE_MODE_NORMAL, EcarxVehicleAdapter.DRIVE_MODE_ECO_HEV_PHEV, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE18, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE72, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE79, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE97});
        addCommandGroup(root, "Experimental: custom propulsion", EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                new String[]{"Propulsion Eco", "Propulsion Comfort", "Propulsion Sport", "Propulsion Offroad", "Propulsion Snow", "Propulsion Sand", "Propulsion Hybrid", "Propulsion Pure", "Propulsion Power", "Propulsion AWD", "Propulsion off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO, EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_SPORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_PROPULSION_SNOW, EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND, EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID, EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE, EcarxVehicleAdapter.CUSTOM_PROPULSION_POWER, EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: custom suspension", EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                new String[]{"Suspension Standard", "Suspension Comfort", "Suspension Sport", "Suspension Offroad", "Suspension Snow", "Suspension Automatic", "Suspension off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_SUSPENSION_STANDARD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SNOW, EcarxVehicleAdapter.CUSTOM_SUSPENSION_AUTOMATIC, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: steering feel", EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                new String[]{"Steering feel Light", "Steering feel Balanced", "Steering feel Heavy", "Steering feel off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT, EcarxVehicleAdapter.CUSTOM_STEERING_BALANCED, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: climate mode", EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                new String[]{"Drive climate Normal", "Drive climate Eco", "Drive climate off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: DIM theme", EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                new String[]{"DIM theme Red", "DIM theme Gold", "DIM theme Blue", "DIM theme off"},
                new int[]{EcarxVehicleAdapter.DIM_THEME_RED, EcarxVehicleAdapter.DIM_THEME_GOLD, EcarxVehicleAdapter.DIM_THEME_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: energy drive mode", EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                new String[]{"Energy Range", "Energy Tour", "Energy Sport", "Energy off"},
                new int[]{EcarxVehicleAdapter.ENERGY_MODE_RANGE, EcarxVehicleAdapter.ENERGY_MODE_TOUR, EcarxVehicleAdapter.ENERGY_MODE_SPORT, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: ESC level", EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                new String[]{"ESC level 1", "ESC level 2", "ESC level 3", "ESC level 4", "ESC level 5", "ESC off"},
                new int[]{EcarxVehicleAdapter.ESC_LEVEL_1, EcarxVehicleAdapter.ESC_LEVEL_2, EcarxVehicleAdapter.ESC_LEVEL_3, EcarxVehicleAdapter.ESC_LEVEL_4, EcarxVehicleAdapter.ESC_LEVEL_5, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: Startrack / champion", EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                new String[]{"Startrack type 18", "Startrack type 72", "Startrack type 79", "Startrack type 97", "Startrack off"},
                new int[]{EcarxVehicleAdapter.STARTRACK_TYPE18, EcarxVehicleAdapter.STARTRACK_TYPE72, EcarxVehicleAdapter.STARTRACK_TYPE79, EcarxVehicleAdapter.STARTRACK_TYPE97, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: risky toggles", EcarxVehicleAdapter.DRIVE_CREEP_SET,
                new String[]{"Creep on", "Creep off"},
                new int[]{EcarxVehicleAdapter.COMMON_ON, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Launch control on", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Launch control off", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Noise control on", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Noise control off", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Performance saving on", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Performance saving off", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Experimental: power-train-stop", EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP,
                new String[]{"Power train stop not blocked", "Power train stop EV blocked", "Power train stop HEV blocked", "Power train stop EV+ blocked"},
                new int[]{EcarxVehicleAdapter.POWER_TRAIN_STOP_NOT_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_HEV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_PLUS_BLOCKED});
    }

    private void showHud() {
        LinearLayout root = commandRoot("HUD / Cluster / OneOS");
        root.addView(Ui.text(this, new EcarxHudDimAdapter(this).availability(), 14, false));
        addDiagnostic(root, "HUD", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.HUD_DISPLAY_SAFETY);
        addCommand(root, "HUD включить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD выключить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "HUD calibration", EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD angle reset", EcarxVehicleAdapter.HUD_ANGLE_RESET, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD snow mode", EcarxVehicleAdapter.HUD_SNOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD safety on", EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD media on", EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD navi on", EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD phone on", EcarxVehicleAdapter.HUD_DISPLAY_BTPHONE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD drive env on", EcarxVehicleAdapter.HUD_DISPLAY_DRIVE_ENVIRONMENT, EcarxVehicleAdapter.COMMON_ON);
        addHudDimAction(root, "HUDInteraction: статус", a -> a.hudStatus());
        addHudDimAction(root, "HUDInteraction: height/sync", a -> a.hudSync());
        addHudDimAction(root, "DIMInteraction: статус", a -> a.dimStatus());
        addHudDimAction(root, "DIM: запрос day/night", a -> a.requestDayNightMode());
        addHudDimAction(root, "DIM: presentation on", a -> a.setPresentation(true));
        addHudDimAction(root, "DIM: presentation off", a -> a.setPresentation(false));
        addHudDimAction(root, "DIM Menu: IHU ready/theme", a -> a.dimMenuReadyAndTheme());
        addHudDimAction(root, "DIM Menu: вкладка навигации", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_NAVIGATION));
        addHudDimAction(root, "DIM Menu: вкладка музыки", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addHudDimAction(root, "DIM Menu: control center", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        addHudDimAction(root, "DIM Navi: simplify", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_SIMPLIFY));
        addHudDimAction(root, "DIM Navi: AR", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_AR));
        addHudDimAction(root, "DIM volume 10", a -> a.setDimVolume(false, 10));
        addHudDimAction(root, "DIM climate unit Celsius", a -> a.climateCelsiusUnit());
        addHudDimAction(root, "DIM climate temp 22.0C", a -> a.climateTemp(22.0f));
        addHudDimAction(root, "DIM avg fuel sample", a -> a.updateAvgFuelRanking(0, "{\"source\":\"GControl\",\"avg\":0}"));
        addHudDimAction(root, "DIM media mute", a -> a.publishMediaMuteState(1));
        addHudDimAction(root, "DIM media unmute", a -> a.publishMediaMuteState(0));
        addAudioExtAction(root, "AudioExt: bind services", a -> a.bindAudioExt());
        addAudioExtAction(root, "AudioExt: visualizer status", a -> a.visualizerStatus());
        addAudioExtAction(root, "AudioExt: media playing", a -> a.notifyMediaStatus(1, getPackageName()));
        addAudioExtAction(root, "AudioExt: media paused", a -> a.notifyMediaStatus(0, getPackageName()));
        addAudioExtAction(root, "AudioExt: VR active", a -> a.notifyVrStatus(1, 0));
        addAudioExtAction(root, "AudioExt: VR inactive", a -> a.notifyVrStatus(0, 0));
        addAudioExtAction(root, "AudioExt: PDC volume on", a -> a.notifyPdcVolumeSwitch(1));
        addAudioExtAction(root, "AudioExt: voice light 0.8", a -> a.voiceLight(0.8f));
        addAudioExtAction(root, "AudioExt: anti-shake on", a -> a.antiShake(true, 0.5f));
        addAudioExtAction(root, "AudioExt: loudness on", a -> a.loudness(true));
        addAudioExtAction(root, "AudioExt: section max on", a -> a.useSectionMax(true));
        addAudioExtAction(root, "AudioExt: voice base -35dB", a -> a.voiceDb(-35));
        addAudioExtAction(root, "AudioExt: spectrum preset", a -> a.spectrumPreset(0, 1, 1.0f, 1.0f));
        Button hud = Ui.button(this, "Запустить HUD service");
        hud.setOnClickListener(v -> startForegroundService(new Intent(this, HudPresentationService.class)));
        Button observer = Ui.button(this, "Запустить HUD observer");
        observer.setOnClickListener(v -> startForegroundService(new Intent(this, HudObserverService.class)));
        Button cluster = Ui.button(this, "Запустить Cluster bridge");
        cluster.setOnClickListener(v -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
        root.addView(hud);
        root.addView(observer);
        root.addView(cluster);
    }

    private void addHudDimAction(LinearLayout root, String label, HudDimAction action) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxHudDimAdapter.Result result;
            try {
                result = action.run(new EcarxHudDimAdapter(this));
            } catch (Exception e) {
                result = EcarxHudDimAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "OneOS команда отправлена" : "OneOS команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    interface HudDimAction {
        EcarxHudDimAdapter.Result run(EcarxHudDimAdapter adapter);
    }

    private void addAudioExtAction(LinearLayout root, String label, AudioExtAction action) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            AudioExtServiceAdapter.Result result;
            try {
                result = action.run(new AudioExtServiceAdapter(this));
            } catch (Exception e) {
                result = AudioExtServiceAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "AudioExt команда отправлена" : "AudioExt команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(b);
    }

    interface AudioExtAction {
        AudioExtServiceAdapter.Result run(AudioExtServiceAdapter adapter);
    }
    private void showLauncher() { startActivity(new Intent(this, DesktopActivity.class)); }
    private void showSystem() { panel("ADB / Система", "ADB toggle, локальный shell, adb-grants, DPI/масштаб, автозум, автозапуск, watchdog и accessibility tracking."); }
    private void showWeb() { startActivity(new Intent(this, WeatherActivity.class)); }
    private void openSplitLauncher() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(apps, Comparator.comparing(a -> a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        if (apps.isEmpty()) {
            Ui.toast(this, "Нет приложений для split-запуска");
            return;
        }
        String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) labels[i] = apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Запустить рядом").setItems(labels, (d, which) -> {
            ResolveInfo info = apps.get(which);
            Intent launch = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(info.activityInfo.packageName, info.activityInfo.name);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(launch);
        }).show();
    }
}
