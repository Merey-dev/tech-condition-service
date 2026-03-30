package kz.kus.sa.tech.condition.exception;

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
    public ResponseEntity<ValidationErrorResponse> methodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException e) {
        final String message = getMessage(ErrorCode.VALIDATION_ERROR, req);
        Set<String> errors = new HashSet<>();
        e.getBindingResult().getFieldErrors().forEach(
                error -> {
                    String fieldMessage = error.getField();
                    errors.add(fieldMessage + " " + error.getDefaultMessage());
                }
        );
        ValidationErrorResponse errorBody = new ValidationErrorResponse(message, ErrorCode.VALIDATION_ERROR.name(), req.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomErrorResponse> notFoundException(HttpServletRequest req, NotFoundException e) {
        final String message = getMessage(ErrorCode.RESOURCE_NOT_FOUND, req);
        CustomErrorResponse errorBody = new CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomErrorResponse> badRequestException(HttpServletRequest req, BadRequestException e) {
        final String message = getMessage(ErrorCode.BAD_REQUEST, req);
        CustomErrorResponse errorBody = new CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomErrorResponse> businessException(HttpServletRequest req, BusinessException e) {
        final String message = getMessage(ErrorCode.BUSINESS_ERROR, req);
        CustomErrorResponse errorBody = new CustomErrorResponse(message, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    private String getMessage(ErrorCode errorCode, HttpServletRequest req) {
//        return messageSource.getMessage(errorCode.name(), null, errorCode.name(), req.getLocale());
        return errorCode.name();
    }
}
