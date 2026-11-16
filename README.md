# WashTrack Backend

RESTful API de WashTrack (TechNova). Java 27, Spring Boot 4.1.2, Maven.

## Ejecutar

```bash
./mvnw spring-boot:run      # API en http://localhost:8080
./mvnw test                 # tests
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html` (siempre habilitado)
- Health check: `http://localhost:8080/actuator/health`

> Persistencia: Order Management ya usa **JPA con PostgreSQL** (Supabase). Para arrancar la API se necesitan las variables
> de entorno `DATABASE_USER` y `DATABASE_PASSWORD` (y en produccion `DATABASE_URL`, `DATABASE_PORT` y `DATABASE_NAME`).
> Los demas contextos siguen con repositorios **en memoria** (los datos se pierden al reiniciar) hasta que se migren a adaptadores JPA.

## Estructura

Arquitectura por capas y contextos (DDD): `com.technova.washtrack.<contexto>` con
`domain` (modelo, eventos, puertos), `application` (servicios, politicas),
`infrastructure` (persistencia, pasarela) e `interfaces.rest` (controladores y recursos).

## Order Management (`/api/v1`)

| Metodo | Ruta | Descripcion |
| :--- | :--- | :--- |
| POST | `/orders` | Crea un pedido en DRAFT (`customerId`, `laundryId`, `promisedDate`, `currency`, `specialInstructions`, `items`). Genera un numero unico `ORD-0001` y calcula el total. `promisedDate` debe ser futura |
| POST | `/orders/{orderId}/items` | Agrega una prenda (`serviceId`, `garmentType`, `quantity`, `weightKg`, `unitPrice`, `currency`, `notes`) y recalcula el total. Solo en DRAFT: si no, 409 |
| POST | `/orders/{orderId}/confirm` | DRAFT -> CONFIRMED. Si no esta en DRAFT: 409; sin prendas: 422 |
| POST | `/orders/{orderId}/cancel` | Cancela un pedido en DRAFT o CONFIRMED (`reason` obligatorio). Si ya esta en proceso o listo para entrega: 409 |
| GET | `/orders/{orderId}` | Detalle del pedido con sus prendas, total, estado y fecha prometida |
| GET | `/orders?customerId=` o `/orders?laundryId=` | Pedidos de un cliente o de una lavanderia (exactamente un filtro; si falta o se envian ambos: 400) |

Calculo: el subtotal de cada prenda es `unitPrice x quantity` (redondeado a 2 decimales) y el total del pedido es la suma
de los subtotales; se recalcula automaticamente al agregar una prenda. `weightKg` es informativo. Todas las prendas usan
la moneda del pedido. Tipos de prenda: SHIRT, PANTS, SUIT, BEDDING, DELICATE, BULK_LAUNDRY.
Estados: DRAFT, CONFIRMED, IN_PROCESS, READY_FOR_DELIVERY, COMPLETED, CANCELLED.

Errores en formato `application/problem+json` con mensajes traducidos segun el header `Accept-Language` (`en` o `es`):
400 (validacion), 404 (pedido no encontrado), 409 (estado no permite la operacion) y 422 (pedido sin prendas).

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
- Commits en Conventional Commits.