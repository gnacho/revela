package app.revela.core

/**
 * Capa visible en el editor de capas: un edit del stack con su etiqueta y su estado.
 * El stack sigue siendo la unica fuente de verdad de la imagen; las capas ocultas viven
 * aparte (ver [HiddenLayers]) para no ensuciar la serializacion de las ediciones.
 */
data class LayerRow(
    val toolId: String,
    val label: String,
    val hidden: Boolean,
    val tool: String
)

object LayerList {

    /** Orden de presentacion: el orden real del stack (de la primera a la ultima aplicada). */
    fun rows(edits: List<Edit>, hidden: Set<String>, labelFor: (Edit) -> String): List<LayerRow> =
        edits.map { edit ->
            LayerRow(
                toolId = edit.toolId,
                label = labelFor(edit),
                hidden = edit.toolId in hidden,
                tool = edit.toolId
            )
        }

    /** Edits que se deben renderizar: los que no estan ocultos y no son de valor neutro. */
    fun visibleEdits(edits: List<Edit>, hidden: Set<String>): List<Edit> =
        edits.filterNot { it.toolId in hidden }
}
