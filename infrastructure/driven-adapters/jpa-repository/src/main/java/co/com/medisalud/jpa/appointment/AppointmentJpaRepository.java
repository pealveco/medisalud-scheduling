package co.com.medisalud.jpa.appointment;

import co.com.medisalud.jpa.entity.AppointmentData;
import co.com.medisalud.model.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for appointment persistence records.
 */
public interface AppointmentJpaRepository extends CrudRepository<AppointmentData, UUID>,
        QueryByExampleExecutor<AppointmentData> {

    /**
     * Checks whether a scheduled appointment exists for a doctor and slot.
     *
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @param status appointment status
     * @return true when a matching appointment exists
     */
    boolean existsByDoctorIdAndDateTimeAndStatus(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status);

    /**
     * Checks whether a scheduled appointment exists for a patient, doctor, and slot.
     *
     * @param patientId patient identifier
     * @param doctorId doctor identifier
     * @param dateTime appointment slot start
     * @param status appointment status
     * @return true when a matching appointment exists
     */
    boolean existsByPatientIdAndDoctorIdAndDateTimeAndStatus(
            UUID patientId,
            UUID doctorId,
            LocalDateTime dateTime,
            AppointmentStatus status);

    /**
     * Finds appointments by doctor, status, and half-open date-time range.
     *
     * @param doctorId doctor identifier
     * @param status appointment status
     * @param startDateTime inclusive range start
     * @param endDateTime exclusive range end
     * @return matching persistence records
     */
    @Query("""
            SELECT appointment
            FROM AppointmentData appointment
            WHERE appointment.doctorId = :doctorId
              AND appointment.status = :status
              AND appointment.dateTime >= :startDateTime
              AND appointment.dateTime < :endDateTime
            """)
    List<AppointmentData> findDoctorAppointmentsInRange(
            @Param("doctorId") UUID doctorId,
            @Param("status") AppointmentStatus status,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
