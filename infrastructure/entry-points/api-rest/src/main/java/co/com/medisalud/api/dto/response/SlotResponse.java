package co.com.medisalud.api.dto.response;

import java.time.LocalTime;

/**
 * Response item that represents an available time slot.
 *
 * @param start slot start time
 * @param end slot end time
 */
public record SlotResponse(
        LocalTime start,
        LocalTime end
) {
}
