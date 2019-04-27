package edu.hm.cs.fwp.cloud.common.test;

import org.junit.jupiter.api.Assertions;

import javax.json.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class JsonpAssertions {

    public static void assertIsPresent(JsonObject underTest, String jsonPointer) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        Assertions.assertTrue(pointer.containsValue(underTest), String.format("Expected JSON object to contain value at [%s]", jsonPointer));
    }

    public static void assertIsUuidIfPresent(JsonObject underTest, String jsonPointer) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        if (pointer.containsValue(underTest)) {
            String text = asString(pointer, underTest);
            try {
                UUID uuid = UUID.fromString(text);
            } catch (IllegalArgumentException ex) {
                Assertions.fail(String.format("Expected value [%s] at [%s] to be convertible to UUID", text, jsonPointer));
            }
        }
    }

    public static void assertIsUuid(JsonObject underTest, String jsonPointer) {
        assertIsPresent(underTest, jsonPointer);
        assertIsUuidIfPresent(underTest, jsonPointer);
    }

    public static void assertIsLocalDateTimeIfPresent(JsonObject underTest, String jsonPointer) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        if (pointer.containsValue(underTest)) {
            String text = asString(pointer, underTest);
            try {
                LocalDateTime dateTime = LocalDateTime.parse(text);
            } catch (DateTimeParseException ex) {
                Assertions.fail(String.format("Expected value [%s] at [%s] to be convertible to LocalDateTime", text, jsonPointer));
            }
        }
    }

    public static void assertIsLocalDateTime(JsonObject underTest, String jsonPointer) {
        assertIsPresent(underTest, jsonPointer);
        assertIsLocalDateTimeIfPresent(underTest, jsonPointer);
    }

    public static void assertNotEmptyIfPresent(JsonObject underTest, String jsonPointer) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        if (pointer.containsValue(underTest)) {
            JsonValue value = pointer.getValue(underTest);
            if (JsonValue.ValueType.STRING.equals(value.getValueType())) {
                String text = value.toString().replaceAll("\"", "");
                Assertions.assertFalse(text.isEmpty(), String.format("Expected value at [%s] to be not empty", jsonPointer));
            } else if (JsonValue.ValueType.ARRAY.equals(value.getValueType())) {
                JsonArray array = value.asJsonArray();
                Assertions.assertFalse(array.isEmpty(), String.format("Expected value at [%s] to be not empty", jsonPointer));
            }
        }
    }

    public static void assertNotEmpty(JsonObject underTest, String jsonPointer) {
        assertIsPresent(underTest, jsonPointer);
        assertNotEmptyIfPresent(underTest, jsonPointer);
    }

    public static void assertNotEqualsIfPresent(JsonObject underTest, String jsonPointer, String expected) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        if (pointer.containsValue(underTest)) {
            String text = asString(pointer, underTest);
            Assertions.assertTrue(!expected.equals(text), String.format("Expected value [%s] at [%s] not to be equal to [%s]", text, jsonPointer, expected));
        }
    }

    public static void assertNotEquals(JsonObject underTest, String jsonPointer, String expected) {
        assertIsPresent(underTest, jsonPointer);
        assertNotEqualsIfPresent(underTest, jsonPointer, expected);
    }

    public static void assertEqualsIfPresent(JsonObject underTest, String jsonPointer, String expected) {
        JsonPointer pointer = Json.createPointer(jsonPointer);
        if (pointer.containsValue(underTest)) {
            String text = asString(pointer, underTest);
            Assertions.assertEquals(expected, text, String.format("Expected value at [%s] not to be equal", jsonPointer, expected));
        }
    }

    public static void assertEquals(JsonObject underTest, String jsonPointer, String expected) {
        assertIsPresent(underTest, jsonPointer);
        assertEqualsIfPresent(underTest, jsonPointer, expected);
    }

    private static final String asString(JsonPointer pointer, JsonObject object) {
        String result = pointer.getValue(object).toString();
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
