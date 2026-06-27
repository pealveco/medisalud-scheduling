package co.com.medisalud.model.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model that represents a scheduled, cancelled, or attended appointment.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Appointment {

    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private LocalDateTime cancelledAt;
}
