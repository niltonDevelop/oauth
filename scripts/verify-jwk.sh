#!/usr/bin/env bash
set -euo pipefail

# Verifica que oauth (prod local) expone JWKS con el key-id esperado.
# Uso: ./scripts/verify-jwk.sh [issuer-url]

ISSUER="${1:-http://127.0.0.1:9190}"

echo "==> OpenID configuration"
curl -sf "${ISSUER}/.well-known/openid-configuration" | python3 -m json.tool | head -20

echo ""
echo "==> JWKS"
curl -sf "${ISSUER}/oauth2/jwks" | python3 -m json.tool
