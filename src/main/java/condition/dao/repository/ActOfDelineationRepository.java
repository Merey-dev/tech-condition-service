package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActOfDelineationRepository extends JpaRepository<ActOfDelineationEntity, UUID>, JpaSpecificationExecutor<ActOfDelineationEntity> {

    Optional<ActOfDelineationEntity> findByActOfDelineationRenewalExecutionId(UUID actOfDelineationRenewalExecutionId);

    @Query(value = "SELECT coalesce(max(e.internalRegistrationNumber), 0) FROM ActOfDelineationEntity e " +
            "WHERE e.providerId = ?1 AND date_part('year', e.preparationDatetime) = ?2")
    Long getMaxInternalRegistrationNumberByProviderIdAndYear(UUID providerId, Integer currentYear);
}
