package com.shibler.transferfiles

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shibler.transferfiles.domain.NetworkMonitor
import com.shibler.transferfiles.domain.Picture
import com.shibler.transferfiles.domain.ThumbnailGenerator
import com.shibler.transferfiles.domain.UDPBroadcaster
import com.shibler.transferfiles.domain.getAllFiles
import com.shibler.transferfiles.domain.getLocalIpAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidVM(context: Context): ViewModel()  {
    val server = AndroidFileServer(this)

    private val _serverIP = MutableStateFlow("Ip du serveur...")
    val serverIP = _serverIP.asStateFlow()

    private val _fileList = MutableStateFlow<List<String>>(emptyList())
    val fileList = _fileList.asStateFlow()

    private val _serverStatus = MutableStateFlow("")
    val serverStatus = _serverStatus.asStateFlow()

    var isSearching = MutableStateFlow(false)
        private set

    private val _compressedImages = MutableStateFlow<List<Picture>>(emptyList())
    val compressedImages = _compressedImages.asStateFlow()

    private val networkMonitor = NetworkMonitor(context)

    private val _isWifiEnabled = MutableStateFlow(false)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()

    init {
        observeWifiStatus()
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
                _compressedImages.value += Picture(it, ThumbnailGenerator().invoke(it))
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

        _serverStatus.value = "En attente d'une connexion..."

        viewModelScope.launch(Dispatchers.IO) {
            UDPBroadcaster().sendBroadcastAndListen { ip ->
                _serverStatus.value = "Connecté"
                isSearching.value = false
                println("packet recu: $ip")
            }
        }

    }

    private fun observeWifiStatus() {
        viewModelScope.launch {
            networkMonitor.isWifiConnected.collect { isConnected ->
                _isWifiEnabled.value = isConnected

                if (isConnected) {
                    println("Wifi is back")
                } else {
                    println("Wifi is down")
                }
            }
        }
    }


}