package co.com.medisalud.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the generated OpenAPI documentation metadata.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Provides API metadata displayed by Swagger UI.
     *
     * @return OpenAPI metadata configuration
     */
    @Bean
    public OpenAPI medisSaludOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediSalud Scheduling API")
                        .version("1.0.0")
                        .description("REST API for medical appointment scheduling."))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local runtime")));
    }
}
