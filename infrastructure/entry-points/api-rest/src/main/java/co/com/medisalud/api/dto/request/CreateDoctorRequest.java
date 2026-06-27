package co.com.medisalud.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a doctor through the REST API.
 *
 * @param fullName doctor's full name, required from 3 to 100 characters
 * @param specialty doctor's medical specialty, required
 * @param phone optional phone number with at least 7 digits
 * @param email optional email address
 */
public record CreateDoctorRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String fullName,

        @NotBlank
        String specialty,

        @Pattern(regexp = "^\\d{7,}$", message = "must contain at least 7 digits")
        String phone,

        @Email
        String email
) {
}
