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
}
