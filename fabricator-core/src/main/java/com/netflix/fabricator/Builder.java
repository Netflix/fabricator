package com.netflix.fabricator;

/**
 * Interface definition for a builder pattern.  Use then when providing a specific
 * builder implementation for a type without going through the internal {@link ComponentFactoryProvider).
 * 
 * @see MyComponentBuilder#builder()
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface Builder<T> {
    public T build();
}
