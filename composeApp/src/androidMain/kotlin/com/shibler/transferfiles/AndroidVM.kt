package com.shibler.transferfiles

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shibler.transferfiles.domain.TCPServer
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

class AndroidVM(application: Application): AndroidViewModel(application)  {

    private val _serverIP = MutableStateFlow(getApplication<Application>().getString(R.string.server_ip))
    val serverIP = _serverIP.asStateFlow()

    private val _fileList = MutableStateFlow<List<String>>(emptyList())
    val fileList = _fileList.asStateFlow()

    private val _serverStatus = MutableStateFlow("")
    val serverStatus = _serverStatus.asStateFlow()

    var isSearching = MutableStateFlow(false)
        private set

    private val _compressedImages = MutableStateFlow<List<Picture>>(emptyList())
    val compressedImages = _compressedImages.asStateFlow()

    private val networkMonitor = NetworkMonitor(getApplication<Application>())

    private val _isWifiEnabled = MutableStateFlow(false)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()


    init {
        observeWifiStatus()
        SocketManager.tcpServer = TCPServer(_fileList, _compressedImages) {
            _serverStatus.value = it
        }
        startSocketService()
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

    fun startSocketService() {
        val intent = Intent(getApplication<Application>(), SocketService::class.java).apply {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }

        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            sendBroadcastHandshake()
        }
    }

    /*fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            server.start()
        }
    }*/

    /*override fun onCleared() {
        super.onCleared()
        val intent = Intent(context, SocketService::class.java)
        context.stopService(intent)
    }*/

    fun sendBroadcastHandshake() {

        if(isSearching.value) return
        isSearching.value = true

        _serverStatus.value = getApplication<Application>().getString(R.string.connexion_loading)

        viewModelScope.launch(Dispatchers.IO) {
            UDPBroadcaster().sendBroadcastAndListen { ip ->
                _serverStatus.value = getApplication<Application>().getString(R.string.online)
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