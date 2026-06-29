package co.com.medisalud.usecase.createappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.InvalidAppointmentSlotException;
import co.com.medisalud.model.appointment.exceptions.OutsideWorkingHoursException;
import co.com.medisalud.model.appointment.exceptions.PatientBlockedException;
import co.com.medisalud.model.appointment.exceptions.SlotConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import co.com.medisalud.usecase.common.WorkingHoursPolicy;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case responsible for scheduling appointments in available working-hour slots.
 */
@RequiredArgsConstructor
public class CreateAppointmentUseCase {

    private static final String DOCTOR_RESOURCE_NAME = "Doctor";
    private static final String PATIENT_RESOURCE_NAME = "Patient";
    private static final int MIN_PENALTIES_TO_BLOCK_PATIENT = 3;
    private static final int PENALTY_WINDOW_DAYS = 30;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PenaltyRepository penaltyRepository;

    /**
     * Schedules an appointment after validating existence, working hours, conflicts, and patient block status.
     *
     * @param appointment appointment request data
     * @return scheduled appointment
     * @throws ResourceNotFoundException when doctor or patient does not exist
     * @throws InvalidAppointmentSlotException when the requested slot is malformed or patient birth date is future
     * @throws OutsideWorkingHoursException when the requested slot is outside clinic working hours
     * @throws PatientBlockedException when the patient has three or more recent penalties
     * @throws SlotConflictException when doctor or patient already has a conflicting appointment
     */
    public Appointment createAppointment(Appointment appointment) {
        Patient patient = patientRepository.findById(appointment.getPatientId());
        if (patient == null) {
            throw new ResourceNotFoundException(PATIENT_RESOURCE_NAME, appointment.getPatientId());
        }
        if (doctorRepository.findById(appointment.getDoctorId()) == null) {
            throw new ResourceNotFoundException(DOCTOR_RESOURCE_NAME, appointment.getDoctorId());
        }

        validatePatientBirthDate(patient);
        WorkingHoursPolicy.validateSlotAlignment(appointment.getDateTime());
        WorkingHoursPolicy.validateWorkingHours(appointment.getDateTime());
        validatePatientBlock(appointment.getPatientId());
        validatePatientAppointmentConflict(appointment);
        validateDoctorSlotConflict(appointment);

        Appointment appointmentToSave = appointment.toBuilder()
                .id(appointment.getId() == null ? UUID.randomUUID() : appointment.getId())
                .status(AppointmentStatus.SCHEDULED)
                .cancelledAt(null)
                .build();

        return appointmentRepository.save(appointmentToSave);
    }

    private static void validatePatientBirthDate(Patient patient) {
        if (patient.getBirthDate() != null && patient.getBirthDate().isAfter(LocalDate.now())) {
            throw new InvalidAppointmentSlotException("Patient birth date cannot be in the future");
        }
    }

    private void validatePatientBlock(UUID patientId) {
        LocalDateTime lowerBound = LocalDateTime.now().minusDays(PENALTY_WINDOW_DAYS);
        long recentPenalties = penaltyRepository.countByPatientIdAndCreatedAtGreaterThanEqual(patientId, lowerBound);
        if (recentPenalties >= MIN_PENALTIES_TO_BLOCK_PATIENT) {
            throw new PatientBlockedException(patientId);
        }
    }

    private void validatePatientAppointmentConflict(Appointment appointment) {
        boolean patientAlreadyHasAppointment = appointmentRepository.existsScheduledByPatientIdAndDoctorIdAndDateTime(
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime()
        );
        if (patientAlreadyHasAppointment) {
            throw new SlotConflictException("Patient already has an appointment with this doctor at "
                    + appointment.getDateTime());
        }
    }

    private void validateDoctorSlotConflict(Appointment appointment) {
        boolean doctorAlreadyBooked = appointmentRepository.existsScheduledByDoctorIdAndDateTime(
                appointment.getDoctorId(),
                appointment.getDateTime()
        );
        if (doctorAlreadyBooked) {
            throw new SlotConflictException("Doctor already has an appointment at " + appointment.getDateTime());
        }
    }

}
