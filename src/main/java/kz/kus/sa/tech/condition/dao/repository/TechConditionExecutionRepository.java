package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechConditionExecutionRepository extends JpaRepository<TechConditionExecutionEntity, UUID>, JpaSpecificationExecutor<TechConditionExecutionEntity> {

    Page<TechConditionExecutionEntity> findAllByTechConditionIdAndDeletedDatetimeIsNullOrderByCreatedDatetime(UUID techConditionId, Pageable pageable);

    List<TechConditionExecutionEntity> findAllByTechConditionIdAndDeletedDatetimeIsNullOrderByCreatedDatetime(UUID techConditionId);

    Optional<TechConditionExecutionEntity> findByIdAndDeletedDatetimeIsNull(UUID id);

    Boolean existsByTechConditionIdAndDeletedDatetimeIsNullAndStatusCodeIsNot(UUID id, String statusCode);

    @Query(value = "SELECT coalesce(max(e.reasonForRefusalInternalRegistrationNumber), 0) FROM TechConditionExecutionEntity e " +
            "WHERE e.techCondition.providerId = ?1 AND date_part('year', e.techCondition.applicationDatetime) = ?2")
    Long getMaxReasonForRefusalInternalRegistrationNumberByOrganizationIdAndYear(UUID organizationId, Integer currentYear);
}
