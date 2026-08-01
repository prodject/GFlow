# Agent Changelog

## 2026-07-30

### Drive / Steering / Assistants UI

- Added a stock `Drive / Steering` card in `VehicleActivity` for `0x22010100`, `0x20070800`, and `0x22040300`.
- Drive mode UI now exposes stock-confirmed quick actions plus a full selector backed by catalog/runtime supported values.
- Steering UI now uses `VEHICLE_STEERING_ASSISTANCE_LEVEL [0x20070800]` instead of relying only on legacy `DRIVE_STEERING_MODE`.
- Added ADAS stock-assistants block for confirmed lane/speed/rear-warning contracts: LKA, LKA warning, RCTA, RCW, speed limit warning, steering assistance, and steering sync.
- Unconfirmed PAS/experimental assistants remain behind existing experimental UI.

### Hidden Assistants Plan / Import

Plan:

1. Add safe hidden groups to the main ADAS screen: `Speed Assist`, `Lane Assist`, `Collision Assist`, `Traffic Light Assist`.
2. Use selectors for multi-value functions and runtime support checks for every write.
3. Keep AI Pilot/TLB helpers under the existing experimental gate.
4. Do not move PAS/parking flow commands into the main assistant UI until a stock flow log confirms write order and values.

Imported into UI:

- `0x200b0100` traffic sign recognition toggle.
- `0x200b0200` traffic sign alert toggle.
- `0x28060300` ACC with TSR toggle.
- `0x20030500` speed limitation mode selector.
- `0x28060200` speed warning mode selector.
- `0x28060400` speed warning offset selector.
- `0x200e0100` FCW sensitivity buttons: off/low/normal/high.
- `0x20070e00` AEB toggle.
- `0x20070600` ELKA toggle.
- `0x20070700` lane change assist enable/disable.
- `0x28081b00` paddle lane change enable/disable.
- `0x20070d00` traffic light attention toggle.
- `0x28010100` traffic light attention sound toggle.
- AI Pilot/TLB remains experimental: `0x28080100`, `0x28080200`, `0x28080300`, `0x28080400`, `0x28080500`, `0x28080600`, `0x28080700`, `0x28080b00`, `0x28080c00`.

### Diagnostics Coverage Update

- Auto diagnostics now writes the latest report to `/storage/emulated/0/gflow_data.log`.
- Removable SD export is no longer the primary diagnostics path.
- Diagnostics groups now include the full new drive/steering/hidden-assistants set.
- Each function now logs a `SUMMARY` line with `OK`, `UNSUPPORTED`, `READ_FAIL`, or `SUPPORT_ERROR`, plus the recommended action.
- Write sweep now logs `WRITE_SUMMARY` with `OK`, `WRITE_FAIL`, or `UNSUPPORTED`.
- ADAS developer diagnostics now shows hidden speed/lane/collision/traffic-light assistants and AI Pilot experimental coverage.

### Logs 1.31 Analysis

`gflow_data.log`

What really works:

- Base AdaptAPI access is available.
- Climate:
  - power/auto/ac/fan/temp/defrost/seat heat/wheel heat — OK.
  - `0x10020100` fan, zone `0x8` — OK.
  - `0x10060100` temperature float, zone `0x1` — OK.
- Drive / steering:
  - `0x22010100` drive mode — OK.
  - `0x20070800` steering assistance — OK.
  - `0x22040300` steering sync — OK.
- ADAS / assistants:
  - `0x20070e00` AEB — OK.
  - `0x20070600` ELKA — OK.
  - `0x20070d00` traffic light attention — OK.
  - `0x200b0100` traffic sign recognition — OK.
  - `0x28060200` speed warning mode — OK.
- Seats:
  - `0x2d020100` seat length — OK.
  - `0x2d020200` seat height — OK.
  - `0x2d030200` seat backrest — OK.
  - `0x2d400100` memory save — OK.
  - `0x2d400200` memory set/readback — OK, but write sweep sent the wrong value and got unsupported.

What must be disabled/hidden:

- `0x10050100` seat ventilation — unsupported.
- `0x10070100` blowing mode failed in write sweep because sweep used wrong global zone; it needs zone `0x8`.
- `0x20060300` PDC switch — unsupported through the current path.
- `0x200b0200` traffic sign alert — unsupported.
- `0x28060300` ACC with TSR — unsupported.
- `0x20030500` speed limitation mode — unsupported.
- `0x28060400` speed offset — read OK, write false.
- `0x200e0100` FCW read/support OK, but write false; needs another function/value or readback-only treatment.
- `0x20070700` lane change assist read/support OK, but write false with `0x100d01`; value is wrong for this ID.
- `0x28081b00` paddle lane change — unsupported.
- `0x28010100` traffic light sound — unsupported.

Camera 360 / stock logs:

Stock does not show a normal `PAS_* setFunctionValue` contract for 360. It shows:

- `0x2141f000`
  - `funcId 30`
  - `commandId 45/46`
  - `dataLen 2`
  - `int32value 3`
- `0x21417523`
  - raw bytes: `0c 1b 02 03 00`
- `0x214085e6`
  - value `01`
- related read/update properties:
  - `0x2141f004`
  - `0x21415116`
  - `0x2160728c`
- observed error:
  - `AvmServiceManager get avm_service fail from ServiceManager`

Conclusion: stock 360 control goes through low-level vehicle HAL props / SVP, not through the current `PAS_PAC_*` AdaptAPI functions. Therefore current `PAS_PAC_VIEW_SELECTION`, `PAS_PAC_3DVIEW_POSITION`, overlays, and similar GFlow controls are expected to be mostly unsupported.

Next 360 step:

- Add a separate `AvmHalAdapter` for these props:
  - write `0x2141f000` with `commandId 45/46`;
  - write `0x21417523` bytes;
  - write `0x214085e6 = 1`;
  - read/watch `0x2141f004`, `0x21415116`, `0x2160728c`.
- Keep current `EcarxDvrAdapter.openEvs()` as fallback.

Steering wheel buttons:

- Stock logs contain many repeats of `0x2141f000 funcId 30 commandId 45/46`.
- This looks like a shared input/command bridge.
- These logs are not enough to map each steering wheel button to a value because filtered log has no explicit button label/name.
- Need either raw full log with exact press timestamps, or GFlow timestamp markers before each manual press.

Seats:

- The UI exists in code:
  - `VehicleActivity -> buildSeatsPanel()`;
  - opened through `Mode.SEATS`.
- Driver controls:
  - forward/back;
  - up/down;
  - backrest +/-.
- Passenger controls:
  - forward/back;
  - backrest +/-.

Current problem:

- The seat UI is not obvious as a separate polished joystick UI.
- Access is hidden inside the Vehicle screen mode.
- Passenger block has no height because stock confirmed passenger length/backrest, but not height.
- There is no explicit `Driver / Passenger joystick` interface.

Required seat UI fix:

- Add a large `Seat Control` card.
- Two sections:
  - Driver;
  - Passenger.
- Driver:
  - joystick: forward/back/up/down;
  - backrest +/-;
  - memory save/set.
- Passenger:
  - joystick forward/back;
  - backrest +/-;
  - hide height until support is confirmed.
- Keep hold-to-move behavior:
  - down = direction;
  - up/cancel = `0`.

Nearest fix candidates:

- Fix diagnostics write sweep:
  - blowing mode zone must be `0x8`;
  - seat memory set value must be `0x2d400201` or stock value `1`, not `0x2d400101`.
- Hide/disable unsupported hidden assistants based on `gflow_data.log`.
- Add normal seat joystick UI.
- For 360, add a separate HAL adapter instead of trying to solve it through `PAS_*`.

Current fix pass:

- Diagnostics write sweep:
  - `0x10070100` blowing mode now uses `zone 0x8`;
  - `0x2d400200` seat memory set now uses driver zone `0x1` and stock value `1`;
  - confirmed unsupported writes were removed from sweep: `0x20060300`, `0x200b0200`, `0x28060300`, `0x20030500`, `0x28081b00`, `0x28010100`.
- Hidden Assistants UI:
  - FCW write moved to sensitivity function `0x200e0200`; `0x200e0100` stays status/readback;
  - LCA `0x20070700` now tries normal `0/1`, not `0x100d01/0x100d00`;
  - speed offset write moved to fallback `0x28062100`; `0x28060400` stays diagnostics/readback;
  - unsupported controls are status/readback buttons, not action buttons.
- Seats:
  - added Driver/Passenger joystick UI;
  - hold-to-move sends direction on down and `0` on up/cancel;
  - passenger height remains hidden until support is confirmed.
  - automation/profile seat recall also uses stock memory values `1/2/3`, not old encoded `0x2d40010x`.
- 360:
  - added separate `AvmHalAdapter` for low-level HAL props from stock logs;
  - EVS open remains fallback.

Experimental drive diagnostics pass:

- committed previous vehicle-control fixes as `a485c2f`.
- diagnostics write sweep now covers experimental drive values:
  - `0x22010100` drive mode values: Offroad, Mud, Rock, Sand, AWD, eAWD, Adaptive, Custom, Eco+, Sport+, Start Type18/72/79/97;
  - custom propulsion: Offroad, Sand, AWD;
  - custom suspension: Offroad;
  - custom steering: Heavy;
  - custom climate: Eco;
  - custom driver info: Offroad;
  - energy mode: Sport;
  - launch control: ON;
  - ESC level: 1/3/5;
  - StarTrack: Type18/72/79/97.
- diagnostics read/support groups now include the experimental drive control function IDs separately from value constants, so values are validated through write sweep output, not misread as standalone function IDs.

PDC candidate pass:

- `0x20060300` remains readback/status-only because `logs_1.31` showed it unsupported.
- Added write candidates to diagnostics sweep instead:
  - `0x23021000 PAS_RADAR_WORK_MODE` with front+rear, front, rear active values;
  - `0x23030100 PAS_PAC_ACTIVATION` with `1`.
- These PAS candidates are writable only for diagnostics; unsupported PAS/PAC view controls remain readback-only.

Catalog sweep diagnostics pass:

- Added full catalog sweep over every `CarFunctionCatalog.TYPE_FUNCTION` entry.
- For each functionId diagnostics now logs:
  - support;
  - default zone;
  - static value count;
  - readback;
  - writable flag;
  - final status.
- Status labels are explicit:
  - `SUPPORTED_READ_OK`;
  - `SUPPORTED_WRITE_OK`;
  - `WRITE_FAIL`;
  - `UNSUPPORTED`;
  - `READ_ONLY`;
  - `NO_VALUES`.
- Write probing is conservative:
  - only when diagnostics is launched with `includeWrites=true`;
  - only for writable functions;
  - only if static catalog values exist;
  - maximum first 2 static values per functionId to avoid sending the entire huge catalog blindly.

### Logs 1.30 and Full Stock Settings Import Plan

`logs_1.30` showed that the 1.28 permission regression is fixed: the car API starts through `CarImpl`, and successful GFlow writes are present. Stock logs from `.src/logs_stock` produced 54 unique write contracts; GFlow 1.30 confirmed 13 of them.

What works in GFlow 1.30:

- base Car access;
- HVAC `AUTO`, `AC`, front/rear defrost;
- driver seat heat and steering wheel heat;
- driver seat length/height/backrest;
- windows through `BCM_WINDOW`;
- engine start/stop.

What must be transferred from stock logs:

1. Climate:
   - `0x10010100` HVAC power;
   - `0x10010400` AC max;
   - `0x10020100` fan speed, zone `0x8`, values `268566788/789/790`;
   - `0x10020200` auto fan setting, zone `0x8`, values `268567041/042`;
   - `0x10030100` recirculation, values `268632321/322`;
   - `0x10060100` temperature float, zone `0x1`, values `17.0/18.5/20.0`;
   - `0x10070100` blowing mode, zone `0x8`, values `268894468/469/471`;
   - `0x10080100` eco switch;
   - `0x21110600` mirror defrost.

2. Sunroof / sun curtain:
   - `0x21200200` sunroof open, zone `0x4`, values `0/1`;
   - `0x21200300` sunroof close, zone `0x4`, values `0/1`;
   - `0x21200400` curtain open, zone `0x8`, values `0/1`;
   - `0x21200500` curtain close, zone `0x8`, values `0/1`;
   - `0x21030400` sunroof tilt, global, values `0/1`;
   - `0x21200000` sunroof init switch, global, values `0/1`;
   - `0x21030300` position float, zones `0x4/0x8`, values `0/3/33/40/100`.

3. Ambience light:
   - `0x21051000` atmosphere lamps `0/1`;
   - `0x200a0200` main color;
   - `0x2a010100` intensity float, zone `0x8`, values `10..100`;
   - `0x2a050400` phone call reminder `0/1`;
   - `0x2a070200` transition start color;
   - `0x2a070300` transition end color;
   - `0x2a080100` effect set;
   - `0x2a080200` climate sync `0/1`;
   - `0x2a500000` solid color set;
   - `0x2a500100` breathe color set.

4. Drive / ADAS / steering:
   - `0x22010100` drive mode select;
   - `0x20070800` steering assistance level;
   - `0x22040300` steering feel sync drive mode;
   - `0x21200100` follow DRL.

5. Light / locks / car locator / sound:
   - `0x20040600` courtesy light;
   - `0x20040700` home safe light;
   - `0x20040900` approach light;
   - `0x20080400` auto close window;
   - `0x20100300` audible locking feedback;
   - `0x20160400` car locator reminder mode;
   - `0x201d0100` warning sound volume;
   - `0x20320300` P-gear unlock;
   - `0x2c010100` approach unlock;
   - `0x2e020100` soft button sound type.

