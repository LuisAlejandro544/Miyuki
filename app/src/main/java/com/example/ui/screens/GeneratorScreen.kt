package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
    onExecuteLuaDirect: (String) -> Unit = {},
    // Photo to Pattern (Native CIELAB + Floyd-Steinberg)
    photoTargetCols: Int = 24,
    photoTargetRows: Int = 48,
    photoBrightness: Float = 0f,
    photoContrast: Float = 1.15f,
    photoDithering: Boolean = true,
    photoMaxColors: Int = 12,
    isPhotoProcessing: Boolean = false,
    onUpdatePhotoParams: (Int?, Int?, Float?, Float?, Boolean?, Int?) -> Unit = { _, _, _, _, _, _ -> },
    onConvertBitmapToPattern: (Bitmap, String) -> Unit = { _, _ -> },
    // PDF / Chart Calibration (Native C++20 & Rust)
    pdfSourceBitmap: Bitmap? = null,
    pdfPageCount: Int = 1,
    pdfCurrentPage: Int = 0,
    cropLeft: Float = 0.11f,
    cropTop: Float = 0.16f,
    cropRight: Float = 0.89f,
    cropBottom: Float = 0.93f,
    calibratedCols: Int = 16,
    calibratedRows: Int = 40,
    calibratedTechnique: BeadTechnique = BeadTechnique.PEYOTE,
    calibratedSampleWindow: Float = 0.50f,
    calibratedBrightness: Float = 0f,
    calibratedContrast: Float = 1.15f,
    calibratedMaxColors: Int = 8,
    isCalibrating: Boolean = false,
    isAutoDetectingGrid: Boolean = false,
    chartDocumentName: String = "Documento de Patrón",
    onUpdateCalibrationCrop: (Float?, Float?, Float?, Float?) -> Unit = { _, _, _, _ -> },
    onUpdateCalibrationSettings: (Int?, Int?, BeadTechnique?, Float?, Float?, Float?, Int?) -> Unit = { _, _, _, _, _, _, _ -> },
    onLoadSamplePdfChart: () -> Unit = {},
    onLoadPdfOrImageUri: (Uri) -> Unit = {},
    onChangePdfPage: (Int) -> Unit = {},
    onAutoDetectGridWithRust: () -> Unit = {},
    onCalibrateAndExtractPattern: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(generatedPattern.title) }
    var selectedGeneratorMode by remember { mutableIntStateOf(0) } // 0: Pulseras, 1: Zarcillos Rust, 2: Foto a Patrón, 3: Calibrar PDF, 4: Lua 5.4 C

    var luaCols by remember { mutableIntStateOf(11) }
    var luaRows by remember { mutableIntStateOf(32) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoLabel by remember { mutableStateOf("Ninguna foto cargada aún") }

    val pdfOrFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onLoadPdfOrImageUri(uri)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream)
                    if (decoded != null) {
                        selectedBitmap = decoded
                        photoLabel = "Foto cargada (${decoded.width}x${decoded.height} px)"
                    }
                }
            } catch (e: Exception) {
                photoLabel = "Error al abrir imagen: ${e.localizedMessage}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Mode Selector Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedGeneratorMode,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            edgePadding = 6.dp
        ) {
            Tab(
                selected = selectedGeneratorMode == 0,
                onClick = { selectedGeneratorMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Pulseras", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 1,
                onClick = { selectedGeneratorMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Zarcillos", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 2,
                onClick = { selectedGeneratorMode = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Foto a Patrón", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 3,
                onClick = { selectedGeneratorMode = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Calibrar PDF", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedGeneratorMode == 4,
                onClick = { selectedGeneratorMode = 4 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Lua C", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
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
                                2 -> if (selectedBitmap != null) onConvertBitmapToPattern(selectedBitmap!!, title)
                                3 -> onCalibrateAndExtractPattern("Patrón PDF ($calibratedCols x $calibratedRows)")
                                4 -> onRunLuaScript(luaCols, luaRows)
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

        // MODE 2: FOTO A PATRÓN (MOTOR NATIVO C++20 CIELAB & FLOYD-STEINBERG)
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MiyukiGoldDark, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Conversor Foto a Patrón Miyuki", fontWeight = FontWeight.Bold)
                        Text(
                            "Cuantización nativa C++ en espacio perceptual CIELAB (Delta E) contra el catálogo Delica 11/0, con difusión de error Floyd-Steinberg.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Selector de Imagen de Galería / Archivos
            Text("1. Seleccionar Imagen de Origen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyukiTurquoise,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Galería", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plantillas rápidas de prueba
            Text("O prueba con una plantilla de muestra:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val bmp = generateSampleArt(0)
                        selectedBitmap = bmp
                        photoLabel = "Muestra: Flor de Loto (256x256)"
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Flor de Loto", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val bmp = generateSampleArt(1)
                        selectedBitmap = bmp
                        photoLabel = "Muestra: Mandala Solar (256x256)"
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mandala Solar", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val bmp = generateSampleArt(2)
                        selectedBitmap = bmp
                        photoLabel = "Muestra: Atardecer Marino (256x256)"
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Atardecer Marino", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview de la imagen seleccionada
            if (selectedBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Vista previa imagen",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MiyukiGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(photoLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                "Resolución lista para cuantizar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Selecciona una foto de tu galería o una plantilla de muestra para comenzar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Parámetros del Patrón (Grid Dimensiones)
            Text("2. Dimensiones del Patrón (Cuentas)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))

            // Columnas
            Text(
                "Ancho: $photoTargetCols cuentas (~${String.format("%.1f", photoTargetCols * 0.16f)} cm en Delica 11/0)",
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = photoTargetCols.toFloat(),
                onValueChange = { onUpdatePhotoParams(it.toInt(), null, null, null, null, null) },
                valueRange = 10f..60f,
                steps = 49
            )

            // Filas
            Text(
                "Largo: $photoTargetRows cuentas (~${String.format("%.1f", photoTargetRows * 0.13f)} cm)",
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = photoTargetRows.toFloat(),
                onValueChange = { onUpdatePhotoParams(null, it.toInt(), null, null, null, null) },
                valueRange = 10f..100f,
                steps = 89
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Ajustes de Imagen y Cuantización
            Text("3. Ajustes de Imagen y Colorimetría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))

            // Contraste
            Text("Contraste: ${String.format("%.2f", photoContrast)}x", fontWeight = FontWeight.SemiBold)
            Slider(
                value = photoContrast,
                onValueChange = { onUpdatePhotoParams(null, null, null, it, null, null) },
                valueRange = 0.5f..2.5f
            )

            // Brillo
            Text("Brillo: ${photoBrightness.toInt()}", fontWeight = FontWeight.SemiBold)
            Slider(
                value = photoBrightness,
                onValueChange = { onUpdatePhotoParams(null, null, it, null, null, null) },
                valueRange = -50f..50f
            )

            // Tramado Floyd-Steinberg Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tramado Floyd-Steinberg (Dithering)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        "Difusión de error en C++ para crear gradientes continuos y detalles fotográficos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = photoDithering,
                    onCheckedChange = { onUpdatePhotoParams(null, null, null, null, it, null) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MiyukiGoldDark)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Límite de Colores en Paleta
            Text("Límite de Colores en el Patrón:", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(4, 8, 12, 16, 20).forEach { count ->
                    FilterChip(
                        selected = photoMaxColors == count,
                        onClick = { onUpdatePhotoParams(null, null, null, null, null, count) },
                        label = { Text("$count col") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Ejecución del Motor Nativo
            Button(
                onClick = {
                    selectedBitmap?.let { bmp ->
                        onConvertBitmapToPattern(bmp, "Foto a Miyuki ($photoTargetCols x $photoTargetRows)")
                    }
                },
                enabled = selectedBitmap != null && !isPhotoProcessing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiyukiGoldDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isPhotoProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando en Motor C++...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convertir Foto con Motor Nativo", fontWeight = FontWeight.Bold)
                }
            }
        }

        // MODE 3: IMPORTADOR Y CALIBRADOR DE PDF / GRÁFICOS (C++20 & RUST)
        if (selectedGeneratorMode == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MiyukiGoldDark, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Calibrador de Patrones PDF y Gráficos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Importa un PDF o foto de patrón (Etsy, Pinterest, revista), encuadra la cuadrícula y extrae automáticamente las cuentas Miyuki con Rust y C++20.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: Pick file or load demo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        pdfOrFileLauncher.launch(arrayOf("application/pdf", "image/*"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MiyukiGoldDark, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir PDF / Gráfico", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onLoadSamplePdfChart() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Muestra PDF", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pdfSourceBitmap != null) {
                // Info header & page navigation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chartDocumentName,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Resolución nativa: ${pdfSourceBitmap.width} x ${pdfSourceBitmap.height} px",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (pdfPageCount > 1) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.IconButton(
                                        onClick = { if (pdfCurrentPage > 0) onChangePdfPage(pdfCurrentPage - 1) },
                                        enabled = pdfCurrentPage > 0
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Pág anterior")
                                    }
                                    Text("${pdfCurrentPage + 1}/$pdfPageCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    androidx.compose.material3.IconButton(
                                        onClick = { if (pdfCurrentPage < pdfPageCount - 1) onChangePdfPage(pdfCurrentPage + 1) },
                                        enabled = pdfCurrentPage < pdfPageCount - 1
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Pág siguiente")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Framing Canvas Preview
                Text(
                    text = "Encuadre de Cuadrícula (Muestra Visual)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imageBitmap = remember(pdfSourceBitmap) { pdfSourceBitmap.asImageBitmap() }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height

                            val imgW = imageBitmap.width.toFloat()
                            val imgH = imageBitmap.height.toFloat()
                            val scale = kotlin.math.min(canvasW / imgW, canvasH / imgH)
                            val drawW = imgW * scale
                            val drawH = imgH * scale
                            val offsetX = (canvasW - drawW) / 2f
                            val offsetY = (canvasH - drawH) / 2f

                            drawImage(
                                image = imageBitmap,
                                dstOffset = androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()),
                                dstSize = androidx.compose.ui.unit.IntSize(drawW.toInt(), drawH.toInt())
                            )

                            val cropX = offsetX + (cropLeft * drawW)
                            val cropY = offsetY + (cropTop * drawH)
                            val cropW = (cropRight - cropLeft) * drawW
                            val cropH = (cropBottom - cropTop) * drawH

                            // Dim outside masks
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(drawW, (cropY - offsetY).coerceAtLeast(0f))
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(offsetX, cropY + cropH),
                                size = Size(drawW, (offsetY + drawH - (cropY + cropH)).coerceAtLeast(0f))
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(offsetX, cropY),
                                size = Size((cropX - offsetX).coerceAtLeast(0f), cropH)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(cropX + cropW, cropY),
                                size = Size((offsetX + drawW - (cropX + cropW)).coerceAtLeast(0f), cropH)
                            )

                            // Bright frame for calibrated crop area
                            drawRect(
                                color = Color(0xFFFFD54F),
                                topLeft = Offset(cropX, cropY),
                                size = Size(cropW, cropH),
                                style = Stroke(width = 3f)
                            )

                            // Grid lines inside crop
                            val safeCols = calibratedCols.coerceAtLeast(1)
                            val stepX = cropW / safeCols
                            for (c in 1 until safeCols) {
                                val gx = cropX + (c * stepX)
                                drawLine(
                                    color = Color(0x66FFD54F),
                                    start = Offset(gx, cropY),
                                    end = Offset(gx, cropY + cropH),
                                    strokeWidth = 1f
                                )
                            }
                            val safeRows = calibratedRows.coerceAtLeast(1)
                            val stepY = cropH / safeRows
                            for (r in 1 until safeRows) {
                                val gy = cropY + (r * stepY)
                                drawLine(
                                    color = Color(0x66FFD54F),
                                    start = Offset(cropX, gy),
                                    end = Offset(cropX + cropW, gy),
                                    strokeWidth = 1f
                                )
                            }
                        }

                        // Badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Cuadrícula: $calibratedCols col x $calibratedRows filas",
                                color = Color(0xFFFFD54F),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Crop Sliders
                Text("Ajuste de Margen del Documento:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Margen Sup: ${(cropTop * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = cropTop,
                            onValueChange = { onUpdateCalibrationCrop(null, it, null, null) },
                            valueRange = 0.0f..0.50f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Margen Inf: ${(cropBottom * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = cropBottom,
                            onValueChange = { onUpdateCalibrationCrop(null, null, null, it) },
                            valueRange = 0.50f..1.0f
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Margen Izq: ${(cropLeft * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = cropLeft,
                            onValueChange = { onUpdateCalibrationCrop(it, null, null, null) },
                            valueRange = 0.0f..0.50f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Margen Der: ${(cropRight * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = cropRight,
                            onValueChange = { onUpdateCalibrationCrop(null, null, it, null) },
                            valueRange = 0.50f..1.0f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto detect with Rust button
                Button(
                    onClick = { onAutoDetectGridWithRust() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = !isAutoDetectingGrid
                ) {
                    if (isAutoDetectingGrid) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando gradientes con Rust...")
                    } else {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-detectar Cuadrícula (Motor Rust)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Manual Dimensions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Columnas (Ancho):", fontWeight = FontWeight.SemiBold)
                    Text("$calibratedCols cuentas", fontWeight = FontWeight.Bold, color = MiyukiGoldDark)
                }
                Slider(
                    value = calibratedCols.toFloat(),
                    onValueChange = { onUpdateCalibrationSettings(it.toInt(), null, null, null, null, null, null) },
                    valueRange = 6f..60f,
                    steps = 54
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filas (Largo):", fontWeight = FontWeight.SemiBold)
                    Text("$calibratedRows cuentas", fontWeight = FontWeight.Bold, color = MiyukiGoldDark)
                }
                Slider(
                    value = calibratedRows.toFloat(),
                    onValueChange = { onUpdateCalibrationSettings(null, it.toInt(), null, null, null, null, null) },
                    valueRange = 10f..120f,
                    steps = 110
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Technique Selector
                Text("Técnica de Tejido del Patrón:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = calibratedTechnique == BeadTechnique.PEYOTE,
                        onClick = { onUpdateCalibrationSettings(null, null, BeadTechnique.PEYOTE, null, null, null, null) },
                        label = { Text("Peyote") }
                    )
                    FilterChip(
                        selected = calibratedTechnique == BeadTechnique.BRICK_STITCH,
                        onClick = { onUpdateCalibrationSettings(null, null, BeadTechnique.BRICK_STITCH, null, null, null, null) },
                        label = { Text("Brick Stitch") }
                    )
                    FilterChip(
                        selected = calibratedTechnique == BeadTechnique.LOOM,
                        onClick = { onUpdateCalibrationSettings(null, null, BeadTechnique.LOOM, null, null, null, null) },
                        label = { Text("Telar (Loom)") }
                    )
                }
                Text(
                    text = if (calibratedTechnique == BeadTechnique.LOOM) "Alineación rectangular directa en telar." else "Compensación de escalonado de cuentas pares/impares para Peyote/Brick.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Advanced sampling settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Muestreo Central de Cuenta:", fontWeight = FontWeight.SemiBold)
                    Text("${(calibratedSampleWindow * 100).toInt()}% ventana", fontWeight = FontWeight.Bold, color = MiyukiGoldDark)
                }
                Slider(
                    value = calibratedSampleWindow,
                    onValueChange = { onUpdateCalibrationSettings(null, null, null, it, null, null, null) },
                    valueRange = 0.25f..0.80f
                )
                Text(
                    text = "Ignora las líneas negras impresas y analiza sólo el corazón de cada cuenta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Max colors
                Text("Máximo de Colores Delica a Mapear:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 6, 8, 12, 16, 24).forEach { count ->
                        FilterChip(
                            selected = calibratedMaxColors == count,
                            onClick = { onUpdateCalibrationSettings(null, null, null, null, null, null, count) },
                            label = { Text("$count cols") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action: Calibrate Button
                Button(
                    onClick = {
                        onCalibrateAndExtractPattern("Patrón PDF ($calibratedCols x $calibratedRows)")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyukiGold,
                        contentColor = Color.Black
                    ),
                    enabled = !isCalibrating
                ) {
                    if (isCalibrating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calibrando y Muestreando en C++20...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.GridOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calibrar y Muestrear Patrón en C++20", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MiyukiGoldDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Ningún PDF o gráfico abierto", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Toca 'Abrir PDF / Gráfico' para cargar un archivo tuyo o 'Muestra PDF' para probar la calibración con el diseño de demostración.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // MODE 4: LUA 5.4 C SCRIPT STUDIO
        if (selectedGeneratorMode == 4) {
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

            Spacer(modifier = Modifier.height(10.dp))

            // In-app Logcat Debugging (Lynx)
            val context = androidx.compose.ui.platform.LocalContext.current
            OutlinedButton(
                onClick = { com.example.util.DebugTools.openLynxLogcat(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MiyukiGoldDark
                )
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Logcat del Sistema (Lynx Console)", fontWeight = FontWeight.SemiBold)
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

private fun generateSampleArt(type: Int): Bitmap {
    val size = 256
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    when (type) {
        0 -> { // Flor de Loto Turquesa y Oro
            paint.color = android.graphics.Color.rgb(24, 23, 26) // Fondo
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            for (i in 0 until 12) {
                val angle = i * (360f / 12f)
                canvas.save()
                canvas.rotate(angle, size / 2f, size / 2f)
                paint.color = if (i % 2 == 0) android.graphics.Color.rgb(0, 164, 166) else android.graphics.Color.rgb(223, 172, 56)
                canvas.drawOval(size / 2f - 18f, size / 2f - 95f, size / 2f + 18f, size / 2f - 20f, paint)
                canvas.restore()
            }
            paint.color = android.graphics.Color.rgb(198, 40, 40) // Rubí
            canvas.drawCircle(size / 2f, size / 2f, 26f, paint)
            paint.color = android.graphics.Color.rgb(250, 249, 246) // Perla
            canvas.drawCircle(size / 2f, size / 2f, 10f, paint)
        }
        1 -> { // Mandala Solar Dorado
            val shader = RadialGradient(
                size / 2f, size / 2f, size / 2f,
                intArrayOf(android.graphics.Color.rgb(247, 209, 56), android.graphics.Color.rgb(238, 90, 61), android.graphics.Color.rgb(25, 23, 26)),
                floatArrayOf(0.15f, 0.65f, 1.0f),
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            paint.shader = null
            paint.color = android.graphics.Color.rgb(255, 255, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas.drawCircle(size / 2f, size / 2f, 40f, paint)
            canvas.drawCircle(size / 2f, size / 2f, 80f, paint)
        }
        else -> { // Atardecer Marino
            paint.color = android.graphics.Color.rgb(26, 54, 126) // Azul Cobalto
            canvas.drawRect(0f, 0f, size.toFloat(), size * 0.55f, paint)
            paint.color = android.graphics.Color.rgb(0, 164, 166) // Turquesa
            canvas.drawRect(0f, size * 0.55f, size.toFloat(), size.toFloat(), paint)
            paint.color = android.graphics.Color.rgb(238, 90, 61) // Coral
            canvas.drawCircle(size / 2f, size * 0.52f, 42f, paint)
            paint.color = android.graphics.Color.rgb(247, 209, 56) // Amarillo
            canvas.drawCircle(size / 2f, size * 0.50f, 28f, paint)
        }
    }
    return bmp
}

