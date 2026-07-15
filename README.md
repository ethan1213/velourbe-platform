# VeloUrbe — Plataforma de Arriendo de Patinetas Eléctricas

Plataforma de microservicios construida con **Spring Boot 3.5**, **Java 21** y **Docker** que gestiona el arriendo de patinetas eléctricas en la ciudad. Implementa autenticación JWT, HATEOAS, logging estructurado, API Gateway y un BFF (Backend for Frontend).

---

## Arquitectura

El flujo es en capas: el **API Gateway** va primero como único punto de entrada, seguido del **BFF**, y el BFF apunta a **todos** los microservicios. El Gateway no lleva código: es solo configuración de enrutamiento.

```
                  Cliente (Postman / App)
                          │
                   Puerto 8080
                          │
              ┌───────────▼───────────┐
              │      API Gateway      │  Spring Cloud Gateway
              │  (solo configuración) │  CORS + enrutamiento /api/** → BFF
              └───────────┬───────────┘
                          │
                   Puerto 8083
                          │
              ┌───────────▼───────────┐
              │          BFF          │  Agrega vistas (/api/bff/**) y
              │  (Backend for Front)  │  proxea /api/** al microservicio dueño
              └───────────┬───────────┘
                          │
   ┌──────┬──────┬──────┬─┴────┬──────┬──────┬──────┬──────┬──────┐
   │      │      │      │      │      │      │      │      │      │
 8081   8082   8084   8085   8086   8087   8088   8089   8090   8091
 user  rental payment notif analyt logist maint support station review
   │      │      │      │      │      │      │      │      │      │
   └──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┘
                          │
              PostgreSQL (una BD por servicio)
```

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| **api-gateway** | 8080 | Punto de entrada único, CORS y enrutamiento hacia el BFF (sin código) |
| **bff-service** | 8083 | Vistas agregadas para el frontend + proxy hacia los 10 microservicios |
| **user-auth-service** | 8081 | Registro, login, JWT, gestión de usuarios |
| **scooter-rental-service** | 8082 | Inventario de patinetas y arriendos |
| **payment-service** | 8084 | Pagos de arriendos (procesar, completar, cancelar, reembolsar) |
| **notification-service** | 8085 | Notificaciones EMAIL/SMS e historial por usuario |
| **analytics-service** | 8086 | Eventos de arriendo y estadísticas de usuario/sistema |
| **logistics-service** | 8087 | Ubicación GPS de patinetas y búsqueda por área |
| **maintenance-service** | 8088 | Issues de mantención de patinetas (reporte y ciclo de vida) |
| **support-service** | 8089 | Tickets de soporte al cliente |
| **station-service** | 8090 | Estaciones de carga/estacionamiento (capacidad, dock/undock, cercanía) |
| **review-service** | 8091 | Reseñas y calificaciones de arriendos por patineta |

El BFF no cuenta como microservicio de dominio: la plataforma tiene **10 microservicios funcionales** más el Gateway y el BFF.

---

## Inicio Rápido (Docker)

Requisito: [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y corriendo.

```bash
git clone https://github.com/ethan1213/velourbe-platform.git
cd velourbe-platform
docker compose up --build
```

Listo. La plataforma levanta en `http://localhost:8080`.

**Primera petición — login como admin:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@velourbe.cl",
  "password": "admin123"
}
```

---

## Ejecución sin Docker (local)

**Requisitos:** Java 21, Gradle 9.4.1, PostgreSQL 14+

```bash
# 1. Crear bases de datos
psql -U postgres -c "CREATE DATABASE db_scooter_users;"
psql -U postgres -c "CREATE DATABASE db_scooter_rentals;"
psql -U postgres -c "CREATE DATABASE db_scooter_payments;"
psql -U postgres -c "CREATE DATABASE db_scooter_notifications;"
psql -U postgres -c "CREATE DATABASE db_scooter_analytics;"
psql -U postgres -c "CREATE DATABASE db_scooter_logistics;"
psql -U postgres -c "CREATE DATABASE db_scooter_maintenance;"
psql -U postgres -c "CREATE DATABASE db_scooter_support;"
psql -U postgres -c "CREATE DATABASE db_scooter_stations;"
psql -U postgres -c "CREATE DATABASE db_scooter_reviews;"

