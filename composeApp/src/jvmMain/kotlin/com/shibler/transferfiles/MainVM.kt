package com.shibler.transferfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainVM : ViewModel() {


    private val _remoteFiles = MutableStateFlow<List<String>>(emptyList())
    val remoteFiles = _remoteFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<String>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _serverSocketAddress = MutableStateFlow<String>("")
    val serverSocketAddress = _serverSocketAddress.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            UDPDiscovery().listenForPhone()
        }
    }

    fun setRemoteFiles(newFiles : List<String>) {
        _remoteFiles.value = newFiles
    }

    fun addSelectedFile(index : String) {
        _selectedFiles.value += index
    }

    fun removeSelectedFile(index : String) {
        _selectedFiles.value -= index
    }

    fun setServerSocket(newAddress : String) {
        _serverSocketAddress.value = newAddress
    }

}