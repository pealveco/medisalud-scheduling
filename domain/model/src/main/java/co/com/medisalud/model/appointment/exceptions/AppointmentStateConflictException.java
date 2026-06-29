package co.com.medisalud.model.appointment.exceptions;

/**
 * Exception thrown when an appointment operation is not allowed for the current appointment status.
 */
public class AppointmentStateConflictException extends RuntimeException {

    /**
     * Creates the exception with a clear business detail.
     *
     * @param message explanation of the invalid appointment state
     */
    public AppointmentStateConflictException(String message) {
        super(message);
    }
}
