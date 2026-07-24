$ErrorActionPreference = "Stop"

$Package = "com.prodject.gflow"
$RemoteApk = "/data/local/tmp/GFlow.apk"
$RemoteHelper = "/data/local/tmp/installer.dex"
$HelperClass = "Installer"

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

function Grant-Permission {
    param([string]$Permission)
    & adb shell pm grant $Package $Permission 2>$null | Out-Null
}

function Set-AppOp {
    param([string]$Op, [string]$Mode)
    & adb shell appops set $Package $Op $Mode 2>$null | Out-Null
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

$apk = if ($args.Length -gt 0) { $args[0] } else { Get-ChildItem -Path . -Filter "GFlow-*.apk" | Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName }
if (-not $apk -or -not (Test-Path $apk)) {
    throw "APK not found. Put GFlow-*.apk in the current directory or pass a path."
}

$helper = $env:GFLOW_INSTALL_HELPER
if (-not $helper) {
    $candidate = @(
        ".\gflow-installer.dex",
        ".\build\installer-helper\gflow-installer.dex"
    ) | Where-Object { Test-Path $_ } | Select-Object -First 1
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

$grants = @(
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
    "android.permission.WRITE_SETTINGS",
    "android.permission.PACKAGE_USAGE_STATS",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.REQUEST_DELETE_PACKAGES",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_CAMERA",
    "android.permission.FOREGROUND_SERVICE_MICROPHONE",
    "android.car.permission.CAR_INFO",
    "android.car.permission.CAR_VENDOR_EXTENSION",
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

foreach ($grant in $grants) {
    Grant-Permission $grant
}

Set-AppOp MANAGE_EXTERNAL_STORAGE allow
Set-AppOp REQUEST_INSTALL_PACKAGES allow
Set-AppOp SYSTEM_ALERT_WINDOW allow
Set-AppOp WRITE_SETTINGS allow
Set-AppOp GET_USAGE_STATS allow
Set-AppOp POST_NOTIFICATION allow

& adb shell cmd role add-role-holder android.app.role.SYSTEM_AUTOMATION $Package 2>$null | Out-Null
& adb shell settings put global package_verifier_enable 0 2>$null | Out-Null
& adb shell settings put secure accessibility_enabled 1 2>$null | Out-Null
Append-SecureSetting enabled_accessibility_services "$Package/com.prodject.gflow.AppWatchdogAccessibilityService"
Append-SecureSetting enabled_accessibility_services "$Package/com.prodject.gflow.GFlowMediaSessionListener"
Append-SecureSetting enabled_accessibility_services "$Package/com.prodject.gflow.ClusterBridgeService"
Append-SecureSetting enabled_notification_listeners "$Package/com.prodject.gflow.AppWatchdogAccessibilityService"
Append-SecureSetting enabled_notification_listeners "$Package/com.prodject.gflow.GFlowMediaSessionListener"
Append-SecureSetting enabled_notification_listeners "$Package/com.prodject.gflow.ClusterBridgeService"

& adb shell rm $RemoteApk 2>$null | Out-Null
& adb shell rm $RemoteHelper 2>$null | Out-Null

Write-Host "GFlow installed. Launching..."
& adb shell monkey -p $Package -c android.intent.category.LAUNCHER 1 2>$null | Out-Null
