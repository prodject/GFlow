package com.prodject.gflow;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Locale;
import android.util.Log;

final class EcarxVehicleAdapter {
    private static final String TAG = "GFlowCarApi";
    private static final Spec SPEC_DEFAULT = new Spec(0, Backend.ADAPT_API, true, false, false);
    
    static final int COMMON_OFF = 0x0;
    static final int COMMON_ON = 0x1;
    static final int ZONE_ALL = 0x80000000;
    static final int ZONE_DRIVER_LEFT = 0x1;
    static final int ZONE_ROW_1_CENTER = 0x2;
    static final int ZONE_PASSENGER_RIGHT = 0x4;
    static final int ZONE_ROW_1_ALL = 0x8;
    static final int ZONE_ROW_2_LEFT = 0x10;
    static final int ZONE_ROW_2_CENTER = 0x20;
    static final int ZONE_ROW_2_RIGHT = 0x40;
    static final int ZONE_ROW_2_ALL = 0x80;
    static final int ZONE_ROW_3_LEFT = 0x100;
    static final int ZONE_ROW_3_CENTER = 0x200;
    static final int ZONE_ROW_3_RIGHT = 0x400;
    static final int ZONE_ROW_3_ALL = 0x800;
    static final int BCM_DOOR_ROW_1_LEFT = 0x1;
    static final int BCM_DOOR_ROW_1_RIGHT = 0x4;
    static final int BCM_DOOR_ROW_2_LEFT = 0x10;
    static final int BCM_DOOR_ROW_2_RIGHT = 0x40;
    static final int BCM_DOOR_HOOD = 0x10000000;
    static final int BCM_DOOR_REAR = 0x20000000;
    static final int BCM_WINDOW_ROW_1_LEFT = 0x10;
    static final int BCM_WINDOW_ROW_1_RIGHT = 0x20;
    static final int BCM_WINDOW_ROW_1_ALL = 0x30;
    static final int BCM_WINDOW_ROW_2_LEFT = 0x100;
    static final int BCM_WINDOW_ROW_2_RIGHT = 0x200;
    static final int BCM_WINDOW_ROW_2_ALL = 0x300;
    static final int BCM_WINDOW_ALL = 0x330;

    static final int HVAC_POWER = 0x10010100;
    static final int HVAC_AUTO = 0x10010200;
    static final int HVAC_AC = 0x10010300;
    static final int HVAC_AC_MAX = 0x10010400;
    static final int HVAC_FAN_SPEED = 0x10020100;
    static final int HVAC_CIRCULATION = 0x10030100;
    static final int HVAC_BLOWING_MODE = 0x10070100;
    static final int HVAC_AUTO_BLOWING_MODE = 0x10070700;
    static final int HVAC_DEFROST_FRONT = 0x10040100;
    static final int HVAC_DEFROST_FRONT_MAX = 0x10040200;
    static final int HVAC_DEFROST_REAR = 0x10040300;
    static final int HVAC_CLIMATE_ZONE = 0x10010500;
    static final int HVAC_AUTO_FAN_SETTING = 0x10020200;
    static final int HVAC_CIRCULATION_TIMER = 0x10030200;
    static final int HVAC_CIRCULATION_LONG_TOUCH = 0x10030300;
    static final int HVAC_AUTO_DEFROST_REAR = 0x10040400;
    static final int HVAC_AUTO_DEFROST_FRONT = 0x10040500;
    static final int HVAC_AUTO_DEFROST_REQUEST = 0x10040600;
    static final int HVAC_AUTO_DEFROST_CONFIRM = 0x10040700;
    static final int HVAC_TEMP = 0x10060100;
    static final int HVAC_TEMP_MAX = 0x10060200;
    static final int HVAC_TEMP_MIN = 0x10060300;
    static final int HVAC_TEMP_STEP = 0x10060400;
    static final int HVAC_TEMP_DUAL = 0x10060500;
    static final int HVAC_TEMP_UNIT = 0x10060600;
    static final int HVAC_TEMP_HARD_KEY = 0x10060700;
    static final int HVAC_SWEEPING_MODE = 0x10070200;
    static final int HVAC_DIRECTION_MODE = 0x10070300;
    static final int HVAC_SWEEPING_HORIZONTAL_POS = 0x10070400;
    static final int HVAC_SWEEPING_VERTICAL_POS = 0x10070500;
    static final int HVAC_BLOWING_TEMP_COLOR = 0x10070600;
    static final int HVAC_SEAT_VENTILATION = 0x10050100;
    static final int HVAC_SEAT_HEATING = 0x10050200;
    static final int HVAC_AUTO_SEAT_VENTILATION = 0x10050300;
    static final int HVAC_AUTO_SEAT_VENTILATION_TIME = 0x10050400;
    static final int HVAC_AUTO_SEAT_HEATING = 0x10050500;
    static final int HVAC_AUTO_SEAT_HEATING_TIME = 0x10050600;
    static final int HVAC_SEAT_MASSAGE = 0x10050700;
    static final int HVAC_AUTO_SEAT_MASSAGE = 0x10050800;
    static final int HVAC_AUTO_SEAT_MASSAGE_TIME = 0x10050900;
    static final int HVAC_ECO = 0x10080100;
    static final int HVAC_AQS_SWITCH = 0x10080200;
    static final int HVAC_AUTO_DEHUMIDIFICATION = 0x10080300;
    static final int HVAC_OVERHEAT_PROTECTION = 0x10080400;
    static final int HVAC_IONS_SWITCH = 0x10080500;
    static final int HVAC_STEERING_WHEEL_HEAT = 0x10090100;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT = 0x10090200;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT_TIME = 0x10090300;
    static final int HVAC_AUTO_STEERING_WHEEL_HEAT_SWITCH = 0x10090400;
    static final int HVAC_PRE_CLIMATISATION = 0x100a0100;
    static final int HVAC_POST_CLIMATISATION = 0x100a0200;
    static final int HVAC_AI_POWER = 0x100a0400;
    static final int HVAC_AIR_FRAGRANCE = 0x100b0100;
    static final int HVAC_AIR_FRAGRANCE_TYPE = 0x100b0200;
    static final int HVAC_AIR_FRAGRANCE_LEVEL = 0x100b0300;
    static final int HVAC_AIR_FRAGRANCE_RATIO = 0x100b0700;
    static final int HVAC_AUTO_ION_REQUEST = 0x100c0200;
    static final int HVAC_AUTO_ION_CONFIRM = 0x100c0300;
    static final int HVAC_AUTO_DEHUMIDIFICATION_REQUEST = 0x100d0100;
    static final int HVAC_AUTO_DEHUMIDIFICATION_CONFIRM = 0x100d0200;
    static final int HVAC_CO2_SWITCH = 0x100e0100;
    static final int HVAC_AUTO_CLOSE_WINDOW_REMIND = 0x100f0100;
    static final int HVAC_AUTO_SECOND_ROW_CLIMATE = 0x10100100;
    static final int HVAC_CLIMATE_LOCK = 0x10100200;
    static final int HVAC_DISPLAY_WINDOW_TAB = 0x10100300;
    static final int HVAC_G_CLEAN = 0x10100400;
    static final int HVAC_AUTOMATIC_VENTILATION_DRY = 0x10100500;
    static final int HVAC_AUTO_CZIS = 0x10100600;
    static final int HVAC_VENTILATION_ONTIME = 0x10100700;
    static final int HVAC_CLIMATE_HARDKEY_SOUND = 0x10100800;
    static final int HVAC_INTELLIGENT_RECOMMENDATION = 0x10120100;
    static final int HVAC_TEMP_OPTIMIZE = 0x10120200;
    static final int HVAC_MODULE_CONNECT_STATUS = 0x10130100;
    static final int HVAC_ELECTRICAL_AIR_VENT = 0x10140100;
    static final int HVAC_HARDKEY = 0x10140200;
    static final int HVAC_FILTER_ELEMENT_LIFE = 0x10140300;
    static final int HVAC_RESET_FILTER_ELEMENT_LIFE = 0x10141000;
    static final int HVAC_RAPID_COOLING = 0x10140f00;
    static final int HVAC_RAPID_WARMING = 0x10141100;
    static final int HVAC_IONIZER_CLS_WIN_POPUP_SETTING = 0x10141400;
    static final int HVAC_IONIZER_CLS_WIN_POPUP = 0x10141500;
    static final int HVAC_AQS_STATUS = 0x10141600;
    static final int HVAC_FAN_SPEED_BLOWER = 0x10141700;
    static final int HVAC_SEAT_LEVEL_OFF = 0x0;
    static final int HVAC_SEAT_HEATING_LEVEL_1 = 0x10050201;
    static final int HVAC_SEAT_HEATING_LEVEL_2 = 0x10050202;
    static final int HVAC_SEAT_HEATING_LEVEL_3 = 0x10050203;
    static final int HVAC_SEAT_HEATING_AUTO = 0x1005020f;
    static final int HVAC_SEAT_VENTILATION_LEVEL_1 = 0x10050101;
    static final int HVAC_SEAT_VENTILATION_LEVEL_2 = 0x10050102;
    static final int HVAC_SEAT_VENTILATION_LEVEL_3 = 0x10050103;
    static final int HVAC_SEAT_VENTILATION_AUTO = 0x1005010f;

