package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AbdAddressRepository extends JpaRepository<AbdAddressEntity, UUID> {

    List<AbdAddressEntity> findAllByTechConditionId(UUID techConditionId);

    List<AbdAddressEntity> findAllByTechConditionExecutionId(UUID techConditionExecutionId);

    List<AbdAddressEntity> findAllByTechConditionProjectId(UUID techConditionProjectId);

    List<AbdAddressEntity> findAllByActOfDelineationRenewalId(UUID actOfDelineationRenewalId);

    List<AbdAddressEntity> findAllByActOfDelineationRenewalExecutionId(UUID actOfDelineationRenewalExecutionId);

    List<AbdAddressEntity> findAllByActOfDelineationId(UUID actOfDelineationId);
}
