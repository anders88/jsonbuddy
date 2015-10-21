package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonValue;

import java.io.PrintWriter;
import java.util.Objects;

public class JsonNumber extends JsonValue {
    final private Number value;


    public JsonNumber(Number value) {
        if (value == null) {
            throw new NullPointerException("Use JsonNull with null");
        }
        this.value = value;
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
    public JsonNode deepClone() {
        return this;
    }

    public long longValue() {
        return value.longValue();
    }

    public int intValue() {
        return value.intValue();
    }

    public byte byteValue() {
        return value.byteValue();
    }

    public short shortValue() {
        return value.shortValue();
    }

    public float floatValue() {
        return value.floatValue();
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonNumber)) return false;
        JsonNumber jsonLong = (JsonNumber) o;
        return Objects.equals(value, jsonLong.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
