$ErrorActionPreference = "Stop"

$Package = "com.prodject.gflow"
$RemoteApk = "/data/local/tmp/GFlow.apk"
$RemoteHelper = "/data/local/tmp/installer.dex"
$HelperClass = "Installer"
$SatellitePackages = @("com.maxinf.car", "org.kman.WifiManager", "com.salat.gbinder")
$WifiToolkitPmGrants = @(
    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
    "android.permission.WRITE_SECURE_SETTINGS",
    "android.permission.WRITE_GLOBAL_SETTINGS",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.ACCESS_SURFACE_FLINGER",
    "android.permission.WRITE_SETTINGS",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.MODIFY_PHONE_STATE",
    "android.permission.DUMP",
    "android.permission.CHANGE_CONFIGURATION",
    "android.permission.READ_LOGS",
    "android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER",
    "android.permission.INSTALL_PACKAGES",
    "android.permission.DELETE_PACKAGES",
    "android.permission.FORCE_STOP_PACKAGES",
    "android.permission.GET_TASKS",
    "android.permission.REAL_GET_TASKS",
    "android.permission.MANAGE_ACTIVITY_TASKS",
    "android.permission.REORDER_TASKS",
    "android.permission.STATUS_BAR_SERVICE",
    "android.permission.EXPAND_STATUS_BAR",
    "android.permission.UPDATE_DEVICE_STATS",
    "android.permission.BLUETOOTH_PRIVILEGED",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.READ_INTERNAL_STORAGE",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.READ_MEDIA_IMAGES",
    "android.permission.READ_MEDIA_VIDEO",
    "android.permission.READ_MEDIA_AUDIO",
    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
    "android.permission.READ_PHONE_STATE",
    "android.permission.CALL_PHONE",
    "android.permission.READ_CONTACTS",
    "android.permission.GET_ACCOUNTS",
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.NFC",
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION",
    "android.permission.ENFORCE_UPDATE_OWNERSHIP",
    "android.permission.TURN_SCREEN_ON"
)
$AppOpsAllowMax = @(
    "POST_NOTIFICATION",
    "START_FOREGROUND",
    "RUN_IN_BACKGROUND",
    "RUN_ANY_IN_BACKGROUND",
    "REQUEST_INSTALL_PACKAGES",
    "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
    "SYSTEM_ALERT_WINDOW",
    "WRITE_SETTINGS",
    "READ_LOGS",
    "SCHEDULE_EXACT_ALARM",
    "USE_FULL_SCREEN_INTENT",
    "TURN_SCREEN_ON",
    "WAKE_LOCK",
    "GET_USAGE_STATS",
    "ACCESS_BACKGROUND_LOCATION"
)
$BasePmGrants = @(
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.READ_MEDIA_IMAGES",
    "android.permission.READ_MEDIA_VIDEO",
    "android.permission.READ_MEDIA_AUDIO",
    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.BLUETOOTH_SCAN",
    "android.permission.BLUETOOTH_ADVERTISE",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.READ_PHONE_STATE",
    "android.permission.CALL_PHONE",
    "android.permission.ANSWER_PHONE_CALLS",
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.SEND_SMS",
    "android.permission.READ_CALENDAR",
    "android.permission.WRITE_CALENDAR",
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.NEARBY_WIFI_DEVICES",
    "android.permission.WRITE_SECURE_SETTINGS",
    "android.permission.WRITE_GLOBAL_SETTINGS",
    "android.permission.WRITE_SETTINGS",
    "android.permission.PACKAGE_USAGE_STATS",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.INSTALL_PACKAGES",
    "android.permission.DELETE_PACKAGES",
    "android.permission.REQUEST_DELETE_PACKAGES",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_CAMERA",
    "android.permission.FOREGROUND_SERVICE_MICROPHONE",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.MEDIA_CONTENT_CONTROL",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.KILL_BACKGROUND_PROCESSES",
    "android.permission.DUMP",
    "android.permission.READ_LOGS",
    "geely.oneos.permission.SERVICE",
    "android.car.permission.CAR_INFO",
    "android.car.permission.CAR_VENDOR_EXTENSION",
    "android.car.permission.CAR_CONTROL_AUDIO_VOLUME",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_DOOR",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_DOOR",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_SEAT",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_SEAT",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_MIRROR",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_MIRROR",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_INFO",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_INFO",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_HVAC",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_HVAC",
    "android.car.permission.GET_CAR_VENDOR_CATEGORY_LIGHT",
    "android.car.permission.SET_CAR_VENDOR_CATEGORY_LIGHT",
    "ecarx.car.permission.CAR_HVAC"
)

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & adb @Args
}

