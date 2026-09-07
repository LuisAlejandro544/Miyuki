package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlin.math.max
import kotlin.math.min

object PdfPatternExtractor {

    fun isPdf(context: Context, uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri)
        if (mime?.contains("pdf", ignoreCase = true) == true) return true
        val path = uri.path ?: uri.toString()
        return path.endsWith(".pdf", ignoreCase = true)
    }

    fun getPdfPageCount(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                renderer.close()
                count
            } ?: 1
        } catch (e: Exception) {
            1
        }
    }

    fun renderPdfPage(context: Context, uri: Uri, pageIndex: Int = 0): Bitmap? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                val totalPages = renderer.pageCount
                val safeIndex = pageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                val page = renderer.openPage(safeIndex)

                // Scale for high fidelity rasterization (max dimension ~1800 px)
                val maxDim = max(page.width, page.height)
                val scale = if (maxDim > 0) (1800f / maxDim).coerceIn(1.0f, 3.0f) else 2.0f

                val targetW = (page.width * scale).toInt().coerceAtLeast(100)
                val targetH = (page.height * scale).toInt().coerceAtLeast(100)

                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeImageUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // First get bounds
            var opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            }

            val maxDim = max(opts.outWidth, opts.outHeight)
            var sampleSize = 1
            while (maxDim / (sampleSize * 2) >= 1800) {
                sampleSize *= 2
            }

            opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun cropBitmap(source: Bitmap, leftFrac: Float, topFrac: Float, rightFrac: Float, bottomFrac: Float): Bitmap {
        val l = (leftFrac * source.width).toInt().coerceIn(0, source.width - 2)
        val t = (topFrac * source.height).toInt().coerceIn(0, source.height - 2)
        val r = (rightFrac * source.width).toInt().coerceIn(l + 2, source.width)
        val b = (bottomFrac * source.height).toInt().coerceIn(t + 2, source.height)

        val cropW = (r - l).coerceAtLeast(2)
        val cropH = (b - t).coerceAtLeast(2)

        return Bitmap.createBitmap(source, l, t, cropW, cropH)
    }

    // Creates a realistic sample pattern document/chart (e.g. Aztec Diamond Bracelet)
    // with grid lines, margins, header, and Delica colors
    fun createSampleChartDocument(): Bitmap {
        val w = 1200
        val h = 1600
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // White paper background
        canvas.drawColor(Color.rgb(250, 250, 252))

        // Title Header of the PDF chart
        paint.color = Color.rgb(30, 30, 35)
        paint.textSize = 42f
        paint.isFakeBoldText = true
        canvas.drawText("Patrón Miyuki Delica 11/0 - Pulsera Étnica Sol", 80f, 100f, paint)

        paint.textSize = 24f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 100, 110)
        canvas.drawText("Técnica: Peyote / Telar (16 cols x 40 filas) • Catálogo Oficial", 80f, 145f, paint)

        // Palette legend at top
        val legendColors = intArrayOf(
            Color.rgb(223, 172, 56),  // DB-0031 Oro Galvanizado
            Color.rgb(0, 164, 166),   // DB-0729 Turquesa Mate
            Color.rgb(198, 40, 40),   // DB-0723 Rubí Opaco
            Color.rgb(26, 54, 126),   // DB-0726 Azul Cobalto
            Color.rgb(248, 246, 240)  // DB-0200 Blanco Perla
        )
        val legendNames = arrayOf("DB-0031 Oro", "DB-0729 Turquesa", "DB-0723 Rubí", "DB-0726 Cobalto", "DB-0200 Perla")
        for (i in legendColors.indices) {
            val lx = 80f + (i * 220f)
            paint.color = legendColors[i]
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(lx, 175f, lx + 30f, 205f, 6f, 6f, paint)
            paint.color = Color.rgb(40, 40, 40)
            paint.textSize = 18f
            canvas.drawText(legendNames[i], lx + 38f, 198f, paint)
        }

        // Draw chart grid area with frame
        val gridLeft = 140f
        val gridTop = 260f
        val gridRight = 1060f
        val gridBottom = 1480f

        val cols = 16
        val rows = 40
        val cellW = (gridRight - gridLeft) / cols
        val cellH = (gridBottom - gridTop) / rows

        // Grid border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(80, 80, 80)
        canvas.drawRect(gridLeft, gridTop, gridRight, gridBottom, paint)

        // Beads inside chart
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = gridLeft + (c * cellW)
                val cy = gridTop + (r * cellH)

                // Geometric Aztec diamond pattern
                val distCenterC = kotlin.math.abs(c - (cols - 1) / 2.0f)
                val periodR = (r % 10)
                val distCenterR = kotlin.math.abs(periodR - 4.5f)
                val manhattan = distCenterC + distCenterR

                val beadColor = when {
                    manhattan < 2.0f -> legendColors[0] // Oro
                    manhattan < 3.5f -> legendColors[1] // Turquesa
                    manhattan < 5.0f -> legendColors[2] // Rubí
                    (c == 0 || c == cols - 1) -> legendColors[3] // Cobalto bordes
                    else -> legendColors[4] // Perla
                }

                // Fill bead cell
                paint.style = Paint.Style.FILL
                paint.color = beadColor
                canvas.drawRect(cx, cy, cx + cellW, cy + cellH, paint)

                // Grid line border for every bead (as printed on PDF charts)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f
                paint.color = Color.argb(160, 50, 50, 50)
                canvas.drawRect(cx, cy, cx + cellW, cy + cellH, paint)
            }

            // Row numbers on right
            if (r % 5 == 0 || r == rows - 1) {
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(100, 100, 110)
                paint.textSize = 18f
                canvas.drawText("${r + 1}", gridRight + 12f, gridTop + (r * cellH) + (cellH * 0.75f), paint)
            }
        }

        // Col numbers on top
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(100, 100, 110)
        paint.textSize = 18f
        for (c in 0 until cols step 2) {
            canvas.drawText("${c + 1}", gridLeft + (c * cellW) + (cellW * 0.25f), gridTop - 12f, paint)
        }

        return bmp
    }
}
