package co.com.medisalud.api.mapper;

import co.com.medisalud.api.dto.request.CreateAppointmentRequest;
import co.com.medisalud.api.dto.response.AppointmentResponse;
import co.com.medisalud.api.dto.response.AppointmentStatusDto;
import co.com.medisalud.model.appointment.Appointment;
import org.springframework.stereotype.Component;

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
}
