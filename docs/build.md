# Build

Bootstrap toolchain:

- Android Gradle Plugin: `9.4.0`
- Gradle: `9.6.0`
- JDK: `17`
- compileSdk: `37`
- targetSdk: `37`
- minSdk: `26`
- Compose BOM: `2026.09.00`
- Compose compiler/Kotlin plugin line: `2.4.10`

The project intentionally pins versions instead of using dynamic `+` dependencies.

## Local build

Install JDK 17, Android SDK Platform 37, and Android SDK Build Tools 36.0.0, then run Gradle 9.6:

```bash
gradle :app:assembleDebug :app:testDebugUnitTest
```

A Gradle Wrapper will be added once its binary wrapper JAR is generated from a trusted local/CI Gradle installation.

## CI

GitHub Actions installs the pinned JDK, Android SDK components, and Gradle distribution, then compiles the debug APK and runs unit tests.
