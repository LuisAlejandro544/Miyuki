package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique
import kotlin.math.floor

@Composable
fun InteractiveBeadCanvas(
    pattern: BeadPattern,
    modifier: Modifier = Modifier,
    activeRow: Int? = null,
    isEditable: Boolean = false,
    selectedColor: Int = 0,
    isMirrorMode: Boolean = false,
    isEraser: Boolean = false,
    onCellClicked: ((col: Int, row: Int) -> Unit)? = null,
    onCellDragged: ((col: Int, row: Int) -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.4f, 4.0f)
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState)
            .pointerInput(isEditable, pattern, scale, offset) {
                if (isEditable) {
                    detectTapGestures { tapOffset ->
                        val cell = screenToGrid(
                            screenX = tapOffset.x,
                            screenY = tapOffset.y,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            scale = scale,
                            columns = pattern.columns,
                            rows = pattern.rows,
                            isStaggered = pattern.technique.isStaggered,
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat()
                        )
                        if (cell != null) {
                            onCellClicked?.invoke(cell.first, cell.second)
                        }
                    }
                }
            }
            .pointerInput(isEditable, pattern, scale, offset) {
                if (isEditable) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val cell = screenToGrid(
                            screenX = change.position.x,
                            screenY = change.position.y,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            scale = scale,
                            columns = pattern.columns,
                            rows = pattern.rows,
                            isStaggered = pattern.technique.isStaggered,
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat()
                        )
                        if (cell != null) {
                            onCellDragged?.invoke(cell.first, cell.second)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBeadPattern(
                pattern = pattern,
                activeRow = activeRow,
                scale = scale,
                offset = offset
            )
        }
    }
}

/**
 * Compact read-only preview canvas suitable for cards and list items.
 */
@Composable
fun BeadPatternPreview(
    pattern: BeadPattern,
    modifier: Modifier = Modifier,
    activeRow: Int? = null,
    showBorder: Boolean = true
) {
    Canvas(modifier = modifier) {
        val totalCols = pattern.columns
        val totalRows = pattern.rows
        val isStaggered = pattern.technique.isStaggered

        // Calculate bead size to fit canvas cleanly
        val beadW = size.width / (totalCols + if (isStaggered) 0.5f else 0.0f)
        val rowH = if (isStaggered) beadW * 1.1f else beadW * 1.25f
        val beadH = rowH * 0.92f

        for (r in 0 until totalRows) {
            val y = r * rowH
            if (y > size.height + 10f) break // clip invisible rows in preview

            val xShift = if (isStaggered && r % 2 == 1) beadW * 0.5f else 0f
            val isActive = activeRow == r

            for (c in 0 until totalCols) {
                val x = c * beadW + xShift
                val colorInt = pattern.getBeadAt(c, r)

                drawSingleDelica(
                    x = x,
                    y = y,
                    width = beadW * 0.90f,
                    height = beadH,
                    colorInt = colorInt,
                    isActiveRow = isActive
                )
            }
        }

        if (showBorder) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                size = size,
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

private fun DrawScope.drawBeadPattern(
    pattern: BeadPattern,
    activeRow: Int?,
    scale: Float,
    offset: Offset
) {
    val totalCols = pattern.columns
    val totalRows = pattern.rows
    val isStaggered = pattern.technique.isStaggered

    // Base Delica bead dimensions in dp equivalent
    val baseBeadWidth = (size.width / (totalCols + 2)).coerceIn(16f, 40f) * scale
    val baseRowHeight = if (isStaggered) baseBeadWidth * 1.15f else baseBeadWidth * 1.35f
    val beadWidth = baseBeadWidth * 0.92f
    val beadHeight = baseRowHeight * 0.90f

    val startX = (size.width - (totalCols * baseBeadWidth)) / 2f + offset.x
    val startY = 40f * scale + offset.y

    // Draw active row guide line behind if selected
    if (activeRow != null && activeRow in 0 until totalRows) {
        val activeY = startY + activeRow * baseRowHeight - 4f
        drawRoundRect(
            color = Color(0xFFD4AF37).copy(alpha = 0.25f),
            topLeft = Offset(startX - 16f, activeY),
            size = Size(totalCols * baseBeadWidth + 32f, baseRowHeight + 8f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = Color(0xFFD4AF37),
            topLeft = Offset(startX - 16f, activeY),
            size = Size(totalCols * baseBeadWidth + 32f, baseRowHeight + 8f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2.5f)
        )
    }

    // Draw bead grid
    for (r in 0 until totalRows) {
        val y = startY + r * baseRowHeight
        if (y + beadHeight < -100 || y > size.height + 100) continue // culling

        val xShift = if (isStaggered && r % 2 == 1) baseBeadWidth * 0.5f else 0f
        val isActive = activeRow == r

        for (c in 0 until totalCols) {
            val x = startX + c * baseBeadWidth + xShift
            val colorInt = pattern.getBeadAt(c, r)

            drawSingleDelica(
                x = x,
                y = y,
                width = beadWidth,
                height = beadHeight,
                colorInt = colorInt,
                isActiveRow = isActive
            )
        }
    }
}

private fun DrawScope.drawSingleDelica(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    colorInt: Int,
    isActiveRow: Boolean
) {
    val cornerRadius = CornerRadius(width * 0.25f, width * 0.25f)
    val beadColor = if (colorInt != 0 && colorInt != -1) Color(colorInt) else Color(0xFFE8E5DD)

    // Bead Body with subtle cylindrical glass gradient
    val cylinderBrush = Brush.horizontalGradient(
        colors = listOf(
            beadColor.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.28f),
            beadColor,
            beadColor.copy(alpha = 0.70f)
        ),
        startX = x,
        endX = x + width
    )

    // Base fill
    drawRoundRect(
        color = beadColor,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = cornerRadius
    )

    // Glass sheen overlay
    drawRoundRect(
        brush = cylinderBrush,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = cornerRadius
    )

    // Bead Thread Hole (delica central lumen indicator)
    val holeWidth = width * 0.22f
    val holeHeight = height * 0.35f
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(x + (width - holeWidth) / 2f, y + (height - holeHeight) / 2f),
        size = Size(holeWidth, holeHeight),
        cornerRadius = CornerRadius(holeWidth / 2, holeWidth / 2)
    )

    // Bead border / subtle depth stroke
    val strokeColor = if (isActiveRow) Color(0xFFD4AF37) else Color.Black.copy(alpha = 0.15f)
    val strokeWidth = if (isActiveRow) 2.0f else 0.8f
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = cornerRadius,
        style = Stroke(width = strokeWidth)
    )
}

private fun screenToGrid(
    screenX: Float,
    screenY: Float,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    columns: Int,
    rows: Int,
    isStaggered: Boolean,
    canvasWidth: Float,
    canvasHeight: Float
): Pair<Int, Int>? {
    val baseBeadWidth = (canvasWidth / (columns + 2)).coerceIn(16f, 40f) * scale
    val baseRowHeight = if (isStaggered) baseBeadWidth * 1.15f else baseBeadWidth * 1.35f

    val startX = (canvasWidth - (columns * baseBeadWidth)) / 2f + offsetX
    val startY = 40f * scale + offsetY

    val relY = screenY - startY
    if (relY < 0) return null
    val row = floor(relY / baseRowHeight).toInt()
    if (row !in 0 until rows) return null

    val xShift = if (isStaggered && row % 2 == 1) baseBeadWidth * 0.5f else 0f
    val relX = screenX - (startX + xShift)
    if (relX < 0) return null
    val col = floor(relX / baseBeadWidth).toInt()
    if (col !in 0 until columns) return null

    return Pair(col, row)
}
