package com.moppletop.ddd.wiring;

import com.moppletop.ddd.aggregate.AggregateStateHandler;
import com.moppletop.ddd.command.Command;
import com.moppletop.ddd.command.CommandHandler;
import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.error.*;
import com.moppletop.ddd.event.*;
import com.moppletop.ddd.query.QueryHandler;
import com.moppletop.ddd.query.QueryHandlerMethod;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@RequiredArgsConstructor
public class ExplicitWiringManager implements WiringManager {

    private final DependencyRegister dependencyRegister;
    private final Set<Class<?>> aggregates = new HashSet<>();
    private final Map<Class<?>, Executable> commandHandlers = new HashMap<>();
    private final Map<Class<?>, Method> stateHandlers = new HashMap<>();
    private final Map<Class<?>, List<EventHandlerMethod>> domainEventHandlers = new HashMap<>();
    private final Map<Class<?>, List<EventHandlerMethod>> streamEventHandlers = new HashMap<>();
    private final Map<String, QueryHandlerMethod<?, ?>> queryHandlers = new HashMap<>();

    public void registerAggregate(Class<?> clazz) {
        if (aggregates.add(clazz)) {
            registerCommandHandlers(clazz);
            registerAggregateStateHandlers(clazz);
        }
    }

    public void registerCommandHandlers(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            constructor.setAccessible(true);

            if (constructor.getDeclaredAnnotation(CommandHandler.class) != null) {
                Class<?> classOfCommand = null;

                for (Parameter parameter : constructor.getParameters()) {
                    if (Command.class.isAssignableFrom(parameter.getType())) {
                        if (classOfCommand == null) {
                            classOfCommand = parameter.getType();
                        } else {
                            throw new IllegalDomainDefinitionException("The constructor of " + clazz.getName() + " has more than one parameter that implements Command");
                        }
                    }
                }

                if (classOfCommand == null) {
                    throw new IllegalDomainDefinitionException("The constructor of " + clazz.getName() + " doesn't have a parameter that implements Command");
                } else {
                    commandHandlers.put(classOfCommand, constructor);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.getDeclaredAnnotation(CommandHandler.class) != null) {
                Class<?> classOfCommand = null;

                for (Parameter parameter : method.getParameters()) {
                    if (Command.class.isAssignableFrom(parameter.getType())) {
                        if (classOfCommand == null) {
                            classOfCommand = parameter.getType();
                        } else {
                            throw new IllegalDomainDefinitionException("The method " + clazz.getName() + '#' + method.getName() + " has more than one parameter than implements Command");
                        }
                    }
                }

                if (classOfCommand == null) {
                    throw new IllegalDomainDefinitionException("The method " + clazz.getName() + '#' + method.getName() + " doesn't have a parameter that implements Command");
                } else {
                    commandHandlers.put(classOfCommand, method);
                }
            }
        }
    }

