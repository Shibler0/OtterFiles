package com.shibler.transferfiles.domain

import android.os.Environment
import java.io.File

fun getAllFiles(): List<String> {
    val fileList = mutableListOf<String>()

    val root = Environment.getExternalStorageDirectory()

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