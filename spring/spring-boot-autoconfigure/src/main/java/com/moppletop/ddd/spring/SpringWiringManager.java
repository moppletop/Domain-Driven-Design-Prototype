package com.moppletop.ddd.spring;

import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.event.EventStreamer;
import com.moppletop.ddd.wiring.ExplicitWiringManager;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Once the Application Context has been created, beans are scanned for any of the "Wired" annotations, if present,
 * they will automatically be registered with the framework
 *
 * @see WiredAggregate
 * @see WiredQueryHandlers
 * @see WiredEventHandlers
 */
public class SpringWiringManager extends ExplicitWiringManager implements ApplicationListener<ContextRefreshedEvent> {

    // Autowire this since it won't be available on construction
    @Autowired
    private EventStreamer eventStreamer;

    public SpringWiringManager(DependencyRegister dependencyRegister) {
        super(dependencyRegister);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        event.getApplicationContext().getBeansWithAnnotation(WiredEventHandlers.class).values().forEach(bean -> {
            // In case the bean is a proxy, we need to get the actual object
            // For example if @Transactional from JPA is used
            if (AopUtils.isAopProxy(bean)) {
                bean = AopProxyUtils.getSingletonTarget(bean);
            }

            WiredEventHandlers annotation = bean.getClass().getAnnotation(WiredEventHandlers.class);

            if (annotation.domainEvents()) {
                registerDomainEventHandlers(bean);
            }

            if (annotation.streamEvents()) {
                registerStreamEventHandlers(bean, eventStreamer);
            }
        });

        event.getApplicationContext().getBeansWithAnnotation(WiredQueryHandlers.class).values().forEach(this::registerQueryHandlers);
        event.getApplicationContext().getBeansWithAnnotation(WiredAggregate.class).values().forEach(bean -> registerAggregate(bean.getClass()));
    }

}
