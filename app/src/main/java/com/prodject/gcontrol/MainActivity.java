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
        add(grid, "HUD / OneOS", this::showHud);
        add(grid, "Браузер / Погода", this::showWeb);
        add(grid, "Рабочий стол", this::showLauncher);
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
    private void showCar() {
        LinearLayout root = commandRoot("Управление автомобилем");
        root.addView(Ui.text(this, "BCM-функции из IBcm.smali. Часть команд зональная; zone=0 используется как базовый fallback.", 14, false));
        addDiagnostic(root, "BCM / Drive / Seat", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.SEAT_POSITION_SET);
        addCommand(root, "Окна открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN);
        addCommand(root, "Окна закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE);
        addCommand(root, "Окна пауза", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_PAUSE);
        addCommand(root, "Блокировка окон вкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Блокировка окон выкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Двери открыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_OPEN);
        addCommand(root, "Двери закрыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_CLOSE);
        addCommand(root, "Двери пауза", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_PAUSE);
        addCommand(root, "Door lock вкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Door lock выкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Child safety lock вкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Child safety lock выкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Автозакрытие дверей по скорости вкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Автозакрытие дверей по скорости выкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Все двери one key", EcarxVehicleAdapter.BCM_ALL_DOORS_ONE_KEY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк открыть", EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк закрыть", EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка открыть", EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка закрыть", EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала сложить/переключить", EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON);
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
        addCommand(root, "Аварийка вкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Аварийка выкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Передние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Передние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Задние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Задние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome light вкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome light выкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Custom key: багажник", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        addCommand(root, "Custom key: 360 камера", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "Custom key: DVR", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR);
        root.addView(Ui.text(this, "Режимы движения из IDriveMode.smali.", 14, false));
        addCommand(root, "Drive Eco", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO);
        addCommand(root, "Drive Comfort", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT);
        addCommand(root, "Drive Dynamic", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC);
        addCommand(root, "Drive Snow", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW);
        addCommand(root, "Drive Offroad", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD);
        addCommand(root, "Eco button", EcarxVehicleAdapter.DRIVE_ECO_BUTTON, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Руль мягкий", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT);
        addCommand(root, "Руль динамичный", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC);
        root.addView(Ui.text(this, "Профили и регулировки сидений из ISeat.smali.", 14, false));
        addCommand(root, "Сиденье вперед", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_FORWARD);
        addCommand(root, "Сиденье назад", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_BACKWARD);
        addCommand(root, "Сиденье выше", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_UP);
        addCommand(root, "Сиденье ниже", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN);
        addCommand(root, "Спинка вперед", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD);
        addCommand(root, "Спинка назад", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD);
        addCommand(root, "Сохранить профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Сохранить профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Вызвать профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Вызвать профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Сиденье comfort", EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Сиденье restore", EcarxVehicleAdapter.SEAT_RESTORE, EcarxVehicleAdapter.COMMON_ON);
    }

    private void showClimate() {
        LinearLayout root = commandRoot("Климат");
        root.addView(Ui.text(this, "HVAC-функции из IHvac.smali. Для сидений и зон сейчас используется zone=0 fallback.", 14, false));
        addDiagnostic(root, "HVAC", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.HVAC_TEMP_MIN, EcarxVehicleAdapter.HVAC_TEMP_MAX, EcarxVehicleAdapter.HVAC_TEMP_STEP);
        addDiagnostic(root, "HVAC расширенный", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.HVAC_FILTER_ELEMENT_LIFE, EcarxVehicleAdapter.HVAC_MODULE_CONNECT_STATUS);
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
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addPreset(root, "Пресет Зима",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2),
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
        addCommand(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addCommand(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1);
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
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2),
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
        addCommand(root, "Seat vent off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Seat vent 2", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2);
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
        addCommand(root, "AI driver assist вкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AI driver assist выкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "AI lane strategy gentle", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_GENTLE);
        addCommand(root, "AI lane strategy standard", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_STANDARD);
        addCommand(root, "AI lane strategy radical", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_RADICAL);
        addCommand(root, "AI lane warning voice", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VOICE);
        addCommand(root, "AI lane warning vibrate", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VIBRATE);
        addCommand(root, "AI lane warning both", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_BOTH);
        addCommand(root, "AI lane warning off", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING, EcarxVehicleAdapter.COMMON_OFF);
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
