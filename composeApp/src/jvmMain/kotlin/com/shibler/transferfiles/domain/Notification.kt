package com.shibler.transferfiles.domain

import dorkbox.notify.Notify
import dorkbox.notify.Position
import jdk.internal.util.StaticProperty.userHome
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.File
import javax.imageio.ImageIO

@Suppress("SuspiciousIndentation")
fun showNotification(title: String, message: String) {
    if (!SystemTray.isSupported()) {
        println("Les notifications ne sont pas supportées sur ce système.")
        return
    }


    val imageURL = object {}.javaClass.getResource("/otterfileslogo.png")


    if (imageURL == null) {
        println("Erreur : Fichier image introuvable")
        return
    }

    try {
        val tray = SystemTray.getSystemTray()
        val image = ImageIO.read(imageURL)
        val trayIcon = TrayIcon(image, "OtterFiles")

        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "OtterFiles - Transfert"

        trayIcon.addActionListener {
            val dir = Language.getString("dir")
            val downloadFolder = File(System.getProperty("user.home"), "Downloads/$dir")
            if (downloadFolder.exists()) {
                Desktop.getDesktop().open(downloadFolder)
            }
        }

    trayIcon.isImageAutoSize = true
    trayIcon.toolTip = "Transfert de fichier"


        tray.add(trayIcon)
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.NONE)

        Thread {
            Thread.sleep(7000)
            tray.remove(trayIcon)
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/*if (!SystemTray.isSupported()) {
        println("Les notifications ne sont pas supportées sur ce système.")
        return
    }


    val imageURL = object {}.javaClass.getResource("/otterfileslogo.png")
    val image = ImageIO.read(imageURL)

    val userHome = System.getProperty("user.home")
    val dir = Language.getString("dir")


    if (imageURL == null) {
        println("Erreur : Fichier image introuvable")
        return
    }

    Notify.create()
        .title(title)
        .text(message)
        .image(image) // ICI tu peux mettre TON image !
        .position(Position.BOTTOM_RIGHT)
        .hideAfter(5000)
        .onClickAction{
            Desktop.getDesktop().open(File(userHome, "Downloads/$dir"))
        }
        .show()*/