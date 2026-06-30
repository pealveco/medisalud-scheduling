package co.com.medisalud.usecase.getdoctoravailability;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.availabilityday.AvailabilityDay;
import co.com.medisalud.model.availabilityslot.AvailabilitySlot;
import co.com.medisalud.model.common.exceptions.InvalidDateRangeException;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.usecase.common.TimeWindow;
import co.com.medisalud.usecase.common.WorkingHoursPolicy;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Use case responsible for calculating a doctor's free appointment slots in a date range.
 */
@RequiredArgsConstructor
public class GetDoctorAvailabilityUseCase {

    private static final String DOCTOR_RESOURCE_NAME = "Doctor";

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Returns free appointment slots for a doctor inside an inclusive date range.
     *
     * @param doctorId doctor identifier
     * @param startDate first date included in the search
     * @param endDate last date included in the search
     * @return working days with their available slots
     * @throws ResourceNotFoundException when the doctor does not exist
     * @throws InvalidDateRangeException when startDate is after endDate
     */
    public List<AvailabilityDay> getAvailability(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        if (doctorRepository.findById(doctorId) == null) {
            throw new ResourceNotFoundException(DOCTOR_RESOURCE_NAME, doctorId);
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("startDate must be before or equal to endDate");
        }

        Set<LocalDateTime> occupiedSlots = occupiedSlots(doctorId, startDate, endDate);
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> availabilityForDate(date, occupiedSlots))
                .flatMap(Optional::stream)
                .toList();
    }

    private Set<LocalDateTime> occupiedSlots(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findScheduledByDoctorIdAndDateTimeBetween(
                doctorId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
        Set<LocalDateTime> occupiedSlots = new HashSet<>();
        appointments.forEach(appointment -> occupiedSlots.add(appointment.getDateTime()));
        return occupiedSlots;
    }

    private Optional<AvailabilityDay> availabilityForDate(LocalDate date, Set<LocalDateTime> occupiedSlots) {
        return WorkingHoursPolicy.workingWindow(date)
                .map(timeWindow -> AvailabilityDay.builder()
                        .date(date)
                        .slots(slotsForDate(date, timeWindow, occupiedSlots))
                        .build());
    }

    private List<AvailabilitySlot> slotsForDate(
            LocalDate date,
            TimeWindow timeWindow,
            Set<LocalDateTime> occupiedSlots) {
        return timeSlots(timeWindow)
                .stream()
                .filter(slotStart -> !occupiedSlots.contains(LocalDateTime.of(date, slotStart)))
                .map(slotStart -> AvailabilitySlot.builder()
                        .start(slotStart)
                        .end(slotStart.plusMinutes(WorkingHoursPolicy.SLOT_MINUTES))
                        .build())
                .toList();
    }

    private List<LocalTime> timeSlots(TimeWindow timeWindow) {
        return Stream.iterate(
                        timeWindow.start(),
                        slotStart -> slotStart.isBefore(timeWindow.end()),
                        slotStart -> slotStart.plusMinutes(WorkingHoursPolicy.SLOT_MINUTES)
                )
                .toList();
    }
}
