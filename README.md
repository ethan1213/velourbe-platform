# VeloUrbe — Plataforma de Arriendo de Patinetas Eléctricas

---

## El Problema

Las ciudades modernas enfrentan el **problema de la última milla**: los ciudadanos necesitan desplazarse distancias cortas (500 m – 5 km) entre el transporte público y su destino final, pero no tienen medios ágiles ni económicos para hacerlo. Los taxis son caros, el transporte público no cubre el último tramo y caminar toma demasiado tiempo.

Las patinetas eléctricas de arriendo son la solución ideal, pero requieren una **plataforma digital** que gestione en tiempo real:
- Qué patinetas están disponibles y dónde.
- Quién tiene arrendada cada patineta.
- El cobro justo por tiempo de uso.
- La identidad y autorización de cada usuario.

---

## La Solución

VeloUrbe es una **plataforma de microservicios** diseñada para escalar cada componente de forma independiente:

| Servicio | Responsabilidad | Puerto |
|---|---|---|
| **user-auth-service** | Registro, login, gestión de usuarios, emisión de JWT | 8081 |
| **scooter-rental-service** | Inventario de patinetas, inicio y fin de arriendos | 8082 |

**¿Por qué microservicios?** Si la demanda de arriendos se multiplica por 10, solo escalamos el `scooter-rental-service`. Si se necesita mejorar la autenticación (agregar OAuth, 2FA), solo tocamos el `user-auth-service`. Cada servicio tiene su propia base de datos, puede desplegarse y actualizarse de forma independiente sin afectar al otro.

---

## Comunicación entre Microservicios mediante JWT

El `user-auth-service` genera tokens JWT al hacer login. El `scooter-rental-service` los valida **sin consultar ninguna base de datos adicional**, usando el mismo secret compartido:

```
CLIENTE                user-auth-service (8081)       scooter-rental-service (8082)
   │                           │                                    │
   │── POST /api/auth/login ──►│                                    │
   │                           │ Valida credenciales en DB          │
   │                           │ Genera JWT firmado (HS256)         │
   │◄── { token, role } ───────│                                    │
   │                                                                │
   │── POST /api/rentals/start ─────────────────────────────────────►│
   │   Authorization: Bearer <token>                                 │
   │                                                          JwtAuthFilter verifica
   │                                                          la firma del token con
   │                                                          el mismo secret compartido.
   │                                                          Sin consulta a BD de usuarios.
   │                                                          Extrae: email, role, userId
   │◄── { rental creado } ───────────────────────────────────────────│
```

**El JWT contiene en su payload:**
```json
{
  "sub": "usuario@velourbe.cl",
  "role": "CLIENT",
  "userId": 2,
  "iat": 1716636000,
  "exp": 1716639600
}
```

Este diseño garantiza que los servicios estén **desacoplados**: el `scooter-rental-service` conoce la identidad del usuario únicamente a través del token, nunca consultando la base de datos de usuarios.

---

VeloUrbe es una plataforma de microservicios construida con **Spring Boot 3.5**, **Java 21** y **Gradle (Kotlin DSL)** que gestiona el arriendo de patinetas eléctricas en la ciudad. Está compuesta por dos servicios completamente independientes que se comunican mediante JWT.

---

## Tabla de Contenidos

