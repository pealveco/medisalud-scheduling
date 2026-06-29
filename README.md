# MediSalud Scheduling

API REST para agendamiento de citas médicas. Proyecto construido como prueba técnica usando Java 21, Spring Boot y Clean Architecture con el scaffold de Bancolombia.

El objetivo del MVP es priorizar arquitectura clara, reglas de negocio testeables y separación estricta entre dominio, casos de uso, entry points y adapters.

## Estado Actual

Implementado:

- Modelos de dominio puros: `Doctor`, `Patient`, `Appointment`, `Penalty` y `AppointmentStatus`.
- DTOs explícitos de request/response para la API.
- `POST /api/doctors` funcional con persistencia JPA.
- `POST /api/patients` funcional con persistencia JPA y unicidad de documento.
- `POST /api/appointments` funcional con validaciones de reserva RN-01 a RN-05.
- `DELETE /api/appointments/{id}` funcional con cancelación y penalización por cancelación tardía.
- `GET /api/doctors/{id}/availability` funcional con generación de franjas libres por rango.
- Validaciones declarativas para registro de médicos, pacientes y reserva/cancelación de citas.
- Respuestas de validación con `ProblemDetail` y campo `errors`.
- Respuestas `400`, `404` y `409` con `ProblemDetail` para rangos inválidos, recursos inexistentes, duplicidades, conflictos de franja, estado inválido de cita y bloqueo de paciente.
- PostgreSQL local con Docker Compose para perfil `local`.
- H2 en memoria para perfil `test`.

Pendiente:

- Listado y reprogramación de citas.
- Handler completo para las excepciones de dominio de las próximas HU, si aparecen nuevos casos.
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
- La política de horario laboral RN-01 está centralizada en `WorkingHoursPolicy`, reutilizada por reserva y disponibilidad.

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

El perfil `local` ejecuta `data-local.sql` al arrancar la aplicación. Este seed carga médicos, pacientes, citas y penalizaciones de ejemplo en PostgreSQL local.

La carga se activa con:

```yaml
spring:
  sql:
    init:
      mode: always
      platform: local
```

El script puede ejecutarse en cada arranque sin duplicar registros. Médicos y pacientes usan `ON CONFLICT DO NOTHING`; citas y penalizaciones usan `ON CONFLICT DO UPDATE` para refrescar fechas relativas a `NOW()` y mantener reproducibles los escenarios temporales, como el paciente bloqueado por 3 penalizaciones en los últimos 30 días.

Las entidades JPA declaran explícitamente los nombres de columna en `snake_case` cuando el campo Java usa `camelCase` (`fullName` -> `full_name`, `patientId` -> `patient_id`, etc.). Esto mantiene compatible el esquema generado por Hibernate con `data-local.sql`.

Si cambian entidades, nombres de columnas o el contenido del seed, y PostgreSQL local ya tenía un volumen creado, `JPA_DDL_AUTO=update` no renombra columnas existentes de forma segura. Para reiniciar la base local desde cero:

```bash
docker compose --env-file medisalud.env.local down -v
docker compose --env-file medisalud.env.local up -d postgres
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

### Reservar Cita

```http
POST /api/appointments
```

Request:

```json
{
  "patientId": "d4e5f6a7-b8c9-0123-def0-234567890123",
  "doctorId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "dateTime": "YYYY-MM-DDT08:00:00"
}
```

`dateTime` debe reemplazarse por una fecha futura, en día laboral, no festivo y alineada a una franja de 30 minutos.

Response `201`:

```json
{
  "id": "generated-uuid",
  "patientId": "d4e5f6a7-b8c9-0123-def0-234567890123",
  "doctorId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "dateTime": "YYYY-MM-DDT08:00:00",
  "status": "SCHEDULED"
}
```

Validaciones de negocio:

- El paciente y el médico deben existir; si no existen, retorna `404`.
- `dateTime` debe estar alineado a una franja de 30 minutos.
- Horario laboral: lunes a viernes de `08:00` a `18:00`, último inicio válido `17:30`.
- Sábado de `08:00` a `13:00`, último inicio válido `12:30`.
- Domingo y festivos no hay atención.
- El calendario de festivos asumido para el MVP es Colombia, calculado por año. Incluye festivos fijos y festivos móviles derivados del Domingo de Pascua.
- Si el médico ya tiene una cita `SCHEDULED` en la franja, retorna `409`.
- Si el mismo paciente ya tiene cita `SCHEDULED` con el mismo médico en la franja, retorna `409`.
- Si el paciente tiene 3 o más penalizaciones en los últimos 30 días, retorna `409`.

Decisión de negocio:

- RN-04 se implementa de forma literal: el conflicto de paciente aplica para mismo paciente + mismo médico + misma franja. El mismo paciente con otro médico en la misma franja no se bloquea por esta regla.
- Para el MVP, los festivos se calculan localmente usando el calendario de Colombia. Los festivos móviles se derivan del Domingo de Pascua mediante un cálculo interno, porque cambian cada año. En un producto real convendría externalizar esta fuente mediante una tabla administrable o una integración sincronizada con una API de festivos, evitando depender de una API externa en tiempo real durante la reserva.

Ejemplo con curl:

```bash
APPOINTMENT_DATE_TIME="YYYY-MM-DDT08:00:00"

