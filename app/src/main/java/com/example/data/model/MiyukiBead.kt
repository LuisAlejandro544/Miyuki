package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class BeadFinish(val label: String) {
    OPAQUE("Opaco"),
    GALVANIZED("Galvanizado 24K"),
    METALLIC("Metálico"),
    LUSTER("Lustre"),
    MATTE("Mate"),
    SILK("Seda")
}

data class MiyukiBead(
    val code: String,
    val name: String,
    val colorInt: Int,
    val finish: BeadFinish = BeadFinish.OPAQUE
) {
    val composeColor: Color get() = Color(colorInt)
}

object MiyukiCatalog {
    // Official & Most Popular Miyuki Delica 11/0 Beads
    val DB_0010 = MiyukiBead("DB-0010", "Negro Mate", 0xFF18171A.toInt(), BeadFinish.MATTE)
    val DB_0031 = MiyukiBead("DB-0031", "Oro 24K Galvanizado", 0xFFDFAC38.toInt(), BeadFinish.GALVANIZED)
    val DB_0035 = MiyukiBead("DB-0035", "Plata Galvanizada", 0xFFD8DCE0.toInt(), BeadFinish.GALVANIZED)
    val DB_0200 = MiyukiBead("DB-0200", "Blanco Puro Opaco", 0xFFFAF9F6.toInt(), BeadFinish.OPAQUE)
    val DB_0201 = MiyukiBead("DB-0201", "Alabaster Perla", 0xFFECE7DB.toInt(), BeadFinish.LUSTER)
    val DB_0651 = MiyukiBead("DB-0651", "Turquesa Capri", 0xFF00A4A6.toInt(), BeadFinish.OPAQUE)
    val DB_0723 = MiyukiBead("DB-0723", "Rojo Rubí Opaco", 0xFFC62828.toInt(), BeadFinish.OPAQUE)
    val DB_0729 = MiyukiBead("DB-0729", "Coral Mandarina", 0xFFEE5A3D.toInt(), BeadFinish.OPAQUE)
    val DB_0166 = MiyukiBead("DB-0166", "Verde Esmeralda", 0xFF197A52.toInt(), BeadFinish.OPAQUE)
    val DB_0753 = MiyukiBead("DB-0753", "Azul Cobalto Real", 0xFF1A367E.toInt(), BeadFinish.OPAQUE)
    val DB_1530 = MiyukiBead("DB-1530", "Rosa Blush Pastel", 0xFFEAA6A0.toInt(), BeadFinish.OPAQUE)
    val DB_0410 = MiyukiBead("DB-0410", "Mostaza Cálido", 0xFFE09F25.toInt(), BeadFinish.OPAQUE)
    val DB_0656 = MiyukiBead("DB-0656", "Menta Pastel", 0xFF7DCFB6.toInt(), BeadFinish.OPAQUE)
    val DB_0042 = MiyukiBead("DB-0042", "Amatista Púrpura", 0xFF5E3573.toInt(), BeadFinish.METALLIC)
    val DB_0791 = MiyukiBead("DB-0791", "Bronce Metálico", 0xFF65493B.toInt(), BeadFinish.METALLIC)
    val DB_1496 = MiyukiBead("DB-1496", "Lavanda Suave", 0xFFBBA7CD.toInt(), BeadFinish.OPAQUE)
    val DB_0113 = MiyukiBead("DB-0113", "Aguamarina Cielo", 0xFF58B3C5.toInt(), BeadFinish.LUSTER)
    val DB_0725 = MiyukiBead("DB-0725", "Amarillo Canario", 0xFFF7D138.toInt(), BeadFinish.OPAQUE)
    val DB_0653 = MiyukiBead("DB-0653", "Verde Jade Oliva", 0xFF437A4B.toInt(), BeadFinish.OPAQUE)
    val DB_0310 = MiyukiBead("DB-0310", "Negro Ónix Brillante", 0xFF0D0D0E.toInt(), BeadFinish.OPAQUE)

    val ALL_BEADS = listOf(
        DB_0010, DB_0031, DB_0035, DB_0200, DB_0201, DB_0651,
        DB_0723, DB_0729, DB_0166, DB_0753, DB_1530, DB_0410,
        DB_0656, DB_0042, DB_0791, DB_1496, DB_0113, DB_0725,
        DB_0653, DB_0310
    )

    fun findByCode(code: String): MiyukiBead? = ALL_BEADS.find { it.code == code }

    fun findClosest(colorInt: Int): MiyukiBead {
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF

        return ALL_BEADS.minByOrNull { bead ->
            val br = (bead.colorInt shr 16) and 0xFF
            val bg = (bead.colorInt shr 8) and 0xFF
            val bb = bead.colorInt and 0xFF
            val dr = r - br
            val dg = g - bg
            val db = b - bb
            dr * dr + dg * dg + db * db
        } ?: DB_0010
    }
}

data class BeadPaletteTheme(
    val name: String,
    val description: String,
    val beads: List<MiyukiBead>
)

object CuratedPalettes {
    val ORO_Y_NOCHE = BeadPaletteTheme(
        "Oro & Noche",
        "Elegancia clásica en negro carbón con destellos de oro 24k y blanco puro",
        listOf(MiyukiCatalog.DB_0010, MiyukiCatalog.DB_0031, MiyukiCatalog.DB_0200, MiyukiCatalog.DB_0035)
    )

    val BOHO_TURQUESA = BeadPaletteTheme(
        "Boho Turquesa",
        "Inspiración del sudoeste con turquesa, coral, oro y crema alabastro",
        listOf(MiyukiCatalog.DB_0651, MiyukiCatalog.DB_0729, MiyukiCatalog.DB_0031, MiyukiCatalog.DB_0201, MiyukiCatalog.DB_0010)
    )

    val SAKURA_PASTEL = BeadPaletteTheme(
        "Sakura Pastel",
        "Tonos suaves primaverales en rosa blush, menta, blanco perla y lavanda",
        listOf(MiyukiCatalog.DB_1530, MiyukiCatalog.DB_0656, MiyukiCatalog.DB_0201, MiyukiCatalog.DB_1496, MiyukiCatalog.DB_0031)
    )

    val ATARDECER_FUEGO = BeadPaletteTheme(
        "Atardecer Fuego",
        "Gradiente cálido y vibrante con rubí, coral, mostaza y oro",
        listOf(MiyukiCatalog.DB_0723, MiyukiCatalog.DB_0729, MiyukiCatalog.DB_0410, MiyukiCatalog.DB_0031, MiyukiCatalog.DB_0010)
    )

    val ESMERALDA_IMPERIAL = BeadPaletteTheme(
        "Esmeralda Imperial",
        "Verde esmeralda profundo con oro galvanizado, bronce y blanco perla",
        listOf(MiyukiCatalog.DB_0166, MiyukiCatalog.DB_0031, MiyukiCatalog.DB_0791, MiyukiCatalog.DB_0201, MiyukiCatalog.DB_0010)
    )

    val OCEANO_PROFUNDO = BeadPaletteTheme(
        "Océano Profundo",
        "Gama de azules marinos con aguamarina, plata y blanco alabastro",
        listOf(MiyukiCatalog.DB_0753, MiyukiCatalog.DB_0113, MiyukiCatalog.DB_0035, MiyukiCatalog.DB_0200, MiyukiCatalog.DB_0010)
    )

    val ALL_PALETTES = listOf(
        ORO_Y_NOCHE, BOHO_TURQUESA, SAKURA_PASTEL, ATARDECER_FUEGO, ESMERALDA_IMPERIAL, OCEANO_PROFUNDO
    )
}
