# Polaris Network Monitor

An Android app for location and cellular measurements, network tests, and measurement history.

## Highlights

- Collect location and cellular information.
- Record measurements and review history with Room.
- Define and run network tests.
- Monitor through a foreground service.
- Present screens with Jetpack Compose and inject dependencies with Hilt.

**Stack:** Kotlin · Jetpack Compose · Hilt · Room · Coroutines · OkHttp · Retrofit

## Getting started

Use Android Studio with JDK 17, Android SDK 36, and a device or emulator running Android API 28 or newer.

```bash
git clone https://github.com/imhmdrz/polaris-network-monitor.git
cd polaris-network-monitor
./gradlew :app:assembleDebug
```

Open the repository in Android Studio, let Gradle sync, and run the `app` configuration. On Windows, use `gradlew.bat` in place of `./gradlew`.

Use a physical Android device with a SIM for meaningful cellular measurements. Grant the location and phone permissions requested by the app; the information available depends on the device and Android version.

## Code map

Paths below are relative to `app/src/main/java/ir/uni/polaris/`.

| Directory | Responsibility |
| --- | --- |
| `data/` | Location, telephony, and local storage |
| `domain/` | Measurement, authentication, and network-test repositories |
| `service/` | Foreground monitoring |
| `ui/` | Login, monitoring, tests, and history screens |
| `di/` | Dependency injection |
