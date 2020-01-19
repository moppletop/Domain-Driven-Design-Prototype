package com.moppletop.ddd.aggregate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on methods inside an aggregate which will update it's states based on the contents of the event
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateStateHandler {
}
