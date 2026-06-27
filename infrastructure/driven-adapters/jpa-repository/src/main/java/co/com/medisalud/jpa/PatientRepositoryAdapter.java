package co.com.medisalud.jpa;

import co.com.medisalud.jpa.entity.PatientData;
import co.com.medisalud.jpa.helper.AdapterOperations;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.exceptions.PatientDocumentAlreadyExistsException;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA adapter that maps patient domain models to persistence records.
 */
@Repository
public class PatientRepositoryAdapter extends AdapterOperations<Patient, PatientData, UUID, PatientJpaRepository>
        implements PatientRepository {

    public PatientRepositoryAdapter(PatientJpaRepository repository, ObjectMapper mapper) {
        super(repository, mapper, patientData -> mapper.mapBuilder(patientData, Patient.PatientBuilder.class).build());
    }

    /**
     * Persists a patient and translates unique document violations to a domain exception.
     *
     * @param patient patient to persist
     * @return persisted patient
     */
    @Override
    public Patient save(Patient patient) {
        try {
            return super.save(patient);
        } catch (DataIntegrityViolationException exception) {
            throw new PatientDocumentAlreadyExistsException(patient.getDocumentId());
        }
    }

    /**
     * Checks whether a patient document identifier already exists.
     *
     * @param documentId patient document identifier
     * @return true when the document identifier is already registered
     */
    @Override
    public boolean existsByDocumentId(String documentId) {
        return repository.existsByDocumentId(documentId);
    }
}
