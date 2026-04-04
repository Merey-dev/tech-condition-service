package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionMaximumLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechConditionMaximumLoadRepository extends JpaRepository<TechConditionMaximumLoadEntity, UUID> {

    List<TechConditionMaximumLoadEntity> findAllByTechConditionId(UUID techConditionId);
}
