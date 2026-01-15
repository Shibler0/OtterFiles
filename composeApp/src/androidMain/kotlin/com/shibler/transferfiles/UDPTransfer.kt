package com.shibler.transferfiles

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log


class UDPTransfer(private val context: Context) {

    val nsdManager: NsdManager? = context.getSystemService(NsdManager::class.java)
    private var registrationListener: NsdManager.RegistrationListener? = null

    private val serviceInfo = NsdServiceInfo().apply {
        serviceName = "MyAppServer"
        serviceType = "_myapp._tcp.local."
        port = 5000
    }

    fun register() {
        if (registrationListener != null) return

        registrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d("NSD", "Service registered: ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d("NSD", "Service unregistered")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Unregister failed: $errorCode")
            }
        }

        nsdManager?.registerService(
            serviceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            registrationListener
        )
    }

    fun unregister() {
        registrationListener?.let {
            nsdManager?.unregisterService(it)
            registrationListener = null
        }
    }

}
