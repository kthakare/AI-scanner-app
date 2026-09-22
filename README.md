# AI Scanner

Native Android document scanner built with Kotlin, Jetpack Compose, and Google ML Kit.

## Features

- Multi-page document capture with automatic edge detection, crop, filters, and stain cleanup (ML Kit Document Scanner)
- Import from the photo gallery
- Local library of saved scans (JPEG pages + PDF)
- Rename, share, open, and delete scans
- On-device OCR shown as a Key / Value table, with copy-to-clipboard (ML Kit Text Recognition)

The scanner UI and camera permission flow are provided by Google Play services. The app does not declare a camera permission.

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android device or emulator with **Google Play services** (min SDK 24)

## Open and run

1. Clone this repository and open the project root in Android Studio.
2. Let Gradle sync.
3. Select a Play-services device or emulator.
4. Run the `app` configuration.

From the command line (with the Android SDK installed):

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## How to use

1. Tap **Scan** on the library screen.
2. Capture or import up to 10 pages, then accept the ML Kit preview.
3. Open a saved scan to share or view the PDF, rename it, extract text as a Key/Value table, or delete it.

Example: a page that reads `name : abc, address: xyz` is shown as:

| Key | Value |
| --- | --- |
| name | abc |
| address | xyz |

Scans are stored only on the device under the app’s private files directory.
