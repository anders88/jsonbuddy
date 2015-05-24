package org.jsonbuddy;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayFactory extends JsonFactory {
    List<JsonFactory> nodes = new ArrayList<>();

    JsonArrayFactory() {
    }

    @Override
    public JsonArray create() {
        return new JsonArray(this);
    }

    public JsonArrayFactory add(String value) {
        return add(JsonSimpleValueFactory.text(value));
    }

    public JsonArrayFactory add(JsonFactory value) {
        nodes.add(value);
        return this;
    }


}
