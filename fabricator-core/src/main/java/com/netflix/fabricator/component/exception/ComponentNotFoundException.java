package com.netflix.fabricator.component.exception;

public class ComponentNotFoundException extends Exception {
    private static final long serialVersionUID = 5358538299200779367L;

    public ComponentNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
