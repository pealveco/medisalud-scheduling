package co.com.medisalud.usecase.listappointments;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.common.exceptions.InvalidDateRangeException;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Use case responsible for listing appointments using optional combinable filters.
 */
@RequiredArgsConstructor
public class ListAppointmentsUseCase {

    private final AppointmentRepository appointmentRepository;

    /**
     * Lists appointments that match the supplied criteria.
     *
     * @param criteria optional search criteria
     * @return appointments matching the criteria
     * @throws InvalidDateRangeException when start date is after end date
     */
    public List<Appointment> listAppointments(AppointmentSearchCriteria criteria) {
        validateDateRange(criteria);
        return appointmentRepository.findByCriteria(criteria);
    }

    private static void validateDateRange(AppointmentSearchCriteria criteria) {
        if (criteria.getStartDate() != null
                && criteria.getEndDate() != null
                && criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new InvalidDateRangeException("Start date must be before or equal to end date");
        }
    }
}
