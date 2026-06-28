package co.com.medisalud.api.exception;

import co.com.medisalud.model.appointment.exceptions.InvalidAppointmentSlotException;
import co.com.medisalud.model.appointment.exceptions.OutsideWorkingHoursException;
import co.com.medisalud.model.appointment.exceptions.PatientBlockedException;
import co.com.medisalud.model.appointment.exceptions.SlotConflictException;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.patient.exceptions.PatientDocumentAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

/**
 * Maps API exceptions to RFC 7807 problem details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI VALIDATION_ERROR_TYPE = URI.create("https://medisalud.com/errors/validation");
    private static final URI PATIENT_DOCUMENT_CONFLICT_TYPE = URI.create("https://medisalud.com/errors/patient-document-already-exists");
    private static final URI RESOURCE_NOT_FOUND_TYPE = URI.create("https://medisalud.com/errors/resource-not-found");
    private static final URI INVALID_APPOINTMENT_SLOT_TYPE =
            URI.create("https://medisalud.com/errors/invalid-appointment-slot");
    private static final URI OUTSIDE_WORKING_HOURS_TYPE =
            URI.create("https://medisalud.com/errors/outside-working-hours");
    private static final URI SLOT_CONFLICT_TYPE = URI.create("https://medisalud.com/errors/slot-conflict");
    private static final URI PATIENT_BLOCKED_TYPE = URI.create("https://medisalud.com/errors/patient-blocked");

    /**
     * Converts request body validation failures to a problem detail response.
     *
     * @param exception validation exception raised by Spring MVC
     * @param request current web request
     * @return problem detail with field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );
        problemDetail.setType(VALIDATION_ERROR_TYPE);
        problemDetail.setTitle("Invalid request");
        problemDetail.setInstance(resolveInstance(request));
        problemDetail.setProperty("errors", resolveValidationErrors(exception));
        return problemDetail;
    }

    /**
     * Converts duplicated patient document failures to a conflict response.
     *
     * @param exception domain exception raised by the patient registration use case
     * @param request current web request
     * @return problem detail describing the document conflict
     */
    @ExceptionHandler(PatientDocumentAlreadyExistsException.class)
    public ProblemDetail handlePatientDocumentAlreadyExists(
            PatientDocumentAlreadyExistsException exception,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setType(PATIENT_DOCUMENT_CONFLICT_TYPE);
        problemDetail.setTitle("Patient document already exists");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    /**
     * Converts missing domain resources to not-found responses.
     *
     * @param exception domain not-found exception
     * @param request current web request
     * @return problem detail describing the missing resource
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setType(RESOURCE_NOT_FOUND_TYPE);
        problemDetail.setTitle("Resource not found");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    /**
     * Converts invalid appointment slot failures to bad request responses.
     *
     * @param exception domain invalid slot exception
     * @param request current web request
     * @return problem detail describing the invalid slot
     */
    @ExceptionHandler(InvalidAppointmentSlotException.class)
    public ProblemDetail handleInvalidAppointmentSlot(
            InvalidAppointmentSlotException exception,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setType(INVALID_APPOINTMENT_SLOT_TYPE);
        problemDetail.setTitle("Invalid appointment slot");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    /**
     * Converts outside working hours failures to bad request responses.
     *
     * @param exception domain working-hours exception
     * @param request current web request
     * @return problem detail describing the working-hours violation
     */
    @ExceptionHandler(OutsideWorkingHoursException.class)
    public ProblemDetail handleOutsideWorkingHours(
            OutsideWorkingHoursException exception,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setType(OUTSIDE_WORKING_HOURS_TYPE);
        problemDetail.setTitle("Outside working hours");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    /**
     * Converts slot conflicts to conflict responses.
     *
     * @param exception domain slot conflict exception
     * @param request current web request
     * @return problem detail describing the conflict
     */
    @ExceptionHandler(SlotConflictException.class)
    public ProblemDetail handleSlotConflict(SlotConflictException exception, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setType(SLOT_CONFLICT_TYPE);
        problemDetail.setTitle("Slot conflict");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    /**
     * Converts patient block failures to conflict responses.
     *
     * @param exception domain patient blocked exception
     * @param request current web request
     * @return problem detail describing the patient block
     */
    @ExceptionHandler(PatientBlockedException.class)
    public ProblemDetail handlePatientBlocked(PatientBlockedException exception, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setType(PATIENT_BLOCKED_TYPE);
        problemDetail.setTitle("Patient blocked");
        problemDetail.setInstance(resolveInstance(request));
        return problemDetail;
    }

    private static URI resolveInstance(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return URI.create(servletWebRequest.getRequest().getRequestURI());
        }
        return null;
    }

    private static List<ValidationErrorResponse> resolveValidationErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
    }
}
