package com.prodject.gcontrol;

import android.content.*;
import java.util.*;

public class SteeringWheelReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String command = textExtra(intent, "command", "cmd", "voice", "text");
        int keyCode = intExtra(intent, "keyCode", "keycode", "key_code", "code");
        String gesture = gesture(intent);
        String modifier = modifier(intent);
        String foreground = context.getSharedPreferences(AppWatchdogAccessibilityService.PREFS, Context.MODE_PRIVATE)
                .getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, "");
        String event = "action=" + intent.getAction() + " key=" + keyCode + " command=" + command;
        context.getSharedPreferences("steering", Context.MODE_PRIVATE).edit()
                .putString("last_event", event)
                .putLong("last_event_at", System.currentTimeMillis())
                .apply();
        AutomationEngine.runTrigger(context, "button", keyCode + ":" + gesture);
        AutomationEngine.SteeringResult steering = AutomationEngine.runSteering(context, keyCode, gesture, modifier, foreground);
        if (steering.replaceStock) return;
        if (command != null && !command.trim().isEmpty()) {
            CarCommandBus.send(context, "steering", command);
            openVoice(context, command, event);
            return;
        }
        if (isVoiceKey(intent, keyCode)) openVoice(context, "", event);
    }

    private void openVoice(Context context, String command, String event) {
        Intent voice = new Intent(context, VoiceActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("source", "steering")
                .putExtra("command", command == null ? "" : command)
                .putExtra("event", event);
        context.startActivity(voice);
    }

    private boolean isVoiceKey(Intent intent, int keyCode) {
        String action = String.valueOf(intent.getAction()).toLowerCase(Locale.ROOT);
        if (action.contains("voice") || action.contains("assist") || action.contains("steering")) return true;
        return keyCode == 0xe7 || keyCode == 0x30e27 || keyCode == 231 || keyCode == 0x54;
    }

    private String textExtra(Intent intent, String... names) {
        for (String name : names) {
            String value = intent.getStringExtra(name);
            if (value != null) return value;
        }
        return null;
    }

    private int intExtra(Intent intent, String... names) {
        for (String name : names) {
            if (intent.hasExtra(name)) return intent.getIntExtra(name, 0);
        }
        return 0;
    }

    private String gesture(Intent intent) {
        String value = textExtra(intent, "gesture", "actionType", "eventType", "keyAction");
        if (value != null && !value.trim().isEmpty()) return value.trim().toLowerCase(Locale.ROOT);
        int repeat = intExtra(intent, "repeatCount", "repeat", "longPress");
        if (repeat > 0) return "hold";
        String action = String.valueOf(intent.getAction()).toLowerCase(Locale.ROOT);
        if (action.contains("long") || action.contains("hold")) return "hold";
        if (action.contains("triple")) return "triple";
        if (action.contains("double")) return "double";
        int clickCount = intExtra(intent, "clickCount", "clicks", "tapCount");
        if (clickCount == 3) return "triple";
        if (clickCount == 2) return "double";
        return "press";
    }

    private String modifier(Intent intent) {
        String value = textExtra(intent, "modifier", "heldKey", "withKey", "comboKey");
        if (value != null) return value.trim();
        int key = intExtra(intent, "modifierKeyCode", "heldKeyCode", "withKeyCode");
        return key == 0 ? "" : String.valueOf(key);
    }
}
