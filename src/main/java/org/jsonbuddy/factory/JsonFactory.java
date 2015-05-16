package org.jsonbuddy.factory;


public abstract class JsonFactory {
    public static JsonObjectFactory jsonObject() {
        return new JsonObjectFactory();
    }

}
