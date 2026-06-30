package co.com.medisalud.model.appointment.exceptions;

/**
 * Exception thrown when an appointment date-time is not aligned to a valid slot boundary.
 */
public class InvalidAppointmentSlotException extends RuntimeException {

    /**
     * Creates the exception with a clear business detail.
     *
     * @param message explanation of the invalid slot
     */
    public InvalidAppointmentSlotException(String message) {
        super(message);
    }
}
