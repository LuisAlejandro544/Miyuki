# Arquitectura y Estructura del Proyecto

Este documento detalla la estructura física del repositorio, la responsabilidad de cada paquete y el flujo de datos entre las capas de Kotlin, C++, Rust y Lua.

---

## 🌳 Árbol de Directorios

```
/
├── app/
│   ├── build.gradle.kts                # Configuración de compilación de Android, NDK, KSP y dependencias
│   ├── proguard-rules.pro              # Reglas de ofuscación y mantenimiento de clases JNI
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # Manifiesto principal de la aplicación
│       │   ├── cpp/                    # Capa nativa C++20 y motor Lua ANSI C
│       │   │   ├── CMakeLists.txt      # Script de CMake que enlaza C++, Lua y Rust
│       │   │   ├── miyuki_engine.cpp   # Implementación del JNI Bridge (Kotlin <-> C++ <-> Rust/Lua)
│       │   │   └── lua/                # Código fuente oficial de Lua 5.4.6 ANSI C (PUC-Rio)
│       │   ├── rust/                   # Capa nativa de cálculos geométricos y matemáticos
│       │   │   └── miyuki_rust/
│       │   │       ├── Cargo.toml      # Configuración del crate estático de Rust (crate-type = ["staticlib"])
│       │   │       ├── Cargo.lock
│       │   │       └── src/
│       │   │           └── lib.rs      # Lógica matemática de zarcillos, flecos y auto-detección de cuadrícula (Rust)
│       │   ├── java/com/example/       # Código fuente Kotlin (Jetpack Compose, Room, MVVM)
│       │   │   ├── MainActivity.kt     # Actividad única con Edge-to-Edge habilitado
│       │   │   ├── data/
│       │   │   │   ├── local/          # Capa de datos y persistencia Room
│       │   │   │   │   ├── AppDatabase.kt        # Definición de base de datos Room SQLite
│       │   │   │   │   ├── PatternDao.kt         # Consultas reactivas (Flow) para patrones
│       │   │   │   │   └── PatternEntity.kt      # Entidad persistente de patrones y cuadrículas
│       │   │   │   └── model/          # Modelos de dominio
│       │   │   │       ├── BeadPattern.kt        # Modelo principal de patrón, técnica y cuadrícula
│       │   │   │       ├── BeadTechnique.kt      # Enums: LOOM, PEYOTE, BRICK_STITCH
│       │   │   │       └── MiyukiBead.kt         # Catálogo oficial Delica 11/0 y paletas curadas
│       │   │   ├── generator/          # Generadores de patrones procedurales
│       │   │   │   └── PatternGenerator.kt   # Algoritmos de chevrons, grecas, rombos y rayas
│       │   │   ├── util/               # Utilidades de documentos, gráficos y diagnóstico móvil
│       │   │   │   ├── PdfPatternExtractor.kt# Rasterizador nativo de PDF y decodificador de gráficos
│       │   │   │   └── DebugTools.kt         # Lanzador y configuración de la consola Logcat Lynx en pantalla
│       │   │   ├── nativebridge/       # Puente JNI con libmiyuki_native.so
│       │   │   │   └── MiyukiNativeBridge.kt # Declaraciones de métodos C++, Rust y Lua
│       │   │   └── ui/                 # Capa de presentación (Jetpack Compose)
│       │   │       ├── MiyukiMainApp.kt      # Contenedor de navegación y barra inferior
│       │   │       ├── components/           # Componentes visuales reutilizables
│       │   │       │   └── BeadCanvas.kt     # Lienzo de dibujo y renderizado de cuadrículas (Loom, Peyote, Brick Stitch)
│       │   │       ├── screens/              # Pantallas de la aplicación
│       │   │       │   ├── CatalogScreen.kt        # Explorador del catálogo Miyuki Delica 11/0
│       │   │       │   ├── EditorScreen.kt         # Editor táctil con cuadrícula interactiva y herramientas de dibujo
│       │   │       │   ├── GeneratorScreen.kt      # Generador algorítmico, Foto a Patrón con Cámara/AI, Calibrador PDF y Lua
│       │   │       │   ├── GuideCalculatorScreen.kt# Calculadora de muñeca, hilos y técnicas
│       │   │       │   └── TrackerScreen.kt        # Tejedor paso a paso con fila activa, hápticos y zoom
│       │   │       ├── theme/              # Sistema de diseño Material 3
│       │   │       │   ├── Color.kt          # Colores inspirados en cuentas Miyuki (Oro, Turquesa, etc.)
│       │   │       │   ├── Theme.kt          # ColorScheme claro y oscuro
│       │   │       │   └── Type.kt           # Tipografía de la aplicación
│       │   │       └── viewmodel/          # Gestión de estado UI
│       │   │           └── PatternViewModel.kt   # ViewModel central con StateFlows y Coroutines
│       │   └── res/                    # Recursos Android
│       │       ├── values/
│       │       │   └── strings.xml     # Strings localizados en español
│       │       ├── drawable/           # Iconos vectoriales y recursos gráficos
│       │       └── mipmap-*/           # Iconos adaptativos del lanzador
│       └── test/                       # Pruebas unitarias de arquitectura en JVM
│           ├── ExampleRobolectricTest.kt       # Pruebas de integración con Robolectric
│           └── MiyukiPatternGeneratorTest.kt   # Pruebas unitarias de algoritmos y catálogo
├── build.gradle.kts                    # Configuración Gradle raíz del proyecto
├── settings.gradle.kts                 # Repositorios Maven y módulos del proyecto
├── metadata.json                       # Metadatos de la plataforma AI Studio
├── README.md                           # Documentación principal del proyecto
├── ROADMAP.md                          # Plan de desarrollo y futuras versiones
├── STRUCTURE.md                        # Descripción de la arquitectura (este archivo)
├── AI_CONTEXT.md                       # Contexto técnico para inteligencias artificiales
└── AGENTS.md                           # Directrices operativas para agentes automatizados
```

