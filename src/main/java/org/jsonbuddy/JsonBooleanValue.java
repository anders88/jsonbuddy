package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.Objects;

public class JsonBooleanValue extends JsonValue {
    private final boolean value;

    public JsonBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Boolean.toString(value);
    }

    @Override
    public Object javaObjectValue() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    public boolean boolValue() {
        return value;
    }

    @Override
    public JsonBooleanValue deepClone() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonBooleanValue)) return false;
        JsonBooleanValue that = (JsonBooleanValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
