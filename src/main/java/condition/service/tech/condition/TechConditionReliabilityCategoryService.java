package condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionReliabilityCategoryEntity;

import java.util.List;
import java.util.UUID;

public interface TechConditionReliabilityCategoryService {

    void saveList(List<TechConditionReliabilityCategoryEntity> oldList,
                  List<TechConditionReliabilityCategoryEntity> newList,
                  TechConditionEntity techCondition);

    List<TechConditionReliabilityCategoryEntity> getByTechConditionId(UUID techConditionId);
}
