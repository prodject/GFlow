#!/bin/sh
set -eu

PACKAGE="com.prodject.gflow"
REMOTE_APK="/data/local/tmp/GFlow.apk"
REMOTE_HELPER="/data/local/tmp/installer.dex"
HELPER_CLASS="Installer"
REINSTALL_ON_SIGNATURE_MISMATCH=0
APK="${1:-}"
HELPER="${GFLOW_INSTALL_HELPER:-}"
SATELLITE_PACKAGES="com.maxinf.car org.kman.WifiManager com.salat.gbinder"
WIFI_TOOLKIT_PM_GRANTS="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE android.permission.WRITE_SECURE_SETTINGS android.permission.WRITE_GLOBAL_SETTINGS android.permission.SYSTEM_ALERT_WINDOW android.permission.ACCESS_SURFACE_FLINGER android.permission.WRITE_SETTINGS android.permission.MODIFY_AUDIO_SETTINGS android.permission.MODIFY_PHONE_STATE android.permission.DUMP android.permission.CHANGE_CONFIGURATION android.permission.READ_LOGS android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER android.permission.INSTALL_PACKAGES android.permission.DELETE_PACKAGES android.permission.FORCE_STOP_PACKAGES android.permission.GET_TASKS android.permission.REAL_GET_TASKS android.permission.MANAGE_ACTIVITY_TASKS android.permission.REORDER_TASKS android.permission.STATUS_BAR_SERVICE android.permission.EXPAND_STATUS_BAR android.permission.UPDATE_DEVICE_STATS android.permission.BLUETOOTH_PRIVILEGED android.permission.MANAGE_EXTERNAL_STORAGE android.permission.READ_INTERNAL_STORAGE android.permission.READ_EXTERNAL_STORAGE android.permission.WRITE_EXTERNAL_STORAGE android.permission.READ_MEDIA_IMAGES android.permission.READ_MEDIA_VIDEO android.permission.READ_MEDIA_AUDIO android.permission.READ_MEDIA_VISUAL_USER_SELECTED android.permission.READ_PHONE_STATE android.permission.CALL_PHONE android.permission.READ_CONTACTS android.permission.GET_ACCOUNTS android.permission.CAMERA android.permission.RECORD_AUDIO android.permission.NFC android.permission.ACTIVITY_RECOGNITION android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION android.permission.ENFORCE_UPDATE_OWNERSHIP android.permission.TURN_SCREEN_ON"
APPOPS_ALLOW_MAX="POST_NOTIFICATION START_FOREGROUND RUN_IN_BACKGROUND RUN_ANY_IN_BACKGROUND REQUEST_INSTALL_PACKAGES REQUEST_IGNORE_BATTERY_OPTIMIZATIONS SYSTEM_ALERT_WINDOW WRITE_SETTINGS READ_LOGS SCHEDULE_EXACT_ALARM USE_FULL_SCREEN_INTENT TURN_SCREEN_ON WAKE_LOCK GET_USAGE_STATS ACCESS_BACKGROUND_LOCATION"
BASE_PM_GRANTS="android.permission.CAMERA android.permission.RECORD_AUDIO android.permission.ACCESS_FINE_LOCATION android.permission.ACCESS_COARSE_LOCATION android.permission.ACCESS_BACKGROUND_LOCATION android.permission.POST_NOTIFICATIONS android.permission.READ_MEDIA_IMAGES android.permission.READ_MEDIA_VIDEO android.permission.READ_MEDIA_AUDIO android.permission.READ_MEDIA_VISUAL_USER_SELECTED android.permission.READ_EXTERNAL_STORAGE android.permission.WRITE_EXTERNAL_STORAGE android.permission.BLUETOOTH_CONNECT android.permission.BLUETOOTH_SCAN android.permission.BLUETOOTH_ADVERTISE android.permission.READ_CONTACTS android.permission.READ_CALL_LOG android.permission.WRITE_CALL_LOG android.permission.READ_PHONE_STATE android.permission.CALL_PHONE android.permission.ANSWER_PHONE_CALLS android.permission.READ_SMS android.permission.RECEIVE_SMS android.permission.SEND_SMS android.permission.READ_CALENDAR android.permission.WRITE_CALENDAR android.permission.ACTIVITY_RECOGNITION android.permission.NEARBY_WIFI_DEVICES android.permission.WRITE_SECURE_SETTINGS android.permission.WRITE_GLOBAL_SETTINGS android.permission.WRITE_SETTINGS android.permission.PACKAGE_USAGE_STATS android.permission.MANAGE_EXTERNAL_STORAGE android.permission.REQUEST_INSTALL_PACKAGES android.permission.INSTALL_PACKAGES android.permission.DELETE_PACKAGES android.permission.REQUEST_DELETE_PACKAGES android.permission.QUERY_ALL_PACKAGES android.permission.INTERNET android.permission.ACCESS_NETWORK_STATE android.permission.RECEIVE_BOOT_COMPLETED android.permission.FOREGROUND_SERVICE android.permission.FOREGROUND_SERVICE_CAMERA android.permission.FOREGROUND_SERVICE_MICROPHONE android.permission.MODIFY_AUDIO_SETTINGS android.permission.MEDIA_CONTENT_CONTROL android.permission.SYSTEM_ALERT_WINDOW android.permission.KILL_BACKGROUND_PROCESSES android.permission.DUMP android.permission.READ_LOGS geely.oneos.permission.SERVICE android.car.permission.CAR_INFO android.car.permission.CAR_VENDOR_EXTENSION android.car.permission.CAR_CONTROL_AUDIO_VOLUME android.car.permission.GET_CAR_VENDOR_CATEGORY_DOOR android.car.permission.SET_CAR_VENDOR_CATEGORY_DOOR android.car.permission.GET_CAR_VENDOR_CATEGORY_SEAT android.car.permission.SET_CAR_VENDOR_CATEGORY_SEAT android.car.permission.GET_CAR_VENDOR_CATEGORY_MIRROR android.car.permission.SET_CAR_VENDOR_CATEGORY_MIRROR android.car.permission.GET_CAR_VENDOR_CATEGORY_INFO android.car.permission.SET_CAR_VENDOR_CATEGORY_INFO android.car.permission.GET_CAR_VENDOR_CATEGORY_HVAC android.car.permission.SET_CAR_VENDOR_CATEGORY_HVAC android.car.permission.GET_CAR_VENDOR_CATEGORY_LIGHT android.car.permission.SET_CAR_VENDOR_CATEGORY_LIGHT ecarx.car.permission.CAR_HVAC"