6. Seats:
   - `0x2d020100`, zone `0x4`, passenger length;
   - `0x2d030200`, zone `0x4`, passenger backrest;
   - `0x2d400100`, zone `0x1`, seat memory save, value `2`;
   - `0x2d400200`, zone `0x1`, seat memory set, value `1`.

`BCM_CUSTOM_KEY` stock writes directly to `0x21110100` with values `1`, `6`, `100`, `102`. GFlow should try normal `setFunctionValue` first and keep the existing `vfmisc` path as fallback until the machine confirms it.

Transfer priority:

1. climate fan/temp/blowing/circulation;
2. sunroof/sun curtain zones `0x4` and `0x8`;
3. ambience float brightness/color/effects;
4. drive mode and steering assistance;
5. sound/locks/car locator;
6. passenger seat and memory.

### Post-import UI Fixes

Implemented after commit `87d8262 Import stock vehicle control contracts`:

- fixed seat movement controls in `VehicleActivity`:
  - seat movement buttons now behave like stock settings;
  - `ACTION_DOWN` sends the movement value;
  - `ACTION_UP` / `ACTION_CANCEL` sends `0`;
  - this prevents the previous behavior where one tap could keep moving the seat until the mechanical limit;
  - applied to driver and passenger length/height/backrest controls where available.

- added a fluent-style stock HVAC block in `ClimateActivity`:
  - new `Stock HVAC` section on the climate home screen;
  - added `FluentFanDial` custom view as a glass/fluent fan selector;
  - fan dial sends only stock-confirmed values:
    - level 4 -> `0x10020100`, zone `0x8`, value `0x10020104`;
    - level 5 -> `0x10020100`, zone `0x8`, value `0x10020105`;
    - level 6 -> `0x10020100`, zone `0x8`, value `0x10020106`;
  - auto fan sends stock-confirmed:
    - silent `0x10020201`;
    - normal `0x10020202`;
    - zone `0x8`;
  - blowing mode buttons now use stock-confirmed zone `0x8` and values:
    - windshield `0x10070104`;
    - face + windshield `0x10070105`;
    - all zones `0x10070107`;
  - temperature quick pills send stock-observed float values:
    - `17.0`;
    - `18.5`;
    - `20.0`;
    - zone `0x1`;
  - recirculation quick pills send inner/outside values from stock logs;
  - old fan slider and climate presets were adjusted away from unconfirmed levels `1/3` to stock-confirmed levels `4/5/6`.

- expanded ambience-light UI in `MainActivity`:
  - added a polished `Stock ambience` control panel;
  - added brightness slider for `0x2a010100`, zone `0x8`, values `10..100`;
  - added stock effect selector for `0x2a080100`:
    - solid `0x2a080101`;
    - gradient `0x2a080102`;
    - breathe `0x2a080103`;
  - added a candidate `More...` selector backed by catalog/runtime values for extra effects from the source catalog;
  - added ambience toggles:
    - atmosphere lamps `0x21051000`;
    - climate sync `0x2a080200`;
    - phone reminder `0x2a050400`;
    - main color mode `0x200a0200`;
  - added stock color palette:
    - writes solid color `0x2a500000` with zone `0x200a0100`;
    - writes breathe color `0x2a500100` with zone `0x200a0100`;
    - writes transition start/end `0x2a070200/0x2a070300` with global zone `0x80000000`;
  - corrected `EcarxVehicleAdapter` specs so ambience effect/toggles use global zone, while solid/breathe colors keep ambience zone `0x200a0100`.

- added a dedicated roof/sunshade control UI in `VehicleActivity`:
  - new `Roof control` card inside the Mirrors/Roof screen;
  - sunroof open/close sends `0x21200200/0x21200300`, zone `0x4`, values `1`;
  - sun curtain open/close sends `0x21200400/0x21200500`, zone `0x8`, values `1`;
  - tilt/init controls send `0x21030400` and `0x21200000` with global zone;
  - added stock position presets:
    - sunroof `40%` via float `0x21030300`, zone `0x4`;
    - sun curtain `33%` via float `0x21030300`, zone `0x8`;
  - added separate stepped sliders for sunroof and sun curtain using `BCM_WINDOW_POS` float writes;
  - roof sliders use 10% steps from `0%` to `100%`, because stock logs only captured sampled slider positions, not the full range.

## 2026-07-29 / 2026-07-30

### Logs 1.28 Regression and Stock Settings Import

Runtime testing of the 1.28 build showed a regression back to the old AdaptAPI permission failure:

- logs from `.src/logs_1.28` showed repeated `java.lang.SecurityException: Permission denied`;
- the stack always entered through `com.ecarx.xui.adaptapi.car.Car.create(...)`;
- vendor logs showed the firmware signature/whitelist check for `com.prodject.gflow`;
- conclusion: the typed migration was correct for function signatures, but using `Car.create(context)` reintroduced the authority gate.

Implemented after that finding:

- commit `fc500cb Fix vehicle API startup and diagnostics capture`:
  - `EcarxVehicleAdapter` no longer creates the car API through `Car.create(...)` as the primary path;
  - `CarBridge.createCar(...)` now first instantiates `com.ecarx.xui.adaptapi.car.CarImpl(Context)` reflectively, then keeps the rest of the stack typed through `ICar` / `ICarFunction`;
  - `Car.create(...)` remains only as fallback;
  - added log markers for `createCar via CarImpl` vs fallback;
  - diagnostics report now includes best-effort `logcat -d` output so missing `READ_LOGS` or logcat failures are visible in the report.

The stock logging helper was also repaired after manual testing showed that its interactive package menu was being captured into the package variable:

- commit `1f14d8e Improve stock logging and package visibility`:
  - `collect-adb-log-stock.sh` now sends menu/prompt output to stderr and only emits the selected package on stdout;
  - fixed the `File name too long` session-directory bug;
  - added real firmware settings packages to the menu (`com.android.car.settings`, `com.ecarx.settings`, `com.ecarx.xui.settings`, `com.zeekr.settings`, `com.geely.settings`);
  - added targeted manifest `<queries>` entries for observed vendor/system packages from the provided `dumpsys package` output.

### Stock `main_шторка.log` Findings

Manual stock settings/shade logging in `.src/main_шторка.log` was then used as runtime evidence for function/value import.

The first pass extracted all unique `setFunctionValue` calls from the stock log:

- 18 unique stock functions were found;
- all 18 now have catalog entries and observed values in `possibleValues`;
- `SETTING_FUNC_EASY_INGRESS_EGRESS` was confirmed to use zone `0x1`, not global `ZONE_ALL`;
- several functions already existed in the catalog but had empty value lists, which made the generated UI/selectors incomplete.

Implemented from that pass:

- commit `6bc72b8 Align vehicle controls with stock settings logs`:
  - added stock-confirmed values for Start/Stop, ESC Sport, Approach Light, PBC Auto Apply, Auto Hold, HDC, LKA switch, LKA warning, RCTA, RCW, Easy Ingress/Egress, atmosphere lamps, mirror fold, drive mode, WPC work mode, speed-limit warning mode, DayMode brightness mode, and ambience effect;
  - fixed ADAS `LKA` so the visible button writes `0x20070100 -> 1` instead of sending mode values to the wrong function;
  - fixed ADAS speed-limit warning so it writes to `0x28060200` with observed values instead of sending a `0x280602xx` value to `0x28060100`;
  - added stock-confirmed quick controls in Vehicle/ADAS screens for the functions that had safe observed writes;
  - added `DAYMODE_BRIGHTNESS_DAYMODE = 0x29030200` and included it in shared diagnostics.

A second pass searched the stock log for slider/float behavior, because sliders do not necessarily appear as `setFunctionValue`:

- the only unique `setFloatProperty` in `.src/main_шторка.log` was `SETTING_FUNC_MCD_AUTO_BRIGHTNESS_SCREEN`;
- property/function id: `0x29030500`;
- zone: `0x80000000`;
- observed float range: `2.0..14.0`;
- stock backend path: `setFloatProperty`;
- GFlow path: `ICarFunction.setCustomizeFunctionValue(function, zone, float)`.

Implemented from that pass:

- commit `9df812f Handle stock screen brightness as float`:
  - `DAYMODE_BRIGHTNESS_SCREEN` is now marked as a float/customize-function property;
  - legacy screen brightness controls now send representative float values (`2.0`, `8.0`, `14.0`) instead of integer brightness values (`25`, `50`, `75`).

A third pass extracted every observed `propertyId/areaId` from stock `setIntProperty`, `setFloatProperty`, `getIntProperty`, `getFloatProperty`, `callback`, and `isSupport` lines, not only active writes:

- 34 unique property/area pairs were found;
- after the import pass, missing catalog coverage is `0`;
- missing adapter constants for observed non-sensor properties is `0`.

Implemented from that pass:

- commit `761a0ce Add stock log readback properties`:
  - added adapter constants for stock-observed readback/callback properties:
    - `WPC_CHARGE_STATES = 0x26020100`;
    - `AMBIENCE_LIGHT_TRANSITION_START_COLOR = 0x2a070200`;
    - `AMBIENCE_LIGHT_TRANSITION_END_COLOR = 0x2a070300`;
  - marked float readback for:
    - `DAYMODE_BRIGHTNESS_MAX = 0x20150500`;
    - `DAYMODE_PSD_BRIGHTNESS_SCREEN = 0x29200100`;
  - `catalogReadInt(...)` now routes float functions to `getCustomizeFunctionValue(...)`;
  - shared diagnostics now include WPC charge state and ambience transition colors.

Useful data now known from the stock log but intentionally not promoted to write buttons:

- `WPC_FUNC_CHARGE_STATES` reported callback/status values across `ZONE_ALL`, `0x1`, and `0x8`, but no stock write was observed;
- ambience transition/solid/breathe colors appeared as readback values, not as proven writes in this log;
- `SENSOR_TYPE_DAY_NIGHT` produced a sensor readback and is already catalog-covered as a sensor, not a normal writable function;
- `DAYMODE_BRIGHTNESS_MAX` and `DAYMODE_PSD_BRIGHTNESS_SCREEN` are useful float readbacks, not user-facing writes from this evidence.

Current state after these commits:

- all active stock writes observed in `.src/main_шторка.log` have matching function id / zone / value coverage;
- all stock float properties observed in that log have been handled;
- all observed non-sensor property IDs from get/set/callback/support lines have catalog or adapter coverage;
- no broad inference was made for hidden functions that were not touched in the stock UI.

Open follow-up:

- collect a fuller stock settings log by slowly walking through each settings section and moving sliders min→max→min;
- use new logs to expand only proven `possibleValues`, min/max/step, zones, and write types;
- keep readback-only properties readback-only until a stock `set...` line proves the write contract.


## 2026-07-23

### Product Direction

The project is no longer just a functional car-control shell. The main goal is now to turn it into a polished in-car UI with:

- clear hierarchy;
- smooth motion;
- premium visual depth;
- understandable navigation;
- fewer overwhelming screens;
- a design language closer to Tesla, BYD, and Geely than to a utility dashboard.

The current main screen is acceptable as a base. The rest of the app is too dense, too utility-like, and too inconsistent visually. The priority is to make it feel like one coherent product.

### Initial UI Assessment

Current strengths:

- `MainActivity` already has a recognizable dashboard structure.
- The app already uses a shared `Ui` helper and consistent programmatic composition.
- Several screens already use lightweight transitions via `Ui.animateIn(...)`.

Current problems:

- Many secondary screens are visually overloaded and feel like feature dumps.
- Navigation is function-first, not experience-first.
- The app lacks a stronger sense of depth, pacing, and 3D-like spatial hierarchy.
- There is too much exposed at once, which makes the product hard to understand.

### Design Strategy

The UI direction should shift to:

- dashboard-first navigation;
- progressive disclosure instead of long control sheets;
- stronger grouping of features by task, not by implementation;
- richer motion for screen entry, switching, and card expansion;
- more depth, layering, and ambient surfaces;
- better use of hero panels, 3D views, and focal points;
- fewer dense grids that read like internal tools.

### Source Material

The original OEM/reference trees in [`.src/Ext`](./Ext) should be treated as visual and motion references for:

- transitions;
- screen composition;
- card behavior;
- 3D / spatial presentation;
- control density and hierarchy.

These references are useful as inspiration, not as direct product copy.

### Repository Analysis Summary

The repo is a single-module Android Java app with a flat package structure and a few core orchestration points:

- [MainActivity.java](../Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java)
- [VoiceActivity.java](../Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VoiceActivity.java)
- [AutomationEngine.java](../Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AutomationEngine.java)

Most of the complexity is hidden in:

- shared preferences contracts;
- broadcast routing;
- vehicle/vendor reflection adapters;
- oversized activity classes.

That means UI refactoring will need to happen together with navigation simplification, otherwise the app will stay functionally dense even if the visuals improve.

### Security / Trust Notes

The security review found the main trust boundaries are not visual, but they matter for the UX plan because sensitive flows should not be made easier to trigger accidentally:

- exported steering and voice entry points;
- backup restore import;
- broad file/provider sharing;
- boot-triggered automation.

The premium UI should not make risky actions more prominent without deliberate confirmation states.

### Next UI Work

The next useful steps are:

