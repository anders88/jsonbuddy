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
}
