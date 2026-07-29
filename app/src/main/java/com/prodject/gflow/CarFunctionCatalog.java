package com.prodject.gflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class CarFunctionCatalog {
    static final int TYPE_INFO = 1;
    static final int TYPE_FUNCTION = 2;
    static final int TYPE_SENSOR = 3;

    static final Map<Integer, Entry> BY_ID;
    static final Map<String, Entry> BY_KEY;

    static {
        LinkedHashMap<Integer, Entry> byId = new LinkedHashMap<>();
        LinkedHashMap<String, Entry> byKey = new LinkedHashMap<>();

        put(byId, byKey, new Entry("BCM_FUNC_ALL_READING_LIGHTS_SWITCH", 554763008, 2, "IBcm.BCM_FUNC_ALL_READING_LIGHTS_SWITCH", "Включение/выключение всех плафонов освещения салона.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_CHARGING_CAP", 553780480, 2, "IBcm.BCM_FUNC_CHARGING_CAP", "Электропривод крышки зарядного порта.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_CHILD_SAFETY_LOCK", 553780224, 2, "IBcm.BCM_FUNC_CHILD_SAFETY_LOCK", "Детский замок задних дверей.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_CUSTOM_KEY", 554762496, 2, "IBcm.BCM_FUNC_CUSTOM_KEY", "Назначение функции на пользовательскую кнопку.", new Value[] {
                new Value("CUSTOM_KEY_TYPE_360_PANORAMA", 1),
                new Value("CUSTOM_KEY_TYPE_AUTO_PARK", 101),
                new Value("CUSTOM_KEY_TYPE_COLLECT_FAV", 5),
                new Value("CUSTOM_KEY_TYPE_DIM_FULL_SCREEN_MAP", 3),
                new Value("CUSTOM_KEY_TYPE_DRIVING_MODE", 102),
                new Value("CUSTOM_KEY_TYPE_DVR", 0),
                new Value("CUSTOM_KEY_TYPE_LOUD_SPEAKER", 99),
                new Value("CUSTOM_KEY_TYPE_NAVIGATION", 2),
                new Value("CUSTOM_KEY_TYPE_REAR_MIRROR_ADJUST", 6),
                new Value("CUSTOM_KEY_TYPE_SOUND_SWITCH", 4),
                new Value("CUSTOM_KEY_TYPE_UNLCKTRUNK", 100),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DIM_ZONE_A_WARNING", 555746816, 2, "IBcm.BCM_FUNC_DIM_ZONE_A_WARNING", "Предупреждение о затемнённой зоне A.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DISPLAY_ONOFF", 554697216, 2, "IBcm.BCM_FUNC_DISPLAY_ONOFF", "Питание подсистем/подсветки кузова: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR", 553779456, 2, "IBcm.BCM_FUNC_DOOR", "Команды электропривода двери (открыть/закрыть/пауза).", new Value[] {
                new Value("DOOR_CLOSE", 0),
                new Value("DOOR_OPEN", 1),
                new Value("DOOR_PAUSE", 553779457),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_LOCK", 553779712, 2, "IBcm.BCM_FUNC_DOOR_LOCK", "Запирание дверей.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_LOCK_FAULT", 553713920, 2, "IBcm.BCM_FUNC_DOOR_LOCK_FAULT", "Неисправность привода замков дверей.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_STATUS", 553785856, 2, "IBcm.BCM_FUNC_DOOR_STATUS", "Состояние дверей (открыто/закрыто).", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_FOLD_REAR_MIRROR", 554041600, 2, "IBcm.BCM_FUNC_FOLD_REAR_MIRROR", "Складирование наружных зеркал.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_FPL_FOLLOW_DRL", 555745536, 2, "IBcm.BCM_FUNC_FPL_FOLLOW_DRL", "Связь ближнего света с ДХО (сценарии).", new Value[] {
                new Value("FPL_FOLLOW_DRL_MODE1", 555745537),
                new Value("FPL_FOLLOW_DRL_MODE2", 555745538),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_FUEL_CAP", 553780736, 2, "IBcm.BCM_FUNC_FUEL_CAP", "Электропривод лючка топливного бака.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS", 553979904, 2, "IBcm.BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS", "Атмосферная подсветка салона.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_FRONT_FOG_LAMPS", 553976832, 2, "IBcm.BCM_FUNC_LIGHT_FRONT_FOG_LAMPS", "Передние противотуманные фары: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_HAZARD_FLASHERS", 553979648, 2, "IBcm.BCM_FUNC_LIGHT_HAZARD_FLASHERS", "Аварийная сигнализация: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_LEFT_TRUN_SIGNAL", 553980160, 2, "IBcm.BCM_FUNC_LIGHT_LEFT_TRUN_SIGNAL", "Левый указатель поворота.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_READING_LIGHT", 553980672, 2, "IBcm.BCM_FUNC_LIGHT_READING_LIGHT", "Индивидуальный плафон освещения салона.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_REAR_FOG_LAMPS", 553977088, 2, "IBcm.BCM_FUNC_LIGHT_REAR_FOG_LAMPS", "Задние противотуманные фонари: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_RIGHT_TRUN_SIGNAL", 553980416, 2, "IBcm.BCM_FUNC_LIGHT_RIGHT_TRUN_SIGNAL", "Правый указатель поворота.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_WELCOME_LIGHT", 553981952, 2, "IBcm.BCM_FUNC_LIGHT_WELCOME_LIGHT", "Приветственная подсветка при подходе/отпирании.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_POWER_ONOFF", 554696960, 2, "IBcm.BCM_FUNC_POWER_ONOFF", "Главный выключатель питания BCM‑подсистем.", new Value[] {
                new Value("BCM_FUNC_POWER_ONOFF_CONFIRM", 554696962),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_REAR_MIRROR_ADJUST", 554041856, 2, "IBcm.BCM_FUNC_REAR_MIRROR_ADJUST", "Электрорегулировка наружных зеркал.", new Value[] {
                new Value("CUSTOM_KEY_TYPE_REAR_MIRROR_ADJUST", 6),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_STEERING_WHEEL_ADJUST", 554107136, 2, "IBcm.BCM_FUNC_STEERING_WHEEL_ADJUST", "Электрорегулировка рулевой колонки.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNCURT_CLS_BTN", 555746560, 2, "IBcm.BCM_FUNC_SUNCURT_OPEN_BTN", "Кнопка закрытия солнцезащитной шторки.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNCURT_OPEN_BTN", 555746304, 2, "IBcm.BCM_FUNC_SUNCURT_OPEN_BTN", "Кнопка открытия солнцезащитной шторки.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNROOF_CLS_BTN", 555746048, 2, "IBcm.BCM_FUNC_SUNROOF_CLS_BTN", "Кнопка закрытия люка.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNROOF_ININ_SWITCH", 555745280, 2, "IBcm.BCM_FUNC_SUNROOF_ININ_SWITCH", "Переключатель направления люка (вперёд/назад).", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNROOF_OPEN_BTN", 555745792, 2, "IBcm.BCM_FUNC_SUNROOF_OPEN_BTN", "Кнопка открытия люка.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SUNROOF_TILT", 553845760, 2, "IBcm.BCM_FUNC_SUNROOF_TILT", "Режим приоткрытия (TILT) люка.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WASHER", 553910528, 2, "IBcm.BCM_FUNC_WASHER", "Омыватель ветрового стекла.", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW", 553844992, 2, "IBcm.BCM_FUNC_WINDOW", "Команды стеклоподъёмника (откр./закр./пауза/процент).", new Value[] {
                new Value("WINDOW_PAUSE", 553844993),
                new Value("WINDOW_HALF", 553844994),
                new Value("WINDOW_OPEN_PAUSE", 553844995),
                new Value("WINDOW_CLOSE_PAUSE", 553844996),
                new Value("WINDOW_CLOSE", 0),
                new Value("WINDOW_OPEN", 1),
                new Value("WINDOW_MIN", 0),
                new Value("WINDOW_MAX", 100),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_CURRENT_POS", 553846272, 2, "IBcm.BCM_FUNC_WINDOW_CURRENT_POS", "Текущее положение стеклоподъёмника, %.", new Value[] {
                new Value("WINDOW_CLOSE", 0),
                new Value("WINDOW_OPEN", 1),
                new Value("WINDOW_MIN", 0),
                new Value("WINDOW_MAX", 100),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_MOVING_STATE", 554762752, 2, "IBcm.BCM_FUNC_WINDOW_MOVING_STATE", "Состояние движения стеклоподъёмника.", new Value[] {
                new Value("WINDOW_CLOSE", 0),
                new Value("WINDOW_OPEN", 1),
                new Value("WINDOW_MIN", 0),
                new Value("WINDOW_MAX", 100),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_POS", 553845504, 2, "IBcm.BCM_FUNC_WINDOW_POS", "Целевая позиция стеклоподъёмника, %.", new Value[] {
                new Value("WINDOW_CLOSE", 0),
                new Value("WINDOW_OPEN", 1),
                new Value("WINDOW_MIN", 0),
                new Value("WINDOW_MAX", 100),
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WIPER", 553713920, 2, "IBcm.BCM_FUNC_WIPER", "Управление дворниками (режим/скорость).", new Value[] {
                new Value("WIPER_GEAR_AUTO", 553713921),
                new Value("WIPER_GEAR_LOW", 553713922),
                new Value("WIPER_GEAR_HIGHT", 553713923),
                new Value("WIPER_GEAR_INTERMITTENT", 553713924),
                new Value("WIPER_GEAR_OFF", 0),
        }));
        put(byId, byKey, new Entry("CAR_MODULE_LAMP", 721420288, 2, "ICarFunction.CAR_MODULE_LAMP", "Выбор режима наружного света (OFF/габариты/ближний/авто/AHBC).", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", 537136641),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_BATTERY_CHARGING_CURRENT_POWER", 606080000, 2, "ICharging.CHARGE_FUNC_BATTERY_CHARGING_CURRENT_POWER", "Текущая мощность зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_BATTERY_DISCHARGING_CURRENT_POWER", 606080256, 2, "ICharging.CHARGE_FUNC_BATTERY_DISCHARGING_CURRENT_POWER", "Текущая мощность раздачи энергии (V2L/V2V).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGE_IMMEDIATELY", 609222656, 2, "ICharging.CHARGE_FUNC_CHARGE_IMMEDIATELY", "Немедленный старт зарядки (обход расписания).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING", 605028608, 2, "ICharging.CHARGE_FUNC_CHARGING", "Статусы процесса зарядки/подключения.", new Value[] {
                new Value("CHARGING_PLUG_STATE_CONNECTED_WAITING", 605225493),
                new Value("CHARGING_PLUG_STATE_DISCONNECTED", 605225489),
                new Value("CHARGING_PLUG_STATE_DIS_CHRGN_CONNECTED", 605225492),
                new Value("CHARGING_PLUG_STATE_FAULT", 605225495),
                new Value("CHARGING_PLUG_STATE_NONE", 605225496),
                new Value("CHARGING_PLUG_STATE_QUICK_CHRGN_CONNECTED", 605225491),
                new Value("CHARGING_PLUG_STATE_SLOW_CHRGN_CONNECTED", 605225490),
                new Value("CHARGING_PLUG_STATE_WRONG_OPERATION", 605225494),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_CURRENT", 605029888, 2, "ICharging.CHARGE_FUNC_CHARGING_CURRENT", "Текущий зарядный ток.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_CURRENT_MAX", 605030144, 2, "ICharging.CHARGE_FUNC_CHARGING_CURRENT_MAX", "Максимально допустимый зарядный ток.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_CURRENT_MIN", 605030400, 2, "ICharging.CHARGE_FUNC_CHARGING_CURRENT_MIN", "Минимально допустимый зарядный ток.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_CURRENT_STEP", 605030656, 2, "ICharging.CHARGE_FUNC_CHARGING_CURRENT_STEP", "Шаг регулировки зарядного тока.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_ENERGY", 605291776, 2, "ICharging.CHARGE_FUNC_CHARGING_ENERGY", "Энергия, переданная за текущую сессию зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_ESTIMATED_TIME", 605291264, 2, "ICharging.CHARGE_FUNC_CHARGING_ESTIMATED_TIME", "Расчётное время до завершения AC‑зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_ESTIMATED_TIME_DC", 605292032, 2, "ICharging.CHARGE_FUNC_CHARGING_ESTIMATED_TIME_DC", "Расчётное время до завершения DC‑зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_PLUG_STATE", 605225472, 2, "ICharging.CHARGE_FUNC_CHARGING_PLUG_STATE", "Состояние штекера зарядного разъёма.", new Value[] {
                new Value("CHARGING_PLUG_STATE_CONNECTED_WAITING", 605225493),
                new Value("CHARGING_PLUG_STATE_DISCONNECTED", 605225489),
                new Value("CHARGING_PLUG_STATE_DIS_CHRGN_CONNECTED", 605225492),
                new Value("CHARGING_PLUG_STATE_FAULT", 605225495),
                new Value("CHARGING_PLUG_STATE_NONE", 605225496),
                new Value("CHARGING_PLUG_STATE_QUICK_CHRGN_CONNECTED", 605225491),
                new Value("CHARGING_PLUG_STATE_SLOW_CHRGN_CONNECTED", 605225490),
                new Value("CHARGING_PLUG_STATE_WRONG_OPERATION", 605225494),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_PLUG_TYPE", 605225216, 2, "ICharging.CHARGE_FUNC_CHARGING_PLUG_TYPE", "Тип подключённого зарядного штекера.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_SOC", 605028864, 2, "ICharging.CHARGE_FUNC_CHARGING_SOC", "Уровень заряда HV‑батареи (SoC) при зарядке.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_SOC_MAX", 605029120, 2, "ICharging.CHARGE_FUNC_CHARGING_SOC_MAX", "Целевой максимум SoC при зарядке.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_SOC_MIN", 605029376, 2, "ICharging.CHARGE_FUNC_CHARGING_SOC_MIN", "Минимальный SoC для зарядки/целевой порог.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_SOC_STEP", 605029632, 2, "ICharging.CHARGE_FUNC_CHARGING_SOC_STEP", "Шаг изменения целевого SoC.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_WORK_CURRENT", 605291008, 2, "ICharging.CHARGE_FUNC_CHARGING_WORK_CURRENT", "Ток на разъёме во время зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_CHARGING_WORK_VOLTAGE", 605290752, 2, "ICharging.CHARGE_FUNC_CHARGING_WORK_VOLTAGE", "Напряжение на разъёме во время зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_ENETGY", 605357056, 2, "ICharging.CHARGE_FUNC_DISCHARGING_ENETGY", "Энергия, выданная во внешнюю нагрузку (V2L/V2V).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_ESTIMATED_TIME", 605356800, 2, "ICharging.CHARGE_FUNC_DISCHARGING_ESTIMATED_TIME", "Оценка времени до завершения раздачи энергии.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SOC", 605160192, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SOC", "Текущий SoC при V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SOC_MAX", 605160448, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SOC_MAX", "Порог макс. SoC для V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SOC_MIN", 605160704, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SOC_MIN", "Порог мин. SoC для V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SOC_STEP", 605160960, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SOC_STEP", "Шаг изменения SoC для V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SWITCH_V2L", 605159936, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SWITCH_V2L", "Включение режима V2L (питание внешних устройств).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_SWITCH_V2V", 605159680, 2, "ICharging.CHARGE_FUNC_DISCHARGING_SWITCH_V2V", "Включение режима V2V (подзарядка другого авто).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_WORK_CURRENT", 605356544, 2, "ICharging.CHARGE_FUNC_DISCHARGING_WORK_CURRENT", "Ток при V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISCHARGING_WORK_VOLTAGE", 605356288, 2, "ICharging.CHARGE_FUNC_DISCHARGING_WORK_VOLTAGE", "Напряжение при V2L/V2V.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISTANCE_INTERVAL_MAINTAIN", 609225216, 2, "ICharging.CHARGE_FUNC_DISTANCE_INTERVAL_MAINTAIN", "Поддержание дистанции ради экономии энергии.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISTANCE_PROTECTION", 609222912, 2, "ICharging.CHARGE_FUNC_DISTANCE_PROTECTION", "Ограничение расхода для сохранения запаса хода.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_DISTANCE_PROTECTION_UNIT", 609223168, 2, "ICharging.CHARGE_FUNC_DISTANCE_PROTECTION_UNIT", "Единицы для параметров защиты запаса хода.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_ENDURANCE_MILEAGE", 606079744, 2, "ICharging.CHARGE_FUNC_ENDURANCE_MILEAGE", "Запас хода (оценка).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_EXTERNAL_CHARGING_LIGHT", 605031936, 2, "ICharging.CHARGE_FUNC_EXTERNAL_CHARGING_LIGHT", "Внешний индикатор состояния зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_EXTERNAL_POWER_SUPPLY", 606078976, 2, "ICharging.CHARGE_FUNC_EXTERNAL_POWER_SUPPLY", "Питание внешних устройств от авто (V2L).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_FUEL_TO_BATT_NOTWORK_TOAST", 609225984, 2, "ICharging.CHARGE_FUNC_FUEL_TO_BATT_NOTWORK_TOAST", "Уведомление: заряд от ДВС недоступен.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_GEAR_LVL_INDCN", 609225728, 2, "ICharging.CHARGE_FUNC_GEAR_LVL_INDCN", "Индикация уровня передачи для энергорежимов.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_BATT_ACCHRGNP", 609223936, 2, "ICharging.CHARGE_FUNC_HV_BATT_ACCHRGNP", "Состояние AC‑разъёма HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_BATT_CHRG", 609223424, 2, "ICharging.CHARGE_FUNC_HV_BATT_CHRG", "Состояние зарядки HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_BATT_CHRG_TIME", 609224960, 2, "ICharging.CHARGE_FUNC_HV_BATT_CHRG_TIME", "Время до завершения зарядки HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_BATT_DCCHRGNP", 609224192, 2, "ICharging.CHARGE_FUNC_HV_BATT_DCCHRGNP", "Состояние DC‑разъёма HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_BATT_DCHRGNP", 609224448, 2, "ICharging.CHARGE_FUNC_HV_BATT_DCHRGNP", "Состояние разъёма раздачи HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_HV_DIS_CHRG_STS", 609223680, 2, "ICharging.CHARGE_FUNC_HV_DIS_CHRG_STS", "Состояние режима раздачи энергии (discharge).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_MAINTAIN_BATTERY_TEMP", 605030912, 2, "ICharging.CHARGE_FUNC_MAINTAIN_BATTERY_TEMP", "Поддержание температуры HV‑батареи при зарядке.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_NOTIFICATION_WRONG_OPERATION_REMIND", 606142720, 2, "ICharging.CHARGE_FUNC_NOTIFICATION_WRONG_OPERATION_REMIND", "Напоминание о некорректной операции зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_PHEV_PARKING_POWER", 605358080, 2, "ICharging.CHARGE_FUNC_PHEV_PARKING_POWER", "Питание при парковке для PHEV.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_PRE_CHARGING", 605094144, 2, "ICharging.CHARGE_FUNC_PRE_CHARGING", "Настройки отложенной/плановой зарядки.", new Value[] {
                new Value("PRE_CHARGING_STATUS_CANCELED", 605094918),
                new Value("PRE_CHARGING_STATUS_CANCEL_FAILED", 605094919),
                new Value("PRE_CHARGING_STATUS_CHARGING", 605094917),
                new Value("PRE_CHARGING_STATUS_FAILED", 605094914),
                new Value("PRE_CHARGING_STATUS_FAILURE", 605094915),
                new Value("PRE_CHARGING_STATUS_SCHEDULING", 605094916),
                new Value("PRE_CHARGING_STATUS_SUCCEED", 605094913),
                new Value("PRE_CHARGING_STATUS_TIMEOUT", 605094920),
                new Value("PRE_CHARGING_STATUS_UNKNOWN", 255),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_PRE_CHARGING_IMMEDIATELY", 605095424, 2, "ICharging.CHARGE_FUNC_PRE_CHARGING_IMMEDIATELY", "Немедленный старт запланированной зарядки.", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_PRE_CHARGING_STATUS", 605094912, 2, "ICharging.CHARGE_FUNC_PRE_CHARGING_STATUS", "Статус отложенной/плановой зарядки.", new Value[] {
                new Value("PRE_CHARGING_STATUS_CANCELED", 605094918),
                new Value("PRE_CHARGING_STATUS_CANCEL_FAILED", 605094919),
                new Value("PRE_CHARGING_STATUS_CHARGING", 605094917),
                new Value("PRE_CHARGING_STATUS_FAILED", 605094914),
                new Value("PRE_CHARGING_STATUS_FAILURE", 605094915),
                new Value("PRE_CHARGING_STATUS_SCHEDULING", 605094916),
                new Value("PRE_CHARGING_STATUS_SUCCEED", 605094913),
                new Value("PRE_CHARGING_STATUS_TIMEOUT", 605094920),
                new Value("PRE_CHARGING_STATUS_UNKNOWN", 255),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_PRE_CHARGING_TYPE", 605095168, 2, "ICharging.CHARGE_FUNC_PRE_CHARGING_TYPE", "Тип расписания зарядки (выкл/разово/циклично).", new Value[] {
                new Value("CHARGE_FUNC_PRE_CHARGING_TYPE_CYCLE", 605095170),
                new Value("CHARGE_FUNC_PRE_CHARGING_TYPE_OFF", 605095168),
                new Value("CHARGE_FUNC_PRE_CHARGING_TYPE_SINGLE", 605095169),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_TIME_INTERVAL_MAINTAIN", 609225472, 2, "ICharging.CHARGE_FUNC_TIME_INTERVAL_MAINTAIN", "Управление по времени (энергоуправление).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_TRAVEL_HVAC", 606078208, 2, "ICharging.CHARGE_FUNC_TRAVEL_HVAC", "Параметры климата в поездке (энергопотребление).", new Value[] {
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_WARM_UP", 605030944, 2, "ICharging.CHARGE_FUNC_WARM_UP", "Режим прогрева HV‑системы/протокола зарядки.", new Value[] {
                new Value("WARM_UP_ECO", 605030929),
                new Value("WARM_UP_SPORT", 605030930),
        }));
        put(byId, byKey, new Entry("CHARGE_FUNC_WARM_UP_LEVEL", 605030928, 2, "ICharging.CHARGE_FUNC_WARM_UP_LEVEL", "Уровень прогрева/подготовки.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_BPF", 570622976, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_BPF", "Профиль Custom: BPF.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_CLIMATE_MODE", 570624512, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_CLIMATE_MODE", "Профиль Custom: климат‑система.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_DRIVER_INFO", 570625024, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_DRIVER_INFO", "Профиль Custom: оформление/информация на панели.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_EN_START_STOP", 570625536, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_EN_START_STOP", "Профиль Custom: система Start/Stop.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_INFOR_THEME", 570624768, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_INFOR_THEME", "Профиль Custom: тема панели.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_INTERIOR_LIGHT", 570625280, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_INTERIOR_LIGHT", "Профиль Custom: подсветка салона.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_PROPULSION_SYS", 570622208, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_PROPULSION_SYS", "Профиль Custom: силовая установка.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_RAB", 570622720, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_RAB", "Профиль Custom: RAB.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_STEERING_WHEEL_FEEL", 570624256, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_STEERING_WHEEL_FEEL", "Профиль Custom: усилие на руле.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DM_CUSTOM_SUSPENSION_MODE", 570622464, 2, "IDriveMode.DM_FUNC_DM_CUSTOM_SUSPENSION_MODE", "Профиль Custom: настройки подвески.", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DRIVE_MODE_SELECT", 570491136, 2, "IDriveMode.DM_FUNC_DRIVE_MODE_SELECT", "Переключение режимов вождения.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_ADAPTIVE", 570491158),
                new Value("DRIVE_MODE_SELECTION_AWD", 570491150),
                new Value("DRIVE_MODE_SELECTION_COMFORT", 570491138),
                new Value("DRIVE_MODE_SELECTION_CUSTOM", 570491200),
                new Value("DRIVE_MODE_SELECTION_DYNAMIC", 570491139),
                new Value("DRIVE_MODE_SELECTION_EAWD", 570491154),
                new Value("DRIVE_MODE_SELECTION_ECO", 570491137),
                new Value("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152),
                new Value("DRIVE_MODE_SELECTION_HDC", 570491141),
                new Value("DRIVE_MODE_SELECTION_HYBRID", 570491143),
                new Value("DRIVE_MODE_SELECTION_MUD", 570491146),
                new Value("DRIVE_MODE_SELECTION_NORMAL", 570491153),
                new Value("DRIVE_MODE_SELECTION_OFFROAD", 570491155),
                new Value("DRIVE_MODE_SELECTION_PHEV", 570491148),
                new Value("DRIVE_MODE_SELECTION_POWER", 570491144),
                new Value("DRIVE_MODE_SELECTION_PURE", 570491142),
                new Value("DRIVE_MODE_SELECTION_ROCK", 570491147),
                new Value("DRIVE_MODE_SELECTION_SAND", 570491149),
                new Value("DRIVE_MODE_SELECTION_SAVE", 570491151),
                new Value("DRIVE_MODE_SELECTION_SNOW", 570491145),
                new Value("DRIVE_MODE_SELECTION_START_TYPE18", 570491159),
                new Value("DRIVE_MODE_SELECTION_START_TYPE72", 570491160),
                new Value("DRIVE_MODE_SELECTION_START_TYPE79", 570491161),
                new Value("DRIVE_MODE_SELECTION_START_TYPE97", 570491162),
                new Value("DRIVE_MODE_SELECTION_UNKNOWN", 255),
                new Value("DRIVE_MODE_SELECTION_XC", 570491140),
                new Value("DRIVE_MODE_SPORT_PLUS", 570491157),
        }));
        put(byId, byKey, new Entry("DM_FUNC_STEERING_WHEEL_FEEL_SYNC_DRIVEMODE", 570688256, 2, "IDriveMode.DM_FUNC_STEERING_WHEEL_FEEL_SYNC_DRIVEMODE", "Связать усилие на руле с режимом вождения.", new Value[] {
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_ECO_PLUS", 570491156, 2, "IDriveMode.DRIVE_MODE_ECO_PLUS", "Режим вождения: Эко+.", new Value[] {
                new Value("DRIVE_MODE_ECO_PLUS", 570491156),
                new Value("DRIVE_MODE_ECO_PLUS", 570491156),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_ADAPTIVE", 570491158, 2, "IDriveMode.DRIVE_MODE_SELECTION_ADAPTIVE", "Режим вождения: Адаптивный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_ADAPTIVE", 570491158),
                new Value("DRIVE_MODE_SELECTION_ADAPTIVE", 570491158),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_AWD", 570491150, 2, "IDriveMode.DRIVE_MODE_SELECTION_AWD", "Режим вождения: Полный привод.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_AWD", 570491150),
                new Value("DRIVE_MODE_SELECTION_AWD", 570491150),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_COMFORT", 570491138, 2, "IDriveMode.DRIVE_MODE_SELECTION_COMFORT", "Режим вождения: Комфортный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_COMFORT", 570491138),
                new Value("DRIVE_MODE_SELECTION_COMFORT", 570491138),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_CUSTOM", 570491200, 2, "IDriveMode.DRIVE_MODE_SELECTION_CUSTOM", "Режим вождения: Пользовательский профиль.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_CUSTOM", 570491200),
                new Value("DRIVE_MODE_SELECTION_CUSTOM", 570491200),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_DYNAMIC", 570491139, 2, "IDriveMode.DRIVE_MODE_SELECTION_DYNAMIC", "Режим вождения: Динамичный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_DYNAMIC", 570491139),
                new Value("DRIVE_MODE_SELECTION_DYNAMIC", 570491139),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_ECO", 570491137, 2, "IDriveMode.DRIVE_MODE_SELECTION_ECO", "Режим вождения: Экономичный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_ECO", 570491137),
                new Value("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152),
                new Value("DRIVE_MODE_SELECTION_ECO", 570491137),
                new Value("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152, 2, "IDriveMode.DRIVE_MODE_SELECTION_ECO_HEV_PHEV", "Режим вождения: Эко (HEV/PHEV).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152),
                new Value("DRIVE_MODE_SELECTION_ECO_HEV_PHEV", 570491152),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_HDC", 570491141, 2, "IDriveMode.DRIVE_MODE_SELECTION_HDC", "Режим вождения: Помощь при спуске (HDC).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_HDC", 570491141),
                new Value("DRIVE_MODE_SELECTION_HDC", 570491141),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_HYBRID", 570491143, 2, "IDriveMode.DRIVE_MODE_SELECTION_HYBRID", "Режим вождения: Гибридный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_HYBRID", 570491143),
                new Value("DRIVE_MODE_SELECTION_HYBRID", 570491143),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_MUD", 570491146, 2, "IDriveMode.DRIVE_MODE_SELECTION_MUD", "Режим вождения: Для грязи.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_MUD", 570491146),
                new Value("DRIVE_MODE_SELECTION_MUD", 570491146),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_NORMAL", 570491153, 2, "IDriveMode.DRIVE_MODE_SELECTION_NORMAL", "Режим вождения: Стандартный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_NORMAL", 570491153),
                new Value("DRIVE_MODE_SELECTION_NORMAL", 570491153),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_OFFROAD", 570491155, 2, "IDriveMode.DRIVE_MODE_SELECTION_OFFROAD", "Режим вождения: Внедорожный режим.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_OFFROAD", 570491155),
                new Value("DRIVE_MODE_SELECTION_OFFROAD", 570491155),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_PHEV", 570491148, 2, "IDriveMode.DRIVE_MODE_SELECTION_PHEV", "Режим вождения: Режим PHEV.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_PHEV", 570491148),
                new Value("DRIVE_MODE_SELECTION_PHEV", 570491148),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_POWER", 570491144, 2, "IDriveMode.DRIVE_MODE_SELECTION_POWER", "Режим вождения: Максимальная отдача.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_POWER", 570491144),
                new Value("DRIVE_MODE_SELECTION_POWER", 570491144),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_PURE", 570491142, 2, "IDriveMode.DRIVE_MODE_SELECTION_PURE", "Режим вождения: Чисто электрический ход.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_PURE", 570491142),
                new Value("DRIVE_MODE_SELECTION_PURE", 570491142),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_ROCK", 570491147, 2, "IDriveMode.DRIVE_MODE_SELECTION_ROCK", "Режим вождения: Для каменистых участков.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_ROCK", 570491147),
                new Value("DRIVE_MODE_SELECTION_ROCK", 570491147),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_SAND", 570491149, 2, "IDriveMode.DRIVE_MODE_SELECTION_SAND", "Режим вождения: Для песка.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_SAND", 570491149),
                new Value("DRIVE_MODE_SELECTION_SAND", 570491149),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_SAVE", 570491151, 2, "IDriveMode.DRIVE_MODE_SELECTION_SAVE", "Режим вождения: Сохранение заряда.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_SAVE", 570491151),
                new Value("DRIVE_MODE_SELECTION_SAVE", 570491151),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_SNOW", 570491145, 2, "IDriveMode.DRIVE_MODE_SELECTION_SNOW", "Режим вождения: Для снега.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_SNOW", 570491145),
                new Value("DRIVE_MODE_SELECTION_SNOW", 570491145),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_START_TYPE18", 570491159, 2, "IDriveMode.DRIVE_MODE_SELECTION_START_TYPE18", "Режим вождения: Служебный профиль запуска (тип 18).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_START_TYPE18", 570491159),
                new Value("DRIVE_MODE_SELECTION_START_TYPE18", 570491159),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_START_TYPE72", 570491160, 2, "IDriveMode.DRIVE_MODE_SELECTION_START_TYPE18", "Режим вождения: Служебный профиль запуска (тип 72).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_START_TYPE72", 570491160),
                new Value("DRIVE_MODE_SELECTION_START_TYPE72", 570491160),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_START_TYPE79", 570491161, 2, "IDriveMode.DRIVE_MODE_SELECTION_START_TYPE79", "Режим вождения: Служебный профиль запуска (тип 79).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_START_TYPE79", 570491161),
                new Value("DRIVE_MODE_SELECTION_START_TYPE79", 570491161),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_START_TYPE97", 570491162, 2, "IDriveMode.DRIVE_MODE_SELECTION_START_TYPE97", "Режим вождения: Служебный профиль запуска (тип 97).", new Value[] {
                new Value("DRIVE_MODE_SELECTION_START_TYPE97", 570491162),
                new Value("DRIVE_MODE_SELECTION_START_TYPE97", 570491162),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_XC", 570491140, 2, "IDriveMode.DRIVE_MODE_SELECTION_XC", "Режим вождения: Кросс‑кантри.", new Value[] {
                new Value("DRIVE_MODE_SELECTION_XC", 570491140),
                new Value("DRIVE_MODE_SELECTION_XC", 570491140),
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SELECTION_eAWD", 570491154, 2, "IDriveMode.DRIVE_MODE_SELECTION_NORMAL", "Режим вождения: Электрический полный привод.", new Value[] {
        }));
        put(byId, byKey, new Entry("DRIVE_MODE_SPORT_PLUS", 570491157, 2, "IDriveMode.DRIVE_MODE_ECO_PLUS", "", new Value[] {
                new Value("DRIVE_MODE_SPORT_PLUS", 570491157),
                new Value("DRIVE_MODE_SPORT_PLUS", 570491157),
        }));
        put(byId, byKey, new Entry("ENERGY_REGENERATION_LEVEL_AUTO", 537003268, 2, "IVehicle.ENERGY_REGENERATION_LEVEL_AUTO", "Уровень рекуперации: автоматический.", new Value[] {
                new Value("ENERGY_REGENERATION_LEVEL_AUTO", 537003268),
                new Value("ENERGY_REGENERATION_LEVEL_AUTO", 537003268),
                new Value("FAN_SPEED_LEVEL_AUTO", 268566794),
                new Value("SEAT_HEATING_LEVEL_AUTO", 268763663),
                new Value("SEAT_MASSAGE_LEVEL_AUTO", 268764943),
                new Value("SEAT_VENTILATION_LEVEL_AUTO", 268763407),
        }));
        put(byId, byKey, new Entry("ENERGY_REGENERATION_LEVEL_HIGH", 537003267, 2, "IVehicle.ENERGY_REGENERATION_LEVEL_HIGH", "Уровень рекуперации: высокий.", new Value[] {
                new Value("ENERGY_REGENERATION_LEVEL_HIGH", 537003267),
                new Value("AQI_LEVEL_HIGHER_POLLUTION", 2106117),
                new Value("AQI_LEVEL_HIGH_POLLUTION", 2106116),
                new Value("ENERGY_REGENERATION_LEVEL_HIGH", 537003267),
                new Value("ENGINE_OIL_LEVEL_HIGH", 2098180),
                new Value("ESM_VOLUME_LEVEL_HIGH", 538575363),
                new Value("PM25_LEVEL_HIGHER_POLLUTION", 2105605),
                new Value("PM25_LEVEL_HIGH_POLLUTION", 2105604),
                new Value("SOUND_WARNING_VOLUME_LEVEL_HIGH", 538771715),
                new Value("STEERING_ASSISTANCE_LEVEL_HIGH", 537331713),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_HIGH_1", 538509570),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_HIGH_2", 538509569),
                new Value("TIRE_WARNING_LEVEL_HIGH_PRESSURE", 5245189),
                new Value("TIRE_WARNING_LEVEL_HIGH_WARN", 5245187),
        }));
        put(byId, byKey, new Entry("ENERGY_REGENERATION_LEVEL_LOW", 537003265, 2, "IVehicle.ENERGY_REGENERATION_LEVEL_LOW", "Уровень рекуперации: низкий.", new Value[] {
                new Value("ENERGY_REGENERATION_LEVEL_LOW", 537003265),
                new Value("AQI_LEVEL_LOWER_POLLUTION", 2106118),
                new Value("AQI_LEVEL_LOW_POLLUTION", 2106114),
                new Value("BRAKE_FLUID_LEVEL_LOW", 2098690),
                new Value("ENERGY_REGENERATION_LEVEL_LOW", 537003265),
                new Value("ENGINE_COOLANT_LEVEL_LOW", 2098434),
                new Value("ENGINE_COOLANT_LEVEL_LOW_1", 2098435),
                new Value("ENGINE_OIL_LEVEL_LOW_1", 2098178),
                new Value("ENGINE_OIL_LEVEL_LOW_2", 2098179),
                new Value("ESM_VOLUME_LEVEL_LOW", 538575361),
                new Value("PM25_LEVEL_LOWER_POLLUTION", 2105606),
                new Value("PM25_LEVEL_LOW_POLLUTION", 2105602),
                new Value("SOUND_WARNING_VOLUME_LEVEL_LOW", 538771713),
                new Value("STEERING_ASSISTANCE_LEVEL_LOW", 537331715),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_LOW_1", 538509572),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_LOW_2", 538509573),
                new Value("TIRE_WARNING_LEVEL_LOW_WARN", 5245186),
        }));
        put(byId, byKey, new Entry("ENERGY_REGENERATION_LEVEL_MID", 537003266, 2, "IVehicle.ENERGY_REGENERATION_LEVEL_MID", "Уровень рекуперации: средний.", new Value[] {
                new Value("ENERGY_REGENERATION_LEVEL_MID", 537003266),
                new Value("ENERGY_REGENERATION_LEVEL_MID", 537003266),
                new Value("ESM_VOLUME_LEVEL_MID", 538575362),
                new Value("SOUND_WARNING_VOLUME_LEVEL_MID", 538771714),
        }));
        put(byId, byKey, new Entry("FUNCTION_SPEED_CONTROL_MODE", 537069056, 2, "IVehicle.SETTING_FUNC_SPEED_CONTROL_MODE", "Режим систем поддержки/ограничения скорости.", new Value[] {
                new Value("SPEED_CONTROL_MODE_ACC", 537069058),
                new Value("SPEED_CONTROL_MODE_CC", 537069057),
                new Value("SPEED_CONTROL_MODE_GPILOT", 537069059),
                new Value("SPEED_CONTROL_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_AVG_FUEL", 620822784, 2, "IUnits.FUNC_UNIT_AVG_FUEL", "Единицы среднего расхода топлива.", new Value[] {
                new Value("UNIT_AVG_FUEL_KM_L", 620822786),
                new Value("UNIT_AVG_FUEL_L_100KM", 620822785),
                new Value("UNIT_AVG_FUEL_UK_MPG", 620822788),
                new Value("UNIT_AVG_FUEL_US_MPG", 620822787),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_DATE_FORMAT", 620888576, 2, "IUnits.FUNC_UNIT_DATE_FORMAT", "Формат даты (DMY/MDY/YMD).", new Value[] {
                new Value("UNIT_DATE_FORMAT_DMY", 620888578),
                new Value("UNIT_DATE_FORMAT_MDY", 620888579),
                new Value("UNIT_DATE_FORMAT_YMD", 620888577),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_DRIVEN_DISTANCE", 620823040, 2, "IUnits.FUNC_UNIT_DRIVEN_DISTANCE", "Единицы расстояния/пробега.", new Value[] {
                new Value("UNIT_DRIVEN_DISTANCE_KM", 620823041),
                new Value("UNIT_DRIVEN_DISTANCE_MILES", 620823042),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_SPEED", 620823808, 2, "IUnits.FUNC_UNIT_SPEED", "Единицы скорости (км/ч, миль/ч).", new Value[] {
                new Value("SPEED_CONTROL_MODE_ACC", 537069058),
                new Value("SPEED_CONTROL_MODE_CC", 537069057),
                new Value("SPEED_CONTROL_MODE_GPILOT", 537069059),
                new Value("SPEED_CONTROL_MODE_OFF", 0),
                new Value("SPEED_LIMITATION_MODE_ASL", 537068802),
                new Value("SPEED_LIMITATION_MODE_AVSL", 537068801),
                new Value("SPEED_LIMITATION_MODE_OFF", 0),
                new Value("SPEED_LIMIT_WARNING_MODE_FLASHING", 671482370),
                new Value("SPEED_LIMIT_WARNING_MODE_NO_WARNING", 671482369),
                new Value("SPEED_LIMIT_WARNING_MODE_OFF", 0),
                new Value("SPEED_LIMIT_WARNING_MODE_SOUND", 671482371),
                new Value("SPEED_LIMIT_WARNING_OFFSET_0KM", 671482881),
                new Value("SPEED_LIMIT_WARNING_OFFSET_10KM", 671482883),
                new Value("SPEED_LIMIT_WARNING_OFFSET_5KM", 671482882),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_10KM", 671482885),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_5KM", 671482884),
                new Value("SPEED_LIMIT_WARNING_OFFSET_OFF", 0),
                new Value("UNIT_SPEED_KM_H", 620823809),
                new Value("UNIT_SPEED_MPH", 620823810),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_TEMPERATURE", 620823296, 2, "IUnits.FUNC_UNIT_TEMPERATURE", "Единицы температуры (°C/°F).", new Value[] {
                new Value("TEMPERATURE_UNIT_C", 268830209),
                new Value("TEMPERATURE_UNIT_F", 268830210),
                new Value("UNIT_TEMPERATURE_C", 620823297),
                new Value("UNIT_TEMPERATURE_F", 620823298),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_TIME_INDICATION", 620888320, 2, "IUnits.FUNC_UNIT_TIME_INDICATION", "Формат времени (24ч/AM‑PM).", new Value[] {
                new Value("UNIT_TIME_INDICATION_24H", 620888322),
                new Value("UNIT_TIME_INDICATION_AM_PM", 620888321),
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_TIRE_PRESSURE", 620823552, 2, "IUnits.FUNC_UNIT_TIRE_PRESSURE", "Единицы давления в шинах (kPa/bar/psi).", new Value[] {
                new Value("UNIT_TIRE_PRESSURE_BAR", 620823554),
                new Value("UNIT_TIRE_PRESSURE_KPA", 620823553),
                new Value("UNIT_TIRE_PRESSURE_PSI", 620823555),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AC", 268501760, 2, "IHvac.HVAC_FUNC_AC", "Включение компрессора кондиционера (A/C).", new Value[] {
                new Value("AC_ON", 1),
                new Value("AC_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AC_MAX", 268502016, 2, "IHvac.HVAC_FUNC_AC_MAX", "MAX A/C: максимальное охлаждение.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE", 269156608, 2, "IHvac.HVAC_FUNC_AIR_FRAGRANCE", "Система ароматизации салона (тип/уровень/слот).", new Value[] {
                new Value("AIR_FRAGRANCE_JASMINE", 269156870),
                new Value("AIR_FRAGRANCE_LAVENDER", 269156867),
                new Value("AIR_FRAGRANCE_LEVEL_1", 269157121),
                new Value("AIR_FRAGRANCE_LEVEL_2", 269157122),
                new Value("AIR_FRAGRANCE_LEVEL_3", 269157123),
                new Value("AIR_FRAGRANCE_LEVEL_OFF", 0),
                new Value("AIR_FRAGRANCE_LILY", 269156866),
                new Value("AIR_FRAGRANCE_LONGJING", 269156868),
                new Value("AIR_FRAGRANCE_OFF", 0),
                new Value("AIR_FRAGRANCE_ROSE", 269156865),
                new Value("AIR_FRAGRANCE_SANDALWOOD", 269156869),
                new Value("AIR_FRAGRANCE_SLOT_1", 269157377),
                new Value("AIR_FRAGRANCE_SLOT_2", 269157378),
                new Value("AIR_FRAGRANCE_SLOT_3", 269157379),
                new Value("AIR_FRAGRANCE_SLOT_4", 269157380),
                new Value("AIR_FRAGRANCE_SLOT_5", 269157381),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE_LEVEL", 269157120, 2, "IHvac.HVAC_FUNC_AIR_FRAGRANCE_LEVEL", "Интенсивность ароматизации.", new Value[] {
                new Value("AIR_FRAGRANCE_LEVEL_1", 269157121),
                new Value("AIR_FRAGRANCE_LEVEL_2", 269157122),
                new Value("AIR_FRAGRANCE_LEVEL_3", 269157123),
                new Value("AIR_FRAGRANCE_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE_LOW", 269157888, 2, "IFragrance.HVAC_FUNC_AIR_FRAGRANCE_LOW", "Низкая интенсивность ароматизации.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE_SLOT", 269157376, 2, "IFragrance.HVAC_FUNC_AIR_FRAGRANCE_SLOT", "Выбор картриджа аромата (слот).", new Value[] {
                new Value("AIR_FRAGRANCE_SLOT_1", 269157377),
                new Value("AIR_FRAGRANCE_SLOT_2", 269157378),
                new Value("AIR_FRAGRANCE_SLOT_3", 269157379),
                new Value("AIR_FRAGRANCE_SLOT_4", 269157380),
                new Value("AIR_FRAGRANCE_SLOT_5", 269157381),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE_TYPE", 269156864, 2, "IHvac.HVAC_FUNC_AIR_FRAGRANCE_TYPE", "Тип выбранного аромата (код).", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AIR_FRAGRANCE_TYPE_ID", 269157632, 2, "IFragrance.HVAC_FUNC_AIR_FRAGRANCE_TYPE_ID", "Идентификатор аромата (код).", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AI_POWER", 269091840, 2, "IHvac.HVAC_FUNC_AI_POWER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AQS_STATUS", 269751808, 2, "IHvac.HVAC_FUNC_AQS_STATUS", "Состояние системы контроля качества воздуха (AQS).", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO", 268501504, 2, "IHvac.HVAC_FUNC_AUTO", "Режим автоматического управления климатом.", new Value[] {
                new Value("AUTO_CLOSE_WINDOW_KEY_LONG_PRESS", 537396226),
                new Value("AUTO_CLOSE_WINDOW_OFF", 0),
                new Value("AUTO_CLOSE_WINDOW_VEHICLE_LOCK", 537396225),
                new Value("AUTO_FAN_SETTING_HIGH", 268567043),
                new Value("AUTO_FAN_SETTING_HIGHER", 268567045),
                new Value("AUTO_FAN_SETTING_NORMAL", 268567042),
                new Value("AUTO_FAN_SETTING_QUIETER", 268567044),
                new Value("AUTO_FAN_SETTING_SILENT", 268567041),
                new Value("AUTO_RESET_OPTION_4_HOURS", 612369153),
                new Value("AUTO_RESET_OPTION_CHARGING", 612369154),
                new Value("AUTO_RESET_OPTION_PARKING", 612369156),
                new Value("AUTO_RESET_OPTION_PARKING_OIL", 612369155),
                new Value("AUTO_SEAT_HEATING_LEVEL_1", 268764417),
                new Value("AUTO_SEAT_HEATING_LEVEL_2", 268764418),
                new Value("AUTO_SEAT_HEATING_LEVEL_3", 268764419),
                new Value("AUTO_SEAT_HEATING_OFF", 0),
                new Value("AUTO_SEAT_HEATING_TIME_1", 268764673),
                new Value("AUTO_SEAT_HEATING_TIME_2", 268764674),
                new Value("AUTO_SEAT_HEATING_TIME_3", 268764675),
                new Value("AUTO_SEAT_HEATING_TIME_4", 268764676),
                new Value("AUTO_SEAT_HEATING_TIME_OFF", 0),
                new Value("AUTO_SEAT_MASSAGE_LEVEL_1", 268765185),
                new Value("AUTO_SEAT_MASSAGE_LEVEL_2", 268765186),
                new Value("AUTO_SEAT_MASSAGE_LEVEL_3", 268765187),
                new Value("AUTO_SEAT_MASSAGE_OFF", 0),
                new Value("AUTO_SEAT_MASSAGE_TIME_1", 268765441),
                new Value("AUTO_SEAT_MASSAGE_TIME_2", 268765442),
                new Value("AUTO_SEAT_MASSAGE_TIME_3", 268765443),
                new Value("AUTO_SEAT_MASSAGE_TIME_OFF", 0),
                new Value("AUTO_SEAT_VENTILATION_TIME_1", 268764161),
                new Value("AUTO_SEAT_VENTILATION_TIME_2", 268764162),
                new Value("AUTO_SEAT_VENTILATION_TIME_3", 268764163),
                new Value("AUTO_SEAT_VENTILATION_TIME_4", 268764164),
                new Value("AUTO_SEAT_VENTILATION_TIME_OFF", 0),
                new Value("AUTO_STEERING_WHEEL_HEAT_HIGH", 269025795),
                new Value("AUTO_STEERING_WHEEL_HEAT_LOW", 269025793),
                new Value("AUTO_STEERING_WHEEL_HEAT_MID", 269025794),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_1", 269026049),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_2", 269026050),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_3", 269026051),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTOMATIC_VENTILATION_DRY", 269485312, 2, "IHvac.HVAC_FUNC_AUTOMATIC_VENTILATION_DRY", "Автосушка испарителя после выключения A/C.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND", 269418752, 2, "IHvac.HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND", "Напоминание закрыть окна для эффективности климата.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND_REQUEST", 269419008, 2, "IHvac.HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND_REQUEST", "Запрос напоминания о закрытии окон.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_CONTROL", 269749248, 2, "IHvac.HVAC_FUNC_AUTO_CONTROL", "Автоматическое управление климат‑системой.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_CZIS", 269485568, 2, "IHvac.HVAC_FUNC_AUTO_CZIS", "Авто‑режим системы очистки воздуха CZIS.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEFROST_CONFIRM", 268699392, 2, "IHvac.HVAC_FUNC_AUTO_DEFROST_CONFIRM", "Подтверждение автозапуска обогрева стекла.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEFROST_FRONT", 268698880, 2, "IHvac.HVAC_FUNC_AUTO_DEFROST_FRONT", "Автовключение обдува лобового стекла.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEFROST_REAR", 268698624, 2, "IHvac.HVAC_FUNC_AUTO_DEFROST_REAR", "Автовключение обогрева заднего стекла.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEFROST_REQUEST", 268699136, 2, "IHvac.HVAC_FUNC_AUTO_DEFROST_REQUEST", "Запрос на автоматический дефрост.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_ELECTRIC_DEFROST", 269027328, 2, "IHvac.HVAC_FUNC_ELECTRIC_DEFROST", "Электрообогрев зоны лобового стекла.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_FAN_SETTING", 268567040, 2, "IHvac.HVAC_FUNC_AUTO_FAN_SETTING", "Профиль авто‑скорости вентилятора.", new Value[] {
                new Value("AUTO_FAN_SETTING_HIGH", 268567043),
                new Value("AUTO_FAN_SETTING_HIGHER", 268567045),
                new Value("AUTO_FAN_SETTING_NORMAL", 268567042),
                new Value("AUTO_FAN_SETTING_QUIETER", 268567044),
                new Value("AUTO_FAN_SETTING_SILENT", 268567041),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_FAN_SPEED_HARD_KEY", 268567552, 2, "IHvac.HVAC_FUNC_AUTO_FAN_SPEED_HARD_KEY", "Событие клавиши авто‑скорости вентилятора.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_ION", 269222144, 2, "IHvac.HVAC_FUNC_AUTO_ION", "Автовключение ионизатора.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_ION_CONFIRM", 269222656, 2, "IHvac.HVAC_FUNC_AUTO_ION_CONFIRM", "Подтверждение авто‑ионизатора.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_ION_REQUEST", 269222400, 2, "IHvac.HVAC_FUNC_AUTO_ION_REQUEST", "Запрос на авто‑ионизатор.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_REFRESHING_FRAGRANCE", 269160704, 2, "IFragrance.HVAC_FUNC_AUTO_REFRESHING_FRAGRANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_HEATING", 268764416, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_HEATING", "", new Value[] {
                new Value("AUTO_SEAT_HEATING_LEVEL_1", 268764417),
                new Value("AUTO_SEAT_HEATING_LEVEL_2", 268764418),
                new Value("AUTO_SEAT_HEATING_LEVEL_3", 268764419),
                new Value("AUTO_SEAT_HEATING_OFF", 0),
                new Value("AUTO_SEAT_HEATING_TIME_1", 268764673),
                new Value("AUTO_SEAT_HEATING_TIME_2", 268764674),
                new Value("AUTO_SEAT_HEATING_TIME_3", 268764675),
                new Value("AUTO_SEAT_HEATING_TIME_4", 268764676),
                new Value("AUTO_SEAT_HEATING_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_HEATING_TIME", 268764672, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_HEATING_TIME", "", new Value[] {
                new Value("AUTO_SEAT_HEATING_TIME_1", 268764673),
                new Value("AUTO_SEAT_HEATING_TIME_2", 268764674),
                new Value("AUTO_SEAT_HEATING_TIME_3", 268764675),
                new Value("AUTO_SEAT_HEATING_TIME_4", 268764676),
                new Value("AUTO_SEAT_HEATING_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_MASSAGE_TIME", 268765440, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_MASSAGE_TIME", "", new Value[] {
                new Value("AUTO_SEAT_MASSAGE_TIME_1", 268765441),
                new Value("AUTO_SEAT_MASSAGE_TIME_2", 268765442),
                new Value("AUTO_SEAT_MASSAGE_TIME_3", 268765443),
                new Value("AUTO_SEAT_MASSAGE_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_VENTILATION_TIME", 268764160, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_VENTILATION_TIME", "", new Value[] {
                new Value("AUTO_SEAT_VENTILATION_TIME_1", 268764161),
                new Value("AUTO_SEAT_VENTILATION_TIME_2", 268764162),
                new Value("AUTO_SEAT_VENTILATION_TIME_3", 268764163),
                new Value("AUTO_SEAT_VENTILATION_TIME_4", 268764164),
                new Value("AUTO_SEAT_VENTILATION_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT", 269025792, 2, "IHvac.HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT", "", new Value[] {
                new Value("AUTO_STEERING_WHEEL_HEAT_HIGH", 269025795),
                new Value("AUTO_STEERING_WHEEL_HEAT_LOW", 269025793),
                new Value("AUTO_STEERING_WHEEL_HEAT_MID", 269025794),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_1", 269026049),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_2", 269026050),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_3", 269026051),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT_SWITCH", 269026304, 2, "IHvac.HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT_TIME", 269026048, 2, "IHvac.HVAC_FUNC_AUTO_STEERING_WHEEL_HEAT_TIME", "", new Value[] {
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_1", 269026049),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_2", 269026050),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_3", 269026051),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_BLOWING_MODE", 268894464, 2, "IHvac.HVAC_FUNC_BLOWING_MODE", "Распределение потоков воздуха (лицо/ноги/стекло/авто).", new Value[] {
                new Value("BLOWING_MODE_AUTO_SWITCH", 268894472),
                new Value("BLOWING_MODE_FACE", 268894465),
                new Value("BLOWING_MODE_FACE_AND_FRONT_WINDOW", 268894469),
                new Value("BLOWING_MODE_FACE_AND_LEG", 268894467),
                new Value("BLOWING_MODE_FRONT_WINDOW", 268894468),
                new Value("BLOWING_MODE_LEG", 268894466),
                new Value("BLOWING_MODE_LEG_AND_FRONT_WINDOW", 268894470),
                new Value("BLOWING_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_BLOWING_MODE_HARD_KEY", 268896256, 2, "IHvac.HVAC_FUNC_BLOWING_MODE_HARD_KEY", "Событие клавиши выбора направления обдува.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CIRCULATION", 268632320, 2, "IHvac.HVAC_FUNC_CIRCULATION", "Режим рециркуляции: внутренняя/наружная/авто.", new Value[] {
                new Value("CIRCULATION_AUTO", 268632323),
                new Value("CIRCULATION_INNER", 268632321),
                new Value("CIRCULATION_OFF", 0),
                new Value("CIRCULATION_OUTSIDE", 268632322),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CIRCULATION_LONG_TOUCH", 268632832, 2, "IHvac.HVAC_FUNC_CIRCULATION_LONG_TOUCH", "Долгое нажатие на кнопку рециркуляции.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CIRCULATION_TIMER", 268632576, 2, "IHvac.HVAC_FUNC_CIRCULATION_TIMER", "Таймер переключения рециркуляции.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLIMATE_HARDKEY_SOUND", 269486080, 2, "IHvac.HVAC_FUNC_CLIMATE_HARDKEY_SOUND", "Звуковая индикация клавиш климата.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLIMATE_LOCK", 269484544, 2, "IHvac.HVAC_FUNC_CLIMATE_LOCK", "Блокировка управления климатом.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLIMATE_ZONE", 268502272, 2, "IHvac.HVAC_FUNC_CLIMATE_ZONE", "Количество климатических зон.", new Value[] {
                new Value("CLIMATE_ZONE_DUAL", 268502274),
                new Value("CLIMATE_ZONE_FOUR", 268502276),
                new Value("CLIMATE_ZONE_SINGLE", 268502273),
                new Value("CLIMATE_ZONE_TRIPLE", 268502275),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CO2_HIGHER_CONFIRM", 269353728, 2, "IHvac.HVAC_FUNC_CO2_HIGHER_CONFIRM", "Подтверждение предупреждения о повышенном CO₂.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_DEFROST_FRONT", 268697856, 2, "IHvac.HVAC_FUNC_DEFROST_FRONT", "Антизапотевание лобового стекла.", new Value[] {
                new Value("FRONTDEFROST_ON", 1),
                new Value("FRONTDEFROST_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_DEFROST_FRONT_MAX", 268698112, 2, "IHvac.HVAC_FUNC_DEFROST_FRONT_MAX", "Максимальный режим обдува/обогрева лобового стекла.", new Value[] {
                new Value("FRONTDEFROSTMAX_ON", 1),
                new Value("FRONTDEFROSTMAX_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_DEFROST_REAR", 268698368, 2, "IHvac.HVAC_FUNC_DEFROST_REAR", "Обогрев заднего стекла.", new Value[] {
                new Value("REARDEFROST_ON", 1),
                new Value("REARDEFROST_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_DIRECTION_MODE", 268894976, 2, "IHvac.HVAC_FUNC_DIRECTION_MODE", "Режим направленности обдува (Focus/Avoid/Custom).", new Value[] {
                new Value("DIRECTION_MODE_AVOID", 268894978),
                new Value("DIRECTION_MODE_CUSTOM", 268894723),
                new Value("DIRECTION_MODE_FOCUS", 268894977),
                new Value("DIRECTION_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_DISPLAY_WINDOW_TAB", 269484800, 2, "IHvac.HVAC_FUNC_DISPLAY_WINDOW_TAB", "Текущая вкладка окна климата.", new Value[] {
                new Value("DISPLAY_WINDOW_TAB_DEFAULT", 269484801),
                new Value("DISPLAY_WINDOW_TAB_HARDWARE_POP", 269484804),
                new Value("DISPLAY_WINDOW_TAB_IONS_POP", 269484806),
                new Value("DISPLAY_WINDOW_TAB_LEFT_TEMP", 269484802),
                new Value("DISPLAY_WINDOW_TAB_NONE", 0),
                new Value("DISPLAY_WINDOW_TAB_RIGHT_TEMP", 269484803),
                new Value("DISPLAY_WINDOW_TAB_SEAT", 269484805),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_ECO_SWITCH", 268960000, 2, "IHvac.HVAC_FUNC_ECO_SWITCH", "Эко‑режим A/C (снижение энергопотребления).", new Value[] {
                new Value("ACECO_ON", 1),
                new Value("ACECO_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_ELECTRICAL_AIR_VENT", 269746432, 2, "IHvac.HVAC_FUNC_ELECTRICAL_AIR_VENT", "Электропривод заслонок воздуховодов.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_FAN_SPEED", 268566784, 2, "IHvac.HVAC_FUNC_FAN_SPEED", "Скорость вентилятора климат‑системы.", new Value[] {
                new Value("FAN_SPEED_LEVEL_1", 268566785),
                new Value("FAN_SPEED_LEVEL_2", 268566786),
                new Value("FAN_SPEED_LEVEL_3", 268566787),
                new Value("FAN_SPEED_LEVEL_4", 268566788),
                new Value("FAN_SPEED_LEVEL_5", 268566789),
                new Value("FAN_SPEED_LEVEL_6", 268566790),
                new Value("FAN_SPEED_LEVEL_7", 268566791),
                new Value("FAN_SPEED_LEVEL_8", 268566792),
                new Value("FAN_SPEED_LEVEL_9", 268566793),
                new Value("FAN_SPEED_LEVEL_AUTO", 268566794),
                new Value("FAN_SPEED_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_FAN_SPEED_BLOWER", 269752064, 2, "IHvac.HVAC_FUNC_FAN_SPEED_BLOWER", "Команда изменить скорость вентилятора.", new Value[] {
                new Value("HVAC_FUNC_FAN_SPEED_BLOWER_DOWN", 269752066),
                new Value("HVAC_FUNC_FAN_SPEED_BLOWER_UP", 269752065),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_FAN_SPEED_HARD_KEY", 268567296, 2, "IHvac.HVAC_FUNC_FAN_SPEED_HARD_KEY", "Событие клавиши скорости вентилятора.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_FILTER_ELEMENT_LIFE", 269746944, 2, "IHvac.HVAC_FUNC_FILTER_ELEMENT_LIFE", "Оставшийся ресурс салонного фильтра.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_G_CLEAN", 269485056, 2, "IHvac.HVAC_FUNC_G_CLEAN", "Интенсивная очистка воздуха (G‑Clean/IAPS).", new Value[] {
                new Value("GCLEAN_ON", 1),
                new Value("GCLEAN_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_INTELLIGENT_DEODORIZATION", 269748224, 2, "IHvac.HVAC_FUNC_INTELLIGENT_DEODORIZATION", "Интеллектуальная дезодорация салона.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_INTELLIGENT_RECOMMENDATION", 269615360, 2, "IHvac.HVAC_FUNC_INTELLIGENT_RECOMMENDATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_IONIZER_CLS_WIN_POPUP", 269751552, 2, "IHvac.HVAC_FUNC_IONIZER_CLS_WIN_POPUP", "Поп‑ап «Закройте окна» для ионизатора.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_IONIZER_CLS_WIN_POPUP_SETTING", 269751296, 2, "IHvac.HVAC_FUNC_IONIZER_CLS_WIN_POPUP_SETTING", "Настройка поп‑апа «Закройте окна».", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_IONS_SWITCH", 268961024, 2, "IHvac.HVAC_FUNC_IONS_SWITCH", "Ионизатор воздуха: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_OVERHEAT_PROTECTION", 268960768, 2, "IHvac.HVAC_FUNC_OVERHEAT_PROTECTION", "Защита салона от перегрева на стоянке.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_POST_CLIMATISATION", 269091328, 2, "IHvac.HVAC_FUNC_POST_CLIMATISATION", "Постклиматизация после поездки.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_POWER", 268501248, 2, "IHvac.HVAC_FUNC_POWER", "Главный выключатель климат‑системы.", new Value[] {
                new Value("POWER_CHARGE_MODE_FAIL", 606078979),
                new Value("POWER_CHARGE_MODE_FINISH", 606078980),
                new Value("POWER_CHARGE_MODE_FUEL_LOW", 606078981),
                new Value("POWER_CHARGE_MODE_OFF", 606078978),
                new Value("POWER_CHARGE_MODE_ON", 606078977),
                new Value("POWER_CHARGE_MODE_TIMEOUT", 606078982),
                new Value("POWER_FLOW_BOOST", 604045570),
                new Value("POWER_FLOW_CHARGE_AC", 604045582),
                new Value("POWER_FLOW_CHARGE_DC", 604045583),
                new Value("POWER_FLOW_DISCHARGE", 604045584),
                new Value("POWER_FLOW_DRIVEN_BY_ELECTRIC_MOTOR_AND_ENGINE", 604045592),
                new Value("POWER_FLOW_EAWD", 604045571),
                new Value("POWER_FLOW_ELEC", 604045574),
                new Value("POWER_FLOW_ENGINEOFF_REGBRAKE", 604045578),
                new Value("POWER_FLOW_ENGINEONLY", 604045572),
                new Value("POWER_FLOW_ENGINEONLY_CHARGE", 604045573),
                new Value("POWER_FLOW_ENGINEON_REGBRAKE", 604045579),
                new Value("POWER_FLOW_ENGINEON_REGBRAKE_CHARGE", 604045580),
                new Value("POWER_FLOW_FRONT_ELE_DRIVE", 604045586),
                new Value("POWER_FLOW_MAIN_CHARGE", 604045569),
                new Value("POWER_FLOW_NOT_READY", 0),
                new Value("POWER_FLOW_PURE_ELE_AWD", 604045585),
                new Value("POWER_FLOW_REAR_ELE_DRIVE", 604045587),
                new Value("POWER_FLOW_REGENERATION", 604045589),
                new Value("POWER_FLOW_SAILING", 604045581),
                new Value("POWER_FLOW_STANDSTILL", 604045588),
                new Value("POWER_FLOW_STANDSTILL_AND_BOTH_EM_ENGINE_OFF", 604045590),
                new Value("POWER_FLOW_STANDSTILL_ENGINE_ON_WITH_ISG", 604045591),
                new Value("POWER_FLOW_STILL_ENGINEOFF", 604045575),
                new Value("POWER_FLOW_STILL_ENGINEON", 604045576),
                new Value("POWER_FLOW_STILL_ENGINEON_CHARGE", 604045577),
                new Value("POWER_TRAIN_STOP_EV_BLOCKED", 570691585),
                new Value("POWER_TRAIN_STOP_EV_PLUS_BLOCKED", 570691587),
                new Value("POWER_TRAIN_STOP_HEV_BLOCKED", 570691586),
                new Value("POWER_TRAIN_STOP_NOT_BLOCKED", 570691584),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_POWER_VR", 268505344, 2, "IHvac.HVAC_FUNC_POWER_VR", "Голосовое включение/выключение климата.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_PRE_CLIMATISATION", 269091072, 2, "IHvac.HVAC_FUNC_PRE_CLIMATISATION", "Предклиматизация перед поездкой.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_REFRESHING_FRAGRANCE_POP", 269160960, 2, "IFragrance.HVAC_FUNC_REFRESHING_FRAGRANCE_POP", "Уведомление об ароматизации.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_RESET_FILTER_ELEMENT_LIFE", 269750272, 2, "IHvac.HVAC_FUNC_RESET_FILTER_ELEMENT_LIFE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_HEATING", 268763648, 2, "IHvac.HVAC_FUNC_SEAT_HEATING", "Подогрев сиденья (уровни/авто).", new Value[] {
                new Value("SEAT_HEATING_LEVEL_1", 268763649),
                new Value("SEAT_HEATING_LEVEL_2", 268763650),
                new Value("SEAT_HEATING_LEVEL_3", 268763651),
                new Value("SEAT_HEATING_LEVEL_AUTO", 268763663),
                new Value("SEAT_HEATING_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_HEATING_LVLAUTO", 269751040, 2, "IHvac.HVAC_FUNC_SEAT_HEATING_LVLAUTO", "Авто‑уровень подогрева сиденья.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_MASSAGE", 268764928, 2, "IHvac.HVAC_FUNC_SEAT_MASSAGE", "Массаж сиденья (уровни/программы).", new Value[] {
                new Value("SEAT_MASSAGE_ADJUST", 759236612),
                new Value("SEAT_MASSAGE_LEVEL_1", 268764929),
                new Value("SEAT_MASSAGE_LEVEL_2", 268764930),
                new Value("SEAT_MASSAGE_LEVEL_3", 268764931),
                new Value("SEAT_MASSAGE_LEVEL_AUTO", 268764943),
                new Value("SEAT_MASSAGE_OFF", 0),
                new Value("SEAT_MASSAGE_PROGRAM_1", 268765953),
                new Value("SEAT_MASSAGE_PROGRAM_2", 268765954),
                new Value("SEAT_MASSAGE_PROGRAM_3", 268765955),
                new Value("SEAT_MASSAGE_PROGRAM_4", 268765956),
                new Value("SEAT_MASSAGE_PROGRAM_5", 268765957),
                new Value("SEAT_MASSAGE_PROGRAM_6", 268765958),
                new Value("SEAT_MASSAGE_PROGRAM_7", 268765959),
                new Value("SEAT_MASSAGE_PROGRAM_8", 268765960),
                new Value("SEAT_MASSAGE_PROGRAM_9", 268765961),
                new Value("SEAT_MASSAGE_PROGRAM_A", 268765962),
                new Value("SEAT_MASSAGE_PROGRAM_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_MASSAGE_PROGRAM", 268765952, 2, "IHvac.HVAC_FUNC_SEAT_MASSAGE_PROGRAM", "Выбор программы массажа сиденья.", new Value[] {
                new Value("SEAT_MASSAGE_PROGRAM_1", 268765953),
                new Value("SEAT_MASSAGE_PROGRAM_2", 268765954),
                new Value("SEAT_MASSAGE_PROGRAM_3", 268765955),
                new Value("SEAT_MASSAGE_PROGRAM_4", 268765956),
                new Value("SEAT_MASSAGE_PROGRAM_5", 268765957),
                new Value("SEAT_MASSAGE_PROGRAM_6", 268765958),
                new Value("SEAT_MASSAGE_PROGRAM_7", 268765959),
                new Value("SEAT_MASSAGE_PROGRAM_8", 268765960),
                new Value("SEAT_MASSAGE_PROGRAM_9", 268765961),
                new Value("SEAT_MASSAGE_PROGRAM_A", 268765962),
                new Value("SEAT_MASSAGE_PROGRAM_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_MASSAGE_SWITCH", 268765696, 2, "IHvac.HVAC_FUNC_SEAT_MASSAGE_SWITCH", "Вкл/выкл массаж сиденья.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_VENTILATION", 268763392, 2, "IHvac.HVAC_FUNC_SEAT_VENTILATION", "Вентиляция сиденья (уровни/авто).", new Value[] {
                new Value("SEAT_VENTILATION_LEVEL_1", 268763393),
                new Value("SEAT_VENTILATION_LEVEL_2", 268763394),
                new Value("SEAT_VENTILATION_LEVEL_3", 268763395),
                new Value("SEAT_VENTILATION_LEVEL_AUTO", 268763407),
                new Value("SEAT_VENTILATION_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SEAT_VENTILATION_LVLAUTO", 269750784, 2, "IHvac.HVAC_FUNC_SEAT_VENTILATION_LVLAUTO", "Авто‑уровень вентиляции сиденья.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_STEERING_WHEEL_HEAT", 269025536, 2, "IHvac.HVAC_FUNC_STEERING_WHEEL_HEAT", "Подогрев рулевого колеса (уровни/авто).", new Value[] {
                new Value("STEERING_WHEEL_HEAT_AUTO", 269025551),
                new Value("STEERING_WHEEL_HEAT_HIGH", 269025539),
                new Value("STEERING_WHEEL_HEAT_LOW", 269025537),
                new Value("STEERING_WHEEL_HEAT_MID", 269025538),
                new Value("STEERING_WHEEL_HEAT_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SWEEPING_MODE", 268894720, 2, "IHvac.HVAC_FUNC_SWEEPING_MODE", "Маятниковое перемещение жалюзи (качание).", new Value[] {
                new Value("SWEEPING_MODE_LEFT_RIGHT", 268894721),
                new Value("SWEEPING_MODE_LR_AND_UD", 268894723),
                new Value("SWEEPING_MODE_OFF", 0),
                new Value("SWEEPING_MODE_UP_DOWN", 268894722),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP", 268828928, 2, "IHvac.HVAC_FUNC_TEMP", "Установка требуемой температуры.", new Value[] {
                new Value("DISPLAY_WINDOW_TAB_LEFT_TEMP", 269484802),
                new Value("DISPLAY_WINDOW_TAB_RIGHT_TEMP", 269484803),
                new Value("TEMPERATURE_UNIT_C", 268830209),
                new Value("TEMPERATURE_UNIT_F", 268830210),
                new Value("UNIT_TEMPERATURE_C", 620823297),
                new Value("UNIT_TEMPERATURE_F", 620823298),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_DUAL", 268829952, 2, "IHvac.HVAC_FUNC_TEMP_DUAL", "Синхронизация/развязка температур по зонам.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_HARD_KEY", 268830464, 2, "IHvac.HVAC_FUNC_TEMP_HARD_KEY", "Событие клавиши температуры.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_MAX", 268829184, 2, "IHvac.HVAC_FUNC_TEMP_MAX", "Верхний предел уставки температуры.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_MIN", 268829440, 2, "IHvac.HVAC_FUNC_TEMP_MIN", "Нижний предел уставки температуры.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_OPTIMIZE", 269615616, 2, "IHvac.HVAC_FUNC_TEMP_OPTIMIZE", "Оптимизация регулировки температуры (AUTO).", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_STEP", 268829696, 2, "IHvac.HVAC_FUNC_TEMP_STEP", "Шаг изменения уставки температуры.", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_TEMP_UNIT", 268830208, 2, "IHvac.HVAC_FUNC_TEMP_UNIT", "Единицы температуры для климата.", new Value[] {
                new Value("TEMPERATURE_UNIT_C", 268830209),
                new Value("TEMPERATURE_UNIT_F", 268830210),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_VENTILATION_ONTIME", 269485824, 2, "IHvac.HVAC_FUNC_VENTILATION_ONTIME", "Запланированная вентиляция салона по времени.", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_BATTERY_CHARGE_MODE", 604111360, 2, "IHybrid.HYBRID_FUNC_BATTERY_CHARGE_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_BATTERY_SAVE_MODE", 604111104, 2, "IHybrid.HYBRID_FUNC_BATTERY_SAVE_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_BATTERY_SOC", 604176640, 2, "IHybrid.HYBRID_FUNC_BATTERY_SOC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_CHARGED_QUANTITY_INFO", 826279680, 2, "ISuperHybrid.HYBRID_FUNC_CHARGED_QUANTITY_INFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_CHARGE_IMMEDIATELY", 826278144, 2, "ISuperHybrid.HYBRID_FUNC_CHARGE_IMMEDIATELY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_CHARGING_DURATION_INFO", 826279936, 2, "ISuperHybrid.HYBRID_FUNC_CHARGING_DURATION_INFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_CHARGING_POWER_INFO", 826279424, 2, "ISuperHybrid.HYBRID_FUNC_CHARGING_POWER_INFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_DISCHARGING_DURATION_INFO", 826280448, 2, "ISuperHybrid.HYBRID_FUNC_DISCHARGING_DURATION_INFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_DISCHARGING_POWER_INFO", 606080256, 2, "ISuperHybrid.HYBRID_FUNC_DISCHARGING_POWER_INFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_DISTANCE_PROTECTION_SWITCH", 827326720, 2, "ISuperHybrid.HYBRID_FUNC_DISTANCE_PROTECTION_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_ELECTRIC_AND_HYBRID_SELECT", 604242176, 2, "IHybrid.HYBRID_FUNC_ELECTRIC_AND_HYBRID_SELECT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_ESTIMD_FU_SAVE", 604242944, 2, "IHybrid.HYBRID_FUNC_ESTIMD_FU_SAVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_MAX_EV_MODE", 604242432, 2, "IHybrid.HYBRID_FUNC_MAX_EV_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_MAX_EV_MODE_POP", 604242688, 2, "IHybrid.HYBRID_FUNC_MAX_EV_MODE_POP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_PARKING_POWER_GENERATION", 826278400, 2, "ISuperHybrid.HYBRID_FUNC_PARKING_POWER_GENERATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_POWER_FLOW", 604045568, 2, "IHybrid.HYBRID_FUNC_POWER_FLOW", "", new Value[] {
                new Value("POWER_FLOW_BOOST", 604045570),
                new Value("POWER_FLOW_CHARGE_AC", 604045582),
                new Value("POWER_FLOW_CHARGE_DC", 604045583),
                new Value("POWER_FLOW_DISCHARGE", 604045584),
                new Value("POWER_FLOW_DRIVEN_BY_ELECTRIC_MOTOR_AND_ENGINE", 604045592),
                new Value("POWER_FLOW_EAWD", 604045571),
                new Value("POWER_FLOW_ELEC", 604045574),
                new Value("POWER_FLOW_ENGINEOFF_REGBRAKE", 604045578),
                new Value("POWER_FLOW_ENGINEONLY", 604045572),
                new Value("POWER_FLOW_ENGINEONLY_CHARGE", 604045573),
                new Value("POWER_FLOW_ENGINEON_REGBRAKE", 604045579),
                new Value("POWER_FLOW_ENGINEON_REGBRAKE_CHARGE", 604045580),
                new Value("POWER_FLOW_FRONT_ELE_DRIVE", 604045586),
                new Value("POWER_FLOW_MAIN_CHARGE", 604045569),
                new Value("POWER_FLOW_NOT_READY", 0),
                new Value("POWER_FLOW_PURE_ELE_AWD", 604045585),
                new Value("POWER_FLOW_REAR_ELE_DRIVE", 604045587),
                new Value("POWER_FLOW_REGENERATION", 604045589),
                new Value("POWER_FLOW_SAILING", 604045581),
                new Value("POWER_FLOW_STANDSTILL", 604045588),
                new Value("POWER_FLOW_STANDSTILL_AND_BOTH_EM_ENGINE_OFF", 604045590),
                new Value("POWER_FLOW_STANDSTILL_ENGINE_ON_WITH_ISG", 604045591),
                new Value("POWER_FLOW_STILL_ENGINEOFF", 604045575),
                new Value("POWER_FLOW_STILL_ENGINEON", 604045576),
                new Value("POWER_FLOW_STILL_ENGINEON_CHARGE", 604045577),
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_PT_MOD", 823132416, 2, "ISuperHybrid.HYBRID_FUNC_PT_MOD", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_SMART_ENERGY_MANAGER", 604242176, 2, "IHybrid.HYBRID_FUNC_SMART_ENERGY_MANAGER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HYBRID_FUNC_TOT_FU_SAVE", 604243200, 2, "IHybrid.HYBRID_FUNC_TOT_FU_SAVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_ADAS_PADDLE_LANE_CHANGE_ASSIST_AVAILABLE", 1051904, 1, "ICarInfo.INT_INFO_ADAS_PADDLE_LANE_CHANGE_ASSIST_AVAILABLE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_CONFIG_466", 1052163, 1, "ICarInfo.INT_INFO_CONFIG_466", "Параметр конфигурации (код 466).", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_CRUISE_CONTROL_CC", 1057280, 1, "ICarInfo.INT_INFO_CRUISE_CONTROL_CC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_CSD_VARIANTS", 1057024, 1, "ICarInfo.INT_INFO_CSD_VARIANTS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_DRIVER_ASSISTANCE_SYSTEM", 1056768, 1, "ICarInfo.INT_INFO_HIGHWAY_ASSIST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_HIGHWAY_ASSIST", 1052161, 1, "ICarInfo.INT_INFO_HIGHWAY_ASSIST", "Конфигурация функции Highway Assist.", new Value[] {
                new Value("HIGHWAY_ASSIST_AUTO_ALLOWED_HANDS", 132),
                new Value("HIGHWAY_ASSIST_AUTO_HWA_NOT_HANDS", 131),
                new Value("HIGHWAY_ASSIST_AUTO_NOT_HANDS", 130),
                new Value("HIGHWAY_ASSIST_INCLUDED_HANDS", 4),
                new Value("HIGHWAY_ASSIST_INCLUDED_NOT_HANDS", 3),
                new Value("HIGHWAY_ASSIST_NAVI_HWA_NOT_HANDS", 133),
                new Value("HIGHWAY_ASSIST_NOT_AUTO_NOT_HANDS", 129),
                new Value("HIGHWAY_ASSIST_NOT_INCLUDED_NOT_HANDS", 2),
        }));
        put(byId, byKey, new Entry("INT_INFO_MAINTENANCE_TYPE", 1051648, 1, "ICarInfo.INT_INFO_MAINTENANCE_TYPE", "", new Value[] {
                new Value("MAINTENANCE_TYPE_REGULAR", 1051903),
                new Value("MAINTENANCE_TYPE_REGULAR_AND_ENGINE", 1051649),
        }));
        put(byId, byKey, new Entry("INT_INFO_MIC_TOTAL_COUNT", 1049856, 1, "ICarInfo.INT_INFO_MIC_TOTAL_COUNT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("INT_INFO_VEHICLE_TYPES", 1049088, 1, "ICarInfo.INT_INFO_VEHICLE_TYPES", "", new Value[] {
        }));
        put(byId, byKey, new Entry("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644, 2, "IVehicle.LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", "Наружный свет: автоматический дальний (AHBC).", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644),
        }));
        put(byId, byKey, new Entry("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643, 2, "IVehicle.LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", "Наружный свет: автоматический режим.", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643),
        }));
        put(byId, byKey, new Entry("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642, 2, "IVehicle.LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", "Наружный свет: ближний свет.", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642),
        }));
        put(byId, byKey, new Entry("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0, 2, "IVehicle.LAMP_EXTERIOR_LIGHT_CONTROL_OFF", "Наружный свет: выключено.", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0),
        }));
        put(byId, byKey, new Entry("LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", 537136641, 2, "IVehicle.LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", "Наружный свет: габаритные огни.", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", 537136641),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", 537136641),
        }));
        put(byId, byKey, new Entry("PAS_FUNC_APA_SELF_RECOMMENDED", 587596032, 2, "IPAS.PAS_FUNC_APA_SELF_RECOMMENDED", "Рекомендация места для автопарковки (APA).", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_AUT_PRKG_SLOT_NR_REQ", 588252672, 2, "IPAS.PAS_FUNC_AUT_PRKG_SLOT_NR_REQ", "Запрос/выбор номера парковочного места.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_DRVR_ASSC_SYS_BTN_PUSH", 588252416, 2, "IPAS.PAS_FUNC_DRVR_ASSC_SYS_BTN_PUSH", "События кнопки ассистента парковки.", new Value[] {
                new Value("DRVR_ASSC_SYS_BTN_PUSH_ABORT", 588252428),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_CONFIRM_BTN", 588252424),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_CONFIRM_PARK_OUT", 588252426),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_ENTER_APA", 588252425),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_ENTER_APA_OR_AVM", 588252422),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_EXIT_APA", 588252421),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_MANUAL_BTN", 588252423),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_SELT_APA", 588252417),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_SELT_RPA", 588252418),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_START_PARK", 588252420),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_SUSPEND", 588252427),
                new Value("DRVR_ASSC_SYS_BTN_PUSH_UNDO_BTN", 588252419),
        }));
        put(byId, byKey, new Entry("PAS_FUNC_DRVR_ASSC_SYS_PARK_MOD", 588252928, 2, "IPAS.PAS_FUNC_DRVR_ASSC_SYS_PARK_MOD", "Выбор сценария парковки (паралл./перпендикуляр./выезд).", new Value[] {
                new Value("DRVR_ASSC_SYS_PARK_MOD_CANCEL", 588252929),
                new Value("DRVR_ASSC_SYS_PARK_MOD_DEFAULT", 2),
                new Value("DRVR_ASSC_SYS_PARK_MOD_HORIZ_LEFT_PARK_OUT", 588252937),
                new Value("DRVR_ASSC_SYS_PARK_MOD_HORIZ_PARK_IN", 588252930),
                new Value("DRVR_ASSC_SYS_PARK_MOD_HORIZ_RIGHT_PARK_OUT", 588252938),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_LEFT_PARK_OUT_BW", 588252941),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_LEFT_PARK_OUT_FW", 588252939),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN", 588252931),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN_BW", 588252933),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN_FW", 588252932),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_RIGHT_PARK_OUT_BW", 588252942),
                new Value("DRVR_ASSC_SYS_PARK_MOD_PERPDIR_RIGHT_PARK_OUT_FW", 588252940),
                new Value("DRVR_ASSC_SYS_PARK_MOD_RESERVE_15", 588252943),
                new Value("DRVR_ASSC_SYS_PARK_MOD_RESERVE_6", 588252934),
                new Value("DRVR_ASSC_SYS_PARK_MOD_RESERVE_7", 588252935),
                new Value("DRVR_ASSC_SYS_PARK_MOD_RESERVE_8", 588252936),
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_3DVIEW_LOCK", 587404288, 2, "IPAS.PAS_FUNC_PAC_3DVIEW_LOCK", "Фиксация 3D‑вида камеры.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_ACTIVATION", 587399424, 2, "IPAS.PAS_FUNC_PAC_ACTIVATION", "Активация камер парковочного ассистента.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_CAR_MODE_TRANSPARENT", 587407616, 2, "IPAS.PAS_FUNC_PAC_CAR_MODE_TRANSPARENT", "Режим «прозрачного шасси».", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_STEER_LINK", 587399680, 2, "IPAS.PAS_FUNC_PAC_STEER_LINK", "Связка траекторных линий с углом руля.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_ACTIVATED", 537723136, 2, "IPAS.PAS_FUNC_PAS_ACTIVATED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_MUTE", 587268608, 2, "IPAS.PAS_FUNC_PAS_MUTE", "Отключение звука предупреждений парковочных датчиков.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_TRAILER_PRESENT", 587268864, 2, "IPAS.PAS_FUNC_PAS_TRAILER_PRESENT", "Режим прицепа для систем парковки.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_VOLUME", 537723392, 2, "IPAS.PAS_FUNC_PAS_VOLUME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PRKG_INTRPT_RELD_BTN", 588253184, 2, "IPAS.PAS_FUNC_PRKG_INTRPT_RELD_BTN", "Кнопка прерывания процесса парковки.", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_RCTA_WARNING_VOLUME", 587531520, 2, "IPAS.PAS_FUNC_RCTA_WARNING_VOLUME", "Громкость предупреждений RCTA.", new Value[] {
                new Value("RCTA_WARNING_VOLUME_HIGH", 587531523),
                new Value("RCTA_WARNING_VOLUME_LOW", 587531521),
                new Value("RCTA_WARNING_VOLUME_MID", 587531522),
                new Value("RCTA_WARNING_VOLUME_OFF", 0),
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_ACTIVATION", 587464960, 2, "IPAS.PAS_FUNC_SAP_ACTIVATION", "Активация функции удалённой/смарт‑парковки (SAP).", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_CSD_DRIVER_THEATER_MODE", 788664080, 2, "ISceneMode.SCENE_FUNC_CSD_DRIVER_THEATER_MODE", "Сценарий: «театральный режим» для водителя.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_CSD_PASSENGER_THEATER_MODE", 788664096, 2, "ISceneMode.SCENE_FUNC_CSD_PASSENGER_THEATER_MODE", "Сценарий: «театральный режим» для пассажира.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_NAP_MODE", 788662272, 2, "ISceneMode.SCENE_FUNC_NAP_MODE", "Сценарий: режим сна/отдыха.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_PSD_PASSENGER_THEATER_MODE", 788664112, 2, "ISceneMode.SCENE_FUNC_PSD_PASSENGER_THEATER_MODE", "Сценарий: «театральный режим» (PSD).", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_ADJMT_REQ", 788664320, 2, "ISceneMode.SCENE_FUNC_SEAT_ADJMT_REQ", "Сценарий: запрос на автонастройку сиденья.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_BACK_TARG_POS_AG", 788664576, 2, "ISceneMode.SCENE_FUNC_SEAT_BACK_TARG_POS_AG", "Целевая позиция наклона спинки сиденья (угол).", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_CUSH_EXT_TARG_POS_PERC", 788665600, 2, "ISceneMode.SCENE_FUNC_SEAT_CUSH_EXT_TARG_POS_PERC", "Целевая длина выдвижной подушки, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_CUSH_TILT_TARG_POS_PERC", 788665344, 2, "ISceneMode.SCENE_FUNC_SEAT_CUSH_TILT_TARG_POS_PERC", "Целевой наклон подушки, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_HEI_TARG_POS_PERC", 788665088, 2, "ISceneMode.SCENE_FUNC_SEAT_HEI_TARG_POS_PERC", "Целевая высота сиденья, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_SEAT_LEN_TARG_POS_PERC", 788664832, 2, "ISceneMode.SCENE_FUNC_SEAT_LEN_TARG_POS_PERC", "Целевая продольная позиция сиденья, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SCENE_FUNC_WASH_MODE", 788595200, 2, "ISceneMode.SCENE_FUNC_WASH_MODE", "Сценарий: режим мойки автомобиля.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ABS_WARNING", 1058304, 3, "ISensor.SENSOR_TYPE_ABS_WARNING", "Индикатор ABS (вкл/выкл/мигает).", new Value[] {
                new Value("ABS_WARNING_STATE_FLSG", 1058306),
                new Value("ABS_WARNING_STATE_OFF", 1058308),
                new Value("ABS_WARNING_STATE_ON", 1058305),
                new Value("ABS_WARNING_STATE_RESD", 1058307),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ACCELERATOR_DEPTH", 1053696, 3, "ISensor.SENSOR_TYPE_ACCELERATOR_DEPTH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ALRM_STS", 2122496, 3, "ISensor.SENSOR_TYPE_ALRM_STS", "Состояние охранной сигнализации.", new Value[] {
                new Value("SENSOR_VALUE_ALRM_STS_ACTV", 2122499),
                new Value("SENSOR_VALUE_ALRM_STS_ARMD", 2122498),
                new Value("SENSOR_VALUE_ALRM_STS_DISARMD", 2122497),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_LEVEL_AMBIENT", 2106112, 3, "ISensor.SENSOR_TYPE_AQI_LEVEL_AMBIENT", "Качество наружного воздуха (AQI).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_BRAKE_DEPTH", 1053440, 3, "ISensor.SENSOR_TYPE_BRAKE_DEPTH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_BRAKE_FLUID_LEVEL", 2098688, 3, "ISensor.SENSOR_TYPE_BRAKE_FLUID_LEVEL", "", new Value[] {
                new Value("BRAKE_FLUID_LEVEL_LOW", 2098690),
                new Value("BRAKE_FLUID_LEVEL_NORMAL", 2098689),
                new Value("BRAKE_FLUID_LEVEL_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_BRK_WARNING", 1058048, 3, "ISensor.SENSOR_TYPE_BRK_WARNING", "Индикатор тормозной системы.", new Value[] {
                new Value("BRK_WARNING_STATE_OFF", 1058050),
                new Value("BRK_WARNING_STATE_ON", 1058049),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CAR_MODE", 2102272, 3, "ISensor.SENSOR_TYPE_CAR_MODE", "", new Value[] {
                new Value("CAR_MODE_CRASH", 2102276),
                new Value("CAR_MODE_DYNO", 2102277),
                new Value("CAR_MODE_FACTORY", 2102274),
                new Value("CAR_MODE_NORMAL", 2102273),
                new Value("CAR_MODE_TRANSPORT", 2102275),
                new Value("CAR_MODE_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CAR_SPEED", 1048832, 3, "ISensor.SENSOR_TYPE_CAR_SPEED", "Скорость автомобиля (датчик).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CAR_SPEED_FROM_IPK", 1055232, 3, "ISensor.SENSOR_TYPE_CAR_SPEED_FROM_IPK", "Скорость по данным комбинации приборов.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DAY_NIGHT", 2101248, 3, "ISensor.SENSOR_TYPE_DAY_NIGHT", "Режим день/ночь (по датчику света).", new Value[] {
                new Value("DAY_NIGHT_MODE_DAY", 2101249),
                new Value("DAY_NIGHT_MODE_NIGHT", 2101250),
                new Value("DAY_NIGHT_MODE_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DRIVER_TIREDNESS_STATUS", 3149824, 3, "ISensor.SENSOR_TYPE_DRIVER_TIREDNESS_STATUS", "Индикатор усталости/внимания водителя.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DRVR_SEAT_BACKREST_PERC", 1058560, 3, "ISensor.SENSOR_TYPE_DRVR_SEAT_BACKREST_PERC", "Положение спинки сиденья водителя, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DRVR_SEAT_CUSHION_PERC", 1058304, 3, "ISensor.SENSOR_TYPE_DRVR_SEAT_CUSHION_PERC", "Положение подушки сиденья водителя, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DRVR_SEAT_HEIGHT_PERC", 1057792, 3, "ISensor.SENSOR_TYPE_DRVR_SEAT_HEIGHT_PERC", "Высота сиденья водителя, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_DRVR_SEAT_LENGTH_PERC", 1058048, 3, "ISensor.SENSOR_TYPE_DRVR_SEAT_LENGTH_PERC", "Продольное положение сиденья водителя, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENDURANCE_MILEAGE", 1050624, 3, "ISensor.SENSOR_TYPE_ENDURANCE_MILEAGE", "Оценочный запас хода (общий).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENDURANCE_MILEAGE_EV", 1054976, 3, "ISensor.SENSOR_TYPE_ENDURANCE_MILEAGE_EV", "Оценочный запас хода (EV‑режим).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENDURANCE_MILEAGE_FUEL", 1054720, 3, "ISensor.SENSOR_TYPE_ENDURANCE_MILEAGE_FUEL", "Оценочный запас хода (на топливе).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_COOLANT_LEVEL", 2098432, 3, "ISensor.SENSOR_TYPE_ENGINE_COOLANT_LEVEL", "Уровень охлаждающей жидкости.", new Value[] {
                new Value("ENGINE_COOLANT_LEVEL_LOW", 2098434),
                new Value("ENGINE_COOLANT_LEVEL_LOW_1", 2098435),
                new Value("ENGINE_COOLANT_LEVEL_NORMAL", 2098433),
                new Value("ENGINE_COOLANT_LEVEL_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_COOLANT_TEMPERATURE", 1052416, 3, "ISensor.SENSOR_TYPE_WARN_TRANSMISSION_TEMP_HIGH", "Температура охлаждающей жидкости (датчик).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_OIL_LEVEL", 2098176, 3, "ISensor.SENSOR_TYPE_ENGINE_OIL_LEVEL", "Уровень моторного масла.", new Value[] {
                new Value("ENGINE_OIL_LEVEL_HIGH", 2098180),
                new Value("ENGINE_OIL_LEVEL_LOW_1", 2098178),
                new Value("ENGINE_OIL_LEVEL_LOW_2", 2098179),
                new Value("ENGINE_OIL_LEVEL_OK", 2098177),
                new Value("ENGINE_OIL_LEVEL_RESD", 2098182),
                new Value("ENGINE_OIL_LEVEL_SRVRQRD", 2098181),
                new Value("ENGINE_OIL_LEVEL_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_OIL_PERC", 1057792, 3, "ISensor.SENSOR_TYPE_ENGINE_OIL_PERC", "Остаточный ресурс моторного масла, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_STATE", 2102784, 2, "ISensor.SENSOR_TYPE_ENGINE_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ESC_WARNING", 1058560, 3, "ISensor.SENSOR_TYPE_ESC_WARNING", "Индикатор системы стабилизации (ESC).", new Value[] {
                new Value("ESC_WARNING_STATE_FLSG", 1058562),
                new Value("ESC_WARNING_STATE_OFF", 1058564),
                new Value("ESC_WARNING_STATE_ON", 1058561),
                new Value("ESC_WARNING_STATE_RESD", 1058563),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_EV_BATTERY_LEVEL", 1051136, 3, "ISensor.SENSOR_TYPE_EV_BATTERY_LEVEL", "Уровень заряда HV‑батареи (SoC).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_FUEL_LEVEL", 1050112, 3, "ISensor.SENSOR_TYPE_FUEL_LEVEL", "Уровень топлива (датчик).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_GEAR", 2097664, 3, "ISensor.SENSOR_TYPE_GEAR", "Индикация выбранной передачи.", new Value[] {
                new Value("GEAR_LVL_ONE", 609225730),
                new Value("GEAR_LVL_THREE", 609225732),
                new Value("GEAR_LVL_TWO", 609225731),
                new Value("GEAR_NO_INDICATION", 609225729),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_IGNITION_STATE", 2097408, 3, "ISensor.SENSOR_TYPE_IGNITION_STATE", "Состояние зажигания.", new Value[] {
                new Value("IGNITION_STATE_ACC", 2097412),
                new Value("IGNITION_STATE_DRIVING", 2097415),
                new Value("IGNITION_STATE_LOCK", 2097410),
                new Value("IGNITION_STATE_OFF", 2097411),
                new Value("IGNITION_STATE_ON", 2097413),
                new Value("IGNITION_STATE_START", 2097414),
                new Value("IGNITION_STATE_UNDEFINED", 2097409),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_LIGHT", 2100992, 3, "ISensor.SENSOR_TYPE_LIGHT", "Освещённость (датчик света).", new Value[] {
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_COLOR", 705168900),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_MORE", 705168897),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_MUSIC", 705168898),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_SCREEN", 705168899),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_TIME", 705168901),
                new Value("AMBIENCE_LIGHT_CUSTOM_MODE_BREATHE", 705167619),
                new Value("AMBIENCE_LIGHT_CUSTOM_MODE_GRADIENTS", 705167618),
                new Value("AMBIENCE_LIGHT_CUSTOM_MODE_SOLID_COLOR", 705167617),
                new Value("AMBIENCE_LIGHT_EXPERIENCE_CUSTOM", 537526530),
                new Value("AMBIENCE_LIGHT_EXPERIENCE_FULL", 537526529),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_BREATHE_MODE", 537526790),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_DRIVERMODE", 537526786),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_MUSIC", 537526788),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_NONE", 0),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_NON_POLAR", 537526789),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_SETCOLOR", 537526787),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_SPEED_MODE", 537526791),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_THEME", 537526785),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_WEATHER", 537526792),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_APPLE_GREEN", 704709132),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_BLUE", 704709126),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_GREEN", 704709124),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_ICE_BLUE", 704709129),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_INDIGO", 704709125),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_OFF", 0),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_ORANGE", 704709122),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_RED", 704709121),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_SPANISH_RED", 704709131),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_SUN_RED", 704709130),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_VIOLET", 704709127),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_WHITE", 704709128),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_YELLOW", 704709123),
                new Value("CARPET_LIGHT_THEME_MODE_1", 721489153),
                new Value("CARPET_LIGHT_THEME_MODE_2", 721489154),
                new Value("CARPET_LIGHT_THEME_MODE_3", 721489155),
                new Value("CARPET_LIGHT_TIME_MODE_45s", 0),
                new Value("CARPET_LIGHT_TIME_MODE_60s", 1),
                new Value("CARPET_LIGHT_TIME_MODE_75s", 2),
                new Value("CARPET_LIGHT_TIME_MODE_90s", 3),
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT", 538313730),
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT_SOUND", 538313731),
                new Value("CUSTOM_INTERIOR_LIGHT_COMFORT", 570625282),
                new Value("CUSTOM_INTERIOR_LIGHT_ECO", 570625281),
                new Value("CUSTOM_INTERIOR_LIGHT_HYBRID", 570625287),
                new Value("CUSTOM_INTERIOR_LIGHT_OFF", 0),
                new Value("CUSTOM_INTERIOR_LIGHT_OFFROAD", 570625284),
                new Value("CUSTOM_INTERIOR_LIGHT_PURE", 570625288),
                new Value("CUSTOM_INTERIOR_LIGHT_SAND", 570625286),
                new Value("CUSTOM_INTERIOR_LIGHT_SNOW", 570625285),
                new Value("CUSTOM_INTERIOR_LIGHT_SPORT", 570625283),
                new Value("CUSTOM_INTERIOR_LIGHT_STANDARD", 570625289),
                new Value("CUSTOM_STEERING_WHEEL_FEEL_LIGHT", 570624257),
                new Value("HOME_SAFE_LIGHT_VALUE_30S", 537134849),
                new Value("HOME_SAFE_LIGHT_VALUE_60S", 537134850),
                new Value("HOME_SAFE_LIGHT_VALUE_90S", 537134851),
                new Value("HOME_SAFE_LIGHT_VALUE_OFF", 0),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0),
                new Value("...TRUNCATED_CANDIDATES", 71),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ODOMETER", 1050368, 3, "ISensor.SENSOR_TYPE_ODOMETER", "Пробег (одометр).", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PASS_SEAT_BACKREST_PERC", 1059584, 3, "ISensor.SENSOR_TYPE_PASS_SEAT_BACKREST_PERC", "Положение спинки сиденья пассажира, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PASS_SEAT_CUSHION_PERC", 1059328, 3, "ISensor.SENSOR_TYPE_PASS_SEAT_CUSHION_PERC", "Положение подушки сиденья пассажира, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PASS_SEAT_HEIGHT_PERC", 1058816, 3, "ISensor.SENSOR_TYPE_PASS_SEAT_HEIGHT_PERC", "Высота сиденья пассажира, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PASS_SEAT_LENGTH_PERC", 1059072, 3, "ISensor.SENSOR_TYPE_PASS_SEAT_LENGTH_PERC", "Продольное положение сиденья пассажира, %.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_LEVEL_INDOOR", 2105856, 3, "ISensor.SENSOR_TYPE_PM25_LEVEL_INDOOR", "Уровень PM2.5 в салоне.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_RAIN", 1052160, 3, "ISensor.SENSOR_TYPE_RAIN", "Уровень сигнала датчика дождя/чувствительности.", new Value[] {
                new Value("RAINSENSORSENSILVL_LVL1", 0),
                new Value("RAINSENSORSENSILVL_LVL2", 1),
                new Value("RAINSENSORSENSILVL_LVL3", 2),
                new Value("RAINSENSORSENSILVL_LVL4", 3),
                new Value("RAINSENSORSENSILVL_LVL5", 4),
                new Value("RAINSENSORSENSILVL_LVL6", 5),
                new Value("RAINSENSORSENSILVL_LVL7", 6),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_DRIVER", 2101760, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_DRIVER", "Состояние ремня безопасности водителя.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_OCCUPATION_STATUS_DRIVER", 2110208, 3, "ISensor.SENSOR_TYPE_SEAT_OCCUPATION_STATUS_DRIVER", "Занятость сиденья водителя.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_OCCUPATION_STATUS_PASSENGER", 2110464, 3, "ISensor.SENSOR_TYPE_SEAT_OCCUPATION_STATUS_PASSENGER", "Занятость сиденья переднего пассажира.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_OCCUPATION_STATUS_SECOND_ROW_LEFT", 2110720, 3, "ISensor.SENSOR_TYPE_SEAT_OCCUPATION_STATUS_SECOND_ROW_LEFT", "Занятость второго ряда: левое место.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_OCCUPATION_STATUS_SECOND_ROW_RIGHT", 2110976, 3, "ISensor.SENSOR_TYPE_SEAT_OCCUPATION_STATUS_SECOND_ROW_RIGHT", "Занятость второго ряда: правое место.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SNSR_FR_WARNING", 1058816, 3, "ISensor.SENSOR_TYPE_SNSR_FR_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SNSR_LE_WARNING", 1059328, 3, "ISensor.SENSOR_TYPE_SNSR_LE_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SNSR_RE_WARNING", 1059072, 3, "ISensor.SENSOR_TYPE_SNSR_RE_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SNSR_RI_WARNING", 1059584, 3, "ISensor.SENSOR_TYPE_SNSR_RI_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_STEERING_WHEEL_ANGLE", 1052672, 3, "ISensor.SENSOR_TYPE_STEERING_WHEEL_ANGLE", "Угол поворота рулевого колеса.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_TEMPERATURE_AMBIENT", 1051392, 3, "ISensor.SENSOR_TYPE_TEMPERATURE_AMBIENT", "Температура наружного воздуха.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_TEMPERATURE_INDOOR", 1051648, 3, "ISensor.SENSOR_TYPE_TEMPERATURE_INDOOR", "Температура в салоне.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_TIREDNESS_DRIVING_STATE", 3148544, 3, "ISensor.SENSOR_TYPE_TIREDNESS_DRIVING_STATE", "Состояние усталости при вождении.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_VEH_MTN_STATE", 3148288, 3, "ISensor.SENSOR_TYPE_VEH_MTN_STATE", "Состояние движения/устойчивости авто.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_ENGINE_COOLANT_SYSTEM_FAULT", 3148032, 3, "ISensor.SENSOR_TYPE_WARN_ENGINE_COOLANT_SYSTEM_FAULT", "Предупреждение: неисправность системы охлаждения.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_ENGINE_COOLANT_TEMP_HIGH", 3146752, 3, "ISensor.SENSOR_TYPE_WARN_ENGINE_COOLANT_TEMP_HIGH", "Предупреждение: высокая температура ОЖ.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_ENGINE_OIL_PRESSURE", 3146496, 3, "ISensor.SENSOR_TYPE_WARN_ENGINE_OIL_PRESSURE", "Предупреждение: давление моторного масла.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_ENGINE_OIL_SYSTEM_FAULT", 3147776, 3, "ISensor.SENSOR_TYPE_WARN_ENGINE_OIL_SYSTEM_FAULT", "Предупреждение: неисправность системы смазки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_EV_BATTERY_LOW", 3146240, 3, "ISensor.SENSOR_TYPE_WARN_EV_BATTERY_LOW", "Предупреждение: низкий заряд HV‑батареи.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_FUEL_RED", 3145984, 3, "ISensor.SENSOR_TYPE_WARN_FUEL_RED", "Предупреждение: низкий уровень топлива.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_TRANSMISSION_TEMP_HIGH", 3147008, 3, "ISensor.SENSOR_TYPE_WARN_TRANSMISSION_TEMP_HIGH", "Предупреждение: высокая температура трансмиссии.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ACC_WITH_TSR", 671482624, 2, "IADAS.SETTING_FUNC_ACC_WITH_TSR", "Адаптивный круиз с учётом распознавания знаков (TSR).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ADAS_PADDLE_LANE_CHANGE_ASSIST", 671619840, 2, "IADAS.SETTING_FUNC_ADAS_PADDLE_LANE_CHANGE_ASSIST", "Смена полосы лепестками (вкл/выкл).", new Value[] {
                new Value("ADAS_PADDLE_LANE_CHANGE_ASSIST_DISABLE", 1051904),
                new Value("ADAS_PADDLE_LANE_CHANGE_ASSIST_ENABLE", 1051905),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AIRING_WHEN_SMOKING_MODE", 738395136, 2, "ISafety.SETTING_FUNC_AIRING_WHEN_SMOKING_MODE", "Режим проветривания при курении.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_DEFAULT_ON", 671613440, 2, "IADAS.SETTING_FUNC_AI_ASSIST_DEFAULT_ON", "Включать AI‑ассистента по умолчанию.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_FUSION_NAVI", 671613696, 2, "IADAS.SETTING_FUNC_AI_ASSIST_FUSION_NAVI", "Использовать навигацию в работе AI‑ассистента.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_LANE_CHANGE_CONFIRM", 671614464, 2, "IADAS.SETTING_FUNC_AI_ASSIST_LANE_CHANGE_CONFIRM", "Требовать подтверждение смены полосы.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_LANE_CHANGE_STRATEGY", 671614208, 2, "IADAS.SETTING_FUNC_AI_ASSIST_LANE_CHANGE_STRATEGY", "Стратегия смены полосы (мягкая/стандартная/активная).", new Value[] {
                new Value("AI_ASSIST_LANE_CHANGE_STRATEGY_GENTLE", 671614209),
                new Value("AI_ASSIST_LANE_CHANGE_STRATEGY_OFF", 0),
                new Value("AI_ASSIST_LANE_CHANGE_STRATEGY_RADICAL", 671614211),
                new Value("AI_ASSIST_LANE_CHANGE_STRATEGY_STANDARD", 671614210),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_LANE_CHANGE_WARNING", 671614720, 2, "IADAS.SETTING_FUNC_AI_ASSIST_LANE_CHANGE_WARNING", "Тип предупреждения при смене полосы (голос/вибрация/оба).", new Value[] {
                new Value("AI_ASSIST_LANE_CHANGE_WARNING_BOTH", 671614723),
                new Value("AI_ASSIST_LANE_CHANGE_WARNING_OFF", 0),
                new Value("AI_ASSIST_LANE_CHANGE_WARNING_VIBRATE", 671614722),
                new Value("AI_ASSIST_LANE_CHANGE_WARNING_VOICE", 671614721),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_ASSIST_OUT_OVERTAKING_LANE", 671613952, 2, "IADAS.SETTING_FUNC_AI_ASSIST_OUT_OVERTAKING_LANE", "Авто‑уход из полосы обгона.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AI_DRIVER_ASSIST", 671613184, 2, "IADAS.SETTING_FUNC_AI_DRIVER_ASSIST", "Главный переключатель AI‑ассистента.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_DOOR_OPEN_MUSIC_AUD_TYP", 709886976, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_DOOR_OPEN_MUSIC_AUD_TYP", "Тип звука при открытии двери.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_BREATHE_COLOR_SET", 709886208, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_BREATHE_COLOR_SET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_CLIMATE", 705167872, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_CLIMATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_COLOR_SET", 537528576, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_COLOR_SET", "Выбор цвета атмосферной подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_CONTROL_MODE", 705168896, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_CONTROL_MODE", "Режим управления подсветкой (цвет/музыка/экран/время).", new Value[] {
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_COLOR", 705168900),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_MORE", 705168897),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_MUSIC", 705168898),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_SCREEN", 705168899),
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_TIME", 705168901),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_EFFECT_SET", 705167616, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_EFFECT_SET", "Выбор эффекта подсветки.", new Value[] {
                new Value("AMBIENCE_LIGHT_EFFECT_SOLID", 705167617),
                new Value("AMBIENCE_LIGHT_EFFECT_GRADIENTS", 705167618),
                new Value("AMBIENCE_LIGHT_EFFECT_BREATHE", 705167619),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_ENDURANCE_MIL_REMINDER", 704971520, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_ENDURANCE_MIL_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_EXPERIENCE", 537526528, 2, "IVehicle.SETTING_FUNC_AMBIENCE_LIGHT_EXPERIENCE", "Сценарий работы подсветки (полный/кастом).", new Value[] {
                new Value("AMBIENCE_LIGHT_EXPERIENCE_CUSTOM", 537526530),
                new Value("AMBIENCE_LIGHT_EXPERIENCE_FULL", 537526529),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_GOODBYE_SHOW", 704971264, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_GOODBYE_SHOW", "Прощальная световая анимация.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_ICHARGING_REMIND", 705168128, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_ICHARGING_REMIND", "Световое напоминание о зарядке.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_INTENSITY_SET", 704708864, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_INTENSITY_SET", "Яркость атмосферной подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_INTERACTIVE_EFFECT", 537528320, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_INTERACTIVE_EFFECT", "Интерактивные эффекты подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_MAINCOLOR", 537526784, 2, "IVehicle.SETTING_FUNC_AMBIENCE_LIGHT_MAINCOLOR", "", new Value[] {
                new Value("AMBIENCE_LIGHT_MAINCOLOR_BREATHE_MODE", 537526790),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_DRIVERMODE", 537526786),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_MUSIC", 537526788),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_NONE", 0),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_NON_POLAR", 537526789),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_SETCOLOR", 537526787),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_SPEED_MODE", 537526791),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_THEME", 537526785),
                new Value("AMBIENCE_LIGHT_MAINCOLOR_WEATHER", 537526792),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_MAINZONES", 537527552, 2, "IVehicle.SETTING_FUNC_AMBIENCE_LIGHT_MAINZONES", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_MUSIC", 704974592, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_MUSIC", "Музыкальный режим подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_MUSIC_SHOW_MODE", 704972800, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_MUSIC_SHOW_MODE", "Режим показа подсветки под музыку.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_PHONE_CALL_REMINDER", 704971776, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_PHONE_CALL_REMINDER", "Световое напоминание о звонке.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_SLIDING_DOOR_REMINDER", 704973056, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_SLIDING_DOOR_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_SOLID_COLOR_SET", 709885952, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_SOLID_COLOR_SET", "Выбор фиксированного цвета подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_THEME_COLOR", 704709120, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_THEME_COLOR", "Выбор темы/цвета подсветки (предустановки).", new Value[] {
                new Value("AMBIENCE_LIGHT_THEME_COLOR_APPLE_GREEN", 704709132),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_BLUE", 704709126),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_GREEN", 704709124),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_ICE_BLUE", 704709129),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_INDIGO", 704709125),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_OFF", 0),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_ORANGE", 704709122),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_RED", 704709121),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_SPANISH_RED", 704709131),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_SUN_RED", 704709130),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_VIOLET", 704709127),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_WHITE", 704709128),
                new Value("AMBIENCE_LIGHT_THEME_COLOR_YELLOW", 704709123),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_TOPZONES", 537527296, 2, "IVehicle.SETTING_FUNC_AMBIENCE_LIGHT_TOPZONES", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_VOICE", 704974080, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_VOICE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_WELCOME_SHOW", 704971008, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_WELCOME_SHOW", "Приветственная световая анимация.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_LIGHT_WELCOME_SHOW_MODE", 704972544, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_LIGHT_WELCOME_SHOW_MODE", "Режим приветственной анимации.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_MUSIC_SHOW_PASS_EXCLU", 705169920, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_MUSIC_SHOW_PASS_EXCLU", "Исключать пассажирскую сторону из музыкальной анимации.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_OPEN_PASS_DOOR_SHOW", 705169664, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_OPEN_PASS_DOOR_SHOW", "Световая анимация при открытии двери.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AMBIENCE_WELCOME_AUD_REQ", 709886720, 2, "IAmbienceLight.SETTING_FUNC_AMBIENCE_WELCOME_AUD_REQ", "Запрос звукового приветствия.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_APPROACH_TAIL_UNLOCK", 738264320, 2, "ISafety.SETTING_FUNC_APPROACH_TAIL_UNLOCK", "Авторазблокировка багажника при приближении.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_APPROACH_UNLOCK", 738263296, 2, "ISafety.SETTING_FUNC_APPROACH_UNLOCK", "Авторазблокировка при приближении ключа.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ARTIFICIAL_SOUND_TYPE", 538575872, 2, "IVehicle.SETTING_FUNC_ARTIFICIAL_SOUND_TYPE", "Тип внешнего звука оповещения (AVAS).", new Value[] {
                new Value("ARTIFICIAL_SOUND_TYPE_1", 538575873),
                new Value("ARTIFICIAL_SOUND_TYPE_2", 538575874),
                new Value("ARTIFICIAL_SOUND_TYPE_3", 538575875),
                new Value("ARTIFICIAL_SOUND_TYPE_4", 538575876),
                new Value("ARTIFICIAL_SOUND_TYPE_5", 538575877),
                new Value("ARTIFICIAL_SOUND_TYPE_6", 538575878),
                new Value("ARTIFICIAL_SOUND_TYPE_7", 538575879),
                new Value("ARTIFICIAL_SOUND_TYPE_8", 538575880),
                new Value("ARTIFICIAL_SOUND_TYPE_NONE", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK", 537920256, 2, "ISafety.SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK", "Звуковое подтверждение запирания.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUDIO_SEPARATED", 771948800, 2, "IAudio.SETTING_FUNC_AUDIO_SEPARATED", "Разделение аудио (водитель/пассажир).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTONOMOUS_EMERGENCY_BRAKING", 537333248, 2, "IADAS.SETTING_FUNC_AUTONOMOUS_EMERGENCY_BRAKING", "Автоматическое экстренное торможение (AEB).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTONOMOUS_EMERGENCY_BRAKING_WARN", 537333249, 2, "IADAS.SETTING_FUNC_AUTONOMOUS_EMERGENCY_BRAKING_WARN", "Настройки предупреждения AEB.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_CLOSE_ROOF_RAINY", 537395968, 2, "IVehicle.SETTING_FUNC_AUTO_CLOSE_ROOF_RAINY", "Автозакрытие крыши при дожде.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_CLOSE_WINDOW", 537396224, 2, "IVehicle.SETTING_FUNC_AUTO_CLOSE_WINDOW", "Автозакрытие окон при запирании/удержании.", new Value[] {
                new Value("AUTO_CLOSE_WINDOW_KEY_LONG_PRESS", 537396226),
                new Value("AUTO_CLOSE_WINDOW_OFF", 0),
                new Value("AUTO_CLOSE_WINDOW_VEHICLE_LOCK", 537396225),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_HOLD", 537265152, 2, "IVehicle.SETTING_FUNC_AUTO_HOLD", "Auto Hold: удержание после остановки.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_LANE_CHANGE_ASSIST", 671351040, 2, "IADAS.SETTING_FUNC_AUTO_LANE_CHANGE_ASSIST", "Ассистент автоматической смены полосы.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_REAR_WIPING", 537657856, 2, "IVehicle.SETTING_FUNC_AUTO_REAR_WIPING", "Автовключение заднего стеклоочистителя.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_SHOW_MODE", 540279296, 2, "IVehicle.SETTING_FUNC_AUTO_SHOW_MODE", "Демонстрационный режим интерфейса (Auto Show).", new Value[] {
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_FALSE", 1),
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_GEAR", 2),
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_NORMAL", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_SHOW_MODE_ENTER_CONDITIONS", 540279040, 2, "IVehicle.SETTING_FUNC_AUTO_SHOW_MODE_ENTER_CONDITIONS", "Условия входа в демо‑режим.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_SHOW_MODE_ICON", 540280832, 2, "IVehicle.SETTING_FUNC_AUTO_SHOW_MODE_ICON", "Иконка демо‑режима.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_SHOW_MODE_POPUP", 540280064, 2, "IVehicle.SETTING_FUNC_AUTO_SHOW_MODE_POPUP", "Поп‑ап демо‑режима.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_SHOW_MODE_TEXT", 540280576, 2, "IVehicle.SETTING_FUNC_AUTO_SHOW_MODE_TEXT", "Текст/режим отображения в демо‑режиме.", new Value[] {
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_FALSE", 1),
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_GEAR", 2),
                new Value("SETTING_FUNC_AUTO_SHOW_MODE_TEXT_NORMAL", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AUTO_TRAILER_LAMP_CHECK", 537135872, 2, "IVehicle.SETTING_FUNC_AUTO_TRAILER_LAMP_CHECK", "Автоматическая проверка ламп прицепа.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AWAY_LOCK", 738263552, 2, "ISafety.SETTING_FUNC_AWAY_LOCK", "Автоблокировка при удалении ключа.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BACKLIGHT_LINKAGE", 687931648, 2, "IDayMode.SETTING_FUNC_BACKLIGHT_LINKAGE", "Связать яркость с датчиком освещённости/дневным режимом.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BREATH_SCREEN_MODE", 540284928, 2, "IVehicle.SETTING_FUNC_BREATH_SCREEN_MODE", "Режим «дыхания» подсветки экрана.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_BACKLIGHT", 687997184, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_BACKLIGHT", "Яркость подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_BACKLIGHT_MAX", 687997440, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_BACKLIGHT_MAX", "Максимальная яркость подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_BACKLIGHT_MIN", 687997696, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_BACKLIGHT_MIN", "Минимальная яркость подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_BACKLIGHT_STEP", 687997952, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_BACKLIGHT_STEP", "Шаг изменения яркости подсветки.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DAY", 538247936, 2, "IVehicle.SETTING_FUNC_BRIGHTNESS_DAY", "Яркость дисплея (дневной режим).", new Value[] {
                new Value("DAYMODE_SETTING_BRIGHTNESS_DAY", 538247425),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DAYMODE", 688062976, 2, "ICarFunction.CAR_MODULE_DAYMODE", "Яркость в дневном режиме.", new Value[] {
                new Value("DAYMODE_SETTING_AUTO", 538247427),
                new Value("DAYMODE_SETTING_CUSTOM", 538247428),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DIM", 687998208, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_DIM", "Яркость (режим DIM).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DIM_MAX", 687998464, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_DIM_MAX", "Максимальная яркость (DIM).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DIM_MIN", 687998720, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_DIM_MAX", "Минимальная яркость (DIM).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_DIM_STEP", 687998976, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_DIM_STEP", "Шаг яркости (DIM).", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_MAX", 538248448, 2, "IVehicle.SETTING_FUNC_BRIGHTNESS_MAX", "Максимальная яркость дисплея.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_MIN", 538248704, 2, "IVehicle.SETTING_FUNC_BRIGHTNESS_MIN", "Минимальная яркость дисплея.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_NIGHT", 538248192, 2, "IVehicle.SETTING_FUNC_BRIGHTNESS_NIGHT", "Яркость дисплея (ночной режим).", new Value[] {
                new Value("DAYMODE_SETTING_BRIGHTNESS_NIGHT", 538247426),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_SCREEN", 688063744, 2, "IDayMode.SETTING_FUNC_BRIGHTNESS_SCREEN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BRIGHTNESS_STEP", 538248960, 2, "IVehicle.SETTING_FUNC_BRIGHTNESS_STEP", "Шаг яркости дисплея.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CAE_SWITCH", 771818240, 2, "IAudio.SETTING_FUNC_CAE_SWITCH", "Активное звучание (CAE): вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CARPET_LIGHT", 721488640, 2, "ILamp.SETTING_FUNC_CARPET_LIGHT", "Подсветка зоны у двери: режим/время.", new Value[] {
                new Value("CARPET_LIGHT_THEME_MODE_1", 721489153),
                new Value("CARPET_LIGHT_THEME_MODE_2", 721489154),
                new Value("CARPET_LIGHT_THEME_MODE_3", 721489155),
                new Value("CARPET_LIGHT_TIME_MODE_45s", 0),
                new Value("CARPET_LIGHT_TIME_MODE_60s", 1),
                new Value("CARPET_LIGHT_TIME_MODE_75s", 2),
                new Value("CARPET_LIGHT_TIME_MODE_90s", 3),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CARPET_LIGHT_SWT", 709887232, 2, "IAmbienceLight.SETTING_FUNC_CARPET_LIGHT_SWT", "Подсветка зоны у двери: вкл/выкл.", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CARPET_LIGHT_THEME", 721489152, 2, "ILamp.SETTING_FUNC_CARPET_LIGHT_THEME", "", new Value[] {
                new Value("CARPET_LIGHT_THEME_MODE_1", 721489153),
                new Value("CARPET_LIGHT_THEME_MODE_2", 721489154),
                new Value("CARPET_LIGHT_THEME_MODE_3", 721489155),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CARPET_LIGHT_TIME", 721488896, 2, "ILamp.SETTING_FUNC_CARPET_LIGHT_TIME", "", new Value[] {
                new Value("CARPET_LIGHT_TIME_MODE_45s", 0),
                new Value("CARPET_LIGHT_TIME_MODE_60s", 1),
                new Value("CARPET_LIGHT_TIME_MODE_75s", 2),
                new Value("CARPET_LIGHT_TIME_MODE_90s", 3),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CARPET_LIGHT_TIME_MODE", 709887488, 2, "IAmbienceLight.SETTING_FUNC_CARPET_LIGHT_TIME_MODE", "", new Value[] {
                new Value("CARPET_LIGHT_TIME_MODE_45s", 0),
                new Value("CARPET_LIGHT_TIME_MODE_60s", 1),
                new Value("CARPET_LIGHT_TIME_MODE_75s", 2),
                new Value("CARPET_LIGHT_TIME_MODE_90s", 3),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CAR_LOCATOR", 538312960, 2, "IVehicle.SETTING_FUNC_CAR_LOCATOR", "", new Value[] {
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT", 538313730),
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT_SOUND", 538313731),
                new Value("CAR_LOCATOR_REMINDER_MODE_OFF", 0),
                new Value("CAR_LOCATOR_REMINDER_MODE_SOUND", 538313729),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CAR_LOCATOR_REMINDER_MODE", 538313728, 2, "IVehicle.SETTING_FUNC_CAR_LOCATOR_REMINDER_MODE", "", new Value[] {
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT", 538313730),
                new Value("CAR_LOCATOR_REMINDER_MODE_LIGHT_SOUND", 538313731),
                new Value("CAR_LOCATOR_REMINDER_MODE_OFF", 0),
                new Value("CAR_LOCATOR_REMINDER_MODE_SOUND", 538313729),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CENTRAL_LOCK", 537921792, 2, "ISafety.SETTING_FUNC_CENTRAL_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CHILD_RISKY_BEHAVIOR_MONITOR", 738395392, 2, "ISafety.SETTING_FUNC_CHILD_RISKY_BEHAVIOR_MONITOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CUSTOM_DAY_TIME", 688063232, 2, "IDayMode.SETTING_FUNC_CUSTOM_DAY_TIME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CUSTOM_NIGHT_TIME", 688063488, 2, "IDayMode.SETTING_FUNC_CUSTOM_NIGHT_TIME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DATA_COLLECTION", 539361792, 2, "IVehicle.SETTING_FUNC_DATA_COLLECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DAYMODE_SETTING", 538247424, 2, "IVehicle.SETTING_FUNC_DAYMODE_SETTING", "", new Value[] {
                new Value("DAYMODE_SETTING_AUTO", 538247427),
                new Value("DAYMODE_SETTING_BRIGHTNESS_AUTO", 538247427),
                new Value("DAYMODE_SETTING_BRIGHTNESS_DAY", 538247425),
                new Value("DAYMODE_SETTING_BRIGHTNESS_NIGHT", 538247426),
                new Value("DAYMODE_SETTING_BRIGHTNESS_OFF", 0),
                new Value("DAYMODE_SETTING_CUSTOM", 538247428),
                new Value("DAYMODE_SETTING_DAY", 538247425),
                new Value("DAYMODE_SETTING_NIGHT", 538247426),
                new Value("DAYMODE_SETTING_OFF", 0),
                new Value("DAYMODE_SETTING_SUNRISE_AND_SUNSET", 538247429),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DAYMODE_SYNC", 538247680, 2, "IVehicle.SETTING_FUNC_DAYMODE_SYNC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DND_MODE", 738394880, 2, "ISafety.SETTING_FUNC_DND_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DOOR_OPEN_WARN_ACTIVE", 538050816, 2, "IVehicle.SETTING_FUNC_DOOR_OPEN_WARN_ACTIVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DOUBLE_LOCK", 539756032, 2, "IVehicle.SETTING_FUNC_DOUBLE_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVER_PERFOR_SUPPORT", 537003520, 2, "IVehicle.SETTING_FUNC_DRIVER_PERFOR_SUPPORT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVER_PERFOR_SUPPORT_REMINDER", 671219968, 2, "IADAS.SETTING_FUNC_DRIVER_PERFOR_SUPPORT_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVE_MODE_KNOB_DIRECTION", 570753792, 2, "IDriveMode.SETTING_FUNC_DRIVE_MODE_KNOB_DIRECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVE_MODE_KNOB_ROTATE_STEP", 570754048, 2, "IDriveMode.SETTING_FUNC_DRIVE_MODE_KNOB_ROTATE_STEP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVE_MODE_REQUEST_NEXT", 570753536, 2, "IDriveMode.SETTING_FUNC_DRIVE_MODE_REQUEST_NEXT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVE_MODE_REQUEST_PRE", 570753280, 2, "IDriveMode.SETTING_FUNC_DRIVE_MODE_REQUEST_PRE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVE_PILOT", 671548416, 2, "IADAS.SETTING_FUNC_DRIVE_PILOT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRVR_SEB", 538379264, 2, "IVehicle.SETTING_FUNC_DRVR_SEB", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EASY_INGRESS_EGRESS", 538378496, 2, "IVehicle.SETTING_FUNC_EASY_INGRESS_EGRESS", "Лёгкая посадка/высадка водительского сиденья.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ELECTRIC_MILEAGE_DISPLAY_MODE", 540281088, 2, "IVehicle.SETTING_FUNC_ELECTRIC_MILEAGE_DISPLAY_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ELECTRIC_MILEAGE_DISPLAY_SWITCH", 539429632, 2, "IVehicle.SETTING_FUNC_ELECTRIC_MILEAGE_DISPLAY_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ELECTRONIC_PARKING", 540148736, 2, "IVehicle.SETTING_FUNC_ELECTRONIC_PARKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ELE_SEATBELT_COMFORT", 537333504, 2, "IVehicle.SETTING_FUNC_ELE_SEATBELT_COMFORT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EMGY_LANE_KEEP_AID", 537331200, 2, "IVehicle.SETTING_FUNC_EMGY_LANE_KEEP_AID", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EMGY_LANE_OCC_WARNING", 537332480, 2, "IVehicle.SETTING_FUNC_EMGY_LANE_OCC_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENERGY_REGENERATION", 537003264, 2, "IVehicle.SETTING_FUNC_ENERGY_REGENERATION", "", new Value[] {
                new Value("ENERGY_REGENERATION_LEVEL_AUTO", 537003268),
                new Value("ENERGY_REGENERATION_LEVEL_HIGH", 537003267),
                new Value("ENERGY_REGENERATION_LEVEL_LOW", 537003265),
                new Value("ENERGY_REGENERATION_LEVEL_MID", 537003266),
                new Value("ENERGY_REGENERATION_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENGINE_STOP_START", 537002240, 2, "IVehicle.SETTING_FUNC_ENGINE_STOP_START", "Автоматический старт/стоп двигателя.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENTER_AUTO_SHOW_MODE", 540279808, 2, "IVehicle.SETTING_FUNC_ENTER_AUTO_SHOW_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ESC_SPORT_MODE", 537002752, 2, "IVehicle.SETTING_FUNC_ESC_SPORT_MODE", "Спортивный режим ESC.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ESM_SWITCH", 538575104, 2, "IVehicle.SETTING_FUNC_ESM_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ESM_TYPE", 540281600, 2, "IVehicle.SETTING_FUNC_ESM_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ESM_VOLUME", 538575360, 2, "IVehicle.SETTING_FUNC_ESM_VOLUME", "", new Value[] {
                new Value("ESM_VOLUME_LEVEL_HIGH", 538575363),
                new Value("ESM_VOLUME_LEVEL_LOW", 538575361),
                new Value("ESM_VOLUME_LEVEL_MID", 538575362),
                new Value("ESM_VOLUME_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EVASIVE_MANEUVER_AID", 537332736, 2, "IVehicle.SETTING_FUNC_EVASIVE_MANEUVER_AID", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EXTERNAL_ARTIFICIAL_SOUND_TYPE", 538577664, 2, "IVehicle.SETTING_FUNC_EXTERNAL_ARTIFICIAL_SOUND_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FACE_RECOGNITION_RESULT", 540281344, 2, "IVehicle.SETTING_FUNC_FACE_RECOGNITION_RESULT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FACIAL_RECOGNITION", 538706432, 2, "IVehicle.SETTING_FUNC_FACIAL_RECOGNITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FORWARD_COLLISION_WARN", 537788672, 2, "IVehicle.SETTING_FUNC_FORWARD_COLLISION_WARN", "", new Value[] {
                new Value("FORWARD_COLLISION_WARN_SNVTY_HIGH", 537788931),
                new Value("FORWARD_COLLISION_WARN_SNVTY_LOW", 537788929),
                new Value("FORWARD_COLLISION_WARN_SNVTY_NORMAL", 537788930),
                new Value("FORWARD_COLLISION_WARN_SNVTY_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FORWARD_COLLISION_WARN_SNVTY", 537788928, 2, "IADAS.SETTING_FUNC_FORWARD_COLLISION_WARN_SNVTY", "", new Value[] {
                new Value("FORWARD_COLLISION_WARN_SNVTY_HIGH", 537788931),
                new Value("FORWARD_COLLISION_WARN_SNVTY_LOW", 537788929),
                new Value("FORWARD_COLLISION_WARN_SNVTY_NORMAL", 537788930),
                new Value("FORWARD_COLLISION_WARN_SNVTY_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HDC_SWITCH", 537265408, 2, "IVehicle.SETTING_FUNC_HDC_SWITCH", "Hill Descent Control.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HEAD_LAMP_HEIGHT_ADJUST", 721488384, 2, "ILamp.SETTING_FUNC_HEAD_LAMP_HEIGHT_ADJUST", "", new Value[] {
                new Value("HEAD_LAMP_HEIGHT_ADJUST_LV1", 721488385),
                new Value("HEAD_LAMP_HEIGHT_ADJUST_LV2", 721488386),
                new Value("HEAD_LAMP_HEIGHT_ADJUST_LV3", 721488387),
                new Value("HEAD_LAMP_HEIGHT_ADJUST_LV4", 721488388),
                new Value("HEAD_LAMP_HEIGHT_ADJUST_LV5", 721488389),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HEAD_RESTRAINT_AUDIO", 539100160, 2, "IVehicle.SETTING_FUNC_HEAD_RESTRAINT_AUDIO", "", new Value[] {
                new Value("SETTING_FUNC_HEAD_RESTRAINT_AUDIO_DRVING", 539099906),
                new Value("SETTING_FUNC_HEAD_RESTRAINT_AUDIO_PRIVATE", 539099907),
                new Value("SETTING_FUNC_HEAD_RESTRAINT_AUDIO_SHARE", 539099905),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HEAD_RESTRAINT_AUDIO_TYPE", 539099904, 2, "IVehicle.SETTING_FUNC_HEAD_RESTRAINT_AUDIO_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_ACTIVE", 537985280, 2, "IVehicle.SETTING_FUNC_HUD_ACTIVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_ANGLE_ADJUST", 654378752, 2, "IHUD.SETTING_FUNC_HUD_ANGLE_ADJUST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_ANGLE_RESET", 654379008, 2, "ICarInfo.SETTING_FUNC_HUD_ANGLE_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_AR_ENGINE", 654443008, 2, "IHUD.SETTING_FUNC_HUD_AR_ENGINE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_BRIGHTNESS_ADJUST", 654378240, 2, "IHUD.SETTING_FUNC_HUD_BRIGHTNESS_ADJUST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_CALIBRATION", 537985536, 2, "IVehicle.SETTING_FUNC_HUD_CALIBRATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_POSITION_ADJUST", 654378496, 2, "IHUD.SETTING_FUNC_HUD_POSITION_ADJUST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HUD_SNOW_MODE", 654442752, 2, "IHUD.SETTING_FUNC_HUD_SNOW_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HV_BATT_EGY_SOC", 605489152, 2, "IVehicle.SETTING_FUNC_HV_BATT_EGY_SOC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HV_BATT_HEAT_POP", 538379776, 2, "IVehicle.SETTING_FUNC_HV_BATT_HEAT_POP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HV_BATT_HEAT_TOAST", 538380032, 2, "IVehicle.SETTING_FUNC_HV_BATT_HEAT_TOAST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HXT_SWITCH", 771817984, 2, "IAudio.SETTING_FUNC_HXT_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_INTELLIGENT_FUEL_SAVE", 538904064, 2, "IVehicle.SETTING_FUNC_INTELLIGENT_FUEL_SAVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_INTERNAL_COMMUNICATION", 538902784, 2, "IVehicle.SETTING_FUNC_INTERNAL_COMMUNICATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_INTERNAL_COMMUNICATION_VOLUME", 538903040, 2, "IVehicle.SETTING_FUNC_INTERNAL_COMMUNICATION_VOLUME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_KEYLESS_UNLOCKING", 537920512, 2, "IVehicle.SETTING_FUNC_KEYLESS_UNLOCKING", "", new Value[] {
                new Value("KEYLESS_UNLOCKING_ALL_DOORS", 537920513),
                new Value("KEYLESS_UNLOCKING_OFF", 0),
                new Value("KEYLESS_UNLOCKING_SINGLE_DOOR", 537920514),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_ACTIVE_HIGH_BEAM_CONTROL", 721486080, 2, "ILamp.SETTING_FUNC_LAMP_ACTIVE_HIGH_BEAM_CONTROL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_ADAPTIVE_FRONT_LIGHT", 537136384, 2, "ILamp.SETTING_FUNC_LAMP_ADAPTIVE_FRONT_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_APPROACH_LIGHT", 537135360, 2, "ILamp.SETTING_FUNC_LAMP_APPROACH_LIGHT", "Подсветка при подходе к автомобилю.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_AUTOMATIC_COURTESY_LIGHT", 537134592, 2, "ILamp.SETTING_FUNC_LAMP_AUTOMATIC_COURTESY_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_BENDINGLIGHT", 537134336, 2, "IVehicle.SETTING_FUNC_LAMP_BENDINGLIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_EXTERIOR_LIGHT_CONTROL", 537136640, 2, "ILamp.SETTING_FUNC_LAMP_EXTERIOR_LIGHT_CONTROL", "Режим света фар", new Value[] {
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_POS_LIGHT", 537136641),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_LOWBEAM", 537136642),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AUTOMATIC", 537136643),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_AHBC", 537136644),
                new Value("LAMP_EXTERIOR_LIGHT_CONTROL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_HOME_SAFE_LIGHT", 537134848, 2, "ILamp.SETTING_FUNC_LAMP_HOME_SAFE_LIGHT", "", new Value[] {
                new Value("HOME_SAFE_LIGHT_VALUE_30S", 537134849),
                new Value("HOME_SAFE_LIGHT_VALUE_60S", 537134850),
                new Value("HOME_SAFE_LIGHT_VALUE_90S", 537134851),
                new Value("HOME_SAFE_LIGHT_VALUE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAMP_LR_TRAFFIC_LIGHT", 721551616, 2, "ILamp.SETTING_FUNC_LAMP_LR_TRAFFIC_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_CHANGE_ASSIST", 537331456, 2, "IVehicle.SETTING_FUNC_LANE_CHANGE_ASSIST", "", new Value[] {
                new Value("ADAS_PADDLE_LANE_CHANGE_ASSIST_DISABLE", 1051904),
                new Value("ADAS_PADDLE_LANE_CHANGE_ASSIST_ENABLE", 1051905),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_CHANGE_ASSIST_WARNING", 671351296, 2, "IADAS.SETTING_FUNC_LANE_CHANGE_ASSIST_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_CHANGE_WARING", 537330432, 2, "IVehicle.SETTING_FUNC_LANE_CHANGE_WARING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_CHANGE_WARNING_MODE", 537330704, 2, "IVehicle.SETTING_FUNC_LANE_CHANGE_WARNING_MODE", "", new Value[] {
                new Value("LANE_CHANGE_WARNING_MODE_OFF", 0),
                new Value("LANE_CHANGE_WARNING_MODE_SOUND", 537330706),
                new Value("LANE_CHANGE_WARNING_MODE_VISUAL", 537330705),
                new Value("LANE_CHANGE_WARNING_MODE_VISUAL_SOUND", 537330707),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_DEPARTURE_WARNING", 671285504, 2, "IADAS.SETTING_FUNC_LANE_DEPARTURE_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_KEEPING_AID", 537329920, 2, "IVehicle.SETTING_FUNC_LANE_KEEPING_AID", "", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_KEEPING_AID_MODE", 537330176, 2, "IVehicle.SETTING_FUNC_LANE_KEEPING_AID_MODE", "", new Value[] {
                new Value("LANE_KEEPING_AID_MODE_INTV", 537330178),
                new Value("LANE_KEEPING_AID_MODE_OFF", 0),
                new Value("LANE_KEEPING_AID_MODE_WARN", 537330179),
                new Value("LANE_KEEPING_AID_MODE_WARN_INTV", 537330177),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LANE_KEEPING_AID_WARNING", 537330944, 2, "IVehicle.SETTING_FUNC_LANE_KEEPING_AID_WARNING", "", new Value[] {
                new Value("LANE_KEEPING_AID_WARNING_HAPTIC", 537330946),
                new Value("LANE_KEEPING_AID_WARNING_OFF", 0),
                new Value("LANE_KEEPING_AID_WARNING_SOUND", 537330945),
                new Value("LANE_KEEPING_AID_WARNING_SOUND_HAPTIC", 537330947),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAUNCH_MODE", 539428864, 2, "IVehicle.SETTING_FUNC_LAUNCH_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LIFE_DETECTION", 539427328, 2, "IVehicle.SETTING_FUNC_LIFE_DETECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LOCK_AUDIO_FEEDBACK", 738396672, 2, "ISafety.SETTING_FUNC_LOCK_AUDIO_FEEDBACK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LOCK_FEEDBACK_AUDIO_WARNING", 738459904, 2, "ISafety.SETTING_FUNC_LOCK_FEEDBACK_AUDIO_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MAINTENANCE_MILEAGE_RESET", 538968320, 2, "IVehicle.SETTING_FUNC_MAINTENANCE_MILEAGE_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MCD_AUTO_BRIGHTNESS_SCREEN", 688063744, 2, "IBcm.BCM_FUNC_DOOR_LOCK_FAULT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MIRROR_AUTO_FOLDING", 537461248, 2, "IVehicle.SETTING_FUNC_MIRROR_AUTO_FOLDING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MIRROR_DIPPING", 537461504, 2, "IVehicle.SETTING_FUNC_MIRROR_DIPPING", "Функция автоматического наклона пассажирского зеркала при движении задним ходом, чтобы лучше видеть бордюр/разметку.", new Value[] {
                new Value("MIRROR_DIPPING_BOTH", 537461507),
                new Value("MIRROR_DIPPING_DRIVER", 537461505),
                new Value("MIRROR_DIPPING_OFF", 0),
                new Value("MIRROR_DIPPING_PASSENGER", 537461506),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MOOD_LIGHT", 705036544, 2, "IAmbienceLight.SETTING_FUNC_MOOD_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTIMEDIA_GESTURE", 539494144, 2, "IVehicle.SETTING_FUNC_MULTIMEDIA_GESTURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_SEAT_MENU", 759236608, 2, "ISeat.SETTING_FUNC_MULTI_SEAT_MENU", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_SEAT_MENU_HORIZONTAL_POSITION", 759237376, 2, "ISeat.SETTING_FUNC_MULTI_SEAT_MENU_HORIZONTAL_POSITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_SEAT_MENU_VERTICAL_POSITION", 759237120, 2, "ISeat.SETTING_FUNC_MULTI_SEAT_MENU_VERTICAL_POSITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_ASSIST_SYS_ACTIVATED", 537723136, 2, "IVehicle.SETTING_FUNC_PARK_ASSIST_SYS_ACTIVATED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_ASSIST_SYS_VOLUME", 537723392, 2, "IVehicle.SETTING_FUNC_PARK_ASSIST_SYS_VOLUME", "", new Value[] {
                new Value("PARK_ASSIST_SYS_VOLUME_HIGH", 537723395),
                new Value("PARK_ASSIST_SYS_VOLUME_LOW", 537723393),
                new Value("PARK_ASSIST_SYS_VOLUME_MID", 537723394),
                new Value("PARK_ASSIST_SYS_VOLUME_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_BELT_DOOR_OPEN", 540284416, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_BELT_DOOR_OPEN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_COUNTDOWN_TIMER", 540284672, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_COUNTDOWN_TIMER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_TIMER", 538837248, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_TIMER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_TIMER_MAX", 538837504, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_TIMER_MAX", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_TIMER_MIN", 538837760, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_TIMER_MIN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_TIMER_STEP", 538838016, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_TIMER_STEP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PASSIVE_ARMING", 537921280, 2, "IVehicle.SETTING_FUNC_PASSIVE_ARMING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PBC_AUTO_APPLY", 537264384, 2, "IVehicle.SETTING_FUNC_PBC_AUTO_APPLY", "Автоматическое применение парковочного тормоза.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PBC_DOUBLE_EPB_SWITCH", 537268480, 2, "IVehicle.SETTING_FUNC_PBC_DOUBLE_EPB_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PBC_EPB_SWITCH", 537268224, 2, "IVehicle.SETTING_FUNC_PBC_EPB_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PDC_SWITCH", 537264896, 2, "IVehicle.SETTING_FUNC_PDC_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PEB_MODE", 537264640, 2, "IVehicle.SETTING_FUNC_PEB_MODE", "", new Value[] {
                new Value("PEB_MODE_MSP", 537264642),
                new Value("PEB_MODE_OFF", 0),
                new Value("PEB_MODE_PEB", 537264641),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PERFORMANCE_SAVING_MODE_VALUE", 570691328, 2, "IDriveMode.SETTING_FUNC_PERFORMANCE_SAVING_MODE_VALUE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PGEAR_UNLOCK", 540148480, 2, "IVehicle.SETTING_FUNC_PGEAR_UNLOCK", "", new Value[] {
                new Value("PGEAR_UNLOCK_TYP_OFF", 2),
                new Value("PGEAR_UNLOCK_TYP_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PILOT_LANE_CHANGE_ASSIST", 671351552, 2, "IADAS.SETTING_FUNC_PILOT_LANE_CHANGE_ASSIST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_POWER_TRAIN_STOP", 570691584, 2, "IDriveMode.SETTING_FUNC_POWER_TRAIN_STOP", "", new Value[] {
                new Value("POWER_TRAIN_STOP_EV_BLOCKED", 570691585),
                new Value("POWER_TRAIN_STOP_EV_PLUS_BLOCKED", 570691587),
                new Value("POWER_TRAIN_STOP_HEV_BLOCKED", 570691586),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PRIVATE_LOCK", 537854208, 2, "IVehicle.SETTING_FUNC_PRIVATE_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PSD_BRIGHTNESS_DAYMODE", 689963008, 2, "IDayMode.SETTING_FUNC_PSD_BRIGHTNESS_DAYMODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PSD_BRIGHTNESS_SCREEN", 689963264, 2, "IDayMode.SETTING_FUNC_PSD_BRIGHTNESS_SCREEN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PSD_SCREEN_SWITCH", 539495936, 2, "IVehicle.SETTING_FUNC_PSD_SCREEN_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_P_GEAR_UNLOCK", 738265600, 2, "ISafety.SETTING_FUNC_P_GEAR_UNLOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RAIN_SENSOR_SENSITIVITY", 540148224, 2, "IVehicle.SETTING_FUNC_RAIN_SENSOR_SENSITIVITY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_COLLISION_WARNING", 537333760, 2, "IVehicle.SETTING_FUNC_REAR_COLLISION_WARNING", "Предупреждение заднего столкновения.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_CROSS_TRAFFIC_ALERT", 537332224, 2, "IADAS.SETTING_FUNC_REAR_CROSS_TRAFFIC_ALERT", "Предупреждение поперечного движения сзади.", new Value[] {
                new Value("COMMON_OFF", 0),
                new Value("COMMON_ON", 1),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_MIRROR_FOLD", 539755776, 2, "IVehicle.SETTING_FUNC_REAR_MIRROR_FOLD", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REDUCED_GUARD", 537921536, 2, "IVehicle.SETTING_FUNC_REDUCED_GUARD", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REFUELING_SWT", 538379008, 2, "IVehicle.SETTING_FUNC_REFUELING_SWT", "", new Value[] {
                new Value("REFUELING_SWT_UNLCK", 538379009),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REMOTE_DIAGNOSTICS", 539362048, 2, "IBcm.SETTING_FUNC_REMOTE_DIAGNOSTICS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RIS_SWITCH", 671678720, 2, "IADAS.SETTING_FUNC_RIS_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ROTATED_WHEELS_WARNING", 538771968, 2, "IVehicle.SETTING_FUNC_ROTATED_WHEELS_WARNING", "", new Value[] {
                new Value("ROTATED_WHEELS_WARNING_INFO_NONE", 0),
                new Value("ROTATED_WHEELS_WARNING_INFO_RIGHTWARD", 538772226),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ROTATED_WHEELS_WARNING_INFO", 538772224, 2, "IVehicle.SETTING_FUNC_ROTATED_WHEELS_WARNING_INFO", "", new Value[] {
                new Value("ROTATED_WHEELS_WARNING_INFO_NONE", 0),
                new Value("ROTATED_WHEELS_WARNING_INFO_RIGHTWARD", 538772226),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RVDC", 539361536, 2, "IVehicle.SETTING_FUNC_RVDC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SAILING_MODE", 537003008, 2, "IVehicle.SETTING_FUNC_SAILING_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SCREEN_SAVER_TIME", 539035392, 2, "IVehicle.SETTING_FUNC_SCREEN_SAVER_TIME", "", new Value[] {
                new Value("SCREEN_SAVER_TIME_10", 539035394),
                new Value("SCREEN_SAVER_TIME_5", 539035393),
                new Value("SCREEN_SAVER_TIME_NEVER", 539035395),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_BACKREST", 755171840, 2, "ISeat.SETTING_FUNC_SEAT_BACKREST", "", new Value[] {
                new Value("SEAT_BACKREST_BACKWARD", 755171842),
                new Value("SEAT_BACKREST_FORWARD", 755171841),
                new Value("SEAT_BACKREST_OFF", 0),
                new Value("SEAT_BACKREST_SIDE_ADJUST", 759236611),
                new Value("SEAT_BACKREST_SIDE_SUPPORT_BACKWARD", 755237378),
                new Value("SEAT_BACKREST_SIDE_SUPPORT_FORWARD", 755237377),
                new Value("SEAT_BACKREST_SIDE_SUPPORT_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_BACKREST_POS", 755172352, 2, "ISeat.SETTING_FUNC_SEAT_BACKREST_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_BACKREST_SIDE_SUPPORT", 755237376, 2, "ISeat.SETTING_FUNC_SEAT_BACKREST_SIDE_SUPPORT", "", new Value[] {
                new Value("SEAT_BACKREST_SIDE_SUPPORT_BACKWARD", 755237378),
                new Value("SEAT_BACKREST_SIDE_SUPPORT_FORWARD", 755237377),
                new Value("SEAT_BACKREST_SIDE_SUPPORT_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_CUSHION_EXTENSION", 755433728, 2, "ISeat.SETTING_FUNC_SEAT_CUSHION_EXTENSION", "", new Value[] {
                new Value("SEAT_CUSHION_EXTENSION_BACKWARD", 755433730),
                new Value("SEAT_CUSHION_EXTENSION_FORWARD", 755433729),
                new Value("SEAT_CUSHION_EXTENSION_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_CUSHION_TILT", 755171584, 2, "ISeat.SETTING_FUNC_SEAT_CUSHION_TILT", "", new Value[] {
                new Value("SEAT_CUSHION_TILT_DOWN", 755171586),
                new Value("SEAT_CUSHION_TILT_OFF", 0),
                new Value("SEAT_CUSHION_TILT_UP", 755171585),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_CUSHION_TILT_POS", 755172096, 2, "ISeat.SETTING_FUNC_SEAT_CUSHION_TILT_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_FOLD", 759236352, 2, "ISeat.SETTING_FUNC_SEAT_FOLD", "", new Value[] {
                new Value("SEAT_FOLD_STATE", 759236353),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_HEIGHT", 755106304, 2, "ISeat.SETTING_FUNC_SEAT_HEIGHT", "", new Value[] {
                new Value("SEAT_HEIGHT_DOWN", 755106306),
                new Value("SEAT_HEIGHT_OFF", 0),
                new Value("SEAT_HEIGHT_UP", 755106305),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_HEIGHT_POS", 755106816, 2, "ISeat.SETTING_FUNC_SEAT_HEIGHT_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_KNOB", 759237632, 2, "ISeat.SETTING_FUNC_SEAT_KNOB", "", new Value[] {
                new Value("SEAT_KNOB_DOWN", 759237633),
                new Value("SEAT_KNOB_IDEL", 759237635),
                new Value("SEAT_KNOB_UP", 759237634),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LEG_SUPPORT_HEIGHT", 755499264, 2, "ISeat.SETTING_FUNC_SEAT_LEG_SUPPORT_HEIGHT", "", new Value[] {
                new Value("SEAT_LEG_SUPPORT_HEIGHT_DOWN", 755499266),
                new Value("SEAT_LEG_SUPPORT_HEIGHT_OFF", 0),
                new Value("SEAT_LEG_SUPPORT_HEIGHT_UP", 755499265),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LEG_SUPPORT_HEIGHT_POS", 755499776, 2, "ISeat.SETTING_FUNC_SEAT_LEG_SUPPORT_HEIGHT_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LEG_SUPPORT_LENGTH", 755499520, 2, "ISeat.SETTING_FUNC_SEAT_LEG_SUPPORT_LENGTH", "", new Value[] {
                new Value("SEAT_LEG_SUPPORT_LENGTH_BACKWARD", 755499522),
                new Value("SEAT_LEG_SUPPORT_LENGTH_FORWARD", 755499521),
                new Value("SEAT_LEG_SUPPORT_LENGTH_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LEG_SUPPORT_LENGTH_POS", 755500032, 2, "ISeat.SETTING_FUNC_SEAT_LEG_SUPPORT_LENGTH_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LENGTH", 755106048, 2, "ISeat.SETTING_FUNC_SEAT_LENGTH", "", new Value[] {
                new Value("SEAT_LENGTH_BACKWARD", 755106050),
                new Value("SEAT_LENGTH_FORWARD", 755106049),
                new Value("SEAT_LENGTH_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LENGTH_POS", 755106560, 2, "ISeat.SETTING_FUNC_SEAT_LENGTH_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LUMBAR_EXTENDED", 755368448, 2, "ISeat.SETTING_FUNC_SEAT_LUMBAR_EXTENDED", "", new Value[] {
                new Value("SEAT_LUMBAR_EXTENDED_BACKWARD", 755368450),
                new Value("SEAT_LUMBAR_EXTENDED_FORWARD", 755368449),
                new Value("SEAT_LUMBAR_EXTENDED_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_LUMBAR_HEIGHT", 755368192, 2, "ISeat.SETTING_FUNC_SEAT_LUMBAR_HEIGHT", "", new Value[] {
                new Value("SEAT_LUMBAR_HEIGHT_DOWN", 755368194),
                new Value("SEAT_LUMBAR_HEIGHT_OFF", 0),
                new Value("SEAT_LUMBAR_HEIGHT_UP", 755368193),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_NUM", 759238400, 2, "ISeat.SETTING_FUNC_SEAT_NUM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_PHYSIOTHERAPY_PROGRAM", 760218112, 2, "ISeat.SETTING_FUNC_SEAT_PHYSIOTHERAPY_PROGRAM", "", new Value[] {
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_1", 760218113),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_10", 760218122),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_2", 760218114),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_3", 760218115),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_4", 760218116),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_5", 760218117),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_6", 760218118),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_7", 760218119),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_8", 760218120),
                new Value("SEAT_PHYSIOTHERAPY_PROGRAM_9", 760218121),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_PHYSIOTHERAPY_PROGRAM_ERROR", 760219392, 2, "ISeat.SETTING_FUNC_SEAT_PHYSIOTHERAPY_PROGRAM_ERROR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_PHYSIOTHERAPY_SWITCH", 760217856, 2, "ISeat.SETTING_FUNC_SEAT_PHYSIOTHERAPY_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_POSITION_SAVE", 759169280, 2, "ISeat.SETTING_FUNC_SEAT_POSITION_SAVE", "", new Value[] {
                new Value("SEAT_POSITION_SAVED_1", 759169281),
                new Value("SEAT_POSITION_SAVED_2", 759169282),
                new Value("SEAT_POSITION_SAVED_3", 759169283),
                new Value("SEAT_POSITION_SAVED_4", 759169284),
                new Value("SEAT_POSITION_SAVED_5", 759169285),
                new Value("SEAT_POSITION_SAVED_6", 759169286),
                new Value("SEAT_POSITION_SAVED_7", 759169287),
                new Value("SEAT_POSITION_SAVED_8", 759169288),
                new Value("SEAT_POSITION_SAVED_OFF", 0),
                new Value("SEAT_POSITION_SAVE_AS_1", 760218881),
                new Value("SEAT_POSITION_SAVE_AS_10", 760218896),
                new Value("SEAT_POSITION_SAVE_AS_11", 760218897),
                new Value("SEAT_POSITION_SAVE_AS_2", 760218882),
                new Value("SEAT_POSITION_SAVE_AS_3", 760218883),
                new Value("SEAT_POSITION_SAVE_AS_4", 760218884),
                new Value("SEAT_POSITION_SAVE_AS_5", 760218885),
                new Value("SEAT_POSITION_SAVE_AS_6", 760218886),
                new Value("SEAT_POSITION_SAVE_AS_7", 760218887),
                new Value("SEAT_POSITION_SAVE_AS_8", 760218888),
                new Value("SEAT_POSITION_SAVE_AS_9", 760218889),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_POSITION_SAVE_AS", 760218880, 2, "ISeat.SETTING_FUNC_SEAT_POSITION_SAVE_AS", "", new Value[] {
                new Value("SEAT_POSITION_SAVE_AS_1", 760218881),
                new Value("SEAT_POSITION_SAVE_AS_10", 760218896),
                new Value("SEAT_POSITION_SAVE_AS_11", 760218897),
                new Value("SEAT_POSITION_SAVE_AS_2", 760218882),
                new Value("SEAT_POSITION_SAVE_AS_3", 760218883),
                new Value("SEAT_POSITION_SAVE_AS_4", 760218884),
                new Value("SEAT_POSITION_SAVE_AS_5", 760218885),
                new Value("SEAT_POSITION_SAVE_AS_6", 760218886),
                new Value("SEAT_POSITION_SAVE_AS_7", 760218887),
                new Value("SEAT_POSITION_SAVE_AS_8", 760218888),
                new Value("SEAT_POSITION_SAVE_AS_9", 760218889),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_POSITION_SAVE_AS_RESTORE", 760219136, 2, "ISeat.SETTING_FUNC_SEAT_POSITION_SAVE_AS_RESTORE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_POSITION_SET", 759169536, 2, "ISeat.SETTING_FUNC_SEAT_POSITION_SET", "", new Value[] {
                new Value("SETTING_FUNC_SEAT_POSITION_SET_MEMBTN1", 1),
                new Value("SETTING_FUNC_SEAT_POSITION_SET_MEMBTN2", 2),
                new Value("SETTING_FUNC_SEAT_POSITION_SET_MEMBTN3", 3),
                new Value("SETTING_FUNC_SEAT_POSITION_SET_NO", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_REST_ALARM_TIME_END", 760218624, 2, "ISeat.SETTING_FUNC_SEAT_REST_ALARM_TIME_END", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_REST_PATTERN", 759234816, 2, "ISeat.SETTING_FUNC_SEAT_REST_PATTERN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEAT_SAVE_RESTORE_POPUP", 755041024, 2, "ISeat.SETTING_FUNC_SEAT_SAVE_RESTORE_POPUP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SEB_POP", 538379520, 2, "IVehicle.SETTING_FUNC_SEB_POP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SET_WALLPAPER_TO_DIM", 540283136, 2, "IVehicle.SETTING_FUNC_SET_WALLPAPER_TO_DIM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SOFT_BUTTON_SOUND_TYPE", 771883264, 2, "IAudio.SETTING_FUNC_SOFT_BUTTON_SOUND_TYPE", "", new Value[] {
                new Value("SOFT_BUTTON_SOUND_TYPE_1", 771883265),
                new Value("SOFT_BUTTON_SOUND_TYPE_2", 771883266),
                new Value("SOFT_BUTTON_SOUND_TYPE_3", 771883267),
                new Value("SOFT_BUTTON_SOUND_TYPE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SOUND_LOCKING_PROMPT_SWITCH", 738396416, 2, "ISafety.SETTING_FUNC_SOUND_LOCKING_PROMPT_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SOUND_WARNING_VOLUME", 538771712, 2, "IAudio.SETTING_FUNC_SOUND_WARNING_VOLUME", "", new Value[] {
                new Value("SOUND_WARNING_VOLUME_LEVEL_HIGH", 538771715),
                new Value("SOUND_WARNING_VOLUME_LEVEL_LOW", 538771713),
                new Value("SOUND_WARNING_VOLUME_LEVEL_MID", 538771714),
                new Value("SOUND_WARNING_VOLUME_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMITATION_MODE", 537068800, 2, "IVehicle.SETTING_FUNC_SPEED_LIMITATION_MODE", "", new Value[] {
                new Value("SPEED_LIMITATION_MODE_ASL", 537068802),
                new Value("SPEED_LIMITATION_MODE_AVSL", 537068801),
                new Value("SPEED_LIMITATION_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARN", 671482112, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARN", "", new Value[] {
                new Value("SPEED_LIMIT_WARNING_MODE_FLASHING", 671482370),
                new Value("SPEED_LIMIT_WARNING_MODE_NO_WARNING", 671482369),
                new Value("SPEED_LIMIT_WARNING_MODE_OFF", 0),
                new Value("SPEED_LIMIT_WARNING_MODE_SOUND", 671482371),
                new Value("SPEED_LIMIT_WARNING_OFFSET_0KM", 671482881),
                new Value("SPEED_LIMIT_WARNING_OFFSET_10KM", 671482883),
                new Value("SPEED_LIMIT_WARNING_OFFSET_5KM", 671482882),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_10KM", 671482885),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_5KM", 671482884),
                new Value("SPEED_LIMIT_WARNING_OFFSET_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_MODE", 671482368, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_MODE", "", new Value[] {
                new Value("SPEED_LIMIT_WARNING_MODE_FLASHING", 671482370),
                new Value("SPEED_LIMIT_WARNING_MODE_NO_WARNING", 671482369),
                new Value("SPEED_LIMIT_WARNING_MODE_OFF", 0),
                new Value("SPEED_LIMIT_WARNING_MODE_SOUND", 671482371),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET", 671482880, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET", "", new Value[] {
                new Value("SPEED_LIMIT_WARNING_OFFSET_0KM", 671482881),
                new Value("SPEED_LIMIT_WARNING_OFFSET_10KM", 671482883),
                new Value("SPEED_LIMIT_WARNING_OFFSET_5KM", 671482882),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_10KM", 671482885),
                new Value("SPEED_LIMIT_WARNING_OFFSET_MINUS_5KM", 671482884),
                new Value("SPEED_LIMIT_WARNING_OFFSET_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE", 671483136, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_MAX", 671483392, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_MAX", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_MIN", 671483648, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_MIN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_STEP", 671483904, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_STEP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_SWITCH", 671484160, 2, "IADAS.SETTING_FUNC_SPEED_LIMIT_WARNING_OFFSET_VALUE_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_STEERING_ASSISTANCE_LEVEL", 537331712, 2, "IVehicle.SETTING_FUNC_STEERING_ASSISTANCE_LEVEL", "", new Value[] {
                new Value("STEERING_ASSISTANCE_LEVEL_HIGH", 537331713),
                new Value("STEERING_ASSISTANCE_LEVEL_LOW", 537331715),
                new Value("STEERING_ASSISTANCE_LEVEL_MEDIUM", 537331714),
                new Value("STEERING_ASSISTANCE_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SUSPENSION_DEACTIVATION_DAMPENING", 538509824, 2, "IVehicle.SETTING_FUNC_SUSPENSION_DEACTIVATION_DAMPENING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SUSPENSION_DRIVER_ENTRY_CONTROL", 538510080, 2, "IVehicle.SETTING_FUNC_SUSPENSION_DRIVER_ENTRY_CONTROL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SUSPENSION_HEIGHT_ADJUST", 538509568, 2, "IVehicle.SETTING_FUNC_SUSPENSION_HEIGHT_ADJUST", "", new Value[] {
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_HIGH_1", 538509570),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_HIGH_2", 538509569),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_LOW_1", 538509572),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_LOW_2", 538509573),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_NORMAL", 538509571),
                new Value("SUSPENSION_HEIGHT_ADJUST_LEVEL_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TCAM_RESET", 538314240, 2, "IVehicle.SETTING_FUNC_TCAM_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TELM_PHOTO_SWT", 2123520, 3, "ISensor.SETTING_FUNC_TELM_PHOTO_SWT", "", new Value[] {
                new Value("SETTING_VALUE_TELM_PHOTO_SWT_DEFAULT", 2123521),
                new Value("SETTING_VALUE_TELM_PHOTO_SWT_OFF", 2123523),
                new Value("SETTING_VALUE_TELM_PHOTO_SWT_ON", 2123522),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_THINGS_LEFT_REMIND", 738395648, 2, "ISafety.SETTING_FUNC_THINGS_LEFT_REMIND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_THREE_SCREEN_ANIMATION", 540148992, 2, "IVehicle.SETTING_FUNC_THREE_SCREEN_ANIMATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRAFFIC_LIGHT_ATTENTION", 537332992, 2, "IVehicle.SETTING_FUNC_TRAFFIC_LIGHT_ATTENTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRAFFIC_LIGHT_ATTENTION_SOUND", 671154432, 2, "IADAS.SETTING_FUNC_TRAFFIC_LIGHT_ATTENTION_SOUND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRAFFIC_SIGN_RECOGNITION", 537592064, 2, "IVehicle.SETTING_FUNC_TRAFFIC_SIGN_RECOGNITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRAILER_MODE", 537268736, 2, "IVehicle.SETTING_FUNC_TRAILER_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRANSITION_END_COLOR", 705102592, 2, "IAmbienceLight.SETTING_FUNC_TRANSITION_END_COLOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRANSITION_MODE", 705102080, 2, "IAmbienceLight.SETTING_FUNC_TRANSITION_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRANSITION_START_COLOR", 705102336, 2, "IAmbienceLight.SETTING_FUNC_TRANSITION_START_COLOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRUNK_OPENING_PERCENTAGE", 738395904, 2, "ISafety.SETTING_FUNC_TRUNK_OPENING_PERCENTAGE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRUNK_OPENING_POSITION", 738265088, 2, "ISafety.SETTING_FUNC_TRUNK_OPENING_POSITION", "", new Value[] {
                new Value("TRUNK_OPENING_POSITION_LEVEL_1", 738265089),
                new Value("TRUNK_OPENING_POSITION_LEVEL_2", 738265090),
                new Value("TRUNK_OPENING_POSITION_LEVEL_3", 738265091),
                new Value("TRUNK_OPENING_POSITION_LEVEL_4", 738265092),
                new Value("TRUNK_OPENING_POSITION_LEVEL_5", 738265093),
                new Value("TRUNK_OPENING_POSITION_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRUNK_STATE", 738330112, 2, "ISafety.SETTING_FUNC_TRUNK_STATE", "", new Value[] {
                new Value("TRUNK_STATE_FULL_CLOSE", 738330114),
                new Value("TRUNK_STATE_FULL_OPEN", 738330118),
                new Value("TRUNK_STATE_HALF_CLOSE", 738330128),
                new Value("TRUNK_STATE_MOVE_DOWN", 738330119),
                new Value("TRUNK_STATE_MOVE_DOWN_BREAK", 738330120),
                new Value("TRUNK_STATE_MOVE_UP", 738330115),
                new Value("TRUNK_STATE_MOVE_UP_BREAK", 738330116),
                new Value("TRUNK_STATE_STOP_DURING_CLOSE", 738330121),
                new Value("TRUNK_STATE_STOP_DURING_OPEN", 738330117),
                new Value("TRUNK_STATE_STOP_MIN_POSITION", 738330129),
                new Value("TRUNK_STATE_UNKNOW", 738330113),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TTS_ASY_EYES_OFF_WARN_RQRD_SOUND", 671746048, 2, "IADAS.SETTING_FUNC_TTS_ASY_EYES_OFF_WARN_RQRD_SOUND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TTS_ASY_LAN_CHG_REMINDER", 671745280, 2, "IADAS.SETTING_FUNC_TTS_ASY_LAN_CHG_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TWOSTEP_UNLOCKING", 537922048, 2, "IVehicle.SETTING_FUNC_TWOSTEP_UNLOCKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_ENGINE_TRAVLLED_DISTANCE", 541066496, 2, "IVehicle.SETTING_FUNC_VEHICLE_ENGINE_TRAVLLED_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_EV_TRAVLLED_DISTANCE", 541066240, 2, "IVehicle.SETTING_FUNC_VEHICLE_EV_TRAVLLED_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_OIL_TOTAL_TRAVLLED_DISTANCE", 541065472, 2, "IVehicle.SETTING_FUNC_VEHICLE_OIL_TOTAL_TRAVLLED_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_OIL_USING_DAYS", 541065728, 2, "IVehicle.SETTING_FUNC_VEHICLE_OIL_USING_DAYS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_BRST_MODE", 671748352, 2, "IADAS.SETTING_FUNC_VOICE_BRST_MODE", "", new Value[] {
                new Value("VOICE_BRST_MODE_DETAIL", 671748353),
                new Value("VOICE_BRST_MODE_STREAM_LINE", 671748354),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_CONTROL_LOCKING", 540284160, 2, "IVehicle.SETTING_FUNC_VOICE_CONTROL_LOCKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_CTRL_REQ", 2123264, 3, "ISensor.SETTING_FUNC_VSTD_CTRL_REQ", "", new Value[] {
                new Value("SETTING_VALUE_VSTD_CTRL_REQ_NO_REQ", 2123265),
                new Value("SETTING_VALUE_VSTD_CTRL_REQ_OFF", 2123267),
                new Value("SETTING_VALUE_VSTD_CTRL_REQ_ON", 2123266),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_FAIL_TO_OPEN_TELM", 536953600, 2, "IVehicle.SETTING_FUNC_VSTD_FAIL_TO_OPEN_TELM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_CH", 536954368, 2, "IVehicle.SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_CH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_PS", 536954624, 2, "IVehicle.SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_PS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_SV", 536954880, 2, "IVehicle.SETTING_FUNC_VSTD_VFC_VEHICLE_SENTRY_FT_DET_SV", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WELCOME_LIGHT", 721617152, 2, "ILamp.SETTING_FUNC_WELCOME_LIGHT", "", new Value[] {
                new Value("WELCOME_LIGHT_MODE_1", 721617409),
                new Value("WELCOME_LIGHT_MODE_2", 721617410),
                new Value("WELCOME_LIGHT_MODE_3", 721617411),
                new Value("WELCOME_LIGHT_MODE_4", 721617412),
                new Value("WELCOME_LIGHT_MODE_5", 721617413),
                new Value("WELCOME_LIGHT_MODE_6", 721617414),
                new Value("WELCOME_LIGHT_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WELCOME_LIGHT_MODE", 721617408, 2, "ILamp.SETTING_FUNC_WELCOME_LIGHT_MODE", "", new Value[] {
                new Value("WELCOME_LIGHT_MODE_1", 721617409),
                new Value("WELCOME_LIGHT_MODE_2", 721617410),
                new Value("WELCOME_LIGHT_MODE_3", 721617411),
                new Value("WELCOME_LIGHT_MODE_4", 721617412),
                new Value("WELCOME_LIGHT_MODE_5", 721617413),
                new Value("WELCOME_LIGHT_MODE_6", 721617414),
                new Value("WELCOME_LIGHT_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WINDOW_CLOSE_SUNCURTAIN", 537395456, 2, "IVehicle.SETTING_FUNC_WINDOW_CLOSE_SUNCURTAIN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WINDOW_VENTILATE", 537396736, 2, "IVehicle.SETTING_FUNC_WINDOW_VENTILATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WINDSCREEN_SERVICE_POSITION", 537657600, 2, "IVehicle.SETTING_FUNC_WINDSCREEN_SERVICE_POSITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_TYPE_VSTD_MODE_STS", 536951296, 2, "IVehicle.SETTING_TYPE_VSTD_MODE_STS", "", new Value[] {
                new Value("SETTING_VALUE_VSTD_MODE_STS_OFF", 536951297),
                new Value("SETTING_VALUE_VSTD_MODE_STS_STANDBY", 536951298),
                new Value("SETTING_VALUE_VSTD_MODE_STS_ON", 536951299),
                new Value("SETTING_VALUE_VSTD_MODE_STS_ALERT", 536951300),
                new Value("SETTING_VALUE_VSTD_MODE_STS_ALARM", 536951301),
        }));
        put(byId, byKey, new Entry("TIRE_FLAT_STATE", 5263360, 3, "ITireSensor.TIRE_FLAT_STATE", "", new Value[] {
                new Value("ABS_WARNING_STATE_FLSG", 1058306),
                new Value("ABS_WARNING_STATE_OFF", 1058308),
                new Value("ABS_WARNING_STATE_ON", 1058305),
                new Value("ABS_WARNING_STATE_RESD", 1058307),
                new Value("BRK_WARNING_STATE_OFF", 1058050),
                new Value("BRK_WARNING_STATE_ON", 1058049),
                new Value("CHARGE_PLUG_STATE_CHARGING", 605225474),
                new Value("CHARGE_PLUG_STATE_CHARGING_FAIL", 605225486),
                new Value("CHARGE_PLUG_STATE_CHARGING_PAUSE", 605225483),
                new Value("CHARGE_PLUG_STATE_COMPLETED", 605225475),
                new Value("CHARGE_PLUG_STATE_CONNECTED", 605225481),
                new Value("CHARGE_PLUG_STATE_DISCHARGING", 605225478),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_COMPLETED", 605225479),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_END", 605225488),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_FAIL", 605225487),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_PAUSE", 605225484),
                new Value("CHARGE_PLUG_STATE_DISCONNECTED", 605225482),
                new Value("CHARGE_PLUG_STATE_ERROR", 605225477),
                new Value("CHARGE_PLUG_STATE_HEATING", 605225480),
                new Value("CHARGE_PLUG_STATE_MULTI", 605225476),
                new Value("CHARGE_PLUG_STATE_PREPARED", 605225473),
                new Value("CHARGE_PLUG_STATE_RESERVE_WAITING", 605225485),
                new Value("CHARGE_PLUG_STATE_TARGET_VALUE_OWER_THAN_CURRENT", 606078720),
                new Value("CHARGE_PLUG_STATE_UNKNOWN", 255),
                new Value("CHARGE_STATE_CHARGING", 637665539),
                new Value("CHARGE_STATE_ERROR", 637665541),
                new Value("CHARGE_STATE_FAST_CHARGING", 637665568),
                new Value("CHARGE_STATE_FOD", 637665544),
                new Value("CHARGE_STATE_FULLY_CHARGED", 637665540),
                new Value("CHARGE_STATE_NO_DEVICE", 637665537),
                new Value("CHARGE_STATE_OFF", 0),
                new Value("CHARGE_STATE_OVERHEAT", 637665542),
                new Value("CHARGE_STATE_OVERPOWER", 637665543),
                new Value("CHARGE_STATE_OVERVOLTAGE", 637665552),
                new Value("CHARGE_STATE_PEPS_INTERRUPT", 637665545),
                new Value("CHARGE_STATE_STANDBY", 637665538),
                new Value("CHARGE_STATE_TAKE_MOBILE_DEVICE", 637665584),
                new Value("CHARGE_STATE_UNKNOWN", 255),
                new Value("CHARGING_PLUG_STATE_CONNECTED_WAITING", 605225493),
                new Value("CHARGING_PLUG_STATE_DISCONNECTED", 605225489),
                new Value("CHARGING_PLUG_STATE_DIS_CHRGN_CONNECTED", 605225492),
                new Value("CHARGING_PLUG_STATE_FAULT", 605225495),
                new Value("CHARGING_PLUG_STATE_NONE", 605225496),
                new Value("CHARGING_PLUG_STATE_QUICK_CHRGN_CONNECTED", 605225491),
                new Value("CHARGING_PLUG_STATE_SLOW_CHRGN_CONNECTED", 605225490),
                new Value("CHARGING_PLUG_STATE_WRONG_OPERATION", 605225494),
                new Value("ENGINE_START_STOP_STATE_AUTO_STOPPING", 2103047),
                new Value("ENGINE_START_STOP_STATE_ENGINE_RESTART", 2103045),
                new Value("ENGINE_START_STOP_STATE_OPERATION", 2103046),
                new Value("ENGINE_START_STOP_STATE_STANDBY", 2103042),
                new Value("ENGINE_START_STOP_STATE_STARTER_RESTART", 2103044),
                new Value("ENGINE_START_STOP_STATE_STOPPED", 2103043),
                new Value("ENGINE_START_STOP_STATE_UNKNOWN", -1),
                new Value("ESC_WARNING_STATE_FLSG", 1058562),
                new Value("ESC_WARNING_STATE_OFF", 1058564),
                new Value("ESC_WARNING_STATE_ON", 1058561),
                new Value("ESC_WARNING_STATE_RESD", 1058563),
                new Value("IGNITION_STATE_ACC", 2097412),
                new Value("IGNITION_STATE_DRIVING", 2097415),
                new Value("IGNITION_STATE_LOCK", 2097410),
                new Value("...TRUNCATED_CANDIDATES", 105),
        }));
        put(byId, byKey, new Entry("TIRE_MSG_FLAG_FRONT_LEFT", 5249280, 3, "ITireSensor.TIRE_MSG_FLAG_FRONT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_MSG_FLAG_FRONT_RIGHT", 5251072, 3, "ITireSensor.TIRE_MSG_FLAG_FRONT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_MSG_FLAG_REAR_LEFT", 5251328, 3, "ITireSensor.TIRE_MSG_FLAG_REAR_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_MSG_FLAG_REAR_RIGHT", 5251584, 3, "ITireSensor.TIRE_MSG_FLAG_REAR_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_PRESSURE_FRONT_LEFT", 5243136, 3, "ITireSensor.TIRE_PRESSURE_FRONT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_PRESSURE_FRONT_RIGHT", 5243392, 3, "ITireSensor.TIRE_PRESSURE_FRONT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_PRESSURE_REAR_LEFT", 5243648, 3, "ITireSensor.TIRE_PRESSURE_REAR_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_PRESSURE_REAR_RIGHT", 5243904, 3, "ITireSensor.TIRE_PRESSURE_REAR_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SENSOR_BATTERY_LOW_STATE", 5275648, 3, "ITireSensor.TIRE_SENSOR_BATTERY_LOW_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SENSOR_STATES_FRONT_LEFT", 5248256, 3, "ITireSensor.TIRE_SENSOR_STATES_FRONT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SENSOR_STATES_FRONT_RIGHT", 5248512, 3, "ITireSensor.TIRE_SENSOR_STATES_FRONT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SENSOR_STATES_REAR_LEFT", 5248768, 3, "ITireSensor.TIRE_SENSOR_STATES_REAR_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SENSOR_STATES_REAR_RIGHT", 5249024, 3, "ITireSensor.TIRE_SENSOR_STATES_REAR_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_SYSTEM_FAILURE_STATE", 5271552, 3, "ITireSensor.TIRE_SYSTEM_FAILURE_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_TEMPERATURE_FRONT_LEFT", 5244160, 3, "ITireSensor.TIRE_TEMPERATURE_FRONT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_TEMPERATURE_FRONT_RIGHT", 5244416, 3, "ITireSensor.TIRE_TEMPERATURE_FRONT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_TEMPERATURE_REAR_LEFT", 5244672, 3, "ITireSensor.TIRE_TEMPERATURE_REAR_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_TEMPERATURE_REAR_RIGHT", 5244928, 3, "ITireSensor.TIRE_TEMPERATURE_REAR_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_TEMPERATURE_STATE", 5267456, 3, "ITireSensor.TIRE_TEMPERATURE_STATE", "", new Value[] {
                new Value("ABS_WARNING_STATE_FLSG", 1058306),
                new Value("ABS_WARNING_STATE_OFF", 1058308),
                new Value("ABS_WARNING_STATE_ON", 1058305),
                new Value("ABS_WARNING_STATE_RESD", 1058307),
                new Value("BRK_WARNING_STATE_OFF", 1058050),
                new Value("BRK_WARNING_STATE_ON", 1058049),
                new Value("CHARGE_PLUG_STATE_CHARGING", 605225474),
                new Value("CHARGE_PLUG_STATE_CHARGING_FAIL", 605225486),
                new Value("CHARGE_PLUG_STATE_CHARGING_PAUSE", 605225483),
                new Value("CHARGE_PLUG_STATE_COMPLETED", 605225475),
                new Value("CHARGE_PLUG_STATE_CONNECTED", 605225481),
                new Value("CHARGE_PLUG_STATE_DISCHARGING", 605225478),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_COMPLETED", 605225479),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_END", 605225488),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_FAIL", 605225487),
                new Value("CHARGE_PLUG_STATE_DISCHARGING_PAUSE", 605225484),
                new Value("CHARGE_PLUG_STATE_DISCONNECTED", 605225482),
                new Value("CHARGE_PLUG_STATE_ERROR", 605225477),
                new Value("CHARGE_PLUG_STATE_HEATING", 605225480),
                new Value("CHARGE_PLUG_STATE_MULTI", 605225476),
                new Value("CHARGE_PLUG_STATE_PREPARED", 605225473),
                new Value("CHARGE_PLUG_STATE_RESERVE_WAITING", 605225485),
                new Value("CHARGE_PLUG_STATE_TARGET_VALUE_OWER_THAN_CURRENT", 606078720),
                new Value("CHARGE_PLUG_STATE_UNKNOWN", 255),
                new Value("CHARGE_STATE_CHARGING", 637665539),
                new Value("CHARGE_STATE_ERROR", 637665541),
                new Value("CHARGE_STATE_FAST_CHARGING", 637665568),
                new Value("CHARGE_STATE_FOD", 637665544),
                new Value("CHARGE_STATE_FULLY_CHARGED", 637665540),
                new Value("CHARGE_STATE_NO_DEVICE", 637665537),
                new Value("CHARGE_STATE_OFF", 0),
                new Value("CHARGE_STATE_OVERHEAT", 637665542),
                new Value("CHARGE_STATE_OVERPOWER", 637665543),
                new Value("CHARGE_STATE_OVERVOLTAGE", 637665552),
                new Value("CHARGE_STATE_PEPS_INTERRUPT", 637665545),
                new Value("CHARGE_STATE_STANDBY", 637665538),
                new Value("CHARGE_STATE_TAKE_MOBILE_DEVICE", 637665584),
                new Value("CHARGE_STATE_UNKNOWN", 255),
                new Value("CHARGING_PLUG_STATE_CONNECTED_WAITING", 605225493),
                new Value("CHARGING_PLUG_STATE_DISCONNECTED", 605225489),
                new Value("CHARGING_PLUG_STATE_DIS_CHRGN_CONNECTED", 605225492),
                new Value("CHARGING_PLUG_STATE_FAULT", 605225495),
                new Value("CHARGING_PLUG_STATE_NONE", 605225496),
                new Value("CHARGING_PLUG_STATE_QUICK_CHRGN_CONNECTED", 605225491),
                new Value("CHARGING_PLUG_STATE_SLOW_CHRGN_CONNECTED", 605225490),
                new Value("CHARGING_PLUG_STATE_WRONG_OPERATION", 605225494),
                new Value("ENGINE_START_STOP_STATE_AUTO_STOPPING", 2103047),
                new Value("ENGINE_START_STOP_STATE_ENGINE_RESTART", 2103045),
                new Value("ENGINE_START_STOP_STATE_OPERATION", 2103046),
                new Value("ENGINE_START_STOP_STATE_STANDBY", 2103042),
                new Value("ENGINE_START_STOP_STATE_STARTER_RESTART", 2103044),
                new Value("ENGINE_START_STOP_STATE_STOPPED", 2103043),
                new Value("ENGINE_START_STOP_STATE_UNKNOWN", -1),
                new Value("ESC_WARNING_STATE_FLSG", 1058562),
                new Value("ESC_WARNING_STATE_OFF", 1058564),
                new Value("ESC_WARNING_STATE_ON", 1058561),
                new Value("ESC_WARNING_STATE_RESD", 1058563),
                new Value("IGNITION_STATE_ACC", 2097412),
                new Value("IGNITION_STATE_DRIVING", 2097415),
                new Value("IGNITION_STATE_LOCK", 2097410),
                new Value("...TRUNCATED_CANDIDATES", 105),
        }));
        put(byId, byKey, new Entry("TIRE_TPMS_SYS_STATES", 5259264, 3, "ITireSensor.TIRE_TPMS_SYS_STATES", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_LEFT", 5245184, 3, "ITireSensor.TIRE_WARNING_FRONT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_LEFT_QUICKLEAKING", 5247232, 3, "ITireSensor.TIRE_WARNING_FRONT_LEFT_QUICKLEAKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_LEFT_TEMPERATURE", 5246208, 3, "ITireSensor.TIRE_WARNING_FRONT_LEFT_TEMPERATURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_RIGHT", 5245440, 3, "ITireSensor.TIRE_WARNING_FRONT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_RIGHT_QUICKLEAKING", 5247488, 3, "ITireSensor.TIRE_WARNING_FRONT_RIGHT_QUICKLEAKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_FRONT_RIGHT_TEMPERATURE", 5246464, 3, "ITireSensor.TIRE_WARNING_FRONT_RIGHT_TEMPERATURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_LEFT", 5245696, 3, "ITireSensor.TIRE_WARNING_REAR_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_LEFT_QUICKLEAKING", 5247744, 3, "ITireSensor.TIRE_WARNING_REAR_LEFT_QUICKLEAKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_LEFT_TEMPERATURE", 5246720, 3, "ITireSensor.TIRE_WARNING_REAR_LEFT_TEMPERATURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_RIGHT", 5245952, 3, "ITireSensor.TIRE_WARNING_REAR_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_RIGHT_QUICKLEAKING", 5248000, 3, "ITireSensor.TIRE_WARNING_REAR_RIGHT_QUICKLEAKING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TIRE_WARNING_REAR_RIGHT_TEMPERATURE", 5246976, 3, "ITireSensor.TIRE_WARNING_REAR_RIGHT_TEMPERATURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_DC_AVERAGE_FUEL_CONSUMPTION", 612369924, 2, "ITripData.TRIP_DC_AVERAGE_FUEL_CONSUMPTION", "", new Value[] {
                new Value("TRIP_DC_AVERAGE_FUEL_CONSUMPTION", 612369924),
                new Value("TRIP_DC_AVERAGE_FUEL_CONSUMPTION", 612369924),
        }));
        put(byId, byKey, new Entry("TRIP_DC_AVERAGE_POWER_CONSUMPTION", 612369925, 2, "ITripData.TRIP_DC_AVERAGE_POWER_CONSUMPTION", "", new Value[] {
                new Value("TRIP_DC_AVERAGE_POWER_CONSUMPTION", 612369925),
                new Value("TRIP_DC_AVERAGE_POWER_CONSUMPTION", 612369925),
        }));
        put(byId, byKey, new Entry("TRIP_DC_AVERAGE_SPEED", 612369922, 2, "ITripData.TRIP_DC_AVERAGE_SPEED", "", new Value[] {
                new Value("TRIP_DC_AVERAGE_SPEED", 612369922),
                new Value("TRIP_DC_AVERAGE_SPEED", 612369922),
        }));
        put(byId, byKey, new Entry("TRIP_DC_SUBTOTAL_MILEAGE", 612369921, 2, "ITripData.TRIP_DC_SUBTOTAL_MILEAGE", "", new Value[] {
                new Value("TRIP_DC_SUBTOTAL_MILEAGE", 612369921),
                new Value("TRIP_DC_SUBTOTAL_MILEAGE", 612369921),
        }));
        put(byId, byKey, new Entry("TRIP_DC_TRAVEL_TIME", 612369923, 2, "ITripData.TRIP_DC_TRAVEL_TIME", "", new Value[] {
                new Value("TRIP_DC_TRAVEL_TIME", 612369923),
                new Value("TRIP_DC_TRAVEL_TIME", 612369923),
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AIRCDNEGY_DISTBN", 612370944, 2, "ITripData.TRIP_FUNC_AIRCDNEGY_DISTBN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AUTO_RESET_OPTION", 612369152, 2, "ITripData.TRIP_FUNC_AUTO_RESET_OPTION", "", new Value[] {
                new Value("AUTO_RESET_OPTION_4_HOURS", 612369153),
                new Value("AUTO_RESET_OPTION_CHARGING", 612369154),
                new Value("AUTO_RESET_OPTION_PARKING", 612369156),
                new Value("AUTO_RESET_OPTION_PARKING_OIL", 612369155),
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AVERAGE_CONSUME_100", 612372480, 2, "ITripData.TRIP_FUNC_AVERAGE_CONSUME_100", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AVERAGE_CONSUME_50", 612371968, 2, "ITripData.TRIP_FUNC_AVERAGE_CONSUME_50", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AVERAGE_EN_CONSUME_100", 612372992, 2, "ITripData.TRIP_FUNC_AVERAGE_EN_CONSUME_100", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_AVERAGE_EN_CONSUME_50", 612372736, 2, "ITripData.TRIP_FUNC_AVERAGE_EN_CONSUME_50", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_BATTTHERMEGY_DISTBN", 612371200, 2, "ITripData.TRIP_FUNC_BATTTHERMEGY_DISTBN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_CURRENT_TRIP_RESET", 612370432, 2, "ITripData.TRIP_FUNC_CURRENT_TRIP_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_DIM_UI_SWITCH", 612370176, 2, "ITripData.TRIP_FUNC_DIM_UI_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_DRIVING_COMPUTER", 612369920, 2, "ITripData.TRIP_FUNC_DRIVING_COMPUTER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_DRVREGY_DISTBN", 612370688, 2, "ITripData.TRIP_FUNC_DRVREGY_DISTBN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_ENERGY_RESET", 612371712, 2, "ITripData.TRIP_FUNC_ENERGY_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_OTHEREGY_DISTBN", 612371456, 2, "ITripData.TRIP_FUNC_OTHEREGY_DISTBN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_RESET", 612368896, 2, "ITripData.TRIP_FUNC_RESET", "", new Value[] {
                new Value("AUTO_RESET_OPTION_4_HOURS", 612369153),
                new Value("AUTO_RESET_OPTION_CHARGING", 612369154),
                new Value("AUTO_RESET_OPTION_PARKING", 612369156),
                new Value("AUTO_RESET_OPTION_PARKING_OIL", 612369155),
        }));
        put(byId, byKey, new Entry("TRIP_FUNC_TRIP_RNG_SWT", 612373248, 2, "ITripData.TRIP_FUNC_TRIP_RNG_SWT", "", new Value[] {
                new Value("TRIP_FUNC_TRIP_RNG_SWT_100KM", 612373250),
                new Value("TRIP_FUNC_TRIP_RNG_SWT_50KM", 612373249),
        }));
        put(byId, byKey, new Entry("TRIP_INFO_TYPE_AVG_CONSUMPTION_ARRAY", 20480, 2, "ITripData.TRIP_INFO_TYPE_AVG_CONSUMPTION_ARRAY", "", new Value[] {
                new Value("TRIP_INFO_TYPE_AVG_CONSUMPTION_ARRAY", 20480),
                new Value("TRIP_INFO_TYPE_AVG_CONSUMPTION_ARRAY", 20480),
        }));
        put(byId, byKey, new Entry("TYPE_AVG_FUEL_CONSUMPTION", 4194560, 3, "IVirtualSensor.TYPE_AVG_FUEL_CONSUMPTION", "", new Value[] {
                new Value("TRIP_DC_AVERAGE_FUEL_CONSUMPTION", 612369924),
        }));
        put(byId, byKey, new Entry("TYPE_AVG_FUEL_CONSUMPTION_ONE_IGNITION", 4195072, 3, "IVirtualSensor.TYPE_AVG_FUEL_CONSUMPTION_ONE_IGNITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_EV_BATTERY_PERCENTAGE", 4210688, 3, "IVirtualSensor.TYPE_EV_BATTERY_PERCENTAGE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_FUEL_PERCENTAGE", 4211968, 3, "IVirtualSensor.TYPE_FUEL_PERCENTAGE", "", new Value[] {
                new Value("TRIP_AIR_CONDITION_PERCENTAGE", 12294),
                new Value("TRIP_ED_BATTERY_CLIMATE_PERCENTAGE", 12290),
                new Value("TRIP_ED_PROPULSION_PERCENTAGE", 12289),
        }));
        put(byId, byKey, new Entry("TYPE_JOY_LIMIT_STATE", 4195840, 3, "IVirtualSensor.TYPE_JOY_LIMIT_STATE", "", new Value[] {
                new Value("JOY_LIMIT_STATE_OFF", 4195841),
                new Value("JOY_LIMIT_STATE_ON", 4195842),
                new Value("JOY_LIMIT_STATE_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_MILEAGE", 4206592, 3, "IVirtualSensor.TYPE_MAINTENANCE_MILEAGE", "", new Value[] {
                new Value("TRIP_DC_SUBTOTAL_MILEAGE", 612369921),
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_MILEAGE_EV_SINCE_LAST", 4215808, 3, "IVirtualSensor.TYPE_MAINTENANCE_MILEAGE_EV_SINCE_LAST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_MILEAGE_PURE_OIL_SINCE_LAST", 4216064, 3, "IVirtualSensor.TYPE_MAINTENANCE_MILEAGE_PURE_OIL_SINCE_LAST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_MILEAGE_REMIND", 4207104, 3, "IVirtualSensor.TYPE_MAINTENANCE_MILEAGE_REMIND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_MILEAGE_SINCE_LAST_ALL", 4215552, 3, "IVirtualSensor.TYPE_MAINTENANCE_MILEAGE_SINCE_LAST_ALL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_MAINTENANCE_TIME", 4206848, 3, "IVirtualSensor.TYPE_MAINTENANCE_TIME", "", new Value[] {
                new Value("AMBIENCE_LIGHT_CONTROL_MODE_TIME", 705168901),
                new Value("AUTO_SEAT_HEATING_TIME_1", 268764673),
                new Value("AUTO_SEAT_HEATING_TIME_2", 268764674),
                new Value("AUTO_SEAT_HEATING_TIME_3", 268764675),
                new Value("AUTO_SEAT_HEATING_TIME_4", 268764676),
                new Value("AUTO_SEAT_HEATING_TIME_OFF", 0),
                new Value("AUTO_SEAT_MASSAGE_TIME_1", 268765441),
                new Value("AUTO_SEAT_MASSAGE_TIME_2", 268765442),
                new Value("AUTO_SEAT_MASSAGE_TIME_3", 268765443),
                new Value("AUTO_SEAT_MASSAGE_TIME_OFF", 0),
                new Value("AUTO_SEAT_VENTILATION_TIME_1", 268764161),
                new Value("AUTO_SEAT_VENTILATION_TIME_2", 268764162),
                new Value("AUTO_SEAT_VENTILATION_TIME_3", 268764163),
                new Value("AUTO_SEAT_VENTILATION_TIME_4", 268764164),
                new Value("AUTO_SEAT_VENTILATION_TIME_OFF", 0),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_1", 269026049),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_2", 269026050),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_3", 269026051),
                new Value("AUTO_STEERING_WHEEL_HEAT_TIME_OFF", 0),
                new Value("CARPET_LIGHT_TIME_MODE_45s", 0),
                new Value("CARPET_LIGHT_TIME_MODE_60s", 1),
                new Value("CARPET_LIGHT_TIME_MODE_75s", 2),
                new Value("CARPET_LIGHT_TIME_MODE_90s", 3),
                new Value("POWER_CHARGE_MODE_TIMEOUT", 606078982),
                new Value("PRE_CHARGING_STATUS_TIMEOUT", 605094920),
                new Value("SCREEN_SAVER_TIME_10", 539035394),
                new Value("SCREEN_SAVER_TIME_5", 539035393),
                new Value("SCREEN_SAVER_TIME_NEVER", 539035395),
                new Value("SEB_MAX_TIME", 538379527),
                new Value("TRIP_DC_TRAVEL_TIME", 612369923),
                new Value("UNIT_TIME_INDICATION_24H", 620888322),
                new Value("UNIT_TIME_INDICATION_AM_PM", 620888321),
        }));
        put(byId, byKey, new Entry("TYPE_OIL_HEALTH", 4216576, 3, "IVirtualSensor.TYPE_OIL_HEALTH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_OIL_NUMBER_OF_DAY", 4216320, 3, "IVirtualSensor.TYPE_OIL_NUMBER_OF_DAY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("TYPE_POTENTIAL_ENDURANCE_MILEAGE", 4211456, 3, "IVirtualSensor.TYPE_POTENTIAL_ENDURANCE_MILEAGE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_ANNOUNCEMENTS_FOR_NOA", -2130574336, 2, "IVoice.VOICE_FUNC_ANNOUNCEMENTS_FOR_NOA", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_ANNOUNCEMENTS_FOR_NOA_START", -2130574080, 2, "IVoice.VOICE_FUNC_ANNOUNCEMENTS_FOR_NOA_START", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_DRVR_SOD_REQ_CHG", -2130639872, 2, "IADAS.SETTING_FUNC_DRVR_SOD_REQ_CHG", "", new Value[] {
                new Value("DRVR_SOD_REQ_CHG_LEFT_LAN", -2130639871),
                new Value("DRVR_SOD_REQ_CHG_NO", 254),
                new Value("DRVR_SOD_REQ_CHG_RIGHT_LAN", -2130639870),
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_DRVR_SOD_REQ_PILOT", -2130639360, 2, "IADAS.SETTING_FUNC_DRVR_SOD_REQ_PILOT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_SOD_LANE_CHG_SWITCH", 671748608, 2, "IADAS.VOICE_FUNC_SOD_LANE_CHG_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_FUNC_SOD_PILOT_CFIRM", 805373696, 2, "IVoice.VOICE_FUNC_SOD_PILOT_CFIRM", "", new Value[] {
                new Value("SOD_PILOT_CFIRM_ACTIVE", 805373697),
                new Value("SOD_PILOT_CFIRM_ACTIVE_NO_CMD", 805373699),
                new Value("SOD_PILOT_CFIRM_CAN_NOT_ACTIVE", 805373698),
        }));
        put(byId, byKey, new Entry("WPC_FUNC_CHARGE_FORGET_REMINDER", 637731072, 2, "IWpc.WPC_FUNC_CHARGE_FORGET_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("WPC_FUNC_CHARGE_STATES", 637665536, 2, "IWpc.WPC_FUNC_CHARGE_STATES", "", new Value[] {
        }));
        put(byId, byKey, new Entry("WPC_FUNC_WORK_MODE", 637600000, 2, "IWpc.WPC_FUNC_WORK_MODE", "", new Value[] {
                new Value("WORK_MODE_AUTO", 637600001),
                new Value("WORK_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_DHU_FAST_START_MODE", 539512832, 2, "IVehicle.SETTING_DHU_FAST_START_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ABNORMAL_VEHICLE_ALARM", 539492608, 2, "IVehicle.SETTING_FUNC_ABNORMAL_VEHICLE_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ABNORMAL_VEHICLE_ALARM_MODE", 539497984, 2, "IVehicle.SETTING_FUNC_ABNORMAL_VEHICLE_ALARM_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ARTIFICIAL_SOUND_PREVIEW", 539428608, 2, "IVehicle.SETTING_FUNC_ARTIFICIAL_SOUND_PREVIEW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ARTIFICIAL_SOUND_SWITCH", 538575616, 2, "IVehicle.SETTING_FUNC_ARTIFICIAL_SOUND_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AVAS_SOUND_TYPE", 538576640, 2, "IVehicle.SETTING_FUNC_AVAS_SOUND_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AVAS_SWITCH", 538576128, 2, "IVehicle.SETTING_FUNC_AVAS_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_AVAS_VOLUME", 538576384, 2, "IVehicle.SETTING_FUNC_AVAS_VOLUME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_BLIND_CAMERA_SYNC_RT_TURN", 538772480, 2, "IVehicle.SETTING_FUNC_BLIND_CAMERA_SYNC_RT_TURN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CDC_MODE", 540285440, 2, "IVehicle.SETTING_FUNC_CDC_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CDC_MODE_WARNING", 540285696, 2, "IVehicle.SETTING_FUNC_CDC_MODE_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CONGESTION_AHEAD_ALARM", 539493376, 2, "IVehicle.SETTING_FUNC_CONGESTION_AHEAD_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_CSD_POSITION", 539504640, 2, "IVehicle.SETTING_FUNC_CSD_POSITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DANGEROUS_ROAD_ALARM", 539492864, 2, "IVehicle.SETTING_FUNC_DANGEROUS_ROAD_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DANGEROUS_ROAD_ALARM_MODE", 539498240, 2, "IVehicle.SETTING_FUNC_DANGEROUS_ROAD_ALARM_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DAYLIGHT_SAVING_TIME", 538640896, 2, "IVehicle.SETTING_FUNC_DAYLIGHT_SAVING_TIME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIGITAL_KEY", 539496448, 2, "IVehicle.SETTING_FUNC_DIGITAL_KEY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIGITAL_KEY_REQ_STS", 539496704, 2, "IVehicle.SETTING_FUNC_DIGITAL_KEY_REQ_STS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIGITAL_KEY_SUSPENSION", 539497472, 2, "IVehicle.SETTING_FUNC_DIGITAL_KEY_SUSPENSION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIGITAL_KEY_TERMINATION", 539497216, 2, "IVehicle.SETTING_FUNC_DIGITAL_KEY_TERMINATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIGITAL_KEY_UNPAIR", 539496960, 2, "IVehicle.SETTING_FUNC_DIGITAL_KEY_UNPAIR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DIM_HOLIDAY_WALLPAPER", 538904320, 2, "IVehicle.SETTING_FUNC_DIM_HOLIDAY_WALLPAPER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVER_ALERT_CONTROL", 537002496, 2, "IVehicle.SETTING_FUNC_DRIVER_ALERT_CONTROL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_DRIVER_MODE_SOUND_SWITCH", 539429888, 2, "IVehicle.SETTING_FUNC_DRIVER_MODE_SOUND_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EMERGENCY_VEHICLE_ALARM", 539493120, 2, "IVehicle.SETTING_FUNC_EMERGENCY_VEHICLE_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EMERGENCY_VEHICLE_ALARM_MODE", 539498496, 2, "IVehicle.SETTING_FUNC_EMERGENCY_VEHICLE_ALARM_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENERGY_PREDICTION_SWITCH", 538903808, 2, "IVehicle.SETTING_FUNC_ENERGY_PREDICTION_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENGINE_MAINTENANCE_TIME_RESET", 539430656, 2, "IVehicle.SETTING_FUNC_ENGINE_MAINTENANCE_TIME_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ENTER_AUTO_SHOW_MODE_RE", 540280320, 2, "IVehicle.SETTING_FUNC_ENTER_AUTO_SHOW_MODE_RE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EXTERNAL_ARTIFICIAL_SOUND_SWITCH", 538577408, 2, "IVehicle.SETTING_FUNC_EXTERNAL_ARTIFICIAL_SOUND_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_EYE_BALL_TRACK", 539427584, 2, "IVehicle.SETTING_FUNC_EYE_BALL_TRACK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_E_PEDAL", 538444032, 2, "IVehicle.SETTING_FUNC_E_PEDAL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FACE_CAMERA_COVER", 540147968, 2, "IVehicle.SETTING_FUNC_FACE_CAMERA_COVER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FACE_RECOGNITION", 540279552, 2, "IVehicle.SETTING_FUNC_FACE_RECOGNITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_FRONT_WIPER_IDLE", 537658112, 2, "IVehicle.SETTING_FUNC_FRONT_WIPER_IDLE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HILL_START_ASSIST", 539429376, 2, "IVehicle.SETTING_FUNC_HILL_START_ASSIST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HOLOGRAPHIC_ACTIVATED", 539500032, 2, "IVehicle.SETTING_FUNC_HOLOGRAPHIC_ACTIVATED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HOLOGRAPHIC_BACKLIGHT_LEVEL", 539500544, 2, "IVehicle.SETTING_FUNC_HOLOGRAPHIC_BACKLIGHT_LEVEL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_HOLOGRAPHIC_BACKLIGHT_MODE", 539500288, 2, "IVehicle.SETTING_FUNC_HOLOGRAPHIC_BACKLIGHT_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_JOURNAL_LOGS", 538313472, 2, "IVehicle.SETTING_FUNC_JOURNAL_LOGS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LAUNCH_MODE_INDCN", 539429120, 2, "IVehicle.SETTING_FUNC_LAUNCH_MODE_INDCN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_LOCK_REAR_SEAT_DISPLAY", 538706176, 2, "IVehicle.SETTING_FUNC_LOCK_REAR_SEAT_DISPLAY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MAINTENANCE_TIME_RESET", 538968576, 2, "IVehicle.SETTING_FUNC_MAINTENANCE_TIME_RESET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MIRROR_DIMMING", 537460992, 2, "IVehicle.SETTING_FUNC_MIRROR_DIMMING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MIRROR_DIPPING_SWITCH", 537461760, 2, "IVehicle.SETTING_FUNC_MIRROR_DIPPING_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_FUNC_KNOB_DIRECTION", 540285952, 2, "IVehicle.SETTING_FUNC_MULTI_FUNC_KNOB_DIRECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_FUNC_KNOB_PRESS_STATUS", 540286464, 2, "IVehicle.SETTING_FUNC_MULTI_FUNC_KNOB_PRESS_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_MULTI_FUNC_KNOB_ROTATE_STEP", 540286208, 2, "IVehicle.SETTING_FUNC_MULTI_FUNC_KNOB_ROTATE_STEP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PARK_COMFORT_MODE_OFF_REASON", 538838272, 2, "IVehicle.SETTING_FUNC_PARK_COMFORT_MODE_OFF_REASON", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PASSENGER_AIRBAG", 539428096, 2, "IVehicle.SETTING_FUNC_PASSENGER_AIRBAG", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PCM_TIMER", 538640640, 2, "IVehicle.SETTING_FUNC_PCM_TIMER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_PRIVATE_LOCK_REMINDER", 537854464, 2, "IVehicle.SETTING_FUNC_PRIVATE_LOCK_REMINDER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_MIRR_STREAM_SWITCH", 539100416, 2, "IVehicle.SETTING_FUNC_REAR_MIRR_STREAM_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_SPOILER_ADJUST", 538510336, 2, "IVehicle.SETTING_FUNC_REAR_SPOILER_ADJUST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_SPOILER_POSN_REQUEST", 538510592, 2, "IVehicle.SETTING_FUNC_REAR_SPOILER_POSN_REQUEST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_REAR_WINDOW_CLEAN", 537395712, 2, "IVehicle.SETTING_FUNC_REAR_WINDOW_CLEAN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RED_LIGHT_ALARM", 539493888, 2, "IVehicle.SETTING_FUNC_RED_LIGHT_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RED_LIGHT_ALARM_MODE", 539498752, 2, "IVehicle.SETTING_FUNC_RED_LIGHT_ALARM_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RESET_SETTINGS_DEFAULT", 538181888, 2, "IVehicle.SETTING_FUNC_RESET_SETTINGS_DEFAULT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_RMS_ACTIVE", 538116352, 2, "IVehicle.SETTING_FUNC_RMS_ACTIVE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SAY_HI", 539427840, 2, "IVehicle.SETTING_FUNC_SAY_HI", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SCREEN_SAVER_CUSTOM_NAME", 539034624, 2, "IVehicle.SETTING_FUNC_SCREEN_SAVER_CUSTOM_NAME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SCREEN_SAVER_CUSTOM_PICTURE", 539035136, 2, "IVehicle.SETTING_FUNC_SCREEN_SAVER_CUSTOM_PICTURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SCREEN_SAVER_CUSTOM_TEXT", 539034880, 2, "IVehicle.SETTING_FUNC_SCREEN_SAVER_CUSTOM_TEXT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SCREEN_SAVER_STYLE", 539034368, 2, "IVehicle.SETTING_FUNC_SCREEN_SAVER_STYLE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_CONTROL", 537068032, 2, "IVehicle.SETTING_FUNC_SPEED_CONTROL", "", new Value[] {
                new Value("SPEED_CONTROL_MODE_ACC", 537069058),
                new Value("SPEED_CONTROL_MODE_CC", 537069057),
                new Value("SPEED_CONTROL_MODE_GPILOT", 537069059),
                new Value("SPEED_CONTROL_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_GUIDANCE_ALARM", 539493632, 2, "IVehicle.SETTING_FUNC_SPEED_GUIDANCE_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SPEED_LIMITATION", 537067776, 2, "IVehicle.SETTING_FUNC_SPEED_LIMITATION", "", new Value[] {
                new Value("SPEED_LIMITATION_MODE_ASL", 537068802),
                new Value("SPEED_LIMITATION_MODE_AVSL", 537068801),
                new Value("SPEED_LIMITATION_MODE_OFF", 0),
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_STEERING_WHEEL_ANGLE_WARN_SWITCH", 539430144, 2, "IVehicle.SETTING_FUNC_STEERING_WHEEL_ANGLE_WARN_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_SUNROOF_TRANSPARENCY_AUTO", 537396992, 2, "IVehicle.SETTING_FUNC_SUNROOF_TRANSPARENCY_AUTO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TCAM_5G_SWITCH", 538314496, 2, "IVehicle.SETTING_FUNC_TCAM_5G_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TEM_PROVISIONING_STATE", 538313984, 2, "IVehicle.SETTING_FUNC_TEM_PROVISIONING_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_TRACK_MODE", 538904576, 2, "IVehicle.SETTING_FUNC_TRACK_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_UNLOCK_P_GEAR", 539427072, 2, "IVehicle.SETTING_FUNC_UNLOCK_P_GEAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_SAFETY_ALARM", 539494912, 2, "IVehicle.SETTING_FUNC_VEHICLE_SAFETY_ALARM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VEHICLE_SAFETY_ALARM_MODE", 539497728, 2, "IVehicle.SETTING_FUNC_VEHICLE_SAFETY_ALARM_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_KEY_DISTANCE", 540283904, 2, "IVehicle.SETTING_FUNC_VOICE_KEY_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_POWER_MODE_SET", 540283648, 2, "IVehicle.SETTING_FUNC_VOICE_POWER_MODE_SET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_RECOGNITION", 538706688, 2, "IVehicle.SETTING_FUNC_VOICE_RECOGNITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOICE_SEARCH_KEY", 540283392, 2, "IVehicle.SETTING_FUNC_VOICE_SEARCH_KEY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOLUME_LIMIT", 539495168, 2, "IVehicle.SETTING_FUNC_VOLUME_LIMIT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VOLUME_LIMIT_MAX", 539495424, 2, "IVehicle.SETTING_FUNC_VOLUME_LIMIT_MAX", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_VSTD_VIDEO_UPLOAD_STATUS", 540285184, 2, "IVehicle.SETTING_FUNC_VSTD_VIDEO_UPLOAD_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WAITING_MODE", 539428352, 2, "IVehicle.SETTING_FUNC_WAITING_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WALLPAPER_SYNC", 539033856, 2, "IVehicle.SETTING_FUNC_WALLPAPER_SYNC", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WALLPAPER_SYNC_STYLE", 539034112, 2, "IVehicle.SETTING_FUNC_WALLPAPER_SYNC_STYLE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WELCOME_SOUND", 539099392, 2, "IVehicle.SETTING_FUNC_WELCOME_SOUND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WELCOME_SOUND_TYPE", 539099648, 2, "IVehicle.SETTING_FUNC_WELCOME_SOUND_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_WINDOW_PINCH_WARN", 537396480, 2, "IVehicle.SETTING_FUNC_WINDOW_PINCH_WARN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_XCALL_KEY_LOCK", 538313216, 2, "IVehicle.SETTING_FUNC_XCALL_KEY_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_KEY_SEARCH_STATUS_COMPLETE", 540283396, 2, "IVehicle.VOICE_KEY_SEARCH_STATUS_COMPLETE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_KEY_SEARCH_STATUS_FAILED", 540283395, 2, "IVehicle.VOICE_KEY_SEARCH_STATUS_FAILED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_KEY_SEARCH_STATUS_IDLE", 540283393, 2, "IVehicle.VOICE_KEY_SEARCH_STATUS_IDLE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("VOICE_KEY_SEARCH_STATUS_IN_PROGRESS", 540283394, 2, "IVehicle.VOICE_KEY_SEARCH_STATUS_IN_PROGRESS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AAC_LEVEL", 269747712, 2, "IHvac.HVAC_FUNC_AAC_LEVEL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_WIND_ELECDEFRS", 269753088, 2, "IHvac.HVAC_FUNC_WIND_ELECDEFRS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AI_CLIMATE_STATUS", 269092096, 2, "IHvac.HVAC_FUNC_AI_CLIMATE_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AQS_SWITCH", 268960256, 2, "IHvac.HVAC_FUNC_AQS_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_AAC_SWITCH", 269747456, 2, "IHvac.HVAC_FUNC_AUTO_AAC_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_BLOWING_MODE", 268896000, 2, "IHvac.HVAC_FUNC_AUTO_BLOWING_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND_CONFIRM", 269419264, 2, "IHvac.HVAC_FUNC_AUTO_CLOSE_WINDOW_REMIND_CONFIRM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEHUMIDIFICATION", 268960512, 2, "IHvac.HVAC_FUNC_AUTO_DEHUMIDIFICATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEHUMIDIFICATION_CONFIRM", 269287936, 2, "IHvac.HVAC_FUNC_AUTO_DEHUMIDIFICATION_CONFIRM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_DEHUMIDIFICATION_REQUEST", 269287680, 2, "IHvac.HVAC_FUNC_AUTO_DEHUMIDIFICATION_REQUEST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_MASSAGE", 268765184, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_MASSAGE", "", new Value[] {
                new Value("AUTO_SEAT_MASSAGE_LEVEL_1", 268765185),
                new Value("AUTO_SEAT_MASSAGE_LEVEL_2", 268765186),
                new Value("AUTO_SEAT_MASSAGE_LEVEL_3", 268765187),
                new Value("AUTO_SEAT_MASSAGE_OFF", 0),
                new Value("AUTO_SEAT_MASSAGE_TIME_1", 268765441),
                new Value("AUTO_SEAT_MASSAGE_TIME_2", 268765442),
                new Value("AUTO_SEAT_MASSAGE_TIME_3", 268765443),
                new Value("AUTO_SEAT_MASSAGE_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SEAT_VENTILATION", 268763904, 2, "IHvac.HVAC_FUNC_AUTO_SEAT_VENTILATION", "", new Value[] {
                new Value("AUTO_SEAT_VENTILATION_TIME_1", 268764161),
                new Value("AUTO_SEAT_VENTILATION_TIME_2", 268764162),
                new Value("AUTO_SEAT_VENTILATION_TIME_3", 268764163),
                new Value("AUTO_SEAT_VENTILATION_TIME_4", 268764164),
                new Value("AUTO_SEAT_VENTILATION_TIME_OFF", 0),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_AUTO_SECOND_ROW_CLIMATE", 269484288, 2, "IHvac.HVAC_FUNC_AUTO_SECOND_ROW_CLIMATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_BLOWING_TEMP_COLOR", 268895744, 2, "IHvac.HVAC_FUNC_BLOWING_TEMP_COLOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLIMATE_IS_SHOW", 269748736, 2, "IHvac.HVAC_FUNC_CLIMATE_IS_SHOW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLIMATISATION_ERROR_CONDITIONS", 269091584, 2, "IHvac.HVAC_FUNC_CLIMATISATION_ERROR_CONDITIONS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLOSE_AUTO_CONTROL_CONFIRM", 269749760, 2, "IHvac.HVAC_FUNC_CLOSE_AUTO_CONTROL_CONFIRM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CLOSE_AUTO_CONTROL_REQUEST", 269749504, 2, "IHvac.HVAC_FUNC_CLOSE_AUTO_CONTROL_REQUEST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CO2_HIGHER_REQUEST", 269353472, 2, "IHvac.HVAC_FUNC_CO2_HIGHER_REQUEST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_CO2_SWITCH", 269353216, 2, "IHvac.HVAC_FUNC_CO2_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HARDKEYPOP_AUTOOFF", 269754368, 2, "IHvac.HVAC_FUNC_HARDKEYPOP_AUTOOFF", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HARDKEYPOP_AUTOON", 269754112, 2, "IHvac.HVAC_FUNC_HARDKEYPOP_AUTOON", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HARDKEYPOP_FRONTDEFROSTOFF", 269754880, 2, "IHvac.HVAC_FUNC_HARDKEYPOP_FRONTDEFROSTOFF", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HARDKEYPOP_FRONTDEFROSTON", 269754624, 2, "IHvac.HVAC_FUNC_HARDKEYPOP_FRONTDEFROSTON", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_REWIN_ELECDEFRST", 269753344, 2, "IHvac.HVAC_FUNC_REWIN_ELECDEFRST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HARDKEYPOP_POWERON_AUTOOFF", 269753856, 2, "IHvac.HVAC_FUNC_HARDKEYPOP_POWERON_AUTOOFF", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_SHOW_STEERWHL_A", 269753600, 2, "IHvac.HVAC_SHOW_STEERWHL_A", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_HIDE_CLIMATE_APP", 269748480, 2, "IHvac.HVAC_FUNC_HIDE_CLIMATE_APP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_INTELLIGENT_AIR_POP", 269752320, 2, "IHvac.HVAC_FUNC_INTELLIGENT_AIR_POP", "", new Value[] {
                new Value("INTELLIGENT_AIR_NO_POP", 269752321),
                new Value("INTELLIGENT_AIR_POP_1", 269752322),
                new Value("INTELLIGENT_AIR_POP_2", 269752323),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_INTELLIGENT_AIR_POP_SELECT", 269752832, 2, "IHvac.HVAC_FUNC_INTELLIGENT_AIR_POP_SELECT", "", new Value[] {
                new Value("INTELLIGENT_AIR_NO_POP", 269752321),
                new Value("INTELLIGENT_AIR_POP_1", 269752322),
                new Value("INTELLIGENT_AIR_POP_2", 269752323),
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_INTELLIGENT_AIR_SWITCH", 269752576, 2, "IHvac.HVAC_FUNC_INTELLIGENT_AIR_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_MODULE_CONNECT_STATUS", 269680896, 2, "IHvac.HVAC_FUNC_MODULE_CONNECT_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_PET_WINDOW_REMIND_REQUEST", 269747968, 2, "IHvac.HVAC_FUNC_PET_WINDOW_REMIND_REQUEST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_RAPID_COOLING", 269750016, 2, "IHvac.HVAC_FUNC_RAPID_COOLING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_RAPID_WARMING", 269750528, 2, "IHvac.HVAC_FUNC_RAPID_WARMING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SUNROOF_POPUP", 269747200, 2, "IHvac.HVAC_FUNC_SUNROOF_POPUP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SWEEPING_HORIZONTAL_POS", 268895232, 2, "IHvac.HVAC_FUNC_SWEEPING_HORIZONTAL_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("HVAC_FUNC_SWEEPING_VERTICAL_POS", 268895488, 2, "IHvac.HVAC_FUNC_SWEEPING_VERTICAL_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DIM_THEME_SET", 570688000, 2, "IDriveMode.DM_FUNC_DIM_THEME_SET", "", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_DIM_THEME_SYNC_DRIVEMODE", 570687744, 2, "IDriveMode.DM_FUNC_DIM_THEME_SYNC_DRIVEMODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("DM_FUNC_ECO_BUTTON", 570556672, 2, "IDriveMode.DM_FUNC_ECO_BUTTON", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SETTING_FUNC_ESC_SWITCH_LEVEL", 570690816, 2, "IDriveMode.SETTING_FUNC_ESC_SWITCH_LEVEL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_APA_DETECT_PARKING_SPACE", 588251904, 2, "IPAS.PAS_FUNC_APA_DETECT_PARKING_SPACE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_APA_RPA_SWITCH", 587596288, 2, "IPAS.PAS_FUNC_APA_RPA_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_AVP_ACTIVATED", 588251392, 2, "IPAS.PAS_FUNC_AVP_ACTIVATED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_ELE_MIRROR_SYS_ACTIVATED", 588251648, 2, "IPAS.PAS_FUNC_ELE_MIRROR_SYS_ACTIVATED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_3DVIEW_POSITION", 587403776, 2, "IPAS.PAS_FUNC_PAC_3DVIEW_POSITION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_APP_INIT_COMPLETED", 587404544, 2, "IPAS.PAS_FUNC_PAC_APP_INIT_COMPLETED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_AUTO_FRONT_ACTIV", 587399936, 2, "IPAS.PAS_FUNC_PAC_AUTO_FRONT_ACTIV", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_AUTO_REVERSE_CAMERA", 587400192, 2, "IPAS.PAS_FUNC_PAC_AUTO_REVERSE_CAMERA", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_CAMERA_TYPE", 587400448, 2, "IPAS.PAS_FUNC_PAC_CAMERA_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_NEARBY_OBJ_TRIGGER", 587407872, 2, "IPAS.PAS_FUNC_PAC_NEARBY_OBJ_TRIGGER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_OBSTACLE_DETECTION", 587408128, 2, "IPAS.PAS_FUNC_PAC_OBSTACLE_DETECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_OVERLAY_DSTINFO", 587401728, 2, "IPAS.PAS_FUNC_PAC_OVERLAY_DSTINFO", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_OVERLAY_STEERPATH", 587401216, 2, "IPAS.PAS_FUNC_PAC_OVERLAY_STEERPATH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_OVERLAY_TOWBAR", 587401472, 2, "IPAS.PAS_FUNC_PAC_OVERLAY_TOWBAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_STATUS", 587399425, 2, "IPAS.PAS_FUNC_PAC_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_SYS_AVA_STATUS", 587404032, 2, "IPAS.PAS_FUNC_PAC_SYS_AVA_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_TOP_VIEW_ZOOM_IN", 587408384, 2, "IPAS.PAS_FUNC_PAC_TOP_VIEW_ZOOM_IN", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_TOURING_VIEW", 587408640, 2, "IPAS.PAS_FUNC_PAC_TOURING_VIEW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAC_VIEW_SELECTION", 587403520, 2, "IPAS.PAS_FUNC_PAC_VIEW_SELECTION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_CENTER", 587338240, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_CENTER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_INNER_LEFT", 587333888, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_INNER_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_INNER_RIGHT", 587334144, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_INNER_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_LEFT_SIDE", 587334912, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_LEFT_SIDE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_OUT_LEFT", 587334400, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_OUT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_OUT_RIGHT", 587334656, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_OUT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_FRONT_RIGHT_SIDE", 587335168, 2, "IPAS.PAS_FUNC_PAS_RADAR_FRONT_RIGHT_SIDE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_MAX_DISTANCE", 587336960, 2, "IPAS.PAS_FUNC_PAS_RADAR_MAX_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_MIN_DISTANCE", 587337216, 2, "IPAS.PAS_FUNC_PAS_RADAR_MIN_DISTANCE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_CENTER", 587338496, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_CENTER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_INNER_LEFT", 587336448, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_INNER_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_INNER_RIGHT", 587336704, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_INNER_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_LEFT_SIDE", 587335424, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_LEFT_SIDE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_OUT_LEFT", 587335936, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_OUT_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_OUT_RIGHT", 587336192, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_OUT_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_REAR_RIGHT_SIDE", 587335680, 2, "IPAS.PAS_FUNC_PAS_RADAR_REAR_RIGHT_SIDE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_WORK_MODE", 587337728, 2, "IPAS.PAS_FUNC_PAS_RADAR_WORK_MODE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_RADAR_WORK_STATUS", 587337984, 2, "IPAS.PAS_FUNC_PAS_RADAR_WORK_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_SHOW_GRAPHICS", 587269376, 2, "IPAS.PAS_FUNC_PAS_SHOW_GRAPHICS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_STATUS", 587268352, 2, "IPAS.PAS_FUNC_PAS_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_PAS_TOP_VIEW", 587269120, 2, "IPAS.PAS_FUNC_PAS_TOP_VIEW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_RCTA_ACTIVATION", 587530496, 2, "IPAS.PAS_FUNC_RCTA_ACTIVATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_RCTA_LEFT_WARNING", 587530752, 2, "IPAS.PAS_FUNC_RCTA_LEFT_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_RCTA_RIGHT_WARNING", 587531008, 2, "IPAS.PAS_FUNC_RCTA_RIGHT_WARNING", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_RCTA_SHOW_GRAPHICS", 587531264, 2, "IPAS.PAS_FUNC_RCTA_SHOW_GRAPHICS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_IN_NOTI", 587469056, 2, "IPAS.PAS_FUNC_SAP_PARK_IN_NOTI", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_IN_RESUME", 587465728, 2, "IPAS.PAS_FUNC_SAP_PARK_IN_RESUME", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_IN_TYPE", 587465472, 2, "IPAS.PAS_FUNC_SAP_PARK_IN_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_IN_TYPE_RECOMMEND", 587466496, 2, "IPAS.PAS_FUNC_SAP_PARK_IN_TYPE_RECOMMEND", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_OUT_COMFIRM", 587465984, 2, "IPAS.PAS_FUNC_SAP_PARK_OUT_COMFIRM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_OUT_NOTI", 587469312, 2, "IPAS.PAS_FUNC_SAP_PARK_OUT_NOTI", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PARK_TYPE", 587465216, 2, "IPAS.PAS_FUNC_SAP_PARK_TYPE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("PAS_FUNC_SAP_PROGRESS", 587466240, 2, "IPAS.PAS_FUNC_SAP_PROGRESS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_ALL_DOORS_ONE_KEY_SWITCH", 554763520, 2, "IBcm.BCM_FUNC_ALL_DOORS_ONE_KEY_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_AUTO_CLOSE_DOOR_BY_SPEED_SWITCH", 554763264, 2, "IBcm.BCM_FUNC_AUTO_CLOSE_DOOR_BY_SPEED_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_CSD_CONTROL_CUTOFF_LOCK", 553845249, 2, "IBcm.BCM_FUNC_CSD_CONTROL_CUTOFF_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_ANTI_PINCH", 553785600, 2, "IBcm.BCM_FUNC_DOOR_ANTI_PINCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_CONTROL", 553783296, 2, "IBcm.BCM_FUNC_DOOR_CONTROL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_OBSTACLE_DETECTED", 553785344, 2, "IBcm.BCM_FUNC_DOOR_OBSTACLE_DETECTED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_DOOR_POS", 553779968, 2, "IBcm.BCM_FUNC_DOOR_POS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_FOLD_REAR_MIRROR_DEFROST", 554763776, 2, "IBcm.BCM_FUNC_FOLD_REAR_MIRROR_DEFROST", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_ALL_WEATHER_LIGHT", 553981440, 2, "IBcm.BCM_FUNC_LIGHT_ALL_WEATHER_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_CORNERING_LAMPS", 553977344, 2, "IBcm.BCM_FUNC_LIGHT_CORNERING_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_DAYTIME_RUNNING_LAMPS", 553978112, 2, "IBcm.BCM_FUNC_LIGHT_DAYTIME_RUNNING_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_DIM_DIP_LAMPS", 553978368, 2, "IBcm.BCM_FUNC_LIGHT_DIM_DIP_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_DIPPED_BEAM", 553976064, 2, "IBcm.BCM_FUNC_LIGHT_DIPPED_BEAM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_DRIVING_LAMPS", 553976576, 2, "IBcm.BCM_FUNC_LIGHT_DRIVING_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_FRONT_POSITION_LAMPS", 553977856, 2, "IBcm.BCM_FUNC_LIGHT_FRONT_POSITION_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_GRILLE_LAMP", 553981184, 2, "IBcm.BCM_FUNC_LIGHT_GRILLE_LAMP", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_MAIN_BEAM", 553976320, 2, "IBcm.BCM_FUNC_LIGHT_MAIN_BEAM", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_NUMBER_PLATE_LIGHT", 553981696, 2, "IBcm.BCM_FUNC_LIGHT_NUMBER_PLATE_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_REAR_LOGO_LIGHT", 553980928, 2, "IBcm.BCM_FUNC_LIGHT_REAR_LOGO_LIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_REAR_POSITION_LAMPS", 553978880, 2, "IBcm.BCM_FUNC_LIGHT_REAR_POSITION_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_REVERSING_LAMPS", 553979392, 2, "IBcm.BCM_FUNC_LIGHT_REVERSING_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_SIDE_MARKER_LIGHTS", 553978624, 2, "IBcm.BCM_FUNC_LIGHT_SIDE_MARKER_LIGHTS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_SPOT_LIGHTS", 553977600, 2, "IBcm.BCM_FUNC_LIGHT_SPOT_LIGHTS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_LIGHT_STOP_LAMPS", 553979136, 2, "IBcm.BCM_FUNC_LIGHT_STOP_LAMPS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_SCREEN_SAVER_POWER_KEY_PRESS", 555747072, 2, "IBcm.BCM_FUNC_SCREEN_SAVER_POWER_KEY_PRESS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_LOCK", 553845248, 2, "IBcm.BCM_FUNC_WINDOW_LOCK", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_SYNC_SWITCH", 553846017, 2, "IBcm.BCM_FUNC_WINDOW_SYNC_SWITCH", "", new Value[] {
        }));
        put(byId, byKey, new Entry("BCM_FUNC_WINDOW_TRANSPARENCY", 553846016, 2, "IBcm.BCM_FUNC_WINDOW_TRANSPARENCY", "", new Value[] {
        }));
        put(byId, byKey, new Entry("FUNC_UNIT_WARN_SPEED", 620888832, 2, "IUnits.FUNC_UNIT_WARN_SPEED", "", new Value[] {
                new Value("UNIT_WARN_SPEED_KM_H", 620888833),
                new Value("UNIT_WARN_SPEED_MPH", 620888834),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ABS", 2101504, 3, "ISensor.SENSOR_TYPE_ABS", "", new Value[] {
                new Value("ABS_WARNING_STATE_FLSG", 1058306),
                new Value("ABS_WARNING_STATE_OFF", 1058308),
                new Value("ABS_WARNING_STATE_ON", 1058305),
                new Value("ABS_WARNING_STATE_RESD", 1058307),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AIRBAG_STATUS_DRIVER", 2109696, 3, "ISensor.SENSOR_TYPE_AIRBAG_STATUS_DRIVER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AIRBAG_STATUS_PASSENGER", 2109952, 3, "ISensor.SENSOR_TYPE_AIRBAG_STATUS_PASSENGER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_AMBIENT", 1049600, 3, "ISensor.SENSOR_TYPE_AQI_AMBIENT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_BACK_ROW", 1049872, 3, "ISensor.SENSOR_TYPE_AQI_BACK_ROW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_INDOOR", 1049856, 3, "ISensor.SENSOR_TYPE_AQI_INDOOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_LEVEL_BACK_ROW", 2106384, 3, "ISensor.SENSOR_TYPE_AQI_LEVEL_BACK_ROW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_AQI_LEVEL_INDOOR", 2106368, 3, "ISensor.SENSOR_TYPE_AQI_LEVEL_INDOOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_BATTERY_CURRENT", 1051168, 3, "ISensor.SENSOR_TYPE_BATTERY_CURRENT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_BRAKE_PRESSURE", 1053456, 3, "ISensor.SENSOR_TYPE_BRAKE_PRESSURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CAR_SPEED_ACCELERATION", 1054464, 3, "ISensor.SENSOR_TYPE_CAR_SPEED_ACCELERATION", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CO2_INDOOR", 1051904, 3, "ISensor.SENSOR_TYPE_CO2_INDOOR", "Уровень CO₂ в салоне.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_CO2_LEVEL_INDOOR", 2106624, 3, "ISensor.SENSOR_TYPE_CO2_LEVEL_INDOOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ENGINE_START_STOP_STATE", 2103040, 3, "ISensor.SENSOR_TYPE_ENGINE_START_STOP_STATE", "", new Value[] {
                new Value("ENGINE_START_STOP_STATE_AUTO_STOPPING", 2103047),
                new Value("ENGINE_START_STOP_STATE_ENGINE_RESTART", 2103045),
                new Value("ENGINE_START_STOP_STATE_OPERATION", 2103046),
                new Value("ENGINE_START_STOP_STATE_STANDBY", 2103042),
                new Value("ENGINE_START_STOP_STATE_STARTER_RESTART", 2103044),
                new Value("ENGINE_START_STOP_STATE_STOPPED", 2103043),
                new Value("ENGINE_START_STOP_STATE_UNKNOWN", -1),
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_EV_BATTERY_STATE", 2102528, 3, "ISensor.SENSOR_TYPE_EV_BATTERY_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_EV_BATTERY_TEMPERATURE", 1051152, 3, "ISensor.SENSOR_TYPE_EV_BATTERY_TEMPERATURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_EYE_BALL_TRACK_STATE", 3148800, 3, "ISensor.SENSOR_TYPE_EYE_BALL_TRACK_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_HANDBRAKE_STATE", 2097920, 3, "ISensor.SENSOR_TYPE_HANDBRAKE_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_HVSYSRLY_STS", 3150080, 3, "ISensor.SENSOR_TYPE_HVSYSRLY_STS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_LANE_DEPARTURE", 3149056, 3, "ISensor.SENSOR_TYPE_LANE_DEPARTURE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_MOTO_SPEED_FRONT", 1057024, 3, "ISensor.SENSOR_TYPE_MOTO_SPEED_FRONT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_MOTO_SPEED_REAR", 1057280, 3, "ISensor.SENSOR_TYPE_MOTO_SPEED_REAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_MOTO_TORQUE_FRONT", 1056512, 3, "ISensor.SENSOR_TYPE_MOTO_TORQUE_FRONT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_MOTO_TORQUE_REAR", 1056768, 3, "ISensor.SENSOR_TYPE_MOTO_TORQUE_REAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_AMBIENT", 1049088, 3, "ISensor.SENSOR_TYPE_PM25_AMBIENT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_BACK_ROW", 1049360, 3, "ISensor.SENSOR_TYPE_PM25_BACK_ROW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_INDOOR", 1049344, 3, "ISensor.SENSOR_TYPE_PM25_INDOOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_LEVEL_AMBIENT", 2105600, 3, "ISensor.SENSOR_TYPE_PM25_LEVEL_AMBIENT", "Уровень PM2.5 снаружи.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_LEVEL_BACK_ROW", 2105872, 3, "ISensor.SENSOR_TYPE_PM25_LEVEL_BACK_ROW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_STATE_AMBIENT", 2106880, 3, "ISensor.SENSOR_TYPE_PM25_STATE_AMBIENT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_STATE_BACK_ROW", 2107152, 3, "ISensor.SENSOR_TYPE_PM25_STATE_BACK_ROW", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_PM25_STATE_INDOOR", 2107136, 3, "ISensor.SENSOR_TYPE_PM25_STATE_INDOOR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_RAIN_SENSOR_STATE", 3149568, 3, "ISensor.SENSOR_TYPE_RAIN_SENSOR_STATE", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_REAR_WHEEL_ANGEL", 1057536, 3, "ISensor.SENSOR_TYPE_REAR_WHEEL_ANGEL", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ROLLBAR_POSITION_FRONT", 1055488, 3, "ISensor.SENSOR_TYPE_ROLLBAR_POSITION_FRONT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ROLLBAR_POSITION_REAR", 1055744, 3, "ISensor.SENSOR_TYPE_ROLLBAR_POSITION_REAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ROLLBAR_TORQUE_FRONT", 1056000, 3, "ISensor.SENSOR_TYPE_ROLLBAR_TORQUE_FRONT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_ROLLBAR_TORQUE_REAR", 1056256, 3, "ISensor.SENSOR_TYPE_ROLLBAR_TORQUE_REAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_RPM", 1050880, 3, "ISensor.SENSOR_TYPE_RPM", "Обороты двигателя (RPM), об/мин.", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_DRIVER_ORIGIN_STATUS", 2167296, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_DRIVER_ORIGIN_STATUS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_PASSENGER", 2102016, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_PASSENGER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_ROW2_CENTER", 2103808, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_ROW2_CENTER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_ROW2_LEFT", 2103296, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_ROW2_LEFT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SAFE_BELT_ROW2_RIGHT", 2103552, 3, "ISensor.SENSOR_TYPE_SAFE_BELT_ROW2_RIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_PRESSURE_DRIVER", 1053952, 3, "ISensor.SENSOR_TYPE_SEAT_PRESSURE_DRIVER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_SEAT_PRESSURE_PASSENGER", 1054208, 3, "ISensor.SENSOR_TYPE_SEAT_PRESSURE_PASSENGER", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_STATUS_TWLIBRISTS", 2101008, 3, "ISensor.SENSOR_TYPE_STATUS_TWLIBRISTS", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_STEERING_WHEEL_ANGLE_SPEED", 1052928, 3, "ISensor.SENSOR_TYPE_STEERING_WHEEL_ANGLE_SPEED", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_TOO_CLOSE_TO_FRONT_CAR", 3149312, 3, "ISensor.SENSOR_TYPE_TOO_CLOSE_TO_FRONT_CAR", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_VEHICLE_WEIGHT", 1053184, 3, "ISensor.SENSOR_TYPE_VEHICLE_WEIGHT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_STEERING_ASSISTANCE_FAULT", 3147520, 3, "ISensor.SENSOR_TYPE_WARN_STEERING_ASSISTANCE_FAULT", "", new Value[] {
        }));
        put(byId, byKey, new Entry("SENSOR_TYPE_WARN_TRANSMISSION_SYSTEM_FAULT", 3147264, 3, "ISensor.SENSOR_TYPE_WARN_TRANSMISSION_SYSTEM_FAULT", "", new Value[] {
        }));

        BY_ID = Collections.unmodifiableMap(byId);
        BY_KEY = Collections.unmodifiableMap(byKey);
    }

    private CarFunctionCatalog() {}

    static Entry byId(int id) {
        return BY_ID.get(id);
    }

    static Entry byKey(String key) {
        return BY_KEY.get(key);
    }

    static boolean isFunction(int id) {
        Entry entry = byId(id);
        return entry != null && entry.type == TYPE_FUNCTION;
    }

    static boolean isSensor(int id) {
        Entry entry = byId(id);
        return entry != null && entry.type == TYPE_SENSOR;
    }

    static boolean isInfo(int id) {
        Entry entry = byId(id);
        return entry != null && entry.type == TYPE_INFO;
    }

    static Value[] staticValues(int id) {
        Entry entry = byId(id);
        return entry == null ? new Value[0] : entry.values;
    }

    private static void put(LinkedHashMap<Integer, Entry> byId, LinkedHashMap<String, Entry> byKey, Entry entry) {
        byId.put(entry.id, entry);
        if (!byKey.containsKey(entry.key)) byKey.put(entry.key, entry);
    }

    static final class Entry {
        final String key;
        final int id;
        final int type;
        final String alias;
        final String description;
        final Value[] values;

        Entry(String key, int id, int type, String alias, String description, Value[] values) {
            this.key = key;
            this.id = id;
            this.type = type;
            this.alias = alias;
            this.description = description;
            this.values = values == null ? new Value[0] : values;
        }

        boolean hasStaticValues() {
            return values.length > 0;
        }
    }

    static final class Value {
        final String key;
        final int value;

        Value(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
