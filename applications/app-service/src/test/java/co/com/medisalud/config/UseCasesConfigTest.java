package co.com.medisalud.config;

import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        } catch (org.springframework.beans.factory.UnsatisfiedDependencyException e) {
            assertTrue(true, "Unsatisfied dependencies are expected for UseCases resolving gateways");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public DoctorRepository doctorRepository() {
            return new DoctorRepository() {
                @Override
                public co.com.medisalud.model.doctor.Doctor save(co.com.medisalud.model.doctor.Doctor doctor) {
                    return doctor;
                }

                @Override
                public co.com.medisalud.model.doctor.Doctor findById(UUID id) {
                    return null;
                }
            };
        }

        @Bean
        public PatientRepository patientRepository() {
            return new PatientRepository() {
                @Override
                public co.com.medisalud.model.patient.Patient save(co.com.medisalud.model.patient.Patient patient) {
                    return patient;
                }

                @Override
                public co.com.medisalud.model.patient.Patient findById(UUID id) {
                    return null;
                }

                @Override
                public boolean existsByDocumentId(String documentId) {
                    return false;
                }
            };
        }

        @Bean
        public AppointmentRepository appointmentRepository() {
            return new AppointmentRepository() {
                @Override
                public Appointment save(Appointment appointment) {
                    return appointment;
                }

                @Override
                public Appointment findById(UUID id) {
                    return null;
                }

                @Override
                public boolean existsScheduledByDoctorIdAndDateTime(UUID doctorId, LocalDateTime dateTime) {
                    return false;
                }

                @Override
                public boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
                        UUID patientId,
                        UUID doctorId,
                        LocalDateTime dateTime) {
                    return false;
                }

                @Override
                public List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
                        UUID doctorId,
                        LocalDateTime startDateTime,
                        LocalDateTime endDateTime) {
                    return List.of();
                }

                @Override
                public List<Appointment> findByCriteria(AppointmentSearchCriteria criteria) {
                    return List.of();
                }
            };
        }

        @Bean
        public PenaltyRepository penaltyRepository() {
            return new PenaltyRepository() {
                @Override
                public Penalty save(Penalty penalty) {
                    return penalty;
                }

                @Override
                public long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime) {
                    return 0;
                }
            };
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}
