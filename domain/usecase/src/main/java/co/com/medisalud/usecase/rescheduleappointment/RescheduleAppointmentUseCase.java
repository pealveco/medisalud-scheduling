package co.com.medisalud.usecase.rescheduleappointment;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.exceptions.AppointmentStateConflictException;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentcancellation.AppointmentCancellation;
import co.com.medisalud.model.appointmentreschedule.AppointmentReschedule;
import co.com.medisalud.model.common.exceptions.ResourceNotFoundException;
import co.com.medisalud.usecase.cancelappointment.CancelAppointmentUseCase;
import co.com.medisalud.usecase.createappointment.CreateAppointmentUseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case responsible for rescheduling an existing appointment.
 */
@RequiredArgsConstructor
public class RescheduleAppointmentUseCase {

    private static final String APPOINTMENT_RESOURCE_NAME = "Appointment";

    private final AppointmentRepository appointmentRepository;
    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    /**
     * Reschedules a scheduled appointment to a new date-time.
     *
     * @param appointmentId original appointment identifier
     * @param newDateTime new appointment slot
     * @return rescheduling result with the cancelled original appointment and the new scheduled appointment
     * @throws ResourceNotFoundException when the original appointment does not exist
     * @throws AppointmentStateConflictException when the original appointment is not scheduled
     */
    public AppointmentReschedule rescheduleAppointment(UUID appointmentId, LocalDateTime newDateTime) {
        Appointment originalAppointment = appointmentRepository.findById(appointmentId);
        if (originalAppointment == null) {
            throw new ResourceNotFoundException(APPOINTMENT_RESOURCE_NAME, appointmentId);
        }
        if (AppointmentStatus.SCHEDULED != originalAppointment.getStatus()) {
            throw new AppointmentStateConflictException("Only scheduled appointments can be rescheduled");
        }

        Appointment newAppointment = createAppointmentUseCase.createAppointment(Appointment.builder()
                .patientId(originalAppointment.getPatientId())
                .doctorId(originalAppointment.getDoctorId())
                .dateTime(newDateTime)
                .build());
        AppointmentCancellation cancellation = cancelAppointmentUseCase.cancelAppointment(appointmentId);

        return AppointmentReschedule.builder()
                .originalAppointment(cancellation.getAppointment())
                .penaltyApplied(cancellation.isPenaltyApplied())
                .newAppointment(newAppointment)
                .build();
    }
}
