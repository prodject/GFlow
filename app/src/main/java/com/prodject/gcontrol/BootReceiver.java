package com.prodject.gcontrol;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, VoiceForegroundService.class));
        AutomationEngine.runTrigger(context, "boot", intent == null ? "" : intent.getAction());
        AutomationEngine.runSmartClimateIfEnabled(context);
        if (intent != null && Intent.ACTION_SHUTDOWN.equals(intent.getAction())) SmartClimateController.dryAfterTrip(context);
    }
}
