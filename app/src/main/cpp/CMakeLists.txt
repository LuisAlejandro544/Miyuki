cmake_minimum_required(VERSION 3.22.1)

project("miyuki_native" C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Compile official ANSI C Lua 5.4.6 as a static library
file(GLOB LUA_SRCS "lua/*.c")
list(FILTER LUA_SRCS EXCLUDE REGEX ".*(lua|luac)\\.c$")

add_library(lua_static STATIC ${LUA_SRCS})
target_include_directories(lua_static PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/lua")

# 2. Setup Rust build target and linker for the current ANDROID_ABI
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(RUST_TARGET "aarch64-linux-android")
    set(RUST_TARGET_ENV "AARCH64_LINUX_ANDROID")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(RUST_TARGET "x86_64-linux-android")
    set(RUST_TARGET_ENV "X86_64_LINUX_ANDROID")
elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(RUST_TARGET "armv7-linux-androideabi")
    set(RUST_TARGET_ENV "ARMV7_LINUX_ANDROIDEABI")
elseif(ANDROID_ABI STREQUAL "x86")
    set(RUST_TARGET "i686-linux-android")
    set(RUST_TARGET_ENV "I686_LINUX_ANDROID")
else()
    set(RUST_TARGET "aarch64-linux-android")
    set(RUST_TARGET_ENV "AARCH64_LINUX_ANDROID")
endif()

# Resolve linker from CMAKE_C_COMPILER (set by Android NDK toolchain file) or fallback NDK paths
set(RUST_LINKER "${CMAKE_C_COMPILER}")
if(NOT RUST_LINKER OR NOT EXISTS "${RUST_LINKER}")
    if(DEFINED ANDROID_NDK AND EXISTS "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
        set(RUST_LINKER "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
    elseif(DEFINED ENV{ANDROID_NDK_HOME} AND EXISTS "$ENV{ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
        set(RUST_LINKER "$ENV{ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
    elseif(EXISTS "/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
        set(RUST_LINKER "/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/${RUST_TARGET}24-clang")
    endif()
endif()

set(RUST_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../rust/miyuki_rust")
set(RUST_LIB "${RUST_DIR}/target/${RUST_TARGET}/release/libmiyuki_rust.a")

# Find cargo executable dynamically without hardcoding root user paths
set(CARGO_HINTS)
if(DEFINED ENV{CARGO_HOME} AND NOT "$ENV{CARGO_HOME}" STREQUAL "")
    list(APPEND CARGO_HINTS "$ENV{CARGO_HOME}/bin")
endif()
if(DEFINED ENV{HOME} AND NOT "$ENV{HOME}" STREQUAL "")
    list(APPEND CARGO_HINTS "$ENV{HOME}/.cargo/bin")
endif()
list(APPEND CARGO_HINTS "/root/.cargo/bin")

find_program(CARGO_BIN NAMES cargo HINTS ${CARGO_HINTS})

# Construct extra PATH to include rust binaries directory
set(EXTRA_ENV_PATH "$ENV{PATH}")
if(DEFINED ENV{HOME} AND NOT "$ENV{HOME}" STREQUAL "")
    set(EXTRA_ENV_PATH "$ENV{HOME}/.cargo/bin:${EXTRA_ENV_PATH}")
endif()
if(EXISTS "/root/.cargo/bin")
    set(EXTRA_ENV_PATH "/root/.cargo/bin:${EXTRA_ENV_PATH}")
endif()

# Build Rust library automatically before linking if cargo is available
if(CARGO_BIN)
    add_custom_command(
        OUTPUT "${RUST_LIB}"
        COMMAND ${CMAKE_COMMAND} -E env
            "PATH=${EXTRA_ENV_PATH}"
            "CARGO_TARGET_${RUST_TARGET_ENV}_LINKER=${RUST_LINKER}"
            ${CARGO_BIN} build --manifest-path "${RUST_DIR}/Cargo.toml" --target ${RUST_TARGET} --release
        WORKING_DIRECTORY "${RUST_DIR}"
        COMMENT "Compiling Rust Miyuki Geometry & Earring Engine for ${RUST_TARGET}..."
    )
    add_custom_target(build_miyuki_rust DEPENDS "${RUST_LIB}")
else()
    message(STATUS "Cargo not found; using existing Rust binary at ${RUST_LIB}")
    add_custom_target(build_miyuki_rust)
endif()

add_library(miyuki_rust STATIC IMPORTED GLOBAL)
set_target_properties(miyuki_rust PROPERTIES IMPORTED_LOCATION "${RUST_LIB}")
add_dependencies(miyuki_rust build_miyuki_rust)

# 3. Compile C++ JNI bridge
add_library(miyuki_native SHARED miyuki_engine.cpp)

target_include_directories(miyuki_native PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/lua"
)

target_link_libraries(miyuki_native PRIVATE
    lua_static
    miyuki_rust
    log
)