---

## 🔄 Flujo de Datos y Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                 Jetpack Compose UI (M3)                     │
│  [HomeScreen] [EditorScreen] [TrackerScreen] [Generator]    │
└──────────────────────────────┬──────────────────────────────┘
                               │ Eventos del usuario / Composable State
                               ▼
┌─────────────────────────────────────────────────────────────┐
│               PatternViewModel (StateFlow)                  │
│   • Coordina el estado de la cuadrícula, herramienta activa │
│   • Gestiona la fila en curso del tejedor interactivo       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               ▼ Operaciones CRUD             ▼ Cálculos algorítmicos
┌──────────────────────────────┐ ┌─────────────────────────────┐
│     Room Database (DAO)      │ │   MiyukiNativeBridge (JNI)  │
│      • SQLite Local          │ └──────────────┬──────────────┘
│      • Cache de patrones     │                ▼ JNI Call
│      • Historial de avance   │ ┌─────────────────────────────┐
└──────────────────────────────┘ │  C++20: libmiyuki_native.so │
                                 └──────┬───────────────┬──────┘
                                        │               │
                                        ▼               ▼
                         ┌─────────────────────┐ ┌─────────────────────────┐
                         │ Lua 5.4 ANSI C Core │ │   Rust Static Engine    │
                         │ • Intérprete oficial│ │ • Trigonometría zarcillo│
                         │ • Captura de prints │ │ • Auto-detección matriz │
                         │ • getBead(col, row) │ │ • Gradientes de Sobel   │
                         └─────────────────────┘ └─────────────────────────┘
                                        │
                                        ▼
                         ┌──────────────────────────────────────────────┐
                         │           C++20 Color & AI Core              │
                         │ • Colorimetría CIELAB (D65)                  │
                         │ • Difusión de error Floyd-Steinberg          │
                         │ • AI Local: K-Means (K=3) + Saliency Map     │
                         │ • Segmentación de fondo y mesa por Flood-Fill│
                         │ • Muestreo Trimmed Mean para PDFs            │
                         │ • Compensación Peyote / Brick / Loom         │
                         └──────────────────────────────────────────────┘
```

---

## ⚙️ Integración del Sistema de Compilación

1. **Gradle (`app/build.gradle.kts`)**:
   - Activa `externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }`.
   - Inyecta flags de C++20 (`-std=c++20`).
   - Declara filtros de ABI para dispositivos móviles modernos (`arm64-v8a`) y emuladores (`x86_64`).
   - Configura Room mediante KSP (*Kotlin Symbol Processing*).

2. **CMake (`app/src/main/cpp/CMakeLists.txt`)**:
   - Compila directamente los archivos fuentes de Lua ANSI C (`lua/*.c`) en una librería estática `lua_static`.
   - Invoca a `cargo build --release` para la arquitectura destino correspondiente mediante un `custom_target`.
   - Enlaza `libmiyuki_rust.a` y `liblua_static.a` dentro de la librería compartida final `libmiyuki_native.so`.

3. **Rust Crate (`app/src/main/rust/miyuki_rust`)**:
   - Compilado con `crate-type = ["staticlib"]`.
   - Expone funciones C-ABI mediante `#[no_mangle] pub extern "C"`.
   - No tiene dependencias de runtime externas pesadas, garantizando binarios compactos y seguros.
