package co.com.medisalud.jpa.appointment;

import co.com.medisalud.jpa.entity.AppointmentData;
import co.com.medisalud.jpa.helper.AdapterOperations;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA adapter that maps appointment domain models to persistence records.
 */
@Repository
public class AppointmentRepositoryAdapter extends AdapterOperations<Appointment, AppointmentData, UUID,
        AppointmentJpaRepository> implements AppointmentRepository {

    public AppointmentRepositoryAdapter(AppointmentJpaRepository repository, ObjectMapper mapper) {
        super(
                repository,
                mapper,
                appointmentData -> mapper.mapBuilder(appointmentData, Appointment.AppointmentBuilder.class).build()
        );
    }

    /**
     * Checks whether a doctor already has a scheduled appointment in a date-time slot.
     *
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @return true when the doctor is already booked
     */
    @Override
    public boolean existsScheduledByDoctorIdAndDateTime(UUID doctorId, LocalDateTime dateTime) {
        return repository.existsByDoctorIdAndDateTimeAndStatus(doctorId, dateTime, AppointmentStatus.SCHEDULED);
    }

    /**
     * Checks whether a patient already has a scheduled appointment with the same doctor in a date-time slot.
     *
     * @param patientId patient identifier
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @return true when the patient already has that appointment
     */
    @Override
    public boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
            UUID patientId,
            UUID doctorId,
            LocalDateTime dateTime) {
        return repository.existsByPatientIdAndDoctorIdAndDateTimeAndStatus(
                patientId,
                doctorId,
                dateTime,
                AppointmentStatus.SCHEDULED
        );
    }

    /**
     * Finds scheduled appointments for a doctor inside a half-open date-time range.
     *
     * @param doctorId doctor identifier
     * @param startDateTime inclusive range start
     * @param endDateTime exclusive range end
     * @return scheduled appointments in the range
     */
    @Override
    public List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
            UUID doctorId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        return repository.findDoctorAppointmentsInRange(
                        doctorId,
                        AppointmentStatus.SCHEDULED,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(this::toEntity)
                .toList();
    }
}
