package co.com.medisalud.jpa.doctor;

import co.com.medisalud.jpa.entity.DoctorData;
import co.com.medisalud.jpa.helper.AdapterOperations;
import co.com.medisalud.model.doctor.Doctor;
import co.com.medisalud.model.doctor.gateways.DoctorRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA adapter that maps doctor domain models to persistence records.
 */
@Repository
public class DoctorRepositoryAdapter extends AdapterOperations<Doctor, DoctorData, UUID, DoctorJpaRepository>
        implements DoctorRepository {

    public DoctorRepositoryAdapter(DoctorJpaRepository repository, ObjectMapper mapper) {
        super(repository, mapper, doctorData -> mapper.mapBuilder(doctorData, Doctor.DoctorBuilder.class).build());
    }

    /**
     * Finds a doctor by identifier.
     *
     * @param id doctor identifier
     * @return doctor when it exists, otherwise null
     */
    @Override
    public Doctor findById(UUID id) {
        return super.findById(id);
    }
}
