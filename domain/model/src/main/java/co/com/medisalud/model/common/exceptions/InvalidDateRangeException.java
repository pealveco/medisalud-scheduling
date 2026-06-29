package co.com.medisalud.model.common.exceptions;

/**
 * Exception thrown when a date range violates the expected ordering.
 */
public class InvalidDateRangeException extends RuntimeException {

    /**
     * Creates an invalid date range exception.
     *
     * @param message explanation of the invalid range
     */
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
