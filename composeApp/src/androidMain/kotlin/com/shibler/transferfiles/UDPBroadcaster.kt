package com.shibler.transferfiles

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class UDPBroadcaster() {

    fun sendBroadcastSignal() {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true

                val message = "HELLO_PC".toByteArray()

                repeat(5) {
                    val packet = DatagramPacket(
                        message,
                        message.size,
                        InetAddress.getByName("255.255.255.255"),
                        8888
                    )

                    socket.send(packet)

                    Thread.sleep(1000)
                }


                socket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

}
