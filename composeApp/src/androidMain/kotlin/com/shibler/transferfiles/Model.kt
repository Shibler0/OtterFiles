package com.shibler.transferfiles

import android.os.Environment
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.collections.iterator

class Model() {

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    // On vérifie que ce n'est pas l'adresse de boucle locale (127.0.0.1)
                    // et que c'est bien une adresse IPv4
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "Inconnue"
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "IP non trouvée"
    }

    fun getAllFiles(): List<String> {
        val fileList = mutableListOf<String>()

        // On récupère la racine du stockage interne (ex: /storage/emulated/0)
        val root = Environment.getExternalStorageDirectory()

        // On lance un scan récursif
        scanDirectory(root, fileList)

        return fileList
    }

    private fun scanDirectory(directory: File, fileList: MutableList<String>) {
        val files = directory.listFiles() ?: return // Sécurité si le dossier est inaccessible

        for (file in files) {
            if (file.isDirectory) {
                // 1. On évite le dossier système "Android" (souvent protégé/lent)
                // 2. On évite les dossiers cachés (commençant par point)
                if (file.name != "Android" && !file.name.startsWith(".")) {
                    scanDirectory(file, fileList) // <--- ON RENTRE DANS LE DOSSIER
                }
            } else {
                // On a trouvé un fichier !
                // Astuce : stocke le chemin complet (absolutePath), pas juste le nom,
                // sinon tu auras 50 fichiers nommés "image.jpg" sans savoir lequel est le bon.
                fileList.add(file.absolutePath)
            }
        }
    }

}