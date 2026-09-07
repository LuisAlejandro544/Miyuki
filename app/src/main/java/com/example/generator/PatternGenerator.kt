package com.example.generator

import com.example.data.model.BeadPaletteTheme
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.data.model.CuratedPalettes
import com.example.data.model.MiyukiCatalog
import kotlin.math.abs
import kotlin.math.sin

enum class GeneratorStyle(
    val title: String,
    val description: String
) {
    CHEVRON("Chevron (V-Shape)", "Elegantes flechas angulares en zigzag continuo"),
    DIAMOND("Rombos Étnicos", "Diamantes concéntricos inspirados en joyería tradicional"),
    GRECAS("Grecas Geométricas", "Patrón escalonado tipo meandro azteca o griego"),
    WAVES("Ondas Flotantes", "Líneas onduladas continuas que fluyen a lo largo de la pulsera"),
    GRADIENT("Degradé Suave", "Transición cromática fluida entre los colores de la paleta"),
    BOHO_STRIPES("Rayas Étnicas Boho", "Combinación asimétrica de franjas y acentos metálicos")
}

object PatternGenerator {

    fun generate(
        title: String,
        style: GeneratorStyle,
        technique: BeadTechnique,
        columns: Int = 11,
        rows: Int = 65,
        palette: BeadPaletteTheme = CuratedPalettes.BOHO_TURQUESA,
        symmetry: Boolean = true
    ): BeadPattern {
        val beads = palette.beads
        val bgBead = beads.getOrElse(beads.lastIndex) { MiyukiCatalog.DB_0010 }
        val primaryBead = beads.getOrElse(0) { MiyukiCatalog.DB_0031 }
        val secondaryBead = beads.getOrElse(1) { MiyukiCatalog.DB_0651 }
        val accentBead = beads.getOrElse(2) { MiyukiCatalog.DB_0729 }
        val goldBead = beads.find { it.code == "DB-0031" } ?: MiyukiCatalog.DB_0031

        val grid = MutableList(columns * rows) { bgBead.colorInt }

        val centerCol = (columns - 1) / 2.0f

        when (style) {
            GeneratorStyle.CHEVRON -> {
                val cycle = maxOf(4, columns / 2 + 2)
                for (r in 0 until rows) {
                    val phase = r % cycle
                    for (c in 0 until columns) {
                        val distFromCenter = abs(c - centerCol)
                        val offset = (distFromCenter.toInt() + phase) % cycle
                        val color = when (offset) {
                            0 -> goldBead.colorInt
                            1 -> primaryBead.colorInt
                            2 -> secondaryBead.colorInt
                            3 -> accentBead.colorInt
                            else -> bgBead.colorInt
                        }
                        grid[r * columns + c] = color
                    }
                }
            }

            GeneratorStyle.DIAMOND -> {
                val diamondHeight = maxOf(6, (columns * 1.2f).toInt())
                for (r in 0 until rows) {
                    val rInDiamond = r % diamondHeight
                    val normR = if (rInDiamond < diamondHeight / 2) rInDiamond else diamondHeight - 1 - rInDiamond
                    val targetSpread = normR * (columns / 2f) / (diamondHeight / 2f)

                    for (c in 0 until columns) {
                        val dist = abs(c - centerCol)
                        val color = when {
                            abs(dist - targetSpread) < 0.6f -> goldBead.colorInt
                            dist < targetSpread - 1.5f -> primaryBead.colorInt
                            dist < targetSpread -> secondaryBead.colorInt
                            c == 0 || c == columns - 1 -> goldBead.colorInt
                            else -> bgBead.colorInt
                        }
                        grid[r * columns + c] = color
                    }
                }
            }

            GeneratorStyle.GRECAS -> {
                val stepSize = 8
                for (r in 0 until rows) {
                    val step = (r / stepSize) % 4
                    val localR = r % stepSize
                    for (c in 0 until columns) {
                        val isBorder = c == 0 || c == columns - 1 || c == 1 || c == columns - 2
                        val isMeander = when (step) {
                            0 -> localR == 0 || c == localR
                            1 -> localR == stepSize - 1 || c == columns - 1 - localR
                            2 -> localR == stepSize / 2 || c == localR
                            else -> c == columns / 2 || localR == 2
                        }
                        val color = when {
                            isBorder -> goldBead.colorInt
                            isMeander -> primaryBead.colorInt
                            (r + c) % 4 == 0 -> accentBead.colorInt
                            else -> bgBead.colorInt
                        }
                        grid[r * columns + c] = color
                    }
                }
            }

            GeneratorStyle.WAVES -> {
                val frequency = 0.35
                for (r in 0 until rows) {
                    val waveCenter = centerCol + sin(r * frequency) * (columns / 3f)
                    for (c in 0 until columns) {
                        val dist = abs(c - waveCenter)
                        val color = when {
                            dist < 0.8f -> goldBead.colorInt
                            dist < 1.8f -> primaryBead.colorInt
                            dist < 2.8f -> secondaryBead.colorInt
                            dist < 3.8f -> accentBead.colorInt
                            else -> bgBead.colorInt
                        }
                        grid[r * columns + c] = color
                    }
                }
            }

            GeneratorStyle.GRADIENT -> {
                val paletteSize = beads.size
                for (r in 0 until rows) {
                    val progress = (sin(r / 6.0) + 1.0) / 2.0 // oscillation 0.0 to 1.0
                    val beadIndex = (progress * (paletteSize - 1)).toInt().coerceIn(0, paletteSize - 1)
                    val baseColor = beads[beadIndex].colorInt

                    for (c in 0 until columns) {
                        val isEdge = c == 0 || c == columns - 1
                        val color = if (isEdge) goldBead.colorInt else baseColor
                        grid[r * columns + c] = color
                    }
                }
            }

            GeneratorStyle.BOHO_STRIPES -> {
                val sequence = listOf(1, 3, 1, 2, 1, 4, 1, 2)
                var currR = 0
                var seqIdx = 0
                while (currR < rows) {
                    val blockLen = sequence[seqIdx % sequence.size]
                    val blockColor = beads[seqIdx % beads.size].colorInt
                    for (i in 0 until blockLen) {
                        val r = currR + i
                        if (r < rows) {
                            for (c in 0 until columns) {
                                val isGoldAccent = (c == 0 || c == columns - 1 || (c == columns / 2 && r % 2 == 0))
                                val color = if (isGoldAccent) goldBead.colorInt else blockColor
                                grid[r * columns + c] = color
                            }
                        }
                    }
                    currR += blockLen
                    seqIdx++
                }
            }
        }

        // Apply symmetry if requested
        if (symmetry) {
            for (r in 0 until rows) {
                for (c in 0 until columns / 2) {
                    val mirrorCol = columns - 1 - c
                    grid[r * columns + mirrorCol] = grid[r * columns + c]
                }
            }
        }

        return BeadPattern(
            title = title,
            description = "Pulsera ${technique.title} generada con estilo ${style.title} y paleta ${palette.name}",
            technique = technique,
            columns = columns,
            rows = rows,
            grid = grid,
            category = style.title,
            difficulty = if (columns > 13) "Avanzado" else if (columns > 9) "Intermedio" else "Principiante",
            wristSizeCm = 16.0f
        )
    }
}
