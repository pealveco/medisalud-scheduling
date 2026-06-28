package co.com.medisalud.jpa.patient;

import co.com.medisalud.jpa.entity.PatientData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.UUID;

/**
 * Spring Data repository for patient persistence records.
 */
public interface PatientJpaRepository extends CrudRepository<PatientData, UUID>, QueryByExampleExecutor<PatientData> {

    /**
     * Checks whether a patient exists by document identifier.
     *
     * @param documentId patient document identifier
     * @return true when the document identifier already exists
     */
    boolean existsByDocumentId(String documentId);
}
