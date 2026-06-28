package co.com.medisalud.api;
import co.com.medisalud.api.dto.request.CreateAppointmentRequest;
import co.com.medisalud.api.dto.request.CreateDoctorRequest;
import co.com.medisalud.api.dto.request.CreatePatientRequest;
import co.com.medisalud.api.dto.request.RescheduleAppointmentRequest;
import co.com.medisalud.api.dto.response.AppointmentResponse;
import co.com.medisalud.api.dto.response.AppointmentStatusDto;
import co.com.medisalud.api.dto.response.AvailabilityDayResponse;
import co.com.medisalud.api.dto.response.CancelAppointmentResponse;
import co.com.medisalud.api.dto.response.DoctorResponse;
import co.com.medisalud.api.dto.response.PatientResponse;
import co.com.medisalud.api.dto.response.RescheduleAppointmentResponse;
import co.com.medisalud.api.mapper.AppointmentMapper;
import co.com.medisalud.api.mapper.DoctorMapper;
import co.com.medisalud.api.mapper.PatientMapper;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.usecase.createappointment.CreateAppointmentUseCase;
import co.com.medisalud.usecase.createdoctor.CreateDoctorUseCase;
import co.com.medisalud.usecase.createpatient.CreatePatientUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller that defines the HTTP contracts for the appointment scheduling API.
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class ApiRest {

    private final CreateDoctorUseCase createDoctorUseCase;
    private final CreatePatientUseCase createPatientUseCase;
    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;

    /**
     * Creates a doctor from a validated request body.
     *
     * @param request doctor creation request
     * @return created doctor response
     */
    @PostMapping(path = "/doctors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        Doctor registeredDoctor = createDoctorUseCase.createDoctor(doctorMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorMapper.toResponse(registeredDoctor));
    }

    /**
     * Creates a patient from a validated request body.
     *
     * @param request patient creation request
     * @return created patient response
     */
    @PostMapping(path = "/patients", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        Patient registeredPatient = createPatientUseCase.createPatient(patientMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(registeredPatient));
    }

    /**
     * Schedules an appointment from a validated request body.
     *
     * @param request appointment scheduling request
     * @return scheduled appointment response
     */
    @PostMapping(path = "/appointments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        Appointment scheduledAppointment = createAppointmentUseCase.createAppointment(appointmentMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toResponse(scheduledAppointment));
    }

    /**
     * Returns available appointment slots for a doctor between two dates.
     *
     * @param id doctor identifier
     * @param startDate first date included in the availability search
     * @param endDate last date included in the availability search
     * @return doctor availability grouped by date
     */
    @GetMapping(path = "/doctors/{id}/availability")
    public ResponseEntity<List<AvailabilityDayResponse>> getDoctorAvailability(
            @PathVariable UUID id,
            @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return notImplemented();
    }

    /**
     * Cancels an existing appointment.
     *
     * @param id appointment identifier
     * @return cancelled appointment response
     */
    @DeleteMapping(path = "/appointments/{id}")
    public ResponseEntity<CancelAppointmentResponse> cancelAppointment(@PathVariable UUID id) {
        return notImplemented();
    }

    /**
     * Reschedules an existing appointment atomically.
     *
     * @param id original appointment identifier
     * @param request reschedule request
     * @return rescheduling response containing the original and new appointments
     */
    @PutMapping(path = "/appointments/{id}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RescheduleAppointmentResponse> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return notImplemented();
    }

    /**
     * Lists appointments using optional combinable filters.
     *
     * @param doctorId optional doctor identifier filter
     * @param patientId optional patient identifier filter
     * @param status optional appointment status filter
     * @param startDate optional start date-time filter
     * @param endDate optional end date-time filter
     * @return appointments matching the supplied filters
     */
    @GetMapping(path = "/appointments")
    public ResponseEntity<List<AppointmentResponse>> listAppointments(
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) AppointmentStatusDto status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {
        return notImplemented();
    }

    private static <T> ResponseEntity<T> notImplemented() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Use case not implemented yet");
    }
}
