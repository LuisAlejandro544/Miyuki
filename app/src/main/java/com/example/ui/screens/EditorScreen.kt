package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeadPattern
import com.example.data.model.MiyukiBead
import com.example.data.model.MiyukiCatalog
import com.example.ui.components.InteractiveBeadCanvas
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiTurquoise
import com.example.ui.viewmodel.EditorTool

@Composable
fun EditorScreen(
    pattern: BeadPattern,
    selectedTool: EditorTool,
    selectedBead: MiyukiBead,
    isMirrorMode: Boolean,
    onToolSelected: (EditorTool) -> Unit,
    onBeadSelected: (MiyukiBead) -> Unit,
    onToggleMirrorMode: () -> Unit,
    onCellInteracted: (col: Int, row: Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearCanvas: () -> Unit,
    onSavePattern: (String) -> Unit,
    onStartTracking: (BeadPattern) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showMaterialsDialog by remember { mutableStateOf(false) }
    var patternTitle by remember { mutableStateOf(pattern.title) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
    ) {
        // Top Toolbar Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${pattern.technique.title} • ${pattern.columns} col x ${pattern.rows} filas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MiyukiTurquoise
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onUndo, modifier = Modifier.testTag("undo_btn")) {
                        Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                    }
                    IconButton(onClick = onRedo, modifier = Modifier.testTag("redo_btn")) {
                        Icon(Icons.Default.Redo, contentDescription = "Rehacer")
                    }
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.testTag("editor_save_btn")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Guardar", tint = MiyukiGold)
                    }
                }
            }
        }

        // Tools Row & Secondary Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Brush
            FilterChip(
                selected = selectedTool == EditorTool.BRUSH,
                onClick = { onToolSelected(EditorTool.BRUSH) },
                label = { Text("Pincel") },
                leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            // Bucket Fill
            FilterChip(
                selected = selectedTool == EditorTool.BUCKET,
                onClick = { onToolSelected(EditorTool.BUCKET) },
                label = { Text("Relleno") },
                leadingIcon = { Icon(Icons.Default.FormatColorFill, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            // Eyedropper Pipette
            FilterChip(
                selected = selectedTool == EditorTool.PIPETTE,
                onClick = { onToolSelected(EditorTool.PIPETTE) },
                label = { Text("Gotero") },
                leadingIcon = { Icon(Icons.Default.Colorize, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            // Eraser
            FilterChip(
                selected = selectedTool == EditorTool.ERASER,
                onClick = { onToolSelected(EditorTool.ERASER) },
                label = { Text("Borrador") },
                leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            // Mirror Symmetry Toggle
            FilterChip(
                selected = isMirrorMode,
                onClick = onToggleMirrorMode,
                label = { Text("Modo Espejo") },
                leadingIcon = { Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            // Clear Canvas
            IconButton(onClick = onClearCanvas) {
                Icon(Icons.Default.Delete, contentDescription = "Limpiar lienzo", tint = MaterialTheme.colorScheme.outline)
            }
        }

        // Miyuki Delica Color Picker Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiyukiCatalog.ALL_BEADS.forEach { bead ->
                val isSelected = bead.code == selectedBead.code
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent
                        )
                        .clickable { onBeadSelected(bead) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(bead.composeColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MiyukiGold else Color.Black.copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (bead.colorInt == MiyukiCatalog.DB_0200.colorInt) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = bead.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        HorizontalDivider()

        // Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            InteractiveBeadCanvas(
                pattern = pattern,
                modifier = Modifier.fillMaxSize(),
                isEditable = true,
                selectedColor = selectedBead.colorInt,
                isMirrorMode = isMirrorMode,
                isEraser = selectedTool == EditorTool.ERASER,
                onCellClicked = { col, row -> onCellInteracted(col, row) },
                onCellDragged = { col, row -> onCellInteracted(col, row) }
            )

            // Floating tip overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Pellizca para zoom • Toca o arrastra para colorear",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onStartTracking(pattern) },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyukiGold,
                        contentColor = Color(0xFF1B191B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tejer Fila a Fila", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showMaterialsDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Materiales")
                }
            }
        }
    }

    // Save Pattern Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar Diseño de Pulsera", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Ingresa un nombre para guardar este patrón en tu catálogo:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = patternTitle,
                        onValueChange = { patternTitle = it },
                        label = { Text("Nombre de la pulsera") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSavePattern(patternTitle)
                        showSaveDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Materials Dialog
    if (showMaterialsDialog) {
        val beadCounts = remember(pattern) { pattern.calculateBeadCounts() }
        AlertDialog(
            onDismissRequest = { showMaterialsDialog = false },
            title = { Text("Ficha de Cuentas Necesarias", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total de delicas pintadas: ${pattern.totalBeads} (~${String.format("%.1f", pattern.totalBeads / 105f)} g)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        beadCounts.take(8).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(item.bead.composeColor)
                                            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${item.bead.code} (${item.bead.name})", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${item.count} cuentas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMaterialsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
