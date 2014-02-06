package com.netflix.fabricator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation specifying a specific subtype.
 * This is the value used to determine which instance to allocate in 
 * a configuration of polymorphic components.  
 * 
 * 
 * @author elandau
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubType {
    String value();
}
