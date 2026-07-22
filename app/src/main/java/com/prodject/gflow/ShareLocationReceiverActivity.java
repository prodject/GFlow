package com.prodject.gflow;

import android.app.Activity;
import android.os.Bundle;

public class ShareLocationReceiverActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoiceFlowRouter.routeSharedLocation(this, getIntent());
        finish();
    }
}
