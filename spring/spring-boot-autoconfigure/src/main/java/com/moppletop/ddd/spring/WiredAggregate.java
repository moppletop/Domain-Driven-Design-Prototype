package com.moppletop.ddd.spring;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("prototype") // Aggregates aren't singletons, hence we'll always need a new instance
public @interface WiredAggregate {
}
