#include <jni.h>
#include <string>
#include <vector>
#include <queue>
#include <sstream>
#include <android/log.h>

// Include official Lua headers directly (ANSI C Lua 5.4.6)
extern "C" {
#include "lua/lua.h"
#include "lua/lauxlib.h"
#include "lua/lualib.h"
}

#define TAG "MiyukiNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Declarations of Rust native functions from libmiyuki_rust.a
extern "C" {
    const char* rust_get_version();
    int32_t rust_calculate_earring_fringes(
        int32_t base_width,
        int32_t max_fringe_len,
        int32_t min_fringe_len,
        int32_t style,
        int32_t* out_lengths,
        int32_t max_count
    );
    int32_t rust_calculate_triangle_decrease_rows(int32_t base_width);
    int32_t rust_analyze_chart_grid(
        const uint32_t* pixels,
        int32_t width,
        int32_t height,
        int32_t* out_cols,
        int32_t* out_rows
    );
}

// Global buffer for capturing Lua print() output
static std::string g_lua_output;

static int custom_lua_print(lua_State* L) {
    int nargs = lua_gettop(L);
    for (int i = 1; i <= nargs; i++) {
        if (lua_isstring(L, i)) {
            g_lua_output += lua_tostring(L, i);
        } else if (lua_isnumber(L, i)) {
            g_lua_output += std::to_string(lua_tonumber(L, i));
        } else if (lua_isboolean(L, i)) {
            g_lua_output += lua_toboolean(L, i) ? "true" : "false";
        } else {
            g_lua_output += lua_typename(L, lua_type(L, i));
        }
        if (i < nargs) g_lua_output += "\t";
    }
    g_lua_output += "\n";
    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_getNativeEngineStatus(
    JNIEnv* env,
    jobject /* this */
) {
    std::ostringstream oss;
    oss << "=== MIYUKI NATIVE ENGINE STATUS ===\n";
    oss << "• C++ Core: NDK Clang 17 (C++20, JNI)\n";
    oss << "• Lua Engine: " << LUA_RELEASE << " (" << LUA_COPYRIGHT << ") [ANSI C Oficial sin wrappers]\n";
    
    // Call Rust to verify Rust linking
    const char* rust_ver = rust_get_version();
    oss << "• Rust Math Core: " << (rust_ver ? rust_ver : "No disponible") << "\n";
    oss << "• Estado: Activo, enlazado y listo para ejecución de scripts de patrones y zarcillos.";

    return env->NewStringUTF(oss.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_executeLuaSnippet(
    JNIEnv* env,
    jobject /* this */,
    jstring jscript
) {
    if (!jscript) return env->NewStringUTF("Error: Script nulo");

    const char* script = env->GetStringUTFChars(jscript, nullptr);
    g_lua_output.clear();

    // Create official Lua C state
    lua_State* L = luaL_newstate();
    if (!L) {
        env->ReleaseStringUTFChars(jscript, script);
        return env->NewStringUTF("Error: No se pudo instanciar el intérprete Lua C");
    }

    luaL_openlibs(L);

    // Override print to capture in output string
    lua_register(L, "print", custom_lua_print);

    int status = luaL_dostring(L, script);
    if (status != LUA_OK) {
        const char* err = lua_tostring(L, -1);
        g_lua_output += "\n[Error Lua]: ";
        g_lua_output += (err ? err : "Error desconocido");
    } else {
        if (lua_gettop(L) > 0 && !lua_isnil(L, -1)) {
            const char* ret = lua_tostring(L, -1);
            if (ret) {
                g_lua_output += "\n=> Retorno: ";
                g_lua_output += ret;
            }
        }
    }

    lua_close(L);
    env->ReleaseStringUTFChars(jscript, script);

    return env->NewStringUTF(g_lua_output.c_str());
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_executeLuaPatternScript(
    JNIEnv* env,
    jobject /* this */,
    jstring jscript,
    jint columns,
    jint rows
) {
    int total_cells = columns * rows;
    if (total_cells <= 0) return env->NewIntArray(0);

    std::vector<jint> grid(total_cells, 0xFFFFFFFF); // Default white

    if (!jscript) {
        jintArray result = env->NewIntArray(total_cells);
        env->SetIntArrayRegion(result, 0, total_cells, grid.data());
        return result;
    }

    const char* script = env->GetStringUTFChars(jscript, nullptr);

    lua_State* L = luaL_newstate();
    if (!L) {
        env->ReleaseStringUTFChars(jscript, script);
        jintArray result = env->NewIntArray(total_cells);
        env->SetIntArrayRegion(result, 0, total_cells, grid.data());
        return result;
    }

    luaL_openlibs(L);
    lua_pushinteger(L, columns);
    lua_setglobal(L, "COLUMNS");
    lua_pushinteger(L, rows);
    lua_setglobal(L, "ROWS");

    int status = luaL_dostring(L, script);
    if (status == LUA_OK) {
        // Look for function `getBead(col, row)`
        lua_getglobal(L, "getBead");
        if (lua_isfunction(L, -1)) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    lua_pushvalue(L, -1); // copy getBead function
                    lua_pushinteger(L, c);
                    lua_pushinteger(L, r);
                    if (lua_pcall(L, 2, 1, 0) == LUA_OK) {
                        if (lua_isinteger(L, -1)) {
                            grid[r * columns + c] = static_cast<jint>(lua_tointeger(L, -1));
                        } else if (lua_isnumber(L, -1)) {
                            grid[r * columns + c] = static_cast<jint>(lua_tonumber(L, -1));
                        }
                        lua_pop(L, 1);
                    } else {
                        lua_pop(L, 1); // pop error
                    }
                }
            }
        }
        lua_pop(L, 1); // pop getBead
    }

    lua_close(L);
    env->ReleaseStringUTFChars(jscript, script);

    jintArray result = env->NewIntArray(total_cells);
    env->SetIntArrayRegion(result, 0, total_cells, grid.data());
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_calculateEarringFringesRust(
    JNIEnv* env,
    jobject /* this */,
    jint baseWidth,
    jint maxFringeLen,
    jint minFringeLen,
    jint style
) {
    if (baseWidth <= 0) return env->NewIntArray(0);

    std::vector<int32_t> lengths(baseWidth, 0);

    // Direct invocation of the Rust algorithm compiled in libmiyuki_rust.a
    int32_t count = rust_calculate_earring_fringes(
        baseWidth,
        maxFringeLen,
        minFringeLen,
        style,
        lengths.data(),
        baseWidth
    );

    jintArray result = env->NewIntArray(count);
    env->SetIntArrayRegion(result, 0, count, reinterpret_cast<const jint*>(lengths.data()));
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_calculateEarringTriangleRowsRust(
    JNIEnv* env,
    jobject /* this */,
    jint baseWidth
) {
    // Direct invocation of Rust decrease calculation
    return rust_calculate_triangle_decrease_rows(baseWidth);
}

// -----------------------------------------------------------------------------
// Native CIELAB Color Science & Miyuki Delica 11/0 Beads Quantization
// -----------------------------------------------------------------------------

struct LabColor {
    float l;
    float a;
    float b;
};

struct MiyukiPaletteEntry {
    uint32_t argb;
    float r;
    float g;
    float b;
    LabColor lab;
};

static inline float srgb_to_linear(float c) {
    float norm = std::clamp(c / 255.0f, 0.0f, 1.0f);
    if (norm <= 0.04045f) {
        return norm / 12.92f;
    } else {
        return std::pow((norm + 0.055f) / 1.055f, 2.4f);
    }
}

static inline float f_lab(float t) {
    constexpr float delta = 6.0f / 29.0f;
    constexpr float delta_cubed = delta * delta * delta;
    if (t > delta_cubed) {
        return std::cbrt(t);
    } else {
        return t / (3.0f * delta * delta) + 4.0f / 29.0f;
    }
}

static LabColor rgb_to_lab(float r, float g, float b) {
    float r_lin = srgb_to_linear(r);
    float g_lin = srgb_to_linear(g);
    float b_lin = srgb_to_linear(b);

    // Standard sRGB to XYZ (D65 illuminant, 2-degree observer)
    float x = (r_lin * 0.4124564f + g_lin * 0.3575761f + b_lin * 0.1804375f) / 0.95047f;
    float y = (r_lin * 0.2126729f + g_lin * 0.7151522f + b_lin * 0.0721750f) / 1.00000f;
    float z = (r_lin * 0.0193339f + g_lin * 0.1191920f + b_lin * 0.9503041f) / 1.08883f;

    float fx = f_lab(x);
    float fy = f_lab(y);
    float fz = f_lab(z);

    float l = std::max(0.0f, 116.0f * fy - 16.0f);
    float a = 500.0f * (fx - fy);
    float b_val = 200.0f * (fy - fz);

    return { l, a, b_val };
}

static inline float delta_e_squared(const LabColor& c1, const LabColor& c2) {
    float dl = c1.l - c2.l;
    float da = c1.a - c2.a;
    float db = c1.b - c2.b;
    return dl * dl + da * da + db * db;
}

static inline float delta_e_cie76(const LabColor& c1, const LabColor& c2) {
    return std::sqrt(delta_e_squared(c1, c2));
}

// 20 Official Miyuki Delica 11/0 Beads catalog colors (Hex ARGB)
static const std::vector<uint32_t>& get_official_miyuki_colors() {
    static const std::vector<uint32_t> kBeads = {
        0xFF18171A, // DB-0010 Negro Mate
        0xFFDFAC38, // DB-0031 Oro 24K Galvanizado
        0xFFD8DCE0, // DB-0035 Plata Galvanizada
        0xFFFAF9F6, // DB-0200 Blanco Puro Opaco
        0xFFECE7DB, // DB-0201 Alabaster Perla
        0xFF00A4A6, // DB-0651 Turquesa Capri
        0xFFC62828, // DB-0723 Rojo Rubí Opaco
        0xFFEE5A3D, // DB-0729 Coral Mandarina
        0xFF197A52, // DB-0166 Verde Esmeralda
        0xFF1A367E, // DB-0753 Azul Cobalto Real
        0xFFEAA6A0, // DB-1530 Rosa Blush Pastel
        0xFFE09F25, // DB-0410 Mostaza Cálido
        0xFF7DCFB6, // DB-0656 Menta Pastel
        0xFF5E3573, // DB-0042 Amatista Púrpura
        0xFF65493B, // DB-0791 Bronce Metálico
        0xFFBBA7CD, // DB-1496 Lavanda Suave
        0xFF58B3C5, // DB-0113 Aguamarina Cielo
        0xFFF7D138, // DB-0725 Amarillo Canario
        0xFF437A4B, // DB-0653 Verde Jade Oliva
        0xFF0D0D0E  // DB-0310 Negro Ónix Brillante
    };
    return kBeads;
}

static const std::vector<MiyukiPaletteEntry>& get_cached_miyuki_palette() {
    static const std::vector<MiyukiPaletteEntry> kPalette = []() {
        std::vector<MiyukiPaletteEntry> list;
        const auto& raw = get_official_miyuki_colors();
        list.reserve(raw.size());
        for (uint32_t argb : raw) {
            float r = static_cast<float>((argb >> 16) & 0xFF);
            float g = static_cast<float>((argb >> 8) & 0xFF);
            float b = static_cast<float>(argb & 0xFF);
            LabColor lab = rgb_to_lab(r, g, b);
            list.push_back({ argb, r, g, b, lab });
        }
        return list;
    }();
    return kPalette;
}

static size_t find_closest_bead_index(float r, float g, float b, const std::vector<MiyukiPaletteEntry>& palette) {
    LabColor target_lab = rgb_to_lab(r, g, b);
    float min_dist = std::numeric_limits<float>::max();
    size_t best_idx = 0;

    for (size_t i = 0; i < palette.size(); ++i) {
        float dist = delta_e_squared(target_lab, palette[i].lab);
        if (dist < min_dist) {
            min_dist = dist;
            best_idx = i;
        }
    }
    return best_idx;
}

// Fast and high-quality bilinear interpolation with contrast and brightness adjustment
static void resample_and_adjust_pixels(
    const jint* src,
    int srcW,
    int srcH,
    uint32_t* dst,
    int dstW,
    int dstH,
    float brightness,
    float contrast
) {
    auto clamp255 = [](float v) -> uint32_t {
        if (v < 0.0f) return 0;
        if (v > 255.0f) return 255;
        return static_cast<uint32_t>(std::round(v));
    };

    auto adjust_channel = [contrast, brightness, clamp255](float val) -> uint32_t {
        float adj = (val - 128.0f) * contrast + 128.0f + brightness;
        return clamp255(adj);
    };

    float scaleX = static_cast<float>(srcW) / static_cast<float>(dstW);
    float scaleY = static_cast<float>(srcH) / static_cast<float>(dstH);

    for (int y = 0; y < dstH; ++y) {
        float srcY = (y + 0.5f) * scaleY - 0.5f;
        int y0 = std::clamp(static_cast<int>(std::floor(srcY)), 0, srcH - 1);
        int y1 = std::clamp(y0 + 1, 0, srcH - 1);
        float fy = srcY - std::floor(srcY);

        for (int x = 0; x < dstW; ++x) {
            float srcX = (x + 0.5f) * scaleX - 0.5f;
            int x0 = std::clamp(static_cast<int>(std::floor(srcX)), 0, srcW - 1);
            int x1 = std::clamp(x0 + 1, 0, srcW - 1);
            float fx = srcX - std::floor(srcX);

            uint32_t p00 = static_cast<uint32_t>(src[y0 * srcW + x0]);
            uint32_t p10 = static_cast<uint32_t>(src[y0 * srcW + x1]);
            uint32_t p01 = static_cast<uint32_t>(src[y1 * srcW + x0]);
            uint32_t p11 = static_cast<uint32_t>(src[y1 * srcW + x1]);

            // Bilinear interpolation on R, G, B
            float r0 = ((p00 >> 16) & 0xFF) * (1.0f - fx) + ((p10 >> 16) & 0xFF) * fx;
            float r1 = ((p01 >> 16) & 0xFF) * (1.0f - fx) + ((p11 >> 16) & 0xFF) * fx;
            float r = r0 * (1.0f - fy) + r1 * fy;

            float g0 = ((p00 >> 8) & 0xFF) * (1.0f - fx) + ((p10 >> 8) & 0xFF) * fx;
            float g1 = ((p01 >> 8) & 0xFF) * (1.0f - fx) + ((p11 >> 8) & 0xFF) * fx;
            float g = g0 * (1.0f - fy) + g1 * fy;

            float b0 = (p00 & 0xFF) * (1.0f - fx) + (p10 & 0xFF) * fx;
            float b1 = (p01 & 0xFF) * (1.0f - fx) + (p11 & 0xFF) * fx;
            float b = b0 * (1.0f - fy) + b1 * fy;

            uint32_t adjR = adjust_channel(r);
            uint32_t adjG = adjust_channel(g);
            uint32_t adjB = adjust_channel(b);

            dst[y * dstW + x] = 0xFF000000 | (adjR << 16) | (adjG << 8) | adjB;
        }
    }
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_convertPhotoToPatternNative(
    JNIEnv* env,
    jobject /* this */,
    jintArray srcPixels,
    jint srcWidth,
    jint srcHeight,
    jint targetCols,
    jint targetRows,
    jfloat brightness,
    jfloat contrast,
    jboolean useDithering,
    jint maxColors,
    jint backgroundMode,
    jfloat bgTolerance
) {
    if (!srcPixels || srcWidth <= 0 || srcHeight <= 0 || targetCols <= 0 || targetRows <= 0) {
        return env->NewIntArray(0);
    }

    jint* rawSrc = env->GetIntArrayElements(srcPixels, nullptr);
    if (!rawSrc) return env->NewIntArray(0);

    int totalTarget = targetCols * targetRows;
    std::vector<uint32_t> resizedPixels(totalTarget);

    // 1. Bilinear resampling and brightness/contrast adjustments
    resample_and_adjust_pixels(
        rawSrc,
        srcWidth,
        srcHeight,
        resizedPixels.data(),
        targetCols,
        targetRows,
        brightness,
        contrast
    );

    env->ReleaseIntArrayElements(srcPixels, rawSrc, JNI_ABORT);

    // 2. Intelligent Background Segmentation (White & AI Local Surface/Table Detection)
    std::vector<bool> is_background(totalTarget, false);

    if (backgroundMode == 1) {
        // MODE 1: IGNORAR FONDO BLANCO / CLARO (Lienzos, papel, dibujos, sprites)
        std::vector<bool> white_candidates(totalTarget, false);
        for (int i = 0; i < totalTarget; ++i) {
            uint32_t px = resizedPixels[i];
            uint8_t a = (px >> 24) & 0xFF;
            float r = static_cast<float>((px >> 16) & 0xFF);
            float g = static_cast<float>((px >> 8) & 0xFF);
            float b = static_cast<float>(px & 0xFF);

            if (a < 32) {
                white_candidates[i] = true;
                continue;
            }

            LabColor lab = rgb_to_lab(r, g, b);
            float l_threshold = 100.0f - (bgTolerance * 0.40f);
            float chroma_sq = lab.a * lab.a + lab.b * lab.b;

            bool is_bright_white = (lab.l >= l_threshold && chroma_sq < 220.0f);
            bool is_high_rgb = (r > 220.0f && g > 220.0f && b > 220.0f && std::abs(r - g) < 22.0f && std::abs(g - b) < 22.0f);

            if (is_bright_white || is_high_rgb) {
                white_candidates[i] = true;
            }
        }

        // Flood-fill desde los 4 bordes para segmentar el fondo exterior continuo
        std::queue<int> q;
        for (int x = 0; x < targetCols; ++x) {
            int top = 0 * targetCols + x;
            int bot = (targetRows - 1) * targetCols + x;
            if (white_candidates[top] && !is_background[top]) { is_background[top] = true; q.push(top); }
            if (white_candidates[bot] && !is_background[bot]) { is_background[bot] = true; q.push(bot); }
        }
        for (int y = 0; y < targetRows; ++y) {
            int left = y * targetCols + 0;
            int right = y * targetCols + (targetCols - 1);
            if (white_candidates[left] && !is_background[left]) { is_background[left] = true; q.push(left); }
            if (white_candidates[right] && !is_background[right]) { is_background[right] = true; q.push(right); }
        }

        const int dx[4] = {1, -1, 0, 0};
        const int dy[4] = {0, 0, 1, -1};
        while (!q.empty()) {
            int curr = q.front();
            q.pop();
            int cx = curr % targetCols;
            int cy = curr / targetCols;
            for (int dir = 0; dir < 4; ++dir) {
                int nx = cx + dx[dir];
                int ny = cy + dy[dir];
                if (nx >= 0 && nx < targetCols && ny >= 0 && ny < targetRows) {
                    int nidx = ny * targetCols + nx;
                    if (white_candidates[nidx] && !is_background[nidx]) {
                        is_background[nidx] = true;
                        q.push(nidx);
                    }
                }
            }
        }

        // Limpieza de orillas con blanco casi puro aislado
        for (int i = 0; i < totalTarget; ++i) {
            uint32_t px = resizedPixels[i];
            float r = static_cast<float>((px >> 16) & 0xFF);
            float g = static_cast<float>((px >> 8) & 0xFF);
            float b = static_cast<float>(px & 0xFF);
            if (r > 246.0f && g > 246.0f && b > 246.0f) {
                is_background[i] = true;
            }
        }
    } else if (backgroundMode == 2) {
        // MODE 2: AI LOCAL INTELIGENTE - SEGMENTACIÓN MULTI-CENTROIDE K-MEANS & SALIENCY MAP
        // 1. Muestreo Perimetral Estratificado de la Superficie
        std::vector<LabColor> border_samples;
        border_samples.reserve(targetCols * 4 + targetRows * 4);

        auto add_sample = [&](int x, int y) {
            if (x >= 0 && x < targetCols && y >= 0 && y < targetRows) {
                uint32_t px = resizedPixels[y * targetCols + x];
                float r = static_cast<float>((px >> 16) & 0xFF);
                float g = static_cast<float>((px >> 8) & 0xFF);
                float b = static_cast<float>(px & 0xFF);
                border_samples.push_back(rgb_to_lab(r, g, b));
            }
        };

        for (int x = 0; x < targetCols; ++x) {
            add_sample(x, 0);
            if (targetRows > 1) add_sample(x, 1);
            if (targetRows > 2) add_sample(x, targetRows - 1);
            if (targetRows > 3) add_sample(x, targetRows - 2);
        }
        for (int y = 0; y < targetRows; ++y) {
            add_sample(0, y);
            if (targetCols > 1) add_sample(1, y);
            if (targetCols > 2) add_sample(targetCols - 1, y);
            if (targetCols > 3) add_sample(targetCols - 2, y);
        }

        // 2. Clustering K-Means (K=3) para capturar texturas complejas (vetas de madera, mantel, sombras)
        const int K = 3;
        std::vector<LabColor> centroids;
        centroids.reserve(K);

        if (!border_samples.empty()) {
            // Inicialización de centroides (K-Means++ o dispersos en el muestreo)
            size_t step = border_samples.size() / K;
            for (int k = 0; k < K; ++k) {
                centroids.push_back(border_samples[std::min(k * step, border_samples.size() - 1)]);
            }

            // 6 iteraciones rápidas de convergencia EM (Expectation-Maximization)
            for (int iter = 0; iter < 6; ++iter) {
                std::vector<LabColor> accum(K, {0.0f, 0.0f, 0.0f});
                std::vector<int> counts(K, 0);

                for (const auto& sample : border_samples) {
                    int best_k = 0;
                    float min_dist = delta_e_squared(sample, centroids[0]);
                    for (int k = 1; k < K; ++k) {
                        float d = delta_e_squared(sample, centroids[k]);
                        if (d < min_dist) {
                            min_dist = d;
                            best_k = k;
                        }
                    }
                    accum[best_k].l += sample.l;
                    accum[best_k].a += sample.a;
                    accum[best_k].b += sample.b;
                    counts[best_k]++;
                }

                for (int k = 0; k < K; ++k) {
                    if (counts[k] > 0) {
                        centroids[k].l = accum[k].l / counts[k];
                        centroids[k].a = accum[k].a / counts[k];
                        centroids[k].b = accum[k].b / counts[k];
                    }
                }
            }
        }

        // 3. Mapa de Salicidad (Center-Prior / Saliency) y Clasificación
        float center_x = (targetCols - 1.0f) / 2.0f;
        float center_y = (targetRows - 1.0f) / 2.0f;
        float max_radius_sq = (center_x * center_x + center_y * center_y) + 1.0f;

        std::vector<bool> surface_candidates(totalTarget, false);
        for (int y = 0; y < targetRows; ++y) {
            float dy = y - center_y;
            for (int x = 0; x < targetCols; ++x) {
                int i = y * targetCols + x;
                uint32_t px = resizedPixels[i];
                uint8_t a = (px >> 24) & 0xFF;
                if (a < 32) {
                    surface_candidates[i] = true;
                    continue;
                }

                float dx = x - center_x;
                float r_sq = (dx * dx + dy * dy) / max_radius_sq; // 0.0 en centro, ~1.0 en esquinas

                float r = static_cast<float>((px >> 16) & 0xFF);
                float g = static_cast<float>((px >> 8) & 0xFF);
                float b = static_cast<float>(px & 0xFF);
                LabColor lab = rgb_to_lab(r, g, b);

                // Distancia mínima a cualquiera de los clusters de la superficie
                float min_delta = 9999.0f;
                for (const auto& c : centroids) {
                    float d = delta_e_cie76(lab, c);
                    if (d < min_delta) min_delta = d;
                }

                bool is_white = (lab.l > 90.0f && (lab.a * lab.a + lab.b * lab.b) < 180.0f);
                
                // Ponderación dinámica de tolerancia: los bordes aceptan más variación de mesa
                float effective_tolerance = bgTolerance * (0.85f + 0.35f * r_sq);

                if (min_delta <= effective_tolerance || is_white) {
                    surface_candidates[i] = true;
                }
            }
        }

        // 4. Propagación Conectada (Flood-fill) desde los 4 bordes para blindar el objeto
        std::queue<int> q;
        for (int x = 0; x < targetCols; ++x) {
            int top = 0 * targetCols + x;
            int bot = (targetRows - 1) * targetCols + x;
            if (surface_candidates[top] && !is_background[top]) { is_background[top] = true; q.push(top); }
            if (surface_candidates[bot] && !is_background[bot]) { is_background[bot] = true; q.push(bot); }
        }
        for (int y = 0; y < targetRows; ++y) {
            int left = y * targetCols + 0;
            int right = y * targetCols + (targetCols - 1);
            if (surface_candidates[left] && !is_background[left]) { is_background[left] = true; q.push(left); }
            if (surface_candidates[right] && !is_background[right]) { is_background[right] = true; q.push(right); }
        }

        const int dx[4] = {1, -1, 0, 0};
        const int dy[4] = {0, 0, 1, -1};
        while (!q.empty()) {
            int curr = q.front();
            q.pop();
            int cx = curr % targetCols;
            int cy = curr / targetCols;
            for (int dir = 0; dir < 4; ++dir) {
                int nx = cx + dx[dir];
                int ny = cy + dy[dir];
                if (nx >= 0 && nx < targetCols && ny >= 0 && ny < targetRows) {
                    int nidx = ny * targetCols + nx;
                    if (surface_candidates[nidx] && !is_background[nidx]) {
                        is_background[nidx] = true;
                        q.push(nidx);
                    }
                }
            }
        }
    }

    // 3. Select active palette
    const auto& full_palette = get_cached_miyuki_palette();
    std::vector<MiyukiPaletteEntry> active_palette;

    if (maxColors > 0 && static_cast<size_t>(maxColors) < full_palette.size()) {
        std::vector<int> counts(full_palette.size(), 0);
        int step = std::max(1, totalTarget / 400);
        for (int i = 0; i < totalTarget; i += step) {
            if (is_background[i]) continue; // No contar píxeles de fondo
            uint32_t px = resizedPixels[i];
            float r = static_cast<float>((px >> 16) & 0xFF);
            float g = static_cast<float>((px >> 8) & 0xFF);
            float b = static_cast<float>(px & 0xFF);
            size_t best = find_closest_bead_index(r, g, b, full_palette);
            counts[best]++;
        }

        std::vector<std::pair<int, size_t>> ranked;
        ranked.reserve(full_palette.size());
        for (size_t i = 0; i < full_palette.size(); ++i) {
            ranked.push_back({ counts[i], i });
        }
        std::sort(ranked.begin(), ranked.end(), [](const auto& a, const auto& b) {
            return a.first > b.first;
        });

        size_t k = std::clamp(static_cast<size_t>(maxColors), size_t(2), full_palette.size());
        for (size_t i = 0; i < k; ++i) {
            active_palette.push_back(full_palette[ranked[i].second]);
        }
        // Ensure contrast bounds
        bool has_dark = false;
        bool has_light = false;
        for (const auto& p : active_palette) {
            if (p.lab.l < 30.0f) has_dark = true;
            if (p.lab.l > 75.0f) has_light = true;
        }
        if (!has_dark) active_palette.push_back(full_palette[0]); // DB-0010 Negro Mate
        if (!has_light) active_palette.push_back(full_palette[3]); // DB-0200 Blanco Puro
    } else {
        active_palette = full_palette;
    }

    // 4. Floyd-Steinberg error diffusion in RGB buffer with isolated foreground
    struct PixelF { float r, g, b; };
    std::vector<PixelF> rgb_buf(totalTarget);
    for (int i = 0; i < totalTarget; ++i) {
        uint32_t px = resizedPixels[i];
        rgb_buf[i] = {
            static_cast<float>((px >> 16) & 0xFF),
            static_cast<float>((px >> 8) & 0xFF),
            static_cast<float>(px & 0xFF)
        };
    }

    std::vector<uint32_t> outBeads(totalTarget);

    for (int y = 0; y < targetRows; ++y) {
        for (int x = 0; x < targetCols; ++x) {
            int idx = y * targetCols + x;
            if (is_background[idx]) {
                outBeads[idx] = 0; // Celda vacía / sin cuenta (transparente)
                continue;
            }

            float r = std::clamp(rgb_buf[idx].r, 0.0f, 255.0f);
            float g = std::clamp(rgb_buf[idx].g, 0.0f, 255.0f);
            float b = std::clamp(rgb_buf[idx].b, 0.0f, 255.0f);

            size_t best_idx = find_closest_bead_index(r, g, b, active_palette);
            const auto& chosen = active_palette[best_idx];
            outBeads[idx] = chosen.argb;

            if (useDithering) {
                float err_r = r - chosen.r;
                float err_g = g - chosen.g;
                float err_b = b - chosen.b;

                // Difundir error solo a celdas que también sean parte del objeto (no al fondo)
                auto diffuse_if_valid = [&](int nx, int ny, float weight) {
                    if (nx >= 0 && nx < targetCols && ny >= 0 && ny < targetRows) {
                        int n_idx = ny * targetCols + nx;
                        if (!is_background[n_idx]) {
                            rgb_buf[n_idx].r += err_r * weight;
                            rgb_buf[n_idx].g += err_g * weight;
                            rgb_buf[n_idx].b += err_b * weight;
                        }
                    }
                };

                diffuse_if_valid(x + 1, y, 7.0f / 16.0f);
                diffuse_if_valid(x - 1, y + 1, 3.0f / 16.0f);
                diffuse_if_valid(x, y + 1, 5.0f / 16.0f);
                diffuse_if_valid(x + 1, y + 1, 1.0f / 16.0f);
            }
        }
    }

    // 5. Return native result directly to Kotlin
    jintArray result = env->NewIntArray(totalTarget);
    env->SetIntArrayRegion(result, 0, totalTarget, reinterpret_cast<const jint*>(outBeads.data()));
    return result;
}

// -------------------------------------------------------------
// PDF & CHART PATTERN CALIBRATION & SAMPLING ENGINE
// -------------------------------------------------------------

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_analyzeChartGridRustNative(
    JNIEnv* env,
    jobject /* this */,
    jintArray srcPixels,
    jint width,
    jint height
) {
    if (!srcPixels || width < 20 || height < 20) {
        jintArray fallback = env->NewIntArray(2);
        jint defVals[2] = {20, 40};
        env->SetIntArrayRegion(fallback, 0, 2, defVals);
        return fallback;
    }

    jint* pixels = env->GetIntArrayElements(srcPixels, nullptr);
    int32_t detected_cols = 20;
    int32_t detected_rows = 40;

    rust_analyze_chart_grid(
        reinterpret_cast<const uint32_t*>(pixels),
        width,
        height,
        &detected_cols,
        &detected_rows
    );

    env->ReleaseIntArrayElements(srcPixels, pixels, JNI_ABORT);

    jintArray result = env->NewIntArray(2);
    jint vals[2] = {detected_cols, detected_rows};
    env->SetIntArrayRegion(result, 0, 2, vals);
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_nativebridge_MiyukiNativeBridge_calibrateAndSamplePatternNative(
    JNIEnv* env,
    jobject /* this */,
    jintArray srcPixels,
    jint srcWidth,
    jint srcHeight,
    jint targetCols,
    jint targetRows,
    jint technique, // 0: Loom/Square, 1: Peyote, 2: Brick Stitch
    jfloat sampleWindowRatio,
    jfloat brightness,
    jfloat contrast,
    jint maxColors
) {
    if (!srcPixels || srcWidth <= 0 || srcHeight <= 0 || targetCols <= 0 || targetRows <= 0) {
        return env->NewIntArray(0);
    }

    int totalTarget = targetCols * targetRows;
    jint* pixels = env->GetIntArrayElements(srcPixels, nullptr);

    float cellW = static_cast<float>(srcWidth) / static_cast<float>(targetCols);
    float cellH = static_cast<float>(srcHeight) / static_cast<float>(targetRows);
    float winRatio = std::clamp(sampleWindowRatio, 0.20f, 0.85f);
    float halfWinW = (cellW * winRatio) * 0.5f;
    float halfWinH = (cellH * winRatio) * 0.5f;

    std::vector<uint32_t> outBeads(totalTarget);
    std::unordered_map<uint32_t, int> colorFrequency;

    for (int r = 0; r < targetRows; ++r) {
        for (int c = 0; c < targetCols; ++c) {
            // Apply technique-based offset (staggering)
            float offsetX = 0.0f;
            float offsetY = 0.0f;

            if (technique == 1) {
                // Peyote: vertical columns alternate half-step
                offsetY = (c % 2 == 1) ? (cellH * 0.5f) : 0.0f;
            } else if (technique == 2) {
                // Brick stitch: horizontal rows alternate half-step
                offsetX = (r % 2 == 1) ? (cellW * 0.5f) : 0.0f;
            }

            float cx = (c + 0.5f) * cellW + offsetX;
            float cy = (r + 0.5f) * cellH + offsetY;

            int minX = std::max(0, static_cast<int>(cx - halfWinW));
            int maxX = std::min(srcWidth - 1, static_cast<int>(cx + halfWinW));
            int minY = std::max(0, static_cast<int>(cy - halfWinH));
            int maxY = std::min(srcHeight - 1, static_cast<int>(cy + halfWinH));

            // Accumulate RGB within central window (trimmed mean to reject black grid line pixels)
            struct Sample { float r, g, b, lum; };
            std::vector<Sample> samples;
            samples.reserve((maxX - minX + 1) * (maxY - minY + 1));

            for (int py = minY; py <= maxY; ++py) {
                int rowIdx = py * srcWidth;
                for (int px = minX; px <= maxX; ++px) {
                    uint32_t p = static_cast<uint32_t>(pixels[rowIdx + px]);
                    float pr = static_cast<float>((p >> 16) & 0xFF);
                    float pg = static_cast<float>((p >> 8) & 0xFF);
                    float pb = static_cast<float>(p & 0xFF);
                    float lum = 0.299f * pr + 0.587f * pg + 0.114f * pb;
                    samples.push_back({pr, pg, pb, lum});
                }
            }

            float avgR = 250.0f, avgG = 250.0f, avgB = 250.0f;
            if (!samples.empty()) {
                // Sort by luminance and discard bottom/top 15% to eliminate grid lines and paper glare
                std::sort(samples.begin(), samples.end(), [](const Sample& a, const Sample& b) {
                    return a.lum < b.lum;
                });
                size_t discardCount = samples.size() > 6 ? (samples.size() * 15 / 100) : 0;
                size_t start = discardCount;
                size_t end = samples.size() - discardCount;
                if (start >= end) { start = 0; end = samples.size(); }

                float sumR = 0.0f, sumG = 0.0f, sumB = 0.0f;
                for (size_t i = start; i < end; ++i) {
                    sumR += samples[i].r;
                    sumG += samples[i].g;
                    sumB += samples[i].b;
                }
                size_t validCount = end - start;
                avgR = sumR / validCount;
                avgG = sumG / validCount;
                avgB = sumB / validCount;
            }

            // Apply contrast & brightness
            avgR = std::clamp(((avgR - 128.0f) * contrast) + 128.0f + brightness, 0.0f, 255.0f);
            avgG = std::clamp(((avgG - 128.0f) * contrast) + 128.0f + brightness, 0.0f, 255.0f);
            avgB = std::clamp(((avgB - 128.0f) * contrast) + 128.0f + brightness, 0.0f, 255.0f);

            // CIELAB match to Delica palette
            const auto& full_delica_palette = get_cached_miyuki_palette();
            size_t best_idx = find_closest_bead_index(avgR, avgG, avgB, full_delica_palette);
            uint32_t chosenArgb = full_delica_palette[best_idx].argb;

            int beadIdx = r * targetCols + c;
            outBeads[beadIdx] = chosenArgb;
            colorFrequency[chosenArgb]++;
        }
    }

    env->ReleaseIntArrayElements(srcPixels, pixels, JNI_ABORT);

    // If maxColors is specified, reduce noise by mapping low-frequency beads to the top N colors
    if (maxColors > 0 && static_cast<int>(colorFrequency.size()) > maxColors) {
        std::vector<std::pair<uint32_t, int>> freqList(colorFrequency.begin(), colorFrequency.end());
        std::sort(freqList.begin(), freqList.end(), [](const auto& a, const auto& b) {
            return a.second > b.second;
        });

        const auto& full_delica_palette = get_cached_miyuki_palette();
        std::vector<MiyukiPaletteEntry> topPalette;
        for (int i = 0; i < maxColors && i < static_cast<int>(freqList.size()); ++i) {
            uint32_t argb = freqList[i].first;
            for (const auto& d : full_delica_palette) {
                if (d.argb == argb) {
                    topPalette.push_back(d);
                    break;
                }
            }
        }

        if (!topPalette.empty()) {
            for (int i = 0; i < totalTarget; ++i) {
                uint32_t cur = outBeads[i];
                bool isTop = false;
                for (const auto& tp : topPalette) {
                    if (tp.argb == cur) { isTop = true; break; }
                }
                if (!isTop) {
                    float r = static_cast<float>((cur >> 16) & 0xFF);
                    float g = static_cast<float>((cur >> 8) & 0xFF);
                    float b = static_cast<float>(cur & 0xFF);
                    size_t best = find_closest_bead_index(r, g, b, topPalette);
                    outBeads[i] = topPalette[best].argb;
                }
            }
        }
    }

    jintArray result = env->NewIntArray(totalTarget);
    env->SetIntArrayRegion(result, 0, totalTarget, reinterpret_cast<const jint*>(outBeads.data()));
    return result;
}
