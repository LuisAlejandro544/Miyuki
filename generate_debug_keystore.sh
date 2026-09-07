#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "⚡ GENERADOR OBLIGATORIO DE FIRMA DEBUG (debug.keystore)"
echo "=========================================================="

# Posicionarse siempre en el directorio raíz del proyecto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

KEYSTORE_FILE="$SCRIPT_DIR/debug.keystore"
KEYSTORE_BASE64="$SCRIPT_DIR/debug.keystore.base64"
ALIAS="androiddebugkey"
PASSWORD="android"
DNAME="CN=Android Debug,O=Android,C=US"

# Forzar la regeneración desde cero sin esperar nada
echo "[1/3] Preparando entorno de firma..."
rm -f "$KEYSTORE_FILE"

# Si existe el archivo base64 en el repositorio, intentar decodificarlo
if [ -f "$KEYSTORE_BASE64" ]; then
    echo "[2/3] Decodificando firma desde $KEYSTORE_BASE64..."
    base64 -d "$KEYSTORE_BASE64" > "$KEYSTORE_FILE" 2>/dev/null || true
fi

# Si no se creó o está vacío, generar inmediatamente desde cero con keytool
if [ ! -s "$KEYSTORE_FILE" ]; then
    echo "[2/3] Generando debug.keystore nuevo desde cero con keytool (sin esperas)..."
    keytool -genkeypair \
        -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$PASSWORD" \
        -keypass "$PASSWORD" \
        -dname "$DNAME"
fi

# Validar que el archivo existe y es válido
echo "[3/3] Validando integridad de la firma debug..."
if [ -s "$KEYSTORE_FILE" ]; then
    echo "✓ Archivo debug.keystore listo para firmar APK Debug."
    ls -lh "$KEYSTORE_FILE"
else
    echo "❌ Error: No se pudo generar el archivo debug.keystore"
    exit 1
fi
