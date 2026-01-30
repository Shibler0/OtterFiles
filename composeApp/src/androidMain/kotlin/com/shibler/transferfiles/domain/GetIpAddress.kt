package com.shibler.transferfiles.domain

import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.collections.iterator

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress ?: "Unknow"
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return "IP not found"
}