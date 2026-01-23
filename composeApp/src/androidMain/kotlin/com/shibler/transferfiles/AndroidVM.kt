package com.shibler.transferfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shibler.transferfiles.domain.ThumbnailGenerator
import com.shibler.transferfiles.domain.UDPBroadcaster
import com.shibler.transferfiles.domain.getAllFiles
import com.shibler.transferfiles.domain.getLocalIpAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidVM(): ViewModel()  {
    val server = AndroidFileServer(this)

    private val _serverIP = MutableStateFlow("Ip du serveur...")
    val serverIP = _serverIP.asStateFlow()

    private val _fileList = MutableStateFlow<List<String>>(emptyList())
    val fileList = _fileList.asStateFlow()

    private val _serverStatus = MutableStateFlow("En attente de connexion...")
    val serverStatus = _serverStatus.asStateFlow()

    var isSearching = MutableStateFlow(false)
        private set

    private val _compressedImages = MutableStateFlow<List<ByteArray?>>(emptyList())
    val compressedImages = _compressedImages.asStateFlow()


    init {
        startServer()
        getServerIP()
        _fileList.value = getAllFiles()
        loadThumbnail()
    }

    fun updateStatus(newStatus: String) {
        _serverStatus.value = newStatus
    }

    fun loadThumbnail() {
        viewModelScope.launch {
            _fileList.value.forEach {
                if(it.substringAfterLast(".") == "jpg") {
                    _compressedImages.value += ThumbnailGenerator().invoke(it)
                }

            }
        }
    }

    fun getServerIP() {
        viewModelScope.launch(Dispatchers.IO) {
            _serverIP.value = getLocalIpAddress()
        }

    }

    fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                server.start()
            }
            delay(500)
            sendBroadcastHandshake()
        }

    }

    fun sendBroadcastHandshake() {

        if(isSearching.value) return
        isSearching.value = true

        _serverStatus.value = "En attente de réponse..."

        viewModelScope.launch(Dispatchers.IO) {
            UDPBroadcaster().sendBroadcastAndListen { ip ->
                _serverStatus.value = "Connecté !"
                isSearching.value = false
                println("packet recu: $ip")
            }
        }

    }


}