adb_shell_stdin() {
    command_text="$1"
    printf '%s\nexit\n' "$command_text" | adb shell
}

grant_permission_for_pkg() {
    pkg="$1"
    permission="$2"
    adb shell pm grant "$pkg" "$permission" 2>/dev/null || true
    adb shell pm grant --user 0 "$pkg" "$permission" 2>/dev/null || true
}

set_appop_for_pkg() {
    pkg="$1"
    op="$2"
    mode="$3"
    adb shell appops set "$pkg" "$op" "$mode" 2>/dev/null || true
    adb shell appops set --user 0 "$pkg" "$op" "$mode" 2>/dev/null || true
}

append_secure_setting() {
    key="$1"
    value="$2"
    current="$(adb shell settings get secure "$key" 2>/dev/null | tr -d '\r')"
    case "$current" in
        *"$value"*) return 0 ;;
        null|"") adb shell settings put secure "$key" "$value" 2>/dev/null || true ;;
        *) adb shell settings put secure "$key" "$current:$value" 2>/dev/null || true ;;
    esac
}

restart_launchers() {
    for launcher in com.android.launcher3 com.geely.launcher3 com.geely.oneos.launcher
    do
        adb shell am force-stop "$launcher" 2>/dev/null || true
    done
}

is_pkg_installed() {
    pkg="$1"
    adb shell pm path "$pkg" >/dev/null 2>&1
}

grant_requested_permissions_for_pkg() {
    pkg="$1"
    adb shell dumpsys package "$pkg" 2>/dev/null \
        | sed -n '/requested permissions:/,/install permissions:/p' \
        | tr -d '\r' \
        | while IFS= read -r line; do
            perm="$(printf '%s' "$line" | xargs)"
            case "$perm" in
                ""|"requested permissions:"|"install permissions:") continue ;;
            esac
            grant_permission_for_pkg "$pkg" "$perm"
        done
}

