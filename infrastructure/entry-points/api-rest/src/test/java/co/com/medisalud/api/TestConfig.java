package co.com.medisalud.api;

import co.com.medisalud.api.config.CorsConfig;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.usecase.createdoctor.CreateDoctorUseCase;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "co.com.medisalud.api",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = CorsConfig.class
        ))
@Import({ApiRest.class, CreateDoctorUseCase.class})
public class TestConfig {

    @Bean
    public DoctorRepository doctorRepository() {
        return doctor -> doctor;
    }
}
