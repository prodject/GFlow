package com.prodject.gflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DiagnosticsAutomationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        final PendingResult pending = goAsync();
        final Context appContext = context.getApplicationContext();
        final boolean includeWrites = intent != null && intent.getBooleanExtra(DiagnosticsRunner.EXTRA_INCLUDE_WRITES, true);
        final String reason = intent == null ? "adb" : intent.getStringExtra(DiagnosticsRunner.EXTRA_REASON);
        new Thread(() -> {
            try {
                DiagnosticsRunner.run(appContext, includeWrites, reason == null ? "adb" : reason);
            } finally {
                pending.finish();
            }
        }, "gflow-diagnostics").start();
    }
}
