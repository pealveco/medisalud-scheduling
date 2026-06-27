package co.com.medisalud.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class ApiRestTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldCreateDoctorWhenPayloadIsValid() {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        client.post()
                .uri("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.fullName").isEqualTo("Laura Gomez")
                .jsonPath("$.specialty").isEqualTo("Cardiology")
                .jsonPath("$.phone").isEqualTo("3001234567")
                .jsonPath("$.email").isEqualTo("laura.gomez@medisalud.com");
    }

    @Test
    void shouldRejectDoctorRequestWhenFullNameIsTooShort() {
        String request = """
                {
                  "fullName": "Al",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError(request, "fullName");
    }

    @Test
    void shouldRejectDoctorRequestWhenFullNameIsTooLong() {
        String request = """
                {
                  "fullName": "%s",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """.formatted("A".repeat(101));

        assertBadRequestWithFieldError(request, "fullName");
    }

    @Test
    void shouldRejectDoctorRequestWhenSpecialtyIsBlank() {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "",
                  "phone": "3001234567",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError(request, "specialty");
    }

    @Test
    void shouldRejectDoctorRequestWhenEmailIsInvalid() {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "3001234567",
                  "email": "invalid-email"
                }
                """;

        assertBadRequestWithFieldError(request, "email");
    }

    @Test
    void shouldRejectDoctorRequestWhenPhoneHasLessThanSevenDigits() {
        String request = """
                {
                  "fullName": "Laura Gomez",
                  "specialty": "Cardiology",
                  "phone": "123456",
                  "email": "laura.gomez@medisalud.com"
                }
                """;

        assertBadRequestWithFieldError(request, "phone");
    }

    @Test
    void shouldReturnNotFoundForInvalidPath() {
        client.get()
                .uri("/api/invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    private void assertBadRequestWithFieldError(String request, String fieldName) {
        client.post()
                .uri("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors[?(@.field == '%s')]".formatted(fieldName)).exists();
    }
}
