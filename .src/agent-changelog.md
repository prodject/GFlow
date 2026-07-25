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

Still open inside stage 2:

- review whether `BCM_WINDOW_POS`, `BCM_WINDOW_CURRENT_POS`, and `BCM_DOOR_POS` should move to float/custom readback paths in the generic adapter instead of remaining activity-local diagnostics;
- inspect remaining door/trunk/child-lock entry points that still assume generic BCM zone semantics;
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

Still open inside stage 4:

- confirm whether `Auto Park UI` through `BCM custom key 0x65` is actually effective on this firmware or should also be downgraded to diagnostics-only;
- decide which PAS / AVM commands are truly writable and which are just state/report properties;
- trace the real 360 / AVM entry backend before re-enabling any user-facing 360 launch button.

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
- decide whether `ParkingActivity` should re-enable a visible 360 button now that EVS is the preferred backend, or keep parking conservative until AVM lifecycle behavior is confirmed on-device;
- verify whether EVS `openEvs(EVS_CAMERA_AVM)` should also be paired with an explicit close/lifecycle hook in some flows to avoid sticky camera sessions on this firmware.
