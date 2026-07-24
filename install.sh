#!/bin/sh
set -eu

PACKAGE="com.prodject.gflow"
REMOTE_APK="/data/local/tmp/GFlow.apk"
REMOTE_HELPER="/data/local/tmp/installer.dex"
HELPER_CLASS="Installer"
REINSTALL_ON_SIGNATURE_MISMATCH=0
APK="${1:-}"
HELPER="${GFLOW_INSTALL_HELPER:-}"

adb_shell_stdin() {
    command_text="$1"
    printf '%s\nexit\n' "$command_text" | adb shell
}

grant_permission() {
    permission="$1"
    adb shell pm grant "$PACKAGE" "$permission" 2>/dev/null || true
}

set_appop() {
    op="$1"
    mode="$2"
    adb shell appops set "$PACKAGE" "$op" "$mode" 2>/dev/null || true
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

for permission in \
    android.permission.CAMERA \
    android.permission.RECORD_AUDIO \
    android.permission.ACCESS_FINE_LOCATION \
    android.permission.ACCESS_COARSE_LOCATION \
    android.permission.ACCESS_BACKGROUND_LOCATION \
    android.permission.POST_NOTIFICATIONS \
    android.permission.READ_MEDIA_IMAGES \
    android.permission.READ_MEDIA_VIDEO \
    android.permission.READ_MEDIA_AUDIO \
    android.permission.READ_MEDIA_VISUAL_USER_SELECTED \
    android.permission.READ_EXTERNAL_STORAGE \
    android.permission.WRITE_EXTERNAL_STORAGE \
    android.permission.BLUETOOTH_CONNECT \
    android.permission.BLUETOOTH_SCAN \
    android.permission.BLUETOOTH_ADVERTISE \
    android.permission.READ_CONTACTS \
    android.permission.READ_CALL_LOG \
    android.permission.WRITE_CALL_LOG \
    android.permission.READ_PHONE_STATE \
    android.permission.CALL_PHONE \
    android.permission.ANSWER_PHONE_CALLS \
    android.permission.READ_SMS \
    android.permission.RECEIVE_SMS \
    android.permission.SEND_SMS \
    android.permission.READ_CALENDAR \
    android.permission.WRITE_CALENDAR \
    android.permission.ACTIVITY_RECOGNITION \
    android.permission.NEARBY_WIFI_DEVICES \
    android.permission.WRITE_SECURE_SETTINGS \
    android.permission.WRITE_SETTINGS \
    android.permission.PACKAGE_USAGE_STATS \
    android.permission.MANAGE_EXTERNAL_STORAGE \
    android.permission.REQUEST_INSTALL_PACKAGES \
    android.permission.REQUEST_DELETE_PACKAGES \
    android.permission.QUERY_ALL_PACKAGES \
    android.permission.INTERNET \
    android.permission.ACCESS_NETWORK_STATE \
    android.permission.RECEIVE_BOOT_COMPLETED \
    android.permission.FOREGROUND_SERVICE \
    android.permission.FOREGROUND_SERVICE_CAMERA \
    android.permission.FOREGROUND_SERVICE_MICROPHONE \
    android.car.permission.CAR_INFO \
    android.car.permission.CAR_VENDOR_EXTENSION \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_DOOR \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_DOOR \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_SEAT \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_SEAT \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_MIRROR \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_MIRROR \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_INFO \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_INFO \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_HVAC \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_HVAC \
    android.car.permission.GET_CAR_VENDOR_CATEGORY_LIGHT \
    android.car.permission.SET_CAR_VENDOR_CATEGORY_LIGHT \
    ecarx.car.permission.CAR_HVAC
do
    grant_permission "$permission"
done

set_appop MANAGE_EXTERNAL_STORAGE allow
set_appop REQUEST_INSTALL_PACKAGES allow
set_appop SYSTEM_ALERT_WINDOW allow
set_appop WRITE_SETTINGS allow
set_appop GET_USAGE_STATS allow
set_appop POST_NOTIFICATION allow

adb shell cmd role add-role-holder android.app.role.SYSTEM_AUTOMATION "$PACKAGE" 2>/dev/null || true
adb shell settings put global package_verifier_enable 0 2>/dev/null || true
adb shell settings put secure accessibility_enabled 1 2>/dev/null || true
append_secure_setting enabled_accessibility_services "$PACKAGE/com.prodject.gflow.AppWatchdogAccessibilityService"
append_secure_setting enabled_accessibility_services "$PACKAGE/com.prodject.gflow.GFlowMediaSessionListener"
append_secure_setting enabled_accessibility_services "$PACKAGE/com.prodject.gflow.ClusterBridgeService"
append_secure_setting enabled_notification_listeners "$PACKAGE/com.prodject.gflow.AppWatchdogAccessibilityService"
append_secure_setting enabled_notification_listeners "$PACKAGE/com.prodject.gflow.GFlowMediaSessionListener"
append_secure_setting enabled_notification_listeners "$PACKAGE/com.prodject.gflow.ClusterBridgeService"

adb shell rm "$REMOTE_APK" 2>/dev/null || true
adb shell rm "$REMOTE_HELPER" 2>/dev/null || true

echo "GFlow installed. Launching..."
adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
