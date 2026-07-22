package com.prodject.gflow;

import android.accessibilityservice.*;
import android.content.*;
import android.provider.Settings;
import android.view.accessibility.*;
import java.util.*;

public class AppWatchdogAccessibilityService extends AccessibilityService {
    static final String PREFS = "watchdog";
    static final String KEY_ENABLED = "autozoom_enabled";
    static final String KEY_PACKAGES = "autozoom_packages";
    static final String KEY_SCALE = "autozoom_scale";
    static final String KEY_LAST_PACKAGE = "last_package";

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgSeq = event.getPackageName();
        if (pkgSeq == null) return;
        String pkg = pkgSeq.toString();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_LAST_PACKAGE, pkg).apply();
        AutomationEngine.runTrigger(this, "app", pkg);
        if (!prefs.getBoolean(KEY_ENABLED, false)) return;
        float target = shouldZoom(pkg, prefs.getString(KEY_PACKAGES, "")) ? prefs.getFloat(KEY_SCALE, 1.15f) : 1.0f;
        try {
            Settings.System.putFloat(getContentResolver(), Settings.System.FONT_SCALE, target);
        } catch (Exception e) {
            android.util.Log.e("GFlowWatchdog", "font scale failed", e);
        }
    }

    @Override public void onInterrupt() {}

    private boolean shouldZoom(String pkg, String raw) {
        for (String item : raw.split(",")) {
            String token = item.trim().toLowerCase(Locale.ROOT);
            if (!token.isEmpty() && pkg.toLowerCase(Locale.ROOT).contains(token)) return true;
        }
        return false;
    }
}
