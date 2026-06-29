package co.com.medisalud.api.mapper;

import co.com.medisalud.api.dto.response.AvailabilityDayResponse;
import co.com.medisalud.api.dto.response.SlotResponse;
import co.com.medisalud.model.availabilityday.AvailabilityDay;
import co.com.medisalud.model.availabilityslot.AvailabilitySlot;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps doctor availability domain models to API response DTOs.
 */
@Component
public class AvailabilityMapper {

    /**
     * Maps availability days to response DTOs.
     *
     * @param availability availability grouped by date
     * @return response DTOs
     */
    public List<AvailabilityDayResponse> toResponse(List<AvailabilityDay> availability) {
        return availability.stream()
                .map(this::toResponse)
                .toList();
    }

    private AvailabilityDayResponse toResponse(AvailabilityDay availabilityDay) {
        return new AvailabilityDayResponse(
                availabilityDay.getDate(),
                availabilityDay.getSlots().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private SlotResponse toResponse(AvailabilitySlot availabilitySlot) {
        return new SlotResponse(availabilitySlot.getStart(), availabilitySlot.getEnd());
    }
}
