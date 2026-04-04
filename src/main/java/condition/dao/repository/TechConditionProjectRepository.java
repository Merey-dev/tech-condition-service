package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import kz.kus.sa.tech.condition.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechConditionProjectRepository extends JpaRepository<TechConditionProjectEntity, UUID>, JpaSpecificationExecutor<TechConditionProjectEntity> {

    Optional<TechConditionProjectEntity> findByIdAndDeletedDatetimeIsNull(UUID id);

    @Query(nativeQuery = true, value = "SELECT e.* FROM " + Constants.SCHEMA_NAME + "." + "tech_condition_projects e " +
            "WHERE e.status_code IN ('statement_statuses__SIGNED', 'statement_statuses__UPLOADED') " +
            "AND (e.id IN (SELECT a.tech_condition_projects_id FROM " + Constants.SCHEMA_NAME + "." + "abd_address a " +
            "               WHERE a.address_cadastral_number = ?1 OR a.address_ar_rca_code = ?1)) " +
            "ORDER BY e.created_datetime DESC LIMIT 1")
    Optional<TechConditionProjectEntity> findByCadastralNumberOrArCode(String cadastralNumberOrArCode);

    @Query(value = "SELECT coalesce(max(e.internalRegistrationNumber), 0) FROM TechConditionProjectEntity e " +
            "WHERE e.providerId = ?1 AND date_part('year', e.createdDatetime) = ?2")
    Long getMaxInternalRegistrationNumberByProviderIdAndYear(UUID providerId, Integer currentYear);
}
