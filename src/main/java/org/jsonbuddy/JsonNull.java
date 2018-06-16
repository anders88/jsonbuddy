package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonNull extends JsonValue {
    public JsonNull() {
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Object javaObjectValue() {
        return null;
    }

    @Override
    public void toJson(PrintWriter printWriter, String currentIntentation, String indentationAmount) {
        printWriter.append("null");
    }

    @Override
    public JsonNull deepClone() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonNull);
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
