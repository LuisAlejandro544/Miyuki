# Taller de Patrones y Tejido de Cuentas (Miyuki Delica)

Aplicación nativa para Android diseñada para artesanos, diseñadores y creadores de joyería en mostacilla y cuentas de vidrio de alta precisión (*Miyuki Delica 11/0*). Permite diseñar en cuadrícula táctil, seguir el tejido fila por fila en tiempo real, calcular materiales y medidas de muñeca con exactitud, y generar patrones algorítmicos avanzados mediante un motor nativo en **C++20**, **Rust** y **Lua 5.4**.

La aplicación está optimizada para su uso directo en teléfonos inteligentes y su distribución en plataformas de APK de terceros (como Uptodown, F-Droid o APKPure), sin dependencias obligatorias de servicios privativos.

---

## 🌟 Características Principales

### 1. Editor Táctil de Patrones
- **Cuadrículas adaptativas**: Soporte para **Telar (Loom)**, **Peyote Par e Impar** y **Punto Ladrillo (Brick Stitch)** con desplazamiento escalonado visualmente fiel.
- **Herramientas de dibujo completas**: Lápiz, Borrador, Cubo de Relleno (*Flood Fill*) y Cuentagotas (*Color Picker*).
- **Control de simetría**: Modo espejo horizontal instantáneo para pulseras y gargantillas perfectamente balanceadas.
- **Paleta de trabajo rápida**: Selector dinámico con catálogo de referencias Miyuki Delica 11/0.

### 2. Tejedor Paso a Paso (Modo Seguimiento Táctil)
- **Fila activa destacada**: Resalta la fila que estás tejiendo con lupa visual y desenfoque del resto del patrón.
- **Desglose de ensartado**: Muestra la secuencia exacta de cuentas a insertar en la aguja (por ejemplo: `3x DB-0031 Dorado, 2x DB-0010 Negro, 1x DB-0201 Blanco`).
- **Navegación con hápticos**: Botones grandes de 48dp+ para avanzar/retroceder filas con retroalimentación de vibración suave en el teléfono.
- **Orientación reversible**: Modos de tejido de abajo hacia arriba (*Bottom-Up*) o de arriba hacia abajo (*Top-Down*).
- **Indicador de progreso y tiempo**: Contador de cuentas tejidas, porcentaje completado y tiempo transcurrido en la sesión.

### 3. Taller de Zarcillos con Geometría Rust
- **Copa triangular en Brick Stitch**: Disminuciones matemáticas exactas para la estructura de soporte del pendiente.
- **Motor de flecos colgantes en Rust**: Cálculo trigonométrico de longitudes de flecos en estilos populares:
  - *V Clásica*
  - *Chevron Invertido*
  - *Cascada Ondulada (Sine Wave)*
  - *Escalonado Diagonal*
  - *Rombo Diamante*
- **Ajuste en tiempo real**: Configuración de ancho de base (número de delicas de ancho), largo mínimo y máximo de flecos y combinación de colores.

### 4. Estudio Algorítmico Lua 5.4 ANSI C
- **Intérprete oficial integrado**: Lua 5.4.6 compilado directamente en código nativo ANSI C desde fuentes oficiales de PUC-Rio, enlazado vía JNI en C++20.
- **Generación paramétrica**: Función programable `getBead(col, row)` para producir gradientes, espirales, patrones fractales o grecas matemáticas complejas.
- **Consola interactiva (REPL)**: Permite evaluar expresiones Lua y capturar la salida de `print()` directamente en pantalla.

### 5. Calculadora de Medidas y Materiales
- **Calibración de muñeca**: Deslizador en centímetros con compensación automática según el tipo de broche:
  - Nudo corredizo macramé (-1.5 cm)
  - Terminal de tubo deslizable (-0.8 cm)
  - Broche imán / mosquetón (-1.2 cm)
  - Pulsera elástica (0.0 cm)
- **Estimación de hilos y peso**: Metros de hilo de urdimbre y trama recomendados, junto con el peso total en gramos de cuentas Delica 11/0 requeridas.

### 6. Catálogo Real de Cuentas Delica 11/0
- Catálogo de referencia con códigos `DB-xxxx`, nombres comerciales, acabados (*Metallic, Opaque, Galvanized, Matte*) y valores de color hexadecimal calibrados para pantalla.

---

## 🛠️ Stack Tecnológico y Arquitectura

| Capa | Tecnología | Justificación |
| :--- | :--- | :--- |
| **Frontend UI** | Kotlin + Jetpack Compose (Material Design 3) | Interfaz táctil moderna, responsiva y adaptable a pantallas de móviles. |
| **Arquitectura** | MVVM + StateFlow + Clean Architecture | Separación clara de lógica de presentación, dominio y datos. |
| **Base de Datos** | Android Jetpack Room (SQLite + KSP) | Persistencia local offline rápida y segura para guardar patrones y estados. |
| **Motor JNI / NDK** | C++20 + CMake 3.22 | Puente de alto rendimiento para interactuar con librerías nativas sin sobrecarga. |
| **Motor Geométrico** | Rust 1.70+ (crate `miyuki_rust`) | Trigonometría y cálculos vectoriales de flecos y zarcillos libres de bugs de memoria. |
| **Motor de Scripting** | Lua 5.4.6 ANSI C (Oficial PUC-Rio) | Scripting ligero, embebible y ejecutable en tiempo real en dispositivos móviles. |

---

## 🚀 Compilación y Generación del APK

### Requisitos Previos
- **Android SDK** con API Level 34 (Android 14) y MinSdk 26 (Android 8.0+).
- **Android NDK** (versión 25.x o superior) y **CMake** 3.22.1+.
- **Rust Toolchain** con los targets de Android:
  ```bash
  rustup target add aarch64-linux-android x86_64-linux-android
  ```

### Comandos de Compilación

1. **Compilar el proyecto y generar el APK de desarrollo:**
   ```bash
   gradle assembleDebug
   ```
   El archivo generado se ubicará en:
   `app/build/outputs/apk/debug/app-debug.apk`

2. **Ejecutar la suite de pruebas unitarias:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

3. **Verificar el build completo:**
   ```bash
   gradle build
   ```

---

## 📱 Distribución para Teléfonos Móviles
El archivo APK generado es completamente autónomo y no requiere Google Play Services. Puede instalarse directamente activando "Instalar aplicaciones de fuentes desconocidas" en Android o publicarse en tiendas abiertas como:
- **Uptodown**
- **F-Droid / Repositorios Privados**
- **APKPure / Aptoide**

---

## 📄 Licencia
Este proyecto está bajo licencia MIT. Las fuentes de Lua pertenecen a PUC-Rio (Licencia MIT).