1. define a design system for the whole app;
2. simplify screen hierarchy and navigation;
3. redesign high-density screens like climate, vehicle, parking, DVR, settings;
4. add richer motion and 3D cues;
5. use `.src/Ext` as motion/reference source while keeping the product visually consistent.

### Agent Notes

Read-only analysis completed by one orchestrator agent plus targeted analysis from:

- `mobile-developer`
- `java-architect`
- `security-auditor`

No code changes were made during analysis.

### Implementation Progress

Started implementation of the first UI pass on 2026-07-23.

Completed so far:

- [Ui.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/Ui.java) now has a stronger motion foundation:
  - delayed enter animation;
  - scale-in animation;
  - unified press feedback;
  - simple stagger support.
- [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) was simplified toward a more premium dashboard:
  - less overloaded top bar;
  - secondary information moved into the title block instead of equal-weight stat cards;
  - fewer equal-weight dashboard widgets;
  - dock reduced to higher-frequency destinations;
  - tactile feedback added to hero, cards, drawer actions, and dock buttons.
- `ui-designer` design brief was applied to [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java):
  - hero reduced to 3 key states instead of a mixed utility list;
  - mode navigation rebuilt as a segmented strip;
  - `HOME` reorganized around body-first access, windows, and trunk;
  - roof controls moved under mirrors/roof instead of cluttering the body screen;
  - `LIGHTS` and `DRIVE` split into primary and secondary tiers;
  - experimental drive controls are now collapsed by default behind an explicit reveal;
  - vehicle visualization was upgraded with stronger layered depth and glow.
- `ui-designer` parking brief was applied to [ParkingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ParkingActivity.java):
  - hero reduced to 3 clear parking states instead of mixing quick and expert status;
  - parking mode rebuilt as a segmented selector instead of equal chips;
  - RCTA and assist tools moved out of the primary parking block;
  - raw APA/RPA and PAS/AVM are now hidden behind a darker expert layer;
  - status cards were visually weakened so they stop competing with the main flow;
  - parking visualization was upgraded with more layered depth and guidance rails.
- [ClimateActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ClimateActivity.java) was reorganized into a calmer climate-first flow:
  - top bar now matches the premium hierarchy used by the updated main, vehicle, and parking screens;
  - hero now focuses on zone temperatures, driver temperature control, sync, and quick scenes instead of mixing every control tier together;
  - comfort toggles were reduced into a smaller comfort layer instead of a 4-column switch wall;
  - airflow and comfort actions now dominate the main content while presets moved into the hero as scenarios;
  - advanced HVAC now reads as an expert layer with darker visual treatment;
  - readback cards were visually weakened so diagnostics stop fighting for first attention.
- [VoiceActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VoiceActivity.java) was reorganized into a more understandable assistant-style flow:
  - hero now focuses on listening state, recognition source, and the primary voice actions;
  - overview cards were visually weakened so they stop competing with the main interaction;
  - assistant flow and command composer were moved ahead of aliases and logs;
  - aliases and fallback log are now treated as secondary review/configuration layers;
  - dock and primary actions now use the same press/motion rhythm as the updated main app screens.
- [SettingsActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SettingsActivity.java) was reorganized into a cleaner settings-first structure:
  - top bar and hero now present settings as a product configuration surface instead of a raw service panel;
  - `General Settings` now reads as the main everyday layer;
  - `System / Recovery` was visually darkened and made heavier so risky actions feel deliberate;
  - `Updates` and `Auto Diagnostics` were visually weakened into secondary tooling layers;
  - overview cards and dock were aligned with the calmer hierarchy and motion used across the redesigned screens.
- [FileManagerActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/FileManagerActivity.java) was reorganized into a cleaner content-first browser:
  - top bar and hero now present path, storage and move queue as a calm overview instead of a raw operations screen;
  - storage context and browser actions were separated from the file list so content stays primary;
  - file cards were visually simplified and secondary overview cards were weakened;
  - move-state is now more clearly visible without making the whole screen feel like a transfer utility;
  - dock and buttons now use the same press/motion rhythm as the rest of the redesigned app.
- [TextViewerActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/TextViewerActivity.java) was reorganized into a cleaner reading-first surface:
  - hero now focuses on file identity and reading mode instead of acting like a utility dashboard;
  - search stays available, but the reading surface now dominates the screen;
  - secondary overview cards were visually weakened;
  - dock and actions now use the same calmer press/motion pattern as the rest of the app.
- [MediaViewerActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MediaViewerActivity.java) was reorganized into a cleaner media-first surface:
  - hero now communicates media type, gallery position and viewing mode more clearly;
  - the viewing surface now dominates over metadata and controls;
  - overview cards were visually weakened into a secondary layer;
  - dock and action rhythm were aligned with the redesigned app surfaces.
- Second-wave consistency pass was applied to remaining major control surfaces:
  - [HudActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/HudActivity.java) now better separates primary projection controls from advanced bridge/service tooling and uses calmer secondary status cards.
  - [AutomationActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AutomationActivity.java) now presents templates/libraries as the main layer while notes, status cards, and dock rhythm match the newer product pattern.
  - [ProfileActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ProfileActivity.java) now reads more like a people/profile surface and less like a settings dump, with weaker secondary cards and more consistent interaction rhythm.
- Remaining outlier package was then redesigned into the same hierarchy model:
  - [AdasActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AdasActivity.java) now reads as a clearer drive-assistance surface with a calmer hero, explicit drive layers, darker experimental/diagnostic sections, weaker secondary cards, and consistent press/stagger/dock rhythm.
  - [CameraActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/CameraActivity.java) now reads as a capture-first surface where recording and EVS actions come first, while archive settings and diagnostics are visually demoted into secondary layers.
  - [SteeringActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SteeringActivity.java) now better separates everyday wheel bindings from examples/editor tooling, with darker editor/example layers and consistent motion/press behavior.
  - [WeatherActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/WeatherActivity.java) now reads weather-first, while browser, bookmarks, and forecast behave as calmer secondary layers instead of competing equal-weight panels.
- Final leftover desktop/service surfaces were also aligned:
  - [DesktopActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DesktopActivity.java) now better separates home surface, weather widget, app library, and OneOS bridge tooling, with calmer secondary panels and consistent interaction rhythm.
  - [DvrActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DvrActivity.java) was rebuilt from the old utility layout into a legacy-compatible capture surface that matches the newer card hierarchy and dock rhythm.
  - [AdbShellActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AdbShellActivity.java) now presents shell/system tooling more clearly, with advanced ADB/DPI/output layers visually separated from the primary permission/shell flow.
- Repo-wide polishing pass was applied after the structural redesign wave:
  - [Ui.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/Ui.java) now defines clearer secondary/deep surfaces and slightly tighter typography/button rhythm for a more premium, calmer baseline.
  - [HudActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/HudActivity.java) now has a stronger advanced depth layer, calmer secondary status cards, and more consistent press/stagger behavior in bridge actions.
  - [AutomationActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AutomationActivity.java) now uses calmer secondary note/status surfaces and more consistent interaction rhythm, making the screen read less like a tool dump.
  - [ProfileActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ProfileActivity.java) now has deeper avatar/editor treatment, calmer notes/status layers, and better separation between active identity, lists, and editing.

Not done yet:

- stronger 3D hierarchy outside the home screen;
- a final review/QA pass to score residual weak screens and minor visual outliers;
- selective future enhancement of 3D/spatial presentation on the strongest user-facing surfaces rather than another broad structural cleanup.

## 2026-07-25

### Vehicle Control Repair Plan

Current repair target is no longer visual cleanup. The priority is to make real vehicle-control paths match the actual backend split observed in `.src/logs-1.24` and OEM sources from [`.src/Ext`](./Ext).

The repo currently mixes several different control backends under the same UI assumption:

- `AdaptAPI / ICarFunction` for part of HVAC and BCM;
- `CustomizeFunctionValue(float)` for zoned HVAC temperature;
- `CarSignalManagerAdapter` for confirmed parking / APA signal paths;
- possible `Intent` or system-entry launchers for `360 / AVM`;
- readback-only or status-only IDs that should not be treated as writable toggles.

Because of that, the repair work is being split into backend-specific stages instead of trying to “fix all buttons” with one generic command path.

### Repair Rules

From this point, every repair stage must follow the same workflow:

1. implement one coherent backend fix;
2. update this changelog with what changed and what remains open;
3. create one local git commit for that stage;
4. do not push automatically.

No local build is available, so verification is limited to:

- source analysis;
- `.src` OEM/reference trees;
- collected runtime logs in `.src/logs-1.24`;
- consistency review of call sites and backend contracts.

### Planned Stages

1. `HVAC foundation`
   - normalize zoned vs global HVAC calls;
   - use float read/write for temperature only;
   - correct fan-speed value handling to real enum values;
   - stop treating unsupported raw readback as valid state;
   - normalize seat / wheel level handling.

2. `BCM / Vehicle body`
   - separate raw body status from command availability;
   - stop presenting bitmask-like return values as hard failures;
   - add clearer raw diagnostics for doors / windows / locks;
   - keep commands executable even when one status property is unclear.

3. `ADAS gating`
   - stop sending commands blindly when support is `notavailable`;
   - separate writable functions from readback-only and status-only IDs;
   - move ADAS screens to capability-first execution.

4. `Parking / APA backend split`
   - keep confirmed parking signal flows on `CarSignalManagerAdapter`;
   - remove false assumption that all parking controls are writable `ICarFunction` properties;
   - classify PAS / APA IDs into command vs status groups.

5. `360 / AVM entrypoint`
   - investigate and replace the broken `BCM_CUSTOM_KEY_360` assumption if needed;
   - route 360 opening through the actual backend once identified;
   - keep camera / AVM buttons aligned with that confirmed path only.

6. `Registry cleanup`
   - move function metadata toward explicit specs:
     - backend;
     - default zone;
     - writable/read-only;
     - value kind;
     - allowed values;
   - reduce ad-hoc UI logic spread across activities.

### Stage 1 Progress: HVAC Foundation Started

Initial HVAC repair work has started in:

- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java)
- [ClimateActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ClimateActivity.java)

Implemented in this first pass:

- `EcarxVehicleAdapter` now treats zoned `set/get/isFunctionSupported` as the primary path and only falls back when the zoned signature is unavailable.
- default zones were expanded so global HVAC properties no longer silently use `0`, while fan-related HVAC properties now use a dedicated HVAC area instead of the previous generic fallback.
- `Result` now carries an explicit `supported` flag so UI code can stop inferring support from message text alone.
- climate temperature readback now uses `getFloat(...)` instead of integer `getFunctionValue(...)`.
- climate fan commands no longer send raw `1..9`; they now map to real HVAC enum values based on the IDs found in OEM sources and logs.
- climate seat-heat / seat-vent actions were converted from fixed one-shot values to simple cycling logic so they behave like level-based functions instead of binary toggles.
- climate command toasts now reflect command success instead of always reporting `HVAC updated`.
- HVAC seat and steering-wheel value enums were corrected against OEM `IHvac` values:
  - seat heating now uses `0x10050201/02/03` instead of the previous mismatched `0x100503..` block;
  - seat ventilation now uses `0x10050101/02/03`;
  - steering wheel heat now uses `0x10090101/02/03` instead of the previous `AUTO_STEERING_WHEEL_HEAT` values.
- climate screen wheel heating now cycles through real wheel-heat levels instead of always forcing a fixed mid-level command.
- climate commands now preflight support by function/zone before sending writes, so unsupported HVAC controls fail early with a clear reason instead of looking like silent UI no-ops.
- the corrected HVAC seat/wheel enums were propagated to:
  - voice climate commands;
  - smart climate automation;
  - stored user-profile seat-heat writes.

Still open inside stage 1:

- validate whether `HVAC_FAN_SPEED` should finally stay on the current zone choice or move to another confirmed HVAC area after more backend evidence;
- review remaining HVAC call sites outside the repaired set, especially `MainActivity`, so they stop bypassing the corrected support-aware mapping.

### Stage 2 Progress: BCM / Vehicle Body Started

Next repair work moved into:

- [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java)
- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java)

Implemented in this pass:

- `BCM_DOOR_STATUS` is no longer treated as a normal unsupported boolean when it returns `0xff`; for this function the raw value is now preserved as a known body-status readback.
- `VehicleActivity` top stats and hero summary no longer frame body status as a simple on/off field.
- body readback now surfaces `BCM_DOOR_STATUS` as raw `0x........ bits=...` output so ambiguous BCM state is visible without being mislabeled as API failure.
- window and lock summaries were separated from raw body status so working BCM commands remain visible even when one status property looks unusual.
- BCM window control is no longer treated like HVAC zoning:
  - OEM `VehicleWindow` areas `0x10/0x20/0x100/0x200` were added explicitly;
  - aggregated window areas `front=0x30`, `rear=0x300`, `all=0x330` are now available for BCM window commands;
  - default `BCM_WINDOW*` calls no longer fall back to unsupported `ZONE_ALL`.
- voice window commands now use BCM-specific window areas instead of reusing generic HVAC/seat zones, which previously made `all/left/right/rear` window actions hit the wrong backend area.
- BCM door/control zoning is now split by confirmed behavior instead of using one generic fallback:
  - `BCM_DOOR_LOCK` and `BCM_DOOR_CONTROL` stay on global `ZONE_ALL`, because logs show them active there;
  - `BCM_DOOR` now has explicit OEM areas for row1/row2/hood/rear;
  - `BCM_CHILD_SAFETY_LOCK` now uses rear-door zones instead of a broken global write path.
