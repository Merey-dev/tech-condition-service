package kz.kus.sa.tech.condition.statemachine.base;

import kz.kus.sa.tech.condition.statemachine.exception.GuardException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownEventException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownStateException;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <S>  State
 * @param <E>  Event
 * @param <O>  Business entity
 * @param <OE> Business entity
 */
public abstract class StateConfig<S, E, O, OE> {
    private Map<S, Map<E, EventRule<S, E, O, OE>>> stateMap = new HashMap();
    private StateBuilder<S, E, O, OE> stateBuilder;

    public StateConfig() {
        this.stateBuilder = new StateBuilder(this.stateMap);
    }

    @PostConstruct
    private void init() {
        this.configure(this.stateBuilder);
    }

    protected void configure(StateBuilder<S, E, O, OE> stateBuilder) {
    }

    /**
     * @param entity Business entity
     * @param state  State
     * @param event  Event
     */
    public final O changeState(O entity, OE execution, S state, E event) throws UnknownStateException, UnknownEventException, GuardException {
        if (!this.stateMap.containsKey(state)) {
            throw new UnknownStateException("Unknown state");
        } else {
            Map<E, EventRule<S, E, O, OE>> eventMap = this.stateMap.get(state);
            if (!eventMap.containsKey(event)) {
                throw new UnknownEventException("Unknown event");
            } else {
                EventRule<S, E, O, OE> eventRule = eventMap.get(event);
                if (!eventRule.runGuard(entity, execution)) {
                    throw new GuardException("Guard exception");
                } else {
                    eventRule.runAction(entity, execution);
                    return entity;
                }
            }
        }
    }

    public final void checkState(O entity, OE execution, S state, E event) throws GuardException, UnknownStateException, UnknownEventException {
        if (!this.stateMap.containsKey(state)) {
            throw new UnknownStateException("Unknown state");
        } else {
            Map<E, EventRule<S, E, O, OE>> eventMap = this.stateMap.get(state);
            if (!eventMap.containsKey(event)) {
                throw new UnknownEventException("Unknown event");
            } else {
                EventRule<S, E, O, OE> eventRule = eventMap.get(event);
                if (!eventRule.runGuard(entity, execution)) {
                    throw new GuardException("Guard exception");
                }
            }
        }
    }

    public final Map<E, EventRule<S, E, O, OE>> getActions(S state) {
        return (this.stateMap.containsKey(state) ? this.stateMap.get(state) : new HashMap());
    }

    /**
     * @param <S> State
     * @param <E> Event
     * @param <O> Business entity
     */
    public static class EventRuleBuilder<S, E, O, OE> {
        private EventBuilder<S, E, O, OE> eventBuilder;
        private EventRule<S, E, O, OE> actionRule;

        public EventRuleBuilder(EventBuilder<S, E, O, OE> eventBuilder, EventRule<S, E, O, OE> actionRule, E event) {
            this.eventBuilder = eventBuilder;
            this.actionRule = actionRule;
            this.actionRule.event = event;
        }

        public EventRuleBuilder<S, E, O, OE> targetState(S targetState) {
            this.actionRule.targetState = targetState;
            return this;
        }

        public EventRuleBuilder<S, E, O, OE> action(Action<O, OE, S, E>... action) {
            this.actionRule.actionList.addAll(Arrays.asList(action));
            return this;
        }

        public EventRuleBuilder<S, E, O, OE> action(List<Action<O, OE, S, E>> actionList) {
            this.actionRule.actionList.addAll(actionList);
            return this;
        }

        public EventRuleBuilder<S, E, O, OE> guard(Guard<O, OE> guard) {
            this.actionRule.guard = guard;
            return this;
        }

        public EventBuilder<S, E, O, OE> and() {
            return this.eventBuilder;
        }
    }

    public static class EventBuilder<S, E, O, OE> {
        private Map<E, EventRule<S, E, O, OE>> eventMap;

        public EventBuilder(Map<E, EventRule<S, E, O, OE>> eventMap) {
            this.eventMap = eventMap;
        }

        public EventRuleBuilder<S, E, O, OE> event(E event) {
            this.eventMap.put(event, new EventRule());
            return new EventRuleBuilder(this, this.eventMap.get(event), event);
        }
    }

    public static class StateBuilder<S, E, O, OE> {
        private Map<S, Map<E, EventRule<S, E, O, OE>>> stateMap;

        public StateBuilder(Map<S, Map<E, EventRule<S, E, O, OE>>> stateMap) {
            this.stateMap = stateMap;
        }

        public EventBuilder<S, E, O, OE> state(S state) {
            this.stateMap.put(state, new HashMap());
            return new EventBuilder(this.stateMap.get(state));
        }
    }
}
