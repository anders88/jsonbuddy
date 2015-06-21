package org.jsonbuddy;

public abstract class JsonSimpleValue extends JsonNode {

    public abstract String stringValue();

    @Override
    public String textValue() {
        return stringValue();
    }

    public abstract Object javaObjectValue();
}