- voice door commands no longer send `BCM_DOOR` to unsupported `ZONE_ALL`; they now require a concrete target door and map it to confirmed OEM areas.
- vehicle UI `Child lock` action now writes both rear child-lock zones directly, which matches the rear-door area model seen in runtime callbacks.
- body diagnostics now use float/custom readback for position-like BCM properties instead of pretending they are plain integer states:
  - `BCM_DOOR_POS` is shown through `getCustomizeFunctionValue(...)` for the confirmed rear-door area;
  - `BCM_WINDOW_POS` and `BCM_WINDOW_CURRENT_POS` are shown through float readback for front-left/front-right zones.
- vehicle top summaries now surface live window/door position information, which makes BCM body diagnostics much closer to the actual OEM callback model.
- trunk handling is now explicitly split into:
  - OEM entry through `BCM_CUSTOM_KEY/CUSTOM_KEY_TRUNK` for user actions;
  - real trunk state readback through `ISafety.SETTING_FUNC_TRUNK_STATE` and `ISafety.SETTING_FUNC_TRUNK_OPENING_PERCENTAGE`.
- `VehicleActivity` no longer presents the trunk as if it were just another direct BCM write/read pair; it now shows real `ISafety` state/percentage diagnostics alongside the OEM launch action.

Still open inside stage 2:

- inspect remaining trunk/door automation entry points and decide whether any body actions should be disabled until their per-zone semantics are proven from runtime behavior;
- add clearer per-zone body diagnostics once the useful OEM zone mapping is narrowed down from logs.

### Stage 3 Progress: ADAS Gating Started

ADAS command-path cleanup moved into:

- [AdasActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AdasActivity.java)

Implemented in this pass:

- ADAS commands no longer go directly to `CarCommandBus` without a preflight check.
- before sending a command, `AdasActivity` now asks `EcarxVehicleAdapter.support(functionId)` and stops when the function is not supported on this vehicle.
- a first explicit readback/status-only blacklist was added for ADAS IDs that should not be treated as writable controls from this screen.
- user feedback now distinguishes between:
  - unsupported AdaptAPI functions on this car;
  - readback/status IDs that should not be written at all;
  - genuine command-send failures.

Still open inside stage 3:

- move from a local blacklist toward a fuller per-function registry with `writable/backend/value-kind` metadata;
- review parking-adjacent ADAS items like `PDC` so their control path stays aligned with the later parking/APA split;
- revisit `Max cruising speed` and other selector-style controls to confirm that their value payloads match the real backend contract on this firmware.

### Stage 4 Progress: Parking / APA Backend Split Started

Parking backend cleanup moved into:

- [ParkingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ParkingActivity.java)

Implemented in this pass:

- parking UI now treats `Auto Park` and `360` differently instead of assuming both are valid `BCM custom key` command entries.
- `Auto Park` was kept as a cautious “open stock Auto Park UI” action because that path is still the least risky entrypoint referenced by the OEM notes.
- `360` was removed from the active parking command path because:
  - manual testing already showed the button does not open 360;
  - no confirmed backend entrypoint is available yet;
  - the previous implementation was pretending that `BCM_CUSTOM_KEY_360` was a valid proven route when it is not.
- AVM/PAC preset composition was cleaned so it no longer appends the broken `BCM_CUSTOM_KEY_360` call to otherwise parking-related PAS actions.
- confirmed APA scenario control remains on `CarSignalManagerAdapter` signal methods instead of being folded back into speculative `ICarFunction` writes.
- `ParkingActivity` now uses the same confirmed EVS path as the rest of the app for visible `360` actions:
  - hero `360`;
  - primary parking `Открыть 360`;
  - assist shortcut `Top View`;
  - advanced parking `Открыть 360-панораму`;
  - bottom dock `360`.
- parking write actions no longer go straight to `CarCommandBus`:
  - direct PAS / RCTA / PDC writes are now preflighted through `EcarxVehicleAdapter.support(...)`;
  - unsupported parking properties fail early with an explicit `Функция недоступна...` message instead of pretending the command path is valid.
- advanced PAS / AVM controls were reframed as `support-gated + diagnostics-first`:
  - raw AVM open/close presets that mixed PAC and speculative AVM activation were removed;
  - EVS open is now the only promoted AVM entrypoint in the parking screen;
  - PAC-only presets remain as cautious secondary actions when the backend really reports writable support.
- parking status cards are no longer static filler:
  - `AVM / PAC` now reads live PAC status / view state plus the EVS-entry note;
  - `APA / RPA` now reads live `CarSignalManager` APA display/state signals;
  - `PDC / Radar` now reads live ADAS PDC + PAS radar mode state;
  - `RCTA / SAP` now reads live RCTA activation/warning state.
- parking diagnostics are now more compact and backend-oriented, combining support and readback for each function instead of dumping the same two-line pattern repeatedly.
- direct PAS write classification was then tightened against the collected logs from July 25, 2026:
  - `PAS_FUNC_RCTA_ACTIVATION (0x23050100)` logged `setFunctionValue ... result:false` for both `0` and `1`;
  - `PAS_FUNC_PAC_OVERLAY_STEERPATH (0x23030800)` logged `setFunctionValue ... result:false`;
  - `PAS_FUNC_PAS_RADAR_WORK_MODE (0x23021000)` logged `setFunctionValue ... result:false`;
  - `PAS_FUNC_PAC_TOP_VIEW_ZOOM_IN (0x23032400)` logged `setFunctionValue ... result:false`.
- because no successful `setFunctionValue` evidence was found for the rest of the remaining direct PAS visual controls on this firmware, `ParkingActivity` no longer presents them as actionable commands:
  - radar/overlay/PAS visual controls were removed from the main parking action layer;
  - advanced `PAS / AVM` direct write buttons were removed entirely;
  - the screen now keeps only confirmed control paths there:
    - `CarSignalManagerAdapter` for APA/RPA signals;
    - OEM `BCM_CUSTOM_KEY` entry for `Auto Park`;
    - EVS open for visible `360`.
- remaining PAS properties in the parking screen are now treated as diagnostics/readback-only until a successful write is proven in logs or OEM code paths.

Still open inside stage 4:

- confirm whether `Auto Park UI` through `BCM custom key 0x65` is actually effective on this firmware or should also be downgraded to diagnostics-only;
- verify on-device whether any PAS write path outside `CarSignalManagerAdapter` is actually successful on this firmware, or whether the whole `ICarFunction` PAS control layer should stay permanently diagnostic-only.

### Stage 5 Progress: 360 / AVM Entrypoint Started

360 entrypoint repair moved into:

- [VoiceActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VoiceActivity.java)
- [LowSpeedCameraService.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/LowSpeedCameraService.java)
- [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java)
- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java)

Implemented in this pass:

- user-facing 360 launchers no longer rely on `BCM_CUSTOM_KEY_360`, because manual testing already showed that path does not open the штатный 360 UI on this firmware.
- voice command handling for `360` / `камера` now opens AVM through `EcarxDvrAdapter.openEvs(EVS_CAMERA_AVM)`.
- `LowSpeedCameraService` low-speed auto-open now uses the same EVS AVM backend instead of issuing the broken BCM custom key write.
- the `Vehicle` drive/custom quick action was changed from `Custom 360` to `Open 360` and now opens AVM through EVS as well.
- `EcarxVehicleAdapter.Result` received a small external-result factory so non-`ICarFunction` backends can still report status through existing UI/voice flows without inventing fake BCM function writes.

Still open inside stage 5:

- review every remaining textual or automation entrypoint that may still mention `BCM_CUSTOM_KEY_360`, especially legacy notes/examples and any background automation not yet wired through `EcarxDvrAdapter`;
- verify whether EVS `openEvs(EVS_CAMERA_AVM)` should also be paired with an explicit close/lifecycle hook in some flows to avoid sticky camera sessions on this firmware.

### Stage 6 Progress: Registry Cleanup Started

Registry cleanup moved into:

- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java)
- [AdasActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AdasActivity.java)
- [ParkingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ParkingActivity.java)

Implemented in this pass:

- `EcarxVehicleAdapter` now owns a central per-function spec layer instead of spreading behavior across activities:
  - `defaultZone`;
  - `backend`;
  - `writable` vs `read-only`;
  - `floatValue` vs integer value;
  - `knownRawStatus`.
- the old `defaultZone(...)` switch is now backed by that spec layer instead of being the only source of function metadata.
- the registry currently captures the backend split already proven during the repair passes:
  - global HVAC functions stay `AdaptAPI` on `ZONE_ALL`;
  - HVAC temperature is marked as `AdaptAPI float/customize-function`;
  - BCM window / door / child-lock defaults and raw-status behavior now live in the adapter spec;
  - direct PAS `ICarFunction` properties are marked read-only for this firmware until successful write evidence appears;
  - ADAS readback/failure/status IDs that were previously blacklisted only in `AdasActivity` are now marked read-only in the adapter registry itself.
- `AdasActivity` no longer owns a local `isWritableAdasFunction(...)` blacklist; it now asks the adapter registry whether a function is writable.
- `ParkingActivity` write gating is now also driven by the same adapter registry, so any PAS function classified as diagnostics/readback-only is blocked centrally instead of by screen-specific assumptions.
- this makes the July 25, 2026 parking classification reusable outside the parking screen: the same PAS metadata now follows the function wherever it is referenced.

Still open inside stage 6:

- broaden the registry from the current repaired sets into a fuller repo-wide spec for the remaining BCM / seat / drive / comfort functions that still fall through to the default writable path;
- decide whether some of the existing `backend` enum values (`SIGNAL`, `EVS`, `SAFETY`, `OEM_ENTRY`) should later be wired into a higher-level command router instead of staying descriptive metadata for now.

### Stage 6 Follow-up: Logs 1.25 Regression Sweep

New evidence from `.src/logs_1.25` on Sunday, July 26, 2026 showed that several remaining command paths still misbehave on the target firmware:

- `HUD_ACTIVE (0x20110100)` repeatedly reports `status:notavailable` and direct writes return `result:false`.
- an older 1.25 session still showed legacy `zone 0x0` write attempts for:
  - `HVAC_POWER (0x10010100)`;
  - `HVAC_AUTO (0x10010200)`;
  - `HVAC_FAN_SPEED (0x10020100)`;
  - `HVAC_CLIMATE_ZONE (0x10010500)`.
- the same log set also showed failed direct BCM writes for:
  - `BCM_DOOR_LOCK (0x21020200)`;
  - `BCM_SUNROOF_OPEN/CLOSE (0x21200200 / 0x21200300)`;
  - `BCM_SUNCURT_OPEN/CLOSE (0x21200400 / 0x21200500)`;
  - `BCM_MIRROR_FOLD (0x21060100)`;
  - `BCM_CUSTOM_KEY (0x21110100)`.

Follow-up fixes applied after reviewing the 1.25 logs:

- `EcarxVehicleAdapter.set(...)` and `setFloat(...)` now preflight `writable` and `support` centrally instead of relying on each activity to do that correctly.
- direct unsupported HUD writes are now blocked centrally by the adapter registry, not only by individual screens.
- BCM roof / curtain / mirror-fold / custom-key functions now have explicit default zones in the adapter registry instead of silently falling back to `0x0`.
- this means legacy callers that still go through `CarCommandBus.sendVehicle(...)` now inherit the repaired registry behavior instead of repeating the old broken zone path.

Still open after the 1.25 sweep:

- re-validate on-device whether the new explicit BCM default zones improve `door lock / roof / mirror / custom key` behavior or whether some of those functions must also be demoted to diagnostics-only on this firmware;
- if `logs_1.25` still show fresh `zone 0x0` HVAC writes after these fixes are installed, trace the remaining caller and remove that path explicitly.

### 2026-07-26 Follow-up: Logs 1.25 Findings

New runtime evidence from [`.src/logs_1.25`](./logs_1.25) showed that the app still had live command paths bypassing the repaired model.

Confirmed issues from the July 26, 2026 logs:

- `IVehicle.SETTING_FUNC_HUD_ACTIVE [0x20110100]` repeatedly returned:
  - `status:notavailable`
  - `setFunctionValue ... result:false`
- HVAC still had active call paths producing wrong-zone support checks:
  - `IHvac.HVAC_FUNC_TEMP [0x10060100]` was still seen against `VehicleWindow.WINDOW_ROW_1_LEFT [0x10]`;
  - `IHvac.HVAC_FUNC_SEAT_VENTILATION [0x10050100]` was still seen against `VEHICLE_AREA_TYPE_ZONE [0x1]` and `VEHICLE_AREA_TYPE_DOOR [0x4]`.
- in the early `gflow-20260726-143138` session there were also direct write failures for still-exposed UI commands such as:
  - `IBcm.BCM_FUNC_DOOR_LOCK [0x21020200]`
  - `IBcm.BCM_FUNC_FOLD_REAR_MIRROR [0x21060100]`
  - `IBcm.BCM_FUNC_CUSTOM_KEY [0x21110100]`
  - `IBcm.BCM_FUNC_SUNROOF_* [0x21200200..0x21200500]`
  - raw main-screen HVAC writes including `HVAC_POWER`, `HVAC_AUTO`, `HVAC_CLIMATE_ZONE`, and `HVAC_FAN_SPEED`.

Implemented from those findings:

