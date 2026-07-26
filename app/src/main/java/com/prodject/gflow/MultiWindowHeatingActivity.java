package com.prodject.gflow;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class MultiWindowHeatingActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(1, 1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        finish();
    }
}
