package com.prodject.gflow;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;

public class TextViewerActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this, "Текстовый файл");
        TextView text = Ui.text(this, read(), 15, false);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }
    private String read() {
        String path = getIntent().getStringExtra("path");
        if (path == null) return "Откройте текстовый файл из файлового менеджера.";
        try { return new String(java.nio.file.Files.readAllBytes(new File(path).toPath())); }
        catch (Exception e) { return "Не удалось открыть файл: " + e.getMessage(); }
    }
}