- `EcarxVehicleAdapter` registry now marks direct HUD `AdaptAPI` functions as diagnostics/readback-only instead of leaving them on a writable path.
- [HudActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/HudActivity.java) now routes HUD vehicle actions through centralized writable/support gating, so the screen stops pretending the unsupported HUD direct backend is valid on this firmware.
- [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java) now also uses centralized writable/support gating for direct BCM commands instead of always sending them blindly.
- [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) no longer bypasses the repaired command model:
  - main-screen HVAC fan writes now use the real fan enum mapping instead of raw `progress + 1`;
  - main-screen HVAC / ADAS / HUD toggles now go through support-aware helper methods instead of direct `CarCommandBus` calls.

Still open after the 1.25 follow-up:

- trace the remaining HVAC wrong-zone evidence until the exact call site producing `HVAC_TEMP -> zone 0x10` and `HVAC_SEAT_VENTILATION -> door/zone area` is narrowed down with certainty;
- review whether the newly gated BCM roof / mirror / lock paths should be fully demoted in UI if future logs keep showing `result:false` despite `support=active`.

Additional fixes applied after the initial 1.25 review:

- [ClimateActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ClimateActivity.java) zoned HVAC readback no longer calls plain `support(functionId)` for zone-scoped functions; it now uses `support(functionId, zone)`, which removes one real source of false seat-climate support noise in runtime logs.
- [VoiceActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VoiceActivity.java) now routes seat climate and HUD voice commands through centralized writable/support checks instead of blind sends.
- voice seat heating / ventilation is now intentionally limited to confirmed front-seat zones only; `rear` / `all` seat-climate requests are rejected as unsupported instead of being translated into speculative HVAC zones.

Additional hard demotions from confirmed `result:false` write logs:

- `DRIVE_MODE_SELECT [0x22010100]` is now classified as diagnostics/readback-only. The July 26, 2026 `logs_1.25` capture showed repeated direct write failures for eco / comfort / snow / rock / hdc / eco-plus through the AdaptAPI path, with no corresponding `result=true` evidence in the same capture.
- `BCM_DOOR_LOCK [0x21020200]`, `BCM_CUSTOM_KEY [0x21110100]`, `BCM_SUNROOF_OPEN/CLOSE [0x21200200/0x21200300]`, `BCM_SUNCURT_OPEN/CLOSE [0x21200400/0x21200500]`, and `BCM_LIGHT_HAZARD [0x21050f00]` are now also demoted to readback-only because the current firmware logs show stable `INVALID/result:false` behavior for their direct command path.
- [ParkingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ParkingActivity.java) no longer pretends that `BCM_CUSTOM_KEY_AUTO_PARK` is a valid writable shortcut; it now reports that the direct path is disabled on this firmware instead of claiming a sent command.

Door/HUD follow-up after manual vehicle clarification on Sunday, July 26, 2026:

- projection HUD is physically absent on this car, so direct HUD AdaptAPI actions must not stay exposed as normal interactive controls.
- `BCM_DOOR`, `BCM_DOOR_STATUS`, `BCM_DOOR_CONTROL`, and `BCM_DOOR_POS` still have active runtime evidence in `logs_1.25`, so door handling should stay on targeted per-zone paths instead of being broadly demoted together with unrelated BCM lock/custom-key failures.
- [HudActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/HudActivity.java) now shifts visible actions away from direct `HUD_ACTIVE` toggles and toward DIM / cluster / bridge tooling plus readback diagnostics.
- [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java) now prioritizes concrete door-zone actions and body status readback instead of presenting fake global lock/unlock shortcuts as the main body controls.

Diagnostics automation update on Sunday, July 26, 2026:

- Settings auto diagnostics no longer depends on `ACTION_CREATE_DOCUMENT` for its normal export path.
- diagnostics now run through a shared runner that performs:
  - support/readback coverage;
  - automatic write sweep across UI-facing command probes;
  - latest cache copy for reuse;
  - removable-SD export only, with no fallback export into internal storage.
- added exported adb/broadcast entrypoint `com.prodject.gflow.RUN_AUTODIAGNOSTICS` for headless triggering without opening UI and manually pressing buttons.
- added [scripts/collect-gflow-diagnostics.sh](/Volumes/Store/WORK_PROGRAMMER/GControl/scripts/collect-gflow-diagnostics.sh) to trigger the sweep over adb, wait for completion, collect logcat, and pull the newest removable-SD report automatically.

Stock-app logging helper update on Sunday, July 26, 2026:

- added [collect-adb-log-stock.sh](/Volumes/Store/WORK_PROGRAMMER/GControl/collect-adb-log-stock.sh) for manual reverse-engineering of stock/system apps.
- the script asks which package to track before log capture starts, so package selection is no longer hardcoded to GFlow.
- during capture the user can type comments like `:note climate fan +1` and they are saved into the session as `Введена функция: ...`, making button presses and stock-setting actions easier to correlate with log timestamps.

GInputBridge cross-check update on Sunday, July 26, 2026:

- local reference repo [`.src/examples/GInputBridge`](./examples/GInputBridge) confirms that it uses the same general vendor stack family as GFlow:
  - `AdaptAPI`;
  - `ECarXCarProxy`;
  - `CarSignalManager`.
- the most important backend finding is that `BCM_CUSTOM_KEY` is not handled there as a plain generic `ICarFunction.setFunctionValue(...)` write:
  - custom-key execution is routed through `ECarXCarVfmiscManager.CB_SelfDefineFuncReq(...)`;
  - that is consistent with the current GFlow repair direction where OEM-entry custom keys should not be treated as ordinary direct writable BCM properties.
- confirmed `BCM_CUSTOM_KEY` values from GInputBridge:
  - `360 panorama = 1`;
  - `navigation = 2`;
  - `full-screen map = 3`;
  - `sound switch = 4`;
  - `collect favorite = 5`;
  - `mirror adjust = 6`;
  - `loud speaker = 99`;
  - `trunk = 100`;
  - `auto park = 101`;
  - `driving mode = 102`.
- GInputBridge also applies a custom-key remap before sending values to the hardware API, which matters for future OEM-entry cleanup:
  - `3 -> 0`;
  - `0 -> 1`;
  - `1 -> 2`;
  - `2 -> 3`;
  - `4 -> 4`;
  - `5 -> 5`;
  - `99 -> 6`;
  - `6 -> 7`;
  - `100 -> 8`;
  - `101 -> 9`;
  - `102 -> 10`.
- confirmed body area IDs from GInputBridge match the OEM area model already used in the GFlow body repair work:
  - doors:
    - `front-left = 0x1`;
    - `front-right = 0x4`;
    - `rear-left = 0x10`;
    - `rear-right = 0x40`;
    - `hood = 0x10000000`;
    - `rear/trunk = 0x20000000`.
  - windows:
    - `front-left = 0x10`;
    - `front-right = 0x20`;
    - `rear-left = 0x100`;
    - `rear-right = 0x200`.
  - global zone:
    - `ZONE_ALL = Integer.MIN_VALUE`.
- confirmed `BCM_DOOR` values from GInputBridge:
  - `close = 0`;
  - `open = 1`;
  - `pause = 553779457`.
- confirmed HVAC value enums from GInputBridge match the corrected GFlow HVAC rewrite:
  - seat heating:
    - `off = 0`;
    - `level 1 = 0x10050101`;
    - `level 2 = 0x10050102`;
    - `level 3 = 0x10050103`;
    - `auto = 0x1005010f`.
  - seat ventilation:
    - `off = 0`;
    - `level 1 = 0x10050001`;
    - `level 2 = 0x10050002`;
    - `level 3 = 0x10050003`;
    - `auto = 0x1005000f`.
- confirmed parking-related property inventory from GInputBridge:
  - `PAS_FUNC_DRVR_ASSC_SYS_BTN_PUSH = 0x23100100`;
  - `DRVR_ASSC_SYS_BTN_PUSH_ENTER_APA_OR_AVM = 0x23100106`;
  - `PAS_FUNC_PAC_ACTIVATION = 0x23030000`;
  - `PAS_FUNC_PAS_ACTIVATED = 0x200d0000`;
  - `PAS_FUNC_PAS_VOLUME = 0x200d0100`.
- GInputBridge inventory also keeps `BCM_FUNC_CUSTOM_KEY`, `PAS_FUNC_PAS_ACTIVATED`, and `PAS_FUNC_PAS_VOLUME` in its practical property preset set, which makes it a useful cross-check for property discovery, naming, and enum mapping.
- important limitation:
  - these GInputBridge findings confirm backend shape, enum values, and OEM area IDs;
  - they do not prove that the same direct writes are actually writable on the target car firmware;
  - specifically, `CUSTOM_KEY_360 = 1` is confirmed as a vendor mapping, but user testing on the current firmware already showed that this alone does not open the stock `360` UI.

GSplit split-launch cross-check update on Sunday, July 26, 2026:

- local reference repo [`.src/examples/GSplit`](./examples/GSplit) shows that stable split launch there is implemented as a dedicated subsystem, not as one direct `startActivity(...)` call.
- the most useful architectural takeaway for GControl:
  - split launch should be isolated behind a dedicated launcher/orchestrator class;
  - UI buttons should call that orchestrator, not assemble split/freeform intents inline in activities.
- confirmed native split path from GSplit:
  - `NativeSplitModeUtil` uses `ActivityOptionsCompat.makeBasic()` plus hidden windowing bundle keys:
    - `android.activity.windowingMode = 3` for split primary;
    - `android:activity.splitScreenCreateMode = 0` for top/left dock.
  - it resets the current window mode first by sending the user to `HOME`, then launches both target intents with:
    - `FLAG_ACTIVITY_NEW_TASK`;
    - `FLAG_ACTIVITY_MULTIPLE_TASK`;
    - launcher category;
    - delayed paired launch.
- confirmed freeform fallback path from GSplit:
  - it starts an invisible `MultiWindowHeatingActivity`;
  - that activity uses a `1x1` untouchable window and exits immediately;
  - launch flags include:
    - `FLAG_ACTIVITY_NEW_TASK`;
    - `FLAG_ACTIVITY_LAUNCH_ADJACENT`;
    - `FLAG_ACTIVITY_NO_ANIMATION`.
- confirmed there is no single universal split backend even inside GSplit:
  - native split is only one mode;
  - the main repository also supports freeform-window launch with explicit bounds calculation through `ActivityOptions.setLaunchBounds(...)`-style behavior;
  - an additional accessibility-service path is used for experimental native split replacement, window closing, replacement, and recovery when OEM behavior is inconsistent.
- transferable implementation details worth moving into GControl:
  - add a dedicated split launcher/orchestrator instead of scattering window-launch code across activities;
  - add a small invisible “multiwindow heating” activity for freeform preparation on firmware where direct split launch is unreliable;
  - separate `native split`, `freeform`, and `accessibility fallback` modes explicitly in code and settings, because they are different backends with different failure modes;
  - compute window bounds centrally from real screen metrics, orientation, status-bar offset, and optional overlap/shift values instead of hardcoding one split geometry;
  - support a short staged launch sequence with configurable delays between first and second window, because GSplit explicitly relies on that timing to avoid “only second app opened” behavior.
- transferable operational patterns worth moving into GControl:
  - use a dedicated exported launcher activity for headless split start by preset or explicit packages instead of tying split launch to visible UI only;
  - expose broadcast entrypoints for `launch`, `launch last`, `replace window`, and `close split`, so split behavior can be triggered externally over adb/automation;
  - keep overlay/accessibility helpers as optional layers, not as the only implementation path.
- concrete GControl implication:
  - if split-screen in GControl is still flaky, the next repair should not be “tweak one intent flag”;
  - it should be a proper `SplitLaunchManager` with:
    - mode selection;
    - launch delays;
    - invisible preheat activity;
    - central bounds calculation;
    - optional accessibility fallback.
- important limitation:
  - GSplit proves practical launch patterns for Android automotive-style firmware, but it does not prove that every same flag/key combination will behave identically in GControl on this head unit;
  - migration should therefore be staged: first isolate backend, then add freeform/native launch modes, then validate on-device which path is actually stable.

EVCam camera-recording cross-check update on Sunday, July 26, 2026:

- local reference repo [`.src/examples/EVCam`](./examples/EVCam) confirms that its stable camera/recording behavior is built as a service stack, not around a visible activity lifecycle.
- the most useful architectural takeaway for GControl:
  - EVS / camera preview entry and camera recording must be separated;
  - anything expected to survive backgrounding, boot, or delayed signal triggers needs a dedicated service/controller path instead of living only in `CameraActivity`.
- confirmed service split from EVCam:
  - `CameraForegroundService` keeps the process alive, owns the foreground notification, wake-lock handling, remote-service startup, and restart/repair logic;
  - `service/CameraRecordingService` owns the actual recording/blind-spot commands through explicit actions:
    - `ACTION_START_RECORDING`;
    - `ACTION_STOP_RECORDING`;
    - `ACTION_START_BLIND_SPOT`;
    - `ACTION_STOP_BLIND_SPOT`.
  - `recording/RecordingController` is a separate state machine for `IDLE / INITIALIZING / RECORDING / PAUSED / STOPPING / ERROR`, instead of mixing recording state directly into UI.
- confirmed boot/background orchestration pattern from EVCam:
  - `BootReceiver` immediately starts the foreground service on `BOOT_COMPLETED` / `QUICKBOOT_POWERON`;
  - delayed initialization is done afterwards instead of blocking boot handling;
  - a transparent `TransparentBootActivity` is used only when camera initialization really requires an activity context.
