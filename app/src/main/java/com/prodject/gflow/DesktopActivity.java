package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class DesktopActivity extends Activity {
    private static final String KEY_PINNED_ORDER = "pinned_order";
    private static final String KEY_THEME = "theme";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_WEATHER_AT = "weather_at";
    private LinearLayout root;
    private final ArrayList<String> pinned = new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        loadPinned();
        render();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        root = Ui.root(this, "Рабочий стол", this::finish);
        applyTheme(root);
        addHomeHero();
        Button theme = Ui.button(this, "Тема / обои");
        theme.setOnClickListener(v -> chooseTheme());
        root.addView(theme, margin(0, 0, 0, 12));
        addOneOsDockPanel();
        LinearLayout pinnedCard = Ui.card(this);
        pinnedCard.addView(Ui.text(this, "Док приложений", 18, true));
        LinearLayout oldRoot = root;
        root = pinnedCard;
        for (String pkg : pinned) addAppRow(pkg, true);
        root = oldRoot;
        oldRoot.addView(pinnedCard, margin(0, 0, 0, 12));
        LinearLayout allCard = Ui.card(this);
        allCard.addView(Ui.text(this, "Все приложения", 18, true));
        root = allCard;
        for (ResolveInfo info : apps()) addAppRow(info.activityInfo.packageName, false);
        root = oldRoot;
        oldRoot.addView(allCard);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void addOneOsDockPanel() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "OneOS Dock", 18, true));
        card.addView(Ui.muted(this, new EcarxDockAdapter(this).availability()));
        LinearLayout oldRoot = root;
        root = card;
        addDockAction("OneOS Dock: hand over on", a -> a.handOver(true));
        addDockAction("OneOS Dock: hand over off", a -> a.handOver(false));
        addDockAction("OneOS Dock: status", EcarxDockAdapter::deviceStatus);
        addDockAction("OneOS Dock: show", a -> a.switchDeviceDock(true));
        addDockAction("OneOS Dock: hide", a -> a.switchDeviceDock(false));
        addDockAction("OneOS Dock: HVAC open", a -> a.notifyItem(EcarxDockAdapter.TYPE_HVAC, EcarxDockAdapter.STATE_OPEN));
        addDockAction("OneOS Dock: HVAC close", a -> a.notifyItem(EcarxDockAdapter.TYPE_HVAC, EcarxDockAdapter.STATE_CLOSE));
        addDockAction("OneOS Dock: media open", a -> a.notifyItem(EcarxDockAdapter.TYPE_MEDIA, EcarxDockAdapter.STATE_OPEN));
        addDockAction("OneOS Dock: volume open", a -> a.notifyItem(EcarxDockAdapter.TYPE_VOLUME, EcarxDockAdapter.STATE_OPEN));
        addDockAction("OneOS Dock: defrost open", a -> a.notifyItem(EcarxDockAdapter.TYPE_DEFROSTING, EcarxDockAdapter.STATE_OPEN));
        addDockAction("OneOS Dock: custom HVAC icon 0", a -> a.customHvacIcon(0));
        addDockAction("OneOS Dock: custom app icon marker", a -> a.customAppIcon(0, getPackageName().getBytes()));
        root = oldRoot;
        oldRoot.addView(card, margin(0, 0, 0, 12));
    }

    private void addDockAction(String label, DockAction action) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxDockAdapter.Result result;
            try {
                result = action.run(new EcarxDockAdapter(this));
            } catch (Exception e) {
                result = EcarxDockAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "OneOS Dock команда отправлена" : "OneOS Dock команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), Math.min(4, root.getChildCount()));
        });
        root.addView(b);
    }

    interface DockAction {
        EcarxDockAdapter.Result run(EcarxDockAdapter adapter);
    }

    private List<ResolveInfo> apps() {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(i, 0);
        Collections.sort(list, Comparator.comparing(a -> a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        return list;
    }

    private void addAppRow(String pkg, boolean dock) {
        PackageManager pm = getPackageManager();
        String label = pkg;
        try { label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString(); } catch (Exception ignored) {}
        Button b = Ui.button(this, (dock ? "★ " : "") + label);
        String finalLabel = label;
        b.setOnClickListener(v -> {
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) startActivity(launch);
            else Ui.toast(this, "Не удалось открыть " + finalLabel);
        });
        b.setOnLongClickListener(v -> {
            String[] actions = dock
                    ? new String[]{"Убрать с главной", "Выше", "Ниже", "Удалить приложение"}
                    : new String[]{"Добавить на главную", "Удалить приложение"};
            new AlertDialog.Builder(this).setTitle(finalLabel).setItems(actions, (d, which) -> {
                if (which == 0) {
                    if (dock) pinned.remove(pkg);
                    else if (!pinned.contains(pkg)) pinned.add(pkg);
                    savePinned();
                    render();
                } else if (dock && which == 1) {
                    movePinned(pkg, -1);
                } else if (dock && which == 2) {
                    movePinned(pkg, 1);
                } else {
                    startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
                }
            }).show();
            return true;
        });
        root.addView(b);
    }

    private void loadPinned() {
        String order = getPreferences(0).getString(KEY_PINNED_ORDER, "");
        if (!order.isEmpty()) {
            for (String pkg : order.split("\n")) if (!pkg.trim().isEmpty()) pinned.add(pkg.trim());
            return;
        }
        pinned.addAll(getPreferences(0).getStringSet("pinned", new LinkedHashSet<>()));
        savePinned();
    }

    private void savePinned() {
        StringBuilder sb = new StringBuilder();
        for (String pkg : pinned) sb.append(pkg).append("\n");
        getPreferences(0).edit().putString(KEY_PINNED_ORDER, sb.toString()).apply();
    }

    private void movePinned(String pkg, int delta) {
        int from = pinned.indexOf(pkg);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= pinned.size()) return;
        Collections.swap(pinned, from, to);
        savePinned();
        render();
    }

    private void chooseTheme() {
        String[] items = {"Светлая", "Графит", "Голубая"};
        new AlertDialog.Builder(this).setTitle("Тема рабочего стола").setItems(items, (d, which) -> {
            getPreferences(0).edit().putInt(KEY_THEME, which).apply();
            render();
        }).show();
    }

    private void applyTheme(LinearLayout view) {
        int theme = getPreferences(0).getInt(KEY_THEME, 0);
        if (theme == 1) view.setBackgroundColor(Color.rgb(229, 231, 235));
        else if (theme == 2) view.setBackgroundColor(Color.rgb(232, 238, 244));
    }

    private void addWeatherWidget() {
        String cached = getPreferences(0).getString(KEY_WEATHER, "Погода не обновлялась");
        long at = getPreferences(0).getLong(KEY_WEATHER_AT, 0);
        TextView weather = Ui.text(this, cached + (at == 0 ? "" : "\n" + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(at))), 16, true);
        Button refresh = Ui.button(this, "Обновить погоду");
        refresh.setOnClickListener(v -> {
            weather.setText("Загружаю погоду...");
            loadWeather(weather);
        });
        root.addView(weather);
        root.addView(refresh);
    }

    private void addHomeHero() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), 36, true));
        card.addView(Ui.muted(this, new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault()).format(new Date())));
        root.addView(card, margin(0, 8, 0, 12));
        addWeatherWidget();
    }

    private void loadWeather(TextView weather) {
        new Thread(() -> {
            try {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=55.7558&longitude=37.6173&current=temperature_2m,wind_speed_10m,weather_code";
                JSONObject current = new JSONObject(read(new URL(url))).getJSONObject("current");
                String text = "Погода: " + current.optDouble("temperature_2m") + " C, ветер " + current.optDouble("wind_speed_10m") + " км/ч, код " + current.optInt("weather_code");
                getPreferences(0).edit().putString(KEY_WEATHER, text).putLong(KEY_WEATHER_AT, System.currentTimeMillis()).apply();
                runOnUiThread(() -> weather.setText(text));
            } catch (Exception e) {
                runOnUiThread(() -> weather.setText("Ошибка погоды: " + e.getMessage()));
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

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }
}
