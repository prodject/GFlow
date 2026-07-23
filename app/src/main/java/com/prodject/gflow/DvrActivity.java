package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.camera2.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.*;

public class DvrActivity extends Activity {
    private TextView status;
    private LinearLayout root;
    private EditText camerasInput;
    private EditText segmentInput;
    private EditText limitInput;
    private EditText storagePathInput;
    private Spinner storageInput;
    private Spinner qualityInput;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView scroll = new ScrollView(this);
        root = Ui.root(this, "DVR / Камеры", this::finish);
        Button start = Ui.button(this, "Старт записи");
        Button stop = Ui.button(this, "Стоп записи");
        Button save = Ui.button(this, "Сохранить настройки DVR");
        Button refresh = Ui.button(this, "Обновить камеры/архив");
        status = Ui.text(this, "", 14, false);
        LinearLayout hero = Ui.card(this);
        hero.addView(Ui.text(this, "Камеры и архив", 22, true));
        hero.addView(new CameraPreviewWidget(this), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 150)));
        hero.addView(Ui.muted(this, new EcarxDvrAdapter(this).availability()));
        root.addView(hero, margin(0, 8, 0, 12));
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
        LinearLayout actions = Ui.card(this);
        actions.addView(Ui.text(this, "Запись", 18, true));
        LinearLayout row = Ui.row(this);
        row.addView(save, buttonLp());
        row.addView(start, buttonLp());
        row.addView(stop, buttonLp());
        row.addView(refresh, buttonLp());
        actions.addView(row);
        root.addView(actions, margin(0, 0, 0, 12));
        LinearLayout diag = Ui.card(this);
        diag.addView(Ui.text(this, "Штатные камеры / EVS", 18, true));
        root.addView(diag, margin(0, 0, 0, 12));
        LinearLayout oldRoot = root;
        root = diag;
        addEvs("EVS открыть rear", EcarxDvrAdapter.EVS_CAMERA_REAR, true);
        addEvs("EVS закрыть rear", EcarxDvrAdapter.EVS_CAMERA_REAR, false);
        addEvs("EVS открыть AVM/360", EcarxDvrAdapter.EVS_CAMERA_AVM, true);
        addEvs("EVS закрыть AVM/360", EcarxDvrAdapter.EVS_CAMERA_AVM, false);
        addEvs("EVS открыть DVR", EcarxDvrAdapter.EVS_CAMERA_DVR, true);
        addDvr("DVR камера online", a -> a.dvrCameraOnline());
        addDvr("DVR capture", a -> a.dvrCapture());
        addDvr("DVR mode", a -> a.dvrCurrentMode());
        addDvr("DVR SD status", a -> a.dvrSdcardStatus());
        root = oldRoot;
        LinearLayout statusCard = Ui.card(this);
        statusCard.addView(Ui.text(this, "Статус", 18, true));
        statusCard.addView(status);
        root.addView(statusCard);
        scroll.addView(root);
        setContentView(scroll);
        refresh();
    }

    private void addSettingsUi() {
        SharedPreferences prefs = getSharedPreferences(DvrArchive.PREFS, MODE_PRIVATE);
        camerasInput = new EditText(this);
        camerasInput.setHint("front,rear,camera2:0,evs:360,evs:rear,evs:dvr");
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
        storagePathInput = new EditText(this);
        storagePathInput.setHint("Путь записи, например /storage/XXXX-XXXX/GFlowDvr");
        storagePathInput.setText(prefs.getString(DvrArchive.KEY_STORAGE_PATH, ""));
        qualityInput = new Spinner(this);
        String[] quality = {DvrArchive.QUALITY_720P, DvrArchive.QUALITY_1080P, DvrArchive.QUALITY_480P};
        qualityInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, quality));
        String currentQuality = prefs.getString(DvrArchive.KEY_QUALITY, DvrArchive.QUALITY_720P);
        for (int i = 0; i < quality.length; i++) if (quality[i].equals(currentQuality)) qualityInput.setSelection(i);
        styleInput(camerasInput);
        styleInput(segmentInput);
        styleInput(limitInput);
        styleInput(storagePathInput);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Настройки архива", 18, true));
        card.addView(Ui.muted(this, "Camera2 источники пишутся напрямую. EVS открывает штатный вид и пробует screenrecord."));
        card.addView(camerasInput, margin(0, 8, 0, 6));
        LinearLayout nums = Ui.row(this);
        nums.addView(segmentInput, buttonLp());
        nums.addView(limitInput, buttonLp());
        card.addView(nums);
        card.addView(storageInput, margin(0, 8, 0, 6));
        card.addView(storagePathInput, margin(0, 4, 0, 6));
        card.addView(qualityInput, margin(0, 4, 0, 0));
        root.addView(card, margin(0, 0, 0, 12));
    }

    private void saveSettings() {
        DvrArchive.saveSettings(this,
                camerasInput.getText().toString(),
                parseInt(segmentInput.getText().toString(), 60),
                parseInt(limitInput.getText().toString(), 5),
                String.valueOf(storageInput.getSelectedItem()),
                String.valueOf(qualityInput.getSelectedItem()),
                storagePathInput.getText().toString());
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
                sb.append("camera2:").append(id).append(" · ").append(facingName(facing)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Ошибка Camera2: ").append(e.getMessage()).append("\n");
        }
        sb.append("\nEVS источники из OneOS AdaptAPI: evs:rear, evs:360, evs:dvr\n");
        sb.append("\nUSB кандидаты:\n").append(usbRootsSummary());
        sb.append("\n").append(DvrArchive.summary(this));
        status.setText(sb.toString());
    }

    private String usbRootsSummary() {
        StringBuilder sb = new StringBuilder();
        File storage = new File("/storage");
        File[] roots = storage.listFiles(file -> file.isDirectory()
                && file.canWrite()
                && !"emulated".equals(file.getName())
                && !"self".equals(file.getName()));
        if (roots == null || roots.length == 0) return "не найдены\n";
        Arrays.sort(roots, Comparator.comparing(File::getAbsolutePath));
        for (File root : roots) sb.append(root.getAbsolutePath()).append("\n");
        return sb.toString();
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

    private void styleInput(EditText e) {
        e.setTextColor(Ui.textColor(this));
        e.setHintTextColor(Ui.mutedColor(this));
        e.setSingleLine(true);
        e.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        e.setBackground(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 14), Ui.lineColor(this)));
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1);
        lp.setMargins(Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5));
        return lp;
    }

    private static final class CameraPreviewWidget extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        CameraPreviewWidget(Context c) { super(c); }
        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Ui.dark(getContext()) ? Color.rgb(24, 29, 34) : Color.rgb(222, 230, 236));
            canvas.drawRoundRect(new RectF(w * .03f, h * .08f, w * .97f, h * .92f), Ui.dp(getContext(), 24), Ui.dp(getContext(), 24), p);
            int[] colors = {Color.rgb(54, 132, 210), Color.rgb(45, 150, 118), Color.rgb(190, 92, 75)};
            for (int i = 0; i < 3; i++) {
                float left = w * (.08f + i * .29f);
                RectF r = new RectF(left, h * .22f, left + w * .24f, h * .78f);
                p.setColor(colors[i]);
                canvas.drawRoundRect(r, Ui.dp(getContext(), 16), Ui.dp(getContext(), 16), p);
                p.setColor(Color.argb(110, 255, 255, 255));
                canvas.drawCircle(r.centerX(), r.centerY(), Ui.dp(getContext(), 22), p);
            }
        }
    }
}
