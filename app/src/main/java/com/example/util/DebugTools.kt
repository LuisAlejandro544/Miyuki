package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.github.pedrovgs.lynx.LynxActivity
import com.github.pedrovgs.lynx.LynxConfig

/**
 * Utilidades de depuración en dispositivo móvil (sin PC ni ADB conectado).
 */
object DebugTools {

    /**
     * Abre la consola de Lynx a pantalla completa para visualizar el Logcat del sistema,
     * incluyendo logs nativos de C++20, Rust, Lua y excepciones de Kotlin.
     */
    fun openLynxLogcat(context: Context) {
        try {
            val config = LynxConfig()
                .setMaxNumberOfTracesToShow(3000)
                .setFilter("Miyuki|lua|rust|NativeBridge|PatternViewModel|AndroidRuntime")

            val intent = LynxActivity.getIntent(context, config)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DebugTools", "Error abriendo LynxActivity: ${e.message}", e)
            Toast.makeText(context, "Error abriendo consola Lynx: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
