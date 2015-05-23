package org.jsonbuddy;

public class JsonSimpleValue extends JsonNode {
    private final String value;

    public JsonSimpleValue(JsonSimpleValueFactory jsonSimpleValueFactory) {
        this.value = jsonSimpleValueFactory.getValue();
    }

    public String value() {
        return value;
    }
}
