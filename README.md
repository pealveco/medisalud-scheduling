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
- `GET /api/appointments` funcional con filtros opcionales y combinables.
- `PUT /api/appointments/{id}/reschedule` funcional con atomicidad para reprogramación.
- Validaciones declarativas para registro de médicos, pacientes y reserva/cancelación de citas.
- Respuestas de validación con `ProblemDetail` y campo `errors`.
- Respuestas `400`, `404`, `409` y `500` con `ProblemDetail` para rangos inválidos, recursos inexistentes, duplicidades, conflictos de franja, estado inválido de cita, bloqueo de paciente y errores no controlados.
- PostgreSQL local con Docker Compose para perfil `local`.
- H2 en memoria para perfil `test`.
- README con ejecución local, tests, arquitectura, endpoints y decisiones principales.

## Stack

- Java 21
- Spring Boot 3.5.16
- Gradle
- JPA/Hibernate
- PostgreSQL para runtime local
- H2 para tests
- Docker Compose
- springdoc-openapi / Swagger UI
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

Justificación:

- Clean Architecture / Hexagonal separa reglas de negocio de infraestructura. Esto permite probar los use cases sin levantar Spring ni una base de datos real.
- El dominio se mantiene con modelos puros, sin anotaciones de persistencia ni dependencias de frameworks.
- Los adapters concentran detalles técnicos como JPA, queries, entidades y mapeo entity-dominio.
- El entry point REST se limita a validar DTOs, mapear request/response y delegar a casos de uso.
- Se usa Spring MVC imperativo con JPA/Hibernate porque el proyecto no requiere programación reactiva y la prueba pide explícitamente un backend imperativo con persistencia relacional.

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

Para despliegue en Render existe una plantilla separada:

```text
medisalud.env.render.example
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

## Documentación OpenAPI

Con la aplicación ejecutándose localmente, la documentación interactiva queda disponible en:

```text
http://localhost:8080/swagger-ui.html
```

La especificación OpenAPI en JSON queda disponible en:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI se genera con `springdoc-openapi` y documenta los endpoints, DTOs de request/response y códigos de respuesta principales. Esta documentación complementa los ejemplos del README.

## Ejecutar Tests

```bash
./gradlew test
```

Los tests usan el perfil `test` con H2 en memoria (`@ActiveProfiles("test")`).

La suite cubre 123 tests: 44 unitarios (casos de uso con mocks), 50 de integración
(`@SpringBootTest` + MockMvc sobre todos los endpoints) y 29 generados por el scaffold
de Bancolombia (ArchUnit, JPA, configuración).

También puedes validar la estructura del scaffold:

```bash
./gradlew validateStructure
```

## Integración Continua

El proyecto incluye GitHub Actions para ejecutar build y tests automáticamente.

Workflow:

```text
.github/workflows/ci.yml
```

Se ejecuta en:

- `push` a `develop` y `main`.
- `pull_request` hacia `develop` y `main`.

Comando ejecutado por CI:

```bash
./gradlew clean build --no-daemon --no-configuration-cache
```

Los tests corren con H2 en memoria, por lo que el pipeline no requiere PostgreSQL ni Docker Compose.

## Despliegue en Render

El despliegue en Render usa:

- PostgreSQL administrado por Render.
- Web Service construido con Docker desde el repositorio.
- GitHub Actions como disparador de despliegue mediante Deploy Hook.
- Despliegue solo desde `main`, después de que el workflow `CI` termina exitosamente.

Archivos relevantes:

```text
deployment/Dockerfile
.github/workflows/cd-render.yml
medisalud.env.render.example
```

El `Dockerfile` es multi-stage: primero compila el jar con Gradle y después ejecuta la aplicación con una imagen JRE liviana. Render inyecta la variable `PORT`; la aplicación la usa automáticamente cuando `SERVER_PORT` no está definido.

Pasos en Render:

1. Crear una base PostgreSQL administrada.
2. Crear un Web Service conectado al repositorio.
3. Configurar el runtime como Docker.
4. Usar `deployment/Dockerfile` como Dockerfile path.
5. Configurar las variables de entorno tomando como referencia `medisalud.env.render.example`.
6. Desactivar Auto Deploy en Render, porque el despliegue lo dispara GitHub Actions.
7. Crear un Deploy Hook en Render.
8. Guardar ese hook en GitHub como secret `RENDER_DEPLOY_HOOK_URL`.

Variables esperadas en Render:

```env
SPRING_PROFILES_ACTIVE=prod
APP_NAME=MediSalud-Scheduling
DB_URL=jdbc:postgresql://RENDER_DB_HOST:5432/medisalud
DB_USER=REPLACE_ME
DB_PASS=REPLACE_ME
DB_DRIVER=org.postgresql.Driver
DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect
JPA_DDL_AUTO=update
CORS_ALLOWED_ORIGINS=https://YOUR_RENDER_APP.onrender.com
MANAGEMENT_ENDPOINTS=health,prometheus
```

Nota: para este MVP se usa `JPA_DDL_AUTO=update` en Render porque aún no hay migraciones de base de datos. En producción real convendría usar Flyway o Liquibase y cambiar a `validate`.

El health check recomendado para Render es:

```text
/actuator/health
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
- RN-03 no persiste ni expone un campo `age`, porque la edad es un dato derivado de `birthDate`. Cuando `birthDate` está ausente, la regla se interpreta como edad efectiva `0` y el agendamiento se permite.
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

