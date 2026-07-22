package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.regex.*;

public class VoiceActivity extends Activity {
    private final VoskVoiceRecognizer recognizer = new VoskVoiceRecognizer();
    private TextView result;
    private LinearLayout commands;
    private EditText commandInput;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        startForegroundService(new Intent(this, VoiceForegroundService.class));
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, "Голосовой ассистент", this::finish);
        LinearLayout hero = Ui.card(this);
        hero.addView(Ui.text(this, "Голосовые команды", 22, true));
        hero.addView(Ui.muted(this, "Vosk работает локально. Команды можно редактировать: фраза слева, действие справа."));
        root.addView(hero, margin(0, 8, 0, 12));
        EditText input = new EditText(this);
        commandInput = input;
        input.setHint("Введите или продиктуйте команду");
        input.setText(getIntent().getStringExtra("command"));
        styleInput(input);
        Button run = Ui.button(this, "Выполнить");
        result = Ui.text(this, "", 16, true);
        String source = getIntent().getStringExtra("source");
        String event = getIntent().getStringExtra("event");
        if (source != null || event != null) {
            result.setText("Источник: " + source + "\n" + event);
        }
        Button listen = Ui.button(this, "Слушать Vosk");
        Button stop = Ui.button(this, "Стоп");
        run.setOnClickListener(v -> {
            String cmd = input.getText().toString().toLowerCase(Locale.ROOT);
            result.setText(runVoiceCommand(cmd));
        });
        listen.setOnClickListener(v -> recognizer.start(this, text -> runOnUiThread(() -> result.setText(text))));
        stop.setOnClickListener(v -> recognizer.stop());
        LinearLayout commandCard = Ui.card(this);
        commandCard.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 56)));
        LinearLayout row = Ui.row(this);
        row.addView(run, buttonLp());
        row.addView(listen, buttonLp());
        row.addView(stop, buttonLp());
        commandCard.addView(row);
        commandCard.addView(result);
        root.addView(commandCard, margin(0, 0, 0, 12));
        commands = Ui.card(this);
        root.addView(commands);
        renderCommands(input);
        scroll.addView(root);
        setContentView(scroll);
    }

    private String runVoiceCommand(String cmd) {
        if (cmd.trim().isEmpty()) return "Пустая команда";
        String alias = aliasFor(cmd);
        if (alias != null && !alias.equals(cmd)) cmd = alias.toLowerCase(Locale.ROOT);
        AutomationEngine.runTrigger(this, "voice", cmd);

        EcarxVehicleAdapter.Result[] preset = parsePreset(cmd);
        if (preset != null) return describePreset(cmd, preset);

        EcarxVehicleAdapter.Result result = parseVehicleCommand(cmd);
        if (result != null) return result.message;

        CarCommandBus.send(this, "voice", cmd);
        return "Команда отправлена в broadcast: " + cmd;
    }

    private void renderCommands(EditText input) {
        commands.removeAllViews();
        LinearLayout head = Ui.row(this);
        head.addView(Ui.text(this, "Доступные команды", 18, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button add = Ui.iconButton(this, "+", "Добавить команду");
        add.setOnClickListener(v -> editAlias("", ""));
        head.addView(add, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44)));
        commands.addView(head);
        for (String item : aliases()) {
            String[] parts = item.split("\\|", 2);
            String phrase = parts[0];
            String action = parts.length > 1 ? parts[1] : parts[0];
            Button b = Ui.button(this, phrase + "  ->  " + action);
            b.setOnClickListener(v -> {
                EditText target = input != null ? input : commandInput;
                if (target != null) target.setText(phrase);
            });
            b.setOnLongClickListener(v -> { editAlias(phrase, action); return true; });
            commands.addView(b, margin(0, 4, 0, 4));
        }
    }

    private Set<String> aliases() {
        LinkedHashSet<String> defaults = new LinkedHashSet<>();
        defaults.add("включи климат|климат включить");
        defaults.add("охлади салон|пресет охлаждение");
        defaults.add("зимний режим|зима");
        defaults.add("открой камеры|камера 360");
        defaults.add("включи подогрев руля|подогрев руля");
        return getPreferences(0).getStringSet("aliases", defaults);
    }

    private String aliasFor(String cmd) {
        String normalized = cmd.trim().toLowerCase(Locale.ROOT);
        for (String item : aliases()) {
            String[] parts = item.split("\\|", 2);
            if (parts.length == 2 && normalized.equals(parts[0].trim().toLowerCase(Locale.ROOT))) return parts[1];
        }
        return null;
    }

    private void editAlias(String oldPhrase, String oldAction) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 18);
        box.setPadding(p, p, p, 0);
        EditText phrase = new EditText(this);
        phrase.setHint("Фраза");
        phrase.setText(oldPhrase);
        styleInput(phrase);
        EditText action = new EditText(this);
        action.setHint("Команда для выполнения");
        action.setText(oldAction);
        styleInput(action);
        box.addView(phrase, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 56)));
        box.addView(action, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 56)));
        new AlertDialog.Builder(this)
                .setView(box)
                .setPositiveButton("Сохранить", (d, w) -> {
                    LinkedHashSet<String> items = new LinkedHashSet<>(aliases());
                    if (!oldPhrase.isEmpty()) items.remove(oldPhrase + "|" + oldAction);
                    items.add(phrase.getText().toString().trim() + "|" + action.getText().toString().trim());
                    getPreferences(0).edit().putStringSet("aliases", items).apply();
                    renderCommands(null);
                })
                .setNegativeButton("Удалить", (d, w) -> {
                    LinkedHashSet<String> items = new LinkedHashSet<>(aliases());
                    items.remove(oldPhrase + "|" + oldAction);
                    getPreferences(0).edit().putStringSet("aliases", items).apply();
                    renderCommands(null);
                })
                .show();
    }

    private void styleInput(EditText e) {
        e.setTextColor(Ui.textColor(this));
        e.setHintTextColor(Ui.mutedColor(this));
        e.setSingleLine(true);
        e.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        e.setBackground(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 14), Ui.lineColor(this)));
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4), 0);
        return lp;
    }

    private EcarxVehicleAdapter.Result parseVehicleCommand(String cmd) {
        EcarxVehicleAdapter.Result temperature = parseTemperatureCommand(cmd);
        if (temperature != null) return temperature;

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

    @Override protected void onDestroy() {
        recognizer.stop();
        super.onDestroy();
    }
}
