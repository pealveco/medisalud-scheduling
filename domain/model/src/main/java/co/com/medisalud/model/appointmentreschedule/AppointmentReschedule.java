package co.com.medisalud.model.appointmentreschedule;

import co.com.medisalud.model.appointment.Appointment;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain result returned after rescheduling an appointment.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AppointmentReschedule {

    private Appointment originalAppointment;
    private boolean penaltyApplied;
    private Appointment newAppointment;
}
