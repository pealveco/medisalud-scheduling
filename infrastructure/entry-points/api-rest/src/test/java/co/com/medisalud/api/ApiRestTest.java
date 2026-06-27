package co.com.medisalud.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class ApiRestTest {

    @Autowired
    private WebApplicationContext context;

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
    void shouldReturnNotFoundForInvalidPath() throws Exception {
        client.perform(get("/api/invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
}
