package com.company.exception;

public class DepartmentServiceBadResponseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DepartmentServiceBadResponseException(String message) {
        super(message);
    }
}