### Reprogramar Cita

```http
PUT /api/appointments/{id}/reschedule
```

Request:

```json
{
  "newDateTime": "2026-07-02T09:00:00"
}
```

Response `200`:

```json
{
  "id": "original-appointment-uuid",
  "patientId": "patient-uuid",
  "doctorId": "doctor-uuid",
  "dateTime": "2026-07-01T08:00:00",
  "status": "CANCELLED",
  "penaltyApplied": false,
  "newAppointment": {
    "id": "new-appointment-uuid",
    "patientId": "patient-uuid",
    "doctorId": "doctor-uuid",
    "dateTime": "2026-07-02T09:00:00",
    "status": "SCHEDULED"
  }
}
```

Reglas aplicadas:

- La cita original debe existir y estar en estado `SCHEDULED`.
- La nueva fecha debe cumplir horario laboral, alineación de franja, disponibilidad del médico y conflicto paciente-médico.
- Si la cancelación de la cita original ocurre con menos de 2 horas de antelación, se registra penalización y `penaltyApplied` retorna `true`.
- Si el nuevo horario está ocupado o es inválido, retorna el error correspondiente y la cita original permanece `SCHEDULED`.

Decisión técnica:

- Aunque RN-06 se expresa como regla de negocio, se expone como `PUT /api/appointments/{id}/reschedule` porque requiere atomicidad: cancelar la cita original y crear la nueva deben comportarse como una sola operación. El use case crea primero la nueva cita y solo después cancela la original; si la nueva reserva falla por validaciones de negocio, la cita original permanece `SCHEDULED`. Además, el endpoint se ejecuta con frontera transaccional Spring para revertir cualquier escritura parcial si ocurre un fallo durante la operación.

Ejemplo con curl:

```bash
curl -i -X PUT "http://localhost:8080/api/appointments/appointment-uuid/reschedule" \
  -H "Content-Type: application/json" \
  -d '{
    "newDateTime": "2026-07-02T09:00:00"
  }'
```

### Listar Citas

```http
GET /api/appointments?doctorId=&patientId=&status=&startDate=&endDate=
```

Todos los filtros son opcionales y combinables.

Response `200`:

```json
[
  {
    "id": "appointment-uuid",
    "patientId": "patient-uuid",
    "doctorId": "doctor-uuid",
    "dateTime": "2026-07-01T08:00:00",
    "status": "SCHEDULED"
  }
]
```

Filtros soportados:

