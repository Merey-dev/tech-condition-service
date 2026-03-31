package kz.kus.sa.tech.condition.exception;

import java.util.HashSet;
import java.util.Set;

public class ValidationErrorResponse extends BaseErrorResponse {
    private Set<String> invalidFields = new HashSet<>();

    public ValidationErrorResponse(String message, String error, String path) {
        super(message, error, path);
    }

    public void setInvalidFields(Set<String> invalidFields) {
        this.invalidFields = invalidFields;
    }

    public Set<String> getInvalidFields() {
        return this.invalidFields;
    }

    public ValidationErrorResponse(String message, String error, String path, Set<String> invalidFields) {
        super(message, error, path);
        this.invalidFields = invalidFields;
    }

    public static class Builder {
        private String message;
        private String error;
        private String path;
        private Set<String> invalidFields = new HashSet<>();

        public Builder() {
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setError(String error) {
            this.error = error;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setInvalidFields(Set<String> invalidFields) {
            this.invalidFields = invalidFields;
            return this;
        }

        public ValidationErrorResponse build() {
            return new ValidationErrorResponse(this.message, this.error, this.path, this.invalidFields);
        }
    }
}