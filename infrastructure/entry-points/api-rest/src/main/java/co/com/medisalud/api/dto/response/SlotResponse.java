package co.com.medisalud.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

/**
 * Response item that represents an available time slot.
 *
 * @param start slot start time
 * @param end slot end time
 */
public record SlotResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        LocalTime start,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        LocalTime end
) {
}
