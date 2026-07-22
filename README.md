# GFlow

GFlow is an Android 11+ application for an Android-based car head unit.

Package: `com.prodject.gflow`

The app provides a single car-control workspace with:

- onboarding and legal screen;
- file manager with internal storage, USB lookup, free/used space, open, share, delete and folder creation;
- media and text viewers;
- split-mode launch helper;
- DVR/camera foreground service scaffold for stock cameras and archive settings;
- voice assistant entry points
- car command surface for climate, windows, sunroof, trunk, mirrors, seats, drive modes and sensors;
- climate preset, ADAS, HUD/Cluster/OneOS, browser/weather, desktop and ADB/system screens.

## Current Implementation Status

Implemented:

- full Design.txt migration for the main user-facing app shell and major sections;
- dedicated new-style screens for Home, Climate, Vehicle, ADAS, DVR/Cameras, Parking/APA, HUD/Cluster/OneOS, Profiles, Steering Wheel Buttons, Voice, Desktop, Browser/Weather, Files, Media, Text, ADB/System and Settings;
- climate controls with comfort, advanced HVAC, smart climate and preset flows;
- vehicle/body controls including windows, locks, sunroof, mirrors, lights, seats and drive-mode entry points;
- extended drive-mode support including experimental modes and custom-profile groups;
- ADAS controls for high-confidence functions plus experimental ADAS toggles and readback blocks;
- parking and AVM/APA coverage including PAS, PAC, SAP, RCTA, radar/overlay controls and raw APA/RPA diagnostics;
- HUD/DIM/OneOS bridge coverage including HUD, DIM navigation modes, AudioExt hooks and service actions;
- voice flows with preset routing, app launch routing and structured custom voice command editor;
- desktop/app launcher with pinned dock, OneOS Dock bridge and app uninstall intent;
- file manager with internal storage, USB lookup, move/copy flows, text/media open, share and delete;
- media and text viewers in the new UI;
- local shell command screen, ADB setting toggle attempt, DPI shortcut and system tools;
- settings screen with experimental/developer toggles, GitHub release updater, full app backup/restore and full reset flow;
- unified auto diagnostics report covering current UI-exposed vehicle functions plus Parking signals/HAL, HUD/DIM, AudioExt, DVR/EVS, Camera2 inventory, OneOS Dock and ControlBoard availability;
- DVR archive directory, segment naming, storage settings and disk-limit pruning;
- Android notification/media session listener service;
- Vosk recording pipeline over the bundled Russian model.

Still firmware-dependent:

- actual execution of vehicle functions depends on the target firmware exposing compatible Geely/ECARX/OneOS APIs and permissions;
- some experimental drive, ADAS, parking, HUD and OneOS bridge functions are implemented as researched integrations, but still need validation on real firmware;
- real stock-camera recording depends on whether the head unit exposes those cameras through Camera2 or EVS in a usable way;
- privileged settings and some system-level operations require system signature, root, or ADB grants;
- unsupported firmware may show the UI and diagnostics successfully while rejecting the underlying command path.

Some vehicle functions depend on firmware-specific Geely/ECARX/OneOS APIs, privileged Android permissions, or ADB grants. Unsupported firmware will show the UI but may not execute the underlying vehicle command until the matching integration layer is added.

## Build And Releases

GitHub Actions builds the APK on every push to `main`/`master` and on manual workflow runs.

Prerelease versions use the format `1.X`, starting from `1.0` and incrementing from the latest existing GitHub prerelease. The generated APK is attached to the GitHub prerelease as `GFlow-1.X.apk`.

## Warranty Disclaimer

GFlow is an independent project and is not affiliated with, endorsed by, or certified by Geely, ECARX, OneOS, or any vehicle manufacturer.

Using third-party software, ADB grants, privileged permissions, system settings changes, camera recording, or vehicle-control integrations may affect the warranty, service eligibility, safety behavior, or legal compliance of a Geely vehicle. You use this application at your own risk. Before installing or enabling vehicle-control functions, check the warranty terms, local laws, and service requirements for your specific vehicle and head-unit firmware.
