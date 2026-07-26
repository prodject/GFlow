package com.prodject.gflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.ComponentActivity;

public class SplitLauncherActivity extends ComponentActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        SplitLaunchManager manager = new SplitLaunchManager(this);
        SplitLaunchManager.Config config = manager.configFromIntent(intent);
        if (config == null) {
            manager.launchLast(this);
            finish();
            return;
        }
        manager.execute(this, config);
    }
}
