-- ============================================================
-- Datos iniciales — perfil: local (solo PostgreSQL)
--
-- Spring Boot ejecuta este archivo automáticamente al arrancar
-- con el perfil local porque application-local.yml define:
--   spring.sql.init.platform: local
--
-- Requiere en application-local.yml:
--   spring.jpa.defer-datasource-initialization: true  (Hibernate
--     crea el esquema antes de que corra este SQL)
--   spring.sql.init.mode: always  (PostgreSQL no es BD embebida;
--     sin esta propiedad Spring Boot nunca ejecuta el archivo)
--
-- Actualmente están activos los seeds de doctors, patients,
-- appointments y penalties.
--
-- Doctors y patients usan ON CONFLICT DO NOTHING para no sobreescribir
-- datos editados localmente. Appointments y penalties usan ON CONFLICT
-- DO UPDATE para refrescar fechas relativas a NOW() en cada arranque;
-- así los escenarios de demo no dependen del día en que se revise la prueba.
--
-- Los nombres de columna siguen la estrategia de nomenclatura de
-- Spring (SpringPhysicalNamingStrategy): camelCase del campo Java
-- se convierte a snake_case en la BD (ej. fullName → full_name).
--
-- IMPORTANTE: el campo status de AppointmentData debe anotarse
-- con @Enumerated(EnumType.STRING) para que los valores guardados
-- coincidan con los literales usados aquí ('SCHEDULED','CANCELLED').
-- ============================================================

-- ---------------------------------------------------------------
-- DOCTORS
-- ---------------------------------------------------------------
INSERT INTO doctors (id, full_name, specialty, phone, email)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Carlos Andres Mejia',   'Cardiology',       '3001234567', 'carlos.mejia@medisalud.com'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Maria Fernanda Ospina', 'Pediatrics',       '3012345678', 'maria.ospina@medisalud.com'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Juan David Vargas',     'General Medicine', '3023456789', 'juan.vargas@medisalud.com')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- PATIENTS
--   mateo.restrepo  → clean record, no penalties
--   laura.jimenez   → 2 penalties in last 30 days (approaching block)
--   ricardo.morales → 3 penalties in last 30 days → BLOCKED (RN-05)
-- ---------------------------------------------------------------
INSERT INTO patients (id, full_name, document_id, phone, email, birth_date)
VALUES
    ('d4e5f6a7-b8c9-0123-def0-234567890123', 'Mateo Restrepo Gomez',  '10234567890', '3034567890', 'mateo.restrepo@email.com',  '1990-03-15'),
    ('e5f6a7b8-c9d0-1234-ef01-345678901234', 'Laura Jimenez Herrera', '20234567890', '3045678901', 'laura.jimenez@email.com',   '1985-07-22'),
    ('f6a7b8c9-d0e1-2345-f012-456789012345', 'Ricardo Morales Pena',  '30234567890', '3056789012', 'ricardo.morales@email.com', '1978-11-08')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- APPOINTMENTS
-- ---------------------------------------------------------------

-- Mateo: one upcoming SCHEDULED appointment (5 days from now at 08:00)
INSERT INTO appointments (id, patient_id, doctor_id, date_time, status, cancelled_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'd4e5f6a7-b8c9-0123-def0-234567890123',
     'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
     date_trunc('day', NOW() + INTERVAL '5 days') + TIME '08:00:00',
     'SCHEDULED',
     NULL)
ON CONFLICT (id) DO UPDATE SET
    patient_id = EXCLUDED.patient_id,
    doctor_id = EXCLUDED.doctor_id,
    date_time = EXCLUDED.date_time,
    status = EXCLUDED.status,
    cancelled_at = EXCLUDED.cancelled_at;

