package kz.kus.sa.tech.condition.service.act;

import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;

import java.util.UUID;

public interface ActOfDelineationRenewalService {

    ActOfDelineationRenewalDto getByStatementId(UUID statementId);

    void consume(ActOfDelineationRenewalDto dto);

    void takeToExecution(UUID statementId);

    void assign(UUID statementId, AssignDto dto);

    void sendForApproval(UUID statementId, AssignDto dto);

    void sendForConfirmation(UUID statementId, AssignDto dto);

    void sendForRevision(UUID statementId, AssignDto dto, String reason);

    ActOfDelineationRenewalEntity findById(UUID id);
}
