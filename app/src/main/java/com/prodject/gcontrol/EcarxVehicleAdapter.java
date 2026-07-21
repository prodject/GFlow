package com.prodject.gcontrol;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Locale;

final class EcarxVehicleAdapter {
    static final int COMMON_OFF = 0x0;
    static final int COMMON_ON = 0x1;

    static final int HVAC_POWER = 0x10010100;
    static final int HVAC_AUTO = 0x10010200;
    static final int HVAC_AC = 0x10010300;
    static final int HVAC_AC_MAX = 0x10010400;
    static final int HVAC_FAN_SPEED = 0x10020100;
    static final int HVAC_CIRCULATION = 0x10030100;
    static final int HVAC_DEFROST_FRONT = 0x10040100;
    static final int HVAC_DEFROST_REAR = 0x10040300;
    static final int HVAC_SEAT_VENTILATION = 0x10050100;
    static final int HVAC_SEAT_HEATING = 0x10050200;
    static final int HVAC_SEAT_MASSAGE = 0x10050700;
    static final int HVAC_ECO = 0x10080100;
    static final int HVAC_STEERING_WHEEL_HEAT = 0x10090100;

    static final int FAN_SPEED_1 = 0x10020101;
    static final int FAN_SPEED_3 = 0x10020103;
    static final int FAN_SPEED_5 = 0x10020105;
    static final int CIRCULATION_INNER = 0x10030101;
    static final int CIRCULATION_OUTSIDE = 0x10030102;
    static final int SEAT_LEVEL_1 = 0x10050301;
    static final int SEAT_LEVEL_2 = 0x10050302;
    static final int SEAT_LEVEL_3 = 0x10050303;
    static final int WHEEL_HEAT_LOW = 0x10090201;
    static final int WHEEL_HEAT_MID = 0x10090202;
    static final int WHEEL_HEAT_HIGH = 0x10090203;

    static final int BCM_WINDOW = 0x21030100;
    static final int BCM_WINDOW_LOCK = 0x21030200;
    static final int BCM_SUNROOF_OPEN = 0x21200200;
    static final int BCM_SUNROOF_CLOSE = 0x21200300;
    static final int BCM_SUNCURT_OPEN = 0x21200400;
    static final int BCM_SUNCURT_CLOSE = 0x21200500;
    static final int BCM_MIRROR_FOLD = 0x21060100;
    static final int BCM_READING_LIGHT = 0x21051300;
    static final int BCM_CUSTOM_KEY = 0x21110100;

    static final int WINDOW_CLOSE = 0x0;
    static final int WINDOW_OPEN = 0x1;
    static final int WINDOW_PAUSE = 0x21030101;
    static final int CUSTOM_KEY_DVR = 0x0;
    static final int CUSTOM_KEY_TRUNK = 0x64;
    static final int CUSTOM_KEY_360 = 0x1;

    static final int ADAS_AEB = 0x20070e00;
    static final int ADAS_FCW = 0x200e0100;
    static final int ADAS_LKA = 0x20070100;
    static final int ADAS_LDW = 0x28030100;
    static final int ADAS_RCW = 0x20071000;
    static final int ADAS_ELKA = 0x20070600;
    static final int ADAS_SPEED_LIMIT_WARN = 0x28060100;
    static final int ADAS_PDC = 0x20060300;

    static final int HUD_ACTIVE = 0x20110100;
    static final int HUD_CALIBRATION = 0x20110200;
    static final int HUD_ANGLE_RESET = 0x27010800;
    static final int HUD_SNOW_MODE = 0x27020100;
    static final int HUD_DISPLAY_SAFETY = 0x27030100;
    static final int HUD_DISPLAY_MEDIA = 0x27030200;
    static final int HUD_DISPLAY_NAVI = 0x27030300;
    static final int HUD_DISPLAY_BTPHONE = 0x27030400;
    static final int HUD_DISPLAY_DRIVE_ENVIRONMENT = 0x27030500;

    static final int DRIVE_MODE_SELECT = 0x22010100;
    static final int DRIVE_MODE_ECO = 0x22010101;
    static final int DRIVE_MODE_COMFORT = 0x22010102;
    static final int DRIVE_MODE_DYNAMIC = 0x22010103;
    static final int DRIVE_MODE_SNOW = 0x22010109;
    static final int DRIVE_MODE_OFFROAD = 0x22010113;
    static final int DRIVE_ECO_BUTTON = 0x22020100;
    static final int DRIVE_STEERING_MODE = 0x22040400;
    static final int STEERING_MODE_SOFT = 0x22040401;
    static final int STEERING_MODE_DYNAMIC = 0x22040402;

    static final int SEAT_LENGTH = 0x2d020100;
    static final int SEAT_HEIGHT = 0x2d020200;
    static final int SEAT_BACKREST = 0x2d030200;
    static final int SEAT_POSITION_SAVE = 0x2d400100;
    static final int SEAT_POSITION_SET = 0x2d400200;
    static final int SEAT_RESTORE = 0x2d400300;
    static final int SEAT_ONE_KEY_COMFORT = 0x2d411100;
    static final int SEAT_POSITION_1 = 0x2d400101;
    static final int SEAT_POSITION_2 = 0x2d400102;
    static final int SEAT_FORWARD = 0x2d020101;
    static final int SEAT_BACKWARD = 0x2d020102;
    static final int SEAT_HEIGHT_UP = 0x2d020201;
    static final int SEAT_HEIGHT_DOWN = 0x2d020202;
    static final int SEAT_BACKREST_FORWARD = 0x2d030201;
    static final int SEAT_BACKREST_BACKWARD = 0x2d030202;

