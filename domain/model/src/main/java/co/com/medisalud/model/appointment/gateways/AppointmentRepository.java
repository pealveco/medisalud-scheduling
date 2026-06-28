package co.com.medisalud.model.appointment.gateways;

import co.com.medisalud.model.appointment.Appointment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gateway that defines persistence operations required by appointments.
 */
public interface AppointmentRepository {

    /**
     * Persists an appointment and returns the saved domain model.
     *
     * @param appointment appointment to persist
     * @return persisted appointment
     */
    Appointment save(Appointment appointment);

    /**
     * Checks whether a doctor already has a scheduled appointment in a date-time slot.
     *
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @return true when the doctor is already booked
     */
    boolean existsScheduledByDoctorIdAndDateTime(UUID doctorId, LocalDateTime dateTime);

    /**
     * Checks whether a patient already has a scheduled appointment with the same doctor in a date-time slot.
     *
     * @param patientId patient identifier
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @return true when the patient already has that appointment
     */
    boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
            UUID patientId,
            UUID doctorId,
            LocalDateTime dateTime);
}
