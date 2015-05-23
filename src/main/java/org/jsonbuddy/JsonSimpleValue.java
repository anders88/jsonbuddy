package org.jsonbuddy;

import org.jsonbuddy.factory.JsonSimpleValueFactory;

public class JsonSimpleValue {
    private final String value;

    public JsonSimpleValue(JsonSimpleValueFactory jsonSimpleValueFactory) {
        this.value = jsonSimpleValueFactory.getValue();
    }

    public String value() {
        return value;
    }
}
