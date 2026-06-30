package co.com.medisalud.usecase.common;

import co.com.medisalud.model.appointment.exceptions.InvalidAppointmentSlotException;
import co.com.medisalud.model.appointment.exceptions.OutsideWorkingHoursException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/**
 * Centralizes the clinic working-hours rules used by appointment-related use cases.
 */
public final class WorkingHoursPolicy {

    public static final int SLOT_MINUTES = 30;

    private static final LocalTime WEEKDAY_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime WEEKDAY_CLOSING_TIME = LocalTime.of(18, 0);
    private static final LocalTime SATURDAY_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime SATURDAY_CLOSING_TIME = LocalTime.of(13, 0);

    private WorkingHoursPolicy() {
    }

    /**
     * Validates that an appointment start is aligned to a valid slot boundary.
     *
     * @param dateTime appointment start date-time
     * @throws InvalidAppointmentSlotException when minutes, seconds, or nanos are not aligned
     */
    public static void validateSlotAlignment(LocalDateTime dateTime) {
        boolean minuteIsAligned = dateTime.getMinute() % SLOT_MINUTES == 0;
        boolean secondIsAligned = dateTime.getSecond() == 0 && dateTime.getNano() == 0;
        if (!minuteIsAligned || !secondIsAligned) {
            throw new InvalidAppointmentSlotException("Appointment dateTime must be aligned to a 30-minute slot");
        }
    }

    /**
     * Validates that an appointment start belongs to the clinic working schedule.
     *
     * @param dateTime appointment start date-time
     * @throws OutsideWorkingHoursException when the date is non-working or outside the time window
     */
    public static void validateWorkingHours(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        Optional<TimeWindow> timeWindow = workingWindow(date);
        if (timeWindow.isEmpty()) {
            throw new OutsideWorkingHoursException("Clinic does not work on Sundays or holidays");
        }
        validateTimeWindow(dateTime.toLocalTime(), timeWindow.get());
    }

    /**
     * Resolves the working window for a date.
     *
     * @param date date to evaluate
     * @return working time window, or empty when the date is Sunday or a Colombian holiday
     */
    public static Optional<TimeWindow> workingWindow(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == SUNDAY || isColombianHoliday(date)) {
            return Optional.empty();
        }
        if (dayOfWeek == SATURDAY) {
            return Optional.of(new TimeWindow(SATURDAY_OPENING_TIME, SATURDAY_CLOSING_TIME));
        }
        return Optional.of(new TimeWindow(WEEKDAY_OPENING_TIME, WEEKDAY_CLOSING_TIME));
    }

    private static void validateTimeWindow(LocalTime time, TimeWindow timeWindow) {
        if (time.isBefore(timeWindow.start()) || !time.isBefore(timeWindow.end())) {
            throw new OutsideWorkingHoursException("Appointment dateTime is outside working hours");
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
