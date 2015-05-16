package org.jsonbuddy.factory;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.factory.JsonFactory;

public class JsonObjectFactory extends JsonFactory {
    JsonObjectFactory() {

    }

    public JsonObject create() {
        return new JsonObject(this);
    }
}
