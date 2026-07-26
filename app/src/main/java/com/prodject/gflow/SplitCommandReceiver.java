package com.prodject.gflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SplitCommandReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        SplitLaunchManager manager = new SplitLaunchManager(context);
        String action = intent.getAction();
        if (SplitLaunchManager.ACTION_CLOSE.equals(action)) {
            manager.closeSplit();
            return;
        }
        if (SplitLaunchManager.ACTION_LAUNCH_LAST.equals(action)) {
            manager.launchLast(context);
            return;
        }
        if (SplitLaunchManager.ACTION_LAUNCH.equals(action)) {
            SplitLaunchManager.Config config = manager.configFromIntent(intent);
            if (config != null) manager.startLauncher(context, config);
        }
    }
}
