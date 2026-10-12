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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revela.core.CurvePoint
import app.revela.core.Edit
import app.revela.core.EditStack
import app.revela.core.GeometryModel
import app.revela.core.Tool
import app.revela.render.BaseImage
import app.revela.render.EffectiveAdjustments
import app.revela.render.ExportOptions
import app.revela.render.ExportProcessor
import app.revela.render.PreviewRender
import app.revela.render.SessionStore
import app.revela.render.WbParam
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class EditorTool(val id: String, val label: String) {
    EXPOSURE(Tool.EXPOSURE, "Exposicion"),
    CONTRAST(Tool.CONTRAST, "Contraste"),
    SATURATION(Tool.SATURATION, "Saturacion"),
    WHITE_BALANCE(Tool.WHITE_BALANCE, "Temperatura"),
    CURVES(Tool.CURVES, "Curvas"),
    CROP(Tool.CROP, "Recortar")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EditorScreen()
                }
            }
        }
    }
}

@Composable
fun EditorScreen() {
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
    var wbParam by remember { mutableStateOf(WbParam.TEMPERATURE) }
    var dragValue by remember { mutableFloatStateOf(Float.NaN) }
    var renderJob by remember { mutableStateOf<Job?>(null) }
    var workingGeometry by remember { mutableStateOf<GeometryModel?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val cropWorking = if (activeTool == EditorTool.CROP) (workingGeometry ?: geometry) else null

    fun effective(): app.revela.core.ColorMath.Adjustments {
        val tool = activeTool
        return EffectiveAdjustments.withWorking(
            EffectiveAdjustments.from(stack),
            tool?.id ?: "",
            workingAdj
        )
    }

    fun persistSession() {
        val u = sourceUri ?: return
        SessionStore.save(context, u, stack, geometry)
    }

    fun commitToolValue(value: Float) {
        val tool = activeTool ?: return
        val edit = EffectiveAdjustments.buildEdit(tool.id, effective(), wbParam, value) ?: return
        stack.apply(edit)
        stackVersion++
        workingAdj = null
        dragValue = Float.NaN
        persistSession()
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        android.util.Log.i("revela", "picker result uri=$uri")
        if (uri != null) {
            scope.launch {
                runCatching {
                    val loaded = PreviewRender.decode(context.contentResolver, uri)
                    android.util.Log.i(
                        "revela",
                        "decode ok=${loaded != null} ${loaded?.width}x${loaded?.height}"
                    )
                    base = loaded
                    originalBitmap = loaded?.toBitmap()
                    sourceUri = uri
                    activeTool = null
                    workingAdj = null
                    workingGeometry = null
                    val restored = loaded?.let { SessionStore.load(context, uri) }
                    stack = restored?.first ?: EditStack()
                    geometry = restored?.second ?: GeometryModel()
                    stackVersion++
                }.onFailure {
                    android.util.Log.e("revela", "error al abrir imagen", it)
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(base, stackVersion, workingAdj, activeTool, showOriginal, wbParam) {
        val b = base ?: return@LaunchedEffect
        renderJob?.cancel()
        val snapshotTool = activeTool
        val adj = EffectiveAdjustments.withWorking(
            EffectiveAdjustments.from(stack),
            snapshotTool?.id ?: "",
            workingAdj
        )
        renderJob = scope.launch(Dispatchers.Default) {
            val bmp = PreviewRender.render(b, adj, showOriginal)
            preview = bmp
        }
    }

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
                IconButton(
                    onClick = { activeTool = EditorTool.CROP },
                    enabled = base != null
                ) { Icon(Icons.Filled.RotateRight, "Recortar y rotar", tint = Color.White) }
                IconButton(
                    onClick = { showExportDialog = true },
                    enabled = sourceUri != null
                ) { Icon(Icons.Filled.Save, "Exportar", tint = Color.White) }
            } else {
                IconButton(onClick = {
                    activeTool = null
                    workingAdj = null
                    workingGeometry = null
                    dragValue = Float.NaN
                }) { Icon(Icons.Filled.Close, "Cerrar herramienta", tint = Color.White) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    if (activeTool == EditorTool.CROP) {
                        workingGeometry?.let {
                            geometry = it
                            persistSession()
                        }
                    }
                    activeTool = null
                    workingAdj = null
                    workingGeometry = null
                }) { Icon(Icons.Filled.Check, "Listo", tint = Color.White) }
            }
        }

        // Valor en curso durante el gesto
        val toolForValue = activeTool
        if (toolForValue != null && toolForValue != EditorTool.CROP && toolForValue != EditorTool.CURVES) {
            val shown = if (dragValue.isNaN()) {
                EffectiveAdjustments.valueOf(effective(), toolForValue.id, wbParam)
            } else {
                dragValue
            }
            Text(
                text = EffectiveAdjustments.formatValue(toolForValue.id, wbParam, shown),
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Ventana de imagen
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

                Box(
                    modifier = Modifier
                        .requiredSize(boxWdp, boxHdp)
                        .clipToBounds()
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .requiredSize(frameWdp, frameHdp)
                            .absoluteOffset(offsetXdp, offsetYdp)
                            .graphicsLayer {
                                rotationZ = g.totalRotationDeg
                                scaleX = cover
                                scaleY = cover
                            }
                    )

                    // Gestos de ajuste (herramientas de color)
                    val tool = activeTool
                    if (tool != null && tool != EditorTool.CROP && tool != EditorTool.CURVES) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(tool.id, wbParam) {
                                    val range = EffectiveAdjustments.rangeFor(tool.id)
                                    var start = 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            start = EffectiveAdjustments.valueOf(
                                                effective(), tool.id, wbParam
                                            )
                                        },
                                        onDragEnd = {
                                            if (!dragValue.isNaN()) commitToolValue(dragValue)
                                        },
                                        onDragCancel = { dragValue = Float.NaN }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val span = range.endInclusive - range.start
                                        if (dragValue.isNaN()) dragValue = start
                                        dragValue = (dragValue + dragAmount / (viewportWpx * 1.8f) * span)
                                            .coerceIn(range.start, range.endInclusive)
                                        workingAdj = makeWorking(tool.id, wbParam, dragValue)
                                    }
                                }
                        )
                    }

                    if (inCrop && cropWorking != null) {
                        CropOverlay(
                            modifier = Modifier.matchParentSize(),
                            geometry = cropWorking,
                            frameAspect = frameAspect,
                            onGeometry = { workingGeometry = it }
                        )
                    }
                }
            }
        }

        // Paneles inferiores
        when (activeTool) {
            null -> ToolRail(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                enabled = base != null,
                onSelect = { activeTool = it }
            )

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

            EditorTool.CROP -> {
                val g0 = cropWorking ?: geometry
                val baseAspect0 = base?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
                val odd0 = ((g0.quarterTurns % 4) + 4) % 4 % 2 == 1
                val frameAspect0 = if (odd0) 1f / baseAspect0 else baseAspect0
                // Caja inicial: 80% centrada (si la actual ocupa todo el marco)
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

            EditorTool.WHITE_BALANCE -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = wbParam == WbParam.TEMPERATURE,
                        onClick = { wbParam = WbParam.TEMPERATURE; dragValue = Float.NaN },
                        label = { Text("Temperatura") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = wbParam == WbParam.TINT,
                        onClick = { wbParam = WbParam.TINT; dragValue = Float.NaN },
                        label = { Text("Tinte") }
                    )
                }
                Text(
                    "Desliza horizontalmente sobre la foto",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> Text(
                "Desliza horizontalmente sobre la foto",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            )
        }
    }

    if (sourceUri != null && showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { options ->
                showExportDialog = false
                scope.launch {
                    val result = ExportProcessor.export(
                        context, sourceUri!!, stack, geometry, options
                    )
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

private fun makeWorking(
    toolId: String,
    wbParam: WbParam,
    value: Float
): app.revela.core.ColorMath.Adjustments {
    val base = app.revela.core.ColorMath.Adjustments()
    return when (toolId) {
        Tool.EXPOSURE -> base.copy(exposureStops = value)
        Tool.CONTRAST -> base.copy(contrast = value)
        Tool.SATURATION -> base.copy(saturation = value)
        Tool.WHITE_BALANCE ->
            if (wbParam == WbParam.TEMPERATURE) base.copy(temperature = value)
            else base.copy(tint = value)
        else -> base
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
fun ToolRail(modifier: Modifier = Modifier, enabled: Boolean, onSelect: (EditorTool) -> Unit) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        EditorTool.entries.forEach { tool ->
            OutlinedButton(
                onClick = { onSelect(tool) },
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(tool.label, fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
        }
    }
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

    // Si la fuente cambia (commit/reset), sincroniza
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
            horizontalArrangement = Arrangement.SpaceEvenly
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
            Text("Enderezar", color = Color.White, fontSize = 13.sp)
            Slider(
                value = geometry.straightenDeg,
                onValueChange = { onChange(geometry.copy(straightenDeg = it)) },
                valueRange = -45f..45f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            IconButton(onClick = { onChange(GeometryModel()) }) {
                Icon(Icons.Filled.RestartAlt, "Restablecer geometria", tint = Color.White)
            }
            Button(onClick = onCommit) { Text("OK") }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
        }
    }
}

/** Rect maximo con la relacion de aspecto pedida (en pixeles del marco), centrado en el foco actual. */
private fun centeredRatioRect(
    g: GeometryModel,
    ratio: Float,
    frameAspect: Float
): GeometryModel {
    val cx = (g.cropLeft + g.cropRight) / 2f
    val cy = (g.cropTop + g.cropBottom) / 2f
    val target = ratio / frameAspect // w_norm / h_norm
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
    onGeometry: (GeometryModel) -> Unit
) {
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val geometryRef = rememberUpdatedState(geometry)
    val onGeometryRef = rememberUpdatedState(onGeometry)
    var handle by remember { mutableStateOf<Handle?>(null) }
    var mode by remember { mutableStateOf(CropMode.NONE) }
    var moveOffset by remember { mutableStateOf(Offset.Zero) }
    var drawStart by remember { mutableStateOf(Offset.Zero) }
    val handlePx = with(LocalDensity.current) { 26.dp.toPx() }

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
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val g = geometryRef.value
                        val w = viewport.width.toFloat()
                        val h = viewport.height.toFloat()
                        val minSize = 0.1f
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
                    onDragEnd = {
                        // descarta selecciones minusculas dibujadas por error
                        val g = geometryRef.value
                        if (mode == CropMode.DRAW && (g.cropWidthNorm < 0.06f || g.cropHeightNorm < 0.06f)) {
                            onGeometryRef.value(g.copy(cropLeft = 0.1f, cropTop = 0.1f, cropRight = 0.9f, cropBottom = 0.9f))
                        }
                        mode = CropMode.NONE
                        handle = null
                    },
                    onDragCancel = {
                        mode = CropMode.NONE
                        handle = null
                    }
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
            // tercios
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
