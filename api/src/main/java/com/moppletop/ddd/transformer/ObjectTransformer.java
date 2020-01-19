package com.moppletop.ddd.transformer;

/**
 * Used for object serialisation for events and aggregate state
 */
public interface ObjectTransformer {

    /**
     * @param obj The object to serialise
     * @return The serialised object
     * @throws Exception Any exception thrown during the serialisation would be thrown up the stack to here
     */
    String serialise(Object obj) throws Exception;

    /**
     * @param obj The serialised object to deserialise
     * @param classOfT The class to deserialise into
     * @param <T> The type of the class to deserialise into
     * @return The deserialised object
     * @throws Exception Any exception thrown during the deserialisation would be thrown up the stack to here
     */
    <T> T deserialise(String obj, Class<T> classOfT) throws Exception;

}
