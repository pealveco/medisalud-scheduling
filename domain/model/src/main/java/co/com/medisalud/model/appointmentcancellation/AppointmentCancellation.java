package co.com.medisalud.model.appointmentcancellation;

import co.com.medisalud.model.appointment.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain result returned after cancelling an appointment.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AppointmentCancellation {

    private Appointment appointment;
    private boolean penaltyApplied;
}
