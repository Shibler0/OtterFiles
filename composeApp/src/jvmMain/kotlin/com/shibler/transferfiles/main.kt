package com.shibler.transferfiles

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import transferfiles.composeapp.generated.resources.Res
import transferfiles.composeapp.generated.resources.unbounded
import kotlin.concurrent.thread

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

    val listState = rememberLazyListState()

    val remoteFiles by vm.remoteFiles.collectAsState()
    val selectedFiles by vm.selectedFiles.collectAsState()

    var isShowingPhone by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()



    Column(modifier = Modifier.fillMaxSize()) {
        // --- Barre de sélection du mode ---
        TopNavigationRow(
            onShowPhone = {
                isLoading = true
                thread {
                    vm.setRemoteFiles(client.getRemoteFiles())
                    isShowingPhone = true
                    isLoading = false
                }
            },
            refresh = {
                thread {
                    vm.setRemoteFiles(client.getRemoteFiles())
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
            } else {
                // LISTE TÉLÉPHONE
                LazyColumn(
                    state = listState
                ) {
                    items(remoteFiles) { fileName ->
                        FileItemRow(name = fileName) {
                            if(selectedFiles.contains(fileName)) {
                                vm.removeSelectedFile(fileName)
                            } else {
                                vm.addSelectedFile(fileName)
                                println(vm.selectedFiles.value)
                            }

                        }
                    }
                }
                if(selectedFiles.isNotEmpty()) {

                        DownloadBtn(modifier = Modifier.align(Alignment.BottomCenter)){
                            selectedFiles.forEach {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        client.downloadFile(it)
                                    } catch(e: Exception) {
                                        println("Erreur : ${e.message}")
                                    }

                                }
                            }


                        }

                }

            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun TopNavigationRow(onShowPhone: () -> Unit, refresh : () -> Unit = {}, isPhoneActive: Boolean, phoneIP : String) {
    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = onShowPhone,
            colors = ButtonDefaults.buttonColors(backgroundColor = if (isPhoneActive) Color.LightGray else Color.White)
        ) {
            Icon(Icons.Default.PhoneAndroid, null)
            Spacer(Modifier.width(4.dp))
            Text("Mon Téléphone ($phoneIP)")
        }

        Icon(Icons.Default.Refresh, null, modifier = Modifier.clickable { refresh() }.clip(CircleShape))
    }
}

@Composable
fun FileItemRow(name: String, onClick: () -> Unit = {}) {

    var isSelected by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth()
            .clickable{
                isSelected = !isSelected
                onClick()
            }
            .background(if (isSelected) Color.LightGray else Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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