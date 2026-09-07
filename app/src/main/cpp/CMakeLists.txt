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
    set(RUST_LINKER "/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android24-clang")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(RUST_TARGET "x86_64-linux-android")
    set(RUST_LINKER "/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android24-clang")
else()
    set(RUST_TARGET "aarch64-linux-android")
    set(RUST_LINKER "/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android24-clang")
endif()

set(RUST_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../rust/miyuki_rust")
set(RUST_LIB "${RUST_DIR}/target/${RUST_TARGET}/release/libmiyuki_rust.a")

set(CARGO_BIN "/root/.cargo/bin/cargo")
if(NOT EXISTS "${CARGO_BIN}")
    find_program(CARGO_BIN cargo REQUIRED)
endif()

# Build Rust library automatically before linking
add_custom_command(
    OUTPUT "${RUST_LIB}"
    COMMAND ${CMAKE_COMMAND} -E env
        "CARGO_HOME=/root/.cargo"
        "RUSTUP_HOME=/root/.rustup"
        "PATH=/root/.cargo/bin:$ENV{PATH}"
        "CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER=/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android24-clang"
        "CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER=/opt/android/sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android24-clang"
        ${CARGO_BIN} build --manifest-path "${RUST_DIR}/Cargo.toml" --target ${RUST_TARGET} --release
    WORKING_DIRECTORY "${RUST_DIR}"
    COMMENT "Compiling Rust Miyuki Geometry & Earring Engine for ${RUST_TARGET}..."
)

add_custom_target(build_miyuki_rust DEPENDS "${RUST_LIB}")

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
