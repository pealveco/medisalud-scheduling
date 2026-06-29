package co.com.medisalud.model.appointment.gateways;

import co.com.medisalud.model.appointment.Appointment;

import java.time.LocalDateTime;
import java.util.List;
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
     * Finds an appointment by identifier.
     *
     * @param id appointment identifier
     * @return appointment when it exists, otherwise null
     */
    Appointment findById(UUID id);

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

    /**
     * Finds scheduled appointments for a doctor inside a half-open date-time range.
     *
     * @param doctorId doctor identifier
     * @param startDateTime inclusive range start
     * @param endDateTime exclusive range end
     * @return scheduled appointments in the range
     */
    List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
            UUID doctorId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime);
}
