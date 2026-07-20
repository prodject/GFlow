package com.prodject.gcontrol;

import android.app.*;
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
        output = Ui.text(this, "", 14, false);
        run.setOnClickListener(v -> execute(command.getText().toString()));
        adb.setOnClickListener(v -> toggleAdb());
        dpi.setOnClickListener(v -> execute("wm density 440"));
        root.addView(command);
        root.addView(run);
        root.addView(adb);
        root.addView(dpi);
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
                Process p = new ProcessBuilder("sh", "-c", cmd).redirectErrorStream(true).start();
                String text = read(p.getInputStream());
                int code = p.waitFor();
                runOnUiThread(() -> output.setText("$ " + cmd + "\nexit " + code + "\n" + text));
            } catch (Exception e) {
                runOnUiThread(() -> output.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n; (n = in.read(buf)) > 0;) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }
}
