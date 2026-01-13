package com.shibler.transferfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainVM : ViewModel() {

    private val _pathFile = MutableStateFlow<String>("")
    val pathFile = _pathFile.asStateFlow()

    private val _remoteFiles = MutableStateFlow<List<String>>(emptyList())
    val remoteFiles = _remoteFiles.asStateFlow()

    init {
        viewModelScope.launch {

        }
    }

    fun savePathFile(newPath : String) {
        _pathFile.value = newPath
    }

    fun getRemoteFiles(newFiles : List<String>) {
        _remoteFiles.value = newFiles
    }
}