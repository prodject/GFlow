package com.prodject.gcontrol;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, VoiceForegroundService.class));
    }
}
