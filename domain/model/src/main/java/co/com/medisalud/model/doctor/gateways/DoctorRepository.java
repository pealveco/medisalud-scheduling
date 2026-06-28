package co.com.medisalud.model.doctor.gateways;

import co.com.medisalud.model.doctor.Doctor;

import java.util.UUID;

/**
 * Gateway that defines persistence operations required by the doctor domain.
 */
public interface DoctorRepository {

    /**
     * Persists a doctor and returns the saved domain model.
     *
     * @param doctor doctor to persist
     * @return persisted doctor
     */
    Doctor save(Doctor doctor);

    /**
     * Finds a doctor by identifier.
     *
     * @param id doctor identifier
     * @return doctor when it exists, otherwise null
     */
    Doctor findById(UUID id);
}
