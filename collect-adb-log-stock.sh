#!/usr/bin/env bash
set -euo pipefail

OUTPUT_DIR="${1:-./logs_stock}"
NO_CLEAR="${NO_CLEAR:-0}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Ошибка: команда '$1' не найдена." >&2
    exit 1
  fi
}

require_command adb
require_command zip

devices=()
while IFS= read -r line; do
  [[ -n "$line" ]] && devices+=("$line")
done < <(adb devices | tail -n +2 | awk '$2 == "device" { print $1 }')

if [[ ${#devices[@]} -eq 0 ]]; then
  echo "Ошибка: ADB-устройство не найдено." >&2
  echo "Проверь USB debugging и выполни: adb devices" >&2
  exit 1
fi

if [[ ${#devices[@]} -gt 1 && -z "${ANDROID_SERIAL:-}" ]]; then
  echo "Ошибка: подключено несколько устройств." >&2
  echo "Задай нужное устройство так:" >&2
  echo "ANDROID_SERIAL=<serial> ./collect-adb-log-stock.sh" >&2
  exit 1
fi

choose_package() {
  local packages=(
    "*|Без фильтра пакета"
    "com.android.settings|Android Settings"
    "com.android.car.settings|Car Settings"
    "com.ecarx.settings|Ecarx Settings"
    "com.ecarx.xui.settings|Ecarx XUI Settings"
    "com.zeekr.settings|Zeekr Settings"
    "com.geely.settings|Geely Settings"
    "com.geely.hvac|Geely HVAC"
    "com.desaysv.engmode|Desay Engineering Mode"
    "com.android.permissioncontroller|Android Permission Controller"
    "com.geely.vrmiddlewareservice|Geely VR Middleware Service"
    "com.geely.scene.engine|Geely Scene Engine"
    "com.geely.service.cloud|Geely Cloud Service"
    "com.desaysv.permission.service|Desay Permission Service"
    "com.iflytek.cutefly.speechclient.hmi|iFlytek Speech Client HMI"
    "com.iflytek.autofly.mediax|iFlytek MediaX"
    "com.astrob.turbodog|Astrob TurboDog Navi"
    "custom|Ввести пакет вручную"
  )
  echo "Выбери пакет для отслеживания:" >&2
  local i=1
  for item in "${packages[@]}"; do
    echo "  ${i}. ${item#*|} [${item%%|*}]" >&2
    i=$((i + 1))
  done
  local choice raw selected
  while true; do
    read -r -p "Номер пакета: " choice </dev/tty
    if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#packages[@]} )); then
      selected="${packages[$((choice - 1))]}"
      raw="${selected%%|*}"
      if [[ "$raw" == "custom" ]]; then
        read -r -p "Введи package name: " raw </dev/tty
      fi
      echo "$raw"
      return
    fi
    echo "Неверный выбор." >&2
  done
}

PACKAGE="${PACKAGE:-$(choose_package)}"

timestamp="$(date '+%Y%m%d-%H%M%S')"
safe_package="$(printf '%s' "$PACKAGE" | sed 's/\*/all/g; s/[^A-Za-z0-9._-]/_/g; s/[.]/_/g')"
session_dir="${OUTPUT_DIR}/stock-${safe_package}-${timestamp}"
mkdir -p "$session_dir"

main_log="${session_dir}/logcat-full.txt"
crash_log="${session_dir}/logcat-crash.txt"
events_log="${session_dir}/logcat-events.txt"
filtered_log="${session_dir}/stock-filtered.txt"
device_info="${session_dir}/device-info.txt"
package_info="${session_dir}/package-info.txt"
stderr_log="${session_dir}/logcat-stderr.txt"
notes_log="${session_dir}/session-notes.txt"

{
  echo "Timestamp: $(date -Iseconds)"
  echo "ADB serial: $(adb get-serialno 2>&1 || true)"
  echo "Tracked package: ${PACKAGE}"
  echo
  echo "=== getprop ==="
  adb shell getprop 2>&1 || true
  echo
  echo "=== services ==="
  adb shell service list 2>&1 || true
} > "$device_info"

{
  echo "=== dumpsys package ${PACKAGE} ==="
  if [[ "$PACKAGE" != "*" ]]; then
    adb shell dumpsys package "$PACKAGE" 2>&1 || true
  else
    echo "package filter disabled"
  fi
  echo
  echo "=== pidof ${PACKAGE} ==="
  if [[ "$PACKAGE" != "*" ]]; then
    adb shell pidof "$PACKAGE" 2>&1 || true
  else
    echo "package filter disabled"
  fi
  echo
  echo "=== appops ==="
  if [[ "$PACKAGE" != "*" ]]; then
    adb shell appops get "$PACKAGE" 2>&1 || true
  else
    echo "package filter disabled"
  fi
} > "$package_info"

{
  echo "Session started: $(date -Iseconds)"
  echo "Tracked package: ${PACKAGE}"
  echo
  echo "Комментарии пользователя:"
} > "$notes_log"

if [[ "$NO_CLEAR" != "1" ]]; then
  adb logcat -c >/dev/null 2>&1 || true
  adb logcat -b crash -c >/dev/null 2>&1 || true
  adb logcat -b events -c >/dev/null 2>&1 || true
fi

echo
echo "Логирование запущено для пакета: ${PACKAGE}"
echo "Открой системное приложение и меняй функции."
echo "Команды в этой консоли:"
echo "  Enter        -> остановить лог"
echo "  :note текст  -> добавить комментарий в session-notes.txt"
echo "  :mark текст  -> то же самое"
echo

adb logcat -b main -b system -b radio -v threadtime > "$main_log" 2> "$stderr_log" &
logcat_pid=$!

cleanup() {
  if kill -0 "$logcat_pid" >/dev/null 2>&1; then
    kill "$logcat_pid" >/dev/null 2>&1 || true
    wait "$logcat_pid" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

while true; do
  IFS= read -r line || true
  if [[ -z "${line:-}" ]]; then
    break
  fi
  if [[ "$line" == :note\ * || "$line" == :mark\ * ]]; then
    note="${line#*:note }"
    if [[ "$line" == :mark\ * ]]; then
      note="${line#*:mark }"
    fi
    printf '[%s] Введена функция: %s\n' "$(date '+%H:%M:%S')" "$note" | tee -a "$notes_log"
  else
    printf '[%s] Введена функция: %s\n' "$(date '+%H:%M:%S')" "$line" | tee -a "$notes_log"
  fi
done

cleanup
trap - EXIT INT TERM

adb logcat -b crash -d -v threadtime > "$crash_log" 2>&1 || true
adb logcat -b events -d -v threadtime > "$events_log" 2>&1 || true

if [[ "$PACKAGE" == "*" ]]; then
  grep -Ei \
    'Settings|SystemUI|Ecarx|ECARX|Geely|Desay|AdaptAPI|ICarFunction|CarService|Vehicle|HVAC|BCM|ADAS|PAS|APA|AVM|SecurityException|Permission Denial|FATAL EXCEPTION|AndroidRuntime|DeadObjectException|RemoteException|setFunctionValue|getFunctionValue|setCustomizeFunctionValue|getCustomizeFunctionValue|isFunctionSupported|status:|result:false|result:true|0xff|0xdd' \
    "$main_log" > "$filtered_log" || true
else
  grep -Ei \
    "${PACKAGE}|Settings|SystemUI|Ecarx|ECARX|Geely|Desay|AdaptAPI|ICarFunction|CarService|Vehicle|HVAC|BCM|ADAS|PAS|APA|AVM|SecurityException|Permission Denial|FATAL EXCEPTION|AndroidRuntime|DeadObjectException|RemoteException|setFunctionValue|getFunctionValue|setCustomizeFunctionValue|getCustomizeFunctionValue|isFunctionSupported|status:|result:false|result:true|0xff|0xdd" \
    "$main_log" > "$filtered_log" || true
fi

zip_path="${session_dir}.zip"
(
  cd "$OUTPUT_DIR"
  zip -qr "$(basename "$zip_path")" "$(basename "$session_dir")"
)

echo
echo "Готово:"
echo "Пакет:          $PACKAGE"
echo "Полный лог:     $main_log"
echo "Фильтрованный:  $filtered_log"
echo "Комментарии:    $notes_log"
echo "Крэши:          $crash_log"
echo "Архив:          $zip_path"
