package co.com.medisalud.api;

import co.com.medisalud.api.config.CorsConfig;
import co.com.medisalud.model.appointment.Appointment;
import co.com.medisalud.model.appointment.AppointmentStatus;
import co.com.medisalud.model.appointment.gateways.AppointmentRepository;
import co.com.medisalud.model.appointmentsearchcriteria.AppointmentSearchCriteria;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import co.com.medisalud.model.patient.Patient;
import co.com.medisalud.model.patient.gateways.PatientRepository;
import co.com.medisalud.model.penalty.Penalty;
import co.com.medisalud.model.penalty.gateways.PenaltyRepository;
import co.com.medisalud.usecase.cancelappointment.CancelAppointmentUseCase;
import co.com.medisalud.usecase.createappointment.CreateAppointmentUseCase;
import co.com.medisalud.usecase.createdoctor.CreateDoctorUseCase;
import co.com.medisalud.usecase.createpatient.CreatePatientUseCase;
import co.com.medisalud.usecase.getdoctoravailability.GetDoctorAvailabilityUseCase;
import co.com.medisalud.usecase.listappointments.ListAppointmentsUseCase;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "co.com.medisalud.api",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = CorsConfig.class
        ))
@Import({
        ApiRest.class,
        CreateDoctorUseCase.class,
        CreatePatientUseCase.class,
        CreateAppointmentUseCase.class,
        CancelAppointmentUseCase.class,
        GetDoctorAvailabilityUseCase.class,
        ListAppointmentsUseCase.class
})
public class TestConfig {

    static final String UNEXPECTED_ERROR_DOCTOR_NAME = "Trigger Failure";
    static final UUID EXISTING_DOCTOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID OTHER_DOCTOR_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    static final UUID EXISTING_PATIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID BLOCKED_PATIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    static final UUID OTHER_PATIENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Bean
    public DoctorRepository doctorRepository() {
        return new InMemoryDoctorRepository();
    }

    @Bean
    public PatientRepository patientRepository() {
        return new InMemoryPatientRepository();
    }

    @Bean
    public AppointmentRepository appointmentRepository() {
        return new InMemoryAppointmentRepository();
    }

