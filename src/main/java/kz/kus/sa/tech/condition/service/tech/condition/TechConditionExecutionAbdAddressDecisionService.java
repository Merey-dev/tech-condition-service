package kz.kus.sa.tech.condition.service.tech.condition;

import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dto.TechConditionExecuteDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;

import java.util.List;
import java.util.UUID;

public interface TechConditionExecutionAbdAddressDecisionService {

    void assign(UUID executionId, AssignDto dto);

    List<TechConditionExecutionAbdAddressDecisionEntity> findAllByExecutionId(UUID executionId);

    void executeApplication(UUID executionId, TechConditionExecuteDto dto);

    TechConditionProjectDto createProject(UUID decisionId, TechConditionProjectCreateDto dto);

//    void formationReasonedRefusal(UUID executionId, TechConditionExecuteDto dto);

    // методы переехавшие из TechConditionExecutionService
    void takeToExecution(UUID decisionId);

    void sendForRevision(UUID decisionId, String reason);

    void sendForApproval(UUID decisionId, AssignDto dto);

    void approve(UUID decisionId);

    void withdraw(UUID decisionId);

    void sendForSign(UUID decisionId, AssignDto dto);

    void approveAndSendForSign(UUID decisionId, AssignDto dto);

    void sign(UUID decisionId, SignCreateDto sign);
}
