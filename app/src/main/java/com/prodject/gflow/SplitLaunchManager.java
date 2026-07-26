package com.prodject.gflow;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class SplitLaunchManager {
    static final String ACTION_LAUNCH = "com.prodject.gflow.SPLIT_LAUNCH";
    static final String ACTION_LAUNCH_LAST = "com.prodject.gflow.SPLIT_LAUNCH_LAST";
    static final String ACTION_CLOSE = "com.prodject.gflow.SPLIT_CLOSE";
    static final String EXTRA_FIRST_PACKAGE = "first_package";
    static final String EXTRA_SECOND_PACKAGE = "second_package";
    static final String EXTRA_MODE = "mode";
    static final String EXTRA_SECOND_WINDOW_DELAY_MS = "second_window_delay_ms";
    static final String EXTRA_BOTTOM_WINDOW_SHIFT = "bottom_window_shift";
    static final String MODE_ADJACENT = "adjacent";
    static final String MODE_NATIVE = "native";
    static final String MODE_FREEFORM = "freeform";
    private static final String PREFS = "gflow_split";
    private static final String KEY_LAST_FIRST = "last_first";
    private static final String KEY_LAST_SECOND = "last_second";
    private static final String KEY_LAST_MODE = "last_mode";
    private static final String KEY_SECOND_DELAY = "second_delay";
    private static final String KEY_BOTTOM_SHIFT = "bottom_shift";
    private static final long DEFAULT_SECOND_WINDOW_DELAY_MS = 400L;

    static final class LaunchableApp {
        final String label;
        final String packageName;
        final String activityName;

        LaunchableApp(String label, String packageName, String activityName) {
            this.label = label;
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    static final class Config {
        final String firstPackage;
        final String secondPackage;
        final String mode;
        final long secondWindowDelayMs;
        final int bottomWindowShift;

        Config(String firstPackage, String secondPackage, String mode, long secondWindowDelayMs, int bottomWindowShift) {
            this.firstPackage = firstPackage;
            this.secondPackage = secondPackage;
            this.mode = sanitizeMode(mode);
            this.secondWindowDelayMs = secondWindowDelayMs <= 0 ? DEFAULT_SECOND_WINDOW_DELAY_MS : secondWindowDelayMs;
            this.bottomWindowShift = Math.max(0, bottomWindowShift);
        }
    }

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    SplitLaunchManager(Context context) {
        this.context = context.getApplicationContext();
    }

    List<LaunchableApp> apps() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(query, 0);
        ArrayList<LaunchableApp> apps = new ArrayList<>();
        for (ResolveInfo info : infos) {
            CharSequence label = info.loadLabel(context.getPackageManager());
            String packageName = info.activityInfo.packageName;
            String activityName = info.activityInfo.name;
            apps.add(new LaunchableApp(label == null ? packageName : label.toString(), packageName, activityName));
        }
        Collections.sort(apps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
        return apps;
    }

    Config loadLast() {
        SharedPreferences prefs = prefs();
        String first = prefs.getString(KEY_LAST_FIRST, "");
        String second = prefs.getString(KEY_LAST_SECOND, "");
        if (first == null || first.trim().isEmpty() || second == null || second.trim().isEmpty()) return null;
        return new Config(
                first,
                second,
                prefs.getString(KEY_LAST_MODE, MODE_NATIVE),
                prefs.getLong(KEY_SECOND_DELAY, DEFAULT_SECOND_WINDOW_DELAY_MS),
                prefs.getInt(KEY_BOTTOM_SHIFT, 0)
        );
    }

    Config configFromIntent(Intent intent) {
        if (intent == null) return null;
        String first = trim(intent.getStringExtra(EXTRA_FIRST_PACKAGE));
        String second = trim(intent.getStringExtra(EXTRA_SECOND_PACKAGE));
        if (first.isEmpty() || second.isEmpty()) return null;
        return new Config(
                first,
                second,
                intent.getStringExtra(EXTRA_MODE),
                intent.getLongExtra(EXTRA_SECOND_WINDOW_DELAY_MS, prefs().getLong(KEY_SECOND_DELAY, DEFAULT_SECOND_WINDOW_DELAY_MS)),
                intent.getIntExtra(EXTRA_BOTTOM_WINDOW_SHIFT, prefs().getInt(KEY_BOTTOM_SHIFT, 0))
        );
    }

    void startLauncher(Context source, Config config) {
        if (config == null) return;
        saveLast(config);
        Intent intent = new Intent(source, SplitLauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(EXTRA_FIRST_PACKAGE, config.firstPackage);
        intent.putExtra(EXTRA_SECOND_PACKAGE, config.secondPackage);
        intent.putExtra(EXTRA_MODE, config.mode);
        intent.putExtra(EXTRA_SECOND_WINDOW_DELAY_MS, config.secondWindowDelayMs);
        intent.putExtra(EXTRA_BOTTOM_WINDOW_SHIFT, config.bottomWindowShift);
        source.startActivity(intent);
    }

    void launchLast(Context source) {
        Config config = loadLast();
        if (config != null) startLauncher(source, config);
    }

    void closeSplit() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(home);
    }

    void execute(Activity activity, Config config) {
        if (activity == null || config == null) {
            if (activity != null) activity.finish();
            return;
        }
        saveLast(config);
        if (MODE_NATIVE.equals(config.mode)) {
            launchNativeSplit(activity, config);
            return;
        }
        if (MODE_FREEFORM.equals(config.mode)) {
            launchFreeform(activity, config);
            return;
        }
        launchAdjacent(activity, config);
    }

    private void launchAdjacent(Activity activity, Config config) {
        Intent first = buildLaunchIntent(config.firstPackage);
        Intent second = buildLaunchIntent(config.secondPackage);
        if (first == null || second == null) {
            Ui.toast(activity, "Не удалось найти приложения для split");
            activity.finish();
            return;
        }
        second.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(first);
        } catch (Exception e) {
            Ui.toast(activity, "Ошибка запуска первого окна: " + compact(e));
            activity.finish();
            return;
        }
        mainHandler.postDelayed(() -> {
            try {
                activity.startActivity(second);
            } catch (Exception e) {
                Ui.toast(activity, "Ошибка запуска второго окна: " + compact(e));
            }
            activity.finish();
        }, config.secondWindowDelayMs);
    }

    private void launchNativeSplit(Activity activity, Config config) {
        Intent first = buildLaunchIntent(config.firstPackage);
        Intent second = buildLaunchIntent(config.secondPackage);
        if (first == null || second == null) {
            Ui.toast(activity, "Не удалось найти приложения для native split");
            activity.finish();
            return;
        }
        DefaultLifecycleObserver observer = new DefaultLifecycleObserver() {
            @Override public void onStop(LifecycleOwner owner) {
                Intent intentTop = cloneIntent(first);
                Intent intentBottom = cloneIntent(second);
                BundleOptions options = buildNativeSplitOptions();
                mainHandler.postDelayed(() -> {
                    try {
                        intentTop.addCategory(Intent.CATEGORY_LAUNCHER);
                        intentBottom.addCategory(Intent.CATEGORY_LAUNCHER);
                        intentTop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        intentBottom.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        if (options.bundle != null) activity.startActivities(new Intent[]{intentBottom, intentTop}, options.bundle);
                        else {
                            activity.startActivity(intentBottom);
                            mainHandler.postDelayed(() -> activity.startActivity(intentTop), 80L);
                        }
                    } catch (Exception e) {
                        Ui.toast(activity, "Native split fallback: " + compact(e));
                        launchAdjacent(activity, new Config(config.firstPackage, config.secondPackage, MODE_ADJACENT, config.secondWindowDelayMs, config.bottomWindowShift));
                        return;
                    } finally {
                        activity.getLifecycle().removeObserver(this);
                        activity.finish();
                    }
                }, config.secondWindowDelayMs);
            }
        };
        activity.getLifecycle().addObserver(observer);
        try {
            Intent resetIntent = cloneIntent(first);
            resetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resetIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            activity.startActivity(resetIntent);
        } catch (Exception e) {
            activity.getLifecycle().removeObserver(observer);
            Ui.toast(activity, "Ошибка native split reset: " + compact(e));
            launchAdjacent(activity, new Config(config.firstPackage, config.secondPackage, MODE_ADJACENT, config.secondWindowDelayMs, config.bottomWindowShift));
        }
    }

    private void launchFreeform(Activity activity, Config config) {
        startFreeformHack(activity);
        mainHandler.postDelayed(() -> {
            boolean firstOk = launchWindow(config.firstPackage, true, config.bottomWindowShift, 5);
            mainHandler.postDelayed(() -> {
                boolean secondOk = launchWindow(config.secondPackage, false, config.bottomWindowShift, 5);
                if (!firstOk || !secondOk) {
                    Ui.toast(activity, "Freeform частично недоступен, fallback на adjacent");
                    launchAdjacent(activity, new Config(config.firstPackage, config.secondPackage, MODE_ADJACENT, config.secondWindowDelayMs, config.bottomWindowShift));
                    return;
                }
                activity.finish();
            }, config.secondWindowDelayMs);
        }, 120L);
    }

    private boolean launchWindow(String packageName, boolean primary, int bottomWindowShift, int windowingMode) {
        Intent launchIntent = buildLaunchIntent(packageName);
        if (launchIntent == null) return false;
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        Rect bounds = computeHalfBounds(primary, bottomWindowShift);
        try {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, 0, 0);
            options.setLaunchBounds(bounds);
            invokeLaunchWindowingMode(options, windowingMode);
            context.startActivity(launchIntent, options.toBundle());
            return true;
        } catch (Exception e) {
            try {
                context.startActivity(launchIntent);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private Rect computeHalfBounds(boolean primary, int bottomWindowShift) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm == null ? null : wm.getDefaultDisplay();
        if (display != null) display.getRealMetrics(dm);
        int width = dm.widthPixels <= 0 ? 1920 : dm.widthPixels;
        int height = dm.heightPixels <= 0 ? 1080 : dm.heightPixels;
        int statusBarHeight = statusBarHeight();
        boolean portrait = height >= width;
        if (portrait) {
            int half = height / 2;
            if (primary) return new Rect(0, statusBarHeight, width, half);
            return new Rect(0, Math.max(statusBarHeight, half - bottomWindowShift), width, height);
        }
        int half = width / 2;
        if (primary) return new Rect(0, statusBarHeight, half, height);
        return new Rect(Math.max(0, half - bottomWindowShift), statusBarHeight, width, height);
    }

    private int statusBarHeight() {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id == 0 ? 0 : context.getResources().getDimensionPixelSize(id);
    }

    private void startFreeformHack(Context source) {
        if (!Settings.canDrawOverlays(source)) return;
        Intent intent = new Intent(source, MultiWindowHeatingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        try {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(source, 0, 0);
            source.startActivity(intent, options.toBundle());
        } catch (Exception ignored) {
        }
    }

    private BundleOptions buildNativeSplitOptions() {
        try {
            android.os.Bundle bundle = ActivityOptions.makeBasic().toBundle();
            if (bundle != null) {
                bundle.putInt("android.activity.windowingMode", 3);
                bundle.putInt("android:activity.splitScreenCreateMode", 0);
                return new BundleOptions(bundle);
            }
        } catch (Exception ignored) {
        }
        return new BundleOptions(null);
    }

    private Intent buildLaunchIntent(String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage(packageName);
        if (launch != null) return launch;
        for (LaunchableApp app : apps()) {
            if (packageName.equals(app.packageName)) {
                return new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(app.packageName, app.activityName);
            }
        }
        return null;
    }

    private static Intent cloneIntent(Intent source) {
        return new Intent(source);
    }

    private void saveLast(Config config) {
        prefs().edit()
                .putString(KEY_LAST_FIRST, config.firstPackage)
                .putString(KEY_LAST_SECOND, config.secondPackage)
                .putString(KEY_LAST_MODE, config.mode)
                .putLong(KEY_SECOND_DELAY, config.secondWindowDelayMs)
                .putInt(KEY_BOTTOM_SHIFT, config.bottomWindowShift)
                .apply();
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String sanitizeMode(String mode) {
        if (MODE_FREEFORM.equals(mode)) return MODE_FREEFORM;
        if (MODE_ADJACENT.equals(mode)) return MODE_ADJACENT;
        return MODE_NATIVE;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String compact(Throwable error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }

    private static void invokeLaunchWindowingMode(ActivityOptions options, int mode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return;
        try {
            Method method = options.getClass().getMethod("setLaunchWindowingMode", Integer.TYPE);
            method.invoke(options, mode);
        } catch (Exception ignored) {
        }
    }

    private static final class BundleOptions {
        final android.os.Bundle bundle;

        BundleOptions(android.os.Bundle bundle) {
            this.bundle = bundle;
        }
    }
}
