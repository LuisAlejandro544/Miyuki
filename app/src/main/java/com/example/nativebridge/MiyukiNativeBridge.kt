package com.example.nativebridge

object MiyukiNativeBridge {
    private var isLoaded = false
    private var loadError: String? = null

    init {
        try {
            System.loadLibrary("miyuki_native")
            isLoaded = true
        } catch (e: Throwable) {
            loadError = e.message
            isLoaded = false
        }
    }

    fun isNativeLoaded(): Boolean = isLoaded
    fun getLoadError(): String? = loadError

    external fun getNativeEngineStatus(): String
    external fun executeLuaSnippet(script: String): String
    external fun executeLuaPatternScript(script: String, columns: Int, rows: Int): IntArray
    external fun calculateEarringFringesRust(baseWidth: Int, maxFringeLen: Int, minFringeLen: Int, style: Int): IntArray
    external fun calculateEarringTriangleRowsRust(baseWidth: Int): Int
    external fun convertPhotoToPatternNative(
        srcPixels: IntArray,
        srcWidth: Int,
        srcHeight: Int,
        targetCols: Int,
        targetRows: Int,
        brightness: Float,
        contrast: Float,
        useDithering: Boolean,
        maxColors: Int
    ): IntArray

    external fun analyzeChartGridRustNative(
        srcPixels: IntArray,
        width: Int,
        height: Int
    ): IntArray

    external fun calibrateAndSamplePatternNative(
        srcPixels: IntArray,
        srcWidth: Int,
        srcHeight: Int,
        targetCols: Int,
        targetRows: Int,
        technique: Int,
        sampleWindowRatio: Float,
        brightness: Float,
        contrast: Float,
        maxColors: Int
    ): IntArray
}
