package com.company.exception;

public class DepartmentServiceTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DepartmentServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
