package co.com.medisalud.api.exception;

/**
 * Field-level validation error exposed in RFC 7807 problem details.
 *
 * @param field invalid field name
 * @param message validation error message
 */
public record ValidationErrorResponse(
        String field,
        String message
) {
}
