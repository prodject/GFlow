package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class VoiceActivity extends Activity {
    private final VoskVoiceRecognizer recognizer = new VoskVoiceRecognizer();
    private TextView result;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        startForegroundService(new Intent(this, VoiceForegroundService.class));
        LinearLayout root = Ui.root(this, "Голосовой ассистент");
        root.addView(Ui.text(this, "Экран прослушивания. Локальная модель: assets/vosk-model-ru, нативная библиотека: libvosk.so. Поддерживаются app.monji.VOICE, VOICE_COMMAND и ASSIST.", 16, false));
        EditText input = new EditText(this);
        input.setHint("Введите или продиктуйте команду");
        Button run = Ui.button(this, "Выполнить");
        result = Ui.text(this, "", 16, true);
        Button listen = Ui.button(this, "Слушать Vosk");
        Button stop = Ui.button(this, "Стоп");
        run.setOnClickListener(v -> {
            String cmd = input.getText().toString().toLowerCase(Locale.ROOT);
            CarCommandBus.send(this, "voice", cmd);
            result.setText("Команда отправлена: " + cmd);
        });
        listen.setOnClickListener(v -> recognizer.start(this, text -> runOnUiThread(() -> result.setText(text))));
        stop.setOnClickListener(v -> recognizer.stop());
        root.addView(input);
        root.addView(run);
        root.addView(listen);
        root.addView(stop);
        root.addView(result);
        setContentView(root);
    }

    @Override protected void onDestroy() {
        recognizer.stop();
        super.onDestroy();
    }
}
