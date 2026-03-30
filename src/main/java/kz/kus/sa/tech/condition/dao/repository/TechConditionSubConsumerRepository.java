package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionSubConsumerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechConditionSubConsumerRepository extends JpaRepository<TechConditionSubConsumerEntity, UUID> {

    List<TechConditionSubConsumerEntity> findAllByTechConditionId(UUID techConditionId);
}
