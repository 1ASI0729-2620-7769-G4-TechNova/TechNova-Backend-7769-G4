# WashTrack Backend

RESTful API de WashTrack (TechNova). Java 27, Spring Boot 4.1.2, Maven.

## Ejecutar

```bash
./mvnw spring-boot:run      # API en http://localhost:8080
./mvnw test                 # tests
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html` (siempre habilitado)
- Health check: `http://localhost:8080/actuator/health`

> Persistencia: por ahora los repositorios son **en memoria** (los datos se pierden al reiniciar).
> Cuando se conecte PostgreSQL (Supabase) se reemplazan por adaptadores JPA sin tocar dominio ni aplicacion.

## Estructura

Arquitectura por capas y contextos (DDD): `com.technova.washtrack.<contexto>` con
`domain` (modelo, eventos, puertos), `application` (servicios, politicas),
`infrastructure` (persistencia, pasarela) e `interfaces.rest` (controladores y recursos).

## Billing & Subscriptions (`/api/v1`)

| Metodo | Ruta | Descripcion |
| :--- | :--- | :--- |
| GET | `/plans`, `/plans/{id}` | Planes: FREE (S/ 0), BASIC (S/ 49), ADVANCED (S/ 99) |
| POST | `/payments` | Registra un pago (`orderId`, `customerName`, `amount`, `paymentMethod`). Digitales (CARD, YAPE, PLIN) se verifican con la pasarela; CASH queda PENDING |
| GET | `/payments/{id}` | Detalle de un pago |
| GET | `/payments` | Historial, mas reciente primero. Filtros: `orderId`, `status`, `from`, `to` (fechas ISO) |
| POST | `/payments/{id}/confirm` | Confirma un pago PENDING |
| POST | `/payments/{id}/reject` | Rechaza un pago PENDING (`reason` obligatorio) |
| POST | `/subscriptions` | Suscribe una lavanderia (`laundryId`, `planId`); solo una activa por lavanderia |
| GET | `/subscriptions?laundryId=` | Suscripciones de la lavanderia, mas reciente primero (marca EXPIRED al vencer) |
| GET | `/subscriptions/{id}` | Detalle de una suscripcion |
| POST | `/subscriptions/{id}/change-plan` | Cambia de plan (`planId`), conserva el periodo |
| POST | `/subscriptions/{id}/renew` | Renueva un periodo; si ya vencio, reinicia desde hoy |
| POST | `/subscriptions/{id}/cancel` | Cancela una suscripcion activa |
| GET | `/subscriptions/features?laundryId=` | Funcionalidades habilitadas por el plan activo |

Errores en formato `application/problem+json`: 400 (validacion, con `errors` por campo), 404 y 422 (regla de negocio).
El periodo de los planes de pago es de 1 mes; el plan FREE dura 12 meses.

## Flujo de trabajo

- Gitflow: ramas `feature/<nombre>` desde `develop`; `main` solo recibe el merge final.
- Commits en Conventional Commits (`feat(billing): ...`), PR de maximo 400 lineas.
