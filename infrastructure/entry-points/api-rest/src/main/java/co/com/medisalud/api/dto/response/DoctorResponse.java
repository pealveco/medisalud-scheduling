package co.com.medisalud.api.dto.response;

import java.util.UUID;

/**
 * Response body returned after creating or reading a doctor.
 *
 * @param id generated doctor identifier
 * @param fullName doctor's full name
 * @param specialty doctor's medical specialty
 * @param phone optional phone number
 * @param email optional email address
 */
public record DoctorResponse(
        UUID id,
        String fullName,
        String specialty,
        String phone,
        String email
) {
}
