package co.com.medisalud.usecase.createpatient;

import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.exceptions.PatientDocumentAlreadyExistsException;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePatientUseCaseTest {

    @Test
    void shouldCreatePatientWithGeneratedIdentifier() {
        InMemoryPatientRepository repository = new InMemoryPatientRepository();
        CreatePatientUseCase useCase = new CreatePatientUseCase(repository);
        Patient patient = patient("123456789");

        Patient result = useCase.createPatient(patient);

        assertNotNull(result.getId());
        assertEquals("Mateo Perez", result.getFullName());
        assertEquals("123456789", result.getDocumentId());
    }

    @Test
    void shouldKeepExistingIdentifierWhenProvided() {
        InMemoryPatientRepository repository = new InMemoryPatientRepository();
        CreatePatientUseCase useCase = new CreatePatientUseCase(repository);
        UUID patientId = UUID.randomUUID();
        Patient patient = patient("123456789").toBuilder().id(patientId).build();

        Patient result = useCase.createPatient(patient);

        assertEquals(patientId, result.getId());
    }

    @Test
    void shouldRejectDuplicatedDocumentIdentifier() {
        InMemoryPatientRepository repository = new InMemoryPatientRepository();
        CreatePatientUseCase useCase = new CreatePatientUseCase(repository);
        Patient patient = patient("123456789");
        useCase.createPatient(patient);

        assertThrows(PatientDocumentAlreadyExistsException.class, () -> useCase.createPatient(patient));
        assertTrue(repository.existsByDocumentId("123456789"));
    }

    private static Patient patient(String documentId) {
        return Patient.builder()
                .fullName("Mateo Perez")
                .documentId(documentId)
                .phone("3107654321")
                .email("mateo.perez@medisalud.com")
                .birthDate(LocalDate.of(1990, 5, 12))
                .build();
    }

    private static class InMemoryPatientRepository implements PatientRepository {

        private final Map<String, Patient> patientsByDocumentId = new HashMap<>();

        @Override
        public Patient save(Patient patient) {
            patientsByDocumentId.put(patient.getDocumentId(), patient);
            return patient;
        }

        @Override
        public Patient findById(UUID id) {
            return patientsByDocumentId.values().stream()
                    .filter(patient -> id.equals(patient.getId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public boolean existsByDocumentId(String documentId) {
            return patientsByDocumentId.containsKey(documentId);
        }
    }
}
