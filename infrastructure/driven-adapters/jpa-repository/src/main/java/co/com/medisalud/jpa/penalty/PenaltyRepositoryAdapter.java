package co.com.medisalud.jpa.penalty;

import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA adapter for penalty business-rule reads.
 */
@Repository
public class PenaltyRepositoryAdapter implements PenaltyRepository {

    private final PenaltyJpaRepository repository;

    public PenaltyRepositoryAdapter(PenaltyJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Counts penalties for a patient since the supplied date-time.
     *
     * @param patientId patient identifier
     * @param fromDateTime inclusive lower bound for penalty creation date-time
     * @return number of penalties in the window
     */
    @Override
    public long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime) {
        return repository.countByPatientIdAndCreatedAtGreaterThanEqual(patientId, fromDateTime);
    }
}
