package condition.service.tech.condition;

import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.registry.dto.tc.v1.TechConditionStatementDto;
import kz.kus.sa.tech.condition.dto.TechConditionDto;

import java.util.UUID;

public interface TechConditionService {

    TechConditionStatementDto getByStatementId(UUID statementId);

    void consume(TechConditionStatementDto dto);

    TechConditionDto getTechConditionByStatementId(UUID statementId);

    void assign(UUID statementId, AssignDto dto);

    void reAssign(UUID statementId, AssignDto dto);

    void returnToConsumer(UUID statementId, String comment);

    void refuseByConsumer(UUID statementId, FileCreateDto dto);

    TechConditionStatementDto exampleConsume(TechConditionStatementDto dto);
}
