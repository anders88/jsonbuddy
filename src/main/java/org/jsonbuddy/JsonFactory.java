package org.jsonbuddy;


public abstract class JsonFactory {
    public static JsonObjectFactory jsonObject() {
        return new JsonObjectFactory();
    }

    public abstract JsonNode create();

}
