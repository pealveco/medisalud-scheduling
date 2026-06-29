package co.com.medisalud.usecase.listappointments;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.common.exceptions.InvalidDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListAppointmentsUseCaseTest {

    private static final UUID DOCTOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_DOCTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PATIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_PATIENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Test
    void shouldReturnAllAppointmentsWhenNoFiltersAreProvided() {
        TestContext context = TestContext.withDefaults();

        List<Appointment> result = context.useCase.listAppointments(emptyCriteria());

        assertEquals(4, result.size());
    }

    @Test
    void shouldFilterAppointmentsByDoctorId() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder().doctorId(DOCTOR_ID).build();

        List<Appointment> result = context.useCase.listAppointments(criteria);

        assertEquals(3, result.size());
        assertEquals(DOCTOR_ID, result.get(0).getDoctorId());
        assertEquals(DOCTOR_ID, result.get(1).getDoctorId());
        assertEquals(DOCTOR_ID, result.get(2).getDoctorId());
    }

    @Test
    void shouldFilterAppointmentsByPatientId() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder().patientId(PATIENT_ID).build();

        List<Appointment> result = context.useCase.listAppointments(criteria);

        assertEquals(2, result.size());
        assertEquals(PATIENT_ID, result.get(0).getPatientId());
        assertEquals(PATIENT_ID, result.get(1).getPatientId());
    }

    @Test
    void shouldFilterAppointmentsByStatus() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder().status(AppointmentStatus.CANCELLED).build();

        List<Appointment> result = context.useCase.listAppointments(criteria);

        assertEquals(1, result.size());
        assertEquals(AppointmentStatus.CANCELLED, result.get(0).getStatus());
    }

    @Test
    void shouldFilterAppointmentsByDateRange() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder()
                .startDate(LocalDateTime.of(2026, 7, 2, 0, 0))
                .endDate(LocalDateTime.of(2026, 7, 3, 23, 59))
                .build();

        List<Appointment> result = context.useCase.listAppointments(criteria);

        assertEquals(2, result.size());
        assertEquals(LocalDateTime.of(2026, 7, 2, 9, 0), result.get(0).getDateTime());
        assertEquals(LocalDateTime.of(2026, 7, 3, 10, 0), result.get(1).getDateTime());
    }

    @Test
    void shouldFilterAppointmentsByStatusAndDateRangeIntersection() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder()
                .status(AppointmentStatus.SCHEDULED)
                .startDate(LocalDateTime.of(2026, 7, 2, 0, 0))
                .endDate(LocalDateTime.of(2026, 7, 4, 23, 59))
                .build();

        List<Appointment> result = context.useCase.listAppointments(criteria);

        assertEquals(2, result.size());
        assertEquals(AppointmentStatus.SCHEDULED, result.get(0).getStatus());
        assertEquals(AppointmentStatus.SCHEDULED, result.get(1).getStatus());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        TestContext context = TestContext.withDefaults();
        AppointmentSearchCriteria criteria = emptyCriteria().toBuilder()
                .startDate(LocalDateTime.of(2026, 7, 5, 0, 0))
                .endDate(LocalDateTime.of(2026, 7, 4, 23, 59))
                .build();

        assertThrows(InvalidDateRangeException.class, () -> context.useCase.listAppointments(criteria));
    }

    private static AppointmentSearchCriteria emptyCriteria() {
        return AppointmentSearchCriteria.builder().build();
    }

    private static Appointment appointment(
            UUID id,
            UUID doctorId,
            UUID patientId,
            LocalDateTime dateTime,
            AppointmentStatus status) {
        return Appointment.builder()
                .id(id)
                .doctorId(doctorId)
                .patientId(patientId)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    private static class TestContext {

        private final InMemoryAppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        private final ListAppointmentsUseCase useCase = new ListAppointmentsUseCase(appointmentRepository);

        private static TestContext withDefaults() {
            TestContext context = new TestContext();
            context.appointmentRepository.save(appointment(
                    UUID.randomUUID(),
                    DOCTOR_ID,
                    PATIENT_ID,
                    LocalDateTime.of(2026, 7, 1, 8, 0),
                    AppointmentStatus.SCHEDULED
            ));
            context.appointmentRepository.save(appointment(
                    UUID.randomUUID(),
                    DOCTOR_ID,
                    OTHER_PATIENT_ID,
                    LocalDateTime.of(2026, 7, 2, 9, 0),
                    AppointmentStatus.CANCELLED
            ));
            context.appointmentRepository.save(appointment(
                    UUID.randomUUID(),
                    OTHER_DOCTOR_ID,
                    PATIENT_ID,
                    LocalDateTime.of(2026, 7, 3, 10, 0),
                    AppointmentStatus.SCHEDULED
            ));
            context.appointmentRepository.save(appointment(
                    UUID.randomUUID(),
                    DOCTOR_ID,
                    OTHER_PATIENT_ID,
                    LocalDateTime.of(2026, 7, 4, 11, 0),
                    AppointmentStatus.SCHEDULED
            ));
            return context;
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
            return false;
        }

        @Override
        public boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
                UUID patientId,
                UUID doctorId,
                LocalDateTime dateTime) {
            return false;
        }

        @Override
        public List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
                UUID doctorId,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime) {
            return List.of();
        }

        @Override
        public List<Appointment> findByCriteria(AppointmentSearchCriteria criteria) {
            return appointmentsById.values().stream()
                    .filter(appointment -> criteria.getDoctorId() == null
                            || criteria.getDoctorId().equals(appointment.getDoctorId()))
                    .filter(appointment -> criteria.getPatientId() == null
                            || criteria.getPatientId().equals(appointment.getPatientId()))
                    .filter(appointment -> criteria.getStatus() == null
                            || criteria.getStatus() == appointment.getStatus())
                    .filter(appointment -> criteria.getStartDate() == null
                            || !appointment.getDateTime().isBefore(criteria.getStartDate()))
                    .filter(appointment -> criteria.getEndDate() == null
                            || !appointment.getDateTime().isAfter(criteria.getEndDate()))
                    .sorted(Comparator.comparing(Appointment::getDateTime))
                    .toList();
        }
    }
}
