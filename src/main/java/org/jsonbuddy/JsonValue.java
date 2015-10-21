package org.jsonbuddy;

public abstract class JsonValue extends JsonNode {

    public abstract String stringValue();

    @Override
    public String textValue() {
        return stringValue();
    }

    public abstract Object javaObjectValue();
}
