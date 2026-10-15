# Build

Bootstrap toolchain:

- Android Gradle Plugin: `9.4.0`
- Gradle: `9.6.0`
- JDK: `17`
- compileSdk: `37`
- targetSdk: `37`
- minSdk: `26`
- Android SDK package: `platforms;android-37.0`
- Android SDK Build Tools: `37.0.0`
- Compose BOM: `2026.09.00`
- Compose compiler/Kotlin plugin line: `2.4.10`

The project intentionally pins versions instead of using dynamic `+` dependencies.

## Android 17 SDK package naming

Android 17 / API 37 uses a minor-versioned SDK package in the command-line SDK manager. Install `platforms;android-37.0`, not `platforms;android-37`.

Because Android 17 is currently distributed from a preview/canary SDK channel, CI installs it with:

```bash
sdkmanager --channel=3 "platforms;android-37.0" "build-tools;37.0.0"
```

Gradle still uses `compileSdk = 37` and `targetSdk = 37`.

## Local build

Install JDK 17, Android SDK Platform 37.0, and Android SDK Build Tools 37.0.0, then run Gradle 9.6:

```bash
gradle :app:assembleDebug :app:testDebugUnitTest
```

A Gradle Wrapper will be added once its binary wrapper JAR is generated from a trusted local/CI Gradle installation.

## CI

GitHub Actions installs the pinned JDK, Android SDK components, and Gradle distribution, then compiles the debug APK, runs unit tests, and publishes the debug APK as a workflow artifact.

The bootstrap baseline was verified successfully by Android CI run #4 on 2026-10-15.
