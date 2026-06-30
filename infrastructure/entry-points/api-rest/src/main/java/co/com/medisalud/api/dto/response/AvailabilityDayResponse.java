package co.com.medisalud.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Response body item for doctor availability on a given date.
 *
 * @param date availability date
 * @param slots free slots for the date
 */
public record AvailabilityDayResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date,
        List<SlotResponse> slots
) {
}
