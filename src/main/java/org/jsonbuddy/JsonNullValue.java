package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonNullValue extends JsonSimpleValue {
    JsonNullValue() {
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("null");
    }
}
