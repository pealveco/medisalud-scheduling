package co.com.medisalud.model.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model that represents a patient who can schedule medical appointments.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Patient {

    private UUID id;
    private String fullName;
    private String documentId;
    private String phone;
    private String email;
    private LocalDate birthDate;
}
