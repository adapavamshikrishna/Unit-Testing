package com.company.exception;

public class DepartmentServiceUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DepartmentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DepartmentServiceUnavailableException(String message) {
        super(message);
    }
}
