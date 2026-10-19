package app.revela

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
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
    "recent" to R.string.sort_recent,
    "oldest" to R.string.sort_oldest,
    "name_az" to R.string.sort_name_az,
    "name_za" to R.string.sort_name_za
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
    var lookIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var filter by remember { mutableStateOf("all") }
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
            lookIds = list.filter { SessionIndexStore.hasLook(context.filesDir, it.id) }
                .map { it.id }.toSet()
        }
    }

    // Tocar "Con nota" relee el indice: una nota puede haberse guardado en otra pantalla
    LaunchedEffect(refreshTick, appSettings.sortMode, filter) { reload() }

    val pickerLauncher = rememberLauncherForActivityResult(
        OpenImageDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
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
                stringResource(R.string.app_name),
                color = Ui.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            // Ordenacion
            Box {
                IconButton(onClick = { sortMenuOpen = true }) {
                    Icon(Icons.Outlined.Sort, stringResource(R.string.sort_photos), tint = Ui.icon)
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    SORT_OPTIONS.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(label),
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
                    stringResource(R.string.toggle_view),
                    tint = Ui.icon
                )
            }
            IconButton(onClick = {
                pickerLauncher.launch(arrayOf("image/*"))
            }) {
                Icon(Icons.Filled.AddPhotoAlternate, stringResource(R.string.open_photo), tint = Ui.icon)
            }
        }

        val visible = when (filter) {
            "looks" -> entries.filter { it.id in lookIds }
            "notes" -> entries.filter { it.note.isNotBlank() }
            else -> entries
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == "all",
                onClick = { filter = "all" },
                label = { Text(stringResource(R.string.filter_all), fontSize = 12.sp) }
            )
            FilterChip(
                selected = filter == "looks",
                onClick = { filter = "looks" },
                label = { Text(stringResource(R.string.filter_looks), fontSize = 12.sp) }
            )
            FilterChip(
                selected = filter == "notes",
                onClick = { filter = "notes" },
                label = { Text(stringResource(R.string.filter_notes), fontSize = 12.sp) }
            )
        }

        if (visible.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(if (entries.isEmpty()) R.string.empty_library else R.string.filter_empty),
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
                items(visible, key = { it.id }) { entry ->
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
                                relativeTime(context, entry.lastOpenedAt),
                                color = Ui.textDim,
                                fontSize = 12.sp
                            )
                            if (entry.note.isNotBlank()) {
                                Text(
                                    entry.note,
                                    color = Ui.accent,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = { confirmDelete = entry }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_session),
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
                items(visible, key = { it.id }) { entry ->
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
                                    contentDescription = stringResource(R.string.delete_session),
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
                            relativeTime(context, entry.lastOpenedAt),
                            color = Ui.textDim,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                        if (entry.note.isNotBlank()) {
                            Text(
                                entry.note,
                                color = Ui.accent,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    confirmDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.delete_edit), color = Ui.text) },
            text = {
                Text(
                    stringResource(R.string.delete_session_message, entry.displayName),
                    color = Ui.textDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    SessionIndexStore.delete(context.filesDir, entry.id)
                    confirmDelete = null
                    reload()
                }) { Text(stringResource(R.string.delete), color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.cancel), color = Ui.text) }
            }
        )
    }
}

internal fun relativeTime(context: Context, thenMs: Long): String {
    val diff = System.currentTimeMillis() - thenMs
    if (diff < 60_000) return context.getString(R.string.relative_now)
    if (diff < 3_600_000) return context.getString(R.string.relative_minutes, diff / 60_000)
    if (diff < 86_400_000) return context.getString(R.string.relative_hours, diff / 3_600_000)
    if (diff < 7L * 86_400_000) return context.getString(R.string.relative_days, diff / 86_400_000)
    val days = diff / 86_400_000
    return context.getString(R.string.relative_days, days)
}
