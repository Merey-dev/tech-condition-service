package condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionPlannedEquipmentEntity;

import java.util.List;
import java.util.UUID;

public interface TechConditionPlannedEquipmentService {

    void saveList(List<TechConditionPlannedEquipmentEntity> oldList,
                                  List<TechConditionPlannedEquipmentEntity> newList,
                                  TechConditionEntity techCondition);

    List<TechConditionPlannedEquipmentEntity> getByTechConditionId(UUID techConditionId);
}
