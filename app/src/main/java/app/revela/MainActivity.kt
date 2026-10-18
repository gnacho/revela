package app.revela

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
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalDensity
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
import app.revela.render.ExportOptions
import app.revela.render.ExportProcessor
import app.revela.render.PreviewRender
import app.revela.render.SessionIndexStore
import app.revela.render.SessionStore
import app.revela.core.AutoLevels
import app.revela.core.CustomStyle
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

enum class EditorTool(val id: String, val label: String) {
    TUNE("tune", "Ajustar imagen"),
    CURVES(Tool.CURVES, "Curvas"),
    TONE(Tool.TONAL, "Contraste tonal"),
    VIGNETTE(Tool.VIGNETTE, "Vineta"),
    HEAL(Tool.HEAL, "Quitamanchas"),
    CROP(Tool.CROP, "Recortar"),
    ROTATE("rotate", "Girar"),
    PERSPECTIVE(Tool.PERSPECTIVE, "Perspectiva"),
    EXPAND(Tool.EXPAND, "Expandir"),
    SELECTIVE(Tool.SELECTIVE, "Selectivo"),
    BRUSH(Tool.BRUSH, "Pincel"),
    HEAD_POSE(Tool.HEAD_POSE, "Pose"),
    BNW(Tool.BNW, "Blanco y negro"),
    WB(Tool.WHITE_BALANCE, "Balance blancos"),
    DETAILS(Tool.DETAILS, "Detalles"),
    HDR(Tool.HDR, "HDR-scape"),
    GRAIN(Tool.GRAIN, "Grano"),
    GLOW(Tool.GLOW, "Brillo glamuroso"),
    LENS_BLUR(Tool.LENS_BLUR, "Desenfoque"),
    DRAMA("drama", "Drama"),
    VINTAGE("vintage", "Vintage"),
    GRAINY_FILM("grainyFilm", "Pelicula granulada"),
    RETROLUX("retrolux", "Retrolux"),
    GRUNGE("grunge", "Grunge"),
    NOIR("noir", "Noir"),
    PORTRAIT("portrait", "Retrato"),
    DOUBLE_EXPO(Tool.DOUBLE_EXPO, "Doble exposicion"),
    TEXT(Tool.TEXT, "Texto"),
    FRAME(Tool.FRAME, "Marcos")
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

/** Pestañas inferiores estilo Snapseed. */
enum class BottomTab { NONE, LOOKS, TOOLS }

/** Parametros de la herramienta Ajustar (gesto vertical para elegir, horizontal para valor). */
enum class TuneParam(val toolId: String, val wbParam: WbParam, val label: String) {
    EXPOSURE(Tool.EXPOSURE, WbParam.TEMPERATURE, "Exposicion"),
    CONTRAST(Tool.CONTRAST, WbParam.TEMPERATURE, "Contraste"),
    SATURATION(Tool.SATURATION, WbParam.TEMPERATURE, "Saturacion"),
    TEMPERATURE(Tool.WHITE_BALANCE, WbParam.TEMPERATURE, "Temperatura"),
    TINT(Tool.WHITE_BALANCE, WbParam.TINT, "Tinte")
}

enum class SelParam(val label: String) {
    BRIGHTNESS("Brillo"), CONTRAST("Contraste"), SATURATION("Saturacion")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    var screen by remember { mutableStateOf("home") }
                    var startUri by remember { mutableStateOf<Uri?>(null) }
                    var startSessionId by remember { mutableStateOf<String?>(null) }
                    var homeTick by remember { mutableIntStateOf(0) }
                    var showWelcome by remember {
                        mutableStateOf(!WelcomeStore.isSeen(context.filesDir))
                    }

