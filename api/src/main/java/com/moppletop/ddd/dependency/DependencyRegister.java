package com.moppletop.ddd.dependency;

/**
 * A dependency register, keeps a mapping of dependencies which can be injected into Command/AggregateState/Event handlers
 */
public interface DependencyRegister {

    /**
     * @param classOfT The class of type T
     * @param <T> The type of the dependency
     * @return The dependency mapped to the class provider, otherwise null
     */
    <T> T getDependency(Class<T> classOfT);

}
