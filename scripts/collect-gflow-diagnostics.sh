#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.prodject.gflow"
ACTION="com.prodject.gflow.RUN_AUTODIAGNOSTICS"
REMOTE_DIR="/sdcard/Android/data/${PACKAGE}/files/Documents/GFlow"
OUT_ROOT="${1:-diagnostics_runs}"
STAMP="$(date +%Y%m%d-%H%M%S)"
SESSION_DIR="${OUT_ROOT}/gflow-diag-${STAMP}"
mkdir -p "${SESSION_DIR}"

command -v adb >/dev/null 2>&1 || {
  echo "Missing command: adb" >&2
  exit 1
}

echo "[1/5] wait-for-device"
adb wait-for-device

echo "[2/5] clear logcat"
adb logcat -c >/dev/null 2>&1 || true
adb logcat -b crash -c >/dev/null 2>&1 || true

echo "[3/5] trigger diagnostics sweep"
adb shell am broadcast \
  -a "${ACTION}" \
  --ez include_writes true \
  --es reason "adb-script" >/dev/null

echo "[4/5] wait for diagnostic marker"
for _ in $(seq 1 60); do
  if adb logcat -d -s GFlowDiagnostics:I 2>/dev/null | grep -q "removableSd="; then
    break
  fi
  sleep 2
done
adb logcat -d -v threadtime > "${SESSION_DIR}/logcat.txt" 2>&1 || true

echo "[5/5] pull removable SD report"
adb shell "ls -1 ${REMOTE_DIR}" > "${SESSION_DIR}/remote-files.txt" 2>&1 || true
latest_remote="$(adb shell "ls -1t ${REMOTE_DIR}/gflow-diagnostics-*.txt 2>/dev/null | head -n 1" | tr -d '\r')"
if [[ -n "${latest_remote}" ]]; then
  adb pull "${latest_remote}" "${SESSION_DIR}/" >/dev/null
  echo "Pulled: ${latest_remote}"
else
  echo "No removable SD report found. Check SD mount and app permissions." >&2
fi

echo "Saved session: ${SESSION_DIR}"