function Invoke-AdbShellCommand {
    param([string]$Command)
    $tmp = [System.IO.Path]::GetTempFileName()
    try {
        [System.IO.File]::WriteAllText($tmp, "$Command`nexit`n")
        Get-Content $tmp | & adb shell
    } finally {
        Remove-Item $tmp -ErrorAction SilentlyContinue
    }
}

function Grant-PermissionForPkg {
    param([string]$Pkg, [string]$Permission)
    & adb shell pm grant $Pkg $Permission 2>$null | Out-Null
    & adb shell pm grant --user 0 $Pkg $Permission 2>$null | Out-Null
}

function Set-AppOpForPkg {
    param([string]$Pkg, [string]$Op, [string]$Mode)
    & adb shell appops set $Pkg $Op $Mode 2>$null | Out-Null
    & adb shell appops set --user 0 $Pkg $Op $Mode 2>$null | Out-Null
}

function Append-SecureSetting {
    param([string]$Key, [string]$Value)
    $current = (& adb shell settings get secure $Key 2>$null | Out-String).Trim()
    if (-not $current -or $current -eq "null") {
        & adb shell settings put secure $Key $Value 2>$null | Out-Null
    } elseif ($current -notlike "*$Value*") {
        & adb shell settings put secure $Key "$current`:$Value" 2>$null | Out-Null
    }
}

function Restart-Launchers {
    foreach ($launcher in @("com.android.launcher3", "com.geely.launcher3", "com.geely.oneos.launcher")) {
        & adb shell am force-stop $launcher 2>$null | Out-Null
    }
}

function Test-PackageInstalled {
    param([string]$Pkg)
    & adb shell pm path $Pkg 2>$null | Select-String "package:" -Quiet
}

function Apply-WifiToolkitPmGrants {
    param([string]$Pkg)
    foreach ($perm in $WifiToolkitPmGrants) {
        Grant-PermissionForPkg $Pkg $perm
    }
}

function Apply-AllowMaxAppOps {
    param([string]$Pkg)
    foreach ($op in $AppOpsAllowMax) {
        Set-AppOpForPkg $Pkg $op allow
    }
}

function Grant-RequestedPermissionsForPkg {
    param([string]$Pkg)
    $dump = & adb shell dumpsys package $Pkg 2>$null | Out-String
    $capture = $false
    foreach ($line in ($dump -split "`n")) {
        $trimmed = $line.Trim()
        if ($trimmed -eq "requested permissions:") {
            $capture = $true
            continue
        }
        if ($capture -and $trimmed -eq "install permissions:") {
            break
        }
        if ($capture -and $trimmed) {
            Grant-PermissionForPkg $Pkg $trimmed
        }
    }
}

function Apply-CommonPowerNetworkSettings {
    param([string]$Pkg)
    & adb shell cmd deviceidle whitelist "+$Pkg" 2>$null | Out-Null
    & adb shell cmd power whitelist-add $Pkg 2>$null | Out-Null
    & adb shell settings put secure location_mode 3 2>$null | Out-Null
    & adb shell settings put secure location_providers_allowed +gps,+network 2>$null | Out-Null
    & adb shell svc wifi enable 2>$null | Out-Null
    & adb shell settings put global wifi_on 1 2>$null | Out-Null
    & adb shell cmd wifi set-wifi-enabled enabled 2>$null | Out-Null
    & adb shell settings put global wifi_sleep_policy 2 2>$null | Out-Null
    & adb shell settings put global device_provisioned 1 2>$null | Out-Null
    & adb shell settings put global geely_device_provisioned 1 2>$null | Out-Null
}

