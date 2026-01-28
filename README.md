# OtterFiles

## Introduction

OtterFiles is both an android app and desktop app that lets you transfer files locally from your android phone to your PC.<br>
Remotely select files from the desktop app that you want to download.

## Preview

![DesktopPreview](screenshots/Capture%20d’écran%202026-01-27%20111538.png)
![PhonePreview](screenshots/Screenshot_20260128_075724_OtterFiles.jpg)

## Installation Guide

### Prerequisites
- Android Studio
- Phone with Android 11 or more
- wifi connection

### Steps

1. Clone repo in Android studio <br>
2. Run the **mobile app** on your Android phone. <br>
3. Create a new **Gradle run configuration** and use the following command:
   ```bash
   jvmRun -DmainClass=com.shibler.transferfiles.MainKt --quiet
   ```
4. Launch the desktop application.<br>
5. Click the top-left button in the desktop app to display the list of files.


## Limitations

Only works when both devices are connected to the same network.<br>
Speed transfer depends on the network speed and devices limitations.

## Technologies

- [Kotlin](https://kotlinlang.org/)
- [Compose]()
- ServerSocket


---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…