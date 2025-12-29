# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TravelLog is an Android application built with Kotlin and Jetpack Compose. The project uses modern Android development practices including:
- **Jetpack Compose** for declarative UI (Material 3)
- **Gradle Version Catalogs** for dependency management (libs.versions.toml)
- **Kotlin 2.0.21** with Compose compiler plugin
- **MinSDK 26, TargetSDK 36**
- Package namespace: `com.example.travellog`

## Build Commands

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Run unit tests (JVM)
./gradlew test

# Run instrumented tests (requires Android device/emulator)
./gradlew connectedAndroidTest

# Run tests for a specific variant
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Lint and code quality
```bash
./gradlew lint

# View lint results at: app/build/reports/lint-results.html
```

### Install on device/emulator
```bash
./gradlew installDebug
./gradlew installRelease
```

### Clean build
```bash
./gradlew clean
```

## Project Structure

```
app/src/
├── main/
│   ├── java/com/example/travellog/
│   │   ├── MainActivity.kt           # Main entry point, sets up Compose UI
│   │   └── ui/theme/                 # Theme configuration (Color, Type, Theme)
│   ├── res/                          # Resources (layouts, strings, drawables)
│   └── AndroidManifest.xml
├── test/                             # Unit tests (run on JVM)
└── androidTest/                      # Instrumented tests (run on device)
```

## Architecture Notes

### Compose UI
- The app uses `enableEdgeToEdge()` for modern edge-to-edge display
- Main UI is wrapped in `TravelLogTheme` which provides Material 3 theming
- Composables follow the pattern of accepting a `Modifier` parameter for flexibility

### Dependencies
- Dependencies are managed via Gradle Version Catalog in `gradle/libs.versions.toml`
- To add new dependencies, update the TOML file and reference with `libs.` prefix in build.gradle.kts
- Compose BOM (Bill of Materials) manages Compose library versions consistently

### Testing
- **Unit tests** go in `src/test/` - use JUnit, run on JVM
- **Instrumented tests** go in `src/androidTest/` - use AndroidJUnit4, require device/emulator
- Test instrumentation runner: `androidx.test.runner.AndroidJUnitRunner`

## Development Notes

### Running on Windows
- Use `gradlew.bat` instead of `./gradlew` when running commands in CMD/PowerShell
- In Git Bash or WSL, `./gradlew` works as shown above

### Compose Previews
- Annotate composables with `@Preview` to see them in Android Studio's preview panel
- Previews don't require running the app on a device
