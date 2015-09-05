package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonNullValue extends JsonSimpleValue {
    public JsonNullValue() {
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
    public void toJson(PrintWriter printWriter) {
        printWriter.append("null");
    }

    @Override
    public JsonNullValue deepClone() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonNullValue);
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
