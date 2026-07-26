# Agent Changelog

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
