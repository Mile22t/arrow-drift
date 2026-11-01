# Arrow Drift

A browser-based 3D tap-away puzzle and timed challenge, built with plain JavaScript, HTML, CSS, and Canvas. No dependencies or build step are required.

## Play locally

Install Node.js, then run:

```sh
node preview.cjs
```

Open http://127.0.0.1:8765 in your browser. The playable application is in `dist/` and can also be served by any static web server.

## Gameplay

- Clear a random 8 × 8 × 16 grid as quickly as possible.
- Cubes, bars, and slabs use randomly rotated orientations.
- Pieces move along their arrows only when their entire path is clear.
- Drag to rotate, Shift-drag or right-drag to pan, and scroll to zoom.
- Three hints and five arrow shuffles are available per run.
- No straight row contains more than three touching pieces with the same arrow direction.
- Every generated puzzle and shuffle has a valid removal sequence.
- Personal-best times are saved locally in the browser.

## Verification

```sh
node verify.cjs
node verify-runs.cjs
node verify-race.cjs
node measure-difficulty.cjs
```

These checks cover grid tiling, piece orientations, collisions, solvability, arrow-run limits, timing, lifeline limits, personal records, camera projection, and picking.

## Source layout

- `dist/puzzle.js`: piece generation, constraints, and shuffle logic.
- `dist/game.js`: rendering, camera, interaction, and race state.
- `dist/index.html` and `dist/voxel.css`: interface and styling.
- `.openai/hosting.json`: configuration for the existing Sites deployment.

## Android APK

The `android/` project packages the same `dist/` files as an offline Android app (Android 8.0 or later). It uses the system Android WebView, stores personal records locally, and requests no Internet permission.

With JDK 17, Android SDK platform 35, and Gradle 8.11.1 installed:

```sh
gradle -p android :app:assembleDebug
```

The installable debug-signed APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for direct installation and testing; a store release needs a separate release signing key.

The **Build Android APK** GitHub Actions workflow builds the APK, checks its signature, runs the puzzle verification scripts, and exercises the offline game on an Android 35 emulator. Download the `ArrowDrift-Android-APK` artifact from a successful workflow run.

## Touch controls and signed Android updates

Drag with one finger to rotate. Pinch with two fingers to zoom, or move both fingers together to pan. The game screen contains only the puzzle and play controls.

Version 1.1.0 uses a persistent release signing key stored in encrypted GitHub Actions secrets (ANDROID_KEYSTORE_BASE64 and ANDROID_KEYSTORE_PASSWORD). The repository contains no private key. CI builds and tests the release variant, including real two-finger input in the Android emulator. Future builds signed with this key can update each other. The earlier 1.0.0 debug build used an ephemeral key and must be uninstalled once before installing 1.1.0; uninstalling clears local records.

The release artifact is ArrowDrift.apk. To build a signed release locally, set ANDROID_KEYSTORE_FILE and ANDROID_KEYSTORE_PASSWORD, then run gradle -p android :app:assembleRelease.

## Depth rendering (1.1.1)

The game uses a WebGL depth buffer for every piece face, outline, and arrow marking, replacing face-center painter sorting. This prevents rotated slabs and newly exposed faces from drawing through other pieces. Android regression checks read rendered pixels for overlapping surfaces in both draw orders, hidden arrow markings, and 2× display density, alongside offline gameplay and pinch input.
