package co.com.medisalud.model.availabilityday;

import co.com.medisalud.model.availabilityslot.AvailabilitySlot;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain model that groups available slots for a doctor on a date.
 */
@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AvailabilityDay {

    private LocalDate date;
    private List<AvailabilitySlot> slots;
}
