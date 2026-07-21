package com.prodject.gcontrol;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Locale;

final class CarSignalManagerAdapter {
    static final int APA_BUTTON_NO_PRESS = 0x0;
    static final int APA_BUTTON_ON = 0x1;
    static final int APA_UNDO_NO_PRESS = 0x2;
    static final int APA_UNDO = 0x3;
    static final int APA_CANCEL_NO_PRESS = 0x4;
    static final int APA_CANCEL = 0x5;
    static final int APA_CONFIRM_NO_PRESS = 0x6;
    static final int APA_MANUAL = 0x7;
    static final int APA_CONFIRM_ENTER = 0x8;
    static final int APA_PAS = 0x9;
    static final int APA_RPA = 0x0a;
    static final int APA_RPA_ALT = 0x0b;

    static final int SIG_DRVR_ASSC_SYS_BTN_PUSH = 0x21407011;
    static final int SIG_DRVR_ASSC_SYS_PARK_MOD = 0x21407012;
    static final int SIG_DRVR_ASSC_SYS_DISP = 0x2140713f;
    static final int SIG_DRVR_ASSC_SYS_STS = 0x21407140;
    static final int SIG_ICC_VEH_STS = 0x21407266;
    static final int SIG_REM_PRKG_ENA_REQ = 0x21407029;
    static final int SIG_REM_PRKG_ENA_STS = 0x21407177;
    static final int SIG_REM_PRKG_SELF_SEARCH_REQ = 0x2140702a;

    static final int PARK_MODE_DEFAULT = 0x0;
    static final int PARK_MODE_CANCEL = 0x1;
    static final int PARK_MODE_HORIZONTAL_IN = 0x2;
    static final int PARK_MODE_PERPENDICULAR_IN = 0x3;
    static final int PARK_MODE_PERPENDICULAR_IN_FORWARD = 0x4;
    static final int PARK_MODE_PERPENDICULAR_IN_BACKWARD = 0x5;
    static final int PARK_MODE_HORIZONTAL_LEFT_OUT = 0x9;
    static final int PARK_MODE_HORIZONTAL_RIGHT_OUT = 0x0a;
    static final int PARK_MODE_PERPENDICULAR_LEFT_OUT_FORWARD = 0x0b;
    static final int PARK_MODE_PERPENDICULAR_RIGHT_OUT_FORWARD = 0x0c;
    static final int PARK_MODE_PERPENDICULAR_LEFT_OUT_BACKWARD = 0x0d;
    static final int PARK_MODE_PERPENDICULAR_RIGHT_OUT_BACKWARD = 0x0e;

    static final int VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS = 0x2140a763;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS = 0x2140a764;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR = 0x2140a765;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX = 0x2140a766;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY = 0x2140a767;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ_AUTHENT_STS = 0x2140a768;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ_CHKS = 0x2140a769;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ_CNTR = 0x2140a76a;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ_RNDX = 0x2140a76b;
    static final int VEH_MOBDEV_RPA_AUTHENT_REQ_RNDY = 0x2140a76c;
    static final int VEH_MOBDEV_RPA_REQ_RESP = 0x2140a76d;
    static final int VEH_MOBDEV_RPA_STS_ON_OFF1 = 0x2140a76e;
    static final int VEH_MOBDEV_RPA_STS_UINT8 = 0x2140a76f;
    static final int VEH_PUSH_APA_INFO_REQ = 0x2140a8f5;

    private final Context context;

    CarSignalManagerAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Result get(String methodName, int signalId) {
        try {
            Object manager = manager();
            Method method = manager.getClass().getMethod(methodName);
            Object value = method.invoke(manager);
            return Result.ok(methodName, signalId, ((Number) value).intValue());
        } catch (Exception e) {
            return Result.error(methodName, signalId, e);
        }
    }

    Result set(String methodName, int signalId, int value) {
        try {
            Object manager = manager();
            Method method = manager.getClass().getMethod(methodName, int.class);
            Object result = method.invoke(manager, value);
            return Result.set(methodName, signalId, value, String.valueOf(result));
        } catch (Exception e) {
            return Result.error(methodName, signalId, e);
        }
    }

    Result rawHalProperty(int propertyId, String name) {
        return Result.status(name, propertyId,
                "HAL property found in VehiclePropertyVEH2; direct read/write is not wired yet");
    }

    private Object manager() throws Exception {
        Class<?> cls = Class.forName("ecarx.car.hardware.signal.CarSignalManager");
        try {
            return cls.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            return cls.getConstructor(Context.class).newInstance(context);
        }
    }

    static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    static final class Result {
        final boolean success;
        final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static Result ok(String method, int signalId, int value) {
            return new Result(true, String.format(Locale.US, "%s %s = %s", method, hex(signalId), hex(value)));
        }

        static Result set(String method, int signalId, int value, String apiResult) {
            return new Result(true, String.format(Locale.US, "%s %s=%s -> %s", method, hex(signalId), hex(value), apiResult));
        }

        static Result status(String method, int signalId, String status) {
            return new Result(true, String.format(Locale.US, "%s %s: %s", method, hex(signalId), status));
        }

        static Result error(String method, int signalId, Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            return new Result(false, method + " " + hex(signalId) + ": " + root.getClass().getSimpleName() + ": " + root.getMessage());
        }
    }
}
