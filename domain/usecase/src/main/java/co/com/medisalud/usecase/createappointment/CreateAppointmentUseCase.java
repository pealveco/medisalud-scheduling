package co.com.medisalud.usecase.createappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.InvalidAppointmentSlotException;
import co.com.medisalud.model.appointment.exceptions.OutsideWorkingHoursException;
import co.com.medisalud.model.appointment.exceptions.PatientBlockedException;
import co.com.medisalud.model.appointment.exceptions.SlotConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/**
 * Use case responsible for scheduling appointments in available working-hour slots.
 */
@RequiredArgsConstructor
public class CreateAppointmentUseCase {

    private static final String DOCTOR_RESOURCE_NAME = "Doctor";
    private static final String PATIENT_RESOURCE_NAME = "Patient";
    private static final int SLOT_MINUTES = 30;
    private static final int MIN_PENALTIES_TO_BLOCK_PATIENT = 3;
    private static final int PENALTY_WINDOW_DAYS = 30;
    private static final LocalTime WEEKDAY_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime WEEKDAY_CLOSING_TIME = LocalTime.of(18, 0);
    private static final LocalTime SATURDAY_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime SATURDAY_CLOSING_TIME = LocalTime.of(13, 0);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PenaltyRepository penaltyRepository;

    /**
     * Schedules an appointment after validating existence, working hours, conflicts, and patient block status.
     *
     * @param appointment appointment request data
     * @return scheduled appointment
     * @throws ResourceNotFoundException when doctor or patient does not exist
     * @throws InvalidAppointmentSlotException when the requested slot is malformed or patient birth date is future
     * @throws OutsideWorkingHoursException when the requested slot is outside clinic working hours
     * @throws PatientBlockedException when the patient has three or more recent penalties
     * @throws SlotConflictException when doctor or patient already has a conflicting appointment
     */
    public Appointment createAppointment(Appointment appointment) {
        Patient patient = patientRepository.findById(appointment.getPatientId());
        if (patient == null) {
            throw new ResourceNotFoundException(PATIENT_RESOURCE_NAME, appointment.getPatientId());
        }
        if (doctorRepository.findById(appointment.getDoctorId()) == null) {
            throw new ResourceNotFoundException(DOCTOR_RESOURCE_NAME, appointment.getDoctorId());
        }

        validatePatientBirthDate(patient);
        validateSlotAlignment(appointment.getDateTime());
        validateWorkingHours(appointment.getDateTime());
        validatePatientBlock(appointment.getPatientId());
        validatePatientAppointmentConflict(appointment);
        validateDoctorSlotConflict(appointment);

        Appointment appointmentToSave = appointment.toBuilder()
                .id(appointment.getId() == null ? UUID.randomUUID() : appointment.getId())
                .status(AppointmentStatus.SCHEDULED)
                .cancelledAt(null)
                .build();

        return appointmentRepository.save(appointmentToSave);
    }

    private static void validatePatientBirthDate(Patient patient) {
        if (patient.getBirthDate() != null && patient.getBirthDate().isAfter(LocalDate.now())) {
            throw new InvalidAppointmentSlotException("Patient birth date cannot be in the future");
        }
    }

    private static void validateSlotAlignment(LocalDateTime dateTime) {
        boolean minuteIsAligned = dateTime.getMinute() % SLOT_MINUTES == 0;
        boolean secondIsAligned = dateTime.getSecond() == 0 && dateTime.getNano() == 0;
        if (!minuteIsAligned || !secondIsAligned) {
            throw new InvalidAppointmentSlotException("Appointment dateTime must be aligned to a 30-minute slot");
        }
    }

    private static void validateWorkingHours(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == SUNDAY || isColombianHoliday(date)) {
            throw new OutsideWorkingHoursException("Clinic does not work on Sundays or holidays");
        }

        LocalTime time = dateTime.toLocalTime();
        if (dayOfWeek == SATURDAY) {
            validateTimeWindow(time, SATURDAY_OPENING_TIME, SATURDAY_CLOSING_TIME);
            return;
        }

