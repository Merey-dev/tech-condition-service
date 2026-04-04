package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionReliabilityCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechConditionReliabilityCategoryRepository extends JpaRepository<TechConditionReliabilityCategoryEntity, UUID> {

    List<TechConditionReliabilityCategoryEntity> findAllByTechConditionId(UUID techConditionId);
}
