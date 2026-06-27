package co.com.medisalud.usecase.createdoctor;

import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreateDoctorUseCaseTest {

    @Test
    void shouldGenerateIdWhenDoctorDoesNotHaveOne() {
        AtomicReference<Doctor> savedDoctor = new AtomicReference<>();
        DoctorRepository repository = doctor -> {
            savedDoctor.set(doctor);
            return doctor;
        };
        CreateDoctorUseCase useCase = new CreateDoctorUseCase(repository);
        Doctor doctor = Doctor.builder()
                .fullName("Laura Gomez")
                .specialty("Cardiology")
                .phone("3001234567")
                .email("laura.gomez@medisalud.com")
                .build();

        Doctor result = useCase.createDoctor(doctor);

        assertNotNull(result.getId());
        assertEquals(result.getId(), savedDoctor.get().getId());
        assertEquals("Laura Gomez", result.getFullName());
        assertEquals("Cardiology", result.getSpecialty());
        assertEquals("3001234567", result.getPhone());
        assertEquals("laura.gomez@medisalud.com", result.getEmail());
    }

    @Test
    void shouldKeepExistingIdWhenDoctorAlreadyHasOne() {
        UUID existingId = UUID.randomUUID();
        DoctorRepository repository = doctor -> doctor;
        CreateDoctorUseCase useCase = new CreateDoctorUseCase(repository);
        Doctor doctor = Doctor.builder()
                .id(existingId)
                .fullName("Laura Gomez")
                .specialty("Cardiology")
                .build();

        Doctor result = useCase.createDoctor(doctor);

        assertEquals(existingId, result.getId());
    }
}
