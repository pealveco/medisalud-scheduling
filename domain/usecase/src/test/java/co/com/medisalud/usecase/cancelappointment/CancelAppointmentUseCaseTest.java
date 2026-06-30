package co.com.medisalud.usecase.cancelappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.AppointmentStateConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentcancellation.AppointmentCancellation;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelAppointmentUseCaseTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PATIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DOCTOR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 1, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    void shouldCancelScheduledAppointmentWithoutPenaltyWhenCancellationIsAtLeastTwoHoursBefore() {
        TestContext context = TestContext.withAppointment(NOW.plusHours(3), AppointmentStatus.SCHEDULED);

        AppointmentCancellation result = context.useCase.cancelAppointment(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CANCELLED, result.getAppointment().getStatus());
        assertEquals(NOW, result.getAppointment().getCancelledAt());
        assertFalse(result.isPenaltyApplied());
        assertEquals(0, context.penaltyRepository.penalties.size());
    }

    @Test
    void shouldNotApplyPenaltyWhenCancellationIsExactlyTwoHoursBefore() {
        TestContext context = TestContext.withAppointment(NOW.plusHours(2), AppointmentStatus.SCHEDULED);

        AppointmentCancellation result = context.useCase.cancelAppointment(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CANCELLED, result.getAppointment().getStatus());
        assertEquals(NOW, result.getAppointment().getCancelledAt());
        assertFalse(result.isPenaltyApplied());
        assertEquals(0, context.penaltyRepository.penalties.size());
    }

    @Test
    void shouldCancelScheduledAppointmentWithPenaltyWhenCancellationIsLate() {
        TestContext context = TestContext.withAppointment(NOW.plusMinutes(90), AppointmentStatus.SCHEDULED);

        AppointmentCancellation result = context.useCase.cancelAppointment(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CANCELLED, result.getAppointment().getStatus());
        assertEquals(NOW, result.getAppointment().getCancelledAt());
        assertTrue(result.isPenaltyApplied());
        assertEquals(1, context.penaltyRepository.penalties.size());
        assertEquals(PATIENT_ID, context.penaltyRepository.penalties.get(0).getPatientId());
        assertEquals(APPOINTMENT_ID, context.penaltyRepository.penalties.get(0).getAppointmentId());
        assertEquals(NOW, context.penaltyRepository.penalties.get(0).getCreatedAt());
    }

    @Test
    void shouldRejectWhenAppointmentDoesNotExist() {
        TestContext context = new TestContext();

        assertThrows(ResourceNotFoundException.class, () -> context.useCase.cancelAppointment(APPOINTMENT_ID));
    }

    @Test
    void shouldRejectWhenAppointmentIsAlreadyCancelled() {
        TestContext context = TestContext.withAppointment(NOW.plusHours(3), AppointmentStatus.CANCELLED);

        assertThrows(AppointmentStateConflictException.class, () -> context.useCase.cancelAppointment(APPOINTMENT_ID));
    }

    private static Appointment appointment(LocalDateTime dateTime, AppointmentStatus status) {
        return Appointment.builder()
                .id(APPOINTMENT_ID)
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    private static class TestContext {

        private final InMemoryAppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        private final InMemoryPenaltyRepository penaltyRepository = new InMemoryPenaltyRepository();
        private final CancelAppointmentUseCase useCase = new CancelAppointmentUseCase(
                appointmentRepository,
                penaltyRepository,
                FIXED_CLOCK
        );

        private static TestContext withAppointment(LocalDateTime dateTime, AppointmentStatus status) {
            TestContext context = new TestContext();
            context.appointmentRepository.save(appointment(dateTime, status));
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
            return List.of();
        }
    }

    private static class InMemoryPenaltyRepository implements PenaltyRepository {

        private final List<Penalty> penalties = new java.util.ArrayList<>();

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
