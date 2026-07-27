package com.prodject.gflow;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.LinkedHashSet;

final class CarFunctionSelector {
    interface Sender {
        void send(int functionId, int zone, int value);
    }

    private CarFunctionSelector() {}

    static void show(Activity activity, String title, int functionId, int zone, Sender sender) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(activity);
        CarFunctionCatalog.Entry entry = adapter.catalogEntry(functionId);
        int[] runtimeValues = adapter.supportedValues(functionId, zone);
        ValueItem[] values = resolveValues(adapter, functionId, runtimeValues);

        Dialog dialog = new Dialog(activity);
        LinearLayout root = Ui.deepCard(activity);
        root.addView(Ui.label(activity, entry == null ? "Vehicle function" : entry.key));
        root.addView(Ui.text(activity, title, 24, true));
        if (entry != null && !entry.description.isEmpty()) {
            root.addView(Ui.muted(activity, entry.description));
        }
        root.addView(Ui.muted(activity, compact(adapter.catalogReadInt(functionId, zone).message)));
        root.addView(Ui.muted(activity, "Runtime values: " + runtimeValues.length));

        GridLayout grid = new GridLayout(activity);
        grid.setColumnCount(2);
        int top = Ui.dp(activity, 14);
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) grid.getLayoutParams()).topMargin = top;

        if (values.length == 0) {
            root.addView(Ui.muted(activity, "Нет static possibleValues и runtime getSupportedFunctionValue пустой."));
        } else {
            for (ValueItem item : values) {
                Button chip = Ui.button(activity, cleanLabel(item.label));
                chip.setTextSize(13);
                chip.setEnabled(item.supported);
                chip.setAlpha(item.supported ? 1f : 0.38f);
                chip.setBackground(Ui.cardBg(activity,
                        item.supported ? Color.argb(92, 77, 163, 255) : Color.argb(34, 255, 255, 255),
                        Ui.dp(activity, 18),
                        item.supported ? Color.argb(128, 77, 163, 255) : Color.TRANSPARENT));
                chip.setOnClickListener(v -> {
                    dialog.dismiss();
                    sender.send(functionId, zone, item.value);
                });
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = Ui.dp(activity, 58);
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                lp.setMargins(Ui.dp(activity, 5), Ui.dp(activity, 5), Ui.dp(activity, 5), Ui.dp(activity, 5));
                grid.addView(chip, lp);
            }
        }

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(root);
        dialog.setContentView(scroll);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
        Window shown = dialog.getWindow();
        if (shown != null) {
            shown.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            shown.setGravity(Gravity.BOTTOM);
        }
    }

    static boolean shouldSelect(Activity activity, int functionId, int zone, int requestedValue) {
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(activity);
        CarFunctionCatalog.Value[] staticValues = CarFunctionCatalog.staticValues(functionId);
        int[] runtimeValues = adapter.supportedValues(functionId, zone);
        if (requestedValue == EcarxVehicleAdapter.COMMON_ON && hasSelectorValues(staticValues, runtimeValues)) {
            return true;
        }
        if (contains(staticValues, requestedValue) || contains(runtimeValues, requestedValue)) return false;
        return staticValues.length > 0 || runtimeValues.length > 0;
    }

    private static boolean hasSelectorValues(CarFunctionCatalog.Value[] staticValues, int[] runtimeValues) {
        if (staticValues.length > 0) return !isBinary(staticValues);
        return runtimeValues.length > 0 && !isBinary(runtimeValues);
    }

    private static boolean isBinary(CarFunctionCatalog.Value[] values) {
        if (values.length != 2) return false;
        return contains(values, EcarxVehicleAdapter.COMMON_OFF) && contains(values, EcarxVehicleAdapter.COMMON_ON);
    }

    private static boolean isBinary(int[] values) {
        if (values.length != 2) return false;
        return contains(values, EcarxVehicleAdapter.COMMON_OFF) && contains(values, EcarxVehicleAdapter.COMMON_ON);
    }

    private static ValueItem[] resolveValues(EcarxVehicleAdapter adapter, int functionId, int[] runtimeValues) {
        CarFunctionCatalog.Value[] staticValues = CarFunctionCatalog.staticValues(functionId);
        LinkedHashSet<Integer> runtime = new LinkedHashSet<>();
        for (int value : runtimeValues) runtime.add(value);

        LinkedHashSet<ValueItem> out = new LinkedHashSet<>();
        if (staticValues.length > 0) {
            for (CarFunctionCatalog.Value value : staticValues) {
                boolean supported = runtime.isEmpty() || runtime.contains(value.value);
                out.add(new ValueItem(value.key, value.value, supported));
            }
        } else {
            for (int value : runtimeValues) {
                out.add(new ValueItem(EcarxVehicleAdapter.hex(value), value, true));
            }
        }
        return out.toArray(new ValueItem[0]);
    }

    private static boolean contains(CarFunctionCatalog.Value[] values, int requested) {
        for (CarFunctionCatalog.Value value : values) {
            if (value.value == requested) return true;
        }
        return false;
    }

    private static boolean contains(int[] values, int requested) {
        for (int value : values) {
            if (value == requested) return true;
        }
        return false;
    }

    private static String cleanLabel(String value) {
        return value.replace("SETTING_FUNC_", "")
                .replace("CUSTOM_KEY_TYPE_", "")
                .replace("_", " ");
    }

    private static String compact(String value) {
        return value == null ? "" : value.replace("getFunctionValue", "").replace("getCustomizeFunctionValue", "").trim();
    }

    private static final class ValueItem {
        final String label;
        final int value;
        final boolean supported;

        ValueItem(String label, int value, boolean supported) {
            this.label = label;
            this.value = value;
            this.supported = supported;
        }

        @Override public boolean equals(Object other) {
            return other instanceof ValueItem && ((ValueItem) other).value == value;
        }

        @Override public int hashCode() {
            return value;
        }
    }
}
