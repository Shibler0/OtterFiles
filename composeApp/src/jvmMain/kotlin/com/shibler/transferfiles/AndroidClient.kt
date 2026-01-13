package com.shibler.transferfiles

import java.net.InetSocketAddress
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream

class AndroidClient(val ip: String, val port: Int = 9999) {

    fun getRemoteFiles(command : Int): List<String> {
        println("--- DÉBUT TENTATIVE DE CONNEXION ---")
        println("Cible : $ip:$port")

        return try {
            val socket = Socket()
            // Tente de se connecter pendant 3 secondes max, sinon échoue
            socket.connect(InetSocketAddress(ip, port), 3000)
            println("✅ SUCCES : Socket connecte physiquement !")

            val output = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())
            val files = mutableListOf<String>()

            println("📤 Envoi de la commande 'GET_LIST'...")

            if(command == 0) {
                output.writeUTF("GET_LIST")
                output.flush()

                println("📥 Attente de la reponse du telephone...")
                val count = input.readInt()
                println("✅ Reponse reçue ! Le telephone va envoyer $count fichiers.")

                val files = mutableListOf<String>()
                for (i in 0 until count) {
                    val name = input.readUTF()
                    files.add(name)
                }
            } else if (command == 1) {
                output.writeUTF("GET_FILE")
                output.flush()
            }


            println("✅ Reception terminee.")

            socket.close()
            files
        } catch (e: Exception) {
            println("❌ ERREUR CRITIQUE : ${e.message}")
            e.printStackTrace()
            // On renvoie une liste contenant l'erreur pour l'afficher à l'écran
            listOf("ERREUR: ${e.message}")
        }
    }
}