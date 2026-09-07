package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import com.example.data.model.MiyukiCatalog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object PdfPatternExporter {

    private const val PAGE_WIDTH = 595  // Standard A4 at 72 dpi
    private const val PAGE_HEIGHT = 842 // Standard A4 at 72 dpi

    data class BeadColorEntry(
        val letterKey: String,
        val colorInt: Int,
        val dbCode: String,
        val name: String,
        val finish: String,
        val count: Int,
        val grams: Float
    )

    /**
     * Generates a multi-page printable PDF workshop sheet for the given bead pattern.
     * Includes:
     * 1. Technical specs & Delica materials shopping tally.
     * 2. Scaled, numbered color grid (with staggered offset for Peyote/Brick stitch).
     * 3. Row-by-row written Word Chart with threading direction.
     */
    fun exportPatternToPdf(context: Context, pattern: BeadPattern): File {
        val pdfDocument = PdfDocument()

        // 1. Compute color assignments and letters (A, B, C...)
        val uniqueColors = pattern.grid.filter { it != 0 && it != -1 }.distinct()
        val letterAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val colorEntries = uniqueColors.mapIndexed { index, colorInt ->
            val bead = MiyukiCatalog.findClosest(colorInt)
            val count = pattern.grid.count { it == colorInt }
            val letter = if (index < letterAlphabet.length) letterAlphabet[index].toString() else "X$index"
            BeadColorEntry(
                letterKey = letter,
                colorInt = colorInt,
                dbCode = bead.code,
                name = bead.name,
                finish = bead.finish.label,
                count = count,
                grams = count / 105f
            )
        }.sortedByDescending { it.count }

        val colorToKeyMap = colorEntries.associate { it.colorInt to it.letterKey }

        // Setup common paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 43, 76) // Deep Navy
            textSize = 18f
            isFakeBoldText = true
        }

        val subheadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(184, 134, 11) // Miyuki Gold
            textSize = 12f
            isFakeBoldText = true
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            color = Color.DKGRAY
        }

        var pageIndex = 1

        // -------------------------------------------------------------
        // PAGE 1: FICHA TÉCNICA Y TABLA DE MATERIALES (SHOPPING LIST)
        // -------------------------------------------------------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Header Background Banner
        fillPaint.color = Color.rgb(27, 34, 45) // Deep Slate
        canvas1.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 70f, fillPaint)

        fillPaint.color = Color.rgb(212, 175, 55) // Gold accent line
        canvas1.drawRect(0f, 70f, PAGE_WIDTH.toFloat(), 74f, fillPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true
        canvas1.drawText("GUÍA DE TALLER: ${pattern.title.uppercase()}", 28f, 38f, textPaint)

        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(200, 210, 225)
        canvas1.drawText("Patrón Oficial de Tejido con Delicas Miyuki 11/0 • Documento de Producción", 28f, 54f, textPaint)

        // Metadata box
        var y = 100f
        fillPaint.color = Color.rgb(245, 247, 250)
        canvas1.drawRoundRect(28f, y, PAGE_WIDTH - 28f, y + 105f, 10f, 10f, fillPaint)
        strokePaint.color = Color.rgb(210, 220, 230)
        strokePaint.strokeWidth = 1f
        canvas1.drawRoundRect(28f, y, PAGE_WIDTH - 28f, y + 105f, 10f, 10f, strokePaint)

        val col1X = 42f
        val col2X = 220f
        val col3X = 400f

        textPaint.color = Color.rgb(100, 110, 120)
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = true
        canvas1.drawText("TÉCNICA DE TEJIDO", col1X, y + 24f, textPaint)
        canvas1.drawText("DIMENSIONES DEL DISEÑO", col2X, y + 24f, textPaint)
        canvas1.drawText("MATERIAL Y PESO ESTIMADO", col3X, y + 24f, textPaint)

        textPaint.color = Color.rgb(20, 30, 45)
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        canvas1.drawText(pattern.technique.title, col1X, y + 42f, textPaint)
        canvas1.drawText("${pattern.columns} col × ${pattern.rows} filas", col2X, y + 42f, textPaint)
        canvas1.drawText("${pattern.totalBeads} delicas (~${String.format(Locale.US, "%.1f", pattern.totalBeads / 105f)} g)", col3X, y + 42f, textPaint)

        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(80, 90, 100)
        val widthMm = if (pattern.technique.isStaggered) pattern.columns * 1.5f else pattern.columns * 1.3f
        val heightMm = pattern.rows * 1.6f
        canvas1.drawText(pattern.technique.subtitle, col1X, y + 60f, textPaint)
        canvas1.drawText("~${String.format(Locale.US, "%.1f", widthMm / 10f)} cm ancho × ~${String.format(Locale.US, "%.1f", heightMm / 10f)} cm largo", col2X, y + 60f, textPaint)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas1.drawText("Fecha de emisión: $dateStr", col3X, y + 60f, textPaint)

        val wristSize = pattern.wristSizeCm
        canvas1.drawText("Ajuste muñeca: ~$wristSize cm estándar", col2X, y + 78f, textPaint)
        canvas1.drawText("Calibre: Miyuki Delica 11/0 (1.6 x 1.3 mm)", col3X, y + 78f, textPaint)

        // Materials Table (Shopping List)
        y = 230f
        headerPaint.textSize = 13f
        headerPaint.color = Color.rgb(27, 43, 76)
        canvas1.drawText("TABLA DE MATERIALES Y CÓDIGOS DE COLOR MIYUKI", 28f, y, headerPaint)

        y += 15f
        // Table Header row
        fillPaint.color = Color.rgb(230, 235, 245)
        canvas1.drawRect(28f, y, PAGE_WIDTH - 28f, y + 22f, fillPaint)

        textPaint.color = Color.rgb(40, 50, 60)
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true

        canvas1.drawText("Clave", 36f, y + 15f, textPaint)
        canvas1.drawText("Color", 80f, y + 15f, textPaint)
        canvas1.drawText("Código DB", 130f, y + 15f, textPaint)
        canvas1.drawText("Nombre / Acabado Oficial", 215f, y + 15f, textPaint)
        canvas1.drawText("Cantidad", 440f, y + 15f, textPaint)
        canvas1.drawText("Gramos Aprox.", 510f, y + 15f, textPaint)

        y += 24f
        var rowBgAlt = false
        val itemHeight = 22f
        val maxTableItems = min(colorEntries.size, 16)

        for (i in 0 until maxTableItems) {
            val item = colorEntries[i]
            if (rowBgAlt) {
                fillPaint.color = Color.rgb(248, 250, 252)
                canvas1.drawRect(28f, y, PAGE_WIDTH - 28f, y + itemHeight, fillPaint)
            }
            rowBgAlt = !rowBgAlt

            // Key Badge (A, B, C...)
            fillPaint.color = Color.rgb(220, 225, 235)
            canvas1.drawRoundRect(35f, y + 3f, 55f, y + itemHeight - 3f, 4f, 4f, fillPaint)
            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(20, 30, 40)
            textPaint.textSize = 9.5f
            canvas1.drawText(item.letterKey, 41f, y + 15f, textPaint)

            // Bead Color Swatch
            fillPaint.color = item.colorInt
            val swatchRect = RectF(82f, y + 4f, 106f, y + itemHeight - 4f)
            canvas1.drawRoundRect(swatchRect, 3f, 3f, fillPaint)
            strokePaint.color = Color.argb(80, 0, 0, 0)
            strokePaint.strokeWidth = 0.5f
            canvas1.drawRoundRect(swatchRect, 3f, 3f, strokePaint)

            // DB Code
            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(20, 30, 40)
            textPaint.textSize = 9f
            canvas1.drawText(item.dbCode, 130f, y + 15f, textPaint)

            // Name and finish
            textPaint.isFakeBoldText = false
            textPaint.textSize = 8.5f
            val nameDisplay = if (item.name.length > 34) item.name.take(32) + ".." else item.name
            canvas1.drawText("$nameDisplay (${item.finish})", 215f, y + 15f, textPaint)

            // Bead count
            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(27, 43, 76)
            canvas1.drawText("${item.count} delicas", 440f, y + 15f, textPaint)

            // Weight in grams
            textPaint.isFakeBoldText = false
            textPaint.color = Color.rgb(80, 90, 100)
            canvas1.drawText("~${String.format(Locale.US, "%.1f", item.grams)} g", 515f, y + 15f, textPaint)

            y += itemHeight
        }

        // Workshop recommendation tips at bottom of Page 1
        val tipY = 660f
        fillPaint.color = Color.rgb(253, 249, 237)
        canvas1.drawRoundRect(28f, tipY, PAGE_WIDTH - 28f, tipY + 120f, 8f, 8f, fillPaint)
        strokePaint.color = Color.rgb(230, 200, 120)
        canvas1.drawRoundRect(28f, tipY, PAGE_WIDTH - 28f, tipY + 120f, 8f, 8f, strokePaint)

        subheadPaint.textSize = 10f
        subheadPaint.color = Color.rgb(184, 115, 20)
        canvas1.drawText("CONSEJOS DE TALLER PARA EL ARTESANO:", 40f, tipY + 22f, subheadPaint)

        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(60, 60, 60)
        canvas1.drawText("• Aguja recomendada: Aguja para mostacilla extrafina #10 o #12 (Tulip o John James).", 40f, tipY + 42f, textPaint)
        canvas1.drawText("• Hilo: Hilo de nylon trenzado Miyuki, KO Thread o Fireline de 4 lb o 6 lb con cera para evitar nudos.", 40f, tipY + 58f, textPaint)
        canvas1.drawText("• Tensión: Mantén una tensión regular y uniforme en cada pasada para evitar que el tejido se arquee.", 40f, tipY + 74f, textPaint)
        canvas1.drawText("• Calidad: Todas las dimensiones corresponden al estándar japonés Miyuki Delica cilíndrico 11/0.", 40f, tipY + 90f, textPaint)

        drawPageFooter(canvas1, pageIndex, 3)
        pdfDocument.finishPage(page1)
        pageIndex++

        // -------------------------------------------------------------
        // PAGE 2: DIAGRAMA GRÁFICO NUMERADO (COLOR CHART)
        // -------------------------------------------------------------
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        // Header
        headerPaint.textSize = 14f
        headerPaint.color = Color.rgb(27, 43, 76)
        canvas2.drawText("DIAGRAMA GRÁFICO NUMERADO (COLOR CHART)", 28f, 38f, headerPaint)
        textPaint.textSize = 8.5f
        textPaint.color = Color.GRAY
        canvas2.drawText("Sigue la cuadrícula fila por fila. Cada casilla representa 1 Delica 11/0.", 28f, 52f, textPaint)

        // Draw the visual bead matrix with scale calculation
        val gridAvailableW = PAGE_WIDTH - 90f
        val gridAvailableH = PAGE_HEIGHT - 130f

        val beadCellW = min(24f, max(4.5f, gridAvailableW / (pattern.columns + 1)))
        val beadCellH = min(20f, max(4.0f, gridAvailableH / (pattern.rows + 1)))
        val cellSize = min(beadCellW, beadCellH)

        val totalGridW = pattern.columns * cellSize
        val totalGridH = pattern.rows * cellSize
        val startX = (PAGE_WIDTH - totalGridW) / 2f
        val startY = 75f

        val isStaggered = pattern.technique.isStaggered

        // Draw Column numbers at top
        textPaint.textSize = min(8f, max(5f, cellSize * 0.7f))
        textPaint.color = Color.rgb(100, 110, 120)
        textPaint.isFakeBoldText = true
        for (c in 0 until pattern.columns) {
            val colCenterX = startX + c * cellSize + cellSize / 2f
            if (c % 2 == 0 || cellSize > 10f) {
                val label = (c + 1).toString()
                val measure = textPaint.measureText(label)
                canvas2.drawText(label, colCenterX - measure / 2f, startY - 4f, textPaint)
            }
        }

        val beadRadius = min(2.5f, cellSize * 0.25f)
        val letterTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = min(7.5f, max(4f, cellSize * 0.65f))
            isFakeBoldText = true
        }

        // Draw rows
        for (r in 0 until pattern.rows) {
            val rowY = startY + r * cellSize
            val rowOffset = if (isStaggered && r % 2 != 0) cellSize / 2f else 0f

            // Left row number
            val rowLabel = (r + 1).toString()
            textPaint.textSize = min(7.5f, max(4.5f, cellSize * 0.65f))
            textPaint.color = if (r % 5 == 0) Color.rgb(20, 30, 40) else Color.rgb(120, 130, 140)
            textPaint.isFakeBoldText = r % 5 == 0
            val rowMeasure = textPaint.measureText(rowLabel)
            canvas2.drawText(rowLabel, startX - rowMeasure - 6f, rowY + cellSize * 0.75f, textPaint)

            // Right row number
            canvas2.drawText(rowLabel, startX + totalGridW + rowOffset + 6f, rowY + cellSize * 0.75f, textPaint)

            // Beads in row
            for (c in 0 until pattern.columns) {
                val cellX = startX + c * cellSize + rowOffset
                val colorInt = pattern.getBeadAt(c, r)

                fillPaint.color = if (colorInt != 0 && colorInt != -1) colorInt else Color.WHITE
                val beadRect = RectF(cellX + 0.4f, rowY + 0.4f, cellX + cellSize - 0.4f, rowY + cellSize - 0.4f)
                canvas2.drawRoundRect(beadRect, beadRadius, beadRadius, fillPaint)

                // Bead separation stroke
                strokePaint.color = Color.argb(45, 0, 0, 0)
                strokePaint.strokeWidth = 0.4f
                canvas2.drawRoundRect(beadRect, beadRadius, beadRadius, strokePaint)

                // Letter code inside bead if cell size permits
                if (cellSize >= 9f && colorInt != 0 && colorInt != -1) {
                    val letter = colorToKeyMap[colorInt] ?: ""
                    // Contrast text color (white on dark, black on light)
                    val lum = (0.299 * Color.red(colorInt) + 0.587 * Color.green(colorInt) + 0.114 * Color.blue(colorInt))
                    letterTextPaint.color = if (lum < 130) Color.WHITE else Color.BLACK
                    val lm = letterTextPaint.measureText(letter)
                    canvas2.drawText(letter, cellX + (cellSize - lm) / 2f, rowY + cellSize * 0.75f, letterTextPaint)
                }
            }
        }

        drawPageFooter(canvas2, pageIndex, 3)
        pdfDocument.finishPage(page2)
        pageIndex++

        // -------------------------------------------------------------
        // PAGE 3: GUÍA ESCRITA FILA A FILA (WORD CHART)
        // -------------------------------------------------------------
        val pageInfo3 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
        val page3 = pdfDocument.startPage(pageInfo3)
        val canvas3 = page3.canvas

        headerPaint.textSize = 14f
        headerPaint.color = Color.rgb(27, 43, 76)
        canvas3.drawText("INSTRUCCIONES ESCRITAS FILA POR FILA (WORD CHART)", 28f, 38f, headerPaint)
        textPaint.textSize = 8.5f
        textPaint.color = Color.GRAY
        canvas3.drawText("Ideal para tejer a mano sin desviar la mirada al gráfico. Sigue la dirección de cada fila.", 28f, 52f, textPaint)

        // Generate the Word Chart text lines
        val wordChartLines = mutableListOf<String>()
        for (r in 0 until pattern.rows) {
            val isLeftToRight = r % 2 == 0
            val directionIcon = if (isLeftToRight) "-> (Der)" else "<- (Izq)"
            val rowColors = pattern.getRowBeads(r)
            val orderedColors = if (isLeftToRight) rowColors else rowColors.reversed()

            // Group consecutive identical colors
            val runs = mutableListOf<Pair<String, Int>>()
            if (orderedColors.isNotEmpty()) {
                var currentColor = orderedColors[0]
                var runCount = 1
                for (ci in 1 until orderedColors.size) {
                    if (orderedColors[ci] == currentColor) {
                        runCount++
                    } else {
                        val key = colorToKeyMap[currentColor] ?: "?"
                        runs.add(Pair(key, runCount))
                        currentColor = orderedColors[ci]
                        runCount = 1
                    }
                }
                val key = colorToKeyMap[currentColor] ?: "?"
                runs.add(Pair(key, runCount))
            }

            val runsText = runs.joinToString(", ") { "(${it.second}) ${it.first}" }
            wordChartLines.add("Fila ${r + 1} $directionIcon: $runsText")
        }

        // Render Word Chart lines in 2 columns
        val colLeftX = 32f
        val colRightX = 300f
        val startWordY = 76f
        var currentWordY = startWordY
        val lineHeight = 11.2f
        textPaint.textSize = 7.8f

        val halfCount = (wordChartLines.size + 1) / 2
        for (idx in wordChartLines.indices) {
            val isRightCol = idx >= halfCount
            val xPos = if (isRightCol) colRightX else colLeftX
            val lineIdxInCol = if (isRightCol) idx - halfCount else idx
            val yPos = startWordY + lineIdxInCol * lineHeight

            if (yPos < PAGE_HEIGHT - 50f) {
                val line = wordChartLines[idx]
                // Alternate row background shading
                if (idx % 2 == 0) {
                    fillPaint.color = Color.rgb(246, 248, 252)
                    canvas3.drawRect(xPos - 4f, yPos - 9f, xPos + 258f, yPos + 2.5f, fillPaint)
                }

                textPaint.color = Color.rgb(30, 40, 50)
                textPaint.isFakeBoldText = true
                val prefix = line.substringBefore(":")
                canvas3.drawText("$prefix:", xPos, yPos, textPaint)

                textPaint.isFakeBoldText = false
                textPaint.color = Color.rgb(60, 70, 80)
                val suffix = line.substringAfter(":", "")
                val prefixWidth = textPaint.measureText("$prefix: ")
                canvas3.drawText(suffix, xPos + prefixWidth, yPos, textPaint)
            }
        }

        drawPageFooter(canvas3, pageIndex, 3)
        pdfDocument.finishPage(page3)

        // Save PDF to App Cache / Documents for seamless zero-permission sharing
        val safeTitle = pattern.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val fileName = "Patron_Miyuki_${safeTitle}_${System.currentTimeMillis()}.pdf"
        val outputDir = File(context.cacheDir, "exported_patterns")
        if (!outputDir.exists()) outputDir.mkdirs()

        val pdfFile = File(outputDir, fileName)
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val footerY = PAGE_HEIGHT - 22f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(140, 150, 160)
            textSize = 8f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 225, 230)
            strokeWidth = 0.5f
        }
        canvas.drawLine(28f, footerY - 10f, PAGE_WIDTH - 28f, footerY - 10f, linePaint)
        canvas.drawText("Diseñador de Pulseras Miyuki • Guía Técnica para Artesanos", 28f, footerY, footerPaint)

        val pageStr = "Página $pageNumber de $totalPages"
        val measure = footerPaint.measureText(pageStr)
        canvas.drawText(pageStr, PAGE_WIDTH - 28f - measure, footerY, footerPaint)
    }

    /**
     * Launches Android's native share sheet to send the generated PDF to WhatsApp, Telegram,
     * Drive, Email, or open directly in an external PDF viewer/printer.
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Patrón de Pulsera Miyuki Delica (PDF)")
            putExtra(Intent.EXTRA_TEXT, "Te comparto la ficha técnica de este patrón Miyuki en PDF con cuadrícula, lista de materiales y Word Chart.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Compartir Ficha Técnica en PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
