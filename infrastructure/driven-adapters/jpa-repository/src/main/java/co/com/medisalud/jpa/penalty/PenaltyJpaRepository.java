package co.com.medisalud.jpa.penalty;

import co.com.medisalud.jpa.entity.PenaltyData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data repository for penalty persistence records.
 */
public interface PenaltyJpaRepository extends CrudRepository<PenaltyData, UUID>, QueryByExampleExecutor<PenaltyData> {

    /**
     * Counts penalties for a patient since the supplied date-time.
     *
     * @param patientId patient identifier
     * @param createdAt inclusive lower bound for penalty creation date-time
     * @return number of matching penalties
     */
    long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime createdAt);
}
