package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActOfDelineationRenewalRepository extends JpaRepository<ActOfDelineationRenewalEntity, UUID>, JpaSpecificationExecutor<ActOfDelineationRenewalEntity> {

    Optional<ActOfDelineationRenewalEntity> findByStatementIdAndDeletedDatetimeIsNull(UUID statementId);

    Optional<ActOfDelineationRenewalEntity> findByIdAndDeletedDatetimeIsNull(UUID id);
}
