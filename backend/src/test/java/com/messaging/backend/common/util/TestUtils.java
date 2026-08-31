package com.messaging.backend.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generic helper methods for testing context.
 * 
 * Purpose:
 * Streamlines repetitive test-level logic such as reading stub files or mapping JSON.
 * 
 * Usage:
 * Call statically during test execution. 
 * Note: Assumes a configured ObjectMapper is provided via parameters or Spring context if advanced mapping is needed,
 * though a generic mapper is instantiated for standard operations.
 * 
 * Extension points:
 * Extensible for advanced IO operations, reflection helpers, or header generation.
 */
public final class TestUtils {

    // Simple mapper for basic parsing outside of Spring Context tests.
    // Registering modules handles Java 8 time elements elegantly.
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private TestUtils() {
        // Prevent instantiation
    }

    /**
     * Reads a resource from the test classpath into a String.
     * 
     * @param path the resource path
     * @return the file content
     * @throws IOException if the file is unreadable or missing
     */
    public static String readResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Converts an object to its JSON string representation.
     * 
     * @param object the object to convert
     * @return JSON string
     * @throws JsonProcessingException if mapping fails
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    /**
     * Parses a JSON string into an object.
     * 
     * @param json the JSON string
     * @param clazz the target class type
     * @param <T> the generic type
     * @return mapped object
     * @throws JsonProcessingException if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }
}
