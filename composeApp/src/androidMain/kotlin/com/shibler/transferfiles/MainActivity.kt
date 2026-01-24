package com.shibler.transferfiles

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import transferfiles.composeapp.generated.resources.Res
import transferfiles.composeapp.generated.resources.unbounded
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val vm = AndroidVM()

        setContent {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = "package:${LocalContext.current.packageName}".toUri()
                    }
                    LocalContext.current.startActivity(intent)
                }
            }

            Scaffold { paddingValues ->
                AndroidAppContent(paddingValues, vm)
            }

        }
    }
}

@Composable
fun AndroidAppContent(paddingValues: PaddingValues, vm : AndroidVM) {

    val fileList = vm.fileList.collectAsState().value
    val serverIP = vm.serverIP.collectAsState().value
    val unbounded = FontFamily(Font(Res.font.unbounded))
    val serverStatus by vm.serverStatus.collectAsStateWithLifecycle()
    val isSearching by vm.isSearching.collectAsStateWithLifecycle()
    val compressedImages by vm.compressedImages.collectAsStateWithLifecycle()


    Box(
        modifier = Modifier
            .background(Color(18, 18, 18, 255))
            .padding(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(compressedImages.size) {

                    AsyncImage(
                        model = compressedImages[it].thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Text(compressedImages[it].path.substringAfterLast("/"), fontSize = 12.sp, color = Color.White)
                }
            }

        SendBroadcastBtn(
            onClick = {
                vm.sendBroadcastHandshake()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        ) {

            Text(serverStatus, color = Color.White, fontSize = 16.sp, fontFamily = unbounded)
            if(isSearching) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp).size(30.dp), color = Color.White)
            }
        }


            /*Text("Fichiers détectés : ${fileList.size}", style = MaterialTheme.typography.headlineSmall)

            LazyColumn(Modifier.weight(1f)) {
                items(fileList) { fileName ->
                    Text(fileName, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Statut serveur :", color = Color.Black, fontSize = 14.sp)
                Text(serverStatus, color = Color.Black, fontSize = 14.sp)
            }

            SendBroadcastBtn(
                onClick = {
                    vm.sendBroadcastHandshake()
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text("En attente d'une connexion", color = Color.White, fontSize = 16.sp, fontFamily = unbounded, modifier = Modifier.weight(2f).align(Alignment.CenterHorizontally))
                if(isSearching) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp).size(30.dp), color = Color.White)
                }

            }

            Text(serverIP, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(30.dp))*/


    }


}


@Composable
fun SendBroadcastBtn(modifier: Modifier = Modifier, onClick: () -> Unit = {} , composable : @Composable () -> Unit = {}) {

    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(45, 61, 190, 255), Color(22, 50, 190, 255))),
                CircleShape
            )
            .clickable{ onClick() }
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        composable()
    }
}

