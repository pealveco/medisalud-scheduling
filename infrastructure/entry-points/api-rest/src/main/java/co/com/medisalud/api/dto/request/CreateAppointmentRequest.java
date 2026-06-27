package co.com.medisalud.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request body for scheduling a medical appointment through the REST API.
 *
 * @param patientId existing patient identifier, required
 * @param doctorId existing doctor identifier, required
 * @param dateTime appointment start date and time, required and future
 */
public record CreateAppointmentRequest(
        @NotNull
        UUID patientId,

        @NotNull
        UUID doctorId,

        @NotNull
        @Future
        LocalDateTime dateTime
) {
}
