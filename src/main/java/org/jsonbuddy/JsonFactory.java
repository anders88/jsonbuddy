package org.jsonbuddy;

public class JsonFactory {
    public static JsonObject jsonObject() {
        return new JsonObject();
    }

    public static JsonArray jsonArray() {
        return new JsonArray();
    }

    public static JsonTextValue jsonText(String text) {
        return new JsonTextValue(text);
    }

    public static JsonLong jsonLong(long number) {
        return new JsonLong(number);
    }

    public static JsonDouble jsonDouble(double number) {
        return new JsonDouble(number);
    }

    public static JsonBooleanValue jsonTrue() {
        return new JsonBooleanValue(true);
    }

    public static JsonBooleanValue jsonFalse() {
        return new JsonBooleanValue(false);
    }

    public static JsonBooleanValue jsonBoolean(boolean value) {
        return new JsonBooleanValue(value);
    }
}