apply_wifi_toolkit_pm_grants() {
    pkg="$1"
    for perm in $WIFI_TOOLKIT_PM_GRANTS
    do
        grant_permission_for_pkg "$pkg" "$perm"
    done
}

apply_allow_max_appops() {
    pkg="$1"
    for op in $APPOPS_ALLOW_MAX
    do
        set_appop_for_pkg "$pkg" "$op" allow
    done
}

apply_common_power_network_settings() {
    pkg="$1"
    adb shell cmd deviceidle whitelist +"$pkg" 2>/dev/null || true
    adb shell cmd power whitelist-add "$pkg" 2>/dev/null || true
    adb shell settings put secure location_mode 3 2>/dev/null || true
    adb shell settings put secure location_providers_allowed +gps,+network 2>/dev/null || true
    adb shell svc wifi enable 2>/dev/null || true
    adb shell settings put global wifi_on 1 2>/dev/null || true
    adb shell cmd wifi set-wifi-enabled enabled 2>/dev/null || true
    adb shell settings put global wifi_sleep_policy 2 2>/dev/null || true
    adb shell settings put global device_provisioned 1 2>/dev/null || true
    adb shell settings put global geely_device_provisioned 1 2>/dev/null || true
}

apply_accessibility_and_listeners() {
    pkg="$1"
    append_secure_setting enabled_accessibility_services "$pkg/com.prodject.gflow.AppWatchdogAccessibilityService"
    append_secure_setting enabled_accessibility_services "$pkg/com.prodject.gflow.GFlowMediaSessionListener"
    append_secure_setting enabled_accessibility_services "$pkg/com.prodject.gflow.ClusterBridgeService"
    append_secure_setting enabled_notification_listeners "$pkg/com.prodject.gflow.AppWatchdogAccessibilityService"
    append_secure_setting enabled_notification_listeners "$pkg/com.prodject.gflow.GFlowMediaSessionListener"
    append_secure_setting enabled_notification_listeners "$pkg/com.prodject.gflow.ClusterBridgeService"
    adb shell settings put secure accessibility_enabled 1 2>/dev/null || true
    adb shell cmd notification allow_listener "$pkg/com.prodject.gflow.GFlowMediaSessionListener" 2>/dev/null || true
    adb shell cmd notification allow_listener "$pkg/com.prodject.gflow.AppWatchdogAccessibilityService" 2>/dev/null || true
    adb shell cmd notification allow_listener "$pkg/com.prodject.gflow.ClusterBridgeService" 2>/dev/null || true
}

grant_main_pkg_profile() {
    pkg="$1"
    for permission in $BASE_PM_GRANTS
    do
        grant_permission_for_pkg "$pkg" "$permission"
    done
    apply_wifi_toolkit_pm_grants "$pkg"
    apply_allow_max_appops "$pkg"
    for op in GET_USAGE_STATS RUN_IN_BACKGROUND START_FOREGROUND REQUEST_INSTALL_PACKAGES SYSTEM_ALERT_WINDOW WRITE_SETTINGS POST_NOTIFICATION
    do
        set_appop_for_pkg "$pkg" "$op" allow
    done
    adb shell cmd role add-role-holder android.app.role.SYSTEM_AUTOMATION "$pkg" 2>/dev/null || true
    apply_common_power_network_settings "$pkg"
    adb shell settings put global package_verifier_enable 0 2>/dev/null || true
    apply_accessibility_and_listeners "$pkg"
}

grant_satellite_pkg() {
    pkg="$1"
    is_pkg_installed "$pkg" || return 0
    apply_wifi_toolkit_pm_grants "$pkg"
    apply_allow_max_appops "$pkg"
    grant_requested_permissions_for_pkg "$pkg"
    apply_common_power_network_settings "$pkg"
}

if [ "${1:-}" = "--replace-incompatible" ]; then
    REINSTALL_ON_SIGNATURE_MISMATCH=1
    APK="${2:-}"
fi

if [ -z "$APK" ]; then
    APK=$(ls -1t ./GFlow-*.apk 2>/dev/null | head -n 1 || true)
