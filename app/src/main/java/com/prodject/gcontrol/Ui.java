package com.prodject.gcontrol;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

final class Ui {
    static final int BLACK = Color.rgb(5, 5, 5);
    static final int LINE = Color.rgb(224, 224, 224);
    static final int BG = Color.rgb(247, 248, 250);

    static LinearLayout root(Activity a, String title) {
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a, 16), dp(a, 14), dp(a, 16), dp(a, 12));
        root.setBackgroundColor(BG);
        TextView h = text(a, title, 26, true);
        root.addView(h);
        return root;
    }

    static TextView text(Context c, String s, int sp, boolean bold) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(BLACK);
        v.setPadding(0, dp(c, 6), 0, dp(c, 6));
        if (bold) v.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return v;
    }

    static Button button(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        return b;
    }

    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }
}
