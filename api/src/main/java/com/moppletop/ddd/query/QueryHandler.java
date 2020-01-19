package com.moppletop.ddd.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on methods which will be called when the {@link QueryGateway#query(String, Object, Class)} for it's "value"
 * is called
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryHandler {

    String value();

}