    private void registerAggregateStateHandlers(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.getDeclaredAnnotation(AggregateStateHandler.class) != null) {
                Parameter[] parameters = method.getParameters();

                if (parameters.length > 1) {
                    continue;
                }

                Class<?> classOfEvent = parameters[0].getType();

                if (classOfEvent == null) {
                    throw new IllegalDomainDefinitionException("The method " + clazz.getName() + '#' + method.getName() + " doesn't have a parameter that implements Command");
                } else {
                    stateHandlers.put(classOfEvent, method);
                }
            }
        }
    }

    public void registerDomainEventHandlers(Object object) {
        registerEventHandlers(object, DomainEventHandler.class, domainEventHandlers);
    }

    public void registerStreamEventHandlers(Object object, EventStreamer eventStreamer) {
        if (registerEventHandlers(object, StreamEventHandler.class, streamEventHandlers)) {
            Class<?> clazz = object.getClass();
            ProcessingGroup groupAnnotation = clazz.getDeclaredAnnotation(ProcessingGroup.class);
            String processingGroup;
            int threads;

            if (groupAnnotation == null) {
                processingGroup = clazz.getPackage().getName();
                threads = 1;
            } else {
                if (groupAnnotation.name().isEmpty()) {
                    processingGroup = clazz.getPackage().getName();
                } else {
                    processingGroup = groupAnnotation.name();
                }

                threads = groupAnnotation.threads();
            }

            if (threads <= 0) {
                throw new IllegalArgumentException("The number of threads in a processing group must be a positive number!");
            } else if (threads > ProcessingGroup.MAX_THREADS) {
                throw new IllegalArgumentException("The number of threads in a processing group must be less than " + ProcessingGroup.MAX_THREADS + "!");
            }

            eventStreamer.subscribe(processingGroup, threads);
        }
    }

    private boolean registerEventHandlers(Object object, Class<? extends Annotation> classOfAnnotation, Map<Class<?>, List<EventHandlerMethod>> map) {
        int registered = 0;

        for (Method method : object.getClass().getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.getDeclaredAnnotation(classOfAnnotation) != null) {
                EventHandlerMethod eventHandlerMethod = new EventHandlerMethod(object);
                eventHandlerMethod.setExecutable(method, dependencyRegister);

                Class<?> clazz = method.getParameters()[eventHandlerMethod.getPrimaryParameterIndex()].getType();

                map.computeIfAbsent(clazz, k -> new LinkedList<>()).add(eventHandlerMethod);
                registered++;
            }
        }

        return registered > 0;
    }

    public void registerQueryHandlers(Object object) {
        Class<?> clazz = object.getClass();
        QueryHandler annotation;

        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            annotation = method.getDeclaredAnnotation(QueryHandler.class);

            if (annotation != null) {
                QueryHandlerMethod<?, ?> queryHandlerMethod = new QueryHandlerMethod<>(object, method.getReturnType());
                queryHandlerMethod.setExecutable(method, dependencyRegister);
                queryHandlers.put(annotation.value(), queryHandlerMethod);
            }
        }
    }

    @Override
    public <T> T executeCommandHandler(T aggregate, Command<T> cmd) {
        Objects.requireNonNull(cmd, "Cannot execute a null command");
        Executable executable = commandHandlers.get(cmd.getClass());

        if (executable == null) {
            throw new CommandHandlerNotFoundException("Unable to find a command handler for " + cmd.getClass().getName());
        }

//        } else if (!aggregate.getClass().equals(method.getClass())) {
//            throw new IllegalAggregateDefinitionException("Unable to find a " + cmd.getClass().getName() + " command handler for " + aggregate.getClass().getName());
//        }

        return execute(aggregate, executable, cmd);
    }

    @Override
    public void executeAggregateStateHandler(Object aggregate, Object event) {
        Objects.requireNonNull(aggregate, "Cannot apply against a null aggregate");
        Objects.requireNonNull(event, "Cannot apply a null event");
        Executable executable = stateHandlers.get(event.getClass());

        if (executable == null) {
            throw new CommandHandlerNotFoundException("Unable to find an aggregate state handler for " + event.getClass().getName());
        }

        execute(aggregate, executable, event);
    }

    @Override
    public void executeDomainEventHandler(Object event) {
        executeEventHandler(event, domainEventHandlers);
    }

    @Override
    public void executeStreamEventHandler(Object event) {
        executeEventHandler(event, streamEventHandlers);
    }

    public void executeEventHandler(Object event, Map<Class<?>, List<EventHandlerMethod>> map) {
        Objects.requireNonNull(event, "Cannot handle a null event");
        List<EventHandlerMethod> executables = map.get(event.getClass());

        if (executables == null || executables.isEmpty()) {
            return;
        }

        for (EventHandlerMethod method : executables) {
            method.execute(event, dependencyRegister);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O executeQueryHandler(String key, I input, Class<O> classOfOutput) {
        QueryHandlerMethod<I, O> queryHandlerMethod = (QueryHandlerMethod<I, O>) queryHandlers.get(key);

        if (queryHandlerMethod == null) {
            throw new QueryHandlerNotFoundException("A query handler for key " + key + " could not be found");
        }

        return queryHandlerMethod.execute(input, dependencyRegister);
    }

    @SuppressWarnings("unchecked")
    private <T> T execute(T object, Executable executable, Object primaryParam) {
        Parameter[] parameters = executable.getParameters();
        Object[] parameterObjs = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType().equals(primaryParam.getClass())) {
                parameterObjs[i] = primaryParam;
            } else {
                Object dependency = dependencyRegister.getDependency(parameter.getType());

                if (dependency == null) {
                    throw new UnsatisfiedDependencyException("Unsatisfied dependency for " + executable.getClass() + "(.." + parameter.getType().getName() + "..)");
                }

                parameterObjs[i] = dependency;
            }
        }

        try {
            if (executable instanceof Method) {
                if (object == null) {
                    throw new AggregateNotFoundException("Attempted to send a non-initial-state command when the aggregate did not exist");
                }

                ((Method) executable).invoke(object, parameterObjs);
                return object;
            } else if (executable instanceof Constructor) {
                if (object != null) {
                    throw new AggregateNotFoundException("Attempted to send an initial-state command when the aggregate already exists");
                }

                return ((Constructor<T>) executable).newInstance(parameterObjs);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new IllegalDomainDefinitionException(ex);
        }

        return null;
    }
}
