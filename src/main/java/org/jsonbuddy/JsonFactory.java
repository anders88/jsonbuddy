package org.jsonbuddy;

import java.time.Instant;

public class JsonFactory {
    public static JsonObject jsonObject() {
        return new JsonObject();
    }

    public static JsonArray jsonArray() {
        return new JsonArray();
    }

    public static JsonString jsonText(String text) {
        return new JsonString(text);
    }

    public static JsonLong jsonLong(long number) {
        return new JsonLong(number);
    }

    public static JsonDouble jsonDouble(double number) {
        return new JsonDouble(number);
    }

    public static JsonBoolean jsonTrue() {
        return new JsonBoolean(true);
    }

    public static JsonBoolean jsonFalse() {
        return new JsonBoolean(false);
    }

    public static JsonBoolean jsonBoolean(boolean value) {
        return new JsonBoolean(value);
    }

    public static JsonInstantValue jsonInstance(Instant instant) {
        return new JsonInstantValue(instant);
    }
}
