package co.com.medisalud.usecase.rescheduleappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.SlotConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentreschedule.AppointmentReschedule;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import co.com.medisalud.usecase.cancelappointment.CancelAppointmentUseCase;
import co.com.medisalud.usecase.createappointment.CreateAppointmentUseCase;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RescheduleAppointmentUseCaseTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DOCTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PATIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_PATIENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 1, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    void shouldCancelOriginalAppointmentAndCreateNewAppointmentWhenNewSlotIsAvailable() {
        TestContext context = TestContext.withDefaults();
        context.appointmentRepository.save(appointment(APPOINTMENT_ID, PATIENT_ID, DOCTOR_ID,
                LocalDateTime.of(2026, 7, 2, 8, 0), AppointmentStatus.SCHEDULED));

        AppointmentReschedule result = context.useCase.rescheduleAppointment(
                APPOINTMENT_ID,
                LocalDateTime.of(2026, 7, 2, 9, 0)
        );

        assertEquals(AppointmentStatus.CANCELLED, result.getOriginalAppointment().getStatus());
        assertEquals(AppointmentStatus.SCHEDULED, result.getNewAppointment().getStatus());
        assertEquals(PATIENT_ID, result.getNewAppointment().getPatientId());
        assertEquals(DOCTOR_ID, result.getNewAppointment().getDoctorId());
        assertEquals(LocalDateTime.of(2026, 7, 2, 9, 0), result.getNewAppointment().getDateTime());
        assertFalse(result.isPenaltyApplied());
        assertEquals(AppointmentStatus.CANCELLED, context.appointmentRepository.findById(APPOINTMENT_ID).getStatus());
    }

    @Test
    void shouldKeepOriginalAppointmentScheduledWhenNewSlotIsOccupied() {
        TestContext context = TestContext.withDefaults();
        context.patientRepository.save(patient(OTHER_PATIENT_ID).toBuilder().documentId("987654321").build());
        context.appointmentRepository.save(appointment(APPOINTMENT_ID, PATIENT_ID, DOCTOR_ID,
                LocalDateTime.of(2026, 7, 2, 8, 0), AppointmentStatus.SCHEDULED));
        context.appointmentRepository.save(appointment(UUID.randomUUID(), OTHER_PATIENT_ID, DOCTOR_ID,
                LocalDateTime.of(2026, 7, 2, 9, 0), AppointmentStatus.SCHEDULED));

        assertThrows(SlotConflictException.class, () -> context.useCase.rescheduleAppointment(
                APPOINTMENT_ID,
                LocalDateTime.of(2026, 7, 2, 9, 0)
        ));

        assertEquals(AppointmentStatus.SCHEDULED, context.appointmentRepository.findById(APPOINTMENT_ID).getStatus());
        assertEquals(2, context.appointmentRepository.appointmentsById.size());
    }

    @Test
    void shouldApplyPenaltyWhenOriginalAppointmentIsRescheduledLate() {
        TestContext context = TestContext.withDefaults();
        context.appointmentRepository.save(appointment(APPOINTMENT_ID, PATIENT_ID, DOCTOR_ID,
                NOW.plusMinutes(90), AppointmentStatus.SCHEDULED));

        AppointmentReschedule result = context.useCase.rescheduleAppointment(
                APPOINTMENT_ID,
                NOW.plusHours(3)
        );

        assertEquals(AppointmentStatus.CANCELLED, result.getOriginalAppointment().getStatus());
        assertEquals(AppointmentStatus.SCHEDULED, result.getNewAppointment().getStatus());
        assertTrue(result.isPenaltyApplied());
        assertEquals(1, context.penaltyRepository.penalties.size());
        assertEquals(PATIENT_ID, context.penaltyRepository.penalties.get(0).getPatientId());
        assertEquals(APPOINTMENT_ID, context.penaltyRepository.penalties.get(0).getAppointmentId());
        assertEquals(NOW, context.penaltyRepository.penalties.get(0).getCreatedAt());
    }

    private static Appointment appointment(
            UUID appointmentId,
            UUID patientId,
            UUID doctorId,
            LocalDateTime dateTime,
            AppointmentStatus status) {
        return Appointment.builder()
                .id(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    private static Doctor doctor() {
        return Doctor.builder()
                .id(DOCTOR_ID)
                .fullName("Carlos Mejia")
                .specialty("Cardiology")
                .build();
    }

    private static Patient patient(UUID id) {
        return Patient.builder()
                .id(id)
                .fullName("Mateo Perez")
                .documentId("123456789")
                .phone("3107654321")
                .email("mateo.perez@medisalud.com")
                .birthDate(LocalDate.of(1990, 5, 12))
                .build();
    }

    private static class TestContext {

        private final InMemoryAppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        private final InMemoryDoctorRepository doctorRepository = new InMemoryDoctorRepository();
        private final InMemoryPatientRepository patientRepository = new InMemoryPatientRepository();
        private final InMemoryPenaltyRepository penaltyRepository = new InMemoryPenaltyRepository();
        private final CreateAppointmentUseCase createAppointmentUseCase = new CreateAppointmentUseCase(
                appointmentRepository,
                doctorRepository,
                patientRepository,
                penaltyRepository,
                FIXED_CLOCK
        );
        private final CancelAppointmentUseCase cancelAppointmentUseCase = new CancelAppointmentUseCase(
                appointmentRepository,
                penaltyRepository,
                FIXED_CLOCK
        );
        private final RescheduleAppointmentUseCase useCase = new RescheduleAppointmentUseCase(
                appointmentRepository,
                createAppointmentUseCase,
                cancelAppointmentUseCase
        );

        private static TestContext withDefaults() {
            TestContext context = new TestContext();
            context.doctorRepository.save(doctor());
            context.patientRepository.save(patient(PATIENT_ID));
            return context;
        }
    }

    private static class InMemoryAppointmentRepository implements AppointmentRepository {

        private final Map<UUID, Appointment> appointmentsById = new HashMap<>();

        @Override
        public Appointment save(Appointment appointment) {
            Appointment appointmentToSave = appointment.toBuilder()
                    .id(appointment.getId() == null ? UUID.randomUUID() : appointment.getId())
                    .build();
            appointmentsById.put(appointmentToSave.getId(), appointmentToSave);
            return appointmentToSave;
        }

        @Override
        public Appointment findById(UUID id) {
            return appointmentsById.get(id);
        }

        @Override
        public boolean existsScheduledByDoctorIdAndDateTime(UUID doctorId, LocalDateTime dateTime) {
            return appointmentsById.values().stream()
                    .anyMatch(appointment -> doctorId.equals(appointment.getDoctorId())
                            && dateTime.equals(appointment.getDateTime())
                            && AppointmentStatus.SCHEDULED == appointment.getStatus());
        }

        @Override
        public boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
                UUID patientId,
                UUID doctorId,
                LocalDateTime dateTime) {
            return appointmentsById.values().stream()
                    .anyMatch(appointment -> patientId.equals(appointment.getPatientId())
                            && doctorId.equals(appointment.getDoctorId())
                            && dateTime.equals(appointment.getDateTime())
                            && AppointmentStatus.SCHEDULED == appointment.getStatus());
        }

        @Override
        public List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
                UUID doctorId,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime) {
            return appointmentsById.values().stream()
                    .filter(appointment -> doctorId.equals(appointment.getDoctorId()))
                    .filter(appointment -> AppointmentStatus.SCHEDULED == appointment.getStatus())
                    .filter(appointment -> !appointment.getDateTime().isBefore(startDateTime))
                    .filter(appointment -> appointment.getDateTime().isBefore(endDateTime))
                    .toList();
        }

        @Override
        public List<Appointment> findByCriteria(AppointmentSearchCriteria criteria) {
            return List.of();
        }
    }

    private static class InMemoryDoctorRepository implements DoctorRepository {

        private final Map<UUID, Doctor> doctorsById = new HashMap<>();

        @Override
        public Doctor save(Doctor doctor) {
            doctorsById.put(doctor.getId(), doctor);
            return doctor;
        }

        @Override
        public Doctor findById(UUID id) {
            return doctorsById.get(id);
        }
    }

    private static class InMemoryPatientRepository implements PatientRepository {

        private final Map<UUID, Patient> patientsById = new HashMap<>();

        @Override
        public Patient save(Patient patient) {
            patientsById.put(patient.getId(), patient);
            return patient;
        }

        @Override
        public Patient findById(UUID id) {
            return patientsById.get(id);
        }

        @Override
        public boolean existsByDocumentId(String documentId) {
            return patientsById.values().stream()
                    .anyMatch(patient -> documentId.equals(patient.getDocumentId()));
        }
    }

    private static class InMemoryPenaltyRepository implements PenaltyRepository {

        private final List<Penalty> penalties = new ArrayList<>();

        @Override
        public Penalty save(Penalty penalty) {
            penalties.add(penalty);
            return penalty;
        }

        @Override
        public long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime) {
            return penalties.stream()
                    .filter(penalty -> patientId.equals(penalty.getPatientId()))
                    .filter(penalty -> !penalty.getCreatedAt().isBefore(fromDateTime))
                    .count();
        }
    }
}
