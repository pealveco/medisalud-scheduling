package co.com.medisalud.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity for doctors.
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorData {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private String specialty;

    @Column
    private String phone;

    @Column
    private String email;
}
