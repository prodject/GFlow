# GControl

GControl is an Android 11+ application for an Android-based car head unit.

Package: `com.prodject.gcontrol`

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

- Open-Meteo weather request screen;
- Android notification/media session listener service;
- local shell command screen, ADB setting toggle attempt, DPI command shortcut;
- launchable-app desktop with pinned dock, app launch, shortcut removal and uninstall intent;
- Vosk recording pipeline over the bundled Russian model;
- DVR screen with Camera2 camera discovery;
- DVR archive directory, segment naming and disk-limit pruning;
- ECARX/Geely/OneOS intent broadcast attempts for vehicle commands.

Still firmware-dependent:

- real stock-camera recording depends on whether the head unit exposes those cameras through Camera2;
- climate, windows, seats, ADAS, HUD and vehicle sensor commands need the exact Geely/ECARX/OneOS binder or intent contract for the target firmware;
- privileged settings require system signature, root, or ADB grants.

Some vehicle functions depend on firmware-specific Geely/ECARX/OneOS APIs, privileged Android permissions, or ADB grants. Unsupported firmware will show the UI but may not execute the underlying vehicle command until the matching integration layer is added.

## Build And Releases

GitHub Actions builds the APK on every push to `main`/`master` and on manual workflow runs.

Release versions use the format `1.X`, where `X` is the GitHub Actions run number. The generated APK is attached to the GitHub Release as `GControl-1X.apk`.

## Warranty Disclaimer

GControl is an independent project and is not affiliated with, endorsed by, or certified by Geely, ECARX, OneOS, or any vehicle manufacturer.

Using third-party software, ADB grants, privileged permissions, system settings changes, camera recording, or vehicle-control integrations may affect the warranty, service eligibility, safety behavior, or legal compliance of a Geely vehicle. You use this application at your own risk. Before installing or enabling vehicle-control functions, check the warranty terms, local laws, and service requirements for your specific vehicle and head-unit firmware.
