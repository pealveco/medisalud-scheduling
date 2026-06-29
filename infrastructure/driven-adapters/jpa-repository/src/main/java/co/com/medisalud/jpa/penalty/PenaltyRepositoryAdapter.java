package co.com.medisalud.jpa.penalty;

import co.com.medisalud.jpa.entity.PenaltyData;
import co.com.medisalud.jpa.helper.AdapterOperations;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA adapter that maps penalty domain models to persistence records.
 */
@Repository
public class PenaltyRepositoryAdapter extends AdapterOperations<Penalty, PenaltyData, UUID, PenaltyJpaRepository>
        implements PenaltyRepository {

    public PenaltyRepositoryAdapter(PenaltyJpaRepository repository, ObjectMapper mapper) {
        super(repository, mapper, penaltyData -> mapper.mapBuilder(penaltyData, Penalty.PenaltyBuilder.class).build());
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
