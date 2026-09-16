# AI Scanner

Native Android document scanner built with Kotlin, Jetpack Compose, and Google ML Kit.

## Features

- Multi-page document capture with automatic edge detection, crop, filters, and stain cleanup (ML Kit Document Scanner)
- Import from the photo gallery
- Local library of saved scans (JPEG pages + PDF)
- Rename, share, open, and delete scans
- On-device OCR with copy-to-clipboard (ML Kit Text Recognition)

The scanner UI and camera permission flow are provided by Google Play services. The app does not declare a camera permission.

## Requirements

- Android Studio Ladybug or newer
- JDK 17 (Gradle JDK in Studio)
- Android SDK **35** (compile / target)
- Device or emulator with **Google Play services**, API **24+**

## Open and run

1. Clone this repository and open the **project root** in Android Studio (the folder with `settings.gradle.kts`, not `app/`).
2. Let Gradle sync. Use **Gradle JDK 17** (Settings → Build, Execution, Deployment → Build Tools → Gradle).
3. Select a **Google Play** device or emulator (Play Store icon on the system image).
4. Run the **`app`** configuration (toolbar, or Run → `app`).

The shared run configuration is [`.idea/runConfigurations/app.xml`](.idea/runConfigurations/app.xml).

### Error: Module not specified

The **Module** dropdown is empty or the saved name does not match until Gradle has imported `:app`.

1. Confirm you opened the **repo root** (`settings.gradle.kts` is visible in the project view, not only the `app` folder).
2. **File → Sync Project with Gradle Files**. Wait until it finishes with no errors. Gradle JDK must be **17**.
3. **Run → Edit Configurations…** → select **`app`** (or **+ → Android App** if it is gone).
4. Set **Module** to **`app`**. Studio may show it as `AIScanner.app` or `app.main` — pick the Android application module, not a test module.
5. **Launch:** Default Activity. **Deploy:** Default APK. Apply, then Run.

If Module is still empty, Gradle never created the Android module: fix the sync error in the Build window, or **File → Invalidate Caches → Invalidate and Restart**, then sync again.

| Field | Value |
| --- | --- |
| Name | `app` |
| Module | `app` (`AIScanner.app`) |
| Launch | Default Activity (`MainActivity`) |
| Deploy | Default APK |
| Build variant | `debug` |
| Launch flags | empty |

Application id: `com.kthakare.aiscanner`. Build Variants should show `app` → **debug**.

**SDK Manager:** install Android 15.0 (API 35), Android SDK Build-Tools, Platform-Tools, and the Emulator if you use an AVD. Prefer an API 34 or 35 **Google Play** system image. AOSP or Google APIs images without Play will install the app but Scan/OCR will fail.

`local.properties` (`sdk.dir`) is created by Studio; do not commit it.

From the command line (with the Android SDK installed):

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Use Android Studio **Run** if you want the APK installed and `MainActivity` launched.

## How to use

1. Tap **Scan** on the library screen.
2. Capture or import up to 10 pages, then accept the ML Kit preview.
3. Open a saved scan to share or view the PDF, rename it, extract text, or delete it.

Scans are stored only on the device under the app’s private files directory.
