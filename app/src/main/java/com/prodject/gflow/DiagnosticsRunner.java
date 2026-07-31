package com.prodject.gflow;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
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
    static final String FIXED_PUBLIC_REPORT = "/storage/emulated/0/gflow_data.log";

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
                    EcarxVehicleAdapter.Result support = safeResult(() -> adapter.catalogSupport(functionId, zone));
                    EcarxVehicleAdapter.Result read = safeResult(() -> adapter.catalogReadInt(functionId, zone));
                    log.append(safeBlock("catalog " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.catalogSummary(functionId))).append("\n");
                    log.append(safeBlock("values " + EcarxVehicleAdapter.hex(functionId),
                            () -> adapter.valuesSummary(functionId, zone))).append("\n");
                    log.append("support ").append(EcarxVehicleAdapter.hex(functionId)).append(": ").append(support.message).append("\n");
                    log.append("get ").append(EcarxVehicleAdapter.hex(functionId)).append(": ").append(read.message).append("\n");
                    log.append(diagnosticSummary(adapter, functionId, zone, support, read)).append("\n\n");
                }
            }

            appendSection(log, "Function Watcher", safeBlock("Function Watcher", () -> collectFunctionWatcherDiagnostics(adapter, groups)));
            appendAdvancedDiagnostics(context, log);
            appendSection(log, "Logcat Snapshot", safeBlock("Logcat Snapshot", DiagnosticsRunner::collectLogcatSnapshot));
            if (includeWrites) appendWriteSweep(context, log);
            appendRoofPositionSweep(adapter, log, includeWrites);
            appendCatalogSweep(adapter, log, includeWrites);

            File cacheFile = new File(context.getCacheDir(), LATEST_REPORT);
            writeText(cacheFile, log.toString());
            File publicFile = saveToFixedPublicLog(log.toString());
            String message = "Лог готов: " + total + " function IDs"
                    + "\ncache=" + cacheFile.getAbsolutePath()
                    + "\npublic=" + (publicFile == null ? "unavailable: " + FIXED_PUBLIC_REPORT : publicFile.getAbsolutePath());
            Log.i(TAG, message);
            return new Result(true, message, cacheFile, publicFile);
        } catch (Exception e) {
            String message = "Ошибка диагностики: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            Log.e(TAG, message, e);
            return new Result(false, message, null, null);
        }
    }

    static File saveLatestToFixedPublicLog(Context context) {
        File cacheFile = new File(context.getCacheDir(), LATEST_REPORT);
        if (!cacheFile.exists()) return null;
        try (FileInputStream in = new FileInputStream(cacheFile)) {
            File target = fixedPublicLogFile();
            byte[] buf = new byte[8192];
            try (FileOutputStream out = new FileOutputStream(target)) {
                for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
                out.flush();
            }
            return target;
        } catch (Exception e) {
            Log.e(TAG, "saveLatestToFixedPublicLog failed", e);
            return null;
        }
    }

    static File fixedPublicLogFile() {
        return new File(FIXED_PUBLIC_REPORT);
    }

    private static void appendWriteSweep(Context context, StringBuilder log) {
        log.append("== Write Sweep ==\n");
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        for (EcarxVehicleAdapter.Command command : buildWriteSweep()) {
            Snapshot snapshot = snapshotInt(adapter, command.functionId, command.zone);
            EcarxVehicleAdapter.Result result = safeResult(() -> adapter.set(command.functionId, command.zone, command.value));
            RestoreResult restore = restoreInt(adapter, snapshot);
            log.append("set ")
                    .append(EcarxVehicleAdapter.hex(command.functionId))
                    .append("/")
                    .append(command.zone)
                    .append(" -> ")
                    .append(EcarxVehicleAdapter.hex(command.value))
                    .append(" :: ")
                    .append(result.message)
                    .append(" :: WRITE_SUMMARY status=")
                    .append(result.success ? "OK action=keep_ui_enabled" : result.isSupported() ? "WRITE_FAIL action=fix_value_or_zone" : "UNSUPPORTED action=disable_ui")
                    .append(" snapshot=")
                    .append(snapshot.status)
                    .append(" restore=")
                    .append(restore.status)
                    .append(" restoreMessage=")
                    .append(compactLine(restore.message))
                    .append("\n");
        }
        log.append("\n");
    }

    private static void appendCatalogSweep(EcarxVehicleAdapter adapter, StringBuilder log, boolean includeWrites) {
        log.append("== Catalog Sweep ==\n");
        int total = 0;
        int supportedReadOk = 0;
        int supportedWriteOk = 0;
        int writeFail = 0;
        int unsupported = 0;
        int readOnly = 0;
        int noValues = 0;
        for (CarFunctionCatalog.Entry entry : CarFunctionCatalog.BY_ID.values()) {
            if (entry.type != CarFunctionCatalog.TYPE_FUNCTION) continue;
            total++;
            int functionId = entry.id;
            EcarxVehicleAdapter.Spec spec = adapter.spec(functionId);
            int zone = spec.defaultZone;
            EcarxVehicleAdapter.Result support = safeResult(() -> adapter.catalogSupport(functionId, zone));
            EcarxVehicleAdapter.Result read = safeResult(() -> adapter.catalogReadInt(functionId, zone));
            CarFunctionCatalog.Value[] values = CarFunctionCatalog.staticValues(functionId);
            String status;
            String writeStatus = "";

            if (support == null || !support.success || !support.isSupported()) {
                status = "UNSUPPORTED";
                unsupported++;
            } else if (!spec.writable) {
                status = "READ_ONLY";
                readOnly++;
            } else if (read != null && read.success) {
                status = "SUPPORTED_READ_OK";
                supportedReadOk++;
            } else {
                status = "SUPPORTED_READ_OK";
                supportedReadOk++;
            }

            if (includeWrites && support != null && support.success && support.isSupported() && spec.writable) {
                if (isUnsafeCatalogWrite(functionId)) {
                    writeStatus = " write=SKIPPED_UNSAFE";
                } else if (values == null || values.length == 0) {
                    writeStatus = " write=NO_VALUES";
                    noValues++;
                } else {
                    WriteProbeResult probe = probeCatalogWrite(adapter, functionId, zone, values);
                    writeStatus = " write=" + probe.status + " value=" + EcarxVehicleAdapter.hex(probe.value)
                            + " message=" + compactLine(probe.message)
                            + " snapshot=" + probe.snapshotStatus
                            + " restore=" + probe.restoreStatus
                            + " restoreMessage=" + compactLine(probe.restoreMessage);
                    if ("SUPPORTED_WRITE_OK".equals(probe.status)) supportedWriteOk++;
                    else writeFail++;
                }
            } else if (includeWrites && spec.writable && (values == null || values.length == 0)) {
                writeStatus = " write=NO_VALUES";
                noValues++;
            }

            log.append("CATALOG_SWEEP ")
                    .append(status)
                    .append(" id=")
                    .append(EcarxVehicleAdapter.hex(functionId))
                    .append(" zone=")
                    .append(zone)
                    .append(" key=")
                    .append(entry.key)
                    .append(" writable=")
                    .append(spec.writable)
                    .append(" values=")
                    .append(values == null ? 0 : values.length)
                    .append(" support=")
                    .append(compactLine(support == null ? "null" : support.message))
                    .append(" read=")
                    .append(compactLine(read == null ? "null" : read.message))
                    .append(writeStatus)
                    .append("\n");
        }
        log.append("CATALOG_SWEEP_SUMMARY total=").append(total)
                .append(" SUPPORTED_READ_OK=").append(supportedReadOk)
                .append(" SUPPORTED_WRITE_OK=").append(supportedWriteOk)
                .append(" WRITE_FAIL=").append(writeFail)
                .append(" UNSUPPORTED=").append(unsupported)
                .append(" READ_ONLY=").append(readOnly)
                .append(" NO_VALUES=").append(noValues)
                .append("\n\n");
    }

    private static WriteProbeResult probeCatalogWrite(EcarxVehicleAdapter adapter, int functionId, int zone, CarFunctionCatalog.Value[] values) {
        int attempts = Math.min(values.length, 2);
        EcarxVehicleAdapter.Result last = null;
        int lastValue = 0;
        Snapshot snapshot = snapshotInt(adapter, functionId, zone);
        for (int i = 0; i < attempts; i++) {
            final int value = values[i].value;
            lastValue = value;
            last = safeResult(() -> adapter.set(functionId, zone, value));
            if (last.success) {
                RestoreResult restore = restoreInt(adapter, snapshot);
                return new WriteProbeResult("SUPPORTED_WRITE_OK", value, last.message, snapshot.status, restore.status, restore.message);
            }
        }
        RestoreResult restore = restoreInt(adapter, snapshot);
        return new WriteProbeResult("WRITE_FAIL", lastValue, last == null ? "no write attempted" : last.message, snapshot.status, restore.status, restore.message);
    }

    private static void appendRoofPositionSweep(EcarxVehicleAdapter adapter, StringBuilder log, boolean includeWrites) {
        log.append("== Roof Position Sweep ==\n");
        appendRoofPositionZone(adapter, log, "sunroof", EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, includeWrites);
        appendRoofPositionZone(adapter, log, "sun curtain", EcarxVehicleAdapter.ZONE_ROW_1_ALL, includeWrites);
        log.append("\n");
    }

    private static void appendRoofPositionZone(EcarxVehicleAdapter adapter, StringBuilder log, String label, int zone, boolean includeWrites) {
        int functionId = EcarxVehicleAdapter.BCM_WINDOW_POS;
        EcarxVehicleAdapter.Result support = safeResult(() -> adapter.catalogSupport(functionId, zone));
        EcarxVehicleAdapter.Result read = safeResult(() -> adapter.getFloat(functionId, zone));
        log.append("ROOF_POSITION ")
                .append(label)
                .append(" id=")
                .append(EcarxVehicleAdapter.hex(functionId))
                .append(" zone=")
                .append(EcarxVehicleAdapter.hex(zone))
                .append(" support=")
                .append(compactLine(support == null ? "null" : support.message))
                .append(" read=")
                .append(compactLine(read == null ? "null" : read.message));
        if (!includeWrites) {
            log.append(" write=SKIPPED_READ_ONLY_RUN\n");
            return;
        }
        if (support == null || !support.success || !support.isSupported()) {
            log.append(" write=UNSUPPORTED\n");
            return;
        }
        FloatSnapshot snapshot = snapshotFloat(adapter, functionId, zone);
        float[] candidates = {12f, 40f};
        EcarxVehicleAdapter.Result last = null;
        float lastValue = 0f;
        for (float candidate : candidates) {
            if (snapshot.available() && Math.abs(snapshot.value - candidate) < 0.01f) continue;
            lastValue = candidate;
            last = safeResult(() -> adapter.setFloat(functionId, zone, candidate));
            if (last != null && last.success) break;
        }
        FloatRestoreResult restore = restoreFloat(adapter, snapshot);
        String status = last != null && last.success ? "SUPPORTED_FLOAT_WRITE_OK" : "FLOAT_WRITE_FAIL";
        log.append(" write=")
                .append(status)
                .append(" value=")
                .append(String.format(Locale.US, "%.1f", lastValue))
                .append(" message=")
                .append(compactLine(last == null ? "no write attempted" : last.message))
                .append(" snapshot=")
                .append(snapshot.status)
                .append(" restore=")
                .append(restore.status)
                .append(" restoreMessage=")
                .append(compactLine(restore.message))
                .append("\n");
    }

    private static boolean isUnsafeCatalogWrite(int functionId) {
        switch (functionId) {
            case EcarxVehicleAdapter.BCM_WINDOW:
            case EcarxVehicleAdapter.BCM_WINDOW_POS:
            case EcarxVehicleAdapter.BCM_WINDOW_LOCK:
            case EcarxVehicleAdapter.BCM_SUNROOF_OPEN:
            case EcarxVehicleAdapter.BCM_SUNROOF_CLOSE:
            case EcarxVehicleAdapter.BCM_SUNCURT_OPEN:
            case EcarxVehicleAdapter.BCM_SUNCURT_CLOSE:
            case EcarxVehicleAdapter.BCM_SUNROOF_TILT:
            case EcarxVehicleAdapter.SEAT_LENGTH:
            case EcarxVehicleAdapter.SEAT_HEIGHT:
            case EcarxVehicleAdapter.SEAT_BACKREST:
            case EcarxVehicleAdapter.SEAT_POSITION_SET:
            case EcarxVehicleAdapter.SEAT_POSITION_SAVE:
            case EcarxVehicleAdapter.SEAT_RESTORE:
                return true;
            default:
                return false;
        }
    }

    private static Snapshot snapshotInt(EcarxVehicleAdapter adapter, int functionId, int zone) {
        if (adapter.spec(functionId).floatValue) {
            return new Snapshot(functionId, zone, 0, "SKIPPED_FLOAT", "float readback is not restored by int snapshot");
        }
        EcarxVehicleAdapter.Result read = safeResult(() -> adapter.catalogReadInt(functionId, zone));
        if (read != null && read.success && read.isSupported()) {
            return new Snapshot(functionId, zone, read.value, "OK", read.message);
        }
        return new Snapshot(functionId, zone, 0, "SKIPPED_NO_READBACK", read == null ? "null" : read.message);
    }

    private static RestoreResult restoreInt(EcarxVehicleAdapter adapter, Snapshot snapshot) {
        if (!snapshot.available()) {
            return new RestoreResult("SKIPPED_NO_SNAPSHOT", snapshot.message);
        }
        EcarxVehicleAdapter.Result restore = safeResult(() -> adapter.set(snapshot.functionId, snapshot.zone, snapshot.value));
        if (restore != null && restore.success) {
            return new RestoreResult("OK", restore.message);
        }
        return new RestoreResult("FAILED", restore == null ? "null" : restore.message);
    }

    private static FloatSnapshot snapshotFloat(EcarxVehicleAdapter adapter, int functionId, int zone) {
        EcarxVehicleAdapter.Result read = safeResult(() -> adapter.getFloat(functionId, zone));
        if (read != null && read.success && read.isSupported() && !Float.isNaN(read.floatData)) {
            return new FloatSnapshot(functionId, zone, read.floatData, "OK", read.message);
        }
        return new FloatSnapshot(functionId, zone, 0f, "SKIPPED_NO_FLOAT_READBACK", read == null ? "null" : read.message);
    }

    private static FloatRestoreResult restoreFloat(EcarxVehicleAdapter adapter, FloatSnapshot snapshot) {
        if (!snapshot.available()) {
            return new FloatRestoreResult("SKIPPED_NO_SNAPSHOT", snapshot.message);
        }
        EcarxVehicleAdapter.Result restore = safeResult(() -> adapter.setFloat(snapshot.functionId, snapshot.zone, snapshot.value));
        if (restore != null && restore.success) {
            return new FloatRestoreResult("OK", restore.message);
        }
        return new FloatRestoreResult("FAILED", restore == null ? "null" : restore.message);
    }

    private static String compactLine(String message) {
        if (message == null) return "";
        String out = message.replace('\n', ' ').replace('\r', ' ').trim();
        return out.length() > 220 ? out.substring(0, 220) : out;
    }

    private static String diagnosticSummary(EcarxVehicleAdapter adapter, int functionId, int zone,
                                            EcarxVehicleAdapter.Result support,
                                            EcarxVehicleAdapter.Result read) {
        StringBuilder sb = new StringBuilder();
        String key = "unknown";
        CarFunctionCatalog.Entry entry = adapter.catalogEntry(functionId);
        if (entry != null) key = entry.key;
        boolean writable = adapter.isWritable(functionId);
        sb.append("SUMMARY ")
                .append(EcarxVehicleAdapter.hex(functionId))
                .append(" ")
                .append(key)
                .append(" zone=")
                .append(zone)
                .append(" writable=")
                .append(writable)
                .append(" status=");
        if (support == null || !support.success) {
            sb.append("SUPPORT_ERROR action=fix_adapter_or_permissions");
        } else if (!support.isSupported()) {
            sb.append("UNSUPPORTED action=hide_or_disable_in_ui");
        } else if (read == null || !read.success) {
            sb.append("READ_FAIL action=keep_control_but_mark_readback_unknown");
        } else {
            sb.append("OK action=ui_enabled");
        }
        if (writable) {
            CarFunctionCatalog.Value[] values = CarFunctionCatalog.staticValues(functionId);
            sb.append(" writeContract=");
            if (values != null && values.length > 0) sb.append(values.length).append("_static_values");
            else sb.append("runtime_or_direct_value_required");
        } else {
            sb.append(" writeContract=readback_only");
        }
        return sb.toString();
    }

    private static EcarxVehicleAdapter.Result safeResult(ResultSupplier supplier) {
        try {
            EcarxVehicleAdapter.Result result = supplier.get();
            return result == null ? EcarxVehicleAdapter.Result.external("no result", false, false) : result;
        } catch (Throwable t) {
            return EcarxVehicleAdapter.Result.external("error " + t.getClass().getSimpleName() + ": " + t.getMessage(), false, false);
        }
    }

    private static EcarxVehicleAdapter.Command[] buildWriteSweep() {
        return new EcarxVehicleAdapter.Command[]{
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.ZONE_ROW_1_ALL, EcarxVehicleAdapter.BLOWING_MODE_FACE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.HVAC_SEAT_HEATING_LEVEL_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.WHEEL_HEAT_LOW),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.VEHICLE_CENTRAL_LOCK, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.VEHICLE_CENTRAL_LOCK, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_OFF),
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
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_STOCK_MEMORY_SET_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_COMFORT),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_MUD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_ROCK),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_SAND),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_AWD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_EAWD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE18),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE72),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE79),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE97),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.VEHICLE_STEERING_ASSISTANCE_LEVEL, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.STEERING_ASSISTANCE_MEDIUM),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.DRIVE_STEERING_FEEL_SYNC_DRIVE_MODE, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARNING_MODE, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.SPEED_LIMIT_WARNING_MODE_FLASHING),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_NOP_SPEED_LIMIT_WARNING_OFFSET, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.SPEED_LIMIT_WARNING_OFFSET_0KM),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_FCW_SENSITIVITY, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.FCW_SENSITIVITY_NORMAL),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.COMMON_ON)
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
                "BCM_WINDOW_POS", "SETTING_FUNC_CENTRAL_LOCK",
                "BCM_SUNROOF_OPEN", "BCM_MIRROR_FOLD", "BCM_LIGHT_DIPPED_BEAM", "BCM_LIGHT_GRILLE"
        ));
        groups.put("Drive / Cluster", resolveFunctionIds(
                "DRIVE_MODE_SELECT", "DRIVE_CUSTOM_PROPULSION", "DRIVE_CUSTOM_SUSPENSION",
                "DRIVE_CUSTOM_STEERING_FEEL", "DRIVE_CUSTOM_CLIMATE", "DRIVE_DIM_THEME_SET",
                "DRIVE_ENERGY_MODE", "DRIVE_CREEP_SET", "DRIVE_LAUNCH_CONTROL",
                "DRIVE_NOISE_CONTROL", "DRIVE_ESC_LEVEL", "DRIVE_STARTRACK_MODE",
                "DRIVE_PERFORMANCE_SAVING", "DRIVE_POWER_TRAIN_STOP",
                "VEHICLE_STEERING_ASSISTANCE_LEVEL", "DRIVE_STEERING_FEEL_SYNC_DRIVE_MODE"
        ));
        groups.put("Experimental Drive Controls", resolveFunctionIds(
                "DRIVE_MODE_SELECT", "DRIVE_CUSTOM_PROPULSION", "DRIVE_CUSTOM_SUSPENSION",
                "DRIVE_CUSTOM_STEERING_FEEL", "DRIVE_CUSTOM_CLIMATE", "DRIVE_CUSTOM_DRIVER_INFO",
                "DRIVE_ENERGY_MODE", "DRIVE_LAUNCH_CONTROL", "DRIVE_ESC_LEVEL", "DRIVE_STARTRACK_MODE"
        ));
        groups.put("ADAS Core", resolveFunctionIds(
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
        groups.put("PDC Write Candidates", resolveFunctionIds(
                "ADAS_PDC", "PAS_RADAR_WORK_MODE", "PAS_RADAR_WORK_STATUS",
                "PAS_PAC_ACTIVATION", "PAS_PAC_STATUS", "PAS_STATUS", "PAS_ACTIVATED"
        ));
        groups.put("Hidden Assistants", resolveFunctionIds(
                "ADAS_TRAFFIC_SIGN_RECOGNITION", "ADAS_TRAFFIC_SIGN_ALERT", "ADAS_ACC_WITH_TSR",
                "ADAS_SPEED_LIMITATION_MODE", "ADAS_SPEED_LIMIT_WARNING_MODE", "ADAS_SPEED_LIMIT_WARNING_OFFSET",
                "ADAS_NOP_SPEED_LIMIT_WARNING_OFFSET", "ADAS_AEB", "ADAS_FCW", "ADAS_FCW_SENSITIVITY", "ADAS_ELKA", "ADAS_LANE_CHANGE_ASSIST",
                "ADAS_PADDLE_LANE_CHANGE_ASSIST", "ADAS_TRAFFIC_LIGHT_ATTENTION",
                "ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND", "VEHICLE_LANE_KEEPING_AID_WARNING",
                "VEHICLE_REAR_CROSS_TRAFFIC_ALERT", "VEHICLE_STEERING_ASSISTANCE_LEVEL"
        ));
        groups.put("AI Pilot Experimental", resolveFunctionIds(
                "ADAS_AI_DRIVER_ASSIST", "ADAS_AI_ASSIST_DEFAULT_ON", "ADAS_AI_ASSIST_FUSION_NAVI",
                "ADAS_AI_ASSIST_OUT_OVERTAKING_LANE", "ADAS_AI_LANE_CHANGE_STRATEGY",
                "ADAS_AI_LANE_CHANGE_CONFIRM", "ADAS_AI_LANE_CHANGE_WARNING",
                "ADAS_TLB_SWITCH", "ADAS_TLB_MODE"
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
                "AMBIENCE_LIGHT_BREATHE_COLOR_SET", "AMBIENCE_LIGHT_TRANSITION_START_COLOR",
                "AMBIENCE_LIGHT_TRANSITION_END_COLOR", "AMBIENCE_LIGHT_ENDURANCE_MILE_REMINDER",
                "AMBIENCE_LIGHT_ICHARGING_REMIND", "DAYMODE_SETTING", "DAYMODE_SYNC",
                "DAYMODE_BRIGHTNESS_DAY", "DAYMODE_BRIGHTNESS_NIGHT", "DAYMODE_BRIGHTNESS_MAX",
                "DAYMODE_BRIGHTNESS_MIN", "DAYMODE_BRIGHTNESS_STEP", "DAYMODE_BACKLIGHT_LINKAGE",
                "DAYMODE_BACKLIGHT_BRIGHTNESS", "DAYMODE_DIM_BRIGHTNESS", "DAYMODE_FLOODLIGHT_BRIGHTNESS",
                "DAYMODE_BRIGHTNESS_DAYMODE", "DAYMODE_BRIGHTNESS_SCREEN", "DAYMODE_ELECTRIC_REAR_VIEW_MIRROR"
        ));
        groups.put("AVAS / Digital Key / Seat", resolveFunctionIds(
                "VEHICLE_AVAS_SWITCH", "VEHICLE_AVAS_VOLUME", "VEHICLE_AVAS_SOUND_TYPE",
                "VEHICLE_AVAS_SOUND_TYPE_NAME", "VEHICLE_AVAS_SOUND_TYPE_PATH",
                "VEHICLE_DIGITAL_KEY", "VEHICLE_DIGITAL_KEY_REQ_STS", "VEHICLE_DIGITAL_KEY_UNPAIR",
                "VEHICLE_DIGITAL_KEY_TERMINATION", "VEHICLE_DIGITAL_KEY_SUSPENSION",
                "VEHICLE_DIGITAL_KEY_PAIRING_FAILED", "VEHICLE_DIGITAL_KEY_TRACKING_WAIT",
                "VEHICLE_DIGITAL_KEY_TRACKING_RESULT", "VEHICLE_DIGITAL_KEY_RES_TIMEOUT",
                "WPC_WORK_MODE", "WPC_CHARGE_STATES",
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
        sb.append("AVM HAL\n").append(new AvmHalAdapter(context).diagnostics()).append("\n");
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

    private static File saveToFixedPublicLog(String text) {
        try {
            File target = fixedPublicLogFile();
            writeText(target, text);
            return target;
        } catch (Exception e) {
            Log.e(TAG, "Unable to save diagnostics to " + FIXED_PUBLIC_REPORT, e);
            return null;
        }
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

    interface ResultSupplier {
        EcarxVehicleAdapter.Result get() throws Exception;
    }

    static final class WriteProbeResult {
        final String status;
        final int value;
        final String message;
        final String snapshotStatus;
        final String restoreStatus;
        final String restoreMessage;

        WriteProbeResult(String status, int value, String message,
                         String snapshotStatus, String restoreStatus, String restoreMessage) {
            this.status = status;
            this.value = value;
            this.message = message;
            this.snapshotStatus = snapshotStatus;
            this.restoreStatus = restoreStatus;
            this.restoreMessage = restoreMessage;
        }
    }

    static final class Snapshot {
        final int functionId;
        final int zone;
        final int value;
        final String status;
        final String message;

        Snapshot(int functionId, int zone, int value, String status, String message) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
            this.status = status;
            this.message = message;
        }

        boolean available() {
            return "OK".equals(status);
        }
    }

    static final class RestoreResult {
        final String status;
        final String message;

        RestoreResult(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    static final class FloatSnapshot {
        final int functionId;
        final int zone;
        final float value;
        final String status;
        final String message;

        FloatSnapshot(int functionId, int zone, float value, String status, String message) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
            this.status = status;
            this.message = message;
        }

        boolean available() {
            return "OK".equals(status);
        }
    }

    static final class FloatRestoreResult {
        final String status;
        final String message;

        FloatRestoreResult(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    static final class Result {
        final boolean success;
        final String message;
        final File cacheFile;
        final File publicLogFile;

        Result(boolean success, String message, File cacheFile, File publicLogFile) {
            this.success = success;
            this.message = message;
            this.cacheFile = cacheFile;
            this.publicLogFile = publicLogFile;
        }
    }
}
