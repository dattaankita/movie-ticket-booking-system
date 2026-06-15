package com.codingtest.movieticketbookingsystem.common.exception;

import com.codingtest.movieticketbookingsystem.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        return buildResponse(ex.getStatus(), ex.getMessage(), ex.getErrorCode(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "VALIDATION_ERROR",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed or unreadable request body",
                "MALFORMED_JSON",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = ex.getName() + " must be a valid " + simpleTypeName(ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                "TYPE_MISMATCH",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking conflict on {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Resource was modified by another request. Please retry.",
                "CONCURRENT_MODIFICATION",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                "INTERNAL_ERROR",
                request.getRequestURI(),
                null
        );
    }

    private String simpleTypeName(MethodArgumentTypeMismatchException ex) {
        Class<?> required = ex.getRequiredType();
        if (required == null) {
            return "value";
        }
        if (required == Long.class || required == long.class) {
            return "number";
        }
        return required.getSimpleName();
    }

    private ApiErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return ApiErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String errorCode,
            String path,
            List<ApiErrorResponse.FieldError> fieldErrors) {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
