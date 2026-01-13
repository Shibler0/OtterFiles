package com.shibler.transferfiles

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket


class AndroidFileServer(private val vm: ViewModel) {

    private var isRunning = false

    fun start(port: Int = 9999) {
        if (isRunning) return
        isRunning = true

            val serverSocket = ServerSocket(port)
            while (isRunning) {
                try {
                    vm.updateStatus("En attente de connexion...")

                    val socket = serverSocket.accept() // Attend le PC
                    val input = DataInputStream(socket.getInputStream())
                    val output = DataOutputStream(socket.getOutputStream())

                    vm.updateStatus("Connexion établie")

                    val command = input.readUTF()

                    vm.updateStatus("Commande recue: $command")

                    if (command == "GET_LIST") {

                        val files = Model().getAllFiles()
                        // 1. Envoie le nombre de fichiers
                        output.writeInt(files.size)
                        // 2. Envoie chaque nom
                        files.forEach { output.writeUTF(it) }
                    }

                    if(command == "GET_FILE") {

                        val path = input.readUTF()
                        val fileObj = File(path)

                        if (fileObj.exists()) {
                            output.writeLong(fileObj.length())

                            // 3. Ouvrir le flux sur le DISQUE (pas en RAM)
                            val fileInput = FileInputStream(fileObj)
                            val buffer = ByteArray(4096) // Le seau de 4 Ko

                            var bytesRead: Int

                            // 4. La boucle magique
                            // On remplit le buffer. 'bytesRead' contient le nombre d'octets réellement lus (ex: 4096, ou 200 à la fin)
                            while (fileInput.read(buffer).also { bytesRead = it } != -1) {
                                // On écrit dans le tuyau vers le Client
                                output.write(buffer, 0, bytesRead)
                            }

                            // 5. Ménage
                            output.flush() // On s'assure que tout est parti
                            fileInput.close()
                        } else {
                            // Gérer le cas où le fichier n'existe pas (envoyer une taille de 0 ou -1 par exemple)
                            output.writeLong(0)
                        }
                    }

                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

    }
}