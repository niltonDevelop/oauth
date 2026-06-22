# Distributed Tracing en oauth

> **Guía completa:** [docs/ZIPKIN.md](../../docs/ZIPKIN.md)

[Micrometer Tracing](https://docs.micrometer.io/tracing/reference/) + **Brave** + **Zipkin**.

| Componente | Span automático |
|------------|-----------------|
| HTTP entrante (`/api/auth/login`, `/oauth2/**`, etc.) | Sí |
| OpenFeign → `msvc-users` (`/internal/auth/users/{username}`) | Sí |
| Logs | `[oauth,traceId,spanId]` |

## Flujo típico

```
Cliente → gateway → oauth → msvc-users (internal auth)
          └────────────── mismo traceId ──────────────┘
```

## Zipkin (local)

Desde la raíz `SpringCloud/`:

```bash
docker compose up -d
```

## Referencias

- [Spring Boot — Tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
