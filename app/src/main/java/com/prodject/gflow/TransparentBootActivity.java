package com.prodject.gflow;

import android.app.Activity;
import android.os.Bundle;

public class TransparentBootActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraForegroundService.start(this);
        finish();
    }
}
