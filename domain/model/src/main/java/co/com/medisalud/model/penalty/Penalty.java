package co.com.medisalud.model.penalty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model that records a penalty applied to a patient after a late cancellation.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Penalty {

    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
    private LocalDateTime createdAt;
}
