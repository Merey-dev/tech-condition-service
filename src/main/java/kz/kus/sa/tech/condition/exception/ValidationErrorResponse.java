package kz.kus.sa.tech.condition.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class ValidationErrorResponse extends BaseErrorResponse {

    private Set<String> invalidFields = new HashSet<>();

    public ValidationErrorResponse(String message, String error, String path) {
        super(message, error, path);
    }

    public ValidationErrorResponse(String message, String error, String path, Set<String> invalidFields) {
        super(message, error, path);
        this.invalidFields = invalidFields;
    }
}