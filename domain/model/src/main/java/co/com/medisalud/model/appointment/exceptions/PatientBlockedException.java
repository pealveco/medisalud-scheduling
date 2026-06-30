package co.com.medisalud.model.appointment.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a patient is temporarily blocked due to recent penalties.
 */
public class PatientBlockedException extends RuntimeException {

    /**
     * Creates the exception for a blocked patient.
     *
     * @param patientId blocked patient identifier
     */
    public PatientBlockedException(UUID patientId) {
        super("Patient is blocked from scheduling appointments: " + patientId);
    }
}
