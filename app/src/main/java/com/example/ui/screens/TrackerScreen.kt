package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeadPattern
import com.example.data.model.MiyukiCatalog
import com.example.ui.components.InteractiveBeadCanvas
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiGoldDark
import com.example.ui.theme.MiyukiTurquoise

data class RowBeadRun(
    val beadCode: String,
    val beadName: String,
    val colorInt: Int,
    val count: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackerScreen(
    pattern: BeadPattern,
    currentRow: Int,
    onNextRow: () -> Unit,
    onPreviousRow: () -> Unit,
    onSetRow: (Int) -> Unit,
    onReset: () -> Unit,
    onExportPdf: (BeadPattern) -> Unit = {}
) {
    val totalRows = pattern.rows
    val progressFraction = ((currentRow + 1).toFloat() / totalRows).coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100).toInt()
    val cmWoven = String.format("%.1f", (currentRow + 1) * 0.16f)

    // Direction: Even rows left-to-right, odd rows right-to-left
    val isLeftToRight = currentRow % 2 == 0

    // Compute bead sequence runs for the current row
    val rowColors = remember(pattern, currentRow) {
        val raw = pattern.getRowBeads(currentRow)
        if (isLeftToRight) raw else raw.reversed()
    }

    val beadRuns = remember(rowColors) {
        val runs = mutableListOf<RowBeadRun>()
        if (rowColors.isNotEmpty()) {
            var currColor = rowColors[0]
            var currCount = 1
            for (i in 1 until rowColors.size) {
                if (rowColors[i] == currColor) {
                    currCount++
                } else {
                    if (currColor == 0 || currColor == -1) {
                        runs.add(RowBeadRun("Vacío", "Espacio vacío (Saltar)", 0, currCount))
                    } else {
                        val bead = MiyukiCatalog.findClosest(currColor)
                        runs.add(RowBeadRun(bead.code, bead.name, currColor, currCount))
                    }
                    currColor = rowColors[i]
                    currCount = 1
                }
            }
            if (currColor == 0 || currColor == -1) {
                runs.add(RowBeadRun("Vacío", "Espacio vacío (Saltar)", 0, currCount))
            } else {
                val bead = MiyukiCatalog.findClosest(currColor)
                runs.add(RowBeadRun(bead.code, bead.name, currColor, currCount))
            }
        }
        runs
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Pattern Title & Technique
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Modo Tejedor Paso a Paso • ${pattern.technique.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MiyukiTurquoise
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onExportPdf(pattern) }) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar PDF",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reiniciar al inicio")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Progress Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fila ${currentRow + 1} de $totalRows",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$progressPercent% completado • ~$cmWoven cm tejidos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLeftToRight) MiyukiTurquoise.copy(alpha = 0.2f) else MiyukiGold.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isLeftToRight) "👉 Izq a Der" else "👈 Der a Izq",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isLeftToRight) MiyukiTurquoise else MiyukiGoldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MiyukiGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Row Beads Reading Order (The crafter's guide)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Enhebrar en este orden:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Beads run chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    beadRuns.forEach { run ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (run.colorInt == 0 || run.colorInt == -1) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(run.colorInt))
                                        .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${run.count}x",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = run.beadCode,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Individual Bead Beads Sequence Preview
                Text(
                    text = "Visualización cuenta a cuenta:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowColors.forEachIndexed { index, colorInt ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (colorInt == 0 || colorInt == -1) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Transparent)
                                        .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("—", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(colorInt))
                                        .border(1.5.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                )
                            }
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Large Navigation Buttons (Min 56dp tall for effortless thumb tapping)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousRow,
                enabled = currentRow > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .testTag("tracker_prev_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fila Anterior", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Button(
                onClick = onNextRow,
                enabled = currentRow < totalRows - 1,
                modifier = Modifier
                    .weight(1.3f)
                    .height(60.dp)
                    .testTag("tracker_next_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiyukiGold,
                    contentColor = Color(0xFF1B191B)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Siguiente Fila", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Visual Context Canvas (Full bracelet with highlighted active row)
        Text(
            text = "Posición en la Pulsera",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            InteractiveBeadCanvas(
                pattern = pattern,
                modifier = Modifier.fillMaxSize(),
                activeRow = currentRow,
                isEditable = false
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fast Row Jump Slider
        Text(
            text = "Saltar rápidamente a una fila:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = currentRow.toFloat(),
            onValueChange = { onSetRow(it.toInt()) },
            valueRange = 0f..(totalRows - 1).toFloat(),
            steps = if (totalRows > 1) totalRows - 2 else 0
        )
    }
}
