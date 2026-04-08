package kz.kus.sa.tech.condition.statemachine;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.enums.AbdAddressDecisionStatus;
import kz.kus.sa.tech.condition.statemachine.base.StateConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class TechConditionDecisionStatemachine extends StateConfig<String, Event, TechConditionExecutionAbdAddressDecisionEntity, TechConditionExecutionEntity> {

    private final CurrentUserApiService currentUserApiService;

    @SuppressWarnings("unchecked")
    protected void configure(StateBuilder<String, Event, TechConditionExecutionAbdAddressDecisionEntity, TechConditionExecutionEntity> stateBuilder) {

        // Статус "Назначен"
        stateBuilder.state(AbdAddressDecisionStatus.ASSIGNED.getCode())
                .event(Event.TCE_TAKE_TO_EXECUTION).targetState(AbdAddressDecisionStatus.ON_EXECUTION.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.ASSIGN_TO_DIVISION)
                .action(this::saveState)

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR)
                .action(this::saveState);

        // Статус "На исполнении"
        stateBuilder.state(AbdAddressDecisionStatus.ON_EXECUTION.getCode())
                .event(Event.EXECUTE).targetState(AbdAddressDecisionStatus.EXECUTED.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.TCE_SEND_FOR_REVISION).targetState(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.ASSIGN_TO_DIVISION).targetState(AbdAddressDecisionStatus.ASSIGNED.getCode())
                .action(this::setState)

                .and()
                .event(Event.ASSIGN_TO_EXECUTOR).targetState(AbdAddressDecisionStatus.ASSIGNED.getCode())
                .action(this::setState);

        // Статус "Возвращено на доработку"
        stateBuilder.state(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode())
                .event(Event.EXECUTE).targetState(AbdAddressDecisionStatus.EXECUTED.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.TCE_SEND_FOR_APPROVAL).targetState(AbdAddressDecisionStatus.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision));

        // Статус "Исполнено"
        stateBuilder.state(AbdAddressDecisionStatus.EXECUTED.getCode())
                .event(Event.TCE_SEND_FOR_APPROVAL).targetState(AbdAddressDecisionStatus.UNDER_APPROVAL.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                // когда исполняет начальник сразу согласовывает
                .and()
                .event(Event.TCE_APPROVE).targetState(AbdAddressDecisionStatus.APPROVED.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision));

        // Статус "На согласовании"
        stateBuilder.state(AbdAddressDecisionStatus.UNDER_APPROVAL.getCode())
                .event(Event.TCE_APPROVE).targetState(AbdAddressDecisionStatus.APPROVED.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.TCE_SEND_FOR_REVISION).targetState(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision));

        // Статус "Согласовано"
        stateBuilder.state(AbdAddressDecisionStatus.APPROVED.getCode())
                .event(Event.TC_SEND_FOR_SIGN).targetState(AbdAddressDecisionStatus.AT_SIGNING.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.TCE_SEND_FOR_REVISION).targetState(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision));

        // Статус "На подписании"
        stateBuilder.state(AbdAddressDecisionStatus.AT_SIGNING.getCode())
                .event(Event.TC_SIGN).targetState(AbdAddressDecisionStatus.SIGNED.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision))

                .and()
                .event(Event.TCE_SEND_FOR_REVISION).targetState(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode())
                .action(this::setState)
                .guard((decision, execution) -> isExecutor(decision));

        // Статус "Подписано"
        stateBuilder.state(AbdAddressDecisionStatus.SIGNED.getCode());
    }

    private void setState(TechConditionExecutionAbdAddressDecisionEntity decision,
                          TechConditionExecutionEntity execution,
                          String state, Event event) {
        decision.setStatusCode(state);
        decision.setLastModifiedDatetime(OffsetDateTime.now());
        log.info("DECISION [STATE CHANGED]: id=[{}], state=[{}], event=[{}]", decision.getId(), state, event);
    }

    private void saveState(TechConditionExecutionAbdAddressDecisionEntity decision,
                           TechConditionExecutionEntity execution,
                           String state, Event event) {
        decision.setLastModifiedDatetime(OffsetDateTime.now());
        log.info("DECISION [SAVE STATE]: id=[{}], event=[{}]", decision.getId(), event);
    }

    private boolean isExecutor(TechConditionExecutionAbdAddressDecisionEntity decision) {
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        boolean result = decision.getAssignees() != null
                && decision.getAssignees().contains(currentUser.getId());
        log.info("DECISION statemachine user id=[{}], isExecutor=[{}], assignees=[{}]",
                currentUser.getId(), result, decision.getAssignees());
        return result;
    }
}
