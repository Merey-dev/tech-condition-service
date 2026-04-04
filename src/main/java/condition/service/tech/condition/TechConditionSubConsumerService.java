package condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionSubConsumerEntity;

import java.util.List;
import java.util.UUID;

public interface TechConditionSubConsumerService {

    void saveList(List<TechConditionSubConsumerEntity> oldList,
                  List<TechConditionSubConsumerEntity> newList,
                  TechConditionEntity techCondition);

    List<TechConditionSubConsumerEntity> getByTechConditionId(UUID techConditionId);
}
