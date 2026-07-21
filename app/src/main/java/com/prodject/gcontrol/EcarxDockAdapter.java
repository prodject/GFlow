package com.prodject.gcontrol;

import android.content.Context;
import java.lang.reflect.Method;

final class EcarxDockAdapter {
    static final int TYPE_HOME = 0;
    static final int TYPE_BUSINESS_CLASS = 1;
    static final int TYPE_CAR_SETTING = 2;
    static final int TYPE_HVAC_CIRCULATE = 3;
    static final int TYPE_COCKPIT_TEMPERATURE = 4;
    static final int TYPE_HVAC = 5;
    static final int TYPE_BUSINESS_TEMPERATURE = 6;
    static final int TYPE_DEFROSTING = 7;
    static final int TYPE_APP_STORE = 8;
    static final int TYPE_MEDIA = 9;
    static final int TYPE_VOLUME = 10;

    static final int STATE_NONE = 0;
    static final int STATE_OPEN = 1;
    static final int STATE_CLOSE = 2;

    private final Context context;
    private Object dock;
    private Object deviceDock;

    EcarxDockAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    String availability() {
        StringBuilder sb = new StringBuilder("OneOS Dock\n");
        sb.append("com.ecarx.xui.adaptapi.dock.Dock: ").append(probeDock()).append("\n");
        sb.append("com.ecarx.xui.adaptapi.device.dock.Dock: ").append(probeDeviceDock());
        return sb.toString();
    }

    Result handOver(boolean enabled) {
        try {
            Method m = dock().getClass().getMethod("handOverDock", boolean.class);
            Object value = m.invoke(dock(), enabled);
            return Result.text(Boolean.TRUE.equals(value), "handOverDock(" + enabled + ") -> " + value);
        } catch (NoSuchMethodException e) {
            try {
                Method m = dock().getClass().getMethod("handOverDock");
                Object value = m.invoke(dock());
                return Result.text(Boolean.TRUE.equals(value), "handOverDock() -> " + value);
            } catch (Exception nested) {
                return Result.error("handOverDock", nested);
            }
        } catch (Exception e) {
            return Result.error("handOverDock", e);
        }
    }

    Result customHvacIcon(int icon) {
        try {
            Method m = dock().getClass().getMethod("setDockCustomHvacIcon", int.class);
            Object value = m.invoke(dock(), icon);
            return Result.text(Boolean.TRUE.equals(value), "setDockCustomHvacIcon(" + icon + ") -> " + value);
        } catch (Exception e) {
            return Result.error("setDockCustomHvacIcon", e);
        }
    }

    Result customAppIcon(int index, byte[] bytes) {
        try {
            Method m = dock().getClass().getMethod("setDockCustomAppIcon", int.class, byte[].class);
            Object value = m.invoke(dock(), index, bytes);
            return Result.text(Boolean.TRUE.equals(value), "setDockCustomAppIcon(" + index + ", " + bytes.length + " bytes) -> " + value);
        } catch (Exception e) {
            return Result.error("setDockCustomAppIcon", e);
        }
    }

    Result deviceStatus() {
        try {
            Method m = deviceDock().getClass().getMethod("isDockBarShowing");
            Object value = m.invoke(deviceDock());
            return Result.text(true, "isDockBarShowing() -> " + value);
        } catch (Exception e) {
            return Result.error("isDockBarShowing", e);
        }
    }

    Result switchDeviceDock(boolean show) {
        try {
            Method m = deviceDock().getClass().getMethod("switchDockBar", boolean.class);
            m.invoke(deviceDock(), show);
            return Result.text(true, "switchDockBar(" + show + ") -> ok");
        } catch (Exception e) {
            return Result.error("switchDockBar", e);
        }
    }

    Result notifyItem(int type, int state) {
        try {
            Method m = deviceDock().getClass().getMethod("notifyDockItemChanged", int.class, int.class);
            m.invoke(deviceDock(), type, state);
            return Result.text(true, "notifyDockItemChanged(type=" + type + ", state=" + state + ") -> ok");
        } catch (Exception e) {
            return Result.error("notifyDockItemChanged", e);
        }
    }

    private String probeDock() {
        try {
            dock();
            return "available";
        } catch (Exception e) {
            return compact(e);
        }
    }

    private String probeDeviceDock() {
        try {
            deviceDock();
            return "available";
        } catch (Exception e) {
            return compact(e);
        }
    }

    private Object dock() throws Exception {
        if (dock != null) return dock;
        Class<?> c = Class.forName("com.ecarx.xui.adaptapi.dock.Dock");
        dock = c.getMethod("create", Context.class).invoke(null, context);
        if (dock == null) throw new IllegalStateException("Dock.create returned null");
        return dock;
    }

    private Object deviceDock() throws Exception {
        if (deviceDock != null) return deviceDock;
        Class<?> c = Class.forName("com.ecarx.xui.adaptapi.device.dock.Dock");
        deviceDock = c.getMethod("create", Context.class).invoke(null, context);
        if (deviceDock == null) throw new IllegalStateException("device Dock.create returned null");
        return deviceDock;
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
