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
    private EditText camerasInput;
    private EditText segmentInput;
    private EditText limitInput;
    private Spinner storageInput;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        root = Ui.root(this, "DVR / Камеры");
        Button start = Ui.button(this, "Старт записи");
        Button stop = Ui.button(this, "Стоп записи");
        Button save = Ui.button(this, "Сохранить настройки DVR");
        Button refresh = Ui.button(this, "Обновить камеры/архив");
        status = Ui.text(this, "", 14, false);
        root.addView(Ui.text(this, new EcarxDvrAdapter(this).availability(), 14, false));
        addSettingsUi();
        start.setOnClickListener(v -> {
            saveSettings();
            startForegroundService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_START));
            refresh();
        });
        stop.setOnClickListener(v -> {
            startService(new Intent(this, DvrService.class).setAction(DvrService.ACTION_STOP));
            refresh();
        });
        save.setOnClickListener(v -> {
            saveSettings();
            refresh();
        });
        refresh.setOnClickListener(v -> refresh());
        root.addView(save);
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

    private void addSettingsUi() {
        SharedPreferences prefs = getSharedPreferences(DvrArchive.PREFS, MODE_PRIVATE);
        camerasInput = new EditText(this);
        camerasInput.setHint("front,rear,left,right или Camera2 id через запятую");
        camerasInput.setText(prefs.getString(DvrArchive.KEY_CAMERAS, DvrArchive.DEFAULT_CAMERAS));
        segmentInput = new EditText(this);
        segmentInput.setHint("Длина сегмента, сек");
        segmentInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        segmentInput.setText(String.valueOf(prefs.getInt(DvrArchive.KEY_SEGMENT_SECONDS, 60)));
        limitInput = new EditText(this);
        limitInput.setHint("Лимит архива, GB");
        limitInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        limitInput.setText(String.valueOf(prefs.getInt(DvrArchive.KEY_LIMIT_GB, 5)));
        storageInput = new Spinner(this);
        String[] storage = {DvrArchive.STORAGE_EXTERNAL, DvrArchive.STORAGE_INTERNAL, DvrArchive.STORAGE_USB};
        storageInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, storage));
        String current = prefs.getString(DvrArchive.KEY_STORAGE, DvrArchive.STORAGE_EXTERNAL);
        for (int i = 0; i < storage.length; i++) if (storage[i].equals(current)) storageInput.setSelection(i);
        root.addView(Ui.text(this, "Настройки записи marker-сегментов: камеры, длина сегмента, лимит и целевое хранилище.", 14, false));
        root.addView(camerasInput);
        root.addView(segmentInput);
        root.addView(limitInput);
        root.addView(storageInput);
    }

    private void saveSettings() {
        DvrArchive.saveSettings(this,
                camerasInput.getText().toString(),
                parseInt(segmentInput.getText().toString(), 60),
                parseInt(limitInput.getText().toString(), 5),
                String.valueOf(storageInput.getSelectedItem()));
        Ui.toast(this, "Настройки DVR сохранены");
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

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
