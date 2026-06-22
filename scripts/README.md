# Scripts OAuth — claves JWK y perfil `prod` local

Utilidades para **simular producción en tu Mac** sin desplegar a un servidor real. Generan claves RSA en formato PEM y arrancan el servicio `oauth` con `SPRING_PROFILES_ACTIVE=prod`.

## ¿Cuándo usar cada script?

| Script | Úsalo cuando… | No lo uses cuando… |
|--------|----------------|---------------------|
| **`generate-jwk.sh`** | Primera vez que pruebas prod local; rotaste claves; borraste `.jwk-local/` | Solo desarrollas con perfil `dev` (claves efímeras automáticas) |
| **`run-prod-local.sh`** | Quieres validar config prod, JWT estables tras reinicios, o reproducir bugs de prod | Desarrollo diario normal → arranca oauth con perfil `dev` desde el IDE |
| **`verify-jwk.sh`** | Tras arrancar oauth prod local: comprobar OIDC y `/oauth2/jwks` | El servicio oauth no está levantado |

### Perfil `dev` vs simulación `prod` local

| | **dev** (default) | **prod local** (estos scripts) |
|---|---|---|
| Activación | Automático (`spring.profiles.default=dev`) | `./scripts/run-prod-local.sh` |
| Claves JWT | Se generan solas en cada arranque | **Fijas** en `.jwk-local/*.pem` |
| Reiniciar oauth | Tokens viejos pueden invalidarse | Mismos tokens siguen válidos (misma clave) |
| Secretos | Defaults en `application-dev.properties` | Variables en `.jwk-local/oauth-prod-local.env` |
| Uso típico | Programar Flutter / gateway en local | Probar hardening antes de desplegar |

### Producción real (servidor / K8s / cloud)

Checklist completo de despliegue (todos los servicios + Flutter): **[PRODUCTION-CHECKLIST.md](../../PRODUCTION-CHECKLIST.md)**

En un entorno real **no copies** `.jwk-local/` al servidor. Allí:

- Las claves PEM van en un **secret manager** (Vault, AWS Secrets Manager, K8s Secrets).
- Las variables las define tu pipeline o la plataforma (`OAUTH_JWK_*`, `OAUTH_ISSUER` con HTTPS, etc.).
- Puedes generar el par con el mismo `openssl` que usa `generate-jwk.sh`, pero **guardarlo en la infra**, no en el repo.

---

## Requisitos

- **OpenSSL** (incluido en macOS).
- **Java 21** y **Maven** (wrapper `./mvnw` del proyecto).
- **Python 3** (solo para formatear JSON en `verify-jwk.sh`).
- **Eureka**, **msvc-users** y **gateway** levantados si pruebas login end-to-end.

---

## Flujo rápido

```bash
cd /Users/ngonzano/personal/SpringCloud/oauth

# 1) Una sola vez (o al rotar claves)
./scripts/generate-jwk.sh

# 2) Terminal 1 — oauth en modo prod local
./scripts/run-prod-local.sh

# 3) Terminal 2 — alinear token interno del gateway
export GATEWAY_INTERNAL_TOKEN=local-prod-oauth-internal-token-change-me
# (arranca msvc-gateway-server como siempre)

# 4) Comprobar JWKS
./scripts/verify-jwk.sh
```

---

## Scripts en detalle

### `generate-jwk.sh`

Genera:

```
.jwk-local/
├── private-pkcs8.pem      # Clave privada (PKCS#8) — firma JWT
├── public.pem             # Clave pública (SPKI) — validación
└── oauth-prod-local.env   # Variables export para perfil prod
```

**Formato PEM:** el servicio espera `BEGIN PRIVATE KEY` (PKCS#8), no el formato PKCS#1 antiguo (`BEGIN RSA PRIVATE KEY`). El script ya genera el formato correcto.

**Directorio de salida opcional:**

```bash
./scripts/generate-jwk.sh /ruta/custom
```

**Seguridad:**

- `.jwk-local/` está en `.gitignore` — **nunca hagas commit** de estas claves.
- Los valores del `.env` son para **prueba local**; cámbialos antes de exponer nada a red pública.

---

### `run-prod-local.sh`

1. Carga `.jwk-local/oauth-prod-local.env`.
2. Ejecuta `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`.

Si falta el `.env`, indica que ejecutes `generate-jwk.sh` primero.

**Importante:** el gateway debe usar el **mismo** `GATEWAY_INTERNAL_TOKEN` que `OAUTH_INTERNAL_TOKEN` en el `.env`, o `/api/auth/login` responderá **403 Acceso denegado**.

---

### `verify-jwk.sh`

Consulta:

- `GET /.well-known/openid-configuration`
- `GET /oauth2/jwks`

Issuer por defecto: `http://127.0.0.1:9190`

```bash
./scripts/verify-jwk.sh
./scripts/verify-jwk.sh https://auth.tudominio.com
```

Comprueba que aparezca `"kid": "oauth-signing-key"` (o el valor de `OAUTH_JWK_KEY_ID`).

---

## Probar login desde Flutter / curl

Con oauth prod local + gateway alineado:

```bash
# Login (siempre vía gateway :8080)
curl -X POST http://127.0.0.1:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"tu_usuario","password":"tu_password"}'

# API con token
curl http://127.0.0.1:8080/api/users/v1 \
  -H "Authorization: Bearer <accessToken>"
```

Emulador Android: usa `10.0.2.2` en lugar de `127.0.0.1`.

---

## Rotación de claves

1. Detén oauth.
2. Borra o renombra `.jwk-local/`.
3. Vuelve a ejecutar `./scripts/generate-jwk.sh`.
4. Reinicia oauth con `./scripts/run-prod-local.sh`.

Los JWT firmados con la clave anterior dejarán de ser válidos (comportamiento esperado en rotación).

---

## Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---------|----------------|-----------|
| `No existe oauth-prod-local.env` | No corriste `generate-jwk.sh` | Ejecutarlo primero |
| `403 Acceso denegado` en `/api/auth/login` | Token interno gateway ≠ oauth | Exportar `GATEWAY_INTERNAL_TOKEN` del `.env` |
| `401` con Bearer en gateway | Token expirado o oauth reiniciado en **dev** | Login de nuevo; en prod local las claves son estables |
| OAuth no arranca en prod | Faltan variables obligatorias | Revisar `application-prod.properties` y el `.env` |
| `verify-jwk.sh` falla | oauth no está en `:9190` | Levantar `./scripts/run-prod-local.sh` |

---

## Referencia de variables (prod)

Definidas en `application-prod.properties` y rellenadas por `oauth-prod-local.env`:

| Variable | Descripción |
|----------|-------------|
| `OAUTH_ISSUER` | URL pública del authorization server |
| `OAUTH_INTERNAL_TOKEN` | Token oauth → msvc-users |
| `GATEWAY_INTERNAL_TOKEN` | Token gateway → oauth (`/api/auth/**`) |
| `OAUTH_JWK_PRIVATE_KEY` | PEM clave privada |
| `OAUTH_JWK_PUBLIC_KEY` | PEM clave pública |
| `OAUTH_JWK_KEY_ID` | ID en JWKS (default: `oauth-signing-key`) |
| `OAUTH_GATEWAY_CLIENT_*` | Cliente OAuth del gateway |
| `OAUTH_FLUTTER_CLIENT_*` | Cliente OAuth móvil |

Lista completa en comentarios de `src/main/resources/application-prod.properties`.
