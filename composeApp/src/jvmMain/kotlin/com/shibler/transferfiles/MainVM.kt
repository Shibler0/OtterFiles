package com.shibler.transferfiles

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class fileList(val files : String, var selectedFiles : MutableState<Boolean> = mutableStateOf(false))

class MainVM : ViewModel() {

    private val _remoteFiles = MutableStateFlow<List<fileList>>(emptyList())
    val remoteFiles = _remoteFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<String>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _serverSocketAddress = MutableStateFlow("?.?.?.?")
    val serverSocketAddress = _serverSocketAddress.asStateFlow()


    val desktopClient = DesktopClient(serverSocketAddress.value)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            UDPDiscovery().listenForPhone(this@MainVM)
            desktopClient
        }
    }

    fun setRemoteFiles(newFiles : List<String>) {
        _remoteFiles.value = newFiles.map { fileList(it) }
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