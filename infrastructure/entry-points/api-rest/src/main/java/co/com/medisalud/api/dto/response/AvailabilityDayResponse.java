package co.com.medisalud.api.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Response body item for doctor availability on a given date.
 *
 * @param date availability date
 * @param slots free slots for the date
 */
public record AvailabilityDayResponse(
        LocalDate date,
        List<SlotResponse> slots
) {
}
