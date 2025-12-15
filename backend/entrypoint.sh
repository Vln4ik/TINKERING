#!/usr/bin/env sh
set -eu

# Optional: install extra CA certs at container start (for environments with non-standard trust stores).
# Mount your certs to /app/extra-ca as *.crt files.
EXTRA_CA_DIR="${EXTRA_CA_DIR:-/app/extra-ca}"

if [ -d "$EXTRA_CA_DIR" ]; then
  found="$(ls -1 "$EXTRA_CA_DIR"/*.crt 2>/dev/null || true)"
  if [ -n "$found" ]; then
    echo "[entrypoint] Installing extra CA certs from $EXTRA_CA_DIR"
    cp -f "$EXTRA_CA_DIR"/*.crt /usr/local/share/ca-certificates/ 2>/dev/null || true
    update-ca-certificates || true
  fi
fi

exec uvicorn app.main:app --host 0.0.0.0 --port 8080


