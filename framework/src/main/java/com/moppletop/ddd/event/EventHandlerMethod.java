package com.moppletop.ddd.event;

import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.error.IllegalDomainDefinitionException;
import com.moppletop.ddd.wiring.DependantExecutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
@Getter
public class EventHandlerMethod extends DependantExecutable<Method> {

    private final Object parentObject;

    public void execute(Object event, DependencyRegister registrar) {
        try {
            getExecutable().invoke(parentObject, getParameters(event, registrar));
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalDomainDefinitionException(ex);
        }
    }

}
