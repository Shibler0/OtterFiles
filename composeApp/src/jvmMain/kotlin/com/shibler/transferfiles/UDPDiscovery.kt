package com.shibler.transferfiles

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

class UDPDiscovery {

    private val jmdns = JmDNS.create()
    private var connected = false

    fun start(vm : MainVM) {
        jmdns.addServiceListener("_myapp._tcp.local.", object : ServiceListener {

            override fun serviceAdded(event: ServiceEvent) {
                jmdns.requestServiceInfo(event.type, event.name)
            }

            override fun serviceResolved(event: ServiceEvent) {

                if (connected) return

                val info = event.info
                val ip = info.inetAddresses.first().hostAddress
                val port = info.port

                connected = true

                vm.setServerSocket("$ip:$port")
                println("Serveur trouvé: $ip:$port")
            }

            override fun serviceRemoved(event: ServiceEvent) {
                println("Serveur disparu: ${event.name}")
                connected = false
            }
        })
    }

    fun stop() {
        jmdns.close()
    }
}