    static final int FAN_SPEED_1 = 0x10020101;
    static final int FAN_SPEED_2 = 0x10020102;
    static final int FAN_SPEED_3 = 0x10020103;
    static final int FAN_SPEED_4 = 0x10020104;
    static final int FAN_SPEED_5 = 0x10020105;
    static final int FAN_SPEED_6 = 0x10020106;
    static final int FAN_SPEED_7 = 0x10020107;
    static final int FAN_SPEED_8 = 0x10020108;
    static final int FAN_SPEED_9 = 0x10020109;
    static final int FAN_SPEED_AUTO = 0x1002010a;
    static final int CIRCULATION_INNER = 0x10030101;
    static final int CIRCULATION_OUTSIDE = 0x10030102;
    static final int CIRCULATION_AUTO = 0x10030103;
    static final int BLOWING_MODE_FACE = 0x10070101;
    static final int BLOWING_MODE_LEG = 0x10070102;
    static final int BLOWING_MODE_FACE_AND_LEG = 0x10070103;
    static final int BLOWING_MODE_FRONT_WINDOW = 0x10070104;
    static final int BLOWING_MODE_FACE_AND_FRONT_WINDOW = 0x10070105;
    static final int BLOWING_MODE_LEG_AND_FRONT_WINDOW = 0x10070106;
    static final int BLOWING_MODE_ALL = 0x10070107;
    static final int BLOWING_MODE_AUTO = 0x10070108;
    static final int CLIMATE_ZONE_SINGLE = 0x10010501;
    static final int CLIMATE_ZONE_DUAL = 0x10010502;
    static final int CLIMATE_ZONE_TRIPLE = 0x10010503;
    static final int CLIMATE_ZONE_FOUR = 0x10010504;
    static final int TEMP_UNIT_C = 0x10060601;
    static final int TEMP_UNIT_F = 0x10060602;
    static final int DISPLAY_WINDOW_TAB_DEFAULT = 0x10100301;
    static final int DISPLAY_WINDOW_TAB_LEFT_TEMP = 0x10100302;
    static final int DISPLAY_WINDOW_TAB_RIGHT_TEMP = 0x10100303;
    static final int DISPLAY_WINDOW_TAB_HARDWARE_POP = 0x10100304;
    static final int DISPLAY_WINDOW_TAB_SEAT = 0x10100305;
    static final int DISPLAY_WINDOW_TAB_IONS_POP = 0x10100306;
    static final int HVAC_HARDKEY_LEFT_TEMP = 0x10140201;
    static final int HVAC_HARDKEY_RIGHT_TEMP = 0x10140202;
    static final int HVAC_HARDKEY_FAN_UP = 0x10140203;
    static final int HVAC_HARDKEY_FAN_DOWN = 0x10140204;
    static final int HVAC_HARDKEY_MODE = 0x10140205;
    static final int HVAC_HARDKEY_AUTO = 0x10140206;
    static final int HVAC_HARDKEY_AC = 0x10140207;
    static final int HVAC_HARDKEY_LOOP = 0x10140208;
    static final int HVAC_HARDKEY_FRONT_DEFROST = 0x10140209;
    static final int HVAC_HARDKEY_REAR_DEFROST = 0x1014020a;
    static final int HVAC_HARDKEY_TEMP_SYNC = 0x1014020b;
    static final int DIRECTION_MODE_FOCUS = 0x10070301;
    static final int DIRECTION_MODE_AVOID = 0x10070302;
    static final int SWEEPING_MODE_ALL = 0x10070201;
    static final int SWEEPING_MODE_CUSTOM = 0x10070203;
    static final int SEAT_LEVEL_1 = HVAC_SEAT_HEATING_LEVEL_1;
    static final int SEAT_LEVEL_2 = HVAC_SEAT_HEATING_LEVEL_2;
    static final int SEAT_LEVEL_3 = HVAC_SEAT_HEATING_LEVEL_3;
    static final int WHEEL_HEAT_LOW = 0x10090101;
    static final int WHEEL_HEAT_MID = 0x10090102;
    static final int WHEEL_HEAT_HIGH = 0x10090103;
    static final int WHEEL_HEAT_AUTO = 0x1009010f;

    static final int BCM_WINDOW = 0x21030100;
    static final int BCM_WINDOW_LOCK = 0x21030200;
    static final int BCM_DOOR = 0x21020100;
    static final int BCM_DOOR_LOCK = 0x21020200;
    static final int BCM_DOOR_POS = 0x21020300;
    static final int BCM_CHILD_SAFETY_LOCK = 0x21020400;
    static final int BCM_CHARGING_CAP = 0x21020500;
    static final int BCM_FUEL_CAP = 0x21020600;
    static final int BCM_CHILD_SAFETY_LOCK_SCENE = 0x21020700;
    static final int BCM_DOOR_CONTROL = 0x21021000;
    static final int BCM_DOOR_OBSTACLE_DETECTED = 0x21021800;
    static final int BCM_DOOR_ANTI_PINCH = 0x21021900;
    static final int BCM_DOOR_STATUS = 0x21021a00;
    static final int BCM_AUTO_CLOSE_DOOR_BY_SPEED = 0x21110400;
    static final int BCM_ALL_DOORS_ONE_KEY = 0x21110500;
    static final int BCM_SUNROOF_OPEN = 0x21200200;
    static final int BCM_SUNROOF_CLOSE = 0x21200300;
    static final int BCM_SUNCURT_OPEN = 0x21200400;
    static final int BCM_SUNCURT_CLOSE = 0x21200500;
    static final int BCM_MIRROR_FOLD = 0x21060100;
    static final int BCM_REAR_MIRROR_ADJUST = 0x21060200;
    static final int BCM_MIRROR_DEFROST = 0x21110600;
    static final int BCM_STEERING_WHEEL_ADJUST = 0x21070100;
    static final int BCM_READING_LIGHT = 0x21051300;
    static final int BCM_ALL_READING_LIGHTS = 0x21110300;
    static final int BCM_CUSTOM_KEY = 0x21110100;
    static final int BCM_WIPER = 0x21010100;
    static final int BCM_WASHER = 0x21040100;
    static final int BCM_LIGHT_DIPPED_BEAM = 0x21050100;
    static final int BCM_LIGHT_MAIN_BEAM = 0x21050200;
    static final int BCM_LIGHT_DRIVING_LAMPS = 0x21050300;
    static final int BCM_LIGHT_FRONT_FOG = 0x21050400;
    static final int BCM_LIGHT_REAR_FOG = 0x21050500;
    static final int BCM_LIGHT_CORNERING = 0x21050600;
    static final int BCM_LIGHT_SPOT = 0x21050700;
    static final int BCM_LIGHT_FRONT_POSITION = 0x21050800;
    static final int BCM_LIGHT_DAYTIME_RUNNING = 0x21050900;
    static final int BCM_LIGHT_DIM_DIP = 0x21050a00;
    static final int BCM_LIGHT_SIDE_MARKER = 0x21050b00;
    static final int BCM_LIGHT_REAR_POSITION = 0x21050c00;
    static final int BCM_LIGHT_STOP = 0x21050d00;
    static final int BCM_LIGHT_REVERSING = 0x21050e00;
    static final int BCM_LIGHT_HAZARD = 0x21050f00;
    static final int BCM_LIGHT_ATMOSPHERE = 0x21051000;
    static final int BCM_LIGHT_LEFT_TURN = 0x21051100;
    static final int BCM_LIGHT_RIGHT_TURN = 0x21051200;
    static final int BCM_LIGHT_REAR_LOGO = 0x21051400;
    static final int BCM_LIGHT_GRILLE = 0x21051500;
    static final int BCM_LIGHT_ALL_WEATHER = 0x21051600;
    static final int BCM_LIGHT_NUMBER_PLATE = 0x21051700;
    static final int BCM_LIGHT_WELCOME = 0x21051800;
    static final int BCM_LIGHT_GRILLE_COLOR = 0x21051e00;
    static final int BCM_POWER_ONOFF = 0x21100100;
    static final int BCM_POWER_ONOFF_CONFIRM = 0x21100102;
    static final int BCM_DISPLAY_ONOFF = 0x21100200;
    static final int BCM_WINDOW_MOVING_STATE = 0x21110200;
    static final int BCM_WINDOW_POS = 0x21030300;
    static final int BCM_SUNROOF_TILT = 0x21030400;
    static final int BCM_WINDOW_TRANSPARENCY = 0x21030500;
    static final int BCM_WINDOW_CURRENT_POS = 0x21030600;
    static final int BCM_DISPLAY_POSITION = 0x21110700;
    static final int BCM_ICC_NOTIFICATION = 0x21110800;
    static final int BCM_RAIN_SENSOR_SENSITIVITY = 0x21110900;
    static final int BCM_RAIN_SENSOR_SENSITIVITY_MAX = 0x21110a00;
    static final int BCM_RAIN_SENSOR_SENSITIVITY_MIN = 0x21110b00;
    static final int BCM_RAIN_SENSOR_SENSITIVITY_STEP = 0x21110c00;
    static final int BCM_FPL_FOLLOW_DRL = 0x21200100;
    static final int BCM_SUNROOF_INIT = 0x21200000;

    static final int WINDOW_CLOSE = 0x0;
    static final int WINDOW_OPEN = 0x1;
    static final int WINDOW_PAUSE = 0x21030101;
    static final int WINDOW_HALF = 0x21030102;
    static final int WINDOW_OPEN_PAUSE = 0x21030103;
    static final int WINDOW_CLOSE_PAUSE = 0x21030104;
    static final int DOOR_CLOSE = 0x0;
    static final int DOOR_OPEN = 0x1;
    static final int DOOR_PAUSE = 0x21020101;
    static final int WIPER_OFF = 0x0;
    static final int WIPER_AUTO = 0x21010101;
    static final int WIPER_LOW = 0x21010102;
    static final int WIPER_HIGH = 0x21010103;
    static final int WIPER_INTERMITTENT = 0x21010104;
    static final int CUSTOM_KEY_DVR = 0x0;
    static final int CUSTOM_KEY_TRUNK = 0x64;
    static final int CUSTOM_KEY_360 = 0x1;
    static final int CUSTOM_KEY_NAVIGATION = 0x2;
    static final int CUSTOM_KEY_DIM_FULL_SCREEN_MAP = 0x3;
    static final int CUSTOM_KEY_SOUND_SWITCH = 0x4;
    static final int CUSTOM_KEY_COLLECT_FAV = 0x5;
    static final int CUSTOM_KEY_REAR_MIRROR_ADJUST = 0x6;
    static final int CUSTOM_KEY_LOUD_SPEAKER = 0x63;
    static final int CUSTOM_KEY_AUTO_PARK = 0x65;
    static final int CUSTOM_KEY_DRIVING_MODE = 0x66;
    static final int DISPLAY_POSITION_IDLE = 0x21110700;
    static final int DISPLAY_POSITION_A = 0x21110701;
    static final int DISPLAY_POSITION_B = 0x21110702;
    static final int FPL_FOLLOW_DRL_MODE1 = 0x21200101;
    static final int FPL_FOLLOW_DRL_MODE2 = 0x21200102;
    static final int GRILLE_LAMP_COLOR_1 = 0x21051e01;
    static final int GRILLE_LAMP_COLOR_2 = 0x21051e02;
    static final int GRILLE_LAMP_COLOR_3 = 0x21051e03;
    static final int ICC_NOTIFY_NORMAL = 0x21110800;
    static final int ICC_NOTIFY_WARNING = 0x21110804;
    static final int ICC_NOTIFY_ERROR = 0x21110805;
    static final int MIRROR_ADJUST_ACTIVE = 0x1;

