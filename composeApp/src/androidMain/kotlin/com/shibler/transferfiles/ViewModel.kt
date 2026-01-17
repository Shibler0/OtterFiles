package com.shibler.transferfiles

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ViewModel(context: Context): ViewModel()  {

    val model = Model()
    val server = AndroidFileServer(this, context)

    private val _serverIP = MutableStateFlow("Ip du serveur...")
    val serverIP = _serverIP.asStateFlow()

    private val _fileList = MutableStateFlow<List<String>>(emptyList())
    val fileList = _fileList.asStateFlow()

    private val _serverStatus = MutableStateFlow("Arret")
    val serverStatus = _serverStatus.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            UDPBroadcaster().sendBroadcastSignal()
            server.start()
        }

        _serverIP.value = model.getLocalIpAddress()
        _fileList.value = model.getAllFiles()
    }

    fun refreshFileList() {
        Thread {
            val results = Model().getAllFiles()
            Handler(Looper.getMainLooper()).post {
                _fileList.value = results
                println("Scan terminé : ${results.size} fichiers trouvés")
            }
        }.start()
    }

    fun updateStatus(newStatus: String) {
        _serverStatus.value = newStatus

    }

}