        validateTimeWindow(time, WEEKDAY_OPENING_TIME, WEEKDAY_CLOSING_TIME);
    }

    private static void validateTimeWindow(LocalTime time, LocalTime openingTime, LocalTime closingTime) {
        if (time.isBefore(openingTime) || !time.isBefore(closingTime)) {
            throw new OutsideWorkingHoursException("Appointment dateTime is outside working hours");
        }
    }

    private void validatePatientBlock(UUID patientId) {
        LocalDateTime lowerBound = LocalDateTime.now().minusDays(PENALTY_WINDOW_DAYS);
        long recentPenalties = penaltyRepository.countByPatientIdAndCreatedAtGreaterThanEqual(patientId, lowerBound);
        if (recentPenalties >= MIN_PENALTIES_TO_BLOCK_PATIENT) {
            throw new PatientBlockedException(patientId);
        }
    }

    private void validatePatientAppointmentConflict(Appointment appointment) {
        boolean patientAlreadyHasAppointment = appointmentRepository.existsScheduledByPatientIdAndDoctorIdAndDateTime(
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime()
        );
        if (patientAlreadyHasAppointment) {
            throw new SlotConflictException("Patient already has an appointment with this doctor at "
                    + appointment.getDateTime());
        }
    }

    private void validateDoctorSlotConflict(Appointment appointment) {
        boolean doctorAlreadyBooked = appointmentRepository.existsScheduledByDoctorIdAndDateTime(
                appointment.getDoctorId(),
                appointment.getDateTime()
        );
        if (doctorAlreadyBooked) {
            throw new SlotConflictException("Doctor already has an appointment at " + appointment.getDateTime());
        }
    }

    private static boolean isColombianHoliday(LocalDate date) {
        return colombianHolidays(date.getYear()).contains(date);
    }

    private static Set<LocalDate> colombianHolidays(int year) {
        LocalDate easterSunday = easterSunday(year);
        return Set.of(
                LocalDate.of(year, Month.JANUARY, 1),
                nextMonday(LocalDate.of(year, Month.JANUARY, 6)),
                nextMonday(LocalDate.of(year, Month.MARCH, 19)),
                easterSunday.minusDays(3),
                easterSunday.minusDays(2),
                LocalDate.of(year, Month.MAY, 1),
                nextMonday(easterSunday.plusDays(43)),
                nextMonday(easterSunday.plusDays(64)),
                nextMonday(easterSunday.plusDays(71)),
                nextMonday(LocalDate.of(year, Month.JUNE, 29)),
                LocalDate.of(year, Month.JULY, 20),
                LocalDate.of(year, Month.AUGUST, 7),
                nextMonday(LocalDate.of(year, Month.AUGUST, 15)),
                nextMonday(LocalDate.of(year, Month.OCTOBER, 12)),
                nextMonday(LocalDate.of(year, Month.NOVEMBER, 1)),
                nextMonday(LocalDate.of(year, Month.NOVEMBER, 11)),
                LocalDate.of(year, Month.DECEMBER, 8),
                LocalDate.of(year, Month.DECEMBER, 25)
        );
    }

    /**
     * Applies the Colombian holiday transfer rule by moving a date to Monday when required.
     *
     * @param date original holiday date
     * @return the same date if it is Monday, otherwise the next Monday
     */
    private static LocalDate nextMonday(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Calculates Gregorian Easter Sunday for a year using the computus algorithm.
     *
     * @param year year used to calculate Easter Sunday
     * @return Easter Sunday date for the given year
     */
    private static LocalDate easterSunday(int year) {
        int remainderA = year % 19;
        int century = year / 100;
        int yearOfCentury = year % 100;
        int leapCenturyAdjustment = century / 4;
        int centuryRemainder = century % 4;
        int correction = (century + 8) / 25;
        int moonCorrection = (century - correction + 1) / 3;
        int epact = (19 * remainderA + century - leapCenturyAdjustment - moonCorrection + 15) % 30;
        int yearOfCenturyLeap = yearOfCentury / 4;
        int yearOfCenturyRemainder = yearOfCentury % 4;
        int weekdayCorrection = (32 + 2 * centuryRemainder + 2 * yearOfCenturyLeap
                - epact - yearOfCenturyRemainder) % 7;
        int finalCorrection = (remainderA + 11 * epact + 22 * weekdayCorrection) / 451;
        int month = (epact + weekdayCorrection - 7 * finalCorrection + 114) / 31;
        int day = ((epact + weekdayCorrection - 7 * finalCorrection + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
