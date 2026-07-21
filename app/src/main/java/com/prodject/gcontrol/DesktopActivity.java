package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.net.*;
import android.os.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class DesktopActivity extends Activity {
    private static final String KEY_PINNED_ORDER = "pinned_order";
    private static final String KEY_THEME = "theme";
    private LinearLayout root;
    private final ArrayList<String> pinned = new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        loadPinned();
        render();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        root = Ui.root(this, "Рабочий стол");
        applyTheme(root);
        root.addView(Ui.text(this, new SimpleDateFormat("HH:mm · dd.MM.yyyy", Locale.getDefault()).format(new Date()), 20, true));
        Button theme = Ui.button(this, "Тема / обои");
        theme.setOnClickListener(v -> chooseTheme());
        root.addView(theme);
        root.addView(Ui.text(this, "Док приложений", 18, true));
        for (String pkg : pinned) addAppRow(pkg, true);
        root.addView(Ui.text(this, "Все приложения", 18, true));
        for (ResolveInfo info : apps()) addAppRow(info.activityInfo.packageName, false);
        scroll.addView(root);
        setContentView(scroll);
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
}
