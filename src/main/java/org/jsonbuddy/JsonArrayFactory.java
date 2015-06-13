package org.jsonbuddy;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayFactory extends JsonFactory {
    private JsonArray jsonArray = new JsonArray();

    JsonArrayFactory() {
    }

    @Override
    public JsonArray create() {
        return jsonArray;
    }

    public JsonArrayFactory add(String value) {
        jsonArray.add(JsonSimpleValueFactory.text(value).create());
        return this;
    }

    public JsonArrayFactory add(JsonFactory value) {
        jsonArray.add(value.create());
        return this;
    }


}