    @Bean
    public PenaltyRepository penaltyRepository() {
        return new InMemoryPenaltyRepository();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    private static class InMemoryDoctorRepository implements DoctorRepository {

        private final Map<UUID, Doctor> doctorsById = new ConcurrentHashMap<>();

        private InMemoryDoctorRepository() {
            doctorsById.put(EXISTING_DOCTOR_ID, Doctor.builder()
                    .id(EXISTING_DOCTOR_ID)
                    .fullName("Carlos Mejia")
                    .specialty("Cardiology")
                    .build());
            doctorsById.put(OTHER_DOCTOR_ID, Doctor.builder()
                    .id(OTHER_DOCTOR_ID)
                    .fullName("Andrea Torres")
                    .specialty("Pediatrics")
                    .build());
        }

        @Override
        public Doctor save(Doctor doctor) {
            if (UNEXPECTED_ERROR_DOCTOR_NAME.equals(doctor.getFullName())) {
                throw new IllegalStateException("Forced unexpected test error");
            }
            doctorsById.put(doctor.getId(), doctor);
            return doctor;
        }

        @Override
        public Doctor findById(UUID id) {
            return doctorsById.get(id);
        }
    }

    private static class InMemoryPatientRepository implements PatientRepository {

        private final Map<UUID, Patient> patientsById = new ConcurrentHashMap<>();
        private final Map<String, Patient> patientsByDocumentId = new ConcurrentHashMap<>();

        private InMemoryPatientRepository() {
            save(Patient.builder()
                    .id(EXISTING_PATIENT_ID)
                    .fullName("Mateo Perez")
                    .documentId("EXISTING1001")
                    .phone("3107654321")
                    .email("mateo.perez@medisalud.com")
                    .birthDate(LocalDate.of(1990, 5, 12))
                    .build());
            save(Patient.builder()
                    .id(BLOCKED_PATIENT_ID)
                    .fullName("Ricardo Morales")
                    .documentId("BLOCKED1001")
                    .phone("3107654322")
                    .email("ricardo.morales@medisalud.com")
                    .birthDate(LocalDate.of(1978, 11, 8))
                    .build());
            save(Patient.builder()
                    .id(OTHER_PATIENT_ID)
                    .fullName("Camila Ruiz")
                    .documentId("EXISTING1002")
                    .phone("3107654323")
                    .email("camila.ruiz@medisalud.com")
                    .birthDate(null)
                    .build());
        }

        @Override
        public Patient save(Patient patient) {
            patientsById.put(patient.getId(), patient);
            patientsByDocumentId.put(patient.getDocumentId(), patient);
            return patient;
        }

        @Override
        public Patient findById(UUID id) {
            return patientsById.get(id);
        }

        @Override
        public boolean existsByDocumentId(String documentId) {
            return patientsByDocumentId.containsKey(documentId);
        }
    }

    private static class InMemoryAppointmentRepository implements AppointmentRepository {

        private final Map<UUID, Appointment> appointmentsById = new ConcurrentHashMap<>();

        @Override
        public Appointment save(Appointment appointment) {
            appointmentsById.put(appointment.getId(), appointment);
            return appointment;
        }

        @Override
        public Appointment findById(UUID id) {
            return appointmentsById.get(id);
        }

        @Override
        public boolean existsScheduledByDoctorIdAndDateTime(UUID doctorId, LocalDateTime dateTime) {
            return appointmentsById.values().stream()
                    .anyMatch(appointment -> doctorId.equals(appointment.getDoctorId())
                            && dateTime.equals(appointment.getDateTime())
                            && AppointmentStatus.SCHEDULED == appointment.getStatus());
        }

        @Override
        public boolean existsScheduledByPatientIdAndDoctorIdAndDateTime(
                UUID patientId,
                UUID doctorId,
                LocalDateTime dateTime) {
            return appointmentsById.values().stream()
                    .anyMatch(appointment -> patientId.equals(appointment.getPatientId())
                            && doctorId.equals(appointment.getDoctorId())
                            && dateTime.equals(appointment.getDateTime())
                            && AppointmentStatus.SCHEDULED == appointment.getStatus());
        }

        @Override
        public List<Appointment> findScheduledByDoctorIdAndDateTimeBetween(
                UUID doctorId,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime) {
            return appointmentsById.values().stream()
                    .filter(appointment -> doctorId.equals(appointment.getDoctorId()))
                    .filter(appointment -> AppointmentStatus.SCHEDULED == appointment.getStatus())
                    .filter(appointment -> !appointment.getDateTime().isBefore(startDateTime))
                    .filter(appointment -> appointment.getDateTime().isBefore(endDateTime))
                    .toList();
        }

        @Override
        public List<Appointment> findByCriteria(AppointmentSearchCriteria criteria) {
            return appointmentsById.values().stream()
                    .filter(appointment -> criteria.getDoctorId() == null
                            || criteria.getDoctorId().equals(appointment.getDoctorId()))
                    .filter(appointment -> criteria.getPatientId() == null
                            || criteria.getPatientId().equals(appointment.getPatientId()))
                    .filter(appointment -> criteria.getStatus() == null
                            || criteria.getStatus() == appointment.getStatus())
                    .filter(appointment -> criteria.getStartDate() == null
                            || !appointment.getDateTime().isBefore(criteria.getStartDate()))
                    .filter(appointment -> criteria.getEndDate() == null
                            || !appointment.getDateTime().isAfter(criteria.getEndDate()))
                    .sorted(Comparator.comparing(Appointment::getDateTime))
                    .toList();
        }
    }

    private static class InMemoryPenaltyRepository implements PenaltyRepository {

        private final Map<UUID, Penalty> penaltiesById = new ConcurrentHashMap<>();

        @Override
        public Penalty save(Penalty penalty) {
            penaltiesById.put(penalty.getId(), penalty);
            return penalty;
        }

        @Override
        public long countByPatientIdAndCreatedAtGreaterThanEqual(UUID patientId, LocalDateTime fromDateTime) {
            if (BLOCKED_PATIENT_ID.equals(patientId)) {
                return 3;
            }
            return penaltiesById.values().stream()
                    .filter(penalty -> patientId.equals(penalty.getPatientId()))
                    .filter(penalty -> !penalty.getCreatedAt().isBefore(fromDateTime))
                    .count();
        }
    }
}
