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
        add(grid, "DVR / Камеры", this::showDvr);
        add(grid, "Голос", () -> startActivity(new Intent(this, VoiceActivity.class)));
        add(grid, "Автомобиль", this::showCar);
        add(grid, "Климат", this::showClimate);
        add(grid, "ADAS", this::showAdas);
        add(grid, "HUD / OneOS", this::showHud);
        add(grid, "Браузер / Погода", this::showWeb);
        add(grid, "Рабочий стол", this::showLauncher);
        add(grid, "ADB / Система", this::showSystem);
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

    private void showDvr() {
        startForegroundService(new Intent(this, DvrService.class));
        panel("Monji DVR", "Запись со штатных камер: передняя ADAS, левая, задняя, правая. Настройки: выбор камер, длина сегмента, лимит диска, внутренняя память или USB. Сервис DVR запущен.");
    }
    private void showCar() { panel("Управление автомобилем", CarCommandBus.supportedCommands()); }
    private void showClimate() { panel("Климатические пресеты", "Сохранение текущего климата, создание/изменение/удаление пресетов, шаблоны комфорт/охлаждение/лето/зима, виджет и comfort climate экран."); }
    private void showAdas() { panel("ADAS / Вождение", "AEB, режим после старта, ELKA, FCW, LKA, LDW, RCW, предупреждение о скорости и удержание выбранного режима."); }
    private void showHud() { panel("HUD / Cluster / OneOS", "HUD presentation service, observer service, cluster bridge, media session listener, кнопки руля, ECARX/Geely intents."); }
    private void showLauncher() { panel("Рабочий стол Monji", "Док приложений, выбор и порядок иконок, удаление с главной, запуск удаления приложений, темы, обои, часы, погода и виджеты."); }
    private void showSystem() { panel("ADB / Система", "ADB toggle, локальный shell, adb-grants, DPI/масштаб, автозум, автозапуск, watchdog и accessibility tracking."); }
    private void showWeb() {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"));
        startActivity(i);
    }
    private void openSplitLauncher() {
        Intent i = getPackageManager().getLaunchIntentForPackage("com.android.settings");
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(i);
        } else Ui.toast(this, "Не найдено приложение для split-запуска");
    }
}
