package com.energymanagement.usermanagement.exception;

// Another microservice could not be reached or answered with an error
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}