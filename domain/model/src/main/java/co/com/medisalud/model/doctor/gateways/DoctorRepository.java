package co.com.medisalud.model.doctor.gateways;

import co.com.medisalud.model.doctor.Doctor;

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
}
