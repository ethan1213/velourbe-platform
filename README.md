# VeloUrbe — Plataforma de Arriendo de Patinetas Eléctricas

Plataforma de microservicios construida con **Spring Boot 3.5**, **Java 21** y **Docker** que gestiona el arriendo de patinetas eléctricas en la ciudad. Implementa autenticación JWT, HATEOAS, logging estructurado, API Gateway y un BFF (Backend for Frontend).

---

## Arquitectura

```
                    Cliente (Postman / App)
                            │
                     Puerto 8080
                            │
                ┌───────────▼───────────┐
                │      API Gateway      │  Spring Cloud Gateway
                │   Enrutamiento CORS   │  Logging de requests
                └──────────┬────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   Puerto 8081      Puerto 8082      Puerto 8083
          │                │                │
┌─────────▼──────┐ ┌───────▼──────┐ ┌──────▼──────────┐
│ user-auth-     │ │ scooter-     │ │  bff-service    │
│ service        │ │ rental-      │ │                 │
│                │ │ service      │ │  Dashboard      │
│ Registro/Login │ │              │ │  Resumen        │
│ JWT / HATEOAS  │ │ Patinetas    │ │  Scooters       │
│ Flyway         │ │ Arriendos    │ │  (agrega datos  │
│ Tests          │ │ HATEOAS      │ │   de los otros  │
└────────┬───────┘ │ Flyway/Tests │ │   servicios)    │
         │         └───────┬──────┘ └──────────────────┘
         │                 │               │  │
  ┌──────▼──────┐   ┌──────▼──────┐       │  │
  │db_scooter_  │   │db_scooter_  │◄──────┘  │
  │users        │   │rentals      │◄──────────┘
  │(PostgreSQL) │   │(PostgreSQL) │  WebClient
  └─────────────┘   └─────────────┘
```

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| **api-gateway** | 8080 | Punto de entrada único, CORS, logging |
| **user-auth-service** | 8081 | Registro, login, JWT, gestión de usuarios |
| **scooter-rental-service** | 8082 | Inventario de patinetas, arriendos |
| **bff-service** | 8083 | Dashboard y resumen agregado para el frontend |

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

# 2. Levantar cada servicio en una terminal distinta
cd user-auth-service   && gradle bootRun   # terminal 1
cd scooter-rental-service && gradle bootRun  # terminal 2
cd bff-service         && gradle bootRun   # terminal 3
cd api-gateway         && gradle bootRun   # terminal 4
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

El punto de entrada es siempre el Gateway en el puerto **8080**.

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

### BFF — Backend for Frontend (`/api/bff`)

El BFF agrega datos de los dos microservicios en una sola respuesta. Requiere token JWT.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/bff/dashboard` | Perfil del usuario + arriendos activos + recientes |
| `GET` | `/api/bff/scooters/available` | Scooters disponibles con todos sus datos |
| `GET` | `/api/bff/rental-summary` | Resumen estadístico de arriendos del usuario |
| `GET` | `/api/bff/scooters` | Todas las patinetas (solo ADMIN) |

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
| Gestionar patinetas (CRUD) | ❌ | ❌ | ✅ |
| Ver todos los usuarios | ❌ | ❌ | ✅ |
| Ver arriendos largos | ❌ | ❌ | ✅ |
| Swagger UI | ✅ | ✅ | ✅ |

---

## Swagger / OpenAPI

Cada servicio tiene su propia documentación interactiva:

- **user-auth-service:** http://localhost:8081/swagger-ui.html
- **scooter-rental-service:** http://localhost:8082/swagger-ui.html
- **bff-service:** http://localhost:8083/swagger-ui.html

Para autenticarse en Swagger: login → copia el `token` → botón **Authorize** → escribe `Bearer <token>`.

---

## Base de Datos y Migraciones

**Flyway** crea las tablas automáticamente al arrancar cada servicio.

| Servicio | Base de datos | Migraciones |
|---|---|---|
| user-auth-service | `db_scooter_users` | V1 tabla users, V2 seed admin, V3 fix password |
| scooter-rental-service | `db_scooter_rentals` | V1 tabla scooters, V2 tabla rentals |

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
cd user-auth-service     && SPRING_PROFILES_ACTIVE=dev gradle bootRun
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
cd user-auth-service     && gradle test
cd scooter-rental-service && gradle test
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
docker logs -f velourbe-gateway        # logs del API Gateway
docker logs -f velourbe-user-auth      # logs del servicio de auth
docker logs -f velourbe-rental         # logs de arriendos
docker logs -f velourbe-bff            # logs del BFF
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
