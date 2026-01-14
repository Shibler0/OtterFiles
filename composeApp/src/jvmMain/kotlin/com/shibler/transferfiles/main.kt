package com.shibler.transferfiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import transferfiles.composeapp.generated.resources.Res
import transferfiles.composeapp.generated.resources.unbounded

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TransferFiles",
    ) {
        val phoneIP = "192.168.1.89"
        val vm = MainVM()
        val client = DesktopClient(phoneIP)
        FileExplorerContent(vm, client, phoneIP)
    }
}

@Composable
fun FileExplorerContent(vm : MainVM, client : DesktopClient, phoneIP: String) {
    var currentDirectory: File? by remember { mutableStateOf(null) }

    val remoteFiles by vm.remoteFiles.collectAsState()
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
                    vm.getRemoteFiles(client.getRemoteFiles())
                    isShowingPhone = true
                    isLoading = false
                }
            },
            isPhoneActive = isShowingPhone,
            phoneIP = phoneIP
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
                        FileItemRow(name = fileName, isDirectory = false, isPhone = true) {
                        }
                    }
                    item {
                        /*Row(Modifier.fillMaxWidth().clickable{ client.downloadFile("test") }.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Save")
                        }*/
                    }
                }

                DownloadBtn(modifier = Modifier.align(Alignment.BottomCenter)){ client.downloadFile() }
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

                DownloadBtn(modifier = Modifier.align(Alignment.BottomCenter)){}
            }
        }
    }
}

@Composable
fun TopNavigationRow(onShowPC: () -> Unit, onShowPhone: () -> Unit, isPhoneActive: Boolean, phoneIP : String) {
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
            Text("Mon Téléphone ($phoneIP)")
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


@Composable
fun DownloadBtn(modifier: Modifier, onClick: () -> Unit) {

    val unbounded = FontFamily(Font(Res.font.unbounded))

    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .fillMaxWidth(0.9f)
            .clickable{onClick()}
            .border(2.dp, brush = Brush.linearGradient(listOf(Color(153, 51, 255, 255), Color(217, 51, 255, 255)
            )), RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(top = 12.dp, bottom = 12.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Telecharger", fontSize = 16.sp, fontFamily = unbounded)
    }
}