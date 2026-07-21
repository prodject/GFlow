package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.hardware.camera2.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class DvrActivity extends Activity {
    private TextView status;
    private LinearLayout root;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        root = Ui.root(this, "DVR / Камеры");
        Button start = Ui.button(this, "Старт записи");
        Button stop = Ui.button(this, "Стоп записи");
        Button refresh = Ui.button(this, "Обновить камеры/архив");
        status = Ui.text(this, "", 14, false);
        root.addView(Ui.text(this, new EcarxDvrAdapter(this).availability(), 14, false));
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
        addEvs("EVS открыть rear", EcarxDvrAdapter.EVS_CAMERA_REAR, true);
        addEvs("EVS закрыть rear", EcarxDvrAdapter.EVS_CAMERA_REAR, false);
        addEvs("EVS открыть AVM/360", EcarxDvrAdapter.EVS_CAMERA_AVM, true);
        addEvs("EVS закрыть AVM/360", EcarxDvrAdapter.EVS_CAMERA_AVM, false);
        addEvs("EVS открыть DVR", EcarxDvrAdapter.EVS_CAMERA_DVR, true);
        addDvr("DVR камера online", a -> a.dvrCameraOnline());
        addDvr("DVR capture", a -> a.dvrCapture());
        addDvr("DVR mode", a -> a.dvrCurrentMode());
        addDvr("DVR SD status", a -> a.dvrSdcardStatus());
        root.addView(status, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refresh();
    }

    private void addEvs(String label, int cameraId, boolean open) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxDvrAdapter adapter = new EcarxDvrAdapter(this);
            EcarxDvrAdapter.Result result = open ? adapter.openEvs(cameraId) : adapter.closeEvs(cameraId);
            Ui.toast(this, result.success ? "EVS команда отправлена" : "EVS ошибка");
            status.setText(result.message + "\n\n" + status.getText());
        });
        root.addView(b);
    }

    private interface DvrCall { EcarxDvrAdapter.Result run(EcarxDvrAdapter adapter); }

    private void addDvr(String label, DvrCall call) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxDvrAdapter.Result result = call.run(new EcarxDvrAdapter(this));
            Ui.toast(this, result.success ? "DVR команда выполнена" : "DVR ошибка");
            status.setText(result.message + "\n\n" + status.getText());
        });
        root.addView(b);
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
