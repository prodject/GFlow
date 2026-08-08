package com.prodject.gflow;

import android.content.*;
import java.util.*;

final class SmartClimateController {
    static final String PREFS = "smart_climate_controller";
    static final String KEY_ENABLED = "enabled";
    static final String KEY_MODE = "mode";
    static final String KEY_CABIN_TEMP = "cabin_temp";
    static final String KEY_OUTSIDE_TEMP = "outside_temp";
    static final String KEY_DRIVER_TARGET = "driver_target";
    static final String KEY_PASSENGER_TARGET = "passenger_target";
    static final String KEY_ENGINE_MINUTES = "engine_minutes";
    static final String KEY_FOGGING = "fogging";
    static final String KEY_CALL_ACTIVE = "call_active";
    static final String KEY_DRY_AFTER_TRIP = "dry_after_trip";
    static final String KEY_LAST_APPLY_AT = "last_apply_at";
    static final String KEY_LAST_STAGE = "last_stage";
    static final String KEY_LOG = "log";
    static final String KEY_OVERRIDE_FAN_UNTIL = "override_fan_until";
    static final String KEY_OVERRIDE_AC_UNTIL = "override_ac_until";
    static final String KEY_OVERRIDE_FLOW_UNTIL = "override_flow_until";
    static final String KEY_OVERRIDE_CIRC_UNTIL = "override_circ_until";
    static final String KEY_OVERRIDE_DEFROST_UNTIL = "override_defrost_until";
    static final String KEY_OVERRIDE_SEAT_UNTIL = "override_seat_until";
    static final String KEY_OVERRIDE_WHEEL_UNTIL = "override_wheel_until";
    private static final long MANUAL_OVERRIDE_MS = 10 * 60_000L;

    static final String MODE_OFF = "off";
    static final String MODE_AUTO = "auto";
    static final String MODE_FAST_COOL = "fast_cool";
    static final String MODE_FAST_HEAT = "fast_heat";
    static final String MODE_STABILIZE = "stabilize";
    static final String MODE_MAINTAIN = "maintain";
    static final String MODE_DRY = "dry";
    static final String MODE_SUMMER = "summer";

