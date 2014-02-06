package com.netflix.fabricator.component.exception;

public class ComponentAlreadyExistsException extends Exception {
    private static final long serialVersionUID = -5928460848593945925L;

    public ComponentAlreadyExistsException(String message) {
        super(message);
    }
}
