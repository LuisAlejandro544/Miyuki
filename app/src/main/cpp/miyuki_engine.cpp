#include <jni.h>
#include <string>
#include <vector>
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
