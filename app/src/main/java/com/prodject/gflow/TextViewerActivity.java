package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.InputType;
import android.text.style.BackgroundColorSpan;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

public class TextViewerActivity extends Activity {
    private File currentFile;
    private String fullText = "";
    private LinearLayout contentHost;
    private TextView textView;
    private TextView statusView;
    private EditText searchInput;
    private boolean monospace = true;
    private boolean wrapLines = true;
    private String lastQuery = "";
    private int lastMatchStart = -1;
    private int lastMatchEnd = -1;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentFile = pathFile();
        fullText = read();
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        currentFile = pathFile();
        fullText = read();
        renderContent();
    }

    private View buildShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(Ui.dashboardBg(this));
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 72)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));

        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));

        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildSearchPanel(), lpMatchWrap(0, 0, 0, 16));
        contentHost.addView(buildViewerPanel(), lpMatchWrap(0, 0, 0, 16));
        updateStatus();
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Search", this::findNext, true);
        addDockButton(dock, "Copy", this::copyText, false);
        addDockButton(dock, "Share", this::shareCurrent, false);
        addDockButton(dock, "Wrap", () -> {
            wrapLines = !wrapLines;
            renderContent();
        }, false);
        addDockButton(dock, "Mono", () -> {
            monospace = !monospace;
            renderContent();
        }, false);
        return dock;
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, "Text Viewer"));
        titleBlock.addView(Ui.text(this, "Текст", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Type", extension()));
        bar.addView(buildTopStat("Lines", String.valueOf(lineCount())));
        bar.addView(buildTopStat("Mode", monospace ? "mono" : "ui"));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Open / Search / Copy / Share"));

        LinearLayout row = Ui.row(this);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Файл", currentFile == null ? "Нет файла" : currentFile.getName()));
        left.addView(metricLine("Путь", currentFile == null ? "n/a" : currentFile.getAbsolutePath()));
        left.addView(metricLine("Размер", currentFile == null ? "n/a" : readableBytes(currentFile.length())));
        left.addView(metricLine("Режим", (monospace ? "monospace" : "default") + " · " + (wrapLines ? "wrap on" : "wrap off")));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout badge = Ui.glassCard(this);
        badge.setGravity(Gravity.CENTER);
        TextView label = Ui.text(this, extension().toUpperCase(Locale.ROOT), 28, true);
        label.setGravity(Gravity.CENTER);
        badge.addView(label);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        badgeLp.leftMargin = Ui.dp(this, 12);
        row.addView(badge, badgeLp);
        hero.addView(row);

        statusView = Ui.text(this, statusText(), 16, true);
        statusView.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 4));
        hero.addView(statusView);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Поиск", this::findNext);
        addActionChip(quick, "Копировать", this::copyText);
        addActionChip(quick, "Share", this::shareCurrent);
        addActionChip(quick, wrapLines ? "Wrap Off" : "Wrap On", () -> {
            wrapLines = !wrapLines;
            renderContent();
        });
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "File", currentFile == null ? "n/a" : currentFile.getName(), Ui.CYAN);
        addStatusCard(grid, "Search", lastQuery.isEmpty() ? "idle" : lastQuery, Ui.SUCCESS);
        addStatusCard(grid, "Lines", String.valueOf(lineCount()), Ui.WARNING);
        addStatusCard(grid, "Wrap", wrapLines ? "on" : "off", Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildSearchPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Search"));
        panel.addView(Ui.text(this, "Поиск по тексту, переключение monospace и wrap lines on/off.", 14, false));

        searchInput = edit("Поиск по тексту", lastQuery);
        panel.addView(searchInput);

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Найти", this::findNext);
        addActionChip(row, monospace ? "UI Font" : "Monospace", () -> {
            monospace = !monospace;
            renderContent();
        });
        addActionChip(row, wrapLines ? "Wrap Off" : "Wrap On", () -> {
            wrapLines = !wrapLines;
            renderContent();
        });
        addActionChip(row, "Очистить", () -> {
            lastQuery = "";
            if (searchInput != null) searchInput.setText("");
            renderContent();
        });
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private LinearLayout buildViewerPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Viewer"));
        panel.addView(Ui.text(this, "Поддержка .txt, .log, .json, .xml и других текстовых файлов.", 14, false));

        if (!wrapLines) {
            HorizontalScrollView horizontal = new HorizontalScrollView(this);
            horizontal.setHorizontalScrollBarEnabled(true);
            textView = buildTextView();
            horizontal.addView(textView);
            panel.addView(horizontal, lpFixedHeight(Ui.dp(this, 560)));
        } else {
            ScrollView inner = new ScrollView(this);
            inner.setVerticalScrollBarEnabled(true);
            textView = buildTextView();
            inner.addView(textView);
            panel.addView(inner, lpFixedHeight(Ui.dp(this, 560)));
        }

        LinearLayout row = Ui.row(this);
        addActionChip(row, "Copy", this::copyText);
        addActionChip(row, "Share", this::shareCurrent);
        addActionChip(row, "Info", this::showInfoSheet);
        panel.addView(row, lpMatchWrap(0, 12, 0, 0));
        return panel;
    }

    private TextView buildTextView() {
        TextView view = Ui.text(this, "", 14, false);
        view.setText(highlightedText());
        view.setTextColor(Ui.primaryText(this));
        view.setTextIsSelectable(true);
        view.setMovementMethod(new ScrollingMovementMethod());
        view.setTypeface(monospace ? Typeface.MONOSPACE : Typeface.DEFAULT);
        view.setHorizontallyScrolling(!wrapLines);
        view.setBackground(Ui.cardBg(this, Color.argb(28, 255, 255, 255), Ui.dp(this, 22), Ui.glassLine(this)));
        view.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        return view;
    }

    private void findNext() {
        String query = searchInput == null ? lastQuery : searchInput.getText().toString().trim();
        lastQuery = query;
        if (query.isEmpty()) {
            Ui.toast(this, "Введите строку поиска");
            updateStatus();
            return;
        }
        String source = fullText.toLowerCase(Locale.ROOT);
        int index = source.indexOf(query.toLowerCase(Locale.ROOT));
        if (index < 0) {
            lastMatchStart = -1;
            lastMatchEnd = -1;
            Ui.toast(this, "Совпадение не найдено");
            renderContent();
            updateStatus();
            return;
        }
        lastMatchStart = index;
        lastMatchEnd = Math.min(fullText.length(), index + query.length());
        renderContent();
        Ui.toast(this, "Найдено: " + (index + 1));
        updateStatus();
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(currentFile == null ? "text" : currentFile.getName(), fullText));
            Ui.toast(this, "Текст скопирован");
        }
    }

    private void shareCurrent() {
        if (currentFile == null || !currentFile.exists()) {
            Ui.toast(this, "Файл не найден");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", currentFile);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, fullText.length() > 2000 ? fullText.substring(0, 2000) : fullText);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Поделиться"));
        } catch (Exception e) {
            Ui.toast(this, "Share error: " + e.getMessage());
        }
    }

    private void showInfoSheet() {
        String body = currentFile == null ? "Нет данных"
                : "Имя: " + currentFile.getName()
                + "\nПуть: " + currentFile.getAbsolutePath()
                + "\nРазмер: " + readableBytes(currentFile.length())
                + "\nСтрок: " + lineCount()
                + "\nТип: " + extension();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, "Text Info", 22, true));
        sheet.addView(Ui.muted(this, body));
        builder.setView(sheet);
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    private void updateStatus() {
        if (statusView != null) statusView.setText(statusText());
    }

    private String displayText() {
        if (fullText == null || fullText.isEmpty()) return "Откройте текстовый файл из файлового менеджера.";
        return fullText;
    }

    private CharSequence highlightedText() {
        String value = displayText();
        if (lastMatchStart < 0 || lastMatchEnd <= lastMatchStart || lastMatchEnd > value.length()) return value;
        SpannableString span = new SpannableString(value);
        span.setSpan(new BackgroundColorSpan(Color.argb(140, 77, 163, 255)), lastMatchStart, lastMatchEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private String statusText() {
        return "query: " + (lastQuery.isEmpty() ? "idle" : lastQuery)
                + " · mode: " + (monospace ? "monospace" : "default")
                + " · wrap: " + (wrapLines ? "on" : "off");
    }

    private File pathFile() {
        String path = getIntent().getStringExtra("path");
        return path == null ? null : new File(path);
    }

    private String read() {
        if (currentFile == null) return "";
        try {
            return new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Не удалось открыть файл: " + e.getMessage();
        }
    }

    private String extension() {
        if (currentFile == null) return "txt";
        String name = currentFile.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "txt";
    }

    private int lineCount() {
        if (fullText == null || fullText.isEmpty()) return 0;
        return fullText.split("\n", -1).length;
    }

    private String readableBytes(long bytes) {
        if (bytes > 1024L * 1024L * 1024L) return String.format(Locale.US, "%.1f GB", bytes / 1024d / 1024d / 1024d);
        if (bytes > 1024L * 1024L) return String.format(Locale.US, "%.1f MB", bytes / 1024d / 1024d);
        if (bytes > 1024L) return String.format(Locale.US, "%.1f KB", bytes / 1024d);
        return bytes + " B";
    }

    private EditText edit(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setTypeface(Typeface.MONOSPACE);
        field.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 18), Ui.glassLine(this)));
        field.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        field.setLayoutParams(lpMatchWrap(0, 12, 0, 0));
        return field;
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : Color.argb(54, 255, 255, 255),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : Color.TRANSPARENT));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 18, true));
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private LinearLayout.LayoutParams lpFixedHeight(int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.topMargin = Ui.dp(this, 12);
        return lp;
    }
}
