package app.revela

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vignette
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.HdrStrong
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Texture
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revela.core.ControlPoint
import app.revela.core.CurvePoint
import app.revela.core.Edit
import app.revela.core.EditStack
import app.revela.core.FrameMath
import app.revela.core.GeometryModel
import app.revela.core.Look
import app.revela.core.Tool
import app.revela.render.BaseImage
import app.revela.core.EffectiveAdjustments
import app.revela.core.HiddenLayers
import app.revela.core.LayerList
import app.revela.render.ExportOptions
import app.revela.render.ExportProcessor
import app.revela.render.PreviewRender
import app.revela.render.SessionIndexStore
import app.revela.render.SessionStore
import app.revela.core.AutoLevels
import app.revela.core.CustomStyle
import app.revela.core.CustomStylesCodec
import app.revela.core.WbParam
import app.revela.core.WelcomeStore
import app.revela.core.Presets
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Details
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.Flare
import androidx.compose.material.icons.outlined.HdrStrong
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.Tonality
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vignette
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.outlined.FilterBAndW
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

/** Nombre legible de la herramienta que genero una capa (0 si no tiene mapeo). */
@StringRes
fun toolLabelRes(toolId: String): Int = when (toolId.substringBefore(':')) {
    Tool.EXPOSURE, Tool.CONTRAST, Tool.SATURATION -> R.string.tool_tune
    Tool.CURVES -> R.string.tool_curves
    Tool.TONAL -> R.string.tool_tone
    Tool.VIGNETTE -> R.string.tool_vignette
    Tool.HEAL -> R.string.tool_heal
    Tool.CROP -> R.string.tool_crop
    Tool.PERSPECTIVE -> R.string.tool_perspective
    Tool.EXPAND -> R.string.tool_expand
    Tool.SELECTIVE -> R.string.tool_selective
    Tool.BRUSH -> R.string.tool_brush
    Tool.HEAD_POSE -> R.string.tool_head_pose
    Tool.BNW -> R.string.tool_bnw
    Tool.WHITE_BALANCE -> R.string.tool_white_balance
    Tool.DETAILS -> R.string.tool_details
    Tool.HDR -> R.string.tool_hdr
    Tool.GRAIN -> R.string.tool_grain
    Tool.GLOW -> R.string.tool_glow
    Tool.LENS_BLUR -> R.string.tool_lens_blur
    Tool.DOUBLE_EXPO -> R.string.tool_double_expo
    Tool.TEXT -> R.string.tool_text
    Tool.FRAME -> R.string.tool_frame
    Tool.LOOK -> R.string.tool_looks
    else -> 0
}

enum class EditorTool(val id: String) {
    TUNE("tune"),
    CURVES(Tool.CURVES),
    TONE(Tool.TONAL),
    VIGNETTE(Tool.VIGNETTE),
    HEAL(Tool.HEAL),
    CROP(Tool.CROP),
    ROTATE("rotate"),
    PERSPECTIVE(Tool.PERSPECTIVE),
    EXPAND(Tool.EXPAND),
    SELECTIVE(Tool.SELECTIVE),
    BRUSH(Tool.BRUSH),
    HEAD_POSE(Tool.HEAD_POSE),
    BNW(Tool.BNW),
    WB(Tool.WHITE_BALANCE),
    DETAILS(Tool.DETAILS),
    HDR(Tool.HDR),
    GRAIN(Tool.GRAIN),
    GLOW(Tool.GLOW),
    LENS_BLUR(Tool.LENS_BLUR),
    DRAMA("drama"),
    VINTAGE("vintage"),
    GRAINY_FILM("grainyFilm"),
    RETROLUX("retrolux"),
    GRUNGE("grunge"),
    NOIR("noir"),
    PORTRAIT("portrait"),
    DOUBLE_EXPO(Tool.DOUBLE_EXPO),
    TEXT(Tool.TEXT),
    FRAME(Tool.FRAME)
}

/** Receta de preset asociada a una herramienta (solo las 7 de preset). */
fun EditorTool.presetKind(): Presets.Kind? = when (this) {
    EditorTool.DRAMA -> Presets.Kind.DRAMA
    EditorTool.VINTAGE -> Presets.Kind.VINTAGE
    EditorTool.GRAINY_FILM -> Presets.Kind.GRAINY_FILM
    EditorTool.RETROLUX -> Presets.Kind.RETROLUX
    EditorTool.GRUNGE -> Presets.Kind.GRUNGE
    EditorTool.NOIR -> Presets.Kind.NOIR
    EditorTool.PORTRAIT -> Presets.Kind.PORTRAIT
    else -> null
}

/** Manijas visibles de Perspectiva segun el modo (8 puntos: 4 esquinas + 4 puntos medios de borde). */
fun perspHandles(mode: String, c: app.revela.core.PerspectiveCorners): List<Pair<Float, Float>> = when (mode) {
    "scale" -> listOf(
        (c.tlX + c.trX) / 2f to minOf(c.tlY, c.trY),
        maxOf(c.trX, c.brX) to (c.trY + c.brY) / 2f,
        (c.blX + c.brX) / 2f to maxOf(c.blY, c.brY),
        minOf(c.tlX, c.blX) to (c.tlY + c.blY) / 2f
    )
    "rotate" -> emptyList()
    else -> listOf(c.tlX to c.tlY, c.trX to c.trY, c.brX to c.brY, c.blX to c.blY)
}

fun perspNearestHandle(mode: String, c: app.revela.core.PerspectiveCorners, u: Float, v: Float): Int {
    val hs = perspHandles(mode, c)
    var best = -1
    var bd = 0.09f
    hs.forEachIndexed { i, (hx, hy) ->
        val d = abs(hx - u) + abs(hy - v)
        if (d < bd) { bd = d; best = i }
    }
    return best
}

fun perspMoveHandle(
    mode: String,
    c: app.revela.core.PerspectiveCorners,
    idx: Int,
    u: Float,
    v: Float
): app.revela.core.PerspectiveCorners {
    val cu = u.coerceIn(-0.2f, 1.2f)
    val cv = v.coerceIn(-0.2f, 1.2f)
    return when (mode) {
        // scale: el punto medio mueve todo el borde perpendicularmente
        "scale" -> when (idx) {
            0 -> c.copy(tlY = cv, trY = cv)
            1 -> c.copy(trX = cu, brX = cu)
            2 -> c.copy(blY = cv, brY = cv)
            3 -> c.copy(tlX = cu, blX = cu)
            else -> c
        }
        // tilt: arrastrar una esquina mueve el borde al que pertenece (correccion de convergencia)
        "tilt" -> when (idx) {
            0 -> c.copy(tlY = cv, trY = cv)
            1 -> c.copy(trX = cu, brX = cu)
            2 -> c.copy(blY = cv, brY = cv)
            3 -> c.copy(tlX = cu, blX = cu)
            else -> c
        }
        else -> when (idx) {
            0 -> c.copy(tlX = cu, tlY = cv)
            1 -> c.copy(trX = cu, trY = cv)
            2 -> c.copy(brX = cu, brY = cv)
            3 -> c.copy(blX = cu, blY = cv)
            else -> c
        }
    }
}

/** Pestañas inferiores disponibles. */
enum class BottomTab { NONE, LOOKS, TOOLS }

/** Pestañas inferiores estilo Snapseed: texto plano en mayúsculas, seleccionado en azul. */
@Composable
fun BottomTabs(
    selected: BottomTab,
    enabled: Boolean,
    onSelect: (BottomTab) -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ui.card)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabButton(
            stringResource(R.string.tab_looks), selected == BottomTab.LOOKS, enabled, Modifier.weight(1f)
        ) { onSelect(if (selected == BottomTab.LOOKS) BottomTab.NONE else BottomTab.LOOKS) }
        TabButton(
            stringResource(R.string.tab_tools), selected == BottomTab.TOOLS, enabled, Modifier.weight(1f)
        ) { onSelect(if (selected == BottomTab.TOOLS) BottomTab.NONE else BottomTab.TOOLS) }
        TabButton(stringResource(R.string.tab_export), false, enabled, Modifier.weight(1f)) { onExport() }
    }
}

@Composable
fun TabButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        label,
        color = when {
            !enabled -> Ui.line
            selected -> Ui.accent
            else -> Ui.textDim
        },
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        letterSpacing = 1.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

/** Parametros de la herramienta Ajustar (gesto vertical para elegir, horizontal para valor). */
enum class TuneParam(val toolId: String, val wbParam: WbParam, @StringRes val labelRes: Int) {
    EXPOSURE(Tool.EXPOSURE, WbParam.TEMPERATURE, R.string.exposure),
    CONTRAST(Tool.CONTRAST, WbParam.TEMPERATURE, R.string.contrast),
    SATURATION(Tool.SATURATION, WbParam.TEMPERATURE, R.string.saturation),
    TEMPERATURE(Tool.WHITE_BALANCE, WbParam.TEMPERATURE, R.string.temperature),
    TINT(Tool.WHITE_BALANCE, WbParam.TINT, R.string.tint)
}

enum class SelParam(@StringRes val labelRes: Int) {
    BRIGHTNESS(R.string.brightness), CONTRAST(R.string.contrast), SATURATION(R.string.saturation)
}

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = runCatching {
            app.revela.core.SettingsStore.load(newBase.filesDir).language
        }.getOrDefault("system")
        super.attachBaseContext(app.revela.core.LocaleHelper.apply(newBase, lang))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // El contexto real de la activity no es el que devuelve attachBaseContext, asi que se
        // re-aplica aqui para que getString (toasts y textos fuera de Compose) use el idioma elegido.
        val lang = app.revela.core.SettingsStore.load(filesDir).language
        if (lang != "system") app.revela.core.LocaleHelper.apply(this, lang)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            // El tema se aplica ANTES de componer: la paleta Ui es global y reactiva al recomponer
            val initialTheme = remember { app.revela.core.SettingsStore.load(filesDir).theme }
            Ui.setDark(initialTheme == "dark")
            val scheme = if (Ui.dark) {
                androidx.compose.material3.darkColorScheme(
                    primary = Ui.accent,
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    secondary = Ui.accent,
                    surface = Ui.card,
                    onSurface = Ui.text,
                    surfaceVariant = Ui.panel,
                    onSurfaceVariant = Ui.textDim,
                    background = Ui.bg,
                    onBackground = Ui.text
                )
            } else {
                androidx.compose.material3.lightColorScheme(
                    primary = Ui.accent,
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    secondary = Ui.accent,
                    surface = Ui.card,
                    onSurface = Ui.text,
                    surfaceVariant = Ui.panel,
                    onSurfaceVariant = Ui.textDim,
                    background = Ui.bg,
                    onBackground = Ui.text
                )
            }
            MaterialTheme(
                colorScheme = scheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    var screen by remember { mutableStateOf("home") }
                    var startUri by remember { mutableStateOf<Uri?>(null) }
                    var startSessionId by remember { mutableStateOf<String?>(null) }
                    var homeTick by remember { mutableIntStateOf(0) }
                    var appSettings by remember {
                        mutableStateOf(app.revela.core.SettingsStore.load(context.filesDir))
                    }
                    var showWelcome by remember {
                        mutableStateOf(!WelcomeStore.isSeen(context.filesDir))
                    }

                    Box {
                        when (screen) {
                            "home" -> HomeScreen(
                                refreshTick = homeTick,
                                appSettings = appSettings,
                                onSettingsChange = {
                                    appSettings = it
                                    app.revela.core.SettingsStore.save(context.filesDir, it)
                                },
                                onOpenPhoto = { uri ->
                                    startUri = uri
                                    startSessionId = null
                                    screen = "editor"
                                },
                                onOpenSession = { entry ->
                                    startUri = entry.uri.toUri()
                                    startSessionId = entry.id
                                    screen = "editor"
                                }
                            )
                            else -> key(startUri) {
                            EditorScreen(
                                initialUri = startUri,
                                initialSessionId = startSessionId,
                                onHome = {
                                    homeTick++
                                    screen = "home"
                                },
                                onChanged = { homeTick++ }
                            )
                            }
                        }
                        if (showWelcome) {
                            WelcomeWizard(
                                onFinish = {
                                    WelcomeStore.markSeen(context.filesDir)
                                    showWelcome = false
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
fun EditorScreen(
    initialUri: Uri? = null,
    initialSessionId: String? = null,
    onHome: () -> Unit = {},
    onChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var base by remember { mutableStateOf<BaseImage?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var stack by remember { mutableStateOf(EditStack()) }
    var stackVersion by remember { mutableIntStateOf(0) }
    var geometry by remember { mutableStateOf(GeometryModel()) }
    var activeTool by remember { mutableStateOf<EditorTool?>(null) }
    var workingAdj by remember { mutableStateOf<app.revela.core.ColorMath.Adjustments?>(null) }
    var showOriginal by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(Float.NaN) }
    var renderJob by remember { mutableStateOf<Job?>(null) }
    var workingGeometry by remember { mutableStateOf<GeometryModel?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImageDetails by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf(app.revela.core.SettingsStore.load(context.filesDir)) }
    var showFavEditor by remember { mutableStateOf(false) }
    var cropDrawMode by remember { mutableStateOf(false) }
    var viewScale by remember { mutableFloatStateOf(1f) }
    var viewOffset by remember { mutableStateOf(Offset.Zero) }

    // Navegacion estilo Snapseed
    var bottomTab by remember { mutableStateOf(BottomTab.NONE) }
    var tuneParam by remember { mutableStateOf(TuneParam.EXPOSURE) }
    var paramMenuVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var showDeleteOriginal by remember { mutableStateOf(false) }
    val deleteRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, context.getString(R.string.delete_original_done), Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteOriginalPhoto() {
        val u = sourceUri ?: return
        val esPicker = u.authority?.contains("photopicker") == true || u.path?.contains("/picker/") == true
        // Con GetContent no llega permiso de escritura, asi que el camino fiable es el dialogo
        // del sistema (createDeleteRequest), que funciona tambien con URIs de documentos.
        if (!esPicker) {
            val launch = runCatching {
                val pi = android.provider.MediaStore.createDeleteRequest(context.contentResolver, listOf(u))
                deleteRequestLauncher.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(pi.intentSender).build()
                )
                true
            }.getOrDefault(false)
            if (launch) return
        }
        // Intento directo (solo funciona si el proveedor ya nos dio escritura)
        val deleted = runCatching {
            if (android.provider.DocumentsContract.isDocumentUri(context, u)) {
                android.provider.DocumentsContract.deleteDocument(context.contentResolver, u)
            } else {
                context.contentResolver.delete(u, null, null) > 0
            }
        }.getOrDefault(false)
        if (deleted) {
            Toast.makeText(context, context.getString(R.string.delete_original_done), Toast.LENGTH_SHORT).show()
            return
        }
        if (esPicker) {
            Toast.makeText(context, context.getString(R.string.delete_original_repick), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, context.getString(R.string.delete_original_error), Toast.LENGTH_SHORT).show()
        }
    }
    var showRevertConfirm by remember { mutableStateOf(false) }
    var showLayers by remember { mutableStateOf(false) }
    var showTip by remember { mutableStateOf<String?>(null) }
    var hiddenLayers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var lookThumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }

    // Estilos personalizados
    var customStyles by remember {
        mutableStateOf(app.revela.render.CustomStylesStore.load(context))
    }
    var customThumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    var showStyleNameDialog by remember { mutableStateOf(false) }
    var styleName by remember { mutableStateOf("") }
    var deleteStyle by remember { mutableStateOf<CustomStyle?>(null) }
    var manageStyle by remember { mutableStateOf<CustomStyle?>(null) }
    var manageName by remember { mutableStateOf("") }
    var selectedCustomId by remember { mutableStateOf<String?>(null) }

    // Barra de favoritos (ids de herramienta, persistidos)
    var favorites by remember { mutableStateOf(app.revela.core.FavoritesStore.load(context.filesDir)) }
    fun toggleFavorite(id: String) {
        favorites = if (id in favorites) favorites - id else favorites + id
        app.revela.core.FavoritesStore.save(context.filesDir, favorites)
    }

    // Selectivo
    var selParam by remember { mutableStateOf(SelParam.BRIGHTNESS) }
    var activePoint by remember { mutableIntStateOf(-1) }
    var workingPoint by remember { mutableStateOf<ControlPoint?>(null) }

    // Vineta
    var workingVignette by remember { mutableFloatStateOf(Float.NaN) }

    // Sanar
    var brushSize by remember { mutableFloatStateOf(0.05f) }
    val strokeX = remember { mutableStateListOf<Float>() }
    val strokeY = remember { mutableStateListOf<Float>() }

    // AUTO (mejorar foto) + histograma
    var histBins by remember { mutableStateOf(IntArray(48)) }
    var showAutoSnack by remember { mutableStateOf(false) }
    var autoSnapshot by remember { mutableStateOf<List<Edit>?>(null) }

    // Herramientas nuevas: estado de trabajo generico (un solo edit) + intensidad de preset
    var workingEdit by remember { mutableStateOf<Edit?>(null) }
    var workingPresetIntensity by remember { mutableFloatStateOf(Float.NaN) }

    // Perspectiva / Expandir / Pincel / Pose: estado de trabajo
    var perspMode by remember { mutableStateOf("free") }
    var perspCorners by remember { mutableStateOf(app.revela.core.PerspectiveCorners.IDENTITY) }
    var perspYaw by remember { mutableFloatStateOf(0f) }
    var perspPitch by remember { mutableFloatStateOf(0f) }
    var perspRoll by remember { mutableFloatStateOf(0f) }
    var expandEdges by remember { mutableStateOf(app.revela.core.ExpandEdges()) }
    var expandMirror by remember { mutableStateOf(false) }
    var brushParam by remember { mutableStateOf(app.revela.core.BrushMath.EXPOSURE) }
    var brushAmount by remember { mutableFloatStateOf(0.5f) }
    var hpLeft by remember { mutableStateOf<ControlPoint?>(null) }
    var hpRight by remember { mutableStateOf<ControlPoint?>(null) }
    var hpMouth by remember { mutableStateOf<ControlPoint?>(null) }
    var hpYawDeg by remember { mutableFloatStateOf(0f) }
    var hpPitchDeg by remember { mutableFloatStateOf(0f) }
    var hpStep by remember { mutableIntStateOf(0) }

    // Inicializa el estado de trabajo al entrar en cada herramienta geometrica
    LaunchedEffect(activeTool) {
        when (activeTool) {
            EditorTool.PERSPECTIVE -> {
                val existing = stack.edits.filterIsInstance<Edit.Perspective>().lastOrNull()
                perspMode = existing?.mode ?: "free"
                perspCorners = existing?.corners ?: app.revela.core.PerspectiveCorners.IDENTITY
                workingEdit = existing ?: Edit.Perspective(perspCorners, perspMode)
            }
            EditorTool.EXPAND -> {
                val existing = stack.edits.filterIsInstance<Edit.Expand>().lastOrNull()
                expandEdges = existing?.edges ?: app.revela.core.ExpandEdges()
                expandMirror = existing?.mirror ?: false
                workingEdit = existing ?: Edit.Expand(expandEdges, expandMirror)
            }
            EditorTool.HEAD_POSE -> {
                val existing = stack.edits.filterIsInstance<Edit.HeadPose>().lastOrNull()
                hpLeft = existing?.leftEye
                hpRight = existing?.rightEye
                hpMouth = existing?.mouth
                hpYawDeg = existing?.yawDeg ?: 0f
                hpPitchDeg = existing?.pitchDeg ?: 0f
                hpStep = 0
                workingEdit = existing
            }
            EditorTool.BRUSH -> {
                strokeX.clear()
                strokeY.clear()
            }
            else -> {}
        }
    }

    // Doble exposicion: 2a imagen (sesion volatil; no se persiste en SessionStore para no tocarlo)
    var overlayImage by remember { mutableStateOf<BaseImage?>(null) }

    val cropWorking = if (activeTool == EditorTool.CROP) (workingGeometry ?: geometry) else null

    fun effective(): app.revela.core.ColorMath.Adjustments {
        val toolId = when (val t = activeTool) {
            EditorTool.TUNE -> tuneParam.toolId
            EditorTool.TONE -> Tool.TONAL
            null -> ""
            else -> t.id
        }
        return EffectiveAdjustments.withWorking(
            EffectiveAdjustments.from(stack),
            toolId,
            workingAdj
        )
    }

    var thumbJob by remember { mutableStateOf<Job?>(null) }

    /** Miniatura de la sesion actual para la biblioteca HOME (PNG en filesDir/thumbs). */
    suspend fun flushThumb() {
        val u = sourceUri ?: return
        val b = base ?: return
        runCatching {
            val bmp = withContext(Dispatchers.Default) {
                app.revela.render.ThumbRender.forEdits(b, stack, stack.edits, geometry.quarterTurns, 256)
            }
            val stream = ByteArrayOutputStream()
            bmp.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
            SessionIndexStore.saveThumb(
                context.filesDir,
                SessionIndexStore.idFor(u.toString()),
                stream.toByteArray()
            )
        }
    }

    fun persistSession() {
        val u = sourceUri ?: return
        // Un fallo guardando la sesion no debe impedir el indice (ni la nota)
        runCatching { SessionStore.save(context, u, stack, geometry) }
        SessionIndexStore.upsert(context.filesDir, u.toString(), stack, geometry)
        thumbJob?.cancel()
        thumbJob = scope.launch {
            delay(400)
            flushThumb()
        }
    }

    /** Publica el estado de trabajo de Pose como edit en vivo. */
    fun pushHeadPose() {
        workingEdit = Edit.HeadPose(hpLeft, hpRight, hpMouth, hpYawDeg, hpPitchDeg)
    }

    /** Ajuste de trabajo para un parametro de Ajustar (WB conserva el campo no tocado). */
    fun workingFor(tp: TuneParam, v: Float): app.revela.core.ColorMath.Adjustments {
        val cur = EffectiveAdjustments.from(stack)
        return when (tp) {
            TuneParam.EXPOSURE -> cur.copy(exposureStops = v)
            TuneParam.CONTRAST -> cur.copy(contrast = v)
            TuneParam.SATURATION -> cur.copy(saturation = v)
            TuneParam.TEMPERATURE -> cur.copy(temperature = v)
            TuneParam.TINT -> cur.copy(tint = v)
        }
    }

    fun commitParamValue(tp: TuneParam, value: Float) {
        val edit = EffectiveAdjustments.buildEdit(tp.toolId, effective(), tp.wbParam, value) ?: return
        stack.apply(edit)
        stackVersion++
        workingAdj = null
        dragValue = Float.NaN
        persistSession()
    }

    /** AUTO de un toque: auto-niveles sobre la base; sustituye los ajustes tonales. */
    fun runAuto() {
        val b = base ?: return
        scope.launch {
            val r = withContext(Dispatchers.Default) { AutoLevels.compute(b) }
            autoSnapshot = stack.edits
            stack.removeTool(Tool.EXPOSURE)
            stack.removeTool(Tool.CONTRAST)
            stack.removeTool(Tool.SATURATION)
            stack.removeTool(Tool.WHITE_BALANCE)
            if (r.exposureStops != 0f) stack.apply(Edit.Exposure(r.exposureStops))
            if (r.contrast != 0f) stack.apply(Edit.Contrast(r.contrast))
            if (r.saturation != 0f) stack.apply(Edit.Saturation(r.saturation))
            stackVersion++
            persistSession()
            showAutoSnack = true
        }
    }

    /** Deshace el AUTO completo (vuelve al instante previo a la varita). */
    fun undoAuto() {
        autoSnapshot?.let { stack.reset(it) }
        autoSnapshot = null
        stackVersion++
        persistSession()
        showAutoSnack = false
    }

    /** Lista de edits efectiva (incluye el estado de trabajo del selectivo y la vineta). */
    fun effectiveEdits(): List<Edit> {
        var list = stack.edits
        if (activeTool == EditorTool.SELECTIVE && workingPoint != null) {
            val pts = stack.selectivePoints().toMutableList()
            if (activePoint in pts.indices) {
                pts[activePoint] = workingPoint!!
                list = list.map { if (it is Edit.Selective) it.copy(points = pts) else it }
            }
        }
        if (activeTool == EditorTool.VIGNETTE && !workingVignette.isNaN()) {
            list = list.filterNot { it is Edit.Vignette } + Edit.Vignette(workingVignette)
        }
        if (workingEdit != null) {
            val we = workingEdit!!
            list = list.filterNot { it.toolId == we.toolId } + we
        }
        val pk = activeTool?.presetKind()
        if (pk != null && !workingPresetIntensity.isNaN()) {
            val toolIds = pk.toolIds()
            list = list.filterNot { it.toolId in toolIds } + pk.edits(workingPresetIntensity)
        }
        // Capas ocultas: se conservan en la pila (y en el JSON) pero no se pintan
        return list.filterNot { it.toolId in hiddenLayers }
    }

    /** Etiqueta legible de una capa (nombre de la herramienta que la genero). */
    fun layerLabel(edit: Edit): String {
        val res = toolLabelRes(edit.toolId)
        return if (res != 0) context.getString(res) else edit.toolId
    }

    /** Herramienta que reabre una capa ("brush:temperature" -> BRUSH). */
    fun openToolForLayer(toolId: String): EditorTool? {
        val base = toolId.substringBefore(':')
        EditorTool.entries.firstOrNull { it.id == base }?.let { return it }
        // Las capas de valor suelto viven dentro de Ajustar imagen
        return when (base) {
            Tool.EXPOSURE, Tool.CONTRAST, Tool.SATURATION -> EditorTool.TUNE
            else -> null
        }
    }

    /** Id de sesion de la foto abierta (clave de las capas ocultas). */
    fun currentSessionId(): String? = sourceUri?.let { SessionIndexStore.idFor(it.toString()) }

    fun persistHidden() {
        val id = currentSessionId() ?: return
        HiddenLayers.save(context.filesDir, id, hiddenLayers)
    }

    /** Filtro B/N comprometido en la pila (o null si no hay). */
    fun currentBnwFilter(): String? = stack.edits.filterIsInstance<Edit.Bnw>().lastOrNull()?.filter

    /** Gira el marco en cuartos de vuelta (90 grados por pulsacion). */
    fun rotateBy(quarters: Int) {
        geometry = geometry.copy(quarterTurns = geometry.quarterTurns + quarters)
        viewScale = 1f
        viewOffset = Offset.Zero
        stackVersion++
        persistSession()
    }

    /** Compromete los tres valores del contraste tonal como un unico edit. */
    fun commitTonal() {
        val cur = effective()
        stack.apply(Edit.TonalContrast(cur.tonalShadows, cur.tonalMidtones, cur.tonalHighlights))
        stackVersion++
        workingAdj = null
        persistSession()
    }

    /** Compromete el edit en curso de las herramientas espaciales/de composicion. */
    fun commitWorkingEdit(edit: Edit) {
        stack.apply(edit)
        stackVersion++
        workingEdit = null
        persistSession()
    }

    /** Restablece una herramienta espacial (quita su edit de la pila). */
    fun resetTool(toolId: String) {
        stack.removeTool(toolId)
        stackVersion++
        workingEdit = null
        persistSession()
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        OpenImageDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // GetContent no concede permiso persistente: el de escritura se pide solo al borrar
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            scope.launch {
                val t0 = android.os.SystemClock.elapsedRealtime()
                val loaded = PreviewRender.decode(context.contentResolver, uri)
                android.util.Log.i(
                    "revela",
                    "perf decode=${android.os.SystemClock.elapsedRealtime() - t0}ms ok=${loaded != null} ${loaded?.width}x${loaded?.height}"
                )
                // URI no abrible: se conserva el estado actual (sin reset ni crash)
                if (loaded == null) return@launch
                base = loaded
                originalBitmap = loaded?.toBitmap()
                sourceUri = uri
                activeTool = null
                workingAdj = null
                workingGeometry = null
                workingPoint = null
                activePoint = -1
                strokeX.clear()
                strokeY.clear()
                workingEdit = null
                workingPresetIntensity = Float.NaN
                overlayImage = null
                val restored = loaded?.let { SessionStore.load(context, uri) }
                stack = restored?.first ?: EditStack()
                geometry = restored?.second ?: GeometryModel()
                stackVersion++
            }
        }
    }

    // Selector de la 2a imagen para doble exposicion
    val overlayPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val loaded = PreviewRender.decode(context.contentResolver, uri)
                if (loaded != null) overlayImage = loaded
            }
        }
    }

    // Carga inicial al venir de la HOME (foto nueva o sesion guardada)
    var initialLoadDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (initialLoadDone) return@LaunchedEffect
        initialLoadDone = true
        val target = initialUri ?: return@LaunchedEffect
        val loaded = PreviewRender.decode(context.contentResolver, target)
        if (loaded == null) {
            android.util.Log.w("revela", "no se pudo abrir $target")
            Toast.makeText(context, context.getString(R.string.cannot_open_photo), Toast.LENGTH_SHORT).show()
            onHome()
            return@LaunchedEffect
        }
        base = loaded
        originalBitmap = loaded.toBitmap()
        sourceUri = target
        hiddenLayers = HiddenLayers.load(context.filesDir, SessionIndexStore.idFor(target.toString()))
        val restored = initialSessionId?.let { SessionIndexStore.load(context.filesDir, it) }
            ?: SessionStore.load(context, target)
        stack = restored?.first ?: EditStack()
        geometry = restored?.second ?: GeometryModel()
        stackVersion++
    }

    LaunchedEffect(base, stackVersion, workingAdj, workingPoint, workingVignette, activeTool, showOriginal, tuneParam, workingEdit, workingPresetIntensity, overlayImage) {
        val b = base ?: return@LaunchedEffect
        renderJob?.cancel()
        val snapshotEdits = effectiveEdits()
        val quarter = geometry.quarterTurns
        renderJob = scope.launch(Dispatchers.Default) {
            val bmp = PreviewRender.render(b, stack, quarter, showOriginal, snapshotEdits, effective(), overlayImage)
            preview = bmp
        }
    }

    // Miniaturas del carrusel de estilos (se regeneran al cambiar foto o pila)
    LaunchedEffect(base, stackVersion) {
        val b = base ?: return@LaunchedEffect
        val edits = effectiveEdits()
        val q = geometry.quarterTurns
        launch(Dispatchers.Default) {
            lookThumbs = app.revela.render.ThumbRender.looks(b, stack, edits, q)
        }
    }

    // Histograma del preview actual (48 tramos de luminancia)
    LaunchedEffect(preview) {
        val bmp = preview ?: return@LaunchedEffect
        launch(Dispatchers.Default) {
            val w = bmp.width
            val h = bmp.height
            val total = w * h
            val px = IntArray(total)
            bmp.getPixels(px, 0, w, 0, 0, w, h)
            val stride = maxOf(1, total / 60000)
            val bins = IntArray(48)
            var i = 0
            while (i < total) {
                val p = px[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val l = (r * 299 + g * 587 + b * 114) / 1000
                bins[l * 48 / 256]++
                i += stride
            }
            histBins = bins
        }
    }

    // Autoapagado del snackbar de AUTO
    LaunchedEffect(showAutoSnack) {
        if (showAutoSnack) {
            delay(4000)
            showAutoSnack = false
        }
    }

    // Miniaturas de los estilos personalizados
    LaunchedEffect(base, stackVersion, customStyles) {
        val b = base ?: return@LaunchedEffect
        launch(Dispatchers.Default) {
            val map = mutableMapOf<String, ImageBitmap>()
            for (cs in customStyles) {
                val st = runCatching { EditStack.fromJson(cs.editsJson) }.getOrNull() ?: continue
                map[cs.id] = app.revela.render.ThumbRender.forEdits(
                    b, st, st.edits, geometry.quarterTurns
                )
            }
            customThumbs = map
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ui.bg)
                .statusBarsPadding()
        ) {
            // Barra superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                thumbJob?.cancel()
                scope.launch {
                    persistSession()
                    thumbJob?.cancel()
                    flushThumb()
                    onHome()
                }
            }) { Icon(Icons.Outlined.Home, stringResource(R.string.back_to_library), tint = Ui.icon) }
            TextButton(onClick = {
                pickerLauncher.launch("image/*")
            }) {
                Text(
                    stringResource(R.string.open),
                    color = Ui.textDim,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.weight(1f))

            if (activeTool == null) {
                IconButton(
                    onClick = {
                        if (stack.undo()) {
                            stackVersion++
                            persistSession()
                        }
                    },
                    enabled = stack.canUndo
                ) { Icon(Icons.AutoMirrored.Outlined.Undo, stringResource(R.string.undo), tint = Ui.icon) }
                IconButton(
                    onClick = {
                        if (stack.redo()) {
                            stackVersion++
                            persistSession()
                        }
                    },
                    enabled = stack.canRedo
                ) { Icon(Icons.AutoMirrored.Outlined.Redo, stringResource(R.string.redo), tint = Ui.icon) }
                IconButton(
                    onClick = {},
                    modifier = Modifier.pointerInput(sourceUri) {
                        detectTapGestures(
                            onPress = {
                                showOriginal = true
                                tryAwaitRelease()
                                showOriginal = false
                            }
                        )
                    }
                ) { Icon(Icons.Outlined.Visibility, stringResource(R.string.compare_original), tint = Ui.icon) }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        enabled = sourceUri != null
                    ) { Icon(Icons.Outlined.MoreVert, stringResource(R.string.menu), tint = Ui.icon) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.image_details)) },
                            onClick = {
                                showMenu = false
                                showImageDetails = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.note_menu)) },
                            onClick = {
                                showMenu = false
                                val u = sourceUri
                                noteText = if (u != null) {
                                    val id = SessionIndexStore.idFor(u.toString())
                                    SessionIndexStore.list(context.filesDir)
                                        .firstOrNull { it.id == id }?.note ?: ""
                                } else ""
                                showNote = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.layers_menu)) },
                            onClick = {
                                showMenu = false
                                showLayers = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.revert_all)) },
                            onClick = {
                                showMenu = false
                                showRevertConfirm = true
                            },
                            enabled = stack.edits.isNotEmpty() || geometry != GeometryModel()
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                showMenu = false
                                showSettings = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about)) },
                            onClick = {
                                showMenu = false
                                showAbout = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.delete_original),
                                    color = Color(0xFFD32F2F)
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteOriginal = true
                            }
                        )
                    }
                }
            } else {
                // Consejo de la herramienta activa (patron Snapseed: la ayuda vive en la barra)
                // Idioma efectivo (no el ajuste "system"): el consejo debe seguir al que se ve en pantalla
                val tipLang = context.resources.configuration.locales[0].language
                val activeTip = activeTool?.let { Tutorials.tipFor(it.id, tipLang) }
                if (activeTip != null) {
                    IconButton(onClick = { showTip = activeTip }) {
                        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.tool_tip), tint = Ui.icon)
                    }
                }
                IconButton(onClick = {
                    activeTool = null
                    workingAdj = null
                    workingGeometry = null
                    workingPoint = null
                    activePoint = -1
                    dragValue = Float.NaN
                    workingVignette = Float.NaN
                    strokeX.clear()
                    strokeY.clear()
                    workingEdit = null
                    workingPresetIntensity = Float.NaN
                }) { Icon(Icons.Outlined.Close, stringResource(R.string.close_tool), tint = Ui.icon) }
                IconButton(onClick = {
                    when (activeTool) {
                        EditorTool.CROP -> {
                            workingGeometry?.let {
                                geometry = it
                                persistSession()
                            }
                        }
                        EditorTool.SELECTIVE -> {
                            val wp = workingPoint
                            if (wp != null && activePoint >= 0) {
                                stack.updateSelectivePoint(activePoint, wp)
                                stackVersion++
                                persistSession()
                            }
                        }
                        EditorTool.PERSPECTIVE -> {
                            (workingEdit as? Edit.Perspective)?.let {
                                stack.apply(it)
                                stackVersion++
                                persistSession()
                            }
                        }
                        EditorTool.EXPAND -> {
                            (workingEdit as? Edit.Expand)?.let {
                                stack.apply(it)
                                stackVersion++
                                persistSession()
                            }
                        }
                        EditorTool.HEAD_POSE -> {
                            (workingEdit as? Edit.HeadPose)?.let {
                                stack.apply(it)
                                stackVersion++
                                persistSession()
                            }
                        }
                        EditorTool.BRUSH -> {
                            strokeX.clear()
                            strokeY.clear()
                        }
                        else -> {}
                    }
                    workingAdj = null
                    workingGeometry = null
                    workingPoint = null
                    activePoint = -1
                    activeTool = null
                    workingEdit = null
                    workingPresetIntensity = Float.NaN
                }) { Icon(Icons.Outlined.Check, stringResource(R.string.done), tint = Ui.icon) }
            }
        }
        // Con un panel abierto (Herramientas/Estilos) la foto sube como franja superior
        // siempre visible; con herramienta activa o nada abierto, ocupa todo el espacio.
        // La transicion se ANIMA con animateContentSize: interpola el tamano de la foto
        // entre weight(1f) y height(200dp) SIN medir nada (la medida con onSizeChanged
        // entro en bucle de realimentacion y encogia la foto).
        val panelOpen = activeTool == null && bottomTab != BottomTab.NONE && base != null
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(dampingRatio = 0.85f, stiffness = 320f))
                .then(
                    if (panelOpen) Modifier.height(200.dp)
                    else Modifier.weight(1f)
                )
                .padding(horizontal = 16.dp, vertical = if (panelOpen) 8.dp else 14.dp),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val viewportWpx = constraints.maxWidth.toFloat()
            val viewportHpx = constraints.maxHeight.toFloat()

            val b = base
            val bmp = if (showOriginal) originalBitmap else preview
            if (b == null || bmp == null) {
                Button(onClick = {
                    pickerLauncher.launch("image/*")
                }) { Text(stringResource(R.string.open_photo)) }
            } else {
                val inCrop = activeTool == EditorTool.CROP
                val g = cropWorking ?: geometry

                val baseAspect = b.width.toFloat() / b.height.toFloat()
                val oddTurns = ((g.quarterTurns % 4) + 4) % 4 % 2 == 1
                val frameAspect = if (oddTurns) 1f / baseAspect else baseAspect

                val viewportAspect =
                    if (inCrop) frameAspect else frameAspect * g.cropWidthNorm / g.cropHeightNorm

                var w = viewportWpx
                var h = w / viewportAspect
                if (h > viewportHpx) {
                    h = viewportHpx
                    w = h * viewportAspect
                }

                val frameW = w / g.cropWidthNorm
                val frameH = h / g.cropHeightNorm
                val cover = coverScale(g.totalRotationDeg, frameAspect)
                val offsetXdp = with(density) { (-g.cropLeft * frameW).toDp() }
                val offsetYdp = with(density) { (-g.cropTop * frameH).toDp() }
                val frameWdp = with(density) { frameW.toDp() }
                val frameHdp = with(density) { frameH.toDp() }
                val boxWdp = with(density) { w.toDp() }
                val boxHdp = with(density) { h.toDp() }
                val oddTurnsImg = ((g.quarterTurns % 4) + 4) % 4 % 2 == 1
                val imgWdp = if (oddTurnsImg) frameHdp else frameWdp
                val imgHdp = if (oddTurnsImg) frameWdp else frameHdp

                val toolNow = activeTool
                Box(
                    modifier = Modifier
                        .requiredSize(boxWdp, boxHdp)
                        .clipToBounds()
                        .graphicsLayer {
                            scaleX = viewScale
                            scaleY = viewScale
                            translationX = viewOffset.x
                            translationY = viewOffset.y
                        }
                        .pointerInput("debugtap") {
                            detectTapGestures { pos ->
                                android.util.Log.d("revela", "TAP VIEWPORT en $pos tool=$toolNow")
                            }
                        }
                        .let { mod ->
                            if (toolNow == null) {
                                mod.pointerInput(b, g, frameW, frameH, w, h) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (viewScale * zoom).coerceIn(1f, 6f)
                                        viewScale = newScale
                                        // Limite de pan: la foto no puede salirse del recuadro
                                        // (borde de la imagen escalada siempre dentro del viewport).
                                        val offXpx = -g.cropLeft * frameW
                                        val offYpx = -g.cropTop * frameH
                                        fun clampAxis(img: Float, box: Float, offPx: Float, cur: Float, delta: Float): Float {
                                            val imgLeft = (box - img) / 2f + offPx
                                            val c = box / 2f
                                            val minT = -c - (imgLeft - c) * newScale
                                            val maxT = box - c - (imgLeft + img - c) * newScale
                                            return if (minT >= maxT) 0f else (cur + delta).coerceIn(minT, maxT)
                                        }
                                        viewOffset = Offset(
                                            x = clampAxis(frameW, w, offXpx, viewOffset.x, pan.x),
                                            y = clampAxis(frameH, h, offYpx, viewOffset.y, pan.y)
                                        )
                                    }
                                }
                            } else {
                                mod
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .requiredSize(frameWdp, frameHdp)
                            .absoluteOffset(offsetXdp, offsetYdp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .requiredSize(imgWdp, imgHdp)
                                .graphicsLayer {
                                    rotationZ = g.totalRotationDeg
                                    scaleX = cover
                                    scaleY = cover
                                }
                        )
                    }

                    // Gestos por herramienta
                    when (toolNow) {
                        EditorTool.SELECTIVE -> {
                            val ptsNow = stack.selectivePoints()
                            val ptsNowRef = rememberUpdatedState(ptsNow)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(g, ptsNow.size, activePoint) {
                                        detectTapGestures(
                                            onTap = { pos ->
                                                val fx = g.cropLeft + pos.x / viewportWpx * g.cropWidthNorm
                                                val fy = g.cropTop + pos.y / viewportHpx * g.cropHeightNorm
                                                val pts = ptsNowRef.value
                                                val near = pts.indexOfFirst {
                                                    abs(it.x - fx) < 0.04f && abs(it.y - fy) < 0.06f
                                                }
                                                if (near >= 0) {
                                                    activePoint = near
                                                } else if (pts.size < 8) {
                                                    stack.applySelectivePoint(ControlPoint(fx, fy))
                                                    activePoint = stack.selectivePoints().lastIndex
                                                    stackVersion++
                                                    persistSession()
                                                }
                                            },
                                            onLongPress = { pos ->
                                                val fx = g.cropLeft + pos.x / viewportWpx * g.cropWidthNorm
                                                val fy = g.cropTop + pos.y / viewportHpx * g.cropHeightNorm
                                                val pts = ptsNowRef.value
                                                val near = pts.indexOfFirst {
                                                    abs(it.x - fx) < 0.05f && abs(it.y - fy) < 0.07f
                                                }
                                                if (near >= 0) {
                                                    stack.removeSelectivePoint(near)
                                                    activePoint = -1
                                                    workingPoint = null
                                                    stackVersion++
                                                    persistSession()
                                                }
                                            }
                                        )
                                    }
                                    .pointerInput(selParam, activePoint) {
                                        val span = 2f
                                        var start = 0f
                                        var moveMode = false
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                val fx = g.cropLeft + pos.x / viewportWpx * g.cropWidthNorm
                                                val fy = g.cropTop + pos.y / viewportHpx * g.cropHeightNorm
                                                val pts = ptsNowRef.value
                                                val near = pts.indexOfFirst {
                                                    abs(it.x - fx) < 0.05f && abs(it.y - fy) < 0.08f
                                                }
                                                moveMode = near >= 0
                                                if (moveMode) {
                                                    activePoint = near
                                                } else if (activePoint in pts.indices) {
                                                    val p = pts[activePoint]
                                                    start = when (selParam) {
                                                        SelParam.BRIGHTNESS -> p.brightness
                                                        SelParam.CONTRAST -> p.contrast
                                                        SelParam.SATURATION -> p.saturation
                                                    }
                                                    workingPoint = p
                                                }
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                if (moveMode) {
                                                    val p = workingPoint ?: return@detectDragGestures
                                                    workingPoint = p.copy(
                                                        x = (p.x + amount.x / viewportWpx * g.cropWidthNorm)
                                                            .coerceIn(0f, 1f),
                                                        y = (p.y + amount.y / viewportHpx * g.cropHeightNorm)
                                                            .coerceIn(0f, 1f)
                                                    )
                                                } else if (activePoint in stack.selectivePoints().indices) {
                                                    if (dragValue.isNaN()) dragValue = start
                                                    dragValue = (dragValue + amount.x / viewportWpx * span)
                                                        .coerceIn(-1f, 1f)
                                                    val p = stack.selectivePoints()[activePoint]
                                                    workingPoint = when (selParam) {
                                                        SelParam.BRIGHTNESS -> p.copy(brightness = dragValue)
                                                        SelParam.CONTRAST -> p.copy(contrast = dragValue)
                                                        SelParam.SATURATION -> p.copy(saturation = dragValue)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                val wp = workingPoint
                                                if (wp != null && activePoint >= 0) {
                                                    stack.updateSelectivePoint(activePoint, wp)
                                                    stackVersion++
                                                    persistSession()
                                                }
                                                workingPoint = null
                                                dragValue = Float.NaN
                                            },
                                            onDragCancel = {
                                                workingPoint = null
                                                dragValue = Float.NaN
                                            }
                                        )
                                    }
                            )

                            Canvas(modifier = Modifier.matchParentSize()) {
                                fun drawPt(cx: Float, cy: Float, active: Boolean) {
                                    drawCircle(
                                        if (active) Color(0xFF7AA2FF) else Color.White,
                                        radius = if (active) 16f else 10f,
                                        center = Offset(cx, cy)
                                    )
                                    drawCircle(
                                        Color.Black.copy(alpha = 0.6f),
                                        radius = if (active) 7f else 4f,
                                        center = Offset(cx, cy)
                                    )
                                }
                                ptsNow.forEachIndexed { idx, p ->
                                    val cx = ((p.x - g.cropLeft) / g.cropWidthNorm) * size.width
                                    val cy = ((p.y - g.cropTop) / g.cropHeightNorm) * size.height
                                    drawPt(cx, cy, idx == activePoint)
                                }
                                val wpNow = workingPoint
                                if (wpNow != null) {
                                    val cx = ((wpNow.x - g.cropLeft) / g.cropWidthNorm) * size.width
                                    val cy = ((wpNow.y - g.cropTop) / g.cropHeightNorm) * size.height
                                    drawPt(cx, cy, true)
                                }
                            }
                        }

                        EditorTool.HEAL -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                strokeX.add(pos.x / viewportWpx)
                                                strokeY.add(pos.y / viewportHpx)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                strokeX.add(change.position.x / viewportWpx)
                                                strokeY.add(change.position.y / viewportHpx)
                                            }
                                        )
                                    }
                            )
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val r = brushSize * size.minDimension
                                for (i in strokeX.indices) {
                                    drawCircle(
                                        Color.White.copy(alpha = 0.45f),
                                        radius = r,
                                        center = Offset(strokeX[i] * size.width, strokeY[i] * size.height)
                                    )
                                }
                            }
                        }

                        EditorTool.PERSPECTIVE -> {
                            val cur = (workingEdit as? Edit.Perspective)?.corners ?: perspCorners
                            var dragging by remember { mutableIntStateOf(-1) }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(perspMode) {
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                val u = pos.x / size.width
                                                val v = pos.y / size.height
                                                dragging = perspNearestHandle(perspMode, cur, u, v)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                if (dragging >= 0) {
                                                    val u = change.position.x / size.width
                                                    val v = change.position.y / size.height
                                                    perspCorners = perspMoveHandle(perspMode, cur, dragging, u, v)
                                                    workingEdit = Edit.Perspective(perspCorners, perspMode)
                                                }
                                            },
                                            onDragEnd = { dragging = -1 }
                                        )
                                    }
                            )
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val g = geometry
                                fun pt(nx: Float, ny: Float) = Offset(
                                    ((nx - g.cropLeft) / g.cropWidthNorm) * size.width,
                                    ((ny - g.cropTop) / g.cropHeightNorm) * size.height
                                )
                                val pTL = pt(cur.tlX, cur.tlY); val pTR = pt(cur.trX, cur.trY)
                                val pBR = pt(cur.brX, cur.brY); val pBL = pt(cur.blX, cur.blY)
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(pTL.x, pTL.y); lineTo(pTR.x, pTR.y)
                                    lineTo(pBR.x, pBR.y); lineTo(pBL.x, pBL.y); close()
                                }
                                drawPath(
                                    path, Color.White.copy(alpha = 0.7f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                                perspHandles(perspMode, cur).forEach { (hx, hy) ->
                                    val c = pt(hx, hy)
                                    drawCircle(Color(0xFF2196F3), radius = 11.dp.toPx(), center = c)
                                    drawCircle(
                                        Color.White, radius = 11.dp.toPx(), center = c,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }

                        EditorTool.EXPAND -> {
                            val ec = expandEdges.clamped()
                            val cw = 1f / (1f + ec.left + ec.right)
                            val ch = 1f / (1f + ec.top + ec.bottom)
                            val ox = ec.left * cw
                            val oy = ec.top * ch
                            var dragSide by remember { mutableIntStateOf(-1) }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                val u = pos.x / size.width
                                                val v = pos.y / size.height
                                                dragSide = when {
                                                    abs(u - ox) < 0.06f -> 3
                                                    abs(u - (ox + cw)) < 0.06f -> 1
                                                    abs(v - oy) < 0.06f -> 0
                                                    abs(v - (oy + ch)) < 0.06f -> 2
                                                    else -> -1
                                                }
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                val du = amount.x / size.width
                                                val dv = amount.y / size.height
                                                expandEdges = when (dragSide) {
                                                    0 -> expandEdges.copy(top = (expandEdges.top - dv).coerceIn(0f, 0.45f))
                                                    1 -> expandEdges.copy(right = (expandEdges.right + du).coerceIn(0f, 0.45f))
                                                    2 -> expandEdges.copy(bottom = (expandEdges.bottom + dv).coerceIn(0f, 0.45f))
                                                    3 -> expandEdges.copy(left = (expandEdges.left - du).coerceIn(0f, 0.45f))
                                                    else -> expandEdges
                                                }
                                                workingEdit = Edit.Expand(expandEdges, expandMirror)
                                            },
                                            onDragEnd = { dragSide = -1 }
                                        )
                                    }
                            )
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val g = geometry
                                val l = ((ox - g.cropLeft) / g.cropWidthNorm) * size.width
                                val t = ((oy - g.cropTop) / g.cropHeightNorm) * size.height
                                val r = (((ox + cw) - g.cropLeft) / g.cropWidthNorm) * size.width
                                val b = (((oy + ch) - g.cropTop) / g.cropHeightNorm) * size.height
                                drawRect(
                                    Color.White.copy(alpha = 0.7f),
                                    topLeft = Offset(l, t),
                                    size = androidx.compose.ui.geometry.Size(r - l, b - t),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                                arrayOf(
                                    Offset((l + r) / 2f, t), Offset(r, (t + b) / 2f),
                                    Offset((l + r) / 2f, b), Offset(l, (t + b) / 2f)
                                ).forEach { c ->
                                    drawCircle(Color(0xFF2196F3), radius = 10.dp.toPx(), center = c)
                                }
                            }
                        }

                        EditorTool.BRUSH -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                strokeX.add(pos.x / viewportWpx)
                                                strokeY.add(pos.y / viewportHpx)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                strokeX.add(change.position.x / viewportWpx)
                                                strokeY.add(change.position.y / viewportHpx)
                                            }
                                        )
                                    }
                            )
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val r = brushSize * size.minDimension
                                for (i in strokeX.indices) {
                                    drawCircle(
                                        Color(0xFF2196F3).copy(alpha = 0.45f),
                                        radius = r,
                                        center = Offset(strokeX[i] * size.width, strokeY[i] * size.height)
                                    )
                                }
                            }
                        }

                        EditorTool.HEAD_POSE -> {
                            var dragIdx by remember { mutableIntStateOf(-1) }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { pos ->
                                            val u = pos.x / size.width
                                            val v = pos.y / size.height
                                            when {
                                                hpLeft == null || hpStep == 0 -> { hpLeft = ControlPoint(u, v); hpStep = 1 }
                                                hpRight == null || hpStep == 1 -> { hpRight = ControlPoint(u, v); hpStep = 2 }
                                                hpMouth == null || hpStep == 2 -> { hpMouth = ControlPoint(u, v); hpStep = 3 }
                                                else -> {
                                                    val pts = listOfNotNull(hpLeft, hpRight, hpMouth)
                                                    dragIdx = pts.indexOfFirst { abs(it.x - u) < 0.08f && abs(it.y - v) < 0.08f }
                                                }
                                            }
                                            pushHeadPose()
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { pos ->
                                                val u = pos.x / size.width
                                                val v = pos.y / size.height
                                                val pts = listOfNotNull(hpLeft, hpRight, hpMouth)
                                                dragIdx = pts.indexOfFirst { abs(it.x - u) < 0.08f && abs(it.y - v) < 0.08f }
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                if (dragIdx >= 0) {
                                                    val u = (change.position.x / size.width).coerceIn(0f, 1f)
                                                    val v = (change.position.y / size.height).coerceIn(0f, 1f)
                                                    when (dragIdx) {
                                                        0 -> hpLeft = ControlPoint(u, v)
                                                        1 -> hpRight = ControlPoint(u, v)
                                                        2 -> hpMouth = ControlPoint(u, v)
                                                    }
                                                    pushHeadPose()
                                                }
                                            },
                                            onDragEnd = { dragIdx = -1 }
                                        )
                                    }
                            )
                            Canvas(modifier = Modifier.matchParentSize()) {
                                listOfNotNull(hpLeft, hpRight, hpMouth).forEach { p ->
                                    val c = Offset(p.x * size.width, p.y * size.height)
                                    drawCircle(Color(0xFF2196F3), radius = 12.dp.toPx(), center = c)
                                    drawCircle(
                                        Color.White, radius = 12.dp.toPx(), center = c,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }

                        EditorTool.DOUBLE_EXPO -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, amount ->
                                            change.consume()
                                            val cur = (workingEdit as? Edit.DoubleExpo)
                                                ?: stack.edits.filterIsInstance<Edit.DoubleExpo>().firstOrNull()
                                                ?: Edit.DoubleExpo()
                                            workingEdit = cur.copy(
                                                offsetX = (cur.offsetX + amount.x / size.width).coerceIn(-0.5f, 0.5f),
                                                offsetY = (cur.offsetY + amount.y / size.height).coerceIn(-0.5f, 0.5f)
                                            )
                                        }
                                    }
                            )
                        }

                        EditorTool.CROP -> {
                            if (inCrop && cropWorking != null) {
                                CropOverlay(
                                    modifier = Modifier.matchParentSize(),
                                    geometry = cropWorking,
                                    frameAspect = frameAspect,
                                    drawMode = cropDrawMode,
                                    onGeometry = { workingGeometry = it }
                                )
                            }
                        }

                        // Ajustar (estilo Snapseed): arrastre vertical = elegir parametro,
                        // arrastre horizontal = ajustar el valor del parametro activo.
                        EditorTool.TUNE -> {
                            val haptics = LocalHapticFeedback.current
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        var totalX = 0f
                                        var totalY = 0f
                                        var mode = 0 // 0 sin decidir, 1 vertical (param), 2 horizontal (valor)
                                        var startValue = 0f
                                        var paramAtStart = TuneParam.EXPOSURE
                                        detectDragGestures(
                                            onDragStart = {
                                                totalX = 0f
                                                totalY = 0f
                                                mode = 0
                                                paramAtStart = tuneParam
                                                startValue = EffectiveAdjustments.valueOf(
                                                    effective(), tuneParam.toolId, tuneParam.wbParam
                                                )
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                totalX += amount.x
                                                totalY += amount.y
                                                if (mode == 0 && (abs(totalX) > 28f || abs(totalY) > 28f)) {
                                                    mode = if (abs(totalY) > abs(totalX)) 1 else 2
                                                    if (mode == 1) paramMenuVisible = true
                                                }
                                                when (mode) {
                                                    1 -> {
                                                        val step = (totalY / 110f).toInt()
                                                        val entries = TuneParam.entries
                                                        val idx = (paramAtStart.ordinal + step)
                                                            .coerceIn(0, entries.size - 1)
                                                        if (entries[idx] != tuneParam) {
                                                            tuneParam = entries[idx]
                                                            dragValue = Float.NaN
                                                            haptics.performHapticFeedback(
                                                                HapticFeedbackType.TextHandleMove
                                                            )
                                                        }
                                                    }
                                                    2 -> {
                                                        val tp = tuneParam
                                                        val range = EffectiveAdjustments.rangeFor(tp.toolId)
                                                        val span = range.endInclusive - range.start
                                                        if (dragValue.isNaN()) dragValue = startValue
                                                        dragValue = (dragValue + amount.x / (viewportWpx * 1.8f) * span)
                                                            .coerceIn(range.start, range.endInclusive)
                                                        workingAdj = workingFor(tp, dragValue)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                paramMenuVisible = false
                                                if (mode == 2 && !dragValue.isNaN()) {
                                                    commitParamValue(tuneParam, dragValue)
                                                }
                                                mode = 0
                                            },
                                            onDragCancel = {
                                                paramMenuVisible = false
                                                workingAdj = null
                                                dragValue = Float.NaN
                                                mode = 0
                                            }
                                        )
                                    }
                            )
                            if (paramMenuVisible) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .background(Color(0xCC101014), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    TuneParam.entries.forEach { p ->
                                        Text(
                                            stringResource(p.labelRes),
                                            color = if (p == tuneParam) Color.White else Color.Gray,
                                            fontSize = if (p == tuneParam) 16.sp else 13.sp,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }

                // El panel ya NO solapa la foto: la foto sube como franja superior y la
                // rejilla ocupa el espacio inferior real (patron Snapseed).
            // Indicador fino flotando sobre la foto (nunca la encoje)
            Box(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            // Indicador de valor fino estilo Snapseed (etiqueta + valor + linea con punto)
            val toolForValue = activeTool
            if (toolForValue == EditorTool.TUNE || toolForValue == EditorTool.SELECTIVE ||
                toolForValue == EditorTool.VIGNETTE
            ) {
                val range: ClosedFloatingPointRange<Float>
                val label: String
                val shown: Float
                when (toolForValue) {
                    EditorTool.TUNE -> {
                        label = stringResource(tuneParam.labelRes)
                        range = EffectiveAdjustments.rangeFor(tuneParam.toolId)
                        shown = if (dragValue.isNaN()) {
                            EffectiveAdjustments.valueOf(effective(), tuneParam.toolId, tuneParam.wbParam)
                        } else dragValue
                    }
                    EditorTool.SELECTIVE -> {
                        label = stringResource(selParam.labelRes)
                        range = -1f..1f
                        val pts = stack.selectivePoints()
                        val wp = workingPoint
                        val p = if (wp != null && activePoint in pts.indices) wp
                        else pts.getOrNull(activePoint)
                        shown = if (dragValue.isNaN()) {
                            when (selParam) {
                                SelParam.BRIGHTNESS -> p?.brightness ?: 0f
                                SelParam.CONTRAST -> p?.contrast ?: 0f
                                SelParam.SATURATION -> p?.saturation ?: 0f
                            }
                        } else dragValue
                    }
                    else -> {
                        label = stringResource(R.string.tool_vignette)
                        range = 0f..1f
                        shown = if (workingVignette.isNaN()) {
                            stack.edits.filterIsInstance<Edit.Vignette>().firstOrNull()?.strength ?: 0f
                        } else workingVignette
                    }
                }
                val valueText = when (toolForValue) {
                    EditorTool.TUNE -> EffectiveAdjustments.formatValue(
                        tuneParam.toolId, tuneParam.wbParam, shown
                    )
                    else -> {
                        val pct = (shown * 100).toInt()
                        (if (pct > 0) "+" else "") + "$pct"
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = Ui.textDim, fontSize = 11.sp)
                        Text(valueText, color = Ui.text, fontSize = 12.sp)
                    }
                    val span = range.endInclusive - range.start
                    val frac = if (span > 0) ((shown - range.start) / span).coerceIn(0f, 1f) else 0f
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    ) {
                        val y = size.height / 2
                        drawLine(
                            Color(0x33FFFFFF), Offset(0f, y), Offset(size.width, y),
                            strokeWidth = 3f, cap = StrokeCap.Round
                        )
                        drawCircle(Color.White, radius = 5f, center = Offset(size.width * frac, y))
                    }
                }
            }
            // Ventana de imagen (siempre visible); paneles abiertos van en espacio real bajo la franja
            }
            // Paneles de herramienta como overlay inferior
            if (activeTool != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    when (activeTool) {
                                EditorTool.CURVES -> CurvesPanel(
                                    current = EffectiveAdjustments.from(stack).curve
                                        ?: EffectiveAdjustments.defaultCurvePoints(),
                                    onChange = { points ->
                                        workingAdj = app.revela.core.ColorMath.Adjustments(curve = points)
                                    },
                                    onCommit = { points ->
                                        stack.apply(Edit.Curves(points))
                                        stackVersion++
                                        workingAdj = null
                                        persistSession()
                                    },
                                    onReset = {
                                        stack.removeTool(Tool.CURVES)
                                        stackVersion++
                                        workingAdj = null
                                        persistSession()
                                    }
                                )

                                EditorTool.SELECTIVE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        SelParam.entries.forEach { p ->
                                            FilterChip(
                                                selected = selParam == p,
                                                onClick = {
                                                    selParam = p
                                                    dragValue = Float.NaN
                                                },
                                                label = { Text(stringResource(p.labelRes), fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        stringResource(R.string.selective_hint),
                                        color = Ui.textDim,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                EditorTool.VIGNETTE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    val currentV = if (workingVignette.isNaN()) {
                                        stack.edits.filterIsInstance<Edit.Vignette>().firstOrNull()?.strength ?: 0f
                                    } else workingVignette
                                    Text(
                                        stringResource(R.string.intensity_percent, (currentV * 100).toInt()),
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Slider(
                                        value = currentV,
                                        onValueChange = { workingVignette = it },
                                        valueRange = 0f..1f
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = {
                                            workingVignette = 0f
                                            stack.removeTool(Tool.VIGNETTE)
                                            stackVersion++
                                            workingVignette = Float.NaN
                                            persistSession()
                                        }) { Text(stringResource(R.string.remove)) }
                                        Button(onClick = {
                                            val v = if (workingVignette.isNaN()) currentV else workingVignette
                                            stack.apply(Edit.Vignette(v))
                                            stackVersion++
                                            workingVignette = Float.NaN
                                            persistSession()
                                            activeTool = null
                                        }) { Text(stringResource(R.string.apply)) }
                                    }
                                }

                                EditorTool.HEAL -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.brush_size_percent, (brushSize * 100).toInt()),
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Slider(
                                        value = brushSize,
                                        onValueChange = { brushSize = it },
                                        valueRange = 0.02f..0.12f
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                if (strokeX.isNotEmpty()) {
                                                    stack.applyHealStroke(
                                                        app.revela.core.HealStroke(
                                                            radiusNorm = brushSize,
                                                            xs = strokeX.toList(),
                                                            ys = strokeY.toList()
                                                        )
                                                    )
                                                    stackVersion++
                                                    strokeX.clear()
                                                    strokeY.clear()
                                                    persistSession()
                                                }
                                            },
                                            enabled = strokeX.isNotEmpty()
                                        ) { Text(stringResource(R.string.heal_area)) }
                                        OutlinedButton(onClick = {
                                            strokeX.clear()
                                            strokeY.clear()
                                        }) { Text(stringResource(R.string.discard_stroke)) }
                                    }
                                }

                                EditorTool.PERSPECTIVE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.perspective_hint),
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        listOf(
                                            "free" to R.string.free, "scale" to R.string.scale,
                                            "tilt" to R.string.tilt, "rotate" to R.string.rotate
                                        ).forEach { (id, label) ->
                                            FilterChip(
                                                selected = perspMode == id,
                                                onClick = {
                                                    perspMode = id
                                                    if (id == "rotate") {
                                                        workingEdit = Edit.Perspective(
                                                            app.revela.core.PerspectiveMath.rotatedPlane(
                                                                perspYaw, perspPitch, perspRoll
                                                            ), id
                                                        )
                                                    } else {
                                                        val keep = (workingEdit as? Edit.Perspective)
                                                            ?.takeIf { it.mode != "rotate" }?.corners
                                                        perspCorners = keep ?: app.revela.core.PerspectiveCorners.IDENTITY
                                                        workingEdit = Edit.Perspective(perspCorners, id)
                                                    }
                                                },
                                                label = { Text(stringResource(label)) }
                                            )
                                        }
                                    }
                                    if (perspMode == "rotate") {
                                        Text(
                                            stringResource(R.string.horizontal_degrees, perspYaw.toInt()),
                                            color = Ui.text, fontSize = 12.sp
                                        )
                                        Slider(
                                            value = perspYaw,
                                            onValueChange = {
                                                perspYaw = it
                                                workingEdit = Edit.Perspective(
                                                    app.revela.core.PerspectiveMath.rotatedPlane(
                                                        perspYaw, perspPitch, perspRoll
                                                    ), "rotate"
                                                )
                                            },
                                            valueRange = -30f..30f
                                        )
                                        Text(
                                            stringResource(R.string.vertical_degrees, perspPitch.toInt()),
                                            color = Ui.text, fontSize = 12.sp
                                        )
                                        Slider(
                                            value = perspPitch,
                                            onValueChange = {
                                                perspPitch = it
                                                workingEdit = Edit.Perspective(
                                                    app.revela.core.PerspectiveMath.rotatedPlane(
                                                        perspYaw, perspPitch, perspRoll
                                                    ), "rotate"
                                                )
                                            },
                                            valueRange = -30f..30f
                                        )
                                        Text(
                                            stringResource(R.string.rotation_degrees, perspRoll.toInt()),
                                            color = Ui.text, fontSize = 12.sp
                                        )
                                        Slider(
                                            value = perspRoll,
                                            onValueChange = {
                                                perspRoll = it
                                                workingEdit = Edit.Perspective(
                                                    app.revela.core.PerspectiveMath.rotatedPlane(
                                                        perspYaw, perspPitch, perspRoll
                                                    ), "rotate"
                                                )
                                            },
                                            valueRange = -20f..20f
                                        )
                                    }
                                    OutlinedButton(onClick = {
                                        perspCorners = app.revela.core.PerspectiveCorners.IDENTITY
                                        perspYaw = 0f; perspPitch = 0f; perspRoll = 0f
                                        workingEdit = Edit.Perspective(perspCorners, perspMode)
                                    }) { Text(stringResource(R.string.reset)) }
                                }

                                EditorTool.EXPAND -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.expand_hint),
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        FilterChip(
                                            selected = !expandMirror,
                                            onClick = { expandMirror = false; workingEdit = Edit.Expand(expandEdges, false) },
                                            label = { Text(stringResource(R.string.edge_fill)) }
                                        )
                                        FilterChip(
                                            selected = expandMirror,
                                            onClick = { expandMirror = true; workingEdit = Edit.Expand(expandEdges, true) },
                                            label = { Text(stringResource(R.string.mirror_fill)) }
                                        )
                                    }
                                    OutlinedButton(onClick = {
                                        expandEdges = app.revela.core.ExpandEdges()
                                        workingEdit = Edit.Expand(expandEdges, expandMirror)
                                    }) { Text(stringResource(R.string.reset)) }
                                }

                                EditorTool.BRUSH -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.brush_intensity_combo, (brushSize * 100).toInt(), (brushAmount * 100).toInt()),
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Slider(
                                        value = brushSize,
                                        onValueChange = { brushSize = it },
                                        valueRange = 0.02f..0.12f
                                    )
                                    Slider(
                                        value = brushAmount,
                                        onValueChange = { brushAmount = it },
                                        valueRange = -1f..1f
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        app.revela.core.BrushMath.PARAMS.forEach { (id, label) ->
                                            FilterChip(
                                                selected = brushParam == id,
                                                onClick = { brushParam = id },
                                                label = { Text(stringResource(label)) }
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                if (strokeX.isNotEmpty()) {
                                                    stack.apply(
                                                        Edit.Brush(
                                                            param = brushParam,
                                                            amount = brushAmount,
                                                            strokes = listOf(
                                                                app.revela.core.HealStroke(
                                                                    radiusNorm = brushSize,
                                                                    xs = strokeX.toList(),
                                                                    ys = strokeY.toList()
                                                                )
                                                            )
                                                        )
                                                    )
                                                    stackVersion++
                                                    strokeX.clear()
                                                    strokeY.clear()
                                                    persistSession()
                                                }
                                            },
                                            enabled = strokeX.isNotEmpty()
                                        ) { Text(stringResource(R.string.apply_stroke)) }
                                        OutlinedButton(onClick = {
                                            strokeX.clear()
                                            strokeY.clear()
                                        }) { Text(stringResource(R.string.discard)) }
                                    }
                                }

                                EditorTool.HEAD_POSE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        when (hpStep) {
                                            0 -> stringResource(R.string.head_pose_step1)
                                            1 -> stringResource(R.string.head_pose_step2)
                                            2 -> stringResource(R.string.head_pose_step3)
                                            else -> stringResource(R.string.head_pose_adjust)
                                        },
                                        color = Ui.text,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        stringResource(R.string.horizontal_rotation_degrees, hpYawDeg.toInt()),
                                        color = Ui.text, fontSize = 12.sp
                                    )
                                    Slider(
                                        value = hpYawDeg,
                                        onValueChange = { hpYawDeg = it; pushHeadPose() },
                                        valueRange = -30f..30f
                                    )
                                    Text(
                                        stringResource(R.string.vertical_rotation_degrees, hpPitchDeg.toInt()),
                                        color = Ui.text, fontSize = 12.sp
                                    )
                                    Slider(
                                        value = hpPitchDeg,
                                        onValueChange = { hpPitchDeg = it; pushHeadPose() },
                                        valueRange = -30f..30f
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = {
                                            hpLeft = null; hpRight = null; hpMouth = null
                                            hpStep = 0
                                            workingEdit = null
                                        }) { Text(stringResource(R.string.reset_points)) }
                                    }
                                }

                                EditorTool.CROP -> {
                                    val g0 = cropWorking ?: geometry
                                    val baseAspect0 = base?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
                                    val odd0 = ((g0.quarterTurns % 4) + 4) % 4 % 2 == 1
                                    val frameAspect0 = if (odd0) 1f / baseAspect0 else baseAspect0
                                    LaunchedEffect(sourceUri, activeTool) {
                                        if (activeTool == EditorTool.CROP && workingGeometry == null &&
                                            g0.cropLeft <= 0f && g0.cropTop <= 0f &&
                                            g0.cropRight >= 1f && g0.cropBottom >= 1f
                                        ) {
                                            workingGeometry = g0.copy(
                                                cropLeft = 0.1f, cropTop = 0.1f, cropRight = 0.9f, cropBottom = 0.9f
                                            )
                                        }
                                    }
                                    CropPanel(
                                        geometry = cropWorking ?: geometry,
                                        frameAspect = frameAspect0,
                                        drawMode = cropDrawMode,
                                        onDrawModeChange = { cropDrawMode = it },
                                        onChange = { workingGeometry = it },
                                        onCommit = {
                                            workingGeometry?.let {
                                                geometry = it
                                                persistSession()
                                            }
                                            workingGeometry = null
                                            activeTool = null
                                        },
                                        onCancel = {
                                            workingGeometry = null
                                            activeTool = null
                                        }
                                    )
                                }

                                EditorTool.TUNE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { runAuto() }, modifier = Modifier.size(44.dp)) {
                                            Icon(
                                                Icons.Filled.AutoFixHigh,
                                                contentDescription = stringResource(R.string.auto),
                                                tint = Ui.icon
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        HistogramView(
                                            bins = histBins,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        )
                                    }
                                    AnimatedVisibility(showAutoSnack) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.auto_adjusted), color = Ui.text, fontSize = 13.sp)
                                            Spacer(Modifier.weight(1f))
                                            TextButton(onClick = { undoAuto() }) { Text(stringResource(R.string.undo)) }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TuneParam.entries.forEach { p ->
                                            FilterChip(
                                                selected = tuneParam == p,
                                                onClick = {
                                                    tuneParam = p
                                                    dragValue = Float.NaN
                                                },
                                                label = { Text(stringResource(p.labelRes), fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        stringResource(R.string.tune_gesture_hint),
                                        color = Ui.textDim,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                EditorTool.ROTATE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        OutlinedButton(onClick = { rotateBy(-1) }) {
                                            Icon(Icons.Filled.RotateLeft, contentDescription = null, tint = Ui.icon)
                                            Spacer(Modifier.width(6.dp))
                                            Text(stringResource(R.string.rotate_90_left), color = Ui.text)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        OutlinedButton(onClick = { rotateBy(1) }) {
                                            Icon(Icons.Filled.RotateRight, contentDescription = null, tint = Ui.icon)
                                            Spacer(Modifier.width(6.dp))
                                            Text(stringResource(R.string.rotate_90_right), color = Ui.text)
                                        }
                                    }
                                    Text(
                                        stringResource(R.string.rotate_hint),
                                        color = Ui.textDim,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                    )
                                }

                                EditorTool.BNW -> {
                                    val current = currentBnwFilter()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Ui.panel)
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            listOf(
                                                "neutral" to R.string.neutral,
                                                "red" to R.string.red,
                                                "yellow" to R.string.yellow,
                                                "green" to R.string.green,
                                                "blue" to R.string.blue
                                            ).forEach { (id, label) ->
                                                FilterChip(
                                                    selected = current == id,
                                                    onClick = {
                                                        stack.apply(Edit.Bnw(id))
                                                        stackVersion++
                                                        persistSession()
                                                    },
                                                    label = { Text(stringResource(label), fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                            FilterChip(
                                                selected = current == null,
                                                onClick = {
                                                    stack.removeTool(Tool.BNW)
                                                    stackVersion++
                                                    persistSession()
                                                },
                                                label = { Text(stringResource(R.string.color), fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                        Text(
                                            stringResource(R.string.bnw_hint),
                                            color = Ui.textDim,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                EditorTool.TONE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    TonalSlider(stringResource(R.string.shadows), effective().tonalShadows) { v ->
                                        workingAdj = effective().copy(tonalShadows = v)
                                    }
                                    TonalSlider(stringResource(R.string.midtones), effective().tonalMidtones) { v ->
                                        workingAdj = effective().copy(tonalMidtones = v)
                                    }
                                    TonalSlider(stringResource(R.string.highlights), effective().tonalHighlights) { v ->
                                        workingAdj = effective().copy(tonalHighlights = v)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TextButton(onClick = {
                                            stack.removeTool(Tool.TONAL)
                                            stackVersion++
                                            workingAdj = null
                                            persistSession()
                                        }) { Text(stringResource(R.string.reset), color = Ui.textDim) }
                                        Spacer(Modifier.width(16.dp))
                                        Button(onClick = {
                                            commitTonal()
                                            activeTool = null
                                        }) { Text(stringResource(R.string.apply)) }
                                    }
                                }

                                EditorTool.WB -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Ui.panel)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    TonalSlider(stringResource(R.string.temperature), effective().temperature) { v ->
                                        workingAdj = effective().copy(temperature = v)
                                    }
                                    TonalSlider(stringResource(R.string.tint), effective().tint) { v ->
                                        workingAdj = effective().copy(tint = v)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TextButton(onClick = {
                                            stack.removeTool(Tool.WHITE_BALANCE)
                                            stackVersion++
                                            workingAdj = null
                                            persistSession()
                                        }) { Text(stringResource(R.string.reset), color = Ui.textDim) }
                                        Spacer(Modifier.width(16.dp))
                                        Button(onClick = {
                                            val cur = effective()
                                            stack.apply(
                                                Edit.WhiteBalance(temperature = cur.temperature, tint = cur.tint)
                                            )
                                            stackVersion++
                                            workingAdj = null
                                            persistSession()
                                        }) { Text(stringResource(R.string.apply)) }
                                    }
                                }

                                EditorTool.DETAILS -> {
                                    val cur = (workingEdit as? Edit.Details)
                                        ?: stack.edits.filterIsInstance<Edit.Details>().firstOrNull()
                                        ?: Edit.Details()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Ui.panel)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        TonalSlider(stringResource(R.string.structure), cur.structure) { v ->
                                            workingEdit = cur.copy(structure = v)
                                        }
                                        TonalSlider(stringResource(R.string.sharpening), cur.sharpening) { v ->
                                            workingEdit = cur.copy(sharpening = v)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.DETAILS) }) { Text(stringResource(R.string.reset)) }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.Details ?: cur)
                                                activeTool = null
                                            }) { Text(stringResource(R.string.apply)) }
                                        }
                                    }
                                }

                                EditorTool.HDR -> {
                                    val cur = (workingEdit as? Edit.Hdr)?.strength
                                        ?: stack.edits.filterIsInstance<Edit.Hdr>().firstOrNull()?.strength ?: 0f
                                    StrengthPanel(
                                        label = stringResource(R.string.intensity),
                                        value = cur,
                                        onValueChange = { workingEdit = Edit.Hdr(it) },
                                        onReset = { resetTool(Tool.HDR) },
                                        onApply = { commitWorkingEdit(Edit.Hdr(cur)); activeTool = null }
                                    )
                                }

                                EditorTool.GRAIN -> {
                                    val cur = (workingEdit as? Edit.Grain)?.strength
                                        ?: stack.edits.filterIsInstance<Edit.Grain>().firstOrNull()?.strength ?: 0f
                                    StrengthPanel(
                                        label = stringResource(R.string.intensity),
                                        value = cur,
                                        onValueChange = { workingEdit = Edit.Grain(it) },
                                        onReset = { resetTool(Tool.GRAIN) },
                                        onApply = { commitWorkingEdit(Edit.Grain(cur)); activeTool = null }
                                    )
                                }

                                EditorTool.GLOW -> {
                                    val cur = (workingEdit as? Edit.Glow)?.strength
                                        ?: stack.edits.filterIsInstance<Edit.Glow>().firstOrNull()?.strength ?: 0f
                                    StrengthPanel(
                                        label = stringResource(R.string.intensity),
                                        value = cur,
                                        onValueChange = { workingEdit = Edit.Glow(it) },
                                        onReset = { resetTool(Tool.GLOW) },
                                        onApply = { commitWorkingEdit(Edit.Glow(cur)); activeTool = null }
                                    )
                                }

                                EditorTool.LENS_BLUR -> {
                                    val cur = (workingEdit as? Edit.LensBlur)?.strength
                                        ?: stack.edits.filterIsInstance<Edit.LensBlur>().firstOrNull()?.strength ?: 0f
                                    StrengthPanel(
                                        label = stringResource(R.string.intensity),
                                        value = cur,
                                        onValueChange = { workingEdit = Edit.LensBlur(it) },
                                        onReset = { resetTool(Tool.LENS_BLUR) },
                                        onApply = { commitWorkingEdit(Edit.LensBlur(cur)); activeTool = null }
                                    )
                                }

                                EditorTool.DRAMA, EditorTool.VINTAGE, EditorTool.GRAINY_FILM, EditorTool.RETROLUX,
                                EditorTool.GRUNGE, EditorTool.NOIR, EditorTool.PORTRAIT -> {
                                    val kind = activeTool?.presetKind()
                                    if (kind != null) {
                                        val current = if (workingPresetIntensity.isNaN()) 0f else workingPresetIntensity
                                        StrengthPanel(
                                            label = stringResource(kind.labelRes),
                                            value = current,
                                            onValueChange = { workingPresetIntensity = it },
                                            onReset = {
                                                workingPresetIntensity = Float.NaN
                                                kind.toolIds().forEach { stack.removeTool(it) }
                                                stackVersion++
                                                persistSession()
                                            },
                                            onApply = {
                                                val t = if (workingPresetIntensity.isNaN()) current else workingPresetIntensity
                                                kind.edits(t).forEach { stack.apply(it) }
                                                stackVersion++
                                                workingPresetIntensity = Float.NaN
                                                persistSession()
                                                activeTool = null
                                            }
                                        )
                                    }
                                }

                                EditorTool.DOUBLE_EXPO -> {
                                    val cur = (workingEdit as? Edit.DoubleExpo)
                                        ?: stack.edits.filterIsInstance<Edit.DoubleExpo>().firstOrNull()
                                        ?: Edit.DoubleExpo()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Ui.panel)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            listOf(
                                                "normal" to R.string.blend_normal,
                                                "add" to R.string.blend_add,
                                                "overlay" to R.string.blend_overlay,
                                                "subtract" to R.string.blend_subtract,
                                                "screen" to R.string.blend_screen,
                                                "multiply" to R.string.blend_multiply,
                                                "lighten" to R.string.blend_lighten,
                                                "darken" to R.string.blend_darken
                                            ).forEach { (id, label) ->
                                                FilterChip(
                                                    selected = cur.blend == id,
                                                    onClick = { workingEdit = cur.copy(blend = id) },
                                                    label = { Text(stringResource(label), fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(onClick = {
                                                overlayPickerLauncher.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }) { Text(stringResource(R.string.second_photo)) }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (overlayImage != null) stringResource(R.string.loaded) else stringResource(R.string.no_image),
                                                color = Ui.textDim,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(stringResource(R.string.opacity_percent, (cur.opacity * 100).toInt()), color = Ui.text, fontSize = 13.sp)
                                        Slider(
                                            value = cur.opacity,
                                            onValueChange = { workingEdit = cur.copy(opacity = it) },
                                            valueRange = 0f..1f
                                        )
                                        Text(
                                            stringResource(R.string.scale_rotation_combo, (cur.scale * 100).toInt(), cur.rotationDeg.toInt()),
                                            color = Ui.text, fontSize = 13.sp
                                        )
                                        Slider(
                                            value = cur.scale,
                                            onValueChange = { workingEdit = cur.copy(scale = it) },
                                            valueRange = 0.5f..2f
                                        )
                                        Slider(
                                            value = cur.rotationDeg,
                                            onValueChange = { workingEdit = cur.copy(rotationDeg = it) },
                                            valueRange = -45f..45f
                                        )
                                        Text(
                                            stringResource(R.string.double_expo_hint),
                                            color = Ui.textDim, fontSize = 12.sp
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = {
                                                resetTool(Tool.DOUBLE_EXPO)
                                                overlayImage = null
                                            }) { Text(stringResource(R.string.reset)) }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.DoubleExpo ?: cur)
                                                activeTool = null
                                            }) { Text(stringResource(R.string.apply)) }
                                        }
                                    }
                                }

                                EditorTool.TEXT -> {
                                    val cur = (workingEdit as? Edit.TextOverlay)
                                        ?: stack.edits.filterIsInstance<Edit.TextOverlay>().firstOrNull()
                                        ?: Edit.TextOverlay()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Ui.panel)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cur.content,
                                            onValueChange = { workingEdit = cur.copy(content = it) },
                                            label = { Text(stringResource(R.string.tool_text)) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            textColorChips.forEach { (label, colorInt) ->
                                                FilterChip(
                                                    selected = cur.colorInt == colorInt,
                                                    onClick = { workingEdit = cur.copy(colorInt = colorInt) },
                                                    label = { Text(stringResource(label), fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        Text(stringResource(R.string.size_percent, (cur.sizePct * 100).toInt()), color = Ui.text, fontSize = 13.sp)
                                        Slider(
                                            value = cur.sizePct,
                                            onValueChange = { workingEdit = cur.copy(sizePct = it) },
                                            valueRange = 0.02f..0.5f
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            listOf("top" to R.string.top, "center" to R.string.center, "bottom" to R.string.bottom)
                                                .forEach { (id, label) ->
                                                    FilterChip(
                                                        selected = cur.gravity == id,
                                                        onClick = { workingEdit = cur.copy(gravity = id) },
                                                        label = { Text(stringResource(label), fontSize = 12.sp) },
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.TEXT) }) { Text(stringResource(R.string.reset)) }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.TextOverlay ?: cur)
                                                activeTool = null
                                            }) { Text(stringResource(R.string.apply)) }
                                        }
                                    }
                                }

                                EditorTool.FRAME -> {
                                    val cur = (workingEdit as? Edit.Frame)
                                        ?: stack.edits.filterIsInstance<Edit.Frame>().firstOrNull()
                                        ?: Edit.Frame()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Ui.panel)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            textColorChips.forEach { (label, colorInt) ->
                                                FilterChip(
                                                    selected = cur.colorInt == colorInt,
                                                    onClick = { workingEdit = cur.copy(colorInt = colorInt) },
                                                    label = { Text(stringResource(label), fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        Text(stringResource(R.string.thickness_percent, (cur.widthPct * 100).toInt()), color = Ui.text, fontSize = 13.sp)
                                        Slider(
                                            value = cur.widthPct,
                                            onValueChange = { workingEdit = cur.copy(widthPct = it) },
                                            valueRange = 0.01f..0.25f
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.FRAME) }) { Text(stringResource(R.string.reset)) }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.Frame ?: cur)
                                                activeTool = null
                                            }) { Text(stringResource(R.string.apply)) }
                                        }
                                    }
                                }

                                else -> {}

                            }
                }
            }
        }
        // Panel de herramientas en ESPACIO REAL bajo la franja de la foto, entra deslizandose.
        // El weight va en el AnimatedVisibility (hijo directo de la Column) para que la barra
        // de pestanas quede SIEMPRE abajo aunque el panel este animandose.
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            visible = activeTool == null && bottomTab == BottomTab.TOOLS && base != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
            ) + fadeIn(tween(140)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(150))
        ) {
            ToolsGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                favorites = favorites.toSet(),
                onToggleFavorite = { toggleFavorite(it) },
                onSelect = {
                    activeTool = it
                    bottomTab = BottomTab.NONE
                }
            )
        }
        // Carrusel de estilos en espacio real bajo la franja de la foto, entra deslizandose
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            visible = activeTool == null && bottomTab == BottomTab.LOOKS && base != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
            ) + fadeIn(tween(140)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(150))
        ) {
            if (activeTool == null && base != null) {
            LooksCarousel(
                thumbs = lookThumbs,
                selectedId = stack.edits.filterIsInstance<Edit.Look>().lastOrNull()?.id ?: "none",
                onSelect = { look ->
                    if (look.id == "none") stack.removeTool(Tool.LOOK)
                    else stack.apply(Edit.Look(look.id))
                    stackVersion++
                    persistSession()
                },
                customStyles = customStyles,
                customThumbs = customThumbs,
                selectedCustomId = selectedCustomId,
                onSelectCustom = { cs ->
                    runCatching { EditStack.fromJson(cs.editsJson) }.getOrNull()?.let { st ->
                        stack = st
                        selectedCustomId = cs.id
                        stackVersion++
                        persistSession()
                    }
                },
                onDeleteCustom = { deleteStyle = it },
                onManageCustom = {
                    manageStyle = it
                    manageName = it.name
                },
                onSaveNew = {
                    styleName = ""
                    showStyleNameDialog = true
                }
            )
            }
        }
        // La barra de favoritos solo vive sin paneles: al abrir Estilos/Herramientas se va
        // deslizandose hacia abajo (issue #2: antes quedaba visible en Estilos).
        AnimatedVisibility(
            visible = activeTool == null && base != null && bottomTab == BottomTab.NONE,
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(150, easing = FastOutSlowInEasing)) + fadeOut(tween(150)),
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) + fadeIn(tween(140))
        ) {
            FavoritesBar(
                favorites = favorites,
                onSelect = {
                    activeTool = it
                    bottomTab = BottomTab.NONE
                },
                onEdit = { showFavEditor = true }
            )
        }
        // Barra de pestañas fija (la foto nunca cambia de altura al abrir paneles)
        if (activeTool == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ui.panel)
                    .navigationBarsPadding()
            ) {
                                BottomTabs(
                                    selected = bottomTab,
                                    enabled = base != null,
                                    onSelect = { bottomTab = it },
                                    onExport = { showExportDialog = true }
                                )
            }
        }
    }

    if (showRevertConfirm) {
        AlertDialog(
            onDismissRequest = { showRevertConfirm = false },
            title = { Text(stringResource(R.string.revert_all)) },
            text = { Text(stringResource(R.string.revert_all_message)) },
            confirmButton = {
                Button(onClick = {
                    stack = EditStack()
                    geometry = GeometryModel()
                    stackVersion++
                    persistSession()
                    showRevertConfirm = false
                }) { Text(stringResource(R.string.revert)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRevertConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showStyleNameDialog) {
        AlertDialog(
            onDismissRequest = { showStyleNameDialog = false },
            title = { Text(stringResource(R.string.save_style)) },
            text = {
                Column {
                    Text(stringResource(R.string.save_style_message))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = styleName,
                        onValueChange = { styleName = it },
                        label = { Text(stringResource(R.string.name)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = styleName.trim()
                        if (name.isNotEmpty()) {
                            val cs = CustomStyle(
                                id = "c" + System.currentTimeMillis(),
                                name = name,
                                editsJson = stack.toJson()
                            )
                            customStyles = customStyles + cs
                            app.revela.render.CustomStylesStore.save(context, customStyles)
                            selectedCustomId = cs.id
                        }
                        showStyleNameDialog = false
                    },
                    enabled = styleName.isNotBlank()
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStyleNameDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Detalles de la imagen: dimensiones + EXIF basico de la foto original
    if (showImageDetails) {
        var details by remember { mutableStateOf(context.getString(R.string.reading)) }
        LaunchedEffect(sourceUri) {
            val u = sourceUri
            details = if (u == null) {
                context.getString(R.string.no_image_open)
            } else {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val resolver = context.contentResolver
                        val sb = StringBuilder()
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        resolver.openInputStream(u)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
                        if (bounds.outWidth > 0) {
                            sb.append(context.getString(R.string.dimensions, bounds.outWidth, bounds.outHeight))
                        }
                        resolver.openInputStream(u)?.use { input ->
                            val exif = androidx.exifinterface.media.ExifInterface(input)
                            fun tag(label: String, t: String) {
                                exif.getAttribute(t)?.let { sb.append("$label: $it\n") }
                            }
                            tag(context.getString(R.string.exif_capture_date), androidx.exifinterface.media.ExifInterface.TAG_DATETIME_ORIGINAL)
                            tag(context.getString(R.string.exif_camera), androidx.exifinterface.media.ExifInterface.TAG_MAKE)
                            tag(context.getString(R.string.exif_model), androidx.exifinterface.media.ExifInterface.TAG_MODEL)
                            tag(context.getString(R.string.exif_aperture), androidx.exifinterface.media.ExifInterface.TAG_F_NUMBER)
                            tag(context.getString(R.string.exif_exposure), androidx.exifinterface.media.ExifInterface.TAG_EXPOSURE_TIME)
                            tag(context.getString(R.string.exif_iso), androidx.exifinterface.media.ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                            tag(context.getString(R.string.exif_focal), androidx.exifinterface.media.ExifInterface.TAG_FOCAL_LENGTH)
                        }
                        if (sb.isEmpty()) context.getString(R.string.no_metadata) else sb.toString().trim()
                    }.getOrDefault(context.getString(R.string.metadata_error))
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showImageDetails = false },
            title = { Text(stringResource(R.string.image_details)) },
            text = { Text(details) },
            confirmButton = {
                TextButton(onClick = { showImageDetails = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    // Configuracion: formato y calidad de export por defecto
    if (showSettings) {
        var cfgPng by remember { mutableStateOf(appSettings.exportPng) }
        var cfgQuality by remember { mutableIntStateOf(appSettings.exportQuality) }
        var cfgLang by remember { mutableStateOf(appSettings.language) }
        var cfgTheme by remember { mutableStateOf(appSettings.theme) }
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text(stringResource(R.string.settings)) },
            text = {
                Column {
                    Text(stringResource(R.string.default_export), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !cfgPng, onClick = { cfgPng = false }, label = { Text("JPEG") })
                        FilterChip(selected = cfgPng, onClick = { cfgPng = true }, label = { Text("PNG") })
                    }
                    if (!cfgPng) {
                        Text(stringResource(R.string.quality, cfgQuality), fontSize = 13.sp)
                        Slider(
                            value = cfgQuality.toFloat(),
                            onValueChange = { cfgQuality = it.toInt() },
                            valueRange = 60f..100f
                        )
                    }
                    Text(
                        stringResource(R.string.export_saved_hint),
                        fontSize = 12.sp,
                        color = Ui.textDim
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.theme), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = cfgTheme != "dark",
                            onClick = { cfgTheme = "light" },
                            label = { Text(stringResource(R.string.theme_light)) }
                        )
                        FilterChip(
                            selected = cfgTheme == "dark",
                            onClick = { cfgTheme = "dark" },
                            label = { Text(stringResource(R.string.theme_dark)) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.language), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = cfgLang == "system",
                            onClick = { cfgLang = "system" },
                            label = { Text(stringResource(R.string.system)) }
                        )
                        FilterChip(
                            selected = cfgLang == "es",
                            onClick = { cfgLang = "es" },
                            label = { Text(stringResource(R.string.spanish)) }
                        )
                        FilterChip(
                            selected = cfgLang == "en",
                            onClick = { cfgLang = "en" },
                            label = { Text(stringResource(R.string.english)) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val langChanged = cfgLang != appSettings.language
                    val themeChanged = cfgTheme != appSettings.theme
                    appSettings = appSettings.copy(
                        exportPng = cfgPng, exportQuality = cfgQuality,
                        language = cfgLang, theme = cfgTheme
                    )
                    app.revela.core.SettingsStore.save(context.filesDir, appSettings)
                    Ui.setDark(cfgTheme == "dark")
                    showSettings = false
                    if (langChanged || themeChanged) (context as? android.app.Activity)?.recreate()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSettings = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Editor de la barra de favoritos: lista completa con marcas
    if (showFavEditor) {
        AlertDialog(
            onDismissRequest = { showFavEditor = false },
            title = { Text(stringResource(R.string.toolbar)) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.favorites_editor_hint),
                        fontSize = 13.sp,
                        color = Ui.textDim
                    )
                    Spacer(Modifier.height(8.dp))
                    toolCatalog().forEach { (tool, icon, label) ->
                        val labelText = stringResource(label)
                        val fav = tool.id in favorites
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleFavorite(tool.id) }
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(
                                icon, labelText,
                                tint = if (fav) Ui.accent else Ui.line,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                labelText,
                                fontSize = 14.sp,
                                color = if (fav) Ui.text else Ui.textDim,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (fav) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = if (fav) stringResource(R.string.remove) else stringResource(R.string.add),
                                tint = if (fav) Ui.accent else Ui.line,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFavEditor = false }) { Text(stringResource(R.string.done)) }
            }
        )
    }

    if (showNote) {
        AlertDialog(
            onDismissRequest = { showNote = false },
            title = { Text(stringResource(R.string.note_title)) },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    val u = sourceUri
                    val ok = if (u == null) false else {
                        // La sesion puede no existir aun si la foto no tiene ediciones: crearla antes de la nota
                        persistSession()
                        SessionIndexStore.setNote(
                            context.filesDir,
                            SessionIndexStore.idFor(u.toString()),
                            noteText.trim()
                        )
                    }
                    Toast.makeText(
                        context,
                        context.getString(if (ok) R.string.note_saved else R.string.note_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    // La nota se escribe en el indice fuera del estado de Compose: avisar a la
                    // biblioteca para que recalcule filtros y "Con nota" al volver.
                    if (ok) onChanged()
                    showNote = false
                }) { Text(stringResource(R.string.note_save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNote = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    showTip?.let { tip ->
        AlertDialog(
            onDismissRequest = { showTip = null },
            title = { Text(stringResource(R.string.tool_tip)) },
            text = { Text(tip) },
            confirmButton = {
                TextButton(onClick = { showTip = null }) { Text(stringResource(R.string.done)) }
            }
        )
    }

    if (showLayers) {
        val rows = LayerList.rows(stack.edits, hiddenLayers) { layerLabel(it) }
        LayersSheet(
            rows = rows,
            canUndo = stack.canUndo,
            canRedo = stack.canRedo,
            canRevert = stack.edits.isNotEmpty() || geometry != GeometryModel(),
            onUndo = {
                if (stack.undo()) {
                    stackVersion++
                    persistSession()
                    persistHidden()
                }
            },
            onRedo = {
                if (stack.redo()) {
                    stackVersion++
                    persistSession()
                    persistHidden()
                }
            },
            onRevert = {
                stack.reset(emptyList())
                geometry = GeometryModel()
                hiddenLayers = emptySet()
                stackVersion++
                persistSession()
                persistHidden()
            },
            onToggle = { row ->
                hiddenLayers = if (row.hidden) hiddenLayers - row.toolId else hiddenLayers + row.toolId
                stackVersion++
                persistHidden()
            },
            onEdit = { row ->
                showLayers = false
                openToolForLayer(row.toolId)?.let { tool ->
                    activeTool = tool
                    bottomTab = BottomTab.NONE
                }
            },
            onDelete = { row ->
                stack.removeTool(row.toolId)
                hiddenLayers = hiddenLayers - row.toolId
                stackVersion++
                persistSession()
                persistHidden()
            },
            onDismiss = { showLayers = false }
        )
    }

    if (showDeleteOriginal) {
        AlertDialog(
            onDismissRequest = { showDeleteOriginal = false },
            title = { Text(stringResource(R.string.delete_original)) },
            text = { Text(stringResource(R.string.delete_original_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteOriginal = false
                    deleteOriginalPhoto()
                }) { Text(stringResource(R.string.delete), color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteOriginal = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Column {
                    Text(stringResource(R.string.about_description), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.version_label, app.revela.BuildConfig.VERSION_NAME), fontSize = 13.sp, color = Ui.textDim)
                    Text(stringResource(R.string.license_label), fontSize = 13.sp, color = Ui.textDim)
                    Text("github.com/gnacho/revela", fontSize = 13.sp, color = Ui.textDim)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    deleteStyle?.let { cs ->
        AlertDialog(
            onDismissRequest = { deleteStyle = null },
            title = { Text(stringResource(R.string.delete_style)) },
            text = { Text(stringResource(R.string.delete_style_message, cs.name)) },
            confirmButton = {
                Button(onClick = {
                    customStyles = customStyles.filterNot { it.id == cs.id }
                    app.revela.render.CustomStylesStore.save(context, customStyles)
                    if (selectedCustomId == cs.id) selectedCustomId = null
                    deleteStyle = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteStyle = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    manageStyle?.let { cs ->
        val idx = customStyles.indexOfFirst { it.id == cs.id }
        AlertDialog(
            onDismissRequest = { manageStyle = null },
            title = { Text(stringResource(R.string.manage_style)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = manageName,
                        onValueChange = { manageName = it },
                        label = { Text(stringResource(R.string.style_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                customStyles = CustomStylesCodec.move(customStyles, cs.id, -1)
                                app.revela.render.CustomStylesStore.save(context, customStyles)
                                manageStyle = customStyles.firstOrNull { it.id == cs.id }
                            },
                            enabled = idx > 0
                        ) { Text(stringResource(R.string.move_left)) }
                        OutlinedButton(
                            onClick = {
                                customStyles = CustomStylesCodec.move(customStyles, cs.id, 1)
                                app.revela.render.CustomStylesStore.save(context, customStyles)
                                manageStyle = customStyles.firstOrNull { it.id == cs.id }
                            },
                            enabled = idx in 0 until customStyles.lastIndex
                        ) { Text(stringResource(R.string.move_right)) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!CustomStylesCodec.nameTaken(customStyles, manageName, cs.id)) {
                            customStyles = CustomStylesCodec.rename(customStyles, cs.id, manageName)
                            app.revela.render.CustomStylesStore.save(context, customStyles)
                        }
                        manageStyle = null
                    },
                    enabled = manageName.isNotBlank() &&
                        !CustomStylesCodec.nameTaken(customStyles, manageName, cs.id)
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { manageStyle = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (sourceUri != null && showExportDialog) {
        ExportDialog(
            defaults = appSettings,
            onDismiss = { showExportDialog = false },
            onExport = { options, share ->
                showExportDialog = false
                scope.launch {
                    val result = ExportProcessor.export(
                        context, sourceUri!!, stack, geometry, options, overlayImage
                    )
                    result.onFailure {
                        android.util.Log.e("revela", "export fallo", it)
                    }
                    val uri = result.getOrNull()
                    Toast.makeText(
                        context,
                        if (result.isSuccess) context.getString(R.string.export_saved)
                        else context.getString(R.string.export_error, result.exceptionOrNull()?.message),
                        Toast.LENGTH_LONG
                    ).show()
                    if (share && uri != null) {
                        runCatching {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = if (options.png) "image/png" else "image/jpeg"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(intent, context.getString(R.string.share_image))
                            )
                        }
                    }
                }
            }
        )
    }
    }
}

/** Factor de escala para que el contenido girado cubra siempre la ventana (formula exacta de cover). */
fun coverScale(angleDeg: Float, contentAspect: Float): Float {
    val rad = Math.toRadians(angleDeg.toDouble())
    val c = abs(cos(rad)).toFloat()
    val s = abs(sin(rad)).toFloat()
    val w = contentAspect
    val h = 1f
    return max((w * c + h * s) / w, (w * s + h * c) / h)
}

@Composable
fun CurvesPanel(
    current: List<CurvePoint>,
    onChange: (List<CurvePoint>) -> Unit,
    onCommit: (List<CurvePoint>) -> Unit,
    onReset: () -> Unit
) {
    var points by remember { mutableStateOf(current) }
    val currentRef = rememberUpdatedState(current)

    LaunchedEffect(currentRef.value) {
        points = currentRef.value
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ui.panel)
            .navigationBarsPadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurveEditor(
            points = points,
            onChange = {
                points = it
                onChange(it)
            }
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                val identity = EffectiveAdjustments.defaultCurvePoints()
                points = identity
                onChange(identity)
            }) { Text(stringResource(R.string.reset)) }
            Button(onClick = { onCommit(points) }) { Text(stringResource(R.string.apply)) }
        }
    }
}

@Composable
fun CurveEditor(points: List<CurvePoint>, onChange: (List<CurvePoint>) -> Unit) {
    val side = 280.dp
    val pointsRef = rememberUpdatedState(points)
    val onChangeRef = rememberUpdatedState(onChange)

    Box(
        modifier = Modifier
            .width(side)
            .height(side)
            .background(Ui.panel)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { pos ->
                        val s = size.width.toFloat()
                        val np = CurvePoint(
                            (pos.x / s).coerceIn(0f, 1f),
                            (1f - pos.y / s).coerceIn(0f, 1f)
                        )
                        val pts = pointsRef.value
                        if (pts.none { abs(it.x - np.x) < 0.04f && abs(it.y - np.y) < 0.08f }) {
                            onChangeRef.value((pts + np).sortedBy { p -> p.x })
                        }
                    },
                    onLongPress = { pos ->
                        val s = size.width.toFloat()
                        val np = CurvePoint(
                            (pos.x / s).coerceIn(0f, 1f),
                            (1f - pos.y / s).coerceIn(0f, 1f)
                        )
                        val pts = pointsRef.value
                        val nearest = pts.withIndex().minByOrNull {
                            abs(it.value.x - np.x) + abs(it.value.y - np.y)
                        }
                        if (nearest != null && nearest.index != 0 && nearest.index != pts.lastIndex &&
                            abs(nearest.value.x - np.x) < 0.08f
                        ) {
                            onChangeRef.value(pts.toMutableList().apply { removeAt(nearest.index) })
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos ->
                        val s = size.width.toFloat()
                        val np = CurvePoint(
                            (pos.x / s).coerceIn(0f, 1f),
                            (1f - pos.y / s).coerceIn(0f, 1f)
                        )
                        draggingIndex = pointsRef.value.withIndex().minByOrNull {
                            abs(it.value.x - np.x) + abs(it.value.y - np.y)
                        }?.index ?: -1
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val i = draggingIndex
                        val pts = pointsRef.value
                        if (i != -1 && i < pts.size) {
                            val s = size.width.toFloat()
                            val moved = pts.toMutableList()
                            val cur = moved[i]
                            var nx = (cur.x + amount.x / s).coerceIn(0f, 1f)
                            val ny = (cur.y - amount.y / s).coerceIn(0f, 1f)
                            if (i == 0) nx = 0f
                            if (i == pts.lastIndex) nx = 1f
                            if (i > 0) nx = max(nx, moved[i - 1].x + 0.02f)
                            if (i < pts.lastIndex) nx = min(nx, moved[i + 1].x - 0.02f)
                            moved[i] = CurvePoint(nx, ny)
                            onChangeRef.value(moved)
                        }
                    },
                    onDragEnd = { draggingIndex = -1 },
                    onDragCancel = { draggingIndex = -1 }
                )
            }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val s = size.minDimension
            for (f in listOf(0.25f, 0.5f, 0.75f)) {
                drawLine(Ui.line, Offset(s * f, 0f), Offset(s * f, s))
                drawLine(Ui.line, Offset(0f, s * f), Offset(s, s * f))
            }
            drawLine(Color(0xFF44444F), Offset(0f, s), Offset(s, 0f))
            val lut = app.revela.core.CurveMath.lut(pointsRef.value)
            val path = Path()
            for (i in 0 until 256) {
                val x = i / 255f * s
                val y = (1f - lut[i]) * s
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, Color.White, style = Stroke(width = 3f, cap = StrokeCap.Round))
            pointsRef.value.forEach { p ->
                val center = Offset(p.x * s, (1f - p.y) * s)
                drawCircle(Color.White, radius = 7f, center = center)
                drawCircle(Color(0xFF7AA2FF), radius = 4f, center = center)
            }
        }
    }
}

private var draggingIndex by mutableStateOf(-1)

@Composable
fun CropPanel(
    geometry: GeometryModel,
    frameAspect: Float,
    drawMode: Boolean,
    onDrawModeChange: (Boolean) -> Unit,
    onChange: (GeometryModel) -> Unit,
    onCommit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ui.panel)
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            FilterChip(
                selected = !drawMode,
                onClick = { onDrawModeChange(false) },
                label = { Text(stringResource(R.string.adjust_box), fontSize = 12.sp) }
            )
            FilterChip(
                selected = drawMode,
                onClick = { onDrawModeChange(true) },
                label = { Text(stringResource(R.string.draw_selection), fontSize = 12.sp) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            listOf(
                stringResource(R.string.free) to null,
                "1:1" to 1f,
                "4:3" to 4f / 3f,
                "3:4" to 3f / 4f,
                "16:9" to 16f / 9f,
                "9:16" to 9f / 16f
            ).forEach { (label, ratio) ->
                FilterChip(
                    selected = false,
                    onClick = {
                        if (ratio == null) {
                            onChange(
                                geometry.copy(
                                    cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f
                                )
                            )
                        } else {
                            onChange(centeredRatioRect(geometry, ratio, frameAspect))
                        }
                    },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onChange(
                    geometry.copy(
                        quarterTurns = geometry.quarterTurns - 1,
                        straightenDeg = 0f,
                        cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f
                    )
                )
            }) { Icon(Icons.Filled.RotateLeft, stringResource(R.string.rotate_left), tint = Ui.icon) }
            IconButton(onClick = {
                onChange(
                    geometry.copy(
                        quarterTurns = geometry.quarterTurns + 1,
                        straightenDeg = 0f,
                        cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f
                    )
                )
            }) { Icon(Icons.Filled.RotateRight, stringResource(R.string.rotate_right), tint = Ui.icon) }
            IconButton(onClick = { onChange(GeometryModel()) }) {
                Icon(Icons.Filled.RestartAlt, stringResource(R.string.reset_geometry), tint = Ui.icon)
            }
            Button(onClick = onCommit) { Text(stringResource(R.string.ok)) }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.straighten), color = Ui.text, fontSize = 13.sp)
            Slider(
                value = geometry.straightenDeg,
                onValueChange = { onChange(geometry.copy(straightenDeg = it)) },
                valueRange = -45f..45f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

private fun centeredRatioRect(
    g: GeometryModel,
    ratio: Float,
    frameAspect: Float
): GeometryModel {
    val cx = (g.cropLeft + g.cropRight) / 2f
    val cy = (g.cropTop + g.cropBottom) / 2f
    val target = ratio / frameAspect
    var w: Float
    var h: Float
    if (target >= 1f) {
        w = 1f
        h = 1f / target
    } else {
        h = 1f
        w = target
    }
    val left = (cx - w / 2f).coerceIn(0f, 1f - w)
    val top = (cy - h / 2f).coerceIn(0f, 1f - h)
    return g.copy(cropLeft = left, cropTop = top, cropRight = left + w, cropBottom = top + h)
}

private enum class Handle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT, TOP, BOTTOM
}

private enum class CropMode { NONE, RESIZE, MOVE, DRAW }

@Composable
fun CropOverlay(
    modifier: Modifier = Modifier,
    geometry: GeometryModel,
    frameAspect: Float,
    drawMode: Boolean,
    onGeometry: (GeometryModel) -> Unit
) {
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val geometryRef = rememberUpdatedState(geometry)
    val onGeometryRef = rememberUpdatedState(onGeometry)
    val drawModeRef = rememberUpdatedState(drawMode)
    var handle by remember { mutableStateOf<Handle?>(null) }
    var mode by remember { mutableStateOf(CropMode.NONE) }
    var moveOffset by remember { mutableStateOf(Offset.Zero) }
    var drawStart by remember { mutableStateOf(Offset.Zero) }
    val handlePx = with(LocalDensity.current) { 18.dp.toPx() }

    Box(
        modifier = modifier
            .onSizeChanged { viewport = it }
            .pointerInput(frameAspect) {
                if (viewport.width == 0 || viewport.height == 0) return@pointerInput
                detectDragGestures(
                    onDragStart = { pos ->
                        val g = geometryRef.value
                        val w = viewport.width.toFloat()
                        val h = viewport.height.toFloat()
                        if (drawModeRef.value) {
                            mode = CropMode.DRAW
                            drawStart = pos
                            handle = null
                        } else {
                            val h0 = hitHandle(pos, g, w, h, handlePx)
                            when {
                                h0 != null -> {
                                    handle = h0
                                    mode = CropMode.RESIZE
                                }
                                pos.x in g.cropLeft * w..g.cropRight * w &&
                                    pos.y in g.cropTop * h..g.cropBottom * h -> {
                                    mode = CropMode.MOVE
                                    moveOffset = Offset(
                                        pos.x - g.cropLeft * w,
                                        pos.y - g.cropTop * h
                                    )
                                }
                                else -> {
                                    mode = CropMode.DRAW
                                    drawStart = pos
                                }
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val g = geometryRef.value
                        val w = viewport.width.toFloat()
                        val h = viewport.height.toFloat()
                        val minSize = 0.08f
                        when (mode) {
                            CropMode.RESIZE -> {
                                val dx = change.position.x / w
                                val dy = change.position.y / h
                                var l = g.cropLeft
                                var t = g.cropTop
                                var r = g.cropRight
                                var b = g.cropBottom
                                when (handle) {
                                    Handle.TOP_LEFT -> {
                                        l = dx.coerceIn(0f, r - minSize)
                                        t = dy.coerceIn(0f, b - minSize)
                                    }
                                    Handle.TOP_RIGHT -> {
                                        r = dx.coerceIn(l + minSize, 1f)
                                        t = dy.coerceIn(0f, b - minSize)
                                    }
                                    Handle.BOTTOM_LEFT -> {
                                        l = dx.coerceIn(0f, r - minSize)
                                        b = dy.coerceIn(t + minSize, 1f)
                                    }
                                    Handle.BOTTOM_RIGHT -> {
                                        r = dx.coerceIn(l + minSize, 1f)
                                        b = dy.coerceIn(t + minSize, 1f)
                                    }
                                    Handle.LEFT -> l = dx.coerceIn(0f, r - minSize)
                                    Handle.RIGHT -> r = dx.coerceIn(l + minSize, 1f)
                                    Handle.TOP -> t = dy.coerceIn(0f, b - minSize)
                                    Handle.BOTTOM -> b = dy.coerceIn(t + minSize, 1f)
                                    null -> {}
                                }
                                onGeometryRef.value(
                                    g.copy(cropLeft = l, cropTop = t, cropRight = r, cropBottom = b)
                                )
                            }

                            CropMode.MOVE -> {
                                val cw = g.cropRight - g.cropLeft
                                val ch = g.cropBottom - g.cropTop
                                val l = ((change.position.x - moveOffset.x) / w)
                                    .coerceIn(0f, 1f - cw)
                                val t = ((change.position.y - moveOffset.y) / h)
                                    .coerceIn(0f, 1f - ch)
                                onGeometryRef.value(
                                    g.copy(cropLeft = l, cropTop = t, cropRight = l + cw, cropBottom = t + ch)
                                )
                            }

                            CropMode.DRAW -> {
                                val p = change.position
                                val l = min(drawStart.x, p.x) / w
                                val r = max(drawStart.x, p.x) / w
                                val t = min(drawStart.y, p.y) / h
                                val b = max(drawStart.y, p.y) / h
                                if (r - l > minSize / 2f && b - t > minSize / 2f) {
                                    onGeometryRef.value(
                                        g.copy(
                                            cropLeft = l.coerceIn(0f, 1f),
                                            cropTop = t.coerceIn(0f, 1f),
                                            cropRight = r.coerceIn(0f, 1f),
                                            cropBottom = b.coerceIn(0f, 1f)
                                        )
                                    )
                                }
                            }

                            CropMode.NONE -> {}
                        }
                    },
                    onDragEnd = { mode = CropMode.NONE; handle = null },
                    onDragCancel = { mode = CropMode.NONE; handle = null }
                )
            }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val g = geometryRef.value
            val l = g.cropLeft * w
            val t = g.cropTop * h
            val r = g.cropRight * w
            val b = g.cropBottom * h
            val dim = Color.Black.copy(alpha = 0.55f)
            drawRect(dim, Offset(0f, 0f), Size(w, t))
            drawRect(dim, Offset(0f, b), Size(w, h - b))
            drawRect(dim, Offset(0f, t), Size(l, b - t))
            drawRect(dim, Offset(r, t), Size(w - r, b - t))
            drawRect(
                Color.White,
                topLeft = Offset(l, t),
                size = Size(r - l, b - t),
                style = Stroke(width = 2f)
            )
            val cw = r - l
            val ch = b - t
            for (f in listOf(1f / 3f, 2f / 3f)) {
                drawLine(
                    Color.White.copy(alpha = 0.4f),
                    Offset(l + cw * f, t), Offset(l + cw * f, b), strokeWidth = 1.5f
                )
                drawLine(
                    Color.White.copy(alpha = 0.4f),
                    Offset(l, t + ch * f), Offset(r, t + ch * f), strokeWidth = 1.5f
                )
            }
            val cornerLen = 42f
            fun corner(hx: Float, hy: Float, sx: Float, sy: Float) {
                drawLine(
                    Color.White, Offset(hx, hy), Offset(hx + sx * cornerLen, hy),
                    strokeWidth = 5f, cap = StrokeCap.Round
                )
                drawLine(
                    Color.White, Offset(hx, hy), Offset(hx, hy + sy * cornerLen),
                    strokeWidth = 5f, cap = StrokeCap.Round
                )
            }
            corner(l, t, 1f, 1f)
            corner(r, t, -1f, 1f)
            corner(l, b, 1f, -1f)
            corner(r, b, -1f, -1f)
        }
    }
}

private fun hitHandle(
    pos: Offset,
    g: GeometryModel,
    w: Float,
    h: Float,
    radius: Float
): Handle? {
    val l = g.cropLeft * w
    val t = g.cropTop * h
    val r = g.cropRight * w
    val b = g.cropBottom * h
    fun near(x: Float, y: Float): Boolean = abs(pos.x - x) <= radius && abs(pos.y - y) <= radius
    return when {
        near(l, t) -> Handle.TOP_LEFT
        near(r, t) -> Handle.TOP_RIGHT
        near(l, b) -> Handle.BOTTOM_LEFT
        near(r, b) -> Handle.BOTTOM_RIGHT
        abs(pos.x - l) <= radius && pos.y in t..b -> Handle.LEFT
        abs(pos.x - r) <= radius && pos.y in t..b -> Handle.RIGHT
        abs(pos.y - t) <= radius && pos.x in l..r -> Handle.TOP
        abs(pos.y - b) <= radius && pos.x in l..r -> Handle.BOTTOM
        else -> null
    }
}

@Composable
fun ExportDialog(
    defaults: app.revela.core.AppSettings,
    onDismiss: () -> Unit,
    onExport: (ExportOptions, Boolean) -> Unit
) {
    var png by remember { mutableStateOf(defaults.exportPng) }
    var quality by remember { mutableIntStateOf(defaults.exportQuality) }
    var share by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_image)) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !png, onClick = { png = false }, label = { Text("JPEG") })
                    FilterChip(selected = png, onClick = { png = true }, label = { Text("PNG") })
                }
                if (!png) {
                    Text(stringResource(R.string.quality, quality), fontSize = 13.sp)
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 60f..100f
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = share, onCheckedChange = { share = it })
                    Text(stringResource(R.string.share_after_export), fontSize = 13.sp)
                }
                Text(
                    stringResource(R.string.export_save_hint),
                    fontSize = 12.sp,
                    color = Ui.textDim
                )
            }
        },
        confirmButton = {
            Button(onClick = { onExport(ExportOptions(png = png, quality = quality), share) }) {
                Text(stringResource(R.string.export))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Catalogo completo de herramientas (28, orden Snapseed) compartido por rejilla y barra de favoritos. Iconos outline estilo Snapseed. */
fun toolCatalog(): List<Triple<EditorTool, androidx.compose.ui.graphics.vector.ImageVector, Int>> = listOf(
    Triple(EditorTool.TUNE, Icons.Outlined.Tune, R.string.tool_tune),
    Triple(EditorTool.DETAILS, Icons.Outlined.Details, R.string.tool_details),
    Triple(EditorTool.CURVES, Icons.Outlined.ShowChart, R.string.tool_curves),
    Triple(EditorTool.WB, Icons.Outlined.WbSunny, R.string.tool_white_balance),
    Triple(EditorTool.CROP, Icons.Outlined.Crop, R.string.tool_crop),
    Triple(EditorTool.ROTATE, Icons.Outlined.RotateRight, R.string.tool_rotate),
    Triple(EditorTool.PERSPECTIVE, Icons.Outlined.Transform, R.string.tool_perspective),
    Triple(EditorTool.EXPAND, Icons.Outlined.AspectRatio, R.string.tool_expand),
    Triple(EditorTool.SELECTIVE, Icons.Outlined.CenterFocusStrong, R.string.tool_selective),
    Triple(EditorTool.BRUSH, Icons.Outlined.Brush, R.string.tool_brush),
    Triple(EditorTool.HEAL, Icons.Outlined.Healing, R.string.tool_heal),
    Triple(EditorTool.HDR, Icons.Outlined.HdrStrong, R.string.tool_hdr),
    Triple(EditorTool.GLOW, Icons.Outlined.AutoAwesome, R.string.tool_glow),
    Triple(EditorTool.TONE, Icons.Outlined.Tonality, R.string.tool_tone),
    Triple(EditorTool.DRAMA, Icons.Outlined.Bolt, R.string.tool_drama),
    Triple(EditorTool.VINTAGE, Icons.Outlined.PhotoCamera, R.string.tool_vintage),
    Triple(EditorTool.GRAINY_FILM, Icons.Outlined.Movie, R.string.tool_grainy_film),
    Triple(EditorTool.RETROLUX, Icons.Outlined.Flare, R.string.tool_retrolux),
    Triple(EditorTool.GRUNGE, Icons.Outlined.Texture, R.string.tool_grunge),
    Triple(EditorTool.BNW, Icons.Outlined.FilterBAndW, R.string.tool_bnw),
    Triple(EditorTool.NOIR, Icons.Outlined.Contrast, R.string.tool_noir),
    Triple(EditorTool.PORTRAIT, Icons.Outlined.Face, R.string.tool_portrait),
    Triple(EditorTool.HEAD_POSE, Icons.Outlined.FaceRetouchingNatural, R.string.tool_head_pose),
    Triple(EditorTool.LENS_BLUR, Icons.Outlined.BlurOn, R.string.tool_lens_blur),
    Triple(EditorTool.VIGNETTE, Icons.Outlined.Vignette, R.string.tool_vignette),
    Triple(EditorTool.DOUBLE_EXPO, Icons.Outlined.Layers, R.string.tool_double_expo),
    Triple(EditorTool.TEXT, Icons.Outlined.TextFields, R.string.tool_text),
    Triple(EditorTool.FRAME, Icons.Outlined.CropFree, R.string.tool_frame)
)

/** Barra de favoritos personalizable: iconos de las herramientas elegidas + acceso al editor de la barra. */
@Composable
fun FavoritesBar(
    favorites: List<String>,
    onSelect: (EditorTool) -> Unit,
    onEdit: () -> Unit
) {
    if (favorites.isEmpty()) return
    val catalog = toolCatalog()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        favorites.forEach { id ->
            val item = catalog.find { it.first.id == id } ?: return@forEach
            IconButton(onClick = { onSelect(item.first) }, modifier = Modifier.size(50.dp)) {
                Icon(item.second, stringResource(item.third), tint = Ui.icon, modifier = Modifier.size(38.dp))
            }
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(50.dp)) {
            Icon(Icons.Outlined.Edit, stringResource(R.string.customize_toolbar), tint = Ui.textDim, modifier = Modifier.size(34.dp))
        }
    }
}

/** Panel de herramientas (overlay inferior animado; la foto queda visible detras del velo). */
@Composable
fun ToolsGrid(
    modifier: Modifier = Modifier,
    favorites: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onSelect: (EditorTool) -> Unit
) {
    val tools = toolCatalog()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(Ui.sheet)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 10.dp)
        ) {
            tools.chunked(5).forEach { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowTools.forEach { (tool, icon, label) ->
                        val fav = tool.id in favorites
                        Box(modifier = Modifier.weight(1f)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelect(tool) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    icon, stringResource(label),
                                    tint = Ui.icon,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    stringResource(label),
                                    color = Ui.textDim,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(tool.id) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(26.dp)
                            ) {
                                Icon(
                                    if (fav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (fav) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                                    tint = if (fav) Ui.accent else Ui.line,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/** Carrusel de estilos con miniaturas de la foto real filtrada en vivo. */
@Composable
fun LooksCarousel(
    thumbs: Map<String, ImageBitmap>,
    selectedId: String,
    onSelect: (Look) -> Unit,
    customStyles: List<CustomStyle> = emptyList(),
    customThumbs: Map<String, ImageBitmap> = emptyMap(),
    selectedCustomId: String? = null,
    onSelectCustom: (CustomStyle) -> Unit = {},
    onDeleteCustom: (CustomStyle) -> Unit = {},
    onManageCustom: (CustomStyle) -> Unit = {},
    onSaveNew: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ui.bg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Look.entries.forEach { look ->
            val sel = look.id == selectedId
            // look_thumbnail_state (patron Snapseed): transicion animada del estado seleccionado
            val scale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (sel) 1.14f else 1f,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "lookScale"
            )
            val borderW by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (sel) 3.dp else 1.dp,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "lookBorderW"
            )
            val borderA by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (sel) 1f else 0.35f,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "lookBorderA"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(look) }
                    .padding(4.dp)
            ) {
                val th = thumbs[look.id]
                if (th != null) {
                    Image(
                        bitmap = th,
                        contentDescription = stringResource(look.labelRes),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = borderW,
                                color = Ui.text.copy(alpha = borderA),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Ui.panel, RoundedCornerShape(8.dp))
                    )
                }
                Text(
                    stringResource(look.labelRes),
                    color = if (sel) Ui.text else Ui.textDim,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        customStyles.forEach { cs ->
            val sel = cs.id == selectedCustomId
            val scale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (sel) 1.14f else 1f,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "csScale"
            )
            val borderW by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (sel) 3.dp else 1.dp,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "csBorderW"
            )
            val borderA by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (sel) 1f else 0.35f,
                animationSpec = androidx.compose.animation.core.tween(180, easing = FastOutSlowInEasing),
                label = "csBorderA"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .combinedClickable(
                        onClick = { onSelectCustom(cs) },
                        onLongClick = { onManageCustom(cs) }
                    )
                    .padding(4.dp)
            ) {
                Box {
                    val th = customThumbs[cs.id]
                    if (th != null) {
                        Image(
                            bitmap = th,
                            contentDescription = cs.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = borderW,
                                    color = Ui.accent.copy(alpha = borderA),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Ui.panel, RoundedCornerShape(8.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(18.dp)
                            .background(Color(0xCC101014), RoundedCornerShape(9.dp))
                            .clickable { onDeleteCustom(cs) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("x", color = Ui.text, fontSize = 11.sp)
                    }
                }
                Text(
                    cs.name,
                    color = if (sel) Ui.accent else Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSaveNew() }
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Ui.panel, RoundedCornerShape(8.dp))
                    .border(1.dp, Ui.line, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Ui.text, fontSize = 26.sp)
            }
            Text(
                stringResource(R.string.save),
                color = Ui.textDim,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Histograma de luminancia (48 tramos) dibujado como barras blancas translucidas. */
@Composable
fun HistogramView(bins: IntArray, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val max = (bins.maxOrNull() ?: 1).coerceAtLeast(1)
        val bw = size.width / bins.size
        bins.forEachIndexed { i, v ->
            val bh = (v.toFloat() / max) * size.height
            if (bh > 0f) {
                drawRect(
                    Color.White.copy(alpha = 0.55f),
                    topLeft = Offset(i * bw, size.height - bh),
                    size = Size(kotlin.math.max(bw - 1f, 1f), bh)
                )
            }
        }
    }
}

/** Deslizador de una zona tonal (-1..1) con etiqueta y valor legible. */
@Composable
fun TonalSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, color = Ui.text, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (value >= 0f) "+${"%.1f".format(value)}" else "%.1f".format(value),
                color = Ui.textDim,
                fontSize = 12.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = -1f..1f
        )
    }
}

/** Colores disponibles para Texto y Marcos (ARGB como Long). */
private val textColorChips = listOf(
    R.string.white to 0xFFFFFFFFL,
    R.string.black to 0xFF000000L,
    R.string.red to 0xFFFF0000L,
    R.string.yellow to 0xFFFFFF00L
)

/** Panel de intensidad 0..1 generico (HDR, grano, brillo, desenfoque y presets). */
@Composable
fun StrengthPanel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ui.panel)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("$label: ${(value * 100).toInt()}%", color = Ui.text, fontSize = 13.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
            Button(onClick = onApply) { Text(stringResource(R.string.apply)) }
        }
    }
}
