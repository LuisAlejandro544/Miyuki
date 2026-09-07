package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeadPaletteTheme
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.data.model.CuratedPalettes
import com.example.generator.GeneratorStyle
import com.example.ui.components.InteractiveBeadCanvas
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiGoldDark
import com.example.ui.theme.MiyukiTurquoise

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    generatedPattern: BeadPattern,
    currentStyle: GeneratorStyle,
    currentTechnique: BeadTechnique,
    currentColumns: Int,
    currentRows: Int,
    currentPalette: BeadPaletteTheme,
    isSymmetry: Boolean,
    onUpdateParams: (
        title: String?,
        style: GeneratorStyle?,
        technique: BeadTechnique?,
        columns: Int?,
        rows: Int?,
        palette: BeadPaletteTheme?,
        symmetry: Boolean?
    ) -> Unit,
    onSavePattern: () -> Unit,
    onOpenInEditor: () -> Unit,
    onStartTracking: (BeadPattern) -> Unit,
    // Earring / Zarcillos (Rust Engine)
    earringStyle: Int = 0,
    earringBaseWidth: Int = 9,
    earringMaxFringe: Int = 24,
    earringMinFringe: Int = 8,
    earringPalette: BeadPaletteTheme = CuratedPalettes.ATARDECER_FUEGO,
    onUpdateEarringStyle: (Int) -> Unit = {},
    onUpdateEarringWidth: (Int) -> Unit = {},
    onUpdateEarringMaxFringe: (Int) -> Unit = {},
    onUpdateEarringMinFringe: (Int) -> Unit = {},
    onUpdateEarringPalette: (BeadPaletteTheme) -> Unit = {},
    onGenerateEarringRust: () -> Unit = {},
    // Lua C Studio (Official Lua 5.4 C)
    luaScript: String = "",
    luaConsoleOutput: String = "",
    onUpdateLuaScript: (String) -> Unit = {},
    onRunLuaScript: (Int, Int) -> Unit = { _, _ -> },
    onExecuteLuaDirect: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf(generatedPattern.title) }
    var selectedGeneratorMode by remember { mutableIntStateOf(0) } // 0: Pulseras, 1: Zarcillos Rust, 2: Lua C Studio

    var luaCols by remember { mutableIntStateOf(11) }
    var luaRows by remember { mutableIntStateOf(32) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Mode Selector Tab Row
        TabRow(
            selectedTabIndex = selectedGeneratorMode,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedGeneratorMode == 0,
                onClick = { selectedGeneratorMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pulseras", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 1,
                onClick = { selectedGeneratorMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zarcillos (Rust)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 2,
                onClick = { selectedGeneratorMode = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lua 5.4 C", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Pattern Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                InteractiveBeadCanvas(
                    pattern = generatedPattern,
                    modifier = Modifier.fillMaxSize(),
                    isEditable = false
                )

                // Info overlay at top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${generatedPattern.technique.title} • ${generatedPattern.columns} col x ${generatedPattern.rows} filas (~${generatedPattern.totalBeads} delicas)",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Quick regenerate button on top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Button(
                        onClick = {
                            when (selectedGeneratorMode) {
                                0 -> onUpdateParams(null, null, null, null, null, null, null)
                                1 -> onGenerateEarringRust()
                                2 -> onRunLuaScript(luaCols, luaRows)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MiyukiGold,
                            contentColor = Color.Black
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Save, Edit, Track
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSavePattern,
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_generated_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiyukiGoldDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar Patrón", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenInEditor,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_generated_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Editar en Taller")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onStartTracking(generatedPattern) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiyukiTurquoise,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tejer ahora con Modo Tejedor", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // MODE 0: PULSERAS
        if (selectedGeneratorMode == 0) {
            Text(
                text = "Configuración de Pulseras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onUpdateParams(it, null, null, null, null, null, null)
                },
                label = { Text("Nombre de la Pulsera") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Estilo Geométrico",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GeneratorStyle.values().forEach { style ->
                    FilterChip(
                        selected = currentStyle == style,
                        onClick = { onUpdateParams(null, style, null, null, null, null, null) },
                        label = { Text(style.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Técnica de Tejido",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = currentTechnique == BeadTechnique.LOOM,
                    onClick = { onUpdateParams(null, null, BeadTechnique.LOOM, null, null, null, null) },
                    label = { Text("Telar (Loom)") }
                )
                FilterChip(
                    selected = currentTechnique == BeadTechnique.PEYOTE,
                    onClick = { onUpdateParams(null, null, BeadTechnique.PEYOTE, null, null, null, null) },
                    label = { Text("Peyote (Escalonado)") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Paleta de Cuentas Delica",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CuratedPalettes.ALL_PALETTES.forEach { palette ->
                    PaletteSelectRow(
                        palette = palette,
                        isSelected = currentPalette.name == palette.name,
                        onSelect = { onUpdateParams(null, null, null, null, null, palette, null) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dimensions: Columns
            Text(
                text = "Ancho: $currentColumns cuentas (~${String.format("%.1f", currentColumns * 0.16f)} cm)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = currentColumns.toFloat(),
                onValueChange = { onUpdateParams(null, null, null, it.toInt(), null, null, null) },
                valueRange = 5f..25f,
                steps = 19
            )

            // Dimensions: Rows
            Text(
                text = "Largo: $currentRows filas (~${String.format("%.1f", currentRows * 0.13f)} cm)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = currentRows.toFloat(),
                onValueChange = { onUpdateParams(null, null, null, null, it.toInt(), null, null) },
                valueRange = 20f..120f,
                steps = 99
            )

            // Symmetry toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdateParams(null, null, null, null, null, null, !isSymmetry) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSymmetry,
                    onCheckedChange = { onUpdateParams(null, null, null, null, null, null, it) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Simetría Central Espejada", fontWeight = FontWeight.SemiBold)
                    Text("Duplica el diseño reflejado hacia ambos extremos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // MODE 1: ZARCILLOS / ARETES CON FLECOS (RUST CORE ENGINE)
        if (selectedGeneratorMode == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MiyukiGold.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = MiyukiGoldDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Motor Nativo Rust: Zarcillos & Flecos", fontWeight = FontWeight.Bold, color = MiyukiGoldDark)
                        Text("Cálculo geométrico en Rust para la copa triangular en Brick Stitch y la curvatura armonizada de los flecos colgantes.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Estilo de Caída de los Flecos", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            val fringeStyles = listOf(
                0 to "Punta en V Clásica",
                1 to "Chevron Invertido",
                2 to "Cascada Ondulada",
                3 to "Escalonado Diagonal",
                4 to "Rombo Diamante"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                fringeStyles.forEach { (id, label) ->
                    FilterChip(
                        selected = earringStyle == id,
                        onClick = {
                            onUpdateEarringStyle(id)
                            onGenerateEarringRust()
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Base Width
            Text("Ancho de Base: $earringBaseWidth cuentas (~${String.format("%.1f", earringBaseWidth * 0.16f)} cm)", fontWeight = FontWeight.SemiBold)
            Slider(
                value = earringBaseWidth.toFloat(),
                onValueChange = {
                    onUpdateEarringWidth(it.toInt())
                    onGenerateEarringRust()
                },
                valueRange = 5f..21f,
                steps = 7
            )

            // Max Fringe Length
            Text("Largo Máximo del Fleco: $earringMaxFringe cuentas (~${String.format("%.1f", earringMaxFringe * 0.13f)} cm)", fontWeight = FontWeight.SemiBold)
            Slider(
                value = earringMaxFringe.toFloat(),
                onValueChange = {
                    onUpdateEarringMaxFringe(it.toInt())
                    onGenerateEarringRust()
                },
                valueRange = 12f..45f,
                steps = 32
            )

            // Min Fringe Length
            Text("Largo Mínimo del Fleco: $earringMinFringe cuentas", fontWeight = FontWeight.SemiBold)
            Slider(
                value = earringMinFringe.toFloat(),
                onValueChange = {
                    onUpdateEarringMinFringe(it.toInt())
                    onGenerateEarringRust()
                },
                valueRange = 2f..20f,
                steps = 17
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Paleta para Zarcillos", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CuratedPalettes.ALL_PALETTES.forEach { palette ->
                    PaletteSelectRow(
                        palette = palette,
                        isSelected = earringPalette.name == palette.name,
                        onSelect = {
                            onUpdateEarringPalette(palette)
                            onGenerateEarringRust()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerateEarringRust,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiyukiGoldDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Zarcillo con Rust", fontWeight = FontWeight.Bold)
            }
        }

        // MODE 2: LUA 5.4 C SCRIPT STUDIO
        if (selectedGeneratorMode == 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MiyukiGoldDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Intérprete Oficial Lua 5.4 en C (ANSI C)", fontWeight = FontWeight.Bold)
                        Text("Ejecución directa en el motor original de Lua sin wrappers. Escribe una función 'getBead(col, row)' que retorne colores ARGB.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Presets Rápidos de Script Lua:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onUpdateLuaScript("""
-- Tablero y Diamantes en Lua 5.4
function getBead(col, row)
    local gold = 0xFFFFD700
    local teal = 0xFF008080
    local ivory = 0xFFFFFFF0
    if ((col + row) % 4 == 0) then
        return gold
    elseif ((col - row) % 4 == 0) then
        return teal
    else
        return ivory
    end
end
print("Generando diseño con Lua C Oficial.")
                        """.trimIndent())
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Diamantes Lua", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onUpdateLuaScript("""
-- Ondas Armónicas en Lua 5.4 C
function getBead(col, row)
    local wave = math.sin(row * 0.3) * 3
    local center = math.floor(COLUMNS / 2)
    local dist = math.abs(col - (center + wave))
    if (dist < 1.2) then
        return 0xFFFF4500 -- Orange Red
    elseif (dist < 2.5) then
        return 0xFFFFD700 -- Gold
    else
        return 0xFF191970 -- Midnight Blue
    end
end
print("Ondas senoidales calculadas en Lua C.")
                        """.trimIndent())
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ondas Senoidales", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onUpdateLuaScript("""
-- Rayas Étnicas Alternadas
function getBead(col, row)
    local palette = {0xFF800020, 0xFFDAA520, 0xFF008B8B, 0xFF2F4F4F}
    local idx = (row % #palette) + 1
    if (col == 0 or col == COLUMNS - 1) then
        return 0xFFFFD700 -- Borde oro
    end
    return palette[idx]
end
print("Rayas calculadas con tabla Lua nativa.")
                        """.trimIndent())
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rayas Étnicas", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dimensions for Lua script execution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Columnas: $luaCols", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = luaCols.toFloat(),
                        onValueChange = { luaCols = it.toInt() },
                        valueRange = 5f..25f,
                        steps = 19
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Filas: $luaRows", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = luaRows.toFloat(),
                        onValueChange = { luaRows = it.toInt() },
                        valueRange = 10f..80f,
                        steps = 69
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lua Code Editor
            OutlinedTextField(
                value = luaScript,
                onValueChange = onUpdateLuaScript,
                label = { Text("Código de Script Lua C 5.4") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onRunLuaScript(luaCols, luaRows) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyukiGoldDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generar Patrón con Lua C", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onExecuteLuaDirect(luaScript) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Solo Probar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Console output
            Text("Consola Lua Oficial:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text(
                    text = luaConsoleOutput.ifEmpty { "Salida de consola..." },
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PaletteSelectRow(
    palette: BeadPaletteTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MiyukiGold) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MiyukiGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = palette.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${palette.beads.size} colores Miyuki",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                palette.beads.take(5).forEach { bead ->
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(bead.colorInt))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }
        }
    }
}
