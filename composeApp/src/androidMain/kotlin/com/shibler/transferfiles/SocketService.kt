package com.shibler.transferfiles

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import com.shibler.transferfiles.domain.TCPServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object SocketManager {
    var tcpServer: TCPServer? = null
}

class SocketService() : Service() {

    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var serverJob: Job? = null

    override fun onBind(p0: Intent?) = null


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val notification = createNotification()
        startForeground(1, notification)

        acquireLocks()

        serverJob = CoroutineScope(Dispatchers.IO).launch {
            SocketManager.tcpServer?.start()
        }

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun acquireLocks() {
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TransferApp::WakeLock").apply { acquire(10*60*1000L /*10 minutes*/) }
        }

        wifiLock = (getSystemService(WIFI_SERVICE) as WifiManager).createWifiLock(
            WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "TransferApp::WifiLock"
        ).apply { acquire() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "transfer_channel"
        val channelName = "Transfert de fichiers"

        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("Serveur Actif")
            .setContentText("En attente de connexion...")
            .setSmallIcon(android.R.drawable.stat_sys_download) // Utilise une icône système par défaut pour tester
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        SocketManager.tcpServer?.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        wakeLock?.release()
        wifiLock?.release()
        serverJob?.cancel()
        SocketManager.tcpServer?.stop()
        SocketManager.tcpServer = null
        super.onDestroy()
    }
}