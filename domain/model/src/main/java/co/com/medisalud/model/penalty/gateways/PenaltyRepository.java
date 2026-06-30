package co.com.medisalud.model.penalty.gateways;

import co.com.medisalud.model.penalty.Penalty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gateway that defines read operations required by penalty business rules.
 */
public interface PenaltyRepository {

    /**
     * Persists a penalty and returns the saved domain model.
     *
     * @param penalty penalty to persist
     * @return persisted penalty
     */
    Penalty save(Penalty penalty);

    /**
     * Counts penalties for a patient since the supplied date-time.
     *
     * @param patientId patient identifier
     * @param fromDateTime inclusive lower bound for penalty creation date-time
     * @return number of penalties in the window
     */
    long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime);
}
