package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class VoiceActivity extends Activity {
    private final VoskVoiceRecognizer recognizer = new VoskVoiceRecognizer();
    private TextView result;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        startForegroundService(new Intent(this, VoiceForegroundService.class));
        LinearLayout root = Ui.root(this, "Голосовой ассистент");
        root.addView(Ui.text(this, "Экран прослушивания. Локальная модель: assets/vosk-model-ru, нативная библиотека: libvosk.so. Поддерживаются app.monji.VOICE, VOICE_COMMAND и ASSIST.", 16, false));
        EditText input = new EditText(this);
        input.setHint("Введите или продиктуйте команду");
        Button run = Ui.button(this, "Выполнить");
        result = Ui.text(this, "", 16, true);
        Button listen = Ui.button(this, "Слушать Vosk");
        Button stop = Ui.button(this, "Стоп");
        run.setOnClickListener(v -> {
            String cmd = input.getText().toString().toLowerCase(Locale.ROOT);
            result.setText(runVoiceCommand(cmd));
        });
        listen.setOnClickListener(v -> recognizer.start(this, text -> runOnUiThread(() -> result.setText(text))));
        stop.setOnClickListener(v -> recognizer.stop());
        root.addView(input);
        root.addView(run);
        root.addView(listen);
        root.addView(stop);
        root.addView(result);
        setContentView(root);
    }

    private String runVoiceCommand(String cmd) {
        if (cmd.trim().isEmpty()) return "Пустая команда";

        EcarxVehicleAdapter.Result[] preset = parsePreset(cmd);
        if (preset != null) return describePreset(cmd, preset);

        EcarxVehicleAdapter.Result result = parseVehicleCommand(cmd);
        if (result != null) return result.message;

        CarCommandBus.send(this, "voice", cmd);
        return "Команда отправлена в broadcast: " + cmd;
    }

    private EcarxVehicleAdapter.Result parseVehicleCommand(String cmd) {
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
        if (has(cmd, "окн") && (has(cmd, "откр") || has(cmd, "open"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN);
        }
        if (has(cmd, "окн") && (has(cmd, "закр") || has(cmd, "close"))) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE);
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
        if (has(cmd, "руль") && has(cmd, "подогрев")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.WHEEL_HEAT_MID);
        }
        if (has(cmd, "сиден") && has(cmd, "подогрев")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_SEAT_HEATING, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.SEAT_LEVEL_2);
        }
        if (has(cmd, "сиден") && has(cmd, "вент")) {
            return CarCommandBus.sendVehicle(this, EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, off(cmd) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.SEAT_LEVEL_2);
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
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        }
        if (has(cmd, "охлаж") || has(cmd, "cool")) {
            return adapter.setAll(
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                    new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2));
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

    private boolean has(String cmd, String value) {
        return cmd.contains(value);
    }

    @Override protected void onDestroy() {
        recognizer.stop();
        super.onDestroy();
    }
}