    static final int ADAS_AEB = 0x20070e00;
    static final int ADAS_FCW = 0x200e0100;
    static final int ADAS_LKA = 0x20070100;
    static final int ADAS_LDW = 0x28030100;
    static final int ADAS_RCW = 0x20071000;
    static final int ADAS_ELKA = 0x20070600;
    static final int ADAS_LANE_CHANGE_ASSIST = 0x20070700;
    static final int ADAS_AUTO_LANE_CHANGE_ASSIST = 0x28040100;
    static final int ADAS_BLIND_SPOT_DETECTION = 0x28070100;
    static final int ADAS_TRAFFIC_SIGN_RECOGNITION = 0x200b0100;
    static final int ADAS_TRAFFIC_SIGN_ALERT = 0x200b0200;
    static final int ADAS_SPEED_LIMIT_WARN = 0x28060100;
    static final int ADAS_SPEED_LIMIT_WARNING_MODE = 0x28060200;
    static final int ADAS_ACC_WITH_TSR = 0x28060300;
    static final int ADAS_SPEED_LIMITATION_MODE = 0x20030500;
    static final int ADAS_SPEED_CONTROL_MODE = 0x20030600;
    static final int ADAS_PDC = 0x20060300;
    static final int ADAS_PDC_WARNING_VOLUME = 0x28050100;
    static final int ADAS_ACC_TIME_GAP = 0x280a0d00;
    static final int ADAS_ACC_ICC_SWITCH = 0x280a1300;
    static final int ADAS_AI_DRIVER_ASSIST = 0x28080100;
    static final int ADAS_AI_ASSIST_DEFAULT_ON = 0x28080200;
    static final int ADAS_AI_ASSIST_FUSION_NAVI = 0x28080300;
    static final int ADAS_AI_ASSIST_OUT_OVERTAKING_LANE = 0x28080400;
    static final int ADAS_AI_LANE_CHANGE_STRATEGY = 0x28080500;
    static final int ADAS_AI_LANE_CHANGE_CONFIRM = 0x28080600;
    static final int ADAS_AI_LANE_CHANGE_WARNING = 0x28080700;
    static final int ADAS_APB_SWITCH = 0x28080900;
    static final int ADAS_APB_MODE = 0x28080a00;
    static final int ADAS_TLB_SWITCH = 0x28080b00;
    static final int ADAS_TLB_MODE = 0x28080c00;
    static final int ADAS_DRIVE_PILOT = 0x28070400;
    static final int ADAS_DRIVE_PILOT_STATUS = 0x28070500;
    static final int ADAS_MAX_CRUISING_SPEED = 0x28070600;
    static final int ADAS_DRIVE_NZP_STATUS = 0x28070900;
    static final int ADAS_DRIVE_PILOT_ALARM_INFO = 0x28070a00;
    static final int ADAS_DRIVE_PILOT_ACC_LCC_SWITCH = 0x28070b00;
    static final int ADAS_DRIVE_PILOT_ALARM_INFO_CANCEL = 0x28071a00;
    static final int ADAS_TRAFFIC_LIGHT_ATTENTION = 0x20070d00;
    static final int ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND = 0x28010100;
    static final int ADAS_TTS_ACC_ACTIVATE = 0x280a0b00;
    static final int ADAS_TTS_ACC_ACTIVATE_SOUND = 0x280a0f00;
    static final int ADAS_TTS_ACC_EXIT = 0x280a0c00;
    static final int ADAS_TTS_ICC_ACTIVATE = 0x280a0300;
    static final int ADAS_TTS_ICC_ACTIVATE_REMINDER = 0x280a0700;
    static final int ADAS_TTS_ICC_ACTIVATE_SOUND = 0x280a0e00;
    static final int ADAS_TTS_ICC_DRIVING_STATUS = 0x280a0a00;
    static final int ADAS_TTS_ICC_EXIT = 0x280a0600;
    static final int ADAS_TTS_ICC_NOA_DRIVING_STATUS = 0x280a1000;
    static final int ADAS_ADAPTIVE_CRUISE_FAILURE = 0x28081700;
    static final int ADAS_DRIVER_FATIGUE_FAILURE = 0x28081900;
    static final int ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE = 0x28081300;
    static final int ADAS_EMERGENCY_STEERING_FAILURE = 0x28081400;
    static final int ADAS_FORWARD_PRECOLLISION_FAULT = 0x28081500;
    static final int ADAS_FRONT_SIDE_ASSIST_FAILURE = 0x28081600;
    static final int ADAS_LANE_KEEPING_ASSISTANCE_FAILURE = 0x28081200;
    static final int ADAS_PADDLE_LANE_CHANGE_ASSIST = 0x28081b00;
    static final int ADAS_REAR_COLLISION_WARNING_FAILURE = 0x28081800;
    static final int ADAS_TRAFFIC_LIGHTS_IDENTIFY_FAULTS = 0x28081a00;
    static final int ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE = 0x28081100;

    static final int PDC_VOLUME_LOW = 0x28050101;
    static final int PDC_VOLUME_MID = 0x28050102;
    static final int PDC_VOLUME_HIGH = 0x28050103;
    static final int ACC_ICC_OFF = 0x0;
    static final int ACC_ICC_ACC = 0x1;
    static final int ACC_ICC_ICC = 0x2;
    static final int ACC_TIME_GAP_0 = 0x280a0d01;
    static final int ACC_TIME_GAP_1 = 0x280a0d02;
    static final int ACC_TIME_GAP_2 = 0x280a0d03;
    static final int ACC_TIME_GAP_3 = 0x280a0d04;
    static final int FCW_SENSITIVITY_NORMAL = 0x200e0202;
    static final int LKA_MODE_WARN_INTV = 0x20070201;
    static final int SPEED_LIMITATION_MODE_AVSL = 0x20030501;
    static final int SPEED_LIMIT_WARNING_MODE_SOUND = 0x28060203;
    static final int TLB_MODE_LOW = 0x28080c01;
    static final int TLB_MODE_MIDDLE = 0x28080c02;
    static final int TLB_MODE_HIGH = 0x28080c03;
    static final int AI_LANE_CHANGE_STRATEGY_OFF = 0x0;
    static final int AI_LANE_CHANGE_STRATEGY_GENTLE = 0x28080501;
    static final int AI_LANE_CHANGE_STRATEGY_STANDARD = 0x28080502;
    static final int AI_LANE_CHANGE_STRATEGY_RADICAL = 0x28080503;
    static final int AI_LANE_CHANGE_WARNING_OFF = 0x0;
    static final int AI_LANE_CHANGE_WARNING_VOICE = 0x28080701;
    static final int AI_LANE_CHANGE_WARNING_VIBRATE = 0x28080702;
    static final int AI_LANE_CHANGE_WARNING_BOTH = 0x28080703;
    static final int PADDLE_LANE_CHANGE_DISABLE = 0x100d00;
    static final int PADDLE_LANE_CHANGE_ENABLE = 0x100d01;

    static final int PAS_ACTIVATED = 0x200d0100;
    static final int PAS_VOLUME = 0x200d0200;
    static final int PAS_STATUS = 0x23010100;
    static final int PAS_MUTE = 0x23010200;
    static final int PAS_TRAILER_PRESENT = 0x23010300;
    static final int PAS_TOP_VIEW = 0x23010400;
    static final int PAS_SHOW_GRAPHICS = 0x23010500;
    static final int PAS_RADAR_FRONT_INNER_LEFT = 0x23020100;
    static final int PAS_RADAR_FRONT_INNER_RIGHT = 0x23020200;
    static final int PAS_RADAR_FRONT_OUT_LEFT = 0x23020300;
    static final int PAS_RADAR_FRONT_OUT_RIGHT = 0x23020400;
    static final int PAS_RADAR_FRONT_LEFT_SIDE = 0x23020500;
    static final int PAS_RADAR_FRONT_RIGHT_SIDE = 0x23020600;
    static final int PAS_RADAR_REAR_LEFT_SIDE = 0x23020700;
    static final int PAS_RADAR_REAR_RIGHT_SIDE = 0x23020800;
    static final int PAS_RADAR_REAR_OUT_LEFT = 0x23020900;
    static final int PAS_RADAR_REAR_OUT_RIGHT = 0x23020a00;
    static final int PAS_RADAR_REAR_INNER_LEFT = 0x23020b00;
    static final int PAS_RADAR_REAR_INNER_RIGHT = 0x23020c00;
    static final int PAS_RADAR_MAX_DISTANCE = 0x23020d00;
    static final int PAS_RADAR_MIN_DISTANCE = 0x23020e00;
    static final int PAS_RADAR_WORK_MODE = 0x23021000;
    static final int PAS_RADAR_WORK_STATUS = 0x23021100;
    static final int PAS_RADAR_FRONT_CENTER = 0x23021200;
    static final int PAS_RADAR_REAR_CENTER = 0x23021300;
    static final int PAS_PAC_ACTIVATION = 0x23030100;
    static final int PAS_PAC_STATUS = 0x23030101;
    static final int PAS_PAC_STEER_LINK = 0x23030200;
    static final int PAS_PAC_AUTO_FRONT_ACTIV = 0x23030300;
    static final int PAS_PAC_AUTO_REVERSE_CAMERA = 0x23030400;
    static final int PAS_PAC_CAMERA_TYPE = 0x23030500;
    static final int PAS_PAC_OVERLAY_STEERPATH = 0x23030800;
    static final int PAS_PAC_OVERLAY_TOWBAR = 0x23030900;
    static final int PAS_PAC_OVERLAY_DSTINFO = 0x23030a00;
    static final int PAS_PAC_VIEW_SELECTION = 0x23031100;
    static final int PAS_PAC_3DVIEW_POSITION = 0x23031200;
    static final int PAS_PAC_SYS_AVA_STATUS = 0x23031300;
    static final int PAS_PAC_3DVIEW_LOCK = 0x23031400;
    static final int PAS_PAC_APP_INIT_COMPLETED = 0x23031500;
    static final int PAS_PAC_CAR_MODE_TRANSPARENT = 0x23032100;
    static final int PAS_PAC_NEARBY_OBJ_TRIGGER = 0x23032200;
    static final int PAS_PAC_OBSTACLE_DETECTION = 0x23032300;
    static final int PAS_PAC_TOP_VIEW_ZOOM_IN = 0x23032400;
    static final int PAS_PAC_TOURING_VIEW = 0x23032500;
    static final int PAS_SAP_ACTIVATION = 0x23040100;
    static final int PAS_SAP_PARK_TYPE = 0x23040200;
    static final int PAS_SAP_PARK_IN_TYPE = 0x23040300;
    static final int PAS_SAP_PARK_IN_RESUME = 0x23040400;
    static final int PAS_SAP_PARK_OUT_CONFIRM = 0x23040500;
    static final int PAS_SAP_PROGRESS = 0x23040600;
    static final int PAS_SAP_PARK_IN_TYPE_RECOMMEND = 0x23040700;
    static final int PAS_SAP_PARK_IN_NOTI = 0x23041100;
    static final int PAS_SAP_PARK_OUT_NOTI = 0x23041200;
    static final int PAS_RCTA_ACTIVATION = 0x23050100;
    static final int PAS_RCTA_LEFT_WARNING = 0x23050200;
    static final int PAS_RCTA_RIGHT_WARNING = 0x23050300;
    static final int PAS_RCTA_SHOW_GRAPHICS = 0x23050400;
    static final int PAS_RCTA_WARNING_VOLUME = 0x23050500;
    static final int PAS_AVM_OR_APA_ACTIVATION = 0x23100400;
    static final int PAS_APA_SELF_RECOMMENDED = 0x23060100;
    static final int PAS_APA_DETECT_PARKING_SPACE = 0x23a80100;
    static final int PAS_APA_RPA_SWITCH = 0x23a80200;
    static final int PAS_AUT_PRKG_SLOT_NR_REQ = 0x23100600;
    static final int PAS_DRVR_ASSC_SYS_BTN_PUSH = 0x23100500;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD = 0x23100700;
    static final int PAS_PRKG_AUX_INFO_DISP = 0x23110400;
    static final int PAS_PRKG_INTRPT_RELD_BTN = 0x23100800;