- `doctorId`: UUID del médico.
- `patientId`: UUID del paciente.
- `status`: `SCHEDULED`, `CANCELLED` o `ATTENDED`.
- `startDate`: fecha/hora inicial inclusiva en formato ISO 8601, por ejemplo `2026-07-01T00:00:00`.
- `endDate`: fecha/hora final inclusiva en formato ISO 8601, por ejemplo `2026-07-31T23:59:59`.

Reglas aplicadas:

- Sin filtros retorna todas las citas.
- Cada filtro individual restringe el resultado.
- Los filtros combinados retornan la intersección.
- Si `startDate` es posterior a `endDate`, retorna `400`.

Ejemplos con curl:

```bash
curl -i "http://localhost:8080/api/appointments"

curl -i "http://localhost:8080/api/appointments?doctorId=a1b2c3d4-e5f6-7890-abcd-ef1234567890"

curl -i "http://localhost:8080/api/appointments?status=SCHEDULED&startDate=2026-07-01T00:00:00&endDate=2026-07-31T23:59:59"
```

## Decisiones y Supuestos

- **Arquitectura:** se usa Clean Architecture / Hexagonal mediante el scaffold de Bancolombia para separar dominio, casos de uso, entry points y driven adapters.
- **Backend imperativo:** se usa Spring MVC y JPA/Hibernate. No se usa WebFlux porque el proyecto no requiere flujos reactivos y la prueba define un backend imperativo.
- **Persistencia:** PostgreSQL es la base runtime local mediante Docker Compose; H2 se usa solo para tests automatizados con el perfil `test`.
- **Dominio limpio:** los modelos de dominio no tienen anotaciones JPA. Las entidades JPA viven en el driven adapter y se mapean hacia/desde dominio.
- **Festivos RN-01:** para el MVP se asume calendario de Colombia calculado localmente por año. En un producto real convendría una fuente administrable o sincronizada para festivos.
- **RN-04:** se interpreta de forma literal: el paciente no puede tener dos citas con el mismo médico en la misma franja. El mismo paciente con otro médico en la misma franja no se bloquea por esta regla.
- **RN-05:** exactamente 2 horas de antelación no penaliza; solo penaliza una cancelación con menos de 2 horas.
- **RN-06:** reprogramación se expone como endpoint propio porque necesita atomicidad. Si la nueva cita falla, la original permanece `SCHEDULED`.
- **Registro de médicos:** no se fuerza unicidad porque el enunciado no define un identificador natural obligatorio para médicos. En un caso real debería definirse un dato único como licencia o registro profesional.
- **Edad RN-03:** no se persiste un campo `age`; la edad es derivada de `birthDate`. Si `birthDate` está ausente, la regla se interpreta como edad efectiva `0`.
- **Formato de teléfono:** la validación acepta exclusivamente dígitos (`^\d{7,}$`), sin guiones ni espacios. Decisión tomada por el contexto colombiano: los celulares tienen 10 dígitos y los fijos tienen 7. Almacenar solo dígitos evita variantes de formato (`555-1001`, `(555) 1001`) que complican búsquedas y comparaciones en base de datos. Los ejemplos del enunciado con guiones (`555-1001`) son datos de referencia ilustrativos, no una especificación de formato.

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

Los errores no controlados se registran en logs y responden con `500` usando un mensaje genérico. Esto evita exponer detalles internos de implementación al cliente.

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

Ejemplo `500` por error no controlado:

```json
{
  "type": "https://medisalud.com/errors/internal-server-error",
  "title": "Internal server error",
  "status": 500,
  "detail": "Unexpected internal server error",
  "instance": "/api/doctors"
}
```

## Mejoras Futuras

- Agregar paginación y ordenamiento a `GET /api/appointments` mediante `page`, `size` y `sort`, porque el volumen histórico de citas puede crecer indefinidamente.
- Definir un rango máximo permitido para `GET /api/doctors/{id}/availability`, por ejemplo 31 días, para evitar consultas demasiado amplias sin paginar una respuesta que normalmente debe verse completa por rango.
