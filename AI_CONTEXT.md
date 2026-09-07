# Contexto de Inteligencia Artificial y Dominio del Proyecto (AI Context)

Este documento proporciona a cualquier modelo de lenguaje o asistente de IA todo el contexto conceptual, técnico, de dominio y restricciones operativas necesarias para colaborar en este repositorio.

---

## 🎯 Dominio del Proyecto: Joyería y Tejido de Cuentas (Miyuki Delica)

### 1. ¿Qué son las Cuentas Miyuki Delica 11/0?
- Son cuentas tubulares de vidrio cilíndrico fabricadas en Japón por Miyuki Co., Ltd.
- **Dimensiones estándar**: 1.6 mm de longitud por 1.3 mm de diámetro exterior, con un orificio interior grande (~0.8 mm) que permite múltiples pasos de hilo sin romperse.
- **Factor de peso**: Aproximadamente entre 190 y 205 cuentas por gramo. En los cálculos de la aplicación se utiliza la constante estándar de taller: `105 cuentas / gramo` con margen de seguridad de hilo y merma.
- **Acabados típicos**: Mate, Metálico (*Metallic*), Galvanizado (*Duracoat Galvanized*), Lustre y Transparente.

### 2. Técnicas de Tejido Implementadas
- **Telar (Loom Weaving)**:
  - Las cuentas se disponen en una cuadrícula cartesiana regular (filas y columnas alineadas en 90°).
  - Requiere `N + 1` hilos de urdimbre para una pulsera de `N` columnas.
  - Altura efectiva por fila: ~1.6 mm.
- **Peyote (Peyote Stitch)**:
  - Las cuentas quedan escalonadas horizontalmente como una pared de ladrillos.
  - Cada fila alternada tiene un desfase de media cuenta (`0.5 * ancho`).
  - Altura efectiva por fila: ~1.35 mm (debido al encaje entre ranuras de la fila inferior).
  - Variantes: Peyote Par (giro natural en extremos) y Peyote Impar (giro con lazo en ocho).
- **Punto Ladrillo (Brick Stitch)**:
  - Las cuentas se asientan verticalmente sobre los puentes de hilo de la fila inferior.
  - Facilita disminuciones y aumentos fila a fila, lo que lo convierte en la técnica estándar para copas triangulares de zarcillos (*earrings*).
- **Flecos de Zarcillos (Fringes / Dangles)**:
  - Tiras de cuentas suspendidas libremente desde la base de la copa triangular.
  - Siguen curvas matemáticas (V, chevron invertido, senoidal, escalonado diagonal o rombo).

---

## 💻 Entorno y Perfil del Usuario

1. **Dispositivo del Usuario**: El usuario opera exclusivamente desde un **teléfono móvil Android** (sin computadora portátil ni PC de escritorio).
   - Toda la interfaz debe ser 100% accesible mediante interacción táctil con una sola mano.
   - Las fuentes de texto, botones y sliders deben respetar áreas de pulsación mínima de 48dp.
   - Las operaciones pesadas deben ejecutarse en subprocesos en segundo plano (Coroutines Dispatchers.Default / Dispatchers.IO) para no congelar la pantalla.
2. **Canal de Distribución**: Distribución directa mediante APK y tiendas de terceros como **Uptodown**, F-Droid o APKPure.
   - No depender de Google Play Services privativos.
   - No usar permisos invasivos innecesarios (almacenamiento amplio, cámara no solicitada, etc.).
3. **Propiedad Intelectual**: Evitar nombrar marcas comerciales protegidas en nombres de archivos o paquetes que puedan poner en riesgo al usuario ante políticas de derechos de autor.

---

## ⚙️ Reglas Inquebrantables de Arquitectura

1. **Obligatoriedad de Código Nativo Real**:
   - Se utilizan tres lenguajes nativos acoplados: **C++20**, **Rust** y **Lua 5.4 ANSI C**.
   - **PROHIBIDO** eliminar C++, Rust o Lua de `CMakeLists.txt` o `build.gradle.kts`.
   - **PROHIBIDO** reemplazar llamadas JNI nativas por simulaciones falsas o *placeholders* cuando el motor nativo ya está implementado y funcionando.
2. **Mapeo de Datos Gráficos**:
   - La cuadrícula de cuentas se representa como un arreglo unidimensional plano (`IntArray` o `List<Int>`) de tamaño `ancho * alto`.
   - El índice de una cuenta se calcula como: `index = col + row * columns`.
   - Cada color se almacena como un entero de 32 bits en formato `0xAARRGGBB`. El valor `0x00000000` (alfa 0) representa una celda vacía o transparente.
