package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalAbdAddressDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActOfDelineationRenewalAbdAddressDecisionRepository extends JpaRepository<ActOfDelineationRenewalAbdAddressDecisionEntity, UUID> {

    List<ActOfDelineationRenewalAbdAddressDecisionEntity> findAllByActOfDelineationRenewalId(UUID renewalId);

    Optional<ActOfDelineationRenewalAbdAddressDecisionEntity> findByActOfDelineationRenewalIdAndObjectAbdAddressId(UUID renewalId, UUID abdAddressId);

    boolean existsByActOfDelineationRenewalIdAndStatusCodeNot(UUID renewalId, String statusCode);
}
