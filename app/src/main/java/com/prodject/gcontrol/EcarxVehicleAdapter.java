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
    static final int HVAC_BLOWING_MODE = 0x10070100;
    static final int HVAC_AUTO_BLOWING_MODE = 0x10070700;
    static final int HVAC_DEFROST_FRONT = 0x10040100;
    static final int HVAC_DEFROST_FRONT_MAX = 0x10040200;
    static final int HVAC_DEFROST_REAR = 0x10040300;
    static final int HVAC_CLIMATE_ZONE = 0x10010500;
    static final int HVAC_AUTO_FAN_SETTING = 0x10020200;
    static final int HVAC_CIRCULATION_TIMER = 0x10030200;
    static final int HVAC_CIRCULATION_LONG_TOUCH = 0x10030300;
    static final int HVAC_AUTO_DEFROST_REAR = 0x10040400;
    static final int HVAC_AUTO_DEFROST_FRONT = 0x10040500;
    static final int HVAC_AUTO_DEFROST_REQUEST = 0x10040600;
    static final int HVAC_AUTO_DEFROST_CONFIRM = 0x10040700;
    static final int HVAC_TEMP = 0x10060100;
    static final int HVAC_TEMP_MAX = 0x10060200;
    static final int HVAC_TEMP_MIN = 0x10060300;
    static final int HVAC_TEMP_STEP = 0x10060400;
    static final int HVAC_TEMP_DUAL = 0x10060500;
    static final int HVAC_TEMP_UNIT = 0x10060600;
    static final int HVAC_TEMP_HARD_KEY = 0x10060700;
    static final int HVAC_SWEEPING_MODE = 0x10070200;
    static final int HVAC_DIRECTION_MODE = 0x10070300;
    static final int HVAC_SWEEPING_HORIZONTAL_POS = 0x10070400;
    static final int HVAC_SWEEPING_VERTICAL_POS = 0x10070500;
    static final int HVAC_BLOWING_TEMP_COLOR = 0x10070600;
    static final int HVAC_SEAT_VENTILATION = 0x10050100;
    static final int HVAC_SEAT_HEATING = 0x10050200;
    static final int HVAC_AUTO_SEAT_VENTILATION = 0x10050300;
    static final int HVAC_AUTO_SEAT_VENTILATION_TIME = 0x10050400;
    static final int HVAC_AUTO_SEAT_HEATING = 0x10050500;
    static final int HVAC_AUTO_SEAT_HEATING_TIME = 0x10050600;
    static final int HVAC_SEAT_MASSAGE = 0x10050700;
    static final int HVAC_AUTO_SEAT_MASSAGE = 0x10050800;
    static final int HVAC_AUTO_SEAT_MASSAGE_TIME = 0x10050900;
    static final int HVAC_ECO = 0x10080100;
    static final int HVAC_AQS_SWITCH = 0x10080200;
    static final int HVAC_AUTO_DEHUMIDIFICATION = 0x10080300;
    static final int HVAC_OVERHEAT_PROTECTION = 0x10080400;
    static final int HVAC_IONS_SWITCH = 0x10080500;
    static final int HVAC_STEERING_WHEEL_HEAT = 0x10090100;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT = 0x10090200;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT_TIME = 0x10090300;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT_SWITCH = 0x10090400;
    static final int HVAC_PRE_CLIMATISATION = 0x100a0100;
    static final int HVAC_POST_CLIMATISATION = 0x100a0200;
    static final int HVAC_AI_POWER = 0x100a0400;
    static final int HVAC_AIR_FRAGRANCE = 0x100b0100;
    static final int HVAC_AIR_FRAGRANCE_TYPE = 0x100b0200;
    static final int HVAC_AIR_FRAGRANCE_LEVEL = 0x100b0300;
    static final int HVAC_AIR_FRAGRANCE_RATIO = 0x100b0700;
    static final int HVAC_AUTO_ION_REQUEST = 0x100c0200;
    static final int HVAC_AUTO_ION_CONFIRM = 0x100c0300;
    static final int HVAC_AUTO_DEHUMIDIFICATION_REQUEST = 0x100d0100;
    static final int HVAC_AUTO_DEHUMIDIFICATION_CONFIRM = 0x100d0200;
    static final int HVAC_CO2_SWITCH = 0x100e0100;
    static final int HVAC_AUTO_CLOSE_WINDOW_REMIND = 0x100f0100;
    static final int HVAC_AUTO_SECOND_ROW_CLIMATE = 0x10100100;
    static final int HVAC_CLIMATE_LOCK = 0x10100200;
    static final int HVAC_DISPLAY_WINDOW_TAB = 0x10100300;
    static final int HVAC_G_CLEAN = 0x10100400;
    static final int HVAC_AUTOMATIC_VENTILATION_DRY = 0x10100500;
    static final int HVAC_AUTO_CZIS = 0x10100600;
    static final int HVAC_VENTILATION_ONTIME = 0x10100700;
    static final int HVAC_CLIMATE_HARDKEY_SOUND = 0x10100800;
    static final int HVAC_INTELLIGENT_RECOMMENDATION = 0x10120100;
    static final int HVAC_TEMP_OPTIMIZE = 0x10120200;
    static final int HVAC_MODULE_CONNECT_STATUS = 0x10130100;
    static final int HVAC_ELECTRICAL_AIR_VENT = 0x10140100;
    static final int HVAC_HARDKEY = 0x10140200;
    static final int HVAC_FILTER_ELEMENT_LIFE = 0x10140300;
    static final int HVAC_RESET_FILTER_ELEMENT_LIFE = 0x10141000;
    static final int HVAC_RAPID_COOLING = 0x10140f00;
    static final int HVAC_RAPID_WARMING = 0x10141100;
    static final int HVAC_IONIZER_CLS_WIN_POPUP_SETTING = 0x10141400;
    static final int HVAC_IONIZER_CLS_WIN_POPUP = 0x10141500;
    static final int HVAC_AQS_STATUS = 0x10141600;
    static final int HVAC_FAN_SPEED_BLOWER = 0x10141700;

    static final int FAN_SPEED_1 = 0x10020101;
    static final int FAN_SPEED_2 = 0x10020102;
    static final int FAN_SPEED_3 = 0x10020103;
    static final int FAN_SPEED_4 = 0x10020104;
    static final int FAN_SPEED_5 = 0x10020105;
    static final int FAN_SPEED_6 = 0x10020106;
    static final int FAN_SPEED_7 = 0x10020107;
    static final int FAN_SPEED_8 = 0x10020108;
    static final int FAN_SPEED_9 = 0x10020109;
    static final int FAN_SPEED_AUTO = 0x1002010a;
    static final int CIRCULATION_INNER = 0x10030101;
    static final int CIRCULATION_OUTSIDE = 0x10030102;
    static final int CIRCULATION_AUTO = 0x10030103;
    static final int BLOWING_MODE_FACE = 0x10070101;
    static final int BLOWING_MODE_LEG = 0x10070102;
    static final int BLOWING_MODE_FACE_AND_LEG = 0x10070103;
    static final int BLOWING_MODE_FRONT_WINDOW = 0x10070104;
    static final int BLOWING_MODE_FACE_AND_FRONT_WINDOW = 0x10070105;
    static final int BLOWING_MODE_LEG_AND_FRONT_WINDOW = 0x10070106;
    static final int BLOWING_MODE_ALL = 0x10070107;
    static final int BLOWING_MODE_AUTO = 0x10070108;
    static final int CLIMATE_ZONE_SINGLE = 0x10010501;
    static final int CLIMATE_ZONE_DUAL = 0x10010502;
    static final int CLIMATE_ZONE_TRIPLE = 0x10010503;
    static final int CLIMATE_ZONE_FOUR = 0x10010504;
    static final int TEMP_UNIT_C = 0x10060601;
    static final int TEMP_UNIT_F = 0x10060602;
    static final int DISPLAY_WINDOW_TAB_DEFAULT = 0x10100301;
    static final int DISPLAY_WINDOW_TAB_LEFT_TEMP = 0x10100302;
    static final int DISPLAY_WINDOW_TAB_RIGHT_TEMP = 0x10100303;
    static final int DISPLAY_WINDOW_TAB_HARDWARE_POP = 0x10100304;
    static final int DISPLAY_WINDOW_TAB_SEAT = 0x10100305;
    static final int DISPLAY_WINDOW_TAB_IONS_POP = 0x10100306;
    static final int HVAC_HARDKEY_LEFT_TEMP = 0x10140201;
    static final int HVAC_HARDKEY_RIGHT_TEMP = 0x10140202;
    static final int HVAC_HARDKEY_FAN_UP = 0x10140203;
    static final int HVAC_HARDKEY_FAN_DOWN = 0x10140204;
    static final int HVAC_HARDKEY_MODE = 0x10140205;
    static final int HVAC_HARDKEY_AUTO = 0x10140206;
    static final int HVAC_HARDKEY_AC = 0x10140207;
    static final int HVAC_HARDKEY_LOOP = 0x10140208;
    static final int HVAC_HARDKEY_FRONT_DEFROST = 0x10140209;
    static final int HVAC_HARDKEY_REAR_DEFROST = 0x1014020a;
    static final int HVAC_HARDKEY_TEMP_SYNC = 0x1014020b;
    static final int DIRECTION_MODE_FOCUS = 0x10070301;
    static final int DIRECTION_MODE_AVOID = 0x10070302;
    static final int SWEEPING_MODE_ALL = 0x10070201;
    static final int SWEEPING_MODE_CUSTOM = 0x10070203;
    static final int SEAT_LEVEL_1 = 0x10050301;
    static final int SEAT_LEVEL_2 = 0x10050302;
    static final int SEAT_LEVEL_3 = 0x10050303;
    static final int WHEEL_HEAT_LOW = 0x10090201;
    static final int WHEEL_HEAT_MID = 0x10090202;
    static final int WHEEL_HEAT_HIGH = 0x10090203;

    static final int BCM_WINDOW = 0x21030100;
    static final int BCM_WINDOW_LOCK = 0x21030200;
    static final int BCM_DOOR = 0x21020100;
    static final int BCM_DOOR_LOCK = 0x21020200;
    static final int BCM_DOOR_POS = 0x21020300;
    static final int BCM_CHILD_SAFETY_LOCK = 0x21020400;
    static final int BCM_AUTO_CLOSE_DOOR_BY_SPEED = 0x21110400;
    static final int BCM_ALL_DOORS_ONE_KEY = 0x21110500;
    static final int BCM_SUNROOF_OPEN = 0x21200200;
    static final int BCM_SUNROOF_CLOSE = 0x21200300;
    static final int BCM_SUNCURT_OPEN = 0x21200400;
    static final int BCM_SUNCURT_CLOSE = 0x21200500;
    static final int BCM_MIRROR_FOLD = 0x21060100;
    static final int BCM_READING_LIGHT = 0x21051300;
    static final int BCM_ALL_READING_LIGHTS = 0x21110300;
    static final int BCM_CUSTOM_KEY = 0x21110100;
    static final int BCM_WIPER = 0x21010100;
    static final int BCM_WASHER = 0x21040100;
    static final int BCM_LIGHT_DIPPED_BEAM = 0x21050100;
    static final int BCM_LIGHT_MAIN_BEAM = 0x21050200;
    static final int BCM_LIGHT_DRIVING_LAMPS = 0x21050300;
    static final int BCM_LIGHT_FRONT_FOG = 0x21050400;
    static final int BCM_LIGHT_REAR_FOG = 0x21050500;
    static final int BCM_LIGHT_DAYTIME_RUNNING = 0x21050900;
    static final int BCM_LIGHT_HAZARD = 0x21050f00;
    static final int BCM_LIGHT_WELCOME = 0x21051800;

    static final int WINDOW_CLOSE = 0x0;
    static final int WINDOW_OPEN = 0x1;
    static final int WINDOW_PAUSE = 0x21030101;
    static final int DOOR_CLOSE = 0x0;
    static final int DOOR_OPEN = 0x1;
    static final int DOOR_PAUSE = 0x21020101;
    static final int WIPER_OFF = 0x0;
    static final int WIPER_AUTO = 0x21010101;
    static final int WIPER_LOW = 0x21010102;
    static final int WIPER_HIGH = 0x21010103;
    static final int WIPER_INTERMITTENT = 0x21010104;
    static final int CUSTOM_KEY_DVR = 0x0;
    static final int CUSTOM_KEY_TRUNK = 0x64;
    static final int CUSTOM_KEY_360 = 0x1;

    static final int ADAS_AEB = 0x20070e00;
    static final int ADAS_FCW = 0x200e0100;
    static final int ADAS_LKA = 0x20070100;
    static final int ADAS_LDW = 0x28030100;
    static final int ADAS_RCW = 0x20071000;
    static final int ADAS_ELKA = 0x20070600;
    static final int ADAS_LANE_CHANGE_ASSIST = 0x20070700;
    static final int ADAS_AUTO_LANE_CHANGE_ASSIST = 0x28040100;
    static final int ADAS_BLIND_SPOT_DETECTION = 0x28070100;
    static final int ADAS_TRAFFIC_SIGN_RECOGNITION = 0x200b0100;
    static final int ADAS_TRAFFIC_SIGN_ALERT = 0x200b0200;
    static final int ADAS_SPEED_LIMIT_WARN = 0x28060100;
    static final int ADAS_SPEED_LIMIT_WARNING_MODE = 0x28060200;
    static final int ADAS_PDC = 0x20060300;
    static final int ADAS_PDC_WARNING_VOLUME = 0x28050100;
    static final int ADAS_AI_DRIVER_ASSIST = 0x28080100;
    static final int ADAS_AI_LANE_CHANGE_STRATEGY = 0x28080500;
    static final int ADAS_AI_LANE_CHANGE_WARNING = 0x28080700;

    static final int PDC_VOLUME_LOW = 0x28050101;
    static final int PDC_VOLUME_MID = 0x28050102;
    static final int PDC_VOLUME_HIGH = 0x28050103;
    static final int AI_LANE_CHANGE_STRATEGY_GENTLE = 0x28080501;
    static final int AI_LANE_CHANGE_STRATEGY_STANDARD = 0x28080502;
    static final int AI_LANE_CHANGE_STRATEGY_RADICAL = 0x28080503;
    static final int AI_LANE_CHANGE_WARNING_VOICE = 0x28080701;
    static final int AI_LANE_CHANGE_WARNING_VIBRATE = 0x28080702;
    static final int AI_LANE_CHANGE_WARNING_BOTH = 0x28080703;

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
