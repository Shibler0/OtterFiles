package com.shibler.transferfiles.domain

import java.awt.Desktop
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.File

fun showNotification(title: String, message: String) {
    if (!SystemTray.isSupported()) {
        println("Les notifications ne sont pas supportées sur ce système.")
        return
    }

    val userHome = System.getProperty("user.home")
    val dir = Language.getString("dir")

    val tray = SystemTray.getSystemTray()
    val image = Toolkit.getDefaultToolkit().createImage("")
    val trayIcon = TrayIcon(image, "Mon App de Transfert")

    trayIcon.addActionListener{
        Desktop.getDesktop().open(File(userHome, "Downloads/$dir"))
    }

    trayIcon.isImageAutoSize = true
    trayIcon.toolTip = "Transfert de fichier"

    try {
        tray.add(trayIcon)
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)

        Thread {
            Thread.sleep(5000)
            tray.remove(trayIcon)
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}