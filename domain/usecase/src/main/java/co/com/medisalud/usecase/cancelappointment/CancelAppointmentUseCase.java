package co.com.medisalud.usecase.cancelappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.AppointmentStateConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentcancellation.AppointmentCancellation;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case responsible for cancelling scheduled appointments and applying late-cancellation penalties.
 */
@RequiredArgsConstructor
public class CancelAppointmentUseCase {

    private static final String APPOINTMENT_RESOURCE_NAME = "Appointment";
    private static final int MIN_HOURS_BEFORE_CANCELLATION_WITHOUT_PENALTY = 2;

    private final AppointmentRepository appointmentRepository;
    private final PenaltyRepository penaltyRepository;

    /**
     * Cancels a scheduled appointment and records a penalty when the cancellation is late.
     *
     * @param appointmentId appointment identifier
     * @return cancellation result with the cancelled appointment and penalty flag
     * @throws ResourceNotFoundException when the appointment does not exist
     * @throws AppointmentStateConflictException when the appointment is not scheduled
     */
    public AppointmentCancellation cancelAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException(APPOINTMENT_RESOURCE_NAME, appointmentId);
        }
        if (AppointmentStatus.SCHEDULED != appointment.getStatus()) {
            throw new AppointmentStateConflictException("Only scheduled appointments can be cancelled");
        }

        LocalDateTime cancelledAt = LocalDateTime.now();
        boolean penaltyApplied = shouldApplyPenalty(appointment, cancelledAt);
        Appointment cancelledAppointment = appointment.toBuilder()
                .status(AppointmentStatus.CANCELLED)
                .cancelledAt(cancelledAt)
                .build();

        Appointment savedAppointment = appointmentRepository.save(cancelledAppointment);
        if (penaltyApplied) {
            penaltyRepository.save(Penalty.builder()
                    .id(UUID.randomUUID())
                    .patientId(savedAppointment.getPatientId())
                    .appointmentId(savedAppointment.getId())
                    .createdAt(cancelledAt)
                    .build());
        }

        return AppointmentCancellation.builder()
                .appointment(savedAppointment)
                .penaltyApplied(penaltyApplied)
                .build();
    }

    private static boolean shouldApplyPenalty(Appointment appointment, LocalDateTime cancelledAt) {
        LocalDateTime cancellationLimit = cancelledAt.plusHours(MIN_HOURS_BEFORE_CANCELLATION_WITHOUT_PENALTY);
        return appointment.getDateTime().isBefore(cancellationLimit);
    }
}
