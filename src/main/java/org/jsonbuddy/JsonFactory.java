package org.jsonbuddy;


public abstract class JsonFactory {
    public static JsonObjectFactory jsonObject() {
        return new JsonObjectFactory();
    }

    public static JsonArrayFactory jsonArray() {
        return new JsonArrayFactory();
    }

    public abstract JsonNode create();

}
