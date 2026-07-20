package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class DesktopActivity extends Activity {
    private LinearLayout root;
    private final Set<String> pinned = new LinkedHashSet<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        pinned.addAll(getPreferences(0).getStringSet("pinned", new LinkedHashSet<>()));
        render();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        root = Ui.root(this, "Рабочий стол");
        root.addView(Ui.text(this, new SimpleDateFormat("HH:mm · dd.MM.yyyy", Locale.getDefault()).format(new Date()), 20, true));
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
            String[] actions = dock ? new String[]{"Убрать с главной", "Удалить приложение"} : new String[]{"Добавить на главную", "Удалить приложение"};
            new AlertDialog.Builder(this).setTitle(finalLabel).setItems(actions, (d, which) -> {
                if (which == 0) {
                    if (dock) pinned.remove(pkg); else pinned.add(pkg);
                    getPreferences(0).edit().putStringSet("pinned", pinned).apply();
                    render();
                } else {
                    startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
                }
            }).show();
            return true;
        });
        root.addView(b);
    }
}
