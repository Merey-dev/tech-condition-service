package kz.kus.sa.tech.condition.statemachine;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.enums.TechConditionExecutionType;
import kz.kus.sa.tech.condition.service.history.HistoryService;
import kz.kus.sa.tech.condition.statemachine.base.StateConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class TechConditionExecutionStatemachine extends StateConfig<String, Event, TechConditionEntity, TechConditionExecutionEntity> {

    private final CurrentUserApiService currentUserApiService;
    private final HistoryService historyService;

    @SuppressWarnings("unchecked")
    protected void configure(StateBuilder<String, Event, TechConditionEntity, TechConditionExecutionEntity> stateBuilder) {

        // Статус "Назначен"
        stateBuilder.state(ExecutionStatus.ASSIGNED.getCode())
                .event(Event.TCE_TAKE_TO_EXECUTION).targetState(ExecutionStatus.ON_EXECUTION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(execution))

                .and()
                .event(Event.ASSIGN_TO_DIVISION)
                .action(this::saveHistory)

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR)
                .action(this::saveHistory)

                .and()
                .event(Event.ASSIGN_TO_DIVISION_WITH_ADDRESS)
                .action(this::saveHistory)

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS)
                .action(this::saveHistory)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "На исполнении"
        stateBuilder.state(ExecutionStatus.ON_EXECUTION.getCode())
                .event(Event.TC_SIGN).targetState(ExecutionStatus.SIGNED.getCode())
                .action(this::setState)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "Подписан"
        stateBuilder.state(ExecutionStatus.SIGNED.getCode());

//        // Статус "Назначен"
//        stateBuilder.state(ExecutionStatus.ASSIGNED.getCode())
//                .event(Event.ASSIGN_TO_DIVISION)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isAssignor(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_EXECUTOR)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isAssignor(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_DIVISION_WITH_ADDRESS)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isAssignor(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isAssignor(execution))
//
//                .and()
//                .event(Event.TCE_TAKE_TO_EXECUTION).targetState(ExecutionStatus.ON_EXECUTION.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && isApplication(execution));
//
//        // Статус "На исполнении"
//        stateBuilder.state(ExecutionStatus.ON_EXECUTION.getCode())
//                .event(Event.ASSIGN_TO_DIVISION).targetState(ExecutionStatus.ASSIGNED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && !isExecuted(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_EXECUTOR).targetState(ExecutionStatus.ASSIGNED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && !isExecuted(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_DIVISION_WITH_ADDRESS).targetState(ExecutionStatus.ASSIGNED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && !isExecuted(execution))
//
//                .and()
//                .event(Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS).targetState(ExecutionStatus.ASSIGNED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && !isExecuted(execution))
//
//                .and()
//                .event(Event.EXECUTE)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isExecutor(execution))
//
//                .and()
//                .event(Event.TCE_SEND_FOR_APPROVAL).targetState(ExecutionStatus.UNDER_APPROVAL.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecuted(execution))
//
//                // когда исполняет начальник, сразу согласовывает сам
//                .and()
//                .event(Event.TCE_APPROVE).targetState(ExecutionStatus.APPROVED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution) && isExecuted(execution));
//
//        // Статус "Возвращено на доработку"
//        stateBuilder.state(ExecutionStatus.RETURNED_FOR_REVISION.getCode())
//                .event(Event.TCE_APPROVE).targetState(ExecutionStatus.APPROVED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution))
//
//                .and()
//                .event(Event.EXECUTE)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isExecutor(execution))
//
//                .and()
//                .event(Event.TCE_SEND_FOR_APPROVAL).targetState(ExecutionStatus.UNDER_APPROVAL.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution));
//
//        // Статус "На согласовании"
//        stateBuilder.state(ExecutionStatus.UNDER_APPROVAL.getCode())
//                .event(Event.TCE_SEND_FOR_REVISION).targetState(ExecutionStatus.RETURNED_FOR_REVISION.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution))
//
//                .and()
//                .event(Event.TCE_APPROVE).targetState(ExecutionStatus.APPROVED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution))
//
//                .and()
//                .event(Event.TCE_WITHDRAW)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> !isApproved(execution));
//
//        // Статус "Утвержден"
//        stateBuilder.state(ExecutionStatus.APPROVED.getCode())
//                .event(Event.TCE_SEND_FOR_REVISION).targetState(ExecutionStatus.RETURNED_FOR_REVISION.getCode())
//                .action(this::setState)
//
//                .and()
//                .event(Event.TCE_TAKE_TO_EXECUTION)
//                .action(this::saveHistory)
//                .guard((entity, execution) -> isExecutor(execution) && isMainExecutorNull(entity))
//
//                .and()
//                .event(Event.TC_SIGN).targetState(ExecutionStatus.SIGNED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(execution));
    }

    private void setState(TechConditionEntity entity, TechConditionExecutionEntity execution, String state, Event event) {
        execution.setStatusCode(state);
        saveHistory(entity, execution, state, event);
    }

    private void saveHistory(TechConditionEntity entity, TechConditionExecutionEntity execution, String state, Event event) {
        OffsetDateTime now = OffsetDateTime.now();
        execution.setLastModifiedDatetime(now);
        if (Event.DELETE.equals(event)) {
            entity.setDeletedDatetime(now);
            execution.setDeletedDatetime(now);
        }
        historyService.save(entity, execution, event);
    }

    private boolean isApplication(TechConditionExecutionEntity execution) {
        return execution.getExecutionType() == TechConditionExecutionType.APPLICATION;
    }

    private boolean isApproved(TechConditionExecutionEntity execution) {
        return execution.getManagerApprovedDatetime() != null;
    }

    private boolean isExecuted(TechConditionExecutionEntity execution) {
        return execution.getExecutor() != null;
    }

    private boolean isExecutor(TechConditionExecutionEntity execution) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        return execution.getAssignees().contains(currentUser.getId());
    }

    private boolean isMainExecutorNull(TechConditionEntity entity) {
        return entity.getExecutor() == null;
    }

    private boolean isAssignor(TechConditionExecutionEntity execution) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        return execution.getAssignedBy().equals(currentUser.getId());
    }
}
