package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonBooleanValue extends JsonSimpleValue {
    private final boolean value;

    JsonBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Boolean.toString(value);
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }
}
