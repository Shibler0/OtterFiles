import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class UDPBroadcaster {

    suspend fun sendBroadcastAndListen(onServerFound: (String) -> Unit) {
        withContext(Dispatchers.IO) {

            var socket: DatagramSocket? = null

            try {
                socket = DatagramSocket()
                socket.broadcast = true

                socket.soTimeout = 2000

                val message = "HELLO_PC".toByteArray()
                val buffer = ByteArray(1024)

                // On boucle tant que la coroutine est active (et qu'on a pas return)
                while (isActive) {

                    try {
                        val broadcastAddr = InetAddress.getByName("255.255.255.255")

                        val packet = DatagramPacket(
                            message,
                            message.size,
                            broadcastAddr,
                            8888
                        )

                        socket.send(packet)
                        Log.d("UDP_HANDSHAKE", "Ping envoyé... J'écoute.")

                    } catch (e: Exception) {
                        Log.e("UDP_HANDSHAKE", "Erreur d'envoi: ${e.message}")
                    }

                    try {
                        val responsePacket = DatagramPacket(buffer, buffer.size)

                        socket.receive(responsePacket)

                        val responseMsg = String(responsePacket.data, 0, responsePacket.length)
                        val serverIp = responsePacket.address.hostAddress

                        if (responseMsg.trim() == "ACK") {
                            Log.d("UDP_HANDSHAKE", "✅ Le PC a répondu ! IP: $serverIp")

                            onServerFound(serverIp!!)

                            return@withContext
                        }

                    } catch (e: SocketTimeoutException) {
                        Log.d("UDP_HANDSHAKE", "Pas de réponse, je recommence...")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                socket?.close()
            }
        }
    }
}