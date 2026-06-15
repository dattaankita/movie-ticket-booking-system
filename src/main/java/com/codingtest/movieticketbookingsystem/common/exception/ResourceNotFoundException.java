package com.codingtest.movieticketbookingsystem.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id,
                org.springframework.http.HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, org.springframework.http.HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