    static final int PAS_RADAR_WORK_MODE_OFF = 0x0;
    static final int PAS_RADAR_WORK_MODE_STANDBY = 0x23021001;
    static final int PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE = 0x23021002;
    static final int PAS_RADAR_WORK_MODE_FRONT_ACTIVE = 0x23021003;
    static final int PAS_RADAR_WORK_MODE_REAR_ACTIVE = 0x23021004;
    static final int PAS_AUTO_REVERSE_CAMERA_OFF = 0x0;
    static final int PAS_AUTO_REVERSE_CAMERA_REAR = 0x23030401;
    static final int PAS_AUTO_REVERSE_CAMERA_TOP = 0x23030402;
    static final int PAS_PAC_VIEW_SELECTION_3D = 0x2303110a;
    static final int PAS_PAC_VIEW_REAR_LEFT_3D = 0x2303110c;
    static final int PAS_PAC_VIEW_REAR_RIGHT_3D = 0x2303110d;
    static final int PAS_PAC_3D_POS_OFF = 0x0;
    static final int PAS_PAC_3D_POS_FRONT_CENTER = 0x23031201;
    static final int PAS_PAC_3D_POS_FRONT_RIGHT = 0x23031202;
    static final int PAS_PAC_3D_POS_FRONT_LEFT = 0x23031203;
    static final int PAS_PAC_3D_POS_LEFT = 0x23031204;
    static final int PAS_PAC_3D_POS_RIGHT = 0x23031205;
    static final int PAS_PAC_3D_POS_REAR_CENTER = 0x23031206;
    static final int PAS_PAC_3D_POS_REAR_LEFT = 0x23031207;
    static final int PAS_PAC_3D_POS_REAR_RIGHT = 0x23031208;
    static final int PAS_SAP_PARK_TYPE_IN = 0x23040201;
    static final int PAS_SAP_PARK_TYPE_OUT = 0x23040202;
    static final int PAS_SAP_PARK_IN_TYPE_PERP = 0x23040301;
    static final int PAS_SAP_PARK_IN_TYPE_PARA = 0x23040302;
    static final int PAS_RCTA_VOLUME_OFF = 0x0;
    static final int PAS_RCTA_VOLUME_LOW = 0x23050501;
    static final int PAS_RCTA_VOLUME_MID = 0x23050502;
    static final int PAS_RCTA_VOLUME_HIGH = 0x23050503;
    static final int PAS_DRVR_ASSC_SYS_BTN_SELT_APA = 0x23100501;
    static final int PAS_DRVR_ASSC_SYS_BTN_SELT_RPA = 0x23100502;
    static final int PAS_DRVR_ASSC_SYS_BTN_UNDO = 0x23100503;
    static final int PAS_DRVR_ASSC_SYS_BTN_ENTER_APA_OR_AVM = 0x23100506;
    static final int PAS_DRVR_ASSC_SYS_BTN_MANUAL = 0x23100507;
    static final int PAS_DRVR_ASSC_SYS_BTN_ENTER_APA = 0x23100509;
    static final int PAS_DRVR_ASSC_SYS_BTN_CONFIRM_PARK_OUT = 0x2310050a;
    static final int PAS_DRVR_ASSC_SYS_BTN_SUSPEND = 0x2310050b;
    static final int PAS_DRVR_ASSC_SYS_BTN_ABORT = 0x2310050c;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_CANCEL = 0x23100701;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_HORIZ_PARK_IN = 0x23100702;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN = 0x23100703;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN_FW = 0x23100704;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_PARK_IN_BW = 0x23100705;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_HORIZ_LEFT_PARK_OUT = 0x23100709;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_HORIZ_RIGHT_PARK_OUT = 0x2310070a;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_LEFT_PARK_OUT_FW = 0x2310070b;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_RIGHT_PARK_OUT_FW = 0x2310070c;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_LEFT_PARK_OUT_BW = 0x2310070d;
    static final int PAS_DRVR_ASSC_SYS_PARK_MOD_PERPDIR_RIGHT_PARK_OUT_BW = 0x2310070e;

    static final int VEHICLE_AVAS_SWITCH = 0x201a0500;
    static final int VEHICLE_AVAS_VOLUME = 0x201a0600;
    static final int VEHICLE_AVAS_SOUND_TYPE = 0x201a0700;
    static final int VEHICLE_AVAS_SOUND_TYPE_NAME = 0x201a0800;
    static final int VEHICLE_AVAS_SOUND_TYPE_PATH = 0x201a0900;
    static final int VEHICLE_DIGITAL_KEY = 0x20281000;
    static final int VEHICLE_DIGITAL_KEY_REQ_STS = 0x20281100;
    static final int VEHICLE_DIGITAL_KEY_UNPAIR = 0x20281200;
    static final int VEHICLE_DIGITAL_KEY_TERMINATION = 0x20281300;
    static final int VEHICLE_DIGITAL_KEY_SUSPENSION = 0x20281400;
    static final int VEHICLE_DIGITAL_KEY_PAIRING_FAILED = 0x20281401;
    static final int VEHICLE_DIGITAL_KEY_TRACKING_WAIT = 0x20281402;
    static final int VEHICLE_DIGITAL_KEY_TRACKING_RESULT = 0x20281403;
    static final int VEHICLE_DIGITAL_KEY_RES_TIMEOUT = 0x20291400;

    static final int AVAS_VOLUME_OFF = 0x0;
    static final int AVAS_VOLUME_LOW = 0x201a0601;
    static final int AVAS_VOLUME_MID = 0x201a0602;
    static final int AVAS_VOLUME_HIGH = 0x201a0603;
    static final int AVAS_SOUND_NONE = 0x0;
    static final int AVAS_SOUND_1 = 0x201a0701;
    static final int AVAS_SOUND_2 = 0x201a0702;
    static final int AVAS_SOUND_3 = 0x201a0703;
    static final int AVAS_SOUND_4 = 0x201a0704;
    static final int AVAS_SOUND_5 = 0x201a0705;
    static final int AVAS_SOUND_6 = 0x201a0706;
    static final int AVAS_SOUND_7 = 0x201a0707;
    static final int AVAS_SOUND_8 = 0x201a0708;

    static final int HUD_ACTIVE = 0x20110100;
    static final int HUD_CALIBRATION = 0x20110200;
    static final int HUD_ANGLE_RESET = 0x27010800;
    static final int HUD_SNOW_MODE = 0x27020100;
    static final int HUD_DISPLAY_SAFETY = 0x27030100;
    static final int HUD_DISPLAY_MEDIA = 0x27030200;
    static final int HUD_DISPLAY_NAVI = 0x27030300;
    static final int HUD_DISPLAY_BTPHONE = 0x27030400;
    static final int HUD_DISPLAY_DRIVE_ENVIRONMENT = 0x27030500;

