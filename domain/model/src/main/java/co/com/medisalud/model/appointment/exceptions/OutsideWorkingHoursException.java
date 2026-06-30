package co.com.medisalud.model.appointment.exceptions;

/**
 * Exception thrown when an appointment is requested outside clinic working hours.
 */
public class OutsideWorkingHoursException extends RuntimeException {

    /**
     * Creates the exception with a clear business detail.
     *
     * @param message explanation of the working-hours violation
     */
    public OutsideWorkingHoursException(String message) {
        super(message);
    }
}
