package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechConditionExecutionAbdAddressDecisionRepository extends JpaRepository<TechConditionExecutionAbdAddressDecisionEntity, UUID> {

    List<TechConditionExecutionAbdAddressDecisionEntity> findAllByTechConditionExecutionId(UUID executionId);

    Optional<TechConditionExecutionAbdAddressDecisionEntity> findByTechConditionExecutionIdAndObjectAbdAddressId(UUID executionId, UUID abdAddressId);
}