    static final int DRIVE_MODE_SELECT = 0x22010100;
    static final int DRIVE_MODE_ECO = 0x22010101;
    static final int DRIVE_MODE_COMFORT = 0x22010102;
    static final int DRIVE_MODE_DYNAMIC = 0x22010103;
    static final int DRIVE_MODE_XC = 0x22010104;
    static final int DRIVE_MODE_HDC = 0x22010105;
    static final int DRIVE_MODE_PURE = 0x22010106;
    static final int DRIVE_MODE_HYBRID = 0x22010107;
    static final int DRIVE_MODE_POWER = 0x22010108;
    static final int DRIVE_MODE_SNOW = 0x22010109;
    static final int DRIVE_MODE_MUD = 0x2201010a;
    static final int DRIVE_MODE_ROCK = 0x2201010b;
    static final int DRIVE_MODE_PHEV = 0x2201010c;
    static final int DRIVE_MODE_SAND = 0x2201010d;
    static final int DRIVE_MODE_AWD = 0x2201010e;
    static final int DRIVE_MODE_SAVE = 0x2201010f;
    static final int DRIVE_MODE_ECO_HEV_PHEV = 0x22010110;
    static final int DRIVE_MODE_NORMAL = 0x22010111;
    static final int DRIVE_MODE_EAWD = 0x22010112;
    static final int DRIVE_MODE_OFFROAD = 0x22010113;
    static final int DRIVE_MODE_ECO_PLUS = 0x22010114;
    static final int DRIVE_MODE_SPORT_PLUS = 0x22010115;
    static final int DRIVE_MODE_ADAPTIVE = 0x22010116;
    static final int DRIVE_MODE_START_TYPE18 = 0x22010117;
    static final int DRIVE_MODE_START_TYPE72 = 0x22010118;
    static final int DRIVE_MODE_START_TYPE79 = 0x22010119;
    static final int DRIVE_MODE_START_TYPE97 = 0x2201011a;
    static final int DRIVE_MODE_CUSTOM = 0x22010140;
    static final int DRIVE_ECO_BUTTON = 0x22020100;
    static final int DRIVE_CUSTOM_PROPULSION = 0x22030100;
    static final int DRIVE_CUSTOM_SUSPENSION = 0x22030200;
    static final int DRIVE_CUSTOM_RAB = 0x22030300;
    static final int DRIVE_CUSTOM_BPF = 0x22030400;
    static final int DRIVE_CUSTOM_STEERING_FEEL = 0x22030900;
    static final int DRIVE_CUSTOM_CLIMATE = 0x22030a00;
    static final int DRIVE_CUSTOM_INFOR_THEME = 0x22030b00;
    static final int DRIVE_CUSTOM_DRIVER_INFO = 0x22030c00;
    static final int DRIVE_CUSTOM_INTERIOR_LIGHT = 0x22030d00;
    static final int DRIVE_DIM_THEME_SYNC = 0x22040100;
    static final int DRIVE_DIM_THEME_SET = 0x22040200;
    static final int DRIVE_STEERING_MODE = 0x22040400;
    static final int DRIVE_ENERGY_MODE = 0x22040500;
    static final int DRIVE_CREEP_SET = 0x22040600;
    static final int DRIVE_LAUNCH_CONTROL = 0x22040700;
    static final int DRIVE_NOISE_CONTROL = 0x22040800;
    static final int DRIVE_SPEED_LIMIT_RANGE_VALUE = 0x22040900;
    static final int DRIVE_SPEED_LIMIT_RANGE_MAX = 0x22040a00;
    static final int DRIVE_SPEED_LIMIT_RANGE_MIN = 0x22040b00;
    static final int DRIVE_SPEED_LIMIT_RANGE_STEP = 0x22040c00;
    static final int DRIVE_ESC_LEVEL = 0x22040d00;
    static final int DRIVE_STARTRACK_MODE = 0x22040e00;
    static final int DRIVE_PERFORMANCE_SAVING = 0x22040f00;
    static final int DRIVE_POWER_TRAIN_STOP = 0x22041000;
    static final int STEERING_MODE_SOFT = 0x22040401;
    static final int STEERING_MODE_DYNAMIC = 0x22040402;
    static final int CUSTOM_PROPULSION_ECO = 0x22030101;
    static final int CUSTOM_PROPULSION_COMFORT = 0x22030102;
    static final int CUSTOM_PROPULSION_SPORT = 0x22030103;
    static final int CUSTOM_PROPULSION_OFFROAD = 0x22030104;
    static final int CUSTOM_PROPULSION_SNOW = 0x22030105;
    static final int CUSTOM_PROPULSION_SAND = 0x22030106;
    static final int CUSTOM_PROPULSION_HYBRID = 0x22030107;
    static final int CUSTOM_PROPULSION_PURE = 0x22030108;
    static final int CUSTOM_PROPULSION_POWER = 0x22030109;
    static final int CUSTOM_PROPULSION_AWD = 0x2203010a;
    static final int CUSTOM_SUSPENSION_STANDARD = 0x22030201;
    static final int CUSTOM_SUSPENSION_COMFORT = 0x22030202;
    static final int CUSTOM_SUSPENSION_SPORT = 0x22030203;
    static final int CUSTOM_SUSPENSION_OFFROAD = 0x22030204;
    static final int CUSTOM_SUSPENSION_SNOW = 0x22030205;
    static final int CUSTOM_SUSPENSION_AUTOMATIC = 0x22030206;
    static final int CUSTOM_STEERING_LIGHT = 0x22030901;
    static final int CUSTOM_STEERING_BALANCED = 0x22030902;
    static final int CUSTOM_STEERING_HEAVY = 0x22030903;
    static final int CUSTOM_CLIMATE_NORMAL = 0x22030a01;
    static final int CUSTOM_CLIMATE_ECO = 0x22030a02;
    static final int CUSTOM_INFOR_THEME_LOUDER = 0x22030b01;
    static final int CUSTOM_INFOR_THEME_HYPER = 0x22030b02;
    static final int CUSTOM_INFOR_THEME_INTER = 0x22030b03;
    static final int CUSTOM_INFOR_THEME_CLEAR = 0x22030b04;
    static final int CUSTOM_DRIVER_INFO_STANDARD = 0x22030c01;
    static final int CUSTOM_DRIVER_INFO_ECO = 0x22030c02;
    static final int CUSTOM_DRIVER_INFO_SPORT = 0x22030c03;
    static final int CUSTOM_DRIVER_INFO_OFFROAD = 0x22030c04;
    static final int DIM_THEME_RED = 0x22040201;
    static final int DIM_THEME_GOLD = 0x22040202;
    static final int DIM_THEME_BLUE = 0x22040203;
    static final int ENERGY_MODE_RANGE = 0x22040501;
    static final int ENERGY_MODE_TOUR = 0x22040502;
    static final int ENERGY_MODE_SPORT = 0x22040503;
    static final int ESC_LEVEL_1 = 0x22040d01;
    static final int ESC_LEVEL_2 = 0x22040d02;
    static final int ESC_LEVEL_3 = 0x22040d03;
    static final int ESC_LEVEL_4 = 0x22040d04;
    static final int ESC_LEVEL_5 = 0x22040d05;
    static final int STARTRACK_TYPE18 = 0x22040e01;
    static final int STARTRACK_TYPE72 = 0x22040e02;
    static final int STARTRACK_TYPE79 = 0x22040e03;
    static final int STARTRACK_TYPE97 = 0x22040e04;
    static final int POWER_TRAIN_STOP_NOT_BLOCKED = 0x22041000;
    static final int POWER_TRAIN_STOP_EV_BLOCKED = 0x22041001;
    static final int POWER_TRAIN_STOP_HEV_BLOCKED = 0x22041002;
    static final int POWER_TRAIN_STOP_EV_PLUS_BLOCKED = 0x22041003;

    static final int SCENE_THEATER = 0x2f010100;
    static final int SCENE_WASH = 0x2f010200;
    static final int SCENE_PET = 0x2f010500;
    static final int SCENE_SMOKING = 0x2f020100;
    static final int SCENE_PARENT_CHILD = 0x2f020300;
    static final int SCENE_ROMANTIC = 0x2f020600;
    static final int SCENE_NAP = 0x2f020800;
    static final int SCENE_QUEEN = 0x2f020900;
    static final int SCENE_SLEEP = 0x2f020a00;
    static final int SCENE_REAR_ROW_VIDEO = 0x2f020b00;
    static final int SCENE_MEETING = 0x2f020d00;
    static final int SCENE_CAMP = 0x2f021700;
    static final int SCENE_PSD_PASSENGER_THEATER = 0x2f020f30;

    static final int AMBIENCE_LIGHT_INTENSITY = 0x2a010100;
    static final int AMBIENCE_LIGHT_THEME_COLOR = 0x2a010200;
    static final int AMBIENCE_LIGHT_COLOR_WEATHER = 0x200a0b00;
    static final int AMBIENCE_LIGHT_CLIMATE = 0x2a080200;
    static final int AMBIENCE_LIGHT_EFFECT = 0x2a080100;
    static final int AMBIENCE_LIGHT_CONTROL_MODE = 0x2a080600;
    static final int AMBIENCE_LIGHT_MUSIC = 0x2a050f00;
    static final int AMBIENCE_LIGHT_MUSIC_SHOW_MODE = 0x2a050800;
    static final int AMBIENCE_LIGHT_WELCOME_SHOW = 0x2a050100;
    static final int AMBIENCE_LIGHT_GOODBYE_SHOW = 0x2a050200;
    static final int AMBIENCE_LIGHT_WELCOME_SHOW_MODE = 0x2a050700;
    static final int AMBIENCE_LIGHT_VOICE = 0x2a050d00;
    static final int AMBIENCE_LIGHT_ZONE_EXPERIENCE = 0x200a0100;
    static final int AMBIENCE_LIGHT_MAIN_ZONES = 0x200a0500;
    static final int AMBIENCE_LIGHT_TOP_ZONES = 0x200a0400;
    static final int AMBIENCE_LIGHT_BOT_ZONES = 0x200a0300;
    static final int AMBIENCE_LIGHT_BRIGHTNESS_STATIONARY = 0x200a0600;
    static final int AMBIENCE_LIGHT_BRIGHTNESS_DRIVING = 0x200a0700;
    static final int AMBIENCE_LIGHT_INTERACTIVE_EFFECT = 0x200a0800;
    static final int AMBIENCE_LIGHT_COLOR_SET = 0x200a0900;
    static final int AMBIENCE_LIGHT_COLOR_TYPE = 0x200a0a00;
    static final int AMBIENCE_LIGHT_COLOR_RED = 0x2a010201;
    static final int AMBIENCE_LIGHT_COLOR_ORANGE = 0x2a010202;
    static final int AMBIENCE_LIGHT_COLOR_YELLOW = 0x2a010203;
    static final int AMBIENCE_LIGHT_COLOR_GREEN = 0x2a010204;
    static final int AMBIENCE_LIGHT_COLOR_INDIGO = 0x2a010205;
    static final int AMBIENCE_LIGHT_COLOR_BLUE = 0x2a010206;
    static final int AMBIENCE_LIGHT_COLOR_VIOLET = 0x2a010207;
    static final int AMBIENCE_LIGHT_COLOR_WHITE = 0x2a010208;
    static final int AMBIENCE_LIGHT_COLOR_ICE_BLUE = 0x2a010209;
    static final int AMBIENCE_LIGHT_THEME_RADICAL = 0x2a080101;
    static final int AMBIENCE_LIGHT_THEME_SIMPLE = 0x2a080102;
    static final int AMBIENCE_LIGHT_THEME_LIBERATING = 0x2a080103;
    static final int AMBIENCE_LIGHT_THEME_AGILE = 0x2a080104;
    static final int AMBIENCE_LIGHT_EFFECT_SOLID = 0x2a080101;
    static final int AMBIENCE_LIGHT_EFFECT_GRADIENTS = 0x2a080102;
    static final int AMBIENCE_LIGHT_EFFECT_BREATHE = 0x2a080103;
    static final int AMBIENCE_LIGHT_CONTROL_MORE = 0x2a080601;
    static final int AMBIENCE_LIGHT_CONTROL_MUSIC = 0x2a080602;
    static final int AMBIENCE_LIGHT_CONTROL_SCREEN = 0x2a080603;
    static final int AMBIENCE_LIGHT_CONTROL_COLOR = 0x2a080604;
    static final int AMBIENCE_LIGHT_CONTROL_TIME = 0x2a080605;
    static final int AMBIENCE_LIGHT_WELCOME_PASSIONATE = 0x2a050701;
    static final int AMBIENCE_LIGHT_WELCOME_NORMAL = 0x2a050702;
    static final int AMBIENCE_LIGHT_WELCOME_SUBDUED = 0x2a050703;
    static final int AMBIENCE_LIGHT_PHONE_CALL_REMINDER = 0x2a050400;
    static final int AMBIENCE_LIGHT_SLIDING_DOOR_REMINDER = 0x2a050900;
    static final int AMBIENCE_LIGHT_ENDURANCE_MILE_REMINDER = 0x2a050500;
    static final int AMBIENCE_LIGHT_ICHARGING_REMIND = 0x2a080300;
    static final int AMBIENCE_LIGHT_SOLID_COLOR_SET = 0x2a500000;
    static final int AMBIENCE_LIGHT_BREATHE_COLOR_SET = 0x2a500100;
    static final int AMBIENCE_LIGHT_ZONE_ALL = 0x200a0100;
    static final int AMBIENCE_LIGHT_ZONE_FRONT = 0x200a0101;
    static final int AMBIENCE_LIGHT_ZONE_HEADREST = 0x200a0102;
    static final int AMBIENCE_LIGHT_ZONE_REAR = 0x200a0103;

