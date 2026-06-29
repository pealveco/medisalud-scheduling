package co.com.medisalud.api;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class ApiRestTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private MockMvc client;

    @BeforeEach
    void setup() {
        client = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldCreateDoctorWhenPayloadIsValid() throws Exception {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        client.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Laura Gomez"))
                .andExpect(jsonPath("$.specialty").value("Cardiology"))
                .andExpect(jsonPath("$.phone").value("3001234567"))
                .andExpect(jsonPath("$.email").value("laura.gomez@medisalud.com"));
    }

    @Test
    void shouldRejectDoctorRequestWhenFullNameIsTooShort() throws Exception {
        String request = """
                {
                  "fullName": "Al",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError("/api/doctors", request, "fullName");
    }

    @Test
    void shouldRejectDoctorRequestWhenFullNameIsTooLong() throws Exception {
        String request = """
                {
                  "fullName": "%s",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """.formatted("A".repeat(101));

        assertBadRequestWithFieldError("/api/doctors", request, "fullName");
    }

    @Test
    void shouldRejectDoctorRequestWhenSpecialtyIsBlank() throws Exception {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError("/api/doctors", request, "specialty");
    }

    @Test
    void shouldRejectDoctorRequestWhenEmailIsInvalid() throws Exception {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "invalid-email"
                }
                """;

        assertBadRequestWithFieldError("/api/doctors", request, "email");
    }

    @Test
    void shouldRejectDoctorRequestWhenPhoneHasLessThanSevenDigits() throws Exception {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "123456",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError("/api/doctors", request, "phone");
    }

    @Test
    void shouldReturnProblemDetailWhenUnexpectedErrorOccurs() throws Exception {
        String request = """
                {
                  "fullName": "Trigger Failure",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "trigger.failure@medisalud.com"
                }
                """;

        client.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/internal-server-error"))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Unexpected internal server error"))
                .andExpect(jsonPath("$.instance").value("/api/doctors"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void shouldCreatePatientWhenPayloadIsValid() throws Exception {
        String request = """
                {
                  "fullName": "Mateo Perez",
                  "documentId": "PATIENT1001",
                  "phone": "3107654321",
                  "email": "mateo.perez@medisalud.com",
                  "birthDate": "1990-05-12"
                }
                """;

        client.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Mateo Perez"))
                .andExpect(jsonPath("$.documentId").value("PATIENT1001"))
                .andExpect(jsonPath("$.phone").value("3107654321"))
                .andExpect(jsonPath("$.email").value("mateo.perez@medisalud.com"))
                .andExpect(jsonPath("$.birthDate").value("1990-05-12"));
    }

    @Test
    void shouldRejectPatientWhenDocumentIdAlreadyExists() throws Exception {
        String request = """
                {
                  "fullName": "Sofia Torres",
                  "documentId": "PATIENT2001",
                  "phone": "3207654321",
                  "email": "sofia.torres@medisalud.com"
                }
                """;

        client.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated());

        client.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/patient-document-already-exists"));
    }

    @Test
    void shouldRejectPatientRequestWhenDocumentIdIsTooShort() throws Exception {
        String request = """
                {
                  "fullName": "Mateo Perez",
                  "documentId": "123456",
                  "phone": "3107654321",
                  "email": "mateo.perez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError("/api/patients", request, "documentId");
    }

    @Test
    void shouldRejectPatientRequestWhenEmailOrPhoneAreMissing() throws Exception {
        String request = """
                {
                  "fullName": "Mateo Perez",
                  "documentId": "PATIENT3001"
                }
                """;

        assertBadRequestWithFieldError("/api/patients", request, "email");
        assertBadRequestWithFieldError("/api/patients", request, "phone");
    }

    @Test
    void shouldRejectPatientRequestWhenEmailOrPhoneAreInvalid() throws Exception {
        String request = """
                {
                  "fullName": "Mateo Perez",
                  "documentId": "PATIENT4001",
                  "phone": "123456",
                  "email": "invalid-email"
                }
                """;

        assertBadRequestWithFieldError("/api/patients", request, "email");
        assertBadRequestWithFieldError("/api/patients", request, "phone");
    }

    @Test
    void shouldRejectPatientRequestWhenBirthDateIsFuture() throws Exception {
        String request = """
                {
                  "fullName": "Mateo Perez",
                  "documentId": "PATIENT5001",
                  "phone": "3107654321",
                  "email": "mateo.perez@medisalud.com",
                  "birthDate": "2999-01-01"
                }
                """;

        assertBadRequestWithFieldError("/api/patients", request, "birthDate");
    }

    @Test
    void shouldCreateAppointmentWhenPayloadIsValid() throws Exception {
        LocalDateTime dateTime = nextWorkingDateTime(LocalTime.of(8, 0));
        String request = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patientId").value(TestConfig.EXISTING_PATIENT_ID.toString()))
                .andExpect(jsonPath("$.doctorId").value(TestConfig.EXISTING_DOCTOR_ID.toString()))
                .andExpect(jsonPath("$.dateTime").value(DATE_TIME_FORMATTER.format(dateTime)))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldRejectAppointmentWhenPatientDoesNotExist() throws Exception {
        String request = appointmentRequest(UUID.randomUUID(), TestConfig.EXISTING_DOCTOR_ID,
                nextWorkingDateTime(LocalTime.of(8, 30)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/resource-not-found"));
    }

    @Test
    void shouldRejectAppointmentWhenDoctorDoesNotExist() throws Exception {
        String request = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, UUID.randomUUID(),
                nextWorkingDateTime(LocalTime.of(9, 0)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/resource-not-found"));
    }

    @Test
    void shouldRejectAppointmentWhenDateTimeIsNotAlignedToThirtyMinutes() throws Exception {
        String request = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID,
                nextWorkingDateTime(LocalTime.of(9, 15)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/invalid-appointment-slot"));
    }

    @Test
    void shouldRejectAppointmentOutsideWorkingHours() throws Exception {
        String request = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID,
                nextSundayDateTime(LocalTime.of(10, 0)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/outside-working-hours"));
    }

    @Test
    void shouldRejectAppointmentBeforeOpeningTime() throws Exception {
        String request = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID,
                nextWorkingDateTime(LocalTime.of(7, 30)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/outside-working-hours"));
    }

    @Test
    void shouldRejectAppointmentWhenDoctorSlotIsAlreadyTaken() throws Exception {
        LocalDateTime dateTime = nextWorkingDateTime(LocalTime.of(10, 0));
        String firstRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);
        String secondRequest = appointmentRequest(TestConfig.OTHER_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isCreated());

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(secondRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/slot-conflict"));
    }

    @Test
    void shouldCreateAppointmentWhenDoctorHasAppointmentInImmediatelyNextSlot() throws Exception {
        LocalDateTime firstSlot = nextWorkingDateTime(LocalTime.of(12, 0));
        LocalDateTime nextSlot = firstSlot.plusMinutes(30);
        String firstRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, firstSlot);
        String secondRequest = appointmentRequest(TestConfig.OTHER_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, nextSlot);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isCreated());

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(secondRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateTime").value(DATE_TIME_FORMATTER.format(nextSlot)))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldCreateAppointmentWhenPatientBirthDateIsMissing() throws Exception {
        LocalDateTime dateTime = nextWorkingDateTime(LocalTime.of(14, 0));
        String request = appointmentRequest(TestConfig.OTHER_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(TestConfig.OTHER_PATIENT_ID.toString()))
                .andExpect(jsonPath("$.dateTime").value(DATE_TIME_FORMATTER.format(dateTime)))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldRejectAppointmentWhenPatientAlreadyHasAppointmentWithSameDoctorAtSlot() throws Exception {
        LocalDateTime dateTime = nextWorkingDateTime(LocalTime.of(15, 0));
        String firstRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);
        String secondRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isCreated());

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(secondRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/slot-conflict"));
    }

    @Test
    void shouldCreateAppointmentWhenPatientHasAppointmentWithDifferentDoctorAtSameSlot() throws Exception {
        LocalDateTime dateTime = nextWorkingDateTime(LocalTime.of(16, 0));
        String firstRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID, dateTime);
        String secondRequest = appointmentRequest(TestConfig.EXISTING_PATIENT_ID, TestConfig.OTHER_DOCTOR_ID, dateTime);

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isCreated());

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(secondRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(TestConfig.EXISTING_PATIENT_ID.toString()))
                .andExpect(jsonPath("$.doctorId").value(TestConfig.OTHER_DOCTOR_ID.toString()))
                .andExpect(jsonPath("$.dateTime").value(DATE_TIME_FORMATTER.format(dateTime)))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldRejectAppointmentWhenPatientIsBlocked() throws Exception {
        String request = appointmentRequest(TestConfig.BLOCKED_PATIENT_ID, TestConfig.EXISTING_DOCTOR_ID,
                nextWorkingDateTime(LocalTime.of(11, 0)));

        client.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/patient-blocked"));
    }

    @Test
    void shouldCancelScheduledAppointmentWithoutPenalty() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(appointmentId, TestConfig.EXISTING_DOCTOR_ID,
                LocalDateTime.now().plusHours(3), AppointmentStatus.SCHEDULED));

        client.perform(delete("/api/appointments/{id}", appointmentId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.penaltyApplied").value(false));
    }

    @Test
    void shouldCancelScheduledAppointmentWithPenaltyWhenCancellationIsLate() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(appointmentId, TestConfig.EXISTING_DOCTOR_ID,
                LocalDateTime.now().plusMinutes(90), AppointmentStatus.SCHEDULED));

        client.perform(delete("/api/appointments/{id}", appointmentId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.penaltyApplied").value(true));
    }

    @Test
    void shouldRejectCancellationWhenAppointmentIsAlreadyCancelled() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(appointmentId, TestConfig.EXISTING_DOCTOR_ID,
                LocalDateTime.now().plusHours(3), AppointmentStatus.CANCELLED));

        client.perform(delete("/api/appointments/{id}", appointmentId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/appointment-state-conflict"));
    }

    @Test
    void shouldReturnNotFoundWhenCancellingMissingAppointment() throws Exception {
        client.perform(delete("/api/appointments/{id}", UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/resource-not-found"));
    }

    @Test
    void shouldReturnDoctorAvailabilityExcludingOccupiedSlots() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 1);
        appointmentRepository.save(appointment(TestConfig.EXISTING_DOCTOR_ID, date.atTime(8, 0),
                AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(TestConfig.EXISTING_DOCTOR_ID, date.atTime(8, 30),
                AppointmentStatus.CANCELLED));

        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", date.toString())
                .param("endDate", date.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value(date.toString()))
                .andExpect(jsonPath("$[0].slots.length()").value(19))
                .andExpect(jsonPath("$[0].slots[0].start").value("08:30:00"))
                .andExpect(jsonPath("$[0].slots[0].end").value("09:00:00"));
    }

    @Test
    void shouldReturnSaturdayAvailabilityOnlyUntilThirteen() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 4);

        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", date.toString())
                .param("endDate", date.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value(date.toString()))
                .andExpect(jsonPath("$[0].slots.length()").value(10))
                .andExpect(jsonPath("$[0].slots[0].start").value("08:00:00"))
                .andExpect(jsonPath("$[0].slots[9].start").value("12:30:00"))
                .andExpect(jsonPath("$[0].slots[9].end").value("13:00:00"));
    }

    @Test
    void shouldSkipSundayInDoctorAvailability() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 5);

        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", date.toString())
                .param("endDate", date.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectDoctorAvailabilityWhenDateRangeIsInvalid() throws Exception {
        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", "2026-07-02")
                .param("endDate", "2026-07-01")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/invalid-date-range"));
    }

    @Test
    void shouldRejectDoctorAvailabilityWhenStartDateIsMissing() throws Exception {
        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("endDate", "2026-07-01")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/validation"))
                .andExpect(jsonPath("$.errors[?(@.field == 'startDate')]").exists());
    }

    @Test
    void shouldRejectDoctorAvailabilityWhenEndDateIsMissing() throws Exception {
        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", "2026-07-01")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/validation"))
                .andExpect(jsonPath("$.errors[?(@.field == 'endDate')]").exists());
    }

    @Test
    void shouldRejectDoctorAvailabilityWhenDateFormatIsInvalid() throws Exception {
        client.perform(get("/api/doctors/{id}/availability", TestConfig.EXISTING_DOCTOR_ID)
                .param("startDate", "01-07-2026")
                .param("endDate", "2026-07-01")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/validation"))
                .andExpect(jsonPath("$.errors[?(@.field == 'startDate')]").exists());
    }

    @Test
    void shouldReturnNotFoundWhenDoctorAvailabilityDoctorDoesNotExist() throws Exception {
        client.perform(get("/api/doctors/{id}/availability", UUID.randomUUID())
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-01")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/resource-not-found"));
    }

    @Test
    void shouldListAppointmentsWithoutFilters() throws Exception {
        UUID firstAppointmentId = UUID.randomUUID();
        UUID secondAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(firstAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 1, 8, 0), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(secondAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 1, 8, 30), AppointmentStatus.CANCELLED));

        client.perform(get("/api/appointments")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(firstAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(secondAppointmentId)).exists());
    }

    @Test
    void shouldListAppointmentsByDoctorId() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID matchingAppointmentId = UUID.randomUUID();
        UUID excludedAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(matchingAppointmentId, doctorId, UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 2, 8, 0), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(excludedAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 2, 8, 30), AppointmentStatus.SCHEDULED));

        client.perform(get("/api/appointments")
                .param("doctorId", doctorId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(matchingAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedAppointmentId)).doesNotExist());
    }

    @Test
    void shouldListAppointmentsByPatientId() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID matchingAppointmentId = UUID.randomUUID();
        UUID excludedAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(matchingAppointmentId, UUID.randomUUID(), patientId,
                LocalDateTime.of(2026, 9, 3, 8, 0), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(excludedAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 3, 8, 30), AppointmentStatus.SCHEDULED));

        client.perform(get("/api/appointments")
                .param("patientId", patientId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(matchingAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedAppointmentId)).doesNotExist());
    }

    @Test
    void shouldListAppointmentsByStatus() throws Exception {
        UUID matchingAppointmentId = UUID.randomUUID();
        UUID excludedAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(matchingAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 4, 8, 0), AppointmentStatus.CANCELLED));
        appointmentRepository.save(appointment(excludedAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 4, 8, 30), AppointmentStatus.SCHEDULED));

        client.perform(get("/api/appointments")
                .param("status", "CANCELLED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(matchingAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedAppointmentId)).doesNotExist());
    }

    @Test
    void shouldListAppointmentsByDateRange() throws Exception {
        UUID matchingAppointmentId = UUID.randomUUID();
        UUID excludedAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(matchingAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 5, 10, 0), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(excludedAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 6, 10, 0), AppointmentStatus.SCHEDULED));

        client.perform(get("/api/appointments")
                .param("startDate", "2026-09-05T00:00:00")
                .param("endDate", "2026-09-05T23:59:59")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(matchingAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedAppointmentId)).doesNotExist());
    }

    @Test
    void shouldListAppointmentsByStatusAndDateRangeIntersection() throws Exception {
        UUID matchingAppointmentId = UUID.randomUUID();
        UUID excludedByStatusAppointmentId = UUID.randomUUID();
        UUID excludedByRangeAppointmentId = UUID.randomUUID();
        appointmentRepository.save(appointment(matchingAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 7, 10, 0), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(appointment(excludedByStatusAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 7, 11, 0), AppointmentStatus.CANCELLED));
        appointmentRepository.save(appointment(excludedByRangeAppointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 8, 10, 0), AppointmentStatus.SCHEDULED));

        client.perform(get("/api/appointments")
                .param("status", "SCHEDULED")
                .param("startDate", "2026-09-07T00:00:00")
                .param("endDate", "2026-09-07T23:59:59")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(matchingAppointmentId)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedByStatusAppointmentId)).doesNotExist())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(excludedByRangeAppointmentId)).doesNotExist());
    }

    @Test
    void shouldRejectAppointmentListingWhenDateRangeIsInvalid() throws Exception {
        client.perform(get("/api/appointments")
                .param("startDate", "2026-09-08T00:00:00")
                .param("endDate", "2026-09-07T23:59:59")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/invalid-date-range"));
    }

    @Test
    void shouldReturnNotFoundForInvalidPath() throws Exception {
        client.perform(get("/api/invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://medisalud.com/errors/resource-not-found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value("/api/invalid"));
    }

    private void assertBadRequestWithFieldError(String path, String request, String fieldName) throws Exception {
        client.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@.field == '%s')]".formatted(fieldName)).exists());
    }

    private static String appointmentRequest(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        return """
                {
                  "patientId": "%s",
                  "doctorId": "%s",
                  "dateTime": "%s"
                }
                """.formatted(patientId, doctorId, DATE_TIME_FORMATTER.format(dateTime));
    }

    private static Appointment appointment(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status) {
        return appointment(UUID.randomUUID(), doctorId, dateTime, status);
    }

    private static Appointment appointment(
            UUID appointmentId,
            UUID doctorId,
            LocalDateTime dateTime,
            AppointmentStatus status) {
        return appointment(appointmentId, doctorId, TestConfig.EXISTING_PATIENT_ID, dateTime, status);
    }

    private static Appointment appointment(
            UUID appointmentId,
            UUID doctorId,
            UUID patientId,
            LocalDateTime dateTime,
            AppointmentStatus status) {
        return Appointment.builder()
                .id(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    private static LocalDateTime nextWorkingDateTime(LocalTime time) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.getDayOfWeek() == DayOfWeek.SATURDAY
                || colombianHolidays(date.getYear()).contains(date)) {
            date = date.plusDays(1);
        }
        return LocalDateTime.of(date, time);
    }

    private static LocalDateTime nextSundayDateTime(LocalTime time) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return LocalDateTime.of(date, time);
    }

    private static Set<LocalDate> colombianHolidays(int year) {
        LocalDate easterSunday = easterSunday(year);
        return Set.of(
                LocalDate.of(year, Month.JANUARY, 1),
                nextMonday(LocalDate.of(year, Month.JANUARY, 6)),
                nextMonday(LocalDate.of(year, Month.MARCH, 19)),
                easterSunday.minusDays(3),
                easterSunday.minusDays(2),
                LocalDate.of(year, Month.MAY, 1),
                nextMonday(easterSunday.plusDays(43)),
                nextMonday(easterSunday.plusDays(64)),
                nextMonday(easterSunday.plusDays(71)),
                nextMonday(LocalDate.of(year, Month.JUNE, 29)),
                LocalDate.of(year, Month.JULY, 20),
                LocalDate.of(year, Month.AUGUST, 7),
                nextMonday(LocalDate.of(year, Month.AUGUST, 15)),
                nextMonday(LocalDate.of(year, Month.OCTOBER, 12)),
                nextMonday(LocalDate.of(year, Month.NOVEMBER, 1)),
                nextMonday(LocalDate.of(year, Month.NOVEMBER, 11)),
                LocalDate.of(year, Month.DECEMBER, 8),
                LocalDate.of(year, Month.DECEMBER, 25)
        );
    }

    private static LocalDate nextMonday(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDate easterSunday(int year) {
        int remainderA = year % 19;
        int century = year / 100;
        int yearOfCentury = year % 100;
        int leapCenturyAdjustment = century / 4;
        int centuryRemainder = century % 4;
        int correction = (century + 8) / 25;
        int moonCorrection = (century - correction + 1) / 3;
        int epact = (19 * remainderA + century - leapCenturyAdjustment - moonCorrection + 15) % 30;
        int yearOfCenturyLeap = yearOfCentury / 4;
        int yearOfCenturyRemainder = yearOfCentury % 4;
        int weekdayCorrection = (32 + 2 * centuryRemainder + 2 * yearOfCenturyLeap
                - epact - yearOfCenturyRemainder) % 7;
        int finalCorrection = (remainderA + 11 * epact + 22 * weekdayCorrection) / 451;
        int month = (epact + weekdayCorrection - 7 * finalCorrection + 114) / 31;
        int day = ((epact + weekdayCorrection - 7 * finalCorrection + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
