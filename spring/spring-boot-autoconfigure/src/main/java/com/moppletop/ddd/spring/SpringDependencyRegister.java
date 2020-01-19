package com.moppletop.ddd.spring;

import com.moppletop.ddd.dependeny.ExplicitDependencyRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * A Spring-hook for the dependency register, if a dependency is not registered explicitly, then the default application
 * context is checked to see if an applicable bean is defined
 */
@RequiredArgsConstructor
public class SpringDependencyRegister extends ExplicitDependencyRegister {

    private final ApplicationContext context;

    @Override
    public <T> T getDependency(Class<T> classOfT) {
        T explicit = super.getDependency(classOfT);

        if (explicit != null) {
            return explicit;
        }

        try {
            return context.getBean(classOfT);
        } catch (NoSuchBeanDefinitionException ex) {
            return null;
        }
    }
}
