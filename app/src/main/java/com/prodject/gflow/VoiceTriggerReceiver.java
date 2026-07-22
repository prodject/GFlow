package com.prodject.gflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VoiceTriggerReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String command = intent == null ? "" : intent.getStringExtra("command");
        String action = intent == null ? "app.monji.VOICE" : intent.getAction();
        VoiceFlowRouter.launchVoiceUi(context, "receiver", command, "action=" + action);
    }
}
