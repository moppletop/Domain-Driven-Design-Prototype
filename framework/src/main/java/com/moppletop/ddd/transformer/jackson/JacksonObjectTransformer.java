package com.moppletop.ddd.transformer.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moppletop.ddd.transformer.ObjectTransformer;

import java.util.Map;

/**
 * A Jackson implementation of the object transformer
 */
public class JacksonObjectTransformer implements ObjectTransformer {

    private final ObjectMapper objectMapper;

    public JacksonObjectTransformer() {
        this.objectMapper = new ObjectMapper();

        objectMapper
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.setVisibility(objectMapper.getVisibilityChecker()
                .withCreatorVisibility(Visibility.ANY)
                .withFieldVisibility(Visibility.ANY)
                .withIsGetterVisibility(Visibility.NONE)
                .withGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE));

        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String serialise(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Override
    public <T> T deserialise(String obj, Class<T> classOfT) throws Exception {
        return objectMapper.readValue(obj, classOfT);
    }
}
