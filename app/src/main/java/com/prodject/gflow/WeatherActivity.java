package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class WeatherActivity extends Activity {
    private TextView result;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this, "Браузер / Погода");
        EditText lat = new EditText(this);
        lat.setHint("Широта");
        lat.setText("55.7558");
        EditText lon = new EditText(this);
        lon.setHint("Долгота");
        lon.setText("37.6173");
        EditText address = new EditText(this);
        address.setHint("Сайт или поисковый запрос");
        address.setText("https://www.google.com");
        Button weather = Ui.button(this, "Получить погоду");
        Button browser = Ui.button(this, "Открыть сайт");
        Button search = Ui.button(this, "Поиск");
        Button bookmark = Ui.button(this, "Добавить закладку");
        result = Ui.text(this, "", 16, false);
        weather.setOnClickListener(v -> load(lat.getText().toString(), lon.getText().toString()));
        browser.setOnClickListener(v -> open(normalizeUrl(address.getText().toString())));
        search.setOnClickListener(v -> open("https://www.google.com/search?q=" + encode(address.getText().toString())));
        bookmark.setOnClickListener(v -> {
            String url = normalizeUrl(address.getText().toString());
            LinkedHashSet<String> items = new LinkedHashSet<>(getPreferences(0).getStringSet("bookmarks", new LinkedHashSet<>()));
            items.add(url);
            getPreferences(0).edit().putStringSet("bookmarks", items).apply();
            renderBookmarks(root, address);
        });
        root.addView(lat);
        root.addView(lon);
        root.addView(weather);
        root.addView(address);
        root.addView(browser);
        root.addView(search);
        root.addView(bookmark);
        renderBookmarks(root, address);
        root.addView(result);
        setContentView(root);
    }

    private void renderBookmarks(LinearLayout root, EditText address) {
        root.addView(Ui.text(this, "Закладки", 18, true));
        Set<String> items = getPreferences(0).getStringSet("bookmarks", new LinkedHashSet<>());
        for (String url : items) {
            Button b = Ui.button(this, url);
            b.setOnClickListener(v -> {
                address.setText(url);
                open(url);
            });
            b.setOnLongClickListener(v -> {
                LinkedHashSet<String> copy = new LinkedHashSet<>(getPreferences(0).getStringSet("bookmarks", new LinkedHashSet<>()));
                copy.remove(url);
                getPreferences(0).edit().putStringSet("bookmarks", copy).apply();
                Ui.toast(this, "Закладка удалена, откройте экран заново");
                return true;
            });
            root.addView(b);
        }
    }

    private void open(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
    }

    private String normalizeUrl(String input) {
        String value = input.trim();
        if (value.isEmpty()) return "https://www.google.com";
        if (value.startsWith("http://") || value.startsWith("https://")) return value;
        if (value.contains(".") && !value.contains(" ")) return "https://" + value;
        return "https://www.google.com/search?q=" + encode(value);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private void load(String lat, String lon) {
        result.setText("Загружаю...");
        new Thread(() -> {
            try {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=" + URLEncoder.encode(lat, "UTF-8") +
                        "&longitude=" + URLEncoder.encode(lon, "UTF-8") + "&current=temperature_2m,wind_speed_10m,weather_code";
                JSONObject json = new JSONObject(read(new URL(url)));
                JSONObject current = json.getJSONObject("current");
                String text = "Температура: " + current.optDouble("temperature_2m") + " C\n" +
                        "Ветер: " + current.optDouble("wind_speed_10m") + " км/ч\n" +
                        "Код погоды: " + current.optInt("weather_code");
                runOnUiThread(() -> result.setText(text));
            } catch (Exception e) {
                runOnUiThread(() -> result.setText("Ошибка погоды: " + e.getMessage()));
            }
        }).start();
    }

    private String read(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0;) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }
}
