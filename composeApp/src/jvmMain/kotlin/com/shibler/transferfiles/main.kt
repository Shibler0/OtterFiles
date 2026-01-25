package com.shibler.transferfiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview
import transferfiles.composeapp.generated.resources.Res
import transferfiles.composeapp.generated.resources.unbounded
import java.io.ByteArrayInputStream
import kotlin.concurrent.thread

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OtterFiles",
        icon = painterResource("otterfileslogo.png")
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
    val progression by vm.progress.collectAsState()

    var isLoading = vm.isLoading.collectAsState().value
    val thumbnails by vm.thumbnails.collectAsState()
    var isList by remember { mutableStateOf(false) }

    //var query by remember { mutableStateOf("") }
    val query by vm.query.collectAsState()


    val filteredItems = remember(query, remoteFiles) {
        if (query.isBlank()) {
            remoteFiles
        } else {
            remoteFiles.filter {
                it.files.contains(query, ignoreCase = true)
            }
        }
    }

    val filteredThumbnails = remember(query, thumbnails) {
        if (query.isBlank()) {
            thumbnails
        } else {
            thumbnails.filter {
                it.path.contains(query, ignoreCase = true)
            }
        }
    }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color(18, 18, 18, 255))) {
        TopNavigationRow(
            onShowPhone = {
                isLoading = true
                thread {
                    vm.setRemoteFiles(client.getRemoteFiles())
                    vm.addThumbnail(client.getThumbnails())
                }
            },
            phoneIP = phoneIP,
            fileSize = remoteFiles.size,
            query = query,
            vm = vm
        )

        Divider(color = Color(47, 47, 47, 255))

        Box(modifier = Modifier.fillMaxSize()) {

            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {

                if(!isList) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredThumbnails) {

                            val fileName = it.path

                            ItemListPicture(it, isSelected = it.selectedFiles) {
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
                } else {
                    LazyColumn(
                    state = listState
                ) {
                    items(filteredItems) {

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
                }




                if(selectedFiles.isNotEmpty()) {

                        DownloadBtn(modifier = Modifier.align(Alignment.BottomCenter), progression = progression, fileNumber = selectedFiles.size){
                            selectedFiles.forEach {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        client.downloadFile(it, vm)
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

            changeList(modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
                isList = mutableStateOf(isList),
                onClickList = {
                    isList = true
                    vm.emptySelectedFiles()
                              },
                onClickGrid = {
                    isList = false
                    vm.emptySelectedFiles()
                }
            )

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
fun TopNavigationRow(onShowPhone: () -> Unit, phoneIP : String, fileSize : Int, query: String, vm: MainVM) {

    val unbounded = FontFamily(Font(Res.font.unbounded))

    Column {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onShowPhone,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(30, 30, 30, 255)),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
            ) {
                Icon(imageVector = Icons.Default.PhoneAndroid, tint = Color.White , contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Mon Téléphone ($phoneIP)", fontFamily = unbounded, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Icon(imageVector =  Icons.AutoMirrored.Filled.InsertDriveFile, tint = Color(0xFFFFC107),contentDescription = null)
            Text("$fileSize", fontFamily = unbounded, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            BasicTextField(
                value = query,
                onValueChange = { vm.setQuery(it) },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(Color(0xFFCECECE)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Recherche",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Rechercher...",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }
    }


}

@Composable
fun changeList(modifier: Modifier = Modifier, isList : MutableState<Boolean>, onClickList : () -> Unit = {}, onClickGrid : () -> Unit = {}) {
    Row(
        modifier = Modifier
            .then(modifier)
            .background(Color(30, 30, 30, 255), RoundedCornerShape(16.dp))
    ) {
        Icon(imageVector =  Icons.Default.GridView, tint = if (isList.value) Color.White else Color.Gray, contentDescription = null, modifier = Modifier.padding(8.dp).clickable { onClickGrid() })
        Icon(imageVector = Icons.AutoMirrored.Filled.ViewList, tint = if (!isList.value) Color.White else Color.Gray, contentDescription = null, modifier = Modifier.padding(8.dp).clickable { onClickList() })
    }
}

@Composable
fun ItemListPicture(thumbnail: Thumbnail,isSelected : MutableState<Boolean> = mutableStateOf(false), onClick: () -> Unit = {}) {

    val unbounded = FontFamily(Font(Res.font.unbounded))
    val size = if(isSelected.value) 90.dp else 100.dp

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isSelected.value -> Color(18, 18, 18, 255).lighten(0.08f).copy(alpha = 0.5f)
        isHovered -> Color(18, 18, 18, 255).lighten(0.05f)
        else -> Color(0, 0, 0, 0)
    }

    if(thumbnail.thumbnail != null) {
        Box(
            modifier = Modifier
                .size(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(bitmap = thumbnail.thumbnail.toImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .aspectRatio(1f)
                    .clickable { onClick() }
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
            )
        }

    } else {
        Text(
            thumbnail.path.substringAfterLast(
                "/"
            ), fontSize = 12.sp, color = Color.White,
            fontFamily = unbounded,
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
fun FileItemRow(name: String, extension: String, isSelected : MutableState<Boolean>, onClick: () -> Unit = {}) {

    val unbounded = FontFamily(Font(Res.font.unbounded))

    val interactionSource = remember { MutableInteractionSource() } //0.05f
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isSelected.value -> Color(18, 18, 18, 255).lighten(0.08f)
        isHovered -> Color(18, 18, 18, 255).lighten(0.05f)
        else -> Color(0, 0, 0, 0)
    }


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
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ){
                onClick()
            }
            .hoverable(interactionSource)
            .background(backgroundColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector =  icon, tint = iconTint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(name, fontFamily = unbounded, fontWeight = FontWeight.Bold, color = Color.White)
    }
}


@Composable
fun DownloadBtn(modifier: Modifier, progression : Float = 0f, fileNumber : Int, onClick: () -> Unit) {

    val unbounded = FontFamily(Font(Res.font.unbounded))

    val downloadtxt = if(progression == 0f) "Télécharger" else "${(progression * 100).toInt()}%"

    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .fillMaxWidth(0.9f)
            .clickable{onClick()}
            .border(2.dp, brush = Brush.linearGradient(listOf(Color(153, 51, 255, 255), Color(217, 51, 255, 255)
            )), RoundedCornerShape(20.dp))
            .background(Color(18, 18, 18, 255), RoundedCornerShape(20.dp))
            .drawWithCache {
                val progress = progression
                val progressWidth = size.width * progress

                val gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(153, 51, 255, 255),
                        Color(217, 51, 255, 255)
                    ),
                    start = Offset.Zero,
                    end = Offset(progressWidth, 0f)
                )

                onDrawBehind {
                    drawRect(
                        brush = gradientBrush,
                        size = Size(progressWidth, size.height)
                    )
                }
            }
            .padding(top = 12.dp, bottom = 12.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$downloadtxt ($fileNumber)", fontSize = 16.sp, fontFamily = unbounded, color = Color.White)
    }
}

fun Color.lighten(factor: Float): Color {
    require(factor in 0f..1f)

    return Color(
        red = red + (1f - red) * factor,
        green = green + (1f - green) * factor,
        blue = blue + (1f - blue) * factor,
        alpha = alpha
    )
}

fun ByteArray.toImageBitmap(): ImageBitmap =
    loadImageBitmap(ByteArrayInputStream(this))

