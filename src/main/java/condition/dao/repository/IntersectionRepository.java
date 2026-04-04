package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.IntersectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntersectionRepository extends JpaRepository<IntersectionEntity, UUID> {

    List<IntersectionEntity> findAllByTechConditionId(UUID techConditionId);

    List<IntersectionEntity> findAllByTechConditionExecutionId(UUID techConditionExecutionId);

    List<IntersectionEntity> findAllByTechConditionProjectId(UUID techConditionProjectId);
}
