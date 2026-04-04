package condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionMaximumLoadEntity;

import java.util.List;
import java.util.UUID;

public interface TechConditionMaximumLoadService {

    void saveList(List<TechConditionMaximumLoadEntity> oldList,
                  List<TechConditionMaximumLoadEntity> newList,
                  TechConditionEntity techCondition);

    List<TechConditionMaximumLoadEntity> getByTechConditionId(UUID techConditionId);
}