function Apply-AccessibilityAndListeners {
    param([string]$Pkg)
    Append-SecureSetting enabled_accessibility_services "$Pkg/com.prodject.gflow.AppWatchdogAccessibilityService"
    Append-SecureSetting enabled_accessibility_services "$Pkg/com.prodject.gflow.GFlowMediaSessionListener"
    Append-SecureSetting enabled_accessibility_services "$Pkg/com.prodject.gflow.ClusterBridgeService"
    Append-SecureSetting enabled_notification_listeners "$Pkg/com.prodject.gflow.AppWatchdogAccessibilityService"
    Append-SecureSetting enabled_notification_listeners "$Pkg/com.prodject.gflow.GFlowMediaSessionListener"
    Append-SecureSetting enabled_notification_listeners "$Pkg/com.prodject.gflow.ClusterBridgeService"
    & adb shell settings put secure accessibility_enabled 1 2>$null | Out-Null
    & adb shell cmd notification allow_listener "$Pkg/com.prodject.gflow.GFlowMediaSessionListener" 2>$null | Out-Null
    & adb shell cmd notification allow_listener "$Pkg/com.prodject.gflow.AppWatchdogAccessibilityService" 2>$null | Out-Null
    & adb shell cmd notification allow_listener "$Pkg/com.prodject.gflow.ClusterBridgeService" 2>$null | Out-Null
}

function Grant-MainPkgProfile {
    param([string]$Pkg)
    foreach ($perm in $BasePmGrants) {
        Grant-PermissionForPkg $Pkg $perm
    }
    Apply-WifiToolkitPmGrants $Pkg
    Apply-AllowMaxAppOps $Pkg
    foreach ($op in @("GET_USAGE_STATS", "RUN_IN_BACKGROUND", "START_FOREGROUND", "REQUEST_INSTALL_PACKAGES", "SYSTEM_ALERT_WINDOW", "WRITE_SETTINGS", "POST_NOTIFICATION")) {
        Set-AppOpForPkg $Pkg $op allow
    }
    & adb shell cmd role add-role-holder android.app.role.SYSTEM_AUTOMATION $Pkg 2>$null | Out-Null
    Apply-CommonPowerNetworkSettings $Pkg
    & adb shell settings put global package_verifier_enable 0 2>$null | Out-Null
    Apply-AccessibilityAndListeners $Pkg
}

function Grant-SatellitePkg {
    param([string]$Pkg)
    if (-not (Test-PackageInstalled $Pkg)) { return }
    Apply-WifiToolkitPmGrants $Pkg
    Apply-AllowMaxAppOps $Pkg
    Grant-RequestedPermissionsForPkg $Pkg
    Apply-CommonPowerNetworkSettings $Pkg
}

$apk = if ($args.Length -gt 0) { $args[0] } else { Get-ChildItem -Path . -Filter "GFlow-*.apk" | Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName }
if (-not $apk -or -not (Test-Path $apk)) {
    throw "APK not found. Put GFlow-*.apk in the current directory or pass a path."
}

$helper = $env:GFLOW_INSTALL_HELPER
if (-not $helper) {
    $candidate = @(".\gflow-installer.dex", ".\build\installer-helper\gflow-installer.dex") | Where-Object { Test-Path $_ } | Select-Object -First 1
    $helper = $candidate
}

Write-Host "Waiting for ADB device..."
Invoke-Adb wait-for-device | Out-Null

Write-Host "Uploading $([System.IO.Path]::GetFileName($apk))..."
Invoke-Adb push $apk $RemoteApk | Out-Null

$installOutput = ""
if ($helper -and (Test-Path $helper)) {
    Write-Host "Uploading install helper $([System.IO.Path]::GetFileName($helper))..."
    Invoke-Adb push $helper $RemoteHelper | Out-Null
    Write-Host "Installing GFlow through PackageInstaller helper..."
    $installOutput = Invoke-AdbShellCommand "CLASSPATH=$RemoteHelper app_process /system/bin $HelperClass $RemoteApk" | Out-String
} else {
    Write-Host "Install helper not found. Falling back to pm install."
    $installOutput = (& adb shell pm install --user 0 -r -d -g $RemoteApk 2>&1 | Out-String)
}

if ($LASTEXITCODE -ne 0 -or ($installOutput -match "FAIL" -or $installOutput -match "INSTALL_FAILED")) {
    Write-Output $installOutput
    throw "Install failed"
}

Start-Sleep -Seconds 2

Grant-MainPkgProfile $Package
foreach ($satellite in $SatellitePackages) {
    Grant-SatellitePkg $satellite
}

Restart-Launchers

& adb shell rm $RemoteApk 2>$null | Out-Null
& adb shell rm $RemoteHelper 2>$null | Out-Null

Write-Host "GFlow installed. Launching..."
& adb shell monkey -p $Package -c android.intent.category.LAUNCHER 1 2>$null | Out-Null
