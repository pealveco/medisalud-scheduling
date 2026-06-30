package co.com.medisalud.api.mapper;

import co.com.medisalud.api.dto.request.CreatePatientRequest;
import co.com.medisalud.api.dto.response.PatientResponse;
import co.com.medisalud.model.patient.Patient;
import org.springframework.stereotype.Component;

/**
 * Maps patient API DTOs to domain models and domain models to API responses.
 */
@Component
public class PatientMapper {

    /**
     * Converts a create patient request into a domain model.
     *
     * @param request validated API request
     * @return patient domain model without generated identifier
     */
    public Patient toDomain(CreatePatientRequest request) {
        return Patient.builder()
                .fullName(request.fullName())
                .documentId(request.documentId())
                .phone(request.phone())
                .email(request.email())
                .birthDate(request.birthDate())
                .build();
    }

    /**
     * Converts a patient domain model into an API response.
     *
     * @param patient registered patient domain model
     * @return API response
     */
    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getDocumentId(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getBirthDate()
        );
    }
}
