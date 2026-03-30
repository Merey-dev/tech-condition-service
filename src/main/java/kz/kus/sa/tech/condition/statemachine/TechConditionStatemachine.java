package kz.kus.sa.tech.condition.statemachine;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.service.history.HistoryService;
import kz.kus.sa.tech.condition.statemachine.base.StateConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class TechConditionStatemachine extends StateConfig<String, Event, TechConditionEntity, TechConditionExecutionEntity> {

    private final CurrentUserApiService currentUserApiService;
    private final HistoryService historyService;

    @SuppressWarnings("unchecked")
    protected void configure(StateBuilder<String, Event, TechConditionEntity, TechConditionExecutionEntity> stateBuilder) {

        // Статус "Черновик"
        stateBuilder.state(Status.DRAFT.getCode())
                .event(Event.CREATE)
                .action(this::saveHistory)

                .and()
                .event(Event.UPDATE)
                .action(this::saveHistory)
                .guard((entity, execution) -> isNotDeleted(entity))

                .and()
                .event(Event.DELETE)
                .action(this::saveHistory)
                .guard((entity, execution) -> isNotDeleted(entity))

                .and()
                .event(Event.ADD_CONSUMER_SIGN)
                .action(this::saveHistory)
                .guard((entity, execution) -> isNotDeleted(entity))

                .and()
                .event(Event.DELETE_CONSUMER_SIGN)
                .action(this::saveHistory)
                .guard((entity, execution) -> isNotDeleted(entity))

                .and()
                .event(Event.REGISTER).targetState(Status.REGISTERED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isNotDeleted(entity));

        // Статус "Зарегистрирован"
        stateBuilder.state(Status.REGISTERED.getCode())
                .event(Event.ASSIGN_TO_DIVISION).targetState(Status.ASSIGNED.getCode())
                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR).targetState(Status.ASSIGNED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.ASSIGN_TO_DIVISION_WITH_ADDRESS).targetState(Status.ASSIGNED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS).targetState(Status.ASSIGNED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)
                
                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState);

        // Статус "Назначен"
        stateBuilder.state(Status.ASSIGNED.getCode())
                // Переназначение
                .event(Event.ASSIGN_TO_DIVISION)
                .action(this::saveHistory)
                .guard((entity, execution) -> isInitiator(entity))

                // Переназначение
                .and()
                .event(Event.ASSIGN_TO_EXECUTOR)
                .action(this::saveHistory)
                .guard((entity, execution) -> isInitiator(entity))

                // Переназначение
                .and()
                .event(Event.ASSIGN_TO_DIVISION_WITH_ADDRESS)
                .action(this::saveHistory)
                .guard((entity, execution) -> isInitiator(entity))

                // Переназначение
                .and()
                .event(Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS)
                .action(this::saveHistory)
                .guard((entity, execution) -> isInitiator(entity))

                .and()
                .event(Event.TAKE_TO_EXECUTION).targetState(Status.ON_EXECUTION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "На исполнении"
        stateBuilder.state(Status.ON_EXECUTION.getCode())
                .event(Event.TC_SEND_FOR_APPROVAL).targetState(Status.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

                .and()
                .event(Event.TC_CREATE_PARALLEL_EXECUTION)
                .action(this::saveHistory)
                .guard((entity, execution) -> isExecutor(entity) && hasNotActiveExecutions(entity))

                .and()
                .event(Event.TC_FORMATION_PROJECT)
                .action(this::saveHistory)
                .guard((entity, execution) -> isExecutor(entity))
//                .guard((entity, execution) -> isMainExecutor(entity) && hasNotActiveExecutions(entity))

                .and()
                .event(Event.TC_FORMATION_REASONED_REFUSAL)
                .action(this::saveHistory)
                .guard((entity, execution) -> isMainExecutor(entity))

                // когда исполняет начальник, выполняется авто-согласование при отправке на утверждение
                .and()
                .event(Event.TC_APPROVE).targetState(Status.APPROVED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                // когда исполняет начальник, сразу отправляет на утверждение (выполняется авто-согласование)
                .and()
                .event(Event.TC_SEND_FOR_SIGN).targetState(Status.AT_SIGNING.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity) && hasNotActiveExecutions(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "На согласовании"
        stateBuilder.state(Status.UNDER_APPROVAL.getCode())
                .event(Event.TCE_SEND_FOR_REVISION).targetState(Status.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.TC_APPROVE).targetState(Status.APPROVED.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.TC_RE_SEND_FOR_APPROVAL)
                .action(this::saveHistory)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Возвращено на доработку
        stateBuilder.state(Status.RETURNED_FOR_REVISION.getCode())
                .event(Event.TC_SEND_FOR_APPROVAL).targetState(Status.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.TC_FORMATION_PROJECT)
                .action(this::saveHistory)
                .guard((entity, execution) -> isMainExecutor(entity) && hasNotActiveExecutions(entity))

                .and()
                .event(Event.TC_FORMATION_REASONED_REFUSAL)
                .action(this::saveHistory)
                .guard((entity, execution) -> isMainExecutor(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "Согласовано"
        stateBuilder.state(Status.APPROVED.getCode())
                .event(Event.TC_SEND_FOR_SIGN).targetState(Status.AT_SIGNING.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity) && isManagerSigned(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState);

        // Статус "На подписании ТУ"
        stateBuilder.state(Status.AT_SIGNING.getCode())
                .event(Event.TCE_SEND_FOR_REVISION).targetState(Status.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

//                .and()
//                .event(Event.TC_SIGN).targetState(Status.COMPLETED.getCode())
//                .action(this::setState)
//                .guard((entity, execution) -> isExecutor(entity) && isManagerSigned(entity))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.CHANGE_ASSIGNEE)
                .action(this::saveHistory);

        // Статус "Возврат потребителю"
        stateBuilder.state(Status.RETURNED_TO_CONSUMER.getCode())
                .event(Event.RETURN_TO_CONSUMER)
                .action(this::saveHistory);

        // Статус "Отозван потребителем"
        stateBuilder.state(Status.REFUSED_BY_CONSUMER.getCode())
                .event(Event.REFUSE).targetState(Status.COMPLETED.getCode())
                .action(this::setState);
    }

    private void setState(TechConditionEntity entity, TechConditionExecutionEntity execution, String state, Event event) {
        entity.setStatusCode(state);
        saveHistory(entity, execution, state, event);
    }

    private void saveHistory(TechConditionEntity entity, TechConditionExecutionEntity execution, String state, Event event) {
        OffsetDateTime now = OffsetDateTime.now();
        entity.setLastModifiedDatetime(now);
        if (Event.DELETE.equals(event)) {
            entity.setDeletedDatetime(now);
            execution.setDeletedDatetime(now);
        }

        historyService.save(entity, execution, event);
    }

    private boolean isNotDeleted(TechConditionEntity entity) {
        return entity.getDeletedDatetime() == null;
    }

    private boolean isManagerSigned(TechConditionEntity entity) {
        return entity.getManagerSignedDatetime() != null;
    }

    private boolean isDirectorSigned(TechConditionEntity entity) {
        return entity.getDirectorSignedDatetime() != null;
    }

    private boolean hasNotActiveExecutions(TechConditionEntity entity) {
        return !entity.getHasActiveExecutions();
    }

    private boolean isExecutor(TechConditionEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = entity.getAssignees().contains(currentUser.getId());
        log.info("TECH CONDITION statemachine user id = [{}], isExecutor = [{}], assignees = [{}]",
                currentUser.getId(), result, entity.getAssignees());
        return result;
    }

    private boolean isMainExecutor(TechConditionEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = entity.getExecutor() != null && currentUser.getId().equals(entity.getExecutor().getId());
        log.info("TECH CONDITION statemachine user id = [{}], isMainExecutor = [{}], executor = [{}]",
                currentUser.getId(), result, entity.getExecutor() != null ? entity.getExecutor().getId() : null);
        return result;
    }

    private boolean isInitiator(TechConditionEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = currentUser.getId().equals(entity.getInitiatorId());
        log.info("TECH CONDITION statemachine user id = [{}], isInitiator = [{}], initiator = [{}]",
                currentUser.getId(), result, entity.getInitiatorId());
        return result;
    }
}
