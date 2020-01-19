package com.moppletop.ddd.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on classes with {@link StreamEventHandler} methods to denote the processing group name/how many threads
 * should be created to consumer the events
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessingGroup {

    /**
     * The maximum number of threads allowed per processing group
     */
    int MAX_THREADS = 25;

    String name();

    int threads() default 1;

}
