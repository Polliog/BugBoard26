#!/bin/sh
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CERT_DIR="$ROOT_DIR/nginx/certs"

# Genera certificati self-signed con mkcert se non esistono
if [ ! -f "$CERT_DIR/localhost.pem" ]; then
    echo "==> Generating local HTTPS certificates with mkcert..."

    if ! command -v mkcert > /dev/null 2>&1; then
        echo "ERROR: mkcert non trovato. Installalo:"
        echo "  macOS:   brew install mkcert"
        echo "  Linux:   sudo apt install mkcert  (o vedi https://github.com/FiloSottile/mkcert)"
        echo "  Windows: choco install mkcert"
        exit 1
    fi

    mkdir -p "$CERT_DIR"
    mkcert -install
    mkcert -cert-file "$CERT_DIR/localhost.pem" \
           -key-file "$CERT_DIR/localhost-key.pem" \
           localhost 127.0.0.1 ::1
    echo "==> Certificates generated in $CERT_DIR"
fi

cd "$ROOT_DIR"
echo "==> Starting stack with HTTPS proxy..."
docker compose --profile proxy up --build
