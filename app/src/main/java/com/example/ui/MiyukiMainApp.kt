package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.GuideCalculatorScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiGoldDark
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.PatternViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiyukiMainApp(viewModel: PatternViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allPatterns by viewModel.allPatterns.collectAsStateWithLifecycle()
    val generatedPattern by viewModel.generatedPattern.collectAsStateWithLifecycle()
    val generatorStyle by viewModel.generatorStyle.collectAsStateWithLifecycle()
    val generatorTechnique by viewModel.generatorTechnique.collectAsStateWithLifecycle()
    val generatorColumns by viewModel.generatorColumns.collectAsStateWithLifecycle()
    val generatorRows by viewModel.generatorRows.collectAsStateWithLifecycle()
    val generatorPalette by viewModel.generatorPalette.collectAsStateWithLifecycle()
    val generatorSymmetry by viewModel.generatorSymmetry.collectAsStateWithLifecycle()

    val earringStyle by viewModel.earringStyle.collectAsStateWithLifecycle()
    val earringBaseWidth by viewModel.earringBaseWidth.collectAsStateWithLifecycle()
    val earringMaxFringe by viewModel.earringMaxFringe.collectAsStateWithLifecycle()
    val earringMinFringe by viewModel.earringMinFringe.collectAsStateWithLifecycle()
    val earringPalette by viewModel.earringPalette.collectAsStateWithLifecycle()

    val luaScript by viewModel.luaScript.collectAsStateWithLifecycle()
    val luaConsoleOutput by viewModel.luaConsoleOutput.collectAsStateWithLifecycle()

    val editingPattern by viewModel.editingPattern.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val selectedBead by viewModel.selectedBead.collectAsStateWithLifecycle()
    val isMirrorMode by viewModel.isMirrorMode.collectAsStateWithLifecycle()

    val trackingPattern by viewModel.trackingPattern.collectAsStateWithLifecycle()
    val currentRowIndex by viewModel.currentRowIndex.collectAsStateWithLifecycle()

    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.CATALOG -> "Miyuki Patrones"
                            AppTab.GENERATOR -> "Generador de Pulseras"
                            AppTab.EDITOR -> "Taller de Diseño"
                            AppTab.TRACKER -> "Modo Tejedor Fila a Fila"
                            AppTab.GUIDE -> "Calculadora & Guía"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.CATALOG,
                    onClick = { viewModel.setTab(AppTab.CATALOG) },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Patrones") },
                    label = { Text("Patrones") },
                    modifier = Modifier.testTag("nav_tab_catalog"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MiyukiGold.copy(alpha = 0.25f),
                        selectedIconColor = MiyukiGoldDark,
                        selectedTextColor = MiyukiGoldDark
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.GENERATOR,
                    onClick = { viewModel.setTab(AppTab.GENERATOR) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Generador") },
                    label = { Text("Generador") },
                    modifier = Modifier.testTag("nav_tab_generator"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MiyukiGold.copy(alpha = 0.25f),
                        selectedIconColor = MiyukiGoldDark,
                        selectedTextColor = MiyukiGoldDark
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.EDITOR,
                    onClick = { viewModel.setTab(AppTab.EDITOR) },
                    icon = { Icon(Icons.Default.Brush, contentDescription = "Taller") },
                    label = { Text("Taller") },
                    modifier = Modifier.testTag("nav_tab_editor"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MiyukiGold.copy(alpha = 0.25f),
                        selectedIconColor = MiyukiGoldDark,
                        selectedTextColor = MiyukiGoldDark
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TRACKER,
                    onClick = { viewModel.setTab(AppTab.TRACKER) },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Tejedor") },
                    label = { Text("Tejedor") },
                    modifier = Modifier.testTag("nav_tab_tracker"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MiyukiGold.copy(alpha = 0.25f),
                        selectedIconColor = MiyukiGoldDark,
                        selectedTextColor = MiyukiGoldDark
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.GUIDE,
                    onClick = { viewModel.setTab(AppTab.GUIDE) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Guía") },
                    label = { Text("Guía") },
                    modifier = Modifier.testTag("nav_tab_guide"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MiyukiGold.copy(alpha = 0.25f),
                        selectedIconColor = MiyukiGoldDark,
                        selectedTextColor = MiyukiGoldDark
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.CATALOG -> {
                    CatalogScreen(
                        patterns = allPatterns,
                        onPatternSelectedForTracking = { viewModel.startTrackingPattern(it) },
                        onPatternSelectedForEditing = { viewModel.loadPatternIntoEditor(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeletePattern = { viewModel.deletePattern(it) },
                        onCreateNewPattern = { title, tech, cols, rows ->
                            viewModel.createNewPattern(title, tech, cols, rows)
                        },
                        onNavigateToGenerator = { viewModel.setTab(AppTab.GENERATOR) }
                    )
                }

                AppTab.GENERATOR -> {
                    GeneratorScreen(
                        generatedPattern = generatedPattern,
                        currentStyle = generatorStyle,
                        currentTechnique = generatorTechnique,
                        currentColumns = generatorColumns,
                        currentRows = generatorRows,
                        currentPalette = generatorPalette,
                        isSymmetry = generatorSymmetry,
                        onUpdateParams = { title, style, tech, cols, rows, pal, sym ->
                            viewModel.updateGeneratorParams(title, style, tech, cols, rows, pal, sym)
                        },
                        onSavePattern = { viewModel.saveGeneratedPattern() },
                        onOpenInEditor = { viewModel.loadGeneratedIntoEditor() },
                        onStartTracking = { viewModel.startTrackingPattern(it) },
                        earringStyle = earringStyle,
                        earringBaseWidth = earringBaseWidth,
                        earringMaxFringe = earringMaxFringe,
                        earringMinFringe = earringMinFringe,
                        earringPalette = earringPalette,
                        onUpdateEarringStyle = { viewModel.setEarringStyle(it) },
                        onUpdateEarringWidth = { viewModel.setEarringBaseWidth(it) },
                        onUpdateEarringMaxFringe = { viewModel.setEarringMaxFringe(it) },
                        onUpdateEarringMinFringe = { viewModel.setEarringMinFringe(it) },
                        onUpdateEarringPalette = { viewModel.setEarringPalette(it) },
                        onGenerateEarringRust = { viewModel.generateEarringPatternWithRust() },
                        luaScript = luaScript,
                        luaConsoleOutput = luaConsoleOutput,
                        onUpdateLuaScript = { viewModel.setLuaScript(it) },
                        onRunLuaScript = { cols, rows -> viewModel.runLuaScriptForGrid(cols, rows) },
                        onExecuteLuaDirect = { viewModel.executeLuaSnippetDirect(it) }
                    )
                }

                AppTab.EDITOR -> {
                    EditorScreen(
                        pattern = editingPattern,
                        selectedTool = selectedTool,
                        selectedBead = selectedBead,
                        isMirrorMode = isMirrorMode,
                        onToolSelected = { viewModel.setSelectedTool(it) },
                        onBeadSelected = { viewModel.setSelectedBead(it) },
                        onToggleMirrorMode = { viewModel.toggleMirrorMode() },
                        onCellInteracted = { col, row -> viewModel.onEditorCellInteracted(col, row) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onClearCanvas = { viewModel.clearEditorCanvas() },
                        onSavePattern = { viewModel.saveEditorPattern(it) },
                        onStartTracking = { viewModel.startTrackingPattern(it) }
                    )
                }

                AppTab.TRACKER -> {
                    TrackerScreen(
                        pattern = trackingPattern,
                        currentRow = currentRowIndex,
                        onNextRow = { viewModel.nextRow() },
                        onPreviousRow = { viewModel.previousRow() },
                        onSetRow = { viewModel.setRow(it) },
                        onReset = { viewModel.resetTracker() }
                    )
                }

                AppTab.GUIDE -> {
                    GuideCalculatorScreen()
                }
            }
        }
    }
}
