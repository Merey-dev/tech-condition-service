package kz.kus.sa.tech.condition.service.history.impl;

import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.RegistryType;
import kz.kus.sa.tech.condition.dao.entity.*;
import kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper;
import kz.kus.sa.tech.condition.dao.mapper.HistoryMapper;
import kz.kus.sa.tech.condition.dao.repository.HistoryRepository;
import kz.kus.sa.tech.condition.dto.history.HistoryDto;
import kz.kus.sa.tech.condition.service.history.HistoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Service
@AllArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryMapper historyMapper;
    private final HistoryRepository repository;
    private final UserApiService userApiService;
    private final ExternalUserMapper externalUserMapper;

    @Override
    public void save(TechConditionEntity techCondition, TechConditionExecutionEntity execution, Event event) {
        HistoryEntity history = new HistoryEntity();
        OffsetDateTime now = OffsetDateTime.now();

        history.setRegistryType(RegistryType.STATEMENT);
        history.setEvent(event);
        history.setEventDatetime(now);
        history.setCreatedDatetime(now);
        history.setLastModifiedDatetime(now);

        history.setTechCondition(techCondition);
        history.setStatus(techCondition.getStatusCode());
        history.setAssignees(techCondition.getAssignees());
        if (List.of(Event.TCE_SEND_FOR_REVISION, Event.ASSIGN_TO_DIVISION, Event.ASSIGN_TO_EXECUTOR)
                .contains(event)) {
//            history.setRevisionReason(techCondition.getRevisionReason());//todo statement
            if (Objects.nonNull(execution)) {
                history.setRevisionReason(execution.getRevisionReason());
            }
        }

        setCurrentUser(history, techCondition.getCurrentUserId());
        setExecution(history, execution, event);

        repository.save(history);
    }

    @Override
    public void save(ActOfDelineationRenewalEntity actOfDelineationRenewal, ActOfDelineationRenewalExecutionEntity execution, Event event) {
        HistoryEntity history = new HistoryEntity();
        OffsetDateTime now = OffsetDateTime.now();

        history.setRegistryType(RegistryType.STATEMENT);
        history.setEvent(event);
        history.setEventDatetime(now);
        history.setCreatedDatetime(now);
        history.setLastModifiedDatetime(now);

        history.setActOfDelineationRenewal(actOfDelineationRenewal);
        history.setStatus(actOfDelineationRenewal.getStatusCode());
        history.setAssignees(actOfDelineationRenewal.getAssignees());

        setCurrentUser(history, actOfDelineationRenewal.getCurrentUserId());
        setExecution(history, execution, event);

        repository.save(history);
    }

    private void setCurrentUser(HistoryEntity history, UUID currentUserId) {
        if (nonNull(currentUserId)) {
            UserDto userDto = userApiService.getUserById(currentUserId);
            history.setCurrentUser(externalUserMapper.toEntity(userDto));
        }
    }

    private void setExecution(HistoryEntity history, TechConditionExecutionEntity execution, Event event) {
        if (nonNull(execution)) {
            history.setTechConditionExecution(execution);
            history.setExecutionStatus(execution.getStatusCode());
            history.setAssignees(execution.getAssignees());

            setAssignedByUser(history, execution.getAssignedBy());
            setAssigned(history, execution.getAssignees(), event);

            if (Event.CHANGE_ASSIGNEE == event) {
                UserDto userDto = userApiService.getUserById(execution.getAssignees().get(0));
                history.setAssignedExecutor(externalUserMapper.toEntity(userDto));
            }
        }
    }

    private void setExecution(HistoryEntity history, ActOfDelineationRenewalExecutionEntity execution, Event event) {
        if (nonNull(execution)) {
            history.setActOfDelineationRenewalExecution(execution);
            history.setExecutionStatus(execution.getStatusCode());
            history.setAssignees(execution.getAssignees());

            setAssignedByUser(history, execution.getAssignedBy());
            setAssigned(history, execution.getAssignees(), event);

            if (Event.CHANGE_ASSIGNEE == event) {
                UserDto userDto = userApiService.getUserById(execution.getAssignees().get(0));
                history.setAssignedExecutor(externalUserMapper.toEntity(userDto));
            }
        }
    }

    private void setAssignedByUser(HistoryEntity history, UUID assignedBy) {
        if (nonNull(assignedBy)) {
            UserDto userDto = userApiService.getUserById(assignedBy);
            history.setAssignedByUser(externalUserMapper.toEntity(userDto));
        }
    }

    private void setAssigned(HistoryEntity history, List<UUID> assignees, Event event) {
        if (isNotEmpty(assignees)) {
            UserDto userDto = userApiService.getUserById(assignees.get(0));

            history.setAssignedProviderId(userDto.getOrganizationId());
            history.setAssignedProviderFullName(userDto.getOrganizationName());

            if (Objects.equals(event, Event.ASSIGN_TO_DIVISION)
                    || Objects.equals(event, Event.ASSIGN_TO_EXECUTOR)) {
                if (nonNull(userDto.getSubdivisionId())) {
                    history.setAssignedDivisionId(userDto.getSubdivisionId());
                    history.setAssignedDivisionFullName(userDto.getSubDivisionName());
                } else {
                    history.setAssignedDivisionId(userDto.getDivisionId());
                    history.setAssignedDivisionFullName(userDto.getDivisionName());
                }
            }

            if (Objects.equals(event, Event.ASSIGN_TO_EXECUTOR)) {
                history.setAssignedExecutor(externalUserMapper.toEntity(userDto));
            }
        }
    }

    @Override
    public Page<HistoryDto> getByStatementId(UUID statementId, Pageable pageable) {
        List<HistoryEntity> result = repository.findAllByStatementIdOrderByEventDatetime(statementId);
        return new PageImpl<>(historyMapper.toDtoList(result), pageable, result.size());
    }

    @Override
    public List<HistoryEntity> getLast2ByTechConditionExecutionId(UUID techConditionExecutionId) {
        return repository.findLast2ByTechConditionExecutionId(techConditionExecutionId);
    }
}
