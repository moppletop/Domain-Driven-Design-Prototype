package com.moppletop.ddd.wiring;

import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.error.IllegalDomainDefinitionException;
import com.moppletop.ddd.error.UnsatisfiedDependencyException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class DependantExecutable<T extends Executable> {

    private T executable;
    private int primaryParameterIndex = -1;

    protected void evaluate(DependencyRegister registrar) {
        if (hasEvaluated()) {
            return;
        }

        Parameter[] parameters = executable.getParameters();
        int index = -1;
        List<Class<?>> unknownParams = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object dependency = registrar.getDependency(parameter.getType());

            if (dependency == null) {
                index = i;
                unknownParams.add(parameter.getType());
            }
        }

        if (unknownParams.isEmpty()) {
            throw new IllegalDomainDefinitionException("Expected " + executable.toGenericString() + " to have a command/event as a parameter");
        } else if (unknownParams.size() > 1) {
            throw new IllegalDomainDefinitionException("Expected " + executable.toGenericString() + " to have only one command/event as a parameter, found: " + unknownParams.stream().map(Class::toGenericString).collect(Collectors.joining(", ")));
        }

        this.primaryParameterIndex = index;
    }

    public boolean hasEvaluated() {
        return primaryParameterIndex != -1;
    }

    protected Object[] getParameters(Object primaryParam, DependencyRegister registrar) {
        Parameter[] parameters = executable.getParameters();
        Object[] objects = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (i == primaryParameterIndex) {
                objects[i] = primaryParam;
            } else {
                Object dependency = registrar.getDependency(parameter.getType());

                if (dependency == null) {
                    throw new UnsatisfiedDependencyException("Unsatisfied dependency for " + executable.getClass() + "(.." + parameter.getType().getName() + "..)");
                }

                objects[i] = dependency;
            }
        }

        return objects;
    }

    public void setExecutable(T executable, DependencyRegister registrar) {
        this.executable = executable;
        evaluate(registrar);
    }
}
