package com.prodject.gflow;

import android.content.*;
import android.media.AudioManager;
import java.util.*;

final class UserProfileEngine {
    static final String PREFS = "user_profiles";
    static final String KEY_DRIVER_ORDER = "driver_order";
    static final String KEY_PASSENGER_ORDER = "passenger_order";
    static final String KEY_LAST_USED = "last_used";
    static final String KEY_LAST_APPLIED_AT = "last_applied_at";

    private UserProfileEngine() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static List<String> names(Context context, String type) {
        return AutomationEngine.names(prefs(context), "passenger".equals(type) ? KEY_PASSENGER_ORDER : KEY_DRIVER_ORDER);
    }

    static String save(Context context, String oldName, String name, String type, String identity, String body) {
        return save(context, oldName, name, type, "D1", identity, body);
    }

    static String save(Context context, String oldName, String name, String type, String avatar, String identity, String body) {
        String clean = name.trim();
        if (clean.isEmpty()) return "Имя профиля пустое";
        String orderKey = "passenger".equals(type) ? KEY_PASSENGER_ORDER : KEY_DRIVER_ORDER;
        SharedPreferences p = prefs(context);
        ArrayList<String> order = new ArrayList<>(AutomationEngine.names(p, orderKey));
        if (!oldName.isEmpty() && !oldName.equals(clean)) order.remove(oldName);
        if (!order.contains(clean)) order.add(clean);
        String encoded = "name:" + clean + "\n"
                + "type:" + type + "\n"
                + "avatar:" + avatar.trim() + "\n"
                + "identity:" + identity.trim() + "\n"
                + body.trim();
        SharedPreferences.Editor editor = p.edit()
                .putString("profile2:" + clean, encoded)
                .putString(orderKey, AutomationEngine.join(order));
        if (!oldName.isEmpty() && !oldName.equals(clean)) editor.remove("profile2:" + oldName);
        editor.apply();
        return "Профиль сохранен: " + clean;
    }

    static void delete(Context context, String name, String type) {
        String orderKey = "passenger".equals(type) ? KEY_PASSENGER_ORDER : KEY_DRIVER_ORDER;
        SharedPreferences p = prefs(context);
        ArrayList<String> order = new ArrayList<>(AutomationEngine.names(p, orderKey));
        order.remove(name);
        p.edit().remove("profile2:" + name).putString(orderKey, AutomationEngine.join(order)).apply();
    }

    static String raw(Context context, String name) {
        return prefs(context).getString("profile2:" + name, "");
    }

    static String apply(Context context, String name) {
        Profile profile = Profile.parse(raw(context, name));
        if (profile.name.length() == 0) return "Профиль не найден: " + name;
        EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(context);
        StringBuilder sb = new StringBuilder("Профиль ").append(profile.type).append(": ").append(profile.name).append("\n");
        for (String line : profile.commands) sb.append(runLine(context, adapter, line)).append("\n");
        prefs(context).edit()
                .putString(KEY_LAST_USED, profile.name)
                .putLong(KEY_LAST_APPLIED_AT, System.currentTimeMillis())
                .apply();
        AutomationEngine.prefs(context).edit().putString(AutomationEngine.KEY_ACTIVE_PROFILE, profile.name).apply();
        return sb.toString();
    }