1. [Arquitectura General](#1-arquitectura-general)
2. [Requisitos Previos](#2-requisitos-previos)
3. [Configuración de PostgreSQL](#3-configuración-de-postgresql)
4. [Estructura del Proyecto](#4-estructura-del-proyecto)
5. [Microservicio: user-auth-service](#5-microservicio-user-auth-service)
6. [Microservicio: scooter-rental-service](#6-microservicio-scooter-rental-service)
7. [Cómo Levantar los Servicios](#7-cómo-levantar-los-servicios)
8. [Flujo de Uso Completo](#8-flujo-de-uso-completo)
9. [Seguridad y JWT](#9-seguridad-y-jwt)
10. [Documentación API (Swagger)](#10-documentación-api-swagger)
11. [Colección Postman](#11-colección-postman)
12. [Referencia Completa de Endpoints](#12-referencia-completa-de-endpoints)
13. [Variables de Entorno](#13-variables-de-entorno)
14. [Migraciones de Base de Datos (Flyway)](#14-migraciones-de-base-de-datos-flyway)
15. [Roles y Permisos](#15-roles-y-permisos)
16. [Credenciales por Defecto](#16-credenciales-por-defecto)
17. [Preguntas Frecuentes](#17-preguntas-frecuentes)

---

## 1. Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTE (Postman / Swagger / App)     │
└──────────────────────┬──────────────────────┬───────────────┘
                       │                      │
              Puerto 8081              Puerto 8082
                       │                      │
         ┌─────────────▼──────────┐  ┌────────▼────────────────┐
         │   user-auth-service    │  │  scooter-rental-service  │
         │                        │  │                          │
         │  - Registro de usuarios│  │  - Gestión de patinetas  │
         │  - Login con JWT       │  │  - Inicio de arriendos   │
         │  - Listado de usuarios │  │  - Fin de arriendos      │
         └─────────────┬──────────┘  └────────┬────────────────┘
                       │                      │
              ┌────────▼──────┐      ┌────────▼──────┐
              │ db_scooter_   │      │ db_scooter_   │
              │    users      │      │   rentals     │
              │ (PostgreSQL)  │      │ (PostgreSQL)  │
              └───────────────┘      └───────────────┘
```

**¿Por qué dos servicios separados?**
Cada microservicio tiene su propia base de datos y puede desplegarse, escalarse y actualizarse de forma independiente. El `scooter-rental-service` no consulta la base de datos de usuarios: simplemente valida el JWT que recibe en cada petición para saber quién es el usuario y qué rol tiene.

---

## 2. Requisitos Previos

Antes de comenzar, asegúrate de tener instalado lo siguiente en tu máquina:

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| **Java (JDK)** | 21 | `java -version` |
| **PostgreSQL** | 14+ | `psql --version` |
| **Git** | cualquiera | `git --version` |

> **Nota sobre Gradle:** No necesitas instalar Gradle. Cada microservicio incluye el script `gradlew` (Linux/Mac) y `gradlew.bat` (Windows) que descarga y usa automáticamente la versión correcta de Gradle (9.4.1).

### Instalación de Java 21

- **Windows:** Descarga el JDK 21 desde [adoptium.net](https://adoptium.net/) e instala el instalador `.msi`. Asegúrate de marcar la opción "Set JAVA_HOME variable".
- **macOS:** `brew install openjdk@21`
- **Linux (Ubuntu/Debian):** `sudo apt install openjdk-21-jdk`

Verifica la instalación:
```bash
java -version
# Debe mostrar algo como: openjdk version "21.x.x"
```

### Instalación de PostgreSQL

- **Windows:** Descarga desde [postgresql.org/download](https://www.postgresql.org/download/windows/) y ejecuta el instalador. Durante la instalación, anota el puerto (por defecto `5432`) y la contraseña del usuario `postgres`.
- **macOS:** `brew install postgresql@16 && brew services start postgresql@16`
- **Linux (Ubuntu/Debian):** `sudo apt install postgresql postgresql-contrib && sudo systemctl start postgresql`

---

## 3. Configuración de PostgreSQL

Una vez instalado PostgreSQL, debes crear las dos bases de datos que usará la plataforma. Abre una terminal y conéctate a PostgreSQL:

```bash
# Con el usuario postgres (el superusuario por defecto)
psql -U postgres
```

Luego ejecuta estos comandos SQL:

```sql
CREATE DATABASE db_scooter_users;
CREATE DATABASE db_scooter_rentals;

-- Verifica que se crearon correctamente
\l
```

Deberías ver `db_scooter_users` y `db_scooter_rentals` en la lista.

> **Si tu PostgreSQL tiene un usuario o contraseña diferente** a `postgres`/`postgres`, edita los archivos `application.yml` de cada servicio antes de arrancar (ver sección [Variables de Entorno](#12-variables-de-entorno)).

---

## 4. Estructura del Proyecto

```
velourbe/
├── .env.example                        ← Plantilla de variables de entorno
├── .gitignore
│
├── user-auth-service/                  ← Microservicio de autenticación (puerto 8081)
│   ├── build.gradle.kts                ← Dependencias y configuración de build
│   ├── settings.gradle.kts             ← Nombre del proyecto
│   ├── gradlew / gradlew.bat           ← Scripts para correr Gradle sin instalarlo
│   ├── gradle/wrapper/                 ← Archivos del Gradle Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/cl/velourbe/userauth/
│       │   │   ├── UserAuthServiceApplication.java     ← Punto de entrada
│       │   │   ├── config/
│       │   │   │   ├── JwtAuthFilter.java              ← Intercepta cada request y valida el JWT
│       │   │   │   └── SecurityConfig.java             ← Reglas de acceso por endpoint
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java             ← POST /api/auth/register y /login
│       │   │   │   └── UserController.java             ← GET /api/users (solo ADMIN)
│       │   │   ├── exception/
│       │   │   │   ├── EmailAlreadyExistsException.java
│       │   │   │   ├── InvalidCredentialsException.java
│       │   │   │   ├── ErrorResponse.java              ← Formato estándar de error
│       │   │   │   └── GlobalExceptionHandler.java     ← Convierte excepciones en respuestas HTTP
│       │   │   ├── model/
│       │   │   │   ├── dto/                            ← Objetos de transferencia de datos
│       │   │   │   │   ├── AuthResponseDTO.java        ← Respuesta con token y rol
│       │   │   │   │   ├── LoginRequestDTO.java
│       │   │   │   │   ├── RegisterRequestDTO.java
│       │   │   │   │   └── UserResponseDTO.java
│       │   │   │   └── entity/
│       │   │   │       └── User.java                   ← Entidad JPA mapeada a la tabla "users"
│       │   │   ├── repository/
│       │   │   │   └── UserRepository.java             ← Acceso a base de datos (Spring Data JPA)
│       │   │   ├── security/
│       │   │   │   └── JwtUtil.java                    ← Genera y valida tokens JWT
│       │   │   └── service/
│       │   │       ├── AuthService.java                ← Lógica de registro y login
│       │   │       └── UserService.java                ← Lógica de listado de usuarios
│       │   └── resources/
│       │       ├── application.yml                     ← Configuración del servicio
│       │       └── db/migration/
│       │           ├── V1__create_users_table.sql      ← Crea la tabla users
│       │           └── V2__seed_admin_user.sql         ← Inserta el admin inicial
│       └── test/
│           └── java/cl/velourbe/userauth/
│               └── UserAuthServiceApplicationTests.java
│
└── scooter-rental-service/             ← Microservicio de arriendos (puerto 8082)
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── gradlew / gradlew.bat
    ├── gradle/wrapper/
    └── src/
        ├── main/
        │   ├── java/cl/velourbe/rental/
        │   │   ├── ScooterRentalServiceApplication.java
        │   │   ├── config/
        │   │   │   ├── JwtAuthFilter.java              ← Valida el JWT emitido por user-auth-service
        │   │   │   └── SecurityConfig.java
        │   │   ├── controller/
        │   │   │   ├── ScooterController.java          ← CRUD de patinetas (solo ADMIN)
        │   │   │   └── RentalController.java           ← Inicio/fin/listado de arriendos
        │   │   ├── exception/
        │   │   │   ├── ScooterNotFoundException.java
        │   │   │   ├── ScooterNotAvailableException.java
        │   │   │   ├── RentalNotFoundException.java
        │   │   │   ├── ErrorResponse.java
        │   │   │   └── GlobalExceptionHandler.java
        │   │   ├── model/
        │   │   │   ├── dto/
        │   │   │   │   ├── ScooterRequestDTO.java
        │   │   │   │   ├── ScooterResponseDTO.java
        │   │   │   │   ├── RentalRequestDTO.java
        │   │   │   │   └── RentalResponseDTO.java
        │   │   │   ├── entity/
        │   │   │   │   ├── Scooter.java                ← Entidad mapeada a la tabla "scooters"
        │   │   │   │   └── Rental.java                 ← Entidad mapeada a la tabla "rentals"
        │   │   │   └── enums/
        │   │   │       ├── ScooterStatus.java          ← AVAILABLE, IN_USE, MAINTENANCE
        │   │   │       └── RentalStatus.java           ← ACTIVE, COMPLETED, CANCELLED
        │   │   ├── repository/
        │   │   │   ├── ScooterRepository.java
        │   │   │   └── RentalRepository.java
        │   │   ├── security/
        │   │   │   ├── JwtTokenValidator.java          ← Valida firma y expiración del JWT
        │   │   │   └── SecurityUtils.java              ← Extrae userId y email del contexto de seguridad
        │   │   └── service/
        │   │       ├── ScooterService.java
        │   │       └── RentalService.java
        │   └── resources/
        │       ├── application.yml
        │       └── db/migration/
        │           ├── V1__create_scooters_table.sql
        │           └── V2__create_rentals_table.sql
        └── test/
            └── java/cl/velourbe/rental/
                └── ScooterRentalServiceApplicationTests.java
```

---

## 5. Microservicio: user-auth-service

**Puerto:** `8081`  
**Base de datos:** `db_scooter_users`  
**Responsabilidad:** Gestión de usuarios, registro, autenticación y emisión de tokens JWT.

### Tabla de base de datos: `users`

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGSERIAL (PK) | Identificador único autoincremental |
| `email` | VARCHAR(150) UNIQUE | Email del usuario (clave de login) |
| `password_hash` | VARCHAR(255) | Contraseña encriptada con BCrypt (factor 10) |
| `full_name` | VARCHAR(200) | Nombre completo |
| `role` | VARCHAR(20) | `CLIENT` o `ADMIN` |
| `created_at` | TIMESTAMP | Fecha de creación (se asigna automáticamente) |
| `active` | BOOLEAN | Si el usuario está activo (por defecto `true`) |

### Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Registra un nuevo usuario con rol `CLIENT` |
| `POST` | `/api/auth/login` | Público | Autentica y devuelve un JWT + rol |
| `GET` | `/api/users` | Solo ADMIN | Lista todos los usuarios registrados |

---

## 6. Microservicio: scooter-rental-service

**Puerto:** `8082`  
**Base de datos:** `db_scooter_rentals`  
**Responsabilidad:** Gestión del inventario de patinetas y ciclo de vida de los arriendos.

### Tabla de base de datos: `scooters`

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGSERIAL (PK) | Identificador único |
| `serial_code` | VARCHAR(50) UNIQUE | Código de serie físico de la patineta |
| `model` | VARCHAR(80) | Modelo (ej: "Xiaomi Pro 2") |
| `battery` | INT (0-100) | Porcentaje de batería actual |
| `location` | VARCHAR(100) | Ubicación física actual |
| `status` | VARCHAR(20) | `AVAILABLE`, `IN_USE` o `MAINTENANCE` |
| `created_at` | TIMESTAMP | Fecha de registro |

### Tabla de base de datos: `rentals`

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGSERIAL (PK) | Identificador único del arriendo |
| `user_id` | BIGINT | ID del usuario (tomado del JWT, no FK a otra BD) |
| `scooter_id` | BIGINT (FK) | Referencia a la patineta arrendada |
| `started_at` | TIMESTAMP | Momento de inicio del arriendo |
| `ended_at` | TIMESTAMP | Momento de fin (null si aún activo) |
| `status` | VARCHAR(20) | `ACTIVE`, `COMPLETED` o `CANCELLED` |
| `total_minutes` | INT | Duración total calculada al finalizar |

### Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/scooters` | Solo ADMIN | Lista todas las patinetas |
| `GET` | `/api/scooters/available` | Solo ADMIN | Lista patinetas disponibles |
| `GET` | `/api/scooters/{id}` | Solo ADMIN | Detalle de una patineta |
| `POST` | `/api/scooters` | Solo ADMIN | Registra una nueva patineta |
| `DELETE` | `/api/scooters/{id}` | Solo ADMIN | Elimina una patineta |
| `POST` | `/api/rentals/start` | Autenticado | Inicia un arriendo |
| `PATCH` | `/api/rentals/{id}/end` | Autenticado | Finaliza un arriendo |
| `GET` | `/api/rentals/my` | Autenticado | Mis arriendos (histórico) |

---

## 7. Cómo Levantar los Servicios

### Paso 1 — Clona el repositorio

```bash
git clone <url-del-repositorio>
cd velourbe
```

### Paso 2 — Crea las bases de datos (solo la primera vez)

```bash
psql -U postgres -c "CREATE DATABASE db_scooter_users;"
psql -U postgres -c "CREATE DATABASE db_scooter_rentals;"
```

### Paso 3 — Levanta el user-auth-service

Abre una terminal en la carpeta del proyecto:

```bash
cd user-auth-service

# Linux / macOS
./gradlew bootRun

# Windows (Command Prompt o PowerShell)
.\gradlew.bat bootRun
```

La primera vez tardará unos minutos porque Gradle descarga las dependencias. Espera hasta ver en la consola:

```
Started UserAuthServiceApplication in X.XXX seconds
```

### Paso 4 — Levanta el scooter-rental-service

Abre **otra terminal** (deja la anterior corriendo):

```bash
cd scooter-rental-service

# Linux / macOS
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```

Espera hasta ver:

```
Started ScooterRentalServiceApplication in X.XXX seconds
```

### Paso 5 — Verifica que ambos servicios están corriendo

- user-auth-service: http://localhost:8081/swagger-ui.html
- scooter-rental-service: http://localhost:8082/swagger-ui.html

Si se abren las páginas de Swagger, todo está funcionando correctamente.

---

## 8. Flujo de Uso Completo

Este es el flujo típico desde cero hasta arrendar una patineta:

### Paso A — Registrar un usuario cliente

**Request:**
```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "email": "juan@ejemplo.cl",
  "password": "miContraseña123",
  "fullName": "Juan Pérez"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "CLIENT"
}
```

### Paso B — Login como administrador

El sistema ya tiene un administrador pre-cargado:

**Request:**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@velourbe.cl",
  "password": "admin123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADMIN"
}
```

Copia el token del administrador, lo necesitarás en el siguiente paso.

### Paso C — Crear una patineta (como ADMIN)

**Request:**
```http
POST http://localhost:8082/api/scooters
Content-Type: application/json
Authorization: Bearer <token-del-admin>

{
  "serialCode": "SC-001",
  "model": "Xiaomi Pro 2",
  "battery": 85,
  "location": "Plaza Italia, Santiago"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "serialCode": "SC-001",
  "model": "Xiaomi Pro 2",
  "battery": 85,
  "location": "Plaza Italia, Santiago",
  "status": "AVAILABLE",
  "createdAt": "2025-05-25T10:00:00"
}
```

### Paso D — Iniciar un arriendo (como CLIENT)

Usa el token del usuario cliente registrado en el Paso A:

**Request:**
```http
POST http://localhost:8082/api/rentals/start
Content-Type: application/json
Authorization: Bearer <token-del-cliente>

{
  "scooterId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 2,
  "scooterId": 1,
  "scooterModel": "Xiaomi Pro 2",
  "startedAt": "2025-05-25T10:05:00",
  "endedAt": null,
  "status": "ACTIVE",
  "totalMinutes": null
}
```

La patineta cambia automáticamente su estado a `IN_USE`.

### Paso E — Finalizar el arriendo

**Request:**
```http
PATCH http://localhost:8082/api/rentals/1/end
Authorization: Bearer <token-del-cliente>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 2,
  "scooterId": 1,
  "scooterModel": "Xiaomi Pro 2",
  "startedAt": "2025-05-25T10:05:00",
  "endedAt": "2025-05-25T10:35:00",
  "status": "COMPLETED",
  "totalMinutes": 30
}
```

La patineta vuelve automáticamente a estado `AVAILABLE`.

### Paso F — Ver historial de arriendos

**Request:**
```http
GET http://localhost:8082/api/rentals/my
Authorization: Bearer <token-del-cliente>
```

---

## 9. Seguridad y JWT

### ¿Cómo funciona el JWT en esta plataforma?

1. El usuario hace login en `user-auth-service` (puerto 8081).
2. El servicio genera un **token JWT** firmado con el secret configurado en `application.yml`.
3. El token contiene: email del usuario, su rol (`CLIENT` o `ADMIN`) y su ID.
4. En cada request a `scooter-rental-service` (puerto 8082), el cliente envía el token en el header `Authorization: Bearer <token>`.
5. El filtro `JwtAuthFilter` del servicio de arriendos valida la firma del token usando **el mismo secret** y extrae la identidad del usuario sin consultar ninguna base de datos.

### Estructura del token JWT

El token JWT tiene tres partes separadas por puntos (`.`):

```
eyJhbGciOiJIUzI1NiJ9         ← Header (algoritmo HS256)
.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNsIiwicm9sZSI6IkNMSUVOVCIsInVzZXJJZCI6MX0=   ← Payload
.firma_hmac_sha256            ← Firma
```

El **Payload** decodificado contiene:
```json
{
  "sub": "usuario@email.cl",
  "role": "CLIENT",
  "userId": 1,
  "iat": 1716636000,
  "exp": 1716639600
}
```

### Importante: el secret JWT

Ambos servicios deben usar **exactamente el mismo secret** en sus `application.yml`. Si cambian el secret en un servicio, debes cambiarlo también en el otro, de lo contrario los tokens generados por `user-auth-service` no podrán ser validados por `scooter-rental-service`.

El secret debe tener **mínimo 32 caracteres** (256 bits) para el algoritmo HS256.

---

## 11. Colección Postman

La carpeta `postman/` contiene dos archivos listos para importar:

| Archivo | Descripción |
|---|---|
| `VeloUrbe.postman_collection.json` | Colección con todos los endpoints organizados por servicio |
| `VeloUrbe.postman_environment.json` | Variables de entorno preconfiguradas para localhost |

### Cómo importar

1. Abre Postman → botón **Import** (esquina superior izquierda).
2. Arrastra ambos archivos `.json` o navega hasta la carpeta `postman/`.
3. Selecciona el entorno **"VeloUrbe Local"** en el selector de entornos (esquina superior derecha).

### Variables de entorno incluidas

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `auth_url` | `http://localhost:8081` | URL base del user-auth-service |
| `rental_url` | `http://localhost:8082` | URL base del scooter-rental-service |
| `token_admin` | *(vacío)* | Token JWT del admin — pegar después del login |
| `token_client` | *(vacío)* | Token JWT del cliente — pegar después del login |
| `scooter_id` | `1` | ID de la patineta a usar en los requests |
| `rental_id` | `1` | ID del arriendo a usar en los requests |

### Endpoints marcados como "Custom Query"

Tres endpoints usan las consultas JPQL personalizadas definidas con `@Query` en los repositorios:

- `GET /api/scooters/low-battery?threshold=30` — usa `ScooterRepository.findByBatteryBelow()`
- `GET /api/scooters/search?location=plaza` — usa `ScooterRepository.findByLocationContaining()`
- `GET /api/rentals/long?minMinutes=30` — usa `RentalRepository.findCompletedWithMinDuration()`
- `GET /api/users/active/{role}` — usa `UserRepository.findActiveByRole()`

---

## 10. Documentación API (Swagger)

Cada servicio expone una interfaz gráfica interactiva con Swagger UI donde puedes probar todos los endpoints directamente desde el navegador:

- **user-auth-service:** http://localhost:8081/swagger-ui.html
- **scooter-rental-service:** http://localhost:8082/swagger-ui.html

### Cómo autenticarte en Swagger

1. Primero haz login usando el endpoint `/api/auth/login` dentro del propio Swagger.
2. Copia el valor del campo `token` de la respuesta.
3. Haz clic en el botón **"Authorize"** (candado) en la esquina superior derecha.
4. Escribe `Bearer <token>` y haz clic en "Authorize".
5. Ahora todos los requests del Swagger incluirán el token automáticamente.

---

## 11. Referencia Completa de Endpoints

### user-auth-service (puerto 8081)

#### `POST /api/auth/register`

Registra un nuevo usuario con rol `CLIENT`.

**Body:**
```json
{
  "email": "string (requerido, formato email)",
  "password": "string (requerido)",
  "fullName": "string (requerido)"
}
```

**Respuestas:**
- `201 Created` — Usuario registrado, devuelve token y rol.
- `409 Conflict` — El email ya está registrado.
- `400 Bad Request` — Datos inválidos (email malformado, campos vacíos).

---

#### `POST /api/auth/login`

Autentica un usuario existente.

**Body:**
```json
{
  "email": "string (requerido, formato email)",
  "password": "string (requerido)"
}
```

**Respuestas:**
- `200 OK` — Devuelve token y rol.
- `401 Unauthorized` — Credenciales incorrectas.

---

#### `GET /api/users`

Lista todos los usuarios registrados. **Requiere rol ADMIN.**

**Header:** `Authorization: Bearer <token-admin>`

**Respuesta `200 OK`:**
```json
[
  {
    "id": 1,
    "email": "admin@velourbe.cl",
    "fullName": "Administrador VeloUrbe",
    "role": "ADMIN",
    "createdAt": "2025-05-25T10:00:00",
    "active": true
  }
]
```

---

### scooter-rental-service (puerto 8082)

#### `GET /api/scooters`

Lista todas las patinetas. **Requiere rol ADMIN.**

---

#### `GET /api/scooters/available`

Lista solo las patinetas con estado `AVAILABLE`. **Requiere rol ADMIN.**

---

#### `GET /api/scooters/{id}`

Obtiene el detalle de una patineta por su ID. **Requiere rol ADMIN.**

**Respuestas:**
- `200 OK` — Datos de la patineta.
- `404 Not Found` — Patineta no encontrada.

---

#### `POST /api/scooters`

Registra una nueva patineta. **Requiere rol ADMIN.**

**Body:**
```json
{
  "serialCode": "string (requerido, único)",
  "model": "string (requerido)",
  "battery": "integer (requerido, 0-100)",
  "location": "string (requerido)"
}
```

**Respuestas:**
- `201 Created` — Patineta registrada.
- `400 Bad Request` — Datos inválidos.

---

#### `DELETE /api/scooters/{id}`

Elimina una patineta por su ID. **Requiere rol ADMIN.**

**Respuestas:**
- `204 No Content` — Eliminada correctamente.
- `404 Not Found` — Patineta no encontrada.

---

#### `POST /api/rentals/start`

Inicia un arriendo. La patineta pasa a estado `IN_USE`. **Requiere autenticación.**

**Body:**
```json
{
  "scooterId": 1
}
```

**Respuestas:**
- `201 Created` — Arriendo iniciado.
- `404 Not Found` — Patineta no encontrada.
- `409 Conflict` — La patineta no está disponible (ya está en uso o en mantenimiento).

---

#### `PATCH /api/rentals/{id}/end`

Finaliza un arriendo activo. Calcula los minutos totales y devuelve la patineta a estado `AVAILABLE`. **Requiere autenticación.**

**Respuestas:**
- `200 OK` — Arriendo finalizado con duración calculada.
- `404 Not Found` — Arriendo no encontrado.

---

#### `GET /api/rentals/my`

Lista todos los arriendos del usuario autenticado. **Requiere autenticación.**

**Respuesta `200 OK`:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "scooterId": 1,
    "scooterModel": "Xiaomi Pro 2",
    "startedAt": "2025-05-25T10:05:00",
    "endedAt": "2025-05-25T10:35:00",
    "status": "COMPLETED",
    "totalMinutes": 30
  }
]
```

---

## 12. Variables de Entorno

El archivo `.env.example` en la raíz del proyecto muestra las variables configurables:

```env
JWT_SECRET=cambiame-por-un-secret-de-256-bits-minimo-aqui
JWT_EXPIRATION_MS=3600000
```

Actualmente los valores están hardcodeados en cada `application.yml`. Si quieres personalizar la configuración sin modificar el código fuente, edita directamente los archivos:

**`user-auth-service/src/main/resources/application.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db_scooter_users
    username: postgres        # ← Cambia aquí si tu usuario es diferente
    password: postgres        # ← Cambia aquí si tu contraseña es diferente

jwt:
  secret: cambiame-por-un-secret-de-256-bits-minimo-aqui   # ← Cambia por un secret seguro
  expiration-ms: 3600000     # ← Duración del token en milisegundos (1 hora por defecto)
```

**`scooter-rental-service/src/main/resources/application.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db_scooter_rentals
    username: postgres        # ← Mismo cambio si aplica
    password: postgres

jwt:
  secret: cambiame-por-un-secret-de-256-bits-minimo-aqui   # ← Debe ser IDÉNTICO al de user-auth-service
```

> **Importante para producción:** Nunca uses el secret de ejemplo en un entorno real. Genera uno seguro con: `openssl rand -base64 32`

---

## 13. Migraciones de Base de Datos (Flyway)

Flyway es la herramienta que crea y mantiene las tablas automáticamente. Al arrancar cada servicio por primera vez, Flyway ejecuta los scripts SQL en orden y crea todas las tablas. Si el servicio ya corrió antes, Flyway detecta qué scripts ya ejecutó y solo corre los nuevos.

**user-auth-service:**
- `V1__create_users_table.sql` — Crea la tabla `users`
- `V2__seed_admin_user.sql` — Inserta el administrador inicial

**scooter-rental-service:**
- `V1__create_scooters_table.sql` — Crea la tabla `scooters`
- `V2__create_rentals_table.sql` — Crea la tabla `rentals`

> Si necesitas agregar nuevas tablas o columnas en el futuro, crea un archivo `V3__descripcion.sql`, `V4__descripcion.sql`, etc. **Nunca modifiques archivos de migración ya ejecutados**, ya que Flyway verificará su checksum y lanzará un error.

---

## 14. Roles y Permisos

| Acción | Público | CLIENT | ADMIN |
|---|:---:|:---:|:---:|
| Registrarse | ✅ | — | — |
| Hacer login | ✅ | — | — |
| Ver usuarios | ❌ | ❌ | ✅ |
| Ver patinetas | ❌ | ❌ | ✅ |
| Ver patinetas disponibles | ❌ | ❌ | ✅ |
| Crear patineta | ❌ | ❌ | ✅ |
| Eliminar patineta | ❌ | ❌ | ✅ |
| Iniciar arriendo | ❌ | ✅ | ✅ |
| Finalizar arriendo | ❌ | ✅ | ✅ |
| Ver mis arriendos | ❌ | ✅ | ✅ |
| Acceder a Swagger UI | ✅ | ✅ | ✅ |

Los usuarios registrados mediante `/api/auth/register` siempre obtienen el rol `CLIENT`. El único administrador inicial es el que viene pre-cargado en la migración `V2`.

---

## 15. Credenciales por Defecto

El sistema viene con un administrador pre-cargado listo para usar:

| Campo | Valor |
|---|---|
| **Email** | `admin@velourbe.cl` |
| **Contraseña** | `admin123` |
| **Rol** | `ADMIN` |

> Esta contraseña está almacenada como hash BCrypt (factor 10) en la base de datos. El hash almacenado es:
> `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFkUSC5Nv5FpVx9bkC5z.8K`

---

## 16. Preguntas Frecuentes

**¿Por qué al arrancar el servicio aparece un error de conexión a la base de datos?**

Asegúrate de que:
1. PostgreSQL está corriendo (`pg_ctl status` o verifica en los servicios de Windows).
2. Las bases de datos existen (`CREATE DATABASE db_scooter_users` y `CREATE DATABASE db_scooter_rentals`).
3. El usuario y contraseña en `application.yml` coinciden con los de tu instalación de PostgreSQL.

---

**¿Por qué obtengo error 403 Forbidden al llamar un endpoint?**

El token JWT que estás usando no tiene el rol requerido, o no estás enviando el token. Verifica:
1. Que el header sea exactamente `Authorization: Bearer <token>` (con espacio entre "Bearer" y el token).
2. Que el token no haya expirado (por defecto expiran en 1 hora).
3. Que el rol del usuario tenga acceso al endpoint (ver tabla de [Roles y Permisos](#14-roles-y-permisos)).

---

**¿Por qué obtengo un error al intentar arrendar una patineta?**

Las causas más comunes son:
- La patineta no existe (`404 Not Found`).
- La patineta ya está siendo usada o en mantenimiento (`409 Conflict`). Solo se pueden arrendar patinetas con estado `AVAILABLE`.

---

**¿Puedo cambiar el puerto de algún servicio?**

Sí. En el `application.yml` del servicio correspondiente, cambia:
```yaml
server:
  port: 8081   # ← Cambia por el puerto que necesites
```

---

**¿Por qué la primera vez que corro `./gradlew bootRun` tarda tanto?**

La primera vez Gradle descarga todas las dependencias del proyecto (Spring Boot, JWT, Flyway, etc.) desde Maven Central. Dependiendo de tu conexión, puede tardar entre 2 y 10 minutos. Las siguientes veces es inmediato porque queda en caché local.

---

**¿Cómo detengo los servicios?**

En cada terminal donde está corriendo un servicio, presiona `Ctrl + C`.

---

**¿El proyecto usa Docker?**

No. La plataforma está diseñada para correr directamente sobre una instalación local de PostgreSQL y Java, sin Docker ni docker-compose.

---

## Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.0 | Framework base |
| Spring Security | (incluido en Boot) | Seguridad y control de acceso |
| Spring Data JPA | (incluido en Boot) | Acceso a base de datos |
| Flyway | (incluido en Boot) | Migraciones de esquema SQL |
| PostgreSQL | 14+ | Base de datos relacional |
| JJWT (jjwt-api) | 0.12.6 | Generación y validación de tokens JWT |
| Lombok | 1.18.34 | Reducción de código boilerplate |
| SpringDoc OpenAPI | 2.6.0 | Generación automática de Swagger UI |
| Gradle | 9.4.1 | Sistema de build (Kotlin DSL) |