    static final int DAYMODE_SETTING = 0x20150100;
    static final int DAYMODE_SYNC = 0x20150200;
    static final int DAYMODE_BRIGHTNESS_DAY = 0x20150300;
    static final int DAYMODE_BRIGHTNESS_NIGHT = 0x20150400;
    static final int DAYMODE_BRIGHTNESS_MAX = 0x20150500;
    static final int DAYMODE_BRIGHTNESS_MIN = 0x20150600;
    static final int DAYMODE_BRIGHTNESS_STEP = 0x20150700;
    static final int DAYMODE_BACKLIGHT_LINKAGE = 0x29010100;
    static final int DAYMODE_BACKLIGHT_BRIGHTNESS = 0x29020100;
    static final int DAYMODE_DIM_BRIGHTNESS = 0x29020500;
    static final int DAYMODE_FLOODLIGHT_BRIGHTNESS = 0x29020900;
    static final int DAYMODE_ELECTRIC_REAR_VIEW_MIRROR = 0x29020d00;
    static final int DAYMODE_BRIGHTNESS_SCREEN = 0x29030500;
    static final int DAYMODE_CUSTOM_DAY_TIME = 0x29030300;
    static final int DAYMODE_CUSTOM_NIGHT_TIME = 0x29030400;
    static final int DAYMODE_SUN_TIME = 0x29030600;
    static final int DAYMODE_TIME_CONTROL_THEME_SWITCH = 0x29030700;
    static final int DAYMODE_PSD_BRIGHTNESS_DAYMODE = 0x29200000;
    static final int DAYMODE_PSD_BRIGHTNESS_SCREEN = 0x29200100;
    static final int DAYMODE_VALUE_DAY = 0x20150101;
    static final int DAYMODE_VALUE_NIGHT = 0x20150102;
    static final int DAYMODE_VALUE_AUTO = 0x20150103;

    static final int SEAT_LENGTH = 0x2d020100;
    static final int SEAT_HEIGHT = 0x2d020200;
    static final int SEAT_BACKREST = 0x2d030200;
    static final int SEAT_POSITION_SAVE = 0x2d400100;
    static final int SEAT_POSITION_SET = 0x2d400200;
    static final int SEAT_RESTORE = 0x2d400300;
    static final int SEAT_ONE_KEY_COMFORT = 0x2d411100;
    static final int SEAT_POSITION_1 = 0x2d400101;
    static final int SEAT_POSITION_2 = 0x2d400102;
    static final int SEAT_MEMORY_1 = 0x1;
    static final int SEAT_MEMORY_2 = 0x2;
    static final int SEAT_MEMORY_3 = 0x3;
    static final int SEAT_FORWARD = 0x2d020101;
    static final int SEAT_BACKWARD = 0x2d020102;
    static final int SEAT_HEIGHT_UP = 0x2d020201;
    static final int SEAT_HEIGHT_DOWN = 0x2d020202;
    static final int SEAT_BACKREST_FORWARD = 0x2d030201;
    static final int SEAT_BACKREST_BACKWARD = 0x2d030202;

    private final Context context;
    private Object car;
    private Object carFunction;
    private Object vfmiscManager;
    private String lastError = "";

    EcarxVehicleAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Spec spec(int functionId) {
        return specFor(functionId);
    }

    boolean isWritable(int functionId) {
        return spec(functionId).writable;
    }

    boolean isFloatFunction(int functionId) {
        return spec(functionId).floatValue;
    }

    Result set(int functionId, int value) {
        return set(functionId, defaultZone(functionId), value);
    }

    Result get(int functionId) {
        return get(functionId, defaultZone(functionId));
    }

    Result support(int functionId) {
        return support(functionId, defaultZone(functionId));
    }

