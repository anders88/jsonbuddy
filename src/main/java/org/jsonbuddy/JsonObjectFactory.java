package org.jsonbuddy;

import java.util.HashMap;
import java.util.Map;

public class JsonObjectFactory extends JsonFactory {
    final Map<String,JsonFactory> values = new HashMap<>();

    JsonObjectFactory() {

    }

    @Override
    public JsonObject create() {
        return new JsonObject(this);
    }

    public JsonObjectFactory withValue(String key, JsonSimpleValueFactory text) {
        values.put(key,text);
        return this;
    }



}
