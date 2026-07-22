package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class WeatherActivity extends Activity {
    private TextView result;
    private WeatherIconView icon;
    private LinearLayout bookmarks;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, "Браузер / Погода", this::finish);
        icon = new WeatherIconView(this);
        LinearLayout weatherCard = Ui.card(this);
        weatherCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        LinearLayout weatherRow = Ui.row(this);
        weatherRow.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 150), Ui.dp(this, 130)));
        LinearLayout weatherText = new LinearLayout(this);
        weatherText.setOrientation(LinearLayout.VERTICAL);
        weatherText.addView(Ui.muted(this, "Текущая погода"));
        result = Ui.text(this, "Нажмите обновить, чтобы загрузить данные Open-Meteo.", 20, true);
        weatherText.addView(result);
        weatherRow.addView(weatherText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        weatherCard.addView(weatherRow);
        root.addView(weatherCard, margin(0, 8, 0, 14));

        EditText lat = new EditText(this);
        lat.setHint("Широта");
        lat.setText("55.7558");
        styleInput(lat);
        EditText lon = new EditText(this);
        lon.setHint("Долгота");
        lon.setText("37.6173");
        styleInput(lon);
        EditText address = new EditText(this);
        address.setHint("Сайт или поисковый запрос");
        address.setText("https://www.google.com");
        styleInput(address);
        Button weather = Ui.button(this, "Обновить погоду");
        Button browser = Ui.button(this, "Открыть сайт");
        Button search = Ui.button(this, "Найти");
        Button bookmark = Ui.button(this, "В закладки");
        weather.setOnClickListener(v -> load(lat.getText().toString(), lon.getText().toString()));
        browser.setOnClickListener(v -> open(normalizeUrl(address.getText().toString())));
        search.setOnClickListener(v -> open("https://www.google.com/search?q=" + encode(address.getText().toString())));
        bookmark.setOnClickListener(v -> {
            String url = normalizeUrl(address.getText().toString());
            LinkedHashSet<String> items = new LinkedHashSet<>(getPreferences(0).getStringSet("bookmarks", new LinkedHashSet<>()));
            items.add(url);
            getPreferences(0).edit().putStringSet("bookmarks", items).apply();
            renderBookmarks(address);
        });
        LinearLayout controls = Ui.card(this);
        controls.addView(Ui.muted(this, "Координаты"));
        LinearLayout coords = Ui.row(this);
        coords.addView(lat, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        coords.addView(lon, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        controls.addView(coords);
        controls.addView(weather, margin(0, 8, 0, 12));
        controls.addView(Ui.muted(this, "Браузер"));
        controls.addView(address, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));
        LinearLayout actions = Ui.row(this);
        actions.addView(browser, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        actions.addView(search, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        actions.addView(bookmark, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1));
        controls.addView(actions, margin(0, 8, 0, 0));
        root.addView(controls, margin(0, 0, 0, 14));
        bookmarks = Ui.card(this);
        root.addView(bookmarks);
        renderBookmarks(address);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void renderBookmarks(EditText address) {
        bookmarks.removeAllViews();
        bookmarks.addView(Ui.text(this, "Закладки", 18, true));
        Set<String> items = getPreferences(0).getStringSet("bookmarks", new LinkedHashSet<>());
        if (items.isEmpty()) bookmarks.addView(Ui.muted(this, "Сохраненные сайты появятся здесь."));
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
                renderBookmarks(address);
                return true;
            });
            bookmarks.addView(b, margin(0, 4, 0, 4));
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
                double temp = current.optDouble("temperature_2m");
                double wind = current.optDouble("wind_speed_10m");
                int code = current.optInt("weather_code");
                String text = String.format(Locale.US, "%.1f C\nВетер %.0f км/ч\n%s", temp, wind, weatherName(code));
                runOnUiThread(() -> {
                    result.setText(text);
                    icon.setWeather(code);
                });
            } catch (Exception e) {
                runOnUiThread(() -> result.setText("Ошибка погоды: " + e.getMessage()));
            }
        }).start();
    }

    private void styleInput(EditText e) {
        e.setTextColor(Ui.textColor(this));
        e.setHintTextColor(Ui.mutedColor(this));
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        e.setBackground(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 14), Ui.lineColor(this)));
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private String weatherName(int code) {
        if (code == 0) return "Ясно";
        if (code <= 3) return "Переменная облачность";
        if (code >= 45 && code <= 48) return "Туман";
        if (code >= 51 && code <= 67) return "Дождь";
        if (code >= 71 && code <= 77) return "Снег";
        if (code >= 80 && code <= 82) return "Ливень";
        if (code >= 95) return "Гроза";
        return "Код погоды " + code;
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

    private static final class WeatherIconView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int code = 2;

        WeatherIconView(Context c) { super(c); }

        void setWeather(int code) {
            this.code = code;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            boolean rainy = code >= 51 && code <= 99;
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, w, h, Color.rgb(58, 134, 218), rainy ? Color.rgb(70, 88, 126) : Color.rgb(242, 181, 76), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(w * .08f, h * .08f, w * .92f, h * .92f), Ui.dp(getContext(), 26), Ui.dp(getContext(), 26), p);
            p.setShader(null);
            p.setColor(Color.argb(90, 255, 255, 255));
            canvas.drawCircle(w * .34f, h * .36f, w * .18f, p);
            p.setColor(Color.WHITE);
            canvas.drawCircle(w * .44f, h * .55f, w * .22f, p);
            canvas.drawCircle(w * .58f, h * .50f, w * .18f, p);
            canvas.drawRoundRect(new RectF(w * .28f, h * .52f, w * .75f, h * .70f), Ui.dp(getContext(), 18), Ui.dp(getContext(), 18), p);
            if (rainy) {
                p.setColor(Color.rgb(176, 220, 255));
                p.setStrokeWidth(Ui.dp(getContext(), 4));
                for (int i = 0; i < 4; i++) canvas.drawLine(w * (.32f + i * .11f), h * .76f, w * (.27f + i * .11f), h * .90f, p);
            }
        }
    }
}
