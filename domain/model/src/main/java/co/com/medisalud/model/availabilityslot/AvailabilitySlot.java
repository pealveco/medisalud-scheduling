package co.com.medisalud.model.availabilityslot;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Domain model that represents a free appointment slot.
 */
@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AvailabilitySlot {

    private LocalTime start;
    private LocalTime end;
}
