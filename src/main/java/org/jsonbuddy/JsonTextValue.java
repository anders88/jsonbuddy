package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonTextValue extends JsonSimpleValue {
    private final String value;

    JsonTextValue(String value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("\"");
        printWriter.append(value);
        printWriter.append(("\""));
    }
}
