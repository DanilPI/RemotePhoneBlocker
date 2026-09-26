# AGENTS.md

## Project overview

RemotePhoneBlocker is a small personal Android + Wear OS utility that lets the user lock an Android phone from a Wear OS widget.

The core functionality is already working. Prefer small, focused changes over architectural rewrites.

## Architecture

- `mobile/` — Android phone companion/backend.
- `wear/` — Wear OS widget/frontend.
- Watch-to-phone communication uses the Wear OS Data Layer.
- Phone locking uses Android Device Administration and `DevicePolicyManager.lockNow()`.
- Device Admin must be explicitly enabled by the user.
- The project uses Gradle Kotlin DSL and builds separate phone and watch APKs.

## Current UX

The Wear OS widget is intentionally minimal:

- small widget/card
- one centered Material Design lock icon
- no emoji
- no phone name
- no persistent connection text
- no periodic connectivity UI

On tap:

1. The watch sends the lock request through the existing Data Layer flow.
2. The phone attempts `DevicePolicyManager.lockNow()`.
3. The phone reports the result.
4. On confirmed success, the widget briefly shows a Material check icon.
5. The widget then returns to the lock icon.

Keep the lock button simple and immediately actionable.

## Development rules

- Preserve the working lock flow unless a change genuinely requires modifying it.
- Do not introduce a permanent foreground service.
- Do not introduce continuous connectivity polling just for UI state.
- Do not replace Device Admin with Accessibility-based workarounds.
- Do not add cloud services, analytics, telemetry, or unnecessary networking.
- Do not add destructive remote-wipe functionality without an explicit task requiring it.
- Keep dependencies and abstractions minimal.
- Prefer official Android Developers documentation when working with current or experimental Android/Wear OS APIs.
- Wear Widgets / Remote Compose APIs may be experimental; verify current API names and requirements instead of relying on memory.
- Never commit signing keys, keystores, credentials, `local.properties`, or other machine-specific secrets.

## Build

From the repository root:

```bash
./gradlew :mobile:assembleDebug
./gradlew :wear:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :mobile:assembleDebug
.\gradlew.bat :wear:assembleDebug
```

Do not claim a build succeeds unless it was actually run successfully.

## Scope

This is intentionally a small single-purpose utility. Avoid turning it into an enterprise-style architecture. Read the existing implementation before changing it, preserve behavior that is already proven on real devices, and make the smallest reliable change needed for the task.
