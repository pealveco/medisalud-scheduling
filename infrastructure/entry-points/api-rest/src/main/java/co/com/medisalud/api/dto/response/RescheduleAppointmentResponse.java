package co.com.medisalud.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response body returned after rescheduling an appointment atomically.
 *
 * @param id original appointment identifier
 * @param patientId patient identifier linked to the original appointment
 * @param doctorId doctor identifier linked to the original appointment
 * @param dateTime original appointment date and time
 * @param status status of the original appointment after rescheduling
 * @param penaltyApplied whether cancelling the original appointment generated a penalty
 * @param newAppointment newly scheduled appointment
 */
public record RescheduleAppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        AppointmentStatusDto status,
        boolean penaltyApplied,
        AppointmentResponse newAppointment
) {
}
