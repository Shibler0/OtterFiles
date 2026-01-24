package com.shibler.transferfiles

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket


class AndroidFileServer(private val vm: AndroidVM) {

    private var isRunning = false
    private val BUFFER_SIZE = 64 * 1024

    fun start(port: Int = 9999) {
        if (isRunning) return
        isRunning = true

        val serverSocket = ServerSocket(port)

            while (isRunning) {
                try {

                    val socket = serverSocket.accept()
                    val input = DataInputStream(socket.getInputStream())
                    val output = DataOutputStream(socket.getOutputStream())

                    vm.updateStatus("Connexion établie")

                    val command = input.readUTF()

                    vm.updateStatus("Commande recue: $command")

                    if (command == "GET_LIST") {

                        val files = vm.fileList.value
                        output.writeInt(files.size)
                        files.forEach { output.writeUTF(it) }
                    }

                    if (command.startsWith("GET_FILE")) {
                        val path = command.substringAfter(";", "")
                        val fileObj = File(path)

                        if (fileObj.exists() && fileObj.isFile) {
                            output.writeLong(fileObj.length())

                            FileInputStream(fileObj).buffered(BUFFER_SIZE).use { fileInput ->
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (fileInput.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                }
                            }
                            output.flush()
                        } else {
                            output.writeLong(0L)
                        }
                    }

                    if (command.startsWith("GET_THUMBNAIL")) {
                        val thumbnails = vm.compressedImages.value.filterNotNull()

                        output.writeInt(thumbnails.size)

                        thumbnails.forEach {
                            output.writeInt(it.thumbnail?.size ?: 0)
                            output.write(it.thumbnail)
                        }
                        output.flush()
                    }

                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

    }
}