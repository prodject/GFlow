package com.prodject.gcontrol;

import android.content.*;
import android.os.Build;
import java.util.*;

final class AutomationEngine {
    static final String PREFS = "automation";
    static final String KEY_PRESET_ORDER = "preset_order";
    static final String KEY_TRIGGER_ORDER = "trigger_order";
    static final String KEY_BUTTON_ORDER = "button_order";
    static final String KEY_PROFILE_ORDER = "profile_order";
    static final String KEY_SMART_CLIMATE = "smart_climate_enabled";
    static final String KEY_SMART_TARGET = "smart_climate_target";
    static final String KEY_SMART_HOT = "smart_climate_hot";
    static final String KEY_SMART_COLD = "smart_climate_cold";
    static final String KEY_CABIN_TEMP = "cabin_temp";
    static final String KEY_OUTSIDE_TEMP = "outside_temp";
    static final String KEY_ACTIVE_PROFILE = "active_profile";

    private AutomationEngine() {}

    static String runPreset(Context context, String name) {
        String encoded = prefs(context).getString("preset:" + name, "");
        CommandPlan plan = decodePlan(encoded);
        if (plan.commands.length == 0 && plan.floatCommands.length == 0 && plan.actions.length == 0) return "Пресет пустой: " + name;
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        StringBuilder sb = new StringBuilder("Smart preset: ").append(name).append("\n");
        for (Action action : plan.actions) {
            sb.append(runAction(context, action)).append("\n");
        }
        for (EcarxVehicleAdapter.Command command : plan.commands) {
            sb.append(adapter.set(command.functionId, command.zone, command.value).message).append("\n");
        }
        for (FloatCommand command : plan.floatCommands) {
            sb.append(adapter.setFloat(command.functionId, command.zone, command.value).message).append("\n");
        }
        return sb.toString();
    }

    private static String runAction(Context context, Action action) {
        if ("profile".equals(action.name)) return applyProfile(context, action.value);
        if ("smart_climate".equals(action.name)) return runSmartClimate(context);
        if ("start_dvr".equals(action.name)) {
            Intent intent = new Intent(context, DvrService.class).setAction(DvrService.ACTION_START);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
            else context.startService(intent);
            return "DVR start requested";
        }
        if ("stop_dvr".equals(action.name)) {
            Intent intent = new Intent(context, DvrService.class).setAction(DvrService.ACTION_STOP);
            context.startService(intent);
            return "DVR stop requested";
        }
        if ("autozoom".equals(action.name)) {
            String[] parts = action.value.split(":", 2);
            String packages = parts.length > 0 && !parts[0].trim().isEmpty() ? parts[0].trim() : "maps,navi,browser";
            float scale = parts.length > 1 ? parseFloat(parts[1], 1.15f) : 1.15f;
            context.getSharedPreferences(AppWatchdogAccessibilityService.PREFS, Context.MODE_PRIVATE).edit()
                    .putBoolean(AppWatchdogAccessibilityService.KEY_ENABLED, true)
                    .putString(AppWatchdogAccessibilityService.KEY_PACKAGES, packages)
                    .putFloat(AppWatchdogAccessibilityService.KEY_SCALE, scale)
                    .apply();
            return "Autozoom enabled for " + packages + " scale=" + scale;
        }
        return "Unknown action: " + action.name + "=" + action.value;
    }

    static void runTrigger(Context context, String type, String value) {
        SharedPreferences p = prefs(context);
        for (String name : names(p, KEY_TRIGGER_ORDER)) {
            String raw = p.getString("trigger:" + name, "");
            String[] parts = raw.split("\\|", -1);
            if (parts.length < 4) continue;
            if (!parts[1].equals(type)) continue;
            String expected = parts[2].trim().toLowerCase(Locale.ROOT);
            String actual = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            if (!expected.isEmpty() && !actual.contains(expected)) continue;
            runPreset(context, parts[3]);
        }
    }

    static boolean runSteering(Context context, int keyCode, String gesture) {
        SharedPreferences p = prefs(context);
        String target = keyCode + ":" + gesture;
        for (String name : names(p, KEY_BUTTON_ORDER)) {
            String raw = p.getString("button:" + name, "");
            String[] parts = raw.split("\\|", -1);
            if (parts.length < 4) continue;
            if ((parts[1] + ":" + parts[2]).equals(target)) {
                runPreset(context, parts[3]);
                return true;
            }
        }
        return false;
    }

    static String applyProfile(Context context, String name) {
        SharedPreferences p = prefs(context);
        String raw = p.getString("profile:" + name, "");
        String[] parts = raw.split("\\|", -1);
        if (parts.length < 4) return "Профиль не найден: " + name;
        int zone = parseInt(parts[1], EcarxVehicleAdapter.ZONE_DRIVER_LEFT);
        int memory = parseInt(parts[2], EcarxVehicleAdapter.SEAT_POSITION_1);
        String preset = parts[3];
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        StringBuilder sb = new StringBuilder("Профиль: ").append(name).append("\n");
        sb.append(adapter.set(EcarxVehicleAdapter.SEAT_POSITION_SET, zone, memory).message).append("\n");
        if (!preset.trim().isEmpty()) sb.append(runPreset(context, preset));
        p.edit().putString(KEY_ACTIVE_PROFILE, name).apply();
        return sb.toString();
    }