    Result set(int functionId, int zone, int value) {
        Spec spec = spec(functionId);
        if (!spec.writable) {
            return Result.external("Функция переведена в diagnostics/readback-only: " + hex(functionId), false, true);
        }
        if (spec.backend == Backend.OEM_ENTRY) {
            return setOemEntry(functionId, zone, value);
        }
        if (spec.backend == Backend.ADAPT_API) {
            Result support = support(functionId, zone);
            if (!support.isSupported()) {
                return Result.external("Функция недоступна в AdaptAPI: " + hex(functionId) + "/" + zone, false, false);
            }
        }
        Log.i(TAG, "setFunctionValue id=" + hex(functionId)
                + " zone=" + hex(zone)
                + " value=" + hex(value));

        try {
            Object fn = function();

            try {
                Method method = fn.getClass().getMethod(
                        "setFunctionValue",
                        int.class,
                        int.class,
                        int.class
                );

                Object result = method.invoke(fn, functionId, zone, value);
                boolean success = Boolean.TRUE.equals(result);

                Log.i(TAG, "setFunctionValue(function, zone, value)"
                        + " result=" + result);

                return Result.ok(
                        functionId,
                        zone,
                        value,
                        success,
                        "AdaptAPI setFunctionValue(function, zone, value)"
                );
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Zoned setFunctionValue unavailable, using fallback");

                Method method = fn.getClass().getMethod(
                        "setFunctionValue",
                        int.class,
                        int.class
                );

                Object result = method.invoke(fn, functionId, value);
                boolean success = Boolean.TRUE.equals(result);

                Log.i(TAG, "setFunctionValue(function, value)"
                        + " result=" + result);

                return Result.ok(
                        functionId,
                        zone,
                        value,
                        success,
                        "AdaptAPI setFunctionValue(function, value) fallback"
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "setFunctionValue failed"
                    + " id=" + hex(functionId)
                    + " zone=" + hex(zone)
                    + " value=" + hex(value), e);

            return Result.error(functionId, zone, value, e);
        }
    }

    Result get(int functionId, int zone) {
        if (spec(functionId).backend == Backend.OEM_ENTRY) {
            return Result.external("OEM entry не поддерживает прямой readback: " + hex(functionId), false, true);
        }
        Log.i(TAG, "getFunctionValue id=" + hex(functionId)
                + " zone=" + hex(zone));

        try {
            Object fn = function();

            try {
                Method method = fn.getClass().getMethod(
                        "getFunctionValue",
                        int.class,
                        int.class
                );

                Object result = method.invoke(fn, functionId, zone);
                int value = ((Number) result).intValue();

                Log.i(TAG, "getFunctionValue(function, zone)"
                        + " result=" + hex(value));

                return Result.value(functionId, zone, value);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Zoned getFunctionValue unavailable, using fallback");

                Method method = fn.getClass().getMethod(
                        "getFunctionValue",
                        int.class
                );

                Object result = method.invoke(fn, functionId);
                int value = ((Number) result).intValue();

                Log.i(TAG, "getFunctionValue(function)"
                        + " result=" + hex(value));

                return Result.value(functionId, zone, value);
            }
        } catch (Exception e) {
            Log.e(TAG, "getFunctionValue failed"
                    + " id=" + hex(functionId)
                    + " zone=" + hex(zone), e);

            return Result.error(functionId, zone, 0, e);
        }
    }

    Result setFloat(int functionId, int zone, float value) {
        Spec spec = spec(functionId);
        if (!spec.writable) {
            return Result.external("Функция переведена в diagnostics/readback-only: " + hex(functionId), false, true);
        }
        Result support = support(functionId, zone);
        if (!support.isSupported()) {
            return Result.external("Функция недоступна в AdaptAPI: " + hex(functionId) + "/" + zone, false, false);
        }
        try {
            Object fn = function();
            Method method = fn.getClass().getMethod("setCustomizeFunctionValue", int.class, int.class, float.class);
            Object ok = method.invoke(fn, functionId, zone, value);
            return Result.floatValue(functionId, zone, value, Boolean.TRUE.equals(ok), "AdaptAPI setCustomizeFunctionValue(function, zone, float)");
        } catch (Exception e) {
            return Result.floatError(functionId, zone, value, e);
        }
    }

    Result getFloat(int functionId, int zone) {
        try {
            Object fn = function();
            Method method = fn.getClass().getMethod("getCustomizeFunctionValue", int.class, int.class);
            Object value = method.invoke(fn, functionId, zone);
            return Result.floatStatus(functionId, zone, ((Number) value).floatValue());
        } catch (Exception e) {
            return Result.error(functionId, zone, 0, e);
        }
    }

    Result support(int functionId, int zone) {
        Spec spec = spec(functionId);
        if (spec.backend == Backend.OEM_ENTRY) {
            try {
                if (functionId == BCM_CUSTOM_KEY) {
                    vfmisc();
                    return Result.external("OEM entry available via Vfmisc: " + hex(functionId), true, true);
                }
                return Result.external("OEM entry backend not implemented: " + hex(functionId), false, false);
            } catch (Exception e) {
                return Result.external("OEM entry unavailable: " + hex(functionId) + " " + compact(e), false, false);
            }
        }
        Log.i(TAG, "isFunctionSupported id=" + hex(functionId)
                + " zone=" + hex(zone));

        try {
            Object fn = function();

            try {
                Method method = fn.getClass().getMethod(
                        "isFunctionSupported",
                        int.class,
                        int.class
                );

                Object status = method.invoke(fn, functionId, zone);

                Log.i(TAG, "isFunctionSupported(function, zone)"
                        + " result=" + status);

                return Result.status(
                        functionId,
                        zone,
                        "isFunctionSupported(function, zone) -> " + status
                );
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Zoned isFunctionSupported unavailable, using fallback");

                Method method = fn.getClass().getMethod(
                        "isFunctionSupported",
                        int.class
                );

                Object status = method.invoke(fn, functionId);

                Log.i(TAG, "isFunctionSupported(function)"
                        + " result=" + status);

                return Result.status(
                        functionId,
                        zone,
                        "isFunctionSupported(function) -> " + status
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "isFunctionSupported failed"
                    + " id=" + hex(functionId)
                    + " zone=" + hex(zone), e);

            return Result.error(functionId, zone, 0, e);
        }
    }

    Result[] setAll(Command... commands) {
        Result[] results = new Result[commands.length];
        for (int i = 0; i < commands.length; i++) {
            Command c = commands[i];
            results[i] = set(c.functionId, c.zone, c.value);
        }
        return results;
    }

    String availability() {
        try {
            function();
            return "ECarX AdaptAPI доступен: com.ecarx.xui.adaptapi.car.Car -> ICarFunction";
        } catch (Exception e) {
            lastError = compact(e);
            return "ECarX AdaptAPI недоступен: " + lastError;
        }
    }

    String lastError() {
        return lastError;
    }

    private Object function() throws Exception {
        if (carFunction != null) return carFunction;
        if (car == null) car = CarBridge.create(context);
        callOptional(car, "connect");
        Method getter = car.getClass().getMethod("getICarFunction");
        carFunction = getter.invoke(car);
        if (carFunction == null) throw new IllegalStateException("getICarFunction returned null");
        return carFunction;
    }

    private static int defaultZone(int functionId) {
        return specFor(functionId).defaultZone;
    }

    private static Spec specFor(int functionId) {
        if (isGlobalHvac(functionId)) return new Spec(ZONE_ALL, Backend.ADAPT_API, true, false, false);
        if (functionId == HVAC_FAN_SPEED || functionId == HVAC_FAN_SPEED_BLOWER) {
            return new Spec(ZONE_ROW_1_ALL, Backend.ADAPT_API, true, false, false);
        }
        if (isDriverSeatFunction(functionId)) return new Spec(ZONE_DRIVER_LEFT, Backend.ADAPT_API, true, false, false);
        if (functionId == HVAC_TEMP) return new Spec(0, Backend.ADAPT_API_FLOAT, true, true, false);
        if (functionId == HVAC_TEMP_MAX || functionId == HVAC_TEMP_MIN || functionId == HVAC_TEMP_STEP) {
            return new Spec(0, Backend.ADAPT_API_FLOAT, false, true, false);
        }
        if (isBcmWindowFunction(functionId)) {
            boolean writable = functionId == BCM_WINDOW || functionId == BCM_WINDOW_LOCK;
            return new Spec(BCM_WINDOW_ALL, Backend.ADAPT_API, writable, false, false);
        }
        if (functionId == BCM_DOOR) return new Spec(BCM_DOOR_ROW_1_LEFT, Backend.ADAPT_API, true, false, false);
        if (functionId == BCM_DOOR_POS) return new Spec(BCM_DOOR_REAR, Backend.ADAPT_API_FLOAT, false, true, false);
        if (functionId == BCM_DOOR_STATUS) return new Spec(BCM_DOOR_REAR, Backend.ADAPT_API, false, false, true);
        if (functionId == BCM_CHILD_SAFETY_LOCK) return new Spec(BCM_DOOR_ROW_2_LEFT, Backend.ADAPT_API, true, false, false);
        if (functionId == BCM_CHILD_SAFETY_LOCK_SCENE || functionId == BCM_DOOR_CONTROL) {
            return new Spec(ZONE_ALL, Backend.ADAPT_API, true, false, false);
        }
        if (functionId == BCM_DOOR_LOCK) return new Spec(ZONE_ALL, Backend.ADAPT_API, false, false, false);
        if (functionId == BCM_REAR_MIRROR_ADJUST) return new Spec(ZONE_DRIVER_LEFT, Backend.ADAPT_API, true, false, false);
        if (functionId == BCM_MIRROR_FOLD
                || functionId == BCM_LIGHT_DIPPED_BEAM
                || functionId == BCM_LIGHT_MAIN_BEAM
                || functionId == BCM_LIGHT_FRONT_FOG
                || functionId == BCM_LIGHT_REAR_FOG
                || functionId == BCM_LIGHT_GRILLE
                || functionId == BCM_ALL_READING_LIGHTS
                || functionId == BCM_WIPER
                || functionId == BCM_WASHER) {
            return new Spec(ZONE_ALL, Backend.ADAPT_API, true, false, false);
        }
        if (functionId == BCM_SUNROOF_OPEN
                || functionId == BCM_SUNROOF_CLOSE
                || functionId == BCM_SUNCURT_OPEN
                || functionId == BCM_SUNCURT_CLOSE
                || functionId == BCM_LIGHT_HAZARD) {
            return new Spec(ZONE_ALL, Backend.ADAPT_API, false, false, false);
        }
        if (functionId == BCM_CUSTOM_KEY) return new Spec(ZONE_ALL, Backend.OEM_ENTRY, true, false, false);
        if (functionId == DRIVE_MODE_SELECT) return new Spec(ZONE_ALL, Backend.ADAPT_API, false, false, false);
        if (isPasDirectFunction(functionId)) return new Spec(ZONE_ALL, Backend.ADAPT_API, false, false, false);
        if (isHudDirectFunction(functionId)) return new Spec(ZONE_ALL, Backend.ADAPT_API, false, false, false);
        if (isReadOnlyAdasFunction(functionId)) return new Spec(0, Backend.ADAPT_API, false, false, false);
        return SPEC_DEFAULT;
    }

    private static boolean isGlobalHvac(int functionId) {
        switch (functionId) {
            case HVAC_POWER:
            case HVAC_AUTO:
            case HVAC_AC:
            case HVAC_AC_MAX:
            case HVAC_CIRCULATION:
            case HVAC_BLOWING_MODE:
            case HVAC_AUTO_BLOWING_MODE:
            case HVAC_DEFROST_FRONT:
            case HVAC_DEFROST_FRONT_MAX:
            case HVAC_DEFROST_REAR:
            case HVAC_CLIMATE_ZONE:
            case HVAC_AUTO_FAN_SETTING:
            case HVAC_CIRCULATION_TIMER:
            case HVAC_CIRCULATION_LONG_TOUCH:
            case HVAC_AUTO_DEFROST_REAR:
            case HVAC_AUTO_DEFROST_FRONT:
            case HVAC_AUTO_DEFROST_REQUEST:
            case HVAC_AUTO_DEFROST_CONFIRM:
            case HVAC_TEMP_DUAL:
            case HVAC_TEMP_UNIT:
            case HVAC_TEMP_HARD_KEY:
            case HVAC_SWEEPING_MODE:
            case HVAC_DIRECTION_MODE:
            case HVAC_SWEEPING_HORIZONTAL_POS:
            case HVAC_SWEEPING_VERTICAL_POS:
            case HVAC_BLOWING_TEMP_COLOR:
            case HVAC_ECO:
            case HVAC_AQS_SWITCH:
            case HVAC_AUTO_DEHUMIDIFICATION:
            case HVAC_OVERHEAT_PROTECTION:
            case HVAC_IONS_SWITCH:
            case HVAC_STEERING_WHEEL_HEAT:
            case HVAC_AUTO_STEERING_WHEEL_HEAT:
            case HVAC_AUTO_STEERING_WHEEL_HEAT_TIME:
            case HVAC_AUTO_STEERING_WHEEL_HEAT_SWITCH:
            case HVAC_PRE_CLIMATISATION:
            case HVAC_POST_CLIMATISATION:
            case HVAC_AI_POWER:
            case HVAC_AIR_FRAGRANCE:
            case HVAC_AIR_FRAGRANCE_TYPE:
            case HVAC_AIR_FRAGRANCE_LEVEL:
            case HVAC_AIR_FRAGRANCE_RATIO:
            case HVAC_AUTO_ION_REQUEST:
            case HVAC_AUTO_ION_CONFIRM:
            case HVAC_AUTO_DEHUMIDIFICATION_REQUEST:
            case HVAC_AUTO_DEHUMIDIFICATION_CONFIRM:
            case HVAC_CO2_SWITCH:
            case HVAC_AUTO_CLOSE_WINDOW_REMIND:
            case HVAC_AUTO_SECOND_ROW_CLIMATE:
            case HVAC_CLIMATE_LOCK:
            case HVAC_DISPLAY_WINDOW_TAB:
            case HVAC_G_CLEAN:
            case HVAC_AUTOMATIC_VENTILATION_DRY:
            case HVAC_AUTO_CZIS:
            case HVAC_VENTILATION_ONTIME:
            case HVAC_CLIMATE_HARDKEY_SOUND:
            case HVAC_INTELLIGENT_RECOMMENDATION:
            case HVAC_TEMP_OPTIMIZE:
            case HVAC_MODULE_CONNECT_STATUS:
            case HVAC_ELECTRICAL_AIR_VENT:
            case HVAC_HARDKEY:
            case HVAC_FILTER_ELEMENT_LIFE:
            case HVAC_RESET_FILTER_ELEMENT_LIFE:
            case HVAC_RAPID_COOLING:
            case HVAC_RAPID_WARMING:
            case HVAC_IONIZER_CLS_WIN_POPUP_SETTING:
            case HVAC_IONIZER_CLS_WIN_POPUP:
            case HVAC_AQS_STATUS:
                return true;
            default:
                return false;
        }
    }

    private static boolean isDriverSeatFunction(int functionId) {
        switch (functionId) {
            case HVAC_SEAT_HEATING:
            case HVAC_SEAT_VENTILATION:
            case HVAC_SEAT_MASSAGE:
            case HVAC_AUTO_SEAT_HEATING:
            case HVAC_AUTO_SEAT_VENTILATION:
            case HVAC_AUTO_SEAT_MASSAGE:
            case SEAT_LENGTH:
            case SEAT_HEIGHT:
            case SEAT_BACKREST:
            case SEAT_POSITION_SAVE:
            case SEAT_POSITION_SET:
            case SEAT_RESTORE:
            case SEAT_ONE_KEY_COMFORT:
                return true;
            default:
                return false;
        }
    }

    private static boolean isBcmWindowFunction(int functionId) {
        switch (functionId) {
            case BCM_WINDOW:
            case BCM_WINDOW_LOCK:
            case BCM_WINDOW_POS:
            case BCM_WINDOW_CURRENT_POS:
            case BCM_WINDOW_MOVING_STATE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isPasDirectFunction(int functionId) {
        switch (functionId) {
            case PAS_ACTIVATED:
            case PAS_STATUS:
            case PAS_MUTE:
            case PAS_SHOW_GRAPHICS:
            case PAS_RADAR_WORK_MODE:
            case PAS_RADAR_WORK_STATUS:
            case PAS_RADAR_FRONT_CENTER:
            case PAS_RADAR_REAR_CENTER:
            case PAS_PAC_ACTIVATION:
            case PAS_PAC_STATUS:
            case PAS_PAC_STEER_LINK:
            case PAS_PAC_AUTO_FRONT_ACTIV:
            case PAS_PAC_AUTO_REVERSE_CAMERA:
            case PAS_PAC_CAMERA_TYPE:
            case PAS_PAC_OVERLAY_STEERPATH:
            case PAS_PAC_OVERLAY_TOWBAR:
            case PAS_PAC_OVERLAY_DSTINFO:
            case PAS_PAC_VIEW_SELECTION:
            case PAS_PAC_3DVIEW_POSITION:
            case PAS_PAC_SYS_AVA_STATUS:
            case PAS_PAC_3DVIEW_LOCK:
            case PAS_PAC_APP_INIT_COMPLETED:
            case PAS_PAC_CAR_MODE_TRANSPARENT:
            case PAS_PAC_NEARBY_OBJ_TRIGGER:
            case PAS_PAC_OBSTACLE_DETECTION:
            case PAS_PAC_TOP_VIEW_ZOOM_IN:
            case PAS_PAC_TOURING_VIEW:
            case PAS_SAP_ACTIVATION:
            case PAS_SAP_PARK_TYPE:
            case PAS_SAP_PARK_IN_TYPE:
            case PAS_SAP_PARK_IN_RESUME:
            case PAS_SAP_PARK_OUT_CONFIRM:
            case PAS_SAP_PROGRESS:
            case PAS_SAP_PARK_IN_TYPE_RECOMMEND:
            case PAS_SAP_PARK_IN_NOTI:
            case PAS_SAP_PARK_OUT_NOTI:
            case PAS_RCTA_ACTIVATION:
            case PAS_RCTA_LEFT_WARNING:
            case PAS_RCTA_RIGHT_WARNING:
            case PAS_RCTA_SHOW_GRAPHICS:
            case PAS_RCTA_WARNING_VOLUME:
            case PAS_AVM_OR_APA_ACTIVATION:
            case PAS_APA_SELF_RECOMMENDED:
            case PAS_APA_DETECT_PARKING_SPACE:
            case PAS_APA_RPA_SWITCH:
            case PAS_AUT_PRKG_SLOT_NR_REQ:
            case PAS_DRVR_ASSC_SYS_BTN_PUSH:
            case PAS_DRVR_ASSC_SYS_PARK_MOD:
            case PAS_PRKG_AUX_INFO_DISP:
            case PAS_PRKG_INTRPT_RELD_BTN:
                return true;
            default:
                return false;
        }
    }

    private static boolean isReadOnlyAdasFunction(int functionId) {
        switch (functionId) {
            case ADAS_APB_MODE:
            case ADAS_SPEED_LIMIT_WARNING_MODE:
            case ADAS_DRIVE_PILOT_STATUS:
            case ADAS_DRIVE_NZP_STATUS:
            case ADAS_DRIVE_PILOT_ALARM_INFO:
            case ADAS_DRIVE_PILOT_ALARM_INFO_CANCEL:
            case ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE:
            case ADAS_LANE_KEEPING_ASSISTANCE_FAILURE:
            case ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE:
            case ADAS_EMERGENCY_STEERING_FAILURE:
            case ADAS_FORWARD_PRECOLLISION_FAULT:
            case ADAS_FRONT_SIDE_ASSIST_FAILURE:
            case ADAS_ADAPTIVE_CRUISE_FAILURE:
            case ADAS_REAR_COLLISION_WARNING_FAILURE:
            case ADAS_TRAFFIC_LIGHTS_IDENTIFY_FAULTS:
            case ADAS_DRIVER_FATIGUE_FAILURE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isHudDirectFunction(int functionId) {
        switch (functionId) {
            case HUD_ACTIVE:
            case HUD_CALIBRATION:
            case HUD_ANGLE_RESET:
            case HUD_SNOW_MODE:
            case HUD_DISPLAY_SAFETY:
            case HUD_DISPLAY_MEDIA:
            case HUD_DISPLAY_NAVI:
            case HUD_DISPLAY_BTPHONE:
            case HUD_DISPLAY_DRIVE_ENVIRONMENT:
                return true;
            default:
                return false;
        }
    }

    private void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }

    private Result setOemEntry(int functionId, int zone, int value) {
        if (functionId != BCM_CUSTOM_KEY) {
            return Result.external("OEM entry backend not implemented: " + hex(functionId), false, false);
        }
        try {
            Object manager = vfmisc();
            int hardwareValue = toHardwareApiCustomKey(value);
            Method method = manager.getClass().getMethod("CB_SelfDefineFuncReq", int.class);
            Object raw = method.invoke(manager, hardwareValue);
            boolean success = isVfmiscSuccess(raw);
            return Result.external(
                    "Vfmisc CB_SelfDefineFuncReq -> " + raw
                            + " adapt=" + hex(value)
                            + " hw=" + hex(hardwareValue),
                    success,
                    true
            );
        } catch (Exception e) {
            return Result.external("OEM entry BCM_CUSTOM_KEY failed: " + compact(e), false, false);
        }
    }

    private Object vfmisc() throws Exception {
        if (vfmiscManager != null) return vfmiscManager;
        vfmiscManager = CarBridge.getVfmiscManager(context);
        return vfmiscManager;
    }

    private static int toHardwareApiCustomKey(int adaptApiValue) {
        switch (adaptApiValue) {
            case CUSTOM_KEY_DIM_FULL_SCREEN_MAP:
                return 0;
            case CUSTOM_KEY_DVR:
                return 1;
            case CUSTOM_KEY_360:
                return 2;
            case CUSTOM_KEY_NAVIGATION:
                return 3;
            case CUSTOM_KEY_SOUND_SWITCH:
                return 4;
            case CUSTOM_KEY_COLLECT_FAV:
                return 5;
            case CUSTOM_KEY_LOUD_SPEAKER:
                return 6;
            case CUSTOM_KEY_REAR_MIRROR_ADJUST:
                return 7;
            case CUSTOM_KEY_TRUNK:
                return 8;
            case CUSTOM_KEY_AUTO_PARK:
                return 9;
            case CUSTOM_KEY_DRIVING_MODE:
                return 10;
            default:
                return adaptApiValue;
        }
    }

    private static boolean isVfmiscSuccess(Object raw) {
        if (raw == null) return false;
        if (raw instanceof Boolean) return (Boolean) raw;
        if (raw instanceof Number) return ((Number) raw).intValue() != 0;
        String value = String.valueOf(raw).toLowerCase(Locale.US);
        return value.contains("succeed") || value.contains("success") || value.equals("ok") || value.equals("true");
    }

    private static String compact(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    static final class Result {
        final int functionId;
        final int zone;
        final int value;
        final boolean success;
        final boolean supported;
        final String message;

        private Result(int functionId, int zone, int value, boolean success, boolean supported, String message) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
            this.success = success;
            this.supported = supported;
            this.message = message;
        }

        static Result ok(int functionId, int zone, int value, boolean apiResult, String path) {
            return new Result(functionId, zone, value, apiResult, apiResult,
                    path + " -> " + apiResult + " " + hex(functionId) + "/" + zone + "=" + hex(value));
        }

        static Result value(int functionId, int zone, int value) {
            return new Result(functionId, zone, value, true, isKnownRawStatus(functionId) || value != 0xff,
                    String.format(Locale.US, "getFunctionValue %s/%d = %s", hex(functionId), zone, hex(value)));
        }

        static Result status(int functionId, int zone, String status) {
            String normalized = status == null ? "" : status.toLowerCase(Locale.US);
            boolean supported = !(normalized.contains("notavailable") || normalized.contains("unsupported") || normalized.contains("false"));
            return new Result(functionId, zone, 0, true, supported,
                    String.format(Locale.US, "%s/%d %s", hex(functionId), zone, status));
        }

        static Result floatValue(int functionId, int zone, float value, boolean apiResult, String path) {
            return new Result(functionId, zone, 0, apiResult, apiResult,
                    String.format(Locale.US, "%s -> %s %s/%d=%.1f", path, apiResult, hex(functionId), zone, value));
        }

        static Result floatStatus(int functionId, int zone, float value) {
            return new Result(functionId, zone, 0, true, true,
                    String.format(Locale.US, "getCustomizeFunctionValue %s/%d = %.1f", hex(functionId), zone, value));
        }

        static Result error(int functionId, int zone, int value, Exception e) {
            return new Result(functionId, zone, value, false, false,
                    "Ошибка AdaptAPI " + hex(functionId) + "/" + zone + "=" + hex(value) + ": " + compact(e));
        }

        static Result floatError(int functionId, int zone, float value, Exception e) {
            return new Result(functionId, zone, 0, false, false,
                    String.format(Locale.US, "Ошибка AdaptAPI %s/%d=%.1f: %s", hex(functionId), zone, value, compact(e)));
        }

        static Result external(String message, boolean success, boolean supported) {
            return new Result(0, 0, 0, success, supported, message);
        }

        boolean isSupported() {
            return supported;
        }

        private static boolean isKnownRawStatus(int functionId) {
            return specFor(functionId).knownRawStatus;
        }
    }

    enum Backend {
        ADAPT_API,
        ADAPT_API_FLOAT,
        SIGNAL,
        EVS,
        SAFETY,
        OEM_ENTRY,
        UNKNOWN
    }

    static final class Spec {
        final int defaultZone;
        final Backend backend;
        final boolean writable;
        final boolean floatValue;
        final boolean knownRawStatus;

        Spec(int defaultZone, Backend backend, boolean writable, boolean floatValue, boolean knownRawStatus) {
            this.defaultZone = defaultZone;
            this.backend = backend;
            this.writable = writable;
            this.floatValue = floatValue;
            this.knownRawStatus = knownRawStatus;
        }
    }

    static final class Command {
        final int functionId;
        final int zone;
        final int value;

        Command(int functionId, int value) {
            this(functionId, 0, value);
        }

        Command(int functionId, int zone, int value) {
            this.functionId = functionId;
            this.zone = zone;
            this.value = value;
        }
    }
}
