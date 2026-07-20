package com.prodject.gcontrol;

import android.content.*;

public class SteeringWheelReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null) context.startActivity(new Intent(context, VoiceActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
