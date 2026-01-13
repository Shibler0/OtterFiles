package com.shibler.transferfiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import kotlin.concurrent.thread

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TransferFiles",
    ) {
        FileExplorerContent()
    }
}

@Composable
fun FileExplorerContent() {
    // ÉTAT : Navigation locale
    var currentDirectory: File? by remember { mutableStateOf(null) }

    // ÉTAT : Téléphone
    var remoteFiles by remember { mutableStateOf<List<String>>(emptyList()) }
    var isShowingPhone by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val localFiles = remember(currentDirectory) {
        if (currentDirectory == null) File.listRoots().toList()
        else currentDirectory!!.listFiles()?.toList() ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Barre de sélection du mode ---
        TopNavigationRow(
            onShowPC = { isShowingPhone = false },
            onShowPhone = {
                isLoading = true
                thread {
                    val client =  AndroidClient("192.168.1.89") // METS L'IP DE TON TEL ICI
                    remoteFiles = client.getRemoteFiles()
                    isShowingPhone = true
                    isLoading = false
                }
            },
            isPhoneActive = isShowingPhone
        )

        Divider()

        // --- Affichage du contenu ---
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (isShowingPhone) {
                // LISTE TÉLÉPHONE
                LazyColumn {
                    items(remoteFiles) { fileName ->
                        FileItemRow(name = fileName, isDirectory = false, isPhone = true)
                    }
                }
            } else {
                // LISTE PC (LOCALE)
                Column {
                    // Bouton retour pour le PC
                    if (currentDirectory != null) {
                        TextButton(onClick = { currentDirectory = currentDirectory?.parentFile }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            Text("Retour")
                        }
                    }

                    LazyColumn {
                        items(localFiles) { file ->
                            val name = file.name.ifEmpty { file.absolutePath }
                            FileItemRow(
                                name = name,
                                isDirectory = file.isDirectory,
                                isPhone = false,
                                onClick = {
                                    if (file.isDirectory) currentDirectory = file
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopNavigationRow(onShowPC: () -> Unit, onShowPhone: () -> Unit, isPhoneActive: Boolean) {
    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onShowPC,
            colors = ButtonDefaults.buttonColors(backgroundColor = if (!isPhoneActive) Color.LightGray else Color.White)
        ) {
            Icon(Icons.Default.Computer, null)
            Spacer(Modifier.width(4.dp))
            Text("Mon PC")
        }
        Button(
            onClick = onShowPhone,
            colors = ButtonDefaults.buttonColors(backgroundColor = if (isPhoneActive) Color.LightGray else Color.White)
        ) {
            Icon(Icons.Default.PhoneAndroid, null)
            Spacer(Modifier.width(4.dp))
            Text("Mon Téléphone")
        }
    }
}

@Composable
fun FileItemRow(name: String, isDirectory: Boolean, isPhone: Boolean, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = if (isPhone) Icons.Default.PhoneAndroid else if (isDirectory) Icons.Default.Computer else Icons.Default.Computer
        Text(name)
    }
}