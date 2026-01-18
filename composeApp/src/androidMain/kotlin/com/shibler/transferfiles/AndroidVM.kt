package com.shibler.transferfiles

import UDPBroadcaster
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidVM(): ViewModel()  {

    val model = Model()
    val server = AndroidFileServer(this)

    private val _serverIP = MutableStateFlow("Ip du serveur...")
    val serverIP = _serverIP.asStateFlow()

    private val _fileList = MutableStateFlow<List<String>>(emptyList())
    val fileList = _fileList.asStateFlow()

    private val _serverStatus = MutableStateFlow("En attente de connexion...")
    val serverStatus = _serverStatus.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                server.start()
            }

            delay(500)

            UDPBroadcaster().sendBroadcastAndListen { packet ->
                println("packet recu: $packet")
            }
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