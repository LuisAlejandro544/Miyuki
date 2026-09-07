package com.example.data.repository

import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.data.model.CuratedPalettes
import com.example.generator.GeneratorStyle
import com.example.generator.PatternGenerator

object PresetPatterns {

    val INITIAL_PATTERNS: List<BeadPattern> by lazy {
        listOf(
            PatternGenerator.generate(
                title = "Azteca Dorado & Turquesa",
                style = GeneratorStyle.DIAMOND,
                technique = BeadTechnique.LOOM,
                columns = 11,
                rows = 64,
                palette = CuratedPalettes.BOHO_TURQUESA,
                symmetry = true
            ).copy(
                id = 1L,
                category = "Étnico / Geométrico",
                difficulty = "Intermedio",
                isPreset = true,
                isFavorite = true,
                description = "Diseño tradicional de rombos concéntricos con turquesa Capri, oro 24k y coral brillante sobre negro mate."
            ),

            PatternGenerator.generate(
                title = "Chevron Sunset Boho",
                style = GeneratorStyle.CHEVRON,
                technique = BeadTechnique.PEYOTE,
                columns = 13,
                rows = 68,
                palette = CuratedPalettes.ATARDECER_FUEGO,
                symmetry = true
            ).copy(
                id = 2L,
                category = "Boho Chic",
                difficulty = "Intermedio",
                isPreset = true,
                isFavorite = true,
                description = "Flechas dinámicas en técnica peyote par con degradado cálido desde rojo rubí hasta oro galvanizado."
            ),

            PatternGenerator.generate(
                title = "Sakura Floral Rosa & Menta",
                style = GeneratorStyle.DIAMOND,
                technique = BeadTechnique.PEYOTE,
                columns = 15,
                rows = 72,
                palette = CuratedPalettes.SAKURA_PASTEL,
                symmetry = true
            ).copy(
                id = 3L,
                category = "Floral / Pastel",
                difficulty = "Avanzado",
                isPreset = true,
                isFavorite = false,
                description = "Paleta primaveral inspirada en los cerezos de Kioto con rosa blush pastel, menta y perla japonesa."
            ),

            PatternGenerator.generate(
                title = "Grecas Noche Imperial",
                style = GeneratorStyle.GRECAS,
                technique = BeadTechnique.LOOM,
                columns = 9,
                rows = 60,
                palette = CuratedPalettes.ORO_Y_NOCHE,
                symmetry = true
            ).copy(
                id = 4L,
                category = "Minimalista / Lujo",
                difficulty = "Principiante",
                isPreset = true,
                isFavorite = false,
                description = "Meandros dorados de alto contraste sobre fondo negro ónix. Ideal para principiantes en telar."
            ),

            PatternGenerator.generate(
                title = "Ondas del Mediterráneo",
                style = GeneratorStyle.WAVES,
                technique = BeadTechnique.LOOM,
                columns = 11,
                rows = 62,
                palette = CuratedPalettes.OCEANO_PROFUNDO,
                symmetry = false
            ).copy(
                id = 5L,
                category = "Océano & Marina",
                difficulty = "Intermedio",
                isPreset = true,
                isFavorite = false,
                description = "Movimiento fluido de olas con azul cobalto, aguamarina luminosa y destellos de plata galvanizada."
            ),

            PatternGenerator.generate(
                title = "Esmeralda & Bronce Real",
                style = GeneratorStyle.BOHO_STRIPES,
                technique = BeadTechnique.PEYOTE,
                columns = 13,
                rows = 66,
                palette = CuratedPalettes.ESMERALDA_IMPERIAL,
                symmetry = true
            ).copy(
                id = 6L,
                category = "Étnico / Geométrico",
                difficulty = "Intermedio",
                isPreset = true,
                isFavorite = false,
                description = "Líneas de piedras preciosas con verde esmeralda profundo, oro y acentos de bronce antiguo."
            )
        )
    }
}