3. **Manejo de Memoria en Lua**:
   - Cada ejecución crea o reinicia un estado `lua_State*`.
   - Se inyectan funciones personalizadas (`print` redirigido a un stringstream de C++, y `getBead(col, row)` para rellenar matrices).
   - El estado siempre se cierra con `lua_close(L)` para evitar fugas de memoria en el runtime nativo de Android.
4. **Rust FFI Safety**:
   - Todas las funciones de Rust expuestas al JNI deben usar `#[no_mangle] pub extern "C"` y firmas compatibles con C-ABI (`int32_t`, punteros planos con longitud verificada).
   - Incluye análisis de gradientes de Sobel y autocorrelación (`rust_analyze_chart_grid`) para detección automática de cuadrículas en PDFs y fotos.
5. **Calibración de Gráficos y PDF en C++20**:
   - Muestreo centrado en el interior de cada cuenta (`sampleWindowRatio` del 25% al 80%) para evitar capturar las líneas impresas negras de las cartas de patrones.
   - Algoritmo *Trimmed Mean* que descarta el 15% superior e inferior de luminosidad de la muestra antes de calcular el promedio RGB.
   - Mapeo euclidiano en espacio cromático perceptualmente uniforme **CIELAB (D65)** al catálogo de 20 tonos Delica 11/0.
   - Reducción opcional a paleta dominante de $N$ colores para limpiar ruido de escaneo.

6. **AI Local en C++20 para Detección de Fondo y Mesas**:
   - Algoritmo no supervisado on-device basado en **K-Means Clustering ($K=3$)** en espacio CIELAB para modelar superficies texturizadas (madera, manteles, baldosas) a partir de muestras perimetrales estratificadas.
   - **Mapa de Salicidad (Center-Prior / Saliency)** con ponderación radial gaussiana que expande la tolerancia en las orillas y protege el centro de la pieza.
   - **Segmentación Conectada por Flood-Fill**: Aísla la superficie continua exterior e impide la perforación de cuentas con colores similares en el interior del motivo.

7. **Depuración en Dispositivo Móvil (Sin PC / ADB)**:
   - Dado que el usuario opera el proyecto exclusivamente desde un teléfono celular sin ordenador, se integran herramientas de diagnóstico in-app:
   - **Lynx**: Se accede mediante `DebugTools.openLynxLogcat(context)` desde la TopAppBar o la consola de Lua. Muestra el buffer de Logcat del sistema en tiempo real.
   - **LeakCanary 2.14**: Se ejecuta como servicio/actividad independiente en builds de depuración para auditar la memoria RAM ante la manipulación de Bitmaps y PDF grandes.

---

## 🧭 Glosario Rápido de Archivos Clave

- `app/src/main/cpp/miyuki_engine.cpp`: Contiene las funciones JNI nativas, inicialización de Lua, dithering Floyd-Steinberg, AI local (K-Means + Saliency Map) y calibración *Trimmed Mean* en C++20.
- `app/src/main/rust/miyuki_rust/src/lib.rs`: Contiene las funciones trigonométricas de zarcillos y la auto-detección matemática de cuadrículas (`rust_analyze_chart_grid`).
- `app/src/main/java/com/example/ui/components/BeadCanvas.kt`: Componente Jetpack Compose que dibuja la cuadrícula táctil y representa visualmente cuentas cilíndricas según la técnica (Telar, Peyote y Brick Stitch).
- `app/src/main/java/com/example/ui/screens/EditorScreen.kt`: Editor táctil interactivo con cuadrícula y paleta de herramientas de dibujo.
- `app/src/main/java/com/example/ui/screens/TrackerScreen.kt`: Tejedor interactivo paso a paso con fila activa, lupa visual y hápticos.
- `app/src/main/java/com/example/ui/screens/GeneratorScreen.kt`: Estudio creativo con captura directa de cámara, galería, AI local de eliminación de fondos/mesas, calibrador PDF y consola Lua.
- `app/src/main/java/com/example/ui/screens/CatalogScreen.kt`: Catálogo de cuentas oficiales Delica 11/0.
- `app/src/main/java/com/example/ui/screens/GuideCalculatorScreen.kt`: Calculadora de muñeca, hilos y consumo de material.
- `app/src/main/java/com/example/util/PdfPatternExtractor.kt`: Renderizado nativo de páginas PDF con `PdfRenderer` y utilidades de decodificación/recorte de bitmaps.
- `app/src/main/java/com/example/util/DebugTools.kt`: Lanzador y configuración del visor de Logcat interactivo (Lynx) en pantalla.
- `app/src/main/java/com/example/nativebridge/MiyukiNativeBridge.kt`: Interfaz Kotlin con `System.loadLibrary("miyuki_native")`.
- `app/src/main/java/com/example/ui/viewmodel/PatternViewModel.kt`: Estado reactivo principal de la aplicación, coordinación de importación de PDF/fotos, cámara y parámetros de calibración.
