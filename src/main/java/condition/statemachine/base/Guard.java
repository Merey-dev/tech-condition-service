package condition.statemachine.base;

/**
 * @param <O>  Business entity
 * @param <OE> Business entity
 */
public interface Guard<O, OE> {
    boolean evaluate(O entity, OE execution);
}
