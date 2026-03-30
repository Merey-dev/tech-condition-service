package kz.kus.sa.tech.condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionContractualCapacityOfTransformerEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;

import java.util.List;
import java.util.UUID;

public interface TechConditionContractualCapacityOfTransformerService {

    void saveList(List<TechConditionContractualCapacityOfTransformerEntity> oldList,
                  List<TechConditionContractualCapacityOfTransformerEntity> newList,
                  TechConditionEntity techCondition);

    List<TechConditionContractualCapacityOfTransformerEntity> getByTechConditionId(UUID techConditionId);
}
