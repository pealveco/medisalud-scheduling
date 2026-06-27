package co.com.medisalud.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = TestConfig.class)
class ApiRestTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldRejectInvalidDoctorRequest() {
        String request = """
                {
                  "fullName": "Al",
                  "specialty": "",
                  "phone": "123",
                  "email": "invalid-email"
                }
                """;

        client.post()
                .uri("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldAcceptValidDoctorRequestContract() {
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
                .expectStatus().isEqualTo(501);
    }

    @Test
    void shouldReturnNotFoundForInvalidPath() {
        client.get()
                .uri("/api/invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
