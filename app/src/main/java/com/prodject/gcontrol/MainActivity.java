package com.prodject.gcontrol;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
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

    private void showDvr() {
        startForegroundService(new Intent(this, DvrService.class));
        panel("Monji DVR", "Запись со штатных камер: передняя ADAS, левая, задняя, правая. Настройки: выбор камер, длина сегмента, лимит диска, внутренняя память или USB. Сервис DVR запущен.");
    }
    private void showCar() {
        LinearLayout root = commandRoot("Управление автомобилем");
        root.addView(Ui.text(this, "BCM-функции из IBcm.smali. Часть команд зональная; zone=0 используется как базовый fallback.", 14, false));
        addCommand(root, "Окна открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN);
        addCommand(root, "Окна закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE);
        addCommand(root, "Окна пауза", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_PAUSE);
        addCommand(root, "Блокировка окон вкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Блокировка окон выкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Люк открыть", EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк закрыть", EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка открыть", EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка закрыть", EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала сложить/переключить", EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Свет салона вкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Свет салона выкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Custom key: багажник", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        addCommand(root, "Custom key: 360 камера", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "Custom key: DVR", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR);
    }

    private void showClimate() {
        LinearLayout root = commandRoot("Климат");
        root.addView(Ui.text(this, "HVAC-функции из IHvac.smali. Для сидений и зон сейчас используется zone=0 fallback.", 14, false));
        addCommand(root, "Климат включить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат выключить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C включить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C выключить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C Max", EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto climate", EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Eco climate", EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Вентилятор 1", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        addCommand(root, "Вентилятор 3", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
        addCommand(root, "Вентилятор 5", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
        addCommand(root, "Рециркуляция внутренняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER);
        addCommand(root, "Рециркуляция внешняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE);
        addCommand(root, "Обдув лобового", EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Обогрев заднего стекла", EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Подогрев сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addCommand(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addCommand(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addCommand(root, "Подогрев руля low", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_LOW);
        addCommand(root, "Подогрев руля off", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showAdas() {
        LinearLayout root = commandRoot("ADAS / Вождение");
        root.addView(Ui.text(this, "ADAS-функции из IADAS.smali.", 14, false));
        addCommand(root, "AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AEB выключить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "FCW включить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LDW включить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCW включить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ELKA включить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Speed limit warning", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Парктроник включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON);
    }

    private void showHud() {
        LinearLayout root = commandRoot("HUD / Cluster / OneOS");
        root.addView(Ui.text(this, "HUD/Cluster пока подключены как сервисные entry points. Следующий этап: com.ecarx.xui.adaptapi.hudinteraction и IHUD.", 14, false));
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
    private void showLauncher() { startActivity(new Intent(this, DesktopActivity.class)); }
    private void showSystem() { panel("ADB / Система", "ADB toggle, локальный shell, adb-grants, DPI/масштаб, автозум, автозапуск, watchdog и accessibility tracking."); }
    private void showWeb() { startActivity(new Intent(this, WeatherActivity.class)); }
    private void openSplitLauncher() {
        Intent i = getPackageManager().getLaunchIntentForPackage("com.android.settings");
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(i);
        } else Ui.toast(this, "Не найдено приложение для split-запуска");
    }
}
