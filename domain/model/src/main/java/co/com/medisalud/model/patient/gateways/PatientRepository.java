package co.com.medisalud.model.patient.gateways;

import co.com.medisalud.model.patient.Patient;

import java.util.UUID;

/**
 * Gateway that defines persistence operations required by the patient domain.
 */
public interface PatientRepository {

    /**
     * Persists a patient and returns the saved domain model.
     *
     * @param patient patient to persist
     * @return persisted patient
     */
    Patient save(Patient patient);

    /**
     * Finds a patient by identifier.
     *
     * @param id patient identifier
     * @return patient when it exists, otherwise null
     */
    Patient findById(UUID id);

    /**
     * Checks whether a patient document identifier already exists.
     *
     * @param documentId patient document identifier
     * @return true when the document identifier is already registered
     */
    boolean existsByDocumentId(String documentId);
}
