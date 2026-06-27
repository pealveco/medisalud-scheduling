package co.com.medisalud.jpa;

import co.com.medisalud.jpa.entity.DoctorData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.UUID;

/**
 * Spring Data repository for doctor persistence records.
 */
public interface DoctorJpaRepository extends CrudRepository<DoctorData, UUID>, QueryByExampleExecutor<DoctorData> {
}
