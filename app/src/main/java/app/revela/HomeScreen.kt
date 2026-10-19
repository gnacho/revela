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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

val SORT_OPTIONS = listOf(
    "recent" to "Más recientes",
    "oldest" to "Más antiguas",
    "name_az" to "Nombre (A-Z)",
    "name_za" to "Nombre (Z-A)"
)

fun sortedEntries(entries: List<SessionEntry>, sortMode: String): List<SessionEntry> = when (sortMode) {
    "oldest" -> entries.sortedBy { it.lastOpenedAt }
    "name_az" -> entries.sortedBy { it.displayName.lowercase() }
    "name_za" -> entries.sortedByDescending { it.displayName.lowercase() }
    else -> entries.sortedByDescending { it.lastOpenedAt }
}

/** HOME: biblioteca de sesiones de edicion persistidas; tap reabre, + abre foto nueva. */
@Composable
fun HomeScreen(
    refreshTick: Int,
    appSettings: app.revela.core.AppSettings,
    onSettingsChange: (app.revela.core.AppSettings) -> Unit,
    onOpenPhoto: (Uri) -> Unit,
    onOpenSession: (SessionEntry) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<SessionEntry>>(emptyList()) }
    var thumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    var confirmDelete by remember { mutableStateOf<SessionEntry?>(null) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    fun reload() {
        SessionIndexStore.migrateLegacy(context.filesDir)
        val list = sortedEntries(SessionIndexStore.list(context.filesDir), appSettings.sortMode)
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

    LaunchedEffect(refreshTick, appSettings.sortMode) { reload() }

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
            .background(Ui.bg)
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
                color = Ui.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            // Ordenacion
            Box {
                IconButton(onClick = { sortMenuOpen = true }) {
                    Icon(Icons.Outlined.Sort, "Ordenar fotos", tint = Ui.icon)
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    SORT_OPTIONS.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    label,
                                    color = if (appSettings.sortMode == id) Ui.accent else Ui.text,
                                    fontWeight = if (appSettings.sortMode == id) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                sortMenuOpen = false
                                onSettingsChange(appSettings.copy(sortMode = id))
                            }
                        )
                    }
                }
            }
            // Vista rejilla / lista
            IconButton(onClick = {
                onSettingsChange(
                    appSettings.copy(viewMode = if (appSettings.viewMode == "grid") "list" else "grid")
                )
            }) {
                Icon(
                    if (appSettings.viewMode == "grid") Icons.Outlined.ViewList else Icons.Outlined.ViewModule,
                    "Cambiar vista",
                    tint = Ui.icon
                )
            }
            IconButton(onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.AddPhotoAlternate, "Abrir foto", tint = Ui.icon)
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
                    color = Ui.textDim,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        } else if (appSettings.viewMode == "list") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Ui.card)
                            .clickable { onOpenSession(entry) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Ui.panel)
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
                                    tint = Ui.line,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                entry.displayName,
                                color = Ui.text,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                relativeTime(entry.lastOpenedAt),
                                color = Ui.textDim,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { confirmDelete = entry }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar sesion",
                                tint = Ui.textDim,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
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
                                .background(Ui.card)
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
                                    tint = Ui.line,
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
                                    .background(Color(0x66000000), CircleShape)
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
                            color = Ui.text,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            relativeTime(entry.lastOpenedAt),
                            color = Ui.textDim,
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
            title = { Text("Eliminar edicion", color = Ui.text) },
            text = {
                Text(
                    "Se borraran los ajustes guardados de \"${entry.displayName}\". La foto original no se toca.",
                    color = Ui.textDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    SessionIndexStore.delete(context.filesDir, entry.id)
                    confirmDelete = null
                    reload()
                }) { Text("Eliminar", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancelar", color = Ui.text) }
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
