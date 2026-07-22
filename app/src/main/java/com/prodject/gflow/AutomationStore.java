package com.prodject.gflow;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

final class AutomationStore {
    private AutomationStore() {}

    static void savePreset(Context context, String oldName, String newName, String body) {
        saveNamed(context, AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName, newName, body);
    }

    static void saveNamed(Context context, String orderKey, String prefix, String oldName, String newName, String value) {
        String clean = newName == null ? "" : newName.trim();
        if (clean.isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(context);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        if (!oldName.isEmpty() && !oldName.equals(clean)) names.remove(oldName);
        if (!names.contains(clean)) names.add(clean);
        SharedPreferences.Editor editor = prefs.edit()
                .putString(prefix + clean, value)
                .putString(orderKey, AutomationEngine.join(names));
        if (!oldName.isEmpty() && !oldName.equals(clean)) editor.remove(prefix + oldName);
        editor.apply();
    }

    static void deleteNamed(Context context, String orderKey, String prefix, String name) {
        if (name == null || name.trim().isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(context);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        names.remove(name);
        prefs.edit().remove(prefix + name).putString(orderKey, AutomationEngine.join(names)).apply();
    }

    static String firstPreset(Context context) {
        List<String> names = AutomationEngine.names(AutomationEngine.prefs(context), AutomationEngine.KEY_PRESET_ORDER);
        return names.isEmpty() ? "" : names.get(0);
    }

    static String defaultPresetBody() {
        return "0x10010100/0=0x1\n0x10010300/0=0x1\n0x10020100/0=0x10020103\nfloat:0x10060100/1=22.0\nfloat:0x10060100/4=22.0";
    }

    static String defaultScenarioBody() {
        return "name:Morning comfort\n"
                + "trigger:manual=morning\n"
                + "condition:time=06:00..10:00\n"
                + "condition:profile=Глеб\n"
                + "policy:startDelay=10s\n"
                + "policy:minInterval=30m\n"
                + "policy:oncePerTrip=true\n"
                + "policy:cancelOnConditionChange=true\n"
                + "step:action smart_climate=true\n"
                + "step:delay 5m\n"
                + "step:command 0x10020100/0=0x10020102\n"
                + "step:notify Сценарий завершен";
    }

    static void installClimateScenarios(Context context) {
        saveNamed(context, AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Зимний запуск",
                "name:Зимний запуск\n"
                        + "trigger:manual=winter\n"
                        + "trigger:boot=BOOT_COMPLETED\n"
                        + "condition:outsideTemp<5\n"
                        + "policy:minInterval=30m\n"
                        + "policy:oncePerTrip=true\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Зимний запуск\n"
                        + "step:action smart_climate=true\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command float:0x10060100/1=22.0\n"
                        + "step:command float:0x10060100/4=22.0\n"
                        + "step:command 0x10070100/0=0x10070106\n"
                        + "step:command 0x10040100/0=0x1\n"
                        + "step:command 0x10090100/0=0x10090203\n"
                        + "step:command 0x10050200/1=0x10050303\n"
                        + "step:delay 5m\n"
                        + "step:command 0x10020100/0=0x10020102\n"
                        + "step:wait cabinTemp>=18 timeout=10m\n"
                        + "step:command 0x10050200/1=0x10050301");
        saveNamed(context, AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Летнее охлаждение",
                "name:Летнее охлаждение\n"
                        + "trigger:manual=summer\n"
                        + "condition:cabinTemp>25\n"
                        + "policy:minInterval=30m\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Летнее охлаждение\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command 0x10010400/0=0x1\n"
                        + "step:command float:0x10060100/1=18.0\n"
                        + "step:command float:0x10060100/4=18.0\n"
                        + "step:command 0x10030100/0=0x10030101\n"
                        + "step:command 0x10020100/0=0x10020108\n"
                        + "step:command 0x10050100/1=0x10050302\n"
                        + "step:wait cabinTemp<=25 timeout=10m\n"
                        + "step:command 0x10020100/0=0x10020103\n"
                        + "step:wait cabinTemp<=22 timeout=15m\n"
                        + "step:command 0x10010200/0=0x1");
    }

    static void installWelcomeLeave(Context context) {
        savePreset(context, "", "Welcome drive",
                "action:profile=Driver\n"
                        + "0x10010100/0=0x1\n"
                        + "0x10010200/0=0x1\n"
                        + "float:0x10060100/1=22.0\n"
                        + "float:0x10060100/4=22.0\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010206\n"
                        + "0x2a080100/0=0x2a080103\n"
                        + "0x22010100/0=0x22010102");
        savePreset(context, "", "Leave car",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x10010100/0=0x0\n"
                        + "0x2a010100/0=0x0\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Welcome manual", "Welcome manual|manual|welcome|Welcome drive");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Leave manual", "Leave manual|manual|leave|Leave car");
    }

    static void installParkingGuard(Context context) {
        savePreset(context, "", "Parking guard",
                "action:start_dvr=true\n"
                        + "0x21110100/0=0x1\n"
                        + "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Parking manual", "Parking manual|manual|parking|Parking guard");
    }

    static void installRain(Context context) {
        savePreset(context, "", "Rain safe",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21010100/0=0x21010101\n"
                        + "0x10040100/0=0x1");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Rain manual", "Rain manual|manual|rain|Rain safe");
    }

    static void installNightMode(Context context) {
        savePreset(context, "", "Night mode",
                "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x20150100/0=0x20150102\n"
                        + "0x29020100/0=0x1\n"
                        + "0x29020500/0=0x1\n"
                        + "0x22040200/0=0x22040203\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010205\n"
                        + "0x2a080100/0=0x2a080103");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Night manual", "Night manual|manual|night|Night mode");
    }

    static void installNavigationContext(Context context) {
        savePreset(context, "", "Navigation context",
                "action:autozoom=maps,navi,navitel,yandex,2gis:1.18\n"
                        + "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x21110100/0=0x2\n"
                        + "0x21110100/0=0x3");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app", "Navigation app|app|maps|Navigation context");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app yandex", "Navigation app yandex|app|yandex|Navigation context");
        saveNamed(context, AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app 2gis", "Navigation app 2gis|app|2gis|Navigation context");
    }
}
