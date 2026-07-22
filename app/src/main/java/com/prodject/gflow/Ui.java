package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import java.util.*;

final class Ui {
    static final int BLACK = Color.rgb(18, 20, 22);
    static final int TEXT_MUTED = Color.rgb(96, 103, 110);
    static final int LINE = Color.rgb(220, 225, 229);
    static final int BG = Color.rgb(241, 244, 246);
    static final int PANEL = Color.rgb(255, 255, 255);
    static final int PANEL_DARK = Color.rgb(30, 34, 38);
    static final int BLUE = Color.rgb(34, 116, 205);
    static final int GREEN = Color.rgb(32, 145, 90);
    static final int AMBER = Color.rgb(198, 128, 28);

    static boolean dark(Context c) {
        return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    static int bg(Context c) { return dark(c) ? Color.rgb(13, 15, 17) : BG; }
    static int panel(Context c) { return dark(c) ? PANEL_DARK : PANEL; }
    static int textColor(Context c) { return dark(c) ? Color.rgb(238, 241, 244) : BLACK; }
    static int mutedColor(Context c) { return dark(c) ? Color.rgb(158, 166, 174) : TEXT_MUTED; }
    static int lineColor(Context c) { return dark(c) ? Color.rgb(55, 61, 68) : LINE; }

    static LinearLayout root(Activity a, String title) {
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a, 22), dp(a, 18), dp(a, 22), dp(a, 18));
        root.setBackgroundColor(bg(a));
        LinearLayout head = row(a);
        TextView h = text(a, title, 28, true);
        h.setLetterSpacing(0);
        head.addView(h, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(head);
        return root;
    }

    static LinearLayout root(Activity a, String title, Runnable back) {
        LinearLayout root = root(a, title);
        if (back != null) {
            LinearLayout head = (LinearLayout) root.getChildAt(0);
            Button b = iconButton(a, "‹", "Назад");
            b.setOnClickListener(v -> back.run());
            head.addView(b, new LinearLayout.LayoutParams(dp(a, 44), dp(a, 44)));
        }
        return root;
    }

    static TextView text(Context c, String s, int sp, boolean bold) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(textColor(c));
        v.setPadding(0, dp(c, 6), 0, dp(c, 6));
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setLineSpacing(dp(c, 2), 1.0f);
        return v;
    }

    static Button button(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(textColor(c));
        b.setTextSize(15);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(dp(c, 18), 0, dp(c, 18), 0);
        b.setMinHeight(dp(c, 54));
        b.setBackground(cardBg(c, panel(c), dp(c, 14), lineColor(c)));
        if (Build.VERSION.SDK_INT >= 21) b.setStateListAnimator(null);
        return b;
    }

    static TextView muted(Context c, String s) {
        TextView v = text(c, s, 14, false);
        v.setTextColor(mutedColor(c));
        return v;
    }

    static TextView pill(Context c, String s, int color) {
        TextView v = text(c, s, 12, true);
        v.setTextColor(Color.WHITE);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4));
        v.setBackground(cardBg(c, color, dp(c, 18), color));
        return v;
    }

    static LinearLayout card(Context c) {
        LinearLayout v = new LinearLayout(c);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
        v.setBackground(cardBg(c, panel(c), dp(c, 18), lineColor(c)));
        return v;
    }

    static LinearLayout row(Context c) {
        LinearLayout v = new LinearLayout(c);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    static Button help(Context c, String title, String message) {
        Button b = new Button(c);
        b.setText("?");
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(textColor(c));
        b.setGravity(Gravity.CENTER);
        b.setMinWidth(dp(c, 38));
        b.setMinHeight(dp(c, 38));
        b.setPadding(0, 0, 0, 0);
        b.setBackground(cardBg(c, dark(c) ? Color.rgb(43, 49, 55) : Color.rgb(232, 237, 242), dp(c, 19), Color.TRANSPARENT));
        b.setOnClickListener(v -> dialog(c, title, message));
        return b;
    }

    static Button iconButton(Context c, String symbol, String description) {
        Button b = help(c, description, description);
        b.setText(symbol);
        b.setTextSize(24);
        b.setContentDescription(description);
        b.setOnClickListener(null);
        return b;
    }

    static void dialog(Context c, String title, String message) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 22);
        box.setPadding(p, dp(c, 18), p, dp(c, 10));
        TextView h = text(c, title, 22, true);
        TextView body = muted(c, message);
        body.setTextSize(15);
        box.addView(h);
        box.addView(body);
        AlertDialog dialog = new AlertDialog.Builder(c).setView(box).setPositiveButton("Понятно", null).create();
        dialog.setOnShowListener(d -> {
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(cardBg(c, panel(c), dp(c, 22), Color.TRANSPARENT));
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
        });
        dialog.show();
    }

    static void section(LinearLayout root, String title, String hint) {
        LinearLayout row = row(root.getContext());
        TextView h = text(root.getContext(), title, 18, true);
        row.addView(h, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (hint != null && !hint.trim().isEmpty()) row.addView(help(root.getContext(), title, hint));
        root.addView(row);
    }

    static void animateIn(View v) {
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), 18));
        v.animate().alpha(1f).translationY(0f).setDuration(260).setInterpolator(new DecelerateInterpolator()).start();
    }

    static GradientDrawable cardBg(Context c, int color, int radius, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (stroke != Color.TRANSPARENT) g.setStroke(dp(c, 1), stroke);
        return g;
    }

    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }
}
