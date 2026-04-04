package condition.service.tech.condition;

import kz.kus.sa.registry.dto.common.AbdAddressDto;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dto.ChangeAssigneeDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionDto;
import kz.kus.sa.tech.condition.enums.TechConditionExecutionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TechConditionExecutionService {

    Page<TechConditionExecutionDto> getAllForAdmin(String searchText,
                                                   List<String> statuses,
                                                   List<String> statementStatuses,
                                                   LocalDate dateFrom,
                                                   LocalDate dateTo,
                                                   Source source,
                                                   UUID userId,
                                                   UUID providerId,
                                                   Pageable pageable);

    void changeAssignee(ChangeAssigneeDto dto);

    Page<TechConditionExecutionDto> getAll(String searchText,
                                           List<String> statuses,
                                           List<String> statementStatuses,
                                           LocalDate dateFrom,
                                           LocalDate dateTo,
                                           Source source,
                                           UUID userId,
                                           Pageable pageable);

    TechConditionExecutionDto getById(UUID id);

    Page<TechConditionExecutionDto> findAllByTechConditionId(UUID techConditionId, Pageable pageable);

    List<TechConditionExecutionDto> findAllByTechConditionIdList(UUID techConditionId);

    TechConditionExecutionEntity create(UUID techConditionId, AssignDto assignDto,
                                        TechConditionExecutionType type, Boolean isParallel,
                                        List<AbdAddressDto> objectAbdAddresses);

    TechConditionExecutionEntity baseSave(TechConditionExecutionEntity entity);

    List<TechConditionExecutionEntity> findAllByTechConditionId(UUID techConditionId);

    void takeToExecution(UUID id, AssignDto dto);

    void assign(UUID id, AssignDto dto);

    @Deprecated
    void reAssign(UUID id, AssignDto dto);

    void assignForApproval(UUID id, AssignDto dto);

    void sendForRevision(UUID id, String reason);

    void approve(UUID id);

    //todo refactor execution history
    void withdraw(UUID id);

    void assignForSign(UUID id, AssignDto dto);

    void assignParallel(UUID id, AssignDto dto);

    void sendDecisionForApproval(UUID id, UUID userId);

    void reSendDecisionForApproval(UUID id, UUID userId);

    void sendDecisionForRevision(UUID id, String reason);

    void assignDecisionForSign(UUID id, AssignDto dto);

    TechConditionEntity approveDecision(UUID id);

    void approveDecisionAndSendForSign(UUID id, AssignDto dto);
}
