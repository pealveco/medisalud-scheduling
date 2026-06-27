package co.com.medisalud.model.patient.exceptions;

/**
 * Exception thrown when a patient document identifier is already registered.
 */
public class PatientDocumentAlreadyExistsException extends RuntimeException {

    /**
     * Creates the exception for the duplicated document identifier.
     *
     * @param documentId duplicated document identifier
     */
    public PatientDocumentAlreadyExistsException(String documentId) {
        super("Patient document already exists: " + documentId);
    }
}
