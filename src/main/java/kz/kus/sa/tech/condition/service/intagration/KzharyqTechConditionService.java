package kz.kus.sa.tech.condition.service.intagration;

import kz.kus.sa.integ.kzharyq.dto.internal.tc.completed.TechConditionCompletedRequestDto;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.registered.TechConditionRegisteredRequestDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;

public interface KzharyqTechConditionService {

    TechConditionRegisteredRequestDto getRegisteredRequest(TechConditionEntity tc);

    void sendRegisteredRequest(TechConditionEntity tc);

    TechConditionCompletedRequestDto getCompletedRequest(TechConditionEntity tc, TechConditionExecutionEntity execution);

    void sendCompletedRequest(TechConditionEntity tc);
}
