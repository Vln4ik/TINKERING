#!/usr/bin/env sh
set -eu

# Optional: install extra CA certs at container start (for environments with non-standard trust stores).
# Mount your certs to /app/extra-ca as *.crt / *.pem files (PEM format).
EXTRA_CA_DIR="${EXTRA_CA_DIR:-/app/extra-ca}"

if [ -d "$EXTRA_CA_DIR" ]; then
  found="$(ls -1 "$EXTRA_CA_DIR"/*.crt "$EXTRA_CA_DIR"/*.pem 2>/dev/null || true)"
  if [ -n "$found" ]; then
    echo "[entrypoint] Installing extra CA certs from $EXTRA_CA_DIR"
    for f in $found; do
      b="$(basename "$f")"
      # Skip macOS resource forks like ._README.md
      case "$b" in
        ._* ) continue ;;
      esac
      # update-ca-certificates expects .crt extension
      dst="/usr/local/share/ca-certificates/${b%.*}.crt"
      cp -f "$f" "$dst" 2>/dev/null || true
      chmod 0644 "$dst" 2>/dev/null || true
    done
    update-ca-certificates || true

    # Some Python HTTP stacks use certifi bundle instead of OS trust store.
    # Append extra CAs into certifi bundle as well (best-effort).
    if command -v python >/dev/null 2>&1; then
      CERTIFI_FILE="$(python -c 'import certifi; print(certifi.where())' 2>/dev/null || true)"
      if [ -n "${CERTIFI_FILE:-}" ] && [ -f "$CERTIFI_FILE" ]; then
        echo "[entrypoint] Appending extra CA certs to certifi bundle: $CERTIFI_FILE"
        for c in /usr/local/share/ca-certificates/*.crt; do
          [ -f "$c" ] || continue
          echo "" >> "$CERTIFI_FILE" 2>/dev/null || true
          cat "$c" >> "$CERTIFI_FILE" 2>/dev/null || true
        done
      fi
    fi
  fi
fi

exec uvicorn app.main:app --host 0.0.0.0 --port 8080


