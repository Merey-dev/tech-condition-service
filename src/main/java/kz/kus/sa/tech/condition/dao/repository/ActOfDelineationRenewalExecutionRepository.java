package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActOfDelineationRenewalExecutionRepository extends JpaRepository<ActOfDelineationRenewalExecutionEntity, UUID>, JpaSpecificationExecutor<ActOfDelineationRenewalExecutionEntity> {

    Optional<ActOfDelineationRenewalExecutionEntity> findByIdAndDeletedDatetimeIsNull(UUID id);
}
