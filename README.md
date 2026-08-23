<div align="center">
  <img src="docs/assets/readme-banner.svg" alt="The Living Tribe — Android community application" width="100%" />

  <br />

[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-JVM_11-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Firebase-Authentication-FFCA28?logo=firebase&logoColor=1f2937)](https://firebase.google.com/docs/auth)

A clean Android foundation for building a connected community experience.
</div>

## Table of Contents

- [About](#about)
- [Product Direction](#product-direction)
- [Current Status](#current-status)
- [How It Works](#how-it-works)
- [Technology](#technology)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [Development](#development)

## About

**The Living Tribe** is a native Android application written in Kotlin. The project is set up for a modern, edge-to-edge interface and includes the dependencies needed to add Firebase authentication and Google sign-in.

The repository is intentionally small and provides a straightforward base for developing the product without unnecessary architectural overhead.

## Product Direction

The Living Tribe is designed around a simple idea: digital communities should feel personal, organized, and easy to return to. The application is intended to give groups a shared mobile space where members can establish an identity, stay connected, and take part in community activity from one place.

The product direction prioritizes:

- **Simple onboarding** through familiar Google account authentication
- **Clear member identity** with profiles that help people recognize one another
- **Shared activity** that keeps conversations, updates, and community moments accessible
- **A calm mobile experience** built for regular use rather than visual noise
- **Responsible growth** through a small native foundation that can evolve as requirements become clearer

## Current Status

The application is in its initial development stage. It currently launches a single placeholder screen; authentication libraries and Firebase configuration are present, but the sign-in flow and product features have not yet been implemented.

This repository should currently be viewed as the technical starting point for the product. The Android application shell, package structure, theme, test directories, and authentication dependencies are in place. The next phase is to define the first complete user journey and build the screens and data model around it.

## How It Works

The app uses a single Android application module. `MainActivity` is the current entry point and renders an XML layout through the Android View system. Edge-to-edge window handling is already enabled so future screens can use the full display while respecting system bars.

Firebase Auth, Android Credential Manager, and Google ID are included as the intended authentication stack. Once implemented, Credential Manager will present the account flow, Google ID will provide the user credential, and Firebase Auth will establish the application session.

```text
Android device
      │
      ▼
Credential Manager ──► Google ID
      │
      ▼
Firebase Authentication
      │
      ▼
The Living Tribe session and community experience
```

## Technology

| Area | Choice |
| --- | --- |
| Language | Kotlin, targeting Java 11 |
| UI | Android Views, Material Components, ConstraintLayout |
| Authentication | Firebase Auth, Credential Manager, Google ID |
| Build | Gradle Kotlin DSL with a version catalog |
| Testing | JUnit, AndroidX Test, Espresso |

The app targets Android API 36 and supports devices running Android 7.0 (API 24) or newer.

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) with the Android SDK installed
- JDK 17 or newer (the project compiles application code to Java 11 bytecode)
- A connected Android device or emulator running API 24+

### Run the app

1. Clone the repository and open it in Android Studio.
2. Allow Gradle to sync and install any requested SDK components.
3. Select the `app` run configuration and a device.
4. Click **Run**, or build from the command line:

```bash
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`.

> The repository contains a Firebase configuration file for the current application ID. Use a separate Firebase project and replace `app/google-services.json` when working with your own environment.

## Project Structure

```text
The-Living-Tribe/
├── app/
│   └── src/
│       ├── main/          # Application code and resources
│       ├── test/          # Local unit tests
│       └── androidTest/   # On-device UI tests
├── gradle/                # Wrapper and dependency catalog
├── build.gradle.kts       # Root build configuration
└── settings.gradle.kts    # Gradle project settings
```

The project uses a Gradle version catalog in `gradle/libs.versions.toml`, keeping library and plugin versions in one place. Application settings, supported Android versions, and release configuration live in `app/build.gradle.kts`.

## Roadmap

The immediate roadmap is focused on turning the foundation into a complete first experience:

- Implement Google sign-in and authenticated session handling
- Add onboarding and member profile screens
- Define the community data model and persistence layer
- Build the first shared community activity flow
- Add loading, empty, error, and signed-out states
- Expand unit and instrumentation coverage around core journeys

Roadmap items describe the intended direction and may change as the product is developed.

## Development

Run the available checks before submitting changes:

```bash
./gradlew test
./gradlew connectedAndroidTest  # Requires a running device or emulator
```

Keep product text in `res/values/strings.xml`, add UI resources under `res/`, and place application logic in the existing `com.example.thelivingtribe` package.