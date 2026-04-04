package condition.exception;

import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.ValidationErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<kz.kus.sa.tech.condition.exception.ValidationErrorResponse> methodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException e) {
        final String message = getMessage(kz.kus.sa.tech.condition.exception.ErrorCode.VALIDATION_ERROR, req);
        Set<String> errors = new HashSet<>();
        e.getBindingResult().getFieldErrors().forEach(
                error -> {
                    String fieldMessage = error.getField();
                    errors.add(fieldMessage + " " + error.getDefaultMessage());
                }
        );
        kz.kus.sa.tech.condition.exception.ValidationErrorResponse errorBody = new ValidationErrorResponse(message, kz.kus.sa.tech.condition.exception.ErrorCode.VALIDATION_ERROR.name(), req.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(kz.kus.sa.tech.condition.exception.NotFoundException.class)
    public ResponseEntity<kz.kus.sa.tech.condition.exception.CustomErrorResponse> notFoundException(HttpServletRequest req, kz.kus.sa.tech.condition.exception.NotFoundException e) {
        final String message = getMessage(kz.kus.sa.tech.condition.exception.ErrorCode.RESOURCE_NOT_FOUND, req);
        kz.kus.sa.tech.condition.exception.CustomErrorResponse errorBody = new kz.kus.sa.tech.condition.exception.CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(kz.kus.sa.tech.condition.exception.BadRequestException.class)
    public ResponseEntity<kz.kus.sa.tech.condition.exception.CustomErrorResponse> badRequestException(HttpServletRequest req, BadRequestException e) {
        final String message = getMessage(kz.kus.sa.tech.condition.exception.ErrorCode.BAD_REQUEST, req);
        kz.kus.sa.tech.condition.exception.CustomErrorResponse errorBody = new kz.kus.sa.tech.condition.exception.CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(kz.kus.sa.tech.condition.exception.BusinessException.class)
    public ResponseEntity<kz.kus.sa.tech.condition.exception.CustomErrorResponse> businessException(HttpServletRequest req, kz.kus.sa.tech.condition.exception.BusinessException e) {
        final String message = getMessage(kz.kus.sa.tech.condition.exception.ErrorCode.BUSINESS_ERROR, req);
        kz.kus.sa.tech.condition.exception.CustomErrorResponse errorBody = new kz.kus.sa.tech.condition.exception.CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    private String getMessage(ErrorCode errorCode, HttpServletRequest req) {
//        return messageSource.getMessage(errorCode.name(), null, errorCode.name(), req.getLocale());
        return errorCode.name();
    }
}
