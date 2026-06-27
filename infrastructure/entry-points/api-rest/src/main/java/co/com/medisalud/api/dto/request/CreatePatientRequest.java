package co.com.medisalud.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for creating a patient through the REST API.
 *
 * @param fullName patient's full name, required from 3 to 100 characters
 * @param documentId unique patient document identifier, required with at least 7 characters
 * @param phone required phone number with at least 7 digits
 * @param email required email address
 * @param birthDate optional birth date; future dates are rejected
 */
public record CreatePatientRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String fullName,

        @NotBlank
        @Size(min = 7)
        String documentId,

        @NotBlank
        @Pattern(regexp = "^\\d{7,}$", message = "must contain at least 7 digits")
        String phone,

        @NotBlank
        @Email
        String email,

        @Past
        LocalDate birthDate
) {
}
