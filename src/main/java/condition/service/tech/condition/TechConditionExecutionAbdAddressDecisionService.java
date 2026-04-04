package condition.service.tech.condition;

import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dto.TechConditionExecuteDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionAbdAddressDecisionDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;

import java.util.List;
import java.util.UUID;

public interface TechConditionExecutionAbdAddressDecisionService {

    void saveAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtos);

    void replaceAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtos);

    List<TechConditionExecutionAbdAddressDecisionEntity> findAllByExecutionId(UUID executionId);

    TechConditionExecutionAbdAddressDecisionEntity findByExecutionIdAndAbdAddressId(UUID executionId, UUID abdAddressId);

    void deleteAllByExecutionId(UUID executionId);

    void executeApplication(UUID id, TechConditionExecuteDto dto);

    void signDecision(UUID id, SignCreateDto sign);

    TechConditionProjectDto createProject(UUID id, TechConditionProjectCreateDto dto);

    void formationReasonedRefusal(UUID id, TechConditionExecuteDto dto);
}
