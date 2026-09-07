# Hoja de Ruta de Desarrollo (Roadmap)

Este documento describe la planificación de versiones, objetivos técnicos y evolución funcional del taller de diseño de patrones de cuentas para Android.

---

## 📌 Versión 1.0: Fundación y Motores Nativos (Versión Actual)

- [x] **Núcleo de Diseño y Cuadrículas Táctiles**:
  - [x] Soporte para Telar de cuentas (*Bead Loom*).
  - [x] Soporte para tejido *Peyote* (par e impar con desfase escalonado).
  - [x] Soporte para *Punto Ladrillo (Brick Stitch)* con aumentos y disminuciones.
  - [x] Herramientas de edición: Lápiz, Borrador, Bote de pintura (*Flood Fill*) y Cuentagotas (*Eyedropper*).
  - [x] Modo simetría en tiempo real.
- [x] **Base de Datos Local (Room Database)**:
  - [x] Persistencia de patrones completos con dimensiones, técnica, nombre y fecha.
  - [x] Historial de progreso en el seguimiento del tejido.
- [x] **Tejedor Interactivo Paso a Paso**:
  - [x] Aislamiento de fila activa con desglose de cuentas por código y color.
  - [x] Navegación rápida con botones táctiles grandes optimizados para una sola mano en teléfono.
  - [x] Vibración háptica al avanzar fila.
  - [x] Modos reversible (abajo-arriba y arriba-abajo).
- [x] **Taller de Zarcillos Geométrico (Rust Engine)**:
  - [x] Cálculo de copa triangular en Brick Stitch.
  - [x] Algoritmos trigonométricos de flecos: V clásica, chevron invertido, cascada senoidal, escalonado y rombo.
  - [x] Enlace estático FFI con Rust (`libmiyuki_rust.a`).
- [x] **Estudio Algorítmico Lua 5.4 ANSI C**:
  - [x] Compilación directa del código fuente oficial de Lua 5.4.6 sin wrappers.
  - [x] Ejecución de código paramétrico en el intérprete C con captura de consola en pantalla.
  - [x] Generación de cuadrículas bidimensionales exportables al editor.
- [x] **Calculadora de Muñeca y Materiales**:
  - [x] Compensación de medidas según tipo de broche (macramé, tubo deslizable, imán, elástico).
  - [x] Estimación de cuentas totales, gramos de cuentas Delica 11/0 y metros de hilo requeridos.

---

## 🚀 Versión 1.1: Conversión de Imágenes y Calibración de Documentos (Completada)

- [x] **Conversor Fotográfico a Patrón de Cuentas (C++20 Nativo)**:
  - [x] Selector de imágenes local mediante Android Photo Picker (cero permisos invasivos).
  - [x] Algoritmo de cuantización de color en C++20 con dithering Floyd-Steinberg y ajuste dinámico de brillo/contraste.
  - [x] Asignación cromática de alta fidelidad al catálogo oficial Miyuki Delica 11/0 mediante distancia perceptual en espacio CIELAB (D65).
- [x] **Importador y Calibrador de Documentos PDF y Gráficos (Rust + C++20)**:
  - [x] Rasterización nativa de archivos PDF de patrones (Etsy, Pinterest, revistas) con `PdfRenderer` sin dependencias externas.
  - [x] Encuadre táctil interactivo con previsualización en Canvas de la cuadrícula calibrada.
  - [x] Detección automática de columnas y filas con motor **Rust** (`miyuki_rust`) mediante cálculo de gradientes y autocorrelación.
  - [x] Muestreo inteligente de color en C++20 con ventana central de cuenta (*Trimmed Mean*) para descartar líneas impresas y reflejos.
  - [x] Mapeo con compensación de técnica (Peyote, Telar y Brick Stitch).
- [x] **Herramientas de Depuración Móvil In-App (Desarrollo sin PC ni ADB)**:
  - [x] Consola interactiva **Lynx** para monitorizar Logcat, trazas C++20, Rust, Lua y excepciones de Kotlin directamente en el móvil.
  - [x] Integración de **LeakCanary 2.14** para detección automática de fugas de memoria y retención indebida de Bitmaps de PDF.
  - [x] Acceso directo a Lynx desde la barra superior y desde el estudio de Lua.
- [ ] **Generador de Fichas de Trabajo en PDF**:
  - [ ] Generación de documentos PDF vectoriales con la cuadrícula numerada lista para imprimir.
  - [ ] Tabla de consumo de materiales desglosada por código de delica, color y cantidad exacta de piezas.
  - [ ] Instrucciones escritas de ensartado paso a paso por fila (*Word Chart*).
- [ ] **Exportación e Importación de Respaldo**:
  - [ ] Exportación de patrones a archivo comprimido `.bead` (JSON + miniatura PNG).
  - [ ] Compartir directamente a través de WhatsApp, Telegram o correo electrónico desde el teléfono.

---

## 🎨 Versión 1.2: Expansión de Formas y Joyería 3D

- [ ] **Biblioteca de Formas Geométricas**:
  - [ ] Patrones para anillos peyote con cálculo de tallas universales (US / Europa / Asia).
  - [ ] Patrones para gargantillas y brazaletes de gran formato (hasta 50 columnas).
  - [ ] Estructuras tridimensionales: tubos peyote circulares y cubos de cuentas.
- [ ] **Soporte de Variedades de Cuentas**:
  - [ ] Rocallas redondas 11/0, 15/0 y 8/0 con espaciado elíptico.
  - [ ] Cuentas de dos orificios (Tila, Half Tila y Quarter Tila).
  - [ ] Configuración personalizada de dimensiones de cuenta en milímetros.
- [ ] **Paletas Personalizadas y Favoritos**:
  - [ ] Creación de paletas del usuario almacenadas en Room.
  - [ ] Búsqueda y filtrado rápido de colores por código de catálogo.

---

## ⚡ Versión 2.0: Scripting Extendido y Ecosistema Artesanal

- [ ] **Biblioteca de Scripts Lua para Artesanos**:
  - [ ] Funciones predefinidas en Lua para generar grecas prehispánicas, mosaicos árabes, flores de loto y patrones geométricos paramétricos.
  - [ ] Soporte para compartir y descargar scripts de generación de patrones comunitarios.
- [ ] **Renderizado Acelerado por GPU**:
  - [ ] Optimización del renderizado de la cuadrícula con Canvas de Compose y buffers nativos para manejar proyectos de más de 100,000 cuentas a 60/120 FPS sin calentamiento del dispositivo.
- [ ] **Modo Taller Manos Libres**:
  - [ ] Control por voz local (adelantar fila, retroceder, repetir fila) para tejer sin tener que tocar la pantalla con las manos ocupadas en la aguja y el hilo.
  - [ ] Sonidos de confirmación diferenciados por color de cuenta al avanzar la aguja.