    static String saveCurrentSeatProfile(Context context, String name, int zone, int memory, String preset) {
        EcarxVehicleAdapter.Result save = new EcarxVehicleAdapter(context)
                .set(EcarxVehicleAdapter.SEAT_POSITION_SAVE, zone, memory);
        SharedPreferences p = prefs(context);
        ArrayList<String> order = new ArrayList<>(names(p, KEY_PROFILE_ORDER));
        if (!order.contains(name)) order.add(name);
        p.edit()
                .putString("profile:" + name, name + "|" + zone + "|" + memory + "|" + preset)
                .putString(KEY_PROFILE_ORDER, join(order))
                .apply();
        return save.message + "\nПрофиль сохранен локально: " + name;
    }

    static String runSmartClimate(Context context) {
        SharedPreferences p = prefs(context);
        float cabin = p.getFloat(KEY_CABIN_TEMP, 26.0f);
        float outside = p.getFloat(KEY_OUTSIDE_TEMP, cabin);
        float target = p.getFloat(KEY_SMART_TARGET, 22.0f);
        float hot = p.getFloat(KEY_SMART_HOT, 27.0f);
        float cold = p.getFloat(KEY_SMART_COLD, 8.0f);
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        ArrayList<String> out = new ArrayList<>();
        out.add("Smart climate cabin=" + cabin + " outside=" + outside + " target=" + target);
        out.add(adapter.set(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON).message);
        if (cabin >= hot || outside >= hot) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, Math.max(18.0f, target)).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, Math.max(18.0f, target)).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, cabin - target > 4 ? EcarxVehicleAdapter.FAN_SPEED_5 : EcarxVehicleAdapter.FAN_SPEED_3).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, cabin - target > 2 ? EcarxVehicleAdapter.SEAT_LEVEL_2 : EcarxVehicleAdapter.SEAT_LEVEL_1).message);
        } else if (cabin <= cold || outside <= cold) {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, Math.min(26.0f, target + 3.0f)).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_4).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID).message);
        } else {
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON).message);
            out.add(adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, target).message);
            out.add(adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO).message);
        }
        return joinLines(out);
    }

    static void runSmartClimateIfEnabled(Context context) {
        if (prefs(context).getBoolean(KEY_SMART_CLIMATE, false)) runSmartClimate(context);
    }

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static List<String> names(SharedPreferences p, String key) {
        ArrayList<String> result = new ArrayList<>();
        for (String item : p.getString(key, "").split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    static CommandPlan decodePlan(String raw) {
        ArrayList<EcarxVehicleAdapter.Command> commands = new ArrayList<>();
        ArrayList<FloatCommand> floats = new ArrayList<>();
        ArrayList<Action> actions = new ArrayList<>();
        for (String line : raw.split("\\n")) {
            String item = line.trim();
            if (item.isEmpty() || item.startsWith("#")) continue;
            if (item.startsWith("action:")) {
                String[] sides = item.substring("action:".length()).split("=", 2);
                actions.add(new Action(sides[0].trim(), sides.length > 1 ? sides[1].trim() : ""));
                continue;
            }
            boolean isFloat = item.startsWith("float:");
            if (isFloat) item = item.substring("float:".length());
            String[] sides = item.split("=", 2);
            if (sides.length != 2) continue;
            String[] left = sides[0].split("/", 2);
            int functionId = parseInt(left[0], 0);
            int zone = left.length > 1 ? parseInt(left[1], 0) : 0;
            if (functionId == 0) continue;
            if (isFloat) floats.add(new FloatCommand(functionId, zone, parseFloat(sides[1], 0.0f)));
            else commands.add(new EcarxVehicleAdapter.Command(functionId, zone, parseInt(sides[1], 0)));
        }
        return new CommandPlan(commands.toArray(new EcarxVehicleAdapter.Command[0]), floats.toArray(new FloatCommand[0]), actions.toArray(new Action[0]));
    }

    static int parseInt(String value, int fallback) {
        try {
            return value.trim().startsWith("0x") ? (int) Long.parseLong(value.trim().substring(2), 16) : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim().replace(',', '.'));
        } catch (Exception e) {
            return fallback;
        }
    }

    static String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) sb.append(",");
            sb.append(item);
        }
        return sb.toString();
    }

    private static String joinLines(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        return sb.toString();
    }

    static final class CommandPlan {
        final EcarxVehicleAdapter.Command[] commands;
        final FloatCommand[] floatCommands;
        final Action[] actions;

        CommandPlan(EcarxVehicleAdapter.Command[] commands, FloatCommand[] floatCommands, Action[] actions) {
            this.commands = commands;
            this.floatCommands = floatCommands;
            this.actions = actions;
        }
    }

    static final class Action {
        final String name;
        final String value;

        Action(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    static final class FloatCommand {
        final int functionId;
        final int zone;
        final float value;

        FloatCommand(int functionId, int zone, float value) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
        }
    }
}
