package co.com.medisalud.api.mapper;

import co.com.medisalud.api.dto.request.CreateDoctorRequest;
import co.com.medisalud.api.dto.response.DoctorResponse;
import co.com.medisalud.model.doctor.Doctor;
import org.springframework.stereotype.Component;

/**
 * Maps doctor API DTOs to domain models and domain models to API responses.
 */
@Component
public class DoctorMapper {

    /**
     * Converts a create doctor request into a domain model.
     *
     * @param request validated API request
     * @return doctor domain model without generated identifier
     */
    public Doctor toDomain(CreateDoctorRequest request) {
        return Doctor.builder()
                .fullName(request.fullName())
                .specialty(request.specialty())
                .phone(request.phone())
                .email(request.email())
                .build();
    }

    /**
     * Converts a doctor domain model into an API response.
     *
     * @param doctor registered doctor domain model
     * @return API response
     */
    public DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.getPhone(),
                doctor.getEmail()
        );
    }
}
