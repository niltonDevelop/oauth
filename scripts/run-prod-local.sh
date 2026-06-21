#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.jwk-local/oauth-prod-local.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "No existe ${ENV_FILE}"
  echo "Ejecuta primero: ./scripts/generate-jwk.sh"
  exit 1
fi

# shellcheck disable=SC1090
source "${ENV_FILE}"

echo "==> Arrancando oauth con perfil prod (simulación local)"
echo "    Issuer: ${OAUTH_ISSUER}"
echo "    JWK key-id: ${OAUTH_JWK_KEY_ID}"
echo ""

cd "${ROOT_DIR}"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
