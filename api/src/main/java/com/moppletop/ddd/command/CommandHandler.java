package com.moppletop.ddd.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on methods/constructors inside an aggregate which will apply events based on the contents of this command and inferred values
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHandler {
}
