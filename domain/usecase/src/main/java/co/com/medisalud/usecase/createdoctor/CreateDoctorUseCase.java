package co.com.medisalud.usecase.createdoctor;

import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case responsible for registering doctors in the clinic schedule system.
 */
@RequiredArgsConstructor
public class CreateDoctorUseCase {

    private final DoctorRepository doctorRepository;

    /**
     * Registers a doctor and generates an identifier when it is not already present.
     *
     * @param doctor doctor data to register
     * @return registered doctor with identifier
     */
    public Doctor createDoctor(Doctor doctor) {
        Doctor doctorToSave = doctor.getId() == null
                ? doctor.toBuilder().id(UUID.randomUUID()).build()
                : doctor;

        return doctorRepository.save(doctorToSave);
    }
}
