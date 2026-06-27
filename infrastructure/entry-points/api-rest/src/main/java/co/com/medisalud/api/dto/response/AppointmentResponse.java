package co.com.medisalud.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response body returned after scheduling or reading an appointment.
 *
 * @param id generated appointment identifier
 * @param patientId patient identifier linked to the appointment
 * @param doctorId doctor identifier linked to the appointment
 * @param dateTime appointment start date and time
 * @param status appointment status exposed by the API
 */
public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        AppointmentStatusDto status
) {
}
