package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeadTechnique
import com.example.nativebridge.MiyukiNativeBridge
import com.example.ui.theme.MiyukiGold
import com.example.ui.theme.MiyukiGoldDark
import com.example.ui.theme.MiyukiTurquoise

enum class ClaspType(val label: String, val allowanceCm: Float) {
    MACRAME("Nudo Corredizo Macramé", 1.5f),
    TERMINAL_TUBE("Terminal Tubo Deslizable Miyuki", 0.8f),
    MAGNETIC("Broche Imán / Mosquetón", 1.2f),
    ELASTIC("Pulsera Elástica (Sin Broche)", 0.0f)
}

@Composable
fun GuideCalculatorScreen() {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Calculadora, 1: Técnicas, 2: Motor Nativo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedSection,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calculadora", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Técnicas", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("C++/Rust/Lua", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedSection) {
            0 -> WristCalculatorSection()
            1 -> TechniquesGuideSection()
            2 -> NativeEngineSection()
        }
    }
}

@Composable
private fun WristCalculatorSection() {
    var wristCm by remember { mutableFloatStateOf(16.0f) }
    var selectedClasp by remember { mutableStateOf(ClaspType.MACRAME) }
    var selectedTechnique by remember { mutableStateOf(BeadTechnique.LOOM) }
    var braceletColumns by remember { mutableIntStateOf(11) }

    val netWeaveCm = (wristCm - selectedClasp.allowanceCm).coerceAtLeast(8.0f)
    val rowHeightCm = if (selectedTechnique == BeadTechnique.LOOM) 0.16f else 0.135f
    val requiredRows = (netWeaveCm / rowHeightCm).toInt()
    val totalBeads = requiredRows * braceletColumns
    val gramsEstimate = totalBeads / 105f
    val threadEstimateMeters = if (selectedTechnique == BeadTechnique.LOOM) {
        ((braceletColumns + 1) * 0.40f) + (requiredRows * (braceletColumns * 0.02f))
    } else {
        requiredRows * braceletColumns * 0.025f + 1.2f
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = MiyukiGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculadora de Medidas Exactas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Calibra el largo tejido, número exacto de filas, peso en gramos y metros de hilo según el contorno de tu muñeca.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("1. Contorno de Muñeca: ${String.format("%.1f", wristCm)} cm", fontWeight = FontWeight.Bold)
                Slider(
                    value = wristCm,
                    onValueChange = { wristCm = it },
                    valueRange = 13.0f..22.0f,
                    steps = 17
                )

                Text("2. Tipo de Broche o Cierre", fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ClaspType.values().forEach { clasp ->
                        FilterChip(
                            selected = selectedClasp == clasp,
                            onClick = { selectedClasp = clasp },
                            label = { Text("${clasp.label} (-${clasp.allowanceCm} cm)") }
                        )
                    }
                }

                Text("3. Técnica de Tejido", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedTechnique == BeadTechnique.LOOM,
                        onClick = { selectedTechnique = BeadTechnique.LOOM },
                        label = { Text("Telar (Loom - 1.6mm/fila)") }
                    )
                    FilterChip(
                        selected = selectedTechnique == BeadTechnique.PEYOTE,
                        onClick = { selectedTechnique = BeadTechnique.PEYOTE },
                        label = { Text("Peyote (1.35mm/fila)") }
                    )
                }

                Text("4. Ancho Deseado (Columnas): $braceletColumns cuentas", fontWeight = FontWeight.Bold)
                Slider(
                    value = braceletColumns.toFloat(),
                    onValueChange = { braceletColumns = it.toInt() },
                    valueRange = 5f..25f,
                    steps = 19
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MiyukiGold.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Resultados de Estimación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiyukiGoldDark)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Largo de tejido puro:")
                    Text("${String.format("%.1f", netWeaveCm)} cm", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Filas exactas a tejer:")
                    Text("$requiredRows filas", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total de cuentas Delica 11/0:")
                    Text("~$totalBeads cuentas", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Peso estimado de delicas:")
                    Text("${String.format("%.2f", gramsEstimate)} gramos", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hilo estimado (Miyuki Beading Thread):")
                    Text("${String.format("%.2f", threadEstimateMeters)} metros", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TechniquesGuideSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TechniqueCard(
            title = "Telar Miyuki (Bead Loom)",
            badge = "Ideal para principiantes y pulseras simétricas",
            description = "Se montan hilos de urdimbre en un telar de madera o metal. Las delicas se ensartan en la aguja y se pasan por debajo de la urdimbre, encajando una cuenta entre cada par de hilos. Luego se regresa la aguja por dentro de las cuentas por encima de los hilos para fijarlas.",
            steps = listOf(
                "Monta (N + 1) hilos de urdimbre para una pulsera de N columnas.",
                "Mantén una tensión firme y uniforme en todos los hilos.",
                "Pasa la aguja por debajo con las cuentas alineadas con el dedo índice.",
                "Regresa la aguja por encima de los hilos de urdimbre pasando por el centro de cada delica.",
                "Remata con nudos dobles escondidos o terminales tipo tubo corredero."
            ),
            tips = "Usa cera para hilo de cera de abejas o hilo Miyuki pre-encerado para evitar que se deshilache."
        )

        TechniqueCard(
            title = "Peyote Par e Impar",
            badge = "Textura orgánica tipo tela sin necesidad de telar",
            description = "Técnica a mano alzada donde las cuentas quedan escalonadas como ladrillos horizontales. Es ligera, flexible y muy resistente para pulseras y anillos.",
            steps = listOf(
                "Peyote Par: La cantidad de columnas es número par. El giro en cada extremo es directo y muy fluido.",
                "Peyote Impar: Número impar de columnas. Requiere un nudo de giro en figura de ocho en uno de los extremos.",
                "Trabaja con una tensión moderada: si aprietas demasiado, la pulsera se curvará.",
                "Usa aguja fina para abalorios calibre #10 o #12 (Miyuki / Tulip)."
            ),
            tips = "Comienza tejiendo las filas 1 y 2 juntas como fila base antes de subir a la fila 3."
        )

        TechniqueCard(
            title = "Brick Stitch (Punto Ladrillo)",
            badge = "Perfecto para Zarcillos, aretes y formas triangulares",
            description = "Las cuentas se tejen sobre los puentes de hilo de la fila anterior. Permite aumentos y disminuciones fila a fila con gran facilidad, creando siluetas triangulares y colgantes de flecos.",
            steps = listOf(
                "Crea una fila base usando punto escalera (Ladder Stitch).",
                "Pasa la aguja por debajo del puente de hilo formado entre dos cuentas de la fila anterior.",
                "Vuelve a subir por la cuenta para bloquearla en su posición escalonada.",
                "Para zarcillos con flecos, los flecos se suspenden de cada cuenta de la fila inferior usando tensión relajada para que caigan libremente."
            ),
            tips = "Al tejer flecos, deja un milímetro de holgura de hilo al volver a subir por el fleco para que tenga caída natural y no quede rígido."
        )
    }
}

@Composable
private fun NativeEngineSection() {
    var luaTestSnippet by remember {
        mutableStateOf("local a = 15\nlocal b = 27\nprint('Calculando en Lua 5.4 C:')\nprint(string.format('%d + %d = %d', a, b, a + b))\nprint('¡Motor nativo verificado!')")
    }
    var consoleResult by remember { mutableStateOf("") }
    var engineStatus by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MiyukiGold.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MiyukiGoldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Arquitectura Nativa del Sistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiyukiGoldDark)
                }

                Text(
                    "Esta aplicación integra directamente código nativo compilado con Android NDK y CMake sin wrappers:",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("C++20 JNI Bridge (libmiyuki_native.so)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lua 5.4.6 Oficial ANSI C (PUC-Rio, sin wrappers)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rust Native Geometry & Earring Math Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Diagnostic Button
        Button(
            onClick = {
                engineStatus = if (MiyukiNativeBridge.isNativeLoaded()) {
                    MiyukiNativeBridge.getNativeEngineStatus()
                } else {
                    "Error nativo: ${MiyukiNativeBridge.getLoadError()}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MiyukiGoldDark, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verificar Estado del Motor Nativo (JNI)", fontWeight = FontWeight.Bold)
        }

        if (engineStatus.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF212121))
                    .padding(14.dp)
            ) {
                Text(
                    text = engineStatus,
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Lua C REPL
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MiyukiTurquoise)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consola Interactiva Lua 5.4 C", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    "Prueba cualquier código o expresión en el motor ANSI C oficial de Lua:",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = luaTestSnippet,
                    onValueChange = { luaTestSnippet = it },
                    label = { Text("Código Lua") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        consoleResult = if (MiyukiNativeBridge.isNativeLoaded()) {
                            MiyukiNativeBridge.executeLuaSnippet(luaTestSnippet)
                        } else {
                            "Error: ${MiyukiNativeBridge.getLoadError()}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ejecutar en Lua C")
                }

                if (consoleResult.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = consoleResult,
                            color = Color(0xFF64FFDA),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechniqueCard(
    title: String,
    badge: String,
    description: String,
    steps: List<String>,
    tips: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MiyukiTurquoise.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badge, style = MaterialTheme.typography.labelSmall, color = MiyukiTurquoise, fontWeight = FontWeight.Bold)
                }
            }

            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider()

            Text("Pasos clave:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
            steps.forEachIndexed { _, step ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text("•", fontWeight = FontWeight.Bold, color = MiyukiGold, modifier = Modifier.padding(end = 8.dp))
                    Text(step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Text("💡 Consejo Pro: $tips", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
