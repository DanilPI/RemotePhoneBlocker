# RemotePhoneBlocker

A small Android + Wear OS utility that lets you lock your Android phone directly from your watch.

RemotePhoneBlocker is designed as a simple panic button: tap the lock button on the Wear OS widget and the paired phone is locked immediately using Android's Device Administration API.

## How it works

```text
Wear OS widget
      ↓
Wear OS Data Layer
      ↓
Android companion app
      ↓
DevicePolicyManager.lockNow()
      ↓
Phone locked
```

After the phone confirms the command, the widget briefly changes from the lock icon to a check mark and then returns to the lock icon.

No cloud server is required.

## Features

- One-tap phone locking from Wear OS
- Minimal Wear OS widget
- Direct watch-to-phone communication through the Wear OS Data Layer
- Uses Android's native `DevicePolicyManager.lockNow()`
- Confirmation feedback on the watch
- No permanent foreground service
- No cloud backend
- No analytics or telemetry

## Project structure

```text
RemotePhoneBlocker/
├── mobile/   Android phone companion/backend
├── wear/     Wear OS widget/frontend
└── gradle/   Gradle Wrapper files
```

The project produces separate APKs for the phone and the watch.

## Requirements

- Android phone with Google Play services / Wear OS Data Layer support
- Paired Wear OS watch
- Device Administrator permission enabled for the phone companion
- Android SDK required by the project for building from source

The current project configuration uses Android API 37 for `compileSdk` and `targetSdk`, with `minSdk 30`.

## Installation

This project is currently intended primarily for manual installation/sideloading.

1. Build and install the `mobile` APK on the Android phone.
2. Build and install the `wear` APK on the paired Wear OS watch.
3. On the phone, enable Device Administrator access for RemotePhoneBlocker when prompted.
4. Add the RemotePhoneBlocker widget to the watch.
5. Tap the lock icon to test the connection and lock the phone.

> The phone companion needs Device Administrator access because Android requires it for `DevicePolicyManager.lockNow()`.

## Building

Clone the repository and build both modules with the included Gradle Wrapper.

### Windows

```powershell
.\gradlew.bat :mobile:assembleDebug
.\gradlew.bat :wear:assembleDebug
```

### Linux / macOS

```bash
./gradlew :mobile:assembleDebug
./gradlew :wear:assembleDebug
```

Debug APKs will be generated inside the corresponding module's `build/outputs/apk/` directory.

## Security and privacy

RemotePhoneBlocker is intentionally local and single-purpose.

- Lock commands are sent through the Wear OS Data Layer.
- The project does not require its own cloud service.
- It does not intentionally collect analytics or telemetry.
- The lock action does not erase user data.

Remote wiping is deliberately outside the normal one-button workflow because an accidental destructive action would be significantly more dangerous than an accidental device lock.

## Development

Coding-agent guidance and architectural constraints are documented in [AGENTS.md](AGENTS.md).

When modifying the project, prefer small changes that preserve the already working phone-lock flow.

## License

RemotePhoneBlocker is available under the [MIT License](LICENSE).
