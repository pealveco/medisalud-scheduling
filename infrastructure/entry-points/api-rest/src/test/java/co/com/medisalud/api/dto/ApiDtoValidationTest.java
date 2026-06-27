package co.com.medisalud.api.dto;

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
import co.com.medisalud.api.dto.response.SlotResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiDtoValidationTest {

    private static final String DOMAIN_PACKAGE_PREFIX = "co.com.medisalud.model";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidDoctorRequest() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Laura Gomez",
                "Cardiology",
                "3001234567",
                "laura.gomez@medisalud.com"
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidDoctorRequest() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Al",
                "",
                "123",
                "invalid-email"
        );

        Set<ConstraintViolation<CreateDoctorRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "fullName");
        assertHasViolation(violations, "specialty");
        assertHasViolation(violations, "phone");
        assertHasViolation(violations, "email");
    }

    @Test
    void shouldAcceptValidPatientRequest() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Mateo Perez",
                "123456789",
                "3107654321",
                "mateo.perez@medisalud.com",
                LocalDate.now().minusYears(30)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidPatientRequest() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Ma",
                "123",
                "",
                "",
                LocalDate.now().plusDays(1)
        );

        Set<ConstraintViolation<CreatePatientRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "fullName");
        assertHasViolation(violations, "documentId");
        assertHasViolation(violations, "phone");
        assertHasViolation(violations, "email");
        assertHasViolation(violations, "birthDate");
    }

    @Test
    void shouldAcceptValidAppointmentRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidAppointmentRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                null,
                null,
                LocalDateTime.now().minusMinutes(1)
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "patientId");
        assertHasViolation(violations, "doctorId");
        assertHasViolation(violations, "dateTime");
    }

    @Test
    void shouldAcceptValidRescheduleAppointmentRequest() {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(
                LocalDateTime.now().plusDays(1)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidRescheduleAppointmentRequest() {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(
                LocalDateTime.now().minusMinutes(1)
        );

        Set<ConstraintViolation<RescheduleAppointmentRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "newDateTime");
    }

    @Test
    void shouldKeepResponseDtosIndependentFromDomainModels() {
        List<Class<?>> responseTypes = List.of(
                AppointmentStatusDto.class,
                DoctorResponse.class,
                PatientResponse.class,
                AppointmentResponse.class,
                SlotResponse.class,
                AvailabilityDayResponse.class,
                CancelAppointmentResponse.class,
                RescheduleAppointmentResponse.class
        );

        responseTypes.forEach(ApiDtoValidationTest::assertDoesNotExposeDomainType);
    }

    private static <T> void assertHasViolation(Set<ConstraintViolation<T>> violations, String fieldName) {
        boolean fieldHasViolation = violations.stream()
                .anyMatch(violation -> fieldName.equals(violation.getPropertyPath().toString()));

        assertTrue(fieldHasViolation);
    }

    private static void assertDoesNotExposeDomainType(Class<?> responseType) {
        for (Field field : responseType.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            Package fieldPackage = fieldType.getPackage();
            boolean isDomainType = fieldPackage != null
                    && fieldPackage.getName().startsWith(DOMAIN_PACKAGE_PREFIX);

            assertFalse(isDomainType);
        }
    }
}
