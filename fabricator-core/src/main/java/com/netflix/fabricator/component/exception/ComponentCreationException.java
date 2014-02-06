package com.netflix.fabricator.component.exception;

public class ComponentCreationException extends Exception {
    private static final long serialVersionUID = 7847902103334820478L;
    public ComponentCreationException(String message) {
        super(message);
    }
    public ComponentCreationException(String message, Throwable t) {
        super(message, t);
    }
}
