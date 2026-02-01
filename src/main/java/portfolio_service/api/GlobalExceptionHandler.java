package portfolio_service.api;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import portfolio_service.config.TraceIdFilter;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        return ResponseEntity.badRequest().body(ApiError.builder()
                .code("VALIDATION_ERROR")
                .message("Request validation failed.")
                .traceId(traceId())
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex) {
        List<ApiError.FieldViolation> details = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ApiError.builder()
                .code("VALIDATION_ERROR")
                .message("Constraint validation failed.")
                .traceId(traceId())
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.builder()
                .code("NOT_FOUND")
                .message(ex.getMessage())
                .traceId(traceId())
                .timestamp(Instant.now())
                .details(List.of())
                .build());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDb(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.builder()
                .code("DB_ERROR")
                .message("Database error.")
                .traceId(traceId())
                .timestamp(Instant.now())
                .details(List.of())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.builder()
                .code("INTERNAL_ERROR")
                .message("Unexpected error.")
                .traceId(traceId())
                .timestamp(Instant.now())
                .details(List.of())
                .build());
    }

    private ApiError.FieldViolation mapFieldError(FieldError fe) {
        String issue = fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid";
        return new ApiError.FieldViolation(fe.getField(), issue);
    }

    private String traceId() {
        String t = MDC.get(TraceIdFilter.TRACE_ID);
        return (t == null || t.isBlank()) ? "missing" : t;
    }
}
