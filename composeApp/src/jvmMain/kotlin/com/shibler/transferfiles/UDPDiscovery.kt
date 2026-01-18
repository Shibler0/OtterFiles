package com.shibler.transferfiles

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class UDPDiscovery() {

    fun listenForPhone(onConnectionReceived : (String) -> Unit = {}) {
        val port = 8888
        val buffer = ByteArray(1024)

        println("En attente du telephone sur le port $port...")

        try {
            val socket = DatagramSocket(port)
            val packet = DatagramPacket(buffer, buffer.size)

            socket.receive(packet)


            val phoneIp = packet.address.hostAddress
            val message = String(packet.data, 0, packet.length)

            if(message != "HELLO_PC") {
                socket.close()
                return
            }

            onConnectionReceived(phoneIp)
            println("Message recu : $message")
            println("IP du telephone : $phoneIp")

            val response = "ACK".toByteArray()

            val replyPacket = DatagramPacket(
                response,
                response.size,
                packet.address,
                packet.port
            )

            socket.send(replyPacket)


            socket.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
