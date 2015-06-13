package org.jsonbuddy;

import java.util.HashMap;
import java.util.Map;

public class JsonObjectFactory extends JsonFactory {
    final Map<String,JsonFactory> values = new HashMap<>();
    private final JsonObject jsonObject = new JsonObject();

    public JsonObjectFactory() {

    }

    @Override
    public JsonObject create() {
        return jsonObject;
    }

    public JsonObjectFactory withValue(String key, JsonFactory text) {
        jsonObject.withValue(key,text.create());
        return this;
    }




}
