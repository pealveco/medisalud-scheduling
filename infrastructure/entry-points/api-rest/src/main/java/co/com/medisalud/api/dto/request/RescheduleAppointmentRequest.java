package co.com.medisalud.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request body for rescheduling an existing appointment.
 *
 * @param newDateTime new appointment start date and time, required and future
 */
public record RescheduleAppointmentRequest(
        @NotNull
        @Future
        LocalDateTime newDateTime
) {
}
