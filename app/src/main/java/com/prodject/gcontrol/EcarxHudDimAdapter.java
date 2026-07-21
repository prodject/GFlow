package com.prodject.gcontrol;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Locale;

final class EcarxHudDimAdapter {
    static final int HUD_MODE_SIMPLE_DISPLAY = 1;
    static final int HUD_MODE_SMART_DRIVING_DISPLAY = 2;
    static final int HUD_MODE_SMART_GUIDE_DISPLAY = 3;
    static final int HUD_MODE_AR_DISPLAY = 4;

    static final int DIM_APP_TYPE_DEFAULT = 0;
    static final int DIM_APP_TYPE_AMAP = 1;
    static final int DIM_PRESENTATION_ROUTE = 1;
    static final int DIM_PRESENTATION_ALWAYS = 2;
    static final int DIM_PRESENTATION_NEVER = 3;

    static final int DIM_TAB_PHONE = 1;
    static final int DIM_TAB_NAVIGATION = 2;
    static final int DIM_TAB_MUSIC = 3;
    static final int DIM_TAB_CONTROL_CENTER = 4;
    static final int DIM_TAB_CLOSE = 5;

    static final int NAVI_MODE_OFF = 1;
    static final int NAVI_MODE_SIMPLIFY = 2;
    static final int NAVI_MODE_FULL = 3;
    static final int NAVI_MODE_AR = 4;
    static final int NAVI_MODE_3D_LANE = 5;

    static final int HVAC_FUNC_TEMP = 0x10060100;
    static final int HVAC_FUNC_TEMP_UNIT = 0x10060600;
    static final int TEMP_UNIT_C = 0x10060601;

    private final Context context;
    private Object hud;
    private Object dim;

    EcarxHudDimAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    String availability() {
        StringBuilder sb = new StringBuilder("OneOS HUD/DIM\n");
        sb.append("HUDInteraction: ").append(probeHud()).append("\n");
        sb.append("DimInteraction: ").append(probeDim());
        return sb.toString();
    }

    Result hudStatus() {
        try {
            Object api = hud();
            StringBuilder sb = new StringBuilder("HUDInteraction\n");
            append(sb, "requestHUDMode", intCall(api, "requestHUDMode"));
            append(sb, "requestCalibrationMode", intCall(api, "requestCalibrationMode"));
            append(sb, "requestADASOpenStatus", intCall(api, "requestADASOpenStatus"));
            append(sb, "requestCarFollowingGAPLevel", intCall(api, "requestCarFollowingGAPLevel"));
            append(sb, "getHUDCalibrationParam", objectCall(api, "getHUDCalibrationParam"));
            return Result.text(true, sb.toString());
        } catch (Exception e) {
            return Result.error("HUDInteraction status", e);
        }
    }

    Result hudSync() {
        try {
            Object api = hud();
            StringBuilder sb = new StringBuilder("HUD sync requests\n");
            append(sb, "requestHUDHeight", voidCall(api, "requestHUDHeight"));
            append(sb, "requestADASSyncInfo", voidCall(api, "requestADASSyncInfo"));
            append(sb, "requestVehicleSyncInfo", voidCall(api, "requestVehicleSyncInfo"));
            return Result.text(true, sb.toString());
        } catch (Exception e) {
            return Result.error("HUDInteraction sync", e);
        }
    }

    Result dimStatus() {
        try {
            Object api = dim();
            StringBuilder sb = new StringBuilder("DimInteraction\n");
            append(sb, "getShowPresentationOption", intCall(api, "getShowPresentationOption"));
            append(sb, "getSupportedRankingType", intCall(api, "getSupportedRankingType"));
            Object menu = dimPart("getDimMenuInteraction");
            append(sb, "DimMenu.getNaviMode", intCall(menu, "getNaviMode"));
            return Result.text(true, sb.toString());
        } catch (Exception e) {
            return Result.error("DimInteraction status", e);
        }
    }

    Result requestDayNightMode() {
        try {
            return voidCall(dim(), "requestDayNightMode");
        } catch (Exception e) {
            return Result.error("requestDayNightMode", e);
        }
    }

    Result setPresentation(boolean enabled) {
        try {
            Method m = dim().getClass().getMethod("setPresentationToDimSwitch", int.class, String.class, String.class, boolean.class);
            m.invoke(dim(), DIM_APP_TYPE_DEFAULT, context.getPackageName(), MainActivity.class.getName(), enabled);
            return Result.text(true, "setPresentationToDimSwitch(default, " + context.getPackageName() + ", MainActivity, " + enabled + ") -> ok");
        } catch (Exception e) {
            return Result.error("setPresentationToDimSwitch", e);
        }
    }

    Result updateAvgFuelRanking(int type, String payload) {
        try {
            Method m = dim().getClass().getMethod("updateAvgFuleRanking", int.class, String.class);
            m.invoke(dim(), type, payload);
            return Result.text(true, "updateAvgFuleRanking(" + type + ", " + payload + ") -> ok");
        } catch (Exception e) {
            return Result.error("updateAvgFuleRanking", e);
        }
    }

    Result dimMenuReadyAndTheme() {
        Object menu = dimPart("getDimMenuInteraction");
        StringBuilder sb = new StringBuilder("DimMenu\n");
        append(sb, "notifyIHUReady", voidCall(menu, "notifyIHUReady"));
        append(sb, "requestDimTheme", voidCall(menu, "requestDimTheme"));
        return Result.text(true, sb.toString());
    }

