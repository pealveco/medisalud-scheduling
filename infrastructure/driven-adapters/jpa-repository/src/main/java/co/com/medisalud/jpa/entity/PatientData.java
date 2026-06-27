package co.com.medisalud.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA persistence entity for patients.
 */
@Entity
@Table(
        name = "patients",
        uniqueConstraints = @UniqueConstraint(name = "uk_patients_document_id", columnNames = "document_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientData {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(name = "document_id", nullable = false, length = 50)
    private String documentId;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column
    private LocalDate birthDate;
}
