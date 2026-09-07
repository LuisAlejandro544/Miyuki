package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.ui.components.BeadPatternPreview
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiGoldDark
import com.example.ui.theme.MiyukiTurquoise

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    patterns: List<BeadPattern>,
    onPatternSelectedForTracking: (BeadPattern) -> Unit,
    onPatternSelectedForEditing: (BeadPattern) -> Unit,
    onToggleFavorite: (BeadPattern) -> Unit,
    onDeletePattern: (BeadPattern) -> Unit,
    onCreateNewPattern: (title: String, technique: BeadTechnique, columns: Int, rows: Int) -> Unit,
    onNavigateToGenerator: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTechniqueFilter by remember { mutableStateOf<BeadTechnique?>(null) }
    var onlyFavorites by remember { mutableStateOf(false) }

    // Dialog state for materials / shopping list
    var patternForMaterials by remember { mutableStateOf<BeadPattern?>(null) }

    // Dialog state for creating a new custom blank pattern
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredPatterns = patterns.filter { pattern ->
        val matchesQuery = pattern.title.contains(searchQuery, ignoreCase = true) ||
                pattern.description.contains(searchQuery, ignoreCase = true) ||
                pattern.category.contains(searchQuery, ignoreCase = true)
        val matchesTechnique = selectedTechniqueFilter == null || pattern.technique == selectedTechniqueFilter
        val matchesFav = !onlyFavorites || pattern.isFavorite
        matchesQuery && matchesTechnique && matchesFav
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header & Welcome Banner
            item {
                CatalogHeaderBanner(
                    onNewBlankPattern = { showCreateDialog = true },
                    onGoToGenerator = onNavigateToGenerator
                )
            }

            // Search Bar & Filter Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_search_input"),
                        placeholder = { Text("Buscar por nombre, técnica o estilo...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedTechniqueFilter == null && !onlyFavorites,
                            onClick = {
                                selectedTechniqueFilter = null
                                onlyFavorites = false
                            },
                            label = { Text("Todos (${patterns.size})") },
                            leadingIcon = if (selectedTechniqueFilter == null && !onlyFavorites) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = selectedTechniqueFilter == BeadTechnique.LOOM,
                            onClick = {
                                selectedTechniqueFilter = if (selectedTechniqueFilter == BeadTechnique.LOOM) null else BeadTechnique.LOOM
                            },
                            label = { Text("Telar (Loom)") }
                        )

                        FilterChip(
                            selected = selectedTechniqueFilter == BeadTechnique.PEYOTE,
                            onClick = {
                                selectedTechniqueFilter = if (selectedTechniqueFilter == BeadTechnique.PEYOTE) null else BeadTechnique.PEYOTE
                            },
                            label = { Text("Peyote") }
                        )

                        FilterChip(
                            selected = onlyFavorites,
                            onClick = { onlyFavorites = !onlyFavorites },
                            label = { Text("Favoritos") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (onlyFavorites) Color(0xFFE2584A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredPatterns.size} ${if (filteredPatterns.size == 1) "patrón encontrado" else "patrones disponibles"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Pattern Cards List
            if (filteredPatterns.isEmpty()) {
                item {
                    EmptyCatalogState(
                        hasQuery = searchQuery.isNotBlank() || selectedTechniqueFilter != null || onlyFavorites,
                        onClearFilters = {
                            searchQuery = ""
                            selectedTechniqueFilter = null
                            onlyFavorites = false
                        },
                        onGenerate = onNavigateToGenerator
                    )
                }
            } else {
                items(filteredPatterns, key = { it.id }) { pattern ->
                    PatternCardItem(
                        pattern = pattern,
                        onStartTracking = { onPatternSelectedForTracking(pattern) },
                        onEdit = { onPatternSelectedForEditing(pattern) },
                        onToggleFav = { onToggleFavorite(pattern) },
                        onShowMaterials = { patternForMaterials = pattern },
                        onDelete = { onDeletePattern(pattern) }
                    )
                }
            }
        }

        // Floating Action Buttons
        FloatingActionButton(
            onClick = onNavigateToGenerator,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("fab_generator"),
            containerColor = MiyukiGold,
            contentColor = Color(0xFF1B191B)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Generador")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Material Details Sheet Dialog
    patternForMaterials?.let { pattern ->
        MaterialsDialog(
            pattern = pattern,
            onDismiss = { patternForMaterials = null }
        )
    }

    // Create Blank Pattern Dialog
    if (showCreateDialog) {
        CreatePatternDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, tech, cols, rows ->
                onCreateNewPattern(title, tech, cols, rows)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CatalogHeaderBanner(
    onNewBlankPattern: () -> Unit,
    onGoToGenerator: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MiyukiGoldDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Patrones de Pulseras Miyuki",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Delicas 11/0 en técnicas de Telar y Peyote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Explora diseños listos para tejer con guía paso a paso fila por fila, o crea tus propias combinaciones geométricas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGoToGenerator,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("banner_generate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyukiGoldDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generar", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onNewBlankPattern,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("banner_new_canvas_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lienzo en blanco", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PatternCardItem(
    pattern: BeadPattern,
    onStartTracking: () -> Unit,
    onEdit: () -> Unit,
    onToggleFav: () -> Unit,
    onShowMaterials: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pattern_card_${pattern.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Title & Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pattern.technique.title} • ${pattern.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MiyukiTurquoise,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier.testTag("fav_btn_${pattern.id}")
                    ) {
                        Icon(
                            imageVector = if (pattern.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (pattern.isFavorite) "Quitar favorito" else "Guardar favorito",
                            tint = if (pattern.isFavorite) Color(0xFFE2584A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!pattern.isPreset) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_btn_${pattern.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar patrón",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Preview Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bead bracelet preview band
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BeadPatternPreview(
                        pattern = pattern,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Specs & Bead Counts
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = pattern.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecBadge(label = "Ancho", value = "${pattern.columns} cuentas")
                        SpecBadge(label = "Largo", value = "${pattern.rows} filas")
                        SpecBadge(label = "Muñeca", value = "~${pattern.wristSizeCm.toInt()} cm")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: ~${pattern.totalBeads} delicas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = onShowMaterials,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Materiales", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main CTA: Start weaving with the row tracker
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("track_btn_${pattern.id}"),
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

                // Secondary CTA: Edit / customize in canvas workshop
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("edit_btn_${pattern.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyCatalogState(
    hasQuery: Boolean,
    onClearFilters: () -> Unit,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasQuery) "No se encontraron patrones" else "No hay patrones guardados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasQuery) "Prueba con otros términos de búsqueda o elimina los filtros." else "Crea un diseño desde cero o utiliza el generador para obtener una pulsera automática.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (hasQuery) {
            Button(onClick = onClearFilters) {
                Text("Limpiar filtros")
            }
        } else {
            Button(onClick = onGenerate) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Pulsera")
            }
        }
    }
}

@Composable
private fun MaterialsDialog(
    pattern: BeadPattern,
    onDismiss: () -> Unit
) {
    val beadCounts = remember(pattern) { pattern.calculateBeadCounts() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lista de Cuentas Miyuki",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Materiales para tejer '${pattern.title}':",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(beadCounts) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(item.bead.composeColor)
                                    .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.bead.code,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${item.bead.name} (${item.bead.finish.label})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.count} delicas",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "~${String.format("%.1f", item.gramsEstimate)} g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total de Cuentas:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${pattern.totalBeads} delicas (~${String.format("%.1f", pattern.totalBeads / 105f)} g)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun CreatePatternDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, technique: BeadTechnique, columns: Int, rows: Int) -> Unit
) {
    var title by remember { mutableStateOf("Mi Pulsera Personalizada") }
    var technique by remember { mutableStateOf(BeadTechnique.LOOM) }
    var columns by remember { mutableStateOf(11) }
    var rows by remember { mutableStateOf(64) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo Lienzo de Pulsera", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del proyecto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Técnica de tejido:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = technique == BeadTechnique.LOOM,
                        onClick = { technique = BeadTechnique.LOOM },
                        label = { Text("Telar (Loom)") }
                    )
                    FilterChip(
                        selected = technique == BeadTechnique.PEYOTE,
                        onClick = { technique = BeadTechnique.PEYOTE },
                        label = { Text("Peyote") }
                    )
                }

                Column {
                    Text(
                        text = "Ancho: $columns cuentas (~${String.format("%.1f", columns * 0.15f)} cm)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = columns.toFloat(),
                        onValueChange = { columns = it.toInt() },
                        valueRange = 5f..25f,
                        steps = 19
                    )
                }

                Column {
                    Text(
                        text = "Largo: $rows filas (~${String.format("%.1f", rows * 0.16f)} cm para muñeca)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = rows.toFloat(),
                        onValueChange = { rows = it.toInt() },
                        valueRange = 30f..100f,
                        steps = 69
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, technique, columns, rows) }
            ) {
                Text("Crear Lienzo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