curl -i -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": \"d4e5f6a7-b8c9-0123-def0-234567890123\",
    \"doctorId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",
    \"dateTime\": \"${APPOINTMENT_DATE_TIME}\"
  }"
```

### Consultar Disponibilidad de Médico

```http
GET /api/doctors/{id}/availability?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
```

Response `200`:

```json
[
  {
    "date": "2026-07-01",
    "slots": [
      {
        "start": "08:00:00",
        "end": "08:30:00"
      },
      {
        "start": "08:30:00",
        "end": "09:00:00"
      }
    ]
  }
]
```

Reglas aplicadas:

- El médico debe existir; si no existe, retorna `404`.
- `startDate` y `endDate` son obligatorios.
- `startDate` debe ser menor o igual a `endDate`; si no, retorna `400`.
- Los días laborales generan franjas de 30 minutos entre `08:00` y `18:00`.
- Los sábados generan franjas de 30 minutos entre `08:00` y `13:00`.
- Domingos y festivos no aportan días ni franjas.
- Las citas `SCHEDULED` del médico se excluyen de la respuesta.
- Citas `CANCELLED` o de otros médicos no bloquean disponibilidad.

Ejemplo con curl:

```bash
curl -i "http://localhost:8080/api/doctors/a1b2c3d4-e5f6-7890-abcd-ef1234567890/availability?startDate=2026-07-01&endDate=2026-07-04"
```

### Cancelar Cita

```http
DELETE /api/appointments/{id}
```

Response `200`:

```json
{
  "id": "appointment-uuid",
  "status": "CANCELLED",
  "cancelledAt": "2026-07-01T07:15:00",
  "penaltyApplied": false
}
```

Reglas aplicadas:

- La cita debe existir; si no existe, retorna `404`.
- Solo se pueden cancelar citas `SCHEDULED`.
- Una cita ya `CANCELLED` retorna `409` con `appointment-state-conflict`.
- Si faltan menos de 2 horas para la cita, se registra una penalización y `penaltyApplied` retorna `true`.
- Si faltan 2 horas o más, no se registra penalización y `penaltyApplied` retorna `false`.
- El caso borde de exactamente 2 horas se interpreta como no penalizable.

Ejemplo con curl:

```bash
curl -i -X DELETE "http://localhost:8080/api/appointments/appointment-uuid"
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

Las validaciones de entrada se manejan con Bean Validation en los request DTOs y un handler global con `ProblemDetail`. Los parámetros requeridos de query/path y los errores de conversión de tipo también responden con el mismo contrato de validación.

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

Ejemplo `409` por conflicto de franja:

```json
{
  "type": "https://medisalud.com/errors/slot-conflict",
  "title": "Slot conflict",
  "status": 409,
  "detail": "Doctor already has an appointment at YYYY-MM-DDT08:00",
  "instance": "/api/appointments"
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
./gradlew gm --name=AvailabilityDay
./gradlew gm --name=AvailabilitySlot
./gradlew gm --name=AppointmentCancellation
./gradlew guc --name=CreateDoctor
./gradlew guc --name=CreatePatient
./gradlew guc --name=CreateAppointment
./gradlew guc --name=GetDoctorAvailability
./gradlew guc --name=CancelAppointment
./gradlew gep --type=restmvc
./gradlew gda --type=jpa
```

## Notas de Desarrollo

- `SPEC.md` es una nota local de trabajo y está ignorada por Git.
- Los archivos `medisalud.env.*` reales están ignorados por Git.
- `docker-compose.yml` y `medisalud.env.local.example` sí se versionan.
- Cada cambio importante de ejecución, perfiles, endpoints, arquitectura o decisiones técnicas debe reflejarse en este README.
