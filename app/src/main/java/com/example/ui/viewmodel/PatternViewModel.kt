package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BeadPaletteTheme
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.data.model.CuratedPalettes
import com.example.data.model.MiyukiBead
import com.example.data.model.MiyukiCatalog
import com.example.data.repository.PatternRepository
import com.example.data.repository.PresetPatterns
import com.example.generator.GeneratorStyle
import com.example.generator.PatternGenerator
import com.example.nativebridge.MiyukiNativeBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    CATALOG("Patrones"),
    GENERATOR("Generador"),
    EDITOR("Taller / Diseñador"),
    TRACKER("Modo Tejedor"),
    GUIDE("Calculadora & Guía")
}

enum class EditorTool(val label: String) {
    BRUSH("Pincel"),
    ERASER("Borrador"),
    BUCKET("Relleno"),
    PIPETTE("Gotero")
}

class PatternViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatternRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PatternRepository(db.patternDao(), viewModelScope)
    }

    val allPatterns: StateFlow<List<BeadPattern>> = repository.allPatterns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.CATALOG)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Status / User feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // -------------------------------------------------------------
    // GENERATOR STATE
    // -------------------------------------------------------------
    private val _generatorTitle = MutableStateFlow("Pulsera Étnica Miyuki")
    val generatorTitle: StateFlow<String> = _generatorTitle.asStateFlow()

    private val _generatorStyle = MutableStateFlow(GeneratorStyle.CHEVRON)
    val generatorStyle: StateFlow<GeneratorStyle> = _generatorStyle.asStateFlow()

    private val _generatorTechnique = MutableStateFlow(BeadTechnique.PEYOTE)
    val generatorTechnique: StateFlow<BeadTechnique> = _generatorTechnique.asStateFlow()

    private val _generatorColumns = MutableStateFlow(11)
    val generatorColumns: StateFlow<Int> = _generatorColumns.asStateFlow()

    private val _generatorRows = MutableStateFlow(64)
    val generatorRows: StateFlow<Int> = _generatorRows.asStateFlow()

    private val _generatorPalette = MutableStateFlow(CuratedPalettes.BOHO_TURQUESA)
    val generatorPalette: StateFlow<BeadPaletteTheme> = _generatorPalette.asStateFlow()

    private val _generatorSymmetry = MutableStateFlow(true)
    val generatorSymmetry: StateFlow<Boolean> = _generatorSymmetry.asStateFlow()

    private val _generatedPattern = MutableStateFlow<BeadPattern>(
        PatternGenerator.generate(
            title = "Pulsera Étnica Miyuki",
            style = GeneratorStyle.CHEVRON,
            technique = BeadTechnique.PEYOTE,
            columns = 11,
            rows = 64,
            palette = CuratedPalettes.BOHO_TURQUESA,
            symmetry = true
        )
    )
    val generatedPattern: StateFlow<BeadPattern> = _generatedPattern.asStateFlow()

    // -------------------------------------------------------------
    // EDITOR STATE
    // -------------------------------------------------------------
    private val _editingPattern = MutableStateFlow<BeadPattern>(PresetPatterns.INITIAL_PATTERNS.first())
    val editingPattern: StateFlow<BeadPattern> = _editingPattern.asStateFlow()

    private val _selectedTool = MutableStateFlow(EditorTool.BRUSH)
    val selectedTool: StateFlow<EditorTool> = _selectedTool.asStateFlow()

    private val _selectedBead = MutableStateFlow(MiyukiCatalog.DB_0031)
    val selectedBead: StateFlow<MiyukiBead> = _selectedBead.asStateFlow()

    private val _isMirrorMode = MutableStateFlow(true)
    val isMirrorMode: StateFlow<Boolean> = _isMirrorMode.asStateFlow()

    private val undoStack = mutableListOf<List<Int>>()
    private val redoStack = mutableListOf<List<Int>>()

    // -------------------------------------------------------------
    // TRACKER STATE (Row-by-Row Crafting Assistant)
    // -------------------------------------------------------------
    private val _trackingPattern = MutableStateFlow<BeadPattern>(PresetPatterns.INITIAL_PATTERNS.first())
    val trackingPattern: StateFlow<BeadPattern> = _trackingPattern.asStateFlow()

    private val _currentRowIndex = MutableStateFlow(0)
    val currentRowIndex: StateFlow<Int> = _currentRowIndex.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleFavorite(pattern: BeadPattern) {
        viewModelScope.launch {
            repository.toggleFavorite(pattern.id, pattern.isFavorite)
        }
    }

    fun deletePattern(pattern: BeadPattern) {
        viewModelScope.launch {
            repository.deletePatternById(pattern.id)
            _snackbarMessage.value = "Patrón '${pattern.title}' eliminado"
        }
    }

    // -------------------------------------------------------------
    // GENERATOR ACTIONS
    // -------------------------------------------------------------
    fun updateGeneratorParams(
        title: String? = null,
        style: GeneratorStyle? = null,
        technique: BeadTechnique? = null,
        columns: Int? = null,
        rows: Int? = null,
        palette: BeadPaletteTheme? = null,
        symmetry: Boolean? = null
    ) {
        if (title != null) _generatorTitle.value = title
        if (style != null) _generatorStyle.value = style
        if (technique != null) _generatorTechnique.value = technique
        if (columns != null) _generatorColumns.value = columns
        if (rows != null) _generatorRows.value = rows
        if (palette != null) _generatorPalette.value = palette
        if (symmetry != null) _generatorSymmetry.value = symmetry

        _generatedPattern.value = PatternGenerator.generate(
            title = _generatorTitle.value,
            style = _generatorStyle.value,
            technique = _generatorTechnique.value,
            columns = _generatorColumns.value,
            rows = _generatorRows.value,
            palette = _generatorPalette.value,
            symmetry = _generatorSymmetry.value
        )
    }

    fun saveGeneratedPattern() {
        viewModelScope.launch {
            val patternToSave = _generatedPattern.value.copy(
                id = 0L,
                createdAt = System.currentTimeMillis()
            )
            repository.savePattern(patternToSave)
            _snackbarMessage.value = "Patrón guardado en tus patrones"
        }
    }

    fun loadGeneratedIntoEditor() {
        loadPatternIntoEditor(_generatedPattern.value.copy(id = 0L))
        _currentTab.value = AppTab.EDITOR
    }

    fun startTrackingPattern(pattern: BeadPattern) {
        _trackingPattern.value = pattern
        _currentRowIndex.value = 0
        _currentTab.value = AppTab.TRACKER
    }

    // -------------------------------------------------------------
    // EDITOR ACTIONS
    // -------------------------------------------------------------
    fun loadPatternIntoEditor(pattern: BeadPattern) {
        _editingPattern.value = pattern
        undoStack.clear()
        redoStack.clear()
        _currentTab.value = AppTab.EDITOR
    }

    fun createNewPattern(title: String, technique: BeadTechnique, columns: Int, rows: Int) {
        val blankGrid = List(columns * rows) { MiyukiCatalog.DB_0201.colorInt }
        _editingPattern.value = BeadPattern(
            id = 0L,
            title = title,
            description = "Pulsera artesanal creada en el taller",
            technique = technique,
            columns = columns,
            rows = rows,
            grid = blankGrid,
            category = "Personalizado",
            difficulty = "Intermedio"
        )
        undoStack.clear()
        redoStack.clear()
        _currentTab.value = AppTab.EDITOR
    }

    fun setSelectedTool(tool: EditorTool) {
        _selectedTool.value = tool
    }

    fun setSelectedBead(bead: MiyukiBead) {
        _selectedBead.value = bead
    }

    fun toggleMirrorMode() {
        _isMirrorMode.value = !_isMirrorMode.value
    }

    fun onEditorCellInteracted(col: Int, row: Int) {
        val pattern = _editingPattern.value
        if (col !in 0 until pattern.columns || row !in 0 until pattern.rows) return

        val currentGrid = pattern.grid
        when (_selectedTool.value) {
            EditorTool.PIPETTE -> {
                val color = pattern.getBeadAt(col, row)
                _selectedBead.value = MiyukiCatalog.findClosest(color)
                _selectedTool.value = EditorTool.BRUSH
            }

            EditorTool.BUCKET -> {
                saveUndoState(currentGrid)
                val targetColor = pattern.getBeadAt(col, row)
                val replacementColor = _selectedBead.value.colorInt
                if (targetColor != replacementColor) {
                    val newGrid = floodFill(
                        grid = currentGrid.toMutableList(),
                        columns = pattern.columns,
                        rows = pattern.rows,
                        startCol = col,
                        startRow = row,
                        targetColor = targetColor,
                        replacementColor = replacementColor
                    )
                    _editingPattern.value = pattern.copy(grid = newGrid)
                }
            }

            EditorTool.BRUSH, EditorTool.ERASER -> {
                val newColor = if (_selectedTool.value == EditorTool.ERASER) {
                    MiyukiCatalog.DB_0201.colorInt // Blank/pearl canvas background
                } else {
                    _selectedBead.value.colorInt
                }

                if (pattern.getBeadAt(col, row) != newColor) {
                    saveUndoState(currentGrid)
                    val newGrid = currentGrid.toMutableList()
                    newGrid[row * pattern.columns + col] = newColor

                    // If mirror mode is active, also paint the symmetric column
                    if (_isMirrorMode.value) {
                        val mirrorCol = pattern.columns - 1 - col
                        newGrid[row * pattern.columns + mirrorCol] = newColor
                    }

                    _editingPattern.value = pattern.copy(grid = newGrid)
                }
            }
        }
    }

    private fun saveUndoState(grid: List<Int>) {
        undoStack.add(grid)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_editingPattern.value.grid)
            _editingPattern.value = _editingPattern.value.copy(grid = previous)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_editingPattern.value.grid)
            _editingPattern.value = _editingPattern.value.copy(grid = next)
        }
    }

    fun clearEditorCanvas() {
        val pattern = _editingPattern.value
        saveUndoState(pattern.grid)
        val blankGrid = List(pattern.columns * pattern.rows) { MiyukiCatalog.DB_0201.colorInt }
        _editingPattern.value = pattern.copy(grid = blankGrid)
    }

    fun saveEditorPattern(title: String) {
        viewModelScope.launch {
            val updated = _editingPattern.value.copy(
                title = title.ifBlank { "Mi Pulsera Miyuki" },
                createdAt = System.currentTimeMillis()
            )
            val newId = repository.savePattern(updated)
            _editingPattern.value = updated.copy(id = newId)
            _snackbarMessage.value = "Diseño guardado exitosamente"
        }
    }

    // -------------------------------------------------------------
    // TRACKER ACTIONS
    // -------------------------------------------------------------
    fun nextRow() {
        val maxRow = _trackingPattern.value.rows - 1
        if (_currentRowIndex.value < maxRow) {
            _currentRowIndex.value += 1
        }
    }

    fun previousRow() {
        if (_currentRowIndex.value > 0) {
            _currentRowIndex.value -= 1
        }
    }

    fun setRow(row: Int) {
        val maxRow = _trackingPattern.value.rows - 1
        _currentRowIndex.value = row.coerceIn(0, maxRow)
    }

    fun resetTracker() {
        _currentRowIndex.value = 0
    }

    private fun floodFill(
        grid: MutableList<Int>,
        columns: Int,
        rows: Int,
        startCol: Int,
        startRow: Int,
        targetColor: Int,
        replacementColor: Int
    ): List<Int> {
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(startCol, startRow))
        val visited = BooleanArray(columns * rows)

        while (queue.isNotEmpty()) {
            val (c, r) = queue.removeFirst()
            val index = r * columns + c
            if (visited[index]) continue
            visited[index] = true

            if (grid[index] == targetColor) {
                grid[index] = replacementColor

                // Neighbors (up, down, left, right)
                if (c > 0) queue.add(Pair(c - 1, r))
                if (c < columns - 1) queue.add(Pair(c + 1, r))
                if (r > 0) queue.add(Pair(c, r - 1))
                if (r < rows - 1) queue.add(Pair(c, r + 1))
            }
        }
        return grid
    }

    // -------------------------------------------------------------
    // ZARCILLOS (EARRINGS) - RUST NATIVE ENGINE
    // -------------------------------------------------------------
    private val _earringStyle = MutableStateFlow(0)
    val earringStyle: StateFlow<Int> = _earringStyle.asStateFlow()

    private val _earringBaseWidth = MutableStateFlow(9)
    val earringBaseWidth: StateFlow<Int> = _earringBaseWidth.asStateFlow()

    private val _earringMaxFringe = MutableStateFlow(24)
    val earringMaxFringe: StateFlow<Int> = _earringMaxFringe.asStateFlow()

    private val _earringMinFringe = MutableStateFlow(8)
    val earringMinFringe: StateFlow<Int> = _earringMinFringe.asStateFlow()

    private val _earringPalette = MutableStateFlow<BeadPaletteTheme>(CuratedPalettes.ATARDECER_FUEGO)
    val earringPalette: StateFlow<BeadPaletteTheme> = _earringPalette.asStateFlow()

    fun setEarringStyle(style: Int) { _earringStyle.value = style }
    fun setEarringBaseWidth(width: Int) { _earringBaseWidth.value = width.coerceIn(5, 21) }
    fun setEarringMaxFringe(max: Int) { _earringMaxFringe.value = max.coerceIn(10, 45) }
    fun setEarringMinFringe(min: Int) { _earringMinFringe.value = min.coerceIn(2, _earringMaxFringe.value - 2) }
    fun setEarringPalette(palette: BeadPaletteTheme) { _earringPalette.value = palette }

    fun generateEarringPatternWithRust() {
        val width = _earringBaseWidth.value
        val maxFringe = _earringMaxFringe.value
        val minFringe = _earringMinFringe.value
        val style = _earringStyle.value
        val palette = _earringPalette.value

        val fringeLengths = if (MiyukiNativeBridge.isNativeLoaded()) {
            MiyukiNativeBridge.calculateEarringFringesRust(width, maxFringe, minFringe, style)
        } else {
            IntArray(width) { maxFringe }
        }

        val triangleRows = if (MiyukiNativeBridge.isNativeLoaded()) {
            MiyukiNativeBridge.calculateEarringTriangleRowsRust(width)
        } else {
            width
        }

        val totalRows = triangleRows + maxFringe
        val grid = MutableList(width * totalRows) { 0x00000000 }

        val colorMain = palette.beads.getOrElse(0) { MiyukiCatalog.DB_0031 }.colorInt
        val colorSecondary = palette.beads.getOrElse(1) { MiyukiCatalog.DB_0201 }.colorInt
        val colorAccent = palette.beads.getOrElse(2) { MiyukiCatalog.DB_0042 }.colorInt
        val colorHighlight = palette.beads.getOrElse(3) { MiyukiCatalog.DB_0010 }.colorInt

        // 1. Build Triangle Cap
        for (r in 0 until triangleRows) {
            val beadsInRow = 1 + r
            val startCol = (width - beadsInRow) / 2
            val endCol = startCol + beadsInRow
            for (c in startCol until endCol) {
                val color = if (r == 0) colorHighlight else if ((r + c) % 2 == 0) colorMain else colorSecondary
                grid[r * width + c] = color
            }
        }

        // 2. Build Dangle Fringes with Rust calculated lengths
        for (c in 0 until width) {
            val len = fringeLengths.getOrElse(c) { maxFringe }
            for (f in 0 until len) {
                val row = triangleRows + f
                if (row < totalRows) {
                    val color = when {
                        f == len - 1 -> colorHighlight
                        f == len - 2 -> colorAccent
                        f % 3 == 0 -> colorSecondary
                        else -> colorMain
                    }
                    grid[row * width + c] = color
                }
            }
        }

        val styleName = when (style) {
            0 -> "V Clásica"
            1 -> "Chevron Invertido"
            2 -> "Cascada Ondulada"
            3 -> "Escalonado Diagonal"
            else -> "Rombo Diamante"
        }

        val pattern = BeadPattern(
            id = System.currentTimeMillis(),
            title = "Zarcillo Rust $styleName ($width col x $totalRows filas)",
            description = "Zarcillo con copa triangular Brick Stitch y caída geométrica calculada con Rust.",
            technique = BeadTechnique.BRICK_STITCH,
            columns = width,
            rows = totalRows,
            grid = grid,
            category = "Zarcillos Rust"
        )

        _generatedPattern.value = pattern
        _snackbarMessage.value = "Zarcillo generado con Motor Nativo Rust ($styleName)"
    }

    // -------------------------------------------------------------
    // LUA C SCRIPT STUDIO (OFFICIAL ANSI C LUA 5.4.6)
    // -------------------------------------------------------------
    private val defaultLuaScript = """
-- Generador de Patrón Miyuki en Lua C Oficial 5.4
-- Parámetros disponibles: COLUMNS, ROWS
-- Retorna el color ARGB en cada celda (col, row)

function getBead(col, row)
    local gold = 0xFFFFD700
    local turquoise = 0xFF20B2AA
    local navy = 0xFF191970
    local coral = 0xFFFF6F61
    local white = 0xFFFDF5E6

    local sum = col + row
    if (col == 0 or col == COLUMNS - 1) then
        return gold
    elseif (sum % 4 == 0) then
        return turquoise
    elseif (sum % 4 == 2) then
        return coral
    elseif ((col - row) % 6 == 0) then
        return navy
    else
        return white
    end
end

print(string.format("Script Lua C 5.4 ejecutado para %dx%d cuentas.", COLUMNS, ROWS))
""".trimIndent()

    private val _luaScript = MutableStateFlow(defaultLuaScript)
    val luaScript: StateFlow<String> = _luaScript.asStateFlow()

    private val _luaConsoleOutput = MutableStateFlow("Listo. Presiona 'Ejecutar en Lua C' para procesar el script.")
    val luaConsoleOutput: StateFlow<String> = _luaConsoleOutput.asStateFlow()

    fun setLuaScript(script: String) { _luaScript.value = script }

    fun runLuaScriptForGrid(columns: Int = 11, rows: Int = 32) {
        if (!MiyukiNativeBridge.isNativeLoaded()) {
            _luaConsoleOutput.value = "Error: Biblioteca nativa no cargada: ${MiyukiNativeBridge.getLoadError()}"
            return
        }

        val gridArray = MiyukiNativeBridge.executeLuaPatternScript(_luaScript.value, columns, rows)
        val console = MiyukiNativeBridge.executeLuaSnippet(_luaScript.value)
        _luaConsoleOutput.value = console

        val pattern = BeadPattern(
            id = System.currentTimeMillis(),
            title = "Patrón Algorítmico Lua C ($columns x $rows)",
            description = "Patrón paramétrico generado con el intérprete nativo Lua 5.4 C.",
            technique = BeadTechnique.PEYOTE,
            columns = columns,
            rows = rows,
            grid = gridArray.toList(),
            category = "Generativo Lua"
        )
        _generatedPattern.value = pattern
        _snackbarMessage.value = "Patrón calculado con éxito en Lua 5.4 C Oficial"
    }

    fun executeLuaSnippetDirect(code: String) {
        if (!MiyukiNativeBridge.isNativeLoaded()) {
            _luaConsoleOutput.value = "Error nativo: ${MiyukiNativeBridge.getLoadError()}"
            return
        }
        val out = MiyukiNativeBridge.executeLuaSnippet(code)
        _luaConsoleOutput.value = out
    }

    // -------------------------------------------------------------
    // PHOTO TO PATTERN ENGINE (NATIVE C++20 CIELAB & FLOYD-STEINBERG)
    // -------------------------------------------------------------
    private val _photoTargetCols = MutableStateFlow(24)
    val photoTargetCols: StateFlow<Int> = _photoTargetCols.asStateFlow()

    private val _photoTargetRows = MutableStateFlow(48)
    val photoTargetRows: StateFlow<Int> = _photoTargetRows.asStateFlow()

    private val _photoBrightness = MutableStateFlow(0f)
    val photoBrightness: StateFlow<Float> = _photoBrightness.asStateFlow()

    private val _photoContrast = MutableStateFlow(1.15f)
    val photoContrast: StateFlow<Float> = _photoContrast.asStateFlow()

    private val _photoDithering = MutableStateFlow(true)
    val photoDithering: StateFlow<Boolean> = _photoDithering.asStateFlow()

    private val _photoMaxColors = MutableStateFlow(12)
    val photoMaxColors: StateFlow<Int> = _photoMaxColors.asStateFlow()

    private val _isPhotoProcessing = MutableStateFlow(false)
    val isPhotoProcessing: StateFlow<Boolean> = _isPhotoProcessing.asStateFlow()

    fun updatePhotoParams(
        cols: Int? = null,
        rows: Int? = null,
        brightness: Float? = null,
        contrast: Float? = null,
        dithering: Boolean? = null,
        maxColors: Int? = null
    ) {
        if (cols != null) _photoTargetCols.value = cols
        if (rows != null) _photoTargetRows.value = rows
        if (brightness != null) _photoBrightness.value = brightness
        if (contrast != null) _photoContrast.value = contrast
        if (dithering != null) _photoDithering.value = dithering
        if (maxColors != null) _photoMaxColors.value = maxColors
    }

    fun convertBitmapToPattern(bitmap: android.graphics.Bitmap, patternTitle: String = "Foto a Patrón Miyuki") {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _isPhotoProcessing.value = true
            try {
                val srcWidth = bitmap.width
                val srcHeight = bitmap.height
                val totalPixels = srcWidth * srcHeight
                val pixels = IntArray(totalPixels)
                bitmap.getPixels(pixels, 0, srcWidth, 0, 0, srcWidth, srcHeight)

                val cols = _photoTargetCols.value
                val rows = _photoTargetRows.value
                val brightness = _photoBrightness.value
                val contrast = _photoContrast.value
                val dithering = _photoDithering.value
                val maxColors = _photoMaxColors.value

                val quantizedGrid = MiyukiNativeBridge.convertPhotoToPatternNative(
                    srcPixels = pixels,
                    srcWidth = srcWidth,
                    srcHeight = srcHeight,
                    targetCols = cols,
                    targetRows = rows,
                    brightness = brightness,
                    contrast = contrast,
                    useDithering = dithering,
                    maxColors = maxColors
                )

                if (quantizedGrid.isNotEmpty()) {
                    val pattern = BeadPattern(
                        id = System.currentTimeMillis(),
                        title = patternTitle.ifBlank { "Patrón desde Foto ($cols x $rows)" },
                        description = "Patrón convertido desde imagen con colorimetría nativa CIELAB y tramado Floyd-Steinberg.",
                        technique = BeadTechnique.PEYOTE,
                        columns = cols,
                        rows = rows,
                        grid = quantizedGrid.toList(),
                        category = "Foto a Patrón"
                    )
                    _generatedPattern.value = pattern
                    _snackbarMessage.value = "¡Foto convertida a cuentas Miyuki con éxito!"
                } else {
                    _snackbarMessage.value = "Error al procesar la imagen con el motor nativo."
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isPhotoProcessing.value = false
            }
        }
    }

    // -------------------------------------------------------------
    // PDF & PATTERN CHART CALIBRATION ENGINE (NATIVE C++20 & RUST)
    // -------------------------------------------------------------
    private val _pdfSourceBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val pdfSourceBitmap: StateFlow<android.graphics.Bitmap?> = _pdfSourceBitmap.asStateFlow()

    private val _pdfPageCount = MutableStateFlow(1)
    val pdfPageCount: StateFlow<Int> = _pdfPageCount.asStateFlow()

    private val _pdfCurrentPage = MutableStateFlow(0)
    val pdfCurrentPage: StateFlow<Int> = _pdfCurrentPage.asStateFlow()

    private val _cropLeft = MutableStateFlow(0.11f)
    val cropLeft: StateFlow<Float> = _cropLeft.asStateFlow()

    private val _cropTop = MutableStateFlow(0.16f)
    val cropTop: StateFlow<Float> = _cropTop.asStateFlow()

    private val _cropRight = MutableStateFlow(0.89f)
    val cropRight: StateFlow<Float> = _cropRight.asStateFlow()

    private val _cropBottom = MutableStateFlow(0.93f)
    val cropBottom: StateFlow<Float> = _cropBottom.asStateFlow()

    private val _calibratedCols = MutableStateFlow(16)
    val calibratedCols: StateFlow<Int> = _calibratedCols.asStateFlow()

    private val _calibratedRows = MutableStateFlow(40)
    val calibratedRows: StateFlow<Int> = _calibratedRows.asStateFlow()

    private val _calibratedTechnique = MutableStateFlow(BeadTechnique.PEYOTE)
    val calibratedTechnique: StateFlow<BeadTechnique> = _calibratedTechnique.asStateFlow()

    private val _calibratedSampleWindow = MutableStateFlow(0.50f)
    val calibratedSampleWindow: StateFlow<Float> = _calibratedSampleWindow.asStateFlow()

    private val _calibratedBrightness = MutableStateFlow(0f)
    val calibratedBrightness: StateFlow<Float> = _calibratedBrightness.asStateFlow()

    private val _calibratedContrast = MutableStateFlow(1.15f)
    val calibratedContrast: StateFlow<Float> = _calibratedContrast.asStateFlow()

    private val _calibratedMaxColors = MutableStateFlow(8)
    val calibratedMaxColors: StateFlow<Int> = _calibratedMaxColors.asStateFlow()

    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    private val _isAutoDetectingGrid = MutableStateFlow(false)
    val isAutoDetectingGrid: StateFlow<Boolean> = _isAutoDetectingGrid.asStateFlow()

    private val _chartDocumentName = MutableStateFlow("Documento de Patrón")
    val chartDocumentName: StateFlow<String> = _chartDocumentName.asStateFlow()

    fun updateCalibrationCrop(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
        if (left != null) _cropLeft.value = left.coerceIn(0.0f, (_cropRight.value - 0.05f).coerceAtLeast(0.01f))
        if (top != null) _cropTop.value = top.coerceIn(0.0f, (_cropBottom.value - 0.05f).coerceAtLeast(0.01f))
        if (right != null) _cropRight.value = right.coerceIn((_cropLeft.value + 0.05f).coerceAtMost(0.99f), 1.0f)
        if (bottom != null) _cropBottom.value = bottom.coerceIn((_cropTop.value + 0.05f).coerceAtMost(0.99f), 1.0f)
    }

    fun updateCalibrationSettings(
        cols: Int? = null,
        rows: Int? = null,
        technique: BeadTechnique? = null,
        sampleWindow: Float? = null,
        brightness: Float? = null,
        contrast: Float? = null,
        maxColors: Int? = null
    ) {
        if (cols != null) _calibratedCols.value = cols.coerceIn(4, 80)
        if (rows != null) _calibratedRows.value = rows.coerceIn(6, 150)
        if (technique != null) _calibratedTechnique.value = technique
        if (sampleWindow != null) _calibratedSampleWindow.value = sampleWindow.coerceIn(0.2f, 0.85f)
        if (brightness != null) _calibratedBrightness.value = brightness.coerceIn(-60f, 60f)
        if (contrast != null) _calibratedContrast.value = contrast.coerceIn(0.5f, 2.5f)
        if (maxColors != null) _calibratedMaxColors.value = maxColors.coerceIn(2, 30)
    }

    fun loadSamplePdfChart() {
        val sample = com.example.util.PdfPatternExtractor.createSampleChartDocument()
        _pdfSourceBitmap.value = sample
        _pdfPageCount.value = 1
        _pdfCurrentPage.value = 0
        _chartDocumentName.value = "Muestra PDF: Pulsera Étnica Sol (16x40)"
        _cropLeft.value = 0.11f
        _cropTop.value = 0.16f
        _cropRight.value = 0.89f
        _cropBottom.value = 0.93f
        _calibratedCols.value = 16
        _calibratedRows.value = 40
        _calibratedTechnique.value = BeadTechnique.PEYOTE
        _snackbarMessage.value = "Plantilla PDF cargada. ¡Puedes calibrarla o probar auto-detección!"
    }

    private var _lastPdfUri: android.net.Uri? = null

    fun loadPdfOrImageUri(context: android.content.Context, uri: android.net.Uri) {
        _lastPdfUri = uri
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (com.example.util.PdfPatternExtractor.isPdf(context, uri)) {
                    val count = com.example.util.PdfPatternExtractor.getPdfPageCount(context, uri)
                    _pdfPageCount.value = count
                    _pdfCurrentPage.value = 0
                    val bmp = com.example.util.PdfPatternExtractor.renderPdfPage(context, uri, 0)
                    if (bmp != null) {
                        _pdfSourceBitmap.value = bmp
                        _chartDocumentName.value = "PDF (${bmp.width}x${bmp.height} - Pág 1 de $count)"
                        _snackbarMessage.value = "PDF rasterizado con éxito. Ajusta el recuadro a la cuadrícula."
                    } else {
                        _snackbarMessage.value = "No se pudo renderizar la página del PDF."
                    }
                } else {
                    val bmp = com.example.util.PdfPatternExtractor.decodeImageUri(context, uri)
                    if (bmp != null) {
                        _pdfSourceBitmap.value = bmp
                        _pdfPageCount.value = 1
                        _pdfCurrentPage.value = 0
                        _chartDocumentName.value = "Imagen Gráfico (${bmp.width}x${bmp.height})"
                        _snackbarMessage.value = "Gráfico cargado. Ajusta el recuadro a la cuadrícula."
                    } else {
                        _snackbarMessage.value = "Error al abrir la imagen del patrón."
                    }
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error al abrir archivo: ${e.localizedMessage}"
            }
        }
    }

    fun changePdfPage(context: android.content.Context, newPage: Int) {
        val uri = _lastPdfUri ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val bmp = com.example.util.PdfPatternExtractor.renderPdfPage(context, uri, newPage)
            if (bmp != null) {
                _pdfSourceBitmap.value = bmp
                _pdfCurrentPage.value = newPage
                _chartDocumentName.value = "PDF - Pág ${newPage + 1} de ${_pdfPageCount.value}"
            }
        }
    }

    fun autoDetectGridWithRust() {
        val src = _pdfSourceBitmap.value ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _isAutoDetectingGrid.value = true
            try {
                val cropped = com.example.util.PdfPatternExtractor.cropBitmap(
                    src,
                    _cropLeft.value,
                    _cropTop.value,
                    _cropRight.value,
                    _cropBottom.value
                )
                val w = cropped.width
                val h = cropped.height
                val total = w * h
                val pixels = IntArray(total)
                cropped.getPixels(pixels, 0, w, 0, 0, w, h)

                val detected = MiyukiNativeBridge.analyzeChartGridRustNative(pixels, w, h)
                if (detected.size >= 2 && detected[0] > 0 && detected[1] > 0) {
                    _calibratedCols.value = detected[0].coerceIn(6, 70)
                    _calibratedRows.value = detected[1].coerceIn(10, 120)
                    _snackbarMessage.value = "Rust detectó: ${detected[0]} columnas y ${detected[1]} filas"
                } else {
                    _snackbarMessage.value = "No se detectaron líneas claras. Ajusta manualmente."
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error en auto-detección: ${e.localizedMessage}"
            } finally {
                _isAutoDetectingGrid.value = false
            }
        }
    }

    fun calibrateAndExtractPattern(title: String = "Patrón Calibrado desde PDF") {
        val src = _pdfSourceBitmap.value
        if (src == null) {
            _snackbarMessage.value = "Por favor carga un PDF o gráfico primero."
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _isCalibrating.value = true
            try {
                val cropped = com.example.util.PdfPatternExtractor.cropBitmap(
                    src,
                    _cropLeft.value,
                    _cropTop.value,
                    _cropRight.value,
                    _cropBottom.value
                )
                val w = cropped.width
                val h = cropped.height
                val total = w * h
                val pixels = IntArray(total)
                cropped.getPixels(pixels, 0, w, 0, 0, w, h)

                val cols = _calibratedCols.value
                val rows = _calibratedRows.value
                val tech = _calibratedTechnique.value
                val techCode = when (tech) {
                    BeadTechnique.PEYOTE -> 1
                    BeadTechnique.BRICK_STITCH -> 2
                    BeadTechnique.LOOM -> 0
                }

                val sampleRatio = _calibratedSampleWindow.value
                val brightness = _calibratedBrightness.value
                val contrast = _calibratedContrast.value
                val maxColors = _calibratedMaxColors.value

                val resultGrid = MiyukiNativeBridge.calibrateAndSamplePatternNative(
                    srcPixels = pixels,
                    srcWidth = w,
                    srcHeight = h,
                    targetCols = cols,
                    targetRows = rows,
                    technique = techCode,
                    sampleWindowRatio = sampleRatio,
                    brightness = brightness,
                    contrast = contrast,
                    maxColors = maxColors
                )

                if (resultGrid.isNotEmpty()) {
                    val pattern = BeadPattern(
                        id = System.currentTimeMillis(),
                        title = title.ifBlank { "Patrón PDF Calibrado ($cols x $rows)" },
                        description = "Calibrado y muestreado en C++20 CIELAB desde ${_chartDocumentName.value}. Técnica: ${tech.title}.",
                        technique = tech,
                        columns = cols,
                        rows = rows,
                        grid = resultGrid.toList(),
                        category = "Importado PDF"
                    )
                    _generatedPattern.value = pattern
                    _snackbarMessage.value = "¡Patrón calibrado con éxito! Listo en el Taller y Modo Tejedor."
                } else {
                    _snackbarMessage.value = "Error al calibrar cuadrícula con el motor C++."
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isCalibrating.value = false
            }
        }
    }

    fun getNativeEngineDiagnostic(): String {
        return if (MiyukiNativeBridge.isNativeLoaded()) {
            MiyukiNativeBridge.getNativeEngineStatus()
        } else {
            "Error al cargar motor nativo: ${MiyukiNativeBridge.getLoadError()}"
        }
    }
}
