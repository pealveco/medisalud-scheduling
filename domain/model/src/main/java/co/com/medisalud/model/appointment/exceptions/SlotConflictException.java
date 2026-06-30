package co.com.medisalud.model.appointment.exceptions;

/**
 * Exception thrown when an appointment slot is already occupied.
 */
public class SlotConflictException extends RuntimeException {

    /**
     * Creates the exception with a clear business detail.
     *
     * @param message explanation of the slot conflict
     */
    public SlotConflictException(String message) {
        super(message);
    }
}
