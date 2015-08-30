package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class JsonInstantValue extends JsonSimpleValue {
    private Instant value;

    public JsonInstantValue(Instant instant) {
        this.value = instant;
    }

    @Override
    public String stringValue() {
        //DateTimeFormatter.ofPattern()
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
}
