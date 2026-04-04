package condition.exception;

import kz.kus.sa.tech.condition.exception.BaseErrorResponse;

public class CustomErrorResponse extends BaseErrorResponse {

    public CustomErrorResponse(String message, String error, String path) {
        super(message, error, path);
    }
}
