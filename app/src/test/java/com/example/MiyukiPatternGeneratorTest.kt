package com.example

import com.example.data.model.BeadTechnique
import com.example.data.model.CuratedPalettes
import com.example.data.model.MiyukiCatalog
import com.example.generator.GeneratorStyle
import com.example.generator.PatternGenerator
import com.example.nativebridge.MiyukiNativeBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiyukiPatternGeneratorTest {

    @Test
    fun testPatternGeneratorCreatesCorrectDimensions() {
        val pattern = PatternGenerator.generate(
            title = "Test Chevron",
            style = GeneratorStyle.CHEVRON,
            technique = BeadTechnique.LOOM,
            columns = 11,
            rows = 64,
            palette = CuratedPalettes.ORO_Y_NOCHE,
            symmetry = true
        )

        assertEquals(11, pattern.columns)
        assertEquals(64, pattern.rows)
        assertEquals(11 * 64, pattern.grid.size)
        assertEquals(11 * 64, pattern.totalBeads)
        assertEquals(BeadTechnique.LOOM, pattern.technique)
    }

    @Test
    fun testPeyotePatternGeneration() {
        val pattern = PatternGenerator.generate(
            title = "Test Peyote Rombos",
            style = GeneratorStyle.DIAMOND,
            technique = BeadTechnique.PEYOTE,
            columns = 13,
            rows = 50,
            palette = CuratedPalettes.BOHO_TURQUESA,
            symmetry = true
        )

        assertEquals(13, pattern.columns)
        assertEquals(50, pattern.rows)
        assertEquals(13 * 50, pattern.grid.size)
    }

    @Test
    fun testMiyukiCatalogLookup() {
        val bead = MiyukiCatalog.findClosest(MiyukiCatalog.DB_0031.colorInt)
        assertEquals("DB-0031", bead.code)
    }

    @Test
    fun testRowExtraction() {
        val pattern = PatternGenerator.generate(
            title = "Test Row",
            style = GeneratorStyle.BOHO_STRIPES,
            technique = BeadTechnique.LOOM,
            columns = 7,
            rows = 30,
            palette = CuratedPalettes.SAKURA_PASTEL,
            symmetry = false
        )

        val row0 = pattern.getRowBeads(0)
        assertEquals(7, row0.size)
    }

    @Test
    fun testMaterialBeadCounts() {
        val pattern = PatternGenerator.generate(
            title = "Test Materials",
            style = GeneratorStyle.GRECAS,
            technique = BeadTechnique.LOOM,
            columns = 9,
            rows = 40,
            palette = CuratedPalettes.ESMERALDA_IMPERIAL,
            symmetry = true
        )

        val beadCounts = pattern.calculateBeadCounts()
        assertTrue(beadCounts.isNotEmpty())
        val sumCounts = beadCounts.sumOf { it.count }
        assertEquals(9 * 40, sumCounts)
    }

    @Test
    fun testNativeBridgeStatus() {
        // In local JVM test, isNativeLoaded() cleanly reports status or handles JVM fallback
        val isLoaded = MiyukiNativeBridge.isNativeLoaded()
        if (isLoaded) {
            val status = MiyukiNativeBridge.getNativeEngineStatus()
            assertNotNull(status)
            assertTrue(status.contains("Miyuki Native Engine"))
        } else {
            val err = MiyukiNativeBridge.getLoadError()
            assertNotNull(err)
        }
    }

    @Test
    fun testWordChartExtractionForPdf() {
        val pattern = PatternGenerator.generate(
            title = "Test PDF Word Chart",
            style = GeneratorStyle.CHEVRON,
            technique = BeadTechnique.LOOM,
            columns = 10,
            rows = 20,
            palette = CuratedPalettes.ORO_Y_NOCHE,
            symmetry = true
        )

        val uniqueColors = pattern.grid.filter { it != 0 && it != -1 }.distinct()
        assertTrue(uniqueColors.isNotEmpty())
        for (r in 0 until pattern.rows) {
            val rowBeads = pattern.getRowBeads(r)
            assertEquals(10, rowBeads.size)
        }
    }
}
