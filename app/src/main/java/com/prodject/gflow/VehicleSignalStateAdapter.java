package com.prodject.gflow;

import android.content.Context;
import android.content.SharedPreferences;
import com.ecarx.xui.adaptapi.FunctionStatus;
import com.ecarx.xui.adaptapi.car.sensor.ISensor;
import java.util.ArrayList;
import java.util.List;

final class VehicleSignalStateAdapter {
    static final int SENSOR_CAR_SPEED = 0x100100;
    static final int SENSOR_FUEL_LEVEL = 0x100600;
    static final int SENSOR_TEMPERATURE_AMBIENT = 0x100b00;
    static final int SENSOR_TEMPERATURE_INDOOR = 0x100c00;
    static final int SENSOR_RAIN = 0x100e00;
    static final int SENSOR_IGNITION_STATE = 0x200100;
    static final int SENSOR_GEAR = 0x200200;
    static final int SENSOR_ENGINE_STATE = 0x201600;
    static final int SENSOR_ENGINE_START_STOP_STATE = 0x201700;
    static final int SENSOR_RAIN_SENSOR_STATE = 0x300f00;

    static final String KEY_LAST_STATUS = "vehicle_signal_status";
    private static final String KEY_ENGINE_STARTED_AT = "vehicle_engine_started_at";

    private final Context context;
    private final ArrayList<String> status = new ArrayList<>();
    private ISensor sensor;

    VehicleSignalStateAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    SmartClimateController.State smartClimateState(SharedPreferences prefs) {
        float cabin = readFloat("cabinTemp", SENSOR_TEMPERATURE_INDOOR, prefs.getFloat(SmartClimateController.KEY_CABIN_TEMP, 26.0f));
        float outside = readFloat("outsideTemp", SENSOR_TEMPERATURE_AMBIENT, prefs.getFloat(SmartClimateController.KEY_OUTSIDE_TEMP, 26.0f));
        boolean fogging = prefs.getBoolean(SmartClimateController.KEY_FOGGING, false);
        boolean callActive = prefs.getBoolean(SmartClimateController.KEY_CALL_ACTIVE, false);

        int ignition = readEvent("ignition", SENSOR_IGNITION_STATE, Integer.MIN_VALUE);
        int engine = readEvent("engine", SENSOR_ENGINE_STATE, Integer.MIN_VALUE);
        int engineStartStop = readEvent("engineStartStop", SENSOR_ENGINE_START_STOP_STATE, Integer.MIN_VALUE);
        int engineMinutes = engineMinutes(prefs, ignition, engine, engineStartStop);
        readFloat("speed", SENSOR_CAR_SPEED, Float.NaN);
        readEvent("gear", SENSOR_GEAR, Integer.MIN_VALUE);
        float rain = readFloat("rain", SENSOR_RAIN, Float.NaN);
        int rainSensor = readEvent("rainSensor", SENSOR_RAIN_SENSOR_STATE, Integer.MIN_VALUE);
        boolean humidityRisk = fogging || isRainDetected(rain, rainSensor) || readVehicleFlag("autoDehumidification", EcarxVehicleAdapter.HVAC_AUTO_DEHUMIDIFICATION, false);
        boolean poorAirQuality = readVehicleFlag("aqsStatus", EcarxVehicleAdapter.HVAC_AQS_STATUS, false)
                || readVehicleFlag("aqsSwitch", EcarxVehicleAdapter.HVAC_AQS_SWITCH, false)
                || readVehicleFlag("co2Switch", EcarxVehicleAdapter.HVAC_CO2_SWITCH, false)
                || readVehicleFlag("ionSwitch", EcarxVehicleAdapter.HVAC_IONS_SWITCH, false);

        String engineNote = "ignition=" + valueOrNa(ignition) + ", engine=" + valueOrNa(engine)
                + ", startStop=" + valueOrNa(engineStartStop);
        status.add(engineNote);

        return new SmartClimateController.State(
                cabin,
                outside,
                prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f),
                prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f),
                engineMinutes,
                fogging,
                callActive,
                humidityRisk,
                poorAirQuality);
    }

    String status() {
        return join(status);
    }

    float speedKmh(float fallback) {
        return readFloat("speed", SENSOR_CAR_SPEED, fallback);
    }

    static String lastStatus(Context context) {
        return SmartClimateController.prefs(context).getString(KEY_LAST_STATUS, "");
    }

    private float readFloat(String label, int sensorType, float fallback) {
        try {
            ISensor manager = sensor();
            if (!isSupported(manager, sensorType)) {
                status.add(label + " " + hex(sensorType) + " unsupported, fallback=" + fallback);
                return fallback;
            }
            float result = manager.getSensorLatestValue(sensorType);
            status.add(label + " " + hex(sensorType) + "=" + result);
            return result;
        } catch (Exception e) {
            status.add(label + " " + hex(sensorType) + " fallback=" + fallback + " (" + root(e) + ")");
            return fallback;
        }
    }

    private int readEvent(String label, int sensorType, int fallback) {
        try {
            ISensor manager = sensor();
            if (!isSupported(manager, sensorType)) {
                status.add(label + " " + hex(sensorType) + " unsupported");
                return fallback;
            }
            int result = manager.getSensorEvent(sensorType);
            status.add(label + " " + hex(sensorType) + "=" + hex(result));
            return result;
        } catch (Exception e) {
            status.add(label + " " + hex(sensorType) + " unavailable (" + root(e) + ")");
            return fallback;
        }
    }

    private boolean isSupported(ISensor manager, int sensorType) {
        try {
            FunctionStatus value = manager.isSensorSupported(sensorType);
            return value == null || "active".equalsIgnoreCase(String.valueOf(value));
        } catch (Exception ignored) {
            return true;
        }
    }

    private int engineMinutes(SharedPreferences prefs, int ignition, int engine, int startStop) {
        boolean known = ignition != Integer.MIN_VALUE || engine != Integer.MIN_VALUE || startStop != Integer.MIN_VALUE;
        if (!known) {
            int fallback = prefs.getInt(SmartClimateController.KEY_ENGINE_MINUTES, 0);
            status.add("engineMinutes fallback=" + fallback);
            return fallback;
        }
        boolean running = isActive(ignition) || isActive(engine) || isActive(startStop);
        long now = System.currentTimeMillis();
        if (running) {
            long startedAt = prefs.getLong(KEY_ENGINE_STARTED_AT, 0L);
            if (startedAt <= 0L) {
                prefs.edit().putLong(KEY_ENGINE_STARTED_AT, now).apply();
                status.add("engineMinutes=0 inferred from active engine signal");
                return 0;
            }
            int minutes = (int) Math.max(0L, (now - startedAt) / 60_000L);
            status.add("engineMinutes=" + minutes + " inferred from active engine signal");
            return minutes;
        }
        prefs.edit().putLong(KEY_ENGINE_STARTED_AT, 0L).apply();
        status.add("engineMinutes=0 inferred from inactive engine signal");
        return 0;
    }

    private boolean readVehicleFlag(String label, int functionId, boolean fallback) {
        try {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
            EcarxVehicleAdapter.Result support = adapter.support(functionId);
            if (support != null && !support.isSupported()) {
                status.add(label + " " + hex(functionId) + " unsupported, fallback=" + fallback);
                return fallback;
            }
            EcarxVehicleAdapter.Result result = adapter.get(functionId);
            if (result == null || !result.success) {
                status.add(label + " " + hex(functionId) + " fallback=" + fallback);
                return fallback;
            }
            boolean active = isActiveVehicleValue(result.value);
            status.add(label + " " + hex(functionId) + "=" + hex(result.value));
            return active;
        } catch (Exception e) {
            status.add(label + " " + hex(functionId) + " fallback=" + fallback + " (" + root(e) + ")");
            return fallback;
        }
    }

    private static boolean isRainDetected(float rain, int rainSensor) {
        return (!Float.isNaN(rain) && rain > 0.01f) || isActive(rainSensor);
    }

    private static boolean isActiveVehicleValue(int value) {
        return value != Integer.MIN_VALUE && value != 0 && value != 0xff;
    }

    private static boolean isActive(int value) {
        return value != Integer.MIN_VALUE && value != 0;
    }

    private ISensor sensor() throws Exception {
        if (sensor != null) return sensor;
        sensor = CarBridge.getSensorManager(context);
        return sensor;
    }

    private static String root(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        String message = t.getMessage();
        return t.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    private static String hex(int value) {
        return value == Integer.MIN_VALUE ? "n/a" : "0x" + Integer.toHexString(value);
    }

    private static String valueOrNa(int value) {
        return value == Integer.MIN_VALUE ? "n/a" : hex(value);
    }

    private static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        return sb.toString();
    }
}
