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
}

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
