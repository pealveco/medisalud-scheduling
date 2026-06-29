package co.com.medisalud.usecase.createappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.InvalidAppointmentSlotException;
import co.com.medisalud.model.appointment.exceptions.OutsideWorkingHoursException;
import co.com.medisalud.model.appointment.exceptions.PatientBlockedException;
import co.com.medisalud.model.appointment.exceptions.SlotConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateAppointmentUseCaseTest {

    private static final UUID DOCTOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PATIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTHER_PATIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void shouldCreateScheduledAppointmentWhenSlotIsAvailable() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 0));

        Appointment result = context.useCase.createAppointment(appointment);

        assertNotNull(result.getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertNull(result.getCancelledAt());
        assertEquals(PATIENT_ID, result.getPatientId());
        assertEquals(DOCTOR_ID, result.getDoctorId());
    }

    @Test
    void shouldRejectWhenPatientDoesNotExist() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(UUID.randomUUID(), DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 0));

        assertThrows(ResourceNotFoundException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenDoctorDoesNotExist() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, UUID.randomUUID(), LocalDateTime.of(2026, 7, 1, 8, 0));

        assertThrows(ResourceNotFoundException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenDateTimeIsNotAlignedToThirtyMinutes() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 15));

        assertThrows(InvalidAppointmentSlotException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenWeekdaySlotStartsAtClosingTime() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 18, 0));

        assertThrows(OutsideWorkingHoursException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenSaturdaySlotStartsAtClosingTime() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 4, 13, 0));

        assertThrows(OutsideWorkingHoursException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenDateIsSunday() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, nextDateTime(DayOfWeek.SUNDAY, LocalTime.of(10, 0)));

        assertThrows(OutsideWorkingHoursException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenDateIsColombianHoliday() {
        TestContext context = TestContext.withDefaults();
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2027, 1, 1, 10, 0));

        assertThrows(OutsideWorkingHoursException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenPatientBirthDateIsFuture() {
        TestContext context = TestContext.withDefaults();
        context.patientRepository.save(patient(PATIENT_ID).toBuilder().birthDate(LocalDate.now().plusDays(1)).build());
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 0));

        assertThrows(InvalidAppointmentSlotException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldAllowPatientWithoutBirthDate() {
        TestContext context = TestContext.withDefaults();
        context.patientRepository.save(patient(PATIENT_ID).toBuilder().birthDate(null).build());
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 0));

        Appointment result = context.useCase.createAppointment(appointment);

        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void shouldRejectWhenDoctorAlreadyHasScheduledAppointmentAtSlot() {
        TestContext context = TestContext.withDefaults();
        LocalDateTime dateTime = LocalDateTime.of(2026, 7, 1, 8, 0);
        context.appointmentRepository.save(appointment(OTHER_PATIENT_ID, DOCTOR_ID, dateTime)
                .toBuilder()
                .id(UUID.randomUUID())
                .status(AppointmentStatus.SCHEDULED)
                .build());
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, dateTime);

        assertThrows(SlotConflictException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenPatientAlreadyHasAppointmentWithSameDoctorAtSlot() {
        TestContext context = TestContext.withDefaults();
        LocalDateTime dateTime = LocalDateTime.of(2026, 7, 1, 8, 0);
        context.appointmentRepository.save(appointment(PATIENT_ID, DOCTOR_ID, dateTime)
                .toBuilder()
                .id(UUID.randomUUID())
                .status(AppointmentStatus.SCHEDULED)
                .build());
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, dateTime);

        assertThrows(SlotConflictException.class, () -> context.useCase.createAppointment(appointment));
    }

    @Test
    void shouldRejectWhenPatientHasThreeRecentPenalties() {
        TestContext context = TestContext.withDefaults();
        context.penaltyRepository.penaltyCount = 3;
        Appointment appointment = appointment(PATIENT_ID, DOCTOR_ID, LocalDateTime.of(2026, 7, 1, 8, 0));

        assertThrows(PatientBlockedException.class, () -> context.useCase.createAppointment(appointment));
    }

    private static Appointment appointment(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        return Appointment.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .build();
    }

    private static Doctor doctor(UUID id) {
        return Doctor.builder()
                .id(id)
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

    private static LocalDateTime nextDateTime(DayOfWeek dayOfWeek, LocalTime time) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return LocalDateTime.of(date, time);
    }

    private static class TestContext {

        private final InMemoryDoctorRepository doctorRepository = new InMemoryDoctorRepository();
        private final InMemoryPatientRepository patientRepository = new InMemoryPatientRepository();
        private final InMemoryAppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        private final InMemoryPenaltyRepository penaltyRepository = new InMemoryPenaltyRepository();
        private final CreateAppointmentUseCase useCase = new CreateAppointmentUseCase(
                appointmentRepository,
                doctorRepository,
                patientRepository,
                penaltyRepository
        );

        private static TestContext withDefaults() {
            TestContext context = new TestContext();
            context.doctorRepository.save(doctor(DOCTOR_ID));
            context.patientRepository.save(patient(PATIENT_ID));
            context.patientRepository.save(patient(OTHER_PATIENT_ID).toBuilder().documentId("987654321").build());
            return context;
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

    private static class InMemoryAppointmentRepository implements AppointmentRepository {

        private final Map<UUID, Appointment> appointmentsById = new HashMap<>();

        @Override
        public Appointment save(Appointment appointment) {
            appointmentsById.put(appointment.getId(), appointment);
            return appointment;
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
        public java.util.List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
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
    }

    private static class InMemoryPenaltyRepository implements PenaltyRepository {

        private long penaltyCount;

        @Override
        public Penalty save(Penalty penalty) {
            return penalty;
        }

        @Override
        public long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime) {
            return penaltyCount;
        }
    }
}
