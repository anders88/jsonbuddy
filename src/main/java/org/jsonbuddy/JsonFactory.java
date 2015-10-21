package org.jsonbuddy;

import org.jsonbuddy.pojo.JsonNumber;

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

    public static JsonNumber jsonNumber(Number number) {
        return new JsonNumber(number);
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

    public static JsonString jsonInstance(Instant instant) {
        return new JsonString(instant.toString());
    }
}
