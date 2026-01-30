# 🦦 OtterFiles

OtterFiles let's you transfer files from your android phone to your desktop.<br>

## 👁 Preview

![DesktopPreview](screenshots/softwarepreview.png)
<img src="screenshots/Screenshot_20260128_075724_OtterFiles.jpg" height="500">

## 📦 Installation Guide

### 🧩 Prerequisites
- Android Studio
- Phone with Android 11 or more
- wifi connection

### 🚀 Steps

1. Clone repo in Android studio <br>
2. Run the **mobile app** on your Android phone. <br>
3. Create a new **Gradle run configuration** and use the following command:
   ```bash
   jvmRun -DmainClass=com.shibler.transferfiles.MainKt --quiet
   ```
4. Launch the desktop application.<br>
5. Click the top-left button in the desktop app to display the list of files.

## 🧠 How it works
A TCP server is launched on your phone using foreground service<br>
Your phone send a message through broadcast address of subnet mask<br>
Desktop app listen and make a handshake with the app<br>
Desktop app can start asking for files<br>

## Limitations

Only works when both devices are connected to the same network.<br>

## Technologies

- [Kotlin](https://kotlinlang.org/)
- [Compose]()
- ServerSocket

## 📄 Licence
This project is licensed under the MIT License.
