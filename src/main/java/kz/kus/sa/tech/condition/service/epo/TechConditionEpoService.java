package kz.kus.sa.tech.condition.service.epo;

import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.registry.dto.tc.epo.TechConditionEpoStatementDto;

import java.util.UUID;

public interface TechConditionEpoService {

    void consume(TechConditionEpoStatementDto dto);

    TechConditionEpoStatementDto getByStatementId(UUID statementId);

    void assign(UUID statementId, AssignDto dto);

    void reAssign(UUID statementId, AssignDto dto);

    void returnToConsumer(UUID statementId, String comment);

    void refuseByConsumer(UUID statementId, FileCreateDto dto);
}
