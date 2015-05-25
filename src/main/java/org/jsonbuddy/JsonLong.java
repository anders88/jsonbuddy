package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonLong extends JsonSimpleValue {

    private final long value;

    JsonLong(long value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Long.toString(value);
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    public long longValue() {
        return value;
    }
}
