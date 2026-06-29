package co.com.medisalud.jpa.appointment;

import co.com.medisalud.jpa.entity.AppointmentData;
import co.com.medisalud.jpa.helper.AdapterOperations;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA adapter that maps appointment domain models to persistence records.
 */
@Repository
public class AppointmentRepositoryAdapter extends AdapterOperations<Appointment, AppointmentData, UUID,
        AppointmentJpaRepository> implements AppointmentRepository {

    private static final String FIELD_DATE_TIME = "dateTime";

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

    /**
     * Finds appointments matching optional combinable filters.
     *
     * @param criteria optional search criteria
     * @return appointments matching the supplied filters
     */
    @Override
    public List<Appointment> findByCriteria(AppointmentSearchCriteria criteria) {
        return repository.findAll(
                        buildSpecification(criteria),
                        Sort.by(Sort.Direction.ASC, FIELD_DATE_TIME)
                )
                .stream()
                .map(this::toEntity)
                .toList();
    }

    private static Specification<AppointmentData> buildSpecification(AppointmentSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getDoctorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("doctorId"), criteria.getDoctorId()));
            }
            if (criteria.getPatientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("patientId"), criteria.getPatientId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_DATE_TIME), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_DATE_TIME), criteria.getEndDate()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
