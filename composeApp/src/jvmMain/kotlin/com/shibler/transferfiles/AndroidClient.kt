package com.shibler.transferfiles

import java.net.InetSocketAddress
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

class AndroidClient(val ip: String, val port: Int = 9999) {

    fun <T> sendSocketCommand(command: String, parser: (DataInputStream) -> T): Result<T> {
        println("--- CONNEXION : $ip:$port ($command) ---")

        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 3000)

                val output = DataOutputStream(socket.getOutputStream())
                val input = DataInputStream(socket.getInputStream())

                // 1. On envoie la commande
                output.writeUTF(command)
                output.flush()

                // 2. On laisse la logique spécifique lire la réponse
                val result = parser(input)

                // 3. On retourne le résultat encapsulé dans un succès
                Result.success(result)
            }
        } catch (e: Exception) {
            println("❌ ERREUR : ${e.message}")
            // On retourne l'échec
            Result.failure(e)
        }
    }

    fun getRemoteFiles(): List<String> {
        val result = sendSocketCommand("GET_LIST") { input ->
            val count = input.readInt()
            val list = mutableListOf<String>()
            for (i in 0 until count) {
                list.add(input.readUTF())
            }
            list // Ce que retourne le bloc est le résultat
        }

        // Gestion du résultat (Succès ou Erreur)
        return result.getOrDefault(listOf("ERREUR DE CONNEXION"))
    }

    fun downloadFile(fileName: String, saveDirectory: File): File? {

        val result = sendSocketCommand("GET_FILE;$fileName") { input ->

            val fileSize = input.readLong()

            println("Taille du fichier à recevoir : $fileSize octets")

            // 2. On prépare le fichier vide sur le téléphone
            val destinationFile = File(saveDirectory, fileName)

            // "use" ferme automatiquement le flux fichier à la fin
            FileOutputStream(destinationFile).use { fileOutput ->

                // 3. On crée un tampon (buffer) pour copier par morceaux (ex: 4KB)
                val buffer = ByteArray(4096)
                var totalBytesRead: Long = 0

                // 4. Boucle de lecture : tant qu'on n'a pas tout reçu
                while (totalBytesRead < fileSize) {
                    // On essaie de lire au max 4096 octets
                    // Le min() assure qu'on ne lit pas trop à la toute fin
                    val bytesToRead = minOf(buffer.size.toLong(), fileSize - totalBytesRead).toInt()

                    val bytesRead = input.read(buffer, 0, bytesToRead)

                    // Si le serveur coupe la connexion
                    if (bytesRead == -1) break

                    // On écrit dans le fichier local
                    fileOutput.write(buffer, 0, bytesRead)

                    totalBytesRead += bytesRead
                }
            }

            // On retourne l'objet File pour confirmer que c'est fini
            destinationFile
        }

        return result.getOrNull() // Retourne null si échec, le fichier si succès
    }
}