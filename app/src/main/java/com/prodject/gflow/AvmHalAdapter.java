package com.prodject.gflow;

import android.content.Context;
import java.lang.reflect.Method;

final class AvmHalAdapter {
    static final int PROP_AVM_COMMAND = 0x2141f000;
    static final int PROP_AVM_RAW_OPEN = 0x21417523;
    static final int PROP_AVM_WAKE = 0x214085e6;
    static final int PROP_AVM_STATUS = 0x2141f004;
    static final int PROP_AVM_VIEW = 0x21415116;
    static final int PROP_AVM_STATE = 0x2160728c;

    private static final int AREA_GLOBAL = 0;

    private final Context context;
    private Object car;
    private Object propertyManager;

    AvmHalAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Result open360() {
        StringBuilder sb = new StringBuilder();
        boolean ok = false;
        ok |= append(sb, writeIntArray(PROP_AVM_COMMAND, new int[]{30, 45, 2, 3}));
        ok |= append(sb, writeIntArray(PROP_AVM_COMMAND, new int[]{30, 46, 2, 3}));
        ok |= append(sb, writeIntArray(PROP_AVM_RAW_OPEN, new int[]{0x0c, 0x1b, 0x02, 0x03, 0x00}));
        ok |= append(sb, writeInt(PROP_AVM_WAKE, 1));
        return new Result(ok, sb.toString().trim());
    }

    String diagnostics() {
        StringBuilder sb = new StringBuilder();
        append(sb, read(PROP_AVM_STATUS));
        append(sb, read(PROP_AVM_VIEW));
        append(sb, read(PROP_AVM_STATE));
        return sb.toString().trim();
    }

    private Result writeInt(int propertyId, int value) {
        try {
            Object manager = propertyManager();
            try {
                Method m = manager.getClass().getMethod("setIntProperty", int.class, int.class, int.class);
                m.invoke(manager, propertyId, AREA_GLOBAL, value);
                return Result.ok("setIntProperty 0x" + Integer.toHexString(propertyId) + "=" + value);
            } catch (NoSuchMethodException ignored) {
                Method m = manager.getClass().getMethod("setProperty", Class.class, int.class, int.class, Object.class);
                m.invoke(manager, Integer.class, propertyId, AREA_GLOBAL, Integer.valueOf(value));
                return Result.ok("setProperty<Integer> 0x" + Integer.toHexString(propertyId) + "=" + value);
            }
        } catch (Exception e) {
            return Result.error("write 0x" + Integer.toHexString(propertyId), e);
        }
    }

    private Result writeIntArray(int propertyId, int[] value) {
        try {
            Object manager = propertyManager();
            Method m = manager.getClass().getMethod("setProperty", Class.class, int.class, int.class, Object.class);
            m.invoke(manager, int[].class, propertyId, AREA_GLOBAL, value);
            return Result.ok("setProperty<int[]> 0x" + Integer.toHexString(propertyId));
        } catch (Exception e) {
            return Result.error("write[] 0x" + Integer.toHexString(propertyId), e);
        }
    }

    private Result read(int propertyId) {
        try {
            Object manager = propertyManager();
            try {
                Method m = manager.getClass().getMethod("getIntProperty", int.class, int.class);
                Object value = m.invoke(manager, propertyId, AREA_GLOBAL);
                return Result.ok("getIntProperty 0x" + Integer.toHexString(propertyId) + " -> " + value);
            } catch (NoSuchMethodException ignored) {
                Method m = manager.getClass().getMethod("getProperty", int.class, int.class);
                Object value = m.invoke(manager, propertyId, AREA_GLOBAL);
                return Result.ok("getProperty 0x" + Integer.toHexString(propertyId) + " -> " + value);
            }
        } catch (Exception e) {
            return Result.error("read 0x" + Integer.toHexString(propertyId), e);
        }
    }

    private Object propertyManager() throws Exception {
        if (propertyManager == null) {
            Object c = car();
            propertyManager = c.getClass().getMethod("getCarManager", String.class).invoke(c, "property");
            if (propertyManager == null) throw new IllegalStateException("property manager is null");
        }
        return propertyManager;
    }

    private Object car() throws Exception {
        if (car == null) {
            Class<?> cls = Class.forName("android.car.Car");
            car = cls.getMethod("createCar", Context.class).invoke(null, context);
            if (car == null) throw new IllegalStateException("Car.createCar returned null");
        }
        return car;
    }

    private static boolean append(StringBuilder sb, Result result) {
        if (sb.length() > 0) sb.append('\n');
        sb.append(result.message);
        return result.success;
    }

    static final class Result {
        final boolean success;
        final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static Result ok(String message) {
            return new Result(true, message);
        }

        static Result error(String call, Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            return new Result(false, call + " -> " + root.getClass().getSimpleName() + ": " + root.getMessage());
        }
    }
}
