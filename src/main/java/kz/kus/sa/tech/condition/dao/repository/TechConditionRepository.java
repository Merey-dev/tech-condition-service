package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechConditionRepository extends JpaRepository<TechConditionEntity, UUID>, JpaSpecificationExecutor<TechConditionEntity> {

    Optional<TechConditionEntity> findByStatementIdAndDeletedDatetimeIsNull(UUID statementId);

    Optional<TechConditionEntity> findByIdAndDeletedDatetimeIsNull(UUID id);

//    @Query(value = "SELECT coalesce(max(e.reasonForRefusalInternalRegistrationNumber), 0) FROM TechConditionEntity e " +
//            "WHERE e.providerId = ?1 AND date_part('year', e.applicationDatetime) = ?2")
//    Long getMaxReasonForRefusalInternalRegistrationNumberByOrganizationIdAndYear(UUID organizationId, Integer currentYear);
}
