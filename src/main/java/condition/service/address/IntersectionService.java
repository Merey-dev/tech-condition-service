package condition.service.address;

import kz.kus.sa.tech.condition.dao.entity.IntersectionEntity;

import java.util.List;
import java.util.UUID;

public interface IntersectionService {

    void saveList(List<IntersectionEntity> oldList, List<IntersectionEntity> newList, Object object);

    List<IntersectionEntity> getByTechConditionId(UUID techConditionId);

    List<IntersectionEntity> getByTechConditionExecutionId(UUID techConditionExecutionId);

    List<IntersectionEntity> getByTechConditionProjectId(UUID techConditionProjectId);
}
