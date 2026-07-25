package com.prodject.gflow;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Locale;

final class EcarxSafetyAdapter {
    static final int SETTING_FUNC_TRUNK_STATE = 0x2c020600;
    static final int SETTING_FUNC_TRUNK_OPENING_PERCENTAGE = 0x2c030700;

    static final int TRUNK_STATE_UNKNOW = 0x2c020601;
    static final int TRUNK_STATE_FULL_CLOSE = 0x2c020602;
    static final int TRUNK_STATE_MOVE_UP = 0x2c020603;
    static final int TRUNK_STATE_MOVE_UP_BREAK = 0x2c020604;
    static final int TRUNK_STATE_STOP_DURING_OPEN = 0x2c020605;
    static final int TRUNK_STATE_FULL_OPEN = 0x2c020606;
    static final int TRUNK_STATE_MOVE_DOWN = 0x2c020607;
    static final int TRUNK_STATE_MOVE_DOWN_BREAK = 0x2c020608;
    static final int TRUNK_STATE_STOP_DURING_CLOSE = 0x2c020609;
    static final int TRUNK_STATE_HALF_CLOSE = 0x2c020610;
    static final int TRUNK_STATE_STOP_MIN_POSITION = 0x2c020611;

    private final Context context;
    private Object vehicle;
    private Object safety;

    EcarxSafetyAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    EcarxVehicleAdapter.Result get(int functionId, int zone) {
        try {
            Object value = safety().getClass()
                    .getMethod("getFunctionValue", int.class, int.class)
                    .invoke(safety(), functionId, zone);
            return EcarxVehicleAdapter.Result.external(
                    String.format(Locale.US, "Safety getFunctionValue 0x%08x/%d = 0x%08x",
                            functionId, zone, ((Number) value).intValue()),
                    true,
                    true);
        } catch (Exception e) {
            return EcarxVehicleAdapter.Result.external("Safety getFunctionValue failed: " + compact(e), false, false);
        }
    }

    EcarxVehicleAdapter.Result getFloat(int functionId, int zone) {
        try {
            Object value = safety().getClass()
                    .getMethod("getCustomizeFunctionValue", int.class, int.class)
                    .invoke(safety(), functionId, zone);
            return EcarxVehicleAdapter.Result.external(
                    String.format(Locale.US, "Safety getCustomizeFunctionValue 0x%08x/%d = %.1f",
                            functionId, zone, ((Number) value).floatValue()),
                    true,
                    true);
        } catch (Exception e) {
            return EcarxVehicleAdapter.Result.external("Safety getCustomizeFunctionValue failed: " + compact(e), false, false);
        }
    }

    private Object safety() throws Exception {
        if (safety == null) {
            safety = vehicle().getClass().getMethod("getSafety").invoke(vehicle());
            if (safety == null) throw new IllegalStateException("getSafety returned null");
        }
        return safety;
    }

    private Object vehicle() throws Exception {
        if (vehicle == null) {
            Class<?> cls = Class.forName("com.ecarx.xui.adaptapi.car.vehicle.Vehicle");
            vehicle = cls.getMethod("create", Context.class).invoke(null, context);
            if (vehicle == null) throw new IllegalStateException("Vehicle.create returned null");
        }
        return vehicle;
    }

    private static String compact(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}
