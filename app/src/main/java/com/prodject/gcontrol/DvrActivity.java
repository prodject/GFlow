package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.hardware.camera2.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class DvrActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this, "DVR / Камеры");
        Button start = Ui.button(this, "Старт записи");
        Button stop = Ui.button(this, "Стоп записи");
        Button refresh = Ui.button(this, "Обновить камеры/архив");
        status = Ui.text(this, "", 14, false);
        start.setOnClickListener(v -> {
            startForegroundService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_START));
            refresh();
        });
        stop.setOnClickListener(v -> {
            startService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_STOP));
            refresh();
        });
        refresh.setOnClickListener(v -> refresh());
        root.addView(start);
        root.addView(stop);
        root.addView(refresh);
        root.addView(status, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refresh();
    }

    private void refresh() {
        StringBuilder sb = new StringBuilder();
        sb.append("Камеры Camera2:\n");
        try {
            CameraManager cm = getSystemService(CameraManager.class);
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics cc = cm.getCameraCharacteristics(id);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                sb.append("cam").append(id).append(" · ").append(facingName(facing)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Ошибка Camera2: ").append(e.getMessage()).append("\n");
        }
        sb.append("\n").append(DvrArchive.summary(this));
        status.setText(sb.toString());
    }

    private String facingName(Integer facing) {
        if (facing == null) return "unknown";
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "front";
        if (facing == CameraCharacteristics.LENS_FACING_BACK) return "rear";
        if (Build.VERSION.SDK_INT >= 23 && facing == CameraCharacteristics.LENS_FACING_EXTERNAL) return "external";
        return "other";
    }
}
