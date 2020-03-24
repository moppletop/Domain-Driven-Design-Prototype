package com.moppletop.ddd.dependeny;

import com.moppletop.ddd.database.ConnectionProvider;
import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.database.Transaction;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A simple DependencyRegister where dependencies are defined explicitly (shocking right?).
 */
public class ExplicitDependencyRegister implements DependencyRegister {

    private final Map<Class<?>, Object> dependencies;

    public ExplicitDependencyRegister() {
        this.dependencies = new HashMap<>();

        registerDefaultDependencies();
    }

    protected void registerDefaultDependencies() {
        registerDependency(new TypeSafeSupplier<>(ConnectionProvider.class, () -> Transaction.inTransaction() ? Transaction.get() : Transaction.statelessTransaction()));
    }

    /**
     * Registers a dependency. Note for dependencies that are state dependant, like the current transaction or timestamp of the event
     * you'll need to register it as a TypeSafeSupplier, the get() will be invoked when the {@link DependencyRegister#getDependency(Class)} is called
     * Due to how generic types are implemented a regular {@link Supplier} will throw a {@link IllegalArgumentException}, use a TypeSafeSupplier instead
     *
     * @param dependency the dependency (or supplier of the dependency) to register
     */
    public void registerDependency(Object dependency) {
        Objects.requireNonNull(dependency, "You cannot register a null dependency");

        if (dependency instanceof Supplier) {
            throw new IllegalArgumentException("Suppliers cannot be used for dependencies. For type-safety when evaluating, you must use ExplicitDependencyRegistrar.TypeSafeSupplier!");
        }

        Class<?> clazz = dependency.getClass();

        if (dependency instanceof TypeSafeSupplier) {
            TypeSafeSupplier<?> typeSafeSupplier = (TypeSafeSupplier<?>) dependency;
            clazz = typeSafeSupplier.getType();
            dependency = typeSafeSupplier;
        }

        dependencies.put(clazz, dependency);
    }

    @Override
    public <T> T getDependency(Class<T> classOfT) {
        Object dependency = dependencies.get(classOfT);

        if (dependency == null) {
            return null;
        }

        if (dependency instanceof TypeSafeSupplier) {
            dependency = ((TypeSafeSupplier<?>) dependency).getSupplier().get();
        }

        return classOfT.cast(dependency);
    }

    @Value
    public static class TypeSafeSupplier<T> {

        Class<T> type;
        Supplier<T> supplier;

    }
}
