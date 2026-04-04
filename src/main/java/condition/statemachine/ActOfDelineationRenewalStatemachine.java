package condition.statemachine;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity;
import kz.kus.sa.tech.condition.service.history.HistoryService;
import kz.kus.sa.tech.condition.statemachine.base.StateConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class ActOfDelineationRenewalStatemachine extends StateConfig<String, Event, ActOfDelineationRenewalEntity, ActOfDelineationRenewalExecutionEntity> {

    private final CurrentUserApiService currentUserApiService;
    private final HistoryService historyService;

    @SuppressWarnings("unchecked")
    protected void configure(StateBuilder<String, Event, ActOfDelineationRenewalEntity, ActOfDelineationRenewalExecutionEntity> stateBuilder) {

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

        // Статус "Зарегистрировано"
        stateBuilder.state(Status.REGISTERED.getCode())
                .event(Event.ASSIGN_TO_DIVISION).targetState(Status.ASSIGNED.getCode())
                .action(this::setState)

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
                .event(Event.TAKE_TO_EXECUTION).targetState(Status.ON_EXECUTION.getCode())
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
                .event(Event.TAKE_TO_EXECUTION)
                .action(this::saveHistory)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.SEND_FOR_APPROVAL).targetState(Status.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

                .and()
                .event(Event.SENT_FOR_CONFIRMATION).targetState(Status.AT_SIGNING.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

                .and()
                .event(Event.SEND_FOR_REVISION).targetState(Status.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.TAKE_TO_EXECUTION).targetState(Status.ON_EXECUTION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity));

        // Статус "На согласовании"
        stateBuilder.state(Status.UNDER_APPROVAL.getCode())
                .event(Event.SEND_FOR_REVISION).targetState(Status.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((entity, execution) -> isExecutor(entity))

                .and()
                .event(Event.TC_APPROVE).targetState(Status.APPROVED.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

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

        // Статус "Возвращено на доработку"
        stateBuilder.state(Status.RETURNED_FOR_REVISION.getCode())
                .event(Event.SEND_FOR_APPROVAL).targetState(Status.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

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
                .event(Event.SEND_FOR_APPROVAL).targetState(Status.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

                .and()
                .event(Event.SENT_FOR_CONFIRMATION).targetState(Status.AT_SIGNING.getCode())
                .action(this::setState)
                .guard(((entity, execution) -> isExecutor(entity)))

                .and()
                .event(Event.RETURN_TO_CONSUMER).targetState(Status.RETURNED_TO_CONSUMER.getCode())
                .action(this::setState)

                .and()
                .event(Event.REFUSE).targetState(Status.REFUSED_BY_CONSUMER.getCode())
                .action(this::setState);

        // Статус "На подписании"
        stateBuilder.state(Status.AT_SIGNING.getCode())
                .event(Event.SEND_FOR_REVISION).targetState(Status.RETURNED_FOR_REVISION.getCode())
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

        // Статус "Возврат потребителю"
        stateBuilder.state(Status.RETURNED_TO_CONSUMER.getCode())
                .event(Event.RETURN_TO_CONSUMER)
                .action(this::saveHistory);

        // Статус "Отозван потребителем"
        stateBuilder.state(Status.REFUSED_BY_CONSUMER.getCode())
                .event(Event.REFUSE).targetState(Status.COMPLETED.getCode())
                .action(this::setState);
    }

    private void setState(ActOfDelineationRenewalEntity entity, ActOfDelineationRenewalExecutionEntity execution, String state, Event event) {
        entity.setStatusCode(state);
        saveHistory(entity, execution, state, event);
    }

    private void saveHistory(ActOfDelineationRenewalEntity entity, ActOfDelineationRenewalExecutionEntity execution, String state, Event event) {
        OffsetDateTime now = OffsetDateTime.now();
        entity.setLastModifiedDatetime(now);
        if (Event.DELETE.equals(event)) {
            entity.setDeletedDatetime(now);
            execution.setDeletedDatetime(now);
        }

        historyService.save(entity, execution, event);
    }

    private boolean isNotDeleted(ActOfDelineationRenewalEntity entity) {
        return entity.getDeletedDatetime() == null;
    }

    private boolean isManagerApproved(ActOfDelineationRenewalEntity entity) {
        return entity.getManagerApprovedDatetime() != null;
    }

    private boolean isDirectorApproved(ActOfDelineationRenewalEntity entity) {
        return entity.getDirectorApprovedDatetime() != null;
    }

    private boolean isExecutor(ActOfDelineationRenewalEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = entity.getAssignees().contains(currentUser.getId());
        log.info("ACT OF DELINEATION statemachine user id = [{}], isExecutor = [{}], assignees = [{}]",
                currentUser.getId(), result, entity.getAssignees());
        return result;
    }

    private boolean isMainExecutor(ActOfDelineationRenewalEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = entity.getExecutor() != null && currentUser.getId().equals(entity.getExecutor().getId());
        log.info("ACT OF DELINEATION statemachine user id = [{}], isMainExecutor = [{}], executor = [{}]",
                currentUser.getId(), result, entity.getExecutor() != null ? entity.getExecutor().getId() : null);
        return result;
    }

    private boolean isInitiator(ActOfDelineationRenewalEntity entity) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = currentUser.getId().equals(entity.getInitiatorId());
        log.info("ACT OF DELINEATION statemachine user id = [{}], isInitiator = [{}], initiator = [{}]",
                currentUser.getId(), result, entity.getInitiatorId());
        return result;
    }
}