    private SmartClimateController() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String tick(Context context) {
        SharedPreferences p = prefs(context);
        String mode = p.getString(KEY_MODE, MODE_OFF);
        if (!p.getBoolean(KEY_ENABLED, false) || MODE_OFF.equals(mode)) return "Smart climate off";
        long now = System.currentTimeMillis();
        long last = p.getLong(KEY_LAST_APPLY_AT, 0L);
        if (now - last < 15_000L) return "Cooldown: настройки менялись меньше 15 секунд назад";

        VehicleSignalStateAdapter signals = new VehicleSignalStateAdapter(context);
        State s = signals.smartClimateState(p);
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        ArrayList<String> out = new ArrayList<>();
        ClimatePlan plan = buildPlan(mode, s);
        out.add("SmartClimate mode=" + mode + " stage=" + plan.stage + " cabin=" + s.cabin + " outside=" + s.outside
                + " target=" + plan.targetTemp + " delta=" + plan.deltaTemp + " power=" + plan.climatePower + "%");
        out.add("Signals:\n" + signals.status());
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON).message);
        applyPlan(context, adapter, s, plan, out);

        prefs(context).edit()
                .putLong(KEY_LAST_APPLY_AT, now)
                .putString(KEY_LAST_STAGE, plan.stage)
                .putString(VehicleSignalStateAdapter.KEY_LAST_STATUS, signals.status())
                .putString(KEY_LOG, joinLines(out) + "\n" + p.getString(KEY_LOG, ""))
                .apply();
        return joinLines(out);
    }

    static String dryAfterTrip(Context context) {
        if (!prefs(context).getBoolean(KEY_DRY_AFTER_TRIP, true)) return "Dry after trip disabled";
        prefs(context).edit().putBoolean(KEY_ENABLED, true).putString(KEY_MODE, MODE_DRY).putLong(KEY_LAST_APPLY_AT, 0L).apply();
        return tick(context);
    }

    static String log(Context context) {
        return prefs(context).getString(KEY_LOG, "");
    }

    static String lastStage(Context context) {
        return prefs(context).getString(KEY_LAST_STAGE, "");
    }

    static void noteManualClimateChange(Context context, int functionId) {
        SharedPreferences.Editor editor = prefs(context).edit();
        long until = System.currentTimeMillis() + MANUAL_OVERRIDE_MS;
        if (functionId == EcarxVehicleAdapter.HVAC_FAN_SPEED || functionId == EcarxVehicleAdapter.HVAC_AUTO_FAN_SETTING) {
            editor.putLong(KEY_OVERRIDE_FAN_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_AC || functionId == EcarxVehicleAdapter.HVAC_AC_MAX) {
            editor.putLong(KEY_OVERRIDE_AC_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_CIRCULATION) {
            editor.putLong(KEY_OVERRIDE_CIRC_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_BLOWING_MODE) {
            editor.putLong(KEY_OVERRIDE_FLOW_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_DEFROST_FRONT
                || functionId == EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX
                || functionId == EcarxVehicleAdapter.HVAC_DEFROST_REAR
                || functionId == EcarxVehicleAdapter.HVAC_AUTO_DEFROST_FRONT) {
            editor.putLong(KEY_OVERRIDE_DEFROST_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_SEAT_HEATING
                || functionId == EcarxVehicleAdapter.HVAC_SEAT_VENTILATION
                || functionId == EcarxVehicleAdapter.HVAC_SEAT_MASSAGE) {
            editor.putLong(KEY_OVERRIDE_SEAT_UNTIL, until);
        } else if (functionId == EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT) {
            editor.putLong(KEY_OVERRIDE_WHEEL_UNTIL, until);
        } else {
            return;
        }
        editor.apply();
    }

    private static ClimatePlan buildPlan(String mode, State s) {
        float target = (s.driverTarget + s.passengerTarget) / 2f;
        float delta = s.cabin - target;
        float absDelta = Math.abs(delta);
        int climatePower = climatePower(absDelta);

        if (MODE_DRY.equals(mode)) {
            return new ClimatePlan(mode, "drying", target, delta, climatePower, target, fanForPower(35), true, false, true,
                    EcarxVehicleAdapter.CIRCULATION_OUTSIDE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW,
                    0, EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF, EcarxVehicleAdapter.COMMON_OFF);
        }
        if (MODE_FAST_COOL.equals(mode) || MODE_SUMMER.equals(mode)) {
            return coolingPlan("cooling_boost", s, target, Math.max(delta, 5f), Math.max(climatePower, 90));
        }
        if (MODE_FAST_HEAT.equals(mode)) {
            return heatingPlan("heating_boost", s, target, Math.min(delta, -5f), Math.max(climatePower, 90));
        }
        if (MODE_STABILIZE.equals(mode)) {
            return delta >= 0 ? coolingPlan("stabilize_cool", s, target, delta, Math.max(climatePower, 30))
                    : heatingPlan("stabilize_heat", s, target, delta, Math.max(climatePower, 30));
        }
        if (MODE_MAINTAIN.equals(mode)) {
            return maintainPlan("maintain", s, target, delta);
        }
        if (absDelta < 0.5f) {
            return maintainPlan("hold", s, target, delta);
        }
        if (delta > 0f) {
            return coolingPlan("cooling_auto", s, target, delta, climatePower);
        }
        return heatingPlan("heating_auto", s, target, delta, climatePower);
    }

    private static ClimatePlan coolingPlan(String stage, State s, float target, float delta, int climatePower) {
        boolean max = climatePower >= 95 || (s.outside - s.cabin >= 4f && delta >= 5f);
        float supply = Math.max(17.0f, target - (climatePower >= 85 ? 3.0f : climatePower >= 60 ? 2.0f : 1.0f));
        boolean dry = s.humidityRisk;
        int circulation = s.poorAirQuality
                ? EcarxVehicleAdapter.CIRCULATION_OUTSIDE
                : (s.outside > s.cabin + 1.5f || max ? EcarxVehicleAdapter.CIRCULATION_INNER : EcarxVehicleAdapter.CIRCULATION_OUTSIDE);
        return new ClimatePlan(MODE_AUTO, stage, target, delta, climatePower, supply, fanForPower(climatePower), true, max, dry,
                circulation, EcarxVehicleAdapter.BLOWING_MODE_FACE, EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_LEVEL_2, EcarxVehicleAdapter.COMMON_OFF);
    }

    private static ClimatePlan heatingPlan(String stage, State s, float target, float delta, int climatePower) {
        float supply = Math.min(26.0f, target + (climatePower >= 85 ? 3.0f : climatePower >= 60 ? 2.0f : 1.0f));
        boolean defrost = s.fogging || s.engineMinutes < 5 || delta <= -3.0f;
        int blowing = defrost ? EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW : EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG;
        int wheel = climatePower >= 60 ? EcarxVehicleAdapter.WHEEL_HEAT_HIGH : climatePower >= 30 ? EcarxVehicleAdapter.WHEEL_HEAT_LOW : EcarxVehicleAdapter.COMMON_OFF;
        int seat = climatePower >= 80 ? EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_3 : climatePower >= 50 ? EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_2 : climatePower >= 20 ? EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_1 : EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF;
        return new ClimatePlan(MODE_AUTO, stage, target, delta, climatePower, supply, fanForPower(climatePower), s.humidityRisk, false, defrost,
                s.poorAirQuality ? EcarxVehicleAdapter.CIRCULATION_OUTSIDE : EcarxVehicleAdapter.CIRCULATION_AUTO, blowing, EcarxVehicleAdapter.HVAC_SEAT_HEATING, seat, wheel);
    }

    private static ClimatePlan maintainPlan(String stage, State s, float target, float delta) {
        boolean dry = s.humidityRisk;
        return new ClimatePlan(MODE_AUTO, stage, target, delta, 10, target, s.callActive ? EcarxVehicleAdapter.FAN_SPEED_2 : EcarxVehicleAdapter.FAN_SPEED_AUTO,
                dry || s.poorAirQuality, false, dry, dry ? EcarxVehicleAdapter.CIRCULATION_OUTSIDE : (s.poorAirQuality ? EcarxVehicleAdapter.CIRCULATION_OUTSIDE : EcarxVehicleAdapter.CIRCULATION_AUTO),
                dry ? EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW : EcarxVehicleAdapter.BLOWING_MODE_AUTO,
                0, EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF, EcarxVehicleAdapter.COMMON_OFF);
    }

    private static void applyPlan(Context context, EcarxVehicleAdapter adapter, State s, ClimatePlan plan, ArrayList<String> out) {
        SharedPreferences p = prefs(context);
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, plan.supplyTemp).message);
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, plan.supplyTemp).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON).message);
        if (!overrideActive(p, KEY_OVERRIDE_AC_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC, plan.acEnabled ? EcarxVehicleAdapter.COMMON_ON : EcarxVehicleAdapter.COMMON_OFF).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC_MAX, plan.acMax ? EcarxVehicleAdapter.COMMON_ON : EcarxVehicleAdapter.COMMON_OFF).message);
        }
        if (!overrideActive(p, KEY_OVERRIDE_CIRC_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_CIRCULATION, plan.circulation).message);
        }
        if (!overrideActive(p, KEY_OVERRIDE_FLOW_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_BLOWING_MODE, plan.blowingMode).message);
        }
        if (!overrideActive(p, KEY_OVERRIDE_FAN_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, s.callActive ? EcarxVehicleAdapter.FAN_SPEED_2 : plan.fanSpeed).message);
        }
        if (!overrideActive(p, KEY_OVERRIDE_DEFROST_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, plan.defrost ? EcarxVehicleAdapter.COMMON_ON : EcarxVehicleAdapter.COMMON_OFF).message);
        }
        if (!overrideActive(p, KEY_OVERRIDE_SEAT_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF).message);
            if (plan.seatFunctionId != 0 && plan.seatLevel != EcarxVehicleAdapter.HVAC_SEAT_LEVEL_OFF) {
                out.add(adapter.set(plan.seatFunctionId, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, plan.seatLevel).message);
            }
        }
        if (!overrideActive(p, KEY_OVERRIDE_WHEEL_UNTIL)) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT,
                    plan.wheelLevel != EcarxVehicleAdapter.COMMON_OFF ? plan.wheelLevel : EcarxVehicleAdapter.COMMON_OFF).message);
        }
    }

    private static boolean overrideActive(SharedPreferences prefs, String key) {
        return prefs.getLong(key, 0L) > System.currentTimeMillis();
    }

    private static int climatePower(float absDelta) {
        if (absDelta >= 5f) return 100;
        if (absDelta >= 3f) return 85;
        if (absDelta >= 2f) return 65;
        if (absDelta >= 1f) return 40;
        if (absDelta >= 0.5f) return 20;
        return 10;
    }

    private static int fanForPower(int climatePower) {
        if (climatePower >= 95) return EcarxVehicleAdapter.FAN_SPEED_8;
        if (climatePower >= 80) return EcarxVehicleAdapter.FAN_SPEED_6;
        if (climatePower >= 60) return EcarxVehicleAdapter.FAN_SPEED_4;
        if (climatePower >= 35) return EcarxVehicleAdapter.FAN_SPEED_3;
        if (climatePower >= 20) return EcarxVehicleAdapter.FAN_SPEED_2;
        return EcarxVehicleAdapter.FAN_SPEED_1;
    }

    private static String joinLines(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        return sb.toString();
    }

    static final class State {
        final float cabin;
        final float outside;
        final float driverTarget;
        final float passengerTarget;
        final int engineMinutes;
        final boolean fogging;
        final boolean callActive;
        final boolean humidityRisk;
        final boolean poorAirQuality;

        State(float cabin, float outside, float driverTarget, float passengerTarget, int engineMinutes, boolean fogging, boolean callActive, boolean humidityRisk, boolean poorAirQuality) {
            this.cabin = cabin;
            this.outside = outside;
            this.driverTarget = driverTarget;
            this.passengerTarget = passengerTarget;
            this.engineMinutes = engineMinutes;
            this.fogging = fogging;
            this.callActive = callActive;
            this.humidityRisk = humidityRisk;
            this.poorAirQuality = poorAirQuality;
        }

        static State from(SharedPreferences p) {
            return new State(
                    p.getFloat(KEY_CABIN_TEMP, 26.0f),
                    p.getFloat(KEY_OUTSIDE_TEMP, 26.0f),
                    p.getFloat(KEY_DRIVER_TARGET, 22.0f),
                    p.getFloat(KEY_PASSENGER_TARGET, 22.0f),
                    p.getInt(KEY_ENGINE_MINUTES, 0),
                    p.getBoolean(KEY_FOGGING, false),
                    p.getBoolean(KEY_CALL_ACTIVE, false),
                    p.getBoolean(KEY_FOGGING, false),
                    false);
        }
    }

    static final class ClimatePlan {
        final String mode;
        final String stage;
        final float targetTemp;
        final float deltaTemp;
        final int climatePower;
        final float supplyTemp;
        final int fanSpeed;
        final boolean acEnabled;
        final boolean acMax;
        final boolean defrost;
        final int circulation;
        final int blowingMode;
        final int seatFunctionId;
        final int seatLevel;
        final int wheelLevel;

        ClimatePlan(String mode, String stage, float targetTemp, float deltaTemp, int climatePower, float supplyTemp, int fanSpeed,
                    boolean acEnabled, boolean acMax, boolean defrost, int circulation, int blowingMode, int seatFunctionId, int seatLevel, int wheelLevel) {
            this.mode = mode;
            this.stage = stage;
            this.targetTemp = targetTemp;
            this.deltaTemp = deltaTemp;
            this.climatePower = climatePower;
            this.supplyTemp = supplyTemp;
            this.fanSpeed = fanSpeed;
            this.acEnabled = acEnabled;
            this.acMax = acMax;
            this.defrost = defrost;
            this.circulation = circulation;
            this.blowingMode = blowingMode;
            this.seatFunctionId = seatFunctionId;
            this.seatLevel = seatLevel;
            this.wheelLevel = wheelLevel;
        }
    }
}
