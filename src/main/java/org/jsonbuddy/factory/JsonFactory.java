package org.jsonbuddy.factory;


public class JsonFactory {
    public static JsonFactory instance() {
        return new JsonFactory();
    }

    private JsonFactory() {}

    public JsonObjectFactory jsonObject() {
        return new JsonObjectFactory(this);
    }

}
