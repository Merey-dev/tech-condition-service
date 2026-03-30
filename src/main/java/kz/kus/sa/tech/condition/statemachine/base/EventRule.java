package kz.kus.sa.tech.condition.statemachine.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <S>  State
 * @param <E>  Event
 * @param <O>  Business entity
 * @param <OE> Business entity
 */
public class EventRule<S, E, O, OE> {
    E event;
    S targetState;
    OE execution;
    List<Action<O, OE,  S, E>> actionList = new ArrayList();
    Guard<O, OE> guard;

    public EventRule() {
    }

    public S getTargetState() {
        return this.targetState;
    }

    public E getEvent() {
        return this.event;
    }

    public OE getObjectExecution() {
        return this.execution;
    }

    public void runAction(O entity, OE execution) {
        for (Action action : actionList) {
            action.run(entity, execution, this.targetState, this.event);
        }
    }

    public boolean runGuard(O entity, OE execution) {
        return this.guard != null ? this.guard.evaluate(entity, execution) : true;
    }
}