    Result dimMenuTab(int tab) {
        Object menu = dimPart("getDimMenuInteraction");
        try {
            Method m = menu.getClass().getMethod("requestDimSwitchTabWindow", int.class);
            m.invoke(menu, tab);
            return Result.text(true, "requestDimSwitchTabWindow(" + tab + ") -> ok");
        } catch (Exception e) {
            return Result.error("requestDimSwitchTabWindow", e);
        }
    }

    Result switchNaviMode(int mode) {
        Object menu = dimPart("getDimMenuInteraction");
        try {
            Method m = menu.getClass().getMethod("switchNaviMode", int.class);
            Object value = m.invoke(menu, mode);
            return Result.text(Boolean.TRUE.equals(value), "switchNaviMode(" + mode + ") -> " + value);
        } catch (Exception e) {
            return Result.error("switchNaviMode", e);
        }
    }

    Result setDimVolume(boolean mute, int volume) {
        Object menu = dimPart("getDimMenuInteraction");
        try {
            Method m = menu.getClass().getMethod("setVolume", boolean.class, int.class);
            m.invoke(menu, mute, volume);
            return Result.text(true, "setVolume(" + mute + ", " + volume + ") -> ok");
        } catch (NoSuchMethodException e) {
            try {
                Method m = menu.getClass().getMethod("setVolume", int.class);
                m.invoke(menu, volume);
                return Result.text(true, "setVolume(" + volume + ") -> ok");
            } catch (Exception nested) {
                return Result.error("setVolume", nested);
            }
        } catch (Exception e) {
            return Result.error("setVolume", e);
        }
    }

    Result climateTemp(float tempC) {
        Object climate = dimPart("getClimateInteraction");
        try {
            Method m = climate.getClass().getMethod("updateFunctionValue", int.class, float.class);
            Object value = m.invoke(climate, HVAC_FUNC_TEMP, tempC);
            return Result.text(Boolean.TRUE.equals(value),
                    String.format(Locale.US, "IClimateInteraction.updateFunctionValue(0x10060100, %.1fC) -> %s", tempC, value));
        } catch (Exception e) {
            return Result.error("IClimateInteraction.updateFunctionValue(float)", e);
        }
    }

    Result climateCelsiusUnit() {
        Object climate = dimPart("getClimateInteraction");
        try {
            Method m = climate.getClass().getMethod("updateFunctionValue", int.class, int.class);
            Object value = m.invoke(climate, HVAC_FUNC_TEMP_UNIT, TEMP_UNIT_C);
            return Result.text(Boolean.TRUE.equals(value), "IClimateInteraction.updateFunctionValue(TEMP_UNIT, C) -> " + value);
        } catch (Exception e) {
            return Result.error("IClimateInteraction.updateFunctionValue(int)", e);
        }
    }

    private String probeHud() {
        try {
            hud();
            return "available";
        } catch (Exception e) {
            return compact(e);
        }
    }

    private String probeDim() {
        try {
            dim();
            return "available";
        } catch (Exception e) {
            return compact(e);
        }
    }

    private Object hud() throws Exception {
        if (hud != null) return hud;
        Class<?> c = Class.forName("com.ecarx.xui.adaptapi.hudinteraction.HUDInteraction");
        hud = c.getMethod("create", Context.class).invoke(null, context);
        if (hud == null) throw new IllegalStateException("HUDInteraction.create returned null");
        return hud;
    }

    private Object dim() throws Exception {
        if (dim != null) return dim;
        Class<?> c = Class.forName("com.ecarx.xui.adaptapi.diminteraction.DimInteraction");
        dim = c.getMethod("create", Context.class).invoke(null, context);
        if (dim == null) throw new IllegalStateException("DimInteraction.create returned null");
        callOptional(dim, "connect");
        return dim;
    }

    private Object dimPart(String getter) {
        try {
            Object value = dim().getClass().getMethod(getter).invoke(dim());
            if (value == null) throw new IllegalStateException(getter + " returned null");
            return value;
        } catch (Exception e) {
            throw new IllegalStateException(getter + ": " + compact(e), e);
        }
    }

    private Result intCall(Object target, String name) {
        try {
            Object value = target.getClass().getMethod(name).invoke(target);
            return Result.text(true, name + "() -> " + value);
        } catch (Exception e) {
            return Result.error(name, e);
        }
    }

    private Result objectCall(Object target, String name) {
        try {
            Object value = target.getClass().getMethod(name).invoke(target);
            return Result.text(true, name + "() -> " + String.valueOf(value));
        } catch (Exception e) {
            return Result.error(name, e);
        }
    }

    private Result voidCall(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
            return Result.text(true, name + "() -> ok");
        } catch (Exception e) {
            return Result.error(name, e);
        }
    }

    private void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }

    private void append(StringBuilder sb, String label, Result result) {
        sb.append(label).append(": ").append(result.message).append("\n");
    }

    private static String compact(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    static final class Result {
        final boolean success;
        final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static Result text(boolean success, String message) {
            return new Result(success, message);
        }

        static Result error(String action, Exception e) {
            return new Result(false, action + " -> " + compact(e));
        }
    }
}
