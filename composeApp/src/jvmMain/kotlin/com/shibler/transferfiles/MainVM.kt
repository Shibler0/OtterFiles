package com.shibler.transferfiles

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FileList(val files : String, var selectedFiles : MutableState<Boolean> = mutableStateOf(false))


class MainVM : ViewModel() {

    private val _remoteFiles = MutableStateFlow<List<FileList>>(emptyList())
    val remoteFiles = _remoteFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<String>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _serverSocketAddress = MutableStateFlow("?")//192.0.0.2
    val serverSocketAddress = _serverSocketAddress.asStateFlow()

    val desktopClient = DesktopClient(serverSocketAddress.value)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow<Float>(0f)
    val progress = _progress.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _thumbnails = MutableStateFlow<List<ByteArray>>(emptyList())
    val thumbnails = _thumbnails.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            UDPDiscovery().listenForPhone { phoneIp ->
                setServerSocket(phoneIp)
                _isLoading.value = false
            }
            desktopClient



        }
    }

    fun setRemoteFiles(newFiles : List<String>) {
        _remoteFiles.value = newFiles.map { FileList(it) }
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

    fun setIsLoading() {
        _isLoading.value = !isLoading.value
    }


    fun setProgressionState(fileSize: Long, byteDownloaded: Long) {
        _progress.value = ((byteDownloaded.toFloat() * 100) / fileSize.toFloat())/100
    }

    fun resetProgressionState() {
        _progress.value = 0f
    }

    fun setQuery(query : String) {
        _query.value = query
    }

    fun setThumbnails(newThumbnails : List<ByteArray>) {
        _thumbnails.value = newThumbnails
    }


}