                    Box {
                        when (screen) {
                            "home" -> HomeScreen(
                                refreshTick = homeTick,
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
                            else -> EditorScreen(
                                initialUri = startUri,
                                initialSessionId = startSessionId,
                                onHome = {
                                    homeTick++
                                    screen = "home"
                                }
                            )
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
    onHome: () -> Unit = {}
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
    var cropDrawMode by remember { mutableStateOf(false) }
    var viewScale by remember { mutableFloatStateOf(1f) }
    var viewOffset by remember { mutableStateOf(Offset.Zero) }

    // Navegacion estilo Snapseed
    var bottomTab by remember { mutableStateOf(BottomTab.NONE) }
    var tuneParam by remember { mutableStateOf(TuneParam.EXPOSURE) }
    var paramMenuVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRevertConfirm by remember { mutableStateOf(false) }
    var lookThumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }

    // Estilos personalizados
    var customStyles by remember {
        mutableStateOf(app.revela.render.CustomStylesStore.load(context))
    }
    var customThumbs by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    var showStyleNameDialog by remember { mutableStateOf(false) }
    var styleName by remember { mutableStateOf("") }
    var deleteStyle by remember { mutableStateOf<CustomStyle?>(null) }
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
        SessionStore.save(context, u, stack, geometry)
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
        return list
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
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
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
            Toast.makeText(context, "No se pudo abrir la foto", Toast.LENGTH_SHORT).show()
            onHome()
            return@LaunchedEffect
        }
        base = loaded
        originalBitmap = loaded.toBitmap()
        sourceUri = target
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
                .background(Color.Black)
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
            }) { Icon(Icons.Filled.Home, "Biblioteca", tint = Color.White) }
            IconButton(onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.AddPhotoAlternate, "Abrir foto", tint = Color.White)
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
                ) { Icon(Icons.AutoMirrored.Filled.Undo, "Deshacer", tint = Color.White) }
                IconButton(
                    onClick = {
                        if (stack.redo()) {
                            stackVersion++
                            persistSession()
                        }
                    },
                    enabled = stack.canRedo
                ) { Icon(Icons.AutoMirrored.Filled.Redo, "Rehacer", tint = Color.White) }
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
                ) { Icon(Icons.Filled.Visibility, "Comparar con el original", tint = Color.White) }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        enabled = sourceUri != null
                    ) { Icon(Icons.Filled.MoreVert, "Menu", tint = Color.White) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Revertir todo") },
                            onClick = {
                                showMenu = false
                                showRevertConfirm = true
                            },
                            enabled = stack.edits.isNotEmpty() || geometry != GeometryModel()
                        )
                    }
                }
            } else {
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
                }) { Icon(Icons.Filled.Close, "Cerrar herramienta", tint = Color.White) }
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
                }) { Icon(Icons.Filled.Check, "Listo", tint = Color.White) }
            }
        }
        if (activeTool == null && base != null) {
            FavoritesBar(
                favorites = favorites,
                onSelect = {
                    activeTool = it
                    bottomTab = BottomTab.NONE
                }
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val viewportWpx = constraints.maxWidth.toFloat()
            val viewportHpx = constraints.maxHeight.toFloat()

            val b = base
            val bmp = if (showOriginal) originalBitmap else preview
            if (b == null || bmp == null) {
                Button(onClick = {
                    pickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Abrir foto") }
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
                                mod.pointerInput(b) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        viewScale = (viewScale * zoom).coerceIn(1f, 6f)
                                        viewOffset += pan
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
                                            p.label,
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

                // Velo oscuro sobre la foto mientras el panel esta abierto (la foto sigue visible)
                val toolsOpen = activeTool == null && bottomTab == BottomTab.TOOLS && base != null
                androidx.compose.animation.AnimatedVisibility(
                    visible = toolsOpen,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)),
                    modifier = Modifier.matchParentSize()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .pointerInput(Unit) {
                                detectTapGestures { bottomTab = BottomTab.NONE }
                            }
                    )
                }
                // Panel de herramientas: sube desde abajo ~250 ms (patron Snapseed medido)
                androidx.compose.animation.AnimatedVisibility(
                    visible = toolsOpen,
                    enter = slideInVertically(
                        animationSpec = tween(250, easing = FastOutSlowInEasing),
                        initialOffsetY = { it }
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        targetOffsetY = { it }
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ToolsGrid(
                        modifier = Modifier.fillMaxWidth(),
                        favorites = favorites.toSet(),
                        onToggleFavorite = { toggleFavorite(it) },
                        onSelect = {
                            activeTool = it
                            bottomTab = BottomTab.NONE
                        }
                    )
                }
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
                        label = tuneParam.label
                        range = EffectiveAdjustments.rangeFor(tuneParam.toolId)
                        shown = if (dragValue.isNaN()) {
                            EffectiveAdjustments.valueOf(effective(), tuneParam.toolId, tuneParam.wbParam)
                        } else dragValue
                    }
                    EditorTool.SELECTIVE -> {
                        label = selParam.label
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
                        label = "Viñeta"
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
                        Text(label, color = Color.Gray, fontSize = 11.sp)
                        Text(valueText, color = Color.White, fontSize = 12.sp)
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
            // Ventana de imagen (siempre visible) + overlay de herramientas (patron Snapseed:
            }
            // Carrusel de estilos como overlay inferior (la foto no cambia de tamano)
            if (bottomTab == BottomTab.LOOKS && activeTool == null && base != null) {
                Box(Modifier.align(Alignment.BottomCenter)) {
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
                                            onSaveNew = {
                                                styleName = ""
                                                showStyleNameDialog = true
                                            }
                                        )
                }
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
                                        .background(Color(0xFF101014))
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
                                                label = { Text(p.label, fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Toca para anadir punto (max 8) - arrastra el punto para moverlo - pulsacion larga para borrar - desliza en horizontal para ajustar",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                EditorTool.VIGNETTE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    val currentV = if (workingVignette.isNaN()) {
                                        stack.edits.filterIsInstance<Edit.Vignette>().firstOrNull()?.strength ?: 0f
                                    } else workingVignette
                                    Text(
                                        "Intensidad: ${(currentV * 100).toInt()}%",
                                        color = Color.White,
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
                                        }) { Text("Quitar") }
                                        Button(onClick = {
                                            val v = if (workingVignette.isNaN()) currentV else workingVignette
                                            stack.apply(Edit.Vignette(v))
                                            stackVersion++
                                            workingVignette = Float.NaN
                                            persistSession()
                                            activeTool = null
                                        }) { Text("Aplicar") }
                                    }
                                }

                                EditorTool.HEAL -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Brocha: ${(brushSize * 100).toInt()}%",
                                        color = Color.White,
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
                                        ) { Text("Sanar zona") }
                                        OutlinedButton(onClick = {
                                            strokeX.clear()
                                            strokeY.clear()
                                        }) { Text("Descartar trazo") }
                                    }
                                }

                                EditorTool.PERSPECTIVE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Perspectiva: arrastra los puntos azules sobre la foto",
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        listOf(
                                            "free" to "Libre", "scale" to "Escala",
                                            "tilt" to "Inclinar", "rotate" to "Rotar"
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
                                                label = { Text(label) }
                                            )
                                        }
                                    }
                                    if (perspMode == "rotate") {
                                        Text(
                                            "Horizontal ${perspYaw.toInt()} grados",
                                            color = Color.White, fontSize = 12.sp
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
                                            "Vertical ${perspPitch.toInt()} grados",
                                            color = Color.White, fontSize = 12.sp
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
                                            "Rotacion ${perspRoll.toInt()} grados",
                                            color = Color.White, fontSize = 12.sp
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
                                    }) { Text("Restablecer") }
                                }

                                EditorTool.EXPAND -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Expandir: arrastra los puntos azules hacia fuera para ampliar el lienzo",
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        FilterChip(
                                            selected = !expandMirror,
                                            onClick = { expandMirror = false; workingEdit = Edit.Expand(expandEdges, false) },
                                            label = { Text("Relleno borde") }
                                        )
                                        FilterChip(
                                            selected = expandMirror,
                                            onClick = { expandMirror = true; workingEdit = Edit.Expand(expandEdges, true) },
                                            label = { Text("Relleno espejo") }
                                        )
                                    }
                                    OutlinedButton(onClick = {
                                        expandEdges = app.revela.core.ExpandEdges()
                                        workingEdit = Edit.Expand(expandEdges, expandMirror)
                                    }) { Text("Restablecer") }
                                }

                                EditorTool.BRUSH -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Brocha: ${(brushSize * 100).toInt()}%  |  Intensidad: ${(brushAmount * 100).toInt()}%",
                                        color = Color.White,
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
                                                label = { Text(label) }
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
                                        ) { Text("Aplicar trazo") }
                                        OutlinedButton(onClick = {
                                            strokeX.clear()
                                            strokeY.clear()
                                        }) { Text("Descartar") }
                                    }
                                }

                                EditorTool.HEAD_POSE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        when (hpStep) {
                                            0 -> "Paso 1: toca el ojo izquierdo"
                                            1 -> "Paso 2: toca el ojo derecho"
                                            2 -> "Paso 3: toca la boca"
                                            else -> "Ajusta el giro o arrastra los puntos azules"
                                        },
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Giro horizontal ${hpYawDeg.toInt()} grados",
                                        color = Color.White, fontSize = 12.sp
                                    )
                                    Slider(
                                        value = hpYawDeg,
                                        onValueChange = { hpYawDeg = it; pushHeadPose() },
                                        valueRange = -30f..30f
                                    )
                                    Text(
                                        "Giro vertical ${hpPitchDeg.toInt()} grados",
                                        color = Color.White, fontSize = 12.sp
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
                                        }) { Text("Reiniciar puntos") }
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
                                        .background(Color(0xFF101014))
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
                                                contentDescription = "Auto",
                                                tint = Color.White
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
                                            Text("Auto ajustado", color = Color.White, fontSize = 13.sp)
                                            Spacer(Modifier.weight(1f))
                                            TextButton(onClick = { undoAuto() }) { Text("Deshacer") }
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
                                                label = { Text(p.label, fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Arrastra en vertical para elegir parametro - en horizontal para ajustar",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                EditorTool.ROTATE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        OutlinedButton(onClick = { rotateBy(-1) }) {
                                            Icon(Icons.Filled.RotateLeft, contentDescription = null, tint = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Text("90 izq", color = Color.White)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        OutlinedButton(onClick = { rotateBy(1) }) {
                                            Icon(Icons.Filled.RotateRight, contentDescription = null, tint = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Text("90 der", color = Color.White)
                                        }
                                    }
                                    Text(
                                        "Gira el marco en cuartos de vuelta. El enderece fino esta en Recortar.",
                                        color = Color.Gray,
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
                                            .background(Color(0xFF101014))
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            listOf(
                                                "neutral" to "Neutro",
                                                "red" to "Rojo",
                                                "yellow" to "Amarillo",
                                                "green" to "Verde",
                                                "blue" to "Azul"
                                            ).forEach { (id, label) ->
                                                FilterChip(
                                                    selected = current == id,
                                                    onClick = {
                                                        stack.apply(Edit.Bnw(id))
                                                        stackVersion++
                                                        persistSession()
                                                    },
                                                    label = { Text(label, fontSize = 12.sp) },
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
                                                label = { Text("Color", fontSize = 12.sp) },
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                        Text(
                                            "Cada filtro pondera los canales al pasar a blanco y negro.",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                EditorTool.TONE -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    TonalSlider("Sombras", effective().tonalShadows) { v ->
                                        workingAdj = effective().copy(tonalShadows = v)
                                    }
                                    TonalSlider("Medios", effective().tonalMidtones) { v ->
                                        workingAdj = effective().copy(tonalMidtones = v)
                                    }
                                    TonalSlider("Luces", effective().tonalHighlights) { v ->
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
                                        }) { Text("Restablecer", color = Color.Gray) }
                                        Spacer(Modifier.width(16.dp))
                                        Button(onClick = {
                                            commitTonal()
                                            activeTool = null
                                        }) { Text("Aplicar") }
                                    }
                                }

                                EditorTool.WB -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101014))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    TonalSlider("Temperatura", effective().temperature) { v ->
                                        workingAdj = effective().copy(temperature = v)
                                    }
                                    TonalSlider("Tinte", effective().tint) { v ->
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
                                        }) { Text("Restablecer", color = Color.Gray) }
                                        Spacer(Modifier.width(16.dp))
                                        Button(onClick = {
                                            val cur = effective()
                                            stack.apply(
                                                Edit.WhiteBalance(temperature = cur.temperature, tint = cur.tint)
                                            )
                                            stackVersion++
                                            workingAdj = null
                                            persistSession()
                                        }) { Text("Aplicar") }
                                    }
                                }

                                EditorTool.DETAILS -> {
                                    val cur = (workingEdit as? Edit.Details)
                                        ?: stack.edits.filterIsInstance<Edit.Details>().firstOrNull()
                                        ?: Edit.Details()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF101014))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        TonalSlider("Estructura", cur.structure) { v ->
                                            workingEdit = cur.copy(structure = v)
                                        }
                                        TonalSlider("Enfoque", cur.sharpening) { v ->
                                            workingEdit = cur.copy(sharpening = v)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.DETAILS) }) { Text("Restablecer") }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.Details ?: cur)
                                                activeTool = null
                                            }) { Text("Aplicar") }
                                        }
                                    }
                                }

                                EditorTool.HDR -> {
                                    val cur = (workingEdit as? Edit.Hdr)?.strength
                                        ?: stack.edits.filterIsInstance<Edit.Hdr>().firstOrNull()?.strength ?: 0f
                                    StrengthPanel(
                                        label = "Intensidad",
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
                                        label = "Intensidad",
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
                                        label = "Intensidad",
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
                                        label = "Intensidad",
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
                                            label = kind.label,
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
                                            .background(Color(0xFF101014))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            listOf(
                                                "normal" to "Normal",
                                                "add" to "Suma",
                                                "overlay" to "Superponer",
                                                "subtract" to "Resta",
                                                "screen" to "Pantalla",
                                                "multiply" to "Multiplicar",
                                                "lighten" to "Aclarar",
                                                "darken" to "Oscurecer"
                                            ).forEach { (id, label) ->
                                                FilterChip(
                                                    selected = cur.blend == id,
                                                    onClick = { workingEdit = cur.copy(blend = id) },
                                                    label = { Text(label, fontSize = 12.sp) },
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
                                            }) { Text("2a foto") }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (overlayImage != null) "Cargada" else "Sin imagen",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text("Opacidad: ${(cur.opacity * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = cur.opacity,
                                            onValueChange = { workingEdit = cur.copy(opacity = it) },
                                            valueRange = 0f..1f
                                        )
                                        Text(
                                            "Escala capa: ${(cur.scale * 100).toInt()}%  |  Rotacion: ${cur.rotationDeg.toInt()} grados",
                                            color = Color.White, fontSize = 13.sp
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
                                            "Arrastra la foto para mover la 2a capa",
                                            color = Color(0xFF9E9E9E), fontSize = 12.sp
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = {
                                                resetTool(Tool.DOUBLE_EXPO)
                                                overlayImage = null
                                            }) { Text("Restablecer") }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.DoubleExpo ?: cur)
                                                activeTool = null
                                            }) { Text("Aplicar") }
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
                                            .background(Color(0xFF101014))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cur.content,
                                            onValueChange = { workingEdit = cur.copy(content = it) },
                                            label = { Text("Texto") },
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
                                                    label = { Text(label, fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        Text("Tamano: ${(cur.sizePct * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
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
                                            listOf("top" to "Arriba", "center" to "Centro", "bottom" to "Abajo")
                                                .forEach { (id, label) ->
                                                    FilterChip(
                                                        selected = cur.gravity == id,
                                                        onClick = { workingEdit = cur.copy(gravity = id) },
                                                        label = { Text(label, fontSize = 12.sp) },
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.TEXT) }) { Text("Restablecer") }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.TextOverlay ?: cur)
                                                activeTool = null
                                            }) { Text("Aplicar") }
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
                                            .background(Color(0xFF101014))
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
                                                    label = { Text(label, fontSize = 12.sp) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        Text("Grosor: ${(cur.widthPct * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = cur.widthPct,
                                            onValueChange = { workingEdit = cur.copy(widthPct = it) },
                                            valueRange = 0.01f..0.25f
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { resetTool(Tool.FRAME) }) { Text("Restablecer") }
                                            Button(onClick = {
                                                commitWorkingEdit(workingEdit as? Edit.Frame ?: cur)
                                                activeTool = null
                                            }) { Text("Aplicar") }
                                        }
                                    }
                                }

                                else -> {}

                            }
                }
            }
        }
        // Barra de pestañas fija (la foto nunca cambia de altura al abrir paneles)
        if (activeTool == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF101014))
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
            title = { Text("Revertir todo") },
            text = { Text("Se eliminaran todas las ediciones de esta foto.") },
            confirmButton = {
                Button(onClick = {
                    stack = EditStack()
                    geometry = GeometryModel()
                    stackVersion++
                    persistSession()
                    showRevertConfirm = false
                }) { Text("Revertir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRevertConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showStyleNameDialog) {
        AlertDialog(
            onDismissRequest = { showStyleNameDialog = false },
            title = { Text("Guardar estilo") },
            text = {
                Column {
                    Text("Guarda los ajustes actuales como estilo reutilizable.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = styleName,
                        onValueChange = { styleName = it },
                        label = { Text("Nombre") },
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
                ) { Text("Guardar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStyleNameDialog = false }) { Text("Cancelar") }
            }
        )
    }

    deleteStyle?.let { cs ->
        AlertDialog(
            onDismissRequest = { deleteStyle = null },
            title = { Text("Borrar estilo") },
            text = { Text("Se eliminara el estilo \"${cs.name}\". La foto no se toca.") },
            confirmButton = {
                Button(onClick = {
                    customStyles = customStyles.filterNot { it.id == cs.id }
                    app.revela.render.CustomStylesStore.save(context, customStyles)
                    if (selectedCustomId == cs.id) selectedCustomId = null
                    deleteStyle = null
                }) { Text("Borrar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteStyle = null }) { Text("Cancelar") }
            }
        )
    }

    if (sourceUri != null && showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { options ->
                showExportDialog = false
                scope.launch {
                    val result = ExportProcessor.export(
                        context, sourceUri!!, stack, geometry, options, overlayImage
                    )
                    result.onFailure {
                        android.util.Log.e("revela", "export fallo", it)
                    }
                    Toast.makeText(
                        context,
                        if (result.isSuccess) "Guardado en Imagenes/revela"
                        else "Error al exportar: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
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
            .background(Color(0xFF101014))
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
            }) { Text("Restablecer") }
            Button(onClick = { onCommit(points) }) { Text("Aplicar") }
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
            .background(Color(0xFF1A1A20))
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
                drawLine(Color(0xFF333340), Offset(s * f, 0f), Offset(s * f, s))
                drawLine(Color(0xFF333340), Offset(0f, s * f), Offset(s, s * f))
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
            .background(Color(0xFF101014))
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
                label = { Text("Ajustar caja", fontSize = 12.sp) }
            )
            FilterChip(
                selected = drawMode,
                onClick = { onDrawModeChange(true) },
                label = { Text("Dibujar seleccion", fontSize = 12.sp) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            listOf(
                "Libre" to null,
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
            }) { Icon(Icons.Filled.RotateLeft, "Rotar a la izquierda", tint = Color.White) }
            IconButton(onClick = {
                onChange(
                    geometry.copy(
                        quarterTurns = geometry.quarterTurns + 1,
                        straightenDeg = 0f,
                        cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f
                    )
                )
            }) { Icon(Icons.Filled.RotateRight, "Rotar a la derecha", tint = Color.White) }
            IconButton(onClick = { onChange(GeometryModel()) }) {
                Icon(Icons.Filled.RestartAlt, "Restablecer geometria", tint = Color.White)
            }
            Button(onClick = onCommit) { Text("OK") }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enderezar", color = Color.White, fontSize = 13.sp)
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
    onDismiss: () -> Unit,
    onExport: (ExportOptions) -> Unit
) {
    var png by remember { mutableStateOf(false) }
    var quality by remember { mutableIntStateOf(92) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar imagen") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !png, onClick = { png = false }, label = { Text("JPEG") })
                    FilterChip(selected = png, onClick = { png = true }, label = { Text("PNG") })
                }
                if (!png) {
                    Text("Calidad: $quality", fontSize = 13.sp)
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 60f..100f
                    )
                }
                Text(
                    "Se guarda en Imagenes/revela (max 4096 px de lado).",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(onClick = { onExport(ExportOptions(png = png, quality = quality)) }) {
                Text("Exportar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/** Barra inferior de 3 pestañas estilo Snapseed: Estilos | Herramientas | Exportar. */
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
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabButton(
            "Estilos", selected == BottomTab.LOOKS, enabled, Modifier.weight(1f)
        ) { onSelect(if (selected == BottomTab.LOOKS) BottomTab.NONE else BottomTab.LOOKS) }
        TabButton(
            "Herramientas", selected == BottomTab.TOOLS, enabled, Modifier.weight(1f)
        ) { onSelect(if (selected == BottomTab.TOOLS) BottomTab.NONE else BottomTab.TOOLS) }
        TabButton("Exportar", false, enabled, Modifier.weight(1f)) { onExport() }
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
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(
            label,
            color = if (selected) Color.White else Color(0xFF9E9E9E),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** Catalogo completo de herramientas (28, orden Snapseed) compartido por rejilla y barra de favoritos. */
fun toolCatalog(): List<Triple<EditorTool, androidx.compose.ui.graphics.vector.ImageVector, String>> = listOf(
    Triple(EditorTool.TUNE, Icons.Filled.Tune, "Ajustar imagen"),
    Triple(EditorTool.DETAILS, Icons.Filled.Details, "Detalles"),
    Triple(EditorTool.CURVES, Icons.Filled.ShowChart, "Curvas"),
    Triple(EditorTool.WB, Icons.Filled.WbSunny, "Balance blancos"),
    Triple(EditorTool.CROP, Icons.Filled.Crop, "Recortar"),
    Triple(EditorTool.ROTATE, Icons.Filled.RotateRight, "Girar"),
    Triple(EditorTool.PERSPECTIVE, Icons.Filled.Transform, "Perspectiva"),
    Triple(EditorTool.EXPAND, Icons.Filled.AspectRatio, "Expandir"),
    Triple(EditorTool.SELECTIVE, Icons.Filled.CenterFocusStrong, "Selectivo"),
    Triple(EditorTool.BRUSH, Icons.Filled.Brush, "Pincel"),
    Triple(EditorTool.HEAL, Icons.Filled.Healing, "Quitamanchas"),
    Triple(EditorTool.HDR, Icons.Filled.HdrStrong, "HDR-scape"),
    Triple(EditorTool.GLOW, Icons.Filled.AutoAwesome, "Brillo glamuroso"),
    Triple(EditorTool.TONE, Icons.Filled.Tonality, "Contraste tonal"),
    Triple(EditorTool.DRAMA, Icons.Filled.Bolt, "Drama"),
    Triple(EditorTool.VINTAGE, Icons.Filled.PhotoCamera, "Vintage"),
    Triple(EditorTool.GRAINY_FILM, Icons.Filled.Movie, "Pelicula granulada"),
    Triple(EditorTool.RETROLUX, Icons.Filled.Flare, "Retrolux"),
    Triple(EditorTool.GRUNGE, Icons.Filled.Texture, "Grunge"),
    Triple(EditorTool.BNW, Icons.Filled.FilterBAndW, "B/N"),
    Triple(EditorTool.NOIR, Icons.Filled.Contrast, "Noir"),
    Triple(EditorTool.PORTRAIT, Icons.Filled.Face, "Retrato"),
    Triple(EditorTool.HEAD_POSE, Icons.Filled.FaceRetouchingNatural, "Pose"),
    Triple(EditorTool.LENS_BLUR, Icons.Filled.BlurOn, "Desenfoque"),
    Triple(EditorTool.VIGNETTE, Icons.Filled.Vignette, "Vineta"),
    Triple(EditorTool.DOUBLE_EXPO, Icons.Filled.Layers, "Doble exposicion"),
    Triple(EditorTool.TEXT, Icons.Filled.TextFields, "Texto"),
    Triple(EditorTool.FRAME, Icons.Filled.CropFree, "Marcos")
)

/** Barra de favoritos personalizable: iconos de las herramientas elegidas, siempre bajo la barra superior. */
@Composable
fun FavoritesBar(
    favorites: List<String>,
    onSelect: (EditorTool) -> Unit
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
            IconButton(onClick = { onSelect(item.first) }, modifier = Modifier.size(44.dp)) {
                Icon(item.second, item.third, tint = Color.White)
            }
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
            .fillMaxHeight(0.72f)
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(Color(0xFF101014))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            tools.chunked(3).forEach { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowTools.forEach { (tool, icon, label) ->
                        Card(
                            onClick = { onSelect(tool) },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C))
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(icon, label, tint = Color.White)
                                    Text(
                                        label,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                val fav = tool.id in favorites
                                IconButton(
                                    onClick = { onToggleFavorite(tool.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        if (fav) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = if (fav) "Quitar de favoritos" else "Anadir a favoritos",
                                        tint = if (fav) Color(0xFFFFD54F) else Color(0xFF8A8A8A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
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
    onSaveNew: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Look.entries.forEach { look ->
            val sel = look.id == selectedId
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
                        contentDescription = look.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (sel) 2.dp else 1.dp,
                                color = if (sel) Color.White else Color(0xFF333340),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF1A1A20), RoundedCornerShape(8.dp))
                    )
                }
                Text(
                    look.label,
                    color = if (sel) Color.White else Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        customStyles.forEach { cs ->
            val sel = cs.id == selectedCustomId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectCustom(cs) }
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
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (sel) 2.dp else 1.dp,
                                    color = if (sel) Color(0xFFB388FF) else Color(0xFF333340),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF1A1A20), RoundedCornerShape(8.dp))
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
                        Text("x", color = Color.White, fontSize = 11.sp)
                    }
                }
                Text(
                    cs.name,
                    color = if (sel) Color(0xFFB388FF) else Color.Gray,
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
                    .background(Color(0xFF1A1A20), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF333340), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 26.sp)
            }
            Text(
                "Guardar",
                color = Color.Gray,
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
            Text(label, color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (value >= 0f) "+${"%.1f".format(value)}" else "%.1f".format(value),
                color = Color.Gray,
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
    "Blanco" to 0xFFFFFFFFL,
    "Negro" to 0xFF000000L,
    "Rojo" to 0xFFFF0000L,
    "Amarillo" to 0xFFFFFF00L
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
            .background(Color(0xFF101014))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("$label: ${(value * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onReset) { Text("Restablecer") }
            Button(onClick = onApply) { Text("Aplicar") }
        }
    }
}
