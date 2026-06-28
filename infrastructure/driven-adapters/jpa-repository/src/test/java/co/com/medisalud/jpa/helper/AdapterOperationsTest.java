package co.com.medisalud.jpa.helper;

import co.com.medisalud.jpa.doctor.DoctorJpaRepository;
import co.com.medisalud.jpa.doctor.DoctorRepositoryAdapter;
import co.com.medisalud.jpa.entity.DoctorData;
import co.com.medisalud.model.doctor.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdapterOperationsTest {

    @Mock
    private DoctorJpaRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    private DoctorRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adapter = new DoctorRepositoryAdapter(repository, objectMapper);
    }

    @Test
    void testSave() {
        UUID id = UUID.randomUUID();
        Doctor doctor = doctor(id);
        DoctorData doctorData = doctorData(id);

        when(objectMapper.map(doctor, DoctorData.class)).thenReturn(doctorData);
        when(objectMapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class)).thenReturn(doctor.toBuilder());
        when(repository.save(doctorData)).thenReturn(doctorData);

        Doctor result = adapter.save(doctor);

        assertEquals(id, result.getId());
        assertEquals("Laura Gomez", result.getFullName());
    }

    @Test
    void testSaveAllEntities() {
        UUID id = UUID.randomUUID();
        Doctor doctor = doctor(id);
        DoctorData doctorData = doctorData(id);
        List<Doctor> doctors = List.of(doctor);
        List<DoctorData> doctorDataList = List.of(doctorData);

        when(objectMapper.map(doctor, DoctorData.class)).thenReturn(doctorData);
        when(objectMapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class)).thenReturn(doctor.toBuilder());
        when(repository.saveAll(doctorDataList)).thenReturn(doctorDataList);

        List<Doctor> result = adapter.saveAllEntities(doctors);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getId());
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        Doctor doctor = doctor(id);
        DoctorData doctorData = doctorData(id);

        when(objectMapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class)).thenReturn(doctor.toBuilder());
        when(repository.findById(id)).thenReturn(Optional.of(doctorData));

        Doctor result = adapter.findById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void testFindAll() {
        UUID id = UUID.randomUUID();
        Doctor doctor = doctor(id);
        DoctorData doctorData = doctorData(id);

        when(objectMapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class)).thenReturn(doctor.toBuilder());
        when(repository.findAll()).thenReturn(List.of(doctorData));

        List<Doctor> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getId());
    }

    @Test
    void testFindByExample() {
        UUID id = UUID.randomUUID();
        Doctor doctor = doctor(id);
        DoctorData doctorData = doctorData(id);

        when(objectMapper.map(doctor, DoctorData.class)).thenReturn(doctorData);
        when(objectMapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class)).thenReturn(doctor.toBuilder());
        when(repository.findAll(any(Example.class))).thenReturn(List.of(doctorData));

        List<Doctor> result = adapter.findByExample(doctor);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getId());
    }

    @Test
    void testFindByIdWhenMissing() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        Doctor result = adapter.findById(id);

        assertNull(result);
    }

    private static Doctor doctor(UUID id) {
        return Doctor.builder()
                .id(id)
                .fullName("Laura Gomez")
                .specialty("Cardiology")
                .phone("3001234567")
                .email("laura.gomez@medisalud.com")
                .build();
    }

    private static DoctorData doctorData(UUID id) {
        return DoctorData.builder()
                .id(id)
                .fullName("Laura Gomez")
                .specialty("Cardiology")
                .phone("3001234567")
                .email("laura.gomez@medisalud.com")
                .build();
    }
}
