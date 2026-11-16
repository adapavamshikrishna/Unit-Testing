package com.company.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Resource",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Request validation failed: method={}, path={}, errorCount={}",
                request.getMethod(), request.getRequestURI(),
                ex.getBindingResult().getFieldErrorCount());
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more request fields are invalid", request, fieldErrors);
    }


    @ExceptionHandler(DepartmentServiceTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentServiceTimeout(
            DepartmentServiceTimeoutException ex, HttpServletRequest request) {
        log.warn("Department service timeout: method={}, path={}",
                request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(DepartmentServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentServiceUnavailable(
            DepartmentServiceUnavailableException ex, HttpServletRequest request) {
        log.error("Department service unavailable: method={}, path={}",
                request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(DepartmentServiceBadResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentServiceBadResponse(
            DepartmentServiceBadResponseException ex, HttpServletRequest request) {
        log.warn("Invalid Department service response: method={}, path={}",
                request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_GATEWAY, "Bad Gateway",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        // Log stack trace internally, but keep details out of the API response.
        log.error("Unexpected server error: method={}, path={}",
                request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String error, String message,
            HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
