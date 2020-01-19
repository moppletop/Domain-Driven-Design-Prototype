package com.moppletop.ddd.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on methods which handle business events, for example database table updates.
 * Note! event handlers should be synchronous
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StreamEventHandler {
}
