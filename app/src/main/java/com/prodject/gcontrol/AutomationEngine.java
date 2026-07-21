package com.prodject.gcontrol;

import android.content.*;
import android.os.Build;
import java.util.*;

final class AutomationEngine {
    static final String PREFS = "automation";
    static final String KEY_PRESET_ORDER = "preset_order";
    static final String KEY_TRIGGER_ORDER = "trigger_order";
    static final String KEY_SCENARIO_ORDER = "scenario_order";
    static final String KEY_LOG = "execution_log";
    static final String KEY_TRIP_ID = "trip_id";
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

    static String runScenario(Context context, String name, String triggerType, String triggerValue) {
        Scenario scenario = decodeScenario(prefs(context).getString("scenario:" + name, ""));
        if (scenario.name.length() == 0) return "Scenario not found: " + name;
        RunDecision decision = canRun(context, scenario, triggerType, triggerValue);
        if (!decision.allowed) {
            String skipped = "SKIP " + scenario.name + ": " + decision.reason;
            appendLog(context, skipped);
            return skipped;
        }
        StringBuilder sb = new StringBuilder("Scenario: ").append(scenario.name).append("\n");
        prefs(context).edit()
                .putLong("scenario_last:" + scenario.name, System.currentTimeMillis())
                .putString("scenario_trip:" + scenario.name, prefs(context).getString(KEY_TRIP_ID, "default"))
                .apply();
        appendLog(context, "START " + scenario.name + " trigger=" + triggerType + ":" + triggerValue);
        if (scenario.startDelayMillis > 0) {
            long ms = Math.min(scenario.startDelayMillis, 15000L);
            appendLog(context, scenario.name + " start delay " + ms + "ms");
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appendLog(context, "CANCEL " + scenario.name + ": start delay interrupted");
                return "Scenario interrupted during start delay: " + scenario.name;
            }
        }
        for (ScenarioStep step : scenario.steps) {
            if (scenario.cancelOnConditionChange && !conditionsPass(context, scenario.conditions).allowed) {
                String cancel = "CANCEL " + scenario.name + ": conditions changed";
                sb.append(cancel).append("\n");
                appendLog(context, cancel);
                break;
            }
            String result = runStep(context, step);
            sb.append(result).append("\n");
            appendLog(context, scenario.name + " step " + step.kind + ": " + compactLine(result));
        }
        appendLog(context, "END " + scenario.name);
        return sb.toString();
    }

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
        if ("user_profile".equals(action.name)) return UserProfileEngine.apply(context, action.value);
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
        for (String name : names(p, KEY_SCENARIO_ORDER)) {
            Scenario scenario = decodeScenario(p.getString("scenario:" + name, ""));
            if (scenario.matchesTrigger(type, value)) runScenario(context, name, type, value);
        }
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
        return runSteering(context, keyCode, gesture, "", "").replaceStock;
    }

    static SteeringResult runSteering(Context context, int keyCode, String gesture, String modifier, String foregroundPackage) {
        SharedPreferences p = prefs(context);
        String target = keyCode + ":" + gesture;
        for (String name : names(p, KEY_BUTTON_ORDER)) {
            String raw = p.getString("button2:" + name, "");
            ButtonBinding binding = ButtonBinding.parse(raw);
            if (!binding.matches(context, keyCode, gesture, modifier, foregroundPackage)) continue;
            String result = runButtonTarget(context, binding);
            appendLog(context, "BUTTON " + name + " " + result);
            return new SteeringResult(binding.replacesStock(), result);
        }
        for (String name : names(p, KEY_BUTTON_ORDER)) {
            String raw = p.getString("button:" + name, "");
            String[] parts = raw.split("\\|", -1);
            if (parts.length < 4) continue;
            if ((parts[1] + ":" + parts[2]).equals(target)) {
                runPreset(context, parts[3]);
                return new SteeringResult(true, "legacy preset " + parts[3]);
            }
        }
        return new SteeringResult(false, "no binding");
    }

    private static String runButtonTarget(Context context, ButtonBinding binding) {
        if ("preset".equals(binding.targetType)) return runPreset(context, binding.target);
        if ("scenario".equals(binding.targetType)) return runScenario(context, binding.target, "button", binding.keyCode + ":" + binding.gesture);
        if ("action".equals(binding.targetType)) {
            String[] sides = binding.target.split("=", 2);
            return runAction(context, new Action(sides[0].trim(), sides.length > 1 ? sides[1].trim() : ""));
        }
        if ("voice".equals(binding.targetType)) {
            CarCommandBus.send(context, "steering", binding.target);
            return "voice " + binding.target;
        }
        if ("launch".equals(binding.targetType)) return runStep(context, new ScenarioStep("launch", binding.target));
        if ("command".equals(binding.targetType)) return runStep(context, new ScenarioStep("command", binding.target));
        return "unknown target " + binding.targetType + ":" + binding.target;
    }

    static String runStep(Context context, ScenarioStep step) {
        if ("preset".equals(step.kind)) return runPreset(context, step.value);
        if ("action".equals(step.kind)) {
            String[] sides = step.value.split("=", 2);
            return runAction(context, new Action(sides[0].trim(), sides.length > 1 ? sides[1].trim() : ""));
        }
        if ("command".equals(step.kind)) {
            CommandPlan plan = decodePlan(step.value);
            StringBuilder sb = new StringBuilder();
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
            for (EcarxVehicleAdapter.Command command : plan.commands) sb.append(adapter.set(command.functionId, command.zone, command.value).message).append("\n");
            for (FloatCommand command : plan.floatCommands) sb.append(adapter.setFloat(command.functionId, command.zone, command.value).message).append("\n");
            return sb.length() == 0 ? "No command parsed: " + step.value : sb.toString();
        }
        if ("delay".equals(step.kind)) {
            long ms = Math.min(parseDurationMillis(step.value), 15000L);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Delay interrupted";
            }
            return "Delay " + ms + "ms";
        }
        if ("wait".equals(step.kind)) {
            long deadline = System.currentTimeMillis() + Math.min(step.timeoutMillis, 30000L);
            Condition condition = Condition.parse(step.value);
            while (System.currentTimeMillis() < deadline) {
                if (condition.matches(context).allowed) return "Wait condition matched: " + step.value;
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Wait interrupted";
                }
            }
            return "Wait timeout: " + step.value;
        }
        if ("notify".equals(step.kind)) {
            android.widget.Toast.makeText(context, step.value, android.widget.Toast.LENGTH_LONG).show();
            return "Notify: " + step.value;
        }
        if ("voice".equals(step.kind)) {
            CarCommandBus.send(context, "scenario", step.value);
            return "Voice/broadcast command: " + step.value;
        }
        if ("launch".equals(step.kind)) {
            Intent launch = context.getPackageManager().getLaunchIntentForPackage(step.value);
            if (launch == null) return "Launch package not found: " + step.value;
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
            return "Launch: " + step.value;
        }
        return "Unknown step: " + step.kind + "=" + step.value;
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

    static Scenario decodeScenario(String raw) {
        Scenario scenario = new Scenario();
        for (String line : raw.split("\\n")) {
            String item = line.trim();
            if (item.isEmpty() || item.startsWith("#")) continue;
            String[] sides = item.split(":", 2);
            if (sides.length != 2) continue;
            String key = sides[0].trim();
            String value = sides[1].trim();
            if ("name".equals(key)) scenario.name = value;
            else if ("trigger".equals(key)) scenario.triggers.add(value);
            else if ("condition".equals(key)) scenario.conditions.add(Condition.parse(value));
            else if ("policy".equals(key)) scenario.applyPolicy(value);
            else if ("step".equals(key)) scenario.steps.add(ScenarioStep.parse(value));
        }
        return scenario;
    }

    static String scenarioLog(Context context) {
        return prefs(context).getString(KEY_LOG, "");
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

    static long parseDurationMillis(String value) {
        String raw = value.trim().toLowerCase(Locale.ROOT);
        try {
            if (raw.endsWith("ms")) return Long.parseLong(raw.substring(0, raw.length() - 2).trim());
            if (raw.endsWith("s")) return Long.parseLong(raw.substring(0, raw.length() - 1).trim()) * 1000L;
            if (raw.endsWith("m")) return Long.parseLong(raw.substring(0, raw.length() - 1).trim()) * 60_000L;
            return Long.parseLong(raw);
        } catch (Exception e) {
            return 0L;
        }
    }

    private static RunDecision canRun(Context context, Scenario scenario, String triggerType, String triggerValue) {
        if (!scenario.matchesTrigger(triggerType, triggerValue)) return RunDecision.no("trigger mismatch");
        RunDecision conditions = conditionsPass(context, scenario.conditions);
        if (!conditions.allowed) return conditions;
        SharedPreferences p = prefs(context);
        long now = System.currentTimeMillis();
        long last = p.getLong("scenario_last:" + scenario.name, 0L);
        if (scenario.minIntervalMillis > 0 && now - last < scenario.minIntervalMillis) return RunDecision.no("min interval not elapsed");
        if (scenario.oncePerTrip) {
            String trip = p.getString(KEY_TRIP_ID, "default");
            if (trip.equals(p.getString("scenario_trip:" + scenario.name, ""))) return RunDecision.no("already ran this trip");
        }
        return RunDecision.yes();
    }

    private static RunDecision conditionsPass(Context context, List<Condition> conditions) {
        for (Condition condition : conditions) {
            RunDecision decision = condition.matches(context);
            if (!decision.allowed) return decision;
        }
        return RunDecision.yes();
    }

    private static void appendLog(Context context, String line) {
        SharedPreferences p = prefs(context);
        String previous = p.getString(KEY_LOG, "");
        String next = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + " " + line + "\n" + previous;
        if (next.length() > 12000) next = next.substring(0, 12000);
        p.edit().putString(KEY_LOG, next).apply();
    }

    private static String compactLine(String value) {
        String line = value.replace('\n', ' ').trim();
        return line.length() > 180 ? line.substring(0, 180) : line;
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

    static final class Scenario {
        String name = "";
        final ArrayList<String> triggers = new ArrayList<>();
        final ArrayList<Condition> conditions = new ArrayList<>();
        final ArrayList<ScenarioStep> steps = new ArrayList<>();
        long minIntervalMillis;
        long startDelayMillis;
        boolean oncePerTrip;
        boolean cancelOnConditionChange;

        boolean matchesTrigger(String type, String value) {
            if (triggers.isEmpty()) return "manual".equals(type);
            String actual = value == null ? "" : value.toLowerCase(Locale.ROOT);
            for (String trigger : triggers) {
                String[] parts = trigger.split("=", 2);
                String triggerType = parts[0].trim();
                String expected = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "";
                if (triggerType.equals(type) && (expected.isEmpty() || actual.contains(expected))) return true;
            }
            return false;
        }

        void applyPolicy(String value) {
            String[] sides = value.split("=", 2);
            String key = sides[0].trim();
            String v = sides.length > 1 ? sides[1].trim() : "true";
            if ("minInterval".equals(key)) minIntervalMillis = parseDurationMillis(v);
            else if ("startDelay".equals(key)) startDelayMillis = parseDurationMillis(v);
            else if ("oncePerTrip".equals(key)) oncePerTrip = Boolean.parseBoolean(v);
            else if ("cancelOnConditionChange".equals(key)) cancelOnConditionChange = Boolean.parseBoolean(v);
        }
    }

    static final class ScenarioStep {
        final String kind;
        final String value;
        long timeoutMillis = 30000L;

        ScenarioStep(String kind, String value) {
            this.kind = kind;
            this.value = value;
        }

        static ScenarioStep parse(String raw) {
            String[] parts = raw.split(" ", 2);
            String kind = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";
            ScenarioStep step = new ScenarioStep(kind, value);
            if ("wait".equals(kind) && value.contains(" timeout=")) {
                String[] waitParts = value.split(" timeout=", 2);
                step = new ScenarioStep(kind, waitParts[0].trim());
                step.timeoutMillis = parseDurationMillis(waitParts[1].trim());
            }
            return step;
        }
    }

    static final class Condition {
        final String key;
        final String op;
        final String value;

        Condition(String key, String op, String value) {
            this.key = key;
            this.op = op;
            this.value = value;
        }

        static Condition parse(String raw) {
            for (String op : new String[]{">=", "<=", "!=", "=", ">", "<"}) {
                int pos = raw.indexOf(op);
                if (pos > 0) return new Condition(raw.substring(0, pos).trim(), op, raw.substring(pos + op.length()).trim());
            }
            return new Condition(raw.trim(), "=", "true");
        }

        RunDecision matches(Context context) {
            SharedPreferences p = prefs(context);
            if ("profile".equals(key)) return compare(p.getString(KEY_ACTIVE_PROFILE, ""), value);
            if ("cabinTemp".equals(key)) return compareNumber(p.getFloat(KEY_CABIN_TEMP, 0f), op, parseFloat(value, 0f), key);
            if ("outsideTemp".equals(key)) return compareNumber(p.getFloat(KEY_OUTSIDE_TEMP, 0f), op, parseFloat(value, 0f), key);
            if ("time".equals(key)) return matchTime(value);
            if ("weekday".equals(key)) return matchWeekday(value);
            if ("lastApp".equals(key)) return compare(context.getSharedPreferences(AppWatchdogAccessibilityService.PREFS, Context.MODE_PRIVATE).getString(AppWatchdogAccessibilityService.KEY_LAST_PACKAGE, ""), value);
            if ("engine".equals(key) || "gear".equals(key) || "speed".equals(key) || "fuel".equals(key) || "door".equals(key) || "security".equals(key) || "bluetooth".equals(key)) {
                return RunDecision.no("condition needs mapped vehicle signal: " + key);
            }
            return RunDecision.no("unknown condition: " + key);
        }

        private RunDecision compare(String actual, String expected) {
            boolean ok = "!=".equals(op) ? !actual.equals(expected) : actual.equals(expected);
            return ok ? RunDecision.yes() : RunDecision.no(key + " expected " + op + " " + expected + ", actual " + actual);
        }

        private RunDecision compareNumber(float actual, String op, float expected, String label) {
            boolean ok = ("=".equals(op) && actual == expected) || (">".equals(op) && actual > expected) || ("<".equals(op) && actual < expected)
                    || (">=".equals(op) && actual >= expected) || ("<=".equals(op) && actual <= expected) || ("!=".equals(op) && actual != expected);
            return ok ? RunDecision.yes() : RunDecision.no(label + " expected " + op + " " + expected + ", actual " + actual);
        }

        private RunDecision matchTime(String range) {
            String[] parts = range.split("\\.\\.", 2);
            if (parts.length != 2) return RunDecision.no("bad time range: " + range);
            Calendar now = Calendar.getInstance();
            int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            int start = parseClock(parts[0]);
            int end = parseClock(parts[1]);
            boolean ok = start <= end ? minutes >= start && minutes <= end : minutes >= start || minutes <= end;
            return ok ? RunDecision.yes() : RunDecision.no("time outside " + range);
        }

        private int parseClock(String value) {
            String[] parts = value.trim().split(":", 2);
            return parseInt(parts[0], 0) * 60 + (parts.length > 1 ? parseInt(parts[1], 0) : 0);
        }

        private RunDecision matchWeekday(String expected) {
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            String[] names = {"", "sun", "mon", "tue", "wed", "thu", "fri", "sat"};
            return expected.toLowerCase(Locale.ROOT).contains(names[day]) ? RunDecision.yes() : RunDecision.no("weekday mismatch");
        }
    }

    static final class RunDecision {
        final boolean allowed;
        final String reason;

        private RunDecision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        static RunDecision yes() { return new RunDecision(true, "ok"); }
        static RunDecision no(String reason) { return new RunDecision(false, reason); }
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

    static final class SteeringResult {
        final boolean replaceStock;
        final String message;

        SteeringResult(boolean replaceStock, String message) {
            this.replaceStock = replaceStock;
            this.message = message;
        }
    }

    static final class ButtonBinding {
        final String name;
        final int keyCode;
        final String gesture;
        final String modifier;
        final String condition;
        final String behavior;
        final String targetType;
        final String target;

        ButtonBinding(String name, int keyCode, String gesture, String modifier, String condition, String behavior, String targetType, String target) {
            this.name = name;
            this.keyCode = keyCode;
            this.gesture = gesture;
            this.modifier = modifier;
            this.condition = condition;
            this.behavior = behavior;
            this.targetType = targetType;
            this.target = target;
        }

        static ButtonBinding parse(String raw) {
            String[] p = raw.split("\\|", -1);
            return new ButtonBinding(
                    p.length > 0 ? p[0] : "",
                    p.length > 1 ? parseInt(p[1], 0) : 0,
                    p.length > 2 ? p[2] : "press",
                    p.length > 3 ? p[3] : "",
                    p.length > 4 ? p[4] : "always",
                    p.length > 5 ? p[5] : "replace",
                    p.length > 6 ? p[6] : "preset",
                    p.length > 7 ? p[7] : "");
        }

        boolean matches(Context context, int key, String actualGesture, String actualModifier, String foregroundPackage) {
            if (keyCode != key) return false;
            if (!gesture.equals(actualGesture)) return false;
            if (!modifier.isEmpty() && !modifier.equals(actualModifier)) return false;
            if ("hold-only".equals(behavior) && !"hold".equals(actualGesture)) return false;
            return conditionMatches(context, foregroundPackage);
        }

        boolean replacesStock() {
            return "replace".equals(behavior) || "hold-only".equals(behavior) || "stationary-only".equals(behavior);
        }

        private boolean conditionMatches(Context context, String foregroundPackage) {
            if (condition == null || condition.length() == 0 || "always".equals(condition)) return true;
            if ("stationary".equals(condition) || "moving".equals(condition)) {
                return false;
            }
            if (condition.startsWith("app=")) {
                return foregroundPackage != null && foregroundPackage.toLowerCase(Locale.ROOT).contains(condition.substring(4).toLowerCase(Locale.ROOT));
            }
            RunDecision decision = Condition.parse(condition).matches(context);
            return decision.allowed;
        }
    }
}