-- Laura: 2 CANCELLED appointments that triggered penalties.
-- Each was cancelled 90 min before the appointment (< 2h → penalty per RN-05).
INSERT INTO appointments (id, patient_id, doctor_id, date_time, status, cancelled_at)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'e5f6a7b8-c9d0-1234-ef01-345678901234',
     'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
     date_trunc('day', NOW() - INTERVAL '20 days') + TIME '10:00:00',
     'CANCELLED',
     date_trunc('day', NOW() - INTERVAL '20 days') + TIME '08:30:00'),

    ('cccccccc-cccc-cccc-cccc-cccccccccccc',
     'e5f6a7b8-c9d0-1234-ef01-345678901234',
     'b2c3d4e5-f6a7-8901-bcde-f12345678901',
     date_trunc('day', NOW() - INTERVAL '10 days') + TIME '10:00:00',
     'CANCELLED',
     date_trunc('day', NOW() - INTERVAL '10 days') + TIME '08:30:00')
ON CONFLICT (id) DO UPDATE SET
    patient_id = EXCLUDED.patient_id,
    doctor_id = EXCLUDED.doctor_id,
    date_time = EXCLUDED.date_time,
    status = EXCLUDED.status,
    cancelled_at = EXCLUDED.cancelled_at;

-- Ricardo: 3 CANCELLED appointments that triggered penalties.
-- Also cancelled 90 min before appointment → all 3 within last 30 days → BLOCKED.
INSERT INTO appointments (id, patient_id, doctor_id, date_time, status, cancelled_at)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddddd',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
     date_trunc('day', NOW() - INTERVAL '25 days') + TIME '10:00:00',
     'CANCELLED',
     date_trunc('day', NOW() - INTERVAL '25 days') + TIME '08:30:00'),

    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'b2c3d4e5-f6a7-8901-bcde-f12345678901',
     date_trunc('day', NOW() - INTERVAL '15 days') + TIME '10:00:00',
     'CANCELLED',
     date_trunc('day', NOW() - INTERVAL '15 days') + TIME '08:30:00'),

    ('ffffffff-ffff-ffff-ffff-ffffffffffff',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'c3d4e5f6-a7b8-9012-cdef-123456789012',
     date_trunc('day', NOW() - INTERVAL '5 days') + TIME '10:00:00',
     'CANCELLED',
     date_trunc('day', NOW() - INTERVAL '5 days') + TIME '08:30:00')
ON CONFLICT (id) DO UPDATE SET
    patient_id = EXCLUDED.patient_id,
    doctor_id = EXCLUDED.doctor_id,
    date_time = EXCLUDED.date_time,
    status = EXCLUDED.status,
    cancelled_at = EXCLUDED.cancelled_at;

-- ---------------------------------------------------------------
-- PENALTIES
-- created_at matches the cancelled_at of the originating appointment.
-- ---------------------------------------------------------------

-- Laura: 2 penalties (2 of 3 threshold — next late cancellation will block her)
INSERT INTO penalties (id, patient_id, appointment_id, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111',
     'e5f6a7b8-c9d0-1234-ef01-345678901234',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     date_trunc('day', NOW() - INTERVAL '20 days') + TIME '08:30:00'),

    ('22222222-2222-2222-2222-222222222222',
     'e5f6a7b8-c9d0-1234-ef01-345678901234',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     date_trunc('day', NOW() - INTERVAL '10 days') + TIME '08:30:00')
ON CONFLICT (id) DO UPDATE SET
    patient_id = EXCLUDED.patient_id,
    appointment_id = EXCLUDED.appointment_id,
    created_at = EXCLUDED.created_at;

-- Ricardo: 3 penalties in last 30 days → any new booking returns 409 PatientBlocked
INSERT INTO penalties (id, patient_id, appointment_id, created_at)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'dddddddd-dddd-dddd-dddd-dddddddddddd',
     date_trunc('day', NOW() - INTERVAL '25 days') + TIME '08:30:00'),

    ('44444444-4444-4444-4444-444444444444',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
     date_trunc('day', NOW() - INTERVAL '15 days') + TIME '08:30:00'),

    ('55555555-5555-5555-5555-555555555555',
     'f6a7b8c9-d0e1-2345-f012-456789012345',
     'ffffffff-ffff-ffff-ffff-ffffffffffff',
     date_trunc('day', NOW() - INTERVAL '5 days') + TIME '08:30:00')
ON CONFLICT (id) DO UPDATE SET
    patient_id = EXCLUDED.patient_id,
    appointment_id = EXCLUDED.appointment_id,
    created_at = EXCLUDED.created_at;