- this is directly relevant to GControl because the same pattern can stabilize camera-related automation:
  - do not require the visible camera screen to be open for every delayed or boot-triggered action;
  - keep camera startup logic behind service-safe commands first, then escalate to activity launch only when the backend truly needs a foreground camera context.
- confirmed vehicle-signal integration patterns from EVCam:
  - there are two distinct signal backends:
    - reflection-based `CarSignalManagerObserver` / `DoorSignalObserver` via `ecarxcar_service` and `getCarManager(\"car_signal\", ...)`;
    - native `VhalSignalObserver` using a JNI bridge (`libvhal_decoder.so`) plus gRPC stream decoding.
  - `CarSignalManagerObserver` polls `getIndcrSts()` and `DoorSignalObserver` polls:
    - `getDoorDrvrSts()`;
    - `getDoorPassSts()`;
    - `getDoorLeReSts()`;
    - `getDoorRiReSts()`.
- practical implication for GControl:
  - if some body/camera-trigger workflows remain unreliable through generic `ICarFunction` writes, side-channel observers for turn-signal / door status can be useful as trigger sources even when they are not command backends;
  - these observers should live in a dedicated vehicle-signal layer, not inside UI activities.
- confirmed native VHAL bridge shape from EVCam:
  - `VhalNative` exposes:
    - gRPC host/port discovery;
    - stream/send-all method names;
    - native `decode(byte[])`;
    - configurable custom-key speed/button property IDs.
  - decoded event types include:
    - turn signal;
    - door open;
    - door close;
    - speed;
    - custom key.
- this is useful for GControl as a research direction:
  - if the head unit exposes the same vehicle API stream, a native bridge could become a better source of passive signals than logcat scraping or fragile UI polling;
  - this should be treated as an optional advanced backend, not as the first repair path.
- confirmed automotive persistence/whitelist pattern from EVCam assets:
  - [add_evcam_config.sh](/Volumes/Store/WORK_PROGRAMMER/GControl/.src/examples/EVCam/app/src/main/assets/add_evcam_config.sh) patches three system files:
    - `/system/etc/geely_lifectl_start_list.xml`;
    - `/system/etc/ecarx_str_policies.xml`;
    - `/vendor/etc/bgms_config.xml`.
  - the script explicitly preserves `com.geely.avm_app` while editing BGMS config, which is a strong hint that camera/AVM-related background behavior on this firmware is tied to system whitelist/start-list integration.
- concrete GControl implication:
  - if long-lived camera or EVS helpers are needed, they may require the same kind of Geely/ECARX whitelist/start-list treatment rather than only manifest/service changes inside the APK;
  - any such script must be defensive and must preserve stock `avm_app` entries exactly, just like EVCam does.
- confirmed recording backend details worth copying conceptually, not blindly:
  - `MultiCameraManager` is centralized and owns:
    - multi-camera session setup;
    - segmented recording timing;
    - watchdog/rebuild fallback;
    - alternate recording backends (`MediaRecorder` vs `MediaCodec`);
    - final-save vs relay-write storage behavior.
  - this supports the same design rule for GControl:
    - one central camera/session manager should own EVS/camera lifecycle, not scattered button handlers.
- transferable items worth moving into GControl:
  - add a dedicated camera/recording service layer with explicit actions;
  - add a separate recording/EVS state controller instead of deriving state from UI widgets;
  - add boot-safe foreground-service startup before any heavy camera work;
  - isolate signal observers from UI and reuse them for automation triggers;
  - keep any future whitelist/system-config tooling separate, explicit, and reversible.
- important limitation:
  - EVCam is a practical automotive camera app, but much of its stack targets direct Camera2 multi-recording rather than the existing EVS-open flow already used in GControl;
  - the safest migration path is therefore to borrow:
    - service architecture;
    - signal observation;
    - boot/keep-alive orchestration;
    - whitelist strategy;
  - central session/state management;
  - but not to blindly transplant its full multi-camera recorder into GControl without first deciding whether GControl should stay EVS-centric or become a real recorder app.

GInputBridge backend transfer update on Sunday, July 26, 2026:

- the first concrete runtime transfer from GInputBridge has now been applied to GControl backend code.
- [CarBridge.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/CarBridge.java) now has a dedicated reflection path for OEM `Vfmisc` access:
  - `getCarManager(context, "car_publicattribute")`;
  - `getECarXCarVfmiscManager()`;
  - fallback service lookup through `ecarxcar_service` / `IECarXCar.Stub.asInterface(...)` when the overloaded manager getter needs the remote binder instance.
- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java) no longer leaves `BCM_CUSTOM_KEY` as a descriptive `OEM_ENTRY` placeholder only:
  - `BCM_CUSTOM_KEY` is now marked writable on the `OEM_ENTRY` backend;
  - direct writes for that function now route to `CB_SelfDefineFuncReq(...)` instead of `ICarFunction.setFunctionValue(...)`;
  - support probing for `BCM_CUSTOM_KEY` now checks whether the `Vfmisc` manager is actually obtainable on-device.
- the `GInputBridge` custom-key remap was transferred into GControl for the write path:
  - `DIM_FULL_SCREEN_MAP 3 -> 0`;
  - `DVR 0 -> 1`;
  - `360 1 -> 2`;
  - `NAVIGATION 2 -> 3`;
  - `SOUND_SWITCH 4 -> 4`;
  - `COLLECT_FAV 5 -> 5`;
  - `LOUD_SPEAKER 99 -> 6`;
  - `REAR_MIRROR_ADJUST 6 -> 7`;
  - `TRUNK 100 -> 8`;
  - `AUTO_PARK 101 -> 9`;
  - `DRIVING_MODE 102 -> 10`.
- practical effect of this transfer:
  - custom-key actions like trunk / DVR / driving-mode now have a real OEM backend path in GControl instead of falling through to the broken generic BCM write assumption;
  - this is the first actual code migration from GInputBridge, not just cross-reference documentation.
- still open after this transfer:
  - verify on-device which custom keys truly execute on the target firmware through `Vfmisc`;
  - keep `360` and `auto park` behavior conservative in UI until logs confirm that those specific custom-key actions really launch the expected stock screens on this head unit.

GInputBridge entrypoint transfer update on Sunday, July 26, 2026:

- the next transfer step from GInputBridge was to stop treating confirmed custom-key values as backend-only constants and expose them through real GControl entrypoints.
- [VehicleActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VehicleActivity.java) now has an explicit `OEM Custom Keys` card wired through the new `Vfmisc` backend for:
  - `Trunk`;
  - `DVR`;
  - `Navigation`;
  - `Full Map`.
- `Custom Drive` in the same screen continues to use `BCM_CUSTOM_KEY / CUSTOM_KEY_DRIVING_MODE`, but it now benefits from the real OEM backend that was added in the previous step.
- [VoiceActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/VoiceActivity.java) now routes additional confirmed GInputBridge custom-key actions through `BCM_CUSTOM_KEY` as well:
  - navigation / map;
  - full-screen map;
  - custom-drive menu;
  - trunk;
  - DVR.
- practical effect:
  - confirmed GInputBridge custom keys are no longer stranded as raw constants in the adapter;
  - they now have user-facing entrypoints in both UI and voice, all going through the same backend instead of one-off assumptions.
- still open after this step:
  - verify on-device which of these OEM custom-key entrypoints actually open the expected stock experiences on this firmware;
  - if some of them still no-op despite successful `Vfmisc` calls, split them into:
    - `backend accepted`;
    - `stock UI actually opened`;
    so diagnostics stay honest.

GInputBridge parking inventory transfer update on Sunday, July 26, 2026:

- the next safe transfer from GInputBridge was not to re-enable speculative PAS writes, but to bring its confirmed parking property inventory into GControl so diagnostics and future fixes use the same property map.
- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java) now includes the confirmed GInputBridge parking properties:
  - `PAS_DRVR_ASSC_SYS_BTN_PUSH = 0x23100500`;
  - `PAS_DRVR_ASSC_SYS_PARK_MOD = 0x23100700`;
  - `PAS_AUT_PRKG_SLOT_NR_REQ = 0x23100600`.
- the confirmed GInputBridge value map for those properties was also added as constants for reference and diagnostics:
  - button-push values such as:
    - `SELT_APA = 0x23100501`;
    - `SELT_RPA = 0x23100502`;
    - `UNDO = 0x23100503`;
    - `ENTER_APA_OR_AVM = 0x23100506`;
    - `MANUAL = 0x23100507`;
    - `ENTER_APA = 0x23100509`;
    - `CONFIRM_PARK_OUT = 0x2310050a`;
    - `SUSPEND = 0x2310050b`;
    - `ABORT = 0x2310050c`.
  - park-mode values such as:
    - `HORIZ_PARK_IN = 0x23100702`;
    - `PERPDIR_PARK_IN = 0x23100703`;
    - `HORIZ_LEFT_PARK_OUT = 0x23100709`;
    - `HORIZ_RIGHT_PARK_OUT = 0x2310070a`;
    - left/right forward/backward park-out variants through `0x2310070e`.
- these properties were classified into the existing PAS direct-property bucket, which keeps them diagnostics-first on this firmware instead of falsely promoting them to confirmed writable controls.
- [ParkingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/ParkingActivity.java) now surfaces this inventory explicitly:
  - a new `GInputBridge PAS inventory` diagnostic block was added to the APA section;
  - advanced PAC / AVM diagnostics now also include `PAS_DRVR_ASSC_SYS_BTN_PUSH` and `PAS_DRVR_ASSC_SYS_PARK_MOD`;
  - the APA status summary now appends direct property readback for those two confirmed GInputBridge parking properties next to the existing `CarSignalManager` signals.
- practical effect:
  - parking diagnostics now align much more closely with the property map proven in GInputBridge;
  - the next parking debug pass can compare:
    - signal-manager state;
    - direct PAS property readback;
    - UI behavior;
    without inventing new property IDs on the fly.
- still open after this step:
  - confirm on-device whether these direct PAS properties ever report useful live values on the target firmware;
  - only after that decide whether any of them deserve promotion from diagnostics-first to a real control path in GControl.

GInputBridge diagnostics-group transfer update on Sunday, July 26, 2026:

- the next transfer step was to stop keeping the new GInputBridge-confirmed inventory only inside individual screens and expose it in the shared diagnostics surfaces as well.
- [SettingsActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SettingsActivity.java) and [DiagnosticsRunner.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DiagnosticsRunner.java) now include the extra confirmed GInputBridge parking properties inside the common `Parking / APA / AVM` diagnostics group:
  - `PAS_DRVR_ASSC_SYS_BTN_PUSH`;
  - `PAS_DRVR_ASSC_SYS_PARK_MOD`;
  - `PAS_AUT_PRKG_SLOT_NR_REQ`.
- both shared diagnostics surfaces now also have a dedicated `OEM Custom Keys` group containing:
  - `BCM_CUSTOM_KEY`;
  - `CUSTOM_KEY_DVR`;
  - `CUSTOM_KEY_TRUNK`;
  - `CUSTOM_KEY_360`;
  - `CUSTOM_KEY_NAVIGATION`;
  - `CUSTOM_KEY_DIM_FULL_SCREEN_MAP`;
  - `CUSTOM_KEY_SOUND_SWITCH`;
  - `CUSTOM_KEY_COLLECT_FAV`;
  - `CUSTOM_KEY_REAR_MIRROR_ADJUST`;
  - `CUSTOM_KEY_LOUD_SPEAKER`;
  - `CUSTOM_KEY_AUTO_PARK`;
  - `CUSTOM_KEY_DRIVING_MODE`.
- practical effect:
  - auto-diagnostics and settings diagnostics now see the same custom-key and parking inventory already transferred into the runtime backend;
  - future log collection can compare these properties across:
    - per-screen diagnostics;
    - shared settings diagnostics;
    - headless diagnostic runs.
- this keeps the GInputBridge transfer consistent: inventory is now reflected in backend, UI/voice entrypoints, parking diagnostics, and shared diagnostic sweeps rather than being fragmented.

GInputBridge ambience-light and auto-park cross-check update on Sunday, July 26, 2026:

- a direct pass over the local [GInputBridge](/Volumes/Store/WORK_PROGRAMMER/GControl/.src/examples/GInputBridge) source confirmed that it contains two more relevant blocks:
  - a large `IAmbienceLight` / `IVehicle` ambience-light inventory;
  - extra `IPAS` / custom-key auto-park inventory.
- ambience-light result:
  - there is no missing backend breakthrough here; the main transferable ambience block is already present in GControl.
  - [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java) already carries the same core property IDs confirmed by GInputBridge, including:
    - `AMBIENCE_LIGHT_THEME_COLOR`;
    - `AMBIENCE_LIGHT_EFFECT`;
    - `AMBIENCE_LIGHT_CONTROL_MODE`;
    - `AMBIENCE_LIGHT_MUSIC`;
    - `AMBIENCE_LIGHT_MUSIC_SHOW_MODE`;
    - `AMBIENCE_LIGHT_WELCOME_SHOW`;
    - `AMBIENCE_LIGHT_WELCOME_SHOW_MODE`;
    - `AMBIENCE_LIGHT_VOICE`;
    - `AMBIENCE_LIGHT_ZONE_EXPERIENCE`;
    - `AMBIENCE_LIGHT_MAIN_ZONES`;
    - `AMBIENCE_LIGHT_TOP_ZONES`;
    - `AMBIENCE_LIGHT_BOT_ZONES`.
  - [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) already exposes these as a real experimental screen with:
    - theme colors;
    - theme/effect presets;
    - control modes;
    - welcome/music/voice toggles;
    - zone selection;
    - diagnostics/readback block.
  - [SettingsActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SettingsActivity.java) and [DiagnosticsRunner.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DiagnosticsRunner.java) also already include ambience-light in shared diagnostics.
