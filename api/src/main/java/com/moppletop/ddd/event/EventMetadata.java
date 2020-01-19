package com.moppletop.ddd.event;

import java.util.*;

/**
 * An immutable map representing the metadata of an event, for example, correlation-id, sending-resource, sending-ip etc...
 */
public class EventMetadata implements Map<String, Object> {

    private static final String UNSUPPORTED_MODIFICATION = "Metadata is immutable.";
    private static final EventMetadata EMPTY = new EventMetadata();

    public static EventMetadata empty() {
        return EMPTY;
    }

    public static EventMetadata from(Map<String, ?> map) {
        return new EventMetadata(map);
    }

    private final Map<String, Object> internalMap;

    private EventMetadata() {
        this.internalMap = Collections.emptyMap();
    }

    private EventMetadata(Map<String, ?> map) {
        this.internalMap = new HashMap<>(map);
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return internalMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MODIFICATION);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(UNSUPPORTED_MODIFICATION);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException(UNSUPPORTED_MODIFICATION);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_MODIFICATION);
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(internalMap.keySet());
    }

    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(internalMap.values());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(internalMap.entrySet());
    }

    @Override
    public String toString() {
        return internalMap.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventMetadata) {
            return internalMap.equals(obj);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return internalMap.hashCode();
    }
}
