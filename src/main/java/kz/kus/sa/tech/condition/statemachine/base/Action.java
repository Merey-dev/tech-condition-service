package kz.kus.sa.tech.condition.statemachine.base;

/**
 * @param <O> Business entity
 * @param <S> State
 */
public interface Action<O, OE, S, E> {
    void run(O entity, OE execution, S state, E event);
}