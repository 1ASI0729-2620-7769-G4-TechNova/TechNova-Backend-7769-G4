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

## Delivery (`/api/v1`)

| Metodo | Ruta | Descripcion |
| :--- | :--- | :--- |
| POST | `/deliveries` | Programa un recojo o entrega (`orderId`, `customerName`, `type` PICKUP/DELIVERY, `address`, `scheduledAt`). Fecha pasada: 422; franja de 1 hora llena: 409 (capacidad `washtrack.delivery.slot-capacity`, por defecto 3) |
| GET | `/deliveries`, `/deliveries/{id}` | Lista por hora programada. Filtros: `orderId`, `status`, `type`, `from`, `to` |
| PATCH | `/deliveries/{id}` | Asigna un repartidor disponible (`driverId`); si ya tiene otra entrega esa hora: 409 |
| POST | `/deliveries/{id}/start` | SCHEDULED -> ON_THE_WAY (requiere repartidor) |
| POST | `/deliveries/{id}/complete` | ON_THE_WAY -> COMPLETED y publica `DeliveryCompleted` |
| POST | `/deliveries/{id}/fail` | Falla una entrega activa (`reason` obligatorio) |
| POST, GET | `/drivers`, `/drivers/{id}` | Registra y consulta repartidores (`availableOnly=true` para filtrar) |
| PATCH | `/drivers/{id}` | Marca disponible o no disponible (`available`) |

## Tracking (`/api/v1`)

Las rutas conservan los nombres que ya usa el frontend (`orderTrackings`, `stageChanges`).

| Metodo | Ruta | Descripcion |
| :--- | :--- | :--- |
| POST | `/orderTrackings` | Inicia el seguimiento de un pedido en RECEIVED (`orderId`, `orderNumber`, `customerId`) |
| POST | `/orderTrackings/{id}/advance` | Siguiente etapa: RECEIVED -> IN_PROCESS -> READY_FOR_DELIVERY -> DELIVERED (sin saltos) |
| GET | `/orderTrackings`, `/orderTrackings/{id}` | Consulta; filtro `customerId` |
| GET | `/stageChanges?orderId=` | Historial de etapas, de la mas antigua a la mas reciente |
| GET | `/notifications?recipientId=` | Notificaciones, mas reciente primero (`unread=true` para no leidas) |
| POST | `/notifications` | Crea una notificacion manual |
| PATCH | `/notifications/{id}/read` | Marca una como leida |
| POST | `/notifications/read-all?recipientId=` | Marca todas como leidas |

Politicas por eventos: cada cambio de etapa genera una notificacion para el cliente, y al completar una entrega a
domicilio (`DeliveryCompleted`) un pedido READY_FOR_DELIVERY pasa a DELIVERED. Delivery y Tracking solo se comunican por eventos.

## Flujo de trabajo

- Gitflow: ramas `feature/<nombre>` desde `develop`; `main` solo recibe el merge final.
- Commits en Conventional Commits (`feat(billing): ...`), PR de maximo 400 lineas.
