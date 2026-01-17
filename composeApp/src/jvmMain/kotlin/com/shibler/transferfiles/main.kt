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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
        val vm = MainVM()
        FileExplorerContent(vm)
    }
}

@Composable
fun FileExplorerContent(vm : MainVM) {

    val listState = rememberLazyListState()
    val client = DesktopClient(vm.serverSocketAddress.value)

    val remoteFiles by vm.remoteFiles.collectAsState()
    val phoneIP by vm.serverSocketAddress.collectAsState()
    val selectedFiles by vm.selectedFiles.collectAsState()

    var isShowingPhone by remember { mutableStateOf(false) }
    var isLoading = vm.isLoading.collectAsState().value


    val scope = rememberCoroutineScope()



    Column(modifier = Modifier.fillMaxSize()) {
        TopNavigationRow(
            onShowPhone = {
                isLoading = true
                thread {
                    vm.setRemoteFiles(client.getRemoteFiles())
                    isShowingPhone = true
                }
            },
            isPhoneActive = isShowingPhone,
            phoneIP = phoneIP
        )

        Divider()

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState
                ) {
                    items(remoteFiles) {

                        val fileName = it.files

                        val newName = fileName.substringAfterLast("/")
                        val extension = fileName.substringAfterLast(".").lowercase()


                        FileItemRow(name = newName, extension = extension, isSelected = it.selectedFiles) {
                            if(selectedFiles.contains(fileName)) {
                                vm.removeSelectedFile(fileName)
                                it.selectedFiles.value = false
                            } else {
                                it.selectedFiles.value = true
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
                                        vm.removeSelectedFile(it)
                                        remoteFiles.map { file ->
                                            if(file.files == it) {
                                                file.selectedFiles.value = false
                                            }
                                        }
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
fun TopNavigationRow(onShowPhone: () -> Unit, isPhoneActive: Boolean, phoneIP : String) {
    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
fun FileItemRow(name: String, extension: String, isSelected : MutableState<Boolean>, onClick: () -> Unit = {}) {

    val backgroundColor = if (isSelected.value) Color.LightGray else Color.White


    val iconTint = when (extension) {
        in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac") -> Color(0xFFE91E63)
        in listOf("mp4", "avi", "mkv", "mov", "wmv", "webm") -> Color(0xFFF44336)
        in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg") -> Color(0xFF4CAF50)
        in listOf("zip", "rar", "7z", "tar", "gz") -> Color(0xFFFFC107)
        "pdf" -> Color(0xFFD32F2F)
        in listOf("doc", "docx", "txt", "rtf", "odt") -> Color(0xFF2196F3)
        in listOf("xls", "xlsx", "csv") -> Color(0xFF388E3C)
        in listOf("xml", "json", "html", "css", "js", "kt", "java") -> Color(0xFF607D8B)
        else -> Color(0xFF757575)
    }

    val icon = when (extension) {
        in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac") -> Icons.Default.Audiotrack
        in listOf("mp4", "avi", "mkv", "mov", "wmv", "webm") -> Icons.Default.PlayCircle
        in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg") -> Icons.Default.Image
        in listOf("zip", "rar", "7z", "tar", "gz") -> Icons.Default.Inventory
        in listOf("pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx") -> Icons.Default.Description
        in listOf("xml", "json", "html", "css", "js", "kt") -> Icons.Default.Code
        else -> Icons.Default.InsertDriveFile
    }

    Row(
        Modifier.fillMaxWidth()
            .clickable{
                onClick()
            }
            .background(backgroundColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector =  icon, tint = iconTint, contentDescription = null)
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