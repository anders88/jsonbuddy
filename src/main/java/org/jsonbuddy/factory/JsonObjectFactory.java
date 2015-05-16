package org.jsonbuddy.factory;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.factory.JsonFactory;

public class JsonObjectFactory {
    public JsonObjectFactory(JsonFactory jsonFactory) {

    }

    public JsonObject create() {
        return new JsonObject(this);
    }
}
