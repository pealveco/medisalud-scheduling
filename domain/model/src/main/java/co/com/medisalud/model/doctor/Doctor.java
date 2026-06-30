package co.com.medisalud.model.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Domain model that represents a doctor available for medical appointments.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Doctor {

    private UUID id;
    private String fullName;
    private String specialty;
    private String phone;
    private String email;
}
