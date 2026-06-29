package co.com.medisalud.usecase.getdoctoravailability;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.availabilityday.AvailabilityDay;
import co.com.medisalud.model.common.exceptions.InvalidDateRangeException;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetDoctorAvailabilityUseCaseTest {

    private static final UUID DOCTOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_DOCTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PATIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 7, 1);
    private static final LocalDate THURSDAY = LocalDate.of(2026, 7, 2);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 7, 4);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 7, 5);
    private static final LocalDate COLOMBIAN_HOLIDAY = LocalDate.of(2026, 7, 20);

    @Test
    void shouldReturnWeekdaySlotsExcludingScheduledAppointments() {
        TestContext context = TestContext.withDefaults();
        context.appointmentRepository.save(appointment(DOCTOR_ID, WEDNESDAY.atTime(8, 0), AppointmentStatus.SCHEDULED));
        context.appointmentRepository.save(appointment(DOCTOR_ID, WEDNESDAY.atTime(8, 30), AppointmentStatus.CANCELLED));
        context.appointmentRepository.save(appointment(OTHER_DOCTOR_ID, WEDNESDAY.atTime(9, 0), AppointmentStatus.SCHEDULED));

        List<AvailabilityDay> availability = context.useCase.getAvailability(DOCTOR_ID, WEDNESDAY, WEDNESDAY);

        assertEquals(1, availability.size());
        assertEquals(WEDNESDAY, availability.getFirst().getDate());
        assertEquals(19, availability.getFirst().getSlots().size());
        assertFalse(hasSlotStartingAt(availability.getFirst(), LocalTime.of(8, 0)));
        assertTrue(hasSlotStartingAt(availability.getFirst(), LocalTime.of(8, 30)));
        assertTrue(hasSlotStartingAt(availability.getFirst(), LocalTime.of(9, 0)));
    }

    @Test
    void shouldReturnFullWeekdaySlots() {
        TestContext context = TestContext.withDefaults();

        List<AvailabilityDay> availability = context.useCase.getAvailability(DOCTOR_ID, THURSDAY, THURSDAY);

        assertEquals(1, availability.size());
        assertEquals(20, availability.getFirst().getSlots().size());
        assertEquals(LocalTime.of(8, 0), availability.getFirst().getSlots().getFirst().getStart());
        assertEquals(LocalTime.of(17, 30), availability.getFirst().getSlots().getLast().getStart());
        assertEquals(LocalTime.of(18, 0), availability.getFirst().getSlots().getLast().getEnd());
    }

    @Test
    void shouldReturnSaturdayMorningSlotsOnly() {
        TestContext context = TestContext.withDefaults();

        List<AvailabilityDay> availability = context.useCase.getAvailability(DOCTOR_ID, SATURDAY, SATURDAY);

        assertEquals(1, availability.size());
        assertEquals(10, availability.getFirst().getSlots().size());
        assertEquals(LocalTime.of(8, 0), availability.getFirst().getSlots().getFirst().getStart());
        assertEquals(LocalTime.of(12, 30), availability.getFirst().getSlots().getLast().getStart());
        assertEquals(LocalTime.of(13, 0), availability.getFirst().getSlots().getLast().getEnd());
    }

    @Test
    void shouldSkipSunday() {
        TestContext context = TestContext.withDefaults();

        List<AvailabilityDay> availability = context.useCase.getAvailability(DOCTOR_ID, SUNDAY, SUNDAY);

        assertTrue(availability.isEmpty());
    }

    @Test
    void shouldSkipColombianHoliday() {
        TestContext context = TestContext.withDefaults();

        List<AvailabilityDay> availability = context.useCase.getAvailability(
                DOCTOR_ID,
                COLOMBIAN_HOLIDAY,
                COLOMBIAN_HOLIDAY
        );

        assertTrue(availability.isEmpty());
    }

    @Test
    void shouldRejectWhenDoctorDoesNotExist() {
        TestContext context = new TestContext();

        assertThrows(ResourceNotFoundException.class,
                () -> context.useCase.getAvailability(DOCTOR_ID, WEDNESDAY, WEDNESDAY));
    }

    @Test
    void shouldRejectWhenStartDateIsAfterEndDate() {
        TestContext context = TestContext.withDefaults();

        assertThrows(InvalidDateRangeException.class,
                () -> context.useCase.getAvailability(DOCTOR_ID, THURSDAY, WEDNESDAY));
    }

    private static boolean hasSlotStartingAt(AvailabilityDay availabilityDay, LocalTime start) {
        return availabilityDay.getSlots().stream()
                .anyMatch(slot -> start.equals(slot.getStart()));
    }

    private static Appointment appointment(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status) {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    private static Doctor doctor(UUID id) {
        return Doctor.builder()
                .id(id)
                .fullName("Carlos Mejia")
                .specialty("Cardiology")
                .build();
    }

    private static class TestContext {

        private final InMemoryAppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        private final InMemoryDoctorRepository doctorRepository = new InMemoryDoctorRepository();
        private final GetDoctorAvailabilityUseCase useCase = new GetDoctorAvailabilityUseCase(
                appointmentRepository,
                doctorRepository
        );

        private static TestContext withDefaults() {
            TestContext context = new TestContext();
            context.doctorRepository.save(doctor(DOCTOR_ID));
            context.doctorRepository.save(doctor(OTHER_DOCTOR_ID));
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
            return appointmentsById.values().stream()
                    .filter(appointment -> doctorId.equals(appointment.getDoctorId()))
                    .filter(appointment -> AppointmentStatus.SCHEDULED == appointment.getStatus())
                    .filter(appointment -> !appointment.getDateTime().isBefore(startDateTime))
                    .filter(appointment -> appointment.getDateTime().isBefore(endDateTime))
                    .toList();
        }
    }
}
