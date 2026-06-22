# oauth

Servidor de **autorización OAuth 2.1 / OpenID Connect** (Spring Authorization Server). Autentica usuarios consultando **msvc-users**, emite JWT y expone login nativo para clientes móviles (Flutter).

## Stack

- Java 21 · Spring Boot 4.1.0 · Spring Cloud 2025.1.2
- Puerto: **9190**
- Issuer: `http://127.0.0.1:9190`
- **Distributed tracing:** [Micrometer Tracing](https://docs.micrometer.io/tracing/reference/) + Zipkin. Ver [docs/TRACING.md](docs/TRACING.md)

## Endpoints

### API propia

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/` | Info del usuario autenticado (post login web) |
| POST | `/api/auth/login` | Login nativo JSON → JWT (Flutter/mobile) |
| GET | `/login` | Formulario de login web |
| GET | `/error` | Página de error |

**Login nativo** — body: `{ "username", "password" }` → respuesta: `{ accessToken, tokenType, expiresIn, scope }`

### OAuth2 / OIDC (estándar del Authorization Server)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/oauth2/authorize` | Flujo authorization code |
| POST | `/oauth2/token` | Intercambio de tokens |
| GET | `/oauth2/jwks` | Claves públicas JWK |
| POST | `/oauth2/introspect` | Introspección de token |
| POST | `/oauth2/revoke` | Revocación de token |
| GET | `/.well-known/openid-configuration` | Metadata OIDC |
| GET | `/userinfo` | Información del usuario OIDC |

### Clientes registrados

| Cliente | Uso |
|---------|-----|
| `gateway-app` | Login web del API Gateway (authorization code) |
| `flutter-app` | Cliente móvil con PKCE (public client) |

## Importancia en el ecosistema

Es el **servidor de identidad** del cluster. Centraliza autenticación y emisión de tokens JWT que el gateway valida en cada petición protegida.

**Dependencias:** Eureka, **msvc-users** (Feign con token interno).

**Integrado con:** **msvc-gateway-server** (OAuth2 client + JWT resource server), **flutter_spring_boot** (login directo en `:9190`).

**Orden de arranque recomendado:** 4.º, después de Eureka y msvc-users.

## Tracing (Zipkin)

```bash
cd .. && docker compose up -d   # raíz SpringCloud → http://localhost:9411
```

Detalle: [docs/TRACING.md](docs/TRACING.md).

## Perfiles y scripts

- **dev** (default): desarrollo local con claves JWT efímeras — `application-dev.properties`.
- **prod**: secretos y claves PEM obligatorias — `application-prod.properties`.

Para simular **prod en local** (claves estables, variables de entorno): ver [scripts/README.md](scripts/README.md).

Para desplegar a **producción real** (checklist completo del ecosistema): ver [PRODUCTION-CHECKLIST.md](../PRODUCTION-CHECKLIST.md).
