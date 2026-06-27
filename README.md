# MediSalud Scheduling

API REST para agendamiento de citas médicas. Proyecto construido como prueba técnica usando Java 21, Spring Boot y Clean Architecture con el scaffold de Bancolombia.

El objetivo del MVP es priorizar arquitectura clara, reglas de negocio testeables y separación estricta entre dominio, casos de uso, entry points y adapters.

## Estado Actual

Implementado:

- Modelos de dominio puros: `Doctor`, `Patient`, `Appointment`, `Penalty` y `AppointmentStatus`.
- DTOs explícitos de request/response para la API.
- `POST /api/doctors` funcional con persistencia JPA.
- Validaciones declarativas para registro de médicos.
- Respuestas de validación con `ProblemDetail` y campo `errors`.
- PostgreSQL local con Docker Compose para perfil `local`.
- H2 en memoria para perfil `test`.

Pendiente:

- Registro de pacientes.
- Reserva, disponibilidad, cancelación, listado y reprogramación de citas.
- Handler completo para excepciones de dominio.
- README final con todos los endpoints cuando estén implementados.

## Stack

- Java 21
- Spring Boot 3.5.16
- Gradle
- JPA/Hibernate
- PostgreSQL para runtime local
- H2 para tests
- Docker Compose
- Clean Architecture Scaffold de Bancolombia

## Arquitectura

El proyecto respeta la estructura generada por el scaffold Clean Architecture de Bancolombia:

```text
domain/model                    -> modelos de dominio y gateways
domain/usecase                  -> casos de uso
infrastructure/entry-points     -> API REST, DTOs, handlers
infrastructure/driven-adapters  -> persistencia JPA
applications/app-service        -> aplicación Spring Boot y wiring
```

Reglas principales:

- El dominio no tiene anotaciones JPA ni dependencias de Spring.
- Los modelos de dominio no se exponen directamente en la API.
- Los entry points usan DTOs explícitos.
- La persistencia se implementa en el driven adapter JPA.
- Los use cases se generan con el scaffold y se registran mediante `UseCasesConfig`.

## Perfiles Spring

Los perfiles se activan con `SPRING_PROFILES_ACTIVE`.

| Perfil | Archivo | Uso | Base de datos |
| --- | --- | --- | --- |
| default/prod | `application.yaml` | Configuración base con variables de entorno | PostgreSQL |
| local | `application-local.yaml` | Desarrollo local con Docker Compose | PostgreSQL |
| test | `application-test.yaml` | Tests automatizados | H2 en memoria |

Spring siempre carga `application.yaml`. Si activas `local`, también carga `application-local.yaml`. Si activas `test`, también carga `application-test.yaml`.

## Variables de Entorno

El archivo versionado de referencia para desarrollo local es:

```text
medisalud.env.local.example
```

Cada desarrollador debe crear su archivo local real:

```bash
cp medisalud.env.local.example medisalud.env.local
```

`medisalud.env.local` no se versiona. Puede contener credenciales o valores propios de la máquina.

Variables relevantes:

```env
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5432/medisalud
DB_NAME=medisalud
DB_PORT=5432
DB_USER=medisalud
DB_PASS=medisalud
JPA_DDL_AUTO=update
```

## PostgreSQL Local

Levantar PostgreSQL:

```bash
docker compose --env-file medisalud.env.local up -d postgres
```

Validar el estado:

```bash
docker compose --env-file medisalud.env.local ps
docker exec medisalud-postgres pg_isready -U medisalud -d medisalud
```

Detener PostgreSQL conservando datos:

```bash
docker compose --env-file medisalud.env.local down
```

Detener PostgreSQL eliminando también el volumen de datos:

```bash
docker compose --env-file medisalud.env.local down -v
```

## Ejecutar la Aplicación

Con PostgreSQL local ya levantado:

```bash
set -a
source medisalud.env.local
set +a
./gradlew :app-service:bootRun
```

En IntelliJ, carga `medisalud.env.local` en la Run Configuration o define sus variables manualmente. Debe quedar activo:

```env
SPRING_PROFILES_ACTIVE=local
```

La API queda disponible en:

```text
http://localhost:8080
```

## Ejecutar Tests

```bash
./gradlew test
```

Los tests Spring que requieren perfil usan `@ActiveProfiles("test")`, por lo que se conectan a H2 en memoria.

También puedes validar estructura del scaffold:

```bash
./gradlew validateStructure
```

## Endpoint Implementado

### Registrar Médico

```http
POST /api/doctors
```

Request:

```json
{
  "fullName": "Laura Gomez",
  "specialty": "Cardiology",
  "phone": "3001234567",
  "email": "laura.gomez@medisalud.com"
}
```

Response `201`:

```json
{
  "id": "generated-uuid",
  "fullName": "Laura Gomez",
  "specialty": "Cardiology",
  "phone": "3001234567",
  "email": "laura.gomez@medisalud.com"
}
```

Campos:

- `fullName`: obligatorio, 3 a 100 caracteres.
- `specialty`: obligatorio.
- `phone`: opcional, mínimo 7 dígitos si se envía.
- `email`: opcional, formato email si se envía.

Ejemplo con curl:

```bash
curl -i -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Laura Gomez",
    "specialty": "Cardiology",
    "phone": "3001234567",
    "email": "laura.gomez@medisalud.com"
  }'
```

## Errores de Validación

Las validaciones de entrada se manejan con Bean Validation en los request DTOs y un handler global con `ProblemDetail`.

Ejemplo `400`:

```json
{
  "type": "https://medisalud.com/errors/validation",
  "title": "Invalid request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/doctors",
  "errors": [
    {
      "field": "fullName",
      "message": "size must be between 3 and 100"
    }
  ]
}
```

## Comandos del Scaffold

Este proyecto fue generado con el scaffold Clean Architecture de Bancolombia. Para módulos generados por el scaffold se deben usar sus tareas Gradle.

Comandos usados hasta ahora:

```bash
./gradlew gm --name=Doctor
./gradlew gm --name=Patient
./gradlew gm --name=Appointment
./gradlew gm --name=Penalty
./gradlew guc --name=CreateDoctor
./gradlew gep --type=restmvc
./gradlew gda --type=jpa
```

## Notas de Desarrollo

- `SPEC.md` es una nota local de trabajo y está ignorada por Git.
- Los archivos `medisalud.env.*` reales están ignorados por Git.
- `docker-compose.yml` y `medisalud.env.local.example` sí se versionan.
- Cada cambio importante de ejecución, perfiles, endpoints, arquitectura o decisiones técnicas debe reflejarse en este README.
