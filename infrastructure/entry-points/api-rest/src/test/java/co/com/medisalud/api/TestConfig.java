package co.com.medisalud.api;

import co.com.medisalud.api.config.CorsConfig;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.usecase.createdoctor.CreateDoctorUseCase;
import co.com.medisalud.usecase.createpatient.CreatePatientUseCase;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "co.com.medisalud.api",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = CorsConfig.class
        ))
@Import({ApiRest.class, CreateDoctorUseCase.class, CreatePatientUseCase.class})
public class TestConfig {

    @Bean
    public DoctorRepository doctorRepository() {
        return doctor -> doctor;
    }

    @Bean
    public PatientRepository patientRepository() {
        return new InMemoryPatientRepository();
    }

    private static class InMemoryPatientRepository implements PatientRepository {

        private final Map<String, Patient> patientsByDocumentId = new ConcurrentHashMap<>();

        @Override
        public Patient save(Patient patient) {
            patientsByDocumentId.put(patient.getDocumentId(), patient);
            return patient;
        }

        @Override
        public boolean existsByDocumentId(String documentId) {
            return patientsByDocumentId.containsKey(documentId);
        }
    }
}
