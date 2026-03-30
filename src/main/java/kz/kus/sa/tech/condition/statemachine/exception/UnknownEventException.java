package kz.kus.sa.tech.condition.statemachine.exception;

public class UnknownEventException extends Exception {
    public UnknownEventException(String message) {
        super(message);
    }
}
