#!/usr/bin/env bash
set -euo pipefail

# Genera par RSA (PKCS#8 + SPKI) y un archivo .env para probar SPRING_PROFILES_ACTIVE=prod en local.
#
# Uso:
#   ./scripts/generate-jwk.sh
#   ./scripts/run-prod-local.sh
#
# IMPORTANTE: .jwk-local/ está en .gitignore — no commitear claves.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${1:-${ROOT_DIR}/.jwk-local}"

mkdir -p "${OUT_DIR}"
chmod 700 "${OUT_DIR}"

PRIVATE_PKCS8="${OUT_DIR}/private-pkcs8.pem"
PUBLIC_PEM="${OUT_DIR}/public.pem"
ENV_FILE="${OUT_DIR}/oauth-prod-local.env"

echo "==> Generando claves RSA 2048 en ${OUT_DIR}"

# PKCS#8 (requerido por RsaKeyLoader — no PKCS#1 tradicional)
openssl genpkey -algorithm RSA -out "${PRIVATE_PKCS8}" -pkeyopt rsa_keygen_bits:2048
openssl pkey -in "${PRIVATE_PKCS8}" -pubout -out "${PUBLIC_PEM}"

# Valores de prueba SOLO para simular prod en tu Mac (no usar en internet)
cat > "${ENV_FILE}" <<'EOF'
# Simulación local del perfil prod — NO commitear (.jwk-local/ está ignorado)
export SPRING_PROFILES_ACTIVE=prod

export OAUTH_ISSUER=http://127.0.0.1:9190
export OAUTH_INTERNAL_TOKEN=local-prod-oauth-internal-token-change-me
export GATEWAY_INTERNAL_TOKEN=local-prod-oauth-internal-token-change-me

export OAUTH_ADMIN_USERNAME=admin
export OAUTH_ADMIN_PASSWORD=change-me-admin

export OAUTH_GATEWAY_REGISTRATION_ID=gateway-client
export OAUTH_GATEWAY_CLIENT_ID=gateway-app
export OAUTH_GATEWAY_CLIENT_SECRET=change-me-gateway-secret
export OAUTH_GATEWAY_REDIRECT_URI=http://127.0.0.1:8080/login/oauth2/code/client-app
export OAUTH_GATEWAY_REDIRECT_URI_AUTH=http://127.0.0.1:8080/authorized
export OAUTH_GATEWAY_POST_LOGOUT_REDIRECT_URI=http://127.0.0.1:8080/logout

export OAUTH_FLUTTER_REGISTRATION_ID=flutter-client
export OAUTH_FLUTTER_CLIENT_ID=flutter-app
export OAUTH_FLUTTER_REDIRECT_URI=com.ngonzano.app://oauth/callback

export OAUTH_JWK_KEY_ID=oauth-signing-key
EOF

# PEM multilínea en variables de entorno (Spring Boot las acepta)
{
  echo "export OAUTH_JWK_PRIVATE_KEY=\"$(cat "${PRIVATE_PKCS8}")\""
  echo "export OAUTH_JWK_PUBLIC_KEY=\"$(cat "${PUBLIC_PEM}")\""
} >> "${ENV_FILE}"

chmod 600 "${PRIVATE_PKCS8}" "${PUBLIC_PEM}" "${ENV_FILE}"

echo ""
echo "✓ Claves generadas:"
echo "  - ${PRIVATE_PKCS8}"
echo "  - ${PUBLIC_PEM}"
echo "  - ${ENV_FILE}"
echo ""
echo "Siguiente paso:"
echo "  cd ${ROOT_DIR}"
echo "  ./scripts/run-prod-local.sh"
echo ""
echo "Gateway local debe usar el mismo token interno:"
echo "  export GATEWAY_INTERNAL_TOKEN=local-prod-oauth-internal-token-change-me"