- auto-park result:
  - GInputBridge confirms again that `CUSTOM_KEY_TYPE_AUTO_PARK = 101` exists as a vendor custom key, and that several PAS properties belong to the same parking cluster already being imported into GControl.
  - the custom-key side is already transferred into GControl through the `Vfmisc` backend remap:
    - `CUSTOM_KEY_AUTO_PARK = 0x65` maps to hardware API slot `9`.
  - the direct PAS-property side is also already partially transferred:
    - `PAS_DRVR_ASSC_SYS_BTN_PUSH`;
    - `PAS_DRVR_ASSC_SYS_PARK_MOD`;
    - `PAS_AUT_PRKG_SLOT_NR_REQ`.
  - GInputBridge also shows additional parking-related inventory worth keeping in mind for later verification:
    - `PAS_FUNC_APA_SELF_RECOMMENDED`;
    - `PAS_FUNC_APA_DETECT_PARKING_SPACE`;
    - `PAS_FUNC_APA_RPA_SWITCH`;
    - `PAS_FUNC_PRKG_INTRPT_RELD_BTN`;
    - `PAS_FUNC_SAP_ACTIVATION`;
    - `PAS_FUNC_RCTA_WARNING_VOLUME`.
- practical conclusion:
  - ambience-light is not the missing piece right now, because the core GInputBridge-backed implementation is already in GControl;
  - auto-park is only partially transferred on purpose: inventory and backend hooks are there, but stock-firmware behavior is still not proven enough to promote every PAS path to a normal user-facing control.
- still open after this cross-check:
  - if needed, import the remaining GInputBridge parking inventory as diagnostics-only constants first, not as writable UI actions;
  - keep treating `CUSTOM_KEY_360` and `CUSTOM_KEY_AUTO_PARK` separately from "stock UI definitely opened", because backend acceptance alone is not sufficient proof on this firmware.

GInputBridge ambience-entrypoint and diagnostics inventory transfer on Sunday, July 26, 2026:

- the next practical gap was not backend support but discoverability: the ambience-light screen already existed in code, but had no obvious route from the current home UI, which is why it was effectively invisible during manual testing.
- [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) now exposes `Подсветка` from real user-facing entrypoints:
  - a new drawer action was added in the home navigation drawer;
  - the `Vehicle` dock quick sheet now also links directly to the ambience screen.
- the existing `Experimental: Подсветка` screen was also expanded with a dedicated `GInputBridge ambience extras` diagnostics block so the remaining vendor inventory is visible without pretending it is fully proven writable on this firmware.
- [EcarxVehicleAdapter.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/EcarxVehicleAdapter.java) now includes additional ambience-light properties confirmed from local `GInputBridge`, including:
  - `AMBIENCE_LIGHT_BRIGHTNESS_DRIVING = 0x200a0700`;
  - `AMBIENCE_LIGHT_BRIGHTNESS_STATIONARY = 0x200a0600`;
  - `AMBIENCE_LIGHT_COLOR_TYPE = 0x200a0a00`;
  - `AMBIENCE_LIGHT_CLIMATE = 0x2a080200`;
  - `AMBIENCE_LIGHT_GOODBYE_SHOW = 0x2a050200`;
  - `AMBIENCE_LIGHT_PHONE_CALL_REMINDER = 0x2a050400`;
  - `AMBIENCE_LIGHT_SLIDING_DOOR_REMINDER = 0x2a050900`;
  - `AMBIENCE_LIGHT_INTERACTIVE_EFFECT = 0x200a0800`;
  - `AMBIENCE_LIGHT_SOLID_COLOR_SET = 0x2a500000`;
  - `AMBIENCE_LIGHT_BREATHE_COLOR_SET = 0x2a500100`;
  - `AMBIENCE_LIGHT_ENDURANCE_MILE_REMINDER = 0x2a050500`;
  - `AMBIENCE_LIGHT_ICHARGING_REMIND = 0x2a080300`.
- the same step also imported the next safe parking inventory from `GInputBridge` as diagnostics/readback IDs:
  - `PAS_APA_SELF_RECOMMENDED = 0x23060100`;
  - `PAS_APA_DETECT_PARKING_SPACE = 0x23a80100`;
  - `PAS_APA_RPA_SWITCH = 0x23a80200`;
  - `PAS_PRKG_INTRPT_RELD_BTN = 0x23110600`.
- these extra parking IDs were added to the PAS direct-property bucket on purpose, which keeps them diagnostics-first instead of silently promoting them to normal write actions.
- [SettingsActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SettingsActivity.java) and [DiagnosticsRunner.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DiagnosticsRunner.java) now include those new ambience and parking properties in shared diagnostics as well.
- practical effect:
  - you now have a visible path to the ambience screen from the running UI;
  - the remaining ambience and APA/RPA inventory proven by `GInputBridge` is now visible in both on-screen and shared diagnostics;
  - future stock-app or manual tests can compare those IDs against runtime behavior without adding speculative write buttons first.
- still open after this step:
  - decide later which of the new ambience extras deserve real command widgets rather than diagnostics-only exposure;
  - collect logs before promoting any of the newly imported APA/RPA properties to ordinary parking controls.

GSplit backend transfer update on Sunday, July 26, 2026:

- the previous `GControl` split code was still only a thin UI hack:
  - [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) directly queried launcher apps and fired one `startActivity(...)` with adjacent flags;
  - there was no dedicated backend, no persisted last split, no freeform preheat, and no headless trigger path.
- that gap is now closed with a dedicated split backend layer modeled on the `GSplit` architecture, but reduced to the pieces that actually fit `GControl` today.
- new core backend file:
  - [SplitLaunchManager.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SplitLaunchManager.java)
- what this backend now provides:
  - central launch orchestration instead of inline split logic inside activities;
  - persisted `last split` configuration in `gflow_split` preferences;
  - explicit split modes:
    - `native`;
    - `freeform`;
    - `adjacent`.
  - configurable persisted timing inputs:
    - `second_window_delay_ms`;
    - `bottom_window_shift`.
- the `GSplit`-style headless execution path was also transferred:
  - [SplitLauncherActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SplitLauncherActivity.java) is now a dedicated no-UI launcher activity that receives resolved package pairs and runs the backend;
  - [MultiWindowHeatingActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MultiWindowHeatingActivity.java) was added as the invisible freeform preheat activity, following the same architectural role as in `GSplit`.
- broadcast entrypoints were added as well:
  - [SplitCommandReceiver.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/SplitCommandReceiver.java)
  - manifest actions now exposed:
    - `com.prodject.gflow.SPLIT_LAUNCH`;
    - `com.prodject.gflow.SPLIT_LAUNCH_LAST`;
    - `com.prodject.gflow.SPLIT_CLOSE`.
- transferred native-split behavior:
  - `SplitLaunchManager` now uses the same `GSplit` keys for native split launch:
    - `android.activity.windowingMode = 3`;
    - `android:activity.splitScreenCreateMode = 0`.
  - native split is launched through a staged reset-and-launch sequence rather than one naked adjacent intent.
- transferred freeform behavior:
  - freeform launch now goes through:
    - invisible multiwindow preheat;
    - central bounds calculation;
    - reflective `ActivityOptions.setLaunchWindowingMode(5)`;
    - central `setLaunchBounds(...)` application.
  - bounds are now calculated once in the backend from real display metrics and status-bar height instead of being scattered in UI code.
- user-facing wiring was updated so the backend is actually reachable:
  - [MainActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/MainActivity.java) now exposes `Split Apps` in the main drawer and routes package-pair selection into `SplitLaunchManager`;
  - [DesktopActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DesktopActivity.java) now exposes split launch in:
    - overview navigation;
    - bottom dock;
    - per-app `Split` action from the app library.
- practical effect:
  - split launch in `GControl` is no longer one isolated adjacent-intent hack;
  - there is now one place to evolve timings, mode fallback, launch bounds, and external trigger behavior;
  - adb or automation can trigger the same split backend through broadcast actions instead of only through visible UI taps.
- still open after this step:
  - add an explicit settings UI for changing split mode/delay/shift instead of reusing persisted defaults only;
  - if native split still behaves inconsistently on the target firmware, the next step should be accessibility-based `replace/close split` support, not more inline flag tweaking.

EVCam service-stack transfer update on Sunday, July 26, 2026:

- the previous camera stack in `GControl` was still too close to a UI-driven utility layout:
  - `CameraActivity` and `DvrActivity` triggered recording directly through `DvrService`;
  - `DvrService` mixed recording orchestration, EVS opening, screenrecord fallback, segment rotation, and foreground lifetime in one legacy class;
  - low-speed 360 opening bypassed any common camera service layer.
- the practical `EVCam` takeaway was transferred as a service split, not as a blind Camera2 recorder transplant.
- new service-stack files added:
  - [RecordingStateController.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/RecordingStateController.java)
  - [CameraForegroundService.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/CameraForegroundService.java)
  - [CameraRecordingService.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/CameraRecordingService.java)
  - [TransparentBootActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/TransparentBootActivity.java)
- transferred architecture from `EVCam` into `GControl`:
  - a dedicated foreground keepalive service now exists separately from recording commands;
  - the actual recording/EVS command backend now lives in `CameraRecordingService`;
  - recording state is no longer implicit UI state and is tracked centrally through `RecordingStateController` with:
    - `IDLE`;
    - `INITIALIZING`;
    - `RECORDING`;
    - `STOPPING`;
    - `ERROR`.
- `CameraRecordingService` now exposes explicit actions in the same spirit as `EVCam`'s recording service split:
  - `ACTION_START_RECORDING`;
  - `ACTION_STOP_RECORDING`;
  - `ACTION_OPEN_EVS`;
  - `ACTION_CLOSE_EVS`.
- practical migration details:
  - the old [DvrService.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DvrService.java) was demoted into a compatibility shim that simply forwards legacy `DVR_START / DVR_STOP` requests to `CameraRecordingService`;
  - this preserves compatibility with existing callers while moving real behavior into the new service backend.
- boot/background orchestration was also aligned with the `EVCam` model:
  - [BootReceiver.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/BootReceiver.java) now starts `CameraForegroundService` at boot in addition to the previous voice/automation work;
  - `TransparentBootActivity` was added as the minimal transparent activity counterpart for future camera-safe boot flows that need an activity context.
- shared callers were rewired to the new backend:
  - [AutomationEngine.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/AutomationEngine.java) now sends `start_dvr / stop_dvr` through `CameraRecordingService`;
  - [LowSpeedCameraService.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/LowSpeedCameraService.java) now delegates low-speed `360` opening into `CameraRecordingService` instead of opening EVS directly on its own;
  - [CameraActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/CameraActivity.java) and [DvrActivity.java](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/java/com/prodject/gflow/DvrActivity.java) now call the new explicit camera actions rather than talking to the old monolithic service path.
- user-visible improvement:
  - camera screens now show central recording state/readback from `RecordingStateController`, so the UI is reading one common backend state instead of guessing from button presses.
- manifest updates:
  - `CameraForegroundService`, `CameraRecordingService`, and `TransparentBootActivity` were added to [AndroidManifest.xml](/Volumes/Store/WORK_PROGRAMMER/GControl/app/src/main/AndroidManifest.xml).
- practical effect:
  - EVS open/close, recording start/stop, boot persistence, and automation now all go through one shared camera backend layer;
  - this moves `GControl` much closer to the stable `EVCam` service model without forcing a premature migration to full multi-camera `Camera2` recorder internals everywhere.
- still open after this step:
  - if `GControl` later needs true multi-camera simultaneous recording rather than the current mixed `Camera2` / EVS-screenrecord approach, that should be a separate deliberate migration from `EVCam`'s `MultiCameraManager`;
  - background whitelist/start-list scripts from `EVCam` were intentionally not transplanted automatically, because those touch system files and should stay explicit and reversible.

GFlow `log_1.32` analysis on Friday, July 31, 2026:

- catalog sweep сработал и теперь даёт рабочую карту поддержки по каталогу:
  - `total=802`;
  - `SUPPORTED_READ_OK=107`;
  - `SUPPORTED_WRITE_OK=60`;
  - `WRITE_FAIL=9`;
  - `UNSUPPORTED=686`;
  - `READ_ONLY=9`;
  - `NO_VALUES=524`.
- write sweep:
  - `OK=51`;
  - `UNSUPPORTED=25`.
- климат:
  - `0x10070100` blowing mode с `zone=0x8` теперь `OK`;
  - `0x10050100` seat ventilation всё ещё `unsupported`.
- drive:
  - `0x22010100` принимает много experimental values:
    - Offroad `0x22010113`;
    - Mud `0x2201010a`;
    - Rock `0x2201010b`;
    - Sand `0x2201010d`;
    - AWD `0x2201010e`;
    - eAWD `0x22010112`;
    - Adaptive `0x22010116`;
    - Custom `0x22010140`;
    - Eco+ `0x22010114`;
    - Sport+ `0x22010115`;
    - Start Type18 / Type72 / Type79 / Type97.
  - эти режимы можно переносить из диагностики в UI как кандидаты с readback.
