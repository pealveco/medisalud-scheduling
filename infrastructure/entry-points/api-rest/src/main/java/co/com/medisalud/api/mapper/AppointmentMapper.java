package co.com.medisalud.api.mapper;

import co.com.medisalud.api.dto.request.CreateAppointmentRequest;
import co.com.medisalud.api.dto.response.AppointmentResponse;
import co.com.medisalud.api.dto.response.AppointmentStatusDto;
import co.com.medisalud.api.dto.response.CancelAppointmentResponse;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointmentcancellation.AppointmentCancellation;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Maps appointment API DTOs to domain models and domain models to API responses.
 */
@Component
public class AppointmentMapper {

    /**
     * Converts a create appointment request into a domain model.
     *
     * @param request validated API request
     * @return appointment domain model without generated identifier or status
     */
    public Appointment toDomain(CreateAppointmentRequest request) {
        return Appointment.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .dateTime(request.dateTime())
                .build();
    }

    /**
     * Converts an appointment domain model into an API response.
     *
     * @param appointment scheduled appointment domain model
     * @return API response
     */
    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                AppointmentStatusDto.valueOf(appointment.getStatus().name())
        );
    }

    /**
     * Converts appointment domain models into API responses.
     *
     * @param appointments appointment domain models
     * @return API responses
     */
    public List<AppointmentResponse> toResponse(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Converts an appointment cancellation result into an API response.
     *
     * @param cancellation cancellation domain result
     * @return API cancellation response
     */
    public CancelAppointmentResponse toCancelResponse(AppointmentCancellation cancellation) {
        Appointment appointment = cancellation.getAppointment();
        return new CancelAppointmentResponse(
                appointment.getId(),
                AppointmentStatusDto.valueOf(appointment.getStatus().name()),
                appointment.getCancelledAt(),
                cancellation.isPenaltyApplied()
        );
    }

    /**
     * Converts API query parameters into appointment search criteria.
     *
     * @param doctorId optional doctor identifier filter
     * @param patientId optional patient identifier filter
     * @param status optional appointment status filter
     * @param startDate optional inclusive start date-time filter
     * @param endDate optional inclusive end date-time filter
     * @return domain search criteria
     */
    public AppointmentSearchCriteria toSearchCriteria(
            UUID doctorId,
            UUID patientId,
            AppointmentStatusDto status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return AppointmentSearchCriteria.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .status(status == null ? null : AppointmentStatus.valueOf(status.name()))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
