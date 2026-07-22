package com.prodject.gflow;

import android.app.Activity;
import android.os.Bundle;

public class VoiceAppLaunchActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String target = getIntent().getStringExtra("target");
        if (target == null || target.trim().isEmpty()) target = getIntent().getStringExtra("command");
        VoiceFlowRouter.launchByToken(this, target == null ? "" : target);
        finish();
    }
}