- drive values that did not confirm:
  - `0x22040500` energy mode — `unsupported`;
  - `0x22040700` launch control — `unsupported`;
  - `0x22040d00` ESC level — `unsupported`;
  - `0x22040e00` StarTrack — `unsupported`;
  - `0x22030c00` custom driver info — `unsupported`;
  - `0x22030100` custom propulsion частично: Offroad/Sand `OK`, AWD `unsupported`.
- ADAS:
  - `0x20070700` LCA с value `1` теперь `OK`;
  - `0x200e0200` FCW sensitivity `OK`;
  - `0x28062100` speed offset fallback `OK`;
  - `0x20070e00`, `0x20070600`, `0x20070d00`, `0x200b0100`, `0x28060200` — `OK`.
- PDC:
  - `0x23030100` `PAS_PAC_ACTIVATION = 1` — `OK`;
  - `0x23021000` `PAS_RADAR_WORK_MODE` — `unsupported`;
  - рабочий PDC-кандидат для UI: `PAS_PAC_ACTIVATION`, не `0x20060300` и не radar work mode.
- дверь водителя:
  - `0x21020100` `BCM_FUNC_DOOR` по всем зонам `unsupported`;
  - в логе есть активный `0x21021000` `BCM_FUNC_DOOR_CONTROL` readback/support;
  - это новый кандидат вместо `0x21020100`.
- руль:
  - лог показывает низкоуровневый bridge через `0x2141f000`, `funcId=18`;
  - частые циклические команды:
    - `commandId=50 value=126/79`;
    - `commandId=0 value=1/0`;
    - `commandId=13 value=0/1`;
    - `commandId=2 value=7/2`;
    - `commandId=15 value=10/6`;
    - `commandId=17 value=0/255`.
  - редкие события, похожие на реальные нажатия:
    - `commandId=33 value=1`;
    - `commandId=34 value=2`;
    - `commandId=33 value=2`;
    - `commandId=34 value=1`;
    - `commandId=33 value=4`;
    - `commandId=33 value=0`.
  - точного соответствия “какая кнопка = какое значение” пока нет;
  - нужен лог с маркерами: нажал кнопку X, подождал 2 сек, нажал кнопку Y.
- next fixes:
  - в UI/diagnostics для PDC добавить action-кандидат `0x23030100 = 1`;
  - для двери водителя добавить diagnostics/write candidate `0x21021000 BCM_FUNC_DOOR_CONTROL`;
  - Drive UI расширить confirmed experimental modes из `0x22010100`;
  - спрятать/disable:
    - launch control;
    - ESC level;
    - StarTrack;
    - energy mode;
    - radar work mode.
  - для руля добавить raw logger / marker screen, иначе соответствие кнопок будет гаданием.

GFlow UI cleanup after `log_1.32` on Friday, July 31, 2026:

- moved confirmed `0x22010100` experimental drive values into UI candidates:
  - Offroad `0x22010113`;
  - Mud `0x2201010a`;
  - Rock `0x2201010b`;
  - Sand `0x2201010d`;
  - AWD `0x2201010e`;
  - eAWD `0x22010112`;
  - Adaptive `0x22010116`;
  - Custom `0x22010140`;
  - Eco+ `0x22010114`;
  - Sport+ `0x22010115`;
  - Start Type18 / Type72 / Type79 / Type97.
- custom propulsion `0x22030100` left only with confirmed candidates:
  - Offroad;
  - Sand.
- removed / hidden from normal UI because `log_1.32` confirmed unsupported:
  - `0x10050100` seat ventilation;
  - `0x21020100` direct BCM door open path;
  - `0x2d411100` seat one-key comfort;
  - `0x23021000` PAS radar work mode write actions;
  - custom propulsion AWD;
  - `0x22040500` energy mode;
  - `0x22040700` launch control;
  - `0x22040d00` ESC level;
  - `0x22040e00` StarTrack;
  - `0x22030c00` custom driver info.
- PDC user action changed to confirmed candidate:
  - use `0x23030100 PAS_PAC_ACTIVATION = 1`;
  - do not expose `0x20060300 ADAS_PDC` or `0x23021000 PAS_RADAR_WORK_MODE` as user write controls.
- 360 camera note:
  - `log_1.32` confirms the current camera path is useful;
  - UI should treat AVM 360 as HAL/EVS open path, not as unsupported `PAS_*` view-selection writes.

GFlow `log_1.32/alls` analysis on Friday, July 31, 2026:

- sessions with user comments were found under `.src/log_1.32/alls`.
- steering wheel buttons:
  - high-level InputService keycodes are present and are better for remapping than raw HAL guessing:
    - assistants button: keyCode `6`;
    - lane keeping button: keyCode `300050`;
    - 360 button: keyCode `119`;
    - assistant speed button: keyCode `5`;
    - assistant change 1: keyCode `3`;
    - assistant change 2: keyCode `4`;
    - voice assistant: keyCode `200231`;
    - mute: keyCode `200164`;
    - music button: keyCode `17`;
    - music mode switch: keyCode `200110`;
    - music previous: keyCode `200088`;
    - music next: keyCode `200087`;
    - volume: keyCode `300031` / `300030`, plus keyCode `20` / `19` events.
  - raw HAL bridge is also visible on `0x2141f000`:
    - music previous: `funcId=30 commandId=181 value=1`, then `value=0`;
    - music next: `funcId=30 commandId=182 value=1`, then `value=0`;
    - volume: `funcId=30 commandId=183/184/185`, values `0/1/2`.
  - practical conclusion:
    - use InputService/keyCode path for steering button remap UI;
    - keep `0x2141f000` funcId 30 command IDs as diagnostics/raw fallback only.
- driver door buttons / window buttons:
  - physical window events are visible through `0x2141f000 funcId=30`:
    - commandId `38` = driver window position status;
    - commandId `39` = passenger front window position status;
    - commandId `40` = rear-left window position status;
    - commandId `41` = rear-right window position status.
  - related AdaptAPI callback:
    - `IBcm.BCM_FUNC_WINDOW_POS [0x21030300]`;
    - zones:
      - driver/front-left `0x10`;
      - passenger/front-right `0x20`;
      - rear-left/rear-right follow the same window zone model.
    - supported values shown by stock: `0, 12, 16, 20, ... 100`;
    - observed values include `0.0`, `4.0`, `8.0`, `12.0`.
  - `SETTING_FUNC_WINDOW_VENTILATE [0x20080600]` appears as read/status related to the same physical window events.
  - central lock:
    - `0x2141f000 funcId=30 commandId=57`;
    - `value=3` maps to central lock ON / locked callback;
    - `value=1` maps to central lock OFF / unlocked callback;
    - AdaptAPI callback: `IVehicle.SETTING_FUNC_CENTRAL_LOCK [0x20100900]`, values `1/0`.
  - practical conclusion:
    - direct `BCM_DOOR [0x21020100]` remains wrong for door open/write;
    - for lock/unlock UI use `SETTING_FUNC_CENTRAL_LOCK [0x20100900]`;
    - for window UI/readback prefer `BCM_FUNC_WINDOW_POS [0x21030300]` by zone and percent.
- 360 steering button:
  - the 360 button session again shows the useful path:
    - keyCode `119`;
    - `PAS_PAC_ACTIVATION [0x23030100]`;
    - low-level AVM HAL props around `0x214085e6`, `0x2141f000`, `0x21417523`.
  - practical conclusion:
    - 360 UI should keep the current AVM HAL / EVS open path;
    - steering-button remap can listen for keyCode `119`.
- parking / hazard buttons:
  - session shows `0x2141f000 funcId=17 commandId=46 value=0/15`;
  - this is a candidate for physical parking/hazard state, but mapping is not yet strong enough to write UI control from it.
  - practical conclusion:
    - add to diagnostics/raw logger;
    - do not expose as a normal write action yet.

GFlow implementation from `log_1.32/alls` on Friday, July 31, 2026:

- steering remap UI examples now use confirmed keyCodes:
  - 360: `119`;
  - voice assistant: `200231`;
  - mute: `200164`;
  - media previous: `200088`;
  - media next: `200087`.
- central lock was added as a real writable AdaptAPI function:
  - `SETTING_FUNC_CENTRAL_LOCK [0x20100900]`;
  - values: `1` lock, `0` unlock;
  - visible lock UI and voice lock command now use this path instead of `BCM_DOOR_LOCK`.
- window UI was changed to hold-to-move levers:
  - press/hold sends `BCM_WINDOW [0x21030100]` open/close by zone;
  - release/cancel sends `WINDOW_OPEN_PAUSE` / `WINDOW_CLOSE_PAUSE`;
  - `BCM_FUNC_WINDOW_POS [0x21030300]` stays for readback/diagnostics by zone and percent, not fixed 12/50 user buttons.
- direct door-open UI remains developer-gated:
  - `BCM_DOOR [0x21020100]` is still not a confirmed user write path.
- diagnostics now include:
  - `SETTING_FUNC_CENTRAL_LOCK`;
  - `BCM_FUNC_WINDOW_POS`;
  - raw `0x2141f000` candidates from `alls` for parking/hazard, window physical status, and central-lock physical status.

GFlow diagnostics restore update on Friday, July 31, 2026:

GFlow parking/HUD direct-action cleanup on Saturday, August 1, 2026:

- removed direct PAS/PAC/AVM write entrypoints from the visible parking UI:
  - removed `RCTA on/off` tiles;
  - removed `PDC / PAC start`;
  - removed `overlay steerpath`;
  - removed direct `360` launch buttons from the parking quick/primary shortcut layers.
- `ParkingActivity` now keeps parking UI focused on:
  - OEM Auto Park entry;
  - signal-based APA controls;
  - readback/diagnostics.
- `360` open in both parking and vehicle screens no longer tries the direct AVM HAL write path first.
  - removed the `AvmHalAdapter.open360()` user-facing attempt;
  - user-facing `360` open now uses EVS only.
- removed direct HUD / DIM AdaptAPI action buttons from visible UI surfaces:
  - removed HUD/DIM quick actions in `HudActivity`;
  - removed direct HUD/DIM action grid/chips in `HudActivity`;
  - removed direct HUD/DIM dock actions in `HudActivity`;
  - removed direct HUD/DIM overview toggles in `MainActivity`.
- HUD screen now stays focused on:
  - readback diagnostics;
  - bridge/services;
  - AudioExt service tools.
- rationale:
  - `gflow_data_1.33.log` still shows `SecurityException: requires android.car.permission.CAR_VENDOR_EXTENSION` on parking/AVM raw paths;
  - the same log still shows `HUDInteraction.create returned null`;
  - direct user-facing actions for those paths were therefore misleading and were removed instead of being left as flaky controls.

- write diagnostics now take an int snapshot before probing a function and restore the previous value after the probe.
- restore result is logged per write:
  - `snapshot=OK`;
  - `restore=OK`;
  - `restore=FAILED`;
  - `restore=SKIPPED_NO_SNAPSHOT`;
  - `snapshot=SKIPPED_FLOAT`.
- catalog sweep write probes now also restore the previous value after successful or failed attempts.
- physically moving functions are skipped for catalog writes and stay read/support diagnostics only:
  - windows;
  - window position;
  - sunroof;
  - curtain;
  - seat axes;
  - seat memory save/restore.
- regular write sweep no longer sends window open/close commands.
- practical effect:
  - auto-diagnostics should not leave toggles/functions changed after completion when readback is available;
  - if a function has no reliable readback snapshot, the log marks restore as skipped instead of guessing.

GFlow roof command fix on Friday, July 31, 2026:

- `log_1.32` showed roof/sun-curtain functions as supported, but catalog write only proved value `0`, not a real movement command.
- root cause candidate:
  - sunroof / curtain functions are button contracts with values `[1, 0]`;
  - previous UI sent only `1`;
  - stock-style behavior should be a pulse: press `1`, then release `0`.
- changed roof UI commands to pulse:
  - `0x21200200` sunroof open, zone `0x4`;
  - `0x21200300` sunroof close, zone `0x4`;
  - `0x21200400` curtain open, zone `0x8`;
  - `0x21200500` curtain close, zone `0x8`;
  - `0x21030400` tilt;
  - `0x21200000` init.
- pulse timing:
  - send `COMMON_ON`;
  - after `180 ms`, send `COMMON_OFF`.

GFlow steering assistant button import on Friday, July 31, 2026:

- added Steering UI automation templates for assistant buttons from `log_1.32/alls` user labels:
  - assistant/ICA enable button: keyCode `6`;
  - lane tracking start button: keyCode `300050`;
  - ICA speed lever: keyCode `5`;
  - cruise / intelligent cruise previous: keyCode `3`;
  - cruise / intelligent cruise next: keyCode `4`.
- these are added as remap/scenario templates, not direct vehicle writes.
- purpose:
  - allow binding and later automating assistant activation flows;
  - keep raw `0x2141f000` bridge as diagnostics fallback only until exact low-level contracts are proven.

GFlow roof diagnostics fix on Friday, July 31, 2026:

- root cause:
  - catalog sweep checked `BCM_FUNC_WINDOW_POS [0x21030300]` only through the adapter default zone;
  - previous default/check path did not separately probe roof zones `0x4` and `0x8`;
  - catalog sweep also treats `0x21030300` as unsafe for generic writes, so percent/float roof positions were not validated.
- added dedicated `Roof Position Sweep` diagnostics:
  - sunroof position: `0x21030300`, zone `0x4`;
  - sun curtain position: `0x21030300`, zone `0x8`;
  - support/read are always logged;
  - float writes are attempted only in write-enabled diagnostics;
  - float readback snapshot is restored after the write probe.
