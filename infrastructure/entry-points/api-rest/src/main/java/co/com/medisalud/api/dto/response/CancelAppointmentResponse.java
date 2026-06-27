package co.com.medisalud.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response body returned after cancelling an appointment.
 *
 * @param id cancelled appointment identifier
 * @param status cancellation status exposed by the API
 * @param cancelledAt date and time when the cancellation was registered
 * @param penaltyApplied whether the cancellation generated a penalty
 */
public record CancelAppointmentResponse(
        UUID id,
        AppointmentStatusDto status,
        LocalDateTime cancelledAt,
        boolean penaltyApplied
) {
}
