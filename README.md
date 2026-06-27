# MediSalud Scheduling

API REST para agendamiento de citas médicas. Proyecto construido como prueba técnica usando Java 21, Spring Boot y Clean Architecture con el scaffold de Bancolombia.

El objetivo del MVP es priorizar arquitectura clara, reglas de negocio testeables y separación estricta entre dominio, casos de uso, entry points y adapters.

## Estado Actual

Implementado:

- Modelos de dominio puros: `Doctor`, `Patient`, `Appointment`, `Penalty` y `AppointmentStatus`.
- DTOs explícitos de request/response para la API.
- `POST /api/doctors` funcional con persistencia JPA.
- `POST /api/patients` funcional con persistencia JPA y unicidad de documento.
- Validaciones declarativas para registro de médicos y pacientes.
- Respuestas de validación con `ProblemDetail` y campo `errors`.
- Respuesta `409` con `ProblemDetail` para documento de paciente duplicado.
- PostgreSQL local con Docker Compose para perfil `local`.
- H2 en memoria para perfil `test`.

Pendiente:

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

## Datos Iniciales Locales

El perfil `local` ejecuta `data-local.sql` al arrancar la aplicación. Este seed carga médicos y pacientes de ejemplo en PostgreSQL local.

La carga se activa con:

```yaml
spring:
  sql:
    init:
      mode: always
      platform: local
```

Por ahora solo están activos los seeds de `doctors` y `patients`. Las secciones de `appointments` y `penalties` quedan comentadas hasta implementar sus entidades y adapters JPA.

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

## Endpoints Implementados

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

Decisión de negocio:

- RF-01 no aplica restricción de unicidad para médicos porque el enunciado no define un identificador natural obligatorio. Cada solicitud válida a `POST /api/doctors` crea un nuevo médico.
- En un caso real sería pertinente definir un dato que permita garantizar unicidad, por ejemplo número de licencia médica, registro profesional o un identificador externo obligatorio.

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

### Registrar Paciente

```http
POST /api/patients
```

Request:

```json
{
  "fullName": "Mateo Perez",
  "documentId": "123456789",
  "phone": "3107654321",
  "email": "mateo.perez@medisalud.com",
  "birthDate": "1990-05-12"
}
```

Response `201`:

```json
{
  "id": "generated-uuid",
  "fullName": "Mateo Perez",
  "documentId": "123456789",
  "phone": "3107654321",
  "email": "mateo.perez@medisalud.com",
  "birthDate": "1990-05-12"
}
```

Campos:

- `fullName`: obligatorio, 3 a 100 caracteres.
- `documentId`: obligatorio, único, mínimo 7 caracteres.
- `phone`: obligatorio, mínimo 7 dígitos.
- `email`: obligatorio, formato email.
- `birthDate`: opcional, debe ser una fecha pasada si se envía.

Un `documentId` duplicado retorna `409 Conflict`.

Ejemplo con curl:

```bash
curl -i -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Mateo Perez",
    "documentId": "123456789",
    "phone": "3107654321",
    "email": "mateo.perez@medisalud.com",
    "birthDate": "1990-05-12"
  }'
```

## Manejo de Errores

La API usa `ProblemDetail` de Spring Boot, basado en RFC 7807, para mantener respuestas de error consistentes.

Campos estándar:

- `type`: URI estable que identifica el tipo de error. Puede apuntar a documentación futura.
- `title`: descripción corta del error.
- `status`: código HTTP.
- `detail`: detalle específico de la ocurrencia.
- `instance`: endpoint donde ocurrió el error.
- `errors`: extensión usada solo en errores de validación `400`, con detalle por campo.

Las URLs usadas en `type`, por ejemplo `https://medisalud.com/errors/patient-document-already-exists`, no tienen que existir como páginas reales durante el MVP. Funcionan como identificadores estables del tipo de problema.

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

Ejemplo `409` por documento de paciente duplicado:

```json
{
  "type": "https://medisalud.com/errors/patient-document-already-exists",
  "title": "Patient document already exists",
  "status": 409,
  "detail": "Patient document already exists: 123456789",
  "instance": "/api/patients"
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
./gradlew guc --name=CreatePatient
./gradlew gep --type=restmvc
./gradlew gda --type=jpa
```

## Notas de Desarrollo

- `SPEC.md` es una nota local de trabajo y está ignorada por Git.
- Los archivos `medisalud.env.*` reales están ignorados por Git.
- `docker-compose.yml` y `medisalud.env.local.example` sí se versionan.
- Cada cambio importante de ejecución, perfiles, endpoints, arquitectura o decisiones técnicas debe reflejarse en este README.
