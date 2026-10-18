package app.revela

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revela.core.SessionEntry
import app.revela.render.SessionIndexStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** HOME: biblioteca de sesiones de edicion persistidas; tap reabre, + abre foto nueva. */
@Composable
fun HomeScreen(
    refreshTick: Int,
    onOpenPhoto: (Uri) -> Unit,
    onOpenSession: (SessionEntry) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<SessionEntry>>(emptyList()) }
    var thumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    var confirmDelete by remember { mutableStateOf<SessionEntry?>(null) }

    fun reload() {
        SessionIndexStore.migrateLegacy(context.filesDir)
        val list = SessionIndexStore.list(context.filesDir)
        entries = list
        scope.launch(Dispatchers.Default) {
            val map = mutableMapOf<String, ImageBitmap>()
            for (e in list) {
                if (e.thumbPath.isEmpty()) continue
                val f = File(context.filesDir, e.thumbPath)
                if (!f.exists()) continue
                runCatching {
                    BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap()?.let { map[e.id] = it }
                }
            }
            thumbs = map
        }
    }

    LaunchedEffect(refreshTick) { reload() }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onOpenPhoto(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "revela",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.AddPhotoAlternate, "Abrir foto", tint = Color.White)
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Todavia no hay ediciones.\nAbre una foto con el boton +.",
                    color = Color(0xFF8A8A8A),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1B1B))
                                .clickable { onOpenSession(entry) }
                        ) {
                            val thumb = thumbs[entry.id]
                            if (thumb != null) {
                                Image(
                                    bitmap = thumb,
                                    contentDescription = entry.displayName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF555555),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp)
                                )
                            }
                            IconButton(
                                onClick = { confirmDelete = entry },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(30.dp)
                                    .background(Color(0x99000000), CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Eliminar sesion",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            entry.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            relativeTime(entry.lastOpenedAt),
                            color = Color(0xFF8A8A8A),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    confirmDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Eliminar edicion", color = Color.White) },
            text = {
                Text(
                    "Se borraran los ajustes guardados de \"${entry.displayName}\". La foto original no se toca.",
                    color = Color(0xFFCCCCCC)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    SessionIndexStore.delete(context.filesDir, entry.id)
                    confirmDelete = null
                    reload()
                }) { Text("Eliminar", color = Color(0xFFE57373)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancelar", color = Color.White) }
            }
        )
    }
}

internal fun relativeTime(thenMs: Long): String {
    val diff = System.currentTimeMillis() - thenMs
    if (diff < 60_000) return "ahora"
    if (diff < 3_600_000) return "hace ${diff / 60_000} min"
    if (diff < 86_400_000) return "hace ${diff / 3_600_000} h"
    if (diff < 7L * 86_400_000) return "hace ${diff / 86_400_000} d"
    val days = diff / 86_400_000
    return "hace $days d"
}
