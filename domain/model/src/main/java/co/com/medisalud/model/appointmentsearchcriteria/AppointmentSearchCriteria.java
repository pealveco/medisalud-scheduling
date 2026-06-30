package co.com.medisalud.model.appointmentsearchcriteria;

import co.com.medisalud.model.appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model that groups optional appointment listing filters.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AppointmentSearchCriteria {

    private UUID doctorId;
    private UUID patientId;
    private AppointmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
