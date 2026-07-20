package com.prodject.gcontrol;

import android.accessibilityservice.*;
import android.view.accessibility.*;

public class AppWatchdogAccessibilityService extends AccessibilityService {
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