# 2. Levantar cada servicio en una terminal distinta
#    (primero los microservicios, luego el BFF y al final el Gateway)
cd user-auth-service       && gradle bootRun  # 8081
cd scooter-rental-service  && gradle bootRun  # 8082
cd payment-service         && gradle bootRun  # 8084
cd notification-service    && gradle bootRun  # 8085
cd analytics-service       && gradle bootRun  # 8086
cd logistics-service       && gradle bootRun  # 8087
cd maintenance-service     && gradle bootRun  # 8088
cd support-service         && gradle bootRun  # 8089
cd station-service         && gradle bootRun  # 8090
cd review-service          && gradle bootRun  # 8091
cd bff-service             && gradle bootRun  # 8083
cd api-gateway             && gradle bootRun  # 8080
```

Los `application.yml` tienen valores por defecto apuntando a `localhost:5432` con usuario `postgres`/`postgres`. No se necesitan variables de entorno.

---

## Colección Postman

Importa los archivos de la carpeta `postman/`:

| Archivo | Contenido |
|---|---|
| `VeloUrbe.postman_collection.json` | Todos los endpoints organizados por servicio, incluyendo carpetas **BFF (8083)** y **Vía API Gateway (8080)** |
| `VeloUrbe.postman_environment.json` | Variables preconfiguradas para localhost (`auth_url`, `rental_url`, `bff_url`, `gateway_url`, tokens) |

En Postman: **Import** → selecciona ambos archivos → activa el entorno **"VeloUrbe Local"**.

**Flujo sugerido en Postman:**
1. `Auth / Login Admin` → copia el token a `{{token_admin}}`
2. `Scooters / Crear patineta` (con token admin)
3. `Auth / Registrar cliente` → copia el token a `{{token_client}}`
4. `Rentals / Iniciar arriendo`
5. `BFF / Dashboard` → ver datos agregados
6. `Rentals / Finalizar arriendo`

---

## Endpoints

El punto de entrada es siempre el Gateway en el puerto **8080**. El Gateway enruta todo `/api/**` al BFF, y el BFF reenvía cada petición al microservicio dueño de la ruta (o responde él mismo en `/api/bff/**`).

### Autenticación (`/api/auth`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Registra un usuario con rol `CLIENT` |
| `POST` | `/api/auth/login` | Público | Devuelve JWT + rol + HATEOAS links |

**Registro — body:**
```json
{ "email": "usuario@ejemplo.cl", "password": "miClave123", "fullName": "Juan Perez" }
```

**Login — respuesta:**
```json
{
  "token": "eyJhbGci...",
  "email": "usuario@ejemplo.cl",
  "role": "CLIENT",
  "_links": { "self": {...}, "register": {...} }
}
```

---

### Usuarios (`/api/users`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/users` | ADMIN | Lista todos los usuarios |
| `GET` | `/api/users/{id}` | ADMIN o el propio usuario | Detalle de un usuario |

---

### Patinetas (`/api/scooters`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/scooters` | ADMIN | Lista todas las patinetas |
| `GET` | `/api/scooters/available` | Autenticado | Patinetas disponibles |
| `GET` | `/api/scooters/{id}` | ADMIN | Detalle de una patineta |
| `POST` | `/api/scooters` | ADMIN | Crear patineta |
| `DELETE` | `/api/scooters/{id}` | ADMIN | Eliminar patineta |
| `GET` | `/api/scooters/low-battery?threshold=30` | ADMIN | Batería baja |
| `GET` | `/api/scooters/search?location=plaza` | ADMIN | Buscar por ubicación |

**Crear patineta — body:**
```json
{ "serialCode": "SC-001", "model": "Xiaomi Pro 4", "battery": 95, "location": "Plaza Italia" }
```

---

### Arriendos (`/api/rentals`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/rentals/start` | Autenticado | Inicia un arriendo |
| `PATCH` | `/api/rentals/{id}/end` | Autenticado | Finaliza el arriendo |
| `GET` | `/api/rentals/my` | Autenticado | Mis arriendos |
| `GET` | `/api/rentals/long?minMinutes=30` | ADMIN | Arriendos largos |

**Iniciar arriendo — body:**
```json
{ "scooterId": 1 }
```

**Finalizar — respuesta:**
```json
{
  "id": 1, "scooterId": 1, "scooterModel": "Xiaomi Pro 4",
  "startedAt": "2026-06-17T10:00:00", "endedAt": "2026-06-17T10:30:00",
  "status": "COMPLETED", "totalMinutes": 30,
  "_links": { "self": {...}, "my-rentals": {...} }
}
```

---

### Pagos (`/api/payments`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/payments` | Autenticado | Procesa un pago de arriendo |
| `GET` | `/api/payments/{id}` | Autenticado | Detalle de un pago |
| `GET` | `/api/payments/history` | Autenticado | Historial de pagos del usuario |
| `PATCH` | `/api/payments/{id}/complete` | Autenticado | Completa un pago pendiente |
| `PATCH` | `/api/payments/{id}/cancel` | Autenticado | Cancela un pago pendiente |
| `PATCH` | `/api/payments/{id}/refund` | Autenticado | Reembolsa un pago completado |

**Crear pago — body:**
```json
{ "rentalId": 1, "amount": 2500, "paymentMethod": "CARD", "notes": "Pago arriendo" }
```

---

### Analytics (`/api/analytics`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/analytics/rental?rentalId=1&amount=2500` | Autenticado | Registra un evento de arriendo |
| `GET` | `/api/analytics/user/{userId}` | Autenticado | Estadísticas de un usuario |
| `GET` | `/api/analytics/system` | Autenticado | Estadísticas globales del sistema |

---

### Mantención (`/api/maintenance/issues`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/maintenance/issues` | Autenticado | Reporta un issue de mantención |
| `GET` | `/api/maintenance/issues/{id}` | Autenticado | Detalle de un issue |
| `GET` | `/api/maintenance/issues` | ADMIN | Lista issues (filtro opcional `?status=`) |
| `GET` | `/api/maintenance/issues/scooter/{scooterId}` | Autenticado | Issues de una patineta |
| `PATCH` | `/api/maintenance/issues/{id}/review` | ADMIN | Marca en revisión |
| `PATCH` | `/api/maintenance/issues/{id}/start` | ADMIN | Inicia el trabajo |
| `PATCH` | `/api/maintenance/issues/{id}/resolve` | ADMIN | Resuelve el issue |
| `PATCH` | `/api/maintenance/issues/{id}/close` | ADMIN | Cierra el issue |

**Crear issue — body:**
```json
{ "scooterId": 1, "issueType": "BRAKE", "description": "Freno delantero flojo" }
```

---

### Soporte (`/api/support/tickets`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/support/tickets` | Autenticado | Crea un ticket de soporte |
| `GET` | `/api/support/tickets/my` | Autenticado | Mis tickets |
| `GET` | `/api/support/tickets/{id}` | Autenticado | Detalle de un ticket |
| `GET` | `/api/support/tickets` | ADMIN | Lista todos los tickets |
| `PATCH` | `/api/support/tickets/{id}/assign` | ADMIN | Asigna el ticket |
| `PATCH` | `/api/support/tickets/{id}/resolve` | ADMIN | Resuelve el ticket |
| `PATCH` | `/api/support/tickets/{id}/close` | Autenticado | Cierra el ticket |

**Crear ticket — body:**
```json
{
  "rentalId": 1,
  "subject": "Cobro incorrecto",
  "description": "Se cobró de más al finalizar",
  "priority": "MEDIUM",
  "category": "BILLING"
}
```

---

### Estaciones (`/api/stations`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/stations` | ADMIN | Crea una estación de carga/estacionamiento |
| `GET` | `/api/stations` | Autenticado | Lista todas las estaciones |
| `GET` | `/api/stations/{id}` | Autenticado | Detalle de una estación |
| `GET` | `/api/stations/nearby?latitude=&longitude=&radiusKm=2` | Autenticado | Estaciones activas cercanas (Haversine) |
| `PATCH` | `/api/stations/{id}/dock` | Autenticado | Estaciona una patineta (ocupación +1) |
| `PATCH` | `/api/stations/{id}/undock` | Autenticado | Retira una patineta (ocupación -1) |
| `PATCH` | `/api/stations/{id}/maintenance` | ADMIN | Pone la estación en mantención |
| `PATCH` | `/api/stations/{id}/activate` | ADMIN | Reactiva la estación |
| `DELETE` | `/api/stations/{id}` | ADMIN | Elimina la estación |

**Crear estación — body:**
```json
{ "name": "Estación Plaza Italia", "address": "Av. Providencia 1", "latitude": -33.4372, "longitude": -70.6344, "capacity": 10 }
```

---

### Reseñas (`/api/reviews`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/reviews` | Autenticado | Crea una reseña (una por arriendo) |
| `GET` | `/api/reviews/my` | Autenticado | Mis reseñas |
| `GET` | `/api/reviews/{id}` | Autenticado | Detalle de una reseña |
| `GET` | `/api/reviews/scooter/{scooterId}` | Autenticado | Reseñas de una patineta |
| `GET` | `/api/reviews/scooter/{scooterId}/rating` | Autenticado | Promedio y total de calificaciones |
| `GET` | `/api/reviews` | ADMIN | Lista todas las reseñas |
| `DELETE` | `/api/reviews/{id}` | Autor o ADMIN | Elimina la reseña |

**Crear reseña — body:**
```json
{ "rentalId": 1, "scooterId": 1, "rating": 5, "comment": "Excelente estado y batería" }
```

---

### Notificaciones y logística

El BFF reenvía `/api/notifications/**` → **notification-service** (8085) y `/api/logistics/**` → **logistics-service** (8087).

| Servicio | Dominio |
|---|---|
| **notification-service** | Envío EMAIL/SMS, historial por usuario y marcado como leído |
| **logistics-service** | Registro/consulta de ubicación GPS y búsqueda de patinetas por radio |

---

### BFF — Backend for Frontend (`/api/bff`)

El BFF cumple dos roles:

1. **Proxy**: toda petición `/api/**` que entra por el Gateway pasa por el BFF, que la reenvía al microservicio dueño de la ruta preservando método, query, headers y body.
2. **Agregación**: los endpoints `/api/bff/**` combinan datos de varios microservicios en una sola respuesta. Requieren token JWT.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/bff/dashboard` | Perfil del usuario + arriendos activos + recientes |
| `GET` | `/api/bff/scooters/available` | Scooters disponibles con todos sus datos |
| `GET` | `/api/bff/rental-summary` | Resumen estadístico de arriendos del usuario |
| `GET` | `/api/bff/maintenance/issues` | Issues de mantención (proxy a maintenance-service) |

**Dashboard — respuesta:**
```json
{
  "profile": { "email": "usuario@cl", "fullName": "Juan Perez", "role": "CLIENT" },
  "activeRentals": [ { "id": 2, "scooterModel": "Xiaomi Pro 4", "status": "ACTIVE" } ],
  "recentRentals": [ { "id": 1, "status": "COMPLETED", "totalMinutes": 30 } ]
}
```

**Rental Summary — respuesta:**
```json
{ "userId": 3, "totalRentals": 5, "completedRentals": 4, "activeRentals": 1, "totalMinutes": 120 }
```

---

## Roles y Permisos

| Acción | Público | CLIENT | ADMIN |
|---|:---:|:---:|:---:|
| Registrarse / Login | ✅ | — | — |
| Ver patinetas disponibles | ❌ | ✅ | ✅ |
| Iniciar / finalizar arriendo | ❌ | ✅ | ✅ |
| Ver mis arriendos | ❌ | ✅ | ✅ |
| Usar BFF (dashboard, resumen) | ❌ | ✅ | ✅ |
| Pagos / analytics / tickets propios | ❌ | ✅ | ✅ |
| Reportar issue de mantención | ❌ | ✅ | ✅ |
| Gestionar patinetas (CRUD) | ❌ | ❌ | ✅ |
| Ver todos los usuarios | ❌ | ❌ | ✅ |
| Ver arriendos largos | ❌ | ❌ | ✅ |
| Gestionar mantención / soporte (admin) | ❌ | ❌ | ✅ |
| Swagger UI | ✅ | ✅ | ✅ |

---

## Swagger / OpenAPI

Cada servicio tiene su propia documentación interactiva:

- **user-auth-service:** http://localhost:8081/swagger-ui.html
- **scooter-rental-service:** http://localhost:8082/swagger-ui.html
- **bff-service:** http://localhost:8083/swagger-ui.html
- **payment-service:** http://localhost:8084/swagger-ui.html
- **notification-service:** http://localhost:8085/swagger-ui.html
- **analytics-service:** http://localhost:8086/swagger-ui.html
- **logistics-service:** http://localhost:8087/swagger-ui.html
- **maintenance-service:** http://localhost:8088/swagger-ui.html
- **support-service:** http://localhost:8089/swagger-ui.html
- **station-service:** http://localhost:8090/swagger-ui.html
- **review-service:** http://localhost:8091/swagger-ui.html

Para autenticarse en Swagger: login → copia el `token` → botón **Authorize** → escribe `Bearer <token>`.

---

## Base de Datos y Migraciones

**Flyway** crea las tablas automáticamente al arrancar los servicios que lo usan. Algunos servicios (`analytics`, `notification`, `logistics`) usan `ddl-auto: update` de JPA.

| Servicio | Base de datos | Esquema / migraciones |
|---|---|---|
| user-auth-service | `db_scooter_users` | Flyway V1 users, V2 seed admin, V3 fix password |
| scooter-rental-service | `db_scooter_rentals` | Flyway V1 scooters, V2 rentals |
| payment-service | `db_scooter_payments` | Flyway V1 payments |
| notification-service | `db_scooter_notifications` | JPA → `notifications` |
| analytics-service | `db_scooter_analytics` | JPA → `analytics_events` |
| logistics-service | `db_scooter_logistics` | JPA → `scooter_locations` |
| maintenance-service | `db_scooter_maintenance` | Flyway V1 maintenance_issues |
| support-service | `db_scooter_support` | Flyway V1 support_tickets |
| station-service | `db_scooter_stations` | Flyway V1 stations |
| review-service | `db_scooter_reviews` | Flyway V1 reviews |

**Esquema `users`:**
```
id • email (UNIQUE) • password_hash (BCrypt) • full_name • role • created_at • active
```

**Esquema `scooters`:**
```
id • serial_code (UNIQUE) • model • battery (0-100) • location • status • created_at
```

**Esquema `rentals`:**
```
id • user_id • scooter_id (FK) • started_at • ended_at • status • total_minutes
```

**Esquema `payments`:**
```
id • rental_id • user_id • amount • currency • status • transaction_id • payment_method • notes • created_at • updated_at
```

**Esquema `notifications`:**
```
id • user_id • type (EMAIL/SMS) • message • sent • created_at
```

**Esquema `analytics_events`:**
```
id • rental_id • user_id • amount • event_type • description • created_at
```

**Esquema `scooter_locations`:**
```
id • scooter_id (UNIQUE) • latitude • longitude • created_at • updated_at
```

**Esquema `maintenance_issues`:**
```
id • scooter_id • issue_type • description • status • resolution_notes • created_at • updated_at • resolved_at
```

**Esquema `support_tickets`:**
```
id • user_id • rental_id • subject • description • status • priority • category • assigned_to • resolution_notes • created_at • updated_at
```

**Esquema `stations`:**
```
id • name • address • latitude • longitude • capacity • occupied • status • created_at • updated_at
```

**Esquema `reviews`:**
```
id • user_id • rental_id (UNIQUE por usuario) • scooter_id • rating (1-5) • comment • created_at • updated_at
```

> Nunca modifiques archivos de migración ya ejecutados. Para cambios, crea `V3__descripcion.sql`, etc.

---

## JWT — Comunicación entre Servicios

```
Cliente → [POST /api/auth/login] → user-auth-service
                                        │ genera JWT firmado (HS256)
                                        ▼
                              { token, role, email }

Cliente → [cualquier endpoint protegido]
          Authorization: Bearer <token>
                │
                ▼
        JwtAuthFilter (en cada servicio)
        valida firma con el mismo secret
        extrae: sub (email), role, userId
        → sin consultar la BD de usuarios
```

El JWT contiene:
```json
{ "sub": "usuario@cl", "role": "CLIENT", "userId": 3, "iat": ..., "exp": ... }
```

**El secret debe ser idéntico en todos los servicios.** Por defecto usa el valor del `.env.example`. En producción, usa `openssl rand -base64 32` y configúralo como variable de entorno `JWT_SECRET`.

---

## Variables de Entorno

Copia `.env.example` a `.env` y ajusta los valores:

```env
JWT_SECRET=velourbe-secret-de-256-bits-para-jwt-firma-hmac-sha256
DB_USERNAME=velourbe
DB_PASSWORD=velourbe123
```

Con Docker Compose las variables se cargan automáticamente. Sin Docker, los `application.yml` tienen valores por defecto para desarrollo local.

---

## Datos de Prueba (DataFaker)

Para poblar las bases de datos con información realista durante el desarrollo, los servicios `user-auth-service` y `scooter-rental-service` incluyen un `DataLoader` (`net.datafaker`) que se ejecuta **solo bajo el perfil `dev`** y únicamente si las tablas están vacías (no afecta producción ni Docker).

```bash
# Levantar un servicio con datos falsos
cd scooter-rental-service && SPRING_PROFILES_ACTIVE=dev gradle bootRun
cd user-auth-service      && SPRING_PROFILES_ACTIVE=dev gradle bootRun
```

| Servicio | Genera |
|---|---|
| user-auth-service | 10 usuarios `CLIENT` (password `cliente123`) |
| scooter-rental-service | 15 patinetas + 10 arriendos de muestra |

---

## Tests Unitarios

Los tests están en `src/test/` de cada servicio. Hay tres niveles:

- **Servicio** (`@ExtendWith(MockitoExtension.class)`): lógica de negocio con dependencias simuladas, sin contexto Spring.
- **Controller** (`MockMvc` standalone + Mockito): mapeo HTTP, códigos de estado y serialización.
- **Contexto** (`@SpringBootTest @ActiveProfiles("test")`): verifica que el contexto levanta usando una base de datos **H2 en memoria** (`application-test.yml`), por lo que la suite corre sin PostgreSQL.

```bash
# Ejecutar toda la suite (no requiere PostgreSQL)
cd user-auth-service      && gradle test
cd scooter-rental-service && gradle test
cd payment-service        && gradle test
```

El perfil `test` (`src/test/resources/application-test.yml`) usa H2, desactiva Flyway y aplica `ddl-auto=create-drop`, separando la base de pruebas de la de desarrollo.

**Ejecutar con Docker (no requiere instalación local de Gradle):**
```bash
# user-auth-service
docker run --rm --network velourbe-platform_default \
  -v "$(pwd)/user-auth-service:/app" -w /app \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://velourbe-postgres:5432/db_scooter_users \
  -e SPRING_DATASOURCE_USERNAME=velourbe \
  -e SPRING_DATASOURCE_PASSWORD=velourbe123 \
  -e JWT_SECRET=velourbe-secret-de-256-bits-para-jwt-firma-hmac-sha256 \
  gradle:9.4.1-jdk21-alpine gradle test --no-daemon

# scooter-rental-service (mismo patrón, cambiar db_scooter_rentals)
```

**Cobertura de tests:**

| Servicio | Clase | Tests |
|---|---|---|
| user-auth-service | `AuthServiceTest` | registro exitoso, email duplicado, login correcto, email inexistente, password incorrecta |
| user-auth-service | `UserServiceTest` | findAll, findById existente, findById no encontrado, count |
| scooter-rental-service | `ScooterServiceTest` | crear, listar, findById, disponibles, batería baja, buscar ubicación, eliminar |
| scooter-rental-service | `RentalServiceTest` | iniciar, finalizar, mis arriendos, arriendos largos, scooter no disponible |
| user-auth-service | `AuthControllerTest` | register 201, login 200, credenciales inválidas 401 |
| user-auth-service | `UserControllerTest` | listar usuarios, usuarios activos por rol |
| scooter-rental-service | `ScooterControllerTest` | listar, crear 201, detalle, no encontrado 404, eliminar 204 |
| scooter-rental-service | `RentalControllerTest` | iniciar 201, finalizar 200, no encontrado 404, mis arriendos |
| payment-service | `PaymentControllerTest` | procesar, completar, cancelar, reembolso |
| station-service | `StationServiceTest` | crear, detalle, dock/undock, estación llena/vacía, FULL automático, cercanía |
| review-service | `ReviewServiceTest` | crear, reseña duplicada, promedio por scooter, eliminar (autor/admin) |

---

## Logging Estructurado

Todos los servicios emiten logs en formato JSON (logstash-logback-encoder):

```json
{
  "@timestamp": "2026-06-17T00:07:38Z",
  "level": "INFO",
  "message": "Arriendo iniciado id=1 scooterId=1 userId=3",
  "service": "scooter-rental-service",
  "logger_name": "cl.velourbe.rental.service.RentalService"
}
```

Ver logs en tiempo real:
```bash
docker logs -f velourbe-gateway
docker logs -f velourbe-user-auth
docker logs -f velourbe-rental
docker logs -f velourbe-bff
docker logs -f velourbe-payment
docker logs -f velourbe-notification
docker logs -f velourbe-analytics
docker logs -f velourbe-logistics
docker logs -f velourbe-maintenance
docker logs -f velourbe-support
docker logs -f velourbe-station
docker logs -f velourbe-review
```

---

## Credenciales por Defecto

| Campo | Valor |
|---|---|
| Email | `admin@velourbe.cl` |
| Contraseña | `admin123` |
| Rol | `ADMIN` |

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.0 | Framework base |
| Spring Cloud Gateway | 2025.0.0 | API Gateway reactivo |
| Spring Security | (Boot) | JWT filter, roles, @PreAuthorize |
| Spring Data JPA | (Boot) | ORM con Hibernate |
| Spring HATEOAS | (Boot) | Links en respuestas REST |
| Spring WebFlux | (Boot) | WebClient en BFF |
| Flyway | (Boot) | Migraciones SQL versionadas |
| DataFaker | 2.4.2 | Generación de datos de prueba (perfil dev) |
| H2 | (test) | Base de datos en memoria para los tests |
| PostgreSQL | 16 | Base de datos relacional |
| JJWT | 0.12.6 | Generación y validación JWT (HS256) |
| Lombok | 1.18.34 | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.7.0 | Swagger UI automático |
| logstash-logback-encoder | 8.0 | Logging estructurado JSON |
| Mockito / JUnit 5 | (Boot) | Tests unitarios sin contexto Spring |
| Gradle | 9.4.1 | Build system (Kotlin DSL) |
| Docker / Compose | 27+ | Contenedores y orquestación local |
