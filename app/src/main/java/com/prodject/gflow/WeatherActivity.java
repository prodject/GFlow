package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherActivity extends Activity {
    private static final int REQ_LOCATION = 1201;
    private static final String KEY_BOOKMARKS = "bookmarks";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_FORECAST = "forecast";

    private LinearLayout contentHost;
    private TextView weatherSummaryView;
    private TextView forecastView;
    private EditText latInput;
    private EditText lonInput;
    private EditText addressInput;
    private WeatherIconView iconView;
    private WeatherState state = WeatherState.placeholder();
    private Mode mode = Mode.HOME;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildShell());
        restoreInputs();
        restoreState();
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onPause() {
        super.onPause();
        persistInputs();
    }

    @Override protected void onResume() {
        super.onResume();
        restoreState();
        renderContent();
    }

    private View buildShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(Ui.dashboardBg(this));
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        LinearLayout dock = buildBottomDock();
        root.addView(dock, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        Ui.animateIn(dock, 220, 18f);
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildWeatherPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildBrowserPanel(), lpMatchWrap(0, 0, 0, 16));
        if (mode == Mode.BOOKMARKS) contentHost.addView(buildBookmarksPanel(), lpMatchWrap(0, 0, 0, 16));
        else contentHost.addView(buildForecastPanel(), lpMatchWrap(0, 0, 0, 16));
        Ui.staggerIn(collectChildren(contentHost), 30, 55);
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        Ui.press(back, () -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, mode == Mode.BOOKMARKS ? "Weather Bookmarks / Browser" : "Climate Outside / Browser"));
        titleBlock.addView(Ui.text(this, "Браузер / Погода", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Temp", state.temperatureLabel()));
        bar.addView(buildTopStat("Wind", state.windLabel()));
        bar.addView(buildTopStat("Bookmarks", String.valueOf(bookmarks().size())));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Outside Conditions"));

        LinearLayout row = Ui.row(this);
        iconView = new WeatherIconView(this);
        iconView.setWeather(state.code);
        row.addView(iconView, new LinearLayout.LayoutParams(Ui.dp(this, 210), Ui.dp(this, 180)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(Ui.dp(this, 14), 0, 0, 0);
        details.addView(metricLine("Погода", state.description));
        details.addView(metricLine("Температура", state.temperatureLabel()));
        details.addView(metricLine("Ветер", state.windLabel()));
        details.addView(metricLine("Latitude", latValue()));
        details.addView(metricLine("Longitude", lonValue()));
        row.addView(details, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        hero.addView(row);

        weatherSummaryView = Ui.text(this, state.summary(), 16, true);
        weatherSummaryView.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(weatherSummaryView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Обновить", this::refreshWeather);
        addActionChip(quick, "Координаты", this::editCoordinates);
        addActionChip(quick, "Текущие", this::useCurrentCoordinates);
        addActionChip(quick, "Прогноз", this::openForecast);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Current", state.summaryShort(), Ui.CYAN);
        addStatusCard(grid, "Coords", latValue() + " / " + lonValue(), Ui.SUCCESS);
        addStatusCard(grid, "Forecast", state.forecastShort(), Ui.WARNING);
        addStatusCard(grid, "Search", addressValue(), Color.rgb(129, 149, 255));
        addNavCard(grid, "Weather", "Open-Meteo, координаты, forecast", Ui.CYAN, () -> openMode(Mode.HOME));
        addNavCard(grid, "Browser", "Сайт, поиск, reload и bookmark", Ui.SUCCESS, this::openAddress);
        addNavCard(grid, "Bookmarks", "Список и удаление long press", Ui.WARNING, () -> openMode(Mode.BOOKMARKS));
        addNavCard(grid, "Desktop", "Назад к weather widget на desktop", Color.rgb(129, 149, 255), () -> startActivity(new Intent(this, DesktopActivity.class)));
        return grid;
    }

    private LinearLayout buildWeatherPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Weather Controls"));
        panel.addView(Ui.text(this, "Главный слой: текущая погода, координаты и быстрое обновление через Open-Meteo.", 14, false));

        latInput = edit("Широта", latValue(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        lonInput = edit("Долгота", lonValue(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        panel.addView(latInput);
        panel.addView(lonInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Обновить", this::refreshWeather);
        addActionChip(row, "Изменить", this::editCoordinates);
        addActionChip(row, "Текущие", this::useCurrentCoordinates);
        addActionChip(row, "Прогноз", this::openForecast);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildBrowserPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 16, 24, 42) : Color.argb(246, 240, 244, 250),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Browser"));
        panel.addView(Ui.text(this, "Address/search bar, открыть сайт, поиск, reload и добавление закладки.", 14, false));

        addressInput = edit("Сайт или поисковый запрос", addressValue(), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        panel.addView(addressInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Back", () -> Ui.toast(this, "Используется внешний браузер"));
        addActionChip(row, "Forward", () -> Ui.toast(this, "Используется внешний браузер"));
        addActionChip(row, "Reload", this::openAddress);
        addActionChip(row, "Bookmark", this::addBookmark);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));

        LinearLayout row2 = Ui.row(this);
        addActionChip(row2, "Открыть", this::openAddress);
        addActionChip(row2, "Поиск", this::searchAddress);
        addActionChip(row2, "Закладки", () -> openMode(Mode.BOOKMARKS));
        addActionChip(row2, "Google", () -> {
            if (addressInput != null) addressInput.setText("https://www.google.com");
            openAddress();
        });
        panel.addView(row2, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildForecastPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 14, 21, 38) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Forecast"));
        panel.addView(Ui.text(this, "Краткий forecast summary и быстрый переход к расширенному прогнозу в браузере.", 14, false));
        forecastView = Ui.text(this, state.forecast, 16, true);
        forecastView.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 4));
        panel.addView(forecastView);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Открыть прогноз", this::openForecast);
        addActionChip(row, "Обновить", this::refreshWeather);
        panel.addView(row, lpMatchWrap(0, 10, 0, 0));
        return panel;
    }

    private LinearLayout buildBookmarksPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(236, 14, 21, 38) : Color.argb(245, 238, 242, 248),
                Ui.dp(this, 28),
                Ui.glassLine(this)));
        panel.addView(Ui.label(this, "Bookmarks"));
        panel.addView(Ui.text(this, "Список закладок браузера. Долгое нажатие удаляет запись.", 14, false));

        Set<String> items = bookmarks();
        if (items.isEmpty()) {
            panel.addView(emptyState("Сохраненные сайты появятся здесь"));
            return panel;
        }
        for (String url : items) panel.addView(buildBookmarkCard(url), lpMatchWrap(0, 0, 0, 14));
        return panel;
    }

    private LinearLayout buildBookmarkCard(String url) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, url, 16, true));
        card.addView(Ui.muted(this, "Tap: open / Long press: delete"));
        Ui.press(card, () -> {
            if (addressInput != null) addressInput.setText(url);
            open(url);
        });
        card.setOnLongClickListener(v -> {
            LinkedHashSet<String> copy = new LinkedHashSet<>(bookmarks());
            copy.remove(url);
            getPreferences(0).edit().putStringSet(KEY_BOOKMARKS, copy).apply();
            renderContent();
            return true;
        });
        return card;
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Weather", () -> openMode(Mode.HOME), mode == Mode.HOME);
        addDockButton(dock, "Forecast", this::openForecast, false);
        addDockButton(dock, "Search", this::searchAddress, false);
        addDockButton(dock, "Bookmarks", () -> openMode(Mode.BOOKMARKS), mode == Mode.BOOKMARKS);
        addDockButton(dock, "Back", this::finish, false);
        return dock;
    }

    private void restoreInputs() {
        latInput = null;
        lonInput = null;
        addressInput = null;
    }

    private void restoreState() {
        String raw = getPreferences(0).getString(KEY_WEATHER, "");
        String forecast = getPreferences(0).getString(KEY_FORECAST, "Прогноз не загружался");
        if (raw == null || raw.trim().isEmpty()) {
            state = WeatherState.placeholder();
            state.forecast = forecast;
            return;
        }
        state = WeatherState.parse(raw, forecast);
    }

    private void persistInputs() {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putString(KEY_LAT, latValue());
        editor.putString(KEY_LON, lonValue());
        editor.putString(KEY_ADDRESS, addressValue());
        editor.apply();
    }

    private void openMode(Mode next) {
        persistInputs();
        mode = next;
        renderContent();
    }

    private String latValue() {
        if (latInput != null) return latInput.getText().toString().trim();
        return getPreferences(0).getString(KEY_LAT, "55.7558");
    }

    private String lonValue() {
        if (lonInput != null) return lonInput.getText().toString().trim();
        return getPreferences(0).getString(KEY_LON, "37.6173");
    }

    private String addressValue() {
        if (addressInput != null) return addressInput.getText().toString().trim();
        return getPreferences(0).getString(KEY_ADDRESS, "https://www.google.com");
    }

    private void refreshWeather() {
        persistInputs();
        if (weatherSummaryView != null) weatherSummaryView.setText("Загружаю погоду…");
        if (forecastView != null) forecastView.setText("Загружаю прогноз…");
        load(latValue(), lonValue());
    }

    private void editCoordinates() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 18);
        box.setPadding(p, p, p, 0);
        EditText lat = edit("Широта", latValue(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText lon = edit("Долгота", lonValue(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        box.addView(lat);
        box.addView(lon);
        new AlertDialog.Builder(this)
                .setTitle("Изменить координаты")
                .setView(box)
                .setPositiveButton("Сохранить", (d, w) -> {
                    getPreferences(0).edit()
                            .putString(KEY_LAT, lat.getText().toString().trim())
                            .putString(KEY_LON, lon.getText().toString().trim())
                            .apply();
                    renderContent();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void useCurrentCoordinates() {
        if (!hasAnyLocationPermission()) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQ_LOCATION);
            } else {
                Ui.toast(this, "Нет разрешения location");
            }
            return;
        }
        try {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = null;
            if (manager != null) {
                if (hasFineLocationPermission()) {
                    location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (location == null && hasAnyLocationPermission()) {
                    location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            if (location == null) {
                Ui.toast(this, "Нет last known location. Дайте доступ к геопозиции и дождитесь определения координат.");
                return;
            }
            getPreferences(0).edit()
                    .putString(KEY_LAT, String.format(Locale.US, "%.4f", location.getLatitude()))
                    .putString(KEY_LON, String.format(Locale.US, "%.4f", location.getLongitude()))
                    .apply();
            renderContent();
            refreshWeather();
        } catch (SecurityException e) {
            Ui.toast(this, "Нет разрешения location");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_LOCATION) return;
        if (hasAnyLocationPermission()) {
            useCurrentCoordinates();
        } else {
            Ui.toast(this, "Location permission не выдан");
        }
    }

    private boolean hasAnyLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT < 23) return true;
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasFineLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT < 23) return true;
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void openForecast() {
        String url = "https://www.open-meteo.com/en/docs?latitude=" + encode(latValue()) + "&longitude=" + encode(lonValue());
        open(url);
    }

    private void openAddress() {
        persistInputs();
        open(normalizeUrl(addressValue()));
    }

    private void searchAddress() {
        persistInputs();
        open("https://www.google.com/search?q=" + encode(addressValue()));
    }

    private void addBookmark() {
        persistInputs();
        LinkedHashSet<String> items = new LinkedHashSet<>(bookmarks());
        items.add(normalizeUrl(addressValue()));
        getPreferences(0).edit().putStringSet(KEY_BOOKMARKS, items).apply();
        renderContent();
    }

    private Set<String> bookmarks() {
        return getPreferences(0).getStringSet(KEY_BOOKMARKS, new LinkedHashSet<>());
    }

    private void open(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private String normalizeUrl(String input) {
        String value = input == null ? "" : input.trim();
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
        new Thread(() -> {
            try {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=" + encode(lat)
                        + "&longitude=" + encode(lon)
                        + "&current=temperature_2m,wind_speed_10m,weather_code"
                        + "&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=3";
                JSONObject json = new JSONObject(read(new URL(url)));
                JSONObject current = json.getJSONObject("current");
                double temp = current.optDouble("temperature_2m");
                double wind = current.optDouble("wind_speed_10m");
                int code = current.optInt("weather_code");
                String description = weatherName(code);
                String summary = String.format(Locale.US, "%.1f C | Ветер %.0f км/ч | %s", temp, wind, description);
                String forecast = dailySummary(json.optJSONObject("daily"));
                String raw = temp + "|" + wind + "|" + code + "|" + description + "|" + summary;
                getPreferences(0).edit().putString(KEY_WEATHER, raw).putString(KEY_FORECAST, forecast).apply();
                runOnUiThread(() -> {
                    restoreState();
                    if (iconView != null) iconView.setWeather(state.code);
                    if (weatherSummaryView != null) weatherSummaryView.setText(state.summary());
                    if (forecastView != null) forecastView.setText(state.forecast);
                    renderContent();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (weatherSummaryView != null) weatherSummaryView.setText("Ошибка погоды: " + e.getMessage());
                    if (forecastView != null) forecastView.setText("Ошибка прогноза: " + e.getMessage());
                });
            }
        }).start();
    }

    private String dailySummary(JSONObject daily) {
        if (daily == null) return "Прогноз не загружен";
        JSONArray times = daily.optJSONArray("time");
        JSONArray max = daily.optJSONArray("temperature_2m_max");
        JSONArray min = daily.optJSONArray("temperature_2m_min");
        JSONArray codes = daily.optJSONArray("weather_code");
        if (times == null || max == null || min == null || codes == null) return "Прогноз не загружен";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, times.length());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append('\n');
            sb.append(times.optString(i))
                    .append("  ")
                    .append(Math.round(min.optDouble(i)))
                    .append("..")
                    .append(Math.round(max.optDouble(i)))
                    .append(" C  ")
                    .append(weatherName(codes.optInt(i)));
        }
        return sb.length() == 0 ? "Прогноз не загружен" : sb.toString();
    }

    private EditText edit(String hint, String value, int inputType) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setInputType(inputType);
        field.setTypeface(Typeface.MONOSPACE);
        field.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 18), Ui.glassLine(this)));
        field.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        field.setLayoutParams(lpMatchWrap(0, 12, 0, 0));
        return field;
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        Ui.bindPress(b, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : Color.argb(54, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
        Ui.bindPress(button, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(118, 255, 255, 255) : Color.argb(232, 255, 255, 255),
                Ui.dp(this, 26),
                Ui.glassLine(this)));
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 18, true));
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private void addNavCard(GridLayout grid, String title, String body, int color, Runnable action) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.text(this, title, 18, true));
        card.addView(Ui.muted(this, body));
        Button open = Ui.button(this, "Открыть");
        Ui.bindPress(open, action);
        card.addView(open, lpMatchWrap(0, 12, 0, 0));
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private TextView emptyState(String text) {
        TextView view = Ui.text(this, text, 16, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 24));
        return view;
    }

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private View[] collectChildren(LinearLayout parent) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) views.add(parent.getChildAt(i));
        return views.toArray(new View[0]);
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
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            connection.disconnect();
        }
    }

    private enum Mode {
        HOME,
        BOOKMARKS
    }

    private static final class WeatherState {
        final double temp;
        final double wind;
        final int code;
        final String description;
        final String summaryRaw;
        String forecast;

        WeatherState(double temp, double wind, int code, String description, String summaryRaw, String forecast) {
            this.temp = temp;
            this.wind = wind;
            this.code = code;
            this.description = description;
            this.summaryRaw = summaryRaw;
            this.forecast = forecast;
        }

        static WeatherState placeholder() {
            return new WeatherState(Double.NaN, Double.NaN, 2, "Данные не загружены", "Нажмите обновить, чтобы загрузить данные Open-Meteo.", "Прогноз не загружался");
        }

        static WeatherState parse(String raw, String forecast) {
            String[] parts = raw.split("\\|", 5);
            if (parts.length < 5) {
                WeatherState fallback = placeholder();
                fallback.forecast = forecast;
                return fallback;
            }
            try {
                return new WeatherState(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Integer.parseInt(parts[2]),
                        parts[3],
                        parts[4],
                        forecast
                );
            } catch (Exception e) {
                WeatherState fallback = placeholder();
                fallback.forecast = forecast;
                return fallback;
            }
        }

        String temperatureLabel() {
            return Double.isNaN(temp) ? "n/a" : String.format(Locale.US, "%.1f C", temp);
        }

        String windLabel() {
            return Double.isNaN(wind) ? "n/a" : String.format(Locale.US, "%.0f км/ч", wind);
        }

        String summary() {
            return summaryRaw;
        }

        String summaryShort() {
            return temperatureLabel() + " · " + description;
        }

        String forecastShort() {
            if (forecast == null || forecast.trim().isEmpty()) return "n/a";
            String first = forecast.split("\n")[0];
            return first.length() > 28 ? first.substring(0, 28) + "…" : first;
        }
    }

    private static final class WeatherIconView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int code = 2;

        WeatherIconView(Context c) {
            super(c);
        }

        void setWeather(int value) {
            code = value;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            boolean rainy = code >= 51 && code <= 99;
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, w, h,
                    Color.rgb(58, 134, 218),
                    rainy ? Color.rgb(70, 88, 126) : Color.rgb(242, 181, 76),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(w * .06f, h * .06f, w * .94f, h * .94f), dp(getContext(), 30), dp(getContext(), 30), p);
            p.setShader(null);
            p.setColor(Color.argb(90, 255, 255, 255));
            canvas.drawCircle(w * .34f, h * .34f, w * .16f, p);
            p.setColor(Color.WHITE);
            canvas.drawCircle(w * .40f, h * .55f, w * .18f, p);
            canvas.drawCircle(w * .57f, h * .49f, w * .16f, p);
            canvas.drawRoundRect(new RectF(w * .26f, h * .52f, w * .76f, h * .70f), dp(getContext(), 18), dp(getContext(), 18), p);
            if (rainy) {
                p.setColor(Color.rgb(176, 220, 255));
                p.setStrokeWidth(dp(getContext(), 4));
                for (int i = 0; i < 4; i++) {
                    canvas.drawLine(w * (.32f + i * .11f), h * .77f, w * (.28f + i * .11f), h * .90f, p);
                }
            }
        }

        private int dp(Context context, int value) {
            return Ui.dp(context, value);
        }
    }
}
