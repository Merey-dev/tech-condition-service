package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TechConditionExecutionAbdAddressDecisionRepository extends JpaRepository<TechConditionExecutionAbdAddressDecisionEntity, UUID> {
}
