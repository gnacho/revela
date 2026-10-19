package app.revela

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revela.core.LayerRow

/**
 * Editor de capas: una fila por edicion aplicada, en el orden real de la pila.
 * Ojo = ocultar/mostrar sin perder los valores; toque en la fila = reabrir esa herramienta;
 * aspa = eliminar la capa. Como Snapseed: "Ver modificaciones".
 */
@Composable
fun LayersSheet(
    rows: List<LayerRow>,
    canUndo: Boolean,
    canRedo: Boolean,
    canRevert: Boolean,
    onToggle: (LayerRow) -> Unit,
    onEdit: (LayerRow) -> Unit,
    onDelete: (LayerRow) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onRevert: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.layers_title)) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = onUndo, enabled = canUndo) {
                        Text(stringResource(R.string.layers_undo))
                    }
                    TextButton(onClick = onRedo, enabled = canRedo) {
                        Text(stringResource(R.string.layers_redo))
                    }
                    TextButton(onClick = onRevert, enabled = canRevert) {
                        Text(stringResource(R.string.layers_revert), color = if (canRevert) Color(0xFFD32F2F) else Ui.textDim)
                    }
                }
            if (rows.isEmpty()) {
                Text(stringResource(R.string.layers_empty), color = Ui.textDim)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    itemsIndexed(rows) { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Ui.card, RoundedCornerShape(10.dp))
                                .clickable { onEdit(row) }
                                .padding(start = 12.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Ui.textDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = row.label,
                                color = if (row.hidden) Ui.textDim else Ui.text,
                                fontWeight = if (row.hidden) FontWeight.Normal else FontWeight.Medium,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onToggle(row) }) {
                                Icon(
                                    imageVector = if (row.hidden) Icons.Outlined.VisibilityOff
                                    else Icons.Outlined.Visibility,
                                    contentDescription = stringResource(
                                        if (row.hidden) R.string.layer_show else R.string.layer_hide
                                    ),
                                    tint = Ui.icon
                                )
                            }
                            IconButton(onClick = { onDelete(row) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.layer_delete),
                                    tint = Ui.icon
                                )
                            }
                        }
                    }
                }
            }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        }
    )
}
