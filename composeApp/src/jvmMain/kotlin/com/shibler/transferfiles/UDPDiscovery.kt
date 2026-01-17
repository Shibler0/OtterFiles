package com.shibler.transferfiles

import java.net.DatagramPacket
import java.net.DatagramSocket


class UDPDiscovery() {

    fun listenForPhone(vm : MainVM) {
        val port = 8888
        val buffer = ByteArray(1024)

        println("En attente du telephone sur le port $port...")

        try {
            // On écoute sur le port 8888
            val socket = DatagramSocket(port)
            val packet = DatagramPacket(buffer, buffer.size)

            // Cette ligne bloque le programme jusqu'à ce qu'un message arrive
            socket.receive(packet)

            // Le message est arrivé !
            val phoneIp = packet.address.hostAddress
            val message = String(packet.data, 0, packet.length)

            println("Téléphone trouvé !")
            println("Message reçu : $message")
            println("IP du téléphone : $phoneIp")

            vm.setServerSocket(phoneIp)
            println(vm.serverSocketAddress.value)

            socket.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
