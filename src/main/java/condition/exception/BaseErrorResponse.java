package condition.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class BaseErrorResponse {
    private String message;
    private String error;
    private String path;
}
