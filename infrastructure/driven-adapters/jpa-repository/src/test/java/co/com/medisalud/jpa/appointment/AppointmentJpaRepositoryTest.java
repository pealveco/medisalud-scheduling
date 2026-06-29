package co.com.medisalud.jpa.appointment;

import co.com.medisalud.jpa.entity.AppointmentData;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ContextConfiguration(classes = AppointmentJpaRepositoryTest.TestJpaConfiguration.class)
class AppointmentJpaRepositoryTest {

    @Autowired
    private AppointmentJpaRepository repository;

    @Test
    void shouldFindAppointmentsWithoutFilters() {
        AppointmentData firstAppointment = appointmentData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 7, 10, 0),
                AppointmentStatus.SCHEDULED
        );
        AppointmentData secondAppointment = appointmentData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 9, 7, 11, 0),
                AppointmentStatus.CANCELLED
        );
        repository.save(firstAppointment);
        repository.save(secondAppointment);
        AppointmentRepositoryAdapter adapter = new AppointmentRepositoryAdapter(repository, new ObjectMapperImp());

        List<Appointment> result = adapter.findByCriteria(AppointmentSearchCriteria.builder().build());

        assertEquals(2, result.size());
        assertEquals(firstAppointment.getId(), result.getFirst().getId());
        assertEquals(secondAppointment.getId(), result.get(1).getId());
    }

    @Test
    void shouldFindAppointmentsByOptionalFilters() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        AppointmentData matchingAppointment = appointmentData(
                UUID.randomUUID(),
                doctorId,
                patientId,
                LocalDateTime.of(2026, 9, 7, 10, 0),
                AppointmentStatus.SCHEDULED
        );
        repository.save(matchingAppointment);
        repository.save(appointmentData(
                UUID.randomUUID(),
                doctorId,
                patientId,
                LocalDateTime.of(2026, 9, 7, 11, 0),
                AppointmentStatus.CANCELLED
        ));
        repository.save(appointmentData(
                UUID.randomUUID(),
                doctorId,
                patientId,
                LocalDateTime.of(2026, 9, 8, 10, 0),
                AppointmentStatus.SCHEDULED
        ));
        AppointmentRepositoryAdapter adapter = new AppointmentRepositoryAdapter(repository, new ObjectMapperImp());

        List<Appointment> result = adapter.findByCriteria(AppointmentSearchCriteria.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .status(AppointmentStatus.SCHEDULED)
                .startDate(LocalDateTime.of(2026, 9, 7, 0, 0))
                .endDate(LocalDateTime.of(2026, 9, 7, 23, 59))
                .build());

        assertEquals(1, result.size());
        assertEquals(matchingAppointment.getId(), result.getFirst().getId());
    }

    private static AppointmentData appointmentData(
            UUID id,
            UUID doctorId,
            UUID patientId,
            LocalDateTime dateTime,
            AppointmentStatus status) {
        return AppointmentData.builder()
                .id(id)
                .doctorId(doctorId)
                .patientId(patientId)
                .dateTime(dateTime)
                .status(status)
                .build();
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = AppointmentData.class)
    @EnableJpaRepositories(basePackageClasses = AppointmentJpaRepository.class)
    static class TestJpaConfiguration {
    }
}
