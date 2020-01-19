package com.moppletop.ddd.query;

import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.error.IllegalDomainDefinitionException;
import com.moppletop.ddd.wiring.DependantExecutable;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class QueryHandlerMethod<I, O> extends DependantExecutable<Method> {

    private final Object parentObject;
    private final Class<O> classOfOutput;

    public O execute(I input, DependencyRegister registrar) {
        try {
            return classOfOutput.cast(getExecutable().invoke(parentObject, getParameters(input, registrar)));
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalDomainDefinitionException(ex);
        }
    }
}
