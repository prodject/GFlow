package com.prodject.gflow;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class DiagnosticsRunner {
    static final String TAG = "GFlowDiagnostics";
    static final String ACTION_RUN = "com.prodject.gflow.RUN_AUTODIAGNOSTICS";
    static final String EXTRA_INCLUDE_WRITES = "include_writes";
    static final String EXTRA_REASON = "reason";
    static final String LATEST_REPORT = "gflow-diagnostics-latest.txt";

    private DiagnosticsRunner() {}

    static Result run(Context context, boolean includeWrites, String reason) {
        try {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
            StringBuilder log = new StringBuilder();
            log.append("GFlow auto diagnostics\n")
                    .append(new Date())
                    .append("\nreason=")
                    .append(reason == null ? "manual" : reason)
                    .append("\nincludeWrites=")
                    .append(includeWrites)
                    .append("\n\n");
            log.append(safeBlock("AdaptAPI availability", adapter::availability)).append("\n\n");

            LinkedHashMap<String, int[]> groups = buildDiagnosticsGroups();
            int total = 0;
            for (Map.Entry<String, int[]> entry : groups.entrySet()) {
                log.append("== ").append(entry.getKey()).append(" ==\n");
                for (int functionId : entry.getValue()) {
                    total++;
                    int zone = adapter.spec(functionId).defaultZone;
                    log.append(safeBlock("catalog " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.catalogSummary(functionId))).append("\n");
                    log.append(safeBlock("values " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.valuesSummary(functionId, zone))).append("\n");
                    log.append(safeBlock("support " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.catalogSupport(functionId, zone).message)).append("\n");
                    log.append(safeBlock("get " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.catalogReadInt(functionId, zone).message)).append("\n\n");
                }
            }

            appendSection(log, "Function Watcher", safeBlock("Function Watcher", () -> collectFunctionWatcherDiagnostics(adapter, groups)));
            appendAdvancedDiagnostics(context, log);
            appendSection(log, "Logcat Snapshot", safeBlock("Logcat Snapshot", DiagnosticsRunner::collectLogcatSnapshot));
            if (includeWrites) appendWriteSweep(context, log);

            File cacheFile = new File(context.getCacheDir(), LATEST_REPORT);
            writeText(cacheFile, log.toString());
            File removableFile = saveToRemovableSd(context, log.toString());
            String message = "Лог готов: " + total + " function IDs"
                    + "\ncache=" + cacheFile.getAbsolutePath()
                    + "\nremovableSd=" + (removableFile == null ? "unavailable" : removableFile.getAbsolutePath());
            Log.i(TAG, message);
            return new Result(true, message, cacheFile, removableFile);
        } catch (Exception e) {
            String message = "Ошибка диагностики: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            Log.e(TAG, message, e);
            return new Result(false, message, null, null);
        }
    }

    static File saveLatestToRemovableSd(Context context) {
        File cacheFile = new File(context.getCacheDir(), LATEST_REPORT);
        if (!cacheFile.exists()) return null;
        try (FileInputStream in = new FileInputStream(cacheFile)) {
            File target = resolveRemovableSdReportFile(context, false);
            if (target == null) return null;
            byte[] buf = new byte[8192];
            try (FileOutputStream out = new FileOutputStream(target)) {
                for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
                out.flush();
            }
            return target;
        } catch (Exception e) {
            Log.e(TAG, "saveLatestToRemovableSd failed", e);
            return null;
        }
    }

    private static void appendWriteSweep(Context context, StringBuilder log) {
        log.append("== Write Sweep ==\n");
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        for (EcarxVehicleAdapter.Command command : buildWriteSweep()) {
            EcarxVehicleAdapter.Result result = adapter.set(command.functionId, command.zone, command.value);
            log.append("set ")
                    .append(EcarxVehicleAdapter.hex(command.functionId))
                    .append("/")
                    .append(command.zone)
                    .append(" -> ")
                    .append(EcarxVehicleAdapter.hex(command.value))
                    .append(" :: ")
                    .append(result.message)
                    .append("\n");
        }
        log.append("\n");
    }

    private static EcarxVehicleAdapter.Command[] buildWriteSweep() {
        return new EcarxVehicleAdapter.Command[]{
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.BLOWING_MODE_FACE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.HVAC_SEAT_VENTILATION_LEVEL_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.WHEEL_HEAT_LOW),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_LEFT, EcarxVehicleAdapter.WINDOW_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_WINDOW_ROW_1_RIGHT, EcarxVehicleAdapter.WINDOW_CLOSE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_LEFT, EcarxVehicleAdapter.DOOR_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_1_RIGHT, EcarxVehicleAdapter.DOOR_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_2_LEFT, EcarxVehicleAdapter.DOOR_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_ROW_2_RIGHT, EcarxVehicleAdapter.DOOR_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_HOOD, EcarxVehicleAdapter.DOOR_OPEN),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.BCM_DOOR_ROW_2_LEFT, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.BCM_DOOR_ROW_2_RIGHT, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.WIPER_AUTO),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.SEAT_POSITION_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON)
        };
    }

    private static void appendAdvancedDiagnostics(Context context, StringBuilder log) {
        appendSection(log, "Parking Signals", safeBlock("Parking Signals", () -> collectParkingSignalsDiagnostics(context)));
        appendSection(log, "Parking HAL", safeBlock("Parking HAL", () -> collectParkingHalDiagnostics(context)));
        appendSection(log, "HUD / DIM", safeBlock("HUD / DIM", () -> collectHudDimDiagnostics(context)));
        appendSection(log, "AudioExt", safeBlock("AudioExt", () -> collectAudioExtDiagnostics(context)));
        appendSection(log, "DVR / EVS", safeBlock("DVR / EVS", () -> collectDvrDiagnostics(context)));
        appendSection(log, "Camera2 Inventory", safeBlock("Camera2 Inventory", () -> collectCameraInventoryDiagnostics(context)));
        appendSection(log, "OneOS Dock", safeBlock("OneOS Dock", () -> collectDockDiagnostics(context)));
        appendSection(log, "ControlBoard", safeBlock("ControlBoard", () -> collectControlBoardDiagnostics(context)));
    }

    private static String collectFunctionWatcherDiagnostics(EcarxVehicleAdapter adapter, LinkedHashMap<String, int[]> groups) {
        StringBuilder sb = new StringBuilder();
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        for (int[] group : groups.values()) {
            for (int id : group) ids.add(id);
        }
        int[] functionIds = new int[ids.size()];
        int index = 0;
        for (Integer id : ids) functionIds[index++] = id;

        final Object lock = new Object();
        final int[] events = {0};
        boolean registered = adapter.watchFunctions(new EcarxVehicleAdapter.FunctionWatcher() {
            @Override
            public void onChanged(int functionId) {
                appendEvent("changed", functionId, Integer.MIN_VALUE, 0, 0f);
            }

            @Override
            public void onIntValue(int functionId, int zone, int value) {
                appendEvent("int", functionId, zone, value, 0f);
            }

            @Override
            public void onFloatValue(int functionId, int zone, float value) {
                appendEvent("float", functionId, zone, 0, value);
            }

            @Override
            public void onSupportChanged(int functionId, int zone, String status) {
                synchronized (lock) {
                    events[0]++;
                    sb.append("support ")
                            .append(EcarxVehicleAdapter.hex(functionId))
                            .append("/")
                            .append(zone)
                            .append("=")
                            .append(status)
                            .append("\n");
                    lock.notifyAll();
                }
            }

            @Override
            public void onSupportedValuesChanged(int functionId, int[] values) {
                synchronized (lock) {
                    events[0]++;
                    sb.append("supportedValues ")
                            .append(EcarxVehicleAdapter.hex(functionId))
                            .append("=")
                            .append(values == null ? 0 : values.length)
                            .append(" values\n");
                    lock.notifyAll();
                }
            }

            private void appendEvent(String kind, int functionId, int zone, int intValue, float floatValue) {
                synchronized (lock) {
                    events[0]++;
                    sb.append(kind)
                            .append(" ")
                            .append(EcarxVehicleAdapter.hex(functionId))
                            .append("/")
                            .append(zone);
                    if ("float".equals(kind)) sb.append("=").append(floatValue);
                    else if ("int".equals(kind)) sb.append("=").append(EcarxVehicleAdapter.hex(intValue));
                    sb.append("\n");
                    lock.notifyAll();
                }
            }
        }, functionIds);

        sb.append("registered=").append(registered).append(", ids=").append(functionIds.length).append("\n");
        if (registered) {
            synchronized (lock) {
                if (events[0] == 0) {
                    try {
                        lock.wait(750L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            adapter.unwatchFunctions();
        }
        if (events[0] == 0) sb.append("events=0\n");
        return sb.toString();
    }

    private static void appendSection(StringBuilder log, String title, String body) {
        log.append("== ").append(title).append(" ==\n");
        log.append(body == null || body.trim().isEmpty() ? "No data\n\n" : body.trim() + "\n\n");
    }

    private static String safeBlock(String label, Supplier supplier) {
        try {
            String value = supplier.get();
            return value == null || value.trim().isEmpty() ? label + ": no data" : value;
        } catch (Throwable t) {
            return label + ": error " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
    }

    private static LinkedHashMap<String, int[]> buildDiagnosticsGroups() {
        LinkedHashMap<String, int[]> groups = new LinkedHashMap<>();
        groups.put("HVAC Core", resolveFunctionIds(
                "HVAC_POWER", "HVAC_AUTO", "HVAC_AC", "HVAC_FAN_SPEED", "HVAC_TEMP",
                "HVAC_TEMP_UNIT", "HVAC_CLIMATE_ZONE", "HVAC_DEFROST_FRONT", "HVAC_DEFROST_FRONT_MAX",
                "HVAC_DEFROST_REAR", "HVAC_SEAT_HEATING", "HVAC_SEAT_VENTILATION",
                "HVAC_STEERING_WHEEL_HEAT", "HVAC_IONS_SWITCH", "HVAC_AQS_SWITCH",
                "HVAC_PRE_CLIMATISATION", "HVAC_POST_CLIMATISATION", "HVAC_CO2_SWITCH",
                "HVAC_RAPID_COOLING", "HVAC_RAPID_WARMING", "HVAC_AUTOMATIC_VENTILATION_DRY",
                "HVAC_AIR_FRAGRANCE"
        ));
        groups.put("Vehicle Body", resolveFunctionIds(
                "BCM_WINDOW", "BCM_DOOR", "BCM_DOOR_LOCK", "BCM_DOOR_STATUS",
                "BCM_SUNROOF_OPEN", "BCM_MIRROR_FOLD", "BCM_LIGHT_DIPPED_BEAM", "BCM_LIGHT_GRILLE"
        ));
        groups.put("Drive / Cluster", resolveFunctionIds(
                "DRIVE_MODE_SELECT", "DRIVE_CUSTOM_PROPULSION", "DRIVE_CUSTOM_SUSPENSION",
                "DRIVE_CUSTOM_STEERING_FEEL", "DRIVE_CUSTOM_CLIMATE", "DRIVE_DIM_THEME_SET",
                "DRIVE_ENERGY_MODE", "DRIVE_CREEP_SET", "DRIVE_LAUNCH_CONTROL",
                "DRIVE_NOISE_CONTROL", "DRIVE_ESC_LEVEL", "DRIVE_STARTRACK_MODE",
                "DRIVE_PERFORMANCE_SAVING", "DRIVE_POWER_TRAIN_STOP"
        ));
        groups.put("ADAS", resolveFunctionIds(
                "ADAS_AEB", "ADAS_FCW", "ADAS_LKA", "ADAS_LDW", "ADAS_RCW", "ADAS_ELKA",
                "ADAS_ACC_ICC_SWITCH", "ADAS_ACC_TIME_GAP", "ADAS_ACC_WITH_TSR", "ADAS_PDC",
                "ADAS_PDC_WARNING_VOLUME", "ADAS_DRIVE_PILOT", "ADAS_DRIVE_PILOT_STATUS",
                "ADAS_DRIVE_PILOT_ALARM_INFO", "ADAS_DRIVE_PILOT_ACC_LCC_SWITCH",
                "ADAS_DRIVE_NZP_STATUS", "ADAS_MAX_CRUISING_SPEED", "ADAS_APB_MODE",
                "ADAS_TRAFFIC_LIGHT_ATTENTION", "ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND",
                "ADAS_PADDLE_LANE_CHANGE_ASSIST", "ADAS_SPEED_LIMIT_WARNING_MODE",
                "ADAS_ADAPTIVE_CRUISE_FAILURE", "ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE",
                "ADAS_EMERGENCY_STEERING_FAILURE", "ADAS_FORWARD_PRECOLLISION_FAULT",
                "ADAS_FRONT_SIDE_ASSIST_FAILURE", "ADAS_LANE_KEEPING_ASSISTANCE_FAILURE",
                "ADAS_REAR_COLLISION_WARNING_FAILURE", "ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE"
        ));
        groups.put("Parking / APA / AVM", resolveFunctionIds(
                "PAS_ACTIVATED", "PAS_STATUS", "PAS_SHOW_GRAPHICS", "PAS_RADAR_FRONT_CENTER",
                "PAS_RADAR_REAR_CENTER", "PAS_RADAR_WORK_MODE", "PAS_RADAR_WORK_STATUS",
                "PAS_DRVR_ASSC_SYS_BTN_PUSH", "PAS_DRVR_ASSC_SYS_PARK_MOD", "PAS_AUT_PRKG_SLOT_NR_REQ",
                "PAS_APA_SELF_RECOMMENDED", "PAS_APA_DETECT_PARKING_SPACE", "PAS_APA_RPA_SWITCH",
                "PAS_PRKG_INTRPT_RELD_BTN",
                "PAS_PAC_ACTIVATION", "PAS_PAC_STATUS", "PAS_PAC_AUTO_REVERSE_CAMERA",
                "PAS_PAC_VIEW_SELECTION", "PAS_PAC_3DVIEW_POSITION", "PAS_PAC_OVERLAY_STEERPATH",
                "PAS_PAC_OVERLAY_TOWBAR", "PAS_PAC_OVERLAY_DSTINFO", "PAS_PAC_CAR_MODE_TRANSPARENT",
                "PAS_PAC_TOP_VIEW_ZOOM_IN", "PAS_PAC_TOURING_VIEW", "PAS_SAP_ACTIVATION",
                "PAS_SAP_PARK_TYPE", "PAS_SAP_PARK_IN_TYPE", "PAS_RCTA_ACTIVATION",
                "PAS_RCTA_LEFT_WARNING", "PAS_RCTA_RIGHT_WARNING", "PAS_RCTA_WARNING_VOLUME",
                "PAS_AVM_OR_APA_ACTIVATION"
        ));
        groups.put("OEM Custom Keys", resolveFunctionIds(
                "BCM_CUSTOM_KEY",
                "CUSTOM_KEY_DVR", "CUSTOM_KEY_TRUNK", "CUSTOM_KEY_360",
                "CUSTOM_KEY_NAVIGATION", "CUSTOM_KEY_DIM_FULL_SCREEN_MAP",
                "CUSTOM_KEY_SOUND_SWITCH", "CUSTOM_KEY_COLLECT_FAV",
                "CUSTOM_KEY_REAR_MIRROR_ADJUST", "CUSTOM_KEY_LOUD_SPEAKER",
                "CUSTOM_KEY_AUTO_PARK", "CUSTOM_KEY_DRIVING_MODE"
        ));
        groups.put("HUD / OneOS", resolveFunctionIds(
                "HUD_ACTIVE", "HUD_DISPLAY_SAFETY", "HUD_DISPLAY_MEDIA", "HUD_DISPLAY_NAVI",
                "HUD_DISPLAY_BTPHONE", "HUD_DISPLAY_DRIVE_ENVIRONMENT"
        ));
        groups.put("Ambience / DayMode", resolveFunctionIds(
                "AMBIENCE_LIGHT_THEME_COLOR", "AMBIENCE_LIGHT_EFFECT", "AMBIENCE_LIGHT_CONTROL_MODE",
                "AMBIENCE_LIGHT_MUSIC", "AMBIENCE_LIGHT_MUSIC_SHOW_MODE", "AMBIENCE_LIGHT_WELCOME_SHOW",
                "AMBIENCE_LIGHT_WELCOME_SHOW_MODE", "AMBIENCE_LIGHT_VOICE", "AMBIENCE_LIGHT_ZONE_EXPERIENCE",
                "AMBIENCE_LIGHT_MAIN_ZONES", "AMBIENCE_LIGHT_TOP_ZONES", "AMBIENCE_LIGHT_BOT_ZONES",
                "AMBIENCE_LIGHT_COLOR_WEATHER", "AMBIENCE_LIGHT_BRIGHTNESS_DRIVING",
                "AMBIENCE_LIGHT_BRIGHTNESS_STATIONARY", "AMBIENCE_LIGHT_COLOR_TYPE",
                "AMBIENCE_LIGHT_CLIMATE", "AMBIENCE_LIGHT_GOODBYE_SHOW",
                "AMBIENCE_LIGHT_PHONE_CALL_REMINDER", "AMBIENCE_LIGHT_SLIDING_DOOR_REMINDER",
                "AMBIENCE_LIGHT_INTERACTIVE_EFFECT", "AMBIENCE_LIGHT_SOLID_COLOR_SET",
                "AMBIENCE_LIGHT_BREATHE_COLOR_SET", "AMBIENCE_LIGHT_ENDURANCE_MILE_REMINDER",
                "AMBIENCE_LIGHT_ICHARGING_REMIND", "DAYMODE_SETTING", "DAYMODE_SYNC",
                "DAYMODE_BRIGHTNESS_DAY", "DAYMODE_BRIGHTNESS_NIGHT", "DAYMODE_BRIGHTNESS_MAX",
                "DAYMODE_BRIGHTNESS_MIN", "DAYMODE_BRIGHTNESS_STEP", "DAYMODE_BACKLIGHT_LINKAGE",
                "DAYMODE_BACKLIGHT_BRIGHTNESS", "DAYMODE_DIM_BRIGHTNESS", "DAYMODE_FLOODLIGHT_BRIGHTNESS",
                "DAYMODE_BRIGHTNESS_SCREEN", "DAYMODE_ELECTRIC_REAR_VIEW_MIRROR"
        ));
        groups.put("AVAS / Digital Key / Seat", resolveFunctionIds(
                "VEHICLE_AVAS_SWITCH", "VEHICLE_AVAS_VOLUME", "VEHICLE_AVAS_SOUND_TYPE",
                "VEHICLE_AVAS_SOUND_TYPE_NAME", "VEHICLE_AVAS_SOUND_TYPE_PATH",
                "VEHICLE_DIGITAL_KEY", "VEHICLE_DIGITAL_KEY_REQ_STS", "VEHICLE_DIGITAL_KEY_UNPAIR",
                "VEHICLE_DIGITAL_KEY_TERMINATION", "VEHICLE_DIGITAL_KEY_SUSPENSION",
                "VEHICLE_DIGITAL_KEY_PAIRING_FAILED", "VEHICLE_DIGITAL_KEY_TRACKING_WAIT",
                "VEHICLE_DIGITAL_KEY_TRACKING_RESULT", "VEHICLE_DIGITAL_KEY_RES_TIMEOUT",
                "SEAT_LENGTH", "SEAT_HEIGHT", "SEAT_BACKREST", "SEAT_POSITION_SAVE",
                "SEAT_POSITION_SET", "SEAT_RESTORE", "SEAT_ONE_KEY_COMFORT"
        ));
        return groups;
    }

    private static int[] resolveFunctionIds(String... fieldNames) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        List<String> missing = new ArrayList<>();
        for (String fieldName : fieldNames) {
            try {
                Field field = EcarxVehicleAdapter.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                values.add(field.getInt(null));
            } catch (Exception e) {
                missing.add(fieldName);
            }
        }
        if (!missing.isEmpty()) Log.w(TAG, "Diagnostics fields missing: " + missing);
        int[] result = new int[values.size()];
        int index = 0;
        for (Integer value : values) result[index++] = value;
        return result;
    }

    private static String collectParkingSignalsDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(context);
        Object[] pairs = {
                "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS
        };
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            sb.append(adapter.get(String.valueOf(pairs[i]), (Integer) pairs[i + 1]).message).append("\n");
        }
        return sb.toString();
    }

    private static String collectParkingHalDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(context);
        int[] properties = {
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_REQ_RESP,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_ON_OFF1,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_UINT8,
                CarSignalManagerAdapter.VEH_PUSH_APA_INFO_REQ
        };
        for (int property : properties) {
            sb.append(adapter.rawHalProperty(property, "VehiclePropertyVEH2").message).append("\n");
        }
        return sb.toString();
    }

    private static String collectHudDimDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        EcarxHudDimAdapter adapter = new EcarxHudDimAdapter(context);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.hudStatus().message).append("\n");
        sb.append(adapter.hudSync().message).append("\n");
        sb.append(adapter.dimStatus().message).append("\n");
        sb.append(adapter.dimMenuReadyAndTheme().message).append("\n");
        return sb.toString();
    }

    private static String collectAudioExtDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        AudioExtServiceAdapter adapter = new AudioExtServiceAdapter(context);
        sb.append(adapter.bindAudioExt().message).append("\n");
        sb.append(adapter.visualizerStatus().message).append("\n");
        return sb.toString();
    }

    private static String collectDvrDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        EcarxDvrAdapter adapter = new EcarxDvrAdapter(context);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_REAR).message).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_AVM).message).append("\n");
        sb.append(adapter.isEvsOpened(EcarxDvrAdapter.EVS_CAMERA_DVR).message).append("\n");
        sb.append(adapter.dvrCameraOnline().message).append("\n");
        sb.append(adapter.dvrCapture().message).append("\n");
        sb.append(adapter.dvrCurrentMode().message).append("\n");
        sb.append(adapter.dvrSdcardStatus().message).append("\n");
        return sb.toString();
    }

    private static String collectCameraInventoryDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            CameraManager manager = context.getSystemService(CameraManager.class);
            if (manager == null) return "CameraManager unavailable";
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(id);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                sb.append("camera2:").append(id).append(" · ").append(facingName(facing)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Camera2 error: ").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        }
        sb.append("EVS: rear, 360, dvr");
        return sb.toString();
    }

    private static String collectDockDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();
        EcarxDockAdapter adapter = new EcarxDockAdapter(context);
        sb.append(adapter.availability()).append("\n");
        sb.append(adapter.deviceStatus().message).append("\n");
        return sb.toString();
    }

    private static String collectControlBoardDiagnostics(Context context) {
        return new EcarxControlBoardAdapter(context).availability();
    }

    private static String collectLogcatSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append(runLogcatCommand("recent", new String[]{"logcat", "-d", "-v", "threadtime", "-t", "400"}));
        sb.append("\n");
        sb.append(runLogcatCommand("gflow-filtered", new String[]{
                "logcat", "-d", "-v", "threadtime", "-s",
                "GFlowCarApi", "GFlowDiagnostics", "GFlow"
        }));
        return sb.toString();
    }

    private static String runLogcatCommand(String label, String[] command) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ").append(label).append(" --\n");
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true).start();
            int lines = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    if (lines++ >= 500 || sb.length() > 120000) {
                        sb.append("[truncated]\n");
                        break;
                    }
                    sb.append(line).append("\n");
                }
            }
            int exit = process.waitFor();
            sb.append("exit=").append(exit).append("\n");
        } catch (Exception e) {
            sb.append("error ")
                    .append(e.getClass().getSimpleName())
                    .append(": ")
                    .append(e.getMessage())
                    .append("\n");
        } finally {
            if (process != null) process.destroy();
        }
        return sb.toString();
    }

    private static String facingName(Integer facing) {
        if (facing == null) return "unknown";
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "front";
        if (facing == CameraCharacteristics.LENS_FACING_BACK) return "rear";
        if (android.os.Build.VERSION.SDK_INT >= 23 && facing == CameraCharacteristics.LENS_FACING_EXTERNAL) return "external";
        return "other";
    }

    private static File saveToRemovableSd(Context context, String text) {
        try {
            File target = resolveRemovableSdReportFile(context, true);
            if (target == null) return null;
            writeText(target, text);
            return target;
        } catch (Exception e) {
            Log.e(TAG, "Unable to save diagnostics to removable SD", e);
            return null;
        }
    }

    private static File resolveRemovableSdReportFile(Context context, boolean timestamped) {
        File[] dirs = context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);
        if (dirs == null) return null;
        for (int i = 1; i < dirs.length; i++) {
            File dir = dirs[i];
            if (dir == null) continue;
            File targetDir = new File(dir, "GFlow");
            if (!targetDir.exists() && !targetDir.mkdirs()) continue;
            String name = timestamped
                    ? String.format(Locale.US, "gflow-diagnostics-%d.txt", System.currentTimeMillis())
                    : LATEST_REPORT;
            return new File(targetDir, name);
        }
        return null;
    }

    private static void writeText(File file, String text) throws Exception {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Не удалось создать каталог: " + parent);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes("UTF-8"));
            out.flush();
        }
    }

    interface Supplier {
        String get() throws Exception;
    }

    static final class Result {
        final boolean success;
        final String message;
        final File cacheFile;
        final File removableSdFile;

        Result(boolean success, String message, File cacheFile, File removableSdFile) {
            this.success = success;
            this.message = message;
            this.cacheFile = cacheFile;
            this.removableSdFile = removableSdFile;
        }
    }
}
