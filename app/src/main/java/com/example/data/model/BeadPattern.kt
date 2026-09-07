package com.example.data.model

enum class BeadTechnique(
    val title: String,
    val subtitle: String,
    val description: String
) {
    LOOM(
        "Telar (Loom)",
        "Cuadrícula Rectangular",
        "Técnica con hilos de urdimbre tensados en telar. Las cuentas van perfectamente alineadas en filas y columnas verticales."
    ),
    PEYOTE(
        "Peyote Par / Impar",
        "Puntada Escalonada",
        "Tejido a mano alzada sin telar. Cada fila queda desfasada media cuenta respecto a la anterior en patrón de ladrillo entrelazado."
    ),
    BRICK_STITCH(
        "Puntada Ladrillo",
        "Acabado Escalonado Horizontal",
        "Ideal para figuras, aretes y terminaciones triangulares en los extremos de pulseras de telar."
    );

    val isStaggered: Boolean get() = this == PEYOTE || this == BRICK_STITCH
}

data class BeadCountItem(
    val bead: MiyukiBead,
    val count: Int,
    val gramsEstimate: Float // Miyuki 11/0 has approx 110 beads per gram
)

data class BeadPattern(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val technique: BeadTechnique,
    val columns: Int,
    val rows: Int,
    val grid: List<Int>, // Size = columns * rows, contains colorInt
    val category: String = "Geométrico",
    val difficulty: String = "Intermedio",
    val isFavorite: Boolean = false,
    val isPreset: Boolean = false,
    val wristSizeCm: Float = 16.0f,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalBeads: Int
        get() = grid.count { it != 0 && it != -1 }

    fun getBeadAt(col: Int, row: Int): Int {
        if (col !in 0 until columns || row !in 0 until rows) return 0
        val index = row * columns + col
        return if (index in grid.indices) grid[index] else 0
    }

    fun getRowBeads(row: Int): List<Int> {
        if (row !in 0 until rows) return emptyList()
        val startIndex = row * columns
        val endIndex = minOf(startIndex + columns, grid.size)
        return if (startIndex in grid.indices) grid.subList(startIndex, endIndex) else emptyList()
    }

    /**
     * Calculates the exact tally of Miyuki Delica beads needed to weave this bracelet.
     */
    fun calculateBeadCounts(): List<BeadCountItem> {
        val colorCounts = mutableMapOf<Int, Int>()
        for (color in grid) {
            if (color != 0 && color != -1) {
                colorCounts[color] = (colorCounts[color] ?: 0) + 1
            }
        }
        return colorCounts.map { (colorInt, count) ->
            val bead = MiyukiCatalog.findClosest(colorInt)
            BeadCountItem(
                bead = bead,
                count = count,
                gramsEstimate = count / 105f // ~105-110 delicas per gram
            )
        }.sortedByDescending { it.count }
    }

    /**
     * Compresses the grid to a compact hex string format for Room persistence.
     */
    fun serializeGrid(): String {
        return grid.joinToString(",") { it.toString() }
    }

    companion object {
        fun deserializeGrid(serialized: String, expectedSize: Int): List<Int> {
            if (serialized.isBlank()) return List(expectedSize) { MiyukiCatalog.DB_0201.colorInt }
            val parsed = serialized.split(",").mapNotNull { it.trim().toIntOrNull() }
            return if (parsed.size == expectedSize) parsed else {
                val list = parsed.toMutableList()
                while (list.size < expectedSize) list.add(MiyukiCatalog.DB_0201.colorInt)
                list.take(expectedSize)
            }
        }
    }
}