    static String matchIdentity(Context context, String kind, String value) {
        String token = kind + "=" + value;
        for (String type : new String[]{"driver", "passenger"}) {
            for (String name : names(context, type)) {
                Profile profile = Profile.parse(raw(context, name));
                if (profile.identity.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT))) return name;
            }
        }
        return "";
    }

    static String defaultDriverBody() {
        return "# Сиденье водительское\n"
                + "seatMemory:driver:1\n"
                + "seatLength:driver:save1\n"
                + "seatHeight:driver:save1\n"
                + "seatBackrest:driver:save1\n"
                + "# Климат и комфорт\n"
                + "climateTemp:driver:22.0\n"
                + "climateTemp:passenger:22.0\n"
                + "fan:3\n"
                + "seatHeat:driver:2\n"
                + "seatVent:driver:0\n"
                + "# Автомобиль\n"
                + "drive:comfort\n"
                + "steering:soft\n"
                + "hud:on\n"
                + "brightness:night\n"
                + "ambience:blue\n"
                + "volume:8\n"
                + "desktopPins:com.prodject.gflow\n"
                + "buttonPreset:Driver quick|77|double||always|replace|preset|Welcome drive\n"
                + "adas:aeb:on\n";
    }

    static String defaultPassengerBody() {
        return "# Пассажирский комфорт\n"
                + "seatMemory:passenger:1\n"
                + "climateTemp:passenger:22.0\n"
                + "seatHeat:passenger:1\n"
                + "seatVent:passenger:0\n"
                + "hud:on\n"
                + "ambience:ice\n"
                + "volume:6\n";
    }

    static String captureBody(Context context, String type, Set<String> settings) {
        LinkedHashSet<String> include = new LinkedHashSet<>();
        if (settings != null) include.addAll(settings);
        if (include.isEmpty()) {
            include.add("seat");
            include.add("climate");
            include.add("comfort");
            include.add("drive");
            include.add("hud");
            include.add("cabin");
            include.add("media");
            include.add("desktop");
            include.add("automation");
            include.add("adas");
        }
        ArrayList<String> lines = new ArrayList<>();
        boolean passenger = "passenger".equals(type);
        String zone = passenger ? "passenger" : "driver";
        if (include.contains("seat")) {
            lines.add("seatMemory:" + zone + ":1");
            if (!passenger) {
                lines.add("seatLength:driver:save1");
                lines.add("seatHeight:driver:save1");
                lines.add("seatBackrest:driver:save1");
            }
            lines.add("mirror:" + zone);
        }
        if (include.contains("climate")) {
            SharedPreferences smart = SmartClimateController.prefs(context);
            float temp = smart.getFloat(passenger ? SmartClimateController.KEY_PASSENGER_TARGET : SmartClimateController.KEY_DRIVER_TARGET, 22.0f);
            lines.add("climateTemp:" + zone + ":" + String.format(Locale.US, "%.1f", temp));
            if (!passenger) {
                float other = smart.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, temp);
                lines.add("climateTemp:passenger:" + String.format(Locale.US, "%.1f", other));
            }
            lines.add("fan:3");
        }
        if (include.contains("comfort")) {
            lines.add("seatHeat:" + zone + ":" + (passenger ? "1" : "2"));
            lines.add("seatVent:" + zone + ":0");
        }
        if (include.contains("drive") && !passenger) {
            lines.add("drive:comfort");
            lines.add("steering:soft");
        }
        if (include.contains("hud")) lines.add("hud:on");
        if (include.contains("cabin")) {
            lines.add("brightness:night");
            lines.add("ambience:" + (passenger ? "ice" : "blue"));
        }
        if (include.contains("media")) {
            lines.add("volume:" + (passenger ? "6" : "8"));
            lines.add("mediaSource:resume");
        }
        if (include.contains("desktop")) {
            String pins = context.getSharedPreferences("com.prodject.gflow.DesktopActivity", Context.MODE_PRIVATE)
                    .getString("pinned_order", "com.prodject.gflow");
            lines.add("desktopPins:" + pins.replace("\n", ","));
        }
        if (include.contains("automation") && !passenger) {
            String preset = AutomationStore.firstPreset(context);
            if (preset != null && !preset.trim().isEmpty()) lines.add("preset:" + preset.trim());
        }
        if (include.contains("adas") && !passenger) lines.add("adas:aeb:on");
        return AutomationEngine.join(lines);
    }

    static String updateFromCurrent(Context context, String oldName, String name, String type, String avatar, String identity, Set<String> settings) {
        return save(context, oldName, name, type, avatar, identity, captureBody(context, type, settings));
    }

    private static String runLine(Context context, EcarxVehicleAdapter adapter, String raw) {
        String line = raw.trim();
        if (line.isEmpty() || line.startsWith("#")) return "";
        String[] p = line.split(":", -1);
        try {
            if ("seatMemory".equals(p[0])) {
                int zone = zone(p.length > 1 ? p[1] : "driver");
                int memory = "2".equals(p.length > 2 ? p[2] : "1") ? EcarxVehicleAdapter.SEAT_POSITION_2 : EcarxVehicleAdapter.SEAT_POSITION_1;
                return adapter.set(EcarxVehicleAdapter.SEAT_POSITION_SET, zone, memory).message;
            }
            if ("seatLength".equals(p[0])) return seatMove(adapter, EcarxVehicleAdapter.SEAT_LENGTH, p);
            if ("seatHeight".equals(p[0])) return seatMove(adapter, EcarxVehicleAdapter.SEAT_HEIGHT, p);
            if ("seatBackrest".equals(p[0])) return seatMove(adapter, EcarxVehicleAdapter.SEAT_BACKREST, p);
            if ("mirror".equals(p[0])) return adapter.set(EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, zone(p.length > 1 ? p[1] : "driver"), EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE).message;
            if ("climateTemp".equals(p[0])) return adapter.setFloat(EcarxVehicleAdapter.HVAC_TEMP, zone(p.length > 1 ? p[1] : "driver"), AutomationEngine.parseFloat(p.length > 2 ? p[2] : "22", 22f)).message;
            if ("fan".equals(p[0])) return adapter.set(EcarxVehicleAdapter.HVAC_FAN_SPEED, fanValue(p.length > 1 ? p[1] : "3")).message;
            if ("seatHeat".equals(p[0])) return adapter.set(EcarxVehicleAdapter.HVAC_SEAT_HEATING, zone(p.length > 1 ? p[1] : "driver"), seatLevel(p.length > 2 ? p[2] : "0")).message;
            if ("seatVent".equals(p[0])) return adapter.set(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, zone(p.length > 1 ? p[1] : "driver"), seatLevel(p.length > 2 ? p[2] : "0")).message;
            if ("drive".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_MODE_SELECT, driveValue(p.length > 1 ? p[1] : "comfort")).message;
            if ("steering".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_STEERING_MODE, "dynamic".equals(value(p, 1)) ? EcarxVehicleAdapter.STEERING_MODE_DYNAMIC : EcarxVehicleAdapter.STEERING_MODE_SOFT).message;
            if ("drivePropulsion".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION, propulsionValue(value(p, 1))).message;
            if ("driveSuspension".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION, suspensionValue(value(p, 1))).message;
            if ("driveSteeringFeel".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL, steeringFeelValue(value(p, 1))).message;
            if ("driveClimate".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE, climateValue(value(p, 1))).message;
            if ("driveEnergy".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_ENERGY_MODE, energyValue(value(p, 1))).message;
            if ("driveDimTheme".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DRIVE_DIM_THEME_SET, dimThemeValue(value(p, 1))).message;
            if ("hud".equals(p[0])) return adapter.set(EcarxVehicleAdapter.HUD_ACTIVE, "off".equals(value(p, 1)) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON).message;
            if ("brightness".equals(p[0])) return adapter.set(EcarxVehicleAdapter.DAYMODE_SETTING, "day".equals(value(p, 1)) ? EcarxVehicleAdapter.DAYMODE_VALUE_DAY : EcarxVehicleAdapter.DAYMODE_VALUE_NIGHT).message;
            if ("ambience".equals(p[0])) return adapter.set(EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR, ambienceValue(value(p, 1))).message;
            if ("volume".equals(p[0])) return setVolume(context, AutomationEngine.parseInt(value(p, 1), 8));
            if ("desktopPins".equals(p[0])) return setDesktopPins(context, line.substring("desktopPins:".length()));
            if ("buttonPreset".equals(p[0])) return saveButton(context, line.substring("buttonPreset:".length()));
            if ("preset".equals(p[0])) return AutomationEngine.runPreset(context, line.substring("preset:".length()).trim());
            if ("scenario".equals(p[0])) return AutomationEngine.runScenario(context, line.substring("scenario:".length()).trim(), "profile", "apply");
            if ("adas".equals(p[0])) return adas(adapter, p);
            if ("mediaSource".equals(p[0])) return "Media source marker saved: " + value(p, 1);
            return "Unknown profile setting: " + line;
        } catch (Exception e) {
            return "Profile setting failed: " + line + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static String seatMove(EcarxVehicleAdapter adapter, int functionId, String[] p) {
        int z = zone(p.length > 1 ? p[1] : "driver");
        String value = p.length > 2 ? p[2] : "save1";
        if (value.startsWith("save")) return "Seat axis uses memory profile: " + value;
        int command = EcarxVehicleAdapter.SEAT_FORWARD;
        if (functionId == EcarxVehicleAdapter.SEAT_HEIGHT) command = "down".equals(value) ? EcarxVehicleAdapter.SEAT_HEIGHT_DOWN : EcarxVehicleAdapter.SEAT_HEIGHT_UP;
        else if (functionId == EcarxVehicleAdapter.SEAT_BACKREST) command = "backward".equals(value) ? EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD : EcarxVehicleAdapter.SEAT_BACKREST_FORWARD;
        else command = "backward".equals(value) ? EcarxVehicleAdapter.SEAT_BACKWARD : EcarxVehicleAdapter.SEAT_FORWARD;
        return adapter.set(functionId, z, command).message;
    }

    private static int zone(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("pass")) return EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT;
        if (v.contains("2l")) return EcarxVehicleAdapter.ZONE_ROW_2_LEFT;
        if (v.contains("2r")) return EcarxVehicleAdapter.ZONE_ROW_2_RIGHT;
        return EcarxVehicleAdapter.ZONE_DRIVER_LEFT;
    }

    private static int seatLevel(String value) {
        int level = AutomationEngine.parseInt(value, 0);
        if (level <= 0) return EcarxVehicleAdapter.COMMON_OFF;
        if (level == 1) return EcarxVehicleAdapter.SEAT_LEVEL_1;
        if (level == 3) return EcarxVehicleAdapter.SEAT_LEVEL_3;
        return EcarxVehicleAdapter.SEAT_LEVEL_2;
    }

    private static int fanValue(String value) {
        int level = AutomationEngine.parseInt(value, 3);
        int[] values = {EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.FAN_SPEED_1, EcarxVehicleAdapter.FAN_SPEED_2, EcarxVehicleAdapter.FAN_SPEED_3, EcarxVehicleAdapter.FAN_SPEED_4, EcarxVehicleAdapter.FAN_SPEED_5, EcarxVehicleAdapter.FAN_SPEED_6, EcarxVehicleAdapter.FAN_SPEED_7, EcarxVehicleAdapter.FAN_SPEED_8, EcarxVehicleAdapter.FAN_SPEED_9};
        return values[Math.max(0, Math.min(values.length - 1, level))];
    }

    private static int driveValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("eco")) return EcarxVehicleAdapter.DRIVE_MODE_ECO;
        if (v.contains("pure")) return EcarxVehicleAdapter.DRIVE_MODE_PURE;
        if (v.contains("hybrid")) return EcarxVehicleAdapter.DRIVE_MODE_HYBRID;
        if (v.contains("power")) return EcarxVehicleAdapter.DRIVE_MODE_POWER;
        if (v.contains("dynamic") || v.contains("sport")) return EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC;
        if (v.contains("snow")) return EcarxVehicleAdapter.DRIVE_MODE_SNOW;
        if (v.contains("mud")) return EcarxVehicleAdapter.DRIVE_MODE_MUD;
        if (v.contains("rock")) return EcarxVehicleAdapter.DRIVE_MODE_ROCK;
        if (v.contains("sand")) return EcarxVehicleAdapter.DRIVE_MODE_SAND;
        if (v.contains("awd")) return EcarxVehicleAdapter.DRIVE_MODE_AWD;
        if (v.contains("save")) return EcarxVehicleAdapter.DRIVE_MODE_SAVE;
        if (v.contains("adaptive")) return EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE;
        if (v.contains("custom")) return EcarxVehicleAdapter.DRIVE_MODE_CUSTOM;
        if (v.contains("hdc")) return EcarxVehicleAdapter.DRIVE_MODE_HDC;
        if (v.contains("offroad")) return EcarxVehicleAdapter.DRIVE_MODE_OFFROAD;
        return EcarxVehicleAdapter.DRIVE_MODE_COMFORT;
    }

    private static int propulsionValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("eco")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO;
        if (v.contains("comfort")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT;
        if (v.contains("sport") || v.contains("dynamic")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_SPORT;
        if (v.contains("offroad")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD;
        if (v.contains("snow")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_SNOW;
        if (v.contains("sand")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND;
        if (v.contains("hybrid")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID;
        if (v.contains("pure")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE;
        if (v.contains("power")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_POWER;
        if (v.contains("awd")) return EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD;
        return EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT;
    }

    private static int suspensionValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("comfort")) return EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT;
        if (v.contains("sport")) return EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT;
        if (v.contains("offroad")) return EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD;
        if (v.contains("snow")) return EcarxVehicleAdapter.CUSTOM_SUSPENSION_SNOW;
        if (v.contains("auto")) return EcarxVehicleAdapter.CUSTOM_SUSPENSION_AUTOMATIC;
        return EcarxVehicleAdapter.CUSTOM_SUSPENSION_STANDARD;
    }

    private static int steeringFeelValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("light")) return EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT;
        if (v.contains("heavy")) return EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY;
        return EcarxVehicleAdapter.CUSTOM_STEERING_BALANCED;
    }

    private static int climateValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("eco")) return EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO;
        return EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL;
    }

    private static int energyValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("sport")) return EcarxVehicleAdapter.ENERGY_MODE_SPORT;
        if (v.contains("tour")) return EcarxVehicleAdapter.ENERGY_MODE_TOUR;
        return EcarxVehicleAdapter.ENERGY_MODE_RANGE;
    }

    private static int dimThemeValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("off")) return EcarxVehicleAdapter.COMMON_OFF;
        if (v.contains("gold")) return EcarxVehicleAdapter.DIM_THEME_GOLD;
        if (v.contains("blue")) return EcarxVehicleAdapter.DIM_THEME_BLUE;
        return EcarxVehicleAdapter.DIM_THEME_RED;
    }

    private static int ambienceValue(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("red")) return EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_RED;
        if (v.contains("green")) return EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_GREEN;
        if (v.contains("ice")) return EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ICE_BLUE;
        if (v.contains("white")) return EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WHITE;
        return EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_BLUE;
    }

    private static String setVolume(Context context, int value) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audio == null) return "AudioManager unavailable";
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.max(0, Math.min(max, value)), 0);
        return "Music volume=" + value;
    }

    private static String setDesktopPins(Context context, String pins) {
        context.getSharedPreferences("com.prodject.gflow.DesktopActivity", Context.MODE_PRIVATE)
                .edit().putString("pinned_order", pins.replace(",", "\n")).apply();
        return "Desktop pins saved";
    }

    private static String saveButton(Context context, String raw) {
        String[] p = raw.split("\\|", -1);
        String name = p.length > 0 ? p[0] : "Profile button";
        SharedPreferences prefs = AutomationEngine.prefs(context);
        ArrayList<String> order = new ArrayList<>(AutomationEngine.names(prefs, AutomationEngine.KEY_BUTTON_ORDER));
        if (!order.contains(name)) order.add(name);
        prefs.edit().putString("button2:" + name, raw).putString(AutomationEngine.KEY_BUTTON_ORDER, AutomationEngine.join(order)).apply();
        return "Button binding saved: " + name;
    }

    private static String adas(EcarxVehicleAdapter adapter, String[] p) {
        String key = value(p, 1);
        int functionId = "fcw".equals(key) ? EcarxVehicleAdapter.ADAS_FCW : EcarxVehicleAdapter.ADAS_AEB;
        return adapter.set(functionId, "off".equals(value(p, 2)) ? EcarxVehicleAdapter.COMMON_OFF : EcarxVehicleAdapter.COMMON_ON).message;
    }

    private static String value(String[] p, int index) {
        return p.length > index ? p[index].trim().toLowerCase(Locale.ROOT) : "";
    }

    static final class Profile {
        String name = "";
        String type = "driver";
        String avatar = "";
        String identity = "";
        final ArrayList<String> commands = new ArrayList<>();

        static Profile parse(String raw) {
            Profile profile = new Profile();
            for (String line : raw.split("\\n")) {
                String item = line.trim();
                if (item.isEmpty()) continue;
                if (item.startsWith("name:")) profile.name = item.substring("name:".length()).trim();
                else if (item.startsWith("type:")) profile.type = item.substring("type:".length()).trim();
                else if (item.startsWith("avatar:")) profile.avatar = item.substring("avatar:".length()).trim();
                else if (item.startsWith("identity:")) profile.identity = item.substring("identity:".length()).trim();
                else profile.commands.add(item);
            }
            return profile;
        }
    }
}