fi

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
    echo "APK not found. Put GFlow-*.apk in the current directory or pass a path:" >&2
    echo "./install.sh /path/to/GFlow-1.X.apk" >&2
    echo "./install.sh --replace-incompatible /path/to/GFlow-1.X.apk" >&2
    exit 1
fi

if [ -z "$HELPER" ]; then
    HELPER=$(ls -1t ./gflow-installer*.dex ./build/installer-helper/gflow-installer.dex 2>/dev/null | head -n 1 || true)
fi

echo "Waiting for the short ADB window..."
adb wait-for-device

echo "Uploading $(basename "$APK")..."
adb push -q "$APK" "$REMOTE_APK"

if [ -n "$HELPER" ] && [ -f "$HELPER" ]; then
    echo "Uploading install helper $(basename "$HELPER")..."
    adb push -q "$HELPER" "$REMOTE_HELPER"
    echo "Installing GFlow through PackageInstaller helper..."
    set +e
    INSTALL_OUTPUT=$(adb_shell_stdin "CLASSPATH=$REMOTE_HELPER app_process /system/bin $HELPER_CLASS $REMOTE_APK" 2>&1)
    INSTALL_STATUS=$?
    set -e
else
    echo "Install helper not found. Falling back to pm install."
    set +e
    INSTALL_OUTPUT=$(adb shell pm install --user 0 -r -d -g "$REMOTE_APK" 2>&1)
    INSTALL_STATUS=$?
    set -e
fi

if [ "$INSTALL_STATUS" -ne 0 ]; then
    printf '%s\n' "$INSTALL_OUTPUT" >&2
    if printf '%s\n' "$INSTALL_OUTPUT" | grep -q "INSTALL_FAILED_UPDATE_INCOMPATIBLE"; then
        echo "The installed GFlow package is signed with a different key." >&2
        echo "Android cannot update it in place; uninstall it first, then install this APK." >&2
        if [ "$REINSTALL_ON_SIGNATURE_MISMATCH" -eq 1 ]; then
            echo "Replacing the incompatible installed package..."
            adb shell pm clear --user 0 "$PACKAGE" >/dev/null 2>&1 || true
            adb shell pm uninstall --user 0 "$PACKAGE" >/dev/null 2>&1 || adb shell pm uninstall "$PACKAGE" >/dev/null 2>&1 || true
            if [ -n "$HELPER" ] && [ -f "$HELPER" ]; then
                adb_shell_stdin "CLASSPATH=$REMOTE_HELPER app_process /system/bin $HELPER_CLASS $REMOTE_APK"
            else
                adb shell pm install --user 0 -r -d -g "$REMOTE_APK"
            fi
        else
            echo "Run uninstall for $PACKAGE, then run ./install.sh again." >&2
            echo "Or run: ./install.sh --replace-incompatible \"$APK\"" >&2
            adb shell rm "$REMOTE_APK" 2>/dev/null || true
            adb shell rm "$REMOTE_HELPER" 2>/dev/null || true
            exit "$INSTALL_STATUS"
        fi
    else
        adb shell rm "$REMOTE_APK" 2>/dev/null || true
        adb shell rm "$REMOTE_HELPER" 2>/dev/null || true
        exit "$INSTALL_STATUS"
    fi
fi

echo "Waiting for package registration..."
PACKAGE_READY=0
for _ in 1 2 3 4 5 6 7 8 9 10; do
    if adb shell pm path "$PACKAGE" >/dev/null 2>&1; then
        PACKAGE_READY=1
        break
    fi
    sleep 1
done

if [ "$PACKAGE_READY" -ne 1 ]; then
    echo "Package was not registered after install request. Check device PackageInstaller logs." >&2
    adb shell rm "$REMOTE_APK" 2>/dev/null || true
    adb shell rm "$REMOTE_HELPER" 2>/dev/null || true
    exit 1
fi

grant_main_pkg_profile "$PACKAGE"

for satellite in $SATELLITE_PACKAGES
do
    grant_satellite_pkg "$satellite"
done

restart_launchers

adb shell rm "$REMOTE_APK" 2>/dev/null || true
adb shell rm "$REMOTE_HELPER" 2>/dev/null || true

echo "GFlow installed. Launching..."
adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