    private final Context context;
    private Object car;
    private Object carFunction;
    private String lastError = "";

    EcarxVehicleAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Result set(int functionId, int value) {
        return set(functionId, 0, value);
    }

    Result set(int functionId, int zone, int value) {
        try {
            Object fn = function();
            Method method = fn.getClass().getMethod("setFunctionValue", int.class, int.class, int.class);
            Object ok = method.invoke(fn, functionId, zone, value);
            return Result.ok(functionId, zone, value, Boolean.TRUE.equals(ok), "AdaptAPI setFunctionValue(function, zone, value)");
        } catch (NoSuchMethodException e) {
            try {
                Object fn = function();
                Method method = fn.getClass().getMethod("setFunctionValue", int.class, int.class);
                Object ok = method.invoke(fn, functionId, value);
                return Result.ok(functionId, zone, value, Boolean.TRUE.equals(ok), "AdaptAPI setFunctionValue(function, value)");
            } catch (Exception nested) {
                return Result.error(functionId, zone, value, nested);
            }
        } catch (Exception e) {
            return Result.error(functionId, zone, value, e);
        }
    }

    Result get(int functionId) {
        return get(functionId, 0);
    }

    Result get(int functionId, int zone) {
        try {
            Object fn = function();
            Method method = fn.getClass().getMethod("getFunctionValue", int.class, int.class);
            Object value = method.invoke(fn, functionId, zone);
            return Result.value(functionId, zone, ((Number) value).intValue());
        } catch (NoSuchMethodException e) {
            try {
                Object fn = function();
                Method method = fn.getClass().getMethod("getFunctionValue", int.class);
                Object value = method.invoke(fn, functionId);
                return Result.value(functionId, zone, ((Number) value).intValue());
            } catch (Exception nested) {
                return Result.error(functionId, zone, 0, nested);
            }
        } catch (Exception e) {
            return Result.error(functionId, zone, 0, e);
        }
    }

    Result support(int functionId) {
        return support(functionId, 0);
    }

    Result support(int functionId, int zone) {
        try {
            Object fn = function();
            Method method = fn.getClass().getMethod("isFunctionSupported", int.class, int.class);
            Object status = method.invoke(fn, functionId, zone);
            return Result.status(functionId, zone, "isFunctionSupported(function, zone) -> " + status);
        } catch (NoSuchMethodException e) {
            try {
                Object fn = function();
                Method method = fn.getClass().getMethod("isFunctionSupported", int.class);
                Object status = method.invoke(fn, functionId);
                return Result.status(functionId, zone, "isFunctionSupported(function) -> " + status);
            } catch (Exception nested) {
                return Result.error(functionId, zone, 0, nested);
            }
        } catch (Exception e) {
            return Result.error(functionId, zone, 0, e);
        }
    }

    Result[] setAll(Command... commands) {
        Result[] results = new Result[commands.length];
        for (int i = 0; i < commands.length; i++) {
            Command c = commands[i];
            results[i] = set(c.functionId, c.zone, c.value);
        }
        return results;
    }

    String availability() {
        try {
            function();
            return "ECarX AdaptAPI доступен: com.ecarx.xui.adaptapi.car.Car -> ICarFunction";
        } catch (Exception e) {
            lastError = compact(e);
            return "ECarX AdaptAPI недоступен: " + lastError;
        }
    }

    String lastError() {
        return lastError;
    }

    private Object function() throws Exception {
        if (carFunction != null) return carFunction;
        if (car == null) {
            Class<?> carClass = Class.forName("com.ecarx.xui.adaptapi.car.Car");
            Method create = carClass.getMethod("create", Context.class);
            car = create.invoke(null, context);
            callOptional(car, "connect");
        }
        Method getter = car.getClass().getMethod("getICarFunction");
        carFunction = getter.invoke(car);
        if (carFunction == null) throw new IllegalStateException("getICarFunction returned null");
        return carFunction;
    }

    private void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }

    private static String compact(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    static final class Result {
        final int functionId;
        final int zone;
        final int value;
        final boolean success;
        final String message;

        private Result(int functionId, int zone, int value, boolean success, String message) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
            this.success = success;
            this.message = message;
        }

        static Result ok(int functionId, int zone, int value, boolean apiResult, String path) {
            return new Result(functionId, zone, value, apiResult,
                    path + " -> " + apiResult + " " + hex(functionId) + "/" + zone + "=" + hex(value));
        }

        static Result value(int functionId, int zone, int value) {
            return new Result(functionId, zone, value, true,
                    String.format(Locale.US, "getFunctionValue %s/%d = %s", hex(functionId), zone, hex(value)));
        }

        static Result status(int functionId, int zone, String status) {
            return new Result(functionId, zone, 0, true,
                    String.format(Locale.US, "%s/%d %s", hex(functionId), zone, status));
        }

        static Result error(int functionId, int zone, int value, Exception e) {
            return new Result(functionId, zone, value, false,
                    "Ошибка AdaptAPI " + hex(functionId) + "/" + zone + "=" + hex(value) + ": " + compact(e));
        }
    }

    static final class Command {
        final int functionId;
        final int zone;
        final int value;

        Command(int functionId, int value) {
            this(functionId, 0, value);
        }

        Command(int functionId, int zone, int value) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
        }
    }
}
