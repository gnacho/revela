package app.revela.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Identificadores estables de herramienta (uno por edit; reajustar una herramienta reemplaza su edit). */
object Tool {
    const val EXPOSURE = "exposure"
    const val CONTRAST = "contrast"
    const val SATURATION = "saturation"
    const val WHITE_BALANCE = "whiteBalance"
    const val CURVES = "curves"
    const val CROP = "crop"
    const val SELECTIVE = "selective"
    const val VIGNETTE = "vignette"
    const val HEAL = "heal"
    const val LOOK = "look"
    const val BNW = "bnw"
    const val TONAL = "tonalContrast"
}

/** Punto de control selectivo (coordenadas normalizadas al marco). */
@Serializable
data class ControlPoint(
    val x: Float,
    val y: Float,
    val brightness: Float = 0f,   // -1..1
    val contrast: Float = 0f,     // -1..1
    val saturation: Float = 0f,   // -1..1
    val radius: Float = 0.25f     // fraccion del lado menor del marco
)

@Serializable
sealed interface Edit {
    val toolId: String

    @Serializable
    @SerialName("exposure")
    data class Exposure(val stops: Float = 0f) : Edit {
        override val toolId: String get() = Tool.EXPOSURE
    }

    @Serializable
    @SerialName("contrast")
    data class Contrast(val amount: Float = 0f) : Edit {
        override val toolId: String get() = Tool.CONTRAST
    }

    @Serializable
    @SerialName("saturation")
    data class Saturation(val amount: Float = 0f) : Edit {
        override val toolId: String get() = Tool.SATURATION
    }

    @Serializable
    @SerialName("whiteBalance")
    data class WhiteBalance(val temperature: Float = 0f, val tint: Float = 0f) : Edit {
        override val toolId: String get() = Tool.WHITE_BALANCE
    }

    @Serializable
    @SerialName("curves")
    data class Curves(val points: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))) : Edit {
        override val toolId: String get() = Tool.CURVES
    }

    @Serializable
    @SerialName("selective")
    data class Selective(val points: List<ControlPoint> = emptyList()) : Edit {
        override val toolId: String get() = Tool.SELECTIVE
    }

    @Serializable
    @SerialName("vignette")
    data class Vignette(val strength: Float = 0f) : Edit {
        override val toolId: String get() = Tool.VIGNETTE
    }

    @Serializable
    @SerialName("heal")
    data class Heal(val strokes: List<HealStroke> = emptyList()) : Edit {
        override val toolId: String get() = Tool.HEAL
    }

    @Serializable
    @SerialName("look")
    data class Look(val id: String = "none") : Edit {
        override val toolId: String get() = Tool.LOOK
    }

    @Serializable
    @SerialName("bnw")
    data class Bnw(val filter: String = "neutral") : Edit {
        override val toolId: String get() = Tool.BNW
    }

    @Serializable
    @SerialName("tonalContrast")
    data class TonalContrast(
        val shadows: Float = 0f,
        val midtones: Float = 0f,
        val highlights: Float = 0f
    ) : Edit {
        override val toolId: String get() = Tool.TONAL
    }
}

@Serializable
data class EditState(val edits: List<Edit> = emptyList())

/**
 * Pila de ediciones no destructiva con undo/redo por instantaneas (correcto tambien
 * cuando una herramienta reemplaza su propio edit). Los rangos de undo no se persisten.
 */
class EditStack(initialEdits: List<Edit> = emptyList()) {

    private var current: List<Edit> = initialEdits.toList()
    private val undoStack = ArrayDeque<List<Edit>>()
    private val redoStack = ArrayDeque<List<Edit>>()

    val edits: List<Edit> get() = current
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun apply(edit: Edit) {
        pushUndo()
        current = current.filterNot { it.toolId == edit.toolId } + edit
    }

    /** Elimina el edit de una herramienta (equivale a restablecerla). Devuelve true si habia algo. */
    fun removeTool(toolId: String): Boolean {
        if (current.none { it.toolId == toolId }) return false
        pushUndo()
        current = current.filterNot { it.toolId == toolId }
        return true
    }

    fun undo(): Boolean {
        val previous = undoStack.removeLastOrNull() ?: return false
        redoStack.addLast(current)
        current = previous
        return true
    }

    fun redo(): Boolean {
        val next = redoStack.removeLastOrNull() ?: return false
        undoStack.addLast(current)
        current = next
        return true
    }

    fun reset(edits: List<Edit>) {
        pushUndo()
        current = edits.toList()
    }

    /** Anade un punto de control selectivo (fusiona en el edit Selective existente). */
    fun applySelectivePoint(point: ControlPoint) {
        pushUndo()
        val existing = current.filterIsInstance<Edit.Selective>().firstOrNull()
        val points = existing?.points.orEmpty() + point
        current = current.filterNot { it.toolId == Tool.SELECTIVE } + Edit.Selective(points)
    }

    /** Sustituye el punto de la posicion index. */
    fun updateSelectivePoint(index: Int, point: ControlPoint) {
        pushUndo()
        current = current.map { edit ->
            if (edit is Edit.Selective) {
                Edit.Selective(edit.points.toMutableList().apply { set(index.coerceIn(0, size - 1), point) })
            } else {
                edit
            }
        }
    }

    /** Elimina el punto de la posicion index. */
    fun removeSelectivePoint(index: Int) {
        pushUndo()
        current = current.mapNotNull { edit ->
            if (edit is Edit.Selective) {
                val pts = edit.points.toMutableList().apply { removeAt(index.coerceIn(0, size - 1)) }
                if (pts.isEmpty()) null else Edit.Selective(pts)
            } else {
                edit
            }
        }
    }

    /** Anade un trazo de sanado (fusiona en el edit Heal existente). */
    fun applyHealStroke(stroke: HealStroke) {
        pushUndo()
        val existing = current.filterIsInstance<Edit.Heal>().firstOrNull()
        val strokes = existing?.strokes.orEmpty() + stroke
        current = current.filterNot { it.toolId == Tool.HEAL } + Edit.Heal(strokes)
    }

    fun selectivePoints(): List<ControlPoint> =
        current.filterIsInstance<Edit.Selective>().firstOrNull()?.points.orEmpty()

    fun healStrokes(): List<HealStroke> =
        current.filterIsInstance<Edit.Heal>().firstOrNull()?.strokes.orEmpty()

    fun toState(): EditState = EditState(current)

    fun toJson(): String = json.encodeToString(EditState.serializer(), toState())

    private fun pushUndo() {
        undoStack.addLast(current)
        redoStack.clear()
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

        fun fromJson(text: String): EditStack {
            val state = json.decodeFromString(EditState.serializer(), text)
            return EditStack(state.edits)
        }

        fun fromState(state: EditState): EditStack = EditStack(state.edits)
    }
}
