package co.com.medisalud.usecase.createpatient;

import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.exceptions.PatientDocumentAlreadyExistsException;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case responsible for registering patients in the clinic schedule system.
 */
@RequiredArgsConstructor
public class CreatePatientUseCase {

    private final PatientRepository patientRepository;

    /**
     * Registers a patient and rejects duplicated document identifiers.
     *
     * @param patient patient data to register
     * @return registered patient with identifier
     * @throws PatientDocumentAlreadyExistsException when the document identifier is already registered
     */
    public Patient createPatient(Patient patient) {
        if (patientRepository.existsByDocumentId(patient.getDocumentId())) {
            throw new PatientDocumentAlreadyExistsException(patient.getDocumentId());
        }

        Patient patientToSave = patient.getId() == null
                ? patient.toBuilder().id(UUID.randomUUID()).build()
                : patient;

        return patientRepository.save(patientToSave);
    }
}
