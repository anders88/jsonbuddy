package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonSimpleValue extends JsonNode {
    private final String value;

    public JsonSimpleValue(JsonSimpleValueFactory jsonSimpleValueFactory) {
        this.value = jsonSimpleValueFactory.getValue();
    }

    public String value() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("\"");
        printWriter.append(value);
        printWriter.append(("\""));
    }
}
