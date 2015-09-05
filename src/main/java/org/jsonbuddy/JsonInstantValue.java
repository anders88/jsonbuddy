package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class JsonInstantValue extends JsonSimpleValue {
    private final Instant value;

    public JsonInstantValue(Instant instant) {
        this.value = instant;
    }

    @Override
    public String stringValue() {
        return value.toString();
    }

    @Override
    public Object javaObjectValue() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    @Override
    public JsonInstantValue deepClone() {
        return this;
    }

    public Instant instantValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonInstantValue)) return false;
        JsonInstantValue that = (JsonInstantValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
