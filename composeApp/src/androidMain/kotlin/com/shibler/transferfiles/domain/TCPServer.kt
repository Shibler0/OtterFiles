package com.shibler.transferfiles.domain

import android.content.Context
import com.shibler.transferfiles.R
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket

class TCPServer(val filesFlow: StateFlow<List<String>>, val imagesFlow: StateFlow<List<Picture>>, val message : (String) -> Unit) {

    private var isRunning = false
    private val BUFFER_SIZE = 64 * 1024

    val serverSocket = ServerSocket(9999)

    fun start() {
        if (isRunning) return
        isRunning = true

        while (isRunning) {
            try {
                val socket = serverSocket.accept()

                //message("${context.getString(R.string.client_info)} ${socket.inetAddress.hostAddress}")

                val input = DataInputStream(BufferedInputStream(socket.getInputStream(), BUFFER_SIZE))
                val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE))

                val command = input.readUTF()

                if (command == "GET_LIST") {
                    //message(context.getString(R.string.get_list))
                    val files = filesFlow.value
                    output.writeInt(files.size)
                    files.forEach { output.writeUTF(it) }
                    output.flush()
                }

                if (command.startsWith("GET_FILE")) {
                    //message(context.getString(R.string.get_file))
                    val path = command.substringAfter(";", "")
                    val fileObj = File(path)
                    if (fileObj.exists() && fileObj.isFile) {
                        output.writeLong(fileObj.length())
                        FileInputStream(fileObj).buffered(BUFFER_SIZE).use { fileInput ->
                            val buffer = ByteArray(BUFFER_SIZE)
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
                    //message(context.getString(R.string.get_thumbnail))
                    val thumbnails = imagesFlow.value
                    output.writeInt(thumbnails.size)
                    thumbnails.forEach {
                        val bytes = it.thumbnail
                        output.writeInt(bytes?.size ?: 0)
                        if(bytes != null) { output.write(bytes) }
                        output.writeUTF(it.path)
                    }
                    output.flush()
                }
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isRunning = false
        serverSocket.close()
    }
}