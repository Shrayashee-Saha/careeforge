package com.aizen.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Application-wide Jackson {@link ObjectMapper} configuration, shared by
 * {@code JsonService} (resume import/export) and {@code ApiService}
 * (Gemini request/response payloads). Kept as a single static holder so
 * every part of the app serializes dates and unknown fields the same way.
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = buildMapper();

    private JsonUtil() {
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
