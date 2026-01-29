package com.shibler.transferfiles

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

class DesktopClient(val ip : String , val port: Int = 9999) {

    private val BUFFER_SIZE = 64 * 1024

    fun <T> sendSocketCommand(command: String, parser: (DataInputStream) -> T): Result<T> {
        println("--- CONNEXION : $ip:$port ($command) ---")

        return try {
            Socket().use { socket ->
                println("ip : $ip")
                socket.connect(InetSocketAddress(ip, port), 3000)

                val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE))
                val input = DataInputStream(BufferedInputStream(socket.getInputStream(), BUFFER_SIZE))
                //val input = DataInputStream(socket.getInputStream())

                output.writeUTF(command)
                output.flush()

                val result = parser(input)

                Result.success(result)
            }
        } catch (e: Exception) {
            println("❌ ERREUR : ${e.message}")
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
            list
        }

        return result.getOrDefault(listOf("ERREUR DE CONNEXION"))
    }

    fun getThumbnails() : List<Thumbnail> {
        val result = sendSocketCommand("GET_THUMBNAIL") {
            val listSize = it.readInt()
            val thumbnails = mutableListOf<Thumbnail>()

            for(i in 0 until listSize) {
                val thumbnailSize = it.readInt()

                val thumbnail = if(thumbnailSize > 0) {
                    val bytes = ByteArray(thumbnailSize)
                    it.readFully(bytes)
                    bytes
                } else {
                    null
                }
                val path = it.readUTF()
                thumbnails.add(Thumbnail(path, thumbnail = thumbnail))
            }
            thumbnails
        }
        return result.getOrDefault(listOf())
    }

    fun downloadFile(fileDirectory: String, vm: MainVM) {

        val fileName = fileDirectory.substringAfterLast("/")
        val userHome = System.getProperty("user.home")
        val dir = "mesfichiers"
        val saveDirectory = File(userHome, "Downloads/$dir")
        if(!saveDirectory.exists()) {
            saveDirectory.mkdir()
        }

        sendSocketCommand("GET_FILE;$fileDirectory") { input ->

            val fileSize = input.readLong()

            println("Taille du fichier à recevoir : $fileSize octets")

            val destinationFile = File(saveDirectory, fileName)

            FileOutputStream(destinationFile).buffered(BUFFER_SIZE).use { fileOutput ->

                val buffer = ByteArray(BUFFER_SIZE)
                var totalBytesRead: Long = 0

                while (totalBytesRead < fileSize) {

                    val bytesToRead = minOf(buffer.size.toLong(), fileSize - totalBytesRead).toInt()

                    val bytesRead = input.read(buffer, 0, bytesToRead)

                    if (bytesRead == -1) break

                    fileOutput.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    vm.setProgressionState(fileSize, totalBytesRead)
                }
                vm.resetProgressionState()
            }
        }

    }
}