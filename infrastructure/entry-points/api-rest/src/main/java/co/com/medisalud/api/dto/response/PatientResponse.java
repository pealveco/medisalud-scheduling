package co.com.medisalud.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response body returned after creating or reading a patient.
 *
 * @param id generated patient identifier
 * @param fullName patient's full name
 * @param documentId patient's unique document identifier
 * @param phone patient's phone number
 * @param email patient's email address
 * @param birthDate optional patient birth date
 */
public record PatientResponse(
        UUID id,
        String fullName,
        String documentId,
        String phone,
        String email,
        LocalDate birthDate
) {
}
