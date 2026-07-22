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

    static final String MODE_OFF = "off";
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
        if (now - last < 60_000L) return "Cooldown: настройки менялись меньше минуты назад";

        VehicleSignalStateAdapter signals = new VehicleSignalStateAdapter(context);
        State s = signals.smartClimateState(p);
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        ArrayList<String> out = new ArrayList<>();
        out.add("SmartClimate mode=" + mode + " cabin=" + s.cabin + " outside=" + s.outside
                + " driverTarget=" + s.driverTarget + " passengerTarget=" + s.passengerTarget);
        out.add("Signals:\n" + signals.status());
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON).message);

        if (s.fogging) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON).message);
        }
        if (s.callActive) out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_2).message);

        if (MODE_FAST_COOL.equals(mode) || MODE_SUMMER.equals(mode)) runSummer(adapter, s, out);
        else if (MODE_FAST_HEAT.equals(mode)) runWinter(adapter, s, out);
        else if (MODE_STABILIZE.equals(mode)) runStabilize(adapter, s, out);
        else if (MODE_MAINTAIN.equals(mode)) runMaintain(adapter, s, out);
        else if (MODE_DRY.equals(mode)) runDry(adapter, s, out);

        prefs(context).edit()
                .putLong(KEY_LAST_APPLY_AT, now)
                .putString(KEY_LAST_STAGE, stage(mode, s))
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

    private static void runSummer(EcarxVehicleAdapter adapter, State s, ArrayList<String> out) {
        if (s.cabin >= 27.0f) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 18.0f).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 18.0f).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_8).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2).message);
        } else if (s.cabin > s.driverTarget + 1.0f) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_4).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, s.driverTarget).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, s.passengerTarget).message);
        } else {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_OFF).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_AUTO).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO).message);
        }
    }

    private static void runWinter(EcarxVehicleAdapter adapter, State s, ArrayList<String> out) {
        if (s.cabin < s.driverTarget - 3.0f || s.engineMinutes < 10) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_HIGH).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_3).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, Math.min(26.0f, s.driverTarget + 3.0f)).message);
        } else {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_1).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_2).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, s.driverTarget).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON).message);
        }
    }

    private static void runStabilize(EcarxVehicleAdapter adapter, State s, ArrayList<String> out) {
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, s.driverTarget).message);
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, s.passengerTarget).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, Math.abs(s.cabin - s.driverTarget) > 2.0f ? EcarxVehicleAdapter.FAN_SPEED_4 : EcarxVehicleAdapter.FAN_SPEED_2).message);
    }

    private static void runMaintain(EcarxVehicleAdapter adapter, State s, ArrayList<String> out) {
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, s.driverTarget).message);
        out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, s.passengerTarget).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO).message);
    }

    private static void runDry(EcarxVehicleAdapter adapter, State s, ArrayList<String> out) {
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE).message);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3).message);
    }

    private static String stage(String mode, State s) {
        if (MODE_SUMMER.equals(mode) || MODE_FAST_COOL.equals(mode)) {
            if (s.cabin >= 27f) return "cooling_max";
            if (s.cabin > s.driverTarget + 1f) return "cooling_reduce_fan";
            return "cooling_auto";
        }
        if (MODE_FAST_HEAT.equals(mode)) return s.cabin < s.driverTarget - 3f ? "heating_fast" : "heating_auto";
        return mode;
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

        State(float cabin, float outside, float driverTarget, float passengerTarget, int engineMinutes, boolean fogging, boolean callActive) {
            this.cabin = cabin;
            this.outside = outside;
            this.driverTarget = driverTarget;
            this.passengerTarget = passengerTarget;
            this.engineMinutes = engineMinutes;
            this.fogging = fogging;
            this.callActive = callActive;
        }

        static State from(SharedPreferences p) {
            return new State(
                    p.getFloat(KEY_CABIN_TEMP, 26.0f),
                    p.getFloat(KEY_OUTSIDE_TEMP, 26.0f),
                    p.getFloat(KEY_DRIVER_TARGET, 22.0f),
                    p.getFloat(KEY_PASSENGER_TARGET, 22.0f),
                    p.getInt(KEY_ENGINE_MINUTES, 0),
                    p.getBoolean(KEY_FOGGING, false),
                    p.getBoolean(KEY_CALL_ACTIVE, false));
        }
    }
}
