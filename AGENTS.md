# Reglas Operativas para Agentes de Inteligencia Artificial (AGENTS.md)

Este documento define el protocolo de comportamiento, las restricciones técnicas y el flujo de trabajo que **cualquier agente de IA** (incluidos modelos de Google DeepMind / AI Studio) debe seguir de forma estricta al interactuar con este proyecto.

---

## 🧭 Flujo de Desarrollo (Las 7 Fases)

Cada intervención debe alinearse con la fase correspondiente del ciclo de desarrollo:

1. **Diseñar (El Arquitecto)**:
   - Antes de modificar o añadir módulos, pensar la estructura, modelo de datos y decisiones arquitectónicas.
   - Respetar la separación MVVM, Clean Architecture y capas nativas.
2. **Construir (El Constructor)**:
   - Código modular, tipado estricto, sin *placeholders* ni ejemplos incompletos.
   - Manejo exhaustivo de casos borde y validaciones.
3. **Depurar (El Detective)**:
   - Razonamiento paso a paso (*Chain of Thought*): plantear hipótesis, revisar el flujo y aislar la causa raíz antes de editar.
4. **Revisar (El Crítico)**:
   - Vigilar seguridad, rendimiento en dispositivos móviles modestos y legibilidad.
5. **Optimizar (El Optimizador)**:
   - Minimizar recomposiciones en Jetpack Compose (`remember`, `derivedStateOf`).
   - Evitar cuellos de botella en cálculos en bucles de la cuadrícula.
6. **Testear (El Escudo)**:
   - Verificar compilación con `compile_applet` o Gradle (`gradle :app:testDebugUnitTest`).
   - Nunca usar emuladores locales o comandos `adb` prohibidos en el entorno del agente.
7. **Documentar (El Narrador)**:
   - Explicaciones claras, técnicas y en español, orientadas a la experiencia del usuario móvil.

---

## 📱 Reglas Obligatorias del Proyecto y del Usuario

1. **Entorno del Usuario**:
   - El usuario maneja este proyecto **exclusivamente desde su teléfono móvil** (no tiene computadora/PC).
   - Diseña siempre para pantallas táctiles verticales, gestos con una sola mano y componentes de al menos 48dp de área interactiva.

2. **Canal de Distribución de la Aplicación**:
   - La aplicación se distribuirá en plataformas abiertas como **Uptodown** o descarga directa de APK, **NO** en Google Play Store.
   - No asumir políticas restrictivas de Google Play si entran en conflicto con la soberanía del APK del usuario, pero mantener siempre buenas prácticas de seguridad e integridad del sistema operativo.

3. **Inclusión Obligatoria de Motores Nativos**:
   - Si el proyecto usa **C++**, **Rust** o **Lua**, **TIENEN QUE ESTAR INCLUIDOS** de forma obligatoria en la compilación (`CMakeLists.txt`, `Cargo.toml`, Gradle NDK).
   - **PROHIBIDO** saltarse la compilación nativa o sustituirla por funciones *fallback* ficticias en Kotlin si se requiere el motor correspondiente.

4. **Dependencias y Peso del APK**:
   - Al usuario **no le preocupa el peso final del archivo APK**, siempre que las librerías y dependencias sean 100% funcionales y robustas.
   - **Priorizar dependencias completas y probadas** sobre implementaciones caseras frágiles sin dependencias.

5. **Protección de Marcas y Derechos de Autor**:
   - **PROHIBIDO** utilizar marcas registradas protegidas en nombres de archivos o identificadores de paquetes que puedan exponer al usuario a reclamaciones de copyright o sanciones.

6. **Restricción de Parámetros del Sistema**:
   - En caso de realizar ajustes de rendimiento o perfiladores de juegos/boosters, **NUNCA usar propiedades `persist.sys.*`**.

7. **Gestión de Mensajes de Commit**:
   - Si existe un archivo `commit_message.txt`, la información debe estar redactada estrictamente en **español**.
   - **NO actualizar ni modificar** dicho archivo a menos que el usuario lo solicite expresamente.

8. **Eficiencia en la Inspección de Archivos**:
   - **NO revisar ni abrir archivos de código innecesarios** que no guarden relación directa con la solicitud activa del usuario.
   - Razonar y planificar minuciosamente antes de invocar herramientas de edición o comandos de terminal.

---

## 🛠️ Verificación y Validación

- Tras cualquier modificación de código en Kotlin, C++ o Rust, ejecutar `compile_applet` para confirmar la compilación limpia del proyecto.
- Si se modifican algoritmos o modelos de datos, ejecutar las pruebas unitarias con:
  ```bash
  gradle :app:testDebugUnitTest
  ```
