package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.io.*;

public class AdbShellActivity extends Activity {
    private TextView output;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this, "ADB / Система");
        EditText command = new EditText(this);
        command.setHint("Команда shell");
        command.setText("settings get global adb_enabled");
        Button run = Ui.button(this, "Выполнить");
        Button adb = Ui.button(this, "Переключить ADB");
        Button dpi = Ui.button(this, "DPI 440");
        Button grants = Ui.button(this, "Проверить grants");
        Button accessibility = Ui.button(this, "Открыть Accessibility");
        Button writeSettings = Ui.button(this, "Открыть WRITE_SETTINGS");
        Button allFiles = Ui.button(this, "Открыть All files access");
        Button appInfo = Ui.button(this, "Открыть карточку приложения");
        EditText zoomPackages = new EditText(this);
        zoomPackages.setHint("Autozoom packages: com.nav, maps, browser");
        EditText zoomScale = new EditText(this);
        zoomScale.setHint("Autozoom scale");
        Button zoom = Ui.button(this, "Сохранить autozoom");
        Button zoomToggle = Ui.button(this, "Autozoom вкл/выкл");
        output = Ui.text(this, "", 14, false);
        SharedPreferences watchdog = getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE);
        zoomPackages.setText(watchdog.getString(AppWatchdogAccessibilityService.KEY_PACKAGES, "maps,navi,browser"));
        zoomScale.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        zoomScale.setText(String.valueOf(watchdog.getFloat(AppWatchdogAccessibilityService.KEY_SCALE, 1.15f)));
        run.setOnClickListener(v -> execute(command.getText().toString()));
        adb.setOnClickListener(v -> toggleAdb());
        dpi.setOnClickListener(v -> execute("wm density 440"));
        grants.setOnClickListener(v -> showGrants());
        accessibility.setOnClickListener(v -> openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        writeSettings.setOnClickListener(v -> openSettings(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())));
        allFiles.setOnClickListener(v -> openSettings(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        appInfo.setOnClickListener(v -> openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
        zoom.setOnClickListener(v -> saveAutozoom(zoomPackages.getText().toString(), zoomScale.getText().toString()));
        zoomToggle.setOnClickListener(v -> toggleAutozoom());
        root.addView(command);
        root.addView(run);
        root.addView(adb);
        root.addView(dpi);
        root.addView(grants);
        root.addView(accessibility);
        root.addView(writeSettings);
        root.addView(allFiles);
        root.addView(appInfo);
        root.addView(Ui.text(this, "Autozoom меняет Settings.System.FONT_SCALE для выбранных приложений через AccessibilityService.", 14, false));
        root.addView(zoomPackages);
        root.addView(zoomScale);
        root.addView(zoom);
        root.addView(zoomToggle);
        root.addView(output, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void toggleAdb() {
        try {
            int current = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, current == 1 ? 0 : 1);
            output.setText("ADB: " + (current == 1 ? "выключен" : "включен"));
        } catch (Exception e) {
            output.setText("Нужен WRITE_SECURE_SETTINGS/adb-grants: " + e.getMessage());
        }
    }

    private void execute(String cmd) {
        new Thread(() -> {
            try {
                java.lang.Process p = new ProcessBuilder("sh", "-c", cmd).redirectErrorStream(true).start();
                String text = read(p.getInputStream());
                int code = p.waitFor();
                runOnUiThread(() -> output.setText("$ " + cmd + "\nexit " + code + "\n" + text));
            } catch (Exception e) {
                runOnUiThread(() -> output.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void showGrants() {
        String pkg = getPackageName();
        output.setText("ADB grants для " + pkg + ":\n" +
                "adb shell pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS\n" +
                "adb shell appops set " + pkg + " GET_USAGE_STATS allow\n" +
                "adb shell appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow\n" +
                "adb shell appops set " + pkg + " android:system_alert_window allow\n" +
                "adb shell settings get global adb_enabled\n\n" +
                "Проверка внутри приложения:\n" +
                "WRITE_SETTINGS=" + Settings.System.canWrite(this) + "\n" +
                "MANAGE_EXTERNAL_STORAGE=" + (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()) + "\n" +
                "Accessibility last package=" + getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE).getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "") + "\n" +
                "SDK=" + Build.VERSION.SDK_INT + "\n" +
                "Last foreground package=" + getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE).getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, ""));
    }

    private void openSettings(String action) {
        openSettings(action, null);
    }

    private void openSettings(String action, Uri data) {
        try {
            Intent intent = new Intent(action);
            if (data != null) intent.setData(data);
            startActivity(intent);
        } catch (Exception e) {
            output.setText("Не удалось открыть настройки: " + e.getMessage());
        }
    }

    private void saveAutozoom(String packages, String scale) {
        float value = parseFloat(scale, 1.15f);
        getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE).edit()
                .putString(AppWatchdogAccessibilityService.KEY_PACKAGES, packages)
                .putFloat(AppWatchdogAccessibilityService.KEY_SCALE, Math.max(0.85f, Math.min(1.6f, value)))
                .apply();
        output.setText("Autozoom packages сохранены: " + packages + "\nscale=" + Math.max(0.85f, Math.min(1.6f, value)));
    }

    private void toggleAutozoom() {
        SharedPreferences prefs = getSharedPreferences(AppWatchdogAccessibilityService.PREFS, MODE_PRIVATE);
        boolean next = !prefs.getBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, false);
        prefs.edit().putBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, next).apply();
        output.setText("Autozoom: " + (next ? "включен" : "выключен") + "\nПоследний пакет: " + prefs.getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, ""));
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n; (n = in.read(buf)) > 0;) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